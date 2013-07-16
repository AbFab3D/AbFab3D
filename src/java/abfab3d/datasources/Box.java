/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.datasources;


//import java.awt.image.Raster;

import java.util.Vector;

import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;


import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;

import abfab3d.util.PointToTriangleDistance;

import static java.lang.Math.sqrt;
import static java.lang.Math.atan2;
import static java.lang.Math.abs;

import static abfab3d.util.Output.printf;


import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.intervalCap;
import static abfab3d.util.MathUtil.step10;

import static abfab3d.util.Units.MM;


/**

   makes solid box of given size
       
   @author Vladimir Bulatov

 */
public class Box extends TransformableDataSource {
    
    private double m_sizeX=0.1, m_sizeY=0.1, m_sizeZ=0.1, m_centerX=0, m_centerY=0, m_centerZ=0;
    
    private double
        xmin,
        xmax,
        ymin,
        ymax,
        zmin, zmax;
    
    protected boolean
        m_hasSmoothBoundaryX = false,
        m_hasSmoothBoundaryY = false,
        m_hasSmoothBoundaryZ = false;
    
    public Box(){
    }
    
    /**
       makes block with given center and size
    */
    public Box(double x, double y, double z, double sx, double sy, double sz){
        
        setLocation(x,y,z);
        setSize(sx, sy, sz);
        
    }
    
    
    public void setSmoothBoundaries(boolean boundaryX,boolean boundaryY,boolean boundaryZ){
        m_hasSmoothBoundaryX = boundaryX;
        m_hasSmoothBoundaryY = boundaryY;
        m_hasSmoothBoundaryZ = boundaryZ;
    }
    
    /**
       
     */
    public void setSize(double sx, double sy, double sz){
        m_sizeX = sx;
        m_sizeY = sy;
        m_sizeZ = sz;
    }
    
    /**
       
     */
    public void setLocation(double x, double y, double z){
        m_centerX = x;
        m_centerY = y;
        m_centerZ = z;
    }
    
    /**
       
     */
    public int initialize(){

        super.initialize();

        xmin = m_centerX - m_sizeX/2;
        xmax = m_centerX + m_sizeX/2;
        
        ymin = m_centerY - m_sizeY/2;
        ymax = m_centerY + m_sizeY/2;
        
        zmin = m_centerZ - m_sizeZ/2;
        zmax = m_centerZ + m_sizeZ/2;
        
        return RESULT_OK;
        
    }
    
    /**
     * returns 1 if pnt is inside of block of given size and location
     * returns 0 otherwise
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);

        double res = 1.;
        double
            x = pnt.v[0],
            y = pnt.v[1],
            z = pnt.v[2];
        
        double vs = pnt.getVoxelSize() * pnt.getScaleFactor();
        
        if(vs == 0.){
            // zero voxel size
            if(x < xmin || x > xmax ||
               y < ymin || y > ymax ||
               z < zmin || z > zmax ){
                data.v[0] = 0.;
                return RESULT_OK;
            } else {
                data.v[0] = 1.;
                return RESULT_OK;
            }
        } else {
            
            // finite voxel size
            
            if(x <= xmin - vs || x >= xmax + vs ||
               y <= ymin - vs || y >= ymax + vs ||
               z <= zmin - vs || z >= zmax + vs ){
                data.v[0] = 0.;
                return RESULT_OK;
            }
            double finalValue = 1;
            
            if(m_hasSmoothBoundaryX)
                finalValue = Math.min(finalValue, intervalCap(x, xmin, xmax, vs));
            if(m_hasSmoothBoundaryY)
                finalValue = Math.min(finalValue, intervalCap(y, ymin, ymax, vs));
            if(m_hasSmoothBoundaryZ)
                finalValue = Math.min(finalValue, intervalCap(z, zmin, zmax, vs));
            
            data.v[0] = finalValue;
            return RESULT_OK;
        }
    }
    
}  // class Box
