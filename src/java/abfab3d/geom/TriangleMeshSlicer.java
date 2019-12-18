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

import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;
import abfab3d.distance.DistanceDataHalfSpace;



import javax.vecmath.Vector3d;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.ceil;
import static java.lang.Math.min;
import static java.lang.Math.max;
import static abfab3d.core.Units.MM;
import static abfab3d.core.MathUtil.str;
import static abfab3d.core.Output.printf;


/**
   calculates slices of triangle mesh by family of planes 
 */
public class TriangleMeshSlicer { 
    
    static final boolean DEBUG = true;
    static final boolean DEBUG_SLICE = false;

    Vector3d m_sliceNormal = new Vector3d(0,0,1);
    double m_units = MM;
    String m_unitsName = "mm";
    double m_sliceStep = 1*MM;//0.1*MM;
    int m_sliceCount = 0;
    int m_triCount = 0;

    Slice m_slices[];

    public TriangleMeshSlicer(){
        
    }
    
    public void setSliceStep(double sliceStep){

        m_sliceStep = sliceStep;

    }
    public void setSliceNormal(Vector3d normal){

        m_sliceNormal.set(normal);
        m_sliceNormal.normalize(); 

    }

    public int getTriCount(){
        return m_triCount;
    }

    public int getSliceCount(){
        return m_sliceCount;
    }

    DistanceDataHalfSpace m_plane;

    
    public void makeSlices(TriangleProducer producer){

        if(DEBUG) printf("TriangleMeshSlicer.makeSlices()\n");
               
        
        // find the slices bounds 
        DistanceDataHalfSpace plane = makeSlicingPlane(m_sliceNormal, new Vector3d(0,0,0));
        BoundsCalculator bc = new BoundsCalculator(plane); 

        producer.getTriangles(bc);

        double minSlice = m_sliceStep*Math.floor(bc.minDist/m_sliceStep);
        double maxSlice = m_sliceStep*Math.floor(bc.maxDist/m_sliceStep);
        m_sliceCount =  (int)Math.ceil((maxSlice -  minSlice)/m_sliceStep)+1;
        m_triCount = bc.triCount;


        if(DEBUG){
            
            printf("minDist: %6.3f %s\n", minSlice/m_units, m_unitsName);
            printf("maxDist: %6.3f %s\n", maxSlice/m_units, m_unitsName);            
        }

        Vector3d minSlicePoint = new Vector3d(m_sliceNormal);
        minSlicePoint.scale(minSlice);

        m_slices = new Slice[m_sliceCount];

        for(int i = 0; i < m_slices.length; i++){
            Vector3d slicePoint = new Vector3d(m_sliceNormal);
            slicePoint.scale(minSlice + i*m_sliceStep);
            m_slices[i] = new Slice(m_sliceNormal, slicePoint);
        }
        
        DistanceDataHalfSpace plane1 = makeSlicingPlane(m_sliceNormal, minSlicePoint);
        /*
        BoundsCalculator bc1 = new BoundsCalculator(plane1); 
        producer.getTriangles(bc1);
        if(DEBUG){            
            printf("minDist1: %6.3f %s\n", bc1.minDist/m_units, m_unitsName);
            printf("maxDist1: %6.3f %s\n", bc1.maxDist/m_units, m_unitsName);
            
        }                        
        */
        TriangleSlicer triSlicer = new TriangleSlicer();
        SlicesCalculator  sc = new SlicesCalculator(plane1,triSlicer, m_slices);
        producer.getTriangles(sc);        
        if(DEBUG)printf("segments: %d\n", sc.intersectCount);

    }

    public Slice getSlice(int index){
        return m_slices[index];
    }

    DistanceDataHalfSpace makeSlicingPlane(Vector3d planeNormal, Vector3d pointOnPlane){

        return new DistanceDataHalfSpace(planeNormal, pointOnPlane);
        
    }

    class BoundsCalculator implements TriangleCollector {

        double minDist = 1.e10;
        double maxDist = -1.e10;
        DistanceDataHalfSpace plane;
        int triCount = 0;

        BoundsCalculator(DistanceDataHalfSpace plane){
            this.plane = plane;
        }

        public boolean addTri(Vector3d p0,Vector3d p1,Vector3d p2){
            triCount++;

            double d0 = plane.getDistance(p0.x,p0.y,p0.z);
            double d1 = plane.getDistance(p1.x,p1.y,p1.z);
            double d2 = plane.getDistance(p2.x,p2.y,p2.z);
            minDist = min(d0, minDist);
            minDist = min(d1, minDist);
            minDist = min(d2, minDist);
            maxDist = max(d0, maxDist);
            maxDist = max(d1, maxDist);
            maxDist = max(d2, maxDist);            
            return true;
        }
    }

    
    class SlicesCalculator implements TriangleCollector {

        // plane of first slice 
        DistanceDataHalfSpace plane;
        // triangle slicer 
        TriangleSlicer triSlicer; 
        int noIntersectCount = 0;
        int intersectCount = 0;
        Slice slices[];


        SlicesCalculator(DistanceDataHalfSpace plane, TriangleSlicer triSlicer, Slice slices[]){

            this.plane = plane;
            this.triSlicer = triSlicer;
            this.slices = slices;
        }

        public boolean addTri(Vector3d p0,Vector3d p1,Vector3d p2){
            
            double d0 = plane.getDistance(p0.x,p0.y,p0.z);
            double d1 = plane.getDistance(p1.x,p1.y,p1.z);
            double d2 = plane.getDistance(p2.x,p2.y,p2.z);

            double dmin = min3(d0,d1,d2);
            double dmax = max3(d0,d1,d2);
            int sliceIndex0 = (int)ceil(dmin/m_sliceStep);
            int sliceIndex1 = (int)floor(dmax/m_sliceStep);
            //if(DEBUG)printf("d0:%8.3e d1:%8.3e d2:%8.3e\n", d0, d1, d2);
            Vector3d q0 = new Vector3d();
            Vector3d q1 = new Vector3d();
            String format = "%7.3f";
            // cycle over potential slices 
            if(DEBUG_SLICE)printf("sliceRange[%d, %d]\n", sliceIndex0, sliceIndex1);
            for(int i = sliceIndex0; i <= sliceIndex1; i++){
                double sliceD = i*m_sliceStep;
                // cycle 
                int res = triSlicer.getIntersection(p0, p1, p2, d0-sliceD, d1-sliceD, d2-sliceD, q0, q1);                
                switch(res){
                default:
                    noIntersectCount++;
                    // should ot happens 
                    if(DEBUG_SLICE){
                        printf("no intersection\n");
                    }
                    break;
                case TriangleSlicer.INTERSECT:
                    slices[i].addSegment(q0, q1);
                    intersectCount++;
                    if(DEBUG_SLICE){
                        //double dq0 = abs(this.plane.getDistance(q0.x,q0.y,q0.z));
                        //double dq1 = abs(this.plane.getDistance(q1.x,q1.y,q1.z));
                        printf("intersect q0: %s, q1: %s\n", str(format, q0),str(format, q1));
                    }
                 }
               
            }
                                                     
            //int res = triSlicer.getIntersection(p0, p1, p2, d0, d1, d2, q0, q1);
            //String format = "%8.5f";
            
            return true;
        }
    }

    static final double min3(double x,double y,double z){
        return min(x,min(y,z));
    }

    static final double max3(double x,double y,double z){
        return max(x,max(y,z));
    }

}