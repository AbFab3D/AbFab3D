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
import abfab3d.grid.GridMask;
import abfab3d.grid.ClassTraverser;
import abfab3d.grid.GridBit;
import abfab3d.grid.util.ExecutionStoppedException;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

import static abfab3d.grid.Grid.OUTSIDE;
import static java.lang.Math.max;
import static java.lang.Math.round;
import static java.lang.Math.ceil;

/**
 * 
 calculates signed Distance Transform of the density grid in a shell surrounding shape's surface. 
 Distance Transform of the given voxel is its distance from nearest surface point. 

 Input grid has to be density grid wih density stored in voxel attribute. 
 Density of exterior voxels should be subvoxelResolution 
 density of exterior voxels should be 0. Surface voxel have intermediate values. 
 
 Output grid is distance grid with signed distance data stored in attributes 
 Distance data is measured in subvoxels. 


 the surface of the shape is isosurface with value ((double)subvoxelResolution/2.)  

 "inside boundary voxels" are those with value >= subvoxelResolution/2 and have at least one 6-neighbor with value < maxAttribute/2
 outside neighbours of inside boundary voxels are "outside boundary voxels"
  
 output distances are normalized to subvoxelResolution, 
 this means that a voxel on distance K voxel sizes from the surface will have distance value K*subvoxelResolution

 inside distances are positive
 outside distacnes are negative 
 inside voxeld we were not reached by maxInDistance are initialized to DEFAULT_IN_VALUE
 

 algorithm works as follows 
 1) - initiliaze the distanceGrid to m_defaultValue for inside voxels and -m_defaultValue for outside voxels 
 
 2) 
 scan the grid for inside surface voxels 
 - for each "inside surface voxels" calculate possible isosurface points in all 6 directions  
 - for each isosurface point calculate distance to each grid point inside of a sphere of small radius 
 - update distance value if calculated distance is smaller than the currently stored distance value 
 3) scan calculated points and if the point has neighbour with default value build spherical neighborhood around it
 

 The error in distance value in this algorithm are coming 
   a) from replacement of surfae triangles with vertices only
   b) replacemennt of straight line from surface to a voxel with segmented line 


 * @author Vladimir Bulatov
 * @author Alan Hudson
 */
public class DistanceTransformMultiStep  extends DistanceTransform implements Operation, AttributeOperation {

    public static boolean DEBUG = false;
    static int debugCount = 1000;

    static final int AXIS_X = 0, AXIS_Y = 1, AXIS_Z = 2; // direction  of surface offset from the interior surface point 
	    
    int m_subvoxelResolution = 100; // size of subvoxel 
    double m_inDistance = 0; 
    double m_outDistance = 0;
    
    int m_maxInDistance = 0; // maximal outside distance ( in subvoxels )
    int m_maxOutDistance = 0; // maximal inside distance ( in subvoxels )
    int m_defaultValue = Short.MAX_VALUE;
    int nx, ny, nz;
    int m_surfaceValue;

    int m_inSteps; // how many steps to do inside 
    int m_outSteps; // how many steps to do outside 


    // maximal radius of ball in one step (in voxels) 
    double m_stepSizeVoxels = 4.; // the radius of ball to use at each step
    
