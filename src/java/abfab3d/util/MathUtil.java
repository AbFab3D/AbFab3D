/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.util;

// External Imports

// Internal Imports

/**
 * Math utilities.
 *
 * @author Tony Wong
 */
public class MathUtil {

    /**
     * Calculate distance between two points in Euclidian space.
     * 
     * @param pos1 The first position as an int array of length 3
     * @param pos2 The second position as an int array of length 3
     * @return The distance as a double
     */
    public static double getDistance(int[] pos1, int[] pos2) {
    	int xDistance = pos2[0] - pos1[0];
    	int yDistance = pos2[1] - pos1[1];
    	int zDistance = pos2[2] - pos1[2];
    	
    	double distance = Math.sqrt(Math.pow(xDistance, 2) + 
    			                    Math.pow(yDistance, 2) + 
    			                    Math.pow(zDistance, 2));
    	
    	return distance;
    }

    /**
     * Calculate distance between two points in Euclidian space.
     * 
     * @param pos1 The first position as an double array of length 3
     * @param pos2 The second position as an double array of length 3
     * @return The distance as a double
     */
    public static double getDistance(double[] pos1, double[] pos2) {
    	double xDistance = pos2[0] - pos1[0];
    	double yDistance = pos2[1] - pos1[1];
    	double zDistance = pos2[2] - pos1[2];
    	
    	double distance = Math.sqrt(Math.pow(xDistance, 2) + 
    			                    Math.pow(yDistance, 2) + 
    			                    Math.pow(zDistance, 2));
    	
    	return distance;
    }
    
}