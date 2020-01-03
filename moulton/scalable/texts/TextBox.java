package moulton.scalable.texts;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;

import moulton.scalable.clickables.Clickable;
import moulton.scalable.containers.Panel;
import moulton.scalable.draggables.DraggableComponent;
import moulton.scalable.draggables.ScrollBar;
import moulton.scalable.draggables.ScrollableComponent;

/**
 * This is a text box that users can enter data into, the available characters for which is defined by {@link TextFormat}. This class also has
 * the functionality of mouse dragging to select multiple characters to be manipulated, and thereby offers support for copy, cut, and paste functions.
 * @author Matthew Moulton
 */
public class TextBox extends Clickable implements DraggableComponent, HotkeyTextComponent, ScrollableComponent {
	/**The display string of the text box.
	 * @see #getMessage()
	 * @see #setMessage(String)*/
	protected String message;
	/**Displays this string on the text box when the {@link message} is empty.
	 * @see #setHint(String)
	 * @see #getHint()*/
	protected String hint = null;
	/**Text boxes are rectangles, thus they will be drawn from (x,y) to (x+width,y+width)*/
	protected String width, height;
	/**The font used to display {@link #message}*/
	protected Font font;
	/**The background color used if editable. Otherwise, white is used.*/
	protected Color color;
	/**The formatting scheme for this text box. Determines what characters are valid in the {@link #message}.
	 * @see #getTextFormat()
	 * @see #setTextResizeFactor(int)*/
	protected TextFormat format = null;
	/**The maximum number of characters that can be held in the text box. -1 indicates no limit, which is
	 * the default.
	 * @see #setCharLimit(int)*/
	protected int charMax = -1;
	/**Whether input can be put outside of the visible box.
	 * @see #allowsVirtualSpace()
	 * @see #allowVirtualSpace(boolean)*/
	protected boolean virtualSpace = true;
	/**The alignment of the text to be rendered. Defaults to left alignment.*/
	protected Alignment alignment = Alignment.LEFT_ALIGNMENT;
	/**The color of the box when touched.
	 * @see #setTouchedColor(Color)*/
	protected Color colorTouched = null;
	/**Whether word splitting is allowed for end of lines. In other words, whether in rendering the text box,
	 * encountering and end of line will force previous characters (until a break) to the next line.
	 * Break characters include space, new line, and hyphens. Defaults to false.
	 * @see #allowsWordSplitting()
	 * @see #allowWordSplitting(boolean)*/
	protected boolean wordSplitting = false;
	
	/**Whether or not to show the blinker. The blinker will automatically blink every {@link #blinkTime} ms which is kept track of
	 * by {@link #timer} and {@link #timeLast}. */
	protected boolean showBlinker = false;
	/**The time in milliseconds the blinker should wait before toggling on/off. Default value is 1000 ms = 1 sec. -1 indicates that the
	 * blinker should not toggle */
	protected int blinkTime = 1000;
	/**The last time the time was added onto timer */
	private long timeLast;
	/**The sum of time differences since last blink toggle of on/off. */
	private long timer;
	/**The point at which the blinker is in the message. Also the point at which any new input will be inserted */
	protected int index = 0;
	/**How much the index where the text box starts displaying is shifted. If the text box has more than one row,
	 * a shift will be in rows. Otherwise, shifts will be horizontal. The shift can be controlled by the connected
	 * scroll bar saved as {@link #textScroller}.
	 * @see #setStartShift(int)
	 */
	protected int startShift = 0;
	/**
	 * The scroll bar to change {@link #startShift} and view text in unseen virtual space if allowed ({@link #virtualSpace}).
	 * @see #getTextScroller()
	 * @see #setTextScroller(ScrollBar)
	 */
	protected ScrollBar textScroller = null;
	
	/**
	 * The index of the mouse click. When the user drags the mouse, clickIndex stays the same but {@link #index} changes. The selection
	 * is defined as the text between the two indices.<p>Can be determined by {@link #findIndex(int, int)}.
	 */
	protected int clickIndex = 0;
	/**Whether there currently is a selection of text. Also determines whether {@link #clickIndex} is relevant. That text selection
	 * can be retrieved with {@link #getSelectedText()}.*/
	protected boolean selection = false;
	/**The pixel point of the last mouse click.*/
	protected int[] mouseClickXY = new int[2];
	/**Whether the hotkey commands, copy, cut, paste, should be usable for this text box. Defaults to true.
	 * @see #isHotkeyEnabled()
	 * @see #setHotkeyEnabled(boolean)*/
	protected boolean hotkeyEnabled = true;
	
