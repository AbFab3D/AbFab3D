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
package abfab3d.datasources;

import abfab3d.util.DataSource;

/**
 * A DataSource node that contains multiple children
 *
 * @author Alan Hudson
 */
public interface GroupingNode {
    /**
     * Get the children of this node.
     * @return A live array of children, do not modify
     */
    public DataSource[] getChildren();

    /**
     * Get the bounds of this source containing all its children.
     * Return null for infinite or unknown bounds
     *
     * @return The bounds or null
     */
    /*
    public double[] getBounds();
    */
}
