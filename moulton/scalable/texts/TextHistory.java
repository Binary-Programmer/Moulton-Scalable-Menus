package moulton.scalable.texts;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.LinkedList;

import moulton.scalable.containers.Panel;
import moulton.scalable.draggables.ScrollBar;
import moulton.scalable.draggables.ScrollableComponent;
import moulton.scalable.utils.MenuComponent;

/**
 * A menu component that holds a history of text. A scroll bar can be added to see more than is able to be shown in the
 * specified rectangle. Even if no scroll bar is added, the text history is still functional, just static.
 * @author Matthew Moulton
 */
public class TextHistory extends MenuComponent implements ScrollableComponent{
	/**The list of the strings being held. */
	protected LinkedList<String> history = new LinkedList<>();
	/**The dimensions of the rectangle. */
	protected String width, height;
	/**The font that the history should be drawn in. */
	protected Font font;
	/**The scroll bar to change which strings are displayed.
	 * @see #setScrollBar(ScrollBar)*/
	protected ScrollBar bar;
	/**Whether or not the text history should add new strings to the top or bottom of the list. This correlates with 
	 * NOT {@link ScrollBar#inverseRender}.
	 * @see #setViewMode(boolean)*/
	protected boolean addToTop = false;
	/**The maximum number of messages saved. After this number is reached, the oldest message is deleted once
	 * the newest one is saved.
	 * @see #setMaxMessages(int)
	 * @see #getMaxMessages()*/
	protected int maxMessages;
	/**Whether word splitting is allowed for end of lines. In other words, whether in rendering the text history,
	 * encountering and end of line will force previous characters (until a break) to the next line.
	 * Break characters include space, new line, and hyphens. Defaults to false.
	 * @see #allowsWordSplitting()
	 * @see #allowWordSplitting(boolean)*/
	protected boolean wordSplitting = false;
	/**Whether each text displayed should be separated from other texts by a black line. Defaults to false.
	 * @see #setTextDemarkation(boolean)
	 * @see #getTextDemarkation()*/
	protected boolean textDemarkation = false;
	/**Whether or not this component should render a black outline on the border of the component.
	 * @see #setOutline(boolean)
	 * @see #getOutline()*/
	protected boolean outline = false;
	/**The color with which the messages in {@link #history} are printed.
	 * @see #setTextColor(Color)*/
	protected Color textColor = Color.BLACK;

