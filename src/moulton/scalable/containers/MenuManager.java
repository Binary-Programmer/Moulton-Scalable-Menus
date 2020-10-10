package moulton.scalable.containers;

import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.LinkedList;

import moulton.scalable.clickables.Clickable;
import moulton.scalable.clickables.RadioButton;
import moulton.scalable.clickables.TouchResponsiveComponent;
import moulton.scalable.draggables.DraggableComponent;
import moulton.scalable.draggables.ScrollBar;
import moulton.scalable.draggables.ScrollableComponent;
import moulton.scalable.popups.Popup;
import moulton.scalable.texts.HotkeyTextComponent;
import moulton.scalable.texts.TextBox;
import moulton.scalable.texts.TextFormat;
import moulton.scalable.texts.TextInputComponent;
import moulton.scalable.utils.MenuComponent;

/**
 * The menu manager will handle the rendering of its menu on a graphics object through {@link #render(Graphics)}. When the 
 * {@link Container} knows that it is time to render, have it call {@link #render(Graphics)}. Subclass this Menu Manager to create
 * menu set-ups for yourself. Specifically, use {@link #createMenu()} to create a menu Panel and add components to it. Make sure to
 * save the panel you create to {@link #menu}. <p>
 * Will handle events for mouse moving ({@link #mouseMoved(int, int)}), mouse pressing ({@link #mousePressed(int, int)} 
 * and {@link #mouseReleased(int, int)}), and key typing ({@link #keyTyped(char)} and {@link #keyPressed(int)}) as long
 * as these event methods are being called, likely by {@link Container}.
 * @author Matthew Moulton
 */
public abstract class MenuManager {
	/**The panel that will serve as the menu. Exact usage is specified in the class documentation: {@link Panel}.
	 * @see #hasMenu()
	 * @see #getMenu()*/
	protected Panel menu;
	/**A popup that should be drawn on top of the {@link #menu}. If the popup is set (default is null), it will take precedence
	 * over the menu and take the action events such as {@link #mousePressed} and {@link #mouseScrolled(int, int, int)}.
	 * @see #getPopup()
	 * @see #setPopup(Popup)*/
	protected Popup popup = null;
	/**The container for the menus to manage.*/
	protected Container cont;
	/**The Clickable that was last clicked. Clickables are only considered activated once the user has both clicked and released on the same object.
	 * @see #getClicked()
	 * @see #setClicked(Clickable, int, int)*/
	protected Clickable clicked = null;
	/**mouseX and mouseY are the coordinates of the mouse when a mouse button is first pressed. Later updated by 
	 * draggable components through {@link #mouseMoved(int, int)} and {@link DraggableComponent#drag(double, double)}
	 * to be the location of the mouse at the point of last update.*/
	protected double mouseX, mouseY;
	/**Whether the mouse is currently pressed. Where the mouse was pressed is defined as ({@link #mouseX}, {@link #mouseY}).
	 * @see #mousePressed(int, int)
	 * @see #mouseReleased(int, int)*/
	protected boolean mousePressed = false;
	/**All of the components that the menu needs to check if touched every time the mouse moves
	 * @see #addTouchResponsiveComponent(TouchResponsiveComponent)
	 * @see #removeTouchResponsiveComponent(TouchResponsiveComponent)*/
	protected LinkedList<TouchResponsiveComponent> touchCheckList = new LinkedList<>();
	
	/**
	 * Saves the cont field as {@link #cont}.
	 * @param cont the container for the menus to manage.
	 */
	public MenuManager(Container cont){
		this.cont = cont;
	}
	
	/**
	 * Implement to define the properties of the menu to manage. The created menu should be held in {@link #menu}, to be usable by 
	 * other methods. Thus, all components on the menu should have {@link #menu} saved as their parent Panel.
	 */
	public abstract void createMenu();
	
	/**
	 * Implement to add functionality to any clickable components in the menu managed. A good way to check for
	 * component identity is by using {@link Clickable#getId()}.
	 * @param c the clickable that is being activated, the action will start
	 */
	protected abstract void clickableAction(Clickable c);
	
