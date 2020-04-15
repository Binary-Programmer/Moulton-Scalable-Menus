package moulton.scalable.containers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.security.InvalidParameterException;

import moulton.scalable.utils.MenuComponent;

/**
 * A subclass of {@link PanelPlus}. Built to hold a variable number of elements in the list. These elements
 * must be of type {@link MenuComponent}, whether they be panels or individual components. Elements can be
 * added when the components set it parent, calling {@link #addComponent(MenuComponent, int)}. Elements can
 * be removed to the list by {@link #removeComponent(int, boolean)}.
 * @author Matthew Moulton
 */
public class ListPanel extends PanelPlus {
	/**The height of each row specified by an algebraic expression.*/
	protected String rowHeight;

	/**
	 * @param rowHeight the height of each row, specified as an algebraic expression of a menu component
	 * @param parent the panel this panel will reside upon.
	 * @param x the x coordinate on the screen, given in menu component value format
	 * @param y the y coordinate on the screen, given in menu component value format
	 * @param shownWidth the component width that will be displayed, given in menu component value format
	 * @param shownHeight the component height that will be displayed, given in menu component value format
	 * @param fullWidth the entire width of the panel. In general, intended to be greater than shownWidth
	 * @param color the background color for the box when editable
	 */
	public ListPanel(String rowHeight, Panel parent, String x, String y, String shownWidth, String shownHeight,
			String fullWidth, Color color) {
		super(parent, x, y, shownWidth, shownHeight, fullWidth, "0", color);
		this.rowHeight = rowHeight;
	}
	/**
	 * @param rowHeight the height of each row, specified as an algebraic expression of a menu component
	 * @param parent the panel this panel will reside upon.
	 * @param x the integer x coordinate this panel should appear on its parent panel
	 * @param y the integer y coordinate this panel should appear on its parent panel
	 * @param fullWidth the entire width of the panel. In general, intended to be greater than the shown width,
	 * determined at run time by the space on the grid allotted to this panel.
	 * determined at run time by the space on the grid allotted to this panel.
	 * @param color the background color for the box when editable
	 */
	public ListPanel(String rowHeight, Panel parent, int x, int y, String fullWidth, String fullHeight, Color color) {
		super(parent, x, y, fullWidth, "0", color);
		this.rowHeight = rowHeight;
	}

	/**
	 * The ListPanel stores elements in a vertical list. This element will be added at the end.<p>
	 * This method calls {@link #addComponent(MenuComponent, int)} to store the component.
	 */
	@Override
	public boolean addFreeComponent(MenuComponent comp) {
		return addComponent(comp, grid.getGridHeight());
	}

	/**
	 * The ListPanel stores elements in a vertical list. The specified component will be inserted at its y
	 * location in that list, displacing other elements if necessary. The x-value of the component will
	 * be discarded.<p>
	 * This method calls {@link #addComponent(MenuComponent, int)} to store the component.
	 * @param x an unused value
	 * @param y the location on the list that the component should be stored
	 */
	@Override
	public boolean addToGrid(MenuComponent comp, int x, int y) {
		return addComponent(comp, y);
	}

	/**
	 * The ListPanel stores elements in a vertical list. If there is an element at the given y-value, it
	 * will be removed. The x-value is unused.<p>
	 * This method calls {@link #removeComponent(int, boolean)} to perform the deletion.
	 * @param x an unused value
	 * @param y where on the list the component should be deleted from
	 * @return whether the component at the specified list index (y-value) was removed
	 */
	@Override
	public boolean removeFromGrid(int x, int y, boolean resize) {
		return removeComponent(y, resize);
	}

	/**
	 * Inserts a component to the list at the specified index. Displaces other elements if necessary.
	 * 
	 * @param comp the component to be added
	 * @param listIndex the index of the vertical list to add the component at
	 * @return whether the component was successfully added
	 */
	public boolean addComponent(MenuComponent comp, int listIndex) {
		if(listIndex < 0)
			throw new InvalidParameterException("The list index must be >= 0. Given: "+listIndex);
		
		int insertIndex = grid.getGridHeight();
		while(insertIndex > listIndex) {
			MenuComponent temp = grid.getAt(0, insertIndex-1);
			if(temp != null)
				temp.setGridLocation(new Point(0, insertIndex));
			grid.addComponent(temp, 0, insertIndex);
			insertIndex--;
		}
		comp.setGridLocation(new Point(0, listIndex));
		grid.addComponent(comp, 0, listIndex);
		return true;
	}

	/**
	 * Removes the component at the specified index, if any.
	 * 
	 * @param listIndex the index of the vertical list to remove from
	 * @param pull whether this deletion should pull successive elements down one index
	 * @return whether the component was successfully removed
	 */
	public boolean removeComponent(int listIndex, boolean pull) {
		boolean remove = grid.removeComponent(0, listIndex, !pull);
		if(!remove)
			return false;
		
		if(pull) {
			while(listIndex<grid.getGridHeight()-1) {
				MenuComponent temp = grid.getAt(0, listIndex+1);
				if(temp != null)
					temp.setGridLocation(new Point(0, listIndex));
				grid.addComponent(temp, 0, listIndex);
				listIndex++;
			}//remove the remains
			grid.removeComponent(0, listIndex, true);
		}
		return true;
	}
	
	/**
	 * Draws on the graphics object to represent this ListPanel. The ListPanel changes its {@link PanelPlus#fullHeight}
	 * at render time to have each row drawn at the proper {@link #rowHeight}. Render can also create
	 * dummy null values in the grid to space out the rows properly.
	 */
	@Override
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		//full height is going to be the height of each row multiplied by the number of elements
		int rowH = solveString(rowHeight, ww, hh);
		int fullH = rowH * grid.getGridHeight();
		this.fullHeight = ""+fullH;
		
		//if full height is less than the shown height, we have to add a null element to the grid to correct
		int shownH; //the shown height
		if(parent != null && getGridLocation()==null) {
			if (this.height.charAt(0) == '?') {
				int y = yy + solveString(this.y, ww, hh);
				int y2 = yy + solveString(this.height.substring(1), ww, hh);
				shownH = y2 - y;
			} else
				shownH = solveString(this.height, ww, hh);
		}else
			shownH = hh;
		
		if(fullH < shownH && shownH > 0) {
			int nullPosition = grid.getGridHeight();
			Double beforeWeight = grid.getRowWeight(nullPosition);
			grid.specifyRowWeight(nullPosition, (shownH - fullH)/((double)rowH));
			grid.addComponent(null, 0, nullPosition);
			//now render
			super.render(g, xx, yy, ww, hh);
			//fix the temporary weight values and null item
			grid.removeComponent(0, nullPosition, true);
			if(beforeWeight == null)
				grid.specifyRowWeight(nullPosition, 1d);
			else
				grid.specifyRowWeight(nullPosition, beforeWeight);
		}else
			super.render(g, xx, yy, ww, hh);
	}

}
