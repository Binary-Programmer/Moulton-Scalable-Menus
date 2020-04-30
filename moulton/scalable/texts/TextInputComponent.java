package moulton.scalable.texts;

import moulton.scalable.containers.MenuManager;
import moulton.scalable.clickables.Clickable;

/**
 * Implementations of HotkeyTextComponent are instances of {@link Clickable} that receive text input from
 * the {@link MenuManager} through the {@link #appendMessage(String)} and {@link #removeMessage(int, boolean)}
 * methods.
 * @author Matthew Moulton
 */
public interface TextInputComponent {
	/**
	 * When this component is selected and the menu manager receives key input from the user, it is sent
	 * to the component through this method.
	 * @param string the text to append
	 */
	public void appendMessage(String string);
	/**
	 * When this component is selected and the menu manager receives delete key input from the user, this
	 * component is notified through this method.
	 * @param chars the number of characters requested to delete
	 * @param leftDelete whether the text should be deleted left of the selection index (like the backspace
	 * key) or to the right (like the delete key)
	 */
	public void removeMessage(int chars, boolean leftDelete);
}
