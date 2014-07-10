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

import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Operation;
import abfab3d.grid.AttributeOperation;
import abfab3d.grid.ArrayAttributeGridShort;
import abfab3d.grid.GridBitIntervals;
import abfab3d.grid.ClassTraverser;
import abfab3d.grid.GridBit;

import abfab3d.grid.util.ExecutionStoppedException;

import abfab3d.geom.PointCloud;

import abfab3d.util.PointSet;


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
 - calculate vertices of isosurface and storre them in PointSet 
 - use DistanceToPointSet to calculate the distance transform 

 This approach may have some errors near the surface. It is possible to reduce those errors by splitting surface triangles into smaller triangles

 Some rather small errors are introduced in the body because DistanceToPointSetLayered may generate small errors 

 * @author Vladimir Bulatov
 */
public class DistanceTransformLayered extends DistanceTransform implements Operation, AttributeOperation {

    public static boolean DEBUG = true;
    static int debugCount = 0;

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

        if(DEBUG)printf("DistanceTransformLayered.execute(%s)\n", grid);
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

        PointSet pnts = getSurfacePoints(grid);
        if(DEBUG)printf("surface points count: %d\n", pnts.size());

        AttributeGrid distanceGrid = createDistanceGrid(grid);
        initDistances(grid, distanceGrid);
        
        DistanceToPointSet dps = new DistanceToPointSet(pnts, m_inDistance, m_outDistance, m_subvoxelResolution);        
        dps.execute(distanceGrid);

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
       
       for every grid grid point check if surface intersewct any of x,y,z nearest edges
       and add intersection point to PointSet
    */
    PointSet getSurfacePoints(AttributeGrid grid){
        
        PointCloud pnts = new PointCloud((nx*ny + ny*nz + nz*nx)*2);

        int
                nx1 = nx-1,
                ny1 = ny-1,
                nz1 = nz-1;
        double vs = m_voxelSize; 
        int sv = m_surfaceValue;

        for(int iy = 0; iy < ny1; iy++){

            double y = m_ymin + iy*vs;

            for(int ix = 0; ix < nx1; ix++){

                double x = m_xmin + ix*vs;

                for(int iz = 0; iz < nz1; iz++){
                    
                    double z = m_zmin + iz*vs;
                    
                    int v0 = (int)grid.getAttribute(ix,iy,iz)-sv;

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

        return pnts;

    }
}
