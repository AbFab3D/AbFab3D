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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;

import javax.vecmath.Point3d;

import abfab3d.grid.*;

import abfab3d.grid.util.ExecutionStoppedException;

import abfab3d.util.PointSet;
import abfab3d.util.PointSetArray;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;

import static abfab3d.grid.Grid.OUTSIDE;
import static java.lang.Math.max;
import static java.lang.Math.round;

/**
 *
 calculates Distance Transform on the given AttributeGrid to a specified distance inside and outside of shape 

 voxel values are stored in grid's attribute 
 the input grid is supposed to contain truncated distance data near surface layer 
 outside voxels should have values 0 
 inside voxels should have values subvoxelResolution

 the surface of the shape is isosurface with value ((double)subvoxelResolution/2.)  

 output distances are normalized to subvoxelResolution
 this means that a voxel on distance K voxels from the surface will have distance value K*subvoxelResolution

 inside distances are negative
 outside distances are positive
 inside voxels not reached by maxInDistance are initialized to DEFAULT_IN_VALUE
 outside voxels not reached by maxOutDistance are initialized to DEFAULT_OUT_VALUE

 algorithm works as follows 
 - calculate vertices of isosurface and store them in the PointSet 
 - use DistanceToPointSet to calculate the distance transform 

 This approach may have some errors near the surface. It is possible to reduce those errors by splitting surface triangles into smaller triangles

 Some rather small errors are introduced in the body because DistanceToPointSetLayered may generate small errors 

 * @author Vladimir Bulatov
 */
public class DistanceTransformLayered extends DistanceTransform implements Operation, AttributeOperation {

    public static boolean DEBUG = false;
    public static boolean DEBUG_TIMING = false;
    static int debugCount = 100;

    static final int AXIS_X = 0, AXIS_Y = 1, AXIS_Z = 2; // direction  of surface offset from the interior surface point 

    int m_subvoxelResolution = 100; // distance normalization 
    double m_inDistance = 0;
    double m_outDistance = 0;

    int m_defaultInValue = -Short.MAX_VALUE;
    int m_defaultOutValue = Short.MAX_VALUE;

    int nx, ny, nz;
    int m_surfaceValue;
    double m_xmin, m_ymin, m_zmin;
    double m_voxelSize;
    // number of threads to use in MT processing 
    int m_threadCount = 1;

    /**
     @param subvoxelResolution sub voxel resolution 
     @param inDistance maximal distance to calculate transform inside of the shape. Measured in meters
     @param outDistance maximal distance to calculate transform outside of the shape. Measured in meters
    */
    public DistanceTransformLayered(int subvoxelResolution, double inDistance, double outDistance) {

        m_subvoxelResolution = subvoxelResolution;
        m_inDistance = inDistance;
        m_outDistance = outDistance;

    }

    
    /**
       set threads count for MT processing 
     */
    public void setThreadCount(int count){
        m_threadCount = count;
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

        long t0 = 0;
        if(DEBUG)printf("DistanceTransformLayered.execute(%s)\n", grid);
        if(DEBUG)printf("threadCount: %d\n", m_threadCount);
        if(DEBUG)printf("  m_inDistance: %7.3f mm  m_outDistance: %7.3f mm \n", m_inDistance/MM, m_outDistance/MM);

        m_surfaceValue = m_subvoxelResolution/2;
        double vs = grid.getVoxelSize();


        nx = grid.getWidth();
        ny = grid.getHeight();
        nz = grid.getDepth();
        double bounds[] = new double[6];
        grid.getGridBounds(bounds);
        m_voxelSize = vs;
        m_xmin = bounds[0] + vs/2;
        m_ymin = bounds[2] + vs/2;
        m_zmin = bounds[4] + vs/2;
        if(DEBUG_TIMING)t0 = time();
        AttributeGrid distanceGrid = createDistanceGrid(grid);

        PointSet pnts = getSurfacePoints(grid, distanceGrid);

        if(DEBUG_TIMING)printf("getsurface points: %d ms\n",(time()-t0));
        if(DEBUG)printf("surface point count: %d\n", pnts.size());


        //if(DEBUG_TIMING)t0 = time();
        //initDistances(grid, distanceGrid); thius is done in getSurfacePoints
        //if(DEBUG_TIMING)printf("initDistances: %d ms\n",(time()-t0));
        
        if(DEBUG_TIMING)t0 = time();

        DistanceToPointSet dps = new DistanceToPointSet(pnts, m_inDistance, m_outDistance, m_subvoxelResolution);
        dps.setThreadCount(m_threadCount);

        long voxels = (long) nx * ny *nz;
        long bigGrid = (long) Math.pow(1000,3);

        if (voxels > bigGrid) {
            dps.setVectorIndexerTemplate(new VectorIndexerStructMap(1,1,1));
        }
        dps.execute(distanceGrid);

        if(DEBUG_TIMING)printf("distanceToPointSet: %d ms\n",(time()-t0));

        return distanceGrid;

    }

