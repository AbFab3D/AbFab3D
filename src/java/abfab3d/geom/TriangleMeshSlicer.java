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
    
    static final boolean DEBUG = false;
    static final boolean DEBUG_SLICE = true;

    int m_triCount = 0;
    int m_emptyTriCount = 0;
    int m_interTriCount = 0;


    Slice m_slices[];

    SlicingParam m_slicingParam = new SlicingParam();

    public TriangleMeshSlicer(){
        
    }

    public TriangleMeshSlicer(SlicingParam slicingParam){

        m_slicingParam = slicingParam;
    }

    /*
    public void setSliceStep(double sliceStep){

        m_sliceStep = sliceStep;

    }
    public void setSliceNormal(Vector3d normal){

        m_sliceNormal.set(normal);
        m_sliceNormal.normalize(); 

    }
    */

    public int getTriCount(){
        return m_triCount;
    }

    public int getEmptyTriCount(){
        return m_emptyTriCount;
    }

    public int getInterTriCount(){
        return m_interTriCount;
    }

    public int getSliceCount(){
        return m_slicingParam.sliceCount;
    }

    DistanceDataHalfSpace m_plane;

    
    /**
       perform slicing of the mesh 
     */
    public void makeSlices(TriangleProducer producer){

        if(DEBUG) printf("TriangleMeshSlicer.makeSlices()\n");
               
        
        DistanceDataHalfSpace plane = makeSlicingPlane(m_slicingParam.normal, new Vector3d(0,0,0));


        // auto find the slicing bounds 

        if(m_slicingParam.isAuto) {
            
            BoundsCalculator bc = new BoundsCalculator(plane); 
            producer.getTriangles(bc);        
            m_slicingParam.setSlicesRange(bc.minDist, bc.maxDist);

            //m_triCount = bc.triCount;
        }


        m_slices = new Slice[m_slicingParam.sliceCount];
        Vector3d slicePoint = new Vector3d();
        for(int i = 0; i < m_slices.length; i++){
            m_slicingParam.getSlicePoint(i,slicePoint);
            m_slices[i] = new Slice(m_slicingParam.normal, slicePoint);
        }
        
        DistanceDataHalfSpace plane1 = makeSlicingPlane(m_slicingParam.normal, m_slicingParam.firstSliceLocation);

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
        
        /**
           process mesh triangle
         */
        public boolean addTri(Vector3d p0,Vector3d p1,Vector3d p2){

            m_triCount++;
            double d0 = plane.getDistance(p0.x,p0.y,p0.z);
            double d1 = plane.getDistance(p1.x,p1.y,p1.z);
            double d2 = plane.getDistance(p2.x,p2.y,p2.z);

            double dmin = min3(d0,d1,d2);
            double dmax = max3(d0,d1,d2);
            int sliceIndex0 = (int)floor(dmin/m_slicingParam.sliceStep);
            int sliceIndex1 = (int)ceil(dmax/m_slicingParam.sliceStep);
            if(sliceIndex0 >= slices.length || sliceIndex1 < 0){
                m_emptyTriCount++;
                return true;
            } else {
                m_interTriCount++;
            }
            double testX = 9.9*MM;
            boolean DEBUG_TRI = false;

            /*
            if(p0.x > testX || p1.x > testX || p2.x > testX){
                String f = "%7.6f";
                DEBUG_TRI = true;
                printf("tri:%s %s %s dist:[%8.5f %8.5f %8.5f]\n", str(f,p0),str(f,p1),str(f,p2), d0, d1, d2);
            }
             */
            if(DEBUG_TRI)printf("min:%8.5f max:%8.5f range:[%3d,%3d]\n", dmin, dmax, sliceIndex0, sliceIndex1);
            Vector3d q0 = new Vector3d();
            Vector3d q1 = new Vector3d();
            String format = "%7.3f";
            // cycle over potential slices 
            //if(DEBUG_SLICE)printf("sliceRange[%d, %d]\n", sliceIndex0, sliceIndex1);
            sliceIndex0 = min(slices.length-1, max(0,sliceIndex0));
            sliceIndex1 = min(slices.length-1, max(0,sliceIndex1));
            triSlicer.setDebug(DEBUG_TRI);
            for(int i = sliceIndex0; i <= sliceIndex1; i++){
                double sliceD = i*m_slicingParam.sliceStep;
                if(DEBUG_TRI) printf(" slice:%7.5f  ", sliceD);
                // cycle 
                int res = triSlicer.getIntersection(p0, p1, p2, d0-sliceD, d1-sliceD, d2-sliceD, q0, q1);                
                switch(res){
                default:
                    noIntersectCount++;
                    // should not happens 
                    if(DEBUG_TRI){
                        printf(" no intersection\n");
                    }
                    break;
                case TriangleSlicer.INTERSECT:
                    slices[i].addSegment(q0, q1);
                    intersectCount++;
                    if(DEBUG_TRI){
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
    } //     class SlicesCalculator 



    static final double min3(double x,double y,double z){
        return min(x,min(y,z));
    }

    static final double max3(double x,double y,double z){
        return max(x,max(y,z));
    }

}