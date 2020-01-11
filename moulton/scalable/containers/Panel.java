package moulton.scalable.containers;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;

import moulton.scalable.clickables.TouchResponsiveComponent;
import moulton.scalable.utils.GridFormatter;
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
	/**A list of the components held in free-form. The coordinates of the components held here are found by using this panel's coordinates at render
	 * time and then solving the algebraic expressions in the component that defines its location.*/
	protected LinkedList<MenuComponent> comps = new LinkedList<MenuComponent>();
	
	protected GridFormatter grid = new GridFormatter();
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
			for(MenuComponent mc: grid.getHeldComponents()){
				if(mc!=null && mc.isVisible()) {
					int[] selfDim = {x,y,w,h};
					int dimDetails[] = grid.findCompCoordinates(mc, selfDim);
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
	 * panel will take care of rendering and placing the component.
	 * 
	 * @param comp the component to be added to this panel's grid
	 * @param x the x position on the grid for the component
	 * @param y the y position on the grid for the component
	 */
	public void addToGrid(MenuComponent comp, int x, int y) {
		grid.addComponent(comp, x, y);
	}
	
	/**Deletes the component found at the location (x,y) in {@link #grid}.
	 * @param x the x-value of the component to remove
	 * @param y the y-value of the component to remove
	 * @param resize whether the grid should check for a resize after the deletion.
	 * @return whether a component was removed at (x,y)
	 */
	public void removeFromGrid(int x, int y, boolean resize) {
		grid.removeComponent(x, y, resize);
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
		ArrayList<MenuComponent> gridComps = new ArrayList<>();
		gridComps.addAll(grid.getHeldComponents());
		ArrayList<MenuComponent> both = new ArrayList<>(gridComps.size() + comps.size());
		both.addAll(gridComps);
		both.addAll(comps);
		return both;
	}
	
	/**
	 * Returns the formatter for this panel's grid components. Very useful to set margins, borders, or weights.
	 * @return the value of {@link #grid}
	 * */
	public GridFormatter getGridFormatter() {
		return grid;
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
	
	/**
	 * Recursive method to remove all held components from the specified manager's list of touch responsive
	 * components. This is very useful if this panel will no longer be used or shown.
	 * @param manager the manager to remove {@link #getAllHeldComponents()} from with 
	 * {@link MenuManager#removeTouchResponsiveComponent(TouchResponsiveComponent)}.
	 */
	public void removeTouchResponsiveness(MenuManager manager) {
		for(MenuComponent comp: getAllHeldComponents()) {
			if(comp instanceof TouchResponsiveComponent)
				manager.removeTouchResponsiveComponent((TouchResponsiveComponent)comp);
			else if(comp instanceof Panel)
				((Panel)comp).removeTouchResponsiveness(manager);
		}
	}
	
}