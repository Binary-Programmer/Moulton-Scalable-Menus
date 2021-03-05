package moulton.scalable.clickables;

import java.awt.Cursor;
import java.awt.Polygon;

import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Panel;
import moulton.scalable.utils.MenuComponent;

/**
 * Ancestor class for components that are clickable. These include buttons and text boxes. All subclasses should define {@link #clickBoundary}
 * when convenient to (usually during rendering) or else it won't be clickable. This can be done with {@link #defineClickBoundary(int[][])}.
 * <p>Components default to being deselected upon mouse release. However, this can be overridden by setting {@link #isDeselectedOnRelease()}
 * to false.
 * @author Matthew Moulton
 */
public abstract class Clickable extends MenuComponent implements TouchResponsiveComponent{
	/**A unique string designed to identify this component when an event occurs.
	 * @see #getId()*/
	protected String id;
	/**Whether or not this component is selected by the press of the mouse currently.
	 * @see #setClicked(boolean, int, int)
	 * @see #isClicked()
	 * @see #clickableAt(int, int)*/
	protected boolean clicked = false;
	/**Whether or not the component can be clicked and used.
	 * @see #isEnabled()
	 * @see #setEnabled(boolean)*/
	protected boolean enabled = true;
	/**Whether the clickable is currently touched by the mouse.
	 * @see #isTouched()
	 * @see #setTouched(boolean)
	 * @see #isTouchedAt(int, int)*/
	protected boolean touched = false;
	/**Whether or not this component should render a black outline on the border of the component.
	 * @see #setOutline(boolean)
	 * @see #getOutline()*/
	protected boolean outline = false;
	
	/**The clickable specified here is the next clickable to be selected in the context of a form.
	 * A complete chain can be formed when multiple clickables are linked together in this way. When
	 * the user has a component in the chain selected and presses tab, selection goes to the next
	 * component.
	 * @see #getFormChain()
	 * @see #setFormChain(Clickable)*/
	protected Clickable formChain = null;
	/**The action that should be executed when the the component is first touched or loses touch.
	 * This action is called by {@link MenuManager#mouseMoved(int, int)}.
	 * @see #getTouchAction()
	 * @see #setTouchAction(EventAction)*/
	protected EventAction touchAction = null;
	/**The action that should be executed when the component is successfully clicked.
	 * This action is called by {@link MenuManager#mouseReleased(int, int)}.
	 * If the EventAction returns true, the click event is not considered consumed.
	 * @see #getClickAction()
	 * @see #setClickAction(EventAction)*/
	protected EventAction clickAction = null;
	/**The action that should be executed when the component loses focus.
	 * This action is called by {@link MenuManager#componentLostFocus(Clickable)}.
	 * If the EventAction returns true, the lost focus event is not considered consumed.
	 * @see #getLostFocusAction()
	 * @see #setLostFocusAction(EventAction)*/
	protected EventAction lostFocusAction = null;
	
	/**
	 * A two-dimensional array holding the pixel x and y points for the polygon that represents this component's clickable area.
	 * clickBoundary[0] should hold x points and clickBoundary[1] should hold y points. The number of x and y points should be equal.
	 * Used to determine whether this component is clickable in {@link #clickableAt(int, int)}.
	 * @see #defineClickBoundary(int[][])
	 */
	protected int[][] clickBoundary = null;

	/**
	 * This is for use of creating a clickable that will reside on the panel in a free-floating manner.
	 * @param id a unique string designed to identify this component when an event occurs
	 * @param parent the parent panel for this component. Components on a menu at base-level have a parent panel of {@link MenuManager#menu}.
	 * @param x the string equation dictating where on the parent panel this component should go. Often formatted as "centerx - width/#".
	 * @param y the string equation dictating where on the parent panel this component should go. Often formatted as "centery - height/#".
	 * @see MenuComponent#solveString(String, int, int)
	 */
	public Clickable(String id, Panel parent, String x, String y) {
		super(parent, x, y);
		this.id = id;
	}
	/**
	 * This is for use of creating a clickable that will reside on the panel in a grid
	 * @param id a unique string designed to identify this component when an event occurs.
	 * @param parent the parent panel for this component. Components on a menu at base-level have a parent panel of {@link MenuManager#menu}.
	 * @param x the x coordinate of this clickable in its parent's grid
	 * @param y the y coordinate of this clickable in its parent's grid
	 */
	public Clickable(String id, Panel parent, int x, int y) {
		super(parent, x, y);
		this.id = id;
	}
	
	
	//CLICK ACTIONS
	/**
	 * Returns whether or not the component is selected by the press of the mouse.
	 * @return {@link #clicked}
	 */
	public boolean isClicked(){
		return clicked;
	}
	
	/**
	 * Determines whether the component would be clicked by using the x and y values of the click and the boundary values as defined in
	 * {@link #clickBoundary}. This will always return false if the component is not editable.
	 * @param x the x value of the mouse when clicked
	 * @param y the y value of the mouse when clicked
	 * @return whether the component was decided to be clicked based on the params
	 */
	public boolean clickableAt(int x, int y) {
		if(!isEnabled() || clickBoundary==null) return false;
		
		Polygon polygon = new Polygon(clickBoundary[0], clickBoundary[1], clickBoundary[0].length);
		if(polygon.contains(x, y))
			return true;
		return false;
	}
	
