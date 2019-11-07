/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2019
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.io.cli;

import abfab3d.core.Bounds;

import java.io.IOException;
import java.io.InputStream;

public interface SliceReader {
    void load(String file) throws IOException;
    void load(InputStream is) throws IOException;

    SliceLayer getSlice(int idx);

    SliceLayer[] getSlices();

    int getNumSlices();

    /**
     * Indicates the units of the coordinates in mm
     * @return
     */
    double getUnits();

    Bounds getBounds();
}
