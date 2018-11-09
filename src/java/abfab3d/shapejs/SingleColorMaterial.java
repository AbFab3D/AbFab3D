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
    static final String SINGLE_COLOR = "SingleColor";

    public SingleColorMaterial() {
        this(SINGLE_COLOR);
    }

    public SingleColorMaterial(String name) {
        this(name, SINGLE_COLOR,new Color(0.97,0.97,0.97));
    }
    public SingleColorMaterial(String name,String label) {
        this(name, label,new Color(0.97,0.97,0.97));
    }

    public SingleColorMaterial(double red, double green, double blue) {
        this(SINGLE_COLOR, SINGLE_COLOR,new Color(red, green, blue));
    }

    public SingleColorMaterial(Color diffuse) {
        this(SINGLE_COLOR, SINGLE_COLOR,diffuse);
    }

    public SingleColorMaterial(String name, String label, Color diffuse) {
        super(name,label);
        m_shader = new PhongShader(0.1, diffuse, new Color(0, 0, 0), new Color(1, 1, 1), 0);
        mp_renderingParams.setValue(m_shader);
    }

    public MaterialShader getShader() {
        return m_shader;
    }

    public static Material _getInstance() {
        if (instance != null) return instance;
        instance = new SingleColorMaterial();

        return instance;
    }
}