	/**
	 * Implement to add functionality to any clickable components in the menu managed once they lose focus. Focus is defined
	 * as going from clicked to not clicked. For buttons, this occurs once the mouse has been released. For text boxes, this occurs
	 * when something else is clicked. A good way to check for component identity is by using {@link Clickable#getId()}.
	 * @param c the clickable that is being activated, the action will start
	 */
	protected abstract void lostFocusAction(Clickable c);
	
	/**
	 * The Moulton Scalable Menus handles the mouse press once this method has been called by a mouse listener external to Moulton Scalable
	 * Menus. This method systematically processes every component in {@link #menu} to see if the (x,y) coordinate of the mouse click, as 
	 * determined by the mouse event, was within the boundaries of the component. If so, the menu remembers that component as {@link #clicked}
	 * for the occurrence of a mouse release.
	 * @param x the x position of the mouse relative to the JFrame
	 * @param y the y position of the mouse relative to the JFrame
	 */
	public void mousePressed(int x, int y){
		if(popup != null)
			mousePressed(x,y,popup.getBase().getAllHeldComponents());
		else if(menu != null)
			mousePressed(x,y,menu.getAllHeldComponents());
	}
	/**
	 * internal recursive mousePressed call
	 * @param x the x position of the mouse relative to the JFrame
	 * @param y the y position of the mouse relative to the JFrame
	 * @param collection the collection to iterate through
	 */
	protected void mousePressed(int x, int y, Collection<MenuComponent> collection){
		mouseX = x;
		mouseY = y;
		mousePressed = true;
		for(MenuComponent mc:collection){
			//check to see if it is a panel with components in grid
			if(mc instanceof Panel){
				Panel p = (Panel)mc;
				mousePressed(x,y,p.getAllHeldComponents());
			}else if(mc instanceof Clickable){
				Clickable c = (Clickable)mc;
				if(c.clickableAt(x,y)){ //is clicked
					setClicked(c,x,y);
				}else if(c.getClicked()){ //if something still thought it was clicked
					c.setClicked(false, x, y);
				}
			}
		}
	}
	
	/**
	 * The Moulton Scalable Menus handles the mouse release once this method has been called by another source. 
	 * This method checks to see whether the component that the user first clicked on is the same that they released
	 * on. Sets {@link #clicked} as necessary and calls {@link #clickableAction(Clickable)} if the component
	 * selection was successful.
	 * and the mouse (x,y) coordinate as determined by the mouse event is still on that component.
	 * @param x the x coordinate of the mouse when released
	 * @param y the y coordinate of the mouse when released
	 */
	public void mouseReleased(int x, int y){
		mousePressed = false;
		if(clicked != null) {
			//if clicked is still being clicked by the mouse, perform click action
			boolean successfulClick = clicked.clickableAt(x,y);
			if(successfulClick){
				//if the radio button has a group, set it to selected in the group
				if(clicked instanceof RadioButton){
					RadioButton radio = (RadioButton)clicked;
					if(radio.getGroup() != null)
						radio.getGroup().select(radio);
				}
				//call the action for the click
				clickableAction(clicked);
			}
			//check if it lost focus
			//If release deselects- deselect. If not, no deselect even if unsuccessful click
			//sometimes the click action changes the clicked obj, so check again
			if(!successfulClick || clicked.isDeselectedOnRelease())
				setClicked(null,x,y);
		}
	}
	
	/**
	 * Call this when the mouse is scrolled. Positive values are down, and negative values are up.
	 * The scroll will be passed to the scroll bar of the relevant touched {@link ScrollableComponent}.
	 * @param mouseX the x-position of the mouse when the scrolling occurred
	 * @param mouseY the y-position of the mouse when the scrolling occurred
	 * @param scrollAmount the amount that the mouse is scrolled.
	 */
	public void mouseScrolled(int mouseX, int mouseY, int scrollAmount) {
		Panel searchIn = null;
		if(popup != null)
			searchIn = popup.getBase();
		else if(menu != null)
			searchIn = menu;
		
		if(searchIn == null)
			return;
		
		MenuComponent found = findRelevantScrolledComponent(mouseX, mouseY, searchIn);
		if(found == null) //there is no relevant component
			return;
		ScrollBar toScroll = ((ScrollableComponent)found).getHeightScrollBar();
		if(toScroll == null) //there is no scroll bar
			return;
		
		toScroll.setOffset(toScroll.getOffset()+scrollAmount*toScroll.getScrollRate());
	}
	
