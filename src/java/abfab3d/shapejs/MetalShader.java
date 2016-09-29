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
    private PhongParams sparams;

    public MetalShader(double ambientIntensity, Color diffuseColor, Color emissiveColor, Color specularColor, double shininess, Color albedo) {
        sparams = new PhongParams();
        sparams.setAmbientIntensity(ambientIntensity);
        sparams.setDiffuseColor(diffuseColor);
        sparams.setEmissiveColor(emissiveColor);
        sparams.setSpecularColor(specularColor);
        sparams.setShininess(shininess);
        sparams.setAlbedo(albedo);

        addParams(sparams.getParams());
    }

    @Override
    public DataSource getRenderingSource(DataSource source) {
        // TODO: this needs to change gradientStep currently...
        return source;
    }

    @Override
    public RenderingParams getShaderParams() {
        return sparams;
    }
}
