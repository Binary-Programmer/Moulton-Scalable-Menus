package moulton.scalable.utils;

/* 2021 Jan 4
 * This is a modification of my ExpressionSolver class altered to better suit
 * Moulton Scalable Menus.
 */
/**Solves algebraic expressions in double precision. Note that all whitespace is removed before
 * expression processing. There are eight operators that can be used:
 * <pre>( ) ^ r * / + -</pre> The 'r' is root. For example, "2r4" is the square root of 4. All other
 * operators perform as expected. There are also seven supported functions:
 * <pre>cos sin tan log ln max min</pre> Single argument functions, such as cos, sin, tan, log, and
 * ln, do not need parentheses, ie "cos1" = "cos(1)". Two argument functions, such as max and min,
 * operate almost like operators, taking the number before and the number after as arguments. For  
 * example, "3 max 4" = 4. Again, no parentheses are necessary unless implicit multiplication is
 * desired. In that case, use parentheses, such as "5 (2 min 1)" = 5, instead of "5 1 min 2".
 * <p> There are also 6 variables that may be used in the expression: <pre>
 * centerx centery width height pi e</pre> where the first three refer to the dimensions of the {@link MenuComponent}'s
 * container, and the last two are the mathematical constants.<p> For example, "centerx - (width/10) * lne",
 * when the width of the container is 100, will result to 40.0.
 * <p>
 * Call {@link #solveString(String)} to evaluate the expression. {@link #setVariables(String[], double[])}
 * and {@link #setValues(double[])} are also available for configuration.
 * @author Matthew Moulton
 */
public class ExpressionSolver {
	/**
	 * The variables that have meaning in the context of menu expressions. The variables provided
	 * by default are
	 * <ul>
	 * <li>centerx
	 * <li>centery
	 * <li>width>
	 * <li>height
	 * <li>pi
	 * <li>e
	 * </ul>
	 * The corresponding values are saved in {@link #values}
	 */
	protected String[] variables = {"centerx", "centery", "width", "height", "pi", "e"};
	/**The values that correspond to the {@link #variables}.*/
	protected double[] values;
	
	/**
	 * Creates an solver obj with the values of contWidth and contHeight to use as values for "width" and "height" variables,
	 * respectively. The values provided here are also used for the variables "centerx" as contWidth/2 and "centery" as contHeight/2.
	 * @param contWidth the width of the container
	 * @param contHeight the height of the container
	 */
	public ExpressionSolver(double contWidth, double contHeight){
		this.values = new double[] {
				contWidth/2, contHeight/2, contWidth, contHeight,
				3.1415926536, 2.7182818285
		};
	}
	
	/**
	 * Sets the variables and the values for use in equation solving. Neither variables nor values
	 * may be null, and the lengths of both must match.
	 * @param variables sets the variables for use
	 * @param values sets the values for use
	 * @see #setValues(double[])
	 * @see #solveString(String)
	 */
	public void setVariables(String[] variables, double[] values) {
		if(variables == null || values == null)
			throw new IllegalArgumentException("Both variables and values must be non-null!");
		if(variables.length != values.length)
			throw new IllegalArgumentException("The lengths of the variables and values arrays must match!");
		this.variables = variables;
		this.values = values;
	}
	
	/**
	 * Sets the values for the previously set values. The specified values may not be null and the
	 * array's length must match the number of variables currently set.
	 * @param values the values to use in equation solving
	 * @see #setVariables(String[], double[])
	 */
	public void setValues(double[] values) {
		if(this.values.length < this.variables.length)
		this.values = values;
	}
	
	/**
	 * Solves the given expression. Note that because of issues of circular dependencies in order of operations,
	 * the functions must receive numerical arguments instead of expressions themselves. For example, avoid
	 * evaluating expressions like <code>cossin0</code>, since sin0 is the argument for cosine. Instead, use
	 * parentheses like <code>cos(sin0)</code>, which will correctly result to 1. Additionally, <code>cospi</code>
	 * is acceptable because all variables are replaced by their values before any evaluation. 
	 * @param expression the expression to be solved
	 * @return the double result of the solved expression
	 */
	public double solveString(String expression){
		//remove all whitespace characters
		String equ = "";
		for(int i=0; i<expression.length(); i++) {
			char c = expression.charAt(i);
			if(!Character.isWhitespace(c))
				equ += c;
		}
		expression = equ;
		//replace the variables with their values
		for(int i=0; i<variables.length; i++){
			int index = expression.indexOf(variables[i]);
			while(index!=-1){
				expression = rebuildEquation(expression.substring(0, index), values[i], expression.substring(index+variables[i].length()),true);
				index = expression.indexOf(variables[i]);
			}
		}
		//perform the actual solve, returning the result
		return solve(expression);
	}
	
