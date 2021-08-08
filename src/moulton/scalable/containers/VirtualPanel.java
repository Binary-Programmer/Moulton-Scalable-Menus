package moulton.scalable.containers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import moulton.scalable.draggables.ScrollBar;
import moulton.scalable.draggables.ScrollableComponent;
import moulton.scalable.utils.MenuComponent;

/**
 * An improved Panel class that can be scrolled and can thus hold hidden elements in "virtual space".
 * The VirtualPanel can serve as a Panel parent for any MenuComponent. Keep in mind that the width of
 * child components will be based relative to full width and height of the parent panel rather than
 * just the dimensions.
 * @author Matthew Moulton
 */
public class VirtualPanel extends Panel implements ScrollableComponent{
	/**
	 * The full width and full height values are the minimum values of the actual sizes of the panel. That is,
	 * at render time, if the regular width (shownWidth) or regular height (shownHeight) of this panel is less
	 * than the values of full, the rest of the panel can be shown by repositioning, for example, by the use
	 * of a scroll panel. <p>
	 * 
	 * As said, these values are minimums: if the value of the regular width or height is greater than their
	 * full counterparts, this <code>VirtualPanel</code> will fill to those dimensions. This can be very useful if
	 * there is a lower bound that a scroll panel should support values under. For example, giving fullWidth the
	 * value of "400", scroll bar use will be available only if fewer than 400 pixels are available for the shown
	 * width.
	 */
	protected String fullWidth, fullHeight;
	/**
	 * Stands for last full width and last full height, meaning the values that {@link #fullWidth} and 
	 * {@link #fullHeight} evaluated to at last render. Used to tell the scroll bar its maximum offset.
	 * Set by {@link #render(Graphics, int, int, int, int)}. Not intended to be set directly but subclasses
	 * unless rendering takes place.
	 * 
	 * @see Panel#lastWidth
	 * @see Panel#lastHeight
	 * @see #lastX
	 * @see #lastY
	 * */
	protected int lastFullW, lastFullH;
	/**How much the shown window on the panel is shifted in the x and y direction.
	 * @see #getXOffs()
	 * @see #getYOffs()
	 * @see #setXOffs(int)
	 * @see #setYOffs(int)*/
	protected int xOffs=0, yOffs=0;

	/**The connected scroll bar for x axis scrolling.
	 * @see #setWidthScrollBar(ScrollBar)*/
	protected ScrollBar widthBar;
	/**A modifier to manually set the {@link ScrollBar#totalOffs} with an algebraic expression.
	 * @see #setTotalOffsofScrollBar(String, boolean)*/
	protected String widthBarTotalOffsets;
	/**The connected scroll bar for y axis scrolling.
	 * @see #setHeightScrollBar(ScrollBar)*/
	protected ScrollBar heightBar;
	/**A modifier to manually set the {@link ScrollBar#totalOffs} with an algebraic expression.
	 * @see #setTotalOffsofScrollBar(String, boolean)*/
	protected String heightBarTotalOffsets;
	/**The coordinates of the panel at last render*/
	protected int lastX=0, lastY=0;
	
	/**
	 * @param parent the panel this panel will reside upon.
	 * @param x the x coordinate on the screen, given in menu component value format
	 * @param y the y coordinate on the screen, given in menu component value format
	 * @param shownWidth the component width that will be displayed, given in menu component value format
	 * @param shownHeight the component height that will be displayed, given in menu component value format
	 * @param fullWidth the entire width of the panel. In general, intended to be greater than shownWidth
	 * @param fullHeight the entire height of the panel. In general, intended to be greater than shownHeight
	 * @param color the background color for the box when editable
	 */
	public VirtualPanel(Panel parent, String x, String y, String shownWidth, String shownHeight, String fullWidth, String fullHeight, Color color) {
		super(parent, x, y, shownWidth, shownHeight, color);
		this.fullWidth = fullWidth;
		this.fullHeight = fullHeight;
	}
	/**
	 * @param parent the panel this panel will reside upon.
	 * @param x the integer x coordinate this panel should appear on its parent panel
	 * @param y the integer y coordinate this panel should appear on its parent panel
	 * @param fullWidth the entire width of the panel. In general, intended to be greater than the shown width,
	 * determined at run time by the space on the grid allotted to this panel.
	 * @param fullHeight the entire height of the panel. In general, intended to be greater than the shown height,
	 * determined at run time by the space on the grid allotted to this panel.
	 * @param color the background color for the box when editable
	 */
	public VirtualPanel(Panel parent, int x, int y, String fullWidth, String fullHeight, Color color) {
		super(parent, x, y, color);
		this.fullWidth = fullWidth;
		this.fullHeight = fullHeight;
	}

