package moulton.scalable.containers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;

import moulton.scalable.utils.MenuComponent;

/** 
 * A panel to serve as a parent component and backdrop for other MenuComponents. When you want to add a menu component to this, simply call the component's
 * constructor, which will either call {@link #addToGrid(MenuComponent, int, int)} if the component is to reside in a gridded fashion, or will add itself
 * to this Panel's {@link #comps}.<p>
 * One can use the {@link #createRoot(Color)} method to create a simple menu panel. For any panel, {@link #setColor(Color)} can be used to set its backdrop
 * color. Other methods like {@link #setFrame(String, String)}, {@link #setMargin(String, String)}, {@link #setOutline(boolean)}, and {@link #setTextResize(boolean)}
 * can further be used to customize the Panel.
 * @author Matthew Moulton
 */
public class Panel extends MenuComponent {
	/**The string expressions to define the dimensions of this panel on its parent panel.*/
	protected String width, height;
	/** The background color of this panel. A rectangle filled with this color is rendered unless it is null.
	 * @see #setColor(Color)
	 * @see #getColor()*/
	protected Color color;
	/**Whether or not this panel should draw a black outline on its border.
	 * @see #setOutline(boolean)
	 * @see #getOutline()*/
	protected boolean outline = false;
	/**The components held in the grid form. The x and y of these components are determined at render time from this own panel's coordinates based
	 * on this panel's x, y, width, and height. The width of this panel is split evenly into {@link #gridDim}.x pieces. Likewise, the height of this
	 * panel is split evenly into {@link #gridDim}.y pieces. */
	protected HashMap<Point, MenuComponent> grid = new HashMap<Point, MenuComponent>();
	/**A list of the components held in free-form. The coordinates of the components held here are found by using this panel's coordinates at render
	 * time and then solving the algebraic expressions in the component that defines its location.*/
	protected LinkedList<MenuComponent> comps = new LinkedList<MenuComponent>();
	/**The maximum x and y values of the child components of the grid. At run-time, the grid is split evenly into that many pieces for the x and y axes.*/
	protected Dimension gridDim = new Dimension(0,0);
	/**The width of the x margin for the grid. Defaults to null.
	 * @see #setMargin(String, String)
	 * @see #yMargin*/
	protected String xMargin = null;
	/**The height of the y margin for the grid. Defaults to null.
	 * @see #setMargin(String, String)
	 * @see #xMargin*/
	protected String yMargin = null;
	/**The outside border of the x for grid. Defaults to null.
	 * @see #setFrame(String, String)
	 * @see #yFrame*/
	protected String xFrame = null;
	/**The outside border of the y for grid. Defaults to null.
	 * @see #setFrame(String, String)
	 * @see #xFrame*/
	protected String yFrame = null;
	/**Whether or not text components should increase in size relative to height. When text size must be 
	 * determined, if this panel has not set its value (defaults to null), it will defer to its parent panel.
	 * If no preference has been set, text will not resize relatively.
	 * @see #textResize
	 * @see #setTextResize(boolean)
	 * @see #setTextResizeFactor(int)*/
	private Boolean textResize = null;
	/**The height of the panel (in pixels) at last render. Used by {@link MenuComponent} to know how much
	 * the text should resize for child components.*/
	protected int lastHeight = 0;
	
	/**
	 * @param parent the panel this panel will reside upon. Null if this is being set to {@link MenuManager#menu}.
	 * @param x the x coordinate on the screen, given in menu component value format
	 * @param y the y coordinate on the screen, given in menu component value format
	 * @param width the width of the component, given in menu component value format
	 * @param height the height of the component, given in menu component value format
	 * @param color the background color for the box when editable
	 */
	public Panel(Panel parent, String x, String y, String width, String height, Color color) {
		super(parent, x, y);
		this.width = width;
		this.height = height;
		this.color = color;
	}
	/**
	 * @param parent the panel this panel will reside upon. Null if this is being set to {@link MenuManager#menu}.
	 * @param x the integer x coordinate this panel should appear on its parent panel
	 * @param y the integer y coordinate this panel should appear on its parent panel
	 * @param color the background color for the box when editable
	 */
	public Panel(Panel parent, int x, int y,  Color color){
		super(parent, x, y);
		this.color = color;
	}
	
	/**
	 * Creates a root panel. This is the same as calling {@link #Panel(Panel, int, int, Color)} with null, 0, 0, and background respectively
	 * but sometimes that can look confusing. For readability, this can be used. For the menu to be usable, the root panel must be saved as
	 * {@link MenuManager#menu}.
	 * @param background the background color
	 * @return the created root panel
	 */
	public static Panel createRoot(Color background) {
		return new Panel(null,0,0,background);
	}

	@Override
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		int x, y, w, h;
		if(parent != null && getGridLocation()==null) { //otherwise this should check for dimension modifications
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
		lastHeight = h;

		// draw color
		if(color!=null){
			g.setColor(color);
			g.fillRect(x, y, w, h);
			g.setColor(Color.BLACK);
			if (outline)
				g.drawRect(x, y, w - 1, h - 1);
		}
		
		try{
			//now draw any components in the grid
			for(MenuComponent mc: grid.values()){
				if(mc!=null && mc.isVisible()) {
					int[] selfDim = {x,y,w,h};
					int dimDetails[] = getGriddedComponentCoordinates(mc, selfDim);
					//gridded components have a very specific space where they should be
					mc.render(g, dimDetails[0], dimDetails[1], dimDetails[2], dimDetails[3]);
				}
			}
			
			//now time to draw any free components
			for(MenuComponent mc: comps){
				if(mc!=null && mc.isVisible()) { //free components are drawn anywhere in this panel.
					mc.render(g, x, y, w, h);
				}
			}
		}catch(ConcurrentModificationException cme){
			System.err.println("There was a concurrent access of the components in the panel.");
		}
	}

	/**
	 * Called by the constructor of supported MenuComponents to add the
	 * component to this panel's grid. Once the component has been added, the
	 * panel will take care of rendering and placement of the component.
	 * 
	 * @param comp the component to be added to this panel's grid
	 * @param x the x position on the grid for the component
	 * @param y the y position on the grid for the component
	 */
	public void addToGrid(MenuComponent comp, int x, int y) {
		if (x >= gridDim.getWidth())
			gridDim.setSize(x+1, gridDim.getHeight());
		if (y >= gridDim.getHeight())
			gridDim.setSize(gridDim.getWidth(), y+1);
		grid.put(new Point(x, y), comp);
	}
	
	/**
	 * Finds the specified component in the grid and returns its pixel coordinates. If it cannot be found,
	 * null is returned.
	 * @param comp the component to look for in the grid
	 * @param self the location and dimension of this panel in the render. Ordered as x, y, width, and height.
	 * @return the pixel coordinates for the specified component to be rendered. Ordered as x, y, width, and height.
	 */
	protected int[] getGriddedComponentCoordinates(MenuComponent comp, int[] self) {
		Point gridPoint = comp.getGridLocation();
		//The component must be in a grid for the following calculations to work!
		if(gridPoint!=null){
			int details[] = { 0, 0, 0, 0 };
			//search through grid for this component
			if (grid.get(gridPoint) != comp) // not found!
				return details;
			//found @ gridPoint
			
			//find children components from self
			double wwidth = 0, hheight = 0;
			int wholeWidth = self[2];
			int wholeHeight = self[3];
			//frame
			if(xFrame!=null){
				int frame = solveString(xFrame,wholeWidth,wholeHeight);
				self[0] += frame;
				self[2] -= frame*2;
			}
			if(yFrame!=null){
				int frame = solveString(yFrame,wholeWidth,wholeHeight);
				self[1] += frame;
				self[3] -= frame*2;
			}
			//margins
			boolean margin = false;
			if(xMargin!=null) {
				int marginX = solveString(xMargin,wholeWidth,wholeHeight);
				if(marginX>=1){
					int margins = gridDim.width-1;
					wwidth = (self[2]-(margins*marginX))/gridDim.getWidth();
					details[0] = self[0] + (gridPoint.x)*marginX + (int)(wwidth*gridPoint.x);
					margin = true;
				}
			}//this should run if margin is null or if margin results to 0 or negative
			if(!margin){
				wwidth = self[2]/gridDim.getWidth();
				details[0] = self[0] + (int)(wwidth*gridPoint.x);
			}details[2] = (int)(wwidth*(gridPoint.x+1)) - (int)(wwidth*gridPoint.x);
			
			margin = false;
			if(yMargin!=null) {
				int marginY = solveString(yMargin,wholeWidth,wholeHeight);
				if(marginY>=1){
					int margins = gridDim.height-1;
					hheight = (self[3]-(margins*marginY))/gridDim.getHeight();
					details[1] = self[1] + (gridPoint.y)*marginY + (int)(hheight*gridPoint.y);
					margin = true;
				}
			}if(!margin){
				hheight= self[3]/gridDim.getHeight();
				details[1] = self[1] + (int)(hheight*gridPoint.y);
			}details[3] = (int)(hheight*(gridPoint.y+1)) - (int)(hheight*gridPoint.y);
			return details;
		}
		return null;
	}
	
	/**
	 * Sets the background color of this panel.
	 * @param color {@link #color}
	 */
	public void setColor(Color color) {
		this.color = color;
	}
	/**
	 * Returns the color of this panel's background.
	 * @return {@link #color}
	 */
	public Color getColor(){
		return color;
	}
	
	/**
	 * Sets whether or not the panel should draw a black outline on its boundary.
	 * @param outline {@link #outline}
	 */
	public void setOutline(boolean outline){
		this.outline = outline;
	}
	/**
	 * Returns whether or not the panel is drawing a black outline on its boundary.
	 * @return {@link outline}
	 */
	public boolean getOutline(){
		return outline;
	}
	
	/**
	 * Sets the {@link #xMargin} and {@link #yMargin} for this panel. The margins will
	 * be used to separate components in the grid. Thus the number of marginal dimensions
	 * for the width of a panel is (number of x components)-1, where the number of x 
	 * components is at least one. A null value indicates no margin. The margin is measured
	 * from the dimensions of this panel.
	 * @param xMargin the width of the margin on the x-axis
	 * @param yMargin the height of the margin on the y-axis
	 */
	public void setMargin(String xMargin, String yMargin) {
		this.xMargin = xMargin;
		this.yMargin = yMargin;
	}
	
	/**
	 * Sets the {@link #xFrame} and {@link #yFrame} for this panel. Unlike margins, the frame
	 * will only be on the outside of the panel, not between individual components. A null
	 * value indicates no frame. The frame is measured from the dimensions of this panel.
	 * @param xFrame the algebraic expression for the width of the frame
	 * @param yFrame the algebraic expression for the height of the frame.
	 */
	public void setFrame(String xFrame, String yFrame){
		this.xFrame = xFrame;
		this.yFrame = yFrame;
	}
	
	/**
	 * Overwrites the value of {@link #textResize}.
	 * @param resize Sets whether or not this panel and its descendants should resize text by height.
	 */
	public void setTextResize(Boolean resize) {
		this.textResize = resize;
	}
	/**
	 * @return if text should resize by height. This is found by seeing if this panel has set a preference in {@link #textResize}
	 * If a preference has been set, that is returned. Otherwise, the parent is queried to see if it has been set.
	 */
	@Override
	public boolean textResize() {
		if(textResize == null) {
			if(parent == null)
				return false; //defaults to not resize
			else
				return parent.textResize();
		}else
			return textResize;
	}
	
	/**
	 * Returns the height of this panel at the last render
	 * @return the height of this panel measured in pixels
	 */
	public int getRecentHeight() {
		return lastHeight;
	}
	
	/**
	 * Returns a list of all of the child components that this panel holds. By default, {@link #grid} is added to
	 * the list, then {@link #comps}.
	 * @return the array of all held menu components
	 */
	public ArrayList<MenuComponent> getAllHeldComponents(){
		ArrayList<MenuComponent> both = new ArrayList<>(grid.values().size() + comps.size());
		both.addAll(grid.values());
		both.addAll(comps);
		return both;
	}
	
	/**Searches through this component's {@link #comps} to delete the component.
	 * @param comp The component to remove
	 * @return whether the component was successfully removed
	 */
	public boolean removeFreeComponent(MenuComponent comp) {
		return comps.remove(comp);
	}
	
	/**
	 * Adds a component to the list of free component components.
	 * @param comp the component to be added
	 * @return returns whether the add was successful
	 */
	public boolean addFreeComponent(MenuComponent comp) {
		return comps.add(comp);
	}
	
	/**Deletes the component found at the location (x,y) in {@link #grid}.
	 * @param x the x-value of the component to remove
	 * @param y the y-value of the component to remove
	 * @param resize whether the grid should check for a resize after the deletion.
	 * @return whether a component was removed at (x,y)
	 */
	public boolean removeFromGrid(int x, int y, boolean resize) {
		if(resize && gridDim.width>x && gridDim.height>y) {
			int maxX=0, maxY=0;
			boolean resized = true;
			for(Point p:grid.keySet()) {
				if(p.x > maxX) maxX = p.x;
				if(p.y > maxY) maxY = p.y;
				//if components are found to have higher xs and ys, then the deletion of this object was in the middle
				if(maxX>x && maxY>y) { 
					resized = false;
					break;
				}
			}
			if(resized) {
				gridDim.width = maxX;
				gridDim.height = maxY;
			}
		}
		return grid.remove(new Point(x,y)) != null;
	}
	
}