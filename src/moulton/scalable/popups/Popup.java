package moulton.scalable.popups;

import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;

import moulton.scalable.clickables.TouchResponsiveComponent;
import moulton.scalable.containers.Panel;
import moulton.scalable.utils.ExpressionSolver;
import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Container;

/**
 * A super class for all pop ups. The {@link MenuManager} will give precedence to the popup if there is one
 * set ({@link MenuManager#setPopup(Popup)}). The pop up holds a {@link Panel} that all its components can
 * be components of. That root panel can be retrived by {@link #getBase()}. <p>
 * If {@link #x} or {@link #y} are left unspecified, then the pop up will be centered on that axis within
 * the container. The popup can draw a background over the other components from the main menu, the color
 * of which is specified by {@link #blanketBackground}. Any components belonging to the popup that need
 * touch sensitivity can be added to {@link #touchCheckList}.
 * @author Matthew Moulton
 */
public class Popup {
	/**The root panel of this popup.
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
	
	/**The coordinate of the component specified by an algebraic expression. If null (which is default), the
	 * popup will be centered on the menu.*/
	protected String x,y;
	/**The dimension of the component specified by an algebraic expression.*/
	protected String width, height;
	
	/**
	 * Creates a popup and centers it on the space given by the {@link Container}
	 * @param width the width of the popup. Sets {@link #width}
	 * @param height the height of the popup. Sets {@link #height}
	 * @param color the color of the popup.
	 */
	public Popup(String width, String height, Color color) {
		x = null;
		y = null;
		this.width = width;
		this.height = height;
		base = Panel.createRoot(color);
	}
	
	/**
	 * @param x the x-position of the popup. If set to null, the popup will be horizontally centered.
	 * @param y the y-position of the popup. If set to null, the popup will be vertically centered.
	 * @param width the width of the popup.
	 * @param height the height of the popup.
	 * @param color the color of the popup.
	 */
	public Popup(String x, String y, String width, String height, Color color) {
		x = this.x;
		y = this.y;
		this.width = width;
		this.height = height;
		base = Panel.createRoot(color);
	}
	
	/**
	 * Returns the {@link Panel} that serves as the base level of this popup. This popup is backed up by that
	 * Panel. Thus any changes to it may influence this.
	 * @return {@link #base}
	 */
	public Panel getBase() {
		return base;
	}
	
	/**
	 * Returns the color used to draw over the other components outside of the popup.
	 * @return {@link #blanketBackground}
	 * @see #setBlanketBackground(Color)
	 */
	public Color getBlanketBackground() {
		return blanketBackground;
	}
	/**
	 * Sets the color used to draw over the other components outside of the popup.
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
	 * Renders the popup by calculating the position of the popup on the screen as according to {@link #x},
	 * {@link #y}. {@link #width}, and {@link #height}. If {@link #blanketBackground} is not null, that color
	 * is painted over the previous menu components before drawing the popup.
	 * @param g the graphics object to draw on
	 * @param width the width in pixels of the menu
	 * @param height the height in pixels of the menu
	 */
	public void render(Graphics g, int width, int height) {
		ExpressionSolver solver = new ExpressionSolver(width, height);
		
		int x = 0, y = 0, w = 0, h = 0;
		if(this.width != null && !this.width.isEmpty())
			w = (int)solver.solveString(this.width);
		if(this.height != null && !this.height.isEmpty())
			h = (int)solver.solveString(this.height);
		
		//if x or y is null, then the popup will be centered
		if(this.x != null) {
			if(!this.x.isEmpty())
				x = (int)solver.solveString(this.x);
		}else //centers
			x = (width-w)/2;
		if(this.y != null) {
			if(!this.y.isEmpty())
				y = (int)solver.solveString(this.y);
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
	}
}
