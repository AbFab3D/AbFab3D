package abfab3d.shapejs;

import abfab3d.core.Color;
import abfab3d.core.DataSource;
import abfab3d.param.BaseParameterizable;

/**
 * Metal shader
 *
 * @author Alan Hudson
 */
public class MetalShader extends BaseParameterizable implements MaterialShader {
    private PhongParams params;

    public MetalShader(double ambientIntensity, Color diffuseColor, Color emissiveColor, Color specularColor, double shininess, Color albedo) {
        params = new PhongParams();
        params.setAmbientIntensity(ambientIntensity);
        params.setDiffuseColor(diffuseColor);
        params.setEmissiveColor(emissiveColor);
        params.setSpecularColor(specularColor);
        params.setShininess(shininess);
        params.setAlbedo(albedo);
    }

    @Override
    public DataSource getRenderingSource(DataSource source) {
        // TODO: this needs to change gradientStep currently...
        return source;
    }

    @Override
    public RenderingParams getShaderParams() {
        return params;
    }
}
