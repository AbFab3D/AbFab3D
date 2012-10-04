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

package abfab3d.grid;

import static abfab3d.util.Output.printf;


/**
   variable size stack to store triples of ints (coordinates of voxels)

   @author Vladimir Bulatov
*/
public class StackInt3 {

    short pnt[];
    int count = 0;
    int maxSize;
    int maxCount = 0;

    public StackInt3(int maxsize){
        
        maxSize = maxsize;
        pnt = new short[maxsize];
    }
    
    public boolean push(int x, int y, int z){
        
        if(count + 3 >= maxSize){
            int newsize = maxSize*2;
            short p[] = new short[newsize];
            System.arraycopy(pnt, 0, p, 0, pnt.length);
            pnt = p;
            maxSize = newsize;                
        }
        //printf("push(%d,%d,%d), count: %d\n", x, y, z, count);
        pnt[count++] = (short)x;
        pnt[count++] = (short)y;
        pnt[count++] = (short)z;
        if(count > maxCount)
            maxCount = count;
        return true;
        
    }
    
    public boolean pop(int p[]){  
        if(count >= 3){
            p[2] = pnt[--count];
            p[1] = pnt[--count];
            p[0] = pnt[--count];
            //printf("pop(%d,%d,%d)\n", p[0],p[1],p[2]);
            return true;
        } else {
            return false;
        }
    }

    public void printStat(){

        printf("StackInt3 maxCount:%d maxSize: %d\n", maxCount, maxSize);

    }

    
} // class StackInt3