	private double solve(String expression){
		if(expression==null || expression.isEmpty())
			return 0;
		//while there are parentheses
		while(expression.indexOf('(')!=-1){ //P
			//the last ( and the first ) after go together
			int start = expression.lastIndexOf('(');
			int end = expression.indexOf(')', start);
			if(end == -1) //if it wasn't found, just use the end of the expression
				end = expression.length();
			
			//make the equation what it was before but insert the solved parentheses expression
			expression = rebuildEquation(expression.substring(0, start),
						 solve(expression.substring(start+1, end)),
						 //if the end is longer than the expression, just give an empty string
						 end<expression.length()?expression.substring(end+1):"", true);
		}//perform the functions
		expression = performOperation(expression, "cos", true);
		expression = performOperation(expression, "sin", true);
		expression = performOperation(expression, "tan", true);
		expression = performOperation(expression, "log", true);
		expression = performOperation(expression, "ln", true);
		expression = performOperation(expression, "max", false);
		expression = performOperation(expression, "min", false);
		
		//now order of operations for the rest
		expression = performOperation(expression, "^", false);
		expression = performOperation(expression, "r", false);
		expression = performOperation(expression, "*", false);
		expression = performOperation(expression, "/", false);
		expression = performOperation(expression, "+", false);
		expression = performOperation(expression, "-", false);
		
		//at this point, all operations should have been taken care of, so the final result can be parsed and returned.
		return Double.parseDouble(expression);
	}
	
	private String performOperation(String expression, String operation, boolean afterOnly) {
		int i = expression.indexOf(operation);
		//while there is an instance of the operation, but with two exceptions:
		while(i != -1) {
			//1. a leading -, which would just indicate a negation
			if(operation.equals("-") && i==0) {
				i = expression.indexOf(operation, i+1);
				continue;
			}
			//2. a - preceded by an E, for scientific notation (num E - exp)
			if(operation.equals("-") && (i>0 && expression.charAt(i-1)=='E')) {
				i = expression.indexOf(operation, i+1);
				continue;
			}
			//if we have the operation at index i, then we need to divide up the expression at that point,
			//remove the operation, and perform the action between the two component parts
			//try to find the number on the second end of the operation
			int b = i + operation.length(); //start the index after the operation
			boolean negPossible = true; //negation only possible if first character or after E
			for(; b<expression.length(); b++) {
				char c = expression.charAt(b);
				if(c == '-' && negPossible) {
					//pass- the negative is allowed
				}else if(c == 'E') { //for a scientific notation
					negPossible = true; //the power for the notation can be negative
					continue;
				}else if(!isNumber(c)) //if the character is not a digit or a ., then we have found the end
					break;
				negPossible = false;
			}
			double second = Double.parseDouble(expression.substring(i+operation.length(), b));
			//try to find the first half if needed
			int a = i; //the end is exclusive
			double first = 0.0;
			//some operations, notably trig ones, only have a relevant value after the operation sign
			if(!afterOnly) {
				for(a=i-1; a>-1; a--) {
					char c = expression.charAt(a);
					if(c == '-') {
						// This could be indicating the power of scientific notation, or just negation
						if(a > 0 && expression.charAt(a - 1) == 'E') {
							a--; //skip back past the E
							continue;
						}
						//negation of the first number
						break;
					}else if(!isNumber(c)) { //if the character is not a digit or a ., then we have found the end
						a++; //the first number component begins with the next character
						break;
					}
				}
				if(a<0)
					a = 0;
				first = Double.parseDouble(expression.substring((a<0)? 0:a, i));
			}else if(a<0)
				a = 0;
			
			double value = 0.0;
			switch(operation) {
			case "^":
				value = Math.pow(first, second);
				break;
			case "r":
				value = Math.pow(second, 1/first);
				break;
			case "*":
				value = first*second;
				break;
			case "/":
				value = first/second;
				break;
			case "+":
				value = first+second;
				break;
			case "-":
				value = first-second;
				break;
			case "cos":
				value = Math.cos(second);
				break;
			case "sin":
				value = Math.sin(second);
				break;
			case "tan":
				value = Math.tan(second);
				break;
			case "log":
				value = Math.log10(second);
				break;
			case "ln":
				value = Math.log(second);
				break;
			case "max":
				value = Math.max(first, second);
				break;
			case "min":
				value = Math.min(first, second);
				break;
			default:
				throw new ArithmeticException(operation+" is not a supported operation!");
			}
			//set the expression to rebuild of from the beginning to the beginning of the first number (a), the value, and from the end of the second number (b).
			//any adjacent elements to the left should be added to, unless there wasn't a left value used.
			expression = rebuildEquation(expression.substring(0, a), value, expression.substring(b), afterOnly);
			//reset for the next iteration of the loop
			i = expression.indexOf(operation);
		}
		//return what we got
		return expression;
	}
	
