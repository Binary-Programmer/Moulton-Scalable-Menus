package moulton.scalable.geometrics;

import java.awt.Color;
import java.awt.Graphics;

import moulton.scalable.clickables.Clickable;
import moulton.scalable.clickables.RadioButton;
import moulton.scalable.containers.Panel;
import moulton.scalable.utils.MenuSolver;
import moulton.scalable.utils.MenuSolver.Expression;

/**
 * A menu component button in the shape of a polygon as opposed to a rectangle
 * @author Matthew Moulton
 */
public class PolygonalButton extends RadioButton {
	/**Expressions that determine the shape of the component. Should be written in a way that can
	 * be evaluated by {@link MenuSolver}. */
	protected Expression [] xs, ys;
	/**Internal variable for the number of points in the shape. Results to the minimum length of
	 * {@link #xs} and {@link #ys}. */
	protected int numberOfPoints;
	
	/**
	 * Common constructor setup, including parsing expressions and setting fill color
	 * @param xs string expressions to define the x locations for each point in the alloted space
	 * @param ys string expressions to define the y locations for each point in the alloted space
	 */
	protected void init(String[] xs, String[] ys) {
		if (xs.length != ys.length)
			throw new RuntimeException("The number of x expressions must be equivalent to the"
					+ " number of y expressions!");
			
		numberOfPoints = xs.length;
		this.xs = new Expression[numberOfPoints];
		this.ys = new Expression[numberOfPoints];
		for (int i = 0; i < numberOfPoints; i++) {
			this.xs[i] = solve.parse(xs[i], false, false);
			this.ys[i] = solve.parse(ys[i], false, false);
		}
	}
	
	/**
	 * Creates a new button in the shape of a polygon with arbitrary point number.
	 * @param id a unique string designed to identify this component when an event occurs
	 * @param parent the parent panel for this component.
	 * @param xs string expressions defining this component's horizontal locations.
	 * @param ys string expressions defining this component's vertical locations.
	 * @param color the color of the button when not pressed. By default, the pressed color will be
	 * one shade darker and the not enabled color will be one shade lighter.
	 */
	public PolygonalButton(String id, Panel parent, String [] xs, String [] ys, Color color) {
		super(id, parent, null, null, color);
		init(xs, ys);
	}
	/**
	 * Creates a new button in the shape of a polygon with arbitrary point number.
	 * @param id a unique string designed to identify this component when an event occurs
	 * @param parent the parent panel for this component.
	 * @param x the integer x coordinate this button should appear on its panel
	 * @param y the integer y coordinate this button should appear on its panel
	 * @param xs string expressions defining this component's horizontal locations.
	 * @param ys string expressions defining this component's vertical locations.
	 * @param color the color of the button when not pressed. By default, the pressed color will be
	 * one shade darker and the not enabled color will be one shade lighter.
	 */
	public PolygonalButton(String id, Panel parent, int x, int y,
			String [] xs, String [] ys, Color color) {
		super(id, parent, x, y, color);
		init(xs, ys);
	}

	@Override
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		int xpoints [] = new int[numberOfPoints];
		int ypoints [] = new int[numberOfPoints];
		for(int i=0; i<numberOfPoints; i++){
			xpoints[i] = xx + solve.eval(xs[i]);
			ypoints[i] = yy + solve.eval(ys[i]);
		}
		
		Color fillColor = getFillColor();
		if(fillColor != null) {
			g.setColor(getFillColor());
			g.fillPolygon(xpoints, ypoints, numberOfPoints);
		}
		if(outline){
			g.setColor(Color.BLACK);
			if(!isEnabled())
				g.setColor(Color.GRAY);
			g.drawPolygon(xpoints, ypoints, numberOfPoints);
		}
		
		if(parent != null)
			defineClickBoundary(parent.handleOffsets(xpoints, ypoints, this));
	}
	
	/**
	 * Sets whether this button is touched. Also updates {@link Clickable#outline}.
	 */
	@Override
	public void setTouched(boolean touched) {
		//if the touch state has changed
		if(touched != this.touched && colorTouched == null) {
			setOutline(!getOutline());
		}
		this.touched = touched;
	}
	
	/**
	 * If touchedColor is null, then the toggle outline effect will be used instead
	 * @param touchedColor the color to set as {@link RadioButton#colorTouched}
	 */
	public void setTouchedColor(Color touchedColor) {
		if(colorTouched==null && touchedColor != null) {
			// If the button is touched presently and the new color is not null, that means that
			// the component will show touch through the new color instead of toggling outline.
			// Therefore, the outline should go back to the original state.
			if(touched)
				setOutline(!getOutline());
			
			//set the new darker color
			colorDark = touchedColor.darker();
		}else {
			//resets to the old darker color
			colorDark = color.darker();
		}	
		this.colorTouched = touchedColor;
	}
}
