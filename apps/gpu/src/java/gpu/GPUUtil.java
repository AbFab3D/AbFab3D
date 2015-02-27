package gpu;

/**
 * Little GPU utilities
 *
 * @author Alan Hudson
 */
public class GPUUtil {
    /**
     * Round up a global size based the group size.  Required by OpenCL rules.
     *
     * @param groupSize
     * @param globalSize
     * @return
     */
    public static int roundUp(int groupSize, int globalSize) {

        if (groupSize == 0) return globalSize;

        return groupSize*((globalSize + groupSize - 1)/groupSize);
        /*
        int r = globalSize % groupSize;
        if (r == 0) {
            return globalSize;
        } else {
            return globalSize + groupSize - r;
        }
        */
    }

}
