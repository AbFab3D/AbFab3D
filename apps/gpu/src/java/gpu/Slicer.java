package gpu;

import abfab3d.grid.Bounds;
import com.jogamp.opencl.*;

import java.util.ArrayList;
import java.util.List;
import static abfab3d.util.Output.printf;

/**
 * Slices a job into pieces.
 *
 * @author Alan Hudson
 */
public class Slicer {
    private CLDevice[] devices;
    private Bounds bounds;
    private double vs;

    public Slicer(CLDevice[] devices, Bounds bounds, double vs) {
        this.devices = devices;
        this.bounds = bounds;
        this.vs = vs;
    }

    public List<Slice> slice() {
        ArrayList<Slice> ret_val = new ArrayList<Slice>();

        double w = (bounds.xmax - bounds.xmin) / 2;
        double h = (bounds.ymax - bounds.ymin) / 2;
        double d = (bounds.zmax - bounds.zmin) / 2;

        int factor = 100;
        double slice_size = 2 * h / (devices.length * factor);
        int len = (int) (2 * h / slice_size);

        printf("Slices: %d\n",len);
        for(int i=0; i < len; i++) {
            double[] dev_bounds = null;

            if (len == 1) {
                dev_bounds = new double[]{
                        -w, w, -h, h, -d, d
                };
                Slice slice = new Slice();
                slice.ymin = -h;
                slice.ymax = h;
                slice.idx = i;

                ret_val.add(slice);
            } else {
                dev_bounds = new double[]{
                        -w, w, -h + i * slice_size, -h + (i + 1) * slice_size, -d, d
                };
                Slice slice = new Slice();
                slice.ymin = -h + i * slice_size;
                slice.ymax = -h + (i + 1) * slice_size;
                slice.idx = i;

                //printf("Slice: %f -> %f\n",slice.ymin,slice.ymax);
                ret_val.add(slice);
            }

        }

        return ret_val;
    }
}
