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

// External Imports

import abfab3d.util.Bounds;

import javax.vecmath.Tuple3d;
import java.io.Serializable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;

/**
 * Base class implementation of Grids.  Includes common code that
 * may get overwritten by faster implementations.
 * <p/>
 * Likely better performance for memory access that is not slice aligned.
 * <p/>
 * Uses the X3D coordinate system.  Y-up.  Grid is located
 * on positive right side octant.
 *
 * @author Alan Hudson
 */
public abstract class BaseGrid implements Grid, Cloneable, Serializable {

    static final boolean DEBUG = false;

    private static final boolean STATS = false;

    // Empty voxel data value
    protected static final VoxelData EMPTY_VOXEL;

    /**
     * The width of the grid
     */
    protected int width;

    /**
     * The height of the grid
     */
    protected int height;

    /**
     * The depth of the grid
     */
    protected int depth;

    /**
     * The horizontal voxel size
     */
    protected double pixelSize;

    /**
     * Half the horizontal size
     */
    protected double hpixelSize;

    /**
     * The slice height
     */
    protected double sheight;

    /**
     * Half the slice height
     */
    protected double hsheight;

    /**
     * The number of voxels in a slice
     */
    protected int sliceSize;

    /**
     * location of the grid corner
     */
    protected double xorig = 0.;
    protected double yorig = 0.;
    protected double zorig = 0.;

    static {
        EMPTY_VOXEL = new VoxelDataByte(Grid.OUTSIDE, NO_MATERIAL);
    }

    /**
     * Constructor.
     *
     * @param w       The number of voxels in width
     * @param h       The number of voxels in height
     * @param d       The number of voxels in depth
     * @param pixel   The size of the pixels
     * @param sheight The slice height in meters
     */
    public BaseGrid(int w, int h, int d, double pixel, double sheight) {
        width = w;
        height = h;
        depth = d;
        this.pixelSize = pixel;
        this.hpixelSize = pixelSize / 2.0;
        this.sheight = sheight;
        this.hsheight = sheight / 2.0;

        sliceSize = w * d;

        if (STATS) {
            System.out.println("WARNING:  BaseGrid started in STATS mode");
        }
    }

