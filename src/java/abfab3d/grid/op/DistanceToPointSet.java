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

import abfab3d.grid.util.ExecutionStoppedException;
import abfab3d.util.PointSet;
import abfab3d.transforms.Identity;

import static java.lang.Math.sqrt;
import static abfab3d.util.Output.time;
import static abfab3d.util.Output.printf;
import static abfab3d.util.MathUtil.L2S;


/**
   class fills grid with values of signed distance to a point set 
   signed distance is negative inside of the object, positive outisde of the object and zero at points on surface of the shape    
   if signed distance is needed, the inside/outside tester should be supplied

   @author Vladimir Bulatov
 */
public class DistanceToPointSet implements Operation, AttributeOperation {
    
    int m_algorithm = ALG_LAYERED;

    static public final int ALG_EXACT = 1; // straitforward exact calculation
    static public final int ALG_LAYERED = 2; // building distance in layers 

    static final boolean DEBUG = false;
    static final boolean DEBUG_GRID = false;

    int m_subvoxelResolution = 100;
    int defaultInValue = -Short.MAX_VALUE;
    int defaultOutValue = Short.MAX_VALUE;
    InsideTester m_insideTester;
    PointSet m_points;

    private double m_voxelSize;
    // grid bounds 
    private double m_bounds[] = new double[6];
    // grid sizes
    private int m_nx, m_ny, m_nz;
    // coefficients of convesion from world coord to grid coord 
    private double m_gsx,m_gsy,m_gsz,m_gtx,m_gty,m_gtz;
    AttributeGrid m_grid;
    
    double m_maxInDistance;
    double m_maxOutDistance;
    //int m_neighbors[]; // spherical neighbors 
    double m_maxDistVoxels;

    // vector indexer template used to store indices to neares points
    VectorIndexer m_vectorIndexerTemplate = new VectorIndexerArray(1,1,1);

    /**
       
     */
    public DistanceToPointSet(PointSet points, double maxInDistance, double maxOutDistance, int subvoxelResolution){
        m_points = points;
        m_subvoxelResolution = subvoxelResolution;
        m_maxInDistance = maxInDistance;
        m_maxOutDistance = maxOutDistance;
        
    }
    
    /**
       sets object to be used for inside/outside detection
       it is needed if we want to calculate signed distance function
     */
    public void setInsideTester(InsideTester tester){
        m_insideTester = tester;
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
        m_gsx = 1/vs;
        m_gsy = m_gsx;
        m_gsz = m_gsx;

        m_gtx = -m_bounds[0]/vs - 0.5; // half voxel shift 
        m_gty = -m_bounds[2]/vs - 0.5;
        m_gtz = -m_bounds[4]/vs - 0.5;
        
        double maxOut = m_maxOutDistance/m_voxelSize;
        double maxIn = m_maxOutDistance/m_voxelSize;
        m_maxDistVoxels = Math.max(maxOut, maxIn);
        if(DEBUG)printf("maxDist: %6.2f\n",m_maxDistVoxels);
        
        
    }

