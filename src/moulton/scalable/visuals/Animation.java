package moulton.scalable.visuals;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * A class to describe an animation. The animation holds two connected lists, one of {@link
 * BufferedImage}s, and the other of ints. The images will be the frames in the animation. The list
 * of ints will say how long each frame should be displayed before moving onto the next (measured
 * in milliseconds. Since it is an int-type, the maximum delay per frame is about 2 million
 * seconds. If more time than that needs to be accommodated for, a duplicate frame can be added to
 * the list). The animation needs to be started with {@link #startAnimation()} and can afterward
 * yield the correct image by a call to {@link #getPicture()}.
 * @author Matthew Moulton
 */
public class Animation {
	/**How many times the animation should loop. Default set at 0. Upon reaching the end time for
	 * the last frame, the {@link #showIndex} will return to 0 if loop is greater than 0, then loop
	 * will be decremented. A loop value of -1 indicates infinite looping.
	 * @see #setLoop(int)*/
	protected int loop = 0;
	/**The list of images or frames in the animation.*/
	protected ArrayList<BufferedImage> imageList;
	/**The list of times that go along with each image in {@link #imageList}. A measurement of how
	 * long (in milliseconds) each frame should be displayed before continuing to the next.*/
	protected ArrayList<Integer> timeList;
	/**The listener should be reported to when important events in the animation occur.
	 * @see #setAnimationListener(AnimationListener)*/
	protected AnimationListener listener;
	
	/**The index of the lists that the animation is currently showing. Upon a timeout for the last
	 * index, the index will return to 0 if loop is greater than 0. If not, the animation will stop
	 * playing (as determined by {@link #playing}. However, a loop value of -1 is infinite
	 * looping.*/
	protected int showIndex = 0;
	/**Whether the animation is running through the frames. If this is false, {@link #showIndex}
	 * will stay constant.
	 * @see #setAnimationPlay(boolean)
	 * @see #startAnimation()*/
	protected boolean playing = false;
	/**The last time in ms that the animation checked its image.*/
	protected long lastTime;
	
	/**Creates a new animation with the frames as defined and time values set to match the number
	 * of frames. Once this animation is ready to be shown, {@link #startAnimation()} should be
	 * called. Afterwards, {@link #getPicture()} will return the current frame to be shown.
	 * @param defaultTime all time values corresponding to the given frames will be set to this
	 * value. They can be altered by using {@link #setTimeLimits(int, int...)}.
	 * @param frames the image frames for this animation.*/
	public Animation(int defaultTime, BufferedImage ...frames) {
		imageList = new ArrayList<>(frames.length);
		timeList = new ArrayList<>(frames.length);
		for(BufferedImage frame: frames) {
			imageList.add(frame);
			timeList.add(defaultTime);
		}
	}
	
	/**Alters the time limits for the animation. Each time provided will be set relative to the
	 * start index. Therefore, times[0] will be at startIndex in this animation, times[1] will be
	 * at startIndex+1 and so forth.
	 * @param startIndex the index where the first time is defined
	 * @param times the times that should replace the previously saved times. These times will be a
	 * measurement in milliseconds that the animation should pause on the corresponding frames
	 * before continuing.
	 * @return this*/
	public Animation setTimeLimits(int startIndex, int ...times) {
		for(int i=0; i<times.length; i++) {
			if(startIndex+i>=timeList.size())
				break;
			
			timeList.set(startIndex+i, times[i]);
		}
		return this;
	}
	
	/**Starts the animation by setting {@link #showIndex} to 0 and setting the play state to true
	 * with {@link #setAnimationPlay(boolean)}. Also saves the current time in milliseconds to know
	 * how much time has elapsed once {@link #getPicture()} is called.*/
	public void startAnimation() {
		setAnimationPlay(true);
		showIndex = 0;
		lastTime = System.currentTimeMillis();
	}
	
	/**Pauses or plays the animation. If the animation is paused, the timer for the current index
	 * will be reset. For example, if the first frame should wait for 1000 ms and 900 ms have
	 * elapsed before the animation is paused, once the animation plays again, another 1000 ms
	 * needs to elapse before the next frame is shown.
	 * @param playState whether the animation should be played (true) or paused (false)*/
	public void setAnimationPlay(boolean playState) {
		playing = playState;
		if(playState) {
			lastTime = System.currentTimeMillis();
		}
	}
	
	/**Updates the animation using {@link System#currentTimeMillis()} for the time, changing the
	 * index as necessary. After, the current image for the Animation is returned.
	 * @return the image that should be shown in the animation right now*/
	public BufferedImage getPicture() {
		if(playing) {
			long currentTime = System.currentTimeMillis();
			//the elapsed time
			currentTime -= lastTime;
			//adjust the index
			while(currentTime>=timeList.get(showIndex)) {
				lastTime += currentTime; //update the current time
				//decrease the time by the timing for this frame and increase the index
				currentTime -= timeList.get(showIndex++);
				if(showIndex>=timeList.size()) { //the end of animation reached
					if(loop>0) { //loop again
						showIndex = 0;
						loop--;
						if(listener != null)
							listener.animationLoopEvent(this);
					}else if(loop==-1){
						showIndex = 0;
						if(listener != null)
							listener.animationLoopEvent(this);
					}else { //stop looping
						setAnimationPlay(false);
						showIndex = timeList.size()-1; //return index to valid state
						if(listener != null)
							listener.animationEndEvent(this);
						break;
					}
				}
			}
			return imageList.get(showIndex);
		}else {
			return imageList.get(showIndex);
		}
	}
	
	/**Sets the loop value for the animation.
	 * @param loop {@link #loop}*/
	public Animation setLoop(int loop) {
		this.loop = loop;
		return this;
	}
	
	/**Sets the animation listener that will be notified when important events occur.
	 * @param listener the animation listener that will replace {@link #listener}
	 * return this 
	 */
	public Animation setAnimationListener(AnimationListener listener) {
		this.listener = listener;
		return this;
	}

}