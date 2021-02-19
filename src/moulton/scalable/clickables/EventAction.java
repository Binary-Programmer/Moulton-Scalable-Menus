package moulton.scalable.clickables;

public interface EventAction {
	
	/**
	 * The action that should occur when the specified event does.
	 * @return whether other event actions should occur for this (true) or whether
	 * this event has been consumed (false).
	 */
	public boolean onEvent();

}
