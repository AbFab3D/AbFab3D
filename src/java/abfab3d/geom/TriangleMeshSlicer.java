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
import abfab3d.mesh.AreaCalculator;



import javax.vecmath.Vector3d;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.ceil;
import static java.lang.Math.min;
import static java.lang.Math.max;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Units.CM3;
import static abfab3d.core.Units.CM2;
import static abfab3d.core.MathUtil.str;
import static abfab3d.core.MathUtil.getDistance;
import static abfab3d.core.Output.printf;


/**
   calculates slices of triangle mesh by family of planes 
 */
public class TriangleMeshSlicer { 
    
    static final boolean DEBUG = false;

    static final boolean DEBUG_SLICE = false;

    boolean m_printStat = false;

    int m_segmentsCount = 0;
    int m_triCount = 0;
    int m_emptyTriCount = 0;
    int m_interTriCount = 0;
    // which alg to use 
    int m_sliceVersion = 2; 

    double m_minSliceArea;
    double m_maxSliceArea;
    double m_slicesVolume;
    double m_meshVolume;
    int m_failedSliceCount;
    
    Slice m_slices[];

    AreaCalculator m_areaCalculator;

    SlicingParam m_slicingParam;// = new SlicingParam();


    public TriangleMeshSlicer(){
        
    }

    public TriangleMeshSlicer(SlicingParam slicingParam){

        m_slicingParam = slicingParam;
        
    }


    public int getTriCount(){
        return m_triCount;
    }

