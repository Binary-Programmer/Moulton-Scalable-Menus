package moulton.scalable.clickables;

import java.awt.Color;

import moulton.scalable.containers.Panel;
import moulton.scalable.containers.MenuManager;

/**
 * A button where only one in the group can be clicked at a time. Used in conjunction with {@link
 * RadioGroup}. If this button has no group, then its behavior is that of a normal button: being
 * freely clickable while editable.
 * <p>
 * As a subclass of {@link Clickable} which implements {@link TouchResponsiveComponent}, this
 * button and subclasses can be responsive to mouse touching. However, simply creating the button
 * and adding it to the Panel tree is insufficient for this functionality. The button must also be
 * added to the {@link MenuManager}'s touch list by {@link MenuManager#addTouchResponsiveComponent(
 * TouchResponsiveComponent)}.
 * @author Matthew Moulton
 */
public abstract class RadioButton extends Clickable {
	/**The group of this button. At most one component in a group can be selected at a time.
	 * Setting this to not null will make the subclass act like a radio button.
	 * @see #setGroup(RadioGroup)
	 * @see #getGroup()}*/
	protected RadioGroup group = null;
	/**The color of the button.*/
	protected Color color;
	/**The color of the button when clicked*/
	protected Color colorDark;
	/**The color of the button when not enabled*/
	protected Color colorLight;
	/**The color of the button when touched
	 * @see #setTouchedColor(Color)*/
	protected Color colorTouched = null;
	
	/**
	 * Creates a radio button, which must have some basic features. Initializes the
	 * location of this button with string expressions.
	 * @param parent the panel in which this button resides
	 * @param x the left-most x position of this button
	 * @param y the top-most y position of this button
	 * @param color the color of this button's face
	 */
	public RadioButton(Panel parent, String x, String y, Color color) {
		super(parent, x, y);
		this.color = color;
		if(color != null) {
			colorDark = color.darker();
			colorLight = color.brighter();
		}
	}
	/**
	 * Creates a radio button, which must have some basic features. Initializes the
	 * location of this button with string expressions.
	 * @deprecated use {@link #RadioButton(Panel, String, String, Color)} and
	 * {@link #setId(String)}.
	 */
	@Deprecated(since="1.15")
	public RadioButton(String id, Panel parent, String x, String y, Color color) {
		super(parent, x, y);
		this.id = id;
		this.color = color;
		if(color != null) {
			colorDark = color.darker();
			colorLight = color.brighter();
		}
	}
	/**
	 * Creates a radio button, which must have some basic features. Initializes the
	 * location of this button to reside in a grid at the int coordinates.
	 * @param parent the panel in which this button resides
	 * @param x the x grid index of this component
	 * @param y the y grid index of this component
	 * @param color the color of this button's face
	 */
	public RadioButton(Panel parent, int x, int y, Color color) {
		super(parent, x, y);
		this.color = color;
		if(color != null) {
			colorDark = color.darker();
			colorLight = color.brighter();
		}
	}
	/**
	 * Creates a radio button, which must have some basic features. Initializes the
	 * location of this button to reside in a grid at the int coordinates.
	 * @deprecated use {@link #RadioButton(Panel, int, int, Color)} and
	 * {@link #setId(String)}
	 */
	public RadioButton(String id, Panel parent, int x, int y, Color color) {
		super(parent, x, y);
		this.id = id;
		this.color = color;
		if(color != null) {
			colorDark = color.darker();
			colorLight = color.brighter();
		}
	}
	
	/**
	 * Sets the {@link group} of this button.
	 * @param rg the new group.
	 * @return this
	 */
	public RadioButton setGroup(RadioGroup rg){
		group = rg;
		return this;
	}
	
	/**
	 * Returns the radio group.
	 * @return {@link #group}
	 */
	public RadioGroup getGroup(){
		return group;
	}
	
	/**
	 * Returns true if this button knows it is clicked (through {@link Clickable#clicked}) or if
	 * the group says that the selected button is this.
	 */
	@Override
	public boolean isClicked() {
		return super.isClicked() || (getGroup()!=null && getGroup().getSelected() == this);
	}
	
	/**
	 * Called in rendering. Gives the fill color of the button. If the button is not editable,
	 * {@link #colorLight} is used. If the button is clicked, {@link #colorDark} is used. If the
	 * button is touched and has a touch color ({@link #colorTouched}), that is used. Otherwise,
	 * the normal {@link #color} is returned.
	 * @return the applicable color for rendering the fill of the button
	 * @see #isEnabled()
	 * @see #isClicked()
	 * @see #isTouched()
	 */
	public Color getFillColor() {
		if (!isEnabled())
			return colorLight;
		if(isClicked())
			return colorDark;
		if(isTouched() && colorTouched != null)
			return colorTouched;
		
		return color;
	}
	
	/**
	 * If touchedColor is null, then the toggle outline effect will be used instead
	 * @param touchedColor the color to be set as {@link #colorTouched}
	 * @return this
	 */
	public RadioButton setTouchedColor(Color touchedColor) {
		if(colorTouched==null && touchedColor != null) {
			/* if the button is touched presently and the new color is not null, that means that
			 * the component will show touch through the new color instead of toggling outline.
			 * Therefore, the outline should go back to the original state.
			 */
			if(touched)
				setOutline(!getOutline());
			
			//set the new darker color
			colorDark = touchedColor.darker();
		}else {
			//resets to the old darker color
			colorDark = color.darker();
		}	
		this.colorTouched = touchedColor;
		return this;
	}
	
	/**
	 * Sets whether this button is touched. If the touched color is unset, then an outline toggle
	 * will be used to show touch. Therefore, setting the touch here may trigger the toggle.
	 */
	@Override
	public void setTouched(boolean touched) {
		//if the touch state has changed
		if(touched != this.touched && colorTouched == null) //if the outline effect should be used
			setOutline(!getOutline());
		this.touched = touched;
	}
	
}
