package moulton.scalable.texts;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.LinkedList;

import moulton.scalable.containers.Panel;
import moulton.scalable.draggables.ScrollBar;
import moulton.scalable.utils.MenuComponent;

/**
 * A menu component that holds a history of text. A scroll bar can be added to see more than is able to be shown in the
 * specified rectangle. Even if no scroll bar is added, the text history is still functional, just static.
 * @author Matthew Moulton
 */
public class TextHistory extends MenuComponent{
	/** The list of the strings being held. */
	protected LinkedList<String> history = new LinkedList<>();
	/** The dimensions of the rectangle. */
	protected String width, height;
	/** The font that the history should be drawn in. */
	protected Font font;
	/** The scroll bar to change which strings are displayed.
	 * @see #setScrollBar(ScrollBar)*/
	protected ScrollBar bar;
	/** Whether or not the text history should add new strings to the top or bottom of the list. This correlates with 
	 * !{@link ScrollBar#inverseRender}.
	 * @see #setViewMode(boolean)*/
	protected boolean addToTop = false;
	/**The maximum number of messages saved. After this number is reached, the oldest message is deleted once
	 * the newest one is saved.
	 * @see #setMaxMessages(int)
	 * @see #getMaxMessages()*/
	protected int maxMessages;

	public TextHistory(Panel parent, int x, int y, Font font, boolean addToTop, int maxMessages) {
		super(parent, x, y);
		this.font = font;
		this.addToTop = addToTop;
		this.maxMessages = maxMessages;
	}

	public TextHistory(Panel parent, String x, String y, String w, String h, Font font, boolean addToTop, int maxMessages) {
		super(parent, x, y);
		this.font = font;
		this.addToTop = addToTop;
		this.maxMessages = maxMessages;
		width = w;
		height = h;
	}

	@Override
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		int x, y, w, h;

