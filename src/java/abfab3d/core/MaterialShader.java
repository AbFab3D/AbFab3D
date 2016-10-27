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

package abfab3d.core;

/**
 * Specified material for rendering.  Two params:  source and renderingParams
 *
 * @author Alan Hudson
 */
public interface MaterialShader {

    /**
     * Get the rendering source for this content.
     * @param source The content source to transform
     * @return
     */
    public DataSource getRenderingSource(DataSource source);

    // TODO: THis should be Parameterizable but its not core
    public Object getShaderParams();
}