	/**
	 * Recursive method to find the most specific boundaries 
	 * @param mouseX the mouse's x-position at scroll time
	 * @param mouseY the mouse's y-position at scroll time
	 * @param checkComp the panel of components to search through
	 * @return the MenuComponent most specific and that contains the x,y coordinate
	 */
	private MenuComponent findRelevantScrolledComponent(int mouseX, int mouseY, MenuComponent checkComp) {
		if(checkComp instanceof Panel) { //check children
			Panel compPanel = (Panel)checkComp;
			for(MenuComponent childComp: compPanel.getAllHeldComponents()) {
				MenuComponent relevantScrolled = findRelevantScrolledComponent(mouseX, mouseY, childComp);
				if(relevantScrolled!=null) {
					//must also have a vertical scroll bar
					if(((ScrollableComponent)relevantScrolled).getHeightScrollBar()!=null)
						return relevantScrolled;
				}
			}
		}
		
		//if it was a panel, and the children resulted no, then maybe this is it
		//but regardless, check this
		if(checkComp instanceof ScrollableComponent) {
			ScrollableComponent scrollable = (ScrollableComponent)checkComp;
			int[][] activeCoords = scrollable.getActiveScrollCoordinates();
			Polygon polygon = new Polygon(activeCoords[0], activeCoords[1], activeCoords[0].length);
			//the component must contain the mouse coordinates and have a vertical scroll bar to be relevant
			if(polygon.contains(mouseX, mouseY) && scrollable.getHeightScrollBar()!=null) {
				return checkComp;
			}
		}
		return null; //nothing found here...
	}
	
	/**
	 * Called when the component lost focus and needs to report changes. After this is called to ensure
	 * no component inconsistencies, {@link #lostFocusAction(Clickable)} is called. This will be called
	 * before the component loses focus.
	 * @param c the Clickable menu component that lost focus and needs to report changes to its menu
	 */
	public void componentLostFocus(Clickable c){
		//don't let text boxes report invalid strings
		if(c instanceof TextBox){
			TextBox box = (TextBox) c;
			TextFormat tf = box.getTextFormat();
			if(tf != null && box.getMessage().isEmpty()){
				box.setMessage(tf.emptyText());
			}
		}
		lostFocusAction(c);
	}
	
	/**
	 * Handles any key typed transactions. If the clicked is a {@link TextInputComponent}, the event is
	 * passed to it. This will not occur automatically when a key is typed, rather, it needs to be called
	 * externally. 
	 * @param key the key that was pressed
	 */
	public void keyTyped(char key) {
		if(clicked instanceof HotkeyTextComponent) {
			HotkeyTextComponent c = (HotkeyTextComponent)clicked;
			if(c.isHotkeyEnabled()) {
				//letters typed while control is held are not the same values as without
				//for example, ctr-a is 1, ctr-b is 2, whereas a is 97 and b is 98
				if(key == (char)3) { //copy
					Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
					board.setContents(new StringSelection(c.copy()), null);
					return;
				}else if(key == (char)24) { //cut
					Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
					board.setContents(new StringSelection(c.cut()), null);
					return;
				}else if(key == (char)22) { //paste
					try {
						Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
						c.paste((String)t.getTransferData(DataFlavor.stringFlavor));
					}catch(UnsupportedFlavorException ue) {
						System.err.println("The clipboard contents were not of text type!");
					}catch(Exception e) {
						System.err.println("There was an error in getting the contents of the clipboard!");
						e.printStackTrace(System.err);
					}
					return;
				}else if(key == (char)1) { //select all
					c.selectAll();
					return;
				}
			}
		}
		//if none of the hotkeys were valid, continue with normal functioning
		if(clicked instanceof TextInputComponent){
			TextInputComponent textInputComp = (TextInputComponent) clicked;
			//if it is a valid looking character, just append it
			if(key == KeyEvent.VK_BACK_SPACE || key == KeyEvent.VK_DELETE){ //backspace OR delete
				textInputComp.removeMessage(key==8? 1:-1);
			}else if(key == 10){ //enter
				textInputComp.appendMessage("\n");
				//check for focus removal
				if(textInputComp instanceof TextBox && ((TextBox)textInputComp).getDeselectOnEnter())
					setClicked(null,-1,-1);
			}else if (key>31){
				textInputComp.appendMessage(key+"");
			} //if the key is less than 31 but not checked for, it is just a control character
		}
	}
	