    public int getSegmentsCount(){
        return m_segmentsCount;
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

        //if(DEBUG) printf("TriangleMeshSlicer.makeSlices()\n");
        
        DistanceDataHalfSpace plane = makeSlicingPlane(m_slicingParam.normal, new Vector3d(0,0,0));

        // auto find the slicing bounds 

        if(m_slicingParam.isAuto()) {
            
            BoundsCalculator bc = new BoundsCalculator(plane); 
            producer.getTriangles(bc);        
            m_slicingParam.setSlicesRange(bc.getMinDist(), bc.getMaxDist());

            //m_triCount = bc.triCount;
        }

        m_areaCalculator = new AreaCalculator();
        m_slices = new Slice[m_slicingParam.sliceCount];
        Vector3d slicePoint = new Vector3d();
        for(int i = 0; i < m_slices.length; i++){
            m_slicingParam.getSlicePoint(i,slicePoint);
            switch(m_sliceVersion){
            default: 
            case 1:
                m_slices[i] = new SliceV1(m_slicingParam.normal, slicePoint, m_slicingParam.precision);
                break;
            case 2: 
                m_slices[i] = new SliceV2(m_slicingParam.normal, slicePoint, m_slicingParam.precision);
                break;
            }
            if(m_slicingParam.getSliceOptimization()){
                m_slices[i].setOptimizer(new ContourOptimizer(m_slicingParam.getSegmentPrecision(), m_slicingParam.getMaxSegmentsCount()));
            }
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

        m_segmentsCount = calcSegmentsCount();
        
        if(m_printStat){
            NumberStat stat = calcSegmentsStat();        
            stat.printStat();
        }

    }
    
    private NumberStat calcSegmentsStat(){

        int count = getSliceCount();
        int segCount = 0;

        NumberStat stat = new NumberStat(0., 2*MM, 20);
        
        for(int i = 0; i < count; i++){
            Slice slice = getSlice(i);
            int ccount = slice.getClosedContourCount();
            for(int c = 0;  c < ccount; c++){

                Contour contour = slice.getClosedContour(c);
                int csize = contour.size();
                int start = contour.get(csize-1);

                Vector3d p1 = slice.getPoint(start, null);

                for(int p = 0; p < csize; p++){
                    int end = contour.get(p);
                    Vector3d p2 = slice.getPoint(end, null);                    
                    double slen = getDistance(p1, p2);   
                    p1.set(p2);
                    stat.add(slen);
                }
            }
            
        }

        return stat;

        
    }

    private int calcSegmentsCount(){

        int count = getSliceCount();
        int segCount = 0;

        for(int i = 0; i < count; i++){
            Slice slice = getSlice(i);
            int ccount = slice.getClosedContourCount();
            for(int c = 0;  c < ccount; c++){

                Contour contour = slice.getClosedContour(c);
                int csize = contour.size();
                segCount += csize;
            }
            
        }
        return segCount;
    }

    private void cleanUp(){
        
        int count = getSliceCount();
        for(int i = 0; i < count; i++){
            Slice slice = getSlice(i);
            slice.buildContours();
        }
    }

    public boolean getSuccess(){

        int count = getSliceCount();
        int failedSliceCount = 0;

        for(int i = 0; i < count; i++){

            Slice slice = getSlice(i);
            if(!slice.getSuccess()){
                failedSliceCount++;
            }
        }    

        m_failedSliceCount = failedSliceCount;

        m_slicesVolume = getSlicesVolume();
        m_meshVolume = getMeshVolume();

        // Print out result if slicing failed
        if(DEBUG && m_failedSliceCount > 0 && m_minSliceArea < 0.){
            printf("TriangleMeshSlicer.getSuccess() failed results\n");
            printf("meshVolume: %10.5f cm^3\n", m_meshVolume/CM3);
            printf("slicesVolume: %10.5f cm^3\n", m_slicesVolume/CM3);
            printf("maxSliceArea: %10.5f cm^2\n", m_maxSliceArea/CM2);
            printf("minSliceArea: %10.5f cm^2\n", m_minSliceArea/CM2);
            printf("volumeDifference: %10.5f cm^3\n", (m_meshVolume-m_slicesVolume)/CM3);
            printf("maxSliceVolume: %10.5f cm^3\n", (m_maxSliceArea* m_slicingParam.sliceStep)/CM3);
        }

        if(m_failedSliceCount > 0) {
            printf("TriangelMeshSlicer: no zero failedSliceCount: %d\n", failedSliceCount);
            return false;
        }
        if(m_minSliceArea < 0.) {
            printf("TriangelMeshSlicer: got negative minSliceArea: %12.5f cm2\n", m_minSliceArea/CM2);
            return false;
        }
        
        return true;
    }

    /**
       @return valume calculated via trinagle mesh
     */
    public double getMeshVolume(){
        
        return m_areaCalculator.getVolume();

    }


    /**
       @return volume calculated from slices 
     */
    public double getSlicesVolume(){
        
        int count = getSliceCount();
        double slicesVolume = 0;
        m_minSliceArea = Double.MAX_VALUE;
        m_maxSliceArea = -Double.MAX_VALUE;

        for(int i = 0; i < count; i++){

            Slice slice = getSlice(i);
            int ccount= slice.getClosedContourCount(); 
            double sliceArea = 0;

            for(int k = 0; k < ccount; k++){                    
                double pnt[];
                pnt = slice.getClosedContourPoints(k);
                sliceArea += getContourArea(pnt);
            }                          
  
            m_minSliceArea = min(m_minSliceArea, sliceArea);
            m_maxSliceArea = max(m_maxSliceArea, sliceArea);

            slicesVolume += sliceArea;
        }

        return slicesVolume * m_slicingParam.sliceStep;

    }

    
    /**
       calculates area of single contour. 
       area of CCW contour is positiuve, area of CW contour is negative 
       assumes the contour is closed and last point is equal to the first point 

     */
    public static double getContourArea(double pnt[]){

        int cnt = pnt.length/2;
        double area = 0;
        // calculates sum of areas of individual triangles 
        for(int i = 0; i < cnt-1; i++){
            int i2 = 2*i;
            double x0 = pnt[i2];
            double y0 = pnt[i2+1];
            double x1 = pnt[i2+2];
            double y1 = pnt[i2+3];
            area += x0*y1 - x1*y0;
        }
        
        return area/2;
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

                printf("slice: %10.6f mm open:%d\n",slice.getPointOnPlane().z/MM, occ);
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

    /**
       class calculating mesh bounbs
     */
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

    /**
       class claculating slices for each incoming triangle 
     */
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

            m_areaCalculator.addTri(p0, p1, p2);

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
                //double testZ = -0.002*MM;
                //double EPS = 1.e-5;
                //                if(abs(p0.z-testZ) < EPS || abs(p0.z-testZ) < EPS || abs(p0.z-testZ) < EPS){
                String f = "%10.6f";
                DEBUG_TRI = true;
                printf("tri:%s %s %s mm dist:[%10.6f %10.6f %10.6f]mm\n", str(f,p0,unit),str(f,p1,unit),str(f,p2,unit), d0/unit, d1/unit, d2/unit);
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