package abfab3d.grid;

import com.sun.japex.*;

import java.util.Random;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public class ArrayGridDriver extends BaseGridDriver {

    public void allocate(TestCase testCase) {
        int w = Integer.parseInt(testCase.getParam("width"));
        int h = Integer.parseInt(testCase.getParam("height"));
        int d = Integer.parseInt(testCase.getParam("depth"));

        grid =  new ArrayGridByte(w,h,d,voxel_size, slice_height);
        //System.out.println("Grid dim: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());
        //System.out.println("\nPos FreeMem: " + Runtime.getRuntime().freeMemory() + " max: " +  Runtime.getRuntime().maxMemory() + " tot: " +  Runtime.getRuntime().totalMemory() + " alc: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
    }
}
