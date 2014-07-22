/** 
 *                        Shapeways, Inc Copyright (c) 2014
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;

import java.util.concurrent.atomic.AtomicInteger;

import javax.vecmath.Tuple3d; 
import javax.vecmath.Point3d; 

import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;

import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Operation;
import abfab3d.grid.AttributeOperation;
import abfab3d.grid.GridBit;
import abfab3d.grid.GridMask;
import abfab3d.grid.GridBitIntervals;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.VectorIndexer;
import abfab3d.grid.VectorIndexerArray;
import abfab3d.grid.ArrayInt;


import abfab3d.grid.util.ExecutionStoppedException;

import abfab3d.util.PointSet;
import abfab3d.geom.PointCloud;

import abfab3d.transforms.Identity;

import static java.lang.Math.sqrt;
import static java.lang.Math.max;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static abfab3d.util.Output.time;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.MathUtil.L2S;


/**
   class fills grid with values of signed distance to a point set 
   signed distance is negative inside of the object, positive outisde of the object and zero at points on surface of the shape    
   if signed distance is needed, the inside/outside tester should be supplied

   @author Vladimir Bulatov
 */
public class DistanceToPointSet implements Operation, AttributeOperation {
    
    int m_algorithm = ALG_LAYERED;

    static public final int ALG_EXACT = 1; // straightforward exact calculation
    static public final int ALG_LAYERED = 2; // building distance in layers 

    static final boolean DEBUG = false;
    static final boolean DEBUG_GRID = false;
    static final boolean DEBUG_TIMING = true;
    int m_debugCount = 200;
    int m_subvoxelResolution = 100;
    int defaultInValue = -Short.MAX_VALUE;
    int defaultOutValue = Short.MAX_VALUE;
    
    double m_firstLayerThickness = 2.45;// 2.0, 2.25, 2.45*, 2.84, 3.0 3.17 3.33*, 3.46, 3.62, 3.74*   * - good values 
    double m_nextLayerThickness = 2.45; 
//
    int m_sliceHeight = 0; // to be initialized 

    InsideTester m_insideTester;
    PointSet m_points;

    private double m_voxelSize;
    // grid bounds 
    private double m_bounds[] = new double[6];
    // grid sizes
    private int m_nx, m_ny, m_nz;
    // coefficients of convesion from world coord to grid coord 
    private double m_gs,m_gtx,m_gty,m_gtz;
    AttributeGrid m_grid;
    
    double m_maxInDistance;
    double m_maxOutDistance;
    //int m_neighbors[]; // spherical neighbors 
    int m_maxOutDistSubvoxels;
    int m_maxInDistSubvoxels;

    boolean m_initializeGrid = false;
    int m_threadCount = 1;
    int m_processingDirection = 0;

    // vector indexer template used to store indices to neares points
    VectorIndexer m_vectorIndexerTemplate = new VectorIndexerArray(1,1,1);

    /**
     @param inDistance maximal distance to calculate transform inside of the shape. Measured in meters
     @param outDistance maximal distance to calculate transform outside of the shape. Measured in meters
     */
    public DistanceToPointSet(PointSet points, double inDistance, double outDistance, int subvoxelResolution){
        m_points = points;
        m_subvoxelResolution = subvoxelResolution;
        m_maxInDistance = inDistance;
        m_maxOutDistance = outDistance;
        
    }
    
    /**
       sets object to be used for inside/outside detection
       it is needed if we want to calculate signed distance function
     */
    public void setInsideTester(InsideTester tester){
        m_insideTester = tester;
    }

    /**
       if this flag is ON the distance grid will initialized with default values inside and outside
       if this flag is OFF the calculation assumes, that the grid is correctly initialized 
       this can be used to add additional points to the existing caoculated grid 
     */
    public void setInitializeGrid(boolean value){
        m_initializeGrid = value;
    }

    /**
       set order of y-layers processing (for tests)
     */
    public void setProcessingDirection(int value){
        m_processingDirection = value;
    }

    public void setNextLayerThickness(double value){
        m_nextLayerThickness = value;
    }

    public void setFirstLayerThickness(double value){
        m_firstLayerThickness = value;
    }


    /**
       sets template to be used for VectorIndexer 
     */
    public void setVectorIndexerTemplate(VectorIndexer vectorIndexerTemplate){

        m_vectorIndexerTemplate = vectorIndexerTemplate;

        if(m_vectorIndexerTemplate == null)
            m_vectorIndexerTemplate = new VectorIndexerArray(1,1,1);
    }

    public void setAlgorithm(int algorithm){
        m_algorithm = algorithm;
    }

