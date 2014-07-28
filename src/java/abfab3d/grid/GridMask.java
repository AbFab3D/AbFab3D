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
package abfab3d.grid;


import static abfab3d.util.Output.printf;

/**
   grid array to represent on/off mask. Implemented as array of int 
   groups of 32 points in x-direction are stored as bits in one int 

   @author Vladimir Bulatov
*/
public class GridMask implements GridBit {

    int data[];
    int nx, ny, nz;
    int lenz, lenxz;
    static final int INTLEN = 32;
    
    public GridMask(int nx, int ny, int nz){
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
        this.lenz = ((nz+INTLEN-1)/INTLEN);
        this.lenxz = lenz * nx;
        
        data = new int[nx*ny*lenz];
    }
    
    public long get(int x, int y, int z){

        int zint = z/INTLEN;
        int bit = z % INTLEN;           
        int offset = zint + x*lenz + y * lenxz;
        
        int w = data[offset];
        return ((w >> bit) & 1);

    }
    
    public void set(int x, int y, int z, long value){
        
        //printf("mask.set(%d,%d,%d)\n", x,y,z);
        
        int zint = z/INTLEN;
        int bit = z % INTLEN;           
        int offset = zint + x*lenz + y * lenxz;
        if(value != 0){
            // set bit 
            data[offset] |= (1 << bit);
        } else {
            // clear bit 
            data[offset] &= (0xFFFFFFFF ^ (1 << bit));
        }
    }
    
    public void dump(){
        for(int i = 0; i < data.length; i++){
            printf("%d: %X\n", i, data[i]);
        }
    }

    public void clear(){       
        // clear data, keep the memory 
        for(int i =0; i < data.length; i++)
            data[i] = 0;

        
    }
    public void release(){
        data = null;
    }
    
} // class GridMask