    protected int m_allBallNeighbors[][];// coordinates of neighbors point inside of the sphere of radius m_maxStepDistance
    /**
       @param maxAttribute maximal attribute value for inside voxels 
       @param inDistance maximal distance to calculate transform inside of the shape. Measured in meters 
       @param outDistance maximal distance to calculate transform outside of the shape. Measured in meters 
    */
    public DistanceTransformMultiStep(int subvoxelResolution, double inDistance, double outDistance) {

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
        throw new IllegalArgumentException(fmt("DistanceTransformExact.execute(%s) not implemented!\n", grid));
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

    public AttributeGrid execute(AttributeGrid grid) {

        printf("DistanceTransformMultiStep.execute(%s)\n", grid); 
       
        m_surfaceValue = m_subvoxelResolution/2;
        double vs = grid.getVoxelSize();
                
        m_maxInDistance = (int)round((m_inDistance/vs)*m_subvoxelResolution);
        m_maxOutDistance = (int)round((m_outDistance/vs)*m_subvoxelResolution);
        
        printf("m_maxInDistance: %d subvoxels\n", m_maxInDistance);
        printf("m_maxOutDistance: %d subvoxels\n", m_maxOutDistance);
        printf("stepSize: %f\n", m_stepSizeVoxels);

        m_inSteps = (int)ceil((m_inDistance/vs)/m_stepSizeVoxels);
        m_outSteps = (int)ceil((m_outDistance/vs)/m_stepSizeVoxels);

        printf("m_inSteps: %d\n", m_inSteps);
        printf("m_outSteps: %d\n", m_outSteps);
        
        nx = grid.getWidth();
        ny = grid.getHeight();
        nz = grid.getDepth();

        m_defaultValue = Short.MAX_VALUE;            
        m_allBallNeighbors = makeAllBallNeighborsWithOffset((int)round(m_stepSizeVoxels*m_subvoxelResolution), m_subvoxelResolution);
        
        printf("ballNeighbors count: %d\n",m_allBallNeighbors[0].length/4);
        
        AttributeGrid distanceGrid = createDistanceGrid(grid);
        initDistances(grid, distanceGrid);
        
        scanSurface(grid,distanceGrid);

        GridMask boundary = new GridMask(nx, ny, nz);

        int maxStep = max(m_inSteps, m_outSteps);

        for(int k = 1; k <  maxStep; k++){
            findBoundary(distanceGrid, boundary); 
            //printSlice(boundary, 50, 90, 0, 30, nx/2);
            updateBoundary(distanceGrid, boundary);       
            if(k+1 < m_inSteps)
                boundary.clear();

            if (Thread.currentThread().isInterrupted()) {
                throw new ExecutionStoppedException();
            }
        }     
        return distanceGrid;

    }

    /**

       set distanceGrid values to be -m_defaultValue inside and m_defaultValue outside 
       
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
    // for every iniside point near surface calculate distances of inside and outside points 
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
        int sr = m_subvoxelResolution;
        double dvs = (double)vs; // to enforce FP calculations 
        for(int y = 0; y < ny1; y++){
            for(int x = 0; x < nx1; x++){
                for(int z = 0; z < nz1; z++){
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
                            updateSurfaceDistances(x, y, z, (dvs - v0)/(vx-v0), AXIS_X, distanceGrid);
                        if(vy <= vs)
                            updateSurfaceDistances(x, y, z, (dvs - v0)/(vy-v0), AXIS_Y, distanceGrid);
                        if(vz <= vs)
                            updateSurfaceDistances(x, y, z, (dvs - v0)/(vz-v0), AXIS_Z,distanceGrid);
                    } else { // (v0 <= vs)
                        if(vx > vs)
                            updateSurfaceDistances(x, y, z, (dvs - v0)/(vx-v0), AXIS_X, distanceGrid);
                        if(vy > vs)
                            updateSurfaceDistances(x, y, z, (dvs - v0)/(vy-v0), AXIS_Y, distanceGrid);
                        if(vz > vs)
                            updateSurfaceDistances(x, y, z, (dvs - v0)/(vz-v0), AXIS_Z,distanceGrid);                        
                    }
                }
            }
        }        
    }    


    /**
       scan the grid check if point has inside neighbor and updated distance from that point 
     */
    void findBoundary( AttributeGrid distanceGrid, GridBit boundary){ // input output distance function 
        printf("findInsideBoundary()\n");
        int 
            nx1 = nx-1,
            ny1 = ny-1,
            nz1 = nz-1;
        int inDefault = -m_defaultValue;
        int outDefault = m_defaultValue;


        for(int y = 1; y < ny1; y++){
            for(int x = 1; x < nx1; x++){
                for(int z = 1; z < nz1; z++){

                    int distance = L2S(distanceGrid.getAttribute(x,y,z));
                    if(distance < 0){  // inside 
                        if(distance != inDefault){ // point is ready
                            if( 
                               L2S(distanceGrid.getAttribute(x-1,y,z)) == inDefault || 
                               L2S(distanceGrid.getAttribute(x+1,y,z)) == inDefault ||
                               L2S(distanceGrid.getAttribute(x,y-1,z)) == inDefault ||
                               L2S(distanceGrid.getAttribute(x,y+1,z)) == inDefault ||
                               L2S(distanceGrid.getAttribute(x,y,z-1)) == inDefault ||
                               L2S(distanceGrid.getAttribute(x,y,z+1)) == inDefault ){
                                // point is ready and it has blank neighbors 
                                boundary.set(x,y,z,1);
                            }          
                        }
                    } else if(distance > 0) { // outside 
                        
                        if(distance != outDefault){ // point is ready
                            //TODO avoid recalculation of old points 
                            if( 
                               L2S(distanceGrid.getAttribute(x-1,y,z)) == outDefault || 
                               L2S(distanceGrid.getAttribute(x+1,y,z)) == outDefault ||
                               L2S(distanceGrid.getAttribute(x,y-1,z)) == outDefault ||
                               L2S(distanceGrid.getAttribute(x,y+1,z)) == outDefault ||
                               L2S(distanceGrid.getAttribute(x,y,z-1)) == outDefault ||
                               L2S(distanceGrid.getAttribute(x,y,z+1)) == outDefault ){
                                // point is ready and it has blank neighbors 
                                boundary.set(x,y,z,1);
                            }          
                        }                        
                    }
                }
            }
        }        
    }    
    
    /**
       updates distances inside of spherical neigbourhood 
     */
    void updateBoundary( AttributeGrid distanceGrid, GridBit boundary){ // input output distance function 
        
        printf("updateInsideBoundary()\n");
        int 
            nx1 = nx-1,
            ny1 = ny-1,
            nz1 = nz-1;

        for(int y = 1; y < ny1; y++){
            for(int x = 1; x < nx1; x++){

                for(int z = 1; z < nz1; z++){
                    
                    if(boundary.get(x,y,z) == 1){

                        int distance = L2S(distanceGrid.getAttribute(x,y,z));
                        updateBoundaryDistances(x, y, z, distance, distanceGrid);

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
    void updateSurfaceDistances(int x, int y, int z, double offset, int axis, AttributeGrid distanceGrid){
        
        double sr = m_subvoxelResolution;

        int ioffset = (int)(offset*sr + 0.5);


        if(ioffset >= sr) {
            ioffset -= sr;
            switch(axis){
            case AXIS_X: x++; break;
            case AXIS_Y: y++; break;
            case AXIS_Z: z++; break;
            }
        }
        int xoff,yoff, zoff;
        switch(axis){
        default:
        case AXIS_X: xoff = 0; yoff = 1; zoff = 2; break;
        case AXIS_Y: xoff = 2; yoff = 0; zoff = 1; break;
        case AXIS_Z: xoff = 1; yoff = 2; zoff = 0; break;
        }

        int neigbours[] = m_allBallNeighbors[ioffset];

        for(int k = 0; k < neigbours.length; k+= 4){

            int dx = neigbours[k+xoff];
            int dy = neigbours[k+yoff];
            int dz = neigbours[k+zoff];
            int dist = neigbours[k+3];
            
            int xx = x + dx;
            int yy = y + dy;
            int zz = z + dz;

            if(inGrid(xx,yy,zz)){
                                
                int newDist = dist;
                int curDist = L2S(distanceGrid.getAttribute(xx,yy,zz));

                if(curDist > 0){
                    // we are outside
                    if(newDist <= m_maxOutDistance && newDist < curDist){
                        // new distance is smaller -> replace current distance with new distance 
                        distanceGrid.setAttribute(xx,yy,zz, newDist);
                        if(false){
                            if(newDist == 0){
                                printf("(%2d %2d %2d ) %4d -> %4d \n", x,y,z, (curDist), (newDist));                            
                            }
                        }                                
                    }
                } else if(curDist < 0){
                    // we are inside
                    curDist = (short)(-curDist); // inside distances are negative -> make it positive 
                    if(newDist <= m_maxInDistance && newDist < curDist){
                        // new distance is smaller - replace current distance with new distance 
                        distanceGrid.setAttribute(xx,yy,zz, -newDist);
                        if(false){
                            if(newDist == 0){
                                printf("(%2d %2d %2d ) %4d -> %4d \n", x,y,z, (-curDist), (-newDist));                            
                            }
                        }                                
                    }  
                }
            }
        }
    }
    
    /**
       makes ball around boundaryt point 
     */
    void updateBoundaryDistances(int x, int y, int z, int voxelDistance, AttributeGrid distanceGrid){
        
        double sr = m_subvoxelResolution;
        int neigbours[] = m_allBallNeighbors[0]; // the ball is centered at voxels 

        for(int k = 0; k < neigbours.length; k+= 4){

            int dx = neigbours[k];
            int dy = neigbours[k+1];
            int dz = neigbours[k+2];
            int dist = neigbours[k+3];
            
            int xx = x + dx;
            int yy = y + dy;
            int zz = z + dz;

            if(inGrid(xx,yy,zz)){

                if(voxelDistance < 0) {// inside voxel 
                    int newDist = voxelDistance-dist; // distance inside is negative 
                    int curDist = L2S(distanceGrid.getAttribute(xx,yy,zz));
                    if(newDist > curDist && newDist >  -m_maxInDistance ){
                        distanceGrid.setAttribute(xx,yy,zz, newDist);
                    }  
                } else { // outside 
                    int newDist = voxelDistance+dist; // ouside distance is positive
                    int curDist = L2S(distanceGrid.getAttribute(xx,yy,zz));
                    if(newDist < curDist && newDist <  m_maxOutDistance ){
                        if(false && debugCount-- > 0) 
                            printf("(%3d %3d %3d): %3d\n",xx,yy,zz,newDist);
                        distanceGrid.setAttribute(xx,yy,zz, newDist);
                    }                      
                }
            }
        }
    }

    /**
       returns array of neighbors of a point in a ball or radius @radius 
       radius is expressed in subvoxels 
    */
    public static int[] makeBallNeighbors(int radius, int subvoxelResolution){

        radius += subvoxelResolution; // increment to take into account possible offset along one axis 

        int radius2 = radius*radius; // compare agains radius squared

        // calculate size needed 
        int count = 0;
        int r1 = (radius + subvoxelResolution-1)/subvoxelResolution;        
        
        int sv2 = subvoxelResolution*subvoxelResolution;

        for(int x = -r1; x <= r1; x++){
            for(int y = -r1; y <= r1; y++){
                for(int z = -radius; z <= r1; z++){
                    double d2 = (x*x + y*y + z*z)*sv2;
                    if(d2 <= radius2)
                        count += 3;
                }
            }
        }
        int neig[] = new int[count];

        // store data in array
        count = 0;
        for(int x = -r1; x <= r1; x++){
            for(int y = -r1; y <= r1; y++){
                for(int z = -r1; z <= r1; z++){
                    double d2 = (x*x + y*y + z*z)*sv2;
                    if(d2 <= radius2){
                        neig[count] = x;
                        neig[count+1] = y;
                        neig[count+2] = z;
                        count += 3;
                    }
                }
            }
        }
        
        return neig;
    }

    /**
       makes all spherical neigborhood around point (offset, 0,0)  with different offsets 
       radius and offset are expressed in subvoxels        
    */
    public static int[][] makeAllBallNeighborsWithOffset(int radius, int subvoxelResolution){
        printf("makeAllBallNeighborsWithOffset(%d,%d)\n",radius, subvoxelResolution);
        int neig[][] = new int[subvoxelResolution][];
        for(int offset = 0; offset < subvoxelResolution; offset++){
            neig[offset] =  makeBallNeighborsWithOffset(radius, offset, subvoxelResolution);
        }
        return neig;
    }

    /**
       makes spherical neigborhood around point (offset, 0,0) 
       radius and offset are expressed in subvoxels        
    */
    public static int[] makeBallNeighborsWithOffset(int radius, int offset, int subvoxelResolution){

        int radius2 = radius*radius; // to compare agains radius squared

        // calculate size needed 
        int count = 0;
        int r1 = (radius + subvoxelResolution-1)/subvoxelResolution + 1; // +1 to take into account offset 
        int sv = subvoxelResolution;

        for(int x = -r1; x <= r1; x++){
            for(int y = -r1; y <= r1; y++){
                for(int z = -r1; z <= r1; z++){
                    int xs = 
                        x*sv - offset,
                        ys = y*sv,
                        zs = z*sv;
                    //distance to point( offset, 0,0) 
                    double d2 = (xs*xs + ys*ys + zs*zs);
                    if(d2 <= radius2)
                        count += 4;
                }
            }
        }
        
        int neig[] = new int[count];

        // store data in array
        count = 0;
        for(int x = -r1; x <= r1; x++){
            for(int y = -r1; y <= r1; y++){
                for(int z = -r1; z <= r1; z++){
                    int 
                        xs = x*sv - offset, 
                        ys = y*sv,
                        zs = z*sv;
                    //distance to point( offset, 0,0)                    
                    int d2 = xs*xs + ys*ys + zs*zs;
                    if(d2 <= radius2){
                        
                        neig[count] = x;
                        neig[count+1] = y;
                        neig[count+2] = z;
                        neig[count+3] = (int)(Math.sqrt(d2) + 0.5); // store the distance 
                        count += 4;
                    }
                }
            }
        }
        if(false) {
            //if(offset == 0) {
            int nn = count;//neig.length;
            int nn1 = nn/4;
            printf("nn: %d  nn1: %d\n",nn, nn1);
            for(int k = 0; k < neig.length; k += 4){
                printf("%3d (%3d %3d %3d): %5d\n",k/4, neig[k],neig[k+1],neig[k+2],neig[k+3]);
            }
        }
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
    
    void printSlice(GridBit grid, int xmin, int xmax, int ymin, int ymax, int z){
        for(int y = ymin; y < ymax; y++){
            for(int x = xmin; x < xmax; x++){
                int b = (int)grid.get(x,y,z);
                if(b != 0) printf("%4d",b);
                else printf("   .");
            }
            printf("\n");
        }
    }


}

