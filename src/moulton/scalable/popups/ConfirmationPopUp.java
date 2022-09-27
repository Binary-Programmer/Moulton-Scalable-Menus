package moulton.scalable.popups;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

import moulton.scalable.clickables.Button;
import moulton.scalable.clickables.EventAction;
import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Panel;

/**
 * This is an example of what can be done with pop ups. Sometimes the user needs to choose between
 * two options before they proceed to the next action, and this pop up fulfills this purpose. For
 * greater flexibility, subclass this and override {@link #init(String, String, Font, MenuManager,
 * boolean)}.
 * @author Matthew Moulton
 */
public class ConfirmationPopUp extends CommonPopUp {

	/**
	 * @param text the text that should be accepted or rejected.
	 * @param title the title of the pop up. Set to null for no title.
	 * @param font the font of the text on the pop up.
	 * @param okId the ID that should be used for the accept button.
	 * @param cancelId the ID that should be used for the decline and exit buttons.
	 * @param manager the menu manager that contains this pop up. This is used to exit out on
	 * clicking x.
	 * @param option whether the option should be presented as Yes/No (for true) or Ok/Cancel (for
	 * false).
	 */
	public ConfirmationPopUp(String text, String title, Font font, String okId, String cancelId,
			MenuManager manager, boolean option) {
		super(text, title, font, cancelId, manager);
		init(okId, cancelId, font, manager, option);
	}
	/**
	 * @param text the text that should be accepted or rejected.
	 * @param title the title of the pop up. Set to null for no title.
	 * @param font the font of the text on the pop up.
	 * @param x the x location of the pop up
	 * @param y the y location of the pop up
	 * @param okId the ID that should be used for the accept button.
	 * @param cancelId the ID that should be used for the decline and exit buttons.
	 * @param manager the menu manager that contains this pop up. This is used to exit out on
	 * clicking x.
	 * @param option whether the buttons should be presented as Yes/No (for true) or Ok/Cancel
	 * (for false).
	 */
	public ConfirmationPopUp(String text, String title, Font font, String x, String y,
			String okId, String cancelId, MenuManager manager, boolean option) {
		super(text, title, font, cancelId, manager);
		init(okId, cancelId, font, manager, option);
	}

	/**
	 * Called to set up the pop up
	 * @param okId the id of the ok button
	 * @param cancelId the id of the cancel button
	 * @param font the pop up's font
	 * @param manager the menu manager that contains this pop up.
	 * @param option whether the buttons should be Yes/No (for true), or Ok/Cancel (for false)
	 */
	private void init(String okId, String cancelId, Font font,
			MenuManager manager, boolean option) {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		FontMetrics fm = img.getGraphics().getFontMetrics(font);
		int fontHeight = fm.getHeight();
		int doubleHeight = fontHeight * 2;
		
		String accept = option? "Yes":"Ok";
		String cancel = option? "No":"Cancel";
		int okWidth = fm.stringWidth(accept)*2;
		int noWidth = fm.stringWidth(cancel)*2;
		int spaceWidth = fm.stringWidth("_");
		Panel optButtons = new Panel(base, "CENTERX", "height-1-"+doubleHeight,
				""+(okWidth+spaceWidth+noWidth), ""+doubleHeight, null);
		optButtons.getGridFormatter().setMargin(""+spaceWidth, null);
		
		final MenuManager man = manager;
		final String xxId = okId;
		EventAction quitPopup = new EventAction() {
			@Override public boolean onEvent() {
				man.setPopUp(null);
				return xxId != null;
			}
		};
		Button acceptButton = new Button(okId, accept, optButtons, 0, 0, font, Color.LIGHT_GRAY);
		acceptButton.setClickAction(quitPopup);
		addTouchComponent(acceptButton);
		Button rejectButton = new Button(cancelId, cancel, optButtons, 1, 0,
				font, Color.LIGHT_GRAY);
		rejectButton.setClickAction(quitPopup);
		addTouchComponent(rejectButton);
	}
	
	@Override
	public int getPopupExtraHeight(int fontHeight) {
		return fontHeight * 3;
	}

}
