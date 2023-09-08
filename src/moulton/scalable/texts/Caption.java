package moulton.scalable.texts;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import moulton.scalable.containers.Panel;
import moulton.scalable.utils.MenuComponent;
import moulton.scalable.utils.MenuSolver.Expression;

/**
 * A menu component to display text on a panel.
 * @author Matthew Moulton
 */
public class Caption extends MenuComponent{
	/**The text to be displayed for this caption.
	 * @see #setText(String)
	 * @see #getText()*/
	protected String text;
	/**The font of the text to be rendered*/
	protected Font font;
	/**The color of the text to be drawn.
	 * @see #getColor()
	 * @see #setColor(Color)*/
	protected Color textColor = Color.BLACK;
	/**The alignment of the text to be rendered*/
	protected Alignment alignment;
	/**The text will center and draw from x to x+width if specified and alignment is center*/
	protected Expression centerWidth = null;
	/**Whether or not to center the text on the y-axis while rendering.
	 * @see #getYCentered()
	 * @see #setYCentered(boolean)*/
	protected boolean yCentered = true;
	
	/**
	 * @param text the text to display in the menu
	 * @param parent the panel that this caption will reside upon
	 * @param x the x value of the text
	 * @param y the y-value of the center of the text
	 * @param font the font to be used in rendering
	 * @param alignment left, center, or right at the x-value
	 */
	public Caption(String text, Panel parent, String x, String y, Font font, Alignment alignment) {
		super(parent,x,y);
		this.text = text;
		this.font = font;
		this.alignment = alignment;
	}
	/**
	 * @param text the text to display in the menu
	 * @param parent the panel that this caption will reside upon
	 * @param x the x value of the text
	 * @param y the y-value of the center of the text
	 * @param font the font to be used in rendering
	 * @param width the text will center and draw from x to x+width
	 */
	public Caption(String text, Panel parent, String x, String y, Font font, String width) {
		super(parent,x,y);
		this.text = text;
		this.font = font;
		this.centerWidth = solve.parse(width, true, false);
		this.alignment = Alignment.CENTER_ALIGNMENT;
	}
	/**
	 * @param text the text to display in the menu
	 * @param parent the panel that this caption will reside upon
	 * @param x the integer x coordinate this button should appear on its panel
	 * @param y the integer y coordinate this button should appear on its panel
	 * @param font the font to be used in rendering
	 * @param alignment left, center, or right at the x-value
	 */
	public Caption(String text, Panel parent, int x, int y, Font font, Alignment alignment) {
		super(parent, x, y);
		this.text = text;
		this.font = font;
		this.alignment = alignment;
	}

	@Override
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		//count how many enters there
		String [] texts = text.split("\n");
		
		//draw the text here
		if(textResize())
			g.setFont(new Font(font.getFontName(), font.getStyle(),
					getTextVertResize(font.getSize())));
		else
			g.setFont(font);
		g.setColor(textColor);
		FontMetrics fm = g.getFontMetrics();
		int fontWidth = 0;
		int fontHeight = fm.getHeight();
		
		solve.updateValues(ww, hh);
		int x, y;
		if(getGridLocation()==null){
			x = xx + solve.eval(this.x);
			y = yy + solve.eval(this.y);
		}else{
			x = xx;
			y = yy + hh/2;
		}
		//center by texts length
		if(yCentered)
			y -= (texts.length*fontHeight)/2 - fontHeight/2 - 2*fm.getDescent();
		
		switch(alignment){
		case LEFT_ALIGNMENT:
			for(int i=0; i<texts.length; i++)
				g.drawString(texts[i], x, y+(i*fontHeight));
			break;
		case CENTER_ALIGNMENT:
			if(this.centerWidth != null) {
				//check for x2 variant
				if (centerWidth.prefaced) {
					int x2 = solve.eval(centerWidth);
					ww = x2 - x;
				}else
					ww = solve.eval(centerWidth);
				//redefine center by using x and endBound as opposite edges to center by
				x += ww/2;
				//continue on to draw the string based on redefined values
			}else if (getGridLocation() != null)
				x += ww/2; // use the width provided earlier
			for(int i=0; i<texts.length; i++){
				fontWidth = fm.stringWidth(texts[i]);
				g.drawString(texts[i], x-fontWidth/2, y+(i*fontHeight));
			}
			break;
		case RIGHT_ALIGNMENT:
			//if this is in a grid, align to given width
			if(getGridLocation() != null)
				x += ww;
			
			for(int i=0; i<texts.length; i++){
				fontWidth = fm.stringWidth(texts[i]);
				g.drawString(texts[i], x-fontWidth, y+(i*fontHeight));
			}
			break;
		}
	}
	
	/**
	 * Sets the text that will be displayed when rendered
	 * @param text the text saved as {@link #text}
	 * @return this
	 */
	public Caption setText(String text){
		this.text = text;
		return this;
	}
	/**
	 * Returns the text that is printed on the panel when rendered
	 * @return {@link #text}
	 */
	public String getText(){
		return text;
	}
	/**
	 * Gets the color of the text drawn
	 * @return {@link #textColor}
	 */
	public Color getTextColor() {
		return textColor;
	}
	/**@deprecated Use {@link #getTextColor()} instead. */
	@Deprecated(since="1.14")
	public Color getColor() {
		return getTextColor();
	}
	/**
	 * Sets the color of the text drawn
	 * @param color replaces {@link #textColor}.
	 * @return this
	 */
	public Caption setTextColor(Color color) {
		this.textColor = color;
		return this;
	}
	/**@deprecated Use {@link #setTextColor(Color)} instead. */
	@Deprecated(since="1.14")
	public void setColor(Color color) {
		setTextColor(color);
	}
	
	/**
	 * Returns whether the text is centered on the y point when rendering
	 * @return {@link #yCentered}
	 */
	public boolean getYCentered(){
		return yCentered;
	}
	/**
	 * Sets whether the text rendered should be centered vertically on the y-point.
	 * @param yCentered to replace {@link Caption#yCentered}
	 * @return this
	 */
	public Caption setYCentered(boolean yCentered){
		this.yCentered = yCentered;
		return this;
	}
}