	/**
	 * Puts the string expression back together, if <code>multiplied</code> is true, then this method will append
	 * a * character before and/or after where necessary to make sure adjacent numbers are multiplied instead of
	 * just appended onto this value.
	 * @param bgn the first string in the expression
	 * @param number the value of the middle, will be inserted between <code>bgn</code> and <code>end</code>
	 * @param end the end of the string expression
	 * @param multiplied whether adjacent elements should be multiplied to (true), or just added to (false): <br>
	 * They would be multiplied together if there was some implicit multiplication (for example: "2(5+3)" or "5cos45") <br>
	 * They would need to be added together, <i>not</i> because the input was incorrect (for example: "3 5"), but because when the solving takes
	 * negation, it could have been intended to be a minus instead. Thus the two numbers are left without an operator between them.
	 * (for example: "5-3*-2" is treated as: "5 (-3*-2)", then "5 6", which should be "5+6")
	 * @return the expression once pieced together correctly (will look something like <code>bgn</code>...<code>number</code>...<code>end</code>)
	 */
	private String rebuildEquation(String bgn, double number, String end, boolean multiplied){
		if(multiplied) { //the value of number should be multiplied to any adjacent elements
			//if the element immediately preceding this is a number value, add on a "*", so it will calculate correctly
			if(bgn.length()>0) {
				char c = bgn.charAt(bgn.length()-1);
				if(isNumber(c))
					bgn+="*";
			}//similarly, if the element immediately proceeding is a number value, add the "*".
			if(end.length()>0) {
				char c = end.charAt(0);
				if(isNumber(c))
					end = '*' + end;
			}
		}
		//If the last character of begin is '+' or '-', we may be able to condense the edge.
		// If the first character of number is '+', then we can get rid of one symbol.
		// If '-', then we will either delete the '+', or two negatives make a positive.
		int i= bgn.length()-1;
		if(i>-1) {
			//look for a plus or minus
			char c = bgn.charAt(i);
			if(c=='-') {
				if(isNegative(number)) //two negatives make a positive
					return bgn.substring(0,i) + '+' + (-number) + end;
				else
					return bgn + number + end;
			}if(c=='+') {
				if(isNegative(number)) //remove the + since it is replaced by a -
					return bgn.substring(0, i) + number + end;
				else
					return bgn + number + end;
			}
			//No condensing was possible unfortunately. Return the values.
			//If this will not be signed (bc positive), we may need to add the plus in
			// for neighboring numbers.
			if(number >= 0 && !multiplied && !bgn.isEmpty() && isNumber(bgn.charAt(i)))
				return bgn + '+' + number + end;
			else //otherwise we can just return normally
				return bgn + number + end;
		}//the bgn string must be empty
		return number + end;
	}
	
	/**
	 * Returns whether this character could be part of a number. Also defined as whether the character is a
	 * decimal point ('.') or {@link Character#isDigit(char)}.
	 * @param testChar the character to test
	 * @return whether the test character could be part of a number
	 */
	protected static boolean isNumber(char testChar) {
		if (testChar == '.')
			return true;
		return Character.isDigit(testChar);
	}
	
	/**
	 * It may seem silly to make a method to test if a double is negative, but -0.0 is possible and is not
	 * < 0.0. Therefore, this method will determine if there is a leading negation sign.
	 * @param d the double to test
	 * @return whether the double to test is negative.
	 */
	protected static boolean isNegative(double d) {
	     return Double.doubleToRawLongBits(d) < 0;
	}
	
}
