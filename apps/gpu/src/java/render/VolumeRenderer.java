package render;

import com.jogamp.opencl.CLBuffer;

import javax.vecmath.Matrix4f;

/**
 * Volume renderer using GPU
 *
 * @author Alan Hudson
 */
public class VolumeRenderer {
    private String kernel;

    /**
     * Render the kernel.
     *
     * @param dest
     */
    public void render(Matrix4f view, CLBuffer dest) {
    }
}
