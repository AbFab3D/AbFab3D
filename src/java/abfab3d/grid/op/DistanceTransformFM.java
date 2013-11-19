/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid.op;

import java.util.Arrays;

import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.GridBitIntervals;
import abfab3d.grid.SliceExporter;
import abfab3d.grid.Operation;
import abfab3d.grid.AttributeOperation;
import abfab3d.grid.ArrayAttributeGridShort;
import abfab3d.grid.GridBitIntervals;
import abfab3d.grid.ClassTraverser;
import abfab3d.grid.GridBit;
import abfab3d.util.LongConverter;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.ImageUtil.makeRGB;
import static abfab3d.util.ImageUtil.makeRGBA;
import static abfab3d.util.ImageUtil.MAXC;


import static abfab3d.grid.Grid.OUTSIDE;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

/**
 * 
 calculates Distance Transform on the given AttributeGrid to a specified distance

 uses Fast Marching Algorithm 

 voxel values are stored in grid attribute 
 the input grid is supposed to contain truncated distance data near surface layer 
 outside voxels should have values 0 
 inside voxels should have values maxAttribute  

 the surface of the shape is isosurface with value ((double)maxAttribute/2.)  

 "inside boundary voxels" are those with value >= maxAttribute/2 and have 6 neighgbour with value < maxAttribute/2
 outide neighbours of inside boundary voxels are "outside boundary voxels"
  
 output distances are normalized to maxAttribute, 
 this meand that a voxel on distance K from the surface will have distance value K*maxAttribute

 inside distances are positive
 outside distacnes are negative 
 inside voxeld we were not reached by maxInDistance are initialized to DEFAULT_IN_VALUE
 

 algorithm works as follows 

1) mark "inside boundary voxels" and "outside boundary voxels" as FIXED. Assume that they have correct boundary values 
2) for each boundary voxel mark it's 6 neighbors whcih are not FIXED as CANDIDATE. 
3) Calculate expected value of distance of each candidate from values of FIXED voxels. 

4) get CANDIDATE with minimal distance value. Mark it FIXED. Recalculate values of it's 6-neighbors, which are not yet FIXED 
   some of existing candidates my chamnhge it's values during this recalculation 
5) repeat step 4 until value of new candidate becomes larger than maxDistance 

To make selection of point with minimal distance fast we keep all CANDIDATE points in special special data structure. 
because distances are given as int and normalized to maxAttribute we may have at any given moment only maxAttribiute different distannces. 
we keep all CANDIDATE points in array SetOfVoxels candidates[maxAttribiute].  
SetOfVoxels provides fast methods to add and remove point 

 * @author Vladimir Bulatov
 */
public class DistanceTransformFM implements Operation, AttributeOperation {

    public static boolean DEBUG = false;
    static int debugCount = 0;

    static final int AXIS_X = 0, AXIS_Y = 1, AXIS_Z = 2; // direction  of surface offset from the interior surface point 
	    
    int m_maxAttribute = 255; 
    int m_maxInDistance = 0;
    int m_maxOutDistance = 0;
    int m_defaultValue = Short.MAX_VALUE;
    int nx, ny, nz;
    int m_surfaceValue;
    SliceExporter gridWriter;
    int sliceStart = 0; // slices to write (for debug) 
    int sliceEnd = 1;
    double voxelSizeX, voxelSizeY; 

    AttributeGrid m_distGrid; // grid of calculated distances 
    GridBit m_candGrid; // current candidates 
    GridBit m_fixedGrid; // fixed voxels 
    FMCandidatesPool m_candPool; // pool of candidates 

    /**
       @param maxAttribute maximal attribute value for inside voxels 
       @param maxInDistance maximal distance to calculate inside of the shape. Measured in voxels. 
       @param maxOutDistance maximal distance to calculate outsoide of the shape. Measured in voxels. 
    */
    public DistanceTransformFM(int maxAttribute, int maxInDistance, int maxOutDistance) {

        m_maxAttribute = maxAttribute; 
        m_maxInDistance = maxInDistance;
        m_maxOutDistance = maxOutDistance;
        
    }    

