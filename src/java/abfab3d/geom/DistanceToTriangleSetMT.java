/** 
 *                        Shapeways, Inc Copyright (c) 2015
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

import abfab3d.grid.*;

import abfab3d.grid.op.ClosestPointIndexerMT;
import abfab3d.grid.op.ClosestPointIndexer;

import abfab3d.util.AbFab3DGlobals;
import abfab3d.util.Bounds;
import abfab3d.util.TriangleProducer;


import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;


/**
   calculates distance to set of triangles in given limits
   
   @author Vladimir Bulatov
 */
public class DistanceToTriangleSetMT implements Operation, AttributeOperation {

    static final boolean DEBUG = true;

    static final int DEFAULT_SVR = 100;

    // count of clean up iterations at the end
    int m_iterationsCount = 0;
    // thickenss of intialial shell
//    double m_shellHalfThickness = 1.9;  // 2.0, 2.25, 2.45*, 2.84, 3.0 3.17 3.33*, 3.46, 3.62, 3.74*   * - good values
    double m_shellHalfThickness = 0.9;  // vlad suggested to reduce errors in flat values
    //
    int m_subvoxelResolution = DEFAULT_SVR;
    double m_voxelSize;
    Bounds m_bounds;
    int m_threadCount=1;

    TriangleProducer m_triangleProducer;
    double m_maxInDistance;
    double m_maxOutDistance;

    public DistanceToTriangleSetMT(double maxInDistance, double maxOutDistance, int subvoxelResolution){
        m_subvoxelResolution = subvoxelResolution;
        m_maxInDistance = maxInDistance;
        m_maxOutDistance = maxOutDistance;
    }

    public void setTriangleProducer(TriangleProducer triangleProducer){
        m_triangleProducer = triangleProducer;
    }

    public void setIterationsCount(int count){
        m_iterationsCount = count;
    }

    public void setShellHalfThickness(double halfThickness){
        m_shellHalfThickness = halfThickness;
    }    

    public Grid execute(Grid grid) {
        throw new IllegalArgumentException(fmt("DistanceTransformExact.execute(%d) not implemented!\n", grid));
    }

    public void setThreadCount(int count) {
        if (count < 1) {
            count = Runtime.getRuntime().availableProcessors();
        }

        int max_threads = ((Number) AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY)).intValue();
        if (count > max_threads)
            count = max_threads;

        m_threadCount = count;
    }



    public AttributeGrid execute(AttributeGrid grid) {
        return makeDistanceGrid(grid);
    }    


    
    public AttributeGrid makeDistanceGrid(AttributeGrid distanceGrid){
        
        // create interior grid  
        Bounds bounds = distanceGrid.getGridBounds();
        double vs = distanceGrid.getVoxelSize();
        long t0 = System.nanoTime();
        ZBufferRasterizer zbr = new ZBufferRasterizer(bounds);
        
        m_triangleProducer.getTriangles(zbr);
        AttributeGrid interiorGrid = new ArrayAttributeGridByte(bounds, vs,vs);
        zbr.getRaster(interiorGrid);
        printf("rasterize.  %f ms\n", (System.nanoTime() - t0) / 1e6);
        //if(true) return interiorGrid;

        t0 = System.nanoTime();
        // create shell of closest points around triangle set
        ArrayAttributeGridInt indexGrid = new ArrayAttributeGridInt(bounds, vs, vs);

        TriangleMeshShellBuilder tmsb = new TriangleMeshShellBuilder(indexGrid, m_subvoxelResolution);
        tmsb.setShellHalfThickness(m_shellHalfThickness);        
        tmsb.initialize();
        
        m_triangleProducer.getTriangles(tmsb);
        printf("build shell.  %f ms\n", (System.nanoTime() - t0) / 1e6);

        int pcount = tmsb.getPointCount();
        printf("pcount: %d\n", pcount);
        double pntx[] = new double[pcount];
        double pnty[] = new double[pcount];
        double pntz[] = new double[pcount];
        tmsb.getPointsInGridUnits(pntx, pnty, pntz);

        //if(true) return indexGrid;

//        ClosestPointIndexerMT2 cpi = new ClosestPointIndexerMT2();
        ClosestPointIndexerMT cpi = new ClosestPointIndexerMT();

        // distribute indices on the whole indexGrid        
        ClosestPointIndexerMT.PI3_MT(pntx, pnty, pntz, indexGrid, m_threadCount);
        
        // transform points into world units
        ClosestPointIndexer.getPointsInWorldUnits(indexGrid, pntx, pnty, pntz);

        // calculate final distances in the given interval 
        ClosestPointIndexer.makeDistanceGrid(indexGrid, pntx, pnty, pntz,
                interiorGrid, distanceGrid, m_maxInDistance, m_maxOutDistance);
        return distanceGrid;
    }

} // DistanceToTriangleSet 