	//last font metrics used
	private FontMetrics fontMetrics;
	
	/**
	 * @param id a unique string designed to identify this component when an event occurs.
	 * @param message the string displayed in the box
	 * @param parent the panel that this text box will reside upon
	 * @param x the x coordinate on the screen, given in menu component value format
	 * @param y the y coordinate on the screen, given in menu component value format
	 * @param width the width of the component, given in menu component value format
	 * @param height the height of the component, given in menu component value format
	 * @param font the font for the box
	 * @param color the background color for the box when editable
	 */
	public TextBox(String id, String message, Panel parent, String x, String y, String width, String height, Font font, Color color) {
		super(id, parent, x, y);
		this.width = width;
		this.height = height;
		this.message = message;
		this.color = color;
		this.font = font;
	}
	/**
	 * @param id a unique string designed to identify this component when an event occurs.
	 * @param message the string displayed in the box
	 * @param parent the panel that this text box will reside upon
	 * @param x the x coordinate of this text box in its parent's grid
	 * @param y the y coordinate of this text box in its parent's grid
	 * @param font the font for the box
	 * @param color the background color for the box when editable
	 */
	public TextBox(String id, String message, Panel parent, int x, int y, Font font, Color color) {
		super(id,parent,x,y);
		this.message = message;
		this.color = color;
		this.font = font;
	}
	
