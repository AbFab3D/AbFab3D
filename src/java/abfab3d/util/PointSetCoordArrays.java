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
   
   represent point set as 3 arrays of coordinates 
   new points can't be added to the set 
   @author Vladimir Bulatov
 */
public class PointSetCoordArrays implements PointSet  {
    
    
    // points are represented via 3 coordinates 
    double coordx[] = null;
    double coordy[] = null;
    double coordz[] = null;

    int m_size=0; // points count 

    /**
       accept coordinates as flat array of double
     */
    public PointSetCoordArrays(double coordx[], double coordy[], double coordz[]){
        this.coordx = coordx;
        this.coordy = coordy;
        this.coordz = coordz;
        m_size = coordx.length;

    }
    
    /**
     * Clear all point and triangle data.
     */
    public void clear() {
        // ignore,         
    }

    
    public final void addPoint(double x, double y, double z){
        // ignore, 
    }

    /**
       interface PointSet 
     */
    public int size(){

        return m_size;

    }

    public void getPoint(int index, Tuple3d point){

        point.x = coordx[index];
        point.y = coordy[index];
        point.z = coordz[index];

    }
    
}
