package moulton.scalable.clickables;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Panel;
import moulton.scalable.texts.Alignment;
import moulton.scalable.utils.MenuComponent;
import moulton.scalable.utils.MenuSolver.Expression;

/**
 * A menu component that is a simple rectangular button with optional frontal text saved as {@link
 * #text}. Button is a subclass of {@link RadioButton}, but only acts as a radio button when the
 * {@link RadioButton#group} is set to a not-null value.
 * <p>
 * If this button is registered on the {@link MenuManager}'s touch check list, then it will be
 * responsive to mouse touching. By default, upon mouse touch the outline will toggle, but if
 * {@link RadioButton#colorTouched} is set from non-null, then the fill color of the button will
 * change instead.
 * @author Matthew Moulton
 */
public class Button extends RadioButton {
	/**The string of text displayed on the face of the button.
	 * @see #setText(String)
	 * @see #getText()*/
	protected String text;
	/**The string expressions to define the dimensions of the button on the parent panel.*/
	protected Expression width, height;
	/**The font of the text rendered on the button. */
	protected Font font;
	/**The alignment for the text on the button's face. Defaults to centered.
	 * @see #setAlignment(Alignment)*/
	protected Alignment alignment = Alignment.CENTER_ALIGNMENT;
	/**The color of the text when the button is enabled. When the button is not enabled,
	 * this color will be one shade lighter. If the color is not set (null), then either
	 * black or white will be chosen at render time to maximize contrast with the fill
	 * color as determined by {@link #getFillColor()}. Defaults to null.
	 * @see #getTextColor()
	 * @see #setTextColor(Color)*/
	protected Color textColor = null;
	
	/**
	 * @param id a unique string designed to identify this component when an event occurs.
	 * @param text the string displayed on the button
	 * @param parent the panel that this button will reside upon
	 * @param x the x coordinate on the parent, given in menu component value format
	 * @param y the y coordinate on the parent, given in menu component value format
	 * @param width the width of the component, given in menu component value format
	 * @param height the height of the component, given in menu component value format
	 * @param font the font for the box
	 * @param color the background color for the button when enabled
	 */
	public Button(String id, String text, Panel parent, String x, String y,
			String width, String height, Font font, Color color) {
		super(id, parent, x, y, color);
		this.width = solve.parse(width, true, false);
		this.height = solve.parse(height, true, false);
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
	 * @param color the background color for the button when enabled
	 */
	public Button(String id, String text, Panel parent, int x, int y, Font font, Color color) {
		super(id, parent, x, y, color);
		this.text = text;
		this.font = font;
	}
	
	/**
	 * Draws the button onto the graphics. If this button is in a grid, it will take all of the
	 * space alloted to it. If it is in free-draw mode, then its dimensions will be decided by the
	 * string expressions for {@link MenuComponent#x}, {@link MenuComponent#y}, {@link #width}, and
	 * {@link #height}.
	 * <p>
	 * The method {@link RadioButton#getFillColor()} will be called to determine what color the
	 * body of this button will be. The outline will be in black if it should be drawn (specified
	 * by {@link Clickable#outline}), as will the text on the face of the button, unless the button
	 * is not enabled.
	 */
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		Rectangle rect = this.getRenderRect(xx, yy, ww, hh, width, height);
		int x = rect.x;
		int y = rect.y;
		int w = rect.width;
		int h = rect.height;

		Color fillColor = getFillColor();
		if(fillColor != null) {
			g.setColor(getFillColor());
			g.fillRect(x, y, w, h);
		}
		if(parent != null)
			defineClickBoundary(parent.handleOffsets(new int[] {x, x+w, x+w, x},
					new int[] {y, y, y+h, y+h}, this));
		
		if (outline) {
			g.setColor(Color.BLACK);
			g.drawRect(x, y, w - 1, h - 1);
		}
		
		Color textColor = this.textColor;
		if(textColor == null) {
			//finds whether the background is lighter or darker.
			if(fillColor != null) {
				//The opposite will be used for the text color
				//(299R + 587G + 114B) / 1000 gives a brightness in [0, 255]
				int brightness = (299*fillColor.getRed() +
								587*fillColor.getGreen() +
								114*fillColor.getBlue()) / 1000;
				if(brightness >= 255/2)
					textColor = Color.BLACK;
				else
					textColor = Color.WHITE;
			}else
				textColor = Color.BLACK;
		}if(!isEnabled())
			textColor = textColor.brighter();
		g.setColor(textColor);

		// draw the text
		if (text != null && !text.isEmpty()) {
			if(textResize())
				g.setFont(new Font(font.getFontName(), font.getStyle(),
						getTextVertResize(font.getSize())));
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
			
			switch(alignment) {
			case CENTER_ALIGNMENT:
				g.drawString(shownText, x + w/2 - fontWidth/2, (int) (y + h/2 + fontHeight/2.8));
				break;
			case LEFT_ALIGNMENT:
				g.drawString(shownText, x, (int) (y + h/2 + fontHeight/2.8));
				break;
			case RIGHT_ALIGNMENT:
				g.drawString(shownText, x + w - fontWidth, (int) (y + h/2 + fontHeight/2.8));
				break;
			}
		}
	}
	
	/**
	 * Sets the text that appears on the button face to the new text.
	 * @param text the String text to replace {@link #text}.
	 * @return this
	 */
	public Button setText(String text) {
		this.text = text;
		return this;
	}
	/**
	 * Returns the text that the button uses on its button face in rendering
	 * @return {@link #text}
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Sets the alignment for the button's text.
	 * @param newAlignment the alignment to replace {@link #alignment}
	 * @return this
	 */
	public Button setAlignment(Alignment newAlignment) {
		alignment = newAlignment;
		return this;
	}
	
	/**
	 * Sets the color of the text on the button's face.
	 * @param color to replace {@link #textColor}
	 */
	public Button setTextColor(Color color) {
		this.textColor = color;
		return this;
	}
	/**
	 * Returns the color of the text on the button's face.
	 * @return {@link #textColor}
	 */
	public Color getTextColor() {
		return this.textColor;
	}
}
