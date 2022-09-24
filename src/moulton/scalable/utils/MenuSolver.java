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
	/** A set of extended variables only usable by some expressions. */
	protected static final String[] extended = {"CENTERX", "CENTERY", "WIDTH", "HEIGHT"};
	
	public MenuSolver() {
		solve = new ExpressionSolver(
				// These are the basic variables that all expressions can use
				new String[] {"centerx", "centery", "width", "height", "pi", "e"},
				new double[] {0, 0, 0, 0, 0, 0}
		);
	}

	/**
	 * Update the values for the given container width and container height. This should
	 * be used before the solver is used to evaluate anything.
	 * @param contWidth the container width (in pixels)
	 * @param contHeight the container height (in pixels)
	 */
	public void updateValues(double contWidth, double contHeight) {
		// Set the menu values and keep any others unchanged
		double[] newValues = new double[this.values.length];
		newValues[0] = contWidth / 2;
		newValues[1] = contHeight / 2;
		newValues[2] = contWidth;
		newValues[3] = contHeight;
		for (int i = 4; i < values.length; i++)
			newValues[i] = values[i];
		this.setValues(newValues);
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
	
}