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

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

import static abfab3d.grid.Grid.OUTSIDE;
import static java.lang.Math.max;
import static java.lang.Math.round;

/**
 *
 calculates Distance Transform on the given AttributeGrid to a specified distance inside and outside of shape 

 voxel values are stored in grid's attribute 
 the input grid is supposed to contain truncated distance data near surface layer 
 outside voxels should have values 0 
 inside voxels should have values maxAttribute  

 the surface of the shape is isosurface with value ((double)maxAttribute/2.)  

 "inside boundary voxels" are those with value >= maxAttribute/2 and have at least one 6-neighbor with value < maxAttribute/2
 outside neighbours of inside boundary voxels are "outside boundary voxels"

 output distances are normalized to maxAttribute, 
 this meand that a voxel on distance K from the surface will have distance value K*maxAttribute

 inside distances are negative
 outside distances are positive
 inside voxels not reached by maxInDistance are initialized to DEFAULT_IN_VALUE


 algorithm works as follows 
 - initialize the distanceGrid to -m_defaultValue for inside voxels and m_defaultValue for outside voxels
 scan the grid for inside surface voxels 
 - for each "inside surface voxels" calculate possible isosurface points in all 6 directions  
 - for each isosurface point calculate distance to each grid point inside of a sphere of radius max(inDistance, outDistance)
 - update distance value if calculated distance is smaller than the currently stored distance value at that grid voxel 

 This approach is not absolutely precise. 
 Better is to calculate actual surface triangles and calculate distances to triangles, not points.
 The maximal error of current calculations is near the surface. 

 * @author Vladimir Bulatov
 */
public class DistanceTransformExact implements Operation, AttributeOperation {

    public static boolean DEBUG = false;
    static int debugCount = 0;

    static final int AXIS_X = 0, AXIS_Y = 1, AXIS_Z = 2; // direction  of surface offset from the interior surface point 

    int m_maxAttribute = 100; // distance normalization 
    double m_inDistance = 0;
    double m_outDistance = 0;

    int m_maxInDistance = 0; // maximal outside distance (expressed in units of voxelSize/m_maxAttribute)
    int m_maxOutDistance = 0; // maximal inside distance (expressed in units of voxelSize/m_maxAttribute)
    int m_defaultValue = Short.MAX_VALUE;
    int nx, ny, nz;
    int m_surfaceValue;
    protected int m_ballNeighbors[];// coordinates of neighbors point inside of the sphere of radius m_maxDistance
    /**
     @param maxAttribute maximal attribute value for inside voxels
     @param inDistance maximal distance to calculate transform inside of the shape. Measured in meters
     @param outDistance maximal distance to calculate transform outside of the shape. Measured in meters
     */
    public DistanceTransformExact(int maxAttribute, double inDistance, double outDistance) {

        m_maxAttribute = maxAttribute;
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

        printf("DistanceTransformExact.execute(%s)\n", grid);

        m_surfaceValue = m_maxAttribute/2;
        double vs = grid.getVoxelSize();

        m_maxInDistance = (int)round(m_inDistance*m_maxAttribute/vs);
        m_maxOutDistance = (int)round(m_outDistance*m_maxAttribute/vs);


        nx = grid.getWidth();
        ny = grid.getHeight();
        nz = grid.getDepth();

        m_defaultValue = Short.MAX_VALUE;

        // bal radius normalized to maxAttribute 
        int ballRadius = max(m_maxInDistance, m_maxOutDistance);


        m_ballNeighbors = makeBallNeighbors((ballRadius+m_maxAttribute-1)/m_maxAttribute);


        //TODO what grid to allocate here 
        AttributeGrid distanceGrid = new ArrayAttributeGridShort(nx, ny, nz, grid.getVoxelSize(), grid.getSliceHeight());
        initDistances(grid, distanceGrid);

        scanSurface(grid,distanceGrid);


        return distanceGrid;

    }

    /**
     * Get the default value for distances inside the object.  The value will remain this for voxels past the maximal
     * inside distance
     * @return
     */
    public long getInsideDefault() {
        return -m_defaultValue;
    }

    /**
     * Get the default value for distances outside the object.  The value will remain this for voxels past the maximal
     * outside distance
     * @return
     */
    public long getOutsideDefault() {
        return m_defaultValue;
    }

