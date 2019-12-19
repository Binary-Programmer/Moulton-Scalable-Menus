package moulton.scalable.containers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import moulton.scalable.clickables.Clickable;
import moulton.scalable.draggables.ScrollBar;
import moulton.scalable.draggables.ScrollableComponent;
import moulton.scalable.utils.MenuComponent;

/**
 * An improved Panel class that can be scrolled and can thus hold hidden elements. The PanelPlus can serve as a 
 * Panel parent for any MenuComponent. Keep in mind that the width of child components will be based relative to
 * full width and height of the parent panel rather than just the shown ones.
 * @author Matthew Moulton
 */
public class PanelPlus extends Panel implements ScrollableComponent{
	/**
	 * The full width and full height values are the minimum values of the actual sizes of the panel. That is,
	 * at render time, if the regular width (shownWidth) or regular height (shownHeight) of this panel is less
	 * than the values of full, the rest of the panel can be shown by repositioning, for example, by the use
	 * of a scroll panel. <p>
	 * 
	 * As said, these values are minimums: if the value of the regular width or height is greater than their
	 * full counterparts, this <code>PanelPlus</code> will fill to those dimensions. This can be very useful if
	 * there is a lower bound that a scroll panel should support values under. For example, giving fullWidth the
	 * value of "400", scroll bar use will be available only if fewer than 400 pixels are available for the shown
	 * width.
	 */
	protected String fullWidth, fullHeight;
	
	/**How much the shown window on the panel is shifted in the x and y direction.*/
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
	public PanelPlus(Panel parent, String x, String y, String shownWidth, String shownHeight, String fullWidth, String fullHeight, Color color) {
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
	public PanelPlus(Panel parent, int x, int y, String fullWidth, String fullHeight, Color color) {
		super(parent, x, y, color);
		this.fullWidth = fullWidth;
		this.fullHeight = fullHeight;
	}

	@Override
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		int x, y, w, h;
		if(getGridLocation()==null){
			x = xx + solveString(this.x, ww, hh);
			y = yy + solveString(this.y, ww, hh);
			// variant for input ending points instead of widths indicated by a starting question
			if (this.width.charAt(0) == '?') {
				int x2 = solveString(this.width.substring(1), ww, hh);
				w = x2 - x;
			}else
				w = solveString(this.width, ww, hh);

			if (this.height.charAt(0) == '?') {
				int y2 = solveString(this.height.substring(1), ww, hh);
				h = y2 - y;
			}else
				h = solveString(this.height, ww, hh);
		}else {
			x = xx;
			y = yy;
			w = ww;
			h = hh;
		}
		
		//the full dimensions
		int fullW = solveString(fullWidth, ww, hh);
		int fullH = solveString(fullHeight, ww, hh);
		//if the shown dimension is greater than the full, set full to shown
		if(w>fullW) fullW = w;
		if(h>fullH) fullH = h;
		
		//change the scroll bars to reflect self
		if(widthBar!=null) {
			//normal settings
			if(widthBarTotalOffsets == null){
				//total offs will be the fullWidth
				widthBar.setTotalOffs(fullW, true);
				//the number of bar offs will be pixels visible
				widthBar.setBarOffs(w);
			}else { //overridden setting
				int totalOffs = solveString(widthBarTotalOffsets, fullW, fullH);
				widthBar.setTotalOffs(totalOffs, true);
				widthBar.setBarOffs((totalOffs * w)/fullW);
			}
		}if(heightBar!=null) {
			if(heightBarTotalOffsets == null) {
				heightBar.setTotalOffs(fullH, true);
				heightBar.setBarOffs(h);
			}else {
				int totalOffs = solveString(heightBarTotalOffsets, fullW, fullH);
				widthBar.setTotalOffs(totalOffs, true);
				widthBar.setBarOffs((totalOffs * h)/fullH);
			}
		}

		//get updated values from the scroll bars just in case something changed
		if(widthBar != null)
			xOffs = (fullW*widthBar.getOffset())/widthBar.getTotalOffs();
		if(heightBar != null)
			yOffs = (fullH*heightBar.getOffset())/heightBar.getTotalOffs();

		//create a new image for the shown area
		BufferedImage shown = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		Graphics show = shown.getGraphics();
		// draw color
		if(color!=null) {
			show.setColor(color);
			show.fillRect(0, 0, w, h);
			show.setColor(Color.BLACK);
			if (outline)
				show.drawRect(0, 0, w - 1, h - 1);
		}
		//draw the components onto the bufferedimage and then draw the bufferedimage onto the menu
		ArrayList<MenuComponent> both = getAllHeldComponents();

		try{
			for(MenuComponent mc: both) {
				if(mc.isVisible()) {
					//render each component onto the image with full dimensions.
					int[] self = {-xOffs, -yOffs, fullW, fullH};
					if(mc.getGridLocation() != null)
						self = getGriddedComponentCoordinates(mc, self);
					
					mc.render(show, self[0], self[1], self[2], self[3]);
					//if the menu component is a clickable type, we need to go in and change the click boundary.
					//since the panel itself can be offset, the boundaries are not necessarily where the clickable would assume they are.
					if(mc instanceof Clickable) {
						int clickBoundary[][] = ((Clickable)mc).getClickBoundary();
						//for each of the xs we need to shift it back by xx
						//if any x goes out of its bounds, it needs to be fixed
						for(int i=0; i<clickBoundary[0].length; i++) {
							clickBoundary[0][i]+= x;
							if(clickBoundary[0][i]> x+w)
								clickBoundary[0][i] = x+w;
							else if(clickBoundary[0][i]<x)
								clickBoundary[0][i] = x;
						}
						//do the same with ys
						for(int i=0; i<clickBoundary[1].length; i++) {
							clickBoundary[1][i]+= y;
							if(clickBoundary[1][i]> y+h)
								clickBoundary[1][i] = y+h;
							else if(clickBoundary[1][i]<y)
								clickBoundary[1][i] = y;
						}
					}
				}
			}
		}catch(ConcurrentModificationException cme){
			System.err.println("There was a concurrent access of the components in the panel.");
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
	 * At render time, PanelPlus changes {@link ScrollBar#totalOffs} and {@link ScrollBar#barOffs} of {@link #widthBar} and {@link #heightBar}.
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
	@Override
	public ScrollBar getWidthScrollBar() {
		return widthBar;
	}
	@Override
	public ScrollBar getHeightScrollBar() {
		return heightBar;
	}

}