package moulton.scalable.popups;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

import moulton.scalable.clickables.Button;
import moulton.scalable.clickables.EventAction;
import moulton.scalable.containers.MenuManager;
import moulton.scalable.geometrics.Line;
import moulton.scalable.texts.Alignment;
import moulton.scalable.texts.Caption;
import moulton.scalable.texts.LineBreak;

public abstract class CommonPopup extends Popup {

	/**
	 * Creates a common pop up centered in the menu screen with an x in the top right corner.
	 * @param text the text displayed in the body of the pop up
	 * @param title the title of the pop up. Set to null for no title
	 * @param font the font of the text on the pop up
	 * @param xId the ID that should be used for the exit button
	 * @param manager the menu manager that contains this pop up. This is used to exit out on clicking x.
	 */
	public CommonPopup(String text, String title, Font font, String xId, MenuManager manager) {
		super(null, null, Color.WHITE);
		init(text, title, font, true, xId, manager);
	}
	/**
	 * Creates a common pop up centered in the menu screen.
	 * @param text the text displayed in the body of the pop up
	 * @param title the title of the pop up. Set to null for no title
	 * @param font the font of the text on the pop up
	 */
	public CommonPopup(String text, String title, Font font) {
		super(null, null, Color.WHITE);
		init(text, title, font, false, null, null);
	}
	/**
	 * Creates a common pop up at (x, y) with an x in the top right corner.
	 * @param text the text displayed in the body of the pop up
	 * @param title the title of the pop up. Set to null for no title
	 * @param font the font of the text on the pop up
	 * @param x the x location of the pop up
	 * @param y the y location of the pop up
	 * @param xId the ID that should be used for the exit button
	 * @param manager the menu manager that contains this pop up. This is used to exit out on clicking x.
	 */
	public CommonPopup(String text, String title, Font font, String x, String y, String xId, MenuManager manager) {
		super(x, y, null, null, Color.WHITE);
		init(text, title, font, true, xId, manager);
	}
	/**
	 * Creates a common pop up at (x, y).
	 * @param text the text displayed in the body of the pop up
	 * @param title the title of the pop up. Set to null for no title
	 * @param font the font of the text on the pop up
	 * @param x the x location of the pop up
	 * @param y the y location of the pop up
	 */
	public CommonPopup(String text, String title, Font font, String x, String y) {
		super(x, y, null, null, Color.WHITE);
		init(text, title, font, true, null, null);
	}
	
	/**
	 * Sets up the most default aspects of a pop up, including a title, a blanket color, some text,
	 * and optionally an x button in the top right. The logic is performed here instead of in the 
	 * constructor in order to reduce redundancy and increase adaptability. 
	 * 
	 * @param text the text displayed in the body of the pop up
	 * @param xId the ID that should be used for the exit button
	 * @param manager the menu manager that contains this pop up. This is used to exit out on clicking x.
	 * @param title the title of the pop up. Set to null for no title
	 * @param font the font of the text on the pop up
	 * @param font the font of the text on the pop up
	 * @param exitButton whether an x button in the top right corner should be included in the pop up
	 */
	private void init(String text, String title, Font font, boolean exitButton, String xId, MenuManager manager) {
		base.setOutline(true);
		blanketBackground = new Color(0x33DDDDDD, true);
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		FontMetrics fm = img.getGraphics().getFontMetrics(font);
		int fontHeight = fm.getHeight();
		int doubleHeight = fontHeight * 2;
		
		if(title != null) {
			title = "  " + title;
			new Caption(title,base,"0",""+fm.getHeight(), font, Alignment.LEFT_ALIGNMENT);
			new Line(base, "1",""+(doubleHeight-1), "width-2", "?", Color.GRAY);
		}
		
		if(exitButton) {
			final MenuManager man = manager;
			final String xxId = xId;
			Button exit = new Button(xId, "X", base, "width-"+(fm.stringWidth("X")*2), "0", "?width", ""+doubleHeight, font, Color.RED);
			exit.setClickAction(new EventAction() {
				@Override public boolean onEvent() {
					man.setPopup(null);
					return xxId != null;
				}
			});
			addTouchComponent(exit);
		}
		
		final int DEFAULT_WIDTH = getPopupDefaultWidth(fontHeight);
		final int MAX_WIDTH = getPopupMaxWidth(fontHeight);
		int textWidth = fm.stringWidth(text);
		int linesOfText = 1;
		if(textWidth < DEFAULT_WIDTH) {
			textWidth = DEFAULT_WIDTH;
		}else {
			if(fm.stringWidth(text) > MAX_WIDTH) { //only continue if our text is too long
				textWidth = MAX_WIDTH;
				String rem = text;
				text = ""; //text will be our final result with \n to separate lines
				eachLine: while(rem.length() > 0) {
					String line = "";
					while(rem.length() > 0 && fm.stringWidth(line) <= MAX_WIDTH) {
						//pass the first char from rem to line
						char next = rem.charAt(0); 
						rem = rem.substring(1);
						line += next;
						if(next == '\n') { //"short circuit"
							text += line;
							linesOfText++;
							continue eachLine;
						}
					}
					//now line is too long, or we ran out of rem
					if(rem.length() == 0)
						text += line; //append what we have
					else { //fix the issue that the line is too long
						LineBreak result = LineBreak.check(false, line, rem);
						text += result.LINE + '\n';
						rem = result.REMAINDER;
						linesOfText++;
					}
				}
			}else
				textWidth = fm.stringWidth(text);
		}
		new Caption(text, base, "0", ""+(doubleHeight*(1 + (title==null?0:1))), font, "width");
		
		this.width = ""+(textWidth+doubleHeight); //doubleHeight serves as padding
		this.height = ""+(getPopupExtraHeight(fontHeight) + doubleHeight*(1 + (title==null?0:1)) + linesOfText*fontHeight);
	}
	
	/**
	 * This is used by {@link #init(String, String, String, String, Font, boolean, boolean)} to
	 * find the default width of the pop up if the text is not too long.
	 * @param fontHeight the height of the font given in the constructor
	 * @return an integer number of pixels that should be used as the default pop up width
	 */
	public int getPopupDefaultWidth(int fontHeight) {
		return fontHeight * 6;
	}
	
	/**
	 * This is used by {@link #init(String, String, String, String, Font, boolean, boolean)} to
	 * find the maximum width of the pop up. If the text is too long to fit in the
	 * {@link #getPopupDefaultWidth(int)}, then the width of the pop up will be extended to
	 * accommodate it. However, this is the limit to such extension.
	 * @param fontHeight the height of the font given in the constructor
	 * @return an integer number of pixels that should be used as the maximum pop up width
	 */
	public int getPopupMaxWidth(int fontHeight) {
		return fontHeight * 14;
	}
	
	/**
	 * This is used by {@link #init(String, String, String, String, Font, boolean, boolean)} to
	 * find the height of the pop up. This is extra height after the title and text have been
	 * accounted for.
	 * @param fontHeight the height of the font given in the constructor
	 * @return an integer number of pixels that should be used for extra pop up height
	 */
	public int getPopupExtraHeight(int fontHeight) {
		return 0;
	}
	

}