	/**
	 * Defines the {@link #clickBoundary} from the given. Defining the click boundary can easily be done during rendering
	 * since the component needs to be rendered before it can be clicked.
	 * @param clickBoundary the boundary of this component where each index is a vertex array containing x and y
	 */
	public void defineClickBoundary(int[][] clickBoundary) {
		this.clickBoundary = clickBoundary;
	}
	
	/**
	 * @return {@link #clickBoundary}
	 */
	public int[][] getClickBoundary(){
		return clickBoundary;
	}
	
	/**
	 * Sets whether or not the component is currently selected by the press of a mouse. Different clickables handle this differently. 
	 * Clickables like buttons are only clicked while the mouse button is held down. On the other hand, clickables like text boxes stay
	 * clicked even when the mouse button is released, instead changing to unclicked when the user deselects the text box. Sometimes
	 * clickable components need the exact mouse coordinates when this change occurs, thus they are provided even though they are not
	 * used in this method.
	 * @see #isDeselectedOnRelease()
	 * @param clicked {@link #clicked}
	 * @param mouseX the x coordinate of the mouse when this change occurs
	 * @param mouseY the y coordinate of the mouse when this change occurs 
	 */
	public void setClicked(boolean clicked, int mouseX, int mouseY){
		this.clicked = clicked;
	}
	
	
	//CORE BEHAVIOR
	/**
	 * Returns the id of this component. The id is a unique string designed to identify this component when an event occurs.
	 * @return {@link #id}
	 */
	public String getId(){
		return id;
	}
	
	public String toString() {
		return id+":"+super.toString();
	}
	
	/**
	 * Returns whether or not the component should lose focus once the mouse lets go. 
	 * Defaults to true. <p>
	 * Some classes, like text boxes, need to retain focus so the user can enter secondary input.
	 * These classes need to override this method.
	 * @return whether the component should lose focus on mouse release
	 */
	public boolean isDeselectedOnRelease() {
		return true;
	}
	
	
	//TOUCH FEATURES
	/**
	 * Whether a component is touched at (x,y) or clicked at that location is the same.
	 * @see #clickableAt(int, int)
	 */
	@Override
	public boolean isTouchedAt(int x, int y) {
		return clickableAt(x, y);
	}
	
	/**
	 * @param touched whether the component is touched by the mouse: {@link #touched}.
	 */
	@Override
	public void setTouched(boolean touched) {
		this.touched = touched;
	}
	/**
	 * @return the touched condition as of last update: {@link #touched}.
	 */
	@Override
	public boolean isTouched() {
		return touched;
	}
	
	/**
	 * Returns the cursor type to draw. Defaults to {@link Cursor#HAND_CURSOR}. <p>
	 * If a clickable subclass needs to have a different cursor type (like TEXT_CURSOR for text boxes),
	 * then this method should be overridden.
	 * If a subclass should not change which cursor type is used, {@link Cursor#DEFAULT_CURSOR}
	 * should be returned here.
	 */
	@Override
	public int getTouchedCursorType() {
		return Cursor.HAND_CURSOR;
	}
	
	
	//INDIVIDUAL AESTHETICS AND BEHAVIOR
	/**
	 * Sets whether or not the clickable should be able to be used or just shown.
	 * @param enabled {@link #enabled}
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**Replaced by {@link #setEnabled(boolean)}.
	 * @param enabled whether this component should be enabled.
	 */
	@Deprecated
	public void setEditable(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * Returns whether or not the clickable is usable and enabled.
	 * @return {@link #enabled}
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**Replaced by {@link #isEnabled()}.
	 * @return {@link #enabled}
	 */
	@Deprecated
	public boolean isEditable() {
		return enabled;
	}
	
	/**
	 * Sets whether or not the clickable should display a black outline on its border.
	 * @param outline {@link #outline}
	 */
	public void setOutline(boolean outline){
		this.outline = outline;
	}
	
	/**
	 * Returns whether or not the clickable is displaying a black outline on its border
	 * @return {@link #outline}
	 */
	public boolean getOutline(){
		return outline;
	}
	
	/**
	 * Returns the next clickable in the form chain.
	 * @return {@link #formChain}
	 */
	public Clickable getFormChain() {
		return formChain;
	}
	/**
	 * Sets the clickable that should be next in the form chain.
	 * @param formChain to replace {@link #formChain}
	 */
	public void setFormChain(Clickable formChain) {
		this.formChain = formChain;
	}
	
	
	//EVENT ACTIONS
	/**
	 * The Clickable class defines a touch action that should be executed when this component is
	 * touched or when it stops being touched.
	 * @return {@link #touchAction}
	 */
	public EventAction getTouchAction() {
		return touchAction;
	}
	/**
	 * Returns the action that should be executed on a successful click on this component.
	 * @return {@link #clickAction}
	 */
	public EventAction getClickAction() {
		return clickAction;
	}
	/**
	 * Returns the action that should be executed when this component loses focus.
	 * @return {@link #lostFocusAction}
	 */
	public EventAction getLostFocusAction() {
		return lostFocusAction;
	}
	/**
	 * Sets the touch action that should execute when this class is first touched or loses touch.
	 * @param action to replace {@link #touchAction}
	 */
	public void setTouchAction(EventAction action) {
		this.touchAction = action;
	}
	/**
	 * Sets the action that should execute on a successful click of this component.
	 * @param action {@link #clickAction}
	 */
	public void setClickAction(EventAction action) {
		this.clickAction = action;
	}
	/**
	 * Sets the action that should execute when the component loses focus.
	 * @param action {@link #lostFocusAction}
	 */
	public void setLostFocusAction(EventAction action) {
		this.lostFocusAction = action;
	}
	
}