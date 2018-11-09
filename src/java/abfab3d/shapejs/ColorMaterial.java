/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2017
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.shapejs;

import abfab3d.core.Color;

import abfab3d.core.Material;
import abfab3d.core.MaterialShader;
import abfab3d.shapejs.BasePrintableMaterial;
import abfab3d.shapejs.PhongShader;

/**
 * Represents single color material of given color
 *
 * @author Vladimir Bulatov
 */
public class ColorMaterial extends BasePrintableMaterial {

    private PhongShader m_shader;

    public ColorMaterial(double red, double green, double blue) {
        super("Color");

        m_shader = new PhongShader(0.1, new Color(red, green, blue), new Color(0, 0, 0), new Color(1, 1, 1), 0);
        mp_renderingParams.setValue(m_shader);
    }

    public MaterialShader getShader() {
        return m_shader;
    }

    public void setSpecularColor(double red, double green, double blue) {
        m_shader.setSpecularColor(new Color(red, green, blue));
    }

    public void setDiffuseColor(double red, double green, double blue) {
        m_shader.setDiffuseColor(new Color(red, green, blue));
    }

    public void setEmissiveColor(double red, double green, double blue) {
        m_shader.setEmissiveColor(new Color(red, green, blue));
    }

    public void setShininess(double value) {
        m_shader.setShininess(value);
    }
}