	@Override
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		Rectangle rect = this.getRenderRect(xx, yy, ww, hh, width, height);
		int x = rect.x;
		int y = rect.y;
		int w = rect.width;
		int h = rect.height;
		
		//lastX and lastY have already been set to be true to offset- after all, it is drawing on its parent.
		//It is the children that are being deceived for the purposes of rendering.
		lastX = x;
		lastY = y;
		//width and height stay the same regardless of the offset
		lastWidth = w;
		lastHeight = h;
		
		//the full dimensions
		lastFullW = solveString(fullWidth, ww, hh);
		lastFullH = solveString(fullHeight, ww, hh);
		//if the shown dimension is greater than the full, set full to shown
		if(w>lastFullW) lastFullW = w;
		if(h>lastFullH) lastFullH = h;
		
		//change the scroll bars to reflect self
		if(widthBar!=null) {
			//normal settings
			if(widthBarTotalOffsets == null){
				//total offs will be the fullWidth
				widthBar.setTotalOffs(lastFullW);
				//the number of bar offs will be pixels visible
				widthBar.setBarOffs(w);
			}else { //overridden setting
				int totalOffs = solveString(widthBarTotalOffsets, lastFullW, lastFullH);
				widthBar.setTotalOffs(totalOffs);
				widthBar.setBarOffs((totalOffs * w)/lastFullW);
			}
		}if(heightBar!=null) {
			if(heightBarTotalOffsets == null) {
				heightBar.setTotalOffs(lastFullH);
				heightBar.setBarOffs(h);
			}else {
				int totalOffs = solveString(heightBarTotalOffsets, lastFullW, lastFullH);
				widthBar.setTotalOffs(totalOffs);
				widthBar.setBarOffs((totalOffs * h)/lastFullH);
			}
		}

		//get updated values from the scroll bars just in case something changed
		if(widthBar != null)
			xOffs = (lastFullW*widthBar.getOffset())/widthBar.getTotalOffs();
		else
			setXOffs(xOffs);
		if(heightBar != null)
			yOffs = (lastFullH*heightBar.getOffset())/heightBar.getTotalOffs();
		else
			setYOffs(yOffs);

		//create a new image for the shown area
		if(w <= 0 || h <= 0)
			return;
		BufferedImage shown = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		Graphics show = shown.getGraphics();
		// draw color
		if(color!=null) {
			show.setColor(color);
			show.fillRect(0, 0, w, h);
		}
		//draw the components onto the bufferedimage and then draw the bufferedimage onto the menu
		ArrayList<MenuComponent> both = getAllHeldComponents();

		try{
			for(MenuComponent mc: both) {
				if(mc!=null && mc.isVisible()) {
					//render each component onto the image with full dimensions.
					Rectangle self = new Rectangle(-xOffs, -yOffs, lastFullW, lastFullH);
					if(mc.getGridLocation() != null)
						self = grid.findCompCoordinates(mc, self);
					
					mc.render(show, self.x, self.y, self.width, self.height);
				}
			}
		}catch(ConcurrentModificationException cme){
			System.err.println("There was a concurrent access of the components in the panel.");
		}
		
