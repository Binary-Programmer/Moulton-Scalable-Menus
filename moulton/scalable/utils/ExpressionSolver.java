package moulton.scalable.utils;

/*1 Jan 2020
 * This is a modification of my ExpressionSolver class altered to better suit
 * Moulton Scalable Menus.
 */
/**Solves algebraic expressions in double precision. There are eight operators that can be used:
 * <pre>( ) ^ r * / + -</pre> The 'r' is root. For example, "2r4" is the square root of 4. All other
 * operators perform as expected. There are also five supported functions:
 * <pre>cos sin tan log ln</pre> There are also 6 variables that may be used in the expression: <pre>
 * centerx centery width height pi e</pre> where the first three refer to the dimensions of the {@link MenuComponent}'s
 * container, and the last two are the mathematical constants.<p> For example, "centerx - (width/10) * lne",
 * when the width of the container is 100, will result to 40.0.
 * @author Matthew Moulton
 */
public class ExpressionSolver {
	private static final String[] VARIABLES = {"centerx", "centery", "width", "height", "pi", "e"};
	private double[] values;
	
	/**
	 * Creates an solver obj with the values of contWidth and contHeight to use as values for "width" and "height" variables,
	 * respectively. The values provided here are also used for the variables "centerx" as contWidth/2 and "centery" as contHeight/2.
	 * @param contWidth the width of the container
	 * @param contHeight the height of the container
	 */
	public ExpressionSolver(double contWidth, double contHeight){
		this.values = new double[]{contWidth/2, contHeight/2, contWidth, contHeight,
				3.1415926536,2.7182818285};
	}
	
	/**
	 * Solves the equation as expected.
	 * @param expression the equation to solve
	 * @return the answer in double precision
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
		for(int i=0; i<VARIABLES.length; i++){
			int index = expression.indexOf(VARIABLES[i]);
			while(index!=-1){
				expression = rebuildEquation(expression.substring(0, index), values[i], expression.substring(index+VARIABLES[i].length()),true);
				index = expression.indexOf(VARIABLES[i]);
			}
		}
		//perform the actual solve, returning the result
		try {
			return solve(expression);
		}catch(NumberFormatException nfe) {
			throw new RuntimeException("There was an error solving the expression: "+expression);
		}
	}
	
	private double solve(String expression){
		if(expression==null || expression.isEmpty())
			return 0;
		//while there are parentheses
		while(expression.indexOf('(')!=-1){ //P
			//the last ( and the first ) after go together
			int start = expression.lastIndexOf('(');
			int end = expression.indexOf(')');
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
			if(operation.equals("-") && i==0)
				break;
			//2. a - preceded by an E, for 10^(negative exponent)
			if(operation.equals("-") && i>0 && expression.charAt(i-1)=='E')
				break;
			//if we have the operation at index i, then we need to divide up the expression at that point,
			//remove the operation, and perform the action between the two component parts
			//try to find the number on the second end of the operation
			int b = i + operation.length(); //start the index after the operation
			//the - sign will count as part of the number, only the first time
			for(; b<expression.length(); b++) {
				char c = expression.charAt(b);
				if(c == '-' && b==i+operation.length()) //negation assumed only if it is the first character
					continue;
				else if(!isNumber(c)) //if the character is not a digit or a ., then we have found the end
					break;
			}
			double second = Double.parseDouble(expression.substring(i+operation.length(), b));
			//try to find the first half if needed
			int a = i; //the end is exclusive
			double first = 0.0;
			//some operations, notably trig ones, only have a relevant value after the operation sign
			if(!afterOnly) {
				for(a=i-1; a>-1; a--) {
					char c = expression.charAt(a);
					if(c == '-') //negation of the first number
						break;
					else if(!isNumber(c)) { //if the character is not a digit or a ., then we have found the end
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
		if(!multiplied){
			//work as before with adding
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
				}//otherwise
				if(number<0)
					return bgn + number + end;
				else
					return bgn + '+' + number + end;
			}//the bgn string must be empty
			return number + end;
		}else{ //the value of number should be multiplied to any adjacent elements
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
			return bgn + number + end;
		}
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
	 * It may seem silly to make a method to test if a double is negative, but -0.0 is possible and does not
	 * less than 0.0. Therefore, this method will determine if there is a leading negation sign.
	 * @param d the double to test
	 * @return whether the double to test is negative.
	 */
	protected static boolean isNegative(double d) {
	     return Double.doubleToRawLongBits(d) < 0;
	}
	
}