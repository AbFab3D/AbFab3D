/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package render;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import java.io.IOException;

import javax.imageio.ImageIO;



public class ResultsListener implements TransferResultsListener {
    private int[] pixels;
    private int[] pixel = new int[4];
    private int width;
    private int height;
    private BufferedImage image;
    
    public ResultsListener(int width, int height, int[] pixels, BufferedImage image) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.image = image;
    }
    
    @Override
        public void tileArrived(RenderTile tile) {
        //writeImage(tile);
        
        synchronized (image) {
            long t0 = System.nanoTime();
            //int[] pixels = new int[buffer.getBuffer().capacity()];
            WritableRaster raster = image.getRaster();


            int x;
            int y;

            tile.getDest().getBuffer().get(pixels).rewind();
            int r, g, b;
            int tw = tile.getWidth();
            int th = tile.getHeight();
            for (int w = 0; w < tw; w++) {
                for (int h = 0; h < th; h++) {
                    int packed = pixels[h * tw + w];

                    b = (packed & 0x00FF0000) >> 16;
                    g = (packed & 0x0000FF00) >> 8;
                    r = (packed & 0x000000FF);
                    pixel[0] = r;
                    pixel[1] = g;
                    pixel[2] = b;
                    pixel[3] = 0xFFFFFFFF;

                    x = tile.getX0() * tile.getWidth() + w;
                    y = tile.getY0() * tile.getHeight() + h;

                    //printf("SP: orig: %d %d\n",x,y);
                    raster.setPixel(x, y, pixel);
                }
            }
            //printf("create image: %d\n", (int) ((System.nanoTime() - t0) / 1e6));
        }
    }

    private void writeImage(RenderTile tile) {
        BufferedImage image = new BufferedImage(tile.getWidth(), tile.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[tile.getWidth() * tile.getHeight()];
        WritableRaster raster = image.getRaster();

        int x;
        int y;

        tile.getDest().getBuffer().get(pixels).rewind();
        int r, g, b;
        int tw = tile.getWidth();
        int th = tile.getHeight();
        for (int w = 0; w < tw; w++) {
            for (int h = 0; h < th; h++) {
                int packed = pixels[h * tw + w];

                b = (packed & 0x00FF0000) >> 16;
                g = (packed & 0x0000FF00) >> 8;
                r = (packed & 0x000000FF);
                pixel[0] = r;
                pixel[1] = g;
                pixel[2] = b;
                pixel[3] = 0xFFFFFFFF;

                //printf("SP: orig: %d %d\n",x,y);
                raster.setPixel(w, h, pixel);
            }
        }

        try {
            ImageIO.write(image, "png", new File("/tmp/tile_" + tile.getX0() + "_" + tile.getY0() + ".png"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

}

