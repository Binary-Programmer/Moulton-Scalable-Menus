package moulton.scalable.visuals;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import moulton.scalable.containers.Panel;
import moulton.scalable.utils.MenuComponent;

/**
 * A menu component designed to display an {@link Animation}. The component can either keep the ratio of width
 * to height for the animation or stretch to fill the space. This is determined by {@link #maintainAspectRatio}.
 * @author Matthew Moulton
 */
public class AnimatedView extends MenuComponent {
	/**The image that will be drawn onto the coordinates provided.
	 * @see #setAnimation(Animation)
	 * @see #getAnimation()*/
	protected Animation animation = null;
	/**The expression dimensions of the component*/
	protected String width, height;
	/**Whether or not the view should draw the image in the same ratio as given or should stretch it to fill the area of the view.
	 * @see #setMaintainAspectRatio(boolean)
	 * @see #isMaintainAspectRatio()*/
	protected boolean maintainAspectRatio = true;
	/**Whether or not this component should render a black outline on the border of the component.
	 * @see #setOutline(boolean)
	 * @see #getOutline()*/
	protected boolean outline = false;
	
	/**
	 * @param animation The animation that will be drawn onto the coordinates provided
	 * @param parent the panel that this view will reside upon
	 * @param x the x coordinate on the parent, given in menu component value format
	 * @param y the y coordinate on the parent, given in menu component value format
	 * @param w the width of the component, given in menu component value format
	 * @param h the height of the component, given in menu component value format
	 */
	public AnimatedView(Animation animation, Panel parent, String x, String y, String w, String h) {
		super(parent, x, y);
		this.animation = animation;
		this.width = w;
		this.height = h;
	}
	/**
	 * @param animation The animation that will be drawn onto the coordinates provided
	 * @param parent the panel that this view will reside upon
	 * @param x the integer x coordinate this button should appear on its panel
	 * @param y the integer y coordinate this button should appear on its panel
	 */
	public AnimatedView(Animation animation, Panel parent, int x, int y) {
		super(parent, x, y);
		this.animation = animation;
	}

	@Override
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		if(animation != null) {
			int x, y, w, h;
			if(getGridLocation()==null) {
				x = xx + solveString(this.x, ww, hh);
				y = yy + solveString(this.y, ww, hh);
				// variant for input ending points instead of widths indicated by a starting question
				if (this.width.charAt(0) == '?') {
					//solve for the ending point
					int x2 = xx + solveString(this.width.substring(1), ww, hh);
					//deduce the width
					w = x2 - x;
				} else
					w = xx + solveString(this.width, ww, hh);
				
				if (this.height.charAt(0) == '?') {
					int y2 = yy + solveString(this.height.substring(1), ww, hh);
					h = y2 - y;
				} else
					h = yy + solveString(this.height, ww, hh);
			}else {
				x = xx;
				y = yy;
				w = ww;
				h = hh;
			}
			
			BufferedImage img = getAnimation().getPicture();
			if(maintainAspectRatio) {
				double widthRatio = w/(double)img.getWidth();
				double heightRatio = h/(double)img.getHeight();
				//find which aspect is the limiting dimension
				if(widthRatio <= heightRatio) { //the width is proportionately smaller
					int newHeight = (int)(widthRatio*img.getHeight());
					g.drawImage(img, x, y+h/2-newHeight/2, w, newHeight, null);
				}else { //the height is proportionately smaller
					int newWidth = (int)(heightRatio*img.getWidth());
					g.drawImage(img, x+w/2-newWidth/2, y, newWidth, h, null);
				}
			}else
				g.drawImage(img, x, y, w, h, null);
			if(outline) {
				g.setColor(Color.BLACK);
				g.drawRect(x, y, w, h);
			}
		}
	}
	
	/**
	 * Sets the Animation that this view will draw when rendered.
	 * @param animation {@link #animation}.
	 */
	public void setAnimation(Animation animation){
		this.animation = animation;
	}
	/**
	 * Gets the animation to be drawn. By default this method returns {@link #animation}.
	 * @return {@link #animation}
	 */
	public Animation getAnimation(){
		return animation;
	}
	
	/**
	 * Sets whether this view should keep the aspect ratio of the {@link Animation} that it displays.
	 * @param mar sets the value of {@link #maintainAspectRatio}
	 */
	public void setMaintainAspectRatio(boolean mar){
		maintainAspectRatio = mar;
	}
	/**
	 * Returns whether this view will keep the aspect ratio of the {@link Animation} it displays.
	 * @return the value of {@link #maintainAspectRatio}
	 */
	public boolean isMaintainAspectRatio(){
		return maintainAspectRatio;
	}
	
	/**
	 * Sets whether or not the clickable should display a black outline on its border.
	 * @param outline {@link #outline}
	 */
	public void setOutline(boolean outline){
		this.outline = outline;
	}
	/**
	 * Returns whether or not the clickable is displaying a black outline on its border
	 * @return {@link #outline}
	 */
	public boolean getOutline(){
		return outline;
	}
}