	/**
	 * Handles key pressed transactions for the menu.
	 * If the clicked is a {@link TextBox}, this method will check to see if the key press was either 
	 * the left or right arrow keys. Those keys shift the index for the text box.
	 * @param keyInt the int code of the key pressed. This corresponds to {@link KeyEvent#getExtendedKeyCode()}.
	 */
	public void keyPressed(int keyInt){
		if(clicked instanceof TextBox){
			TextBox box = (TextBox) clicked;
			//if the arrow keys were pressed we need to adjust the blinker position in the text box
			if(keyInt == KeyEvent.VK_LEFT){
				box.shiftIndex(-1);
			}else if(keyInt == KeyEvent.VK_RIGHT){
				box.shiftIndex(1);
			}
		}
	}
	
	/**
	 * This will report to the selected Draggable how much the mouse has moved since the draggable was first clicked.
	 * When the draggable uses some of the change, it can update the mouse coordinates by returning an array of how
	 * much was used. (Index 0 for x, index 1 for y.)
	 * @param x the mouse x coordinate in pixels
	 * @param y the mouse y coordinate in pixels
	 */
	public void mouseMoved(int x, int y) {
		if(mousePressed && clicked instanceof DraggableComponent) {
			double dragDeltaX = x-mouseX;
			double dragDeltaY = y-mouseY;
			double[] changeXY = ((DraggableComponent)clicked).drag(dragDeltaX, dragDeltaY);
			try {
				mouseX += changeXY[0];
				mouseY += changeXY[1];
			}catch(NullPointerException ne) {
				throw new NullPointerException("The draggable component: "+clicked+" must return an "+
						"array of two elements: [changeX, changeY]!");
			}
		}
		LinkedList<TouchResponsiveComponent> touchList = null;
		if(popup != null)
			touchList = popup.getTouchCheckList();
		else
			touchList = touchCheckList;
		
		if(touchList == null)
			return;
		for(TouchResponsiveComponent touchComp: touchList) {
			if(touchComp.isTouchedAt(x, y)) {
				touchComp.setTouched(true);
			}else if(touchComp.isTouched()) { //if it was touched
				touchComp.setTouched(false); //it is no longer
			}
		}
	}
	
	/**
	 * Renders the menu that is being managed ({@link #menu}).
	 * @param g the Graphics to render on
	 */
	public void render(Graphics g) {
		if(menu != null)
			menu.render(g, 0, 0, cont.getMenuWidth(), cont.getMenuHeight());
		if(popup != null)
			popup.render(g, cont.getMenuWidth(), cont.getMenuHeight());
	}
	
	/**
	 * @return true if {@link #menu} is not null. Otherwise, returns false.
	 */
	public boolean hasMenu() {
		return menu != null;
	}
	/**
	 * @return the menu that is currently being used. Defined as {@link #menu}.
	 */
	public Panel getMenu() {
		return menu;
	}
	
	/**
	 * Gets the current {@link #popup} for this menu. Only one popup can be active at a time.
	 * @see #setPopup(Popup)
	 * @return the current popup
	 */
	public Popup getPopup() {
		return popup;
	}
	
	/**
	 * Sets the {@link #popup} for this menu. Only one popup can be active at a time.
	 * @see #getPopup()
	 * @param pop the popup to be set for this menu manager.
	 */
	public void setPopup(Popup pop) {
		this.popup = pop;
	}
	
