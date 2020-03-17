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

import abfab3d.util.IPointMap;
//import abfab3d.util.PointMap;
import abfab3d.util.PointMap2;
import abfab3d.util.PointMap3;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;
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

    //PointMap m_points; // all points stored as coordinates 
    //PointMap3 m_points; // all points stored as coordinates 
    IPointMap m_points; // all points stored as coordinates 
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
    double m_tolerance = EPSILON;

    public Slice(){
        this(new Vector3d(0,0,1),new Vector3d(0,0,0),EPSILON);
    }

    public Slice(Vector3d normal, Vector3d pointOnPlane){        
        this(normal, pointOnPlane,EPSILON);
    }

    public Slice(Vector3d normal, Vector3d pointOnPlane, double epsilon){

        m_normal.set(normal);
        m_tolerance = epsilon;
        m_pointOnPlane.set(pointOnPlane);        
        if(epsilon > 0) 
            m_points = new PointMap2(3, 0.75, epsilon);
        else 
            m_points = new PointMap3(3, 0.75);

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
        if(ind0 == ind1){
            // both ends map to the same point - ignore segment 
            return;
        }
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
            // only start exists
            m_appendCount++;
            c0.append(ind1);
            m_ends.remove(seg0);
            m_ends.put(seg1, c0);
            break;

        case 2: 
            // only end  exists
            m_prependCount++;
            c1.prepend(ind0);
            m_starts.remove(seg1);
            m_starts.put(seg0, c1);
            break;

        case 3: 
            // both exist -> join contours 
            if(c0 != c1){

                // different contours, join them together 
                m_joinCount++;
                c0.append(c1);
                m_ends.remove(seg0);
                m_starts.remove(seg1);
                m_ends.put(new Integer(c0.getEnd()),c0);

            } else {
                // two ends of the same contour => close it 
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

    /**
       final clean up to close open contours 
     */
    public void cleanUp(){
                
        //m_openContours = new Vector<Contour>(max(m_ends.size(), m_starts.size())); 
        if(m_starts.size() == 0 && m_ends.size() == 0) 
            return;

        HashMap<Integer,Contour> cnt = (m_starts.size() >= m_ends.size()) ? m_starts:m_ends;
        
        Vector<Contour> openContours = new Vector<Contour>();
        for (Integer key: cnt.keySet()) {
            
            Contour c = cnt.get(key);
            if(c.get(0) == c.get(c.size()-1)){
                m_closedContours.add(c);
            } else {
                //printf("starts: %d-> %d\n", c.get(0), c.get(c.size()-1));
                openContours.add(c);
            }
        }
        
        initCoordinates();

        if(openContours.size() != 0) {
            checkOpenContours(openContours, m_tolerance*5);  
            joinOpenContours(openContours, m_tolerance*5, m_closedContours);  
            //m_openContours = openContours;
        }
              
    }

    /**
       
     */
    public boolean getSuccess(){

        return (getOpenContourCount() == 0);
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

    double m_coordinates[];

    private void initCoordinates(){
        
        if(m_coordinates == null)
            m_coordinates = m_points.getPoints();
    }


    public double[] getClosedContourPoints(int index){
        
        initCoordinates();
        
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

    public double[] getOpenContourPoints(int index){
        
        initCoordinates();
        
        Contour cnt = getOpenContour(index);
        int size = cnt.size();
        double pnt[] = new double[2*size];

        for(int i = 0; i < size; i++){
            int k = cnt.get(i % size);
            pnt[2*i] = m_coordinates[3*k];
            pnt[2*i+1] = m_coordinates[3*k+1]; 
        }            
        return pnt;
    }
    
    public int getOpenContourCount(){
    
        return (m_openContours != null)? m_openContours.size():0;

    }

    public Contour getOpenContour(int index){

        return (m_openContours != null) ? m_openContours.get(index):null;
    }


    public double[] getPoints(){
        
        return m_points.getPoints();

    }

    public void printStat(){

        String f = "%26.22f";
        printf(" m_pointOnPlane:%s mm\n", fmt(f,m_pointOnPlane.z/MM));
        //printf(" m_newCount: %d\n", m_newCount);
        //printf(" m_appendCount: %d\n", m_appendCount);
        //printf(" m_prependCount: %d\n", m_prependCount);
        //printf(" m_joinCount: %d\n", m_joinCount);
        //printf(" m_closeCount: %d\n", m_closeCount);
        //if(m_starts.size() != 0 || m_ends.size() != 0){
        //    printf(" ***problem!!!  m_starts: %d m_ends: %d\n", m_starts.size(),m_ends.size());
        //}
        printf(" closedContours: %d\n", getClosedContourCount());        
        printf(" openContours: %d\n", getOpenContourCount());
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

        int off = pointIndex*3;
        p.x = m_coordinates[off];
        p.y = m_coordinates[off+1];
        p.z = m_coordinates[off+2];

    }
    
    /**
       chack if open contours form a chain 
     */
    private void checkOpenContours(Vector<Contour> contours, double precision){

        final boolean debug = false;
        Vector3d endPoint = new Vector3d();
        Vector3d startPoint = new Vector3d();
        String f = "%10.8f";
        
        if(debug)printf("checkOpenContours(%d, %10.8f mm)\n", contours.size(), precision/MM);

        for(int i = 0; i < contours.size(); i++){
            Contour cs = contours.get(i);
            getPoint(cs.getEnd(), endPoint);
            for(int j = 0; j < contours.size(); j++){
                Contour ce = contours.get(j);
                getPoint(ce.getStart(), startPoint); 
                if(startPoint.epsilonEquals(endPoint, precision)){
                    if(debug)printf("%d -> %d (%s -> %s)\n", i, j, str(f, endPoint, MM), str(f,startPoint, MM));
                }
            }
        }
    }

    /**
       join contours which are open,
       adds closed contours to the m_closedContours
       and return remaining open contours 
    */
    private  Vector<Contour> joinOpenContours(Vector<Contour> contours, double precision, Vector<Contour> closedContours){

        final boolean debug =  false;
        if(debug)printf("joinOpenContours(%d, %10.8f mm)\n", contours.size(), precision/MM);
        IPointMap points = (precision > 0)? new PointMap2(3, 0.75, precision): new PointMap3(3, 0.75); 
        HashMap<Integer,Contour> starts = new HashMap<Integer,Contour>();
        HashMap<Integer,Contour> ends = new HashMap<Integer,Contour>();
        Vector3d 
            p0 = new Vector3d(), 
            p1 = new Vector3d();
        String f = "%12.10f";
        for(int i = 0; i < contours.size(); i++){

            Contour cont = contours.get(i);
            if(debug)printf("i: %d cont.size: %d\n", i, cont.size());

            getPoint(new Integer(cont.getStart()),  p0);
            getPoint(new Integer(cont.getEnd()) , p1);
            if(debug)printf("p0: %s p1: %s\n", str(f, p0), str(f, p1) );
            
            int ind0 = points.add(p0.x,p0.y,p0.z);
            int ind1 = points.add(p1.x,p1.y,p1.z);
            if(debug)printf("ind0:%d ind1: %d\n", ind0, ind1);
            if(ind0 == ind1){
                if(debug) printf("self closing contour\n");
                // both ends map to the same point - add contour to the closed contours 
                cont.close();
                closedContours.add(cont); 
                continue;                
            } 

            // ends are different 
            Integer seg0 = new Integer(ind0);
            Integer seg1 = new Integer(ind1);
            
            Contour c0 = ends.get(seg0);
            Contour c1 = starts.get(seg1);
            
            int cc = ((c0 != null)?1:0) | ((c1 != null)? 2:0);
            
            if(debug)printf("case: %d\n", cc);
            switch(cc){
                
            case 0:
                if(debug) printf("new contour\n");
                // both ends are new points
                // new contour 
                starts.put(seg0, cont);
                ends.put(seg1, cont);
                break;
                
            case 1: 
                if(debug) printf("start exist - prepend\n");
                // only start exists
                c0.append(cont);
                ends.remove(seg0);
                ends.put(seg1, c0);
                break;
                
            case 2: 
                if(debug) printf("end exist - prepend\n");
                // only end  exists
                c1.prepend(cont);
                starts.remove(seg1);
                starts.put(seg0, c1);
                break;
                
            case 3: 
                // both start and end exist -> join contours 
                if(c0 != c1){
                    // segment points belongs to different contours, join (c0 + cont + c1) together into contour c0
                    c0.append(cont);
                    c0.append(c1);
                    
                    ends.remove(seg0);
                    starts.remove(seg1);
                    // get index of end of joined contour 
                    getPoint(new Integer(c0.getEnd()) , p1);
                    ends.put(new Integer(points.add(p1.x,p1.y,p1.z)),c0); // 
                    if(debug) printf("ends connect to different contours - joining contours\n");
                    
                } else {
                    // both ends belongs to the same contour => close it and store in closed contours 
                    c0.append(cont);
                    c0.close();
                    ends.remove(seg0);
                    starts.remove(seg1);
                    closedContours.add(c0); 
                    if(debug) printf("ends connect to the same contour - join and closing contour\n");
                }  
                break;
                
            }            
        }
        
        if(debug) printf("open contour count: %d %d\n", starts.size(), ends.size());
        if((starts.size() | ends.size()) != 0){
            // few pieces remain
            printf("failed to joing contours");
        }
        //
        // return remaining open contours 
        return null;
    }


} // class Slice 