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
 * Warps a voxel space around a cylinder
 *
 * You have to map these somehow to a particular grid, which is not centered at origin.
 * SO, it should be 3 transform: [grid Trans1]->[warp]->[grid trans2]
 * inverse should be [gird trans2]^-1 -> [warp]^-1 -> [grid trans1]^-1
 * please note the order
 *
 * @author Alan Hudson
 */
public class RingSpaceWarp implements Operation, AttributeOperation {
    private static final boolean DEBUG = true;

    private Grid src;
    private double radius;
    private boolean forward;

    public RingSpaceWarp(Grid src, double r) {
        this(src,r,false);
    }

    public RingSpaceWarp(Grid src, double r, boolean forward) {
        this.src = src;
        radius = r;
        this.forward = forward;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid src
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid dest) {
        if (forward) {
            return executeForward(dest);
        } else {
            return executeReverse(dest);
        }
    }

        /**
        * Execute an operation on a grid.  If the operation changes the grid
        * dimensions then a new one will be returned from the call.
        *
        * @param dest The grid to use for grid src
        * @return The new grid
        */
    public AttributeGrid executeReverse(AttributeGrid dest) {

        int width = dest.getWidth();
        int height = dest.getHeight();
        int depth = dest.getDepth();

        double cx = width * dest.getVoxelSize() * 0.5;
        double cy = height * dest.getSliceHeight() * 0.5;
        double cz = depth * dest.getVoxelSize() * 0.5;

        double[] wc = new double[3];
        int[] pos = new int[3];
        VoxelData vd = dest.getVoxelData();

        long rejected = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
/*
                    if (x != 2 || y != 0 || z != 3) {
                        continue;
                    }
*/
                    dest.getWorldCoords(x, y, z, wc);
                    if (DEBUG) {
                        dest.getGridCoords(wc[0],wc[1],wc[2],pos);
                        System.out.println("Dest coord: " + java.util.Arrays.toString(pos));
                    }

                    // TODO: in theory this should be +cx and then -cx below.  But only -cx is vaguely working right.
                    // Center around origin
                    wc[0] -= cx;
                    //wc[1] += cy;
                    wc[2] -= cz;

                    if (DEBUG) {
                        dest.getGridCoords(wc[0],wc[1],wc[2],pos);
                        System.out.println("Centered dest coord: " + java.util.Arrays.toString(pos));
                        System.out.println("Centered dest wc: " + java.util.Arrays.toString(wc));

                    }
                    invert(wc, wc);
                    if (DEBUG) {
                        dest.getGridCoords(wc[0],wc[1],wc[2],pos);
                        System.out.println("Inverted coord: " + java.util.Arrays.toString(pos));
                        System.out.println("Inverted wc: " + java.util.Arrays.toString(wc));
                    }

/*
                    wc[0] += cx;
                    //wc[1] -= cy;
                    wc[2] += cz;
*/
                    if (DEBUG) {
                        dest.getGridCoords(wc[0],wc[1],wc[2],pos);
                        System.out.println("Uncentered coord: " + java.util.Arrays.toString(pos));
                    }

                    //System.out.println("radius: " + radius);
                    //wc[0] -= radius;
                    //wc[2] -= radius;

                    src.getGridCoords(wc[0],wc[1],wc[2],pos);
                    if (DEBUG) System.out.println("src coord: " + java.util.Arrays.toString(pos) + " inside: " + src.insideGrid(pos[0],pos[1],pos[2]));
/*
                    if (y == 2) {
                        System.out.println("Orig: " + java.util.Arrays.toString(wc) + " val: " + vd.getState());
                        System.out.println("New: " + wx + " " + wy + " " + wz);
                        System.out.println("Inside: " + src.insideGrid(wx, wy, wz));
                        int[] coords = new int[3];
                        src.getGridCoords(wx, wy, wz, coords);
                    }
*/
                    if (src.insideGrid(pos[0],pos[1],pos[2])) {
                        src.getData(pos[0], pos[1], pos[2], vd);

                        if (DEBUG) System.out.println("Src state: " + vd.getState());
                        if (vd.getState() != Grid.OUTSIDE) {
                            dest.setData(x, y, z, vd.getState(), vd.getMaterial());
                        }
                    } else {
                        rejected++;
                    }
                }
            }
        }

        System.out.println("Done with Warp.  Rejected: " + rejected);
        return dest;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid src
     * @return The new grid
     */
    public AttributeGrid executeForward(AttributeGrid dest) {
        int width = src.getWidth();
        int height = src.getHeight();
        int depth = src.getDepth();
        double cx = width * src.getVoxelSize() * 0.5;
        double cy = height * src.getSliceHeight() * 0.5;
        double cz = depth * src.getVoxelSize() * 0.5;

        double[] wc = new double[3];
        int[] pos = new int[3];
        VoxelData vd = dest.getVoxelData();

        long rejected = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
/*
                    if (x != 6 || y != 0 || z != 0) {
                        continue;
                    }
 */
                    src.getData(x, y, z,vd);
                    if (vd.getState() != Grid.OUTSIDE) {
                        src.getWorldCoords(x, y, z, wc);

                        if (DEBUG) System.out.println("src coord: " + x + " y: " + y + " z: " + z);
                        // Center around origin
                        wc[0] -= cx;
                        //wc[1] -= cy;
                        wc[2] -= cz;
                        if (DEBUG) {
                            src.getGridCoords(wc[0],wc[1],wc[2],pos);
                            System.out.println("centered coord: " + java.util.Arrays.toString(pos));
                            System.out.println("centered wc: " + java.util.Arrays.toString(wc));

                        }

                        transform(wc,wc);
                        if (DEBUG) {
                            src.getGridCoords(wc[0],wc[1],wc[2],pos);
                            System.out.println("transformed: " + java.util.Arrays.toString(pos));
                            System.out.println("transformed wc: " + java.util.Arrays.toString(wc));
                        }

                        // Uncenter.  Should this be dest center not source?
                        //wc[0] += cx + radius;
                        //wc[1] += cy;
                        //wc[2] += cz + radius;
                        wc[0] += cx;
                        //wc[1] += cy;
                        wc[2] += cz;

                        // TODO: not needed
                        if (DEBUG) {
                            dest.getGridCoords(wc[0],wc[1],wc[2],pos);
                            System.out.println("final pos: " + java.util.Arrays.toString(pos));
                            System.out.println("final wc: " + java.util.Arrays.toString(wc));
                        }
                        if (dest.insideGridWorld(wc[0], wc[1], wc[2])) {
                            dest.setDataWorld(wc[0], wc[1], wc[2], vd.getState(), vd.getMaterial());
                        } else {
                            dest.getGridCoords(wc[0], wc[1], wc[2], pos);
                            System.out.println("Reject: " + java.util.Arrays.toString(pos));
                            rejected++;
                        }
                    }
                }
            }
        }

        System.out.println("Done with Warp.  Rejected: " + rejected);

        return dest;
    }

    /**
     * Calculate cartesian to polar coordinates
     *
     * @param in
     * @param out
     */
    protected void transform(double[] in, double[] out) {

        double angle = in[0] / radius;
        double r = radius + in[2];
        double sina = Math.sin(angle);
        double cosa = Math.cos(angle);
        out[0] = r * sina;
        out[1] = in[1];
        out[2] = r * cosa;
    }
/*
    protected void transformComplex(double[] in, double[] out) {
        Complex inc = new Complex(in[0]/radius, in[2]/radius);
        Complex outc = Complex.exp(inc.mulSet(Complex.I));

        out[0] = outc.im*radius;
        out[1] = in[1];
        out[2] = outc.re*radius;

    }
*/
    /**
     * Calculate polar to cartesian coordinates
     * @param in
     * @param out
     */
    protected void invert(double[] in, double[] out) {

        double wx = in[0] / radius;
        double wy = in[1];
        double wz = in[2] / radius;

        double dist = Math.sqrt(wx * wx + wz * wz);
        double angle = Math.atan2(wx, wz);

        wx = angle * radius;
        wz = (dist-1) * radius;

        out[0] = wx;
        out[1] = wy;
        out[2] = wz;
    }

/*
    protected void invertComplex(double[] in, double[] out) {
        Complex inc = new Complex(in[0]/radius, in[2]/radius);

        Complex outc = Complex.log(inc);
        outc.divSet(Complex.I);

        out[0] = outc.im*radius;
        out[1] = in[1];
        out[2] = outc.re*radius;

    }
 */
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

        System.out.println("Exec2");

        return src;
    }
}
