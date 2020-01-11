package moulton.scalable.draggables;

import java.awt.MenuComponent;

/**
 * A type of {@link MenuComponent} that can be mouse dragged by the user and thus needs to be responsive in that
 * regard through use of {@link #drag(int, int)}. 
 * @author Matthew Moulton
 */
public interface DraggableComponent{
	/**
	 * This method is called by the menu manager to signal that the mouse has moved x and y pixels
	 * since the component was clicked or last updated.
	 * @param dx the change in x of the mouse
	 * @param dy the change in y of the mouse
	 * @return a two dimensional array of how much change was used by the drag in x and y respectively. Any left over movement
	 * will be saved in the MenuManager and summed with new change when it occurs. This makes it possible that if the mouse hasn't
	 * changed enough to cause a change in the state of the draggable the first time, it will add up over several occurrences.
	 */
	public abstract double[] drag(double dx, double dy);
}