    public void makeDistanceGrid(AttributeGrid grid){
        if(DEBUG) printf("makeDistanceGrid(%s)\n",grid);

        commonInit(grid);


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

    void makeDistanceExact(){

        if(DEBUG)printf("makeDistanceExact()\n");

        int neig[] = makeBallNeighbors((int)Math.ceil(m_maxDistVoxels));
        if(DEBUG)printf("neighbors count: %d\n",neig.length/3);
        int count = m_points.size();
        Point3d pnt = new Point3d();

        int kmax = neig.length;

        for(int i = 0; i < count; i++){

            m_points.getPoint(i, pnt);
            getGridCoord(pnt);
            int 
                cx = ifloor(pnt.x),
                cy = ifloor(pnt.y),
                cz = ifloor(pnt.z);

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
                    // outside 
                    if(dist < d){
                        m_grid.setAttribute(ix, iy, iz, dist);
                    }
                } else {
                    // inside 
                    if(dist > d){
                        m_grid.setAttribute(ix, iy, iz, dist);
                    }
                }
            }
        }
    }

    /**
       build distance in layer around PointSet
       closestPoints grid keeps new added point has associated index to the closest point from the PointSet 
       
     */
    void makeDistanceLayered(){

        if(DEBUG) printf("makeDistanceLayered()\n");
        VectorIndexer closestPoints = m_vectorIndexerTemplate.createEmpty(m_nx, m_ny, m_nz);
        //fillGrid(closestPoints, -1);
        GridBit freshLayer = new GridBitIntervals(m_nx, m_ny, m_nz);
        GridBit nextLayer = new GridBitIntervals(m_nx, m_ny, m_nz);
        
        double layerThickness = 1.9;//2.9;//1.9;  // to make 26 neig
        double firstLayerThickness = 1.9;

        int neigFirst[] = makeBallNeighbors(firstLayerThickness);
        int neig[] = makeBallNeighbors(layerThickness);
        if(DEBUG) printf("neig count: %d\n", neig.length/3);
            
        // 1) make fresh layer around PointSet         
        // 2) for each point in fresh layer make fresh layer around PointSet 
        makeFirstLayer(neigFirst, closestPoints, freshLayer);        
        
        if(DEBUG_GRID){
            printf("distance after first layer:\n");
            printSlice(m_grid,m_nz/2);
            //printf("fresh layer:\n");
            //printSlice((AttributeGrid)freshLayer,m_nz/2);
            //printf("closest points:\n");
            //printSlice(closestPoints,m_nx, m_ny, m_nz, m_nz/2);
        }
        int iter = (int)Math.ceil(m_maxDistVoxels/layerThickness)+5;
        //int iter = iround(m_maxDistVoxels/2);
        if(DEBUG)printf("iterations: %d\n", iter);
        for(int k = 0; k < iter; k++){
            if(DEBUG)printf("iteration: %d\n", (k+1));
            makeNextLayer(neig, closestPoints, freshLayer, nextLayer);
            if(DEBUG_GRID){
                printf("distance after next layer:\n");
                printSlice(m_grid,m_nz/2);
                //printf("next layer:\n");
                //printSlice((AttributeGrid)nextLayer,m_nz/2);
                //printf("closest points:\n");
                //printSlice(closestPoints,m_nx, m_ny, m_nz, m_nz/2);
            }             
            GridBit t = freshLayer;
            freshLayer = nextLayer;
            nextLayer = t;
            nextLayer.clear();
        }
    }

    /**
       creates first fresh layer around original points 
     */
    void makeFirstLayer(int neig[], VectorIndexer closestPoints, GridBit freshLayer){

        int kmax = neig.length;
        int count = m_points.size();
        Point3d pnt = new Point3d();

        for(int pntIndex = 0; pntIndex < count; pntIndex++){
            m_points.getPoint(pntIndex, pnt);
            getGridCoord(pnt);
            int 
                cx = ifloor(pnt.x),
                cy = ifloor(pnt.y),
                cz = ifloor(pnt.z);

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
                    // outside 
                    if(dist < d){
                        m_grid.setAttribute(ix, iy, iz, dist);
                        closestPoints.set(ix, iy, iz, pntIndex);
                        freshLayer.set(ix, iy, iz, 1);
                    }
                } else {
                    // inside 
                    if(dist > d){
                        m_grid.setAttribute(ix, iy, iz, dist);
                        closestPoints.set(ix, iy, iz, pntIndex);
                        freshLayer.set(ix, iy, iz, 1);
                    }
                }
            }
        }
    }

    void makeNextLayer(int neig[], VectorIndexer closestPoints, GridBit oldLayer, GridBit freshLayer){

        int kmax = neig.length;
        int count = m_points.size();
        Point3d pnt = new Point3d();

        // for each point old layer build ball neighborhood 
        // and update distances in grid point 
        // distance in each point s calculated to the closest boundary point stored in closestPoints
        //
        for(int y = 0; y < m_ny; y++){
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
                        int dist = distance(pnt.x,pnt.y,pnt.z,ix,iy,iz);
                        int d = L2S(m_grid.getAttribute(ix, iy, iz));
                        if(d >=0){
                            // outside 
                            if(dist < d){
                                m_grid.setAttribute(ix, iy, iz, dist);
                                closestPoints.set(ix, iy, iz, pntIndex);
                                freshLayer.set(ix, iy, iz, 1);
                            }
                        } else {
                            // inside 
                            if(dist > d){
                                m_grid.setAttribute(ix, iy, iz, dist);
                                closestPoints.set(ix, iy, iz, pntIndex);
                                freshLayer.set(ix, iy, iz, 1);
                            }
                        }                        
                    }                        
                }
            }
        }
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
       convert point world coordinates into grid coordinates 
     */
    void getGridCoord(Tuple3d pnt){

        pnt.x = m_gsx * pnt.x + m_gtx;
        pnt.y = m_gsy * pnt.y + m_gty;
        pnt.z = m_gsz * pnt.z + m_gtz;

    }

    /**
     returns array of neighbors of a point in a ball or radius @radius
     radius is expressed in voxels
     */
    static int[] makeBallNeighbors(double radius){

        int radius2 = (int)(radius*radius); // compare against radius squared
        int iradius = (int)(radius+1);

        // calculate size needed 
        int count = 0;
        for(int x = -iradius; x <= iradius; x++){
            for(int y = -iradius; y <= iradius; y++){
                for(int z = -iradius; z <= iradius; z++){
                    double d2 = x*x + y*y + z*z;
                    if(d2 <= radius2)
                        count += 3;
                }
            }
        }
        int neig[] = new int[count];

        // store data in array
        count = 0;
        for(int x = -iradius; x <= iradius; x++){
            for(int y = -iradius; y <= iradius; y++){
                for(int z = -iradius; z <= iradius; z++){
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
        return neig;
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

    static void printSlice(VectorIndexer vi, int nx, int ny, int nz, int z){

        printf("vi:[ %d x %d x %d] slice %d\n",nx,ny,nz,z);

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                int d = vi.get(x,y,z);
                printf("%5d", d); break;                
            }
            printf("\n");
        }
    }


}