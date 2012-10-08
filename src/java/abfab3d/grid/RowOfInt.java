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

/**
   interface to describe the row of integer of variable length
 */
public interface RowOfInt {
    
    public int get(int x);
    public void set(int x, int value);
    public Object clone();
    public void clear();   // clear data, but keep the memory 
    public void release(); // release all the memory 

    public void setIntervals(int intervals[], int values[], int count);
    
    /**
       traverses all point with given data value 
       from start to (end-1) inclusive 
       and calling IntervalTraverser for each voxel 
    */
    public boolean findInterruptible(int data, IntervalTraverser t);
    public int getDataMemory();
       
}
