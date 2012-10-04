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
   class repredsent queue of points 
 */
public class QueueInt {

    int start = 0;
    int end = 0;
    int count = 0;
    int maxSize;
    int maxCount = 0;

    int data[];
    
    public QueueInt(int initialSize){

        maxSize = initialSize;
        data = new int[maxSize];

    }

    public boolean offer(int x, int y, int z){
        
        if(count+3 > maxSize){
            printf("queue overflow\n");
            return false;
        }

        count += 3;
        data[end++] = x;if(end >= maxSize) end = 0;
        data[end++] = y;if(end >= maxSize) end = 0;
        data[end++] = z;if(end >= maxSize) end = 0;

        if(count > maxCount)
            maxCount = count;
        
        return true;

    }
    
    public int[] remove(int pnt[]){
        if(count < 3){
            printf("queue underflow\n");
            return pnt;
        }
        count -= 3;
        pnt[0] = data[start++]; if(start >= maxSize) start = 0;
        pnt[1] = data[start++]; if(start >= maxSize) start = 0;
        pnt[2] = data[start++]; if(start >= maxSize) start = 0;
        return pnt;
    }

    public boolean isEmpty(){

        return (count < 3);

    }

    public void printStat(){

        printf("QueueInt maxCount: %d\n", maxCount);

    }

}
