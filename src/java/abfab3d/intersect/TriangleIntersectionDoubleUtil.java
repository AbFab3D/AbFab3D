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

package abfab3d.intersect;

// external imports
import java.lang.Math;
import javax.vecmath.Point3d;

// local imports
// none

/**
 * TriangleIntersectionUtil has a number of methods for determining
 * if a triangle intersects other objects.
 *
 * This class is a double version of TriangleIntersectionUtil.  Update
 * both classes when bugs are fixed.
 *
 * One method uses the separating axis theorem to determine
 * if a triangle defined in three-dimensional space intersects with a box.
 *
 * Another method determines triangle-cube intersections.
 *
 * Another method determines ray-triangle intersections.
 *
 * @author Alan Hudson
 */

public class TriangleIntersectionDoubleUtil{

    /** Resolution, smallest voxel size */
    private double resolution;

    /** double p0, p1, and p2 are placeholder doubles for the "axis" methods. */
    double p0, p1, p2;
    /** double v0x through v2z are the coordinates of the triangle's vertices. */
    double v0x, v0y, v0z, v1x, v1y, v1z, v2x, v2y, v2z;
    double min, max;
    double rad;
    double bHalfWidth, bHalfHeight, bHalfDepth;

    /** The last intersection point found by this utils class.  May be null! */
    Point3d lastIntersectionPoint;

	/** Small tolerance value to handle case of ray intersecting a triangle edge **/
	private final double tolerance = -0.000000001;
	
    /**
     * The constructor
     * @param rez Resolution ... this is old code,
     * I do not remember (nor can I remember ever knowing) why this is needed :/
     */
    TriangleIntersectionDoubleUtil(double rez){
        this.resolution = rez;
    }

    /**
     * The new and improved constructor needs no parameters.
     */
    public TriangleIntersectionDoubleUtil(){
        lastIntersectionPoint = new Point3d();
    }


    /**
     * dotProduct takes the coordinates of two vectors as input and returns a scalar
     * @param x1 x coordinate of the first vector
     * @param y1 y coordinate of the first vector
     * @param z1 z coordinate of the first vector
     * @param x2 x coordinate of the second vector
     * @param y2 y coordinate of the second vector
     * @param z2 z coordinate of the second vector
     * @return (x1*x2 + y1*y2 + z1*z2)
     */
    double dotProduct(double x1, double y1, double z1,
                     double x2, double y2, double z2){
        return (x1*x2 + y1*y2 + z1*z2);
    }


    /**
    * set the max and min of three input coordinates.
    * Useful if you want to find the maximum absolute value of
    * one of the coordinates.
    */
    void findMinMax(double x0, double x1, double x2){
        min = x0;
        max = x0;
        if(x1<min) min=x1;
        if(x1>max) max=x1;
        if(x2<min) min=x2;
        if(x2>max) max=x2;
    }


    /**
    * triBoxOverlap tests if a triangle intersects an axis-aligned box.
    * (see Real Time Collision Detection, page 169 [by Christer Ericson])
    *
    * It uses a separating-axis approach.  There are 12 axes that
    * must be considered for projection:
    *
    * 1. Three face normals from the AABB (Axis-aligned bounding box)
    * 2. One face normal from the triangle
    * 3. Nine axes given by the cross product of combination of edges from both
    *
    * @return FALSE As soon as a separating axis is found the test can
    * immediately exit with a 'no intersection' result.
    * @return TRUE If all axes are tested and no separating
    * axis is found, the box and the triangle must be intersecting.
    *
    * It has been suggested that the most efficient order in which
    * to perform these three sets of tests is 3-1-2.
    *
    *
    * @param bx x-coordinate of the center of the box
    * @param by y-coordinate of the center of the box
    * @param bz z-coordinate of the center of the box
    *
    * @param boxHalfWidth half the width of the box
    * @param boxHalfHeight half the height of the box
    * @param boxHalfDepth half the depth of the box
    *
    * @param tri[] indices 0 through 2 contain the x,y, and z coordinates of vertex A
    * of the triangle.  Indices 3 through 5 contain the x,y, and z coordinates of vertex B
    * of the triangle.  Indices 6 through 8 contain the x,y, and z coordinates of vertex C
    * of the triangle.
    */
    boolean triBoxOverlap(double bx,
                          double by,
                          double bz,
                          double boxHalfWidth,
                          double boxHalfHeight,
                          double boxHalfDepth,
                          float[] tri){

        // this is obviously wasteful.
        // I've done it only to improve readability
        double triAx = tri[0];
        double triAy = tri[1];
        double triAz = tri[2];
        double triBx = tri[3];
        double triBy = tri[4];
        double triBz = tri[5];
        double triCx = tri[6];
        double triCy = tri[7];
        double triCz = tri[8];

        bHalfWidth = boxHalfWidth;
        bHalfHeight = boxHalfHeight;
        bHalfDepth = boxHalfDepth;


        // first, adjust everything so that the center of the box is at the
        // origin (0, 0, 0).  The "v's" are the coordinates of the adjusted triangle
        // ie: v0x is the x-coordinate of vertex 0 of the adjusted triangle,
        // whereas v1y is the y-coordinate of vertext 1 of the adjusted triangle
        v0x = triAx - bx;
        v0y = triAy - by;
        v0z = triAz - bz;
        v1x = triBx - bx;
        v1y = triBy - by;
        v1z = triBz - bz;
        v2x = triCx - bx;
        v2y = triCy - by;
        v2z = triCz - bz;
        /*

        System.out.println("Here are the coordinates of the adjusted triangle:");
        System.out.println("A(x,y,z) = (" + v0x + ", " + v0y + ", " + v0z + ")");
        System.out.println("B(x,y,z) = (" + v1x + ", " + v1y + ", " + v1z + ")");
        System.out.println("C(x,y,z) = (" + v2x + ", " + v2y + ", " + v2z + ")");

        */
        // compute the triangle edges
        double e0x = v1x - v0x;
        double e0y = v1y - v0y;
        double e0z = v1z - v0z;
        double e1x = v2x - v1x;
        double e1y = v2y - v1y;
        double e1z = v2z - v1z;
        double e2x = v0x - v2x;
        double e2y = v0y - v2y;
        double e2z = v0z - v2z;

        //3:test the nine axes given by the cross product of combination of edges

        double fex = Math.abs(e0x);
        double fey = Math.abs(e0y);
        double fez = Math.abs(e0z);

        if(!axisTest_X01(e0z, e0y, fez, fey)) return false;
        if(!axisTest_Y02(e0z, e0x, fez, fex)) return false;
        if(!axisTest_Z12(e0y, e0x, fey, fex)) return false;

        fex = Math.abs(e1x);
        fey = Math.abs(e1y);
        fez = Math.abs(e1z);

        if(!axisTest_X01(e1z, e1y, fez, fey)) return false;
        if(!axisTest_Y02(e1z, e1x, fez, fex)) return false;
        if(!axisTest_Z0(e1y, e1x, fey, fex)) return false;

        fex = Math.abs(e2x);
        fey = Math.abs(e2y);
        fez = Math.abs(e2z);

        if(!axisTest_X2(e2z, e2y, fez, fey)) return false;
        if(!axisTest_Y1(e2z, e2x, fez, fex)) return false;
        if(!axisTest_Z12(e2y, e2x, fey, fex)) return false;

        /*
        * 1: test the overlap in the three face normals from the AABB
        *       (the {x, y, z} directions)
        * Find min, max of the triangle in each direction, and test for overlap in
        * that direction - this is equivalent to testing a minimal AABB around the
        * triangle against the AABB.
        */

        // test in the x-direction
        findMinMax(v0x, v1x, v2x);
        if(min>bHalfWidth || max<-bHalfWidth) return false;

        // test in the y-direction
        findMinMax(v0y, v1y, v2y);
        if(min>bHalfHeight || max<-bHalfHeight) return false;

        // test in the z-direction
        findMinMax(v0z, v1z, v2z);
        if(min>bHalfDepth || max<-bHalfDepth) return false;

        /*
        * 2: test the overlap of the one normal of the triangle
        * We compute the plane equation of the triangle: normal*x+d=0 and see
        * if the box intersects the plane of the triangle.
        */
        double triNormalX = e1y*e2z - e1z*e2y;
        double triNormalY = e1z*e2x - e1x*e2z;
        double triNormalZ = e1x*e2y - e1y*e2x;

        if(!planeBoxOverlap(triNormalX, triNormalY, triNormalZ, v0x, v0y, v0z)) return false;
        else return true;  // box and triangle overlap
    }


