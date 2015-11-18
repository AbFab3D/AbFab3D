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

package abfab3d.datasources;

import abfab3d.grid.Grid2D;
import abfab3d.grid.Grid2DShort;
import abfab3d.util.ImageGray16;
import abfab3d.util.ImageUtil;


import java.awt.image.BufferedImage;

import static abfab3d.util.Units.MM;

public class ImageWrapper {
    
    BufferedImage image;
    Grid2D grid;
    static final double DEFAULT_PIXEL_SIZE = 0.1*MM;
    private double vs;

    public ImageWrapper(BufferedImage image){
        this.image = image;
        vs = DEFAULT_PIXEL_SIZE;
    }
    public ImageWrapper(BufferedImage image, double vs){
        this.image = image;
        this.vs = vs;
    }
    public int getWidth(){
        return image.getWidth();
    }
    public int getHeight(){
        return image.getHeight();
    }
    public BufferedImage getImage(){
        return image;
    }

    /**
     * Get a 2D grid representation of this image
     * @return
     */
    public Grid2D getGrid() {
        if (grid != null) return grid;

        grid = Grid2DShort.convertImageToGrid(image, vs);

        return grid;
    }
}