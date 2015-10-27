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

import abfab3d.grid.AttributeChannel;
import abfab3d.grid.Grid2D;
import abfab3d.grid.Grid2DInt;
import abfab3d.grid.Grid2DShort;
import abfab3d.grid.Operation2D;
import abfab3d.grid.util.GridUtil;

import abfab3d.grid.util.ExecutionStoppedException;

import abfab3d.util.AbFab3DGlobals;
import abfab3d.util.PointSet;
import abfab3d.util.PointSetArray;
import abfab3d.util.MathUtil;
import abfab3d.util.PointMap;
import abfab3d.util.Bounds;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;

import static abfab3d.grid.Grid.OUTSIDE;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;
import static java.lang.Math.round;
import static abfab3d.util.MathUtil.iround;

/**
 *
 * calculates signed distance data of 2D shape represened on the Grid2D 
 * 
 * the shape is given via implicit equation AttributeChannel.getValue(grid.getAttribute(x,y)) = threshold
 *  
 * distance inside shape is negative 
 * distance outside shape is positive
 *
 * interior pixels not reached by maxInDistance are initialized to -maxInDstance
 * exterior pixels not reached by maxOutDistance are initialized to maxOutDstance
 *
 * @author Vladimir Bulatov
 */
public class DistanceTransform2D implements Operation2D {

    public static boolean DEBUG = true;
    public static boolean DEBUG_TIMING = true;
    static int debugCount = 100;
    static final double TOL = 1.e-2;
    static final double HALF = 0.5; // half voxel offset to the center of voxel

    double m_layerThickness = 5.;  
    int m_neighbors[]; // offsets to neighbors 

    double m_maxInDistance = 0;
    double m_maxOutDistance = 0;

    long m_defaultInValue;
    long m_defaultOutValue;

    int m_subvoxelResolution = 100;
    int nx, ny, nz;
    int m_surfaceValue;
    // center of corner voxel (0,0,0)
    double m_xmin, m_ymin, m_zmin;
    double m_voxelSize;
    // surface threshold
    double m_threshold;
    int m_nx, m_ny;
    
    // number of threads to use in MT processing 
    //int m_threadCount = 1;

    AttributeChannel m_dataChannel;

    PointMap m_points;
    Grid2D m_indexGrid;
    Grid2D m_distanceGrid;

    /**
     @param inDistance maximal distance to calculate transform inside of the shape. Measured in meters
     @param outDistance maximal distance to calculate transform outside of the shape. Measured in meters
    */
    public DistanceTransform2D(double maxInDistance, double maxOutDistance, double threshold) {

        //m_dataChannel = dataChannel;
        m_maxInDistance = maxInDistance;
        m_maxOutDistance = maxOutDistance;
        m_threshold = threshold;

    }

    public void setDataChannel(AttributeChannel dataChannel){
        m_dataChannel = dataChannel;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid A.
     * @return new grid with distance transform data
     */
    public Grid2D execute(Grid2D grid) {

        long t0 = time();
        if(DEBUG)printf("DistanceTransform2D.execute(%s)\n", grid.getClass().getName());
        if(DEBUG)printf("  m_inDistance: %7.3f mm  m_outDistance: %7.3f mm \n", m_maxInDistance/MM, m_maxOutDistance/MM);
        m_voxelSize = grid.getVoxelSize();
        m_nx = grid.getWidth();
        m_ny = grid.getHeight();
        
        m_neighbors = Neighborhood.makeDisk(m_layerThickness);

        m_indexGrid = new Grid2DInt(m_nx, m_ny, m_voxelSize);
        m_distanceGrid = new Grid2DShort(m_nx, m_ny, m_voxelSize);
        //GridUtil.fill(m_distanceGrid, (long)((m_maxOutDistance/m_voxelSize) * m_subvoxelResolution));
        GridUtil.fill(m_distanceGrid, (long)(m_layerThickness * m_subvoxelResolution));

        m_points = new PointMap(1./m_subvoxelResolution);
        m_points.add(0, 0, 0);
        
        // find surface points 
        // initialize distanceas in thin layer around surface 
        initializeSurfaceLayer(grid);
        if(DEBUG) {
            int pcnt = m_points.getPointCount();
            printf("points count: %d\n", pcnt);
            double px[] = new double[pcnt];
            double py[] = new double[pcnt];
            m_points.getPoints(px, py);
            
            for(int i = 1; i < pcnt; i++){
                // start from 1, pnt[0] - is dummy 
                printf("(%5.2f %5.2f)\n", px[i],py[i]);
            }
        }
        // distribute distances to the whole grid 

        if(DEBUG)printf("DistanceTransformIndexed2D.execute() time: %d ms\n", (time() - t0));
        
        return null;

    }

    public Grid2D getIndexGrid(){
        return m_indexGrid;
    }
    public Grid2D getDistanceGrid(){
        return m_distanceGrid;
    }

    void initializeSurfaceLayer(Grid2D dataGrid){
        int nx = dataGrid.getWidth();
        int ny = dataGrid.getHeight();
        AttributeChannel dataConverter = m_dataChannel;
        int nx1 = nx-1;
        int ny1 = ny-1;
        double vs = dataGrid.getVoxelSize();
        Bounds bounds = dataGrid.getGridBounds();
        double xmin = bounds.xmin;
        double ymin = bounds.ymin;
        double th = m_threshold;


        for(int iy = 0; iy < ny1; iy++){
            double y = iy + HALF;
            for(int ix = 0; ix < nx1; ix++){
                double x = ix + HALF;
                double v0 = dataConverter.getValue(dataGrid.getAttribute(ix,iy))-th;
                double vx = dataConverter.getValue(dataGrid.getAttribute(ix+1,iy))-th;
                double vy = dataConverter.getValue(dataGrid.getAttribute(ix,iy+1))-th;

                if((v0 <= 0. && vx > 0) || (v0 >= 0. && vx < 0)){
                    // add point on x segment 
                    addPoint(x+coeff(v0, vx), y);                    
                }
                if((v0 <= 0. && vy > 0) || (v0 >= 0. && vy < 0)){
                    // add point on y segment 
                    addPoint(x, y+coeff(v0, vy));                    
                }
            }
        }
    } 

    /**
       root of linear map (0,1) -> (v0, v1) 
       v0 + x*(v1-v0) = 0
     */
    final double coeff(double v0, double v1){
        return v0/(v0-v1);
    }

    // add point to the neigborhood in indexGrid 
    // and update layerDistanceGrid 
    final void addPoint(double px, double py){

        printf("addPoint(%7.4f, %7.4f)\n", px, py);
        int x0 = iround(px);
        int y0 = iround(py);
        int index = 0;

        int ncount = m_neighbors.length;
        
        for(int i = 0; i < ncount; i+=3){
            
            int 
                vx = x0 + m_neighbors[i],
                vy = y0 + m_neighbors[i+1];
            
            if( vx >= 0 && vy >= 0 && vx < m_nx && vy < m_ny ){
                double 
                    dx = vx + HALF - px,
                    dy = vy + HALF - py;
                double dist = sqrt(dx*dx + dy*dy);
                if(dist <= m_layerThickness-TOL) {
                    int newdist = (int)(dist*m_subvoxelResolution + 0.5);
                    int olddist = (int)m_distanceGrid.getAttribute(vx, vy);
                    if(newdist < olddist){
                        // better point found
                        m_distanceGrid.setAttribute(vx, vy, newdist);
                        if(index == 0){
                            // point is being added first time 
                            index = m_points.add(px, py, 0.);
                            printf("adding new point (%7.4f, %7.4f) %d \n", px, py, index);
                        }
                        m_indexGrid.setAttribute(vx, vy, index);
                    }                    
                }
            }        
        }
    }

    void distributeDistances(Grid2D indexGrid, Grid2D distanceGrid){
        
    } 
    
} // class DistanceTransformIndexed2D 