    /**
     *
     * Used to test if a box intersects the plane of the triangle.
     * Works by taking the normal of the triangle and doing some dotProduct maths.
     *
     * @param normalX
     * @param normalY
     * @param normalZ
     * @param vertX
     * @param vertY
     * @param vertZ
     * @return
     */
    boolean planeBoxOverlap(double normalX, double normalY, double normalZ,
        double vertX, double vertY, double vertZ){

        double vMinX, vMinY, vMinZ, vMaxX, vMaxY, vMaxZ;

        if(normalX > 0.0f){
            vMinX= -bHalfWidth - vertX;
            vMaxX = bHalfWidth - vertX;
        }else {
            vMinX = bHalfWidth - vertX;
            vMaxX = -bHalfWidth - vertX;
        }

        if(normalY > 0.0f){
            vMinY = -bHalfHeight - vertY;
            vMaxY = bHalfHeight - vertY;
        } else {
            vMinY = bHalfHeight - vertY;
            vMaxY = -bHalfHeight - vertY;
        }

        if(normalZ > 0.0f){
            vMinZ = -bHalfDepth - vertZ;
            vMaxZ = bHalfDepth - vertZ;
        } else {
            vMinZ = bHalfDepth - vertZ;
            vMaxZ = -bHalfDepth - vertZ;
        }
        if(dotProduct(normalX, normalY, normalZ, vMinX, vMinY, vMinZ) > 0.0f) return false;
        if(dotProduct(normalX, normalY, normalZ, vMaxX, vMaxY, vMaxZ) >=0.0f) return true;

        return false;
        }
    /*======================== X-tests ========================*/
    boolean axisTest_X01(double a, double b, double fa, double fb){
        p0 = a*v0y - b*v0z;
        p2 = a*v2y - b*v2z;
        if(p0<p2) {
            min = p0;
            max= p2;
        } else{
            min=p2;
            max=p0;
        }
        rad = fa * bHalfHeight + fb*bHalfDepth;
        if(min>rad || max < -rad) return false;
        else return true;
    }
    boolean axisTest_X2(double a, double b, double fa, double fb){
        p0 = a*v0y - b*v0z;
        p1 = a*v1y - b*v1z;
        if(p0<p1) {
            min = p0;
            max = p1;
        } else {
            min=p1;
            max=p0;
        }
        rad = fa* bHalfHeight + fb*bHalfDepth;
        if(min>rad || max < -rad) return false;
        else return true;
    }
    /*======================== Y-tests ========================*/
    boolean axisTest_Y02(double a, double b, double fa, double fb){
        p0 = -a*v0x + b*v0z;
        p2 = -a*v2x + b*v2z;
        if(p0<p2) {
            min=p0;
            max=p2;
        } else{
            min=p2;
            max=p0;
        }
        rad = fa*bHalfWidth + fb*bHalfDepth;
        if(min>rad || max<-rad) return false;
        else return true;
    }
    boolean axisTest_Y1(double a, double b, double fa, double fb){
        p0 = -a*v0x + b*v0z;
        p1 = -a*v1x + b*v1z;
        if(p0<p1){
            min=p0;
            max=p1;
        }else {
            min=p1;
            max = p0;
        }
        rad = fa* bHalfWidth + fb*bHalfDepth;

        if(min>rad || max <-rad) return false;
        else return true;
    }
    /*======================== Z-tests ========================*/
    boolean axisTest_Z12(double a, double b, double fa, double fb){
        p1 = a*v1x - b*v1y;
        p2 = a*v2x - b*v2y;
        if(p2<p1){
            min=p2;
            max=p1;
        }else{
            min=p1;
            max=p2;
        }
        rad= fa*bHalfWidth + fb*bHalfHeight;
        if(min>rad || max <-rad) return false;
        else return true;
    }
    boolean axisTest_Z0(double a, double b, double fa, double fb){
        p0 = a*v0x - b*v0y;
        p1 = a*v1x - b*v1y;
        if(p0<p1){
            min = p0;
            max=p1;
        } else {
            min=p1;
            max=p0;
        }
        rad=fa*bHalfWidth + fb*bHalfHeight;

        if(min>rad || max<-rad) return false;
        else return true;
    }


