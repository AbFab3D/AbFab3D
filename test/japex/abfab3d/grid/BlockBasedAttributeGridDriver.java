package abfab3d.grid;

import com.sun.japex.TestCase;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public class BlockBasedAttributeGridDriver extends BaseGridDriver {
    double voxel_size = 0.001;
    double slice_height = 0.001;

    public void allocate(TestCase testCase) {
        super.prepare(testCase);

        int w = Integer.parseInt(testCase.getParam("width"));
        int h = Integer.parseInt(testCase.getParam("height"));
        int d = Integer.parseInt(testCase.getParam("depth"));

        grid =  new BlockBasedAttributeGridByte(w,h,d,voxel_size, slice_height);

        String input = testCase.getParam("input");

        if (input.equals("ReadRandom")) {
            writeRandom(grid, 0.1f);
        }
    }
}
