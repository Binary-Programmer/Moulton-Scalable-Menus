package moulton.scalable.draggables;

import java.awt.Color;
import java.awt.Graphics;

import moulton.scalable.clickables.Clickable;
import moulton.scalable.clickables.RadioButton;
import moulton.scalable.containers.Panel;
import moulton.scalable.containers.MenuManager;

/**
 * The scroll bar class is a Draggable component that handles mouse draggging and notifies a component to be
 * scrolled. That component should implement {@link ScrollableComponent}.
 * @author Matthew Moulton
 */
public class ScrollBar extends Clickable implements DraggableComponent {
	/**The string expressions to define the dimensions of the scroll bar on the parent panel.*/
	protected String width, height;
	/**Most recent render values. Depend on whether the scroll bar is vertical as to whether these values are for y or x. */
	private int lastLength;

	/**Whether the scroll bar should render as a vertical:true or a horizontal:false. */
	protected boolean vertical = true;
	/**Determines the direction of +/- offset for the scroll bar. In a vertical plane, increasing offset will be
	 * moving the bar down. In a horizontal plane, increasing offset will be moving the bar right. If <b>inverseRender</b>
	 * is set to true, increasing offset vertically will be up and increasing offset horizonatally will be left.
	 * @see #renderInverse(boolean)*/
	protected boolean inverseRender = false;

	/**The buttons to click as opposed to dragging the scroll bar. This class does not handle them!
	 * It only changes them to editable/uneditable when necessary. */
	protected RadioButton scrollNeg, scrollPos;
	/**Defines the number of offsets the bar will shift for each mouse scroll that occurs. Defaults to 1.
	 * @see #getScrollRate()
	 * @see #setScrollRate(int)*/
	protected int scrollRate = 1;
	/**The offset of the clickable portion of the bar. Possible values range from 0 to {@link #totalOffs}-{@link #barOffs}.
	 * @see #getOffset()
	 * @see #setOffset(int)*/
	protected int offset = 0;
	/**The total number of offsets in the bar. Minimum is 1. 
	 * @see #getTotalOffs()
	 * @see #setTotalOffs(int)
	 * @see #setTotalOffs(int, boolean)*/
	protected int totalOffs = 1;
	/**The height/width of the scroll button on the bar measured in offset distances.
	 * @see #getBarOffs()
	 * @see #setBarOffs(int)*/
	protected int barOffs = 1;

	/**The color of the background.*/
	protected Color color;
	/**The color of the button*/
	protected Color colorButton;
	/**The color of the button when clicked*/
	protected Color colorDark;
	/**The color of the button when touched.
	 * @see #setTouchedColor(Color)*/
	protected Color colorTouched = null;

	/**
	 * Creates a scroll bar that will reside on the grid in the parent panel.
	 * @param vertical The orientation for this scroll bar. True is vertical and false is horizontal
	 * @param parent the parent panel for this component.
	 * @param x the x coordinate of this clickable in its parent's grid
	 * @param y the y coordinate of this clickable in its parent's grid
	 * @param colorButton the color of the button this bar will have. By default, the background is one shade lighter,
	 * and the button turns one shade darker when pressed.
	 */
	public ScrollBar(boolean vertical, Panel parent, int x, int y,  Color colorButton) {
		super("", parent, x, y);
		this.vertical = vertical;
		this.colorButton = colorButton;
		color = colorButton.brighter();
		colorDark = colorButton.darker();
	}

	/**
	 * Creates a scroll bar that will reside on the panel in a free-floating manner.
	 * @param vertical The orientation for this scroll bar. True is vertical and false is horizontal
	 * @param parent the parent panel for this component.
	 * @param x the string equation dictating where on the parent panel this component should be rendered.
	 * @param y the string equation dictating where on the parent panel this component should be rendered.
	 * @param w the string equation for the width of the bar on the parent panel.
	 * @param h the string equation for the height of the bar on the parent panel.
	 * @param colorButton the color of the button this bar will have. By default, the background is one shade lighter,
	 * and the button turns one shade darker when pressed.
	 */
	public ScrollBar(boolean vertical, Panel parent, String x, String y, String w, String h,  Color colorButton) {
		super("",parent, x, y);
		width = w;
		height = h;
		this.vertical = vertical;
		this.colorButton = colorButton;
		color = colorButton.brighter();
		colorDark = colorButton.darker();
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
		if(vertical) {
			lastLength = h;
		}else {
			lastLength = w;
		}

		g.setColor(color);
		g.fillRect(x, y, w, h);
		
		//if it is not editable, don't display the button
		if(!isEditable())
			return;

		//if there is nowhere to scroll
		if(barOffs>=totalOffs)
			//well you can just return here since there is nothing to click
			return;
		
		//draw the clickable button onto the bar.
		g.setColor(getFillColor());
		int cx, cy, cw, ch;
		if(vertical) {
			cx = x;
			cw = w;
			if(!inverseRender) {
				cy = y+(offset*h)/totalOffs;
				ch = ((offset+barOffs)*h)/totalOffs-(offset*h)/totalOffs;
			}else {
				cy = y+h-((offset+barOffs)*h)/totalOffs;
				ch = ((offset+barOffs)*h)/totalOffs-(offset*h)/totalOffs;
			}
		}else {
			cy = y;
			ch = h;
			if(!inverseRender) {
				cx = x+(offset*w)/totalOffs;
				cw = ((offset+barOffs)*w)/(totalOffs)-(offset*w)/totalOffs;
			}else {
				cx = x+w-((offset+barOffs)*w)/totalOffs;
				cw = ((offset+barOffs)*w)/totalOffs-(offset*w)/totalOffs;
			}
		}
		g.fillRect(cx, cy, cw, ch);
		if(getOutline()) {
			g.setColor(Color.BLACK);
			g.drawRect(cx, cy, cw-1, ch-1);
		}
		if(parent != null)
			defineClickBoundary(parent.handleOffsets(new int[] {cx, cx+cw, cx+cw, cx},
													new int[] {cy, cy, cy+ch, cy+ch}, this));
	}