    public void setThreadCount(int threadCount){
        m_threadCount = threadCount;
    }

    public Grid execute(Grid grid) {
        makeDistanceGrid((AttributeGrid)grid);
        return grid;
    }
    
    public AttributeGrid execute(AttributeGrid grid) {
        makeDistanceGrid(grid);
        return grid;
    }
    
    void commonInit(AttributeGrid grid){

        m_grid = grid;
        grid.getGridBounds(m_bounds);
        m_nx = grid.getWidth();
        m_ny = grid.getHeight();
        m_nz = grid.getDepth();
        
        double vs = (m_bounds[1] - m_bounds[0])/m_nx;
        m_voxelSize = vs;

        // scale is isotropic 
        m_gs = 1/vs;

        m_gtx = -m_bounds[0]/vs - 0.5; // half voxel shift 
        m_gty = -m_bounds[2]/vs - 0.5;
        m_gtz = -m_bounds[4]/vs - 0.5;
        
        //double maxOut = m_maxOutDistance/m_voxelSize;
        //double maxIn = m_maxInDistance/m_voxelSize;
        m_maxOutDistSubvoxels = (int)Math.ceil(m_maxOutDistance*m_subvoxelResolution/m_voxelSize);
        m_maxInDistSubvoxels = (int)Math.ceil(m_maxInDistance*m_subvoxelResolution/m_voxelSize);

        if(DEBUG){
            printf("maxOutDist: %d subvoxels\n",m_maxOutDistSubvoxels);
            printf("maxInDist: %d subvoxels\n",m_maxInDistSubvoxels);
        }
        
        
    }

    public void makeDistanceGrid(AttributeGrid grid){
        if(DEBUG) printf("makeDistanceGrid(%s)\n",grid);

        commonInit(grid);

        if(m_initializeGrid)
            fillInOut();
        switch(m_algorithm){
        default: 
        case ALG_EXACT: 
            makeDistanceExact();
            break;
        case ALG_LAYERED: 
            makeDistanceLayered();
            break;
        }
    }

    /**
       calculate distacnes using exact algorithm 
     */
    void makeDistanceExact(){

        if(DEBUG)printf("makeDistanceExact()\n");
        double maxDistVoxels = max(m_maxOutDistance, m_maxInDistance)/m_voxelSize;
        int neig[] = Neighborhood.makeBall((int)Math.ceil(maxDistVoxels)+2);
        if(DEBUG)printf("neighbors count: %d\n",neig.length/3);
        int count = m_points.size();
        Point3d pnt = new Point3d();

        int kmax = neig.length;

        for(int i = 0; i < count; i++){

            m_points.getPoint(i, pnt);
            getGridCoord(pnt);
            int 
                cx = iround(pnt.x),
                cy = iround(pnt.y),
                cz = iround(pnt.z);

            for(int k = 0; k < kmax; k += 3){

                int 
                    ix = cx+neig[k],
                    iy = cy+neig[k+1],
                    iz = cz+neig[k+2];
                if(!isInsideGrid(ix,iy,iz))
                    continue;

                int dist = distance(pnt.x,pnt.y,pnt.z,ix,iy,iz);


                int d = L2S(m_grid.getAttribute(ix, iy, iz));
                if(d >=0){                    
                    if(dist > m_maxOutDistSubvoxels)
                        continue;
                    // outside 
                    if(dist < d){
                        m_grid.setAttribute(ix, iy, iz, dist);
                    }
                } else {
                    // inside 
                    if(dist > m_maxInDistSubvoxels)
                        continue;
                    if(dist < -d){
                        m_grid.setAttribute(ix, iy, iz, -dist);
                    }
                }
            }
        }
    }

