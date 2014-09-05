/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.io.input;

import abfab3d.grid.AttributeGrid;
import org.w3c.dom.Element;

import java.io.IOException;

/**
 * SVX Reader.
 *
 * Reads the Shapeways voxel transmittal format.
 *
 * @author Alan Hudson
 */
public class SVXReader {
    /**
     * Load a SVX file into a grid.
     *
     * @param file The zip file
     * @return
     * @throws IOException
     */
    public AttributeGrid load(String file) throws IOException {
        return null;
    }

    /**
     * Parse the manifest file
     * @param root The root grid
     * @return
     */
    private SVXManifest parseManifest(Element root) {
        return null;
    }
}
