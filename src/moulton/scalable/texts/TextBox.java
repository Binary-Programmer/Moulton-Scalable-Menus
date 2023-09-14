package moulton.scalable.texts;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;

import moulton.scalable.clickables.Clickable;
import moulton.scalable.containers.Panel;
import moulton.scalable.draggables.DraggableComponent;
import moulton.scalable.draggables.ScrollBar;
import moulton.scalable.draggables.ScrollableComponent;
import moulton.scalable.utils.MenuSolver.Expression;

/**
 * This is a text box that users can enter data into, the available characters for which is defined by
 * {@link TextFormat}. This class also has the functionality of mouse dragging to select multiple characters
 * to be manipulated, and thereby offers support for copy, cut, and paste functions.
 * <p>
 * The text that is contained in the box is saved as {@link #message}. A hint (if any) is saved as
 * {@link #hint}. If a scroll bar is to be associated with this text box, use {@link #textScroller}.
 * <p>
 * Beyond the typical function, there are a few custom attributes for the text box that can be enabled or disabled:
 * <ul>
 * <li>{@link #wordSplitting} defines whether lines can be split on words or only on break characters.
 * <li>{@link #hasVirtualSpace} decides whether the text box can have text in its message that is saved but not shown
 * <li>{@link #cutOffMark} determines whether the box should have a visual indication if any of its text is in virtual space.
 * <li>{@link #charMax} sets whether the text box has a maximum number of characters.
 * <li>{@link #charMask} is the character to be used instead of displaying the actual message (used for security, for example: password boxes).
 * <li>{@link #deselectOnEnter} determines whether an input of enter will deselect the text box.
 * <li>{@link #acceptEnter} defines whether this text box can have the \n character input to its message. Not recommended to be true if {@code deselectOnEnter} is true.
 * <li>{@link #hotkeyEnabled} decides whether hot keys (such as copy, cut, and paste) should be usable with this text box. All hot keys that this enables are defined by {@link HotKeyTextComponent}.
 * <li>{@link #clickSelectsAll} determines whether all the text should be selected when the text box is set to clicked.
 * </ul>
 * @author Matthew Moulton
 */
public class TextBox extends Clickable implements DraggableComponent, HotKeyTextComponent, ScrollableComponent {
	//core fields
	/**The display string of the text box.
	 * @see #getMessage()
	 * @see #setMessage(String)*/
	protected String message;
	/**Displays this string on the text box when the {@link message} is empty.
	 * @see #setHint(String)
	 * @see #getHint()*/
	protected String hint = null;
	/**Text boxes are rectangles, thus they will be drawn from (x,y) to (x+width,y+width)*/
	protected Expression width, height;
	/**The font used to display {@link #message}*/
	protected Font font;
	/**The background color used if editable. If not enabled, white is used.*/
	protected Color color;
	/**The formatting scheme for this text box. Determines what characters are valid in the {@link #message}.
	 * @see #getTextFormat()
	 * @see #setTextResizeFactor(int)*/
	protected TextFormat format = null;
	/**The alignment of the text to be rendered. Defaults to left alignment.*/
	protected Alignment alignment = Alignment.LEFT_ALIGNMENT;
	/**The color of the text drawn in the box. Defaults to black.
	 * @see #getTextColor()
	 * @see #setTextColor(Color)*/
	protected Color textColor = Color.BLACK;
	/**The color of the box when touched.
	 * @see #setTouchedColor(Color)*/
	protected Color colorTouched = null;
	/**The string measured for buffering the sides in rendering. Each side of the text must be at least
	 * half this width from the text box border. The actual padding value is calculated at render time
	 * as to scale with the component's text size. Defaults as the underscore (_).*/
	protected String bufferChar = "_";
	
	//text control fields
	/**Whether or not the blinker is shown. The blinker will automatically blink every {@link #blinkTime}
	 * ms which is kept track of
	 * by {@link #timer} and {@link #timeLast}.
	 * @see #refreshBlinker()*/
	protected boolean showBlinker = false;
	/**The time in milliseconds the blinker should wait before toggling on/off.
	 * Default value is 1000 ms = 1 sec. A value of -1 indicates that the blinker should not toggle. */
	protected int blinkTime = 1000;
	/**The last time the time was added onto timer */
	protected long timeLast;
	/**The sum of time differences since last blink toggle of on/off. */
	protected long timer;
	/**The point at which the blinker is in the message. Also the point where any new input will be inserted. */
	protected int index = 0;
	/**How much the index where the text box starts displaying is shifted. If the text box has more than one
	 * row, a shift will be in rows. Otherwise, shifts will be horizontal. The shift can be controlled by
	 * the connected scroll bar saved as {@link #textScroller}.
	 * @see #setStartShift(int)*/
	protected int startShift = 0;
	/**The scroll bar to change {@link #startShift} and view text in unseen virtual space if allowed
	 * ({@link #hasVirtualSpace}).
	 * @see #getTextScroller()
	 * @see #setTextScroller(ScrollBar)*/
	protected ScrollBar textScroller = null;
	
	//click and selection fields
	/**The index of the mouse click. When the user drags the mouse, clickIndex stays the same but
	 * {@link #index} changes. The selection is defined as the text between the two indices.<p>
	 * Can be determined by {@link #findIndex(int, int)}.*/
	protected int clickIndex = 0;
	/**Whether there currently is a selection of text. Also determines whether {@link #clickIndex} is
	 * relevant. That text selection can be retrieved with {@link #getSelectedText()}.*/
	protected boolean selection = false;
	/**The pixel point of the last mouse click.*/
	protected int[] mouseClickXY = new int[2];
	
	//optional functionality
	/**Whether word splitting is allowed for end of lines. In other words, whether in rendering the text
	 * box, encountering and end of line will force previous characters (until a break) to the next line.
	 * Break characters include space, new line, and hyphens. Defaults to false.
	 * @see #getWordSplitting()
	 * @see #setWordSplitting(boolean)*/
	protected boolean wordSplitting = false;
	/**Whether the hotkey commands, copy, cut, paste, should be usable for this text box. Defaults to true.
	 * @see #isHotKeyEnabled()
	 * @see #setHotkeyEnabled(boolean)*/
	protected boolean hotkeyEnabled = true;
	/**The maximum number of characters that can be held in the text box. -1 indicates no limit, which is
	 * the default.
	 * @see #setCharLimit(int)*/
	protected int charMax = -1;
	/**The character to replace the actual message content. The specified character will be repeated to
	 * match the length of the message when shown. If null, no mask will be applied. If the character is
	 * invisible (value less than 32), then an empty string will be returned. Defaults to null.
	 * @see #setCharMask(Character)
	 * @see #getShowMessage()*/
	protected Character charMask = null;
	/**Whether input can be put outside of the visible box. Defaults to true.
	 * @see #getHasVirtualSpace()
	 * @see #setHasVirtualSpace(boolean)*/
	protected boolean hasVirtualSpace = true;
	/**If the text box {@link #getHasVirtualSpace()} and some of the message is cut off in display, this
	 * will decide whether a small mark will be displayed to indicate the cut off. Defaults to true.
	 * @see #setCutOffMark(boolean)
	 * @see #hasCutOffMark()*/
	protected boolean cutOffMark = true;
	/**Whether this text box should accept 'enter' or 'return' input from the user. Defaults to false.
	 * @see #getAcceptEnter()
	 * @see #acceptEnter(boolean)*/
	protected boolean acceptEnter = false;
	/**Whether the user action of 'enter' or 'return' should deselect this text box. Defaults to true.
	 * @see #getDeselectOnEnter()
	 * @see #deselectOnEnter(boolean)*/
	protected boolean deselectOnEnter = true;
	/**Whether the entire contents of the text box should be selected when the box is set to clicked from
	 * a non-clicked state. Defaults to false.
	 * @see #getClickSelectsAll()
	 * @see #setClickSelectsAll(boolean)*/
	protected boolean clickSelectsAll = false;
	
