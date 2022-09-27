package moulton.scalable.clickables;

import java.util.LinkedList;

/**
 * Holds {@link RadioButton}s. Only one button in the group can be selected at a time.
 * @author Matthew Moulton
 */
public class RadioGroup {
	/**The radio buttons in this group. Add buttons with {@link #addButton(RadioButton)} and if
	 * necessary, remove them with {@link #removeButton(RadioButton)}.*/
	protected LinkedList<RadioButton> group = new LinkedList<RadioButton>();
	/**The radio button that is selected. Only one at a time.
	 * @see #getSelected()}
	 * @see #select(RadioButton)*/
	protected RadioButton selected = null;
	
	/**
	 * Creates a new radio group. Though it is not useful until buttons are added.
	 * @see #addButton(RadioButton)
	 */
	public RadioGroup(){}
	
	/**
	 * Creates a new radio group and adds the buttons specified to the group.
	 * @param buttons the buttons to be added to the group
	 */
	public RadioGroup(RadioButton ...buttons){
		for(RadioButton rb: buttons){
			group.add(rb);
			rb.setGroup(this);
		}
	}
	
	/**
	 * Adds the button provided to the group if not already added. Updates the button's group to
	 * this.
	 * @param rb the button to add
	 * @see #removeButton(RadioButton)
	 */
	public void addButton(RadioButton rb){
		if(!group.contains(rb)) {
			group.add(rb);
			rb.setGroup(this);
		}
	}
	
	/**
	 * Removes the specified button from the group. Updates the button's group to null.
	 * @param rb the button to remove
	 * @return whether the button was found to be removed
	 * @see #addButton(RadioButton)
	 */
	public boolean removeButton(RadioButton rb) {
		if(group.contains(rb)) {
			group.remove(rb);
			rb.setGroup(null);
			if(rb == selected)
				selected = null;
			return true;
		}else
			return false;
	}
	
	/**
	 * Selects this radio button and deselects all others in this group
	 * @param selected the radio button to be selected
	 * @see #getSelected()
	 */
	public void select(RadioButton selected){
		RadioButton lastSelected = this.selected;
		this.selected = selected;
		if(lastSelected!=null)
			lastSelected.setClicked(false,-1,-1);
	}
	
	/**
	 * The radio button in this group that is currently selected. Only one button is allowed to be
	 * selected at a time.
	 * @return the presently selected button in the group
	 */
	public RadioButton getSelected(){
		return selected;
	}
}
