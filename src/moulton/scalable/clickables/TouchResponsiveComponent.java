package moulton.scalable.clickables;

import moulton.scalable.containers.MenuManager;
import moulton.scalable.containers.Container;

/**
 * Interface for components that should be responsive to being touched, meaning having the mouse cursor on part of
 * them that would in most cases be clickable. A notable class that implements this is {@link Clickable} and thus
 * all subclasses of buttons and text boxes.
 * @author Matthew Moulton
 */
public interface TouchResponsiveComponent {
	
	/**
	 * Whether the coordinates of (x,y) define a point that touches this component.
	 * @param x the x-coordinate of the point in question
	 * @param y the y-coordinate of the point in question
	 * @return Whether the point touches the component
	 */
	public boolean isTouchedAt(int x, int y);
	
	/**
	 * When the {@link MenuManager} is processing mouse movement, the results of {@link #isTouchedAt(int, int)} for
	 * the coordinates of the mouse will be passed through this method to update whether this component is touched.
	 * Therefore, this method is responsible for informing the component whether it is touched, whatever effect that
	 * may have.
	 * @param touched update as to whether the component is touched by the mouse
	 */
	public void setTouched(boolean touched);
	
	/**
	 * Whether this component thinks that it is touched.
	 * @return the variable's touched condition as of last time it was updated.
	 */
	public boolean isTouched();
	
	/**
	 * Touch Responsive Components may optionally provide an EventAction that should be executed when the
	 * component is first touched and when it stops being touched. At the time the EventAction is executed,
	 * the component should know its new touch status through {@link #setTouched(boolean)}.
	 * @return the event action to execute. null is acceptable.
	 */
	public EventAction getTouchAction();
	
	/**
	 * Should return the cursor type that should be used when this component is touched. The
	 * result will be handled by {@link MenuManager} and passed off to the container via
	 * {@link Container#setCursor(int)}.
	 * @return the cursor type constant
	 */
	public int getTouchedCursorType();
	
}