    public void setSliceExporter(SliceExporter writer){
        this.gridWriter = writer;
    }
    
    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid A.
     * @return new grid with distance transform data
     */
    public Grid execute(Grid grid) {
        throw new IllegalArgumentException(fmt("DistanceTransformExact.execute(%d) not implemented!\n", grid));  
    }

    
    public AttributeGrid execute(AttributeGrid grid) {

        printf("DistanceTransformFM.execute(%s)\n", grid); 
        

        m_surfaceValue = m_maxAttribute;
        nx = grid.getWidth();
        ny = grid.getHeight();
        nz = grid.getDepth();
        voxelSizeX = grid.getVoxelSize();
        voxelSizeY = grid.getSliceHeight();

        int maxDistance = max(m_maxInDistance, m_maxOutDistance);
        m_defaultValue = m_maxAttribute*maxDistance + 1;
        
        sliceStart = nz/2;
        sliceEnd = sliceStart + 1;
        if(gridWriter != null)gridWriter.writeSlices(grid, 2*m_maxAttribute, "/tmp/slices/01_grid_%03d.png", sliceStart, sliceEnd, null);

        //TODO what grid to allocate here 
        m_distGrid = new ArrayAttributeGridShort(nx, ny, nz, voxelSizeX, voxelSizeY);
        initDistances(grid, m_distGrid);        

        if(gridWriter != null)gridWriter.writeSlices(m_distGrid, m_maxAttribute, "/tmp/slices/02_init_%03d.png", sliceStart, sliceEnd, new DistanceColorizer(m_maxAttribute));

        m_fixedGrid = new GridBitIntervals(nx, ny, nz);
        scanSurface(grid, m_distGrid, m_fixedGrid);        
        if(gridWriter != null)gridWriter.writeSlices(m_distGrid, m_maxAttribute, "/tmp/slices/03_start_%03d.png", sliceStart, sliceEnd, new DistanceColorizer(m_maxAttribute));
        //if(gridWriter != null)gridWriter.writeSlices(m_distGrid, m_maxAttribute, "/tmp/slices/03_start_%03d.png", 0, nz, new DistanceColorizer(m_maxAttribute));
        if(gridWriter != null)gridWriter.writeSlices((AttributeGrid)m_fixedGrid, 0, fmt("/tmp/slices/04_fixed_%2d_%%03d.png",1), sliceStart, sliceEnd, new BitColorizer());

        m_candGrid = new GridBitIntervals(nx, ny, nz);
        m_candPool = new FMCandidatesPool(m_maxAttribute);
        
        initCandidates();        
        

        if(gridWriter != null)gridWriter.writeSlices((AttributeGrid)m_candGrid, 0, fmt("/tmp/slices/05_cand_%2d_%%03d.png",1), sliceStart, sliceEnd, new BitColorizer());
        m_candPool.printStat();
        
        for(int k = 2; k < 8; k++){
            int maxValue = k*m_maxAttribute;
            doIteration(maxValue);
            if(gridWriter != null)gridWriter.writeSlices((AttributeGrid)m_distGrid, 2*m_maxAttribute,fmt("/tmp/slices/07_dist_%02d_%%03d.png", k), sliceStart, sliceEnd, new DistanceColorizer(maxValue));
            if(gridWriter != null)gridWriter.writeSlices((AttributeGrid)m_candGrid, 0, fmt("/tmp/slices/05_cand_%2d_%%03d.png",k), sliceStart, sliceEnd, new BitColorizer());
            if(gridWriter != null)gridWriter.writeSlices((AttributeGrid)m_fixedGrid, 0, fmt("/tmp/slices/04_fixed_%2d_%%03d.png",k), sliceStart, sliceEnd, new BitColorizer());

            m_candPool.printStat();
        }

        return grid;

    }

