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

package abfab3d.grid.op;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;

import java.util.concurrent.atomic.AtomicInteger;

import javax.vecmath.Vector3d; 

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
import abfab3d.grid.ArrayAttributeGridShort;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.VectorIndexer;
import abfab3d.grid.VectorIndexerArray;
import abfab3d.grid.ArrayInt;

import abfab3d.grid.op.Neighborhood;
import abfab3d.grid.util.ExecutionStoppedException;

import abfab3d.util.Bounds;
import abfab3d.util.TriangleCollector;
import abfab3d.util.PointSet;
import abfab3d.util.PointSetArray;
import abfab3d.util.TriangleRenderer;
import abfab3d.util.PointToTriangleDistance;
import abfab3d.util.PointMap;

import static java.lang.Math.sqrt;
import static java.lang.Math.max;
import static java.lang.Math.abs;
import static java.lang.Math.min;

import static abfab3d.util.Output.time;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.MathUtil.L2S;
import static abfab3d.util.MathUtil.iround;


/**
   build narrow shell around PointSet

   indices of points closest to given grid point are stored in the indexGrid


   @author Vladimir Bulatov
 */
public class PointSetShellBuilder implements AttributeOperation {

    static final boolean DEBUG = true;
    static final double TOL = 1.e-2;
    static final double HALF = 0.5; // half voxel offset to the center of voxel
    
    static final int DEFAULT_SVR = 50;
    double m_layerThickness = 1;  // 2.0, 2.25, 2.45*, 2.84, 3.0 3.17 3.33*, 3.46, 3.62, 3.74*   * - good values 
    int m_neighbors[]; // offsets to neighbors 
    long m_subvoxelResolution = DEFAULT_SVR;
    double m_voxelSize;
    Bounds m_bounds;

    // grid to world conversion params 
    double m_xmin, m_ymin, m_zmin, m_scale;
    // grud dimension 
    int m_nx, m_ny, m_nz;
    
    PointSet m_points;
        // grid to store indexes of closest point 
    AttributeGrid m_indexGrid; 
    // grid to store current shortest distances 
    AttributeGrid m_distanceGrid; 
    
    public PointSetShellBuilder(){

    }

    /**
       @param points set of points. Coordinates of points should be in grid units. 
       
     */
    public PointSetShellBuilder(PointSet points){

        m_points = points;

    }

    /*
       @param points set of points. Coordinates of points should be in grid units. 

     */
    public void setPoints(PointSet points){
        m_points = points;
    }

    public void setShellHalfThickness(double shellHalfThickness){

        m_layerThickness = shellHalfThickness;

    }
   
    public AttributeGrid execute(AttributeGrid indexGrid) {
        
        m_indexGrid = indexGrid;

        init();
        calculateShell();

        return m_indexGrid;

    }

    public AttributeGrid getDistanceGrid(){
        return m_distanceGrid;
    }

    protected void init(){

        m_bounds = m_indexGrid.getGridBounds();
        m_voxelSize = m_indexGrid.getVoxelSize();
        m_nx = m_indexGrid.getWidth();
        m_ny = m_indexGrid.getHeight();
        m_nz = m_indexGrid.getDepth();

        m_distanceGrid = new ArrayAttributeGridByte(m_bounds, m_voxelSize,m_voxelSize);
        
        m_xmin = m_bounds.xmin;
        m_ymin = m_bounds.ymin;
        m_zmin = m_bounds.zmin;
        m_scale = 1/m_voxelSize;

        m_neighbors = Neighborhood.makeBall(m_layerThickness);

        if(DEBUG) {
            printf("layerThickness: %5.2f\n",m_layerThickness);
            printf("neighboursCount: %d\n",m_neighbors.length/3);
        }
    }

    
    protected void calculateShell(){

        int npnt = m_points.size();
        Vector3d pnt = new Vector3d();
        
        for(int i = 1; i < npnt; i++){// start from 1. Index 0 means undefined
            
            m_points.getPoint(i, pnt);
            processNeighborhood(i, pnt.x, pnt.y, pnt.z);
        }
    }
        
    final void processNeighborhood(int pointIndex, double x, double y, double z){
        
        int 
            x0 = (int)x,
            y0 = (int)y,
            z0 = (int)z;
        //printf("point: %d, (%6.2f,%6.2f,%6.2f): (%2d %2d %2d)\n", pointIndex, x,y,z, x0, y0, z0);
        // scan over neighborhood of the voxel 
        int ncount = m_neighbors.length;
        
        for(int i = 0; i < ncount; i+=3){
            int 
                vx = x0 + m_neighbors[i],
                vy = y0 + m_neighbors[i+1],
                vz = z0 + m_neighbors[i+2];                    
            if( vx >= 0 && vy >= 0 & vz >= 0 && vx < m_nx && vy < m_ny && vz < m_nz){
                double 
                    dx = (vx - x),
                    dy = (vy - y),
                    dz = (vz - z);

                //printf("dx: (%6.2f,%6.2f,%6.2f)\n", dx, dy, dz);
                double d2 = dx*dx + dy*dy + dz*dz;
                // distances are flipped ! 
                // this is to make initial grid 0 
                // distance at points are m_layerThickness
                double dist = m_layerThickness+1 - sqrt(d2);
                
                if(dist > 0 ) {
                    long newdist = iround(dist*m_subvoxelResolution);
                    long olddist = (m_distanceGrid.getAttribute(vx, vy, vz));
                    if(newdist > olddist){
                        // better point found
                        m_distanceGrid.setAttribute(vx, vy, vz, newdist);
                        m_indexGrid.setAttribute(vx, vy, vz, pointIndex);
                    }
                }
            }
        }                    
    } // add neighborhood     

    static final int iround(double x) {
        return (int)(x + 0.5);
    }

}