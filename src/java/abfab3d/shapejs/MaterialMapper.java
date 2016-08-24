/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.shapejs;

/**
 * Maps material names to implementations
 *
 * @author Alan Hudson
 */
public interface MaterialMapper {
    /**
     * Get an implementation for a material
     * @param mat
     * @return
     */
    public RenderingMaterial getImplementation(String mat);
}
