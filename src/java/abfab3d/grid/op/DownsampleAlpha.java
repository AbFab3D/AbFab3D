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
import abfab3d.util.DefaultLongConverter;
import abfab3d.util.LongConverter;

/**
 * Downsample a grid.  Uses attribute data as an alpha channel.
 *
 * Currently uses 2x2x2 box filtering.  Lots of room for better methods.
 *
 * weight voxelWeight = 1 + coeff * voxelFill (or alpha)
 * coeff - arbitrary number.
 * coeff = 0 - simple average
 * coeff = 100 - darkest voxel gives fil to the result
 * voxelFill is assumed to be float between (0., 1.)
 * So, it should be calculated attribute/maxAttribute
 * (double)attribute/maxAttribute
 *
 *
 * @author Alan Hudson
 */
public class DownsampleAlpha implements Operation, AttributeOperation {
    /** The weight of voxel when averaging */
    private double coeff;

    /** How much to downsample.  */
    private int factor;

    /** The maximum alpha value */
    private long maxAttributeValue;

    /** Convert attribute to alpha */
    private LongConverter dataConverter;

    /** Number of voxels in a kernel */
    private int kernelSize;

    /** Is the input a binary grid */
    private boolean binaryInput;

    public DownsampleAlpha(double coeff, int factor, long maxAttributeValue) {
        this(false,coeff,factor,maxAttributeValue);
    }

    public DownsampleAlpha(boolean binary, double coeff, int factor, long maxAttributeValue) {
        this.binaryInput = binary;
        this.coeff = coeff;
        this.factor = factor;
        this.maxAttributeValue = maxAttributeValue;

        kernelSize = factor * factor * factor;
        dataConverter = new DefaultLongConverter();
    }

