package shapejs;

import abfab3d.grid.*;

/**
 * A fake grid that does no allocation.  It only keeps bounds information.
 *
 * @author Alan Hudson
 */
public class FakeGrid extends BaseAttributeGrid {
    /**
     * Constructor.
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public FakeGrid(int w, int h, int d, double pixel, double sheight) {
        this(w,h,d,pixel,sheight,null);
    }

    /**
     * Constructor.
     *
     * @param bounds The bounds of grid in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public FakeGrid(Bounds bounds, double pixel, double sheight) {
        super(bounds, pixel,sheight);
    }

    /**
     * Constructor.
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public FakeGrid(int w, int h, int d, double pixel, double sheight, InsideOutsideFunc ioFunc) {
        super(w,h,d,pixel,sheight,ioFunc);
    }

    @Override
    public Object clone() {
        return null;
    }

    @Override
    public void getDataWorld(double x, double y, double z, VoxelData data) {
    }

    @Override
    public void getData(int x, int y, int z, VoxelData data) {

    }

    @Override
    public byte getStateWorld(double x, double y, double z) {
        return 0;
    }

    @Override
    public byte getState(int x, int y, int z) {
        return 0;
    }

    @Override
    public void setState(int x, int y, int z, byte state) {

    }

    @Override
    public void setStateWorld(double x, double y, double z, byte state) {

    }

    @Override
    public long getAttribute(int x, int y, int z) {
        return 0;
    }

    @Override
    public void setAttribute(int x, int y, int z, long attribute) {

    }

    @Override
    public Grid createEmpty(int w, int h, int d, double pixel, double sheight) {
        return null;
    }
}
