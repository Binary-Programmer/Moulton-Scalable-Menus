package moulton.scalable.clickables;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import moulton.scalable.containers.Panel;
import moulton.scalable.utils.MenuComponent;
import moulton.scalable.containers.MenuManager;

/**
 * A menu component that is a simple rectangular button and is a subclass of {@link RadioButton}. Although it is a sublass
 * of RadioButton it is only used as a radio button when the {@link RadioButton#group} is set to a not-null value.<p>
 * If this button is registered on the {@link MenuManager}'s touch check list, then it will be responsive to mouse
 * touching. By default, upon mouse touch the outline will toggle, but if {@link RadioButton#colorTouched} is set from
 * non-null, then the fill color of the button will change instead.
 * @author Matthew Moulton
 */
public class Button extends RadioButton {
	/**The string of text displayed on the face of the button.
	 * @see #setText(String)
	 * @see #getText()*/
	protected String text;
	/**The string expressions to define the dimensions of the button on the parent panel.*/
	protected String width, height;
	/**The font of the text rendered on the button. */
	protected Font font;
	
	/**
	 * @param id a unique string designed to identify this component when an event occurs.
	 * @param text the string displayed on the button
	 * @param parent the panel that this button will reside upon
	 * @param x the x coordinate on the parent, given in menu component value format
	 * @param y the y coordinate on the parent, given in menu component value format
	 * @param width the width of the component, given in menu component value format
	 * @param height the height of the component, given in menu component value format
	 * @param font the font for the box
	 * @param color the background color for the button when editable
	 */
	public Button(String id, String text, Panel parent, String x, String y, String width, String height, Font font, Color color) {
		super(id, parent, x, y, color);
		this.width = width;
		this.height = height;
		this.text = text;
		this.font = font;
	}
	
	/**
	 * @param id a unique string designed to identify this component when an event occurs.
	 * @param text the string displayed on the button
	 * @param parent the panel that this button will reside upon
	 * @param x the integer x coordinate this button should appear on its panel
	 * @param y the integer y coordinate this button should appear on its panel
	 * @param font the font for the box
	 * @param color the background color for the button when editable
	 */
	public Button(String id, String text, Panel parent, int x, int y, Font font, Color color){
		super(id, parent, x, y, color);
		this.text = text;
		this.font = font;
	}
	
	/**
	 * Draws the button onto the graphics. If this button is in a grid, it will take all of the space alloted to it.
	 * If it is in free-draw mode, then its dimensions will be decided by the string expressions for {@link MenuComponent#x},
	 * {@link MenuComponent#y}, {@link #width}, and {@link #height}.<p>
	 * The method {@link RadioButton#getFillColor()} will be called to determine what color the body of this button
	 * will be. The outline will be in black if it should be drawn (specified by {@link Clickable#outline}), as will
	 * the text on the face of the button, unless the button is not editable.
	 */
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		int x, y, w, h;
		if(getGridLocation()==null) {
			x = xx + solveString(this.x, ww, hh);
			y = yy + solveString(this.y, ww, hh);
			// variant for input ending points instead of widths indicated by a starting question
			if (this.width.charAt(0) == '?') {
				//solve for the ending point
				int x2 = xx + solveString(this.width.substring(1), ww, hh);
				//deduce the width
				w = x2 - x;
			} else
				w = xx + solveString(this.width, ww, hh);
			
			if (this.height.charAt(0) == '?') {
				int y2 = yy + solveString(this.height.substring(1), ww, hh);
				h = y2 - y;
			} else
				h = yy + solveString(this.height, ww, hh);
		}else {
			x = xx;
			y = yy;
			w = ww;
			h = hh;
		}

		g.setColor(getFillColor());
		g.fillRect(x, y, w, h);
		defineClickBoundary(new int[] {x, x+w, x+w, x}, new int[] {y, y, y+h, y+h});
		g.setColor(editable? Color.BLACK:Color.GRAY);
		if (outline)
			g.drawRect(x, y, w - 1, h - 1);

		// draw the text
		if (text != null) {
			if(textResize())
				g.setFont(new Font(font.getFontName(), font.getStyle(), getTextVertResize(font.getSize())));
			else
				g.setFont(font);
			FontMetrics fm = g.getFontMetrics();
			int fontWidth = fm.stringWidth(text);
			String shownText = text;
			while(fontWidth > w && shownText.length()>0) {
				shownText = shownText.substring(0, shownText.length()-1); //decrease size
				fontWidth = fm.stringWidth(shownText);
			}
			int fontHeight = fm.getHeight();
			g.drawString(shownText, x + w/2 - fontWidth/2, (int) (y + h/2 + fontHeight/2.8));
		}
	}
	
	/**
	 * Sets the text that appears on the button face to the new text.
	 * @param text the String text to replace {@link #text}.
	 */
	public void setText(String text) {
		this.text = text;
	}
	/**
	 * Returns the text that the button uses on its button face in rendering
	 * @return {@link #text}
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Sets whether this button is touched. If the touched color is unset, then an outline toggle will be used
	 * to show touch. Therefore, setting the touch here may trigger the toggle.
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
	 * @param touchedColor the color to be set as {@link #colorTouched}
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