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

import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Operation;
import abfab3d.grid.AttributeOperation;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.ArrayAttributeGridInt;

import abfab3d.util.Bounds;
import abfab3d.util.TriangleProducer;

import static java.lang.Math.max;
import static java.lang.Math.abs;
import static java.lang.Math.min;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;


/**
   calculates distance to set of triangles in given limits
   
   @author Vladimir Bulatov
 */
public class DistanceToTriangleSet implements Operation, AttributeOperation {

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
        
    
    TriangleProducer m_triangleProducer;
    double m_maxInDistance;
    double m_maxOutDistance;

    public DistanceToTriangleSet(double maxInDistance, double maxOutDistance, int subvoxelResolution){
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

    public AttributeGrid execute(AttributeGrid grid) {
        return makeDistanceGrid(grid);
    }    


    
    public AttributeGrid makeDistanceGrid(AttributeGrid distanceGrid){
        
        // create interior grid  
        Bounds bounds = distanceGrid.getGridBounds();
        double vs = distanceGrid.getVoxelSize();
        ZBufferRasterizer zbr = new ZBufferRasterizer(bounds);
        
        m_triangleProducer.getTriangles(zbr);
        
        AttributeGrid interiorGrid = new ArrayAttributeGridByte(bounds, vs,vs);
        zbr.getRaster(interiorGrid); 
        //if(true) return interiorGrid;
        
        // create shell of closest points around triangle set
        ArrayAttributeGridInt indexGrid = new ArrayAttributeGridInt(bounds, vs, vs);

        TriangleMeshShellBuilder tmsb = new TriangleMeshShellBuilder(indexGrid, m_subvoxelResolution);
        tmsb.setShellHalfThickness(m_shellHalfThickness);        
        tmsb.initialize();
        
        m_triangleProducer.getTriangles(tmsb);

        int pcount = tmsb.getPointCount();
        printf("pcount: %d\n", pcount);
        double pntx[] = new double[pcount];
        double pnty[] = new double[pcount];
        double pntz[] = new double[pcount];
        tmsb.getPointsInGridUnits(pntx, pnty, pntz);

        //if(true) return indexGrid;

        // distribute indices on the whole indexGrid        
        ClosestPointIndexer.PI3_multiPass(pntx, pnty, pntz, indexGrid, m_iterationsCount);

        // transform points into world units
        ClosestPointIndexer.getPointsInWorldUnits(indexGrid, pntx, pnty, pntz);

        // calculate final distances in the given interval 
        ClosestPointIndexer.makeDistanceGrid(indexGrid, pntx, pnty, pntz, 
                                             interiorGrid, distanceGrid, m_maxInDistance, 
                                             m_maxOutDistance, m_subvoxelResolution);               
        return distanceGrid;
    }

} // DistanceToTriangleSet 