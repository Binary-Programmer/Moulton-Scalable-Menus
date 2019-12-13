package moulton.scalable.geometrics;

import java.text.DecimalFormat;

import moulton.scalable.utils.MenuComponent;

/**
 * A class that provides static shape resources.
 * @author Matthew Moulton
 */
public abstract class ShapeResources {
	
	/**
	 * Generates an array of string x coordinates for a circle shape. These x-coordinates are expressions to be
	 * solved by {@link MenuComponent#solveString(String,int,int)} at run-time in normal rendering. The first coordinate
	 * will be the right of the circle, and following coordinates will be listed in a counter-clockwise direction.
	 * @param centerX the center x-position of the circle expression
	 * @param radius the radius of the circle expression
	 * @param precision indirectly determines the number of points the generated circle will have. Precision of 0 is
	 * a square. Precision of 1 is an octagon. Thus the number of points the generated circle will have is 4 + 4*precision
	 * @return the array of x coordinates as defined.
	 * @throws IllegalArgumentException if precision is not greater than 0.
	 */
	public static String[] generateCircleXCoords(String centerX, String radius, int precision) {
		if(precision<0)
			throw new IllegalArgumentException("Precision for circle generation must be greater than 0!");
		
		DecimalFormat df = new DecimalFormat("0.0#####");
		int points = 4 + 4*precision;
		String[] xCoords = new String[points];
		xCoords[0] = centerX +"+"+ radius +"/2";
		for(int i=1; i<points/2; i++) {
			String x = centerX +"+"+ radius + "*" + df.format(.5*Math.cos(2*Math.PI*((double)i/points)));
			xCoords[i] = xCoords[points-i] = x;
		}
		xCoords[points/2] = centerX +"-"+ radius +"/2";
		return xCoords;
	}
	/**
	 * Generates an array of string y coordinates for a circle shape. These y-coordinates are expressions to be
	 * solved by {@link MenuComponent#solveString(String,int,int)} at run-time in normal rendering. The first coordinate
	 * will be the right of the circle, and following coordinates will be listed in a counter-clockwise direction.
	 * @param centerY the center x-position of the circle expression
	 * @param radius the radius of the circle expression
	 * @param precision indirectly determines the number of points the generated circle will have. Precision of 0 is
	 * a square. Precision of 1 is an octagon. Thus the number of points the generated circle will have is 4 + 4*precision
	 * @return the array of x coordinates as defined.
	 * @throws IllegalArgumentException if precision is not greater than 0.
	 */
	public static String[] generateCircleYCoords(String centerY, String radius, int precision) {
		if(precision<0)
			throw new IllegalArgumentException("Precision for circle generation must be greater than 0!");
		
		DecimalFormat df = new DecimalFormat("#.0#####");
		int points = 4 + 4*precision;
		String[] yCoords = new String[points];
		yCoords[0] = yCoords[points/2] = centerY;
		for(int i=1; i<points/4; i++) {
			String y = centerY +"+"+ radius + "*" + df.format(.5*Math.sin(2*Math.PI*((double)i/points)));
			yCoords[i] = yCoords[points/2-i] = y;
		}
		yCoords[points/4] = centerY +"+"+ radius +"/2";
		yCoords[points - points/4] = centerY +"-"+ radius +"/2";
		for(int i=points/2+1; i<(3*points)/4; i++) {
			String y = centerY +"+"+ radius + "*" + df.format(.5*Math.sin(2*Math.PI*((double)i/points)));
			yCoords[i] = yCoords[points-(i-points/2)] = y;
		}
		return yCoords;
	}
	