	//cached
	/**The font metrics used by the Graphics object that was last rendered on*/
	protected FontMetrics fontMetrics;
	
	/**
	 * @param message the string displayed in the box
	 * @param parent the panel that this text box will reside upon
	 * @param x the x coordinate on the screen, given in menu component value format
	 * @param y the y coordinate on the screen, given in menu component value format
	 * @param width the width of the component, given in menu component value format
	 * @param height the height of the component, given in menu component value format
	 * @param font the font for the box
	 * @param color the background color for the box when editable
	 */
	public TextBox(String message, Panel parent, String x, String y, String width, String height, Font font, Color color) {
		super(parent, x, y);
		this.width = solve.parse(width, true, false);
		this.height = solve.parse(height, true, false);
		init(message, color, font);
	}
	/**
	 * @deprecated Use {@link #TextBox(String, Panel, String, String, String, String, Font, Color)} and
	 * {@link #setId(String)}
	 */
	@Deprecated(since="1.15")
	public TextBox(String id, String message, Panel parent, String x, String y, String width, String height, Font font, Color color) {
		super(parent, x, y);
		this.id = id;
		this.width = solve.parse(width, true, false);
		this.height = solve.parse(height, true, false);
		init(message, color, font);
	}
	/**
	 * @param message the string displayed in the box
	 * @param parent the panel that this text box will reside upon
	 * @param x the x coordinate of this text box in its parent's grid
	 * @param y the y coordinate of this text box in its parent's grid
	 * @param font the font for the box
	 * @param color the background color for the box when editable
	 */
	public TextBox(String message, Panel parent, int x, int y, Font font, Color color) {
		super(parent,x,y);
		init(message, color, font);
	}
	/**
	 * @deprecated use {@link #TextBox(String, Panel, int, int, Font, Color)} and {@link #setId(String)}
	 */
	@Deprecated(since="1.15")
	public TextBox(String id, String message, Panel parent, int x, int y, Font font, Color color) {
		super(parent,x,y);
		this.id = id;
		init(message, color, font);
	}
	
	/**
	 * Just a few set up properties here. Attempting to cut down on code duplication. This is
	 * private just since it is trivial in purpose.
	 * @param message the message
	 * @param color the color
	 * @param font the font
	 */
	private void init(String message, Color color, Font font) {
		if(message == null)
			message = "";
		this.message = message;
		this.color = color;
		this.font = font;
	}
	
	/**
	 * Sets the text format for this box. The text format determines which characters are valid for
	 * {@link #message}.
	 * @param tf the new text format, saved as {@link #format}.
	 * @return this
	 */
	public TextBox setTextFormat(TextFormat tf){
		this.format = tf;
		return this;
	}
	/**
	 * Returns the current text format.
	 * @return {@link #format}
	 */
	public TextFormat getTextFormat(){
		return format;
	}
	
	/**
	 * Returns the hint for the text box- the text shown when the {@link #message} is empty or null.
	 * @return {@link #hint}
	 */
	public String getHint(){
		return hint;
	}
	
	/**
	 * Sets the hint of the text box.
	 * @param hint the string to replace {@link #hint}.
	 * @return this
	 */
	public TextBox setHint(String hint){
		this.hint = hint;
		return this;
	}
	
	/**
	 * Sets the maximum number of characters that this text box can hold. A charMax of -1 means no limit.
	 * @param charMax {@link #charMax}.
	 * @return this
	 */
	public TextBox setCharLimit(int charMax) {
		this.charMax = charMax;
		return this;
	}
	
	/**
	 * Sets the alignment for the text displayed
	 * @param align the new alignment for the text box
	 */
	public void setAlignment(Alignment align){
		this.alignment = align;
	}
	