	/**
	 * Creates a new TextHistory component and adds it to the parent panel by {@link MenuComponent#MenuComponent(Panel,
	 * int, int)}.
	 * @param parent the parent panel that this component will reside on
	 * @param x the x-component this is in the parent's grid
	 * @param y the y-component this is in the parent's grid
	 * @param font the font that will display the different messages in {@link #history}
	 * @param addToTop whether messages should be added and displayed top-down or bottom-up ({@link #addToTop})
	 * @param maxMessages the maximum number of messages that this will hold at a time ({@link #maxMessages})
	 */
	public TextHistory(Panel parent, int x, int y, Font font, boolean addToTop, int maxMessages) {
		super(parent, x, y);
		this.font = font;
		this.addToTop = addToTop;
		this.maxMessages = maxMessages;
	}
	/**
	 * Creates a new TextHistory component and adds it to the parent panel by {@link MenuComponent#MenuComponent(Panel,
	 * String, String)}.
	 * @param parent the parent panel that this component will reside on
	 * @param x the string expression determining the x-position of this component on the parent panel
	 * @param y the string expression determining the y-position of this component on the parent panel
	 * @param w the string expression determining the component's width
	 * @param h the string expression determining the component's height
	 * @param font the font that will display the different messages in {@link #history}
	 * @param addToTop whether messages should be added and displayed top-down or bottom-up ({@link #addToTop})
	 * @param maxMessages the maximum number of messages that this will hold at a time ({@link #maxMessages})
	 */
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
		if(getGridLocation()==null) {
			x = xx + solveString(this.x, ww, hh);
			y = yy + solveString(this.y, ww, hh);
			// variant for input ending points instead of widths indicated by a starting question
			if (this.width.charAt(0) == '?') {
				//solve for the ending point
				int x2 = xx + solveString(this.width.substring(1), ww, hh);
				//deduce the width
				w = x2 - x;
			} else
				w = solveString(this.width, ww, hh);
			
			if (this.height.charAt(0) == '?') {
				int y2 = yy + solveString(this.height.substring(1), ww, hh);
				h = y2 - y;
			} else
				h = solveString(this.height, ww, hh);
		}else {
			x = xx;
			y = yy;
			w = ww;
			h = hh;
		}
		//there must be texts to display to draw them		
		if(history.size()>0){
			//set font
			if(textResize()) {
				int fontSize = getTextVertResize(font.getSize());
				if(fontSize < 1) //if the font is so small you cannot see it, there is nothing left to do here.
					return;
				g.setFont(new Font(font.getFontName(), font.getStyle(), fontSize));
			} else
				g.setFont(font);

			FontMetrics metrics = g.getFontMetrics();
			int numberOfLinesShown = h/metrics.getHeight();
			String[] texts; //the actual lines to be shown
			int textIndex; //the index that texts are taken from
			int textMax; //the index of the max text shown
			boolean allShown = true; //whether all the texts have been shown (serves to indicate redo)
			int endCutOff; //the number of lines cut off from the end
			boolean shouldReset; //whether rendering should be redone to incorporate more texts
			boolean lineSeparate[] = new boolean[numberOfLinesShown];
			do {
				//set the initialization values
				shouldReset = false;
				texts = new String[numberOfLinesShown];
				textMax = 0;
				endCutOff = 0;
				textIndex = history.size()-1; //show the latest text
				int i = 0; //the index from start where the next line is drawn onto texts
				if(bar!=null) {
					textIndex-=bar.getOffset();
					if(bar.getOffset()>0) //some texts were skipped, so not all was shown
						allShown = false;
				}
				//sequentially add text bits into this list, then after it is processed, add
				//them to the texts array in the correct order for the adding direction
				LinkedList<String> thisText = new LinkedList<>();
				while(textIndex>-1) {
					thisText.clear(); //reset when it needs to be used again
					String remainder = history.get(textIndex); //get a text
					String line = "";
					while(remainder.length()>0) {
						line += remainder.charAt(0);
						remainder = remainder.substring(1);
						//if the line is too long
						if(metrics.stringWidth(line)>w || line.charAt(line.length()-1)=='\n') {
							//find the break
							String[] afterBreak = lineSplit(line, remainder);
							remainder = afterBreak[1];
							//the line is just the right length now. add to the list
							thisText.add(afterBreak[0]);
							line = "";
						}
					}//if there was any left over, it should be added too
					if(!line.isEmpty())
						thisText.add(line);
					//that text was finished, now add it to the draw array
					//if we add to top, then we need the to process forwards. Otherwise we will process reverse
					boolean textsFull = false; //break out of even getting more texts
					while(!thisText.isEmpty()) {
						if(addToTop)
							texts[i++] = thisText.pollFirst();
						else
							texts[i++] = thisText.pollLast();
						if(i>=numberOfLinesShown) { //texts is full
							//the number of lines in this text that were cut off
							endCutOff = thisText.size();
							if(thisText.size() + textIndex>0)
								allShown = false;
							textsFull = true; //pull out
							break;
						}
					}if(textsFull) //we don't need to get any more text lines
						break;
					//set line demarkation
					if(textDemarkation)
						lineSeparate[i] = true;
					//get the next text index
					textIndex--;
				}
				//finds the maximum text offset used
				textMax = texts.length-1;
				for(int ii=0; ii<texts.length; ii++) {
					if(texts[ii] == null) {
						textMax = ii-1;
						break;
					}
				}
				//see whether a reset is necessary
				/*If available lines are not being used and there are more lines needed to be shown, reset
				 *the offset for the scroll bar and render again (shouldRender=true).
				 *This could occur if the text history is resized larger when the bar's offset is nonzero.*/
				shouldReset = (bar != null) && (textMax+1 < numberOfLinesShown && !allShown && bar.getOffset()!=0);
				if(shouldReset) {
					//there is one exception: if the next text would be too long to show, some white space is allowed
					int availableLines = numberOfLinesShown-(textMax+1);
					String rem = history.get(history.size()-bar.getOffset()); //get one text before start
					//find how many lines the text will take
					int lines = 1;
					String line = "";
					while(!rem.isEmpty() && lines<=availableLines) {
						line += rem.charAt(0);
						rem = rem.substring(1);
						//if line is too long
						if(metrics.stringWidth(line)>w || line.charAt(line.length()-1)=='\n'){
							String[] afterBreak = lineSplit(line, rem); //split the line
							rem = afterBreak[1];
							line = ""; //reset the line
							lines++;
						}
					}
					if(lines>availableLines)
						shouldReset = false; //exception met, don't reset
					else //decreases the offset down until all possible is accommodated
						bar.setOffset(bar.getOffset()- availableLines);
				}
			}while(shouldReset);
			
			//update the scroll bar
			if(bar!=null) {
				if(!allShown) { //if there were some lines not shown, bar should be editable
					bar.setEditable(true);
					/*To find the total number of offsets, find the initial offset texts (assume one line per
					 *text), then add how many lines were shown, then add how many lines were cut off at the
					 *end (lines from the text and remaining texts at one line per text).*/
					int totalOffs = bar.getOffset() + numberOfLinesShown;
					if(textIndex>-1) //if something was cut off
						totalOffs += textIndex + endCutOff;
					if(bar.getTotalOffs()!=totalOffs)
						bar.setTotalOffs(totalOffs,false);
					int barOffs = numberOfLinesShown; //how many were shown
					bar.setBarOffs(barOffs);
				}else {
					bar.setEditable(false);
				}
			}
			
			//print now
			g.setColor(textColor);
			int remainderSpace = (h - numberOfLinesShown*metrics.getHeight())/2;
			for(int i=0; i<textMax+1; i++){
				String write = texts[i];
				if(addToTop) {
					g.drawString(write, x, y+ i*metrics.getHeight() +metrics.getAscent() +remainderSpace);
					if(lineSeparate[i]) {
						int lineY = y+i*metrics.getHeight() + remainderSpace;
						g.drawLine(x, lineY, x+w, lineY);
					}
				}else {
					g.drawString(write, x, y+ (numberOfLinesShown-1-i)*metrics.getHeight() +
							metrics.getAscent() + remainderSpace);
					if(lineSeparate[i]) {
						int lineY = y+ (numberOfLinesShown-i)*metrics.getHeight() + remainderSpace;
						g.drawLine(x, lineY, x+w, lineY);
					}
				}
			}
		}
		//draw the outline
		if(outline) {
			g.setColor(Color.BLACK);
			g.drawRect(x, y, w, h);
		}
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
	
