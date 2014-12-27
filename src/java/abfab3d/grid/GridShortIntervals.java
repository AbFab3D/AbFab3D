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

/**
 * grid array to represent short values
 * Implemented as 2D array of intervals of shorts
 *
 * @author Vladimir Bulatov
 */
public class GridShortIntervals extends GridBitIntervals {

    /**
     * copy constructor
     */
    public GridShortIntervals(GridShortIntervals grid) {

        this(grid.m_nx, grid.m_ny, grid.m_nz, grid.m_orientation, grid.pixelSize, grid.sheight);

        for (int i = 0; i < m_data.length; i++) {
            RowOfInt dataItem = grid.m_data[i];
            if (dataItem != null)
                m_data[i] = (RowOfInt) dataItem.clone();
        }

    }

    public GridShortIntervals(int nx, int ny, int nz, double pixelSize, double sliceHeight) {
        this(nx, ny, nz, pixelSize, sliceHeight, null);
    }

    public GridShortIntervals(int nx, int ny, int nz, int orientation, double pixelSize, double sliceHeight) {
        this(nx, ny, nz, orientation, pixelSize, sliceHeight,null);
    }

    public GridShortIntervals(int nx, int ny, int nz, double pixelSize, double sliceHeight, InsideOutsideFunc ioFunc) {
        this(nx, ny, nz, ORIENTATION_Z, pixelSize, sliceHeight, ioFunc);
    }

    /**
     * Constructor.
     *
     * @param w       The width in world coords
     * @param h       The height in world coords
     * @param d       The depth in world coords
     * @param pixel   The size of the pixels
     * @param sheight The slice height in meters
     */
    public GridShortIntervals(double w, double h, double d, double pixel, double sheight) {
        this(roundSize(w / pixel),roundSize(h / sheight),roundSize(d / pixel),  pixel, sheight);
    }

    /**
     * Constructor.
     *
     * @param w       The width in world coords
     * @param h       The height in world coords
     * @param d       The depth in world coords
     * @param pixel   The size of the pixels
     * @param sheight The slice height in meters
     */
    public GridShortIntervals(double w, double h, double d, double pixel, double sheight, InsideOutsideFunc ioFunc) {
        this(roundSize(w / pixel),roundSize(h / sheight),roundSize(d / pixel), pixel, sheight, ioFunc);
    }

    public GridShortIntervals(int nx, int ny, int nz, int orientation, double pixelSize, double sliceHeight, InsideOutsideFunc ioFunc) {
        super(nx, ny, nz, orientation, pixelSize, sliceHeight);
    }

    /**
     * method to return new interval (to be overridden by subclass)
     */
    protected RowOfInt newInterval() {
        return new ShortIntervals();
    }

    public Grid createEmpty(int w, int h, int d, double pixel, double sheight) {
        return new GridShortIntervals(w, h, d, m_orientation, pixel, sheight, ioFunc);
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
        return ioFunc.getAttribute(get(x,y,z));
    }

    public long getAttribute(double x, double y, double z) {
        int iy = (int) (y / sheight);
        int ix = (int) (x / pixelSize);
        int iz = (int) (z / pixelSize);

        return ioFunc.getAttribute(get(ix,iy,iz));
    }

    public void setData(int x, int y, int z, byte state, long attribute) {

        // Optimization as search is expensive
        long curCode = get(x, y, z);
        long curAtt = ioFunc.getAttribute(curCode);
        byte curState = ioFunc.getState(curCode);

        if (curState == state && curAtt == attribute)
            return;

        set(x, y, z, ioFunc.combineStateAndAttribute(state,attribute));
    }

    /**
     * set raw data at given point
     */
    public void set(int x, int y, int z, int value) {

        int ind = x + m_nx * y;
        RowOfInt interval = m_data[ind];
        //RowOfInt interval = null;//m_data[ind];
        if (interval == null) {
            m_data[ind] = interval = new ShortIntervals();//newInterval();
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
        
        GridShortIntervals ret_val = new GridShortIntervals(this);
        copyBounds(this, ret_val);
        return ret_val;

    }

}
