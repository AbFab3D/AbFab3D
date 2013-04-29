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

package volumesculptor;

// External Imports

import abfab3d.creator.GeometryKernel;
import abfab3d.creator.KernelResults;
import abfab3d.creator.Parameter;
import abfab3d.creator.shapeways.HostedKernel;
import abfab3d.creator.util.ParameterUtil;

import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.GridShortIntervals;
import abfab3d.grid.Grid;

import abfab3d.grid.op.DataSources;
import abfab3d.grid.op.DataSourceImageBitmap;
import abfab3d.grid.op.GridMaker;
import abfab3d.grid.op.VecTransforms;

import abfab3d.io.output.BoxesX3DExporter;
import abfab3d.io.output.SAVExporter;
import abfab3d.io.output.MeshMakerMT;

import abfab3d.mesh.AreaCalculator;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import abfab3d.mesh.LaplasianSmooth;
import abfab3d.mesh.IndexedTriangleSetBuilder;

import abfab3d.util.DataSource;
import abfab3d.util.TextUtil;

import app.common.GridSaver;
import app.common.RegionPrunner;
import app.common.ShellResults;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DXMLRetainedExporter;
import org.web3d.vrml.sav.BinaryContentHandler;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static abfab3d.util.MathUtil.TORAD;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.MathUtil.extendBounds;

/**
 * Geometry Kernel for the VolumeSculptor
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov
 */
public class VolumeSculptorKernel extends HostedKernel {
    private static final boolean USE_MIP_MAPPING = false;
    private static final boolean USE_FAST_MATH = true;

    static final int 
        GRID_SHORT_INTERVALS=1,
        GRID_BYTE_ARRAY = 2,
        GRID_AUTO = -1;        

    /**
     * Debugging level.  0-5.  0 is none
     */
    private static final int DEBUG_LEVEL = 0;
    static final double MM = 0.001; // millmeters to meters


    // large enough font size to be used to render text 
    static final int DEFAULT_FONT_SIZE = 50;
    // point size unit 
    static final double POINT_SIZE = 25.4 * MM / 72;
    static final double TEXT_RENDERING_PIXEL_SIZE = 25.4 * MM / 600; // 600 dpi 
    int MINIMAL_TEXT_OFFSET = 10; // minimal border around engraved text 

    enum EdgeStyle {NONE, TOP, BOTTOM, BOTH}    
    ;

    // High = print resolution.  Medium = print * 1.5, LOW = print * 2
    enum PreviewQuality {
        LOW(2.0), MEDIUM(1.5), HIGH(1.0);

        private double factor;

        PreviewQuality(double f) {
            factor = f;
        }

        public double getFactor() {
            return factor;
        }
    }

    ;

    enum SymmetryStyle {
        NONE(-1),
        FRIEZE_II(0),   // oo oo
        FRIEZE_IX(1),   // oo X
        FRIEZE_IS(2),   // oo *
        FRIEZE_SII(3),  // * oo oo
        FRIEZE_22I(4),  // 2 2 oo
        FRIEZE_2SI(5),  // 2 * oo
        FRIEZE_S22I(6); // * 2 2 oo

        private int code;

