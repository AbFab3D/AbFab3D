/** 
 *                        Shapeways, Inc Copyright (c) 2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/


package abfab3d.geom;

import javax.vecmath.Vector3d;


import static abfab3d.core.MathUtil.clamp;

/**
   utility class to calculate intersection of single  triangle and  a plane 
   @author Vladimir Bulatov
 */
public class TriangleSlicer {

    public static final int INSIDE = 0, OUTSIDE = 1, INTERSECT = 2, ERROR = -1;
    private double epsilon = 1.e-10;

    public TriangleSlicer(){
        
    }



    /**
       return intersection of triangle and a plane 
       plane is represented by distance of trinagle vertices to the plane
       the negative distance means the vertex is inside of halfspace defined by the plane 
       positive distance means the vertex is outside 
       if distance value is 0.0 (within tolerance epsilon) 
       the distance shifted down to make point 
       

       @param p0 triangle vertex
       @param p1 triangle vertex
       @param p2 triangle vertex
       @param d0 distance from vertex v0 to the plane 
       @param d1 distance from vertex v1 to the plane 
       @param d2 distance from vertex v2 to the plane 

       @param q0 on return contains starting point of intersection 
       @param q1 on return contains ending point of intersection 

       @return INSIDE if all points are inside (no intersection) 
       @return INTERSECT if there is interseciton 
       @return OUTSIDE if all ponts are outside 
       @return ERROR if failed

       

     */
    int getIntersection(Vector3d p0, Vector3d p1, Vector3d p2,
                        double d0, double d1, double d2, Vector3d q0, Vector3d q1){
        int index = 0;
        if(d0 > 0. )index |= 1;
        if(d1 > 0. )index |= 2;
        if(d2 > 0. )index |= 4;

        switch(index){
        default:
            // should not happens 
            return ERROR;
        case 0:
            return INSIDE;
        case 1:
            q0.interpolate(p2,p0, alpha(d2,d0));
            q1.interpolate(p1,p0, alpha(d1,d0));
            return INTERSECT;
        case 2:
            q0.interpolate(p0,p1, alpha(d0,d1));
            q1.interpolate(p2,p1, alpha(d2,d1));
            return INTERSECT;
        case 3:
            q0.interpolate(p2,p0, alpha(d2,d0));
            q1.interpolate(p2,p1, alpha(d2,d1));
            return INTERSECT;
        case 4:
            q0.interpolate(p1,p2, alpha(d1,d2));
            q1.interpolate(p0,p2, alpha(d0,d2));
            return INTERSECT;
        case 5:
            q0.interpolate(p1,p2, alpha(d1,d2));
            q1.interpolate(p1,p0, alpha(d1,d0));
            return INTERSECT;
        case 6:
            q0.interpolate(p0,p1, alpha(d0,d1));
            q1.interpolate(p0,p2, alpha(d0,d2));
            return INTERSECT;
        case 7: 
            return OUTSIDE;
        }        

    }


    /**
       combine interpolation coeff and clamping
     */
    static final double alpha(double d1,double d2) {
        return clamp(d1/(d1-d2), 0., 1.);
    }
    
} // class TriangleSlicer 