    /**
    * triCubeOverlap tests if a triangle intersects an axis-aligned CUBE, rather than
    * testing intersection against any old AABBox.
    *
    * It expects that data[] has length 4.
    *
    * It uses a separating-axis approach (see Real Time Collision Detection, page 169
    * [by Christer Ericson]).
    * There are 12 axes that must be considered for projection:
    *
    * 1. Three face normals from the AABB (Axis-aligned bounding box)
    * 2. One face normal from the triangle
    * 3. Nine axes given by the cross product of combination of edges from both
    *
    * @return FALSE As soon as a separating axis is found the test can
    * immediately exit with a 'no intersection' result.
    * @return TRUE If all axes are tested and no separating
    * axis is found, the box and the triangle must be intersecting.
    *
    * It has been suggested that the most efficient order in which
    * to perform these three sets of tests is 3-1-2.
    *
    *
    * @param data[] is the data array from a voxel (an OctreeTriangleNode). Indices 0
    * through 2 contain the x,y, and z coordinates of the center
    * of the cube.  Index 3 contains the side length of the cube.
    *
    * @param tri[] indices 0 through 2 contain the x,y, and z coordinates of vertex A
    * of the triangle.  Indices 3 through 5 contain the x,y, and z coordinates of vertex B
    * of the triangle.  Indices 6 through 8 contain the x,y, and z coordinates of vertex C
    * of the triangle.
    *
    */
    public boolean triCubeIntersect(double[] data, float[] tri){

        // this is obviously wasteful.
        // I've done it only to improve readability
        double triAx = tri[0];
        double triAy = tri[1];
        double triAz = tri[2];
        double triBx = tri[3];
        double triBy = tri[4];
        double triBz = tri[5];
        double triCx = tri[6];
        double triCy = tri[7];
        double triCz = tri[8];

        bHalfWidth = (data[3]/2f);
        bHalfHeight = bHalfWidth;
        bHalfDepth = bHalfWidth;

        // first, adjust everything so that the center of the box is at the
        // origin (0, 0, 0).  The "v's" are the coordinates of the adjusted triangle
        // ie: v0x is the x-coordinate of vertex 0 of the adjusted triangle,
        // whereas v1y is the y-coordinate of vertext 1 of the adjusted triangle
        v0x = triAx - data[0];
        v0y = triAy - data[1];
        v0z = triAz - data[2];
        v1x = triBx - data[0];
        v1y = triBy - data[1];
        v1z = triBz - data[2];
        v2x = triCx - data[0];
        v2y = triCy - data[1];
        v2z = triCz - data[2];

        // compute the triangle edges
        double e0x = v1x - v0x;
        double e0y = v1y - v0y;
        double e0z = v1z - v0z;
        double e1x = v2x - v1x;
        double e1y = v2y - v1y;
        double e1z = v2z - v1z;
        double e2x = v0x - v2x;
        double e2y = v0y - v2y;
        double e2z = v0z - v2z;

        //3:test the nine axes given by the cross product of combination of edges

        double fex = Math.abs(e0x);
        double fey = Math.abs(e0y);
        double fez = Math.abs(e0z);

        if(!axisTest_X01(e0z, e0y, fez, fey)) return false;;
        if(!axisTest_Y02(e0z, e0x, fez, fex)) return false;
        if(!axisTest_Z12(e0y, e0x, fey, fex)) return false;

        fex = Math.abs(e1x);
        fey = Math.abs(e1y);
        fez = Math.abs(e1z);

        if(!axisTest_X01(e1z, e1y, fez, fey)) return false;
        if(!axisTest_Y02(e1z, e1x, fez, fex)) return false;
        if(!axisTest_Z0(e1y, e1x, fey, fex)) return false;

        fex = Math.abs(e2x);
        fey = Math.abs(e2y);
        fez = Math.abs(e2z);

        if(!axisTest_X2(e2z, e2y, fez, fey)) return false;
        if(!axisTest_Y1(e2z, e2x, fez, fex)) return false;
        if(!axisTest_Z12(e2y, e2x, fey, fex)) return false;

        /*
        * 1: test the overlap in the three face normals from the AABB
        *       (the {x, y, z} directions)
        * Find min, max of the triangle in each direction, and test for overlap in
        * that direction - this is equivalent to testing a minimal AABB around the
        * triangle against the AABB.
        */

        // test in the x-direction
        findMinMax(v0x, v1x, v2x);
        if(min>bHalfWidth || max<-bHalfWidth) return false;

        // test in the y-direction
        findMinMax(v0y, v1y, v2y);
        if(min>bHalfHeight || max<-bHalfHeight) return false;

        // test in the z-direction
        findMinMax(v0z, v1z, v2z);
        if(min>bHalfDepth || max<-bHalfDepth) return false;

        /*
        * 2: test the overlap of the one normal of the triangle
        * We compute the plane equation of the triangle: normal*x+d=0 and see
        * if the box intersects the plane of the triangle.
        */
        double triNormalX = e1y*e2z - e1z*e2y;
        double triNormalY = e1z*e2x - e1x*e2z;
        double triNormalZ = e1x*e2y - e1y*e2x;

        if(!planeBoxOverlap(triNormalX, triNormalY, triNormalZ, v0x, v0y, v0z)) return false;

        // also test to make sure the triangle is not degenerate
        if(triNormalX*triNormalX + triNormalY*triNormalY + triNormalZ*triNormalZ == 0) return false;
        else return true;  // box and triangle overlap
    }


    /**
     *
     * Need a direction vector?
     * If you have an origin point for a ray and another point through which the ray passes,
     * this simple subtraction method will give you the direction vector for that ray.
     * @param origin origin point for a ray
     * @param rayPassesThruPoint another point through which the ray passes
     * @return the direction vector of the ray
     */
    double[] getDirectionVector(double[] origin, double[] rayPassesThruPoint){
        double[] dirVec = {rayPassesThruPoint[0]-origin[0], rayPassesThruPoint[1]-origin[1], rayPassesThruPoint[2]-origin[2]};
        return dirVec;
    }


    /**
    * An all-purpose ray-triangle intersection test.
    * Note: assumes you have an origin point for the ray
    * and a direction vector.  If you *don't* have a direction
    * vector for the ray, just call getDirectionVector(double[], double[])
    * to get one.
    *
    * This method is based off of Justin Couch's method,
    * private boolean rayTriangle(Point3d origin,Vector3f direction, double[] coords, Point3d point)
    * found in VolumeChecker.java by Russell Dodds.
    * 
    * Original reference:
    * http://www.siggraph.org/education/materials/HyperGraph/raytrace/raypolygon_intersection.htm
    *
    * This code exists to test ray-triangle intersections for users
    * who don't want to import javax.vecmath.*; - it does all the work
    * with double arrays instead of Point3d and Vector3f classes.
    *
    * CAUTION: For the case of a ray intersecting exactly on a triangle edge, this algorithm
    * arbitrarily determines the intersection point as inside or outside of the triangle. Thus,
    * a small tolerance is used to compare against the distance instead of zero.
    * 
    * @param origin a double array of length 3: the x, y, and z coordinates for the origin of the ray
    * @param dir a double array of length 3 -> a direction vector for the ray
    * @param tri a double array of length 9: the x, y, and z coordinates for vertex A, B, and C of the triangle.
    */
    public boolean rayTriangleIntersection(double[] origin, double[] dir, float[] tri){
        /*
        the equation of a plane can be written as:
        ax+by+cz = d
        The coefficients a, b, and c form a vector that is normal to the plane,
        n = [a b c]^T
        Thus, we can re-write the plane equation as: n(dot)x = d
        where x = [x y z]^T

        Now we consider the ray determined by P and d: R(t) = P + td
        To solve for the intersection of ray R(t) with the plane, we simply
        substitute x=R(t) into the plane equation and solve for t:
        n(dot)R(t) = d
        n(dot)[P+td] = d
        n(dot)P + tn(dot)d = d
        t = (d - n(dot)P) / n(dot)d)
        Note that if n(dot)d = 0, then d is parallel to the plane and does not intersect.
        */
    	
        v0x = tri[3] - tri[0];
        v0y = tri[4] - tri[1];
        v0z = tri[5] - tri[2];

        v1x = tri[6] - tri[3];
        v1y = tri[7] - tri[4];
        v1z = tri[8] - tri[5];

        // compute the cross product
        double triNormalX = v0y*v1z - v0z*v1y;
        double triNormalY = v0z*v1x - v0x*v1z;
        double triNormalZ = v0x*v1y - v0y*v1x;
/*
if (origin[0] == 0.015 && origin[2] == 0.015) {
System.out.println("\nray: " + origin[0] + " " + origin[1] + " " + origin[2]);
System.out.println("triangle: " + tri[0] + " " + tri[1] + " " + tri[2] + ", " + 
		                          tri[3] + " " + tri[4] + " " + tri[5] + ", " +
		                          tri[6] + " " + tri[7] + " " + tri[8]);
System.out.println("v0: " + v0x + " " + v0y + " " + v0z);
System.out.println("v1: " + v1x + " " + v1y + " " + v1z);
System.out.println("triNormal: " + triNormalX + " " + triNormalY + " " + triNormalZ);
}
*/
        // if normal.lengthSquared() == 0 return false
        if(triNormalX*triNormalX + triNormalY*triNormalY + triNormalZ*triNormalZ == 0) return false;

        double n_dot_dir = //-triNormalZ;
        // note: we don't need to do
        dotProduct(triNormalX, triNormalY, triNormalZ, dir[0], dir[1], dir[2]);
/*
if (origin[0] == 0.015 && origin[2] == 0.015) {
System.out.println("n_dot_dir: " + n_dot_dir);
}
*/
         // ray and plane parallel?
        if(n_dot_dir == 0)  return false;

        double d = dotProduct(triNormalX, triNormalY, triNormalZ, tri[0], tri[1], tri[2]);
        double n_dot_o = dotProduct(triNormalX, triNormalY, triNormalZ, origin[0], origin[1], origin[2]);
        double t = (d - n_dot_o)/n_dot_dir;
/*
if (origin[0] == 0.015 && origin[2] == 0.015) {
System.out.println("d: " + d);
System.out.println("n_dot_o: " + n_dot_o);
System.out.println("t: " + t);
}
*/
        //if(t < 0) System.out.println("T less than 0");

        // so we have an intersection with the plane of the polygon and the
        // segment/ray.  Using the winding rule to see if inside or outside.
        // First store the exact intersection point anyway, regardless of
        // whether this is an intersection or not.
        // NOTE: because the rays are all parallel to the z-axis, the x and y points will be the same
        // as the origin.
        lastIntersectionPoint.set(origin[0] + dir[0]*t,
                                  origin[1] + dir[1]*t,
                                  origin[2] + dir[2]*t);
/*
if (origin[0] == 0.015 && origin[2] == 0.015) {
System.out.println("lastIntersectionPoint.set: " + (origin[0] + dir[0]*t) + " " + (origin[1] + dir[1]*t) + " " + (origin[2] + dir[2]*t));
}
*/        
        // bounds check

        // find the dominant axis to resolve to a 2 axis system

        double abs_nrm_x = (triNormalX >= 0) ? triNormalX : -triNormalX;
        double abs_nrm_y = (triNormalY >= 0) ? triNormalY : -triNormalY;
        double abs_nrm_z = (triNormalZ >= 0) ? triNormalZ : -triNormalZ;

        int dom_axis;

        if(abs_nrm_x > abs_nrm_y)
            dom_axis = 0;
        else
            dom_axis = 1;

        if(dom_axis == 0)
        {
            if(abs_nrm_x < abs_nrm_z)
                dom_axis = 2;
        }
        else if(abs_nrm_y < abs_nrm_z)
        {
            dom_axis = 2;
        }
/*
if (origin[0] == 0.015 && origin[2] == 0.015) {
System.out.println("dom_axis: " + dom_axis);
System.out.println("");
}
*/
        // Map all the coordinates to the 2D plane. The u and v coordinates
        // are interleaved as u == even indicies and v = odd indicies

        // Steps 1 & 2 combined
        // 1. For NV vertices [Xn Yn Zn] where n = 0..Nv-1, project polygon
        // vertices [Xn Yn Zn] onto dominant coordinate plane (Un Vn).
        // 2. Translate (U, V) polygon so intersection point is origin from
        // (Un', Vn').
        int i;
        int j = 5;
        /* working coords */
        double[] working2dCoords = new double[6];

        switch(dom_axis)
        {
            case 0:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = tri[i * 3 + 2] - (double)lastIntersectionPoint.z;
                    working2dCoords[j--] = tri[i * 3 + 1] - (double)lastIntersectionPoint.y;
                }
                break;

            case 1:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = tri[i * 3 + 2] - (double)lastIntersectionPoint.z;
                    working2dCoords[j--] = tri[i * 3]     - (double)lastIntersectionPoint.x;
                }
                break;

