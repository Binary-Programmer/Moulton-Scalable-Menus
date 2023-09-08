package moulton.scalable.geometrics;

import java.awt.Color;
import java.awt.Graphics;

import moulton.scalable.containers.Panel;
import moulton.scalable.utils.MenuComponent;
import moulton.scalable.utils.MenuSolver;
import moulton.scalable.utils.MenuSolver.Expression;

/**
 * A aesthetic menu component in a polygonal shape determined by {@link #xs} and {@link #ys}. The
 * fill color of the shape is determined by {@link #fillColor} and the outline color is determined
 * by {@link #outlineColor}. If not desired, each can be set to null color.
 * @author Matthew Moulton
 * @see ShapeResources
 * @see Line
 */
public class Polygon extends MenuComponent {
	/**The fill color for the shape. If fill color is null, no fill will be used.
	 * @see #setFillColor(Color)
	 * @see #getFillColor()*/
	protected Color fillColor;
	/**The outline color for the shape. Defaults to null. If null, no outline will be used.
	 * @see #setOutline(Color)
	 * @see #getOutlineColor()*/
	protected Color outlineColor = null;
	
	/**Expressions that determine the shape of the component. Should be written in a way that can
	 * be evaluated by {@link MenuSolver}. */
	protected Expression [] xs, ys;
	/**Internal variable for the number of points in the shape. Results to the minimum length of
	 * {@link #xs} and {@link #ys}.*/
	protected int numberOfPoints;
	
	/**
	 * Common constructor setup, including parsing expressions and setting fill color
	 * @param xs string expressions to define the x locations for each point in the alloted space
	 * @param ys string expressions to define the y locations for each point in the alloted space
	 * @param fillColor the color that the polygon is filled in with. The polygon can also have an
	 * outline, dictated by {@link #outlineColor} and set by {@link #setOutline(Color)}.
	 */
	protected void init(String[] xs, String ys[], Color fillColor) {
		this.fillColor = fillColor;
		if (xs.length != ys.length)
			throw new RuntimeException("The number of x expressions must be equivalent to the"
					+ " number of y expressions!");
			
		numberOfPoints = xs.length;
		this.xs = new Expression[numberOfPoints];
		this.ys = new Expression[numberOfPoints];
		for (int i = 0; i < numberOfPoints; i++) {
			this.xs[0] = solve.parse(xs[i], false, false);
			this.ys[0] = solve.parse(ys[i], false, false);
		}
	}

	/**
	 * Creates a polygon and sets in on the grid of the parent panel.
	 * @param parent the panel to be drawn on to
	 * @param x the x-coordinate on the parent grid
	 * @param y the y-coordinate on the parent grid
	 * @param xs string expressions to define the x locations for each point in the alloted space
	 * @param ys string expressions to define the y locations for each point in the alloted space
	 * @param fillColor the color that the polygon is filled in with. The polygon can also have an
	 * outline, dictated by {@link #outlineColor} and set by {@link #setOutline(Color)}.
	 */
	public Polygon(Panel parent, int x, int y, String[] xs, String ys[], Color fillColor) {
		super(parent, x, y);
		init(xs, ys, fillColor);
	}
	/**
	 * Creates a polygon to be drawn onto the parent panel
	 * @param parent the panel to be drawn on
	 * @param xs string expressions to define the x locations for each point in the alloted space
	 * @param ys string expressions to define the y locations for each point in the alloted space
	 * @param fillColor the color that the polygon is filled in with. The polygon can also have an
	 * outline, dictated by {@link #outlineColor} and set by {@link #setOutline(Color)}.
	 */
	public Polygon(Panel parent, String[] xs, String ys[], Color fillColor) {
		super(parent, xs[0], ys[0]);
		init(xs, ys, fillColor);
	}

	@Override
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		int xpoints [] = new int[xs.length];
		int ypoints [] = new int[ys.length];
		for(int i=0; i<numberOfPoints; i++){
			xpoints[i] = xx + solve.eval(xs[i]);
			ypoints[i] = yy + solve.eval(ys[i]);
		}
		if(fillColor != null) {
			g.setColor(fillColor);
			g.fillPolygon(xpoints, ypoints, numberOfPoints);
		}
		if(outlineColor != null) {
			g.setColor(outlineColor);
			g.drawPolygon(xpoints, ypoints, numberOfPoints);
		}
	}
	
	/**
	 * Sets the outline color for this polygon. If no outline color is set, or if it is set to
	 * null, no outline will be drawn.
	 * @param outlineColor the color for the {@link #outlineColor}
	 * @return this
	 */
	public Polygon setOutline(Color outlineColor) {
		this.outlineColor = outlineColor;
		return this;
	}
	/**
	 * Returns the outline color for the polygon.
	 * @return {@link #outlineColor}
	 */
	public Color getOutlineColor() {
		return outlineColor;
	}
	
	/**
	 * Sets the fill color for the polygon.
	 * @param fillColor the new color saved as {@link Polygon#fillColor}
	 * @return this
	 */
	public Polygon setFillColor(Color fillColor) {
		this.fillColor = fillColor;
		return this;
	}
	/**
	 * Returns the fill color for the polygon.
	 * @return {@link Polygon#fillColor}
	 */
	public Color getFillColor() {
		return fillColor;
	}

}