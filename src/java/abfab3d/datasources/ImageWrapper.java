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

public class ImageWrapper {
    
    BufferedImage image;
    Grid2D grid;

    public ImageWrapper(BufferedImage image){
        this.image = image;
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

        // assume greyscale for now
        grid = new Grid2DShort(image.getWidth(), image.getHeight());

        // I assume this is in Java image coordinates 0,0 in upper left
        short imageDataShort[] = ImageUtil.getGray16Data(image);

        ((Grid2DShort)grid).copyData(imageDataShort);

        return grid;
    }
}