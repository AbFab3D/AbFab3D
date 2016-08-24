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
package abfab3d.shapejs;

import abfab3d.core.Color;
import abfab3d.core.DataSource;

/**
 * Default material for rendering
 *
 * @author Alan Hudson
 */
public class DefaultMaterial extends BaseMaterial {
    private PhongParams params = new PhongParams();

    public DefaultMaterial() {
        params.setAmbientIntensity(0);
        params.setDiffuseColor(new Color(1, 1, 1));
        params.setEmissiveColor(new Color(0, 0, 0));
        params.setSpecularColor(new Color(0.25,0.25,0.25));
        params.setShininess(0.1);

        mp_renderingParams.setValue(params);
    }

    public DataSource getRenderingSource(DataSource source) {
        return source;
    }

    public RenderingParams getRenderingParams() {
        return params;
    }
}
