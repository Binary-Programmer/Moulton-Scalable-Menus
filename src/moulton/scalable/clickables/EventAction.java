package moulton.scalable.clickables;

/**
 * The EventAction is an interface serving like a functor. It has a single method to be called,
 * {@link #onEvent()}, which should trigger the execution of the action.
 * @author Matthew Moulton
 */
public interface EventAction {
	
	/**
	 * The action that should occur when the specified event does.
	 * @return whether other event actions should occur for this (true) or whether
	 * this event has been consumed (false).
	 */
	public boolean onEvent();

}
