/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package imagepopper;

// External Imports

import abfab3d.creator.KernelResults;
import abfab3d.creator.Parameter;
import abfab3d.creator.shapeways.HostedKernel;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.Grid;
import abfab3d.grid.op.DataSources;
import abfab3d.grid.op.GridMaker;
import abfab3d.io.output.BoxesX3DExporter;
import abfab3d.io.output.SAVExporter;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import app.common.GridSaver;
import app.common.RegionPrunner;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.sav.BinaryContentHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static abfab3d.util.Output.printf;

//import java.awt.*;

/**
 * Geometry Kernel for the ImageEditor.
 * <p/>
 * Some images don't seem to work right, saving them with paint "fixes" this.  Not sure why.
 * And example of this is the cat.png image.
 *
 * @author Alan Hudson
 */
public class ImagePopperKernel extends HostedKernel {
    /**
     * Debugging level.  0-5.  0 is none
     */
    private static final int DEBUG_LEVEL = 0;

    /**
     * The horizontal and vertical resolution
     */
    private double resolution;
    private int smoothSteps;
    private double maxDecimationError;

    /**
     * How many regions to keep
     */
    private RegionPrunner.Regions regions;

    /**
     * The width of the body geometry
     */
    private double bodyWidth1;
    private double bodyWidth2;

    /**
     * The height of the body geometry
     */
    private double bodyHeight1;
    private double bodyHeight2;

    /**
     * The depth of the body geometry
     */
    private double bodyDepth1;
    private double bodyDepth2;

    /**
     * The image filename
     */
    private String filename;
    private String filename2;

    private String material;

    private String[] availableMaterials = new String[]{"White Strong & Flexible", "White Strong & Flexible Polished",
            "Silver", "Silver Glossy", "Stainless Steel", "Gold Plated Matte", "Gold Plated Glossy", "Antique Bronze Matte",
            "Antique Bronze Glossy", "Alumide", "Polished Alumide"};