    /**

     set distanceGrid values to bes to -max inside and max outside

     */
    void initDistances(AttributeGrid grid, AttributeGrid distanceGrid){

        long distOut = m_defaultValue;
        long distIn = -m_defaultValue;

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
    // for every inside point near surface calculate distances of inside and outside points
    //  for inside points distance is positive
    //  for outside points distance is negative
    //  replace distance values with ( abs(distance), distanceToPoint) it is is less than current distance 
    //
    void scanSurface(AttributeGrid grid,      // input original grid 
                     AttributeGrid distanceGrid // output distance function 
    ){
        int
                nx1 = nx-1,
                ny1 = ny-1,
                nz1 = nz-1;
        int vs = m_surfaceValue;
        double dvs = (double)vs; // to enforce FP calculations 
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    int v0 = (int)grid.getAttribute(x,y,z);
                    int
                            vx = v0,
                            vy = v0,
                            vz = v0;
                    if(x < nx1)vx = (int)grid.getAttribute(x+1,y,z);
                    if(y < ny1)vy = (int)grid.getAttribute(x,y+1,z);
                    if(z < nz1)vz = (int)grid.getAttribute(x,y,z+1);
                    if(v0 > vs ) {
                        if(vx <= vs)
                            updateDistances(x, y, z, (dvs - v0)/(vx-v0), AXIS_X, distanceGrid);
                        if(vy <= vs)
                            updateDistances(x, y, z, (dvs - v0)/(vy-v0), AXIS_Y, distanceGrid);
                        if(vz <= vs)
                            updateDistances(x, y, z, (dvs - v0)/(vz-v0), AXIS_Z,distanceGrid);
                    } else { // (v0 <= vs)
                        if(vx > vs)
                            updateDistances(x, y, z, (dvs - v0)/(vx-v0), AXIS_X, distanceGrid);
                        if(vy > vs)
                            updateDistances(x, y, z, (dvs - v0)/(vy-v0), AXIS_Y, distanceGrid);
                        if(vz > vs)
                            updateDistances(x, y, z, (dvs - v0)/(vz-v0), AXIS_Z,distanceGrid);
                    }
                }
            }
        }
    }

    /**
     calculates distances in the ball surronding point x,y,z
     and updates distances in distanceGrid if new distance is shorter
     makes sign of new distance the same as old distance
     */
    void updateDistances(int x, int y, int z, double offset, int axis, AttributeGrid distanceGrid){

        double
                x0 = x,
                y0 = y,
                z0 = z;
        double norm = m_maxAttribute;
        int neigbours[] = m_ballNeighbors;

        for(int k = 0; k < neigbours.length; k+= 3){

            int dx = neigbours[k];
            int dy = neigbours[k+1];
            int dz = neigbours[k+2];

            int xx = x + dx;
            int yy = y + dy;
            int zz = z + dz;
            if(inGrid(xx,yy,zz)){

                double
                        ddx = dx,
                        ddy = dy,
                        ddz = dz;

                switch(axis){
                    default:
                    case AXIS_X: ddx -= offset; break;
                    case AXIS_Y: ddy -= offset; break;
                    case AXIS_Z: ddz -= offset; break;
                }

                int newDist = (int)(norm*Math.sqrt(ddx*ddx + ddy*ddy + ddz*ddz) + 0.5);
                int curDist = L2S(distanceGrid.getAttribute(xx,yy,zz));

                if(curDist < 0){
                    // we are inside
                    curDist = (short)(-curDist); // inside distances are negative -> make it positive

                    if(newDist <= m_maxInDistance && newDist < curDist){
                        // new distance is smaller -> replace current distance with new distance 
                        distanceGrid.setAttribute(xx,yy,zz, -newDist);
                        if(false && debugCount-- > 0){
                            printf("(%2d %2d %2d ) %4d -> %4d \n", x,y,z, -curDist, -newDist);
                        }
                    }
                } else if(curDist > 0){
                    // we are outside 
                    if(newDist <= m_maxOutDistance && newDist < curDist){
                        // new distance is smaller - replace current distance with new distance 
                        distanceGrid.setAttribute(xx,yy,zz, newDist);
                        if(false && debugCount-- > 0){
                            printf("(%2d %2d %2d ) %4d -> %4d \n", x,y,z, (curDist), (newDist));
                        }
                    }
                }
            }
        }
    }

    /**
     returns array of neighbors of a point in a ball or radius @radius
     radius is expressed in voxel size
     */
    public static int[] makeBallNeighbors(int radius){

        radius++; // increment to take into account possible offset along one axis 
        int radius2 = radius*radius; // compare against radius squared

        // calculate size needed 
        int count = 0;
        for(int x = -radius; x <= radius; x++){
            for(int y = -radius; y <= radius; y++){
                for(int z = -radius; z <= radius; z++){
                    double d2 = x*x + y*y + z*z;
                    if(d2 <= radius2)
                        count += 3;
                }
            }
        }
        int neig[] = new int[count];

        // store data in array
        count = 0;
        for(int x = -radius; x <= radius; x++){
            for(int y = -radius; y <= radius; y++){
                for(int z = -radius; z <= radius; z++){
                    double d2 = x*x + y*y + z*z;
                    if(d2 <= radius2){
                        neig[count] = x;
                        neig[count+1] = y;
                        neig[count+2] = z;
                        count += 3;
                    }
                }
            }
        }

        printf("Neighbors count: %d\n",neig.length);
        return neig;
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

}
