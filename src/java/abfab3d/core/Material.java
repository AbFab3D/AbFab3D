package abfab3d.core;

import java.util.List;

/**
 * A physical printing material
 *
 * @author Alan Hudson
 */
public interface Material {
    /**
     * Get a shader for rendering
     * @return
     */
    public MaterialShader getShader();
    public String getName();
    public String getAlternateName();
    public List<String> getAlternateNames();
    public String getLabel();

    /**
     * Does this material contain color information
     * @return
     */
    public MaterialType getMaterialType();
}
