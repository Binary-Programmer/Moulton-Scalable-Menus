package moulton.scalable.utils;

import expression.ExpressionSolver;

/**
 * This class bundles the necessary functionality for MSM out of Expression's expression solver.
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
public class MenuSolver {
	/** The underlying expression solver object to use. */
	protected ExpressionSolver solve;
	/** The list of accepted variable names */
	protected String[] variables;
	/** The list of values for the variables */
	protected double[] values = null;
	/** A set of extended variables only usable by some expressions. */
	protected static final String[] extended = {"CENTERX", "CENTERY", "WIDTH", "HEIGHT"};
	
	public MenuSolver() {
		// These are the basic variables that all expressions can use
		variables = new String[] {"centerx", "centery", "width", "height", "pi", "e"};
		solve = new ExpressionSolver(variables);
	}

	/**
	 * Update the values for the given container width and container height. This should
	 * be used before the solver is used to evaluate anything.
	 * @param contWidth the container width (in pixels)
	 * @param contHeight the container height (in pixels)
	 */
	public void updateValues(double contWidth, double contHeight) {
		// Set the menu values and keep any others unchanged
		double[] newValues = new double[variables.length];
		newValues[0] = contWidth / 2;
		newValues[1] = contHeight / 2;
		newValues[2] = contWidth;
		newValues[3] = contHeight;
		for (int i = 4; values != null && i < values.length; i++)
			newValues[i] = values[i];
		solve.setValues(newValues);
	}
	
	public void addVariable(String name, double val) {
		
	}
	
	public void setVariable(String name, double val) {
		
	}
	
	public static class Expression implements ExpressionSolver.Expression {
		protected final ExpressionSolver.Expression expr;
		public final boolean prefaced;
		
		public Expression(ExpressionSolver.Expression expr, boolean prefaced) {
			this.expr = expr;
			this.prefaced = prefaced;
		}

		@Override
		public double getValue(ExpressionSolver s) {
			return s.eval(expr);
		}
	}
	
	public String[] extendVars() {
		if (values == null)
			throw new RuntimeException("Undefined values! Update first via updateValues(...)");
		String[] vars = new String[variables.length + extended.length];
		// Copy from the originals into the new
		for (int i = 0; i < variables.length; i++)
			vars[i] = variables[i];
		for (int i = 0; i < extended.length; i++)
			vars[variables.length + i] = extended[i];
		
		return vars;
	}
	
	/**
	 * Parses the string expression and produces an expression result.
	 * @param expr the string to parse
	 * @param prefaceAllowable whether a leading ? char is acceptable. This is often used for
	 * expressions of dimension (width or height) to indicate it ends at a given location. If
	 * this expression is a dimension, the extended variable set should not be used.
	 * @param extended whether the extended variables should be used. This should generally be
	 * true unless the expression being parsed is a dimension (width or height) in which case,
	 * the extended variable set is recursively defined.
	 * @return the Expression object result
	 */
	public Expression parse(String expr, boolean prefaceAllowable, boolean extended) {
		boolean preface = prefaceAllowable && expr.charAt(0) == '?';
		if (preface)
			expr = expr.substring(1);
		if (extended)
			solve.setVariables(extendVars());
		Expression exp = new Expression(solve.parseString(expr), preface);
		// return the variables to original
		solve.setVariables(variables);
		
		return exp;
	}
	
	public double eval(Expression expr) {
		return expr.getValue(solve);
	}
	
	public double evalExtended(Expression expr, double compWidth, double compHeight) {
		// Add in the extended variables for evaluation
		String[] extendedVars = extendVars();
		solve.setVariables(extendedVars);
		double[] vals = new double[extendedVars.length];
		for (int i = 0; i < values.length; i++)
			vals[i] = values[i];
		vals[values.length + 0] = (vals[2] - compWidth)/2;  // CENTERX = (width - compWidth)/2
		vals[values.length + 1] = (vals[3] - compHeight)/2; // CENTERY = (height - compHeight)/2
		vals[values.length + 2] = compHeight;               // WIDTH
		vals[values.length + 3] = compWidth;                // HEIGHT
		
		double ans = expr.getValue(solve);
		
		// Restore previous state
		solve.setVariables(variables);
		return ans;
	}
	
}