package moulton.scalable.containers;

import java.awt.Color;
import java.awt.Graphics;

import moulton.scalable.utils.MenuComponent;

public class ListPanel extends PanelPlus {
	protected String rowHeight;

	public ListPanel(String rowHeight, Panel parent, String x, String y, String shownWidth, String shownHeight,
			String fullWidth, Color color) {
		super(parent, x, y, shownWidth, shownHeight, fullWidth, "0", color);
		this.rowHeight = rowHeight;
	}

	public ListPanel(Panel parent, int x, int y, String fullWidth, String fullHeight, Color color) {
		super(parent, x, y, fullWidth, fullHeight, color);
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
	 * The ListPanel stores elements in a vertical list. The specified component will be placed at its y
	 * location in that list, replacing another element if necessary. The x-value of the component will
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
	 * Adds a component to the list at the specified index. Replaces any pre-existing elements at that
	 * index.
	 * 
	 * @param comp the component to be added
	 * @param listIndex the index of the vertical list to add the component at
	 * @return whether the component was successfully added
	 */
	public boolean addComponent(MenuComponent comp, int listIndex) {
		grid.addComponent(comp, 0, listIndex);
		return true;
	}

	/**
	 * Removes the component at the specified index, if any.
	 * 
	 * @param listIndex the index of the vertical list to remove from
	 * @param resize whether the list should be checked for a resizing after the deletion
	 * @return whether the component was successfully removed
	 */
	public boolean removeComponent(int listIndex, boolean resize) {
		return grid.removeComponent(0, listIndex, resize);
	}
	
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
