package abfab3d.shapejs;

import abfab3d.param.Parameterizable;

/**
 * A physical printing material
 *
 * @author Alan Hudson
 */
public interface Material extends Parameterizable {
    /**
     * Get a shader for rendering
     * @return
     */
    public MaterialShader getShader();
    public String getName();
}
