package moulton.scalable.containers;

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
}