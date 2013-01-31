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

import abfab3d.grid.GridMask;

import static abfab3d.util.Output.printf;

public class VoxelShapeFactory {
    

    /**
       returns 6 neighbours shape
       this is mostly for tests
    */
    public static VoxelShape get6neighbors(){

        int shape[] = new int[]{1,0,0, 
                                    -1,0,0, 
                                    0,1,0,
                                    0,-1,0,
                                    0,0,1,
                                    0,0,-1
        };

        return new VoxelShapeBase(shape);

    }
    
    /**
       makes 3D cross shape 
     */
    public static VoxelShape getCross(int length){

        int count = length*6; // count of 
        int shape[] = new int[count*3];

        int offset = 0;
        for(int i = 1; i <= length; i++){
            
            shape[offset++] = i;
            shape[offset++] = 0;
            shape[offset++] = 0;

            shape[offset++] = -i;
            shape[offset++] = 0;
            shape[offset++] = 0;

            shape[offset++] = 0;
            shape[offset++] = i;
            shape[offset++] = 0;

            shape[offset++] = 0;
            shape[offset++] = -i;
            shape[offset++] = 0;

            shape[offset++] = 0;
            shape[offset++] = 0;
            shape[offset++] = i;

            shape[offset++] = 0;
            shape[offset++] = 0;
            shape[offset++] = -i;

        }
        return new VoxelShapeBase(shape);

    }

    public static VoxelShape getBall(int x, int y, int z){

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
        // make exact array of data 
        System.arraycopy(a, 0, newarray, 0, index);

        return new VoxelShapeBase(newarray);
                
    }

    
    /**
       returns xmin, xmax, ymin, ymax, zmin, zmax of coords array 
     */
    public static int[] findBounds(int coord[]){

        int xmin = coord[0];
        int ymin = coord[1];
        int zmin = coord[2];
        int xmax = xmin, ymax = ymin, zmax = zmin;
        
        for(int i = 3; i < coord.length; i+= 3){
            int x = coord[i];
            int y = coord[i+1];
            int z = coord[i+2];
            
            if(x < xmin)xmin = x;
            if(x > xmax)xmax = x;
            if(y < ymin)ymin = y;
            if(y > ymax)ymax = y;
            if(z < zmin)zmin = z;
            if(z > zmax)zmax = z;
            
        }
        
        return new int[]{xmin, xmax, ymin, ymax, zmin, zmax}; 
        
    }
    
    public static int [] findIncrementedCoords(int coords[], int bounds[]){

        printf("findIncrementedCoords()\n");

        int xmin = bounds[0];
        int ymin = bounds[2];
        int zmin = bounds[4];

        int nx = bounds[1] - bounds[0] + 1;
        int ny = bounds[3] - bounds[2] + 1;
        int nz = bounds[5] - bounds[4] + 1;
        
        printf("shape: size %d x %d x %d \n", nx, ny, nz);
        
        GridMask grid = new GridMask(nx, ny, nz);
        // fill lmask with shape 
        for(int i =0; i < coords.length; i += 3){

            int x = coords[i]   - xmin;
            int y = coords[i+1] - ymin;
            int z = coords[i+2] - zmin;
            grid.set(x,y,z,1);
            
        }
        
        // subtract shifted shape 
        for(int i = 0; i < coords.length; i += 3){
            
            int x = coords[i]  - xmin;
            int y = coords[i+1]- ymin;
            int z = coords[i+2]- zmin;
            if(z > 0)
                grid.set(x,y,z-1,0);            
        }

        int t[] = new int[coords.length];

        int count = 0;
        for(int y =0; y < ny; y++){
            for(int x =0; x < nx; x++){
                for(int z =0; z < nz; z++){
                    if(grid.get(x,y,z) != 0){
                        t[count++] = x + xmin;
                        t[count++] = y + ymin;
                        t[count++] = z + zmin;
                    }
                }
            }
        }

        int newarray[] = new int[count];
        System.arraycopy(t, 0, newarray, 0, count);
        
        return newarray;
        
    }

    static public class VoxelShapeBase implements VoxelShape {
        
        int coords[];
        int bounds[];
        int coordsIncremeted[];

        public VoxelShapeBase(int shape[]){
            coords = shape;
            bounds = findBounds(coords);
            // TODO - calculate incremented shape  
            coordsIncremeted = findIncrementedCoords(coords, bounds);
            printf("shape voxel count: %d\n",coords.length/3 );
            printf("incremented shape voxel count: %d\n",coordsIncremeted.length/3 );
        }
        
        public int[] getCoords(){
            return coords;
        }

        public int[] getCoordsIncremented(){
            return coordsIncremeted;
        }
        
        public int[] getBounds(){
            return bounds;
        }

        public int getIterationCount(){
            return 1;
        }        
    }
}
