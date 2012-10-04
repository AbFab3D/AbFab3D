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
 * @author Alan Hudson
 */
public class RingSpaceWarp implements Operation, AttributeOperation {
    private static final boolean DEBUG = true;

    private Grid src;
    private double radius;

    public RingSpaceWarp(Grid src, double r) {
        this.src = src;
        radius = r;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid src
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid dest) {
        int width = src.getWidth();
        int height = src.getHeight();
        int depth = src.getDepth();

        double[] wc = new double[3];
        double wx, wy, wz;
        double angle;
        double r;
        double sina;
        double cosa;
        VoxelData vd;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    vd = src.getData(x, y, z);
                    if (vd.getState() != Grid.OUTSIDE) {
                        src.getWorldCoords(x, y, z, wc);

                        transform(wc,wc);

                        if (src.insideGrid(wc[0],wc[1],wc[2])) {
                            //System.out.println("Set data");
                            dest.setData(wc[0], wc[1], wc[2], vd.getState(), vd.getMaterial());
                        }
                    }
                }
            }
        }

        return dest;
    }

    /**
     * Calculate cartesian to polar coordinates
     *
     * @param in
     * @param out
     */
    protected void transformNew(double[] in, double[] out) {
        double angle = in[0] / radius;
        double r = radius + in[2];
        double sina = Math.sin(angle);
        double cosa = Math.cos(angle);
        out[0] = r * cosa;
        out[1] = in[1];
        out[2] = r * sina;
    }

    /**
     * Calculate polar to cartesian coordinates
     * @param in
     * @param out
     */
    protected void invertNew(double[] in, double[] out) {
        double wx = in[0] / radius;
        double wy = in[1];
        double wz = in[2] / radius;

        double dist = Math.sqrt(wx * wx + wz * wz);
        double phi = Math.atan2(wz, wx);
        wx = dist - 1;
        wz = phi + Math.PI;
        wx = wx * radius;
        wz = wz * radius;

        out[0] = wx;
        out[1] = wy;
        out[2] = wz;
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
        out[2] = r * cosa;      // TODO: most list these in opposite order  y = sin
    }

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
        double phi = Math.atan2(wz, wx);
        wx = dist - 1;
        wz = phi + Math.PI;    // says add only if negative? doesn't fix

        //if (phi < 0) phi += Math.PI;
        wx = wx * radius;
        wz = wz * radius;

        out[0] = wx;
        out[1] = wy;
        out[2] = wz;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid src
     * @return The new grid
     */
    public AttributeGrid executeReverse(AttributeGrid dest) {

        System.out.println("Exec1");
        int width = dest.getWidth();
        int height = dest.getHeight();
        int depth = dest.getDepth();

        double[] wc = new double[3];
        double wx, wy, wz;
        double dist;
        double phi;
        VoxelData vd = src.getData(0, 0, 0);

        double[] min = new double[3];
        double[] max = new double[3];
        src.getGridBounds(min, max);
        System.out.println("Min: " + java.util.Arrays.toString(min));
        System.out.println("Max: " + java.util.Arrays.toString(max));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    dest.getWorldCoords(x, y, z, wc);
                    wx = wc[0] / radius;
                    wy = wc[1];
                    wz = wc[2] / radius;

                    dist = Math.sqrt(wx * wx + wz * wz);
                    phi = Math.atan2(wz, wx);
                    wx = dist - 1;
                    wz = phi + Math.PI;
                    wx = wx * radius;
                    wz = wz * radius;

                    if (y == 2) {
                        System.out.println("Orig: " + java.util.Arrays.toString(wc) + " val: " + vd.getState());
                        System.out.println("New: " + wx + " " + wy + " " + wz);
                        System.out.println("Inside: " + src.insideGrid(wx, wy, wz));
                        int[] coords = new int[3];
                        src.getGridCoords(wx, wy, wz, coords);
                    }

                    if (src.insideGrid(wx, wy, wz)) {
                        src.getData(wx, wy, wz, vd);

                        if (vd.getState() != Grid.OUTSIDE) {
                            dest.setData(x, y, z, vd.getState(), vd.getMaterial());
                        }
                    }
                }
            }
        }

        System.out.println("Done with Warp");
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

        System.out.println("Exec2");

        return src;
    }
}
