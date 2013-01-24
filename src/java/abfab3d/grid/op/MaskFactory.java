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
   makes different cayustom masks for erosion/dilation operations 

*/
public class MaskFactory {
        
    /**
       makes ball of voxel coordinates of given radius 
     */
    public static final int[] makeBall(int radius){
        
        return makeBall(radius, 0,0);

    }        
    
 
    /**
       makes ball centered at origin, which passes via given point
     */
    public static final int[] makeBall(int x, int y, int z){

        int radius2 = x*x + y*y + z*z;
        int radius = (int)Math.ceil(Math.sqrt(radius2));

        int w = (int)(2*radius + 1);
        int a[] = new int[w*w*w*3];
        int index = 0;
        for(int iy = -radius; iy <= radius; iy++){
            for(int ix = -radius; ix <= radius; ix++){
                for(int iz = -radius; iz <= radius; iz++){
                    int r2 = (ix*ix + iy*iy + iz*iz);
                    if(r2 <= radius2){
                        a[index++] = ix;
                        a[index++] = iy;
                        a[index++] = iz;
                    }
                }
            }
        }
        
        int newarray[] = new int[index];
        // return exact array of data 
        System.arraycopy(a, 0, newarray, 0, index);
        return newarray;
        
    }

}
