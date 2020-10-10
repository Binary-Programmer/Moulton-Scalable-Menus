package moulton.scalable.geometrics;

import java.awt.Color;
import java.awt.Graphics;

import moulton.scalable.containers.Panel;
import moulton.scalable.utils.MenuComponent;

/**
 * A aesthetic menu component in a polygonal shape determined by {@link #xs} and {@link #ys}. The fill color
 * of the shape is determined by {@link #fillColor} and the outline color is determined by {@link #outlineColor}.
 * If not desired, each can be set to null color.
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
	
	/**
	 * Expressions that determine the shape of the component. Should be written in a way that can be
	 * solved by {@link #solveString(String, int, int)} at runtime.
	 */
	protected String [] xs, ys;
	/**
	 * Internal variable for the number of points in the shape. Results to the minimum length of {@link #xs}
	 * and {@link #ys}.
	 */
	protected int numberOfPoints;

	/**
	 * Creates a polygon and sets in on the grid of the parent panel.
	 * @param parent the panel to be drawn on to
	 * @param x the x-coordinate on the parent grid
	 * @param y the y-coordinate on the parent grid
	 * @param xs string expressions to represent the x locations for each point in the alloted space
	 * @param ys string expressions to represent the y locations for each point in the alloted space
	 * @param fillColor the color that the polygon is filled in with. The polygon can also have an outline,
	 * dictated by {@link #outlineColor} and set by {@link #setOutline(Color)}.
	 */
	public Polygon(Panel parent, int x, int y, String[] xs, String ys[], Color fillColor) {
		super(parent, x, y);
		this.fillColor = fillColor;
		this.xs = xs;
		this.ys = ys;
		numberOfPoints = Math.min(xs.length, ys.length);
	}
	/**
	 * Creates a polygon to be drawn onto the parent panel
	 * @param parent the panel to be drawn on
	 * @param xs string expressions to represent the x locations for each point in the alloted space
	 * @param ys string expressions to represent the y locations for each point in the alloted space
	 * @param fillColor the color that the polygon is filled in with. The polygon can also have an outline,
	 * dictated by {@link #outlineColor} and set by {@link #setOutline(Color)}.
	 */
	public Polygon(Panel parent, String[] xs, String ys[], Color fillColor) {
		super(parent, xs[0], ys[0]);
		this.fillColor = fillColor;
		this.xs = xs;
		this.ys = ys;
		numberOfPoints = Math.min(xs.length, ys.length);
	}

	@Override
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		int xpoints [] = new int[xs.length];
		int ypoints [] = new int[ys.length];
		for(int i=0; i<numberOfPoints; i++){
			xpoints[i] = xx + solveString(xs[i], ww, hh);
			ypoints[i] = yy + solveString(ys[i], ww, hh);
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
	 * Sets the outline color for this polygon. If no outline color is set, or if it is set to null, no outline
	 * will be drawn.
	 * @param outlineColor the color for the {@link #outlineColor}
	 */
	public void setOutline(Color outlineColor) {
		this.outlineColor = outlineColor;
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
	 */
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}
	/**
	 * Returns the fill color for the polygon.
	 * @return {@link Polygon#fillColor}
	 */
	public Color getFillColor() {
		return fillColor;
	}

}