    /**
       build distance using layred algorithm 
       
     */
    void makeDistanceLayered(){

        if(DEBUG) printf("makeDistanceLayered()\n");
        m_sliceHeight = (int)m_nextLayerThickness;

        //
        // closestPoints grid keeps has index of the the closest point from the PointSet to the given voxel 
        //
        VectorIndexer closestPoints = m_vectorIndexerTemplate.createEmpty(m_nx, m_ny, m_nz);
        //fillGrid(closestPoints, -1);
        //GridBit freshLayer = new GridBitIntervals(m_nx, m_ny, m_nz);
        //GridBit nextLayer = new GridBitIntervals(m_nx, m_ny, m_nz);
        GridBit freshLayer = new GridMask(m_nx, m_ny, m_nz);
        GridBit nextLayer = new GridMask(m_nx, m_ny, m_nz);
        
        double nextLayerThickness = m_nextLayerThickness;
        double firstLayerThickness = m_firstLayerThickness;
        if(DEBUG)printf("firstLayerThickness: %4.2f nextLayerThickness: %4.2f\n", firstLayerThickness, nextLayerThickness);


        int firstNeig[] = Neighborhood.makeBall(firstLayerThickness);
        int nextNeig[] = Neighborhood.makeBall(nextLayerThickness);


        if(DEBUG) printf("first neig count: %d\n", firstNeig.length/3);
        if(DEBUG) printf("next neig count: %d\n", nextNeig.length/3);
        //if(DEBUG) printNeighbors(nextNeig, (int)Math.ceil(nextLayerThickness));

        double maxDistVoxels = max(m_maxOutDistance, m_maxInDistance)/m_voxelSize;

        int iter = (int)Math.ceil((maxDistVoxels - firstLayerThickness)/Math.floor(nextLayerThickness));
        if(DEBUG) printf("iter count: %d\n", iter);
        
        // 1) make fresh layer around PointSet         
        // 2) for each point in fresh layer make fresh layer around PointSet 
        if(DEBUG)printf("first layer\n");
        int setCount = 0;
        long t0 = time();
        if(m_threadCount > 1 ) {
            //m_threadCount = 1;
            setCount = makeFirstLayerMT(m_points, firstNeig, closestPoints, freshLayer);         
            //setCount = makeFirstLayerST(m_points, firstNeig, closestPoints, freshLayer);        
        } else {
            setCount = makeFirstLayerST(m_points, firstNeig, closestPoints, freshLayer);        
        }

        if(DEBUG_TIMING)printf("fist layer set count: %6d %6d ms\n", setCount, time() - t0);
        
        if(DEBUG_GRID){
            printf("distance after first layer:\n");
            printSlice(m_grid,m_nz/2);
            printf("freshLayer after first layer:\n");
            printSlice(freshLayer,m_nx, m_ny, m_nz/2);
            printf("closest points after first layer:\n");
            printSlice(closestPoints,m_nx, m_ny, m_nz, m_nz/2);
        }
        
        //m_threadCount = 1;
        if(DEBUG)printf("threads: %d, iterations: %d\n", m_threadCount, iter);
        long t00 = time();
        for(int k = 0; k < iter; k++){

            //int setCount = makeNextLayer(k, nextNeig, closestPoints, freshLayer, nextLayer);            
            t0 = time();
            if(m_threadCount > 1 ) 
                setCount = makeNextLayerSlicesMT(k, nextNeig, closestPoints, freshLayer, nextLayer);    
            else 
                setCount = makeNextLayerSlicesST(k, nextNeig, closestPoints, freshLayer, nextLayer);             
            if(DEBUG_TIMING)printf("iter:%d set count: %6d %6d ms\n", (k+1), setCount, time() - t0);
            //if(setCount == 0) break;
            if(DEBUG_GRID){
                printf("distance after next layer:\n");
                printSlice(m_grid,m_nz/2);
                printf("fresh layer after next layer:\n");
                printSlice(freshLayer,m_nx, m_ny, m_nz/2);
                printf("next layer after next layer:\n");
                printSlice(nextLayer,m_nx, m_ny, m_nz/2);
                printf("closest points after next layer:\n");
                printSlice(closestPoints,m_nx, m_ny, m_nz, m_nz/2);
            }             
            GridBit t = freshLayer;
            freshLayer = nextLayer;
            nextLayer = t;
            nextLayer.clear();
        }
        if(DEBUG_TIMING) printf("layers done: %6d ms\n",(time() - t00));
    }

