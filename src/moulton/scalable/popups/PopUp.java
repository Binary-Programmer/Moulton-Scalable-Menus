package moulton.scalable.popups;

import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;

import moulton.scalable.clickables.TouchResponsiveComponent;
import moulton.scalable.containers.Panel;
import moulton.scalable.utils.MenuSolver;
import moulton.scalable.utils.MenuSolver.Expression;
import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Container;

/**
 * A super class for all pop ups. The {@link MenuManager} will give precedence to the pop up if there is one
 * set ({@link MenuManager#setPopUp(PopUp)}). The pop up holds a {@link Panel} that all its components can
 * be components of. That root panel can be retrieved by {@link #getBase()}. <p>
 * If {@link #x} or {@link #y} are left unspecified, then the pop up will be centered on that axis within
 * the container. The pop up can draw a background over the other components from the main menu, the color
 * of which is specified by {@link #blanketBackground}. Any components belonging to the pop up that need
 * touch sensitivity can be added to {@link #touchCheckList}.
 * @author Matthew Moulton
 */
public class PopUp {
	/**The root panel of this pop up.
	 * @see #getBase()*/
	protected Panel base;
	/**All of the components that need to be check if touched when the mouse moves
	 * @see #getTouchCheckList()
	 * @see #addTouchResponsiveComponent(TouchResponsiveComponent)
	 * @see #removeTouchResponsiveComponent(TouchResponsiveComponent)*/
	protected LinkedList<TouchResponsiveComponent> touchCheckList = new LinkedList<>();
	/**The color to draw on top of the other menu that the {@link MenuManager} has. Recommended to be
	 * translucent.*/
	protected Color blanketBackground = null;
	
	/**The coordinate of the component specified by an algebraic expression.
	 * If null (which is default), the pop up will be centered on the menu.*/
	protected Expression x,y;
	/**The dimension of the component specified by an algebraic expression.*/
	protected Expression width, height;
	
	/** The pop up for this pop up. Pop ups can be saved recursively. */
	protected PopUp popup = null;
	/** The solver for this menu item */
	protected MenuSolver solve = new MenuSolver();
	
	/**
	 * Creates a pop up and centers it on the space given by the {@link Container}
	 * @param width the width of the pop up. Sets {@link #width}
	 * @param height the height of the pop up. Sets {@link #height}
	 * @param color the color of the pop up.
	 */
	public PopUp(String width, String height, Color color) {
		x = null;
		y = null;
		this.width = solve.parse(width, false, false);
		this.height = solve.parse(height, false, false);
		base = Panel.createRoot(color);
	}
	
	/**
	 * @param x the x-position of the pop up. If set to null, the pop up will be horizontally centered.
	 * @param y the y-position of the pop up. If set to null, the pop up will be vertically centered.
	 * @param width the width of the pop up.
	 * @param height the height of the pop up.
	 * @param color the color of the pop up.
	 */
	public PopUp(String x, String y, String width, String height, Color color) {
		this.x = solve.parse(x, false, false);
		this.y = solve.parse(y, false, false);
		this.width = solve.parse(width, false, false);
		this.height = solve.parse(height, false, false);
		base = Panel.createRoot(color);
	}
	
	/**
	 * Returns {@link #popup}, which serves as the base level of this pop up.
	 * This pop up is backed by that Panel. Thus any changes to it may influence this.
	 * <p>
	 * If this pop up has another pop up saved in {@link #popup}, the base of the
	 * saved pop up will be returned instead.
	 * @return the pop up base.
	 */
	public Panel getBase() {
		if(popup != null)
			return popup.base;
		return base;
	}
	
	/**
	 * Returns the color used to draw over the other components outside of the pop up.
	 * @return {@link #blanketBackground}
	 * @see #setBlanketBackground(Color)
	 */
	public Color getBlanketBackground() {
		return blanketBackground;
	}
	/**
	 * Sets the color used to draw over the other components outside of the pop up.
	 * @param background to set {@link #blanketBackground}
	 * @see #getBlanketBackground()
	 */
	public void setBlanketBackground(Color background) {
		this.blanketBackground = background;
	}

	/**
	 * Returns the list of components that the {@link MenuManager} should check for selection each time the
	 * mouse moves. For adding and removing components from this list, use {@link #addTouchResponsiveComponent(TouchResponsiveComponent)}
	 * and {@link #removeTouchResponsiveComponent(TouchResponsiveComponent)} respectively.
	 * @return {@link #touchCheckList}
	 */
	public LinkedList<TouchResponsiveComponent> getTouchCheckList() {
		return touchCheckList;
	}
	
	/**Use {@link #addTouchComponent(TouchResponsiveComponent)} instead*/
	@Deprecated
	public void addTouchResponsiveComponent(TouchResponsiveComponent comp) {
		touchCheckList.add(comp);
	}
	/**
	 * Adds the component to the list of components to check each time the mouse moves. If the component
	 * is later removed from visibility (for example if the panel it is on is removed from the root-tree),
	 * then the programmer needs to call {@link #removeTouchResponsiveComponent(TouchResponsiveComponent)}.
	 * @param comp the component to add on
	 * @see #removeTouchResponsiveComponent(TouchResponsiveComponent)
	 */
	public void addTouchComponent(TouchResponsiveComponent comp) {
		touchCheckList.add(comp);
	}
	/**Use {@link #removeTouchComponent(TouchResponsiveComponent)} instead*/
	@Deprecated
	public void removeTouchResponsiveComponent(TouchResponsiveComponent comp) {
		touchCheckList.remove(comp);
	}
	/**
	 * Removes the specified component from the touch component list.
	 * @param comp the component to remove
	 * @see #addTouchResponsiveComponent(TouchResponsiveComponent)
	 */
	public void removeTouchComponent(TouchResponsiveComponent comp) {
		touchCheckList.remove(comp);
	}

	/**
	 * Renders the pop up by calculating the position of the pop up on the screen as according to {@link #x},
	 * {@link #y}. {@link #width}, and {@link #height}. If {@link #blanketBackground} is not null, that color
	 * is painted over the previous menu components before drawing the pop up.
	 * @param g the graphics object to draw on
	 * @param width the width in pixels of the menu
	 * @param height the height in pixels of the menu
	 */
	public void render(Graphics g, int width, int height) {
		MenuSolver solve = new MenuSolver();
		solve.updateValues(width, height);
		
		int x = 0, y = 0, w = 0, h = 0;
		w = solve.eval(this.width);
		h = solve.eval(this.height);
		
		//if x or y is null, then the popup will be centered
		if(this.x != null) {
			x = solve.eval(this.x);
		}else //centers
			x = (width-w)/2;
		if(this.y != null) {
			y = solve.eval(this.y);
		}else //centers
			y = (height-h)/2;
		
		//draw the background if any
		if(blanketBackground != null) {
			g.setColor(blanketBackground);
			g.fillRect(0, 0, width, height);
		}
		
		//now render the base panel
		if(base != null)
			base.render(g, x, y, w, h);
		
		//continue the recursion, if any
		if(popup != null)
			popup.render(g, width, height);
	}
	
	/**
	 * Returns the pop up that this pop up has saved.
	 * @return {@link #popup}
	 */
	public PopUp getPopup() {
		return popup;
	}
}
