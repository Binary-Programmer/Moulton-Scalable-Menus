package moulton.scalable.visuals;

/**
 * An interface for objects that listen to the actions of an {@link Animation}.
 * Animations can play on an {@link AnimatedButton} or an {@link AnimatedView}. 
 * @author Matthew Moulton
 */
public interface AnimationListener {
	
	/**
	 * This action occurs when an animation reaches a natural end and stops. This will
	 * <i>not</i> be called when the animation is paused by 
	 * {@link Animation#setAnimationPlay(boolean)}, nor will it be called when the animation
	 * reaches the end but continues if it is set to loop.<p>
	 * Also recall that the animation works by a lazy rendering system. It does not keep its
	 * own time, but rather relies on being rendered by {@link Animation#getPicture()} to
	 * process its events.
	 * @param animation the animation that reached an end
	 */
	public void animationEndEvent(Animation animation);
	
	/**
	 * This action occurs when an animation reaches a natural end but continues because of
	 * a loop setting.<p>
	 * Recall that the animation works by a lazy rendering system. It does not keep its
	 * own time, but rather relies on being rendered by {@link Animation#getPicture()} to
	 * process its events.
	 * @param animation the animation that looped
	 */
	public void animationLoopEvent(Animation animation);
}
