package moulton.scalable.clickables;

import java.awt.Color;
import java.awt.Font;

import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Panel;
import moulton.scalable.texts.TextInputComponent;

/**
 * The Form Button is a button specially adapted to work with forms. It will hold the focus of the Menu Manager
 * and receive text input. If the user presses enter ('\n'), the FormButton will recognize the event as if it
 * was being pressed by a mouse click.
 * @author Matthew Moulton
 */
public class FormButton extends Button implements TextInputComponent {
	/**The button needs to keep track of the menu manager to be able to initiate phantom click events when
	 * appropriate text input is received.*/
	protected MenuManager manager;
	
	/**
	 * @param id a unique string designed to identify this component when an event occurs.
	 * @param text the string displayed on the button
	 * @param menu the menu manager to be saved to {@link #manager}. This is needed for the button to call
	 * phantom internal click events on itself.
	 * @param parent the panel that this button will reside upon
	 * @param x the integer x coordinate this button should appear on its panel
	 * @param y the integer y coordinate this button should appear on its panel
	 * @param font the font for the box
	 * @param color the background color for the button when enabled
	 */
	public FormButton(String id, String text, MenuManager menu, Panel parent, int x, int y, Font font, Color color) {
		super(id, text, parent, x, y, font, color);
		this.manager = menu;
	}
	
	/**
	 * @param id a unique string designed to identify this component when an event occurs.
	 * @param text the string displayed on the button
	 * @param menu the menu manager to be saved to {@link #manager}. This is needed for the button to call
	 * phantom internal click events on itself.
	 * @param parent the panel that this button will reside upon
	 * @param x the x coordinate on the parent, given in menu component value format
	 * @param y the y coordinate on the parent, given in menu component value format
	 * @param width the width of the component, given in menu component value format
	 * @param height the height of the component, given in menu component value format
	 * @param font the font for the box
	 * @param color the background color for the button when enabled
	 */
	public FormButton(String id, String text, MenuManager menu, Panel parent, String x, String y, String width, String height,
			Font font, Color color) {
		super(id, text, parent, x, y, width, height, font, color);
		this.manager = menu;
	}

	/**
	 * The form button does not actually keep track of any message or text, but it needs to receive updates
	 * from the menu manager when it has focus in order to receive enter events.
	 * @param string the string that would typically be appended, but here is compared against '\n' to
	 * identify an enter event.
	 */
	@Override
	public void appendMessage(String string) {
		//we want to be able to handle the enter character
		if(string.equals("\n")) {
			//If there is a click action, do that first
			if(manager != null)
				manager.mouseReleased(-1, -1); //release focus of any others
			
			boolean consumed = false;
			if(clickAction != null)
				consumed = clickAction.onEvent();
			
			//send a phantom click to the menu manager.
			if(!consumed && manager != null)
				manager.clickableAction(this);
			
			if(this.getGroup() != null)
				getGroup().select(this);
		}
	}

	/**
	 * Not used by the FormButton.
	 */
	@Override
	public void removeMessage(int chars) {}

}
