package gpu;

/**
 * Slice for GPU computation
 *
 * @author Alan Hudson
 */
public class Slice {
    /** The index of the slice */
    public int idx;

    /** ymin in world space */
    public double ymin;

    /** ymax in world space */
    public double ymax;

    public SliceBuffer buffer = null;

    public float[] voxel = new float[3];
    public float[] offset = new float[3];


}
