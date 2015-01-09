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

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;



/**
 * grid array to represent int values
 * Implemented as 2D array of intervals of ints
 *  
 * @author Vladimir Bulatov
 */
public class GridIntIntervals extends GridBitIntervals {

    // bit mask for maximal data stored in the grid 
    static final long DATA_MASK = 0xFFFFFFFFL;
    static final boolean DEBUG = false;
    /**
     * copy constructor
     */
    
    public GridIntIntervals(GridIntIntervals grid) {

        super(grid.m_nx, grid.m_ny, grid.m_nz, grid.m_orientation, grid.pixelSize, grid.sheight);

        for (int i = 0; i < m_data.length; i++) {
            RowOfInt dataItem = grid.m_data[i];
            if (dataItem != null)
                m_data[i] = (RowOfInt) dataItem.clone();
        }

    }
    
    public GridIntIntervals(int nx, int ny, int nz, double pixelSize, double sliceHeight) {
        super(nx, ny, nz, ORIENTATION_Z, pixelSize, sliceHeight);
    }
    
    /**
     * Constructor.
     *
     * @param bounds  The boudds of grid in world coords
     * @param pixel   The size of the pixels
     * @param sheight The slice height in meters
     */
    
    public GridIntIntervals(Bounds bounds, double pixel, double sheight) {
        super(bounds,  pixel, sheight);
    }

    /**
     * Constructor.
     *
     * @param w       The width in grid coords
     * @param h       The height in grid coords
     * @param d       The depth in grid coords
     * @param pixel   The size of the pixels
     * @param sheight The slice height in meters
     */
    
    public GridIntIntervals(int nx, int ny, int nz, int orientation, double pixelSize, double sliceHeight) {
        super(nx, ny, nz, orientation, pixelSize, sliceHeight);
    }
    
    /**
      @return interval of tyoe specific for this subclass
      @override 
     */
    protected RowOfInt newInterval() {
        return new IntIntervals();
    }

    /**
      @override 
     */
    public Grid createEmpty(int w, int h, int d, double pixel, double sheight) {
        return new GridIntIntervals(w, h, d, m_orientation, pixel, sheight);
    }

    public void setState(int x, int y, int z, byte state) {

        long curCode = get(x,y,z);

        byte currState = ioFunc.getState(curCode);

        if (state == currState) {
            return;
        }

        long att = ioFunc.getAttribute(curCode);
        set(x,y,z,ioFunc.combineStateAndAttribute(state,att));
    }

    public byte getState(int x, int y, int z) {
        return ioFunc.getState(get(x,y,z));
    }

    public void setAttribute(int x, int y, int z, long attribute) {

        long curCode = get(x, y, z);
        
        long curAtt = ioFunc.getAttribute(curCode);
        if (curAtt == attribute)
            return;
        
        set(x, y, z, ioFunc.updateAttribute(curCode,attribute));
    }

    public long getAttribute(int x, int y, int z) {

        return ioFunc.getAttribute(get(x,y,z) & DATA_MASK);

    }


    public void setData(int x, int y, int z, byte state, long attribute) {

        // Optimization as search is expensive
        long curCode = get(x, y, z);
        long curAtt = ioFunc.getAttribute(curCode);
        byte curState = ioFunc.getState(curCode);

        if (curState == state && curAtt == attribute)
            return;

        set(x, y, z, DATA_MASK & ioFunc.combineStateAndAttribute(state,attribute));
    }

    /**
     * set raw data at given point
     */
    public void set(int x, int y, int z, long value) {
        if(DEBUG)printf("set(%d %d %d %d)\n", x,y,z,value);
        int ind = x + m_nx * y;
        RowOfInt interval = m_data[ind];
        //RowOfInt interval = null;//m_data[ind];
        if (interval == null) {
            m_data[ind] = interval = new IntIntervals();//newInterval();
        }
        interval.set(z, value);

    }
    
    public long get(int x, int y, int z) {

        int ind = x + m_nx * y;
        if (x < 0 || x >= m_nx || y < 0 || y >= m_ny) {//ind < 0){
            throw new IllegalArgumentException(fmt("x: %d, y: %d, ind: %d\n", x, y, ind));
        }
        RowOfInt interval = m_data[x + m_nx * y];
        if (interval == null)
            return 0;
        else
            return interval.get(z);

    }


    public void getData(int x, int y, int z, VoxelData data) {
        long encoded = get(x, y, z);

        long att = ioFunc.getAttribute(encoded);
        byte state = ioFunc.getState(encoded);

        data.setData(state,att);
    }


    /**
     * interface Grid
     */
    public Object clone() {
        
        GridIntIntervals ret_val = new GridIntIntervals(this);
        copyBounds(this, ret_val);
        return ret_val;

    }
}