	/**
	 * @param inverse sets {@link #inverseRender}
	 */
	public void renderInverse(boolean inverse) {
		inverseRender = inverse;
	}

	/**
	 * Sets the buttons to be linked to this scroll bar. The scroll bar will set these to editable or uneditable when necessary,
	 * but will not provide any other function.
	 * @param scrollNeg a button that when pressed, should make this bar scroll in the negative direction
	 * @param scrollPos a button that when pressed, should make this bar scroll in the positive direction
	 */
	public void setScrollButtons(RadioButton scrollNeg, RadioButton scrollPos) {
		this.scrollNeg = scrollNeg;
		this.scrollPos = scrollPos;
		updateScrollButtons();
	}
	
	/**
	 * Sets all of the offsets at once. Used for an initialization or change of state. Sets them in the order
	 * that they are listed for the sake of logical cohesion, otherwise offset may temporarily be an at illegal
	 * value if updated before total and bar offsets.
	 * @param totalOffs to replace {@link #totalOffs}
	 * @param barOffs to replace {@link #barOffs}
	 * @param offset to replace {@link #offset}
	 * @see #setTotalOffs(int, boolean)
	 * @see #setBarOffs(int)
	 * @see #setOffset(int)
	 */
	public void setOffsets(int totalOffs, int barOffs, int offset) {
		setTotalOffs(totalOffs);
		setBarOffs(barOffs);
		setOffset(offset);
		updateScrollButtons();
	}

	/**
	 * Returns the offset of the scroll bar. The offset is the value that determines how far the bar is on the line. The minimum offset value is
	 * 0 and the maximum is {@link #totalOffs}-{@link #barOffs}.
	 * @return {@link #offset}
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Sets the offset of the scroll bar. If the offset provided is out of range, the closest value to provided will be used.
	 * Additionally, this will update the editability of {@link #scrollNeg} and {@link #scrollPos} if they are not null.
	 * @param offset the new value for {@link #offset}
	 */
	public void setOffset(int offset) {
		if(offset<0)
			offset = 0;
		if(offset+barOffs > totalOffs)
			offset = totalOffs-barOffs;
		this.offset = offset;
		updateScrollButtons();
	}

	/**
	 * Returns the total number of offset ticks that the bar has.
	 * @return {@link #totalOffs}
	 */
	public int getTotalOffs() {
		return totalOffs;
	}
	
	/**
	 * Sets the total number of offsets for the scroll bar. This method is useful for more internal
	 * workings of the scroll bar. It will set the new {@link #totalOffs}, decrease {@link #offset}
	 * if it is too high, then {@link #updateScrollButtons()}.
	 * @param total the new variable to be {@link #totalOffs}. Must be greater than 0.
	 */
	public void setTotalOffs(int total) {
		if(total > -1) {
			totalOffs = total;
			if(offset > totalOffs)
				offset = totalOffs-1;
			updateScrollButtons();
		}
	}
	
