import abfab3d.grid.*;
import abfab3d.datasources.*;
import abfab3d.grid.op.GridMaker;
import abfab3d.io.input.*;
import abfab3d.io.output.*;
import abfab3d.util.BoundingBoxCalculator;
import abfab3d.util.MathUtil;
import java.io.IOException;
import static abfab3d.util.Units.MM;

/**
 * Simple example showing how to volume sculpt an STL model with gyroid
 *
 * @author Alan Hudson
 */
public class Gyroidize {
    public static final void main(String[] args) throws IOException {
        int maxAttribute = 63;
        double voxelSize = 0.1 * MM;

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

        GridMaker gm = new GridMaker();
        gm.setBounds(bounds);

        // Modify the voxel space by intersecting the STL file with a Gyroid
        DataSourceGrid model = new DataSourceGrid(grid, bounds, maxAttribute);
        Intersection intersection = new Intersection();
        intersection.add(model);

        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(10*MM, 1*MM);
        intersection.add(gyroid);

        gm.setSource(intersection);
        gm.setMaxAttributeValue(maxAttribute);
        gm.setVoxelSize(voxelSize * Math.sqrt(3)/2);

        AttributeGrid dest = new ArrayAttributeGridByte(nx,ny,nz, voxelSize, voxelSize);
        System.out.println("nx: " + nx + " ny: " + ny + " nz: " + nz + " bounds:" + java.util.Arrays.toString(bounds));
        dest.setGridBounds(bounds);
        gm.makeGrid(dest);

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
        meshmaker.makeMesh(dest, stlw);
        stlw.close();
    }
}
