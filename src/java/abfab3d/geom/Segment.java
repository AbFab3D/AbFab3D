/***************************************************************************
 * 
 *                        Shapeways, Inc Copyright (c) 2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/


package abfab3d.geom;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;

/**
   represent single segment of contour 
 */
public class Segment {

    int start, end;

    int index; // index of this segment in the array

    Segment(int start, int end, int index){
        this.start = start;
        this.end = end;
        this.index = index;
    }

    void toSB(StringBuffer sb){

        sb.append(this.toString());

    }


    public String toString(){

        return fmt("[%d,%d]",start, end);

    }
    
} // class Segment 