	/**
	 * Finds the index in the message that is at the point (mouseX, mouseY)
	 * @param mouseX the x-coordinate of the mouse
	 * @param mouseY the y-coordinate of the mouse
	 * @return the index in {@link #message}
	 */
	protected int findIndex(int mouseX, int mouseY) {
		if(fontMetrics!=null && message!=null && !message.isEmpty()) {
			//Remember that clickBoundary is a two-dimensional array. The 0th index holds x coords,
			//the 1st holds y coordinates.
			//the TextBox formats the points in click boundary such that point 0 is the top-left,
			//point 1 is the top-right, point 2 is the bottom right, and point 3 is the bottom-left
			int topY = clickBoundary[1][0];
			int bottomY = clickBoundary[1][3];
			int leftX = clickBoundary[0][0];
			int rightX = clickBoundary[0][1];
			int hh = bottomY-topY;
			int ww = rightX-leftX;
			
			int hheight = fontMetrics.getHeight();
			String line = "";
			int rows = hh/hheight;
			int centeringY = (hh-(rows*hheight))/2;
			//find which row the click was on
			int row = rows; //default to bottom (impossible index)
			int bufferWidth = fontMetrics.stringWidth(bufferChar)/2;
			if(mouseY-topY < 2*fontMetrics.getLeading() + centeringY) { //above the top row
				if(rows>1) { //multi-line box
					//the index will be the end of the row above the start shift
					if(startShift>0)
						row = -1;
					else //no start shift, and top of row just means index 0
						return 0;
				}else { //one-line box
					//for one-liners, just treat this as the very beginning since there is only one row
					return 0;
				}
			}else {
				//start at top and work down
				for(int i=0; i<rows; i++) {
					//find if the click location was less than the next row:
					if(mouseY-topY < hheight*(i+1) + centeringY -1){
						row = i;
						break;
					}
				}
			}if(row == rows) { //so if it was unchanged, below the last row
				//if we have multiple rows and virtual space, then we have to keep processing to find which line
				if(rows<=1 || !getHasVirtualSpace()) //there is only one row or virtual space is off
					return message.length();
			}
			
			//checks for single row boxes of left and right
			if(rows <= 1) {
				//if the mouse's x was in the left margin, then just one short of shift
				if(mouseX < leftX + bufferWidth) {
					if(startShift>0)
						return startShift-1;
					return 0;
				}
				//or for the right margin, would be the end for shift once more. check for shift modifications later
			}
			
			int sum = 0; //variable that will keep track of the number of letters shown thus far
			String rem = message; //the remainder to process. We only find an index in the message- not hint
			
			//shift modifications
			int shift=0;
			if(rows==1 && startShift>0 && message.length()>startShift) {
				//if the mouse in right margin
				if(mouseX > rightX - bufferWidth && message.length()>startShift+1) {
					shift = startShift+1;
				}else
					shift = startShift;
				rem = message.substring(shift);
				sum += shift;
				shift = 0; //need to reset to not interfere with the multi-line computations
			}
			if(rows>1)
				shift = startShift;
			
			int underscoreWidth = fontMetrics.stringWidth(bufferChar);
			/*The inside width is the difference of the pixel width of the box and the underscore width.
			 *This leaves spacing for half an underscore on both sides. */
			int insideWidth = ww - underscoreWidth;
			
			boolean charConsumed = false; //a character consumed on the most recent line break
			if(rem != null && !rem.isEmpty()){
				//it only needs to go to row (instead of the whole message) because that is the data we need
				for(int i=-shift; i<=row; i++) {
					//the text that fits on the currently processed row. If this isn't the row we need, the
					//text will be reset when processing progresses to the next line.
					String text = "";
					int wwidth = 0; //the width in pixels of text on the line
					
					charConsumed = false; //resets for each line
					while(wwidth<=insideWidth && !rem.isEmpty()) {
						//transfer a character from rem to text
						char nextChar = rem.charAt(0);
						text += nextChar;
						rem = rem.substring(1);
						if(nextChar == '\n') //new line will force too long
							wwidth = insideWidth+1;
						else
							wwidth = fontMetrics.stringWidth(text); //find the new width
					}
					
					if(wwidth>insideWidth && rows>1) { //the width is too long
						LineBreak result = LineBreak.check(getWordSplitting(), text, rem);
						text = result.LINE;
						rem = result.REMAINDER;
						charConsumed = result.CHAR_CONSUMED;
					}
					if(i!=row)
						sum += text.length() + (charConsumed? 1:0);
					else
						line = text;
				}
			}
			//now we have the line
			int i = line.length();
			//this line could be left aligned, center aligned, or right aligned
			int here = getLineXOffs(line, underscoreWidth/2, leftX, ww);
			
			//start at the end, and shrink the string until we have nothing left
			//i gets reused as the last j
			
			for(int j=i; j>-1; j--) {
				//if it is less now, the index is one more than this
				int newWidth = fontMetrics.stringWidth(line.substring(0, j));
				if(here + newWidth < mouseX) {
					int oldWidth = fontMetrics.stringWidth(line.substring(0, i));
					//see which one is closer
					if(mouseX < here + newWidth + (oldWidth-newWidth)/2)
						i = j;
					break;
				}
				i = j;
			}
			//find the index of the letter we have now with i
			return i + sum;
		}
		//if something didn't work, like if the message is empty or nothing, then return the message length, ie the index at the end of the message
		return message.length();
	}
	/**
	 * Gets the x-offset of where a line should begin based on the {@link #alignment}.
	 * @param line the line to be rendered or to otherwise get the offset for.
	 * @param bufferWidth the width of the buffer between the side of the text box and the beginning of
	 * the text on that line. Typically half the width of an underscore character (_).
	 * @param leftX the x value where the text box begins. The left-most x.
	 * @param width the width of the box
	 * @return the x-offset of where the given line should be drawn.
	 */
	protected int getLineXOffs(String line, int bufferWidth, int leftX, int width) {
		int here = leftX;
		switch(alignment) {
		case LEFT_ALIGNMENT:
			here += bufferWidth;
			break;
		case CENTER_ALIGNMENT:
			here += width/2 - (fontMetrics.stringWidth(line))/2;
			break;
		case RIGHT_ALIGNMENT:
			here += width - fontMetrics.stringWidth(line)-bufferWidth;
			break;
		}
		return here;
	}
	
	/**
	 * Called in rendering. Gives the fill color of the box. If the box is not editable, white is used.
	 * If the box is touched but does not have focus, colorTouched is used. Otherwise, the normal color
	 * is returned.
	 * @return the applicable color for rendering the fill of the box
	 */
	public Color getFillColor() {
		if (!isEnabled())
			return Color.WHITE;
		if(isTouched() && !isClicked() && colorTouched != null)
			return colorTouched;
		
		return color;
	}

	/**
	 * Renders this text box with the provided graphics. If the box is displaying the message, is clicked,
	 * and allows virtual text, it will shift the starting position ({@link #startShift}) to display where
	 * the blinker would be placed. <p>
	 * Calls {@link #drawTextLines(Graphics, String[], int, int, int, int, int, int)} and
	 * {@link #drawBlinker(Graphics, int, int, int, int, int, int, int, int, int)} in the process.
	 * These can be overridden to easily adapt the functionality for subclasses.
	 */
	@Override
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		Rectangle rect = this.getRenderRect(xx, yy, ww, hh, width, height);
		int x = rect.x;
		int y = rect.y;
		int w = rect.width;
		int h = rect.height;
		
		Color fillColor = getFillColor();
		if(fillColor != null) {
			g.setColor(fillColor);
			g.fillRect(x, y, w, h);			
		}
		if(parent != null)
			defineClickBoundary(parent.handleOffsets(new int[] {x, x+w, x+w, x}, new int[] {y, y, y+h, y+h}, this));
		g.setColor(enabled? textColor: textColor.brighter());
		if (outline)
			g.drawRect(x, y, w - 1, h - 1);
		
		if(textScroller != null)
			//try to get the new start shift from the scroll bar
			startShift = textScroller.getOffset();

		// draw the text
		if(clicked && blinkTime!=-1){
			timer+=System.currentTimeMillis()-timeLast;
			timeLast = System.currentTimeMillis();
			if(timer>blinkTime){
				//if the timer fell behind, catch up
				for(int i=0; i<timer/blinkTime; i++)
					showBlinker = !showBlinker;
				timer = 0;
				timeLast = System.currentTimeMillis();
			}
		}
		if(textResize())
			g.setFont(new Font(font.getFontName(), font.getStyle(), getTextVertResize(font.getSize())));
		else
			g.setFont(font);
		fontMetrics = g.getFontMetrics();
		//draw the string correctly, not drawing outside of the box and conforming to lines
		int hheight = fontMetrics.getHeight();
		//an array to be filled by the text to be displayed. The size of this array is the visual maximum
		String texts [] = new String[h/hheight];
		//the amount that the text is offset rendered in the box
		int textOffset = fontMetrics.getAscent() + fontMetrics.getLeading() + (h-(texts.length*hheight))/2;

