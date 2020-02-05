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


import static abfab3d.core.MathUtil.str;
import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;

import static java.lang.Math.abs;

/**
   utility class to calculate intersection of single  triangle and  a plane 
   @author Vladimir Bulatov
 */
public class TriangleSlicer {

    public static final int INSIDE = 0, OUTSIDE = 1, INTERSECT = 2, ERROR = -1;
    static final boolean DEBUG = false;
    static final boolean PRINT_STAT = true;
    // epsilon to break degenerate cases
    private double epsilon = 0.;//1.e-8;
    private int caseCount[]= new int[8];

    public TriangleSlicer(){        
    }

    public TriangleSlicer(double tolerance){
        this.epsilon = tolerance;
    }



    /**
       return intersection of triangle and a plane 
       plane is represented by distance of trinagle vertices to the plane
       the negative distance means the vertex is inside of halfspace defined by the plane 
       positive distance means the vertex is outside 
       if distance value is 0.0 (within tolerance epsilon) 
       the distance is shifted up to make non degenerate point 
       

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
        //
        // shift values from zero in positive direction 
        //
        if(abs(d0) < this.epsilon) d0 = this.epsilon;
        if(abs(d1) < this.epsilon) d1 = this.epsilon;
        if(abs(d2) < this.epsilon) d2 = this.epsilon;
           
        int index = 0;
        if(d0 >= this.epsilon) index |= 1;
        if(d1 >= this.epsilon) index |= 2;
        if(d2 >= this.epsilon) index |= 4;
        if(this.debug){
            printf("d:[%12.10f %12.10f %12.10f]mm, index:%d\n", d0/MM, d1/MM, d2/MM, index);
        }
        if(DEBUG)printf("case: %d\n", index);
        if(PRINT_STAT)caseCount[index]++;
 
        switch(index){
        default:
            // should not happens 
            return ERROR;
        case 0:
            return INSIDE;
        case 7: 
            return OUTSIDE;
        case 1:
            interpolate(q1, p2,p0, alpha(d2,d0));
            interpolate(q0, p1,p0, alpha(d1,d0));
            break;
        case 2:
            interpolate(q1, p0,p1, alpha(d0,d1));
            interpolate(q0, p2,p1, alpha(d2,d1));
            break;
        case 3:
            interpolate(q1, p2,p0, alpha(d2,d0));
            interpolate(q0, p2,p1, alpha(d2,d1));
            break;
        case 4:
            interpolate(q1, p1,p2, alpha(d1,d2));
            interpolate(q0, p0,p2, alpha(d0,d2));
            break;
        case 5:
            interpolate(q1, p1,p2, alpha(d1,d2));
            interpolate(q0, p1,p0, alpha(d1,d0));
            break;
        case 6:
            interpolate(q1, p0,p1, alpha(d0,d1));
            interpolate(q0, p0,p2, alpha(d0,d2));
            break;
        }        
        return INTERSECT;

    }

    void interpolate(Vector3d q, Vector3d p1, Vector3d p2, double t){
        
        q.interpolate(p1, p2, t);
        if(this.debug) {
            String f = "%10.6f";
            printf("  interpolate(%s,%s, %s) -> %s\n", str(f, p1,MM),str(f, p2,MM),fmt(f, t), str(f, q, MM));
        }

    }

    boolean debug = false;

    public void setDebug(boolean value){
        this.debug = value;
    }


    /**
       combine interpolation coeff and clamping
     */
    static final double alpha(double d1,double d2) {
        return clamp(d1/(d1-d2), 0., 1.);
    }


    public static void getTriangleNormal(Vector3d p0, Vector3d p1, Vector3d p2, Vector3d normal){

        double x1 = p1.x - p0.x;
        double y1 = p1.y - p0.y;
        double z1 = p1.z - p0.z;

        double x2 = p2.x - p0.x;
        double y2 = p2.y - p0.y;
        double z2 = p2.z - p0.z;
                                                
        normal.set(y1*z2-z1*y2, z1*x2 - x1*z2, x1*y2-y1*x2);
        normal.normalize();
    }

    public static void getIntersectionDirection(Vector3d planeNormal, Vector3d triangleNormal, Vector3d direction){
        direction.cross(planeNormal, triangleNormal);
    }

    void printStat(){
        printf("TriangleSlicer case statistics\n");
        printf("cases\n");        
        for(int i = 0; i < caseCount.length; i++){
            printf("case: %d, count:%d\n", i, caseCount[i]);
        }
    }
    
} // class TriangleSlicer 
