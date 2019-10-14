/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2019
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

    static final double DEF_AMBIENT_INTENSITY  = 0.1;
    static final Color DEF_DIFFUSE_COLOR = new Color(0.97,0.97,0.97);
    static final Color DEF_EMISSIVE_COLOR = new Color(0, 0, 0);
    static final Color DEF_SPECULAR_COLOR = new Color(1, 1, 1);
    static final double DEF_SHININESS = 0;

    public SingleColorMaterial() {
        this(SINGLE_COLOR);
    }

    public SingleColorMaterial(String name) {
        this(name, SINGLE_COLOR,DEF_DIFFUSE_COLOR);
    }
    public SingleColorMaterial(String name,String label) {
        this(name, label,DEF_DIFFUSE_COLOR);
    }

    public SingleColorMaterial(double red, double green, double blue) {
        this(SINGLE_COLOR, SINGLE_COLOR,new Color(red, green, blue));
    }

    public SingleColorMaterial(Color diffuse) {
        this(SINGLE_COLOR, SINGLE_COLOR,diffuse);
    }

    public SingleColorMaterial(String name, String label, Color diffuse) {
        super(name,label);
        m_shader = new PhongShader(DEF_AMBIENT_INTENSITY, diffuse, DEF_EMISSIVE_COLOR, DEF_SPECULAR_COLOR, DEF_SHININESS);
        mp_renderingParams.setValue(m_shader);
    }

    public void setShaderParam(String name, Object value){
        m_shader.set(name, value);
    }

    public MaterialShader getShader() {
        return m_shader;
    }

    public static Material getInstance() {

        // Change to return new SingleColorMaterial
        instance = new SingleColorMaterial();

        return instance;
    }
}