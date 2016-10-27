package abfab3d.shapejs;

import abfab3d.core.Color;
import abfab3d.core.DataSource;
import abfab3d.core.MaterialShader;
import abfab3d.param.BaseParameterizable;

/**
 * Metal shader
 *
 * @author Alan Hudson
 */
public class MetalShader extends BaseParameterizable implements MaterialShader {
    private PhongParams sparams;

    public MetalShader() {
        sparams = new PhongParams();
        addParams(sparams.getParams());
    }

    public MetalShader(double ambientIntensity, Color diffuseColor, Color emissiveColor, Color specularColor,
                       double shininess, Color albedo, double roughness, double gradientFactor) {
        sparams = new PhongParams();
        sparams.setAmbientIntensity(ambientIntensity);
        sparams.setDiffuseColor(diffuseColor);
        sparams.setEmissiveColor(emissiveColor);
        sparams.setSpecularColor(specularColor);
        sparams.setShininess(shininess);
        sparams.setAlbedo(albedo);
        sparams.setRoughness(roughness);
        sparams.setGradientFactor(gradientFactor);

        addParams(sparams.getParams());
    }

    @Override
    public DataSource getRenderingSource(DataSource source) {
        return source;
    }

    @Override
    public RenderingParams getShaderParams() {
        return sparams;
    }

    public void setDiffuseColor(Color diffuseColor) {
        sparams.setDiffuseColor((Color) diffuseColor.clone());
    }

    public void setEmissiveColor(Color emissiveColor) {
        sparams.setEmissiveColor((Color) emissiveColor.clone());
    }

    public void setShininess(double shininess) {
        sparams.setShininess(shininess);
    }

    public void setSpecularColor(Color specularColor) {
        sparams.setSpecularColor((Color)specularColor.clone());
    }

    public void setAmbientIntensity(double ambientIntensity) {
        sparams.setAmbientIntensity(ambientIntensity);
    }

    public void setAlbedo(Color albedo) {
        sparams.setAlbedo((Color) albedo.clone());
    }

    public void setRoughness(double roughness) {
        sparams.setRoughness(roughness);
    }

    public void setGradientFactor(double factor) {
        sparams.setGradientFactor(factor);
    }
}
