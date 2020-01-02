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
import java.util.Vector;
import java.util.Arrays;
import java.util.HashMap;

import abfab3d.util.PointMap;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.MathUtil.str;
import static java.lang.Math.*;


/**
   represents single slice 
 */
public class Slice {
    
    static final boolean DEBUG = false;
    static final double EPSILON =1.e-8;

    
    Vector3d m_normal = new Vector3d();
    Vector3d m_pointOnPlane = new Vector3d();

    // count of segments 
    int m_segCount;
    int m_contourCount = 0;

    PointMap m_points;
    HashMap<Integer,Contour> m_starts = new HashMap<Integer,Contour>();
    HashMap<Integer,Contour> m_ends = new HashMap<Integer,Contour>();
    Vector<Contour> m_closedContours = new Vector<Contour>();
    Vector<Contour> m_openContours = null;

    //
    int m_appendCount = 0;
    int m_prependCount = 0;
    int m_joinCount = 0;
    int m_closeCount = 0;
    int m_newCount = 0;

    public Slice(){
        this(new Vector3d(0,0,1),new Vector3d(0,0,0),EPSILON);
    }

    public Slice(Vector3d normal, Vector3d pointOnPlane){        
        this(normal, pointOnPlane,EPSILON);
    }

    public Slice(Vector3d normal, Vector3d pointOnPlane, double epsilon){

        m_normal.set(normal);
        m_pointOnPlane.set(pointOnPlane);        
        m_points = new PointMap(3, 0.75, epsilon);

    }
    
    public double getSliceDistance(){
        return m_normal.dot(m_pointOnPlane);
    }
    
    /**
       add new segment to the slice 
     */
    public void addSegment(Vector3d p0, Vector3d p1){

        m_segCount++;
        int ind0 = m_points.add(p0.x,p0.y,p0.z);
        int ind1 = m_points.add(p1.x,p1.y,p1.z);
        Integer seg0 = new Integer(ind0);
        Integer seg1 = new Integer(ind1);
        String fmt = "%7.4f";
        Contour c0 = m_ends.get(seg0);
        Contour c1 = m_starts.get(seg1);
        int cc = ((c0 != null)?1:0) | ((c1 != null)?2:0);
        if(DEBUG)printf("addSegment(%s:%s %s:%s)  ", str(fmt, p0), seg0, str(fmt, p1), seg1);
        if(DEBUG)printf("cc:%d, c0:%s c1:%s\n", cc, c0, c1);
        switch(cc){
        case 0:
            // new contour 
            m_newCount++;
            Contour c = new Contour(seg0, seg1);
            m_starts.put(seg0, c);
            m_ends.put(seg1, c);
            break;
        case 1: 
            // start exists
            m_appendCount++;
            c0.append(ind1);
            m_ends.remove(seg0);
            m_ends.put(seg1, c0);
            break;
        case 2: 
            // end  exists
            m_prependCount++;
            c1.prepend(ind0);
            m_starts.remove(seg1);
            m_starts.put(seg0, c1);
            break;
        case 3: 
            // both ends exist -> join contours 
            if(c0 != c1){
                // different contours, join them together 
                m_joinCount++;
                c0.append(c1);
                m_ends.remove(seg0);
                m_starts.remove(seg1);
                m_ends.put(new Integer(c0.getEnd()),c0);
            } else {
                m_closeCount++;
                //printf("closing contour\n");
                c0.close();
                m_ends.remove(seg0);
                m_starts.remove(seg1);
                m_closedContours.add(c0);
            }  
            break;

        }

    }

    public int getPointCount(){
        return m_points.getPointCount();
    }

    public int getSegmentCount(){
        return m_segCount;
    }

    public int getClosedContourCount(){

        return m_closedContours.size();

    }

    public Contour getClosedContour(int index){
        
        return m_closedContours.get(index);

    }

    private void getCoordinates(){
        
    }

    double m_coordinates[];

    public double[] getClosedContourPoints(int index){
        
        if(m_coordinates == null)
            m_coordinates = m_points.getPoints();
        
        Contour cnt = getClosedContour(index);
        int size = cnt.size();
        double pnt[] = new double[2*size+2];

        for(int i = 0; i <= size; i++){
            int k = cnt.get(i % size);
            //printf("i: %d k: %d\n", i,k);
            pnt[2*i] = m_coordinates[3*k];
            pnt[2*i+1] = m_coordinates[3*k+1]; 
        }            
        return pnt;
    }
    
    public int getOpenContourCount(){
        if(m_openContours == null){
            makeOpenContours();
        }
        return m_openContours.size();
    }

    public Contour getOpenContour(int index){
        if(m_openContours == null){
            makeOpenContours();
        }
        return m_openContours.get(index);
    }

    /**
       generates open contours from non closed hashtrable entries 
     */
    private void makeOpenContours(){
        m_openContours = new Vector<Contour>(m_starts.size());
        //TODO 
        //m_starts.
    }

    public double[] getPoints(){
        
        return m_points.getPoints();

    }

    public void printStat(){
        printf("***Slice.printStat()***\n");
        printf("m_newCount: %d\n", m_newCount);
        printf("m_appendCount: %d\n", m_appendCount);
        printf("m_prependCount: %d\n", m_prependCount);
        printf("m_joinCount: %d\n", m_joinCount);
        printf("m_closeCount: %d\n", m_closeCount);
        
        printf("m_starts.size(): %d\n", m_starts.size());
        printf("m_ends.size(): %d\n", m_ends.size());
        printf("m_closedContours.size(): %d\n", m_closedContours.size());        
    }


    /**
       return contour with given index
     */
    public Contour getContour(int index){

        return m_closedContours.get(index);

    }

    /**
       return coordinates of the point with given index
     */
    public void getPoint(int pointIndex, Vector3d p){
        
    }



}