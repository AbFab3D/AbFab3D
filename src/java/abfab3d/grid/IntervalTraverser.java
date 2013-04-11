
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

/**
   interface to be called for every data point at the interval 
 */
public interface IntervalTraverser {
    

    /**

       return true to continue 
       return false to stop 
       
     */
    public boolean foundInterruptible(int x, int data);
    public void found(int x, int data);

}
