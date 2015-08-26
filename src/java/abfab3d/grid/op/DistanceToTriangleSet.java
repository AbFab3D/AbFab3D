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
import abfab3d.grid.VectorIndexer;
import abfab3d.grid.VectorIndexerArray;
import abfab3d.grid.ArrayInt;



import abfab3d.grid.util.ExecutionStoppedException;

import abfab3d.util.Bounds;
import abfab3d.util.TriangleCollector;
import abfab3d.util.PointSet;
import abfab3d.util.PointSetArray;
import abfab3d.util.TriangleRenderer;


import static java.lang.Math.sqrt;
import static java.lang.Math.max;
import static java.lang.Math.abs;
import static java.lang.Math.min;

import static abfab3d.util.Output.time;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.MathUtil.L2S;


/**
   calculates distance to set of triangles in the narrow layer around triangle. 

   Distances are calculated on a grid with given bounds and given voxel size. 
   
   Distance grid is initialized to undefined max value 
   Each triangle is voxelized in one of 3 orietations.
   In the neighborhood of predefined radius about each voxel we calculate the closest distance 
   to that triangle, compare it to the current shortest distance stored in grid and select smaller distance. 
   and update closest point to that grid voxel. 
   
   @author Vladimir Bulatov
 */
public class DistanceToTriangleSet implements TriangleCollector {

    static final boolean DEBUG = true;
    
    static final int DEFAULT_SVR = 100;
    double m_layerThickness = 1;// 2.0, 2.25, 2.45*, 2.84, 3.0 3.17 3.33*, 3.46, 3.62, 3.74*   * - good values 

    int m_subvoxelResolution = DEFAULT_SVR;
    double m_voxelSize;
    Bounds m_bounds;
    AttributeGrid m_distanceGrid; 

    TriangleRenderer m_triRenderer;
    VoxelRenderer m_voxelRenderer;
    double m_xmin, m_ymin, m_zmin, m_scale;

    
    public DistanceToTriangleSet(Bounds bounds, double voxelSize, int subvoxelResolution){
        m_bounds = bounds;
        m_voxelSize = voxelSize;
        m_subvoxelResolution = subvoxelResolution;
        init();
    }

    void init(){

        m_distanceGrid = new ArrayAttributeGridShort(m_bounds, m_voxelSize,m_voxelSize);
        m_triRenderer = new TriangleRenderer();
        m_voxelRenderer = new VoxelRenderer();
        m_xmin = m_bounds.xmin;
        m_ymin = m_bounds.ymin;
        m_zmin = m_bounds.zmin;
        m_scale = 1/m_voxelSize;

    }
    
    public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2){
 
        
        m_triRenderer.fillTriangle(m_voxelRenderer, 
                                   toGridX(v0.x), toGridZ(v0.y), 
                                   toGridX(v1.x), toGridY(v1.y), 
                                   toGridX(v2.x), toGridY(v2.y));
        
        return true;
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
 
    class VoxelRenderer implements TriangleRenderer.PixelRenderer {
        
        public void setPixel(int x, int y){
            if(DEBUG) printf("setPixel(%2d, %2d)\n", x, y);
        }       
    }
  
}