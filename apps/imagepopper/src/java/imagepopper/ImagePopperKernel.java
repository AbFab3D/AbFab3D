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

import abfab3d.creator.GeometryKernel;
import abfab3d.creator.KernelResults;
import abfab3d.creator.Parameter;
import abfab3d.creator.shapeways.HostedKernel;
import abfab3d.creator.util.ParameterUtil;
import abfab3d.grid.*;

import abfab3d.datasources.ImageBitmap;
import abfab3d.datasources.Union;


import abfab3d.grid.op.GridMaker;
import abfab3d.io.output.BoxesX3DExporter;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.SAVExporter;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.AreaCalculator;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import app.common.GridSaver;
import app.common.RegionPrunner;
import app.common.ShellResults;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DXMLRetainedExporter;
import org.web3d.vrml.sav.BinaryContentHandler;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static java.lang.System.currentTimeMillis;

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
    public enum ImagePlace {TOP, BOTTOM, BOTH};

    private static final boolean DEBUG = true;
    private static final boolean USE_MIP_MAPPING = false;
    private final boolean USE_MESH_MAKER_MT = true;
    private static final boolean USE_FAST_MATH = true;

    /**
     * Debugging level.  0-5.  0 is none
     */
    private static final int DEBUG_LEVEL = 0;

    // High = print resolution.  Medium = print * 1.5, LOW = print * 2
    enum PreviewQuality {LOW(2.0), MEDIUM(1.5), HIGH(1.0);

        private double factor;

        PreviewQuality(double f) {
            factor = f;
        }

        public double getFactor() {
            return factor;
        }
    };

    /**
     * The horizontal and vertical resolution
     */
    private double resolution;
    private PreviewQuality previewQuality;
    private int smoothSteps;
    private double smoothingWidth;
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
    private double bodyWidth3;

    /**
     * The height of the body geometry
     */
    private double bodyHeight1;
    private double bodyHeight2;
    private double bodyHeight3;

    /**
     * The depth of the body geometry
     */
    private double bodyDepth1;
    private double bodyDepth2;
    private double bodyDepth3;

    /**
     * Where to start placing the image, determines direction of grayscale height
     */
    private ImagePlace bodyImagePlacement1;
    private ImagePlace bodyImagePlacement2;
    private ImagePlace bodyImagePlacement3;

    /**
     * The image filename
     */
    private String filename1;
    private String filename2;
    private String filename3;

    private String material;
    private boolean useGrayscale1;
    private boolean useGrayscale2;
    private boolean useGrayscale3;

    private boolean imageInvert1 = false;
    private boolean imageInvert2 = false;
    private boolean imageInvert3 = false;

    private boolean visRemovedRegions;
    private int threads;


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

        params.put("bodyImage1", new Parameter("bodyImage1", "Image Layer 1", "The image to use for layer1", "images/leaf/5_cleaned.png", 1,
                Parameter.DataType.URI, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );
        params.put("bodyImage2", new Parameter("bodyImage2", "Image Layer 2", "The image to use for layer2", "NONE", 1,
                Parameter.DataType.URI, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );
        params.put("bodyImage3", new Parameter("bodyImage3", "Image Layer 3", "The image to use for layer3", "NONE", 1,
                Parameter.DataType.URI, Parameter.EditorType.FILE_DIALOG,
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

        params.put("bodyDepth3", new Parameter("bodyDepth3", "Depth Amount - Layer 3", "The depth of the image", "0.0008", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        params.put("bodyImagePlacement1", new Parameter("bodyImagePlacement1", "Body Image Placement - Layer 1", "Where to start the image", ImagePlace.TOP.toString(), 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, enumToStringArray(ImagePlace.values()))
        );

        params.put("bodyImagePlacement2", new Parameter("bodyImagePlacement2", "Body Image Placement - Layer 2", "Where to start the image", ImagePlace.TOP.toString(), 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, enumToStringArray(ImagePlace.values()))
        );

        params.put("bodyImagePlacement3", new Parameter("bodyImagePlacement3", "Body Image Placement - Layer 3", "Where to start the image", ImagePlace.TOP.toString(), 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, enumToStringArray(ImagePlace.values()))
        );

        params.put("useGrayscale1", new Parameter("useGrayscale1", "Use Grayscale1", "Should we use grayscale", "false", 1,
                Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 100, null, null)
        );
        params.put("useGrayscale2", new Parameter("useGrayscale2", "Use Grayscale2", "Should we use grayscale", "false", 1,
                Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 100, null, null)
        );
        params.put("useGrayscale3", new Parameter("useGrayscale3", "Use Grayscale3", "Should we use grayscale", "false", 1,
                Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 100, null, null)
        );

        params.put("imageInvert1", new Parameter("imageInvert1", "ImageInvert1", "Invert the images", "false", 1,
                Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 0.1, null, null)
        );
        params.put("imageInvert2", new Parameter("imageInvert2", "ImageInvert2", "Invert the images", "false", 1,
                Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 0.1, null, null)
        );
        params.put("imageInvert3", new Parameter("imageInvert3", "ImageInvert3", "Invert the images", "false", 1,
                Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 0.1, null, null)
        );

        step++;
        seq = 0;

        params.put("bodyWidth1", new Parameter("bodyWidth1", "Body Width1", "The width of layer 1", "0.055330948", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0.002, 1, null, null)
        );

        params.put("bodyHeight1", new Parameter("bodyHeight1", "Body Height1", "The height of layer 1", "0.04", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0.002, 1, null, null)
        );

        params.put("bodyWidth2", new Parameter("bodyWidth2", "Body Width2", "The width of layer 2", "0.055330948", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0.01, 1, null, null)
        );

        params.put("bodyHeight2", new Parameter("bodyHeight2", "Body Height2", "The height of layer 2", "0.04", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0.01, 1, null, null)
        );

        params.put("bodyWidth3", new Parameter("bodyWidth3", "Body Width3", "The width of layer 3", "0.055330948", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0.01, 1, null, null)
        );

        params.put("bodyHeight3", new Parameter("bodyHeight3", "Body Height3", "The height of layer 3", "0.04", 1,
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
        params.put("resolution", new Parameter("resolution", "Resolution", "How accurate to model the object", "0.0001", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 0.1, null, null)
        );

        params.put("threads", new Parameter("threads", "Threads", "Threads to use for operations", "0", 1,
                Parameter.DataType.INTEGER, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 50, null, null)
        );

        params.put("previewQuality", new Parameter("previewQuality", "PreviewQuality", "How rough is the preview", PreviewQuality.HIGH.toString(), 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, enumToStringArray(PreviewQuality.values()))
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
                step, seq++, false, 0, 100, null, null)
        );

        params.put("smoothingWidth", new Parameter("smoothingWidth", "Smoothing Width", "How many voxles to smooth", "0.", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0., 5., null, null)
        );


        params.put("visRemovedRegions", new Parameter("visRemovedRegions", "Vis Removed Regions", "Visualize removed regions", "false", 1,
                Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 100, null, null)
        );


        return params;
    }

    /**
     * @param params  The parameters
     * @param acc     The accuracy to generate the model
     * @param handler The X3D content handler to use
     */
    public KernelResults generate(Map<String, Object> params, Accuracy acc, BinaryContentHandler handler) throws IOException {
        if (USE_FAST_MATH) {
            System.setProperty("jodk.fastmath.usejdk", "false");
        } else {
            System.setProperty("jodk.fastmath.usejdk", "true");
        }

        long start = currentTimeMillis();

        pullParams(params);

        Grid grid = null;

        if (acc == Accuracy.VISUAL) {
            resolution = resolution * previewQuality.getFactor();
        }

        if (maxDecimationError > 0) {
            maxDecimationError = 0.1*resolution*resolution;
        }

        double voxelSize = resolution;
        double margin = 5 * voxelSize;

        double gridWidth = Math.max(Math.max(bodyWidth1,bodyWidth2),bodyWidth3) + 2 * margin;
        double gridHeight = Math.max(Math.max(bodyHeight1,bodyHeight2),bodyHeight3) + 2 * margin;
        double bodyDepth = bodyDepth1;
        double layer1z = bodyDepth1/2; // z-center
        double layer2z = 0;            // z-center of middle layer
        double layer3z = 0;            // z-center of bottom layer

        // HARD CODED params to play with
        // width of Gaussian smoothing of grid, may be 0. - no smoothing
        //double smoothingWidth = 0.0;
        // size of grid block for MT calculatins
        // (larger values reduce processor cache performance)
        int blockSize = 50;
        // max number to use for surface transitions. Should be ODD number
        // set it to 1 to have binary grid
        int maxGridAttributeValue = 63;
        // width of surface transition area relative to voxel size
        // optimal value sqrt(3)/2. Larger value causes rounding of sharp edges
        // sreyt it to 0. to make no surface transitions
        double surfaceTransitionWidth = Math.sqrt(3)/2; // 0.866
        double imagesBlurWidth = surfaceTransitionWidth*voxelSize;
        double baseThreshold = 0.1;
        int interpolationType = ImageBitmap.INTERPOLATION_BOX;


        if (!filename2.equalsIgnoreCase("NONE")) {
            bodyDepth += bodyDepth2;
            layer1z += bodyDepth2;
            layer2z = bodyDepth2/2;
        }
        if (!filename3.equalsIgnoreCase("NONE")) {
            bodyDepth += bodyDepth3;
            layer1z += bodyDepth3;
            layer2z += bodyDepth3;
            layer3z = bodyDepth3/2;
        }

        double bounds[] = new double[]{-gridWidth / 2, gridWidth / 2, -gridHeight / 2, gridHeight / 2, -margin, bodyDepth + margin};
        int nx = (int) ((bounds[1] - bounds[0]) / voxelSize);
        int ny = (int) ((bounds[3] - bounds[2]) / voxelSize);
        int nz = (int) ((bounds[5] - bounds[4]) / voxelSize);
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);
        printf("bodyDepth: %f mm\n", bodyDepth*1000);

        Union union = new Union();

        ImageBitmap layer1 = new ImageBitmap();
        layer1.setSize(bodyWidth1, bodyHeight1, bodyDepth1);
        layer1.setLocation(0, 0, layer1z);
        layer1.setBaseThickness(0.0);
        layer1.setImageType(ImageBitmap.IMAGE_TYPE_EMBOSSED);
        layer1.setTiles(1, 1);
        layer1.setImagePath(filename1);
        layer1.setUseGrayscale(useGrayscale1);
        layer1.setBlurWidth((useGrayscale1)? 0.: imagesBlurWidth);
        layer1.setVoxelSize(resolution);


        layer1.setImagePlace(getPlacementValue(bodyImagePlacement1));
        if (imageInvert1) {
            layer1.setImageType(ImageBitmap.IMAGE_TYPE_ENGRAVED);
        }
        layer1.setBaseThreshold(baseThreshold);
        layer1.setInterpolationType(interpolationType);

        union.addDataSource(layer1);

        if (!filename2.equalsIgnoreCase("NONE")) {
            ImageBitmap layer2 = new ImageBitmap();
            layer2.setSize(bodyWidth2, bodyHeight2, bodyDepth2);

            layer2.setLocation(0, 0, layer2z);
            layer2.setBaseThickness(0.0);
            layer2.setImageType(ImageBitmap.IMAGE_TYPE_EMBOSSED);
            layer2.setTiles(1, 1);
            layer2.setImagePath(filename2);
            layer2.setUseGrayscale(useGrayscale2);
            layer2.setBlurWidth((useGrayscale2)? 0: imagesBlurWidth);
            layer2.setImagePlace(getPlacementValue(bodyImagePlacement2));
            if (imageInvert2) {
                layer2.setImageType(ImageBitmap.IMAGE_TYPE_ENGRAVED);
            }

            layer2.setInterpolationType(interpolationType);
            layer2.setBaseThreshold(baseThreshold);
            layer2.setVoxelSize(resolution);

            union.addDataSource(layer2);

        }

        if (!filename3.equalsIgnoreCase("NONE")) {
            ImageBitmap layer3 = new ImageBitmap();
            layer3.setSize(bodyWidth3, bodyHeight3, bodyDepth3);

            layer3.setLocation(0, 0, layer3z);
            layer3.setBaseThickness(0.0);
            layer3.setImageType(ImageBitmap.IMAGE_TYPE_EMBOSSED);
            layer3.setTiles(1, 1);
            layer3.setImagePath(filename3);
            layer3.setUseGrayscale(useGrayscale3);
            layer3.setBlurWidth((useGrayscale3)? 0: imagesBlurWidth);

            layer3.setImagePlace(getPlacementValue(bodyImagePlacement3));
            if (imageInvert3) {
                layer3.setImageType(ImageBitmap.IMAGE_TYPE_ENGRAVED);
            }

            layer3.setInterpolationType(interpolationType);
            layer3.setBaseThreshold(baseThreshold);
            layer3.setVoxelSize(resolution);

            union.addDataSource(layer3);

        }


        GridMaker gm = new GridMaker();

        gm.setBounds(bounds);
        gm.setDataSource(union);
        gm.setThreadCount(threads);

        gm.setMaxAttributeValue(maxGridAttributeValue);
        gm.setVoxelSize(voxelSize*surfaceTransitionWidth);

        // TODO: Change to use BlockBased for some size
        //grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        grid = new GridShortIntervals(nx, ny, nz, resolution, resolution);
        grid.setGridBounds(bounds);

        long t0 = time();
        printf("GridMaker.execute()\n");
        gm.execute(grid);
        printf("GridMaker.execute() done %d ms\n", (time() - t0));


        int min_volume = 10;
        int regions_removed = 0;
/*
        if (regions != RegionPrunner.Regions.ALL) {
            long t0 = currentTimeMillis();
            if (visRemovedRegions) {
                regions_removed = RegionPrunner.reduceToOneRegion(grid, handler, bounds, min_volume);
            } else {
                regions_removed = RegionPrunner.reduceToOneRegion(grid, min_volume);
            }
            printf("Regions removed: %d\n", regions_removed);
            printf("regions removal done %d ms\n", (currentTimeMillis() - t0));

        }
 */
        System.out.println("Writing grid");

        HashMap<String, Object> exp_params = new HashMap<String, Object>();
        exp_params.put(SAVExporter.EXPORT_NORMALS, false);   // Required now for ITS?
        if (acc == Accuracy.VISUAL) {
            params.put(SAVExporter.GEOMETRY_TYPE, SAVExporter.GeometryType.INDEXEDTRIANGLESET);
            params.put(SAVExporter.VERTEX_NORMALS, true);
        } else {
            params.put(SAVExporter.GEOMETRY_TYPE, SAVExporter.GeometryType.INDEXEDTRIANGLESET);
        }


        WingedEdgeTriangleMesh mesh;

        double gbounds[] = new double[6];
        grid.getGridBounds(gbounds);

        // place of default viewpoint
        double viewDistance = GridSaver.getViewDistance(grid);

        if(USE_MESH_MAKER_MT){

            MeshMakerMT meshmaker = new MeshMakerMT();

            t0 = time();
            meshmaker.setBlockSize(blockSize);
            meshmaker.setThreadCount(threads);
            meshmaker.setSmoothingWidth(smoothingWidth);
            meshmaker.setMaxDecimationError(maxDecimationError);
            meshmaker.setMaxAttributeValue(maxGridAttributeValue);

            // TODO: Need to get a better way to estimate this number
            IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
            meshmaker.makeMesh(grid, its);
            mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

            printf("MeshMakerMT.makeMesh(): %d ms\n", (time()-t0));

            // extra decimation to get rid of seams
            if(maxDecimationError > 0){
                t0 = time();
                mesh = GridSaver.decimateMesh(mesh, maxDecimationError);
                printf("final decimation: %d ms\n", (time()-t0));
            }

        } else {

            mesh = GridSaver.createIsosurface(grid, smoothSteps);
            // Release grid to lower total memory requirements
            if(maxDecimationError > 0)
                mesh = GridSaver.decimateMesh(mesh, maxDecimationError);
        }

        // Release grid to save memory
        grid = null;

        if(regions != RegionPrunner.Regions.ALL) {
            t0 = time();
            ShellResults sr = GridSaver.getLargestShell(mesh, min_volume);
            mesh = sr.getLargestShell();
            regions_removed = sr.getShellsRemoved();
            printf("GridSaver.getLargestShell(): %d ms\n", (time()-t0));
        }

        GridSaver.writeMesh(mesh, viewDistance, handler, params, true);

        AreaCalculator ac = new AreaCalculator();
        mesh.getTriangles(ac);
        double volume = ac.getVolume();
        double surface_area = ac.getArea();

        // Do not shorten the accuracy of these prints they need to be high
        printf("final surface area: %12.8f cm^2\n", surface_area * 1.e4);
        printf("final volume: %12.8f cm^3\n", volume * 1.e6);

        printf("Total time: %d ms\n", (time() - start));
        printf("-------------------------------------------------\n");

        double min_bounds[] = new double[]{gbounds[0],gbounds[2],gbounds[4]};
        double max_bounds[] = new double[]{gbounds[1],gbounds[3],gbounds[5]};
        return new KernelResults(true, min_bounds, max_bounds, volume, surface_area, regions_removed);
    }

    /**
     * Pull the params into local variables
     *
     * @param params The parameters
     */
    private void pullParams(Map<String, Object> params) {
        String pname = null;

        if (DEBUG) {
            System.out.println("ImagePopperKernel Params: " + params);
        }
        try {
            pname = "resolution";
            resolution = ((Double) params.get(pname)).doubleValue();

            pname = "previewQuality";
            previewQuality = PreviewQuality.valueOf((String) params.get(pname));

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

            pname = "bodyWidth3";
            bodyWidth3 = ((Double) params.get(pname)).doubleValue();

            pname = "bodyHeight3";
            bodyHeight3 = ((Double) params.get(pname)).doubleValue();

            pname = "bodyImage1";
            filename1 = (String) params.get(pname);

            pname = "bodyImage2";
            filename2 = (String) params.get(pname);

            pname = "bodyImage3";
            filename3 = (String) params.get(pname);

            pname = "bodyDepth1";
            bodyDepth1 = ((Double) params.get(pname)).doubleValue();

            pname = "bodyDepth2";
            bodyDepth2 = ((Double) params.get(pname)).doubleValue();

            pname = "bodyDepth3";
            bodyDepth3 = ((Double) params.get(pname)).doubleValue();

            pname = "bodyImagePlacement1";
            bodyImagePlacement1 = ImagePlace.valueOf((String) params.get(pname));

            pname = "bodyImagePlacement2";
            bodyImagePlacement2 = ImagePlace.valueOf((String) params.get(pname));

            pname = "bodyImagePlacement3";
            bodyImagePlacement3 = ImagePlace.valueOf((String) params.get(pname));

            pname = "smoothSteps";
            smoothSteps = ((Integer) params.get(pname)).intValue();

            pname = "smoothingWidth";
            smoothingWidth = ((Double) params.get(pname)).doubleValue();

            pname = "regions";
            regions = RegionPrunner.Regions.valueOf((String) params.get(pname));

            pname = "useGrayscale1";
            useGrayscale1 = (Boolean) params.get(pname);

            pname = "useGrayscale2";
            useGrayscale2 = (Boolean) params.get(pname);

            pname = "useGrayscale3";
            useGrayscale3 = (Boolean) params.get(pname);

            pname = "visRemovedRegions";
            visRemovedRegions = (Boolean) params.get(pname);

            pname = "imageInvert1";
            imageInvert1 = (Boolean) params.get(pname);

            pname = "imageInvert2";
            imageInvert2 = (Boolean) params.get(pname);

            pname = "imageInvert3";
            imageInvert3 = (Boolean) params.get(pname);

            pname = "threads";
            threads = ((Integer) params.get(pname)).intValue();

            if (threads == 0) {
                int cores = Runtime.getRuntime().availableProcessors();

                threads = cores;

                // scales well to 4 threads, stop there.
                if (threads > 4) {
                    threads = 4;
                }

                System.out.println("Number of cores:" + threads);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error parsing: " + pname + " val: " + params.get(pname));
        }
    }

    private int getPlacementValue(ImagePlace place) {
        switch(place) {
        case TOP: return ImageBitmap.IMAGE_PLACE_TOP;
        case BOTTOM: return ImageBitmap.IMAGE_PLACE_BOTTOM;
        case BOTH: return ImageBitmap.IMAGE_PLACE_BOTH;
        default :
            System.out.println("Unhandled place: " + place);
            new Exception().printStackTrace();
            return ImageBitmap.IMAGE_PLACE_TOP;
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
        colors.put(new Integer(Grid.INSIDE), new float[]{1, 0, 0});
        colors.put(new Integer(Grid.OUTSIDE), new float[]{0, 0, 1});

        HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
        transparency.put(new Integer(Grid.INSIDE), new Float(0));
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

    public static void main(String[] args) {
        int loops = 3;

        for(int n=0; n < loops; n++) {
        int threads = 1;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        long stime = System.currentTimeMillis();

        for(int i=0; i < threads; i++) {
            HostedKernel kernel = new ImagePopperKernel();
          Runnable runner = new KernelRunner(kernel);
//            Runnable runner = new FakeRunner(kernel);
            executor.submit(runner);
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Total Runtime: " + (System.currentTimeMillis() - stime));
        }
    }
}

class KernelRunner implements Runnable {
    private HostedKernel kernel;

    public KernelRunner(HostedKernel kernel) {
        this.kernel = kernel;
    }

    @Override
    public void run() {
        HashMap<String,String> params = new HashMap<String,String>();

        // params for garbage gen, originally 100 million objects
        params.put("bodyWidth1","0.1016");
        params.put("bodyHeight1","0.1016");
        params.put("bodyDepth1","0.012");

        params.put("regions","ALL");
        params.put("previewQuality","LOW");
        params.put("bodyImage","C:\\cygwin\\home\\giles\\projs\\abfab3d\\code\\trunk\\apps\\ringpopper\\images\\Tile_dilate8_unedged.png");
/*
        // params for regions test
        params.put("bodyWidth1","0.0216");
        params.put("bodyHeight1","0.0216");
        params.put("bodyDepth1","0.012");

        params.put("regions","ONE");
        params.put("previewQuality","LOW");
        params.put("visRemovedRegions","true");
        params.put("bodyImage","C:\\cygwin\\home\\giles\\projs\\abfab3d\\code\\trunk\\apps\\imagepopper\\images\\leaf\\5_cleaned.png");
*/
        Map<String,Object> parsed_params = ParameterUtil.parseParams(kernel.getParams(), params);

        try {
            FileOutputStream fos = new FileOutputStream("/tmp/thread" + Thread.currentThread().getName() + ".x3d");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            PlainTextErrorReporter console = new PlainTextErrorReporter();

            BinaryContentHandler writer = new X3DXMLRetainedExporter(bos,3,2,console);
            writer.startDocument("","","utf8","#X3D", "V3.2", "");
            writer.profileDecl("Immersive");


            kernel.generate(parsed_params, GeometryKernel.Accuracy.VISUAL, writer);

            writer.endDocument();
            bos.close();
            fos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}

class FakeRunner implements Runnable {
    private HostedKernel kernel;

    static class EdgeArray {

        Random m_rnd = new Random(101);

        public int nextInt() {
            return m_rnd.nextInt(10000);
        }
    }

    public FakeRunner(HostedKernel kernel) {
        this.kernel = kernel;
    }

    @Override
    public void run() {
        System.out.println("Running Fake");
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("bodyWidth1","0.1016");
        params.put("bodyHeight1","0.1016");
        params.put("bodyDepth1","0.03");
        params.put("regions","ALL");
        params.put("previewQuality","LOW");
        params.put("bodyImage1","C:\\cygwin\\home\\giles\\projs\\abfab3d\\code\\trunk\\apps\\ringpopper\\images\\Tile_dilate8_unedged.png");

        Map<String,Object> parsed_params = ParameterUtil.parseParams(kernel.getParams(), params);

        try {
            FileOutputStream fos = new FileOutputStream("/tmp/thread" + Thread.currentThread().getName());
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            PlainTextErrorReporter console = new PlainTextErrorReporter();

            BinaryContentHandler writer = new X3DXMLRetainedExporter(bos,3,2,console);
            writer.startDocument("","","utf8","#X3D", "V3.2", "");
            writer.profileDecl("Immersive");


            //kernel.generate(parsed_params, GeometryKernel.Accuracy.VISUAL, writer);
            long TIMES = (long) 2e7;
            long tot = 0;

            EdgeArray m_edgeArray = new EdgeArray();

            System.out.println("Times: " + TIMES);
            for(int i=0; i < TIMES; i++) {
                tot += Math.ceil(Math.sqrt(i) * Math.sin(i) + Math.cos(i));
                tot -= Math.ceil(Math.sqrt(i) * Math.sin(i) + Math.cos(i) + Math.asin(i));

                tot += m_edgeArray.nextInt();
            }

            System.out.println("Total Count: " + tot);
            fos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}


