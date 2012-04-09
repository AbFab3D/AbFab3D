package abfab3d.grid;

import com.sun.japex.TestCase;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public class BlockArrayGridRLEArrayListDriver extends BaseGridDriver {
    public void allocate(TestCase testCase) {
        int w = Integer.parseInt(testCase.getParam("width"));
        int h = Integer.parseInt(testCase.getParam("height"));
        int d = Integer.parseInt(testCase.getParam("depth"));

        grid =  new BlockArrayGrid(w,h,d,voxel_size, slice_height, new int[] {3,3,3}, BlockArrayGrid.BlockType.RLEArrayList);
        ((BlockArrayGrid)grid).clean(); // only do this if checking for compressed memory usage
    }
}
