package moulton.scalable.utils;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;

/**
 * GridFormatter will hold MenuComponents in a grid that can have variable margins, frames, and weights, then
 * will give the coordinate specifications for each component through {@link #findCompCoordinates(MenuComponent, int[])}.
 * @author Matthew Moulton
 */
public class GridFormatter {
	/**The components that are held at the location they are in the grid. Technically, the maximum x and y are
	 * already stored in the map, but for speed, {@link #gridDim} will keep track of that when components are
	 * added or removed.
	 * @see #addComponent(MenuComponent, int, int)
	 * @see #removeComponent(int, int, boolean)
	 * @see #getHeldComponents()*/
	protected HashMap<Point, MenuComponent> gridComponents = new HashMap<Point, MenuComponent>();
	/**The maximum x and y values of the child components of the grid. This is kept track of as components
	 * are added to the grid. At run-time, the grid is split evenly into that many pieces for the x and y axes.*/
	protected Dimension gridDim = new Dimension(0,0);
	/**The width of the x margin. This margin will separate all elements in the x-plane. Defaults to null.
	 * @see #setMargin(String, String)
	 * @see #yMargin*/
	protected String xMargin = null;
	/**The height of the y margin. This margin will separate all elements in the y-plane. Defaults to null.
	 * @see #setMargin(String, String)
	 * @see #xMargin*/
	protected String yMargin = null;
	/**The width of the outside border. Defaults to null.
	 * @see #setFrame(String, String)
	 * @see #yFrame*/
	protected String xFrame = null;
	/**The height of the outside border. Defaults to null.
	 * @see #setFrame(String, String)
	 * @see #xFrame*/
	protected String yFrame = null;
	/**Holds the values of unique row weights. At default, this map will be empty, and all shown rows will
	 * have an implied weight of 1. However, row weights can be specified otherwise with {@link #specifyRowWeight(int, double)}
	 * and they will be saved here. A row with a weight double to another row's weight will have double the
	 * width of the latter's row.*/
	protected HashMap<Integer, Double> rowWeights = new HashMap<Integer, Double>();
	/**Holds the values of unique column weights. At default, this map will be empty, and all shown columns will
	 * have an implied weight of 1. However, column weights can be specified otherwise with 
	 * {@link #specifyColumnWeight(int, double)} and they will be saved here. A column with a weight double to
	 * another column's weight will have double the height of the latter's column.*/
	protected HashMap<Integer, Double> colWeights = new HashMap<Integer, Double>();
	
	public void addComponent(MenuComponent comp, int x, int y) {
		if (x >= gridDim.getWidth())
			gridDim.setSize(x+1, gridDim.getHeight());
		if (y >= gridDim.getHeight())
			gridDim.setSize(gridDim.getWidth(), y+1);
		gridComponents.put(new Point(x, y), comp);
	}
	
	/**
	 * Returns the components that are held.
	 * @return more formally, returns the components in {@link #gridComponents}
	 */
	public Collection<MenuComponent> getHeldComponents(){
		return gridComponents.values();
	}
	
	/**
	 * Sets the {@link #xMargin} and {@link #yMargin} for this panel. The margins will
	 * be used to separate components in the grid. Thus the number of marginal dimensions
	 * for the width of a panel is (number of x components)-1, where the number of x 
	 * components is at least one. A null value indicates no margin.
	 * @param xMargin the width of the margin on the x-axis
	 * @param yMargin the height of the margin on the y-axis
	 */
	public void setMargin(String xMargin, String yMargin) {
		this.xMargin = xMargin;
		this.yMargin = yMargin;
	}
	
	/**
	 * Sets the {@link #xFrame} and {@link #yFrame} for this panel. Unlike margins, the frame
	 * will only be on the outside of the panel, not between individual components. A null
	 * value indicates no frame.
	 * @param xFrame the algebraic expression for the width of the frame
	 * @param yFrame the algebraic expression for the height of the frame.
	 */
	public void setFrame(String xFrame, String yFrame){
		this.xFrame = xFrame;
		this.yFrame = yFrame;
	}
	
	/**Deletes the component found at the location (x,y) in {@link #grid}.
	 * @param x the x-value of the component to remove
	 * @param y the y-value of the component to remove
	 * @param resize whether the grid should check for a resize after the deletion.
	 * @return whether a component was removed at (x,y)
	 */
	public boolean removeComponent(int x, int y, boolean resize) {
		if(resize && gridDim.width>x && gridDim.height>y) {
			int maxX=0, maxY=0;
			boolean resized = true;
			for(Point p:gridComponents.keySet()) {
				if(p.x > maxX) maxX = p.x;
				if(p.y > maxY) maxY = p.y;
				//if components are found to have higher xs and ys, then the deletion of this object was in the middle
				if(maxX>x && maxY>y) { 
					resized = false;
					break;
				}
			}
			if(resized) {
				gridDim.width = maxX;
				gridDim.height = maxY;
			}
		}
		return gridComponents.remove(new Point(x,y)) != null;
	}
	