	/**
	 * The X values of a "circle" with 32 points. The circle will be centered around "centerx" and will
	 * take up the whole width of its parent panel or cell.
	 */
	public static final String CIRCLE_XS[] ={
			"centerx",
			"centerx+width/10.3",
			"centerx+width/5.23",
			"centerx+width/3.6",
			"centerx+width/2.83",
			"centerx+width/2.41",
			"centerx+width/2.17",
			"centerx+width/2.04",
			"centerx+width/2",
			"centerx+width/2.04",
			"centerx+width/2.17",
			"centerx+width/2.41",
			"centerx+width/2.83",
			"centerx+width/3.6",
			"centerx+width/5.23",
			"centerx+width/10.3",
			"centerx",
			"centerx-width/10.3",
			"centerx-width/5.23",
			"centerx-width/3.6",
			"centerx-width/2.83",
			"centerx-width/2.41",
			"centerx-width/2.17",
			"centerx-width/2.04",
			"centerx-width/2",
			"centerx-width/2.04",
			"centerx-width/2.17",
			"centerx-width/2.41",
			"centerx-width/2.83",
			"centerx-width/3.6",
			"centerx-width/5.23",
			"centerx-width/10.3",
	};
	/**
	 * The Y values of a "circle" with 32 points. The circle will be centered around "centery" and will
	 * take up the whole height of its parent panel or cell.
	 */
	public static final String CIRCLE_YS[] = {
			"centery-height/2",
			"centery-height/2.04",
			"centery-height/2.17",
			"centery-height/2.41",
			"centery-height/2.83",
			"centery-height/3.6",
			"centery-height/5.23",
			"centery-height/10.3",
			"centery",
			"centery+height/10.3",
			"centery+height/5.23",
			"centery+height/3.6",
			"centery+height/2.83",
			"centery+height/2.41",
			"centery+height/2.17",
			"centery+height/2.04",
			"centery+height/2",
			"centery+height/2.04",
			"centery+height/2.17",
			"centery+height/2.41",
			"centery+height/2.83",
			"centery+height/3.6",
			"centery+height/5.23",
			"centery+height/10.3",
			"centery",
			"centery-height/10.3",
			"centery-height/5.23",
			"centery-height/3.6",
			"centery-height/2.83",
			"centery-height/2.41",
			"centery-height/2.17",
			"centery-height/2.04"
	};

	/**
	 * The x values for an isosceles triangle that has the two long legs pointing vertically. Can be paired
	 * with {@link #TRIANGLE_UP_YS} to point up, or {@link #TRIANGLE_DOWN_YS} to point down.
	 */
	public static final String TRIANGLE_VERT_XS[] = {
			"centerx-width/2",
			"centerx",
			"centerx+width/2"
	};
	/**The y values for an upward facing isosceles triangle. Paired with {@link #TRIANGLE_VERT_XS}.*/
	public static final String TRIANGLE_UP_YS[] = {
			"centery+height/2",
			"centery-height/2",
			"centery+height/2"
	};
	/**The y values for a downward facing isosceles triangle. Paired with {@link #TRIANGLE_VERT_XS}.*/
	public static final String TRIANGLE_DOWN_YS[] = {
			"centery-height/2",
			"centery+height/2",
			"centery-height/2"
	};

	/**The x values for a leftward facing isosceles triangle. Paired with {@link #TRIANGLE_HORIZ_YS}.*/
	public static final String TRIANGLE_LEFT_XS[] = {
			"centerx+width/2",
			"centerx-width/2",
			"centerx+width/2"
	};
	/**The x values for a rightward facing isosceles triangle. Paired with {@link #TRIANGLE_HORIZ_YS}.*/
	public static final String TRIANGLE_RIGHT_XS[] = {
			"centerx-width/2",
			"centerx+width/2",
			"centerx-width/2"
	};
	/**
	 * The y values for an isosceles triangle that has the two long legs pointing horizontally. Can be paired
	 * with {@link #TRIANGLE_RIGHT_XS} to point right, or {@link #TRIANGLE_LEFT_XS} to point left.
	 */
	public static final String TRIANGLE_HORIZ_YS[] = {
			"centery-height/2",
			"centery",
			"centery+height/2"
	};
}