		boolean messageShown;
		boolean isClicked = isClicked(); //we should only shift to the blinker if the box is selected
		//the string that will be displayed. usually message but sometimes the hint
		String rem;
		//message should always be not null. Even if it is empty, it should still be not null
		if(message.length()>0 || hint==null) {
			rem = getShowMessage();
			g.setColor(Color.BLACK);
			messageShown = true;
		}else {
			rem = hint;
			g.setColor(Color.DARK_GRAY);
			messageShown = false;
		}

		int blinkerRow = 0;
		int blinkerX = 0;
		int totalTextLength = rem.length();
		//If we should set the blinker, then the set variable should be false. If we don't set the 
		//blinker, then even though it hasn't been set, still pretend like it is.
		boolean blinkerSet = !isClicked();
		int underscoreWidth = fontMetrics.stringWidth(bufferChar);

		if(h/hheight<1)
			return;
		texts = new String[h/hheight];

		//draw the string correctly, not drawing outside of the box and conforming to lines
		int blinkerPlace = 0;
		boolean setClick = !selection;
		int clickRow = 0;
		int clickPlace = 0;
		int shift = 0;
		if(messageShown) { //shift only used when the message is shown
			if(startShift > rem.length())
				startShift = rem.length();
			shift = startShift;
		}

		boolean finalLine = false; //in cases of newlines or overflow, we need to place an extra line
		/*The inside width is the difference of the pixel width of the box and the underscore width.
		 *This leaves spacing for half an underscore on both sides. */
		int insideWidth = w - underscoreWidth;
		//the iterator of the line we are working with
		int i = 0;
		//In some cases, we need to go back into the line loop after we thought that we finished
		boolean redo;
		do {
			redo = false;
			finalLine = false;
			while(i<texts.length) { //while we haven't exceeded the number of allotted lines
				//create the text if needed
				if(i<texts.length && texts[i] == null) 
					texts[i] = "";
				
				//check for the blinker position
				if(!blinkerSet && rem.length()<=totalTextLength-index){
					blinkerSet = true;
					blinkerRow = i;
					blinkerPlace = texts[i].length();
					blinkerX = fontMetrics.stringWidth(texts[i]);
				}if(!setClick && rem.length()<=totalTextLength-clickIndex){
					setClick = true;
					clickRow = i;
					clickPlace = texts[i].length();
				}
				
				//checked for click and blinker first (for end of line)
				if(rem.isEmpty()) //but if rem is empty, quit out
					break;
				
				//add a character onto the line
				texts[i] += rem.charAt(0);
				rem = rem.substring(1);
				
				//shift for single-line boxes
				if(shift>0 && texts.length==1) {
					//if the blinker has been set, the shift needs to decrease
					if(blinkerSet && isClicked) {
						startShift -= shift; //adjust for next time
						shift = 0; //render normal for the rest
					}else {
						shift--;
						texts[0] = texts[0].substring(1);
						if(setClick)
							clickPlace--;
					}
					continue;
				}
				
				//if the line is now too long to fit in row
				if(fontMetrics.stringWidth(texts[i])>insideWidth || texts[i].charAt(texts[i].length()-1)=='\n') {
					if(texts.length>1) {
						LineBreak split = LineBreak.check(getWordSplitting(), texts[i], rem);
						texts[i] = split.LINE; //for index calculation
						rem = split.REMAINDER;
						
						//if the line break yielded nothing after and this is the end, then we need to
						//signal that there is still a line down there to access
						if(i==texts.length-1 && rem.isEmpty())
							finalLine = true;
						
						//check if the blinker or click is affected by the change
						int splitLocation = split.LINE.length();
						if(blinkerSet && blinkerRow == i && blinkerPlace>splitLocation)
							blinkerSet = false;
						if(setClick && clickRow == i && clickPlace>splitLocation)
							setClick = false;
						
						//shift modifications
						if(shift>0) {
							if(blinkerSet && isClicked) { //if the blinker has already been placed
								//then we should shift to the blinker's place
								//we will only need to shift up if startShift is greater than 0
								startShift -= shift; //adjust for next time
								shift = 0; //render normal for the rest
								i++;
							}else {
								shift--;
								texts[i] = ""; //reset the line
								if(setClick) //if the click position has already been set
									clickRow--;
							}
						}else
							i++;
					}else {
						rem = texts[i].charAt(texts[i].length()-1) + rem;
						texts[i] = texts[i].substring(0, texts[i].length()-1);
						i++;
					}
				}
			}
			
			/* 
			 * As we have reached the end of the allotted rows, there are two distinct problems that
			 * may have arisen during earlier rendering. It is possible that neither has occurred,
			 * but we still need to check for both.
			 * 
			 * First issue: If we have not placed the blinker yet but we were supposed to. In this
			 * occurrence, the blinker position must be later on in the message. The procedure by
			 * which we render (going through all of the message even when startShift will make us
			 * ignore those lines), guarantees that the blinker position could not have been earlier.
			 * If it was, we would have found it. Therefore, the position *must* be later on in the
			 * message.
			 * 
			 * Second issue: It is a very rare case, but sometimes the frame of the application is
			 * resized, and a text box acquires more space. Instead of having empty lines at the
			 * bottom with a start shift, the text box should try to render as much as possible.
			 */
			//Check for first issue: If the blinker has not been set yet but should have (!setBlinker),
			//and also if virtual space allows us to scroll.
			if(!blinkerSet && getHasVirtualSpace()) {
				//The solution to the first issue is to shift all the rows up and redo the last row.
				//Hopefully that will find the blinker. If not, we will end up here again until we do.
				if(texts.length>1) {
					//shift all rows up
					int ii=1;
					for(; ii<texts.length; ii++) {
						texts[ii-1] = texts[ii];
					}//delete the last row
					texts[ii-1] = "";
					if(setClick && selection) //if click index was altered
						clickRow--;
				}else {
					//get rid of the first character
					texts[0] = (texts[0].length() > 0)? texts[0].substring(1): "";
					if(setClick && selection) //if click index was altered
						clickPlace--;
				}
				redo = true; //go back into the loop
				i--; //do the last line again
				startShift++; //save for next time
			}
			//Check for the second issue: If the entire message has been processed (rem.isEmpty()),
			//but we shifted (startShift>0) and we end up with an unprocessed line at the bottom
			//(texts[texts.length-1]==null). It has to be *unprocessed* since sometimes returns or
			//character overflow can put the blinker on the next line without anything to draw.
			if(rem.isEmpty() && startShift>0 && texts.length>0 && texts[texts.length-1]==null) {
				//if so, we have to do a complete redo, where we will move the shift up to include more
				rem = getShowMessage();
				blinkerSet = false;
				setClick = !selection;
				texts = new String[h/hheight];
				i = 0;
				//decrease startShift to try to capture more text in the box
				shift = --startShift;
				redo = true;
			}
		}while(redo);
		
