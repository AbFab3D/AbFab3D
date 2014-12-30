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

package abfab3d.geomutil;

import java.util.Vector;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector2d;

/**
   routines used to subdivide various lines and curves 

   @author Vladimir Bulatov 
 */
public class Subdivider {
    
    static public final int DEFAULTSUBDIVISION = 8;


    public static <T> AInterpolator makeInterpolator(Object item, double maxlength){

        AInterpolator<T> interpolator = null;
        if(item instanceof Vector3d) interpolator = (AInterpolator<T>)new InterpolatorVector3d(maxlength);
        else if(item instanceof Vector2d) interpolator = (AInterpolator<T>)new InterpolatorVector2d(maxlength);        
        return interpolator;

    }



    public static <T> Vector<T> makeQuadricCurve(Vector<T> polyline, boolean closed){

        T item = polyline.get(0);        
        return makeQuadricCurve(polyline, makeInterpolator(polyline.get(0), 0.), closed);

    }

    /**
       makes smooth quadric curve from set of points 
       extra points are inserted at the midpoints of each segment 
       @param polyline set of imput points 
       @param interpolator of points        
       @param closed if true the last point is quadric made closed with smooth connection of ends 
     */
    public static <T> Vector<T> makeQuadricCurve(Vector<T> polyline, AInterpolator<T> interpolator, boolean closed){
        
        Vector<T> points = new Vector<T>();
        int count = polyline.size();
    
        for(int i = 0; i < count; i++){
            
            T p1 = polyline.get(i);           
            T p2 = polyline.get((i+1)%count);  // closed line 
            T p12 = interpolator.midpoint(p1,p2);
            
            points.add(p12);
            points.add(p2);
        }  
        // add last point
        points.add(points.get(0));
        return points;
        
    }

    /**
       return Vector of points generated from polyline via subdivision procedure 
       @param polyline vector of vertices of polyline 
       @param maxdistance maximal distance between points in subdivided polyline
       
    */    
    public static <T> Vector<T> subdividePolyline(Vector<T> polyline, double maxlength){

        T item = polyline.get(0);
        AInterpolator<T> interpolator = null;
        if(item instanceof Vector3d) interpolator = (AInterpolator<T>)new InterpolatorVector3d(maxlength);
        else if(item instanceof Vector2d) interpolator = (AInterpolator<T>)new InterpolatorVector2d(maxlength);
        
        return subdividePolyline(polyline, DEFAULTSUBDIVISION, interpolator);        

    }


    /**
       @return Vector of points generated from polyline via subdivision procedure 
       @param polyline vector of vertices of polyline 
       @param maxlevel maximal subdivision level
       @param interpolator interpoilator of points        
    */
    public static <T>  Vector<T> subdividePolyline(Vector<T> polyline, int level, AInterpolator<T> interpolator){
        
        Vector<T> points = new Vector<T>();
        int qcount = polyline.size()-1;
        
        for(int i = 0; i < qcount; i++){
            
            T p1 = polyline.get(i);
            T p2 = polyline.get(i+1);
            points.add(p1);
            subdivideLineSegment(points, p1, p2,level, interpolator);      
        }  
        // add last point 
        points.add(polyline.get(qcount));    
        return points;

    }
    
    /**
     * subdivides line segment and stores result in points 
     *
     */
    public static <T> void subdivideLineSegment(Vector<T> points, T p1, T p2, int maxSubdivision, AInterpolator<T> interpolator){
        
        if(!interpolator.needSubdivision(p1, p2))
            return; 
        if(maxSubdivision <= 0)
            return;
        
        T p12 = interpolator.midpoint(p1,p2);
        
        maxSubdivision--;
        
        subdivideLineSegment(points, p1, p12, maxSubdivision, interpolator);
        
        points.add(p12);
        
        subdivideLineSegment(points, p12, p2, maxSubdivision, interpolator);
        
    }

    /**
       makes subdivided curve from control points of quad 
       @param polyquad sequence of points of quad segments
       @param maxlength maximal length of subdivided segment 
     */
    public static <T> Vector<T> subdividePolyquad(Vector<T> polyquad, double maxlength){

        T item = polyquad.get(0);
        AInterpolator<T> interpolator = null;
        if(item instanceof Vector3d) interpolator = (AInterpolator<T>)new InterpolatorVector3d(maxlength);
        else if(item instanceof Vector2d) interpolator = (AInterpolator<T>)new InterpolatorVector2d(maxlength);
            
        return subdividePolyquad(polyquad, DEFAULTSUBDIVISION, interpolator);
        
    }

