package moulton.scalable.geometrics;

import java.awt.Color;
import java.awt.Graphics;

import moulton.scalable.clickables.Clickable;
import moulton.scalable.clickables.RadioButton;
import moulton.scalable.containers.Panel;

/**
 * A menu component button in the shape of a polygon as opposed to a rectangle
 * @author Matthew Moulton
 */
public class PolygonalButton extends RadioButton{
	/**
	 * Expressions that determine the shape of the button. Should be written in a way that can be
	 * solved by {@link #solveString(String, int, int)} at runtime.
	 */
	protected String [] xs, ys;
	/**
	 * Internal variable for the number of points in the shape. Results to the minimum length of {@link #xs}
	 * and {@link #ys}.
	 */
	protected int numberOfPoints;
	
	/**
	 * Creates a new button in the shape of a polygon with arbitrary point number.
	 * @param id a unique string designed to identify this component when an event occurs
	 * @param parent the parent panel for this component.
	 * @param xs the string expressions dictating the points on the parent panel that define this component.
	 * @param ys the string expressions dictating the points on the parent panel that define this component.
	 * @param color the color of the button when not pressed. By default, the pressed color will be one shade
	 * darker and the uneditable color will be one shade lighter.
	 */
	public PolygonalButton(String id, Panel parent, String [] xs, String [] ys, Color color){
		super(id,parent,xs[0],ys[0], color);
		this.xs = xs;
		this.ys = ys;
		this.numberOfPoints = Math.min(xs.length, ys.length);
	}
	/**
	 * Creates a new button in the shape of a polygon with arbitrary point number.
	 * @param id a unique string designed to identify this component when an event occurs
	 * @param parent the parent panel for this component.
	 * @param x the integer x coordinate this button should appear on its panel
	 * @param y the integer y coordinate this button should appear on its panel
	 * @param xs the string expressions dictating the points on the parent panel that define this component.
	 * @param ys the string expressions dictating the points on the parent panel that define this component.
	 * @param color the color of the button when not pressed. By default, the pressed color will be one shade
	 * darker and the uneditable color will be one shade lighter.
	 */
	public PolygonalButton(String id, Panel parent, int x, int y, String [] xs, String [] ys, Color color){
		super(id,parent,x,y, color);
		this.xs = xs;
		this.ys = ys;
		this.numberOfPoints = Math.min(xs.length, ys.length);
	}

	@Override
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		int xpoints [] = new int[xs.length];
		int ypoints [] = new int[ys.length];
		for(int i=0; i<numberOfPoints; i++){
			xpoints[i] = xx + solveString(xs[i], ww, hh);
			ypoints[i] = yy + solveString(ys[i], ww, hh);
		}
		
		Color fillColor = getFillColor();
		if(fillColor != null) {
			g.setColor(getFillColor());
			g.fillPolygon(xpoints, ypoints, numberOfPoints);
		}
		if(outline){
			g.setColor(Color.BLACK);
			if(!enabled)
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
		if(touched != this.touched && colorTouched == null) { //if the outline effect should be used
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
			/* if the button is touched presently and the new color is not null, that means that the component will
			 * show touch through the new color instead of toggling outline. Therefore, the outline should go back
			 * to the original state.
			 */
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