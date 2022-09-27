package moulton.scalable.draggables;

/**
 * Components that have attached {@link ScrollBar}s should implement this interface. It is used to
 * give support for mouse wheel scrolling or track pad scrolling. If the component is selected,
 * then that action will cause its connected scroll bar to react.
 * @author Matthew Moulton
 */
public interface ScrollableComponent {

	/**
	 * @return the scroll bar associated with this component's horizontal shifting
	 */
	public abstract ScrollBar getWidthScrollBar();
	
	/**
	 * @return the scroll bar associated with this component's vertical shifting
	 */
	public abstract ScrollBar getHeightScrollBar();
	
	/**
	 * @return where a mouse scrolling action should affect this component. Normally just its
	 * rendering or clicking boundaries. int[0] should hold x points and int[1] should hold y
	 * points. The number of x and y points should be equal.
	 */
	public abstract int[][] getActiveScrollCoordinates();
}
