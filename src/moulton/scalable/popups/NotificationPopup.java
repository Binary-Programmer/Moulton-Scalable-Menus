package moulton.scalable.popups;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

import moulton.scalable.clickables.Button;
import moulton.scalable.clickables.EventAction;
import moulton.scalable.containers.MenuManager;

/**
 * This is an example of what can be done with pop ups. Sometimes the user needs to be notified of some event
 * or information before they proceed to the next action, and this pop up fulfills this purpose. Admittedly
 * its design does not allow for much flexibility: the constructor calls an init method that creates the
 * contents of the pop up on its base panel.
 * @author Matthew Moulton
 */
public class NotificationPopup extends CommonPopup {
	/**
	 * @param text the text that is the notification.
	 * @param title the title of the pop up. Set to null for no title.
	 * @param font the font of the text on the pop up.
	 * @param xId the ID that should be used for the exit buttons.
	 * @param manager the menu manager that contains this pop up. This is used to exit out.
	 */
	public NotificationPopup(String text, String title, Font font, String xId, MenuManager manager) {
		super(text, title, font, xId, manager);
		init(xId, font, manager);
	}
	/**
	 * @param text the text that is the notification.
	 * @param title the title of the pop up. Set to null for no title.
	 * @param font the font of the text on the pop up.
	 * @param x the x location of the pop up
	 * @param y the y location of the pop up
	 * @param xId the ID that should be used for the exit buttons.
	 * @param manager the menu manager that contains this pop up. This is used to exit out.
	 */
	public NotificationPopup(String text, String title, Font font, String x, String y, String xId, MenuManager manager) {
		super(text, title, font, xId, manager);
		init(xId, font, manager);
	}
	
	/**
	 * Called to set up the pop up
	 * @param okId the id of the ok button
	 * @param font the pop up's font
	 */
	private void init(String okId, Font font, MenuManager manager) {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		FontMetrics fm = img.getGraphics().getFontMetrics(font);
		int fontHeight = fm.getHeight();
		int doubleHeight = fontHeight * 2;
		
		int okWidth = fm.stringWidth("Ok");
		final MenuManager man = manager;
		final String xxId = okId;
		Button okButton = new Button(okId, "Ok", base, "centerx-"+okWidth, "height-1-"+doubleHeight, ""+okWidth*2, ""+doubleHeight, font, Color.LIGHT_GRAY);
		okButton.setClickAction(new EventAction() {
			@Override public boolean onEvent() {
				man.setPopup(null);
				return xxId != null;
			}
		});
		addTouchComponent(okButton);
	}
	
	@Override
	public int getPopupExtraHeight(int fontHeight) {
		return fontHeight * 3;
	}

}
