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

package abfab3d.io.output;

// External Imports
import java.util.*;

// Internal Imports
import abfab3d.core.Grid;

/**
 * Exporters grids to external formats.
 *
 * @author Alan Hudson
 */
public interface Exporter {
    /**
     * Write a grid to the stream.
     *
     * @param grid The grid to write
     * @param matColors Maps materials to colors
     */
    public void write(Grid grid, Map<Long, float[]> matColors);

    /**
     * Write a grid to the stream using the grid state
     *
     * @param grid The grid to write
     * @param stateColors Maps states to colors
     * @param stateTransparency Maps states to transparency values.  1 is totally transparent.
     */
    public void writeDebug(Grid grid, Map<Integer, float[]> stateColors,
        Map<Integer, Float> stateTransparency);

    /**
     * Close the exporter.  Must be called when done, does not close the passed in stream.
     */
    public void close();
}