    /**
     * Constructor.
     *
     * @param bounds the bounds of the grid
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public BaseGrid(Bounds bounds, double pixel, double sheight) {
        
        width = bounds.getWidthVoxels(pixel);
        height = bounds.getHeightVoxels(sheight);
        depth = bounds.getDepthVoxels(pixel);
        this.pixelSize = pixel;
        this.hpixelSize = pixelSize / 2.0;
        this.sheight = sheight;
        this.hsheight = sheight / 2.0;
        this.xorig = bounds.xmin;
        this.yorig = bounds.ymin;
        this.zorig = bounds.zmin;

        sliceSize = width * depth;

        if (STATS) {
            System.out.println("WARNING:  BaseGrid started in STATS mode");
        }
    }

    /**
     * Get a new instance of voxel data.  Returns this grids specific sized voxel data.
     *
     * @return The voxel data
     */
    public VoxelData getVoxelData() {
        // This is a default impl.  For larger sizes the grid is expected to implement.
        return new VoxelDataByte();
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t  The traverer to call for each voxel
     */
    public void find(VoxelClasses vc, ClassTraverser t) {
        switch (vc) {
            case ALL:
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        for (int z = 0; z < depth; z++) {
                            t.found(x, y, z, getState(x, y, z));
                        }
                    }
                }
                break;
            case INSIDE:
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        for (int z = 0; z < depth; z++) {
                            byte state = getState(x, y, z);

                            if (state == Grid.INSIDE) {
                                t.found(x, y, z, state);
                            }
                        }
                    }
                }
                break;
            case OUTSIDE:
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        for (int z = 0; z < depth; z++) {
                            byte state = getState(x, y, z);

                            if (state == Grid.OUTSIDE) {
                                t.found(x, y, z, state);
                            }
                        }
                    }
                }
                break;
        }
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations. Uses multiple threads to partition the grid.
     *
     * @param vc The class of voxels to traverse
     * @param t  The traverer to call for each voxel
     */
    public void findMT(VoxelClasses vc, ClassTraverser t, int threadCount) {
        ConcurrentLinkedQueue<MTSlice> slices = new ConcurrentLinkedQueue<MTSlice>();

        int sliceHeight = 1;

        for (int y = 0; y < height; y += sliceHeight) {
            int ymax = y + sliceHeight;
            if (ymax > height)
                ymax = height;

            if (ymax > y) {
                // non zero slice
                slices.add(new MTSlice(y, ymax - 1));
            }
        }

        if (STATS) {
            printf("findMT.  STATS mode.  slices: %d\n",slices.size());
        }
        if (threadCount == 0) {
            threadCount = Runtime.getRuntime().availableProcessors();
        }
        if (threadCount < 1) {
            threadCount = 1;
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Runnable runner = new findMTVoxelClasses(vc,slices,this,t);

            executor.submit(runner);
        }
        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class findMTVoxelClasses implements Runnable {

        private ConcurrentLinkedQueue<MTSlice> slices;
        private VoxelClasses vc;
        private Grid grid;
        private ClassTraverser t;

        /** How many slices where processed.  Only collected with STATS true */
        private long slicesProcessed;

        public findMTVoxelClasses(VoxelClasses vc, ConcurrentLinkedQueue<MTSlice> slices, Grid grid, ClassTraverser t) {
            this.vc = vc;
            this.slices = slices;
            this.grid = grid;
            this.t = t;
        }

        public void run() {
            int width = grid.getWidth();
            int depth = grid.getDepth();

            while (true) {
                MTSlice slice = slices.poll();

                if (slice == null) {
                    // end of processing
                    break;
                }

                if (STATS) {
                    slicesProcessed++;
                }

                int len_x = width;
                int len_y = slice.ymax;
                int len_z = depth;

                switch (vc) {
                    case ALL:
                        for (int y = slice.ymin; y <= len_y; y++) {
                            for (int x = 0; x < len_x; x++) {
                                for (int z = 0; z < len_z; z++) {
                                    t.found(x, y, z, grid.getState(x, y, z));
                                }
                            }
                        }
                        break;
                    case INSIDE:
                        for (int y = slice.ymin; y <= len_y; y++) {
                            for (int x = 0; x < len_x; x++) {
                                for (int z = 0; z < len_z; z++) {
                                    byte state = grid.getState(x, y, z);

                                    if (state == Grid.INSIDE) {
                                        t.found(x, y, z, state);
                                    }
                                }
                            }
                        }
                        break;
                    case OUTSIDE:
                        for (int y = slice.ymin; y <= len_y; y++) {
                            for (int x = 0; x < len_x; x++) {
                                for (int z = 0; z < len_z; z++) {
                                    byte state = grid.getState(x, y, z);

                                    if (state == Grid.OUTSIDE) {
                                        t.found(x, y, z, state);
                                    }
                                }
                            }
                        }
                        break;
                }
            }

            if (STATS) {
                printf("findMT stats.  Thread: %s  SlicesProcessed: %6d\n",Thread.currentThread().getName(),slicesProcessed);
            }

        }
    }

    /**
     * Traverse a class of voxels types over given rectangle in xy plane.
     * May be much faster then full grid traversal for some implementations.
     *
     * @param vc   The class of voxels to traverse
     * @param t    The traverer to call for each voxel
     * @param xmin - minimal x - coordinate of voxels
     * @param xmax - maximal x - coordinate of voxels
     * @param ymin - minimal y - coordinate of voxels
     * @param ymax - maximal y - coordinate of voxels
     */
    public void find(VoxelClasses vc, ClassTraverser t, int xmin, int xmax, int ymin, int ymax) {
        switch (vc) {
            case ALL:
                for (int y = ymin; y <= ymax; y++) {
                    for (int x = xmin; x <= xmax; x++) {
                        for (int z = 0; z < depth; z++) {
                            t.found(x, y, z, getState(x, y, z));
                        }
                    }
                }
                break;
            case INSIDE:
                for (int y = ymin; y <= ymax; y++) {
                    for (int x = xmin; x <= xmax; x++) {
                        for (int z = 0; z < depth; z++) {
                            byte state = getState(x, y, z);

                            if (state == Grid.INSIDE) {
                                t.found(x, y, z, state);
                            }
                        }
                    }
                }
                break;
            case OUTSIDE:
                for (int y = ymin; y <= ymax; y++) {
                    for (int x = xmin; x <= xmax; x++) {
                        for (int z = 0; z < depth; z++) {
                            byte state = getState(x, y, z);

                            if (state == Grid.OUTSIDE) {
                                t.found(x, y, z, state);
                            }
                        }
                    }
                }
                break;
        }
    }


    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     */
    public int findCount(VoxelClasses vc) {
        int ret_val = 0;

        switch(vc) {
            case ALL:
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        for (int z = 0; z < depth; z++) {
                            ret_val++;
                        }
                    }
                }

                break;
            case INSIDE:
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        for (int z = 0; z < depth; z++) {
                            byte state = getState(x, y, z);
                            if (state == Grid.INSIDE) {
                                ret_val++;
                            }
                        }
                    }
                }
                break;
            case OUTSIDE:
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        for (int z = 0; z < depth; z++) {
                            byte state = getState(x, y, z);
                            if (state == Grid.OUTSIDE) {
                                ret_val++;
                            }
                        }
                    }
                }
                break;

        }

        return ret_val;
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t  The traverer to call for each voxel
     */
    public void findInterruptible(VoxelClasses vc, ClassTraverser t) {
        switch (vc) {
        case ALL:
        loop:
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        if (!t.foundInterruptible(x, y, z, getState(x, y, z)))
                            break loop;
                    }
                }
            }
            break;
        case INSIDE:
        loop:
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        byte state = getState(x, y, z);
                        
                        if (state == Grid.INSIDE) {
                            if (!t.foundInterruptible(x, y, z, state))
                                break loop;
                        }
                    }
                }
            }
            break;
        case OUTSIDE:
        loop:
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        byte state = getState(x, y, z);
                        
                        if (state == Grid.OUTSIDE) {
                            if (!t.foundInterruptible(x, y, z, state))
                                break loop;
                        }
                    }
                }
            }
            break;
        }
    }
    
    /**
     * Get the grid coordinates for a world coordinate.
     *
     * @param x      The x value in world coords
     * @param y      The y value in world coords
     * @param z      The z value in world coords
     * @param coords The ans is placed into this preallocated array(3).
     */
    public void getGridCoords(double x, double y, double z, int[] coords) {
        coords[0] = (int) ((x - xorig) / pixelSize);
        coords[1] = (int) ((y - yorig) / sheight);
        coords[2] = (int) ((z - zorig) / pixelSize);
    }

    /**
     * Get the grid coordinates for a world coordinate.
     *
     * @param x      The x value in world coords
     * @param y      The y value in world coords
     * @param z      The z value in world coords
     * @param coords The ans is placed into this preallocated array(3).
     */
    public void getGridCoords(double x, double y, double z, Tuple3d coords) {
        coords.x = ((x - xorig) / pixelSize) - 0.5;
        coords.y = ((y - yorig) / sheight)   - 0.5;
        coords.z = ((z - zorig) / pixelSize) - 0.5;
    }

    /**
     * Get the world coordinates of from the grid coordinates without half voxel shift 
     * 
     * @param x      The x value in grid coords
     * @param y      The y value in grid coords
     * @param z      The z value in grid coords
     * @param coords The ans is placed into this preallocated array(3).
     */
    public void getWorldCoords(int x, int y, int z, double[] coords) {

        coords[0] = x * pixelSize + hpixelSize + xorig;
        coords[1] = y * sheight + hsheight + yorig;
        coords[2] = z * pixelSize + hpixelSize + zorig;

        //coords[0] = x * pixelSize + xorig;
        //coords[1] = y * sheight   + yorig;
        //coords[2] = z * pixelSize + zorig;
    }

    /**
     * Get the world coordinates of from the grid coordinates without half voxel shift
     *
     * @param x      The x value in grid coords
     * @param y      The y value in grid coords
     * @param z      The z value in grid coords
     * @param coords The ans is placed into this preallocated array(3).
     */
    public void getWorldCoords(int x, int y, int z, Tuple3d coords) {

        coords.x = x * pixelSize + hpixelSize + xorig;
        coords.y = y * sheight + hsheight + yorig;
        coords.z = z * pixelSize + hpixelSize + zorig;

        //coords[0] = x * pixelSize + xorig;
        //coords[1] = y * sheight   + yorig;
        //coords[2] = z * pixelSize + zorig;
    }

    /**
     * Get the grid bounds in world coordinates.
     *
     * @param min The min coordinate
     * @param max The max coordinate
     */
    public void getGridBounds(double[] min, double[] max) {
        min[0] = xorig;
        min[1] = yorig;
        min[2] = zorig;

        max[0] = width * pixelSize + xorig;
        max[1] = height * sheight + yorig;
        max[2] = depth * pixelSize + zorig;
    }


    /**
     * Get the grid bounds in world coordinates.
     *
     * @param bounds array {xmin, xmax, ymin, ymax, zmin, zmax}
     */
    public void getGridBounds(double[] bounds) {
        Bounds b = getGridBounds();
        bounds[0] = b.xmin;
        bounds[1] = b.xmax;
        bounds[2] = b.ymin;
        bounds[3] = b.ymax;
        bounds[4] = b.zmin;
        bounds[5] = b.zmax;
    }

    /**
     * Get the grid bounds in world coordinates.
     */
    public Bounds getGridBounds() {

        Bounds bounds = new Bounds(xorig, xorig + width * pixelSize,
                                   yorig, yorig + height * sheight,
                                   zorig, zorig + depth * pixelSize, 
                                   pixelSize);
        return bounds;
    }

    /**
     * Set the grid bounds in world coordinates.
     *
     * @param bounds grid bounds 
     */
    public void setGridBounds(Bounds bounds) {

        if(DEBUG)printf("setGridBounds(%s)\n", bounds);

        xorig = bounds.xmin;
        yorig = bounds.ymin;
        zorig = bounds.zmin;

        pixelSize = (bounds.xmax - bounds.xmin) / width;

        sheight = (bounds.ymax - bounds.ymin) / height;

        double zpixelSize = (bounds.zmax - bounds.zmin) / depth;

        if (Math.abs((pixelSize - zpixelSize) / pixelSize) > 0.01) {
            throw new IllegalArgumentException(fmt("attempt to set non square pixel in Grid.setBounds(%s): [%12.5g x %12.5g] ", bounds, pixelSize, zpixelSize));
        }        
        if(DEBUG)printf("getGridBounds() returns: %s\n", getGridBounds());
    }

    /**
     * Set the grid bounds in world coordinates.
     *
     * @param bounds array {xmin, xmax, ymin, ymax, zmin, zmax}
     */
    public void setGridBounds(double[] bounds) {
        setGridBounds(new Bounds(bounds));
    }


    /**
     * Determine if a voxel coordinate is inside the grid space.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return True if the coordinate is inside the grid space
     */
    public boolean insideGrid(int x, int y, int z) {
        if (x >= 0 && x < width &&
            y >= 0 && y < height &&
            z >= 0 && z < depth) {

            return true;
        }

        return false;
    }

    /**
     * Determine if a voxel coordinate is inside the grid space.
     *
     * @param wx The x world coordinate
     * @param wy The y world coordinate
     * @param wz The z world coordinate
     * @return True if the coordinate is inside the grid space
     */
    public boolean insideGridWorld(double wx, double wy, double wz) {

        int x = (int) ((wx - xorig) / pixelSize);
        int y = (int) ((wy - yorig) / sheight);
        int z = (int) ((wz - zorig) / pixelSize);

        if (x >= 0 && x < width &&
            y >= 0 && y < height &&
            z >= 0 && z < depth) {

            return true;
        }

        return false;
    }

    /**
     * Get the number of height cells.
     *
     * @return the val
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the number of width cells.
     *
     * @return the val
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the number of depth cells.
     *
     * @return the val
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Get the slice height.
     *
     * @return The value
     */
    public double getSliceHeight() {
        return sheight;
    }

    /**
     * Get the number of dots per meter.
     *
     * @return The value
     */
    public double getVoxelSize() {
        return pixelSize;
    }

    /**
     * Print out a slice of data.
     */
    public String toStringSlice(int y) {
        StringBuilder sb = new StringBuilder();

        for (int z = depth - 1; z >= 0; z--) {
            for (int x = 0; x < width; x++) {
                sb.append(getState(x, y, z));
                sb.append(" ");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public String toStringAll() {
        StringBuilder sb = new StringBuilder();

        sb.append("Grid:  height: ");
        sb.append(height);
        sb.append("\n");

        for (int i = 0; i < height; i++) {
            sb.append(i);
            sb.append(":\n");
            sb.append(toStringSlice(i));
        }

        return sb.toString();
    }

    public abstract Object clone();


    /**
       copy grid bounds from srcGrid to destGrid
     */
    public static void copyBounds(Grid srcGrid, Grid destGrid){

        destGrid.setGridBounds(srcGrid.getGridBounds());

    }
    
    /**
       copy grid bounds from srcGrid to this
     */
    public void copyBounds(Grid srcGrid){

        this.setGridBounds(srcGrid.getGridBounds());

    }
    
    /**
       round double to the closest integer
     */
    public static int roundSize(double s){
        return (int)(s+0.5);
    }

}

