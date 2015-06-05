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

package abfab3d.util;

/**
   3 dimensional insets
 */
public class Insets3 {

    public double left, top, right, bottom, front, back;

    public Insets3(double left, double top, double right, double bottom, double front, double back){

        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.front  = front;
        this.back  = back;        
    }
}