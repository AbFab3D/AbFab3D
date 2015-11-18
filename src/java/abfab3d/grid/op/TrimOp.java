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
import abfab3d.grid.*;

/**
 * Trim a grid to its smallest size based on where INSIDE voxels are.
 *
 * @author Alan Hudson
 */
public class TrimOp implements Operation, AttributeOperation, Operation2D {
    private static final boolean DEBUG = true;

    private double threshold;  // Threshold for inside when using attribute grids

    public TrimOp() {
        this(240f/255);
    }

    public TrimOp(double threshold) {
        this.threshold = threshold;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param src The grid to use for grid src
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid src) {
        int width = src.getWidth();
        int height = src.getHeight();
        int depth = src.getDepth();

        int x0 = 0, xn = width;
        int y0 = 0, yn = height;
        int z0 = 0, zn = depth;

        VoxelData vd = src.getVoxelData();

        // Find x0 range
        loop: for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                for(int z=0; z < depth; z++) {
                    src.getData(x,y,z,vd);

                    if (vd.getState() != Grid.OUTSIDE) {
                        x0 = x;
                        break loop;
                    }
                }
            }
        }

        // Find xn range
        loop: for(int x=width - 1; x > x0; x--) {
            for(int y=0; y < height; y++) {
                for(int z=0; z < depth; z++) {
                    src.getData(x,y,z,vd);

                    if (vd.getState() != Grid.OUTSIDE) {
                        xn = x;
                        break loop;
                    }
                }
            }
        }

        // Find y0 range
        loop: for(int y=0; y < height; y++) {
            for(int x=x0; x < xn; x++) {
                for(int z=0; z < depth; z++) {
                    src.getData(x,y,z,vd);

                    if (vd.getState() != Grid.OUTSIDE) {
                        y0 = y;
                        break loop;
                    }
                }
            }
        }

        // Find yn range
        loop: for(int y=height - 1; y > y0; y--) {
            for(int x=x0; x < xn; x++) {
                for(int z=0; z < depth; z++) {
                    src.getData(x,y,z,vd);

                    if (vd.getState() != Grid.OUTSIDE) {
                        yn = y;
                        break loop;
                    }
                }
            }
        }

        // Find z0 range
        loop: for(int z=0; z < depth; z++) {
            for(int y=y0; y < yn; y++) {
                for(int x=x0; x < xn; x++) {
                    src.getData(x,y,z,vd);

                    if (vd.getState() != Grid.OUTSIDE) {
                        z0 = z;
                        break loop;
                    }
                }
            }
        }

        // Find zn range
        loop:for(int z=depth - 1; z > z0; z--) {
            for(int y=y0; y < yn; y++) {
                for(int x=x0; x > xn; x++) {
                    src.getData(x,y,z,vd);

                    if (vd.getState() != Grid.OUTSIDE) {
                        zn = z;
                        break loop;
                    }
                }
            }
        }

        if (DEBUG) {
            System.out.println("Trimming to: x: " + x0 + " " + xn + " y: " + y0 + " " + yn + " z: " + z0 + " " + zn);
        }

        if (x0 == 0 && xn == width &&
            y0 == 0 && yn == height &&
            z0 == 0 && zn == depth) {

            // no margin so return original
            return src;
        }

        AttributeGrid dest = (AttributeGrid) src.createEmpty(xn - x0 + 1, yn - y0 + 1, zn - z0 + 1, src.getVoxelSize(), src.getSliceHeight());

        if (DEBUG) {
            dest = new RangeCheckAttributeWrapper(dest);
        }

        for(int y=y0; y < yn; y++) {
            for(int x=x0; x < xn; x++) {
                for(int z=z0; z < zn; z++) {
                    src.getData(x, y, z,vd);

                    dest.setData(x - x0,y - y0,z - z0,vd.getState(), vd.getMaterial());
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
    public Grid execute(Grid src) {
        int width = src.getWidth();
        int height = src.getHeight();
        int depth = src.getDepth();

        int x0 = 0, xn = width;
        int y0 = 0, yn = height;
        int z0 = 0, zn = depth;

        byte state;

        // Find x0 range
        loop: for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                for(int z=0; z < depth; z++) {
                    state = src.getState(x, y, z);

                    if (state != Grid.OUTSIDE) {
                        x0 = x;
                        break loop;
                    }
                }
            }
        }

        // Find xn range
        loop: for(int x=width - 1; x > x0; x--) {
            for(int y=0; y < height; y++) {
                for(int z=0; z < depth; z++) {
                    state = src.getState(x, y, z);

                    if (state != Grid.OUTSIDE) {
                        xn = x;
                        break loop;
                    }
                }
            }
        }

        // Find y0 range
        loop: for(int y=0; y < height; y++) {
            for(int x=x0; x < xn; x++) {
                for(int z=0; z < depth; z++) {
                    state = src.getState(x, y, z);

                    if (state != Grid.OUTSIDE) {
                        y0 = y;
                        break loop;
                    }
                }
            }
        }

        // Find yn range
        loop: for(int y=height - 1; y > y0; y--) {
            for(int x=x0; x < xn; x++) {
                for(int z=0; z < depth; z++) {
                    state = src.getState(x, y, z);

                    if (state != Grid.OUTSIDE) {
                        yn = y;
                        break loop;
                    }
                }
            }
        }

        // Find z0 range
        loop: for(int z=0; z < depth; z++) {
            for(int y=y0; y < yn; y++) {
                for(int x=x0; x < xn; x++) {
                    state = src.getState(x, y, z);

                    if (state != Grid.OUTSIDE) {
                        z0 = z;
                        break loop;
                    }
                }
            }
        }

        // Find zn range
        loop:for(int z=depth - 1; z > z0; z--) {
            for(int y=y0; y < yn; y++) {
                for(int x=x0; x > xn; x++) {
                    state = src.getState(x, y, z);

                    if (state != Grid.OUTSIDE) {
                        zn = z;
                        break loop;
                    }
                }
            }
        }

        if (DEBUG) {
            System.out.println("Trimming to: x: " + x0 + " " + xn + " y: " + y0 + " " + yn + " z: " + z0 + " " + zn);
        }

        if (x0 == 0 && xn == width &&
                y0 == 0 && yn == height &&
                z0 == 0 && zn == depth) {

            // no margin so return original
            return src;
        }

        Grid dest = (Grid) src.createEmpty(xn - x0 + 1, yn - y0 + 1, zn - z0 + 1, src.getVoxelSize(), src.getSliceHeight());

        if (DEBUG) {
            dest = new RangeCheckWrapper(dest);
        }
        for(int y=y0; y < yn; y++) {
            for(int x=x0; x < xn; x++) {
                for(int z=z0; z < zn; z++) {
                    state = src.getState(x,y,z);

                    dest.setState(x - x0,y - y0,z - z0,state);
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
    public Grid2D execute(Grid2D src) {
        int width = src.getWidth();
        int height = src.getHeight();

        int x0 = 0, xn = width;
        int y0 = 0, yn = height;

        AttributeChannel dataChannel = src.getAttributeDesc().getChannel(0);

        // Find x0 range
        loop: for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                    double val = dataChannel.getValue(src.getAttribute(x,y));

                    if (val <= threshold) {
                        x0 = x;
                        break loop;
                    }
            }
        }

        // Find xn range
        loop: for(int x=width - 1; x > x0; x--) {
            for(int y=0; y < height; y++) {
                double val = dataChannel.getValue(src.getAttribute(x,y));

                if (val <= threshold) {
                    xn = x;
                    break loop;
                }
            }
        }

        // Find y0 range
        loop: for(int y=0; y < height; y++) {
            for(int x=x0; x < xn; x++) {
                double val = dataChannel.getValue(src.getAttribute(x,y));

                if (val <= threshold) {
                    y0 = y;
                    break loop;
                }
            }
        }

        // Find yn range
        loop: for(int y=height - 1; y > y0; y--) {
            for(int x=x0; x < xn; x++) {
                double val = dataChannel.getValue(src.getAttribute(x,y));

                if (val <= threshold) {
                    yn = y;
                    break loop;
                }
            }
        }

        if (DEBUG) {
            System.out.println("Trimming to: x: " + x0 + " " + xn + " y: " + y0 + " " + yn);
        }

        if (x0 == 0 && xn == width && y0 == 0 && yn == height) {
            // no margin so return original
            return src;
        }

        Grid2D dest = src.createEmpty(xn - x0 + 1, yn - y0 + 1, src.getVoxelSize());

        for(int y=y0; y < yn; y++) {
            for(int x=x0; x < xn; x++) {
                long att = src.getAttribute(x, y);

                dest.setAttribute(x - x0,y - y0,att);
            }
        }

        return dest;

    }
}
