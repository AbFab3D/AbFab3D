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

public interface GridIntervals {

    /**
       set one x,y column of this grid to given intervals and values 

       intervals[] - ordered array of starts of intervals of identical pixels 
       values[] - values of pixels in each interval 
       
     */
    public void setIntervals(int x, int y, int intervals[], int values[], int count);
    
}