    /**
       process single slice for the first layer 
       points are assumed to be pre-sorted to be the slice 
       @param inds holds indices of points in the original array 
       it is used by MT version when ecah thread processes separate layers
     */
    int makeFirstLayerSlice(int ymin, int ymax, PointSet points, ArrayInt inds, int neig[], VectorIndexer closestPoints, GridBit freshLayer){

        int kmax = neig.length;
        int count = points.size();
        Point3d pnt = new Point3d();
        int distCalcCount = 0, distSetCount = 0;

        for(int pntIndex = 0; pntIndex < count; pntIndex++){

            points.getPoint(pntIndex, pnt);
            getGridCoord(pnt);
            int 
                cx = iround(pnt.x),
                cy = iround(pnt.y),
                cz = iround(pnt.z);

            if( cy < ymin || cy >= ymax) {
                // should not happens 
                printf("point in wrong slice: %d is outside [%d %d]\n", cy, ymin, ymax);
                continue;
            }

            for(int k = 0; k < kmax; k += 3){

                int 
                    ix = cx+neig[k],
                    iy = cy+neig[k+1],
                    iz = cz+neig[k+2];

                if(!isInsideGrid(ix,iy,iz))
                    continue;

                int dist = distance(pnt.x,pnt.y,pnt.z,ix,iy,iz);
                distCalcCount++;
                int d = L2S(m_grid.getAttribute(ix, iy, iz));

                if(d >=0){// outside 
                    if(dist > m_maxOutDistSubvoxels)
                        continue;
                    if(dist < d){
                        m_grid.setAttribute(ix, iy, iz, dist);
                        closestPoints.set(ix, iy, iz, inds.get(pntIndex));
                        freshLayer.set(ix, iy, iz, 1);
                        distSetCount++;
                    }
                } else { // d < 0 - inside 
                    if(dist > m_maxInDistSubvoxels)
                        continue;
                    if(dist < -d){
                        m_grid.setAttribute(ix, iy, iz, -dist);
                        closestPoints.set(ix, iy, iz, inds.get(pntIndex));
                        freshLayer.set(ix, iy, iz, 1);
                        distSetCount++;
                    }
                }
            }
        }
        return distSetCount;
    }

    /**
       calculates first fresh layer around original points, ST version
     */
    int makeFirstLayerST(PointSet points, int neig[], VectorIndexer closestPoints, GridBit freshLayer){

        long t0 = 0;
        if(DEBUG) t0 = time();
        int kmax = neig.length;
        int count = points.size();
        Point3d pnt = new Point3d();
        int distCalcCount = 0, distSetCount = 0;

        for(int pntIndex = 0; pntIndex < count; pntIndex++){

            points.getPoint(pntIndex, pnt);
            getGridCoord(pnt);
            int 
                cx = iround(pnt.x),
                cy = iround(pnt.y),
                cz = iround(pnt.z);

            for(int k = 0; k < kmax; k += 3){

                int 
                    ix = cx+neig[k],
                    iy = cy+neig[k+1],
                    iz = cz+neig[k+2];
                if(!isInsideGrid(ix,iy,iz))
                    continue;

                int dist = distance(pnt.x,pnt.y,pnt.z,ix,iy,iz);
                distCalcCount++;
                int d = L2S(m_grid.getAttribute(ix, iy, iz));

                if(d >=0){// outside 
                    if(dist > m_maxOutDistSubvoxels)
                        continue;
                    if(dist < d){
                        m_grid.setAttribute(ix, iy, iz, dist);
                        closestPoints.set(ix, iy, iz, pntIndex);
                        freshLayer.set(ix, iy, iz, 1);
                        distSetCount++;
                    }
                } else { // d < 0 - inside 
                    if(dist > m_maxInDistSubvoxels)
                        continue;
                    if(dist < -d){
                        m_grid.setAttribute(ix, iy, iz, -dist);
                        closestPoints.set(ix, iy, iz, pntIndex);
                        freshLayer.set(ix, iy, iz, 1);
                        distSetCount++;
                    }
                }
            }
        }
        if(DEBUG)printf("firstLayerST: calc count: %7d set count: %7d time: %5d ms\n", distCalcCount, distSetCount, (time() - t0));
        return distSetCount;
    }

