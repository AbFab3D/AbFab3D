/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.io.input;

import java.io.IOException;

import abfab3d.grid.AttributeGrid;

/**
 * Slces Reader.
 *
 * Reads the a bunch of slices images into a grid
 *
 * @author Vladimir Bulatov
 */
public class SlicesReader {

    static final int ORIENTATION_X = 1, ORIENTATION_Y = 2, ORIENTATION_Z= 3;

    int m_orientation = ORIENTATION_Y;
    String fileTemplate = "slice%04d.png";    
    
    public SlicesReader(){        

    }

    public void setOrientation(){
        
    }

    /**
       reads a set of PNG image files into a grid 
       start first 
     */
    public int readSlices(AttributeGrid grid, String fileTemplate, 
                          int fromIndex, int toIndex, 
                          int sliceCount) throws IOException {
        
        return 0;
        
    }
}
