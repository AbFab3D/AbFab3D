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
package abfab3d.core;

/**
 * Able to produce a Grid2D result
 *
 * @author Vladimir Bulatov
 */
public interface Grid2DProducer {

    /**
     * Return a Grid2D produced by the object 
     * @return The grid
     */
    public Grid2D getGrid2D();

}