    /**
     * Get the default value for distances inside the object.  The value will remain this for voxels past the maximal
     * inside distance
     * @return
     */
    public long getInsideDefault() {
        return m_defaultInValue;
    }

    /**
     * Get the default value for distances outside the object.  The value will remain this for voxels past the maximal
     * outside distance
     * @return
     */
    public long getOutsideDefault() {
        return m_defaultOutValue;
    }

    /**

       set distanceGrid values to be to m_defaultInValue and m_defaultOutValue  

     */
    void initDistances(AttributeGrid grid, AttributeGrid distanceGrid){

        long distOut = m_defaultOutValue;
        long distIn = m_defaultInValue;

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


    /**
       
       for every grid grid point check if surface intersect any of x,y,z nearest edges
       and add intersection point to PointSet
    */
    PointSet getSurfacePoints(AttributeGrid grid, AttributeGrid distanceGrid){
        if(m_threadCount > 1) 
            return getSurfacePointsMT(grid, distanceGrid);
        else 
            return getSurfacePointsST(grid, distanceGrid);            
    }

    /**
       ST version 
     */
    PointSet getSurfacePointsST(AttributeGrid grid, AttributeGrid distanceGrid){
        
        PointSet pnts = new PointSetArray((nx*ny + ny*nz + nz*nx)*2);
        long distOut = m_defaultOutValue;
        long distIn = m_defaultInValue;

        int
                nx1 = nx-1,
                ny1 = ny-1,
                nz1 = nz-1;
        double vs = m_voxelSize; 
        int sv = m_surfaceValue;
        if(DEBUG)printf("getSurfacePointsST()\n");
        if(DEBUG)printf("  surfaceValue: %d\n",sv);

        for(int iy = 0; iy < ny1; iy++){

            double y = m_ymin + iy*vs;

            for(int ix = 0; ix < nx1; ix++){

                double x = m_xmin + ix*vs;

                for(int iz = 0; iz < nz1; iz++){
                    
                    double z = m_zmin + iz*vs;
                    
                    int v0 = (int)grid.getAttribute(ix,iy,iz)-sv;

                    if(v0 >= 0) distanceGrid.setAttribute(ix,iy,iz,distIn);
                    else        distanceGrid.setAttribute(ix,iy,iz,distOut);
                    
                    int vx = (int)grid.getAttribute(ix+1,iy,iz)-sv;

                    if(v0 * vx <= 0 && v0 != vx)
                        pnts.addPoint(x+(vs*v0)/(v0-vx), y, z);

                    int vy = (int)grid.getAttribute(ix,iy+1,iz)-sv;

                    if(v0 * vy <= 0 && v0 != vy)
                        pnts.addPoint(x,y+(vs*v0)/(v0-vy),z);


                    int vz = (int)grid.getAttribute(ix,iy,iz+1)-sv;

                    if(v0 * vz <= 0 && v0 != vz )
                        pnts.addPoint(x, y, z +(vs*v0)/(v0-vz));                    

                }
            }

            if (Thread.currentThread().isInterrupted()) {
                throw new ExecutionStoppedException();
            }
        }
        if(DEBUG)printf("surface point count: %d\n",pnts.size());
        return pnts;
    }

    /**
       MT version 
     */
    PointSet getSurfacePointsMT(AttributeGrid grid, AttributeGrid distanceGrid){

        if(DEBUG)printf("getSurfacePointsMT()\n");

        PointSet pnts[] = new PointSet[m_threadCount];
        
        ExecutorService executor = Executors.newFixedThreadPool(m_threadCount);

        // rough estimation of points count 
        int pointsPerThread = 2*(nx*ny + ny*nz + nz*nx)/m_threadCount;
        int sliceSize = 1;
        SliceManager slicer = new SliceManager(ny, sliceSize);

        for(int i = 0; i < m_threadCount; i++){

            pnts[i] = new PointSetArray(pointsPerThread);

            SliceProcessor sliceProcessor = new SliceProcessor(grid, distanceGrid, slicer, pnts[i]);            
            executor.submit(sliceProcessor);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // combine all points into one place 
        int count = 0;
        for(int i = 0; i < pnts.length; i++){
            
            count += pnts[i].size();
        }        
        PointSet apnts = new PointSetArray(count);
        Point3d pnt = new Point3d();

        for(int i = 0; i < pnts.length; i++){

            PointSet pnti = pnts[i];
            int kcount = pnti.size();
            
            for(int k = 0; k < kcount; k++){            
                pnti.getPoint(k, pnt);
                apnts.addPoint(pnt.x,pnt.y,pnt.z);
            }
        }
        return apnts;
    }

    /**
       process single slice and store point into pnts 
     */
    void getSurfacePointsSlice(AttributeGrid grid, AttributeGrid distanceGrid, int ymin, int ymax, PointSet pnts){
        
        int
                nx1 = nx-1,
                ny1 = ny-1,
                nz1 = nz-1;
        int count0 = pnts.size();
        double vs = m_voxelSize; 
        int sv = m_surfaceValue;
        long distOut = m_defaultOutValue;
        long distIn = m_defaultInValue;

        for(int iy = ymin; iy < ymax; iy++){

            double y = m_ymin + iy*vs;
            for(int ix = 0; ix < nx1; ix++){

                double x = m_xmin + ix*vs;
                for(int iz = 0; iz < nz1; iz++){                   

                    double z = m_zmin + iz*vs;                    

                    int v0 = (int)grid.getAttribute(ix,iy,iz)-sv;

                    if(v0 >= 0) distanceGrid.setAttribute(ix,iy,iz,distIn);
                    else        distanceGrid.setAttribute(ix,iy,iz,distOut);

                    int vx = (int)grid.getAttribute(ix+1,iy,iz)-sv;
                    if(v0 * vx <= 0 && v0 != vx)
                        pnts.addPoint(x+(vs*v0)/(v0-vx), y, z);

                    int vy = (int)grid.getAttribute(ix,iy+1,iz)-sv;
                    if(v0 * vy <= 0 && v0 != vy)
                        pnts.addPoint(x,y+(vs*v0)/(v0-vy),z);

                    int vz = (int)grid.getAttribute(ix,iy,iz+1)-sv;
                    if(v0 * vz <= 0 && v0 != vz )
                        pnts.addPoint(x, y, z +(vs*v0)/(v0-vz));                    

                }
            }

            if (Thread.currentThread().isInterrupted()) {
                throw new ExecutionStoppedException();
            }
        }
        if(false)printf("slice [%3d x %3d] surface point count: %d\n",ymin, ymax,(pnts.size()-count0));

    }

    /**
       runner for each thread 
     */
    class SliceProcessor implements Runnable {

        AttributeGrid grid;
        AttributeGrid distanceGrid;
        SliceManager slicer;
        PointSet pnts;

        SliceProcessor(AttributeGrid grid, AttributeGrid distanceGrid, SliceManager slicer, PointSet pnts){
            this.grid = grid;
            this.distanceGrid = distanceGrid;
            this.slicer = slicer; 
            this.pnts = pnts;
        }

        public void run(){

            while(true){
                Slice slice = slicer.getNextSlice();
                if(slice == null)
                    break;
                getSurfacePointsSlice(grid, distanceGrid, slice.smin, slice.smax, pnts);            
            }
        }        
    }

    /**
       handles slices for processing 
     */
    static class SliceManager {

        Slice slices[];
        int scount;
        int nextSliceIndex=0;

        SliceManager(int gridWidth, int sliceWidth){

            scount = (gridWidth + sliceWidth-1)/sliceWidth;
            slices = new Slice[scount];

            for(int k = 0; k < scount; k++){
                int smin = k*sliceWidth;
                int smax = smin + sliceWidth;
                if(smax > gridWidth) smax = gridWidth;                    
                slices[k] = new Slice(smin, smax);
            }
        }

        Slice[] getSlices(){
            return slices;
        }

        synchronized Slice getNextSlice(){

            if(nextSliceIndex < scount-1){
                return slices[nextSliceIndex++];
            } else {
                return null;
            }
        }
    }

    /**
       represents single slice to be processed by thread 
     */
    static class Slice {
        int smin;
        int smax;

        Slice(int smin, int smax){
            this.smin = smin;
            this.smax = smax;
        }
    }
 
}
