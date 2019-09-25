/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2017
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.core;

import java.awt.image.BufferedImage;

/**
 * Able to produce a BufferedImage result
 *
 * @author Vladimir Bulatov
 */
public interface ImageStackProducer {

    /**
     * Return a BufferedImage produced by the object 
     * @return The image
     */

    public BufferedImage getImage(int index);

    /**
       return count of images 
     */
    public int getCount();


}
