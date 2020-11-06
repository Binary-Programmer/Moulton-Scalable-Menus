package example7;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import moulton.scalable.containers.Panel;
import moulton.scalable.texts.TextBox;

public class TextEditBox extends TextBox{
	private int quickClicks = 0;
	private long timeLast = -1;
	private int lastBlinkerX;
	private int lastBlinkerY;
	private int hheight;
	
	public TextEditBox(String id, String message, Panel parent, String x, String y, String width, String height, Font font, Color color) {
		super(id, message, parent, x, y, width, height, font, color);
	}
	
	public TextEditBox(String id, String message, Panel parent, int x, int y, Font font, Color color) {
		super(id, message, parent, x, y, font, color);
	}
	
	public void moveVertical(boolean up) {
		if(up)
			lastBlinkerY -= hheight;
		else
			lastBlinkerY += hheight;
		index = findIndex(lastBlinkerX, lastBlinkerY);
		refreshBlinker();
	}
	
	public void selectVertical(boolean up) {
		int ci = index;
		moveVertical(up);
		if(index != ci) {
			selection = true;
			clickIndex = ci;
		}
	}
	
	public void moveToBreak(boolean left) {
		if(left) { //we move back a character at the beginning so checking char in the loop will be the same
			index--;
			if(index < 0) {
				index = 0;
				return;
			}
		}else if(index >= message.length()) //this can only happen moving right, so the end is reached
			return;
		
		boolean breakCharFound = false;
		boolean run = true; //run until a punctuation character has been found, then a non-punct is found
		while(run) {
			//check where we are
			char c = message.charAt(index);
			if(!breakCharFound) {
				if(!(Character.isLetter(c) || Character.isDigit(c))) //if the letter isn't a num or a char, then it is a break
					breakCharFound = true;
			}else {
				if(Character.isLetter(c) || Character.isDigit(c)) {
					//we have reached the end
					//if we were moving left, go back one right
					if(left)
						index++;
					break;
				}
			}
			
			//move index
			if(left) {
				index--;
				if(index < 0) {
					index = 0;
					break;
				}
			}else {
				index++;
				if(index >= message.length()) {
					index = message.length();
					break;
				}
			}
		}
		refreshBlinker();
	}
	
	public void selectShift(boolean left, boolean toBreak) {
		int ci = index;
		if(toBreak)
			moveToBreak(left);
		else
			shiftIndex(left? -1:1);
		if(index != ci) {
			selection = true;
			clickIndex = ci;
		}
	}
	
	public void deleteToBreak(boolean left) {
		int currIndex = index;
		moveToBreak(left);
		int move = currIndex - index;
		index = currIndex;
		this.removeMessage(move);
	}
	
	@Override
	public void setClicked(boolean clicked, int mouseX, int mouseY) {
		long timeNow = System.currentTimeMillis();
		if(!clicked || quickClicks == 0)
			quickClicks = 1;
		else {
			if(timeLast == -1)
				timeLast = timeNow;
			if(timeNow - timeLast < 500)
				quickClicks++;
			else
				quickClicks = 1;
		}
		timeLast = timeNow;
		super.setClicked(clicked, mouseX, mouseY);
		//here we act differently based on the number of quick clicks
		selectByClicks(quickClicks);
	}
	
	protected void selectByClicks(int quickClicks) {
		//two clicks selects the word- to the space on both sides
		//three clicks selects the sentence- to the period, question mark, or exclamation point
		//four clicks selects the paragraph- to the newline on both sides
		//five or more clicks selects all
		if(quickClicks < 2 || message.isEmpty())
			return;
		
		int si = index, ei = index; //the start and end indices
		if(index == message.length())
			si = index - 1;
		char now = message.charAt(si);
		switch(quickClicks) {
		case 2:
			boolean spaces = (now == ' ' || now == '\n'); //if the current character is a space, that is highlight
			while(true) {
				if(si < 0 || (spaces ^ (message.charAt(si) == ' ' || message.charAt(si) == '\n'))) {
					si++;
					break;
				}
				si--;
			}
			while(true) {
				int length = message.length();
				if(ei >= length || (spaces ^ (message.charAt(ei) == ' ' || message.charAt(ei) == '\n'))) {
					break;
				}
				ei++;
			}
			break;
		case 3:
			char[] stopPunc = {'.', '?', '!'};
			boolean puncts = false;
			for(char c: stopPunc) {
				if(c == now) {
					puncts = true;
					break;
				}
			}
			while(true) {
				if(si < 0)
					break;
				char c = message.charAt(si);
				boolean match = false;
				for(char p: stopPunc) {
					if(c==p) {
						match = true;
						break;
					}
				}if(puncts) //reverse the result for punctuation selection
					match = !match; //if no match was found, then a stopping point was reached
				if(match) {
					si++;
					if(!puncts) {
						//keep going while the character is a space or newline
						do {
							si++;
							if(si >= message.length())
								break;
							c = message.charAt(si);
						}while(c == '\n' || c == ' ');
					}
					break;
				}
				si--;
			}
			while(true) {
				int length = message.length();
				if(ei >= length)
					break;
				char c = message.charAt(ei);
				boolean match = false;
				for(char p: stopPunc) {
					if(c==p) {
						match = true;
						break;
					}
				}if(puncts) //reverse the result for punctuation selection
					match = !match; //if no match was found, then a stopping point was reached
				if(match) {
					if(!puncts)
						ei++;
					break;
				}
				ei++;
			}
			break;
		case 4:
			boolean enters = (now == '\n'); //if current is enter, that is our highlight
			while(true) {
				if(si < 0 || (enters ^ message.charAt(si) == '\n')) {
					si++;
					break;
				}
				si--;
			}
			while(true) {
				int length = message.length();
				if(ei >= length || (enters ^ message.charAt(ei) == '\n')) {
					break;
				}
				ei++;
			}
			break;
		default:
			selectAll();
			return;
		}
		if(si != ei) {
			selection = true;
			index = ei;
			clickIndex = si;
		}
	}
	
	@Override
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		super.render(g, xx, yy, ww, hh);
	}
	
	@Override
	protected void drawBlinker(Graphics g, int x, int y, int w, int h, int rowWidth, int blinkerX, int blinkerRow, int underscoreWidth, int textOffset) {
		super.drawBlinker(g, x, y, w, h, rowWidth, blinkerX, blinkerRow, underscoreWidth, textOffset);
		
		//we want to save where the blinker was drawn, so that when we use the up and down arrow keys, we can just
		//use what is actually drawn above and below
		FontMetrics fm = g.getFontMetrics();
		hheight = fm.getHeight();
		int decrease = hheight - fm.getDescent();
		lastBlinkerY = y + hheight*blinkerRow - decrease + textOffset + hheight/2;
		switch(alignment) {
		case LEFT_ALIGNMENT:
			lastBlinkerX = x + underscoreWidth/2 + blinkerX;
			break;
		case CENTER_ALIGNMENT:
			lastBlinkerX = x - rowWidth/2 + w/2 + blinkerX;
			break;
		case RIGHT_ALIGNMENT:
			lastBlinkerX = x + w - (rowWidth+underscoreWidth/2) + blinkerX;
			break;
		}
	}

	public void selectToClick(int x, int y) {
		int index = this.index;
		if(this.selection)
			index = clickIndex;
		this.setClicked(true, x, y);
		this.selection = index != this.index;
		if(selection)
			this.clickIndex = index;
	}

}
