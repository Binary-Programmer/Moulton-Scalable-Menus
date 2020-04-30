package moulton.scalable.popups;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

import moulton.scalable.clickables.Button;
import moulton.scalable.geometrics.Line;
import moulton.scalable.texts.Alignment;
import moulton.scalable.texts.Caption;

public class NotificationPopup extends Popup {

	public NotificationPopup(String text, String okId, String title, Font font, boolean exitButton) {
		super(null, null, Color.WHITE);
		init(text, okId, title, font, exitButton);
	}
	
	public NotificationPopup(String text, String okId, String title, Font font, String x, String y, boolean exitButton) {
		super(x, y, null, null, Color.WHITE);
		init(text, okId, title, font, exitButton);
	}
	
	private void init(String text, String okId, String title, Font font, boolean exitButton) {
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
		
		if(exitButton)
			new Button(okId, "X", base, "width-"+(fm.stringWidth("X")*2), "0", "?width", ""+doubleHeight, font, Color.RED);
		
		int okWidth = fm.stringWidth("Ok");
		new Button(okId, "Ok", base, "centerx-"+okWidth, "height-1-"+doubleHeight, ""+okWidth*2, ""+doubleHeight, font, Color.LIGHT_GRAY);
		
		final int DEFAULT_WIDTH = doubleHeight * 3;
		final int MAX_WIDTH = doubleHeight * 8;
		int textWidth = fm.stringWidth(text);
		int linesOfText = 1;
		if(textWidth < DEFAULT_WIDTH) {
			textWidth = DEFAULT_WIDTH;
		}else {
			textWidth += doubleHeight;
			if(textWidth > MAX_WIDTH) {
				final int MAX_NOT_PAD = MAX_WIDTH - doubleHeight;
				int i = text.length()-1;
				for(; i > 0; i--) {
					//to the proper length
					if(fm.stringWidth(text.substring(0, i)) < MAX_NOT_PAD) {
						//insert the new line
						i = findNaturalBreak(text, 0, i);
						text = text.substring(0, i) + '\n' + text.substring(i);
						linesOfText++;
						textWidth = MAX_WIDTH;
						break;
					}
				}
				i++;
				int stop = i;
				for(; stop<text.length(); stop++) {
					if(fm.stringWidth(text.substring(i, stop)) >= MAX_NOT_PAD) {
						stop = findNaturalBreak(text, i, stop);
						text = text.substring(0, stop) + '\n' + text.substring(stop);
						linesOfText++;
						i = stop;
					}
				}
			}
		}
		new Caption(text, base, "0", "centery", font, "width");
		
		this.width = ""+textWidth;
		this.height = ""+(doubleHeight*3 + linesOfText*fontHeight);
	}
	
	protected int findNaturalBreak(String text, int startIndex, int breakFrom) {
		int ii = breakFrom;
		for(; ii>startIndex; ii--) {
			char c = text.charAt(ii);
			if(c == ' ' || c == '-')
				break;
		}
		if(ii == startIndex) //no space char found
			//go ahead with normal line break
			return breakFrom;
		else  //found a break char
			return ii;
	}

}
