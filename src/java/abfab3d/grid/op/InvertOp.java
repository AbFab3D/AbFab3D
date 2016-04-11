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

package abfab3d.grid.op;

// External Imports

// Internal Imports

import abfab3d.grid.GridDataChannel;
import abfab3d.grid.Grid2D;
import abfab3d.grid.Operation2D;

/**
 * Invert a grid's values
 *
 * @author Alan Hudson
 */
public class InvertOp implements Operation2D {
    private static final boolean DEBUG = false;

    public InvertOp() {

    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param src The grid to use for grid src
     * @return The new grid
     */
    public Grid2D execute(Grid2D src) {

        GridDataChannel channel = src.getAttributeDesc().getDefaultChannel();

        int w = src.getWidth();
        int h = src.getHeight();

        for(int x=0; x < w; x++) {
            for(int y=0; y < h; y++) {
                src.setAttribute(x,y,channel.makeAtt(1.0 - channel.getValue(src.getAttribute(x,y))));
            }
        }
        return src;
    }
}