		if(getGridLocation()==null){
			x = xx + solveString(this.x, ww, hh);
			y = yy + solveString(this.y, ww, hh);

			// variant for input ending points instead of widths indicated by a starting question
			if (width.charAt(0) == '?') {
				int x2 = solveString(width.substring(1), ww, hh);
				w = x2 - x;
			} else
				w = solveString(width, ww, hh);

			if (height.charAt(0) == '?') {
				int y2 = solveString(height.substring(1), ww, hh);
				h = y2 - y;
			} else
				h = solveString(height, ww, hh);
		}else{
			x = xx;
			y = yy;
			w = ww;
			h = hh;
		}
		//always show most recent additions to the list first, more if fits.		
		if(history.size()>0){
			//set font
			if(textResize()) {
				int fontSize = getTextVertResize(font.getSize());
				if(fontSize == 0) //if the font is so small you cannot see it, there is nothing left to do here.
					return;
				g.setFont(new Font(font.getFontName(), font.getStyle(), fontSize));
			} else
				g.setFont(font);

			FontMetrics metrics = g.getFontMetrics();
			int numberOfLinesShown = h/metrics.getHeight();
			String[] texts;
			int textIndex;
			boolean shouldReset;
			int textMax = 0;
			boolean allShown;
			boolean endCutoff;
			do {
				endCutoff = false;
				allShown = true;
				shouldReset = false;
				texts = new String[numberOfLinesShown];
				int textNumber = 0;
				textIndex = history.size()-1;
				if(bar!=null) {
					textIndex-=bar.getOffset();
					if(bar.getOffset()>0) //some texts were skipped, so not all was shown
						allShown = false;
				}
				//sequentially add text bits into this list, then after it is processed, add
				//them to the texts array in the correct order for the adding direction
				LinkedList<String> thisText = new LinkedList<>();
				while(textNumber < numberOfLinesShown && textIndex>-1){
					thisText.clear();
					String line = history.get(textIndex);
					while(line.length()>0){
						int ii = line.length();
						String word = line;
						while(ii>-1 && metrics.stringWidth(word)>w){
							word = line.substring(0,ii);
							ii--;
						}
						if(ii==line.length()) { //the whole text could fit
							thisText.add(line);
							line = "";
						}else if(ii<line.length()) { //it didn't fit, split it up
							thisText.add(word);
							//ii is one less than it should be since it is decremented after the word was gotten
							line = line.substring(++ii);
						}
					}
					//add from thisText
					int textLength = thisText.size();
					for(int i=0; i<textLength; i++) {
						if(addToTop)
							texts[textNumber++] = thisText.get(i);
						else
							texts[textNumber++] = thisText.get(textLength-(i+1));
						//add them until there isn't space (if applicable)
						if(textNumber>=numberOfLinesShown) {
							if(i<textLength-1) { //if there was more in the text that gets skipped over
								allShown = false; //then not all was shown
								endCutoff = true;
							}
							break;
						}
					}//don't clear thisText, the last usage saved for edge checking later
					textIndex--;
				}textIndex++;
				if(textIndex>0) //not all was shown if end wasn't reached
					allShown = false;
				//finds the maximum text offset being used
				textMax = texts.length-1;
				for (int i=0; i<texts.length; i++) {
					if(texts[i] == null) {
						textMax = i-1;
						break;
					}
				}
				/*
				 *If available lines are not being used and there are more lines needed to be shown, reset
				 *the offset for the scroll bar and render again (shouldRender=true).
				 *This could occur if the text history is resized larger when the bar's offset is nonzero.
				 */
				shouldReset = (bar != null) && (textMax+1 < numberOfLinesShown && !allShown && bar.getOffset()!=0);
				if(shouldReset) {
					//there is one exception: if the next text would be too long to show, some white space is allowed
					int availableLines = numberOfLinesShown-(textMax+1);
					String line = history.get(history.size()-bar.getOffset()); //get one line before start
					if(metrics.stringWidth(line)>availableLines*w)
						shouldReset = false; //exception met, don't reset
					else //decreases the offset down until all possible is accommodated
						bar.setOffset(bar.getOffset()- availableLines);
				}
			}while(shouldReset);
			
			//update the scroll bar
			if(bar!=null) {
				if(!allShown) { //if there were some lines not shown, bar should be editable
					bar.setEditable(true);
					int totalOffs = history.size();
					int barOffs = totalOffs - bar.getOffset() - textIndex;
					if(endCutoff)
						totalOffs++;
					if(bar.getTotalOffs()!=totalOffs)
						bar.setTotalOffs(totalOffs,false);
					bar.setBarOffs(barOffs);
				}else {
					bar.setEditable(false);
				}
			}
			
			//print now
			g.setColor(Color.BLACK);
			int remainderSpace = (h - numberOfLinesShown*metrics.getHeight())/2;
			for(int i=0; i<textMax+1; i++){
				String write = texts[i];
				if(addToTop) {
					drawText(g, write, x, y+ i*metrics.getHeight() +metrics.getAscent() +remainderSpace);
				}else {
					drawText(g, write, x, y+ (numberOfLinesShown-1-i)*metrics.getHeight()+metrics.getAscent()
						+remainderSpace);
				}
			}
		}
	}
	
	/**
	 * The {@link #render(Graphics, int, int, int, int)} method sequentially goes through each text that needs to
	 * be shown and calculates where it needs to be. Once a text's position has been calculated, it is drawn with
	 * this method.
	 * @param g The graphics to draw the text on
	 * @param text the message
	 * @param x the x-position, in pixels, where the text should be drawn
	 * @param y the y-position, in pixels, where the text should be drawn
	 */
	protected void drawText(Graphics g, String text, int x, int y) {
		g.drawString(text, x, y);
	}

	/**
	 * Adds the string to {@link #history} and changes {@link ScrollBar#totalOffs} of {@link #bar} if not null.
	 * @param s The string that should be added.
	 */
	public void addToList(String s){
		history.add(s);
		while(history.size()>maxMessages) {
			history.removeFirst();
		}
		if(bar!=null)
			bar.setTotalOffs(history.size(), true);
	}

	/**
	 * Sets the scroll bar so the text history can show more than just recent messages. <br>
	 * Will also override {@link ScrollBar#pullNegative} and {@link ScrollBar#inverseRender} to coincide
	 * with {@link #addToTop}.
	 * @param bar the scroll bar to replace {@link #bar}.
	 */
	public void setScrollBar(ScrollBar bar) {
		this.bar = bar;
		if(bar != null)
			bar.renderInverse(!addToTop);
	}
	
	/**
	 * Sets whether this text history should display most recent additions at the top (true) or the bottom (false)
	 * @param recentAtTop value to be saved as {@link #addToTop}
	 */
	public void setViewMode(boolean recentAtTop) {
		addToTop = recentAtTop;
		if(bar != null) {
			bar.renderInverse(addToTop);
		}
	}
	
	/**
	 * Sets the maximum number of messages that this text history will hold.
	 * @param maxMessages {@link #maxMessages}
	 */
	public void setMaxMessages(int maxMessages) {
		this.maxMessages = maxMessages;
	}
	/**
	 * Returns the maximum number of messages that this text history can hold.
	 * @return {@link #maxMessages}
	 */
	public int getMaxMessages() {
		return maxMessages;
	}

}