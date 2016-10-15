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
import abfab3d.param.*;
import static abfab3d.core.Output.printf;

/**
 * Material parameters for Phong Shading
 *
 * @author Alan Hudson
 */
public class PhongParams extends RenderingParams {
    private ColorParameter mp_diffuseColor = new ColorParameter("diffuseColor","Diffuse Color",new Color(0.8,0.8,0.8));
    private ColorParameter mp_emissiveColor = new ColorParameter("emissiveColor","Emissive Color",new Color(0,0,0));
    private ColorParameter mp_specularColor = new ColorParameter("specularColor","Diffuse Color",new Color(1,1,1));
    private DoubleParameter mp_shininess = new DoubleParameter("shininess","How reflective", 0.2);
    private DoubleParameter mp_ambientIntensity = new DoubleParameter("ambientIntensity","Ambient light", 0.2);

    // TODO: These are really metal stuff, need to separate
    private ColorParameter mp_albedo = new ColorParameter("albedo","albedo",new Color(0,0,0));
    private DoubleParameter mp_roughness = new DoubleParameter("roughness","How rough", 0,0,1);
    private DoubleParameter mp_gradientFactor = new DoubleParameter("gradientFactor","Factor to underlying gradientStep",1);

    private Parameter m_aparam[] = new Parameter[]{
            mp_diffuseColor,
            mp_emissiveColor,
            mp_specularColor,
            mp_albedo,
            mp_shininess,
            mp_ambientIntensity,
            mp_roughness,
            mp_gradientFactor
    };

    public PhongParams() {
        initParams();
    }

    public PhongParams(Color diffuseColor, Color emissiveColor, double shininess, Color specularColor, double ambientIntensity) {
        initParams();

        setDiffuseColor(diffuseColor);
        setEmissiveColor(emissiveColor);
        setShininess(shininess);
        setSpecularColor(specularColor);
        setAmbientIntensity(ambientIntensity);
    }

    public PhongParams(Color diffuseColor, Color emissiveColor, double shininess, Color specularColor, Color albedo, double ambientIntensity) {
        initParams();

        setDiffuseColor(diffuseColor);
        setEmissiveColor(emissiveColor);
        setShininess(shininess);
        setSpecularColor(specularColor);
        setAmbientIntensity(ambientIntensity);
        setAlbedo(albedo);
    }

    protected void initParams(){
        super.addParams(m_aparam);
    }

    public Color getDiffuseColor() {
        return mp_diffuseColor.getValue();
    }

    public void setDiffuseColor(Color diffuseColor) {
        mp_diffuseColor.setValue((Color) diffuseColor.clone());
    }

    public Color getEmissiveColor() {
        return mp_emissiveColor.getValue();
    }

    public void setEmissiveColor(Color emissiveColor) {
        mp_emissiveColor.setValue((Color) emissiveColor.clone());
    }

    public double getShininess() {
        return mp_shininess.getValue();
    }

    public void setShininess(double shininess) {
        mp_shininess.setValue(shininess);
    }

    public Color getSpecularColor() {
        return mp_specularColor.getValue();
    }

    public void setSpecularColor(Color specularColor) {
        mp_specularColor.setValue((Color)specularColor.clone());
    }

    public double getAmbientIntensity() {
        return mp_ambientIntensity.getValue();
    }

    public void setAmbientIntensity(double ambientIntensity) {
        mp_ambientIntensity.setValue(ambientIntensity);
    }

    public Color getAlbedo() {
        return mp_albedo.getValue();
    }

    public void setAlbedo(Color albedo) {
        mp_albedo.setValue((Color) albedo.clone());
    }

    public void setRoughness(double val) {
        mp_roughness.setValue(val);
    }

    public double getRoughness() {
        return mp_roughness.getValue();
    }

    public void setGradientFactor(double val) {
        mp_gradientFactor.setValue(val);
    }

    public double getGradientFactor() {
        return mp_gradientFactor.getValue();
    }
}
