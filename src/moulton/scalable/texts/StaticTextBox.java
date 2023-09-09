package moulton.scalable.texts;

import java.awt.Color;
import java.awt.Font;

import moulton.scalable.containers.Panel;

/**
 * In some instances, the user will find it useful to select and/or copy text from a text box, but
 * should not be able to edit its values. This class is made for that very purpose. If the contents
 * of this text box need to be modified, they can still be altered through
 * {@link #setMessage(String)} but not through {@link #appendMessage(String)} or
 * {@link #removeMessage(int, boolean)}.
 * @author Matthew Moulton
 */
public class StaticTextBox extends TextBox {
	
	/**
	 * @param message the string displayed in the box
	 * @param parent the panel that this text box will reside upon
	 * @param x the x coordinate of this text box in its parent's grid
	 * @param y the y coordinate of this text box in its parent's grid
	 * @param font the font for the box
	 * @param color the background color for the box when editable
	 */
	public StaticTextBox(String message, Panel parent, int x, int y, Font font, Color color) {
		super(message, parent, x, y, font, color);
		blinkTime = -1;
	}
	/**
	 * @deprecated Use {@link #StaticTextBox(String, Panel, int, int, Font, Color)} and
	 * {@link #setId(String)}
	 */
	@Deprecated(since="1.15")
	public StaticTextBox(String id, String message, Panel parent, int x, int y, Font font, Color color) {
		super(message, parent, x, y, font, color);
		this.id = id;
		blinkTime = -1;
	}
	/**
	 * @param message the string displayed in the box
	 * @param parent the panel that this text box will reside upon
	 * @param x the x coordinate on the screen, given in menu component value format
	 * @param y the y coordinate on the screen, given in menu component value format
	 * @param width the width of the component, given in menu component value format
	 * @param height the height of the component, given in menu component value format
	 * @param font the font for the box
	 * @param color the background color for the box when editable
	 */
	public StaticTextBox(String message, Panel parent, String x, String y, String width,
			String height, Font font, Color color) {
		super(message, parent, x, y, width, height, font, color);
		blinkTime = -1;
	}
	/**
	 * @deprecated use {@link #StaticTextBox(String, Panel, String, String, String, String, Font, Color)}
	 * and {@link #setId(String)}
	 */
	public StaticTextBox(String id, String message, Panel parent, String x, String y, String width,
			String height, Font font, Color color) {
		super(message, parent, x, y, width, height, font, color);
		this.id = id;
		blinkTime = -1;
	}
	
	/**
	 * The user cannot modify the static text box by typing commands, so this method overrides the action of
	 * TextBox and does nothing.
	 */
	@Override
	public void removeMessage(int chars) {}
	
	/**
	 * The user cannot modify the static text box by typing commands, so this method overrides the action of
	 * TextBox and does nothing.
	 */
	@Override
	public synchronized void appendMessage(String string) {}
	
	/**
	 * The user cannot modify the static text box by typing commands, so this method overrides the action of
	 * TextBox and does nothing.
	 */
	@Override
	public void paste(String pasteText) {}
	
	/**
	 * Cut commands are invalid, so the value of {@link #copy()} is returned instead.
	 */
	@Override
	public String cut() {
		return copy();
	}
	
	/**Regardless of whether this component is clicked, it should not show the blinker. */
	@Override
	public void setClicked(boolean clicked, int mouseX, int mouseY) {
		super.setClicked(clicked, mouseX, mouseY);
		showBlinker = false;
	}

}