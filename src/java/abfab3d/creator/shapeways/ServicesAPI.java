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

    /**
     * Checks whether a file is printable.  Implements some parts of the model
     * checking pipeline.  Attemps to give a fast check on whether a model is
     * good to print.
     *
     * @param is The stream to check
     * @param materialID The materials to check for
     * @return The status
     */
    public PrintableStatus[] isPrintable(InputStream is, int[] materialID);
}