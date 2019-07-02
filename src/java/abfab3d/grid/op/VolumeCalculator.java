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

// Internal Imports

import abfab3d.core.AttributeGrid;
import abfab3d.core.GridDataChannel;

/**
 * Calculates the approximate volume of shapes on the density grid
 * <p>
 * uses density value as weight of voxel contribution to the total volume
 *
 * @author Vladimir Bulatov
 */
public class VolumeCalculator {

    public VolumeCalculator() {
    }

    /**
     * calculates grid volume
     *
     * @param grid The grid to use for grid A.
     * @return The null
     */
    public double getVolume(AttributeGrid grid) {

        GridDataChannel dm = grid.getDataDesc().getDensityChannel();
        if (dm == null)
            throw new RuntimeException("grid has no density channel");
        int
                nx = grid.getWidth(),
                ny = grid.getHeight(),
                nz = grid.getDepth();
        double sh = grid.getSliceHeight();
        double vs = grid.getVoxelSize();

        double volume = 0;
        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {
                for (int z = 0; z < nz; z++) {
                    volume += dm.getValue(grid.getAttribute(x, y, z));
                }
            }
        }
        return volume * vs * vs * sh;
    }
}
