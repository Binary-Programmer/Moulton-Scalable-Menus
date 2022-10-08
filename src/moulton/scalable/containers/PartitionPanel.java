package moulton.scalable.containers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import moulton.scalable.utils.MenuComponent;
import moulton.scalable.utils.MenuSolver.Expression;

/**
 * PartitionPanel is a type of {@link Panel} with up to two partitions dividing up the space into
 * four sectors: left, right, bottom, and top. Components need to be added to their sector manually
 * using {@link #setLeft(MenuComponent)}, {@link #setRight(MenuComponent)}, {@link
 * #setTop(MenuComponent)}, and {@link #setBottom(MenuComponent)}. The partitions can be set using
 * {@link #setVerticalPartition(String)} and {@link #setHorizontalPartition(String)}.
 * @author Matthew Moulton
 */
public class PartitionPanel extends Panel {
	/**The menu component drawn in the specified sector. There are four sectors that this panel
	 * will draw: left, right, bottom, and top. The left and right sectors are divided by the
	 * {@link #verticalPartition} and the top and bottom sectors by the
	 * {@link #horizontalPartition}.
	 * @see #setLeft(MenuComponent)
	 * @see #setRight(MenuComponent)
	 * @see #setTop(MenuComponent)
	 * @see #setBottom(MenuComponent)*/
	protected MenuComponent left, right, bottom, top;
	/**The x-value divider between the {@link #left} and {@link #right} components.
	 * @see #setVerticalPartition(String)*/
	protected Expression verticalPartition;
	/**The y-value divider between the {@link #top} and {@link #bottom} components.
	 * @see #setHorizontalPartition(String)*/
	protected Expression horizontalPartition;
	/**Determines how the corner is filled. If this is null and the horizontal component is set but
	 * the vertical component is not, then the horizontal component will take the corner as well.
	 * If both horizontal and vertical components are set but this is not, then the corner will be
	 * empty. If both the horizontal and vertical components are set, and this is not null, it will
	 * determine which component takes the corner (true is horizontal, false is vertical). Finally,
	 * if the horizontal component is set, the vertical component is not, but this is set to true,
	 * then the corner will be empty.
	 * @see #setTopLeftCorner(Boolean)
	 * @see #setTopRightCorner(Boolean)
	 * @see #setBottomLeftCorner(Boolean)
	 * @see #setBottomRightCorner(Boolean)*/
	protected Boolean topLeftCorner, topRightCorner, bottomLeftCorner, bottomRightCorner;
	
	/**Creates a partition panel in free form in the parent panel.
	 * @param parent the parent panel to be rendered on
	 * @param x the x-position as a string expression
	 * @param y the y-position as a string expression
	 * @param width the width of this panel as a string expression
	 * @param height the height of this panel as a string expression
	 * @param color this panel's color*/
	public PartitionPanel(Panel parent, String x, String y, String width, String height,
			Color color) {
		super(parent, x, y, width, height, color);
	}
	/**Creates a partition panel in grid form in the parent panel.
	 * @param parent the parent panel whose grid this will be rendered on
	 * @param x the x-position in the parent grid
	 * @param y the y-position in the parent grid
	 * @param color this panel's color*/
	public PartitionPanel(Panel parent, int x, int y, Color color) {
		super(parent, x, y, color);
	}
	
	/**This method is overridden and only returns false because PartitionPanel requires components
	 * to be added in a very specific way. Each component held must be in the top, bottom, left, or
	 * right sector.
	 * @see #setLeft(MenuComponent)
	 * @see #setRight(MenuComponent)
	 * @see #setTop(MenuComponent)
	 * @see #setBottom(MenuComponent)*/
	@Override
	public boolean addToGrid(MenuComponent comp, int x, int y) {
		return false;
	}
	/**This method is overridden and returns false because PartitionPanel cannot directly remove a
	 * component from the grid. Each component held is in one of four sectors: top, bottom, left,
	 * or right.
	 * @see #setLeft(MenuComponent)
	 * @see #setRight(MenuComponent)
	 * @see #setTop(MenuComponent)
	 * @see #setBottom(MenuComponent)*/
	@Override
	public boolean removeFromGrid(int x, int y, boolean resize) {
		return false;
	}
	/**This method is overridden and only returns false because PartitionPanel requires components
	 * to be added in a very specific way. Each component held must be in the top, bottom, left, or
	 * right sector.
	 * @see #setLeft(MenuComponent)
	 * @see #setRight(MenuComponent)
	 * @see #setTop(MenuComponent)
	 * @see #setBottom(MenuComponent)*/
	@Override
	public boolean addFreeComponent(MenuComponent comp) {
		return false;
	}
	
	/**Sets the component in the left sector.
	 * @param comp {@link #left}*/
	public void setLeft(MenuComponent comp) {
		this.left = comp;
	}
	/**Sets the component in the right sector.
	 * @param comp {@link #right}*/
	public void setRight(MenuComponent comp) {
		this.right = comp;
	}
	/**Sets the component in the top sector.
	 * @param comp {@link #top}*/
	public void setTop(MenuComponent comp) {
		this.top = comp;
	}
	/**Sets the component in the bottom sector.
	 * @param comp {@link #bottom}*/
	public void setBottom(MenuComponent comp) {
		this.bottom = comp;
	}
	
