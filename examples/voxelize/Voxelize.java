import abfab3d.grid.*;
import abfab3d.io.input.*;
import abfab3d.io.output.*;
import abfab3d.util.BoundingBoxCalculator;
import abfab3d.util.MathUtil;
import java.io.IOException;
import static abfab3d.util.Units.MM;

/**
 * Simple example showing how to voxelize an STL model and then write it back to disk.
 *
 * @author Alan Hudson
 */
public class Voxelize {
    public static final void main(String[] args) throws IOException {
        int maxAttribute = 255;
        double voxelSize = 0.05 * MM;

        String filePath = args[0];

        // Read the STL file, use the bounds to determine voxel grid size
        STLReader stl = new STLReader();
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        stl.read(filePath, bb);
        double bounds[] = new double[6];
        bb.getBounds(bounds);

        // Add a 1 voxel margin around the model
        MathUtil.roundBounds(bounds, voxelSize);
        bounds = MathUtil.extendBounds(bounds, 1 * voxelSize);
        int nx = (int) Math.round((bounds[1] - bounds[0]) / voxelSize);
        int ny = (int) Math.round((bounds[3] - bounds[2]) / voxelSize);
        int nz = (int) Math.round((bounds[5] - bounds[4]) / voxelSize);

        WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);
        rasterizer.setMaxAttributeValue(maxAttribute);

        stl.read(filePath, rasterizer);

        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        grid.setGridBounds(bounds);

        rasterizer.getRaster(grid);

        double errorFactor = 0.5;
        double maxDecimationError = errorFactor * voxelSize * voxelSize;

        // Write out the grid to an STL file
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(50);
        meshmaker.setThreadCount(Runtime.getRuntime().availableProcessors());
        meshmaker.setSmoothingWidth(0.5);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(10);
        meshmaker.setMaxAttributeValue(maxAttribute);

        STLWriter stlw = new STLWriter(args[1]);
        meshmaker.makeMesh(grid, stlw);
        stlw.close();
    }
}
