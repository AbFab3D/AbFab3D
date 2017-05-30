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
package abfab3d.grid.op;

import abfab3d.core.Grid2D;
import abfab3d.core.GridDataChannel;

import static abfab3d.core.MathUtil.step10;


/**
 * Calculates area of shapes defined on 2D grid
 *
 * @author Vladimir Bulatov
 */
public class AreaCalculator2D {

    double m_area = 0;

    public void calculate(Grid2D grid) {

        GridDataChannel ch = grid.getDataDesc().getChannel(0);
        if (ch.getType().equals(GridDataChannel.DENSITY))
            m_area = getAreaFromDensity(grid);
        else if (ch.getType().equals(GridDataChannel.DISTANCE)) {
            m_area = getAreaFromDistance(grid);
        }
    }


    /**
     * calculates area from density grid
     */
    public static double getAreaFromDensity(Grid2D grid) {

        double area = 0;

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        double vs = grid.getVoxelSize();
        GridDataChannel ch = grid.getDataDesc().getChannel(0);
        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {
                area += ch.getValue(grid.getAttribute(x, y));

            }
        }
        return area * vs * vs;
    }

    /**
     * calculates area from density grid
     */
    public static double getAreaFromDistance(Grid2D grid) {

        double area = 0;

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        double vs = grid.getVoxelSize();
        GridDataChannel ch = grid.getDataDesc().getChannel(0);
        for (int y = 0; y < ny; y++) {
            for (int x = 0; x < nx; x++) {
                area += step10(ch.getValue(grid.getAttribute(x, y)), vs);
            }
        }
        return area * vs * vs;
    }

} // AreaCalculator2D