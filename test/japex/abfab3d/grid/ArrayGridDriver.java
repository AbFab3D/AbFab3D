package abfab3d.grid;

import com.sun.japex.*;

import java.util.Random;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public class ArrayGridDriver extends BaseGridDriver {
    double voxel_size = 0.001;
    double slice_height = 0.001;

    public void allocate(TestCase testCase) {
        int w = Integer.parseInt(testCase.getParam("width"));
        int h = Integer.parseInt(testCase.getParam("height"));
        int d = Integer.parseInt(testCase.getParam("depth"));

        //System.out.println("\nPre FreeMem: " + Runtime.getRuntime().freeMemory() + " max: " +  Runtime.getRuntime().maxMemory() + " tot: " +  Runtime.getRuntime().totalMemory() + " alc: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

        grid =  new ArrayGridByte(w,h,d,voxel_size, slice_height);
        //System.out.println("Grid dim: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());
        //System.out.println("\nPos FreeMem: " + Runtime.getRuntime().freeMemory() + " max: " +  Runtime.getRuntime().maxMemory() + " tot: " +  Runtime.getRuntime().totalMemory() + " alc: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

        String input = testCase.getParam("input");

        if (input.equals("ReadRandom")) {
            writeRandom(grid, 0.1f);
        }
    }
}