        SymmetryStyle(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private String[] availableMaterials = new String[]{"White Strong & Flexible", "White Strong & Flexible Polished",
            "Silver", "Silver Glossy", "Stainless Steel", "Gold Plated Matte", "Gold Plated Glossy", "Antique Bronze Matte",
            "Antique Bronze Glossy", "Alumide", "Polished Alumide"};

    // Physical Sizing
    private double innerDiameter;
    private double ringThickness;
    private double ringWidth;
    private double topBorderWidth;
    private double bottomBorderWidth;
    //private double edgeWidth;

    /**
     * Percentage of the ringThickness to use as a base
     */
    private double baseThickness;

    /**
     * The horizontal and vertical resolution
     */
    private double resolution;
    private PreviewQuality previewQuality;
    private int smoothSteps;
    private double smoothingWidth;
    private double maxDecimationError;

    /**
     * The image filename
     */
    private String imagePath = null;
    private boolean imageInvert = false;
    private String crossSectionPath = null;

    private int tilingX;
    private int tilingY;
    private int threadCount;
    //private EdgeStyle edgeStyle;
    private SymmetryStyle symmetryStyle;

    private String material;
    private String text = "MADE IN THE FUTURE";
    private double textDepth;
    private String fontName = "Times New Roman";
    private boolean fontBold = false;
    private boolean fontItalic = false;
    private int fontSize = 20;

    private double imageBlurWidth = 0.;
    private int imageInterpolationType = DataSourceImageBitmap.INTERPOLATION_LINEAR;
    private double imageBaseThreshold = 0.1;
    private double bandLength; // length of ring perimeter

    /**
     * How many regions to keep
     */
    private RegionPrunner.Regions regions;
    private boolean useGrayscale;
    private boolean visRemovedRegions;

    /**
     * Get the parameters for this editor.
     *
     * @return The parameters.
     */
    public Map<String, Parameter> getParams() {
        HashMap<String, Parameter> params = new HashMap<String, Parameter>();

        int seq = 0;
        int step = 0;

        // Image based params
        params.put("image", new Parameter("image", "Image", "The image to use", "images/tile_01.png", 1,
                Parameter.DataType.URI, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );

        params.put("imageInvert", new Parameter("imageInvert", "ImageInvert", "Invert the image", "false", 1,
                Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 100, null, null)
        );

        params.put("useGrayscale", new Parameter("useGrayscale", "Use Grayscale", "Should we use grayscale", "false", 1,
                Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 100, null, null)
        );

        params.put("crossSection", new Parameter("crossSection", "Cross Section", "The image of cross section", "images/crosssection_01.png", 1,
                Parameter.DataType.URI, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );

        params.put("tilingX", new Parameter("tilingX", "Tiling X", "The tiling along left/right of the ring", "8", 1,
                Parameter.DataType.INTEGER, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 50, null, null)
        );

        params.put("tilingY", new Parameter("tilingY", "Tiling Y", "The tiling along up/down of the ring", "1", 1,
                Parameter.DataType.INTEGER, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 50, null, null)
        );

        params.put("symmetryStyle", new Parameter("symmetryStyle", "Symmetry Style", "Whether to put lines on the band", symmetryStyle.NONE.toString(), 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, enumToStringArray(symmetryStyle.values()))
        );

        //params.put("edgeStyle", new Parameter("edgeStyle", "Edge Style", "Whether to put lines on the band", edgeStyle.BOTH.toString(), 1,
        //        Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
        //        step, seq++, false, -1, 1, null, enumToStringArray(edgeStyle.values()))
        //);

        step++;
        seq = 0;

        // Size based params
        params.put("innerDiameter", new Parameter("innerDiameter", "Inner Diameter", "The inner diameter", "0.02118", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        params.put("ringWidth", new Parameter("ringWidth", "Ring Width", "The ring width(Finger Length)", "0.005", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        params.put("ringThickness", new Parameter("ringThickness", "Ring Thickness", "The thickness of the ring", "0.001", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        params.put("baseThickness", new Parameter("baseThickness", "Base Thickness", "The thickness percent of the ring base", "0", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );
        params.put("topBorderWidth", new Parameter("topBorderWidth", "Top Border Width", "The width of the top border", "0.001", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );
        params.put("bottomBorderWidth", new Parameter("bottomBorderWidth", "Bottom Border Width", "The width of the bottom border", "0.001", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        step++;
        seq = 0;

        params.put("text", new Parameter("text", "text", "Engraved Text", "MADE IN THE FUTURE", 1,
                Parameter.DataType.STRING, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, null)
        );

        params.put("textDepth", new Parameter("textDepth", "Text Depth", "The depth of the text engraving", "0.0003", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        params.put("fontName", new Parameter("fontName", "fontName", "Font Name", "Times New Roman", 1,
                Parameter.DataType.STRING, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, null)
        );

        params.put("fontBold", new Parameter("fontBold", "Font Bold", "Use Bold Font", "false", 1,
                Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, null));

        params.put("fontItalic", new Parameter("fontItalic", "Font Italic", "Use Italic Font", "false", 1,
                Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, null));

        params.put("fontSize", new Parameter("fontSize", "fontSize", "The font size", "12", 1,
                Parameter.DataType.INTEGER, Parameter.EditorType.DEFAULT,
                step, seq++, false, 3, 50, null, null));

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

        params.put("previewQuality", new Parameter("previewQuality", "PreviewQuality", "How rough is the preview", PreviewQuality.MEDIUM.toString(), 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, enumToStringArray(PreviewQuality.values()))
        );

        params.put("maxDecimationError", new Parameter("maxDecimationError", "MaxDecimationError", "Maximum error during decimation", "1e-10", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 1, null, null)
        );

        params.put("regions", new Parameter("regions", "Regions", "How many regions to keep", RegionPrunner.Regions.ALL.toString(), 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, enumToStringArray(RegionPrunner.Regions.values()))
        );

        // Deprecate this param
        params.put("smoothSteps", new Parameter("smoothSteps", "Smooth Steps", "How smooth to make the object", "5", 1,
                Parameter.DataType.INTEGER, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 100, null, null)
        );

        params.put("smoothingWidth", new Parameter("smoothingWidth", "Smoothing Width", "How many voxels to smooth", "0.5", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 100, null, null)
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

        long start = time();

        pullParams(params);

        if (acc == Accuracy.VISUAL) {
            resolution = resolution * previewQuality.getFactor();
        }
        
        /**
         * return bounds extended by given margin
         */

        double modelBonds[] = new double[]{-20*MM, 20*MM, -20*MM, 20*MM, -20*MM, 20*MM};

        double gridBounds[] = extendBounds(modelBonds, 0.2*MM);    

        double voxelSize = resolution;
        
        double cellSize = 5*MM;

        int nx = (int) ((gridBounds[1] - gridBounds[0]) / voxelSize);
        int ny = (int) ((gridBounds[3] - gridBounds[2]) / voxelSize);
        int nz = (int) ((gridBounds[5] - gridBounds[4]) / voxelSize);
        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);
        
        // HARD CODED params to play with 
        // width of Gaussian smoothing of grid, may be 0. - no smoothing 
        double gaussianSmooth = 1.0; 
        // size of grid block for MT calculatins 
        // (larger values reduce processor cache performance)
        int blockSize = 50;
        // max number to use for surface transitions. Should be ODD number 
        // set it to 0 to have binary grid 
        int maxGridAttributeValue = 63; // 63 is max value for BYTE_ARRAY grid 
        int gridType =  GRID_BYTE_ARRAY;
                
        // width of surface transition area relative to voxel size
        // optimal value sqrt(3)/2. Larger value causes rounding of sharp edges
        // set it to 0. to make no surface transitions
        double surfaceTransitionWidth = Math.sqrt(3)/2; // 0.866 

        imageInterpolationType = DataSourceImageBitmap.INTERPOLATION_LINEAR;

        GridMaker gm = new GridMaker();
        

        //DataSource dataSource = new VolumePatterns.Balls(cellSize, (cellSize/2)*1.1);

        DataSources.Ring ring = new DataSources.Ring(17*MM, 3*MM, -7*MM, 7*MM);

        DataSource cubicGrid = new VolumePatterns.CubicGrid(3*MM, 0.5*MM);
        DataSource gyroid = new VolumePatterns.Gyroid(8*MM, 0.5*MM);
        
        DataSources.Ball ball = new DataSources.Ball(0,0,0, 20*MM);
        
        DataSources.Intersection intersection = new DataSources.Intersection();

        intersection.addDataSource(ring);
        //intersection.addDataSource(ball);
        //intersection.addDataSource(gyroid);
        intersection.addDataSource(cubicGrid);

        
        gm.setBounds(gridBounds);
        gm.setDataSource(intersection);

        gm.setMaxAttributeValue(maxGridAttributeValue);
        gm.setVoxelSize(voxelSize*surfaceTransitionWidth);

        Grid grid = null;

        switch(gridType){
        default:
        case GRID_BYTE_ARRAY:
            grid = new ArrayAttributeGridByte(nx, ny, nz, resolution, resolution);
            break;

        case GRID_SHORT_INTERVALS:
            grid = new GridShortIntervals(nx, ny, nz, resolution, resolution);
            break;
            
        }
        grid.setGridBounds(gridBounds);

        printf("gm.makeGrid(), threads: %d\n", threadCount);
        long t0 = time();
        gm.setThreadCount(threadCount);
        gm.makeGrid(grid);
        printf("gm.makeGrid() done %d ms\n", (time() - t0));

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

        MeshMakerMT meshmaker = new MeshMakerMT();        
        
        t0 = time();
        meshmaker.setBlockSize(blockSize);
        meshmaker.setThreadCount(threadCount);
        meshmaker.setSmoothingWidth(smoothingWidth);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxAttributeValue(maxGridAttributeValue);            
        
        // TODO: Need to get a better way to estimate this number
        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
        meshmaker.makeMesh(grid, its);
        
        mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        printf("MeshMakerMT.makeMesh(): %d ms\n", (time()-t0));

        // extra decimation to get rid of seams
        //TODO - better targeting of seams
        if(maxDecimationError > 0){
            t0 = time();
            mesh = GridSaver.decimateMesh(mesh, maxDecimationError);
            printf("final decimation: %d ms\n", (time()-t0));
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
        
        return new KernelResults(true, min_bounds, max_bounds, volume, surface_area, 0);
        
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

            pname = "previewQuality";
            previewQuality = PreviewQuality.valueOf((String) params.get(pname));

            pname = "maxDecimationError";
            maxDecimationError = ((Double) params.get(pname)).doubleValue();

            pname = "innerDiameter";
            innerDiameter = ((Double) params.get(pname)).doubleValue();

            pname = "ringThickness";
            ringThickness = ((Double) params.get(pname)).doubleValue();

            pname = "ringWidth";
            ringWidth = ((Double) params.get(pname)).doubleValue();

            pname = "topBorderWidth";
            topBorderWidth = ((Double) params.get(pname)).doubleValue();

            pname = "bottomBorderWidth";
            bottomBorderWidth = ((Double) params.get(pname)).doubleValue();

            pname = "baseThickness";
            baseThickness = ((Double) params.get(pname)).doubleValue();

            pname = "image";
            imagePath = (String) params.get(pname);

            pname = "imageInvert";
            imageInvert = (Boolean) params.get(pname);

            pname = "crossSection";
            crossSectionPath = (String) params.get(pname);

            pname = "tilingX";
            tilingX = ((Integer) params.get(pname)).intValue();

            pname = "tilingY";
            tilingY = ((Integer) params.get(pname)).intValue();

            //pname = "edgeStyle";
            //edgeStyle = edgeStyle.valueOf((String) params.get(pname));

            pname = "symmetryStyle";
            symmetryStyle = symmetryStyle.valueOf((String) params.get(pname));

            pname = "text";
            text = ((String) params.get(pname));

            pname = "textDepth";
            textDepth = ((Double) params.get(pname));

            pname = "fontName";
            fontName = ((String) params.get(pname));

            pname = "fontBold";
            fontBold = (Boolean) params.get(pname);

            pname = "fontItalic";
            fontItalic = (Boolean) params.get(pname);

            pname = "fontSize";
            fontSize = ((Integer) params.get(pname)).intValue();

            pname = "material";
            material = ((String) params.get(pname));

            pname = "smoothSteps";
            smoothSteps = ((Integer) params.get(pname)).intValue();

            pname = "smoothingWidth";
            smoothingWidth = ((Number) params.get(pname)).doubleValue();

            pname = "threads";
            threadCount = ((Integer) params.get(pname)).intValue();

            if (threadCount == 0) {
                int cores = Runtime.getRuntime().availableProcessors();

                threadCount = cores;

                // scales well to 4 threads, stop there.
                if (threadCount > 4) {
                    threadCount = 4;
                }

                System.out.println("Number of cores:" + threadCount);
            }

            pname = "regions";
            regions = RegionPrunner.Regions.valueOf((String) params.get(pname));

            pname = "useGrayscale";
            useGrayscale = (Boolean) params.get(pname);

            pname = "visRemovedRegions";
            visRemovedRegions = (Boolean) params.get(pname);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error parsing: " + pname + " val: " + params.get(pname));
        }
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

    public static void main(String[] args) {
        HashMap<String,String> params = new HashMap<String,String>();

        int LOOPS = 1;

        for(int i=0; i < LOOPS; i++) {
            HostedKernel kernel = new VolumeSculptorKernel();

            System.out.println("***High Resolution");
            params.put("resolution","0.00002");
            params.put("text","");
            params.put("previewQuality","HIGH");
            params.put("threads","4");

            Map<String,Object> parsed_params = ParameterUtil.parseParams(kernel.getParams(), params);

            try {
                FileOutputStream fos = new FileOutputStream("c:/tmp/thread" + Thread.currentThread().getName() + ".x3d");
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
}
