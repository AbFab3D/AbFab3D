/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
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


/**
   interface to perform dilation/erosion with custom shape of voxles 

   @author Vladimir Bulatov

 */
public interface VoxelShape {

    /**
       return x,y,z array of coordinates of shape's voxels
     */
    public int[] getCoords();

    /**
       return x,y,z array of coordinates of difference 
       of the shape centered at origin and the shape centered at (0,0,-1). 
       
       It is used to optimise operations on sequential voxels.
     */
    public int[] getCoordsIncremented();

    /**
       
       returs bonds of the shape as int array 
       int[] {xmin, xmax, ymin., ymax, zmin, zmax};
     */
    public int[] getBounds();
    
    /**
       returns 6-neighbours iteration count needed to dilate single pixel to cover the whole shape       
     */
    public int getIterationCount();
    

}
