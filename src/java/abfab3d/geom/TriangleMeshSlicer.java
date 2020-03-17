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
import static abfab3d.core.MathUtil.getDistance;
import static abfab3d.core.Output.printf;


/**
   calculates slices of triangle mesh by family of planes 
 */
public class TriangleMeshSlicer { 
    
    static final boolean DEBUG = false;
    static final boolean DEBUG_SLICE = false;

    int m_triCount = 0;
    int m_emptyTriCount = 0;
    int m_interTriCount = 0;


    Slice m_slices[];

    SlicingParam m_slicingParam;// = new SlicingParam();

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

        if(m_slicingParam.isAuto()) {
            
            BoundsCalculator bc = new BoundsCalculator(plane); 
            producer.getTriangles(bc);        
            m_slicingParam.setSlicesRange(bc.getMinDist(), bc.getMaxDist());

            //m_triCount = bc.triCount;
        }


        m_slices = new Slice[m_slicingParam.sliceCount];
        Vector3d slicePoint = new Vector3d();
        for(int i = 0; i < m_slices.length; i++){
            m_slicingParam.getSlicePoint(i,slicePoint);
            m_slices[i] = new Slice(m_slicingParam.normal, slicePoint, m_slicingParam.precision);
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
        TriangleSlicer triSlicer = new TriangleSlicer(m_slicingParam.sliceShift);

        SlicesCalculator  sc = new SlicesCalculator(plane1,triSlicer, m_slices);
        producer.getTriangles(sc);        
        if(DEBUG)printf("segments: %d\n", sc.intersectCount);
        
        cleanUp();

    }

    private void cleanUp(){
        
        int count = getSliceCount();
        for(int i = 0; i < count; i++){
            Slice slice = getSlice(i);
            slice.cleanUp();
        }
    }

    public boolean getSuccess(){

        int count = getSliceCount();
        
        for(int i = 0; i < count; i++){

            Slice slice = getSlice(i);
            if(!slice.getSuccess()){
                return false;
            }
        }       
        return true;
    }

    public void printStat(){

        int count = getSliceCount();
        for(int i = 0; i < count; i++){
            Slice slice = getSlice(i);
            if(!slice.getSuccess()){
                printf("slice:%d\n", i);
                slice.printStat();
            }
        }       
    }

    /**
       return count of open contours 
       non zero count may indicate a problem 
       
     */
    public int getOpenContouresCount(){

        int count = getSliceCount();
        int ocount = 0;
        for(int i = 0; i < count; i++){
            Slice slice = getSlice(i);
            ocount += slice.getOpenContourCount();
        }
        return ocount;
        
    }

    
    public void printProblems(){

        int count = getSliceCount();
        for(int i = 0; i < count; i++){
            Slice slice = getSlice(i);
            //printf("slice:%d\n", i);
            int occ = slice.getOpenContourCount();
            if(occ != 0) {
                printf("slice: %10.6f mm open:%d\n",slice.m_pointOnPlane.z/MM, occ);
                for(int c = 0; c < occ; c++){
                    double[] pnt = slice.getOpenContourPoints(c);
                    printContour(pnt);
                }
            }
        }
        
    }

    static void printContour(double pnt[]){

        double x0 = pnt[0];
        double y0 = pnt[1];
        double x1 = pnt[pnt.length-2];
        double y1 = pnt[pnt.length-1];
        double dist = getDistance(x0, y0, x1, y1);
        printf("length:%4d ends:[%10.6f,%10.6f], [%10.6f,%10.6f] dist: %10.7f mm\n", pnt.length/2, x0/MM, y0/MM, x1/MM, y1/MM, dist/MM);
        /*
        for(int i = 0; i < pnt.length/2; i++){

            printf("%10.6f,%10.6f, ", pnt[2*i]/MM,pnt[2*i+1]/MM);
            if(  ((i+1)  % 5) == 0) 
                printf("\n");                
        }
        printf("\n");
        */
    }

    public Slice getSlice(int index){
        return m_slices[index];
    }

    DistanceDataHalfSpace makeSlicingPlane(Vector3d planeNormal, Vector3d pointOnPlane){

        return new DistanceDataHalfSpace(planeNormal, pointOnPlane);
        
    }

    class BoundsCalculator implements TriangleCollector {

        private double minDist = 1.e10;
        private double maxDist = -1.e10;
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
        double getMinDist(){
            return minDist;
        }
        double getMaxDist(){
            return maxDist;
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

            boolean DEBUG_TRI = false;
            double unit = MM;
            if(false){
                double testZ = -0.002*MM;
                double EPS = 1.e-5;
                if(true){
                    //                if(abs(p0.z-testZ) < EPS || abs(p0.z-testZ) < EPS || abs(p0.z-testZ) < EPS){
                    String f = "%10.6f";
                    DEBUG_TRI = true;
                    printf("tri:%s %s %s mm dist:[%10.6f %10.6f %10.6f]mm\n", str(f,p0,unit),str(f,p1,unit),str(f,p2,unit), d0/unit, d1/unit, d2/unit);
                }
            }
            // if(DEBUG_TRI)printf("min:%8.5f max:%8.5f range:[%3d,%3d]\n", dmin, dmax, sliceIndex0, sliceIndex1);
            Vector3d q0 = new Vector3d();
            Vector3d q1 = new Vector3d();
            String format = "%9.6f";
            // cycle over potential slices 
            //if(DEBUG_SLICE)printf("sliceRange[%d, %d]\n", sliceIndex0, sliceIndex1);
            sliceIndex0 = min(slices.length-1, max(0,sliceIndex0));
            sliceIndex1 = min(slices.length-1, max(0,sliceIndex1));
            for(int i = sliceIndex0; i <= sliceIndex1; i++){
                double sliceD = i*m_slicingParam.sliceStep;
                DEBUG_TRI = false;//(sliceD == 0.);
                if(false) printf(" slice:%7.5f mm  ", sliceD/unit);
                triSlicer.setDebug(false);
                // cycle 
                int res = triSlicer.getIntersection(p0, p1, p2, d0-sliceD, d1-sliceD, d2-sliceD, q0, q1);                
                switch(res){
                default:
                    noIntersectCount++;
                    // should not happens 
                    if(false){
                        printf(" no intersection\n");
                    }
                    break;
                case TriangleSlicer.INTERSECT:
                    slices[i].addSegment(q0, q1);
                    intersectCount++;
                    if(false){
                        //double dq0 = abs(this.plane.getDistance(q0.x,q0.y,q0.z));
                        //double dq1 = abs(this.plane.getDistance(q1.x,q1.y,q1.z));
                        printf("intersect q0: %s, q1: %s mm\n", str(format, q0,unit),str(format, q1,unit));
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