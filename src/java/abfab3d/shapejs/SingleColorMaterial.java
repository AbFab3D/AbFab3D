/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2015
 * Java Source
 * <p/>
 * All rights reserved
 ****************************************************************************/

package abfab3d.shapejs;

import abfab3d.core.Color;
import abfab3d.core.Material;
import abfab3d.core.MaterialShader;
import abfab3d.core.MaterialType;


/**
 * Single color material
 *
 * @author Alan Hudson
 */
public class SingleColorMaterial extends BaseRenderableMaterial {
    private PhongShader m_shader;
    private static Material instance = null;
    protected MaterialType m_matType = MaterialType.SINGLE_MATERIAL;

    public SingleColorMaterial() {
        super("SingleColor");

        m_shader = new PhongShader(0.1, new Color(0.97, 0.97, 0.97), new Color(0, 0, 0), new Color(1, 1, 1), 0);
        mp_renderingParams.setValue(m_shader);
    }

    public SingleColorMaterial(Color diffuse) {
        super("SingleColor");

        m_shader = new PhongShader(0.1, diffuse, new Color(0, 0, 0), new Color(1, 1, 1), 0);
        mp_renderingParams.setValue(m_shader);
    }

    public MaterialShader getShader() {
        return m_shader;
    }

    public static Material getInstance() {
        if (instance != null) return instance;
        instance = new SingleColorMaterial();

        return instance;
    }

    public MaterialType getMaterialType() {
        return m_matType;
    }
}