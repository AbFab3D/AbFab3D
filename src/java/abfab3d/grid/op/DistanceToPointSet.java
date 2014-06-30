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

import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;

import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Operation;
import abfab3d.grid.AttributeOperation;

import abfab3d.grid.util.ExecutionStoppedException;
import abfab3d.util.PointSet;
import abfab3d.transforms.Identity;

import static abfab3d.util.Output.time;
import static abfab3d.util.Output.printf;


/**
   class fills grid with values of signed distance to a point set 
   signed distance is negative inside of the object, positive outisde of the object and zero at points on surface of the shape    
   if signed distance is needed, the inside/outside tester should be supplied

   @author Vladimir Bulatov
 */
public class DistanceToPointSet implements Operation, AttributeOperation {
    
    public static final boolean DEBUG = true;

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
    
    /**
       
     */
    public DistanceToPointSet(PointSet points, double maxInDistance, double maxOutDistance){
        
    }
    
    /**
       sets object to be used for inside/outside detection
       it is needed if we want to calculate signed distance function
     */
    public void setInsideTester(InsideTester tester){
        m_insideTester = tester;
    }

    public Grid execute(Grid grid) {
        makeDistanceGrid((AttributeGrid)grid);
        return grid;
    }
    
    public AttributeGrid execute(AttributeGrid grid) {
        makeDistanceGrid(grid);
        return grid;
    }
    
    void init(AttributeGrid grid){

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
        m_gtx = -m_bounds[0]/vs;
        m_gty = -m_bounds[2]/vs;
        m_gtz = -m_bounds[4]/vs;
                
    }

    public void makeDistanceGrid(AttributeGrid grid){

        init(grid);
        if(DEBUG) printf("makeDistanceGrid(%d %d %d)\n",m_nx, m_ny, m_nz);
            
        if(DEBUG) printf("init outside\n");
        for(int y = 0; y < m_ny; y++){
            for(int x = 0; x < m_nx; x++){
                for(int z = 0; z < m_nz; z++){
                    if(m_insideTester != null){
                        if(m_insideTester.isInside(x,y,z))
                            grid.setAttribute(x,y,z,defaultInValue);
                        else 
                            grid.setAttribute(x,y,z,defaultOutValue);
                    } else { // no tester - default outside 
                        grid.setAttribute(x,y,z,defaultOutValue);                        
                    }                        
                }
            }
        }
        if(DEBUG) printf("done init outside\n");
    }

    /**
       convert point world coordinates into grid coordinates 
     */
    void getGridCoord(Tuple3d pnt){

        pnt.x = m_gsx * pnt.x + m_gtx;
        pnt.y = m_gsy * pnt.y + m_gty;
        pnt.z = m_gsz * pnt.z + m_gtz;

    }
}