    int makeFirstLayerMT(PointSet points, int neig[], VectorIndexer closestPoints, GridBit freshLayer){
        
        long t0 = 0;
        if(DEBUG) t0 = time();
        if(DEBUG) printf("makeFirstLayerMT() thread count: %d\n", m_threadCount);
        // split points into separate slices 
        DistanceToPointSet.SliceManager sliceManager = new DistanceToPointSet.SliceManager(m_ny, m_sliceHeight);
        Slice slices[] = sliceManager.getSlices();
        int pcount = points.size();

        PointCloud pnts[] = new PointCloud[slices.length]; // points split between slices 
        ArrayInt inds[] = new ArrayInt[slices.length];    // original indices of points  

        int sliceCount = (m_ny + m_sliceHeight-1)/ m_sliceHeight;
        
        Point3d pnt = new Point3d();
        int pointsPerSlice = (pcount+sliceCount-1)/sliceCount;

        for(int pntIndex = 0; pntIndex < pcount; pntIndex++){
            
            points.getPoint(pntIndex, pnt);
            // y-coord of point in voxels 
            int cy = iround(m_gs * pnt.y + m_gty);
            int sliceIndex = cy/m_sliceHeight;
            if(sliceIndex >= 0 && sliceIndex < sliceCount){
                if(pnts[sliceIndex] == null){
                    pnts[sliceIndex] = new PointCloud(pointsPerSlice);
                    inds[sliceIndex] = new ArrayInt(pointsPerSlice);
                }
                pnts[sliceIndex].addPoint(pnt.x,pnt.y,pnt.z);
                inds[sliceIndex].add(pntIndex);
                
            } else {
                printf("point outside of slices: %d\n", cy);
            }
        }
        if(DEBUG)printf("points sorting time: %5d ms\n", (time() - t0));
        if(DEBUG){
            for(int k = 0; k < pnts.length; k++){
                if(pnts[k] != null)printf("pnt: %5d\n", pnts[k].size());
                else printf("pnt: NULL\n");
            }
        }

        // process each slice separately by pool of threads 
        ExecutorService executor = Executors.newFixedThreadPool(m_threadCount);
        AtomicInteger sliceCounter = new AtomicInteger(0);
        for(int i = 0; i < m_threadCount; i++){

            SliceProcessorFirst sliceProcessor = new SliceProcessorFirst(neig, closestPoints, freshLayer, sliceCounter, sliceManager, pnts, inds);            
            executor.submit(sliceProcessor);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int count = sliceCounter.intValue();

        if(DEBUG)printf("makeFirstLayerMT: set count: %7d time: %5d ms\n", count, (time() - t0));
        return count;
    }


    int makeNextLayerSlice(int iteration, int ymin, int ymax, int neig[], VectorIndexer closestPoints, GridBit oldLayer, GridBit freshLayer){
        long t0 = 0;
        if(DEBUG) t0 = time();
        int kmax = neig.length;
        Point3d pnt = new Point3d();
        int distCalcCount = 0, distSetCount = 0;
        int maxInDistSubvoxels = m_maxInDistSubvoxels;
        int maxOutDistSubvoxels = m_maxOutDistSubvoxels;
        int maxLayerDistSubvoxels = (int)((iteration + 2.5) * m_sliceHeight * m_subvoxelResolution);

        // for each point old layer build ball neighborhood 
        // and update distances in grid point 
        // distance in each point s calculated to the closest boundary point stored in closestPoints
        //
        //if(z == m_nz/2)printf("%2d %2d %2d: %2d\n", ix,iy,iz, dist);
        for(int y = ymin; y < ymax; y++){
            for(int x = 0; x < m_nx; x++){
                for(int z = 0; z < m_nz; z++){
                    if(oldLayer.get(x,y,z) == 0) continue; // empty point 
                    int pntIndex = closestPoints.get(x,y,z);
                    m_points.getPoint(pntIndex, pnt);
                    getGridCoord(pnt);
                    for(int k = 0; k < kmax; k += 3){                        
                        int 
                            ix = x+neig[k],
                            iy = y+neig[k+1],
                            iz = z+neig[k+2];
                        if(!isInsideGrid(ix,iy,iz))
                            continue;
                        //if(oldLayer.get(ix,iy,iz) == 1) continue; // point in old layer
                        int dist = distance(pnt.x,pnt.y,pnt.z,ix,iy,iz);
                        //if(dist > maxLayerDistSubvoxels) continue;                            
                        if(z == m_nz/2 && dist == -1 && m_debugCount-- > 0)printf("(%2d %2d %2d) -> (%2d %2d %2d)-(%5.2f %5.2f %5.2f) %d\n", x,y,z,ix,iy,iz, pnt.x, pnt.y, pnt.z, dist);
                        distCalcCount++;
                        int d = L2S(m_grid.getAttribute(ix, iy, iz));
                        if(d >=0){
                            // outside 
                            if(dist > maxOutDistSubvoxels)
                                continue;
                            if(dist < d){
                                m_grid.setAttribute(ix, iy, iz, dist);
                                closestPoints.set(ix, iy, iz, pntIndex);
                                freshLayer.set(ix, iy, iz, 1);
                                distSetCount++;
                            }
                        } else { // d < 0 inside 
                            if(dist > maxInDistSubvoxels)
                                continue;
                            if(dist < -d){
                                m_grid.setAttribute(ix, iy, iz, -dist);
                                closestPoints.set(ix, iy, iz, pntIndex);
                                freshLayer.set(ix, iy, iz, 1);
                                distSetCount++;
                            }
                        }                        
                    }                        
                }
            }
        }        
        return distSetCount;
    }

    /**
       do calculations on series of slices stacked along y-axis 
     */
    int makeNextLayerSlicesMT(int iteration, int neig[], VectorIndexer closestPoints, GridBit oldLayer, GridBit freshLayer){

        DistanceToPointSet.SliceManager sliceManager = new DistanceToPointSet.SliceManager(m_ny, m_sliceHeight);
        
        ExecutorService executor = Executors.newFixedThreadPool(m_threadCount);
        AtomicInteger sliceCounter = new AtomicInteger(0);

        for(int i = 0; i < m_threadCount; i++){

            SliceProcessorNext sliceProcessor = new SliceProcessorNext(iteration, neig, closestPoints, oldLayer, freshLayer, sliceCounter, sliceManager);            
            executor.submit(sliceProcessor);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int count = sliceCounter.intValue();
        
        return count;
    }

    /**
       do calculations on series of slices stacked along y-axis 
     */
    int makeNextLayerSlicesST(int iteration, int neig[], VectorIndexer closestPoints, GridBit oldLayer, GridBit freshLayer){
        long t0 = time();
        int sliceHeight = 3;
        int sliceCount = (m_ny + sliceHeight-1) / sliceHeight;
        int count = 0;
        for(int s = 0; s < sliceCount; s++){            
            int ymin;
            switch(m_processingDirection){
            default: 
            case 0: ymin = s*sliceHeight; break;
            case 1: ymin = (sliceCount - s - 1)*sliceHeight; break;
            }
            int ymax = ymin + sliceHeight;
            if(ymax > m_ny) ymax = m_ny;
            count += makeNextLayerSlice(iteration, ymin, ymax, neig, closestPoints, oldLayer, freshLayer);            
        }
        //if(DEBUG)printf("ST iter: %3d, count: %7d time: %5d ms\n", iteration, count, (time() - t0));
        return count;            
    }


    int makeNextLayer(int iteration, PointSet points, int neig[], VectorIndexer closestPoints, GridBit oldLayer, GridBit freshLayer){

        long t0 = 0;
        if(DEBUG) t0 = time();
        int kmax = neig.length;
        int count = points.size();
        Point3d pnt = new Point3d();
        int distCalcCount = 0, distSetCount = 0;
        int maxInDistSubvoxels = m_maxInDistSubvoxels;
        int maxOutDistSubvoxels = m_maxOutDistSubvoxels;

        // for each point old layer build ball neighborhood 
        // and update distances in grid point 
        // distance in each point s calculated to the closest boundary point stored in closestPoints
        //
        for(int y = 0; y < m_ny; y++){
            for(int x = 0; x < m_nx; x++){
                for(int z = 0; z < m_nz; z++){
                    if(oldLayer.get(x,y,z) == 0) continue; // empty point 
                    int pntIndex = closestPoints.get(x,y,z);
                    points.getPoint(pntIndex, pnt);
                    getGridCoord(pnt);
                    for(int k = 0; k < kmax; k += 3){                        
                        int 
                            ix = x+neig[k],
                            iy = y+neig[k+1],
                            iz = z+neig[k+2];
                        if(!isInsideGrid(ix,iy,iz))
                            continue;
                        int dist = distance(pnt.x,pnt.y,pnt.z,ix,iy,iz);
                        distCalcCount++;
                        int d = L2S(m_grid.getAttribute(ix, iy, iz));
                        if(d >=0){
                            // outside 
                            if(dist > maxOutDistSubvoxels)
                                continue;
                            if(dist <= d){
                                m_grid.setAttribute(ix, iy, iz, dist);
                                closestPoints.set(ix, iy, iz, pntIndex);
                                freshLayer.set(ix, iy, iz, 1);
                                distSetCount++;
                            }
                        } else { // d < 0 inside 
                            if(dist > maxInDistSubvoxels)
                                continue;
                            if(dist <= -d){
                                m_grid.setAttribute(ix, iy, iz, -dist);
                                closestPoints.set(ix, iy, iz, pntIndex);
                                freshLayer.set(ix, iy, iz, 1);
                                distSetCount++;
                            }
                        }                        
                    }                        
                }
            }
        }
        if(DEBUG)printf("iter: %3d, calc count: %7d set count: %7d time: %5d ms\n", iteration, distCalcCount, distSetCount, (time() - t0));
        return distSetCount;
    }

    final boolean isInsideGrid(int x, int y, int z){

        return (x >= 0) && (y >= 0) && (z >= 0) && (x < m_nx) && (y < m_ny) && (z < m_nz); 

    }

    /*
      calculates distance between 2 points in subvoxel units
    */
    final int distance(double x,double y, double z, int x0, int y0, int z0){
        
        x -= x0; 
        y -= y0; 
        z -= z0; 
        
        return iround(sqrt(x*x + y*y + z*z)*m_subvoxelResolution);
        
    }

    static final int iabs(int x){
        return (x < 0)? (-x): x;
    }
        
    static final int ifloor(double x){
        if(x >= 0)
            return (int)x;
        else 
            return (int)x - 1;            
    }

    static final int iround(double x){
        x += 0.5;
        if(x >= 0)
            return (int)x;
        else 
            return (int)x - 1;            
    }

    void fillInOut(){

        if(DEBUG) printf("fillInOut start\n");
        for(int y = 0; y < m_ny; y++){
            for(int x = 0; x < m_nx; x++){
                for(int z = 0; z < m_nz; z++){
                    if(m_insideTester != null){
                        if(m_insideTester.isInside(x,y,z))
                            m_grid.setAttribute(x,y,z,defaultInValue);
                        else 
                            m_grid.setAttribute(x,y,z,defaultOutValue);
                    } else { // no tester - default outside 
                        m_grid.setAttribute(x,y,z,defaultOutValue);                        
                    }                        
                }
            }
        }
        if(DEBUG) printf("fillInOut done\n");
    }


    /**
       convert point world coordinates into grid coordinates (in voxels)
     */
    void getGridCoord(Tuple3d pnt){

        pnt.x = m_gs * pnt.x + m_gtx;
        pnt.y = m_gs * pnt.y + m_gty;
        pnt.z = m_gs * pnt.z + m_gtz;

    }

    static void printNeighbors(int neig[], int rmax){

        int s = 2*rmax+1;
        GridBit gb = new GridMask(s,s,s);        
        for(int k =0; k < neig.length/3; k++){
            gb.set(rmax + neig[3*k],rmax + neig[3*k+1],rmax + neig[3*k+2],1);
        }
        for(int z =0; z < s; z++){
            for(int y =0; y < s; y++){
                for(int x =0; x < s; x++){
                    if(gb.get(x,y,z) == 1)
                        printf("+");
                    else 
                        printf(".");
                }
                printf("\n");
            }
            printf("----------\n");        
        }
    }

    static void fillGrid(AttributeGrid grid, int value){
        int 
            nx = grid.getWidth(), 
            ny = grid.getHeight(),
            nz = grid.getDepth();        
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    grid.setAttribute(x,y,z,value);
                }
            }
        }
    }

    /**
       print slice fo grid 
     */
    static void printSlice(AttributeGrid grid, int z){
        int 
            nx = grid.getWidth(), 
            ny = grid.getHeight(),
            nz = grid.getDepth();

        printf("grid:[ %d x %d x %d] slice %d\n",nx,ny,nz,z);
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                int d = L2S(grid.getAttribute(x,y,z));
                switch(d){
                case Short.MAX_VALUE: printf("    +"); break;
                case -Short.MAX_VALUE: printf("    -"); break;
                default:printf("%5d", d); break;
                }
            }
            printf("\n");
        }
    }

    /**
       prints slice fo GridBit
     */
    static void printSlice(GridBit grid, int nx, int ny, int z){

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                int d = (int)grid.get(x,y,z);
                switch(d){
                case 1: printf("    1"); break;
                default:printf("    ."); break;
                }
            }
            printf("\n");
        }
    }

    /**
       prints slice fo VectorIndexer
     */
    static void printSlice(VectorIndexer vi, int nx, int ny, int nz, int z){

        printf("vi:[ %d x %d x %d] slice %d\n",nx,ny,nz,z);
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                int d = vi.get(x,y,z);
                printf("%5d", d); 
            }
            printf("\n");
        }
    }

    /**
       MT runner for process slice next layer
     */
    class SliceProcessorNext implements Runnable {

        SliceManager slicer; 
        AtomicInteger counter;
        int iteration;
        VectorIndexer closestPoints;
        GridBit oldLayer;
        GridBit freshLayer;
        int neig[];
        /**
           @param counter keeps total count of modified voxels in the layer 
         */
        SliceProcessorNext(int iteration, int neig[], VectorIndexer closestPoints, GridBit oldLayer, GridBit freshLayer, AtomicInteger counter, SliceManager slicer){
            
            this.iteration = iteration;
            this.neig = neig;
            this.closestPoints = closestPoints;
            this.oldLayer = oldLayer;
            this.freshLayer = freshLayer;            
            this.counter = counter;
            this.slicer = slicer; 
        }
        
        public void run(){
            Slice slice = null;
            while(true){
                slice = slicer.getNextSlice(slice);
                if(slice == null)
                    break;
                int count = makeNextLayerSlice(iteration, slice.smin, slice.smax, neig, closestPoints, oldLayer, freshLayer);            
                counter.addAndGet(count);
            }
        }
    } // SliceProcessorNext 

    /**
       MT runner to process slice first layer
     */
    class SliceProcessorFirst implements Runnable {

        SliceManager slicer; 
        AtomicInteger counter;
        VectorIndexer closestPoints;
        GridBit freshLayer;
        PointSet points[]; // points partitioned between slices 
        ArrayInt inds[];  // indices or partitioned points 
        int neig[];
        /**
           @param counter keeps total count of modified voxels in the layer 
           @param points[] - points serapated for each slice 
         */
        SliceProcessorFirst(int neig[], VectorIndexer closestPoints, GridBit freshLayer, AtomicInteger counter, SliceManager slicer, PointSet points[], ArrayInt inds[]){
            
            this.neig = neig;
            this.closestPoints = closestPoints;
            this.freshLayer = freshLayer;            
            this.counter = counter;
            this.slicer = slicer; 
            this.points = points;
            this.inds = inds; 
        }
        
        public void run(){
            Slice slice = null;
            while(true){
                slice = slicer.getNextSlice(slice);
                if(slice == null)
                    break;
                if(DEBUG)printf("slice: [%3d %3d; %2d]\n",slice.smin, slice.smax, slice.index);
                if(points[slice.index] != null){
                    int count = makeFirstLayerSlice(slice.smin, slice.smax, points[slice.index], inds[slice.index], neig, closestPoints, freshLayer);            
                    counter.addAndGet(count);
                }
            }
        }
    } // SliceProcessorFirst 

    /**
       manages set of slices for MT processing 
       locks slice and it's neigbors from processing by other threads 
       thread calls getNextSlice() to get slice for procesing
       and call releaseSlice() to release neighbors from lock
     */
    static class SliceManager {

        int gridWidth;
        int sliceWidth;
        Slice slices[];
        int scount;

        SliceManager(int gridWidth, int sliceWidth){

            this.gridWidth = gridWidth;
            this.sliceWidth = sliceWidth;
            scount = (gridWidth + sliceWidth-1)/sliceWidth;
            slices = new Slice[scount];

            for(int k = 0; k < scount; k++){
                int smin = k*sliceWidth;
                int smax = smin + sliceWidth;
                if(smax > gridWidth) smax = gridWidth;                    
                slices[k] = new Slice(smin, smax, k);
            }
        }

        Slice[] getSlices(){
            return slices;
        }

        synchronized Slice getNextSlice(Slice slice){

            if(slice != null){
                slice.processed = true;
                slice.locked = false;
                int index = slice.index;
                if(index > 0)
                    slices[index-1].locked = false;
                index++;
                if(index < slices.length)
                    slices[index].locked = false;
            }

            int startIndex = 0;
            if(slice != null)
                startIndex = (slice.index+1) % scount;
            // do search next unprocessed unlocked slice             
            int index = findSlice(startIndex); 
            if(index < 0)
                return null;

            Slice s0 = null;
            Slice s1 = slices[index];
            Slice s2 = null;
            if(index > 0)
                s0 = slices[index-1];
            if(index < scount-1)
                s2 = slices[index+1];
            if(s0 != null)
                s0.locked = true;
            if(s2 != null)
                s2.locked = true;
            s1.locked = true;
            return s1;                
        }

        int findSlice(int start){
            
            // find unprocessed unlocked slice with 2 unlocked neighbors
            int index = start;
            for(int k = 0; k < scount; k++, index++){
                index %= scount;

                if(slices[index].processed)
                    continue;

                if(index > 0 && slices[index-1].locked) 
                    continue;

                if(index < scount-1 && slices[index+1].locked) 
                    continue;

                return index;
            }            
            return -1;
        }

        void printSlices(){
            for(int k = 0; k < scount; k++){
                printf("%s\n", slices[k]);
            }
        }

        int getUnprocessedCount(){
            int count = 0;
            for(int k = 0; k < scount; k++){
                if(!slices[k].processed)
                    count++;
            }
            return count;
        }
    }

    /**
       represents single slice to be processed by thread 
     */
    static class Slice {
        int smin;
        int smax;
        int index; 

        int voxelCount;
        boolean locked = false;
        boolean processed = false;

        Slice(int smin, int smax, int index){
            this.smin = smin;
            this.smax = smax;
            this.index = index; 
           
            this.voxelCount = 0;
            this.locked = false;
            this.processed = false;                        
        }

        void setLocked(boolean value){
            locked = value;
        }
        boolean getLocked(boolean value){
            return locked;
        }
        public String toString(){
            return fmt("slice[%3d](%3d-%3d), {%s : %s}", index, smin, smax, locked, processed);
        }
    }
    
}