		//draw outline
		if (outline) {
			show.setColor(Color.BLACK);
			show.drawRect(0, 0, w - 1, h - 1);
		}
		g.drawImage(shown, x, y, null);
	}

	/**
	 * @param bar the scroll bar to replace {@link #widthBar}.
	 */
	public void setWidthScrollBar(ScrollBar bar) {
		widthBar = bar;
	}
	/**
	 * @param bar the scroll bar to replace {@link #heightBar}.
	 */
	public void setHeightScrollBar(ScrollBar bar) {
		heightBar = bar;
	}
	/**
	 * At render time, VirtualPanel changes {@link ScrollBar#totalOffs} and {@link ScrollBar#barOffs} of {@link #widthBar} and {@link #heightBar}.
	 * The values of {@link ScrollBar#totalOffs} are set to {@link #fullWidth} and {@link #fullHeight}, respectively.<p>
	 * Use this method to override that setting with another value. {@link ScrollBar#barOffs} will be deduced from the value given. Keep in mind
	 * that "width" and "height" in this context will refer to {@link #fullWidth} and {@link #fullHeight}. Thus, calling
	 * setTotalOffsOfScrollBar("width",true) will do nothing. Remember that this may result in a graphical glitch if the equation results to
	 * too small of a number because {@link ScrollBar#totalOffs} and {@link ScrollBar#barOffs} are integers and the latter is a fraction of the
	 * former.
	 * @param totalOffs the algebraic expression string to be solved for at render time for total number of offsets of the specified scroll bar
	 * @param width true:widthBar, false:heightBar
	 */
	public void setTotalOffsofScrollBar(String totalOffs, boolean width){
		if(width)
			widthBarTotalOffsets = totalOffs;
		else
			heightBarTotalOffsets = totalOffs;
	}
	/**
	 * Returns {@link #widthBar}.
	 */
	@Override
	public ScrollBar getWidthScrollBar() {
		return widthBar;
	}
	/**
	 * Returns {@link #heightBar}.
	 */
	@Override
	public ScrollBar getHeightScrollBar() {
		return heightBar;
	}
	@Override
	public int[][] getActiveScrollCoordinates() {
		int[] offs = getRenderOffset(this);
		return new int[][] {{offs[0], offs[0]+offs[2], offs[0]+offs[2], offs[0]},
							{offs[1], offs[1], offs[1]+offs[3], offs[1]+offs[3]}};
	}
	
	/**
	 * The VirtualPanel gets the offset from its parent panel ({@link Panel#getRenderOffset(MenuComponent)}), then
	 * adds the offset this has saved from the last render, saved as {@link #lastX} and {@link #lastY}.
	 */
	@Override
	public int[] getRenderOffset(MenuComponent comp) {
		int[] parentOffset = super.getRenderOffset(comp); //all the offsets and parent work set in super
		
		if(parentOffset[2]>-1) {
			parentOffset[2] = Math.min(parentOffset[2]-lastX, lastWidth);
			if(parentOffset[2]<0) parentOffset[2] = 0;
		}else
			parentOffset[2] = lastWidth;
		if(parentOffset[3]>-1) {
			parentOffset[3] = Math.min(parentOffset[3]-lastY, lastHeight);
			if(parentOffset[3]<0) parentOffset[3] = 0;
		}else
			parentOffset[3] = lastHeight;
		
		if(parentOffset[0] < 0)
			parentOffset[0] = 0;
		if(parentOffset[1] < 0)
			parentOffset[1] = 0;
		parentOffset[0] += lastX;
		parentOffset[1] += lastY;
		return parentOffset;
	}
	
	/**
	 * Returns the number of pixels the position this panel is horizontally offset virtually.
	 * @return {@link #xOffs}
	 */
	public int getXOffs() {
		return xOffs;
	}
	/**
	 * Sets the number of pixels that this panel should be horizontally offset virtually. Or in other
	 * words, when the {@link #fullWidth} is greater than the shown width, the offset controls what part
	 * of the full width is shown. If the offset is 0, then xs [0-shownWidth] will be shown. <p>
	 * Attempts to set xOffs to negative values or values > fullWidth-shownWidth will be altered to the
	 * nearest possible value from [0, fullWidth-shownWidth]. For example, trying to set xOffs = -5 will
	 * result in xOffs = 0. <p>
	 * Updates the {@link #getWidthScrollBar()} if there is one.
	 * @param xOff the int value to replace {@link #xOffs}
	 */
	public void setXOffs(int xOff) {
		if(xOff < 0)
			xOff = 0;
		if(xOff > lastFullW-lastWidth)
			xOff = lastFullW-lastWidth;
		
		xOffs = xOff;
		if(widthBar != null)
			widthBar.setOffset((xOffs*widthBar.getTotalOffs())/lastFullW);
	}
	
	/**
	 * Returns the number of pixels the position this panel is vertically offset virtually.
	 * @return {@link #yOffs}
	 */
	public int getYOffs() {
		return yOffs;
	}
	/**
	 * Sets the number of pixels that this panel should be vertically offset virtually. Or in other
	 * words, when the {@link #fullHeight} is greater than the shown height, the offset controls what part
	 * of the full height is shown. If the offset is 0, then xs [0-shownHeight] will be shown. <p>
	 * Attempts to set yOffs to negative values or values > fullHeight-shownHeight will be altered to the
	 * nearest possible value from [0, fullHeight-shownHeight]. For example, trying to set yOffs = -5 will
	 * result in yOffs = 0. <p>
	 * Updates the {@link #getHeightScrollBar()} if there is one.
	 * @param yOff the int value to replace {@link #yOffs}
	 */
	public void setYOffs(int yOff) {
		if(yOff < 0)
			yOff = 0;
		if(yOff > lastFullH-lastHeight)
			yOff = lastFullH-lastHeight;
			
		yOffs = yOff;
		if(heightBar != null)
			heightBar.setOffset((yOffs*heightBar.getTotalOffs())/lastFullH);
	}

}