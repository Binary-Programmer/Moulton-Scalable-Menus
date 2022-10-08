package moulton.scalable.geometrics;

import java.awt.Color;
import java.awt.Graphics;

import moulton.scalable.containers.Panel;
import moulton.scalable.utils.MenuComponent;
import moulton.scalable.utils.MenuSolver.Expression;

/**
 * A simple line from two points on the (x,y) plane.
 * @author Matthew Moulton
 *
 */
public class Line extends MenuComponent{
	/**The expressions to define the locations of the start and end points for the line segment.*/
	protected Expression x1,y1, x2,y2;
	/**The thickness of the line to be drawn. If null or default, the line will be 1 pixel thick.
	 * @see #setThickness(String)
	 * @see #getThickness()*/
	protected Expression thickness = null;
	/**The color of the line drawn.*/
	protected Color color;
	
	/**
	 * A simple line rendered from (x,y) to (x2,y2). The coordinates should be in the format
	 * specified by {@link MenuComponent#solveString(String, int, int)}.
	 * @param parent the panel that this text box will reside upon
	 * @param x1 the starting x of the line
	 * @param y1 the starting y of the line
	 * @param x2 the ending x of the line. If x2 is '?', then it is a ditto of x
	 * @param y2 the ending y of the line. If y2 is '?', then it is a ditto of y
	 * @param color the color of the line
	 */
	public Line(Panel parent, String x1, String y1, String x2, String y2, Color color) {
		super(parent,x1,y1);
		this.x1 = solve.parse(x1, false, false);
		this.y1 = solve.parse(y1, false, false);
		this.x2 = solve.parse(x1, true, false);
		this.y2 = solve.parse(x1, true, false);
		this.color = color;
	}
	/**
	 * A simple line rendered from (x,y) to (x2,y2). The coordinates should be in the format
	 * specified by {@link MenuComponent#solveString(String, int, int)}.
	 * @param parent the panel that this text box will reside upon
	 * @param x the integer x coordinate this line should appear on its panel
	 * @param y the integer y coordinate this line should appear on its panel
	 * @param x1 the starting x of the line
	 * @param y1 the starting y of the line
	 * @param x2 the ending x of the line. If x2 is '?', then it is a ditto of x
	 * @param y2 the ending y of the line. If y2 is '?', then it is a ditto of y
	 * @param color the color of the line
	 */
	public Line(Panel parent, int x, int y, String x1, String y1, String x2, String y2, Color color) {
		super(parent,x,y);
		this.x1 = solve.parse(x1, false, false);
		this.y1 = solve.parse(y1, false, false);
		this.x2 = solve.parse(x2, true, false);
		this.y2 = solve.parse(y2, true, false);
		this.color = color;
	}

	@Override
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		int x1, y1, x2, y2;
		
		x1 = xx + solve.eval(this.x1);
		y1 = yy + solve.eval(this.y1);
		//if x2 is '?' or y2 is '?' then it is relative to x or y respectively
		
		x2 = solve.eval(this.x2) + (this.x2.prefaced? x1 : xx);
		y2 = solve.eval(this.y2) + (this.y2.prefaced? y1 : yy);
		
		//draw color
		g.setColor(color);
				
		if(thickness != null) {
			int thick = solve.eval(thickness);
			//for the thick lines we need an angle perpendicular to the line
			//we accomplish this by reversing x and y and negating new x
			double deltaX = -(y2-y1);
			double deltaY = x2-x1;
			//then we simplify by dividing each by the sum
			double sum = Math.abs(deltaX) + Math.abs(deltaY);
			deltaX/=sum;
			deltaY/=sum;
			//to center the thickness, alternate side for each line based on whether the iterator
			//is even or odd
			for(int i=1; i<thick; i++) {
				int diffX = (int)Math.round(deltaX*(i/2+i%2));
				int diffY = (int)Math.round(deltaY*(i/2+i%2));
				if(i%2==1) { //odd
					g.drawLine(x1+diffX, y1+diffY, x2+diffX, y2+diffY);
				}else { //even
					g.drawLine(x1-diffX, y1-diffY, x2-diffX, y2-diffY);
				}
			}
		}
		g.drawLine(x1, y1, x2, y2);
	}
	
	/**
	 * Sets the thickness of the line for drawing. A null thickness will be 1 pixel thick.
	 * @param thickness the thickness that this line will draw perpendicular to the slope of its
	 * line from start and end points
	 */
	public void setThickness(String thickness){
		this.thickness = solve.parse(thickness, false, false);
	}
}