	/**
	 * Sets the text format for this box. The text format determines which characters are valid for {@link #message}.
	 * @param tf the new text format, saved as {@link #format}.
	 */
	public void setTextFormat(TextFormat tf){
		this.format = tf;
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
	 */
	public void setHint(String hint){
		this.hint = hint;
	}
	
	/**
	 * Sets the maximum number of characters that this text box can hold. A charMax of -1 means no limit.
	 * @param charMax {@link #charMax}.
	 */
	public void setCharLimit(int charMax) {
		this.charMax = charMax;
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
			int hh = clickBoundary[1][3]-clickBoundary[1][0];
			int ww = clickBoundary[0][1]-clickBoundary[0][0];
			
			int hheight = fontMetrics.getHeight();
			String line = "";
			int rows = hh/hheight;
			int centeringY = (hh-(rows*hheight))/2;
			//find which row the click was on
			//default to bottom
			int row = rows-1;
			//then start at top and work down. the empty space below the last line will end up being for the last line
			for(int i=0; i<rows; i++) {
				if(mouseY-clickBoundary[1][0] < (int)(hheight*(i+.8) + centeringY)){
					row = i;
					break;
				}
			}
			//variable that will keep track of the number of letters shown thus far
			int sum = 0;
			String rem = message;
			
			//shift modifications
			if(rows==1 && startShift>0 && message.length()>startShift) {
				rem = message.substring(startShift);
				sum+=startShift;
			}
			int shift=0;
			if(rows>1)
				shift = startShift;
			
			int underscoreWidth = fontMetrics.stringWidth("_");
			/*The inside width is the difference of the pixel width of the box and the underscore width.
			 *This leaves spacing for half an underscore on both sides. */
			int insideWidth = ww - underscoreWidth/2;
			if(rem != null && !rem.isEmpty()){
				//it only needs to go to row because that is the data we need
				for(int i=-shift; i<=row; i++){
					String text = "";
					int wwidth = 0;
					
					while(wwidth<insideWidth && !rem.isEmpty()){
						text += rem.charAt(0);
						rem = rem.substring(1);
						wwidth = fontMetrics.stringWidth(text);
						if(text.charAt(text.length()-1) == '\n')
							wwidth += insideWidth;
					}
					
					if(!rem.isEmpty() && rows>1) { //the width is too long
						boolean wordSplit = allowsWordSplitting();
						if(!wordSplit) {
							int ii=text.length()-1;
							for(; ii>-1; ii--) {
								char c = text.charAt(ii);
								if(c == '\n' || c == ' ') {
									//add it back to the remainder
									rem = text.substring(ii+1) + rem;
									text = text.substring(0,ii);
									if(i!=row)
										sum++; //make up for the consumed char
									break;
								}else if(c == '-') {
									if(ii<text.length()-1) {
										//keep the - on this line
										rem = text.substring(ii+1) + rem;
										text = text.substring(0, ii+1);
									}else {
										rem = text.substring(ii) + rem;
										text = text.substring(0, ii);
									}
									break;
								}
							}
							if(ii == -1) { //no break character found, split the word
								wordSplit = true;
							}
						}if(wordSplit){
							int length = text.length();
							rem = text.charAt(length-1) + rem;
							text = text.substring(0, length-1);
						}
					}
					
					if(i!=row)
						sum += text.length();
					else
						line = text;
				}
			}
			//now we have the line
			//this line could be left aligned, center aligned, or right aligned
			int i = line.length();
			int here = clickBoundary[0][0];
			switch(alignment) {
			case LEFT_ALIGNMENT:
				here += underscoreWidth/2;
				break;
			case CENTER_ALIGNMENT:
				here += ww/2 - (fontMetrics.stringWidth(line))/2;
				break;
			case RIGHT_ALIGNMENT:
				here += ww - fontMetrics.stringWidth(line)-underscoreWidth/2;
				break;
			}
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
	 * Called in rendering. Gives the fill color of the box. If the box is not editable, white is used.
	 * If the box is touched but does not have focus, colorTouched is used. Otherwise, the normal color is returned.
	 * @return the applicable color for rendering the fill of the box
	 */
	public Color getFillColor() {
		if (!isEditable())
			return Color.WHITE;
		if(isTouched() && !getClicked() && colorTouched != null)
			return colorTouched;
		
		return color;
	}

	/**
	 * Renders this text box with the provided graphics. If the box is displaying the message, is clicked, and
	 * allows virtual text, it will shift the starting position ({@link #startShift}) to display where the blinker
	 * would be placed.
	 */
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
		
		g.setColor(getFillColor());
		g.fillRect(x, y, w, h);
		defineClickBoundary(new int[] {x, x+w, x+w, x}, new int[] {y, y, y+h, y+h});
		g.setColor(editable? Color.BLACK: Color.GRAY);
		if (outline)
			g.drawRect(x, y, w - 1, h - 1);
		
		if(textScroller != null) {
			//try to get the new start shift from the scroll bar
			startShift = textScroller.getOffset();
		}

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
		int centeringY = (h-(texts.length*hheight))/2;

		boolean messageShown;
		boolean isClicked = getClicked(); //we should only shift to the blinker if the box is selected
		//the string that will be displayed. usually message but sometimes the hint
		String rem;
		if(message!=null && message.length()>0 || clicked || hint==null){
			rem = message;
			g.setColor(Color.BLACK);
			messageShown = true;
		}else{
			rem = hint;
			g.setColor(Color.DARK_GRAY);
			messageShown = false;
		}

		int blinkerRow = 0;
		int blinkerX = 0;
		int totalTextLength = rem.length();
		//sets the blinker position regardless (useful for row or horiz shifting)
		boolean setBlinker = false;
		int underscoreWidth = fontMetrics.stringWidth("_");

		//otherwise we will proceed to draw over the text box with a selection
		if(h/hheight<1)
			return;
		texts = new String[h/hheight];

		//draw the string correctly, not drawing outside of the box and conforming to lines
		int blinkerPlace = 0;
		boolean setClick = !selection;
		int clickRow = 0;
		int clickPlace = 0;
		int shift = messageShown? startShift:0; //shift only used when the message is shown

		/*The inside width is the difference of the pixel width of the box and the underscore width.
		 *This leaves spacing for half an underscore on both sides. */
		int insideWidth = w - underscoreWidth;
		//the iterator of the line we are working with
		int i = 0;
		boolean redo;
		do {
			redo = false;
			boolean lastCheck = false;
			while(i<texts.length){
				if(i<texts.length && texts[i] == null) //create the text if needed
					texts[i] = "";
				
				//check for the blinker position
				if(!setBlinker && rem.length()==totalTextLength-index){
					setBlinker = true;
					blinkerRow = i;
					blinkerPlace = texts[i].length();
					blinkerX = fontMetrics.stringWidth(texts[i]);
				}if(!setClick && rem.length()==totalTextLength-clickIndex){
					setClick = true;
					clickRow = i;
					clickPlace = texts[i].length();
				}
				
				//checked for click and blinker first (for end of line)
				if(rem.isEmpty()) //but if rem is empty, quit out
					break;
				//we have checked for click and blinker one last time
				if(lastCheck) {
					i++; //now we can change i back and quit
					break;
				}
				
				//add a character onto the line
				texts[i] += rem.charAt(0);
				rem = rem.substring(1);
				
				//shift for single-line boxes
				if(shift>0 && texts.length==1) {
					//if the blinker has been set, the shift needs to decrease
					if(setBlinker && isClicked) {
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
				if(fontMetrics.stringWidth(texts[i])>insideWidth || texts[i].charAt(texts[i].length()-1)=='\n'){
					if(texts.length>1) {
						//if word split is not allowed, we will find a space or break
						boolean wordSplit = allowsWordSplitting();
						if(!wordSplit) {
							boolean loseChar = false;
							boolean lineKeep = false;
							int ii = texts[i].length()-1;
							for(; ii>-1; ii--) {
								char c = texts[i].charAt(ii);
								if(c == ' ' || c == '\n') {
									//the empty char should be consumed
									loseChar = true;
									break;
								}else if(c == '-') {
									loseChar = ii<texts[i].length()-1;
									if(loseChar)
										lineKeep = true;
									break;
								}
							}
							if(ii == -1) { //no space char found
								//go ahead with normal line break
								wordSplit = true;
							}else { //found a break char
								rem = texts[i].substring(ii+ (loseChar?1:0)) + rem;
								if(lineKeep)
									ii++;
								texts[i] = texts[i].substring(0, ii);
								//blinker or click effected by the row change
								if(setBlinker && blinkerRow == i && blinkerPlace>ii)
									setBlinker = false;
								if(setClick && clickRow == i && clickPlace>ii)
									setClick = false;
							}
						}//otherwise we can just remove the last added character
						if(wordSplit) {
							rem = texts[i].charAt(texts[i].length()-1) + rem;
							texts[i] = texts[i].substring(0, texts[i].length()-1);
						}
						
						//shift modifications
						if(shift>0) {
							if(setBlinker && isClicked) { //if the blinker has already been placed
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
					
					//if i has now become too large, we need to check blinker and click before breaking
					if(i>=texts.length) {
						lastCheck = true;
						i--;
					}
				}
				
			}
			//if the blinker hasn't been set yet, it is further in the message
			if(!setBlinker && allowsVirtualSpace() && getClicked() && !rem.isEmpty()) {
				if(texts.length>1) {
					//shift all rows up
					int ii=1;
					for(; ii<texts.length; ii++) {
						texts[ii-1] = texts[ii];
					}//delete the last row
					texts[ii-1] = "";
				}else {
					//get rid of the first character
					texts[0] = texts[0].substring(1);
				}
				redo = true; //go back into the loop
				i--; //do the last line again
				startShift++; //save for next time
			}else if(messageShown && startShift>0 && rem.isEmpty()) {
				//if there was empty space and unneeded offset
				//if so, we have to do a complete redo
				if((texts.length>1 && i<texts.length-1) || (texts.length==1 && 
						fontMetrics.stringWidth(message.charAt(startShift-1)+texts[i])
						< insideWidth && message.charAt(startShift-1)!='\n')) {
					rem = message;
					setBlinker = false;
					setClick = !selection;
					texts = new String[h/hheight];
					i = 0;
					//decrease startShift to try to capture more text in the box
					shift = --startShift;
					redo = true;
				}
			}
		}while(redo);
		//do some more operations on the leftover text if it is the message
		if(messageShown) {
			//cut off extra if virtual space not allowed
			if(!virtualSpace && !rem.isEmpty()) {
				String tempMessage = getMessage();
				setMessage(tempMessage.substring(0, tempMessage.length()-rem.length()));
				//update the scroll bar
				if(textScroller!=null)
					textScroller.setBarOffs(0);
			}
			//update the scroll bar
			if(virtualSpace && textScroller!=null) {
				if(texts.length==1) { //one lined text box
					int textLength = 0;
					if(texts[0]!=null)
						textLength = texts[0].length();
					textScroller.setOffsets(totalTextLength, textLength, startShift);
				}else {
					//calculate the total number of lines
					int extraLines = 0;
					String extraText = "";
					while(!rem.isEmpty()) {
						extraText += rem.charAt(0);
						if(fontMetrics.stringWidth(extraText)>insideWidth) {//the line is now too long
							extraLines++;
							extraText = ""+rem.charAt(0); //start the next line
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
		
		
		for(int ii=0; ii<texts.length; ii++){
			if(texts[ii]==null) //the text is done, move onto the next step
				break;
			switch(alignment){
			case LEFT_ALIGNMENT:
				g.drawString(texts[ii], x + underscoreWidth/2, (int)(y + hheight*(ii+.8) + centeringY));
				break;
			case CENTER_ALIGNMENT:
				g.drawString(texts[ii], x - (fontMetrics.stringWidth(texts[ii]))/2 + w/2, (int)(y + hheight*(ii+.8) + centeringY));
				break;
			case RIGHT_ALIGNMENT:
				g.drawString(texts[ii], x + w - (fontMetrics.stringWidth(texts[ii])+underscoreWidth/2), (int)(y + hheight*(ii+.8) + centeringY));
				break;
			}
		}

		//show the blinker if the blinker has been placed and should be shown
		if(setBlinker && showBlinker) {
			int decrease = hheight - fontMetrics.getDescent();
			int rowWidth = 0;
			if(texts[blinkerRow] != null)
				rowWidth = fontMetrics.stringWidth(texts[blinkerRow]);
			g.setColor(Color.BLACK);
			switch(alignment) {
			case LEFT_ALIGNMENT:
				g.fillRect(x +underscoreWidth/2 + blinkerX, (int)(y + hheight*(blinkerRow+.8) + centeringY -decrease), 2, hheight);
				break;
			case CENTER_ALIGNMENT:
				g.fillRect(x - rowWidth/2 + w/2 + blinkerX, (int)(y + hheight*(blinkerRow+.8)
						+ centeringY) -decrease, 2, hheight);
				break;
			case RIGHT_ALIGNMENT:
				g.fillRect(x + w - (rowWidth+underscoreWidth/2) + blinkerX, (int)(y + hheight*
						(blinkerRow+.8) + centeringY) -decrease, 2, hheight);
				break;
			}
		}

		//now to draw the selected portion and selection on top
		if(!selection)
			return;

		//click positions aren't necessarily possible because of shifting. Fix to valid values
		if(clickRow<0) { //it is less than shown row, but set place, so minimum
			clickRow = 0;
			clickPlace = 0;
		}else if(!setClick) { //it hasn't been set, so maximum
			clickRow = texts.length-1;
			if(texts[clickRow] != null)
				clickPlace = (texts[clickRow] != null)? texts[clickRow].length(): 0;
		}
		
		/* A start point and end point needs to be defined for the selection. Although clickIndex is where the click began, backwards dragging is
		 * supported, which requires that we ascertain which is actually first. In rows[] and places[], 0 is the startpoint, and 1 is the end.
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
			int xOffs = 0;
			//use a switch to determine the x offset
			switch(alignment){
			case LEFT_ALIGNMENT:
				xOffs = x + underscoreWidth/2;
				break;
			case CENTER_ALIGNMENT:
				xOffs = x - (fontMetrics.stringWidth(texts[ii]))/2 + w/2;
				break;
			case RIGHT_ALIGNMENT:
				xOffs = x + w - (fontMetrics.stringWidth(texts[ii])+underscoreWidth/2);
				break;
			}
			//draw the rectangles and selection texts
			g.fillRect(xOffs + fontMetrics.stringWidth(texts[ii].substring(0,start)),
					(int)(y + hheight*ii + centeringY), selWidth, hheight);
			g.setColor(Color.WHITE);
			g.drawString(texts[ii].substring(start,end), xOffs + fontMetrics.stringWidth(texts[ii].substring(0,start)),
					(int)(y + hheight*(ii+.8) + centeringY));
		}
	}

	/**
	 * Returns the message displayed on this text box
	 * @return {@link #message}
	 */
	public synchronized String getMessage() {
		return message;
	}
	
	/**
	 * Attempts to set {@link #message} to the specified string. If this text box has a TextFormat saved in {@link #format},
	 * the string provided will be parsed as defined in {@link TextFormat#parseText(String)} and the result will be saved as
	 * {@link #message}. Also, the string will be shortened to {@link #charMax} if necessary and if {@link #charMax} has
	 * been set. 
	 * @param string the string provided as a replacement
	 */
	public synchronized void setMessage(String string) {
		if(format != null)
			message = format.parseText(string);
		else
			message = string;
		if(charMax>-1 && message.length()>charMax)
			message = message.substring(0, charMax);
		index = message.length(); //set the index to the end
	}
	
	/**
	 * Deletes the selection, then appends the given string into {@link #message}. If the append makes 
	 * {@link #message} longer than {@link #charMax} if {@link #charMax} has been set, the tail-end of
	 * the string will be excluded.
	 * @param string the String to append on
	 */
	@Override
	public synchronized void appendMessage(String string) {
		//deletes the selection if there is one
		if(selection) {
			int start, end;
			if(clickIndex < index) {
				start = clickIndex;
				end = index;
			}else {
				start = index;
				end = clickIndex;
			}
			setMessage(message.substring(0, start)+ message.substring(end+1));
			//set the new index where the deletion starts
			index = start;
			//also, there is no more selection since we just deleted it
			selection = false;
		}
		
		if(format != null)
			string = format.parseText(string);
		String fixed = message.substring(0, index) + string;
		if(index < message.length())
			fixed += message.substring(index);
		message = fixed;
		if(charMax>-1 && message.length()>charMax)
			message = message.substring(0, charMax);
		index += string.length();
	}
	/**
	 * Deletes the selection then removes characters from {@link #message} starting at {@link #index}.
	 * @param chars the number of chars to be removed from the message. If there is a selection, 1 of
	 * these characters will be used to delete it.
	 * @param leftDelete whether to delete left or right from the starting index
	 */
	@Override
	public void removeMessage(int chars, boolean leftDelete) {
		//deletes the selection if there is one
		if(selection) {
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
			if(end<message.length())
				newMessage += message.substring(end);
			setMessage(newMessage);
			//set the new index where the deletion starts
			index = start;
			//then decrements the number of characters to delete before passing on to the super
			if(chars>0)
				chars--;
			//also, there is no more selection since we just deleted it
			selection = false;
		}
		//then removes the commanded characters
		if(chars<1) //if there is nothing to delete, exit
			return;
		
		int left, right;
		if(leftDelete){
			left = index - chars;
			right = index;
		}else{
			left = index;
			right = index + chars;
		}
		
		if(left<0) left = 0;
		if(right<0) right = 0;
		if(left>message.length()) left = message.length();
		if(right>message.length()) right = message.length();
		
		if(index>0 && leftDelete){
			index -= chars;
			if(index<0) index = 0;
		}
		
		if(left != right){
			String temp = "";
			if(left>0){
				temp += message.substring(0, left);
			}if(right<message.length())
				temp += message.substring(right);
			message = temp;
		}
	}
	
	@Override
	public synchronized int[] drag(int dx, int dy) {
		//this should be for drag selecting text. Then cut and paste shortcuts should work
		if(mouseClickXY == null)
			return new int[]{0,0};
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
		return new int[] {dx,dy};
	}
	
	/**
	 * @return the selected text from a mouse drag. This is given by returning a substring of {@link message} from {@link clickIndex} to {@link index}.
	 */
	public String getSelectedText(){
		if(selection) {
			int start = (clickIndex < index)? clickIndex: index;
			int end = (index > clickIndex)? index: clickIndex;
			if(end == message.length())
				return message.substring(start);
			return message.substring(start, end);
		}else
			return null;
	}
	
	@Override
	public void setClicked(boolean clicked, int mouseX, int mouseY) {
		this.clicked = clicked;
		if(clicked){
			timeLast = System.currentTimeMillis();
			showBlinker = true;
		}else{
			showBlinker = false;
		}
		
		Polygon clickBox = new Polygon(clickBoundary[0], clickBoundary[1], clickBoundary[0].length);
		if(clicked && clickBox.contains(mouseX, mouseY))
			index = findIndex(mouseX, mouseY);
		else
			index = message.length();
		
		if(clicked) {
			clickIndex = index;
			selection = false;
			mouseClickXY[0] = mouseX;
			mouseClickXY[1] = mouseY;
		}else {
			//disable the selection
			selection = false;
		}
	}

	/**
	 * Returns the selected text as defined by {@link #getSelectedText()}.
	 */
	@Override
	public String copy() {
		if(isHotkeyEnabled())
			return getSelectedText();
		else
			return null;
	}

	/**
	 * Returns the selected text as defined by {@link #getSelectedText()} and removes it from {@link message}.
	 */
	@Override
	public String cut() {
		if(!isHotkeyEnabled())
			return null;
		
		String cut = getSelectedText();
		if(selection) {
			int start, end;
			if(clickIndex < index) {
				start = clickIndex;
				end = index;
			}else {
				start = index;
				end = clickIndex;
			}
			if(editable) {
				if(message.length() > end)
					setMessage(message.substring(0, start)+ message.substring(end));
				else //there is no end part to add on
					setMessage(message.substring(0, start));
			}
			//set the new index where the deletion starts
			index = start;
			//also, there is no more selection since we just deleted it
			selection = false;
		}
		return cut;
	}

	/**
	 * Pastes the text into the {@link message} at the {@link TextBox#index}.
	 */
	@Override
	public void paste(String pasteText) {
		if(editable && isHotkeyEnabled()) {
			if(selection) {
				int start, end;
				if(clickIndex < index) {
					start = clickIndex;
					end = index;
				}else {
					start = index;
					end = clickIndex;
				}
				int messageLength = message.length() - (end-start);
				String newText = "";
				if(start > 0)
					newText += message.substring(0, start);
				newText += pasteText;
				if(end < message.length())
					newText += message.substring(end+1);
				setMessage(newText);
				//we don't technically know the length beforehand since the pasted text may pass through a filter
				int pasteLength = message.length() - messageLength;
				//define new indices
				index = start + pasteLength;
				selection = false;
			}else {
				appendMessage(pasteText);
			}
		}
	}
	
	@Override
	public boolean isDeselectedOnRelease() {
		return false;
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
		if(index>message.length()) index = message.length();
		//reset the blink timer so the user can see where the index is
		if(blinkTime != -1) {
			showBlinker = true;
			timer = 0;
			timeLast = System.currentTimeMillis();
		}
	}
	
	/**
	 * @return Returns the {@link FontMetrics} last used in rendering the text for the text box. As such, this font
	 * metrics is reset each time this text box is rendered again.
	 */
	protected FontMetrics getFontMetrics(){
		return fontMetrics;
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
	 */
	public void setTouchedColor(Color touchedColor) {
		if(colorTouched==null && touchedColor != null) {
			/* if the button is touched presently and the new color is not null, that means that the component will
			 * show touch through the new color instead of toggling outline. Therefore, the outline should go back
			 * to the original state.
			 */
			if(touched)
				setOutline(!getOutline());
		}	
		this.colorTouched = touchedColor;
	}
	
	/**
	 * Returns whether the hotkey commands, copy, cut, paste, should work on this text box.
	 * @return {@link #hotkeyEnabled}
	 */
	public boolean isHotkeyEnabled() {
		return hotkeyEnabled;
	}
	
	/**
	 * Sets whether the hotkey commands, copy, cut, paste, should work on this text box.
	 * @param hotkeys {@link #hotkeyEnabled}
	 */
	public void setHotkeyEnabled(boolean hotkeys) {
		this.hotkeyEnabled = hotkeys;
	}
	
	/**
	 * Sets whether this text box should allow for text scrolling in the text box (true) or treat the borders
	 * as limits for the text length (false).
	 * @param allow sets {@link #virtualSpace}
	 */
	public void allowVirtualSpace(boolean allow) {
		this.virtualSpace = allow;
		if(!allow) { //reset the shift so everything can be deleted properly
			setStartShift(0);
		}
	}
	
	/**
	 * Returns whether this text box allows for text scrolling in the box (true) or treats borders as limits
	 * for the text length (false).
	 * @return {@link #virtualSpace}
	 */
	public boolean allowsVirtualSpace() {
		return virtualSpace;
	}
	
	/**
	 * Returns the scroll bar used to view text hidden in virtual space (if allowed by {@link #virtualSpace}).
	 * @return {@link #textScroller}
	 */
	public ScrollBar getTextScroller() {
		return textScroller;
	}
	/**
	 * Sets the scroll bar that will be used to view text hidden in virtual space if such is allowed by
	 * {@link #virtualSpace}.
	 * @param textScroller the scroll bar to be saved as {@link #textScroller}
	 */
	public void setTextScroller(ScrollBar textScroller) {
		this.textScroller = textScroller;
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
	 */
	public void allowWordSplitting(boolean allowSplit) {
		this.wordSplitting = allowSplit;
	}
	/**
	 * Returns whether word splitting is allowed for this text box
	 * @return the value of {@link #wordSplitting}
	 */
	public boolean allowsWordSplitting() {
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
}