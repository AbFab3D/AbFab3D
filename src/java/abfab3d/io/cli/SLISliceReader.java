/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.io.cli;


import abfab3d.core.Bounds;

import java.io.IOException;
import java.io.InputStream;

/**
 * SLI Format used by some SLS vendors.  Basically CLI format with an index at the end
 *
 * @author Alan Hudson
 */
public class SLISliceReader extends BaseSliceReader {

    @Override
    public void load(InputStream is) throws IOException {

    }

    @Override
    public SliceLayer getSlice(int idx) {
        return null;
    }

    @Override
    public SliceLayer[] getSlices() {
        return new SliceLayer[0];
    }

    @Override
    public int getNumSlices() {
        return 0;
    }

    @Override
    public double getUnits() {
        return 0;
    }

    @Override
    public Bounds getBounds() {
        return null;
    }
}