    void doIteration(int maxValue){
        
        FMCandidate cand = new FMCandidate();
        printf("start iterations\n");
        long icount = 0;
        while( m_candPool.getNext(cand) ){
            if(cand.value > maxValue) {
                break;
            }
            int x = cand.x;
            int y = cand.y;
            int z = cand.z;
            int value = cand.value;

            icount++;

            m_fixedGrid.set(x,y,z,1);
            m_candGrid.set(x,y,z,0);
            m_distGrid.setAttribute(x,y,z,value); 

            if(m_fixedGrid.get(x+1,y,z) == 0) updateCandidate(x+1, y, z);
            if(m_fixedGrid.get(x-1,y,z) == 0) updateCandidate(x-1, y, z);
            if(m_fixedGrid.get(x,y+1,z) == 0) updateCandidate(x, y+1, z);
            if(m_fixedGrid.get(x,y-1,z) == 0) updateCandidate(x, y-1, z);
            if(m_fixedGrid.get(x,y,z+1) == 0) updateCandidate(x, y, z+1);
            if(m_fixedGrid.get(x,y,z-1) == 0) updateCandidate(x, y, z-1);
            
            //printf("cand: (%3d %3d %3d): %3d\n", cand.x,cand.y,cand.z,cand.value);
        }

        printf("end iterations icount: %d\n", icount);

    }

