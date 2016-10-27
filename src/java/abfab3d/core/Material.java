package abfab3d.core;

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
}
