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

import static abfab3d.util.ImageUtil.us2i;

/**
 * Change a grid to a new size.
 *
 * @author Alan Hudson
 */
public class Resample implements Operation2D {
    private static final boolean DEBUG = true;
    public static final int WEIGHTING_MINIMUM = 0;  // take the minimum of all involved pixels
    public static final int WEIGHTING_AVERAGE = 1;  // take the average of all involved pixels
    public static final int WEIGHTING_MAXIMUM = 2;  // take the average of all involved pixels

    private int inwidth;
    private int inheight;
    private int width;
    private int height;

    private int minificationWeighting;

    public Resample(int width, int height) {
        this(width,height,WEIGHTING_MINIMUM);
    }

    public Resample(int width, int height,int minificationWeighting) {
        this.width = width;
        this.height = height;
        this.minificationWeighting = minificationWeighting;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param src The grid to use for grid src
     * @return The new grid
     */
    public Grid2D execute(Grid2D src) {

        if (!(src instanceof Grid2DShort)) {
            throw new IllegalArgumentException("Unsupported Grid2D Type: " + src.getClass().getName());
        }

        Grid2DShort ssrc = (Grid2DShort) src;

        AttributeChannel dataChannel = src.getAttributeDesc().getChannel(0);

        double wr = (double) width / src.getWidth();
        double hr = (double) height / src.getHeight();

        if (wr > 1 || hr > 1) {
            throw new IllegalArgumentException("Magnification not supported yet");
        }

        if (Math.abs(wr - hr) > 0.1) {
            throw new IllegalArgumentException("Non square pixels detected.  wratio: " + wr + " hratio: " + hr);
        }

        double sr = Math.max(wr,hr);   // TODO: what todo about non square pixels
        Grid2DShort dest = (Grid2DShort) ssrc.createEmpty(width, height, src.getVoxelSize() / sr);

        short[] val;

        switch(minificationWeighting) {
            case WEIGHTING_AVERAGE:
                val = getScaledDownData(ssrc, width, height);
                break;
            case WEIGHTING_MINIMUM:
                val = getScaledDownDataBlack(ssrc,width,height);
                break;
            case WEIGHTING_MAXIMUM:
                val = getScaledDownDataWhite(ssrc,width,height);
                break;
            default:
                throw new IllegalArgumentException("Unsupported minificationWeighting: " + minificationWeighting);
        }

        dest.copyData(val);

        return dest;
    }

    /**
     scaled down image from input image
     */
    public static short[] getScaledDownData(Grid2D src, int width, int height){


        int inwidth = src.getWidth();
        int inheight = src.getHeight();

        short outData[] = new short[width * height];

        for(int y = 0; y < height; y++){

            int y0 = (y * inheight)/height;
            int y1 = ((y + 1) * inheight)/height;

            for(int x = 0; x < width; x++){

                int x0 = (x * inwidth)/width;
                int x1 = ((x + 1) * inwidth)/width;

                int pv = 0; // pixel value
                int count = (y1 - y0) * (x1-x0);

                for(int yy = y0; yy < y1; yy++){
                    for(int xx = x0; xx < x1; xx++){
                        pv +=us2i((short)src.getAttribute(xx,yy));
                    }
                }

                outData[x + y*width] = (short)(pv/count);
            }
        }

        return outData;

    } //getScaledDownData

    /**
     scaled down image from input image
     each destination pixel is set to darkest pixel of the input image
     */
    public static short[] getScaledDownDataBlack(Grid2D src, int width, int height){

        int inwidth = src.getWidth();
        int inheight = src.getHeight();
        short outData[] = new short[width * height];

        for(int y = 0; y < height; y++){

            int y0 = (y * inheight)/height;
            int y1 = ((y + 1) * inheight)/height;

            for(int x = 0; x < width; x++){

                int x0 = (x * inwidth)/width;
                int x1 = ((x + 1) * inwidth)/width;

                int pv = 0xFFFF; // pixel value

                for(int yy = y0; yy < y1; yy++){
                    for(int xx = x0; xx < x1; xx++){
                        int v = us2i((short)src.getAttribute(xx,yy));
                        if(v < pv)
                            pv = v;
                    }
                }

                outData[x + y*width] = (short)(pv);
            }
        }

        return outData;

    } //getScaledDownDataBlack

    /**
     scaled down image from input image
     each destination pixel is set to whitest pixel of the input image
     */
    public static short[] getScaledDownDataWhite(Grid2D src, int width, int height){

        int inwidth = src.getWidth();
        int inheight = src.getHeight();
        short outData[] = new short[width * height];

        for(int y = 0; y < height; y++){

            int y0 = (y * inheight)/height;
            int y1 = ((y + 1) * inheight)/height;

            for(int x = 0; x < width; x++){

                int x0 = (x * inwidth)/width;
                int x1 = ((x + 1) * inwidth)/width;

                int pv = 0x0000; // pixel value

                for(int yy = y0; yy < y1; yy++){
                    for(int xx = x0; xx < x1; xx++){
                        int v = us2i((short)src.getAttribute(xx,yy));
                        if(v > pv)
                            pv = v;
                    }
                }

                outData[x + y*width] = (short)(pv);
            }
        }

        return outData;

    } //getScaledDownDataWhite

}
