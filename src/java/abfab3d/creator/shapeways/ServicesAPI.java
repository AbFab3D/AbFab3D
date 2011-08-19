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

package abfab3d.creator.shapeways;

// External Imports
import java.io.InputStream;
import java.awt.Image;

// Internal Imports

/**
 * Services available to hosted editors.
 *
 * In theory we could also replicate the Shapeways API functions here.  Not sure
 * yet whether that's a good idea.
 *
 * @author Alan Hudson
 */
public interface ServicesAPI {
    public enum PrintableStatus {
       OK,                    // Model is ok in specified material
       THIN_WALLS,            // The wall thickness is too small
       THIN_WALLS_SUSPECT,    // The wall thickness may be too small
       TOO_LARGE,             // The model is too large for available printers
       NON_MANIFOLD           // The model is not manifold
    };

    // TODO: Design Questions
    // Require x3db?  support all backend filetypes?

    /**
     * Checks whether a file is printable.  Implements some parts of the model
     * checking pipeline.  Attemps to give a fast check on whether a model is
     * good to print.
     *
     * @param is The stream to check.  Must be an X3DB stream.
     * @param materialID The materials to check for
     * @return The status
     */
    public PrintableStatus[] isPrintable(InputStream is, int[] materialID);

    /**
     * Create an image for this file.
     *
     * @param width The width in pixels
     * @param height The height in pixels
     * @param viewpoint How to calculate a viewpoint.  OVER_SHOULDER, FRONT_FACING, ...
     * @param quality The quality of the image.  FASTEST,HIGHEST,NORMAL.
     */
    public Image createImage(InputStream is, int width, int height, String viewpoint, String quality);
}