	/**
	 * Sets {@link #clicked} to defined. If clicked was non-null before the change, the clickable
	 * will be updated with its new non-clicked status, and any losing focus events will be triggered.
	 * The new clicked will be notified of this event.
	 * @param clicked the component that has been clicked. Saved as {@link #clicked}
	 * @param x the x-location of the mouse when clicked. Measured in pixels.
	 * @param y the y-location of the mouse when clicked. Measured in pixels.
	 */
	public void setClicked(Clickable clicked, int x, int y) {
		if(this.clicked != null && clicked != this.clicked) {//tell the old clicked that it has been replaced
			this.clicked.setClicked(false, x, y);
			//also any lost focus actions triggered
			componentLostFocus(this.clicked);
		}
		this.clicked = clicked;
		if(clicked!=null) //tell the clicked that it has been clicked
			clicked.setClicked(true, (int)mouseX, (int)mouseY);
	}
	
	/**
	 * Returns the currently selected clickable component.
	 * @return {@link #clicked}
	 */
	public Clickable getClicked() {
		return clicked;
	}
	
	/**
	 * Adds the component to the list of components to check each time the mouse moves. If the component
	 * is later removed from visibility (for example if the panel it is on is removed from the root-tree),
	 * then the programmer needs to call {@link #removeTouchResponsiveComponent(TouchResponsiveComponent)}.
	 * @param comp the component to add on
	 * @see #removeTouchResponsiveComponent(TouchResponsiveComponent)
	 */
	public void addTouchResponsiveComponent(TouchResponsiveComponent comp) {
		touchCheckList.add(comp);
	}
	/**
	 * Removes the specified component from the touch component list.
	 * @param comp the component to remove
	 * @see #addTouchResponsiveComponent(TouchResponsiveComponent)
	 */
	public void removeTouchResponsiveComponent(TouchResponsiveComponent comp) {
		touchCheckList.remove(comp);
	}
	
	/**
	 * Finds the clickable component with the specified ID. It is assumed that there will only be one since
	 * ID codes are intended to be unique. Uses the recursive method {@link #findComponent(String, Panel, Panel)}.
	 * @param idToFind the ID that should be matched in the found component
	 * @param startPoint the panel where the searching should begin (for speed reasons). If no panel is specified,
	 * the menu root panel ({@link #menu} will be used as the starting point.
	 * @return the component found that has the matching ID, or if such component cannot be found, null.
	 */
	public Clickable findComponent(String idToFind, Panel startPoint) {
		if(startPoint==null) { //if start point not defined, use the root menu panel
			if(hasMenu())
				startPoint = menu;
			else
				return null;
		}
		return findComponent(idToFind, startPoint, startPoint);
	}
	/**
	 * The recursive method to find the component with the specified ID. Use {@link #findComponent(String, Panel)}
	 * for full functionality. This is just meant to be the recursive version.
	 * @param idToFind the ID that should be matched in the found component
	 * @param startPoint the panel where the searching should begin (for speed reasons). If no panel is specified,
	 * the menu root panel ({@link #menu} will be used as the starting point.
	 * @param ignore this panel and its children components will not be searched. The reason for this is because
	 * they have already been searched earlier, so they aren't searched again. If no panel is given, then this
	 * method will not move up the panel tree, or in other words, the parent panels and their components will not
	 * be searched, only the subdirectories of the specified start point panel.
	 * @return the component found that has the matching ID, or if such component cannot be found, null.
	 */
	protected Clickable findComponent(String idToFind, Panel startPoint, Panel ignore) {
		//search down
		for(MenuComponent comp: startPoint.getAllHeldComponents()) {
			if(comp == ignore || comp == null)
				continue;
			if(comp instanceof Clickable) {
				Clickable c = (Clickable) comp;
				if(c.getId().equals(idToFind))
					return c;
			}else if(comp instanceof Panel) {
				Clickable result = findComponent(idToFind, (Panel)comp, null);
				if(result != null)
					return result;
			}
		}
		if(ignore == null) //if there is nothing to ignore, then we shouldn't backtrack
			return null;
		//if we are still here, then we should go up
		if(startPoint.getParent() == null)
			return null;
		return findComponent(idToFind, startPoint.getParent(), startPoint);
	}
}