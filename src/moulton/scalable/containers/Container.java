package moulton.scalable.containers;

import java.awt.Cursor;
import java.awt.Component;

/**
 * The container of the menu. This class is usually responsible to handle any mousePress, mouseRelease, 
 * mouseMoved, keyPressed, and keyTyped methods and pass them to the menus in use. This can easily be
 * achieved with mouse and key listeners.
 * @author Matthew Moulton
 */
public interface Container {
	/**
	 * @return the width, in pixels, the menu should fill
	 */
	public int getMenuWidth();
	
	/**
	 * @return the height, in pixels, the menu should fill
	 */
	public int getMenuHeight();
	
	/**
	 * This method is responsible for setting the cursor of the menu to the specified
	 * type. If the container is implemented by a JFrame or a JPanel, this method can
	 * be achieved by calling:
	 * <code>yourComp.setCursor(Cursor.getPredefinedCursor(cursorType));</code> <p>
	 * Due to the minimalistic policy of Moulton Scalable Menus, only touch responsive
	 * components added to {@link MenuManager#touchCheckList} will be queried for
	 * cursor updates.
	 * @param cursorType the type of cursor as specified by {@link Cursor}
	 * @see Component#setCursor(Cursor)
	 */
	public void setCursor(int cursorType);
}