    /**
     * Set the data converter to use for turning packaged attribute to alpha value.
     *
     * @param dataConverter
     */
    public void setDataConverter(LongConverter dataConverter) {
        this.dataConverter = dataConverter;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid A.
     * @return The new grid
     */
    public Grid execute(Grid dest) {
        throw new IllegalArgumentException("Not implemented");
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid A.
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid dest) {
        if (binaryInput) {
            return executeBinaryCoeffBoxAverage(dest);
        } else if (coeff != 0.0) {
            return executeCoeffBoxAverage(dest);
        } else {
            return executeBoxAverage(dest,factor);
        }
    }

    /**
     * Execute using a coeff which is slower then doing a simple avg.
     * @param dest
     * @return
     */
    public AttributeGrid executeCoeffBoxAverage(AttributeGrid dest) {
        int
            nx = dest.getWidth(),
            ny = dest.getHeight(),
            nz = dest.getDepth(),
            nx1 = nx/factor,
            ny1 = ny/factor,
            nz1 = nz/factor;

        double bounds[] = new double[6];
        dest.getGridBounds(bounds);

        double dx = (bounds[1] - bounds[0] )/nx;
        double dy = (bounds[3] - bounds[2] )/ny;
        double dz = (bounds[5] - bounds[4] )/nz;
        double
                dx1 = dx * factor,
                dy1 = dy * factor,
                dz1 = dz * factor;

        AttributeGrid g1 = (AttributeGrid) dest.createEmpty(nx1, ny1, nz1, dx1, dz1);

        bounds[1] = bounds[0] + dx1 * nx1;
        bounds[3] = bounds[2] + dy1 * ny1;
        bounds[5] = bounds[4] + dy1 * nz1;

        g1.setGridBounds(bounds);

        // TODO: for some grids this traversal will be very slow.  Should this type
        // of skip traversal be part of the grid interface?

        for(int y = 0, y1 = 0; y < ny1; y++, y1 += factor) {
            for(int x = 0, x1 = 0; x <  nx1; x++, x1 += factor) {

                for(int z = 0, z1 = 0; z <  nz1; z++, z1 += factor) {
                    double sum = 0, total = 0;

                    for(int yy = 0; yy < factor; yy++) {
                        for(int xx = 0; xx < factor; xx++) {
                            for(int zz = 0; zz < factor; zz++) {
                                long mat = dataConverter.get(dest.getAttribute(x1 + xx,y1 + yy,z1 + zz));

                                if (mat == 0) {
                                    total += 1.0 - coeff;
                                } else {
                                    double fill = (double) mat / maxAttributeValue;
                                    double vw = 1.0 + coeff * fill;
                                    sum += vw * fill;
                                    total += vw;
                                }
                            }
                        }
                    }

                    // rounding is more accurate but expensive, favor speed.
//                    long avg = Math.round((float)sum / total * maxAttributeValue);
                    long avg = (long) (sum / total * maxAttributeValue);
                    g1.setAttribute(x,y,z,avg);
                }
            }
        }

        return g1;

    }

    /**
     * Execute using a coeff which is slower then doing a simple avg.
     * @param dest
     * @return
     */
    public AttributeGrid executeBinaryCoeffBoxAverage(AttributeGrid dest) {
        int
                nx = dest.getWidth(),
                ny = dest.getHeight(),
                nz = dest.getDepth(),
                nx1 = nx/factor,
                ny1 = ny/factor,
                nz1 = nz/factor;

        double bounds[] = new double[6];
        dest.getGridBounds(bounds);

        double dx = (bounds[1] - bounds[0] )/nx;
        double dy = (bounds[3] - bounds[2] )/ny;
        double dz = (bounds[5] - bounds[4] )/nz;
        double
                dx1 = dx * factor,
                dy1 = dy * factor,
                dz1 = dz * factor;

        AttributeGrid g1 = (AttributeGrid) dest.createEmpty(nx1, ny1, nz1, dx1, dz1);

        bounds[1] = bounds[0] + dx1 * nx1;
        bounds[3] = bounds[2] + dy1 * ny1;
        bounds[5] = bounds[4] + dy1 * nz1;

        g1.setGridBounds(bounds);

        // TODO: for some grids this traversal will be very slow.  Should this type
        // of skip traversal be part of the grid interface?

        for(int y = 0, y1 = 0; y < ny1; y++, y1 += factor) {
            for(int x = 0, x1 = 0; x <  nx1; x++, x1 += factor) {

                for(int z = 0, z1 = 0; z <  nz1; z++, z1 += factor) {
                    double sum = 0, total = 0;

                    for(int yy = 0; yy < factor; yy++) {
                        for(int xx = 0; xx < factor; xx++) {
                            for(int zz = 0; zz < factor; zz++) {
                                if(dest.getState(x1 + xx,y1 + yy,z1 + zz) != Grid.OUTSIDE){
                                    sum += coeff;
                                    total += coeff;
                                } else {
                                    total += 1.;
                                }

                            }
                        }
                    }

                    // rounding is more accurate but expensive, favor speed.
//                    long avg = Math.round((float)sum / total * maxAttributeValue);
                    long avg = (long) (sum / total * maxAttributeValue);
                    g1.setAttribute(x,y,z,avg);
                }
            }
        }

        return g1;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use for grid A.
     * @return The new grid
     */
    public AttributeGrid executeBoxAverage(AttributeGrid dest, int factor) {
        int width = dest.getWidth();
        int depth = dest.getDepth();
        int height = dest.getHeight();

        int len_x = width / factor;
        int len_y = height / factor;
        int len_z = depth / factor;

        AttributeGrid ret_val = (AttributeGrid) dest.createEmpty(len_x,len_y,len_z,
                dest.getVoxelSize() * factor, dest.getSliceHeight() * factor);


        for(int y=0; y < len_y; y++) {
            for(int x=0; x < len_x; x++) {
                for(int z=0; z < len_z; z++) {
                    long att_avg = avgAttribute(dest, x*2, y*2, z*2);
                    ret_val.setAttribute(x,y,z, att_avg);
                }
            }
        }

        return ret_val;
    }

    /**
     * Average attribute values.
     *
     * @param grid
     * @param x
     * @param y
     * @param z
     * @return
     */
    private long avgAttribute(AttributeGrid grid, int x, int y, int z) {
        long sum = 0;

        for(int yy = 0; yy < factor; yy++) {
            for(int xx = 0; xx < factor; xx++) {
                for(int zz = 0; zz < factor; zz++) {
                    long mat = dataConverter.get(grid.getAttribute(x + xx,y + yy,z + zz));

                    sum += mat;
                }
            }
        }

        return sum / kernelSize;
    }

}
