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
import java.util.Iterator;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.AffineTransformOp;
import java.awt.geom.AffineTransform;

// Internal Imports
import abfab3d.grid.*;

/**
 * Apply an image to a grid.
 *
 *
 * @author Alan Hudson
 */
public class ApplyImage implements Operation {
    /** The y origin */
    private int x0;

    /** The y origin */
    private int y0;

    /** The z origin */
    private int z0;

    /** The width in pixels, image will be rescaled as needed */
    private int w;

    /** The depth in pixels, image will be rescaled as needed */
    private int h;

    /** The image */
    private BufferedImage image;

    /** The threshold for black/white */
    private int threshold;

    /** Black is exterior or white.  Other is outside */
    private boolean blackExterior;

    /** The depth of each pixel, can be negative */
    private int pixelDepth;

    /** The material for new exterior voxels */
    private int material;

    /** Remove stray pixels.  Any exterior pixel with no neighbors */
    private boolean removeStray;

    public ApplyImage(BufferedImage image, int x, int y, int z,
        int w, int h,
        int threshold,
        boolean blackExterior, int pixelDepth, boolean removeStray, int material) {

        this.image = image;
        this.x0 = x;
        this.y0 = y;
        this.z0 = z;
        this.w = w;
        this.h = h;
        this.threshold = threshold;
        this.blackExterior = blackExterior;
        this.pixelDepth = pixelDepth;
        this.material = material;

        this.removeStray = removeStray;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid A.
     * @return The new grid
     */
    public Grid execute(Grid grid) {
        int width = grid.getWidth();
        int depth = grid.getDepth();
        int height = grid.getHeight();

        int src_w_pixels = image.getWidth();
        int src_h_pixels = image.getHeight();

        float w_scale = (float) w / src_w_pixels;
        float h_scale = (float) h / src_h_pixels;

System.out.println("src: " + src_w_pixels + " " + src_h_pixels);
System.out.println("scale: " + w_scale + " h: " + h_scale);
System.out.println("target: " + w + " " + h);
        BufferedImage cell_img =
            new BufferedImage(w, h,
                              BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D g = (Graphics2D)cell_img.getGraphics();

        // TODO: I sort of remember this being crappy.  See dirbs IPTester for results
        AffineTransform tx =
            AffineTransform.getScaleInstance(w_scale, h_scale);
        AffineTransformOp tx_op =
            new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC);


        g.drawImage(image, tx_op, 0, 0);
        long applied = 0;

System.out.println("z0: " + z0 + " depth: " + pixelDepth);
        if (pixelDepth > 0) {
            if (blackExterior) {
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
//                        int rgb = cell_img.getRGB(x, y) & 0xFF;
                        int rgb = cell_img.getRGB(x, h - y - 1) & 0xFF;

                        if (rgb < threshold) {
                            if (removeStray) {
                                boolean anySame = false;
                                int rgb2;

                                if (x  > 1) {
                                    rgb2 = cell_img.getRGB(x-1,h - y - 1) & 0xFF;
                                    if (rgb2 < threshold) {
                                        anySame = true;
                                    }
                                }
                                if (x + 1 < w) {
                                    rgb2 = cell_img.getRGB(x+1,h - y - 1) & 0xFF;
                                    if (rgb2 < threshold) {
                                        anySame = true;
                                    }
                                }
                                if (y + 1 < h) {
                                    rgb2 = cell_img.getRGB(x,h - y - 2) & 0xFF;
                                    if (rgb2 < threshold) {
                                        anySame = true;
                                    }
                                }
                                if (y > 2) {
                                    rgb2 = cell_img.getRGB(x,h - y) & 0xFF;
                                    if (rgb2 < threshold) {
                                        anySame = true;
                                    }
                                }

                                if (!anySame) {
                                    //System.out.println("removing stray");
                                    continue;
                                }
                            }

                            applied++;

                            for(int z = 0; z < pixelDepth; z++) {
                                grid.setData(x0 + x, y0 + y, z0 + z, Grid.EXTERIOR, material);
                            }
                        }
                    }
                }
            } else {
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int rgb = cell_img.getRGB(x, h - y - 1) & 0xFF;

                        if (rgb >= threshold) {
                            if (removeStray) {
                                boolean anySame = false;
                                int rgb2;

                                if (x > 1) {
                                    rgb2 = cell_img.getRGB(x-1,h - y - 1) & 0xFF;
                                    if (rgb2 >= threshold) {
                                        anySame = true;
                                    }
                                }
                                if (x + 1 < w) {
                                    rgb2 = cell_img.getRGB(x+1,h - y - 1) & 0xFF;
                                    if (rgb2 >= threshold) {
                                        anySame = true;
                                    }
                                }
                                if (y + 1 > h) {
                                    rgb2 = cell_img.getRGB(x,h - y - 2) & 0xFF;
                                    if (rgb2 >= threshold) {
                                        anySame = true;
                                    }
                                }
                                if (y > 2) {
//System.out.println("h: " + h + " " + (h-y) + " y: " + y);
                                    rgb2 = cell_img.getRGB(x,h - y) & 0xFF;
                                    if (rgb2 >= threshold) {
                                        anySame = true;
                                    }
                                }

                                if (!anySame) {
//                                    System.out.println("removing stray");
                                    continue;
                                }
                            }

                            applied++;
                            for(int z = 0; z < pixelDepth; z++) {
                                grid.setData(x0 + x, y0 + y, z0 + z, Grid.EXTERIOR, material);
                            }
                        }
                    }
                }
            }
         } else {
            if (blackExterior) {
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
//                        int rgb = cell_img.getRGB(x, y) & 0xFF;
                        int rgb = cell_img.getRGB(x, h - y - 1) & 0xFF;

                        if (rgb < threshold) {
                            if (removeStray) {
                                boolean anySame = false;
                                int rgb2;

                                if (x  > 1) {
                                    rgb2 = cell_img.getRGB(x-1,h - y - 1) & 0xFF;
                                    if (rgb2 < threshold) {
                                        anySame = true;
                                    }
                                }
                                if (x + 1 < w) {
                                    rgb2 = cell_img.getRGB(x+1,h - y - 1) & 0xFF;
                                    if (rgb2 < threshold) {
                                        anySame = true;
                                    }
                                }
                                if (y + 1 < h) {
                                    rgb2 = cell_img.getRGB(x,h - y - 2) & 0xFF;
                                    if (rgb2 < threshold) {
                                        anySame = true;
                                    }
                                }
                                if (y > 2) {
                                    rgb2 = cell_img.getRGB(x,h - y) & 0xFF;
                                    if (rgb2 < threshold) {
                                        anySame = true;
                                    }
                                }

                                if (!anySame) {
                                    //System.out.println("removing stray");
                                    continue;
                                }
                            }

                            applied++;

                            for(int z = 0; z > pixelDepth; z--) {
                                grid.setData(x0 + x, y0 + y, z0 + z, Grid.EXTERIOR, material);
                            }
                        }
                    }
                }
            } else {
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int rgb = cell_img.getRGB(x, h - y - 1) & 0xFF;

                        if (rgb >= threshold) {
                            if (removeStray) {
                                boolean anySame = false;
                                int rgb2;

                                if (x > 1) {
                                    rgb2 = cell_img.getRGB(x-1,h - y - 1) & 0xFF;
                                    if (rgb2 >= threshold) {
                                        anySame = true;
                                    }
                                }
                                if (x + 1 < w) {
                                    rgb2 = cell_img.getRGB(x+1,h - y - 1) & 0xFF;
                                    if (rgb2 >= threshold) {
                                        anySame = true;
                                    }
                                }
                                if (y + 1 > h) {
                                    rgb2 = cell_img.getRGB(x,h - y - 2) & 0xFF;
                                    if (rgb2 >= threshold) {
                                        anySame = true;
                                    }
                                }
                                if (y > 2) {
//System.out.println("h: " + h + " " + (h-y) + " y: " + y);
                                    rgb2 = cell_img.getRGB(x,h - y) & 0xFF;
                                    if (rgb2 >= threshold) {
                                        anySame = true;
                                    }
                                }

                                if (!anySame) {
//                                    System.out.println("removing stray");
                                    continue;
                                }
                            }

                            applied++;
                            for(int z = 0; z > pixelDepth; z--) {
                                grid.setData(x0 + x, y0 + y, z0 + z, Grid.EXTERIOR, material);
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Pixels Applied: " + applied);
        return grid;
    }
}
