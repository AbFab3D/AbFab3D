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
import abfab3d.core.AttributeGrid;
import abfab3d.core.Grid;
import abfab3d.core.Grid2D;
import abfab3d.grid.*;
import abfab3d.param.*;
import abfab3d.util.ImageGray16;

import static abfab3d.core.Output.printf;


// External Imports

// Internal Imports

/**
 * Expand a grid's dimensions along some directions.
 *
 * Order for distances is left,top,right,bottom,front,back
 *
 * @author Alan Hudson
 */
public class ExpandOp extends BaseParameterizable implements Operation, Operation2D, AttributeOperation {
    private static final boolean DEBUG = false;

    LongParameter mp_attribute = new LongParameter("threshold", "Threshold for inside when using attribute grids", 0);
    IntegerListParameter mp_distances = new IntegerListParameter("distances","How far to expand in each direction", null,0,Integer.MAX_VALUE);

    Parameter m_aparam[] = new Parameter[]{
            mp_attribute, mp_distances
    };

    public ExpandOp(int[] distances) {
        setDistances(distances);
        setAttribute(ImageGray16.MAX_USHORT_S);
    }

    public ExpandOp(int[] distances, long att) {
        setDistances(distances);
        setAttribute(att);
    }

    public ExpandOp(int l, int t, int r, int b) {
        setDistances(new int[] {l,t,r,b});
        setAttribute(ImageGray16.MAX_USHORT_S);
    }

    public ExpandOp(int l, int t, int r, int b, long att) {
        setDistances(new int[] {l,t,r,b});
        setAttribute(att);
    }

    public void setAttribute(long val) {
        mp_attribute.setValue(val);
    }

    public void setDistances(int[] val) {
        mp_distances.setValue(val);
    }

    /**
     * @noRefGuide
     */
    protected void initParams(){
        super.addParams(m_aparam);
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param src The grid to use for grid src
     * @return The new grid
     */
    public Grid2D execute(Grid2D src) {
        long attribute = mp_attribute.getValue();
        int[] distances = mp_distances.getValue((int[])null);

        String vhash = BaseParameterizable.getParamString(getClass().getSimpleName(), src, m_aparam);
        Object co = ParamCache.getInstance().get(vhash);
        if (co != null) return ((Grid2D)co);

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

        // TODO: Need to restore for caching
        //Grid2DSourceWrapper wrapper = new Grid2DSourceWrapper(vhash,dest);
        //ParamCache.getInstance().put(vhash,wrapper);
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
        long attribute = mp_attribute.getValue();
        int[] distances = mp_distances.getValue((int[])null);

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
        long attribute = mp_attribute.getValue();
        int[] distances = mp_distances.getValue((int[])null);

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