            case 2:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = tri[i * 3 + 1] - (double)lastIntersectionPoint.y;
                    working2dCoords[j--] = tri[i * 3]     - (double)lastIntersectionPoint.x;
                }
                break;
        }

        int sh;  // current sign holder
        int nsh; // next sign holder
        double dist;
        int crossings = 0;

        // Step 4.
        // Set sign holder as f(V' o) ( V' value of 1st vertex of 1st edge)
        if(working2dCoords[1] < 0.0)
            sh = -1;
        else
            sh = 1;
/*
if (origin[0] == 0.015 && origin[2] == 0.015) {
System.out.println("working2dCoords: " + java.util.Arrays.toString(working2dCoords));
System.out.println("sh: " + sh);
}
*/
        for(i = 0; i < 3; i++)
        {
            // Step 5.
            // For each edge of polygon (Ua' V a') -> (Ub', Vb') where
            // a = 0..Nv-1 and b = (a + 1) mod Nv

            // b = (a + 1) mod Nv
            j = (i + 1) % 3;

            int i_u = i * 2;           // index of Ua'
            int j_u = j * 2;           // index of Ub'
            int i_v = i * 2 + 1;       // index of Va'
            int j_v = j * 2 + 1;       // index of Vb'

            // Set next sign holder (Nsh) as f(Vb')
            // Nsh = -1 if Vb' < 0
            // Nsh = +1 if Vb' >= 0
            if(working2dCoords[j_v] < 0.0)
                nsh = -1;
            else
                nsh = 1;
/*
if (origin[0] == 0.015 && origin[2] == 0.015) {
System.out.println("j: " + j);
System.out.println("i_u: " + i_u);
System.out.println("j_u: " + j_u);
System.out.println("i_v: " + i_v);
System.out.println("j_v: " + j_v);
System.out.println("nsh: " + nsh);
System.out.println("");
}
*/
            // If Sh <> NSH then if = then edge doesn't cross +U axis so no
            // ray intersection and ignore

            if(sh != nsh)
            {
                // if Ua' > 0 and Ub' > 0 then edge crosses + U' so Nc = Nc + 1
                if((working2dCoords[i_u] > 0.0) && (working2dCoords[j_u] > 0.0))
                {
if (origin[0] == 0.015 && origin[2] == 0.015) {
System.out.println("working2dCoords[i_u] > 0.0) && (working2dCoords[j_u] > 0.0: " + working2dCoords[i_u] + " " + working2dCoords[j_u]);
}
                    crossings++;
                }
                else if ((working2dCoords[i_u] > 0.0) ||
                         (working2dCoords[j_u] > 0.0))
                {
/*
if (origin[0] == 0.015 && origin[2] == 0.015) {
System.out.println("working2dCoords[i_u] > 0.0) || (working2dCoords[j_u] > 0.0: " + working2dCoords[i_u] + " " + working2dCoords[j_u]);
}
*/
                    // if either Ua' or U b' > 0 then line might cross +U, so
                    // compute intersection of edge with U' axis
                    dist = working2dCoords[i_u] -
                           (working2dCoords[i_v] *
                           (working2dCoords[j_u] - working2dCoords[i_u])) /
                           (working2dCoords[j_v] - working2dCoords[i_v]);
/*
if (origin[0] == 0.015 && origin[2] == 0.015) {
System.out.println("dist: " + dist);
}
*/
                    // if intersection point is > 0 then must cross,
                    // so Nc = Nc + 1
					//
					// Update 9/27/2011:
					// Checking for dist > 0 does not work reliably when a ray intersects 
					// exactly on a triangle edge. Using a small tolerance value instead.
                    if(dist > tolerance)
                        crossings++;
                }

                // Set SH = Nsh and process the next edge
                sh = nsh;
            }
/*
if (origin[0] == 0.015 && origin[2] == 0.015) {
System.out.println("crossings: " + crossings);
}
*/
        }