    /**

       set distanceGrid values to bes to -max inside and max outside 
       
    */
    void initDistances(AttributeGrid grid, AttributeGrid distanceGrid){

        long distOut = -m_defaultValue;
        long distIn = m_defaultValue;

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){

                    long att = grid.getAttribute(x,y,z);
                    if(att >= m_surfaceValue)
                        distanceGrid.setAttribute(x,y,z,distIn);
                    else 
                        distanceGrid.setAttribute(x,y,z,distOut);
                }
            }
        }        
    }

    //
    // scan the grid
    // for every iniside point near surface calculate distances of inside and outside points 
    //  for inside points distance is positive
    //  for outside points distance is negative
    //  replace distance values with ( abs(distance), distanceToPoint) it is is less than current distance 
    //
    void scanSurface(AttributeGrid grid,      // input original grid 
                     AttributeGrid distanceGrid, // output distance function 
                     GridBit fixedGrid){
        int 
            nx1 = nx-1,
            ny1 = ny-1,
            nz1 = nz-1;

        int vs = m_surfaceValue;
        double dvs = (double)vs; // to make FP calculations 
        for(int y = 1; y < ny1; y++){
            for(int x = 1; x < nx1; x++){
                for(int z = 1; z < nz1; z++){
                    int v0 = (int)grid.getAttribute(x,y,z);                    
                    int 
                        vx = v0,
                        vy = v0,
                        vz = v0;

                    if(x < nx1)vx = (int)grid.getAttribute(x+1,y,z);
                    if(y < ny1)vy = (int)grid.getAttribute(x,y+1,z);
                    if(z < nz1)vz = (int)grid.getAttribute(x,y,z+1);

                    if(v0 < vs ) {
                        // point is outside outside 
                        if((grid.getAttribute(x+1,y,z) >= vs)||
                           (grid.getAttribute(x-1,y,z) >=  vs)||
                           (grid.getAttribute(x,y+1,z) >= vs)||
                           (grid.getAttribute(x,y-1,z) >= vs)||
                           (grid.getAttribute(x,y,z+1) >= vs)||
                           (grid.getAttribute(x,y,z-1) >= vs)){
                            
                            distanceGrid.setAttribute(x,y,z,(v0-vs));                            
                            fixedGrid.set(x,y,z, 1);
                        }
                    } else {
                        // inside 
                        if((grid.getAttribute(x+1,y,z) < vs)||
                           (grid.getAttribute(x-1,y,z) < vs)||
                           (grid.getAttribute(x,y+1,z) < vs)||
                           (grid.getAttribute(x,y-1,z) < vs)||
                           (grid.getAttribute(x,y,z+1) < vs)||
                           (grid.getAttribute(x,y,z-1) < vs)){
                            
                            distanceGrid.setAttribute(x,y,z,(v0-vs)); 
                            fixedGrid.set(x,y,z, 1);
                        }
                        
                    }                    
                }
            }
        }        
    }    

    /**
       scan 6-neig of each fixed voxel and calculate candidate values of candidates 
     */
    void initCandidates(){

        int 
            nx1 = nx-1,
            ny1 = ny-1,
            nz1 = nz-1;

        long insideValue = m_defaultValue;

        for(int y = 1; y < ny1; y++){
            for(int x = 1; x < nx1; x++){
                for(int z = 1; z < nz1; z++){

                    if(m_fixedGrid.get(x,y,z) != 0){
                        
                        if(m_distGrid.getAttribute(x+1,y,z) == insideValue) makeCandidate(x+1, y, z);
                        if(m_distGrid.getAttribute(x-1,y,z) == insideValue) makeCandidate(x-1, y, z);
                        if(m_distGrid.getAttribute(x,y+1,z) == insideValue) makeCandidate(x, y+1, z);
                        if(m_distGrid.getAttribute(x,y-1,z) == insideValue) makeCandidate(x, y-1, z);
                        if(m_distGrid.getAttribute(x,y,z+1) == insideValue) makeCandidate(x, y, z+1);
                        if(m_distGrid.getAttribute(x,y,z-1) == insideValue) makeCandidate(x, y, z-1);
                    }                    
                }
            }
        }        
    }
    

    // work array to store dist data for neighbours 
    int workData[] = new int[3];

    /**
       
     */
    final void makeCandidate(int x, int y, int z){
        
        if(m_candGrid.get(x,y,z) == 1){
            // this candidate is already initialized 
            return;
        }
        m_candGrid.set(x,y,z,1);

        workData[0] = min(m_distGrid.getAttribute(x+1,y,z), m_distGrid.getAttribute(x-1,y,z));
        workData[1] = min(m_distGrid.getAttribute(x,y+1,z), m_distGrid.getAttribute(x,y-1,z));
        workData[2] = min(m_distGrid.getAttribute(x,y,z+1), m_distGrid.getAttribute(x,y,z-1));
        
        int v = getUpwindSolutionInt(workData, m_maxAttribute);
        m_candPool.add(x,y,z,v);

    }

    final void updateCandidate(int x, int y, int z){
        
        if(m_fixedGrid.get(x,y,z) == 1){
            // this candidate is in fixed grid - ignore it 
            return;
        }
        m_candGrid.set(x,y,z,1);

        workData[0] = min(m_distGrid.getAttribute(x+1,y,z), m_distGrid.getAttribute(x-1,y,z));
        workData[1] = min(m_distGrid.getAttribute(x,y+1,z), m_distGrid.getAttribute(x,y-1,z));
        workData[2] = min(m_distGrid.getAttribute(x,y,z+1), m_distGrid.getAttribute(x,y,z-1));
        
        int v = getUpwindSolutionInt(workData, m_maxAttribute);

        m_candPool.update(x,y,z,v);

    }

    //
    // checks point for grid boundaries 
    //
    final boolean inGrid(int x, int y, int z){

        return (x >= 0 && y >= 0 && z >= 0 && x < nx && y < ny && z < nz);
    }

    /**
       makes signed int from short stored as 2 low bytes in long 
     */
    static final int L2S(long v){
        return (int)((short)v);
    }

    /**
       solves equation P(x - a0)^2 + P(x-a1)^2 + P(x-a2)^2 = h^2
       P(x) = x for x> 0 and 0 for x <= 0
       the solution is as follows 
       sort elements in accenting order 
       
     */
    public static final int getUpwindSolutionInt(int a[], int h) {
        
        Arrays.sort(a);
        int x = a[0] + h;
        // check if (x - a0)^2 = h^2 works 
        if(x <= a[1])
            return x;
        // check if (x - a0)^2 +(x-a1)^2 = h^2 works 
        //2 x^2 - 2(a0+a1)x + a0^2 + a1^2 - h^2 = 0;
        // x = ((a0+a1) + sqrt((a0+a1)^2 - 2 (a0^2 + a1^2 - h^2)))/2        
        int a01 = a[0]+a[1];
        int a01_2 = a[0]*a[0] + a[1]*a[1];
        int h2 = h*h;
        x = (int)round((a01 + sqrt(a01*a01 - 2*(a01_2 - h2)))/2);

        if(x <= a[2])
            return x;
        // (x - a0)^2 +(x-a1)^2 + (x-a2)^2 = h^2 should work
        //3 x^2 - 2(a0+a1+a2)x + a0^2 + a1^2 + a2^2 - h^2 = 0;
        //x = ((a0+a1+a2) + sqrt((a0+a1+a2)^2 - 3 (a0^2 + a1^2 + a2^2 - h^2)))/3        
        int a012 = a01+a[2];
        int a012_2 = a01_2 + a[2]*a[2];
        return (int)round((a012 + sqrt(a012*a012 - 3*(a012_2 - h2)))/3);
    }
    
    public static final double getUpwindSolution(double a[], double h) {
        
        Arrays.sort(a);
        double x = a[0] + h;
        // check if (x - a0)^2 = h^2 works 
        if(x <= a[1])
            return x;
        // check if (x - a0)^2 +(x-a1)^2 = h^2 works 
        //2 x^2 - 2(a0+a1)x + a0^2 + a1^2 - h^2 = 0;
        // x = ((a0+a1) + sqrt((a0+a1)^2 - 2 (a0^2 + a1^2 - h^2)))/2        
        double a01 = a[0]+a[1];
        double a01_2 = a[0]*a[0] + a[1]*a[1];
        double h2 = h*h;
        x = ((a01 + sqrt(a01*a01 - 2*(a01_2 - h2)))/2);

        if(x <= a[2])
            return x;
        // (x - a0)^2 +(x-a1)^2 + (x-a2)^2 = h^2 should work
        //3 x^2 - 2(a0+a1+a2)x + a0^2 + a1^2 + a2^2 - h^2 = 0;
        //x = ((a0+a1+a2) + sqrt((a0+a1+a2)^2 - 3 (a0^2 + a1^2 + a2^2 - h^2)))/3        
        double a012 = a01+a[2];
        double a012_2 = a01_2 + a[2]*a[2];
        return ((a012 + sqrt(a012*a012 - 3*(a012_2 - h2)))/3);
    }

    public static final int P(int x){
        return (x > 0)? x: 0;
    }

    public static final double P(double x){
        return (x > 0.)? x: 0.;
    }

    static final int min(long a, long b){
        return (a <= b)? (int)a: (int)b;
    }

    static final int max(long a, long b){
        return (a >= b)? (int)a: (int)b;
    }

    /**
       
       return  P(x - a0)^2 + P(x-a1)^2 + P(x-a2)^2 - h^2
       
    */
    public static int checkUpwindSolution(int x, int a[], int h) {
        int d0 = P(x - a[0]);
        int d1 = P(x - a[1]);
        int d2 = P(x - a[2]);
        return d0 * d0 + d1*d1 + d2*d2 - h*h;
        
    }

    public static double checkUpwindSolution(double x, double  a[], double  h) {
        double d0 = P(x - a[0]);
        double d1 = P(x - a[1]);
        double d2 = P(x - a[2]);
        return d0 * d0 + d1*d1 + d2*d2 - h*h;
        
    }

    class DistanceColorizer implements LongConverter {

        int maxAtt = 100;
        DistanceColorizer(int maxAtt){
            this.maxAtt = maxAtt;
        }
        public long get(long value){
            if(value == m_defaultValue) {
                return makeRGB(MAXC, 0,0);
            } else if(value == -m_defaultValue) {
                return makeRGB(0,0,MAXC); 
            }

            if( value >= 0){
                int v = (int)(MAXC  - (value * MAXC / maxAtt) & MAXC); 
                return makeRGB(v, v, v);
            } else {
                return COLOR_WHITE;
                //value = -value;
                //int v = (int)(MAXC - (value * MAXC / maxAtt) & MAXC);                
                //return makeRGB(v, v, v);
                
            }
        }
    }    

    class BitColorizer implements LongConverter {
        
        public long get(long value){
            if(value != 0)
                return COLOR_BLACK;
            else 
                return COLOR_TRANSPARENT;
        }
        
    }

    static final int COLOR_WHITE = makeRGB(MAXC,MAXC,MAXC);
    static final int COLOR_BLACK = makeRGB(0,0,0);
    static final int COLOR_TRANSPARENT = makeRGBA(0,0,0,0);
    
}
