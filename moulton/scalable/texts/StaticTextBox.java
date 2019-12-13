package moulton.scalable.texts;

import java.awt.Color;
import java.awt.Font;

import moulton.scalable.containers.Panel;

/**
 * In some instances, the user will find it useful to select and/or copy text from a text box, but should not be
 * able to edit its values. This class is made for that very purpose. If the contents of this text box need to
 * be modified, they can still be altered through {@link #setMessage(String)} but not through {@link #appendMessage(String)}
 * or {@link #removeMessage(int, boolean)}.
 * @author Matthew Moulton
 */
public class StaticTextBox extends TextBox {
	
	/**
	 * See {@link TextBox#TextBox(String, String, Panel, int, int, Font, Color)}
	 */
	public StaticTextBox(String id, String message, Panel parent, int x, int y, Font font, Color color) {
		super(id, message, parent, x, y, font, color);
		blinkTime = -1;
	}
	/**
	 * See {@link TextBox#TextBox(String, String, Panel, String, String, String, String, Font, Color)}.
	 */
	public StaticTextBox(String id, String message, Panel parent, String x, String y, String width, String height,
			Font font, Color color) {
		super(id, message, parent, x, y, width, height, font, color);
		blinkTime = -1;
	}
	
	/**
	 * The user cannot modify the static text box by typing commands, so this method overrides the action of TextBox
	 * and does nothing.
	 */
	@Override
	public void removeMessage(int chars, boolean leftDelete) {}
	
	/**
	 * The user cannot modify the static text box by typing commands, so this method overrides the action of TextBox
	 * and does nothing.
	 */
	@Override
	public synchronized void appendMessage(String string) {}
	
	/**
	 * The user cannot modify the static text box by typing commands, so this method overrides the action of TextBox
	 * and does nothing.
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