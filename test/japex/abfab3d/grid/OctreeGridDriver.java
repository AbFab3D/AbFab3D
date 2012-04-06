package abfab3d.grid;

import com.sun.japex.TestCase;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public class OctreeGridDriver extends BaseGridDriver {
    double voxel_size = 0.001;
    double slice_height = 0.001;

    public void allocate(TestCase testCase) {
        int w = Integer.parseInt(testCase.getParam("width"));
        int h = Integer.parseInt(testCase.getParam("height"));
        int d = Integer.parseInt(testCase.getParam("depth"));

        grid =  new OctreeAttributeGridByte(w,h,d,voxel_size, slice_height);

        String input = testCase.getParam("input");
    }
}
