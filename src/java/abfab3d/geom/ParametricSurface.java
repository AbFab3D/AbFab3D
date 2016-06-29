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

package abfab3d.geom;

import javax.vecmath.Vector3d;


/**
 *
 * represents parametric surface in (u,v) -> (x,y,z)
 * @author Vladimir Bulatov
 */
public interface ParametricSurface { 

    /**
       @returns bounds of the domain rectangle in UV space
       return bounds[4] = {umin, umax, vmin, vmax};
     */
    public double[] getDomainBounds();

    /**
       @return count of grid cells in each diraction in uv space
       the domain is divided into nu*nv equal cells 
       surface point is calculated in each vertex of the grid        
       count of the grid points is (nu + 1)*(nv+1)
     */
    public int[] getGridSize();

    /**
       return point with given coordinate 

       @param puv - point in uv space. z-coordinates is ignored 
       @param pxy - point on surface in xyz space. 
       
     */
    public Vector3d getPoint(Vector3d puv, Vector3d pxyz);

}