	/**Sets how the top left corner should be drawn.
	 * @param topLeft {@link #topLeftCorner}*/
	public void setTopLeftCorner(Boolean topLeft) {
		this.topLeftCorner = topLeft;
	}
	/**Sets how the top right corner should be drawn.
	 * @param topRight {@link #topRightCorner}*/
	public void setTopRightCorner(Boolean topRight) {
		this.topRightCorner = topRight;
	}
	/**Sets how the bottom left corner should be drawn.
	 * @param botLeft {@link #bottomLeftCorner}*/
	public void setBottomLeftCorner(Boolean botLeft) {
		this.bottomLeftCorner = botLeft;
	}
	/**Sets how the bottom right corner should be drawn.
	 * @param botRight {@link #bottomRightCorner}*/
	public void setBottomRightCorner(Boolean botRight) {
		this.bottomRightCorner = botRight;
	}
	
	/**Sets the horizontal partition's y-value for the two vertical sectors, top and bottom. If the
	 * horizontal partition has already been set, the partition is now moved.
	 * @param expression replaces {@link #horizontalPartition}*/
	public void setHorizontalPartition(String expression) {
		this.horizontalPartition = solve.parse(expression, true, false);
	}
	/**Sets the vertical partition's x-value for the two horizontal sectors, left and right. If the
	 * vertical partition has already been set, it is now moved.
	 * @param expression replaces {@link #verticalPartition}*/
	public void setVerticalPartition(String expression) {
		this.verticalPartition = solve.parse(expression, true, false);
	}
	
	/**Returns the expression which determines the x-value of the horizontal partition.
	 * @return the expression of {@link #horizontalPartition}*/
	public Expression getHorizontalPartition() {
		return horizontalPartition;
	}
	/**Returns the expression which determines the y-value of the vertical partition.
	 * @return the expression of {@link #verticalPartition}*/
	public Expression getVerticalPartition() {
		return verticalPartition;
	}
	
	@Override
	public ArrayList<MenuComponent> getAllHeldComponents() {
		ArrayList<MenuComponent> list = new ArrayList<MenuComponent>(4);
		list.add(left);
		list.add(right);
		list.add(top);
		list.add(bottom);
		return list;
	}
	
	@Override
	public void render(Graphics g, int xx, int yy, int ww, int hh) {
		Rectangle rect = this.getRenderRect(xx, yy, ww, hh, width, height);
		int x = rect.x;
		int y = rect.y;
		int w = rect.width;
		int h = rect.height;
		lastHeight = h;

		// draw color
		if(color!=null){
			g.setColor(color);
			g.fillRect(x, y, w, h);
			g.setColor(Color.BLACK);
			if (outline)
				g.drawRect(x, y, w - 1, h - 1);
		}
		
		int vertPartition = w/2, horizPartition = h/2;
		if(verticalPartition != null)
			vertPartition = solve.eval(this.verticalPartition);
		if(horizontalPartition != null)
			horizPartition = solve.eval(this.horizontalPartition);
		
		try{
			if(left != null && left.isVisible()) {
				int sectorTop = horizPartition;
				int sectorBot = horizPartition;
				if((topLeftCorner==null && top==null) || topLeftCorner.booleanValue())
					sectorTop = y;
				if((bottomLeftCorner==null && bottom==null) || bottomLeftCorner.booleanValue())
					sectorBot = y+h;
				left.render(g, 0, sectorTop, vertPartition, sectorBot-sectorTop);
			}if(right != null && right.isVisible()) {
				int sectorTop = horizPartition;
				int sectorBot = horizPartition;
				if((topRightCorner==null && top==null) || topRightCorner.booleanValue())
					sectorTop = y;
				if((bottomRightCorner==null && bottom==null) || bottomRightCorner.booleanValue())
					sectorBot = y+h;
				right.render(g, vertPartition, sectorTop, w-vertPartition, sectorBot-sectorTop);
			}if(top != null && top.isVisible()) {
				int sectorLft = vertPartition;
				int sectorRgt = vertPartition;
				if((topLeftCorner==null && left==null) || !topLeftCorner.booleanValue())
					sectorLft = x;
				if((topRightCorner==null && right==null) || !topRightCorner.booleanValue())
					sectorRgt = x+w;
				top.render(g, sectorLft, 0, sectorRgt-sectorLft, horizPartition);
			}if(bottom != null && bottom.isVisible()) {
				int sectorLft = vertPartition;
				int sectorRgt = vertPartition;
				if((bottomLeftCorner==null && left==null) || !bottomLeftCorner.booleanValue())
					sectorLft = x;
				if((bottomRightCorner==null && right==null) || !bottomRightCorner.booleanValue())
					sectorRgt = x+w;
				bottom.render(g, sectorLft, horizPartition, sectorRgt-sectorLft, h-horizPartition);
			}
		}catch(ConcurrentModificationException cme){
			System.err.println("There was a concurrent access of the components in the panel.");
		}
	}

}