/*
if (origin[0] == 0.015 && origin[2] == 0.015) {
System.out.println("crossings % 2: " + (crossings % 2));
}
*/
        // Step 6. If Nc odd, point inside else point outside.
        // Note that we have already stored the intersection point way back up
        // the start.
        return ((crossings % 2) == 1);
    }

    /**
    * A simple overload of the zAxisRayTriangle method to handle different input parameters.
    */
    boolean zAxisRayTriangle(double[] origin, float[] tri){
        return zAxisRayTriangle(origin[0], origin[1], origin[2], tri);
    }



    /**
     * @return the last intersection point found by TriangleIntersectionUtil.
     * May return an invalid point if no intersection points have yet been found!
     */
    public Point3d getLastIntersectionPoint(){
        return lastIntersectionPoint;
    }

    /**
     * A ray<->triangle intersection test for rays ONLY going down the z-axis
     *
     * No need, therefore, for a direction vector; in all cases dir[] = {0, 0, -1}.
     * See rayTriangleIntersection() for comparison.
     *
     * CAUTION: For the case of a ray intersecting exactly on a triangle edge, this algorithm
     * arbitrarily determines the intersection point as inside or outside of the triangle. Thus,
     * a small tolerance is used to compare against the distance instead of zero.
     * 
     * @param origin a double array of length 3: the x, y, and z coordinates for the origin of the ray
     * @param tri a double array of length 9: the x, y, and z
     * coordinates for vertex A, B, and C of the triangle.
     */
    public boolean zAxisRayTriangle(double originX,
                                    double originY,
                                    double originZ,
                                    float[] tri){

        v0x = tri[3] - tri[0];
        v0y = tri[4] - tri[1];
        v0z = tri[5] - tri[2];

        v1x = tri[6] - tri[3];
        v1y = tri[7] - tri[4];
        v1z = tri[8] - tri[5];

        // compute the cross product
        double triNormalX = v0y*v1z - v0z*v1y;
        double triNormalY = v0z*v1x - v0x*v1z;
        double triNormalZ = v0x*v1y - v0y*v1x;

        // if normal.lengthSquared() == 0 return false
        if(triNormalX*triNormalX + triNormalY*triNormalY + triNormalZ*triNormalZ == 0) return false;

        double n_dot_dir = -triNormalZ;
        // note: we can skip dotProduct(triNormalX, triNormalY, triNormalZ, dir[0], dir[1], dir[2]);

        // triangle parallel to the z axis? (normal parallel to the y-axis?)
        //if(triNormalX == 0 && triNormalZ == 0) return false;
         // ray and plane parallel?
        if(n_dot_dir == 0) return false;

        double d = dotProduct(triNormalX, triNormalY, triNormalZ, tri[0], tri[1], tri[2]);
        //double n_dot_o = dotProduct(triNormalX, triNormalY, triNormalZ, origin[0], origin[1], origin[2]);
        double n_dot_o = dotProduct(triNormalX, triNormalY, triNormalZ, originX, originY, originZ);
        double t = (d - n_dot_o)/n_dot_dir;

        if(t < 0) return false;

        /*
        * so we have an intersection with the plane of the polygon and the
        * segment/ray.  Using the winding rule to see if inside or outside.
        * First store the exact intersection point anyway, regardless of
        * whether this is an intersection or not.
        * NOTE: because the rays are all parallel to the z-axis, the x and y points will be the same
        * as the origin.
        */
        lastIntersectionPoint.set(originX, originY, originZ - t);


        // bounds check
        //
        // find the dominant axis to resolve to a 2 axis system

        double abs_nrm_x = (triNormalX >= 0) ? triNormalX : -triNormalX;
        double abs_nrm_y = (triNormalY >= 0) ? triNormalY : -triNormalY;
        double abs_nrm_z = (triNormalZ >= 0) ? triNormalZ : -triNormalZ;

        int dom_axis;

        if(abs_nrm_x > abs_nrm_y)
            dom_axis = 0;
        else
            dom_axis = 1;

        if(dom_axis == 0)
        {
            if(abs_nrm_x < abs_nrm_z)
                dom_axis = 2;
        }
        else if(abs_nrm_y < abs_nrm_z)
        {
            dom_axis = 2;
        }

        // Map all the coordinates to the 2D plane. The u and v coordinates
        // are interleaved as u == even indicies and v = odd indicies

        // Steps 1 & 2 combined
        // 1. For NV vertices [Xn Yn Zn] where n = 0..Nv-1, project polygon
        // vertices [Xn Yn Zn] onto dominant coordinate plane (Un Vn).
        // 2. Translate (U, V) polygon so intersection point is origin from
        // (Un', Vn').
        int i;
        int j = 5;
        /* working coords */
        double[] working2dCoords = new double[6];

        switch(dom_axis)
        {
            case 0:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = tri[i * 3 + 2] - (double)lastIntersectionPoint.z;
                    working2dCoords[j--] = tri[i * 3 + 1] - (double)lastIntersectionPoint.y;
                }
                break;

            case 1:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = tri[i * 3 + 2] - (double)lastIntersectionPoint.z;
                    working2dCoords[j--] = tri[i * 3]     - (double)lastIntersectionPoint.x;
                }
                break;

            case 2:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = tri[i * 3 + 1] - (double)lastIntersectionPoint.y;
                    working2dCoords[j--] = tri[i * 3]     - (double)lastIntersectionPoint.x;
                }
                break;
        }

        int sh;  // current sign holder
        int nsh; // next sign holder
        double dist;
        int crossings = 0;

        // Step 4.
        // Set sign holder as f(V' o) ( V' value of 1st vertex of 1st edge)
        if(working2dCoords[1] < 0.0)
            sh = -1;
        else
            sh = 1;

        for(i = 0; i < 3; i++)
        {
            // Step 5.
            // For each edge of polygon (Ua' V a') -> (Ub', Vb') where
            // a = 0..Nv-1 and b = (a + 1) mod Nv

            // b = (a + 1) mod Nv
            j = (i + 1) % 3;

            int i_u = i * 2;           // index of Ua'
            int j_u = j * 2;           // index of Ub'
            int i_v = i * 2 + 1;       // index of Va'
            int j_v = j * 2 + 1;       // index of Vb'

            // Set next sign holder (Nsh) as f(Vb')
            // Nsh = -1 if Vb' < 0
            // Nsh = +1 if Vb' >= 0
            if(working2dCoords[j_v] < 0.0)
                nsh = -1;
            else
                nsh = 1;

            // If Sh <> NSH then if = then edge doesn't cross +U axis so no
            // ray intersection and ignore

            if(sh != nsh)
            {
                // if Ua' > 0 and Ub' > 0 then edge crosses + U' so Nc = Nc + 1
                if((working2dCoords[i_u] > 0.0) && (working2dCoords[j_u] > 0.0))
                {
                    crossings++;
                }
                else if ((working2dCoords[i_u] > 0.0) ||
                         (working2dCoords[j_u] > 0.0))
                {
                    // if either Ua' or U b' > 0 then line might cross +U, so
                    // compute intersection of edge with U' axis
                    dist = working2dCoords[i_u] -
                           (working2dCoords[i_v] *
                            (working2dCoords[j_u] - working2dCoords[i_u])) /
                           (working2dCoords[j_v] - working2dCoords[i_v]);

                    // if intersection point is > 0 then must cross,
                    // so Nc = Nc + 1
					//
					// Update 9/27/2011:
					// Checking for dist > 0 does not work reliably when a ray intersects 
					// exactly on a triangle edge. Using a small tolerance value instead.
                    if(dist > tolerance)
                        crossings++;
                }

                // Set SH = Nsh and process the next edge
                sh = nsh;
            }
        }

        // Step 6. If Nc odd, point inside else point outside.
        // Note that we have already stored the intersection point way back up
        // the start.
        return ((crossings % 2) == 1);
    }

    /**
     * A ray<->triangle intersection test for rays ONLY going down the y-axis
     *
     * No need, therefore, for a direction vector; in all cases dir[] = {0, -1, 0}.
     * See rayTriangleIntersection() for comparison.
     *
     * CAUTION: For the case of a ray intersecting exactly on a triangle edge, this algorithm
     * arbitrarily determines the intersection point as inside or outside of the triangle. Thus,
     * a small tolerance is used to compare against the distance instead of zero.
     * 
     * @param origin a double array of length 3: the x, y, and z coordinates for the origin of the ray
     * @param tri a double array of length 9: the x, y, and z
     * coordinates for vertex A, B, and C of the triangle.
     */
    public boolean yAxisRayTriangle(double originX,
                                    double originY,
                                    double originZ,
                                    float[] tri){

        v0x = tri[3] - tri[0];
        v0y = tri[4] - tri[1];
        v0z = tri[5] - tri[2];

        v1x = tri[6] - tri[3];
        v1y = tri[7] - tri[4];
        v1z = tri[8] - tri[5];

        // compute the cross product
        double triNormalX = v0y*v1z - v0z*v1y;
        double triNormalY = v0z*v1x - v0x*v1z;
        double triNormalZ = v0x*v1y - v0y*v1x;

        // if normal.lengthSquared() == 0 return false
        if(triNormalX*triNormalX + triNormalY*triNormalY + triNormalZ*triNormalZ == 0) return false;

        double n_dot_dir = -triNormalY;
        // note: we can skip dotProduct(triNormalX, triNormalY, triNormalZ, dir[0], dir[1], dir[2]);

        // triangle parallel to the z axis? (normal parallel to the y-axis?)
        //if(triNormalX == 0 && triNormalZ == 0) return false;
         // ray and plane parallel?
        if(n_dot_dir == 0) return false;

        double d = dotProduct(triNormalX, triNormalY, triNormalZ, tri[0], tri[1], tri[2]);
        double n_dot_o = dotProduct(triNormalX, triNormalY, triNormalZ, originX, originY, originZ);
        double t = (d - n_dot_o)/n_dot_dir;

        if(t < 0) return false;

        /*
        * so we have an intersection with the plane of the polygon and the
        * segment/ray.  Using the winding rule to see if inside or outside.
        * First store the exact intersection point anyway, regardless of
        * whether this is an intersection or not.
        * NOTE: because the rays are all parallel to the y-axis, the x and z points will be the same
        * as the origin.
        */
        lastIntersectionPoint.set(originX, originY - t, originZ);


        // bounds check
        //
        // find the dominant axis to resolve to a 2 axis system

        double abs_nrm_x = (triNormalX >= 0) ? triNormalX : -triNormalX;
        double abs_nrm_y = (triNormalY >= 0) ? triNormalY : -triNormalY;
        double abs_nrm_z = (triNormalZ >= 0) ? triNormalZ : -triNormalZ;

        int dom_axis;

        if(abs_nrm_x > abs_nrm_y)
            dom_axis = 0;
        else
            dom_axis = 1;

        if(dom_axis == 0)
        {
            if(abs_nrm_x < abs_nrm_z)
                dom_axis = 2;
        }
        else if(abs_nrm_y < abs_nrm_z)
        {
            dom_axis = 2;
        }

        // Map all the coordinates to the 2D plane. The u and v coordinates
        // are interleaved as u == even indicies and v = odd indicies

        // Steps 1 & 2 combined
        // 1. For NV vertices [Xn Yn Zn] where n = 0..Nv-1, project polygon
        // vertices [Xn Yn Zn] onto dominant coordinate plane (Un Vn).
        // 2. Translate (U, V) polygon so intersection point is origin from
        // (Un', Vn').
        int i;
        int j = 5;
        /* working coords */
        double[] working2dCoords = new double[6];

        switch(dom_axis)
        {
            case 0:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = tri[i * 3 + 2] - (double)lastIntersectionPoint.z;
                    working2dCoords[j--] = tri[i * 3 + 1] - (double)lastIntersectionPoint.y;
                }
                break;

            case 1:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = tri[i * 3 + 2] - (double)lastIntersectionPoint.z;
                    working2dCoords[j--] = tri[i * 3]     - (double)lastIntersectionPoint.x;
                }
                break;

            case 2:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = tri[i * 3 + 1] - (double)lastIntersectionPoint.y;
                    working2dCoords[j--] = tri[i * 3]     - (double)lastIntersectionPoint.x;
                }
                break;
        }

        int sh;  // current sign holder
        int nsh; // next sign holder
        double dist;
        int crossings = 0;

        // Step 4.
        // Set sign holder as f(V' o) ( V' value of 1st vertex of 1st edge)
        if(working2dCoords[1] < 0.0)
            sh = -1;
        else
            sh = 1;

        for(i = 0; i < 3; i++)
        {
            // Step 5.
            // For each edge of polygon (Ua' V a') -> (Ub', Vb') where
            // a = 0..Nv-1 and b = (a + 1) mod Nv

            // b = (a + 1) mod Nv
            j = (i + 1) % 3;

            int i_u = i * 2;           // index of Ua'
            int j_u = j * 2;           // index of Ub'
            int i_v = i * 2 + 1;       // index of Va'
            int j_v = j * 2 + 1;       // index of Vb'

            // Set next sign holder (Nsh) as f(Vb')
            // Nsh = -1 if Vb' < 0
            // Nsh = +1 if Vb' >= 0
            if(working2dCoords[j_v] < 0.0)
                nsh = -1;
            else
                nsh = 1;

            // If Sh <> NSH then if = then edge doesn't cross +U axis so no
            // ray intersection and ignore

            if(sh != nsh)
            {
                // if Ua' > 0 and Ub' > 0 then edge crosses + U' so Nc = Nc + 1
                if((working2dCoords[i_u] > 0.0) && (working2dCoords[j_u] > 0.0))
                {
                    crossings++;
                }
                else if ((working2dCoords[i_u] > 0.0) ||
                         (working2dCoords[j_u] > 0.0))
                {
                    // if either Ua' or U b' > 0 then line might cross +U, so
                    // compute intersection of edge with U' axis
                    dist = working2dCoords[i_u] -
                           (working2dCoords[i_v] *
                            (working2dCoords[j_u] - working2dCoords[i_u])) /
                           (working2dCoords[j_v] - working2dCoords[i_v]);

                    // if intersection point is > 0 then must cross,
                    // so Nc = Nc + 1
					//
					// Update 9/27/2011:
					// Checking for dist > 0 does not work reliably when a ray intersects 
					// exactly on a triangle edge. Using a small tolerance value instead.
                    if(dist > tolerance)
                        crossings++;
                }

                // Set SH = Nsh and process the next edge
                sh = nsh;
            }
        }

        // Step 6. If Nc odd, point inside else point outside.
        // Note that we have already stored the intersection point way back up
        // the start.
        return ((crossings % 2) == 1);
    }

    /**
     * A ray<->triangle intersection test for rays ONLY going down the x-axis
     *
     * No need, therefore, for a direction vector; in all cases dir[] = {-1, 0, 0}.
     * See rayTriangleIntersection() for comparison.
     *
     * CAUTION: For the case of a ray intersecting exactly on a triangle edge, this algorithm
     * arbitrarily determines the intersection point as inside or outside of the triangle. Thus,
     * a small tolerance is used to compare against the distance instead of zero.
     * 
     * @param origin a double array of length 3: the x, y, and z coordinates for the origin of the ray
     * @param tri a double array of length 9: the x, y, and z
     * coordinates for vertex A, B, and C of the triangle.
     */
    public boolean xAxisRayTriangle(double originX,
                                    double originY,
                                    double originZ,
                                    float[] tri){

        v0x = tri[3] - tri[0];
        v0y = tri[4] - tri[1];
        v0z = tri[5] - tri[2];

        v1x = tri[6] - tri[3];
        v1y = tri[7] - tri[4];
        v1z = tri[8] - tri[5];

        // compute the cross product
        double triNormalX = v0y*v1z - v0z*v1y;
        double triNormalY = v0z*v1x - v0x*v1z;
        double triNormalZ = v0x*v1y - v0y*v1x;

        // if normal.lengthSquared() == 0 return false
        if(triNormalX*triNormalX + triNormalY*triNormalY + triNormalZ*triNormalZ == 0) return false;

        double n_dot_dir = -triNormalX;
        // note: we can skip dotProduct(triNormalX, triNormalY, triNormalZ, dir[0], dir[1], dir[2]);

        // triangle parallel to the z axis? (normal parallel to the y-axis?)
        //if(triNormalX == 0 && triNormalZ == 0) return false;
         // ray and plane parallel?
        if(n_dot_dir == 0) return false;

        double d = dotProduct(triNormalX, triNormalY, triNormalZ, tri[0], tri[1], tri[2]);
        double n_dot_o = dotProduct(triNormalX, triNormalY, triNormalZ, originX, originY, originZ);
        double t = (d - n_dot_o)/n_dot_dir;

        if(t < 0) return false;

        /*
        * so we have an intersection with the plane of the polygon and the
        * segment/ray.  Using the winding rule to see if inside or outside.
        * First store the exact intersection point anyway, regardless of
        * whether this is an intersection or not.
        * NOTE: because the rays are all parallel to the y-axis, the x and z points will be the same
        * as the origin.
        */
        lastIntersectionPoint.set(originX - t, originY, originZ);


        // bounds check
        //
        // find the dominant axis to resolve to a 2 axis system

        double abs_nrm_x = (triNormalX >= 0) ? triNormalX : -triNormalX;
        double abs_nrm_y = (triNormalY >= 0) ? triNormalY : -triNormalY;
        double abs_nrm_z = (triNormalZ >= 0) ? triNormalZ : -triNormalZ;

        int dom_axis;

        if(abs_nrm_x > abs_nrm_y)
            dom_axis = 0;
        else
            dom_axis = 1;

        if(dom_axis == 0)
        {
            if(abs_nrm_x < abs_nrm_z)
                dom_axis = 2;
        }
        else if(abs_nrm_y < abs_nrm_z)
        {
            dom_axis = 2;
        }

        // Map all the coordinates to the 2D plane. The u and v coordinates
        // are interleaved as u == even indicies and v = odd indicies

        // Steps 1 & 2 combined
        // 1. For NV vertices [Xn Yn Zn] where n = 0..Nv-1, project polygon
        // vertices [Xn Yn Zn] onto dominant coordinate plane (Un Vn).
        // 2. Translate (U, V) polygon so intersection point is origin from
        // (Un', Vn').
        int i;
        int j = 5;
        /* working coords */
        double[] working2dCoords = new double[6];

        switch(dom_axis)
        {
            case 0:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = tri[i * 3 + 2] - (double)lastIntersectionPoint.z;
                    working2dCoords[j--] = tri[i * 3 + 1] - (double)lastIntersectionPoint.y;
                }
                break;

            case 1:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = tri[i * 3 + 2] - (double)lastIntersectionPoint.z;
                    working2dCoords[j--] = tri[i * 3]     - (double)lastIntersectionPoint.x;
                }
                break;

            case 2:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = tri[i * 3 + 1] - (double)lastIntersectionPoint.y;
                    working2dCoords[j--] = tri[i * 3]     - (double)lastIntersectionPoint.x;
                }
                break;
        }

        int sh;  // current sign holder
        int nsh; // next sign holder
        double dist;
        int crossings = 0;

        // Step 4.
        // Set sign holder as f(V' o) ( V' value of 1st vertex of 1st edge)
        if(working2dCoords[1] < 0.0)
            sh = -1;
        else
            sh = 1;

        for(i = 0; i < 3; i++)
        {
            // Step 5.
            // For each edge of polygon (Ua' V a') -> (Ub', Vb') where
            // a = 0..Nv-1 and b = (a + 1) mod Nv

            // b = (a + 1) mod Nv
            j = (i + 1) % 3;

            int i_u = i * 2;           // index of Ua'
            int j_u = j * 2;           // index of Ub'
            int i_v = i * 2 + 1;       // index of Va'
            int j_v = j * 2 + 1;       // index of Vb'

            // Set next sign holder (Nsh) as f(Vb')
            // Nsh = -1 if Vb' < 0
            // Nsh = +1 if Vb' >= 0
            if(working2dCoords[j_v] < 0.0)
                nsh = -1;
            else
                nsh = 1;

            // If Sh <> NSH then if = then edge doesn't cross +U axis so no
            // ray intersection and ignore

            if(sh != nsh)
            {
                // if Ua' > 0 and Ub' > 0 then edge crosses + U' so Nc = Nc + 1
                if((working2dCoords[i_u] > 0.0) && (working2dCoords[j_u] > 0.0))
                {
                    crossings++;
                }
                else if ((working2dCoords[i_u] > 0.0) ||
                         (working2dCoords[j_u] > 0.0))
                {
                    // if either Ua' or U b' > 0 then line might cross +U, so
                    // compute intersection of edge with U' axis
                    dist = working2dCoords[i_u] -
                           (working2dCoords[i_v] *
                            (working2dCoords[j_u] - working2dCoords[i_u])) /
                           (working2dCoords[j_v] - working2dCoords[i_v]);

                    // if intersection point is > 0 then must cross,
                    // so Nc = Nc + 1
					//
					// Update 9/27/2011:
					// Checking for dist > 0 does not work reliably when a ray intersects 
					// exactly on a triangle edge. Using a small tolerance value instead.
                    if(dist > tolerance)
                        crossings++;
                }

                // Set SH = Nsh and process the next edge
                sh = nsh;
            }
        }

        // Step 6. If Nc odd, point inside else point outside.
        // Note that we have already stored the intersection point way back up
        // the start.
        return ((crossings % 2) == 1);
    }

    //-----------------------------------------------------------------------
    // Local Methods - package private
    // kept only to preserve functionality with original wall thickness code.
    //-----------------------------------------------------------------------

    /**
     * A ray<->triangle intersection test for rays ONLY going down the z-axis
     *
     * No need, therefore, for a direction vector; in all cases dir[] = {0, 0, -1}.
     * See rayTriangleIntersection() for comparison.
     *
     * CAUTION: For the case of a ray intersecting exactly on a triangle edge, this algorithm
     * arbitrarily determines the intersection point as inside or outside of the triangle. Thus,
     * a small tolerance is used to compare against the distance instead of zero.
     * 
     * @return TRUE if the triangle intersects the ray AND
     * the intersection point is within the bounds of the voxel.
     * TODO: Add final test to end of method to determine if intersection point is within
     * the bounds of the voxel.
     *
     * @param originX: the x coordinate for the origin of the ray
     * @param originY: the y coordinate for the origin of the ray
     * @param originZ: the z coordinate for the origin of the ray
     * @param tri[0], tri[1], tri[2]: the x, y, and z coordinates for vertex A of the triangle.
     * @param tri[3], tri[4], tri[5]: the x, y, and z coordinates for vertex B of the triangle.
     * @param tri[6], tri[7], tri[8]: the x, y, and z coordinates for vertex C of the triangle.
     * @param data: a double array of length 4 containing voxel information (see OctreeNode for more info).
     */
    boolean zAxisRayTriangleVoxel(double originX,
                                  double originY,
                                  double originZ,
                                  float[] tri,
                                  double[] data){

        v0x = tri[3] - tri[0];
        v0y = tri[4] - tri[1];
        v0z = tri[5] - tri[2];

        v1x = tri[6] - tri[3];
        v1y = tri[7] - tri[4];
        v1z = tri[8] - tri[5];

        // compute the cross product
        double triNormalX = v0y*v1z - v0z*v1y;
        double triNormalY = v0z*v1x - v0x*v1z;
        double triNormalZ = v0x*v1y - v0y*v1x;

        // if normal.lengthSquared() == 0 return false
        if(triNormalX*triNormalX + triNormalY*triNormalY + triNormalZ*triNormalZ == 0) return false;

        double n_dot_dir = -triNormalZ;
        // note: we can skip dotProduct(triNormalX, triNormalY, triNormalZ, dir[0], dir[1], dir[2]);

        // triangle parallel to the z axis? (normal parallel to the y-axis?)
        //if(triNormalX == 0 && triNormalZ == 0) return false;
         // ray and plane parallel?
        if(n_dot_dir == 0) return false;

        double d = dotProduct(triNormalX, triNormalY, triNormalZ, tri[0], tri[1], tri[2]);
        //double n_dot_o = dotProduct(triNormalX, triNormalY, triNormalZ, origin[0], origin[1], origin[2]);
        double n_dot_o = dotProduct(triNormalX, triNormalY, triNormalZ, originX, originY, originZ);
        double t = (d - n_dot_o)/n_dot_dir;

        if(t < 0) return false;

        /*
        * so we have an intersection with the plane of the polygon and the
        * segment/ray.  Using the winding rule to see if inside or outside.
        * First store the exact intersection point anyway, regardless of
        * whether this is an intersection or not.
        * NOTE: because the rays are all parallel to the z-axis, the x and y points will be the same
        * as the origin.
        */
        double intersectionPointX = originX;
        double intersectionPointY = originY;
        double intersectionPointZ = originZ -t;

        // bounds check

        // find the dominant axis to resolve to a 2 axis system

        double abs_nrm_x = (triNormalX >= 0) ? triNormalX : -triNormalX;
        double abs_nrm_y = (triNormalY >= 0) ? triNormalY : -triNormalY;
        double abs_nrm_z = (triNormalZ >= 0) ? triNormalZ : -triNormalZ;

        int dom_axis;

        if(abs_nrm_x > abs_nrm_y)
            dom_axis = 0;
        else
            dom_axis = 1;

        if(dom_axis == 0)
        {
            if(abs_nrm_x < abs_nrm_z)
                dom_axis = 2;
        }
        else if(abs_nrm_y < abs_nrm_z)
        {
            dom_axis = 2;
        }

        // Map all the coordinates to the 2D plane. The u and v coordinates
        // are interleaved as u == even indicies and v = odd indicies

        // Steps 1 & 2 combined
        // 1. For NV vertices [Xn Yn Zn] where n = 0..Nv-1, project polygon
        // vertices [Xn Yn Zn] onto dominant coordinate plane (Un Vn).
        // 2. Translate (U, V) polygon so intersection point is origin from
        // (Un', Vn').
        int i;
        int j = 5;
        /* working coords */
        double[] working2dCoords = new double[6];

        switch(dom_axis)
        {
            case 0:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = tri[i * 3 + 2] - (double)intersectionPointZ;
                    working2dCoords[j--] = tri[i * 3 + 1] - (double)intersectionPointY;
                }
                break;

            case 1:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = tri[i * 3 + 2] - (double)intersectionPointZ;
                    working2dCoords[j--] = tri[i * 3]     - (double)intersectionPointX;
                }
                break;

            case 2:
                for(i = 3; --i >= 0; )
                {
                    working2dCoords[j--] = tri[i * 3 + 1] - (double)intersectionPointY;
                    working2dCoords[j--] = tri[i * 3]     - (double)intersectionPointX;
                }
                break;
        }

        int sh;  // current sign holder
        int nsh; // next sign holder
        double dist;
        int crossings = 0;

        // Step 4.
        // Set sign holder as f(V' o) ( V' value of 1st vertex of 1st edge)
        if(working2dCoords[1] < 0.0)
            sh = -1;
        else
            sh = 1;

        for(i = 0; i < 3; i++)
        {
            // Step 5.
            // For each edge of polygon (Ua' V a') -> (Ub', Vb') where
            // a = 0..Nv-1 and b = (a + 1) mod Nv

            // b = (a + 1) mod Nv
            j = (i + 1) % 3;

            int i_u = i * 2;           // index of Ua'
            int j_u = j * 2;           // index of Ub'
            int i_v = i * 2 + 1;       // index of Va'
            int j_v = j * 2 + 1;       // index of Vb'

            // Set next sign holder (Nsh) as f(Vb')
            // Nsh = -1 if Vb' < 0
            // Nsh = +1 if Vb' >= 0
            if(working2dCoords[j_v] < 0.0)
                nsh = -1;
            else
                nsh = 1;

            // If Sh <> NSH then if = then edge doesn't cross +U axis so no
            // ray intersection and ignore

            if(sh != nsh)
            {
                // if Ua' > 0 and Ub' > 0 then edge crosses + U' so Nc = Nc + 1
                if((working2dCoords[i_u] > 0.0) && (working2dCoords[j_u] > 0.0))
                {
                    crossings++;
                }
                else if ((working2dCoords[i_u] > 0.0) ||
                         (working2dCoords[j_u] > 0.0))
                {
                    // if either Ua' or U b' > 0 then line might cross +U, so
                    // compute intersection of edge with U' axis
                    dist = working2dCoords[i_u] -
                           (working2dCoords[i_v] *
                            (working2dCoords[j_u] - working2dCoords[i_u])) /
                           (working2dCoords[j_v] - working2dCoords[i_v]);

                    // if intersection point is > 0 then must cross,
                    // so Nc = Nc + 1
					//
					// Update 9/27/2011:
					// Checking for dist > 0 does not work reliably when a ray intersects 
					// exactly on a triangle edge. Using a small tolerance value instead.
                    if(dist > tolerance)
                        crossings++;
                }

                // Set SH = Nsh and process the next edge
                sh = nsh;
            }
        }

        // Step 6. If Nc odd, point inside else point outside.
        // Note that we have already stored the intersection point way back up
        // the start.


        /*
         * Super-old comments that used to be in TriangleIntersectionUtil
         * and are probably not relevant any more:
         */
         /*
         * note that in the standard triCubeIntersect we have the line:
         * bHalfWidth = (data[3] /2f);
         *
         * Because the new width is data[3] + 2*resolution, it is equivalent to the line:
         * bHalfWidth = (data[3] /2f) + resolution;
         *
         * Which creates an overlap of 'resolution' on all sides.
         */

        double halfSize = (data[3] /2f) + resolution *.2f;
        if( intersectionPointX <= data[0]+halfSize && intersectionPointX >= data[0]-halfSize &&
            intersectionPointY <= data[1]+halfSize && intersectionPointY >= data[1]-halfSize &&
            intersectionPointZ <= data[2]+halfSize && intersectionPointZ >= data[2]-halfSize){


        /*
        * TODO: add the test to see if the point is within the bounds of the voxel,
        * using data[] information.
        * Extend the true dimensions of the voxels just slightly on all sides,
        * to ensure we catch the cases of triangles falling 'between' voxels.
        * The change should not be difficult; note that class OctreeNode
        * already has a "containsPoint()" method.
        */
        return ((crossings % 2) == 1);
        }

        else {  // intersection point does not actually exist within this cube

            return false;
        }
    }

}