	/**
	 * Sets the total number of offsets for the scroll bar like {@link #setTotalOffs(int)}. <p>
	 * The {@link #offset} may change based on the new value for {@link #totalOffs} to have it keep its place.
	 * If <code>offset</code> is at 0 when this method is called, it stay at zero. However, if it is any higher
	 * and <code>pullPositive</code> is true, offset will be shifted by the amount that {@link #totalOffs}
	 * changes. <p>
	 * Regardless, the editability of {@link #scrollNeg} and {@link #scrollPos} may change due to this method.
	 * @param total the new variable to be {@link #totalOffs}. Must be greater than 0.
	 * @param pullPositive whether it is logical to shift based on a total offsets change. If data is being
	 * added at the minimum value of offset side for this bar, then the bar should resultantly pull positively
	 * to compensate. For example, if this scroll bar is keeping track of the user's index in a list, and a
	 * new entry is added to the front of that list, then what was once at index 0 shifts to index 1, what was
	 * once at index 1 goes to index 2, etc. The offset will thus increase to match that change.<br>
	 * However, if data is being added to the maximum value side, then no offset change needs to occur.
	 */
	public void setTotalOffs(int total, boolean pullPositive) {
		if(total > -1) {
			if(offset>0 && pullPositive) { //then stick with the current
				offset += total - totalOffs;
				if(offset < 0)
					offset = 0;
			}
			totalOffs = total;
			if(offset > totalOffs)
				offset = totalOffs-1;
			updateScrollButtons();
		}
	}

	/**
	 * Sets the number of offset ticks the bar occupies.
	 * @param offs sets {@link #barOffs}
	 */
	public void setBarOffs(int offs) {
		if(offs > -1){
			barOffs = offs;
			if(offset+barOffs>totalOffs) {
				offset = totalOffs-barOffs;
				if(offset<0)
					offset = 0;
			}
			updateScrollButtons();
		}
	}
	/**
	 * Returns the number of offsets the button on the bar takes up.
	 * @return {@link #barOffs}
	 */
	public int getBarOffs() {
		return barOffs;
	}
	
	protected void updateScrollButtons() {
		boolean inUse = isEditable() && barOffs<totalOffs;
		if(scrollNeg != null)
			scrollNeg.setEditable(offset > 0 && inUse);
		if(scrollPos != null)
			scrollPos.setEditable(offset+barOffs < totalOffs && inUse);
	}

	@Override
	public double[] drag(double deltaX, double deltaY) {
		double[] change = {0,0};
		if(!isEditable()) {
			return change;
		}
		int bool = inverseRender?-1:1;
		//perform the calculations, using lastLength as the reference (for calculating how many pixels cause a shift)
		double dragged = (vertical)? deltaY:deltaX; //the shift in direction scroll bar measures
		int dir = (dragged>0)? 1:-1;
		dragged *= dir; //make it positive
		
		//an interval is lastLength/totalOffs. Therefore, the amount dragged/interval = how many possible shifts
		int shifts = (int)((dragged*totalOffs)/lastLength);
		if(shifts!=0) {
			//set the new offset, also check for improper value and reset connected button editability
			int tempOffs = offset;
			setOffset(offset + shifts*bool*dir);
			//the shifts that actually occurred here
			shifts = offset-tempOffs;
			//now update the changed for the return. Change the amount of shifts*interval
			change[vertical? 1:0] = (shifts*lastLength*bool)/(double)totalOffs;
		}
		return change;
	}
	
	/**
	 * Sets whether this button on the scroll bar is touched. If the touched color isn't set, then an outline
	 * toggle will be used to show touch. Therefore, setting the touch here may trigger the toggle.
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
	 * If the touched color is null, then the toggle outline effect will be used instead
	 * @param touchedColor to replace {@link #colorTouched}
	 */
	public void setTouchedColor(Color touchedColor) {
		if(colorTouched==null && touchedColor != null) {
			/* if the button is touched presently and the new color is not null, that means that the component will
			 * show touch through the new color instead of toggling outline. Therefore, the outline should go back
			 * to the original state.
			 */
			if(touched)
				setOutline(!getOutline());
			
			//set the new darker color
			colorDark = touchedColor.darker();
		}else {
			//resets to the old darker color
			colorDark = colorButton.darker();
		}	
		this.colorTouched = touchedColor;
	}
	
	/**
	 * Called in rendering. Gives the fill color of the scroll bar's button. If the scroll bar is not editable,
	 * {@link #color} is used. If the button is clicked, {@link #colorDark} is used. If the button is touched and
	 * has {@link #colorTouched} set, that is used. Otherwise, the normal {@link #colorButton} is returned.
	 * @return the applicable color for rendering the fill of the scroll bar's button
	 */
	public Color getFillColor() {
		if (!isEditable())
			return color;
		if(getClicked())
			return colorDark;
		if(isTouched() && colorTouched != null)
			return colorTouched;
		
		return colorButton;
	}
	
	/**
	 * Sets the number of offsets that this scroll bar should be shifted for each unit of mouse scrolling applied.
	 * @param offsetRate sets the value of {@link #scrollRate}
	 */
	public void setScrollRate(int offsetRate) {
		scrollRate = offsetRate;
	}
	/**
	 * Returns the number of offsets that this scroll bar should be shifted for each unit of mouse scrolling. Called
	 * by {@link MenuManager#mouseScrolled(int, int, int)} to determine how much the {@link #offset} of this bar
	 * should be altered.
	 * @return the value of {@link #scrollRate}
	 */
	public int getScrollRate() {
		return scrollRate;
	}
}