    /**
     * Get the parameters for this editor.
     *
     * @return The parameters.
     */
    public Map<String, Parameter> getParams() {
        HashMap<String, Parameter> params = new HashMap<String, Parameter>();

        int seq = 0;
        int step = 0;

        params.put("bodyImage", new Parameter("bodyImage", "Image Layer 1", "The image to use for the front body", "images/leaf/5_04_combined.jpg", 1,
                Parameter.DataType.STRING, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );
        params.put("bodyImage2", new Parameter("bodyImage2", "Image Layer 2", "The image to use for the front body", "NONE", 1,
                Parameter.DataType.STRING, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );

        params.put("bodyDepth1", new Parameter("bodyDepth1", "Depth Amount - Layer 1", "The depth of the image", "0.0013", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        params.put("bodyDepth2", new Parameter("bodyDepth2", "Depth Amount - Layer 2", "The depth of the image", "0.0008", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        step++;
        seq = 0;
/*
        params.put("resolution", new Parameter("resolution", "Resolution", "How accurate to model the object", "0.00006", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, true, 0, 0.1, null, null)
        );
*/
        params.put("bodyWidth1", new Parameter("bodyWidth1", "Body Width", "The width of layer 1", "0.055330948", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0.01, 1, null, null)
        );

        params.put("bodyHeight1", new Parameter("bodyHeight1", "Body Height", "The height of layer 1", "0.04", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0.01, 1, null, null)
        );

        params.put("bodyWidth2", new Parameter("bodyWidth2", "Body Width", "The width of layer 2", "0.055330948", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0.01, 1, null, null)
        );

        params.put("bodyHeight2", new Parameter("bodyHeight2", "Body Height", "The height of layer 2", "0.04", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0.01, 1, null, null)
        );

        step++;
        seq = 0;

        params.put("material", new Parameter("material", "Material", "What material to design for", "Silver Glossy", 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, availableMaterials)
        );

        step++;
        seq = 0;

        // Advanced Params
        params.put("resolution", new Parameter("resolution", "Resolution", "How accurate to model the object", "0.00006", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 0.1, null, null)
        );

        params.put("maxDecimationError", new Parameter("maxDecimationError", "MaxDecimationError", "Maximum error during decimation", "1e-9", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 1, null, null)
        );

        params.put("regions", new Parameter("regions", "Regions", "How many regions to keep", RegionPrunner.Regions.ALL.toString(), 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, enumToStringArray(RegionPrunner.Regions.values()))
        );

        params.put("smoothSteps", new Parameter("smoothSteps", "Smooth Steps", "How smooth to make the object", "3", 1,
                Parameter.DataType.INTEGER, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 100, null, null)
        );

        return params;
    }

    /**
     * @param params  The parameters
     * @param acc     The accuracy to generate the model
     * @param handler The X3D content handler to use
     */
    public KernelResults generate(Map<String, Object> params, Accuracy acc, BinaryContentHandler handler) throws IOException {

        long start = System.currentTimeMillis();

        pullParams(params);

        Grid grid = null;

        if (acc == Accuracy.VISUAL) {
            // TODO: not sure I like this, could have two params:  visualResolution, printResolution
            resolution = resolution * 1.5;
        }
        double image1Width = bodyWidth1;
        double image1Height = bodyHeight1;
        double image1Depth = bodyDepth1;
        double voxelSize = resolution;

        double margin = 1 * voxelSize;

        double gridWidth = image1Width + 2 * margin;
        double gridHeight = image1Height + 2 * margin;
        double gridDepth = 4 * image1Depth + 2 * margin;

        double bounds[] = new double[]{-gridWidth / 2, gridWidth / 2, -gridHeight / 2, gridHeight / 2, -gridDepth / 2, gridDepth / 2};
        int nx = (int) ((bounds[1] - bounds[0]) / voxelSize);
        int ny = (int) ((bounds[3] - bounds[2]) / voxelSize);
        int nz = (int) ((bounds[5] - bounds[4]) / voxelSize);
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.Union union = new DataSources.Union();

        DataSources.ImageBitmap layer1 = new DataSources.ImageBitmap();
        layer1.setSize(image1Width, image1Height, image1Depth);
        layer1.setLocation(0, 0, 0);
        layer1.setBaseThickness(0.0);
        layer1.setImageType(DataSources.ImageBitmap.IMAGE_POSITIVE);
        layer1.setTiles(1, 1);
        layer1.setImagePath(filename);
        union.addDataSource(layer1);

        if (!filename2.equalsIgnoreCase("NONE")) {
            DataSources.ImageBitmap layer2 = new DataSources.ImageBitmap();
            layer2.setSize(image1Width, image1Height, image1Depth);
            layer2.setLocation(0, 0, image1Depth + voxelSize);
            layer2.setBaseThickness(0.0);
            layer2.setImageType(DataSources.ImageBitmap.IMAGE_POSITIVE);
            layer2.setTiles(1, 1);
            layer2.setImagePath(filename2);
            union.addDataSource(layer2);
        }


        GridMaker gm = new GridMaker();

        gm.setBounds(bounds);
        gm.setDataSource(union);

        grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);
        printf("gm.makeGrid() done\n");

        if (regions != RegionPrunner.Regions.ALL) {
            RegionPrunner.reduceToOneRegion(grid);
        }

        System.out.println("Writing grid");

        HashMap<String, Object> exp_params = new HashMap<String, Object>();
        exp_params.put(SAVExporter.EXPORT_NORMALS, false);   // Required now for ITS?
        if (acc == Accuracy.VISUAL) {
            // X3DOM requires IFS for normal generation
            params.put(SAVExporter.GEOMETRY_TYPE, SAVExporter.GeometryType.INDEXEDFACESET);
        } else {
            params.put(SAVExporter.GEOMETRY_TYPE, SAVExporter.GeometryType.INDEXEDTRIANGLESET);
        }

        double[] min_bounds = new double[3];
        double[] max_bounds = new double[3];
        grid.getWorldCoords(0, 0, 0, min_bounds);
        grid.getWorldCoords(grid.getWidth() - 1, grid.getHeight() - 1, grid.getDepth() - 1, max_bounds);

        WingedEdgeTriangleMesh mesh = GridSaver.createIsosurface(grid, smoothSteps);
        int gw = grid.getWidth();
        int gh = grid.getHeight();
        int gd = grid.getDepth();
        double sh = grid.getSliceHeight();
        double vs = grid.getVoxelSize();

        // Release grid to lower total memory requirements
        grid = null;
        System.gc();

        GridSaver.writeIsosurfaceMaker(mesh, gw,gh,gd,vs,sh,handler,params,maxDecimationError, true);



        System.out.println("Total Time: " + (System.currentTimeMillis() - start));
        System.out.println("-------------------------------------------------");
        return new KernelResults(true, min_bounds, max_bounds);
    }

    /**
     * Pull the params into local variables
     *
     * @param params The parameters
     */
    private void pullParams(Map<String, Object> params) {
        String pname = null;

        try {
            pname = "resolution";
            resolution = ((Double) params.get(pname)).doubleValue();

            pname = "maxDecimationError";
            maxDecimationError = ((Double) params.get(pname)).doubleValue();

            pname = "bodyWidth1";
            bodyWidth1 = ((Double) params.get(pname)).doubleValue();

            pname = "bodyHeight1";
            bodyHeight1 = ((Double) params.get(pname)).doubleValue();

            pname = "bodyWidth2";
            bodyWidth2 = ((Double) params.get(pname)).doubleValue();

            pname = "bodyHeight2";
            bodyHeight2 = ((Double) params.get(pname)).doubleValue();

            pname = "bodyImage";
            filename = (String) params.get(pname);

            pname = "bodyImage2";
            filename2 = (String) params.get(pname);

            pname = "bodyDepth1";
            bodyDepth1 = ((Double) params.get(pname)).doubleValue();

            pname = "bodyDepth2";
            bodyDepth2 = ((Double) params.get(pname)).doubleValue();

            pname = "smoothSteps";
            smoothSteps = ((Integer) params.get(pname)).intValue();

            pname = "regions";
            regions = RegionPrunner.Regions.valueOf((String) params.get(pname));


        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error parsing: " + pname + " val: " + params.get(pname));
        }
    }

    /**
     * return bounds extended by given margin
     */
    static double[] extendBounds(double bounds[], double margin) {
        return new double[]{
                bounds[0] - margin,
                bounds[1] + margin,
                bounds[2] - margin,
                bounds[3] + margin,
                bounds[4] - margin,
                bounds[5] + margin,
        };
    }

    private void writeDebug(Grid grid, BinaryContentHandler handler, ErrorReporter console) {
        // Output File

        BoxesX3DExporter exporter = new BoxesX3DExporter(handler, console, true);

        HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
        colors.put(new Integer(Grid.INTERIOR), new float[]{1, 0, 0});
        colors.put(new Integer(Grid.EXTERIOR), new float[]{0, 1, 0});
        colors.put(new Integer(Grid.OUTSIDE), new float[]{0, 0, 1});

        HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
        transparency.put(new Integer(Grid.INTERIOR), new Float(0));
        transparency.put(new Integer(Grid.EXTERIOR), new Float(0.5));
        transparency.put(new Integer(Grid.OUTSIDE), new Float(0.98));

        exporter.writeDebug(grid, colors, transparency);
        exporter.close();
    }

    public static <T extends Enum<T>> String[] enumToStringArray(T[] values) {
        int i = 0;
        String[] result = new String[values.length];
        for (T value : values) {
            result[i++] = value.name();
        }
        return result;
    }
}
