/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2014
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

import javax.vecmath.Tuple3d;

/**
   interface represents unstructured set of 3D points 
 */
public interface PointSet {
    /**
       return point count
     */
    public int size();

    /**
       return coord of a point at given index 
     */
    public void getPoint(int index, Tuple3d point);

    /**
     * Add a point to the set.
     *
     * @param x
     * @param y
     * @param z
     */
    public void addPoint(double x, double y, double z);

    /**
     * Clear all point and triangle data.
     */
    public void clear();
}