    /**
       makes subdivided curve from control points of quad 
       @param polyquad sequence of points of quad segments
       @param level maximal level of sibdivision 
       @param tester test if segment needs more subdivision 
    */
    public static <T> Vector<T> subdividePolyquad(Vector<T> polyquad, int level, AInterpolator<T> interpolator){
    
        Vector<T> points = new Vector<T>();
        int qcount = (polyquad.size() - 1)/2;
    
        for(int i = 0; i < qcount; i++){
            
            T p1 = polyquad.get(i*2);
            T p2 = polyquad.get(i*2+1);
            T p3 = polyquad.get(i*2+2);
            points.add(p1);
            subdivideQuad(points, p1, p2, p3, level, interpolator);      
        }  
        // add last point
        points.add(polyquad.get(2*qcount));            
        return points;
    }
    
    /**
     *  recursive subdivide quad segment 
     * 
     *
     */
    public static <T> void subdivideQuad(Vector<T> points, T p1, T p2, T p3, int maxSubdivision, AInterpolator<T> interpolator){
        
        if(maxSubdivision <= 0)
            return;
        
        T p12 = interpolator.midpoint(p1,p2);
        T p23 = interpolator.midpoint(p2,p3);
        T p123 = interpolator.midpoint(p12,p23);
        
        maxSubdivision--;
        
        if(maxSubdivision > 0 &&  interpolator.needSubdivision(p1, p123))
            subdivideQuad(points, p1, p12, p123, maxSubdivision, interpolator);
        
        points.add(p123);
        
        if(maxSubdivision > 0 && interpolator.needSubdivision(p123, p3))
            subdivideQuad(points, p123, p23, p3, maxSubdivision, interpolator);
        
    }

    /**
       makes subdivided curve from control points of cubc 
       @param polycubic sequence of points of cubic segments
       @param maxlength maximal length of subdivided segment 
     */
    public static <T> Vector<T> subdividePolycubic(Vector<T> polycubic, double maxlength){

        T item = polycubic.get(0);
        AInterpolator<T> interpolator = null;
        if(item instanceof Vector3d) interpolator = (AInterpolator<T>)new InterpolatorVector3d(maxlength);
        else if(item instanceof Vector2d) interpolator = (AInterpolator<T>)new InterpolatorVector2d(maxlength);
        
        return subdividePolycubic(polycubic, DEFAULTSUBDIVISION, interpolator);
        
    }

    /**
       makes subdivided curve from control points of cubi
       @param polycubic sequence of points of cubic segments
       @param level maximal level of sibdivision 
       @param tester test if segment needs more subdivision 
    */
    public static <T> Vector<T> subdividePolycubic(Vector<T> polycubic, int level, AInterpolator<T> interpolator){
        
        Vector<T> points = new Vector<T>();
        int qcount = (polycubic.size() - 1)/3;
    
        for(int i = 0; i < qcount; i++){
            int i3 = 3*i;
            T p1 = polycubic.get(i3);
            T p2 = polycubic.get(i3+1);
            T p3 = polycubic.get(i3+2);
            T p4 = polycubic.get(i3+3);
            points.add(p1);
            subdivideCubic(points, p1, p2, p3, p4, level, interpolator);      
        }  
        // add last point
        points.add(polycubic.get(3*qcount));            
        return points;
    }

    /**
       
     */
    public static <T> void subdivideCubic(Vector<T> points, 
                                      T p1, T p2, T p3, T p4, 
                                      int maxSubdivision,
                                      AInterpolator<T> interpolator){
        if(maxSubdivision <= 0)
            return;
        
        T p12 = interpolator.midpoint(p1,p2);
        T p23 = interpolator.midpoint(p2,p3);
        T p34 = interpolator.midpoint(p3,p4);
        T p123 = interpolator.midpoint(p12,p23);
        T p234 = interpolator.midpoint(p23,p34);
        T p1234 = interpolator.midpoint(p123,p234);
        
        maxSubdivision--;
        
        if(maxSubdivision > 0 &&  interpolator.needSubdivision(p1, p1234))
            subdivideCubic(points, p1, p12, p123, p1234, maxSubdivision, interpolator);
        
        points.add(p1234);
        
        if(maxSubdivision > 0 && interpolator.needSubdivision(p1234, p4))
            subdivideCubic(points, p1234, p234, p34, p4, maxSubdivision, interpolator);
        
    }  

}
