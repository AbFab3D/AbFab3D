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

import abfab3d.grid.AttributeChannel;
import abfab3d.grid.Grid2D;
import abfab3d.grid.Grid2DShort;
import abfab3d.grid.Operation2D;
import abfab3d.util.MathUtil;

import java.util.Arrays;

import static abfab3d.util.ImageUtil.us2i;
import static abfab3d.util.MathUtil.clamp;

/**
 * Smooth a grid
 *
 * @author Alan Hudson
 */
public class SmoothOp implements Operation2D {
    private static final boolean DEBUG = false;

    private double distance;  // distance in meters

    public SmoothOp(double distance) {
        this.distance = distance;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param src The grid to use for grid src
     * @return The new grid
     */
    public Grid2D execute(Grid2D src) {

        double pdistance = distance / src.getVoxelSize();
        double[] kernel = MathUtil.getGaussianKernel(pdistance, 0.001);

        int s = Math.max(src.getWidth(), src.getHeight());

        convoluteX(src, kernel, s);
        convoluteY(src,kernel, s);

        return src;
    }


    private void convoluteX(Grid2D src, double kernel[],int rlen){
        AttributeChannel channel = src.getAttributeDesc().getDefaultChannel();
        int w = src.getWidth();
        int h = src.getHeight();

        int ksize = kernel.length/2;
        int w1 = w-1;
        int len = kernel.length;

        double[] crow = new double[rlen];
        double sum;

        for(int y = 0; y < h; y++){

            for(int x = 0; x < w; x++) {
                crow[x] = channel.getValue(src.getAttribute(x, y));
            }

            for(int x = 0; x < w; x++){
                sum = 0;
                for(int k = 0; k < len; k++){

                    int xx = x - (k-ksize); //offsety + x + k;

                    xx = clamp(xx, 0, w1); // boundary conditions
                    sum += (kernel[k] * crow[xx]);
                }
                src.setAttribute(x,y,channel.makeAtt(sum));
            }
        }
    }

    private void convoluteY(Grid2D src,double kernel[],int rlen){
        AttributeChannel channel = src.getAttributeDesc().getDefaultChannel();

        int w = src.getWidth();
        int h = src.getHeight();
        int ksize = kernel.length/2;
        int h1 = h-1;
        int len = kernel.length;

        double[] crow = new double[rlen];
        double sum;

        for(int x = 0; x < w; x++){
            for(int y = 0; y < h; y++) {
                crow[y] = channel.getValue(src.getAttribute(x, y));
            }

            for(int y = 0; y < h; y++){
                sum = 0;
                for(int k = 0; k < len; k++){
                    int yy = y - (k-ksize);
                    yy = clamp(yy, 0, h1);
                    sum += (kernel[k] * crow[yy]);
                }
                src.setAttribute(x,y,channel.makeAtt(sum));
            }
        }
    } // convolute y
}