	/**
	 * Sets whether word splitting on ends of lines is allowed
	 * @param allowSplit sets {@link #wordSplitting}
	 */
	public void allowWordSplitting(boolean allowSplit) {
		this.wordSplitting = allowSplit;
	}
	/**
	 * Returns whether word splitting is allowed for this text history
	 * @return the value of {@link #wordSplitting}
	 */
	public boolean allowsWordSplitting() {
		return wordSplitting;
	}

	@Override
	public ScrollBar getWidthScrollBar() {
		return null;
	}

	@Override
	public ScrollBar getHeightScrollBar() {
		return bar;
	}
	
	/**Returns whether each text displayed should be separated by a black line.
	 * @return {@link #textDemarkation}*/
	public boolean getTextDemarkation() {
		return textDemarkation;
	}
	/**Sets whether each text displayed should be separated by a black line.
	 * @param textDemarkation sets the value of {@link #textDemarkation}*/
	public void setTextDemarkation(boolean textDemarkation) {
		this.textDemarkation = textDemarkation;
	}
	
	/**
	 * Finds a break character, if any, and splits the text from line into remainder where necessary
	 * @param line the text that needs to be split, returned as the first argument in the array
	 * @param remainder the text that is left over, returned as the first argument in the array
	 * @return after the line is split, the results are given as {line, remainder}
	 */
	private String[] lineSplit(String line, String remainder) {
		boolean wordSplit = allowsWordSplitting();
		if(!wordSplit) {
			int ii=line.length()-1;
			for(; ii>-1; ii--) {
				char c = line.charAt(ii);
				if(c == '\n' || c == ' ') {
					//add it back to the remainder
					remainder = line.substring(ii+1) + remainder;
					line = line.substring(0,ii);
					break;
				}else if(c == '-') {
					if(ii<line.length()-1) {
						//keep the - on this line
						remainder = line.substring(ii+1) + remainder;
						line = line.substring(0, ii+1);
					}else {
						remainder = line.substring(ii) + remainder;
						line = line.substring(0, ii);
					}
					break;
				}
			}
			if(ii == -1) { //no break character found, split the word
				wordSplit = true;
			}
		}if(wordSplit){
			int length = line.length();
			remainder = line.charAt(length-1) + remainder;
			line = line.substring(0, length-1);
		}
		return new String[] {line, remainder};
	}
	
	/**
	 * Sets whether or not the clickable should display a black outline on its border.
	 * @param outline {@link #outline}
	 */
	public void setOutline(boolean outline){
		this.outline = outline;
	}
	/**
	 * Returns whether or not the clickable is displaying a black outline on its border
	 * @return {@link #outline}
	 */
	public boolean getOutline(){
		return outline;
	}
	
	/**Sets the color that messages are printed with in {@link #render(Graphics, int, int, int, int)}.
	 * @param color the color to replace {@link #textColor}*/
	public void setTextColor(Color color) {
		textColor = color;
	}

}