	/**
	 * Finds the specified component in the grid and returns its pixel coordinates. If it cannot be found,
	 * null is returned.
	 * @param comp the component to look for in the grid
	 * @param self the location and dimension of the container component in the render. Ordered as x, y, width, and height.
	 * @return the pixel coordinates for the specified component to be rendered. Ordered as x, y, width, and height.
	 */
	public int[] findCompCoordinates(MenuComponent comp, int[] self) {
		Point gridPoint = comp.getGridLocation();
		//The component must be in a grid for the following calculations to work!
		if(gridPoint!=null){
			int details[] = {0, 0, 0, 0};
			//search through grid for this component
			if (gridComponents.get(gridPoint) != comp) // not found!
				return details;
			//found @ gridPoint
			
			//find children components from self
			int wholeWidth = self[2];
			int wholeHeight = self[3];
			ExpressionSolver solver = new ExpressionSolver(wholeWidth, wholeHeight);
			//frame
			if(xFrame!=null){
				int frame = (int)solver.solveString(xFrame);
				self[2] -= frame*2;
				if(self[2]<0)
					self[2] = 0;
				else
					self[0] += frame;
			}
			if(yFrame!=null){
				int frame = (int)solver.solveString(yFrame);
				self[3] -= frame*2;
				if(self[3]<0)
					self[3] = 0;
				else
					self[1] += frame;
			}
			//margins
			int marginSize = 0;
			if(xMargin!=null) {
				marginSize = (int)solver.solveString(xMargin);
				if(marginSize<0 || self[2]<1)
					marginSize = 0;
			}
			double totalWeight = findXWeights(gridDim.width);
			details[0] = self[0] + (int)((self[2]*findXWeights(gridPoint.x))/totalWeight *
					identityDiv(marginSize*gridPoint.x, marginSize*(gridDim.width-1)));
			int endPoint = self[0] + (int)((self[2]*findXWeights(gridPoint.x+1))/totalWeight *
					identityDiv(marginSize*(gridPoint.x+1), marginSize*(gridDim.width-1)));
			details[2] = endPoint - details[0] - marginSize;
			
			marginSize = 0;
			if(yMargin!=null) {
				marginSize = (int)solver.solveString(yMargin);
				if(marginSize<0 || self[3]<1)
					marginSize = 0;
			}
			totalWeight = findYWeights(gridDim.height);
			details[1] = self[1] + (int)((self[3]*findXWeights(gridPoint.y))/totalWeight *
					identityDiv(marginSize*gridPoint.y, marginSize*(gridDim.height-1)));
			endPoint = self[1] + (int)((self[3]*findXWeights(gridPoint.y+1))/totalWeight *
					identityDiv(marginSize*(gridPoint.y+1), marginSize*(gridDim.height-1)));
			details[3] = endPoint - details[1] - marginSize;
			return details;
		}
		return null;
	}
	
	/**Computes division, but gives priority to identity division, that being: any number divided by itself
	 * is 1. Even 0/0 will be computed as 1.
	 * @param numerator the numerator
	 * @param denominator the denominator
	 * @return the result of the division*/
	private double identityDiv(double numerator, double denominator) {
		if(numerator == denominator)
			return 1.0;
		else
			return numerator/denominator;
	}
	
	/**Defines the weight for the given row. A weight of 1 is default and will delete the entry in {@link #rowWeights}
	 * @param row the row to set the weight for
	 * @param weight the weight value to set*/
	public void specifyRowWeight(int row, double weight) {
		//if the row weight will be default, remove it
		if(weight==1)
			rowWeights.remove(row);
		else
			rowWeights.put(row, weight);
	}
	/**Defines the weight for the given column. A weight of 1 is default and will delete the entry in
	 * {@link #colWeights}
	 * @param col the column to set the weight for
	 * @param weight the weight value to set*/
	public void specifyColumnWeight(int col, double weight) {
		//if the col weight will be default, remove it
		if(weight==1)
			colWeights.remove(col);
		else
			colWeights.put(col, weight);
	}
	
	/**Calculates the total weight of the rows until maxX using saved values in {@link #rowWeights}.
	 * @param maxX the limit where the weight totaling should stop. Using {@link #gridDim}.width will yield
	 * a complete total.
	 * @return the total of all row weights*/
	protected double findXWeights(int maxX) {
		double runningTotal = 0;
		for(int i=0; i<maxX; i++) {
			if(rowWeights.containsKey(i)) {
				runningTotal += rowWeights.get(i);
			}else //add the default of 1 to the total
				runningTotal++;
		}
		return runningTotal;
	}
	/**Calculates the total weight of the columns until maxY using saved values in {@link #colWeights}.
	 * @param maxY the limit where the weight totaling should stop. Using {@link #gridDim}.height will yield
	 * a complete total.
	 * @return the total of all column weights*/
	protected double findYWeights(int maxY) {
		double runningTotal = 0;
		for(int i=0; i<maxY; i++) {
			if(colWeights.containsKey(i)) {
				runningTotal += colWeights.get(i);
			}else //add the default of 1 to the total
				runningTotal++;
		}
		return runningTotal;
	}

}