		//do some more operations on the leftover text if it is the message
		boolean endCutOff = false;
		if(messageShown) {
			//cut off extra if virtual space not allowed
			if(!hasVirtualSpace && !rem.isEmpty()) {
				String tempMessage = getMessage();
				setMessage(tempMessage.substring(0, tempMessage.length()-rem.length()));
				index = message.length();
				//update the scroll bar
				if(textScroller!=null)
					textScroller.setBarOffs(0);
			}
			endCutOff = !rem.isEmpty(); //set this before the scroll bar calculation modifies it
			
			//update the scroll bar
			if(hasVirtualSpace && textScroller!=null) {
				if(texts.length==1) { //one lined text box
					int textLength = 0;
					if(texts[0]!=null)
						textLength = texts[0].length();
					textScroller.setOffsets(totalTextLength, textLength, startShift);
				}else {
					//calculate the total number of lines
					int extraLines = finalLine? 1:0;
					String extraText = "";
					while(!rem.isEmpty()) {
						extraText += rem.charAt(0);
						if(fontMetrics.stringWidth(extraText)>insideWidth) {//the line is now too long
							extraLines++;
							extraText = ""+rem.charAt(0); //start the next line
						}else if(extraText.charAt(extraText.length()-1)=='\n') {
							extraLines++;
							extraText = ""; //reset the text
							
							if(rem.length()==1) //blinker at the very end
								extraLines++;
						}
						rem = rem.substring(1); //remove the used character
					}if(!extraText.isEmpty())
						extraLines++;
					//total lines is the lines shown (texts.length), plus the lines skipped, plus lines left over
					textScroller.setOffsets(startShift+extraLines+texts.length,
								 texts.length, //bar offs is the number of texts shown
								 startShift); //offset for the bar is the startShift
				}
			}
		}
		
		drawTextLines(g, texts, x, y, w, h, underscoreWidth, textOffset);

		//show the blinker if the blinker has been placed and should be shown
		int rowWidth = 0;
		if(texts[blinkerRow] != null)
			rowWidth = fontMetrics.stringWidth(texts[blinkerRow]);
		drawBlinker(g, x, y, w, h, rowWidth, blinkerX, blinkerRow, underscoreWidth, textOffset);
		
		//show text cut off mark
		if(cutOffMark && messageShown) {
			int decrease = hheight - fontMetrics.getDescent();
			if(endCutOff) 
				g.drawLine(x+w-2, y + hheight*(texts.length-1) + textOffset -decrease, x+w-2, (int)(y + hheight*texts.length + textOffset -decrease));
			if(startShift>0)
				g.drawLine(x+1, y + textOffset -decrease, x+1, y + hheight + textOffset -decrease);
		}

		//now to draw the selected portion and selection on top
		if(!selection)
			return;

		//click positions aren't necessarily possible because of shifting. Fix to valid values
		if(clickRow<0) { //it is less than shown row, but set place, so minimum
			clickRow = 0;
			clickPlace = 0;
		}else if(clickPlace<0)
			clickPlace = 0;
		else if(!setClick) { //it hasn't been set, so maximum
			clickRow = texts.length-1;
			if(texts[clickRow] != null)
				clickPlace = (texts[clickRow] != null)? texts[clickRow].length(): 0;
		}
		
		/* A start point and end point needs to be defined for the selection. Although clickIndex is where the click began, backwards dragging is
		 * supported, which requires that we ascertain which is actually first. In rows[] and places[], 0 is the start point, and 1 is the end.
		 */
		int rows[] = {0,0};
		int places[] = {0,0};
		if(clickIndex < index){
			rows[0] = clickRow;
			places[0] = clickPlace;
			rows[1] = blinkerRow;
			places[1] = blinkerPlace;
		}else{
			rows[0] = blinkerRow;
			places[0] = blinkerPlace;
			rows[1] = clickRow;
			places[1] = clickPlace;
		}

		//for each line between the top of the selection and the bottom
		for(int ii=rows[0]; ii<=rows[1]; ii++) {
			//the start and end indices for the selection in this row
			int start = 0;
			int end = texts[ii].length();
			//determine offset, selection width, etc by if this row is the start of the selection
			//the middle, or the end.
			if(ii == rows[0]) { //this is first row of selection
				//set start to be after the end of text preceding the selection
				start = places[0];
			}if(ii == rows[1]) { //this is the last row of selection
				//set the end to be after the selection's end
				end = places[1];
			}//all middle rows will have everything selected

			//define the selection width so the rectangles can be drawn
			int selWidth = fontMetrics.stringWidth(texts[ii].substring(start,end));

			g.setColor(Color.BLACK);
			int xOffs = getLineXOffs(texts[ii], underscoreWidth/2, x, w);
			//draw the rectangles and selection texts
			int centerY = (h - (texts.length*hheight))/2;
			g.fillRect(xOffs + fontMetrics.stringWidth(texts[ii].substring(0,start)), y + hheight*ii + centerY, selWidth, hheight);
			g.setColor(Color.WHITE);
			g.drawString(texts[ii].substring(start,end), xOffs + fontMetrics.stringWidth(texts[ii].substring(0,start)),
					y + hheight*ii + textOffset);
		}
	}
	/**
	 * Draws lines of text configured by the {@link #render(Graphics, int, int, int, int)} method onto the
	 * specified Graphics object. Called by said render method.
	 * @param g the graphics object to draw on
	 * @param texts an array of the lines of texts
	 * @param x the minimum x-value (the left side) of the text box
	 * @param y the minimum y-value (the top side) of the text box
	 * @param w the width of the text box
	 * @param h the height of the text box
	 * @param underscoreWidth the width of an underscore (_) according to the current {@link #fontMetrics}.
	 * @param textOffset a value to correctly center the rows of text in the box. Calculated to be
	 * fontMetrics.getAscent() + fontMetrics.getLeading() + (h-(texts.length*fontMetrics.getHeight()))/2.
	 */
	protected void drawTextLines(Graphics g, String[] texts, int x, int y, int w, int h, int underscoreWidth, int textOffset) {
		int hheight = fontMetrics.getHeight();
		for(int ii=0; ii<texts.length; ii++) {
			if(texts[ii]==null) //the text is done, move onto the next step
				break;
			
			int xx = getLineXOffs(texts[ii], underscoreWidth/2, x, w);
			g.drawString(texts[ii], xx, y + hheight*ii + textOffset);
		}
	}
	/**
	 * Draws the blinker at its correct position, computed by {@link #render(Graphics, int, int, int, int)}.
	 * Draws onto the specified graphics object if the blinker should be shown ({@link #showBlinker}).
	 * Called by said render method.
	 * @param g the graphics object to draw on
	 * @param x the minimum x-value (the left side) of the text box
	 * @param y the minimum y-value (the top side) of the text box
	 * @param w the width of the text box
	 * @param h the height of the text box
	 * @param rowWidth the width in pixels, of the row upon which the blinker should be drawn
	 * @param blinkerX the offset of this blinker's x-value from the start of where the text should be drawn,
	 * dependent upon the {@link #alignment}.
	 * @param blinkerRow the row upon which the blinker should be drawn
	 * @param underscoreWidth the width of an underscore (_) according to the current {@link #fontMetrics}
	 * @param textOffset a value to correctly center the rows of text in the box. Calculated to be
	 * fontMetrics.getAscent() + fontMetrics.getLeading() + (h-(texts.length*fontMetrics.getHeight()))/2.
	 */
	protected void drawBlinker(Graphics g, int x, int y, int w, int h, int rowWidth, int blinkerX, int blinkerRow, int underscoreWidth, int textOffset) {
		int hheight = fontMetrics.getHeight();
		if(showBlinker) {
			int decrease = hheight - fontMetrics.getDescent();
			g.setColor(Color.BLACK);
			int drawY = y + hheight*blinkerRow - decrease + textOffset;
			switch(alignment) {
			case LEFT_ALIGNMENT:
				g.fillRect(x + underscoreWidth/2 + blinkerX, drawY, 2, hheight);
				break;
			case CENTER_ALIGNMENT:
				g.fillRect(x - rowWidth/2 + w/2 + blinkerX, drawY, 2, hheight);
				break;
			case RIGHT_ALIGNMENT:
				g.fillRect(x + w - (rowWidth+underscoreWidth/2) + blinkerX, drawY, 2, hheight);
				break;
			}
		}
	}
	
	@Override
	public int getTouchedCursorType() {
		return Cursor.TEXT_CURSOR;
	}
	
	@Override
	public boolean isDeselectedOnRelease() {
		return false;
	}

	/**
	 * Returns the message displayed on this text box.
	 * @return {@link #message}
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Returns the {@link #message} after applying any {@link #charMask}.
	 * @return the text that should be shown for the message
	 */
	protected String getShowMessage() {
		if(charMask == null)
			return getMessage();
		else {
			String mes = "";
			if(charMask >= ' ') { //if the char mask is something that actually can be printed
				int length = getMessage().length();
				for(int i=0; i<length; i++)
					mes += charMask;
			}
			return mes;
		}
	}
	
	/**
	 * Clears the {@link #message}.
	 */
	public void clearMessage() {
		if (format != null) {
			this.message = format.emptyText();
			if (charMax>-1 && message.length()>charMax)
				message = message.substring(0, charMax);
		}else
			this.message = "";
	}
	
	/**
	 * Parses the new text and combines before + new + after, which is set as {@link #message}
	 * after applying length restrictions (if any).
	 * @param before the before text, which already existed in the message (does not need to be
	 * checked for validity) and is set before parsing out middle (and re-parsing end).
	 * @param newText the new segment of text to be included
	 * @param after text in the message after newText, if any (if not, use "")
	 * @return the length of newText after parsing and constraints (may be used to shift index)
	 */
	protected int parseText(String before, String newText, String after) {
		int newKept, afterKept;
		if (this.format != null) {
			this.message = before; // pre-existing, does not need to be checked for validity by format
			String toCheck = newText + after;
			newKept = 0;
			afterKept = 0;
			for (int i = 0; i < toCheck.length(); i++) {
				char c = toCheck.charAt(i);
				if (format.isValidChar(c)) {
					if (i < newText.length())
						newKept++;
					else
						afterKept++;
					this.message += c;
				}
			}
		}else {
			StringBuilder buf = new StringBuilder(before);
			buf.append(newText);
			buf.append(after);
			message = buf.toString();
			newKept = newText.length();
			afterKept = after.length();
		}
		
		if (charMax>-1 && message.length()>charMax) {
			int diff = message.length() - charMax;
			afterKept -= diff;
			if (afterKept < 0)
				newKept += afterKept;
			message = message.substring(0, charMax);
		}
		return newKept;
	}
	
	/**
	 * Attempts to set {@link #message} to the specified string. If this text box has a TextFormat saved in {@link #format},
	 * the string provided will be parsed as defined in {@link TextFormat#parseText(String)} and the result will be saved as
	 * {@link #message}. Also, the string will be shortened to {@link #charMax} if necessary and if {@link #charMax} has
	 * been set. 
	 * @param string the string provided as a replacement
	 */
	public void setMessage(String string) {
		if(string == null)
			string = "";
		parseText("", string, "");
		
		if (index > message.length())
			index = message.length();
		if (message.isEmpty()) {
			startShift = 0;
			if(textScroller != null) //the scroll bar should be unset
				textScroller.setTotalOffs(0);
		}
	}
	
	/**
	 * Deletes the selection, then appends the given string into {@link #message}. If the append makes 
	 * {@link #message} longer than {@link #charMax} if {@link #charMax} has been set, the tail-end of
	 * the string will be excluded.
	 * Properly handles any new line characters as determined by {@link #acceptEnter}.
	 * @param string the String to append on
	 */
	@Override
	public void appendMessage(String string) {
		//handles any new line characters appropriately
		if(!acceptEnter && string.indexOf('\n')!=-1) {
			String[] sections = string.split("\n");
			string = "";
			if (sections.length > 0) {
				for (int i=0; i<sections.length; i++)
					string += sections[i];
			}
		}
		
		//deletes the selection if there is one
		if(selection)
			deleteSelection(null);
		
		String after = "";
		if (index < message.length())
			after = message.substring(index);
		shiftIndex(parseText(message.substring(0, index), string, after));
	}
	/**
	 * Deletes the selection then removes characters from {@link #message} starting at {@link #index}.
	 * @param chars the number of chars to be removed from the message. If there is a selection, 1 of
	 * these characters will be used to delete it. If chars is positive, characters will be deleted
	 * from right to left (backspace). If chars is positive, characters will be deleted from left to
	 * right (delete).
	 */
	@Override
	public void removeMessage(int chars) {
		//we need to know which direction to delete from
		boolean leftDelete = chars >= 0;
		if(!leftDelete)
			chars *= -1;
		
		//deletes the selection if there is one
		if(selection) {
			deleteSelection(null);
			//then decrements the number of characters to delete before passing on
			chars--;
		}
		//then removes the commanded characters
		if(chars>0) { //if there is still something to delete
			int left, right;
			if (leftDelete) {
				left = index - chars;
				right = index;
			}else {
				left = index;
				right = index + chars;
			}
			
			if (left<0) left = 0;
			if (right<0) right = 0;
			if (left>message.length()) left = message.length();
			if (right>message.length()) right = message.length();
			
			if (leftDelete) {
				index -= chars;
				if(index < 0)
					index = 0;
			}
			
			if (left != right) {
				StringBuilder temp = new StringBuilder();
				if (left > 0)
					temp.append(message.substring(0, left));
				if (right < message.length())
					temp.append(message.substring(right));
				if (format != null)
					message = format.deleteAction(temp.toString());
				else
					message = temp.toString();
			}
		}
		
		if (message.isEmpty()) {
			startShift = 0;
			if (textScroller != null) //the scroll bar should be unset
				textScroller.setTotalOffs(0);
		}
		
		refreshBlinker();
	}
	/**
	 * @param chars the number of characters to delete
	 * @param leftDelete whether the delete should occur from the left (otherwise from right)
	 * @deprecated use {@link #removeMessage(int)} instead
	 */
	@Deprecated(since="1.11")
	public void removeMessage(int chars, boolean leftDelete) {
		removeMessage((leftDelete? 1:-1) * chars);
	}
	
	/**
	 * Deletes the selection for the text box. Assumes that there is a selection to delete. Checks whether
	 * the text box is enabled before the action is performed. Uses {@link #setMessage(String)}, so the
	 * text must follow text box constraints of length and filters.
	 * @param replace the text to replace the deleted selection with. Keep null for a normal deletion.
	 */
	protected void deleteSelection(String replace) {
		if(!enabled)
			return;
		
		int start, end;
		if(clickIndex < index) {
			start = clickIndex;
			end = index;
		}else {
			start = index;
			end = clickIndex;
		}
		String newMessage = "";
		if(start>0)
			newMessage += message.substring(0, start);
		if(replace != null) {
			newMessage += replace;
			//now we need to find the new index:
			//we don't know if any of the pasted text will get filtered out, so we set the message,
			//and that is our index. Then we can add in the ending (if any)
			
			String ending = null;
			if(end<message.length())
				ending= message.substring(end);
			setMessage(newMessage);
			
			if(ending != null)
				setMessage(newMessage + ending);
		}else {
			//if we are just doing a simple delete, the index is easy to find:
			if(end<message.length())
				newMessage += message.substring(end);
			//set the new index where the deletion starts
			index = start;
			setMessage(newMessage);
		}
		//also, there is no more selection since we just deleted it
		selection = false;
	}
	
	@Override
	public double[] drag(double dx, double dy) {
		//this should be for drag selecting text. Then cut and paste shortcuts should work
		if(mouseClickXY == null)
			return new double[]{0d,0d};
		//otherwise
		mouseClickXY[0] += dx;
		mouseClickXY[1] += dy;
		int newIndex = findIndex(mouseClickXY[0], mouseClickXY[1]);
		if(newIndex != index){
			//define that there has been a selection made
			selection = true;
			index = newIndex;
		}
		//all of the change in x and y was used to update the mouse coords
		return new double[] {dx,dy};
	}
	
	/**
	 * @return the selected text from a mouse drag. This is given by returning a substring of {@link message} from {@link clickIndex} to {@link index}.
	 */
	public String getSelectedText(){
		if(selection) {
			int start = (clickIndex < index)? clickIndex: index;
			int end = (index > clickIndex)? index: clickIndex;
			if(start<0)
				start = 0;
			if(end >= message.length())
				return message.substring(start);
			return message.substring(start, end);
		}else
			return null;
	}
	
	@Override
	public void setClicked(boolean clicked, int mouseX, int mouseY) {
		Polygon clickBox = new Polygon(clickBoundary[0], clickBoundary[1], clickBoundary[0].length);
		if(clicked && clickBox.contains(mouseX, mouseY))
			index = findIndex(mouseX, mouseY);
		else //just put the blinker at the end if the mouse position isn't found
			index = message.length();
		
		if(clicked){
			timeLast = System.currentTimeMillis();
			showBlinker = true;
			mouseClickXY[0] = mouseX;
			mouseClickXY[1] = mouseY;
			
			//if the box wasn't clicked prior, select for all (if enabled)
			if(clickSelectsAll && !this.clicked) {
				clickIndex = 0;
				index = message.length();
				selection = true;
			}else { //otherwise just set the clickIndex for dragging and disable selections
				clickIndex = index;
				selection = false;
			}
		}else{
			//disable the selection
			showBlinker = false;
			selection = false;
		}
		this.clicked = clicked;
	}

	/**
	 * Returns the selected text as defined by {@link #getSelectedText()}.
	 */
	@Override
	public String copy() {
		if(isHotKeyEnabled())
			return getSelectedText();
		else
			return null;
	}

	/**
	 * Returns the selected text as defined by {@link #getSelectedText()} and removes it from {@link message}.
	 */
	@Override
	public String cut() {
		if(!isHotKeyEnabled())
			return null;
		
		String cut = getSelectedText();
		if(selection)
			deleteSelection(null);
		return cut;
	}

	/**
	 * Pastes the text into the {@link message} at the {@link TextBox#index}.
	 */
	@Override
	public void paste(String pasteText) {
		if(enabled && isHotKeyEnabled()) {
			if(selection) {
				deleteSelection(pasteText);
			}else {
				appendMessage(pasteText);
			}
		}
	}
	
	/**
	 * Creates a selection if there is not one already that contains all of the text in this box.
	 */
	@Override
	public void selectAll() {
		if(!selection)
			selection = true;
		clickIndex = 0;
		index = message.length();
	}
	
	/**
	 * Shifts the index of the blinker, the vertical line at the position where you type, by the amount specified.
	 * @param delta the amount to shift the blinker index by. This will effectively make {@link #index} += delta.
	 * If the index is out of bounds either positively or negatively, it will be set to the closest usable value.
	 * This method also shows the blinker and resets the {@link TextBox#timer}.
	 */
	public void shiftIndex(int delta){
		index += delta;
		if(index<0)	index = 0;
		if(index>message.length())
			index = message.length();
		if(selection && index == clickIndex) //cease selection if nothing is contained
			selection = false;
		//reset the blink timer so the user can see where the index is
		if(blinkTime != -1 && clicked) {
			refreshBlinker();
		}
	}
	
	/**
	 * Shows the blinker. Tells the blinker that it should be shown again and that the timer should
	 * be reset to now. Sets {@link #showBlinker} to true, among other things.
	 */
	protected void refreshBlinker() {
		showBlinker = true;
		timer = 0;
		timeLast = System.currentTimeMillis();
	}
	
	/**
	 * @return Returns the {@link FontMetrics} last used in rendering the text for the text box. As such, this font
	 * metrics is reset each time this text box is rendered again.
	 */
	protected FontMetrics getFontMetrics(){
		return fontMetrics;
	}
	
	/**
	 * Returns the color used to draw the text in the text box.
	 * @return {@link #textColor}
	 */
	public Color getTextColor() {
		return textColor;
	}
	/**
	 * Sets the color to be used in drawing text in the text box.
	 * @param color to replace {@link #textColor}
	 * @return this
	 */
	public TextBox setTextColor(Color color) {
		textColor = color;
		return this;
	}
	
	/**
	 * Sets whether this text box is touched. If the touched color is not set, then an outline toggle will be used
	 * to show touch. Therefore, setting the touch here may trigger the toggle.
	 */
	@Override
	public void setTouched(boolean touched) {
		//if the touch state has changed
		if(touched != this.touched && colorTouched == null) { //if the outline effect should be used
			setOutline(!getOutline());
		}
		this.touched = touched;
	}
	
	/**
	 * If touchedColor is null, then the toggle outline effect will be used instead
	 * @param touchedColor saved as {@link #colorTouched}
	 * @return this
	 */
	public TextBox setTouchedColor(Color touchedColor) {
		if(colorTouched==null && touchedColor != null) {
			/* if the button is touched presently and the new color is not null, that means that the component will
			 * show touch through the new color instead of toggling outline. Therefore, the outline should go back
			 * to the original state.
			 */
			if(touched)
				setOutline(!getOutline());
		}	
		this.colorTouched = touchedColor;
		return this;
	}
	
	/**
	 * Returns whether the hotkey commands should work on this text box.
	 * @return {@link #hotkeyEnabled}
	 */
	@Override
	public boolean isHotKeyEnabled() {
		return hotkeyEnabled;
	}
	
	/**
	 * Sets whether the hotkey commands, copy, cut, paste, should work on this text box.
	 * @param hotkeys {@link #hotkeyEnabled}
	 * @return this
	 */
	public TextBox setHotkeyEnabled(boolean hotkeys) {
		this.hotkeyEnabled = hotkeys;
		return this;
	}
	
	/**
	 * Sets whether this text box should allow for text scrolling in the text box (true) or treat the borders
	 * as limits for the text length (false).
	 * @param allow sets {@link #hasVirtualSpace}
	 * @return this
	 */
	public TextBox setHasVirtualSpace(boolean allow) {
		this.hasVirtualSpace = allow;
		if (!allow) //reset the shift so everything can be deleted properly
			setStartShift(0);
		return this;
	}
	
	/**
	 * Returns whether this text box allows for text scrolling in the box (true) or treats borders as limits
	 * for the text length (false).
	 * @return {@link #hasVirtualSpace}
	 */
	public boolean getHasVirtualSpace() {
		return hasVirtualSpace;
	}
	
	/**
	 * Sets whether this text box should be deselected by the enter or return key.
	 * @param toDeselect sets {@link #deselectOnEnter}
	 * @deprecated use {@link #setDeselectOnEnter(boolean)} instead
	 */
	@Deprecated(since="1.15")
	public void deselectOnEnter(boolean toDeselect) {
		deselectOnEnter = toDeselect;
	}
	/**
	 * Sets whether this text box should be deselected by the enter or return key.
	 * @param toDeselect sets {@link #deselectOnEnter}
	 * @return this
	 */
	public TextBox setDeselectOnEnter(boolean toDeselect) {
		deselectOnEnter = toDeselect;
		return this;
	}
	
	/**
	 * Returns whether the enter key deselects this text box, as defined by {@link #deselectOnEnter}.
	 * @return {@link #deselectOnEnter}
	 */
	public boolean getDeselectOnEnter() {
		return deselectOnEnter;
	}
	
	/**
	 * Sets whether this box should accept the new line character from user input.<p>
	 * Accepting enter sets {@link #deselectOnEnter} to false
	 * @param acceptEnter sets {@link #acceptEnter}
	 * @deprecated use {@link #setAcceptEnter(boolean)} instead
	 */
	@Deprecated(since="1.15")
	public void acceptEnter(boolean acceptEnter) {
		if(acceptEnter)
			setDeselectOnEnter(false);
		this.acceptEnter = acceptEnter;
	}
	/**
	 * Sets whether this box should accept the new line character from user input.<p>
	 * Accepting enter sets {@link #deselectOnEnter} to false
	 * @param acceptEnter sets {@link #acceptEnter}
	 * @return this
	 */
	public TextBox setAcceptEnter(boolean acceptEnter) {
		if(acceptEnter)
			setDeselectOnEnter(false);
		this.acceptEnter = acceptEnter;
		return this;
	}
	
	/**
	 * Returns whether this box accepts the new line character from user input.
	 * @return {@link #acceptEnter}
	 */
	public boolean getAcceptEnter() {
		return acceptEnter;
	}
	
	/**
	 * Returns the scroll bar used to view text hidden in virtual space (if allowed by {@link #hasVirtualSpace}).
	 * @return {@link #textScroller}
	 */
	public ScrollBar getTextScroller() {
		return textScroller;
	}
	/**
	 * Sets the scroll bar that will be used to view text hidden in virtual space if such is allowed by
	 * {@link #hasVirtualSpace}.
	 * @param textScroller the scroll bar to be saved as {@link #textScroller}
	 * @return this
	 */
	public TextBox setTextScroller(ScrollBar textScroller) {
		this.textScroller = textScroller;
		return this;
	}
	
	/**
	 * Sets how much the text of this text box should be shifted to accommodate to showing text in virtual space.
	 * @param startShift {@link #startShift}
	 */
	public void setStartShift(int startShift) {
		this.startShift = startShift;
		if(textScroller!=null)
			textScroller.setOffset(startShift);
	}
	
	/**
	 * Sets whether word splitting on ends of lines is allowed
	 * @param allowSplit sets {@link #wordSplitting}
	 * @return this
	 */
	public TextBox setWordSplitting(boolean allowSplit) {
		this.wordSplitting = allowSplit;
		return this;
	}
	/**
	 * Returns whether word splitting is allowed for this text box
	 * @return the value of {@link #wordSplitting}
	 */
	public boolean getWordSplitting() {
		return wordSplitting;
	}
	
	@Override
	public ScrollBar getWidthScrollBar() {
		return textScroller;
	}
	@Override
	public ScrollBar getHeightScrollBar() {
		return textScroller;
	}
	
	/**
	 * Sets whether the cut off mark should be shown.
	 * @param shown the value to replace {@link #cutOffMark}
	 * @return this
	 */
	public TextBox setCutOffMark(boolean shown) {
		this.cutOffMark = shown;
		return this;
	}
	/**
	 * Returns whether the cut off mark is shown
	 * @return the value of {@link #cutOffMark}
	 */
	public boolean hasCutOffMark() {
		return cutOffMark;
	}
	
	/**
	 * Sets whether all the text box's contents should be selected when it is first clicked.
	 * @param clickSelects the value to replace {@link #clickSelectsAll}
	 * @return this
	 */
	public TextBox setClickSelectsAll(boolean clickSelects) {
		this.clickSelectsAll = clickSelects;
		return this;
	}
	/**
	 * Returns whether all the characters in the text box are selected when this box is first clicked.
	 * @return the value of {@link #clickSelectsAll}
	 */
	public boolean getClickSelectsAll() {
		return clickSelectsAll;
	}
	
	@Override
	public int[][] getActiveScrollCoordinates() {
		return clickBoundary;
	}
	
	/**
	 * Sets the mask for this text box. The masking character will be used instead of the {@link #message} contents,
	 * but the mask will be repeated the number of times equal to the message length. The char mask can be set to
	 * null to have no mask used.
	 * @param charMask to replace the value of {@link #charMask}
	 * @return this
	 */
	public TextBox setCharMask(Character charMask) {
		this.charMask = charMask;
		return this;
	}
}