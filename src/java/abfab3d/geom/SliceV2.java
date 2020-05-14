/***************************************************************************
 * 
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
import static abfab3d.core.Units.TODEGREE;
import static abfab3d.core.MathUtil.str;
import static java.lang.Math.*;


/**
   represents single slice 
   coordinates of all points are rounded to the given tolerance 
   this effectively snaps all slice coordinates to vertices of a grid with cell size equal to tolarance 

 */
public class SliceV2 implements Slice {
    
    static final boolean DEBUG = false;
    static final double EPSILON =1.e-8;
    static final double DEFAULT_CLOSING_TOLERANCE = 1.e-6;
    
    Vector3d m_normal = new Vector3d();
    Vector3d m_pointOnPlane = new Vector3d();

    // raw array of segments in order of appearance 

    Vector<Segment> m_segments = new Vector<Segment>();
    // 
    Vertices m_vertices = new Vertices();

    int m_contourCount = 0;

    //PointMap m_points; // all points stored as coordinates 
    //PointMap3 m_points; // all points stored as coordinates 
    IPointMap m_points; // all points stored as coordinates 
    Vector<Contour> m_closedContours = new Vector<Contour>();
    Vector<Contour> m_openContours = null;

    //
    int m_appendCount = 0;
    int m_prependCount = 0;
    int m_joinCount = 0;
    int m_closeCount = 0;
    int m_newCount = 0;
    double m_tolerance = EPSILON;
    double m_closingTolerance = m_tolerance*5;


    public SliceV2(){
        this(new Vector3d(0,0,1),new Vector3d(0,0,0),EPSILON);
    }

    public SliceV2(Vector3d normal, Vector3d pointOnPlane){        
        this(normal, pointOnPlane,EPSILON);
    }

    public SliceV2(Vector3d normal, Vector3d pointOnPlane, double epsilon){

        m_normal.set(normal);
        m_tolerance = epsilon;
        m_pointOnPlane.set(pointOnPlane);        
        if(epsilon > 0) {
            m_points = new PointMap2(3, 0.75, epsilon);
        }  else {
            m_points = new PointMap3(3, 0.75);            
        }
        m_closingTolerance = Math.max(5*m_tolerance, DEFAULT_CLOSING_TOLERANCE);

    }
    
    public double getSliceDistance(){

        return m_normal.dot(m_pointOnPlane);

    }
    
    public Vector3d getPointOnPlane(){
        return m_pointOnPlane;
    }

    /**
       add new segment to the slice 
     */
    public void addSegment(Vector3d p0, Vector3d p1){

        final boolean debug = false;

        int ind0 = m_points.add(p0.x,p0.y,p0.z);
        int ind1 = m_points.add(p1.x,p1.y,p1.z);
        if(ind0 == ind1){
            // both ends map to the same point - ignore segment 
            return;
        } 
        if(debug)printf("new segment: [%3d %3d]\n", ind0, ind1);
        Segment segment = new Segment(ind0, ind1, m_segments.size());
        Vertex v0 = m_vertices.get(ind0);
        Vertex v1 = m_vertices.get(ind1);
        v0.addOutSegment(segment);
        v1.addInSegment(segment);
        m_segments.add(segment);

    }


    /**
       final clean up to close open contours 
     */
    public void buildContours(){
        

        final boolean debug = false;

        if(debug)printf("buildContours()\n");

        initCoordinates();
        StringBuffer sb = new StringBuffer();
        m_vertices.toSB(sb);        
        if(debug)printf(sb.toString());        
        for(int is = 0; is < m_segments.size(); is++){
            
            Segment s = m_segments.get(is);
            if(s != null) {
                
                m_segments.set(s.index, null);
                Contour cont = buildContour(s);
                if(cont.getStart() == cont.getEnd()){                
                    m_closedContours.add(cont);
                    if(debug)printf("closed contour: %s\n", cont.toString());
                } else {
                    if(m_openContours == null) 
                        m_openContours = new Vector<Contour>(2);
                    m_openContours.add(cont);
                    if(debug)printf("open contour: %s\n", cont.toString());
                }
            }
        }
              
    }
    
    /**
       build single contour starting from given segment 
       
     */
    private Contour buildContour(Segment segment){

        final boolean debug = false;
        if(debug)printf("buildCountour([%d, %d])\n", segment.start, segment.end);
        Contour contour = new Contour(segment.start, segment.end);
        
        Segment curSegment = segment;
        int startVertexIndex = curSegment.start;
        Vertex startVertex = m_vertices.get(startVertexIndex);
        startVertex.removeOutSegment(curSegment);

        int curVertexIndex = curSegment.end;
        Vertex curVertex = m_vertices.get(curVertexIndex);
        curVertex.removeInSegment(curSegment);  

        // do while contour is closed or next segment not found
        while(true){

            int outSegCount =  curVertex.outSegmentCount();
            if(debug)printf("  currentVertexIndex: %d\n", curVertexIndex);

            double maxAngle = -Math.PI;            
            int nextSegmentIndex = -1;

            for(int i = 0; i < outSegCount; i++){

                Segment seg = curVertex.getOutSegment(i); 
                if(debug)printf("    seg:[%d, %d]\n", seg.start, seg.end);                
                double nextAngle = getContourAngle(curSegment.start, curSegment.end, seg.end);
                if(debug)printf("    nextAngle:%5.1f\n", nextAngle*TODEGREE);
                if(nextAngle > maxAngle) {
                    maxAngle = nextAngle;
                    nextSegmentIndex = i;                    
                }
            }

            if(nextSegmentIndex >= 0){

                Segment seg = curVertex.getOutSegment(nextSegmentIndex); 
                if(debug)printf("    adding vertex:%d\n", seg.end);                 
                // remove segment from current vertex 
                curVertex.removeOutSegment(seg);
                // remove segment from the array 
                m_segments.set(seg.index, null);
                curSegment = seg;
                contour.append(seg.end);
                curVertexIndex = seg.end;
                curVertex = m_vertices.get(curVertexIndex);
                curVertex.removeInSegment(seg);

            } else {            

                if(debug)printf("    next segment not found, vertexIndex:%d segment:[%d, %d]\n", curVertexIndex, curSegment.start, curSegment.end);
                if(debug)printf("return from buildContour()\n");
                return contour;

            }
            
            if(curVertexIndex == startVertexIndex){ // have contour closed?
                
                if(debug)printf("closing contour and return from buildContour()\n");
                contour.close();
                return contour;
            }
        }

    }

    /**
       @return true if input segments form manifold 

       input is manifold if each vertex has equal count of IN and OUT segments 
       
     */
    public boolean testManifold(){
        
        if(DEBUG) printf("testManifold()\n");

        int vertCount = 0;
        int badVertCount = 0;

        for(int i = 0; i < m_vertices.size(); i++){
            Vertex v = m_vertices.get(i);
            if(v != null){
                vertCount++;
                int inCount = v.inSegmentCount();
                int outCount = v.outSegmentCount();

                badVertCount += (inCount != outCount)? 1:0;
            }
        }

        if(DEBUG) printf("  vertCount:%d\n", vertCount);
        if(DEBUG) printf("  badVertCount:%d\n", badVertCount);

        return (badVertCount == 0);

    }

    /**
       
     */
    public boolean getSuccess(){
        
        return (getOpenContourCount() == 0);

    }

    public int getSegmentCount(){
        return m_segments.size();
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
        printf("printStat()\n");

        printf(" slice: %s mm\n", fmt(f,m_pointOnPlane.z/MM));

        int segCount = 0;
        int inSegCount = 0;
        int outSegCount = 0;
        int vertCount = 0;
        int badVertCount = 0;

        for(int i = 0; i < m_segments.size(); i++){
            if(m_segments.get(i) != null) 
                segCount++;
        }

        for(int i = 0; i < m_vertices.size(); i++){
            Vertex v = m_vertices.get(i);
            if(v != null){
                vertCount++;
                int inCount = v.inSegmentCount();
                int outCount = v.outSegmentCount();

                inSegCount += inCount;
                outSegCount += outCount;
                badVertCount += (inCount != outCount)? 1:0;
            }
        }

        printf("  vertCount:%d\n", vertCount);
        printf("  badVertCount:%d\n", badVertCount);
        printf("  segCount:%d\n", segCount);
        printf("  inSegCount:%d\n", inSegCount);
        printf("  outSegCount:%d\n", outSegCount);
        
        printf("  closedContours: %d\n", getClosedContourCount());        
        printf("  openContours: %d\n", getOpenContourCount());
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
    private void getPoint(int pointIndex, Vector3d p){

        int off = pointIndex*3;
        p.x = m_coordinates[off];
        p.y = m_coordinates[off+1];
        p.z = m_coordinates[off+2];

    }
   


    Vector3d 
        p0 = new Vector3d(),
        p1 = new Vector3d(),
        p2 = new Vector3d();

    
    /**
       return angle between vectors (p0, p1) and (p1, p2)
     */
    double getContourAngle(int i0, int i1, int i2){
        
        getPoint(i0, p0);
        getPoint(i1, p1);
        getPoint(i2, p2);
        
        p2.sub(p1);
        p1.sub(p0);
        
        p2.normalize();
        p1.normalize();
        double dot = p1.dot(p2);

        p0.cross(p1, p2);
        double cross = p0.length();

        return Math.atan2(cross, dot);



    }
    

} // class SliceV2