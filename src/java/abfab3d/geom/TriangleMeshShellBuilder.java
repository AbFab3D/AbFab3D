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
   build narrow shell around triangle mesh and calculated closes point in the mesh to each voxel in the shell

   Distances are calculated on a grid with given bounds and given voxel size. 
   
   Distance grid is initialized to undefined max value 
   Each triangle is voxelized in best of 3 orietations.
   In the neighborhood of predefined radius around each voxel we calculate the closest distance 
   to that triangle, compare it to the current shortest distance stored in a grid and select point with smaller distance. 
   closest point to that voxel is stored in voxle grid. 
   
   @author Vladimir Bulatov
 */
public class TriangleMeshShellBuilder implements TriangleCollector {

    static final boolean DEBUG = true;
    static final double TOL = 1.e-2;
    static final double HALF = 0.5; // half voxel offset to the center of voxel

    
    static final int DEFAULT_SVR = 100;
    double m_layerThickness = 1;  // 2.0, 2.25, 2.45*, 2.84, 3.0 3.17 3.33*, 3.46, 3.62, 3.74*   * - good values 
    int m_neighbors[]; // offsets to neighbors 
    int m_subvoxelResolution = DEFAULT_SVR;
    double m_voxelSize;
    Bounds m_bounds;
    // triangles ransterizer 
    TriangleRenderer m_triRenderer;

    // callback class for triangles ransterizer 
    VoxelRenderer m_voxelRenderer;

    // grid to world conversion params 
    double m_xmin, m_ymin, m_zmin, m_scale;
    // grud dimension 
    int m_nx, m_ny, m_nz;
    
    // closest points in triangles 
    PointMap m_points;
    
    // grid to store indexes of closest point 
    AttributeGrid m_indexGrid; 
    // grid to store current shortest distances 
    AttributeGrid m_distanceGrid; 

    int m_triCount = 0; // count of processed triangles 
    
    public TriangleMeshShellBuilder(AttributeGrid indexGrid, int subvoxelResolution){
        m_indexGrid = indexGrid;
        m_subvoxelResolution = subvoxelResolution;
    }
    
    public void setShellHalfThickness(double shellHalfThickness){
        m_layerThickness = shellHalfThickness;
    }

    /**
       @return count of points in triangles which contribute to the shell 
     */
    public int getPointCount(){
        return m_points.getPointCount();
    }

    public int getTriCount(){
        return m_triCount;
    }
    
    /**
       @return coordinates of the points 
     */
    public double[] getPoints(double coord[]){
        //
        // coordinates are in grid units 
        //
        coord = m_points.getPoints(coord);
        
        // transform points into world units 
        for(int i = 0; i< coord.length; i += 3){
            coord[i] = toWorldX(coord[i]);
            coord[i+1] = toWorldY(coord[i+1]);
            coord[i+2] = toWorldZ(coord[i+2]);
        }
        return coord;
    }

    /**
       
     */
    public void getPointsInGridUnits(double pntx[],double pnty[],double pntz[]){
        long t0 = System.nanoTime();
        //
        // coordinates are in grid units 
        //
        m_points.getPoints(pntx, pnty, pntz);
        printf("getPointsInGridUnits.  time: %f ms\n", (System.nanoTime() - t0) / 1e6);

    }


    /**
       this method has to be called before starting adding triangles 
     */
    public boolean initialize(){

        m_bounds = m_indexGrid.getGridBounds();
        m_voxelSize = m_indexGrid.getVoxelSize();
        m_nx = m_indexGrid.getWidth();
        m_ny = m_indexGrid.getHeight();
        m_nz = m_indexGrid.getDepth();

        m_distanceGrid = new ArrayAttributeGridShort(m_bounds, m_voxelSize,m_voxelSize);

        initDistanceGrid(m_distanceGrid, (int)(m_layerThickness * m_subvoxelResolution + 0.5));

        m_triRenderer = new TriangleRenderer();
        m_voxelRenderer = new VoxelRenderer();
        m_xmin = m_bounds.xmin;
        m_ymin = m_bounds.ymin;
        m_zmin = m_bounds.zmin;
        m_scale = 1/m_voxelSize;
        m_neighbors = Neighborhood.makeBall(m_layerThickness);

        m_points = new PointMap(m_voxelSize/m_subvoxelResolution);
        // add unused point to have index start from 1 
        m_points.add(0, 0, 0);
        if(false){
            // axes of grid for visualization 
            for(int i = 1; i <= m_nx; i++)
                m_points.add(i, 0, 0);
            for(int i = 1; i <= m_ny; i++)
                m_points.add(0, i, 0);
            for(int i = 1; i <= m_nz; i++)
                m_points.add(0, 0, i);
        }
        if(DEBUG) {
            printf("layerThickness: %5.2f\n",m_layerThickness);
            printf("neighboursCount: %d\n",m_neighbors.length/3);
            //for(int i = 0; i < m_neighbors.length; i+= 3){
                //printf("(%2d %2d %2d)\n", m_neighbors[i],m_neighbors[i+1],m_neighbors[i+2]);
            //}
        }
        // successfull initialization 
        return true;
    }

    void initDistanceGrid(AttributeGrid grid, long value){
        if(grid instanceof ArrayAttributeGridShort){
            ((ArrayAttributeGridShort)grid).fill(value);
        }
    }

    
    Vector3d // work vectors 
        v0 = new Vector3d(),
        v1 = new Vector3d(),
        v2 = new Vector3d(),
        m_v1 = new Vector3d(),
        m_v2 = new Vector3d(),
        m_normal = new Vector3d();
    
    /**
       method of interface TriangleCollector 
       
     */
    public boolean addTri(Vector3d p0, Vector3d p1, Vector3d p2){
        m_triCount++;
        v0.set(p0);
        v1.set(p1);
        v2.set(p2);

        toGrid(v0);
        toGrid(v1);
        toGrid(v2);
        if(false) printf("addTri([%4.1f, %4.1f, %4.1f], [%4.1f, %4.1f, %4.1f], [%4.1f, %4.1f,%4.1f] )\n",v0.x,v0.y,v0.z,v1.x,v1.y,v1.z,v2.x,v2.y,v2.z);
        m_v1.sub(v1, v0);
        m_v2.sub(v2, v0);
        m_normal.cross(m_v2,m_v1);
        double 
            nv0 = m_normal.dot(v0),
            nx = m_normal.x,
            ny = m_normal.y,
            nz = m_normal.z,
            anx = abs(nx),
            any = abs(ny),
            anz = abs(nz);        
        if(false) printf("normal: [%4.1f, %4.1f, %4.1f]\n",nx, ny, nz);

        // select best axis to rasterize 
        // it is axis which has longest normal projection 
        int axis = 2;
        if(anx >= any) {
            if(anx >= anz) axis = 0;
            else axis = 2;
        } else { // anx < any 
            if(any > anz) axis = 1;
            else axis = 2;            
        }
        //printf("axis: %d\n", axis);
        //axis = 2;
        m_voxelRenderer.setAxis(axis);
        m_voxelRenderer.setTriangle(v0, v1, v2);
        // pass plane equation to voxelRenderer 
        // pass triangle to voxel renderer 
        switch(axis){
        default:
        case 0: 
            m_voxelRenderer.setPlane(-ny/nx, -nz/nx, nv0/nx); 
            m_triRenderer.fillTriangle(m_voxelRenderer, v0.y, v0.z,v1.y, v1.z, v2.y, v2.z);
            break;
        case 1: 
            m_voxelRenderer.setPlane(-nz/ny, -nx/ny, nv0/ny); 
            m_triRenderer.fillTriangle(m_voxelRenderer, v0.z, v0.x,v1.z, v1.x, v2.z, v2.x);
            break;
        case 2: 
            m_voxelRenderer.setPlane(-nx/nz, -ny/nz, nv0/nz); 
            m_triRenderer.fillTriangle(m_voxelRenderer, v0.x, v0.y,v1.x, v1.y, v2.x, v2.y);
            break;
        }
        //TODO 
        // add triagle vertices to deal with super small triangles 
        m_voxelRenderer.addNeighborhood((int)v0.x, (int)v0.y, (int)v0.z);
        m_voxelRenderer.addNeighborhood((int)v1.x, (int)v1.y, (int)v1.z);
        m_voxelRenderer.addNeighborhood((int)v2.x, (int)v2.y, (int)v2.z);
        
        // add triangle edges to deal with super thin triangles 

        if(false){
            printf("addTri() area: %7.1f pointsCount: %d distCalcCount: %d\n", m_normal.length()/2, m_points.getPointCount(),m_voxelRenderer.voxelCount);
        }
        return true;
    }

    final void toGrid(Vector3d v){
        v.x = toGridX(v.x);
        v.y = toGridY(v.y);
        v.z = toGridZ(v.z);
    }

    final double toGridX(double x){
        return (x - m_xmin)*m_scale;
    }
    final double toGridY(double y){
        return (y - m_ymin)*m_scale;
    }
    final double toGridZ(double z){
        return (z - m_zmin)*m_scale;
    }
    final double toWorldX(double x){
        return (x*m_voxelSize + m_xmin);
    }
    final double toWorldY(double y){
        return (y*m_voxelSize + m_ymin);
    }
    final double toWorldZ(double z){
        return (z*m_voxelSize + m_zmin);
    }

        
    class VoxelRenderer implements TriangleRenderer.PixelRenderer {

        double m_ax, m_ay, m_az;
        int m_axis = 2;
        int voxelCount = 0;
        double maxDist = 0;

        Vector3d 
            v0 = new Vector3d(),
            v1 = new Vector3d(),
            v2 = new Vector3d();
        double pointInTriangle[] = new double[3];

        final void setAxis(int axis){
            m_axis = axis;
        }
        final void setTriangle(Vector3d v0, Vector3d v1,Vector3d v2){
            this.v0.set(v0);
            this.v1.set(v1);
            this.v2.set(v2);
        }

        final void setPlane(double ax, double ay, double az){
            m_ax = ax;
            m_ay = ay;
            m_az = az;
        }

        public void setPixel(int ix, int iy){
            
            double z = m_ax*ix + m_ay*iy + m_az;
            int iz = (int)(z); // truncation instead of round is done because of half voxel shift 
            if(false) printf("setPixel(%2d, %2d) z: %4.1f \n", ix, iy, z);

            int x0,y0,z0;
            switch(m_axis){
            default: 
            case 0: x0 = iz;y0 = ix; z0 = iy; break;
            case 1: x0 = iy;y0 = iz; z0 = ix; break;
            case 2: x0 = ix;y0 = iy; z0 = iz; break;
            }
            addNeighborhood(x0,y0,z0);

        }   
        
        final void addNeighborhood(int x0, int y0, int z0){
            
            // scan over neighborhood of the voxel 
            int ncount = m_neighbors.length;
            
            for(int i = 0; i < ncount; i+=3){
                int 
                    vx = x0 + m_neighbors[i],
                    vy = y0 + m_neighbors[i+1],
                    vz = z0 + m_neighbors[i+2];                    
                voxelCount++;
                if( vx >= 0 && vy >= 0 & vz >= 0 && vx < m_nx && vy < m_ny && vz < m_nz){
                    double d2 = PointToTriangleDistance.getSquared(vx+HALF, vy+HALF, vz+HALF,
                                                                   v0.x,v0.y,v0.z, v1.x,v1.y,v1.z, v2.x,v2.y,v2.z, 
                                                                   pointInTriangle);
                    double dist = sqrt(d2);
                    if(dist > maxDist) maxDist = dist;
                    
                    if(dist <= m_layerThickness + TOL) {
                        int newdist = (int)(dist*m_subvoxelResolution + 0.5);
                        int olddist = (int)m_distanceGrid.getAttribute(vx, vy, vz);
                        if(newdist < olddist){
                            // better point found
                            m_distanceGrid.setAttribute(vx, vy, vz, newdist);
                            int index = m_points.add(pointInTriangle[0],pointInTriangle[1],pointInTriangle[2]);
                            m_indexGrid.setAttribute(vx, vy, vz, index);
                        }
                        //if(false) printf("     v: (%2d %2d %2d) dist: %5.2f pnt: [%5.2f, %5.2f,%5.2f] index: %3d \n", 
                        //                 vx, vy, vz, dist, pointInTriangle[0],pointInTriangle[1],pointInTriangle[2], index);
                    }
                }
            }                    
        }
        
    } //  class VoxelRenderer 

}