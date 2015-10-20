/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
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
import abfab3d.grid.*;
import abfab3d.util.ImageGray16;
import abfab3d.util.ImageUtil;

import java.util.EnumSet;


// External Imports

// Internal Imports

/**
 * Expand a grid's dimensions along some directions.
 *
 * Order for distances is left,top,right,bottom,front,back
 *
 * @author Alan Hudson
 */
public class Expand implements Operation, Operation2D, AttributeOperation {
    private static final boolean DEBUG = true;

    private int[] distances;
    private long attribute;  //  the attribute value for new cells, defaults to white

    public Expand(int[] distances) {
        this.distances = distances.clone();

        for(int i=0; i < distances.length; i++) {
            if (distances[i] < 0) throw new IllegalArgumentException("Negative directions not supported yet");
        }

        attribute = ImageGray16.MAX_USHORT_S;
    }

    public Expand(int[] distances, long att) {
        this.distances = distances.clone();

        for(int i=0; i < distances.length; i++) {
            if (distances[i] < 0) throw new IllegalArgumentException("Negative directions not supported yet");
        }

        this.attribute = att;
    }

    public Expand(int l, int t, int r, int b) {
        this.distances = new int[] {l,t,r,b};
        for(int i=0; i < distances.length; i++) {
            if (distances[i] < 0) throw new IllegalArgumentException("Negative directions not supported yet");
        }
        attribute = ImageGray16.MAX_USHORT_S;
    }

    public Expand(int l, int t, int r, int b, long att) {
        this.distances = new int[] {l,t,r,b};
        for(int i=0; i < distances.length; i++) {
            if (distances[i] < 0) throw new IllegalArgumentException("Negative directions not supported yet");
        }

        attribute = att;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param src The grid to use for grid src
     * @return The new grid
     */
    public Grid2D execute(Grid2D src) {
        int wdelta = distances[0] + distances[2];
        int hdelta = distances[1] + distances[3];
        int worigin = distances[0];
        int horigin = distances[1];

        Grid2D dest = (Grid2D) src.createEmpty(src.getWidth() + wdelta,src.getHeight() + hdelta, src.getVoxelSize());

        Copy cop = new Copy(src,worigin,horigin);
        dest = cop.execute(dest);

        if (attribute != 0) {
            //fill in new values for left
            for(int i=0; i < distances[0]; i++) {
                for (int y = 0; y < dest.getHeight(); y++) {
                    dest.setAttribute(i, y, attribute);
                }
            }

            //fill in new values for right
            for(int i=0; i < distances[2]; i++) {
                for (int y = 0; y < dest.getHeight(); y++) {
                    dest.setAttribute(dest.getWidth() - i - 1, y, attribute);
                }
            }

            //fill in new values for top
            for(int i=0; i < distances[1]; i++) {
                for (int x = 0; x < dest.getWidth(); x++) {
                    dest.setAttribute(x, i, attribute);
                }
            }

            //fill in new values for bottom
            for(int i=0; i < distances[3]; i++) {
                for (int x = 0; x < dest.getWidth(); x++) {
                    dest.setAttribute(x,dest.getHeight() - i - 1, attribute);
                }
            }
        }
        return dest;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param src The grid to use for grid src
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid src) {
        int wdelta = distances[0] + distances[2];
        int hdelta = distances[1] + distances[3];
        int ddelta = distances[4] + distances[5];

        int worigin = distances[0];
        int horigin = distances[1];
        int dorigin = distances[2];

        AttributeGrid dest = (AttributeGrid) src.createEmpty(src.getWidth() + wdelta,src.getHeight() + hdelta, src.getDepth() + ddelta,src.getVoxelSize(), src.getSliceHeight());

        Copy cop = new Copy(src,worigin,horigin,dorigin);
        dest = cop.execute(dest);

        if (attribute != 0) {
            throw new IllegalArgumentException("Attribute filling not supported");
        }

        return dest;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param src The grid to use for grid src
     * @return The new grid
     */
    public Grid execute(Grid src) {
        int wdelta = distances[0] + distances[2];
        int hdelta = distances[1] + distances[3];
        int ddelta = distances[4] + distances[5];

        int worigin = distances[0];
        int horigin = distances[1];
        int dorigin = distances[2];

        Grid dest = (Grid) src.createEmpty(src.getWidth() + wdelta,src.getHeight() + hdelta, src.getDepth() + ddelta, src.getVoxelSize(),src.getSliceHeight());

        Copy cop = new Copy(src,worigin,horigin,dorigin);
        dest = cop.execute(dest);

        if (attribute != 0) {
            throw new IllegalArgumentException("Attribute filling not supported");
        }

        return dest;
    }
}
