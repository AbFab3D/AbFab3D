package abfab3d.creator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Generates an image from parameters.  A kernel should require
 * no user-input.
 *
 * All Kernels should have a no parameter constructor.  All
 * instance params should come through the generate method.
 *
 * Hosted editors will insure that only a single thread will call
 * generate at a time on a particular instance.
 * This means class variable usage is safe.
 *
 * @author Alan Hudson
 */
public interface ImageKernel {
    /** The accuracy to generate the model. */
    public enum Accuracy {VISUAL, PRINT};

    /**
     * Generate X3D stream from the specified parameters.  Do not include any
     * X3D Bindables such as Viewpoints, NavigationInfo, Fog, or Background nodes.
     * No lights either.  Just geometry and appearance information.
     *
     * @param params The parameters
     * @param acc The accuracy to generate the model
     * @param os The stream to place the output
     */
    public KernelResults generateImage(Map<String,Object> params, ImageKernel.Accuracy acc, OutputStream os) throws IOException;

    /**
     * Get the parameters for this editor.
     *
     * @return The parameters.
     */
    public Map<String,Parameter> getParams();
}
