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
import java.util.Set;

// Internal Imports
import abfab3d.grid.*;


/**
 * Attempts to merge regions together
 *
 * @author Alan Hudson
 */
public interface RegionMerger {
    /**
     * Attempts to merge regions together.
     *
     * @regions The regions to merge, results placed here
     */
    public void merge(Set<Region> regions);
}
