/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid.query;

// External Imports

// Internal Imports

import abfab3d.grid.*;
import abfab3d.util.Bounds;

/**
 * Calculate the bounds of the object based on the surface density
 *
 * @author Alan Hudson
 */
public class BoundsCalculator {
    private double m_threshold;

    public BoundsCalculator(double threshold) {
        m_threshold = threshold;
    }

    public Bounds execute(AttributeGrid grid,  AttributeChannel channel) {
        double[] min = new double[3];
        double[] max = new double[3];

        min[0] = Double.MAX_VALUE;
        min[1] = Double.MAX_VALUE;
        min[2] = Double.MAX_VALUE;

        max[0] = -Double.MAX_VALUE;
        max[1] = -Double.MAX_VALUE;
        max[2] = -Double.MAX_VALUE;

        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        double[] coord = new double[3];

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                int zmin = Integer.MAX_VALUE;
                int zmax = Integer.MIN_VALUE;
                for(int z=0; z < depth; z++) {
                    if (channel.getValue(grid.getAttribute(x,y,z)) > m_threshold) {
                        if (z > zmax) zmax = z;
                        if (z < zmin) zmin = z;
                    }
                }
                if (zmin < Integer.MAX_VALUE) {
                    grid.getWorldCoords(x,y,zmin,coord);
                    if (coord[0] < min[0]) {
                        min[0] = coord[0];
                    }
                    if (coord[1] < min[1]) {
                        min[1] = coord[1];
                    }
                    if (coord[2] < min[2]) {
                        min[2] = coord[2];
                    }
                }
                if (zmax > Integer.MIN_VALUE) {
                    grid.getWorldCoords(x,y,zmax,coord);
                    if (coord[0] > max[0]) {
                        max[0] = coord[0];
                    }
                    if (coord[1] > max[1]) {
                        max[1] = coord[1];
                    }
                    if (coord[2] > max[2]) {
                        max[2] = coord[2];
                    }
                }

            }
        }


        Bounds bounds = new Bounds(min[0],max[0],min[1],max[1],min[2],max[2]);

        return bounds;
    }

    public Bounds execute(AttributeGrid grid,  AttributeChannel channel, int sign) {
        double[] min = new double[3];
        double[] max = new double[3];

        min[0] = Double.MAX_VALUE;
        min[1] = Double.MAX_VALUE;
        min[2] = Double.MAX_VALUE;

        max[0] = -Double.MAX_VALUE;
        max[1] = -Double.MAX_VALUE;
        max[2] = -Double.MAX_VALUE;

        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        double[] coord = new double[3];

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                int zmin = Integer.MAX_VALUE;
                int zmax = Integer.MIN_VALUE;
                for(int z=0; z < depth; z++) {
                    if ((channel.getValue(grid.getAttribute(x,y,z)) - m_threshold)*sign > 0) {
                        if (z > zmax) zmax = z;
                        if (z < zmin) zmin = z;
                    }
                }
                if (zmin < Integer.MAX_VALUE) {
                    grid.getWorldCoords(x,y,zmin,coord);
                    if (coord[0] < min[0]) {
                        min[0] = coord[0];
                    }
                    if (coord[1] < min[1]) {
                        min[1] = coord[1];
                    }
                    if (coord[2] < min[2]) {
                        min[2] = coord[2];
                    }
                }
                if (zmax > Integer.MIN_VALUE) {
                    grid.getWorldCoords(x,y,zmax,coord);
                    if (coord[0] > max[0]) {
                        max[0] = coord[0];
                    }
                    if (coord[1] > max[1]) {
                        max[1] = coord[1];
                    }
                    if (coord[2] > max[2]) {
                        max[2] = coord[2];
                    }
                }

            }
        }


        Bounds bounds = new Bounds(min[0],max[0],min[1],max[1],min[2],max[2]);

        return bounds;
    }
}
