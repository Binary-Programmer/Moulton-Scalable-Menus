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
 * A menu component that holds a history of text. A scroll bar can be added to see more than is able
 * to be shown in the specified rectangle. Even if no scroll bar is added, the text history is still
 * functional, just static. The scroll bar should be saved as {@link #bar}. New lines can be added by
 * use of {@link #addToList(String...)}.
 * <p>
 * A couple of optional features specific to text history include:<ul>
 * <li>{@link #addToTop} defines the view mode, or in other words, whether most recent messages should be displayed at the top or the bottom.
 * <li>{@link #maxMessages} limits how many messages the text history can hold at a time. If more are added, older lines are deleted.
 * <li>{@link #textDemarkation} decides whether each entry should be separated by a separating line similar to an outline.
 * <li>{@link #wordSplitting} defines whether words can be split on ends of lines, or whether lines can only split on break characters.
 * </ul><p>
 * Although it is highly recommended to add entries to the text history by the provided method, if
 * the list needs to be accessed directly by a subclass, {@link #history} is where the entries are
 * internally saved.
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
	/**Whether or not the text history should add new strings to the top or bottom of the list. This
	 * correlates with NOT {@link ScrollBar#inverseRender}.
	 * @see #setViewMode(boolean)*/
	protected boolean addToTop = false;
	/**The maximum number of messages saved. After this number is reached, the oldest message is
	 * deleted once the newest one is saved.
	 * @see #setMaxMessages(int)
	 * @see #getMaxMessages()*/
	protected int maxMessages;
	/**Whether word splitting is allowed for end of lines. In other words, whether in rendering the
	 * text history, encountering and end of line will force previous characters (until a break) to
	 * the next line. Break characters include space, new line, and hyphens. Defaults to false.
	 * @see #getWordSplitting()
	 * @see #setWordSplitting(boolean)*/
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
	/**Saved scroll coordinates found from most recent render.
	 * @see #getActiveScrollCoordinates()*/
	protected int[][] scrollCoords = new int[2][4];

	/**
	 * Creates a new TextHistory component and adds it to the parent panel by
	 * {@link MenuComponent#MenuComponent(Panel, int, int)}.
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
	 * Creates a new TextHistory component and adds it to the parent panel by
	 * {@link MenuComponent#MenuComponent(Panel, String, String)}.
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
		if(parent != null)
			scrollCoords = parent.handleOffsets(new int[] {x, x+w, x+w, x}, new int[] {y, y, y+h, y+h}, this); 				
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
							LineBreak result = LineBreak.check(wordSplitting, line, remainder);
							remainder = result.REMAINDER;
							//the line is just the right length now. add to the list
							thisText.add(result.LINE);
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
						if(metrics.stringWidth(line)>w || line.charAt(line.length()-1)=='\n') {
							LineBreak result = LineBreak.check(wordSplitting, line, rem);
							rem = result.REMAINDER;
							line = result.LINE;
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
				int totalOffs = bar.getOffset() + numberOfLinesShown;
				if(!allShown) { //if there were some lines not shown, bar should be editable
					bar.setEnabled(true);
					/*To find the total number of offsets, find the initial offset texts (assume one line per
					 *text), then add how many lines were shown, then add how many lines were cut off at the
					 *end (lines from the text and remaining texts at one line per text).*/
					if(textIndex>-1) //if something was cut off
						totalOffs += textIndex + endCutOff;
				}else {
					bar.setEnabled(false);
				}
				if(bar.getTotalOffs()!=totalOffs)
					bar.setTotalOffs(totalOffs);
				bar.setBarOffs(numberOfLinesShown); //how many were shown
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
	 * Adds the string(s) to {@link #history} and changes {@link ScrollBar#totalOffs} of {@link #bar} if not null.
	 * @param s The string(s) that should be added.
	 */
	public void addToList(String ...s){
		for(int i=0; i<s.length; i++)
			history.add(s[i]);
		while(history.size()>maxMessages) {
			history.removeFirst();
		}
		if(bar!=null && bar.getOffset()>0) {
			//change the offset to keep pace with the previously shown texts
			bar.setTotalOffs(bar.getTotalOffs()+s.length);
			bar.setOffset(bar.getOffset()+s.length);
		}
	}

	/**
	 * Sets the scroll bar so the text history can show more than just recent messages. <br>
	 * Will also call {@link #forceScrollBarMatch()} to coincide with {@link #addToTop}.
	 * @param bar the scroll bar to replace {@link #bar}.
	 */
	public void setScrollBar(ScrollBar bar) {
		this.bar = bar;
		forceScrollBarMatch();
	}
	
	/**
	 * Sets whether this text history should display most recent additions at the top (true) or the bottom (false)
	 * @param recentAtTop value to be saved as {@link #addToTop}
	 */
	public void setViewMode(boolean recentAtTop) {
		addToTop = recentAtTop;
		forceScrollBarMatch();
	}
	
	/**
	 * Two attributes of the text history must match the scroll bar's analogous attributes for rendering
	 * and scrolling to operate correctly. Thus, whenever the {@link #bar} or {@link #addToTop} is altered,
	 * the effect must occur in the connected scroll bar.
	 */
	protected void forceScrollBarMatch() {
		if(bar != null) {
			bar.renderInverse(!addToTop);
			int scrollRate = bar.getScrollRate();
			if(scrollRate>-1 ^ addToTop) { //if positivity and addToTop don't match
				//negate the scrollRate
				scrollRate *= -1;
			}
			bar.setScrollRate(scrollRate);
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
	public void setWordSplitting(boolean allowSplit) {
		this.wordSplitting = allowSplit;
	}
	/**
	 * Returns whether word splitting is allowed for this text history
	 * @return the value of {@link #wordSplitting}
	 */
	public boolean getWordSplitting() {
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
	/**
	 * Sets whether each text displayed should be separated by a black line.
	 * @param textDemarkation sets the value of {@link #textDemarkation}
	 */
	public void setTextDemarkation(boolean textDemarkation) {
		this.textDemarkation = textDemarkation;
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
	
	/**
	 * Sets the color that messages are printed with in {@link #render(Graphics, int, int, int, int)}.
	 * @param color the color to replace {@link #textColor}
	 */
	public void setTextColor(Color color) {
		textColor = color;
	}
	
	@Override
	public int[][] getActiveScrollCoordinates() {
		return scrollCoords;
	}

}