package moulton.scalable.utils;

import java.awt.Graphics;
import java.awt.Point;

import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Panel;

/**
 * The parent class for all components to fit inside of a parent panel. Each component is designed to be
 * scalable. The size of text-based components are scaled by height unless disabled by a parent Panel
 * with {@link Panel#setTextResize(boolean)}.
 * @author Matthew Moulton
 */
public abstract class MenuComponent {
	/**The panel that this component lives on.
	 * @see #setParent(Panel)
	 * @see #getParent()*/
	protected Panel parent;
	/**The coordinate of the component specified by an algebraic expression.*/
	protected String x,y;
	/**The default value for {@link #text_resize_factor}. */
	public static final int DEFAULT_ORIGINAL_TEXT_SIZE = 370;
	/**A value that determines the size of text components when resized through {@link #getTextVertResize(int)}.
	 * When the container height is equal to this, then the original font size will be used.
	 * @see #setTextResizeFactor(int)*/
	protected static int text_resize_factor = DEFAULT_ORIGINAL_TEXT_SIZE;
	/**Visibility of component. Invisible components are uneditable.
	 * @see #isVisible()
	 * @see #setVisible(boolean)*/
	protected boolean visible = true;
	/**The coordinates of this component on {@link #parent}'s grid. Null when this component is not using the panel
	 * grid functionality, but rather uses the free-form where the coordinates are derived from {@link #x} and {@link #y}.  */
	protected Point gridPoint = null;
	
	/**
	 * Sets the parent panel for this component. Since the component has no String x and y coordinates, it must be reliant upon the panel for determining that information
	 * by gridding. Therefore, this constructor will also add this component to the panel's grid.
	 * @param parent the parent panel for this component. Components on a menu at base-level have a parent panel of {@link MenuManager#menu}.
	 * @param x the x coordinate of this component in its parent's grid
	 * @param y the y coordinate of this component in its parent's grid
	 */
	public MenuComponent(Panel parent, int x, int y){
		this.parent = parent;
		if(parent != null)
			parent.addToGrid(this, x, y);
		gridPoint = new Point(x,y);
	}
	/**
	 * Sets the parent panel and coordinate {@link #x} and {@link #y} values for this component. They will be solved at run-time by
	 * {@link #solveString(String, int, int)}.
	 * @param parent the parent panel for this component. Components on a menu at base-level have a parent panel of {@link MenuManager#menu}.
	 * @param x the string expression dictating where on the parent panel this component should go. Expressions for 
	 * x-positions are commonly formatted as some variation of "centerx - width/#" in order to scale correctly.
	 * @param y the string expression dictating where on the parent panel this component should go. Expressions for 
	 * y-positions are commonly formatted as some variation of "centery - height/#" in order to scale correctly.
	 * @see MenuComponent#solveString(String, int, int)
	 */
	public MenuComponent(Panel parent, String x, String y){
		this.parent = parent;
		if(parent != null)
			parent.addFreeComponent(this);
		this.x = x;
		this.y = y;
	}
	
	/**Draws on the graphics object to represent this component visually. When this method is called,
	 * the type of panel storing (grid or free) has been taken into account and the dimensions of the space
	 * given to this component have been calculated using {@link #solveString(String, int, int)}.
	 * @param g the graphics object to draw on
	 * @param xx the x-position on the graphics image that this component's alloted space begins
	 * @param yy the y-position on the graphics image that this component's alloted space begins
	 * @param ww the width on the graphics image that this component is given
	 * @param hh the height on the graphics image this component is given
	 */
	public abstract void render(Graphics g, int xx, int yy, int ww, int hh);
	
	/**
	 * Determines the integer value of a coordinate based off the string code by utilizing {@link ExpressionSolver}.
	 * @param code the String to get the format from
	 * @param contWidth the width of the container screen: used in calculations
	 * @param contHeight the height of the container screen: used in calculations
	 * @return the integer value associated with the code
	 */
	protected int solveString(String code, int contWidth, int contHeight){
		if(code==null || code.isEmpty()) return 0;
		ExpressionSolver solver = new ExpressionSolver(contWidth, contHeight);
		return (int)solver.solveString(code);
	}
	
	/**
	 * Returns the grid location of this component if gridded.
	 * @return {@link #gridPoint}
	 */
	public Point getGridLocation(){
		return gridPoint;
	}
	
	/**
	 * Returns the parent panel of this component.
	 * @return ({@link #parent}
	 */
	public Panel getParent(){
		return parent;
	}
	/**
	 * Sets the {@link #parent} of this component, adding this to any parent lists necessary and removing it from the old parent's lists.
	 * @param parent the new parent
	 */
	public void setParent(Panel parent){
		if(this.parent != null){
			if(gridPoint != null)
				this.parent.removeFromGrid(gridPoint.x, gridPoint.y, true);
			else
				this.parent.removeFreeComponent(this);
		}
		
		if(parent != null){
			if(gridPoint!=null)
				parent.addToGrid(this, gridPoint.x, gridPoint.y);
			else
				parent.addFreeComponent(this);
		}
			
		this.parent = parent;
	}
	
	/**
	 * Returns whether this component should be drawn by the parent panel during rendering.
	 * @return {@link #visible}
	 */
	public boolean isVisible() {
		return visible;
	}
	/**
	 * Sets the visibility of this component.
	 * @param visibility replaces the value of {@link #visible}
	 */
	public void setVisible(boolean visibility) {
		visible = visibility;
	}
	
	/**
	 * Sets the static text_original_size constant. Unnecessary for most components, but may be used to give 
	 * more meaning or precision to text sizes for Moulton Scalable Menus.
	 * @param factor an int ratio. higher size means more precise text sizes. When the alloted height for the
	 * textual component is equal to this factor, then the font will be printed in its original (creation) size.
	 */
	public static void setTextResizeFactor(int factor){
		text_resize_factor = factor;
	}
	
	/**When text should be resized based on the vertical size, as indicated in {@link MenuComponent#textResize()},
	 * most components with a textual element will resize their text font to have the size returned by this method. <p>
	 * It is calculated to be in inverse proportion to {@link #text_resize_factor}.
	 * @param originalFontSize the original size of the font, most commonly defined in the creation of the component
	 * @return the new value of the font size for this render
	 */
	protected int getTextVertResize(int originalFontSize) {
		Panel panel = this.parent;
		boolean rootFound = false;
		while(!rootFound) {
			if(panel.getParent() == null)
				rootFound = true;
			else
				panel = panel.getParent();
		}
		return (int)(originalFontSize / ((double) text_resize_factor / panel.getRecentHeight()));
	}
	
	/**
	 * @return if text should resize by height. This is found by asking {@link MenuComponent#parent}.
	 */
	protected boolean textResize() {
		if(parent == null)
			return true;
		else
			return parent.textResize();
	}
}