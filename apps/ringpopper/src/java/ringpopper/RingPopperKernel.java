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

package ringpopper;

// External Imports

import abfab3d.creator.GeometryKernel;
import abfab3d.creator.KernelResults;
import abfab3d.creator.Parameter;
import abfab3d.creator.shapeways.HostedKernel;
import abfab3d.creator.util.ParameterUtil;

import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.GridShortIntervals;
import abfab3d.grid.Grid;

import abfab3d.util.DataSource;
import abfab3d.util.TextUtil;

import abfab3d.datasources.ImageBitmap;
import abfab3d.datasources.Intersection;
import abfab3d.datasources.DataTransformer;
import abfab3d.datasources.Ring;
import abfab3d.datasources.Complement;
import abfab3d.datasources.Box;
import abfab3d.datasources.Union;

import abfab3d.transforms.RingWrap;
import abfab3d.transforms.Rotation;
import abfab3d.transforms.FriezeSymmetry;
import abfab3d.transforms.CompositeTransform;

import abfab3d.grid.op.GridMaker;

import abfab3d.io.output.BoxesX3DExporter;
import abfab3d.io.output.SAVExporter;
import abfab3d.io.output.MeshMakerMT;

import abfab3d.mesh.AreaCalculator;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import abfab3d.mesh.LaplasianSmooth;
import abfab3d.mesh.IndexedTriangleSetBuilder;


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

//import java.awt.*;

/**
 * Geometry Kernel for the RingPopper
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov
 */
public class RingPopperKernel extends HostedKernel {
    private static final boolean USE_MIP_MAPPING = false;
    private static final boolean USE_FAST_MATH = true;
    private final boolean USE_MESH_MAKER_MT = true;

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
    private int imageInterpolationType = ImageBitmap.INTERPOLATION_LINEAR;
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

        params.put("crossSection", new Parameter("crossSection", "Cross Section", "The image of cross section", "NONE", 1,
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
        params.put("topBorderWidth", new Parameter("topBorderWidth", "Top Border Width", "The width of the top border", "0", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );
        params.put("bottomBorderWidth", new Parameter("bottomBorderWidth", "Bottom Border Width", "The width of the bottom border", "0", 1,
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

        params.put("smoothingWidth", new Parameter("smoothingWidth", "Smoothing Width", "How many voxels to smooth", "1.0", 1,
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

        boolean makeRingWrap = true;

        pullParams(params);

        if (acc == Accuracy.VISUAL) {
            resolution = resolution * previewQuality.getFactor();
        }

        if (maxDecimationError > 0) {
            if (acc == Accuracy.VISUAL) {
                maxDecimationError = 0.1 * resolution * resolution;
            } else {
                // Models looked too blocky with .1
                maxDecimationError = 0.05 * resolution * resolution;
            }
        }

        printf("Res: %10.3g  maxDec: %10.3g\n", resolution, maxDecimationError);

        double margin = 2 * resolution;
        double voxelSize = resolution;
        
        double gridWidth = 0;
        if(makeRingWrap)
            gridWidth = (innerDiameter + 2 * ringThickness + 2 * margin);
        else
            gridWidth = (innerDiameter*Math.PI  +  2 * margin);
        
        double ringYmin  = -ringWidth/2;
        double ringYmax  = ringWidth/2;
        if(hasTopBorder()){
            ringYmax += topBorderWidth;
        }
        if(hasBottomBorder()){
            ringYmin -= bottomBorderWidth;
        }
        
        double gridDepth = 0;
        if(makeRingWrap)
            gridDepth = gridWidth;
        else 
            gridDepth = ringThickness + 2 * margin;

        double gridHeight = ringYmax + -ringYmin + 2*margin;

        double offset = voxelSize*0.3; // some hack to get asymetry 
        
        int nx = (int) (gridWidth / voxelSize);
        int ny = (int) (gridHeight / voxelSize);// ((bounds[3] - bounds[2]) / voxelSize);
        int nz = (int) (gridDepth / voxelSize);//((bounds[5] - bounds[4]) / voxelSize);
        double gridXmin = -nx * voxelSize/2;
        double gridYmin = ringYmin - margin;
        double gridZmin = -nz * voxelSize/2;

        double bounds[] = new double[]{ gridXmin, gridXmin + nx * voxelSize, 
                                        gridYmin, gridYmin + ny * voxelSize, 
                                        gridZmin, gridZmin + nz * voxelSize};
        
        //int nx = (int) ((bounds[1] - bounds[0]) / voxelSize);
        // int ny = (int) ((bounds[3] - bounds[2]) / voxelSize);
        //int nz = (int) ((bounds[5] - bounds[4]) / voxelSize);

        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);
        
        // HARD CODED params to play with 
        // width of Gaussian smoothing of grid, may be 0. - no smoothing 
        //double smoothingWidth = 0.0; 
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
        imageBlurWidth = surfaceTransitionWidth*voxelSize;
        imageBaseThreshold = 0.1;            
        imageInterpolationType = ImageBitmap.INTERPOLATION_LINEAR;


        bandLength = innerDiameter * Math.PI;

        Intersection complete_band = new Intersection();

        // add crossSectionImage to complete_band
        if (crossSectionPath != null && crossSectionPath.length() > 0 && !crossSectionPath.equalsIgnoreCase("NONE")) {
            complete_band.add(makeCrossSection(ringYmin, ringYmax));
        }

        DataSource image_band = makeImageBand();       
        complete_band.add(image_band);

        // add Text 
        if (text != null && text.length() > 0) {
            complete_band.add(makeTextComplement(ringYmin, ringYmax));
        }

        RingWrap ringWrap = new RingWrap();
        ringWrap.setRadius(innerDiameter / 2);

        DataTransformer completeRing = new DataTransformer();
        completeRing.setSource(complete_band);
        if(makeRingWrap)
            completeRing.setTransform(ringWrap);
                
        Intersection clippedRing = new Intersection();
        if(makeRingWrap)
            clippedRing.add(new Ring(innerDiameter/2, ringThickness, ringYmin, ringYmax));
        clippedRing.add(completeRing);
        
        GridMaker gm = new GridMaker();
        
        gm.setBounds(bounds);
        gm.setSource(clippedRing);
        gm.setMaxAttributeValue(maxGridAttributeValue);
        gm.setVoxelSize(voxelSize*surfaceTransitionWidth);

        // Seems BlockBased better for this then Array.
        // BlockBasedGridByte is not MT safe (VB)

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
        grid.setGridBounds(bounds);

        printf("gm.makeGrid(), threads: %d\n", threadCount);
        long t0 = time();
        gm.setThreadCount(threadCount);
        gm.execute(grid);
        printf("gm.makeGrid() done %d ms\n", (time() - t0));

        int regions_removed = 0;
        int min_volume = 10;

        if (false) {
        //if (regions != RegionPrunner.Regions.ALL) {
            t0 = time();
            if (visRemovedRegions) {
                regions_removed = RegionPrunner.reduceToOneRegion(grid, handler, bounds, min_volume);
            } else {
                regions_removed = RegionPrunner.reduceToOneRegion(grid, min_volume);
            }
            printf("Regions removed: %d\n", regions_removed);
            printf("regions removal done %d ms\n", (time() - t0));

        }

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
            meshmaker.setThreadCount(threadCount);
            meshmaker.setSmoothingWidth(smoothingWidth);
            meshmaker.setMaxDecimationError(maxDecimationError);
            meshmaker.setMaxAttributeValue(maxGridAttributeValue);            

            // TODO: Need to get a better way to estimate this number
            IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
            meshmaker.makeMesh(grid, its);

            // Release grid to lower total memory requirements
            grid = null;

            mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

            printf("MeshMakerMT.makeMesh(): %d ms\n", (time()-t0));
            if(false){ // this should not be used here
                if(smoothSteps > 0){
                    LaplasianSmooth ls = new LaplasianSmooth();
                    ls.processMesh(mesh, smoothSteps);
                }
            }
            // extra decimation to get rid of seams
            //TODO - better targeting of seams
            if(maxDecimationError > 0){
                t0 = time();
                mesh = GridSaver.decimateMesh(mesh, maxDecimationError);
                printf("final decimation: %d ms\n", (time()-t0));
            }
            
        } else {
            // old ST style 
            mesh = GridSaver.createIsosurface(grid, smoothSteps);

            // Release grid to lower total memory requirements
            grid = null;
            if(maxDecimationError > 0)
                mesh = GridSaver.decimateMesh(mesh, maxDecimationError);
        }

        if(regions != RegionPrunner.Regions.ALL) {
            t0 = time();
            ShellResults sr = GridSaver.getLargestShell(mesh, min_volume);
            mesh = sr.getLargestShell();
            regions_removed = sr.getShellsRemoved();
            printf("GridSaver.getLargestShell(): %d ms\n", (time() - t0));
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

        System.out.println("Regions Removed: " + regions_removed);
        return new KernelResults(true, min_bounds, max_bounds, volume, surface_area, regions_removed);

    }

    boolean hasTopBorder(){
        return (topBorderWidth > 0.);
    }

    boolean hasBottomBorder(){
        return (bottomBorderWidth > 0.);
    }

    /**
     * long horizontal unwrapped image
     */
    DataSource makeImageBand() {

        ImageBitmap image_src = new ImageBitmap();
       
        image_src.setLocation(0, 0, ringThickness / 2);
        image_src.setImagePath(imagePath);
        image_src.setBaseThickness(baseThickness);
        image_src.setUseGrayscale(useGrayscale);
        image_src.setBlurWidth((useGrayscale)? 0.: imageBlurWidth);
        image_src.setBaseThreshold(imageBaseThreshold);
        image_src.setInterpolationType(imageInterpolationType);
        image_src.setVoxelSize(resolution);

        if (imageInvert) {
            image_src.setImageType(ImageBitmap.IMAGE_TYPE_ENGRAVED);
        }

        image_src.setInterpolationType(imageInterpolationType);
        

        if (symmetryStyle == SymmetryStyle.NONE) {

            // image spans the whole ring length
            image_src.setSize(bandLength, ringWidth, ringThickness);
            image_src.setTiles(tilingX, tilingY);

        } else {

            // image spans only one tile
            image_src.setSize(bandLength / tilingX, ringWidth, ringThickness);
            image_src.setTiles(1, tilingY);

        }

        DataSource image_band = image_src;

        if (symmetryStyle != SymmetryStyle.NONE) {

            DataTransformer image_frieze = new DataTransformer();
            image_frieze.setSource(image_src);

            FriezeSymmetry fs = new FriezeSymmetry();
            fs.setFriezeType(symmetryStyle.getCode());
            double tileWidth = bandLength / tilingX;
            fs.setDomainWidth(tileWidth);

            image_frieze.setTransform(fs);
            image_frieze.setSource(image_src);

            image_band = image_frieze;
        }

        if (!(hasTopBorder() || hasBottomBorder() )) {
            return image_band;
        }


        Union union = new Union();

        union.add(image_band);

        if (hasTopBorder()) {
            Box top_band = new Box();
            top_band.setSize(bandLength, topBorderWidth, ringThickness);
            top_band.setLocation(0, ringWidth / 2 + topBorderWidth / 2, ringThickness / 2);
            top_band.setSmoothBoundaries(false, true, true);

            union.add(top_band);
        }

        if (hasBottomBorder()) {
            Box bottom_band = new Box();
            bottom_band.setSize(bandLength, bottomBorderWidth, ringThickness);
            bottom_band.setLocation(0, -ringWidth / 2 - bottomBorderWidth / 2, ringThickness / 2);
            bottom_band.setSmoothBoundaries(false, true, true);
            union.add(bottom_band);
        }

        return union;

    }


    /**
     * complement of text to make text engraving
     */
    DataSource makeTextComplement(double ymin, double ymax) {

        double width = ymax - ymin;

        int maxFontSize = (int) (width / POINT_SIZE);
        if (fontSize > maxFontSize) {
            fontSize = maxFontSize;
            System.err.printf("EXTMSG: Font is too large. Reduced to %d points\n", fontSize);
        }

        // we need to create font of specified size
        // we assume, that the font size is the maximal height of the text string 
        double textHeightM = (fontSize * POINT_SIZE);

        int textBitmapHeight = (int) (width / TEXT_RENDERING_PIXEL_SIZE);
        int textBitmapWidth = (int) (textBitmapHeight * bandLength / width);
        // height of text string in pixels
        int textHeightPixels = (int) (textBitmapHeight * textHeightM / width);
        // we use Insets to have empty space around centered text 
        // it also will make text of specitfied height
        int textOffsetV = (textBitmapHeight - textHeightPixels) / 2;
        int textOffsetH = 10;
        printf("text bitmap size: (%d x %d) pixels\n", textBitmapWidth, textBitmapHeight);
        printf("text height: %d pixels\n", textHeightPixels);
        printf("vertical text offset: %d pixels\n", textOffsetV);

        ImageBitmap textBand = new ImageBitmap();

        textBand.setSize(Math.PI * innerDiameter, width, textDepth);
        // text is offset in opposite z-direction because we have to rotate it 180 deg around Y-axis 
        textBand.setLocation(0, (ymax + ymin)/2, -textDepth / 2);
        textBand.setBaseThickness(0.);
        textBand.setImageType(ImageBitmap.IMAGE_TYPE_EMBOSSED);
        textBand.setTiles(1, 1);
        textBand.setBlurWidth(imageBlurWidth);
        textBand.setUseGrayscale(false);
        textBand.setInterpolationType(ImageBitmap.INTERPOLATION_LINEAR);


        int fontStyle = Font.PLAIN;

        if (fontBold)
            fontStyle = Font.BOLD;

        if (fontItalic)
            fontStyle |= Font.ITALIC;

        boolean font_found = false;

        GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String ffname : genv.getAvailableFontFamilyNames()) {
            if (ffname.equals(fontName)) {
                font_found = true;
                break;
            }
        }

        if (!font_found) {
            System.err.println("EXTMSG: Could not find requested font: " + fontName);
        }

        Font font = new Font(fontName, fontStyle, DEFAULT_FONT_SIZE);

        textBand.setImage(TextUtil.createTextImage(textBitmapWidth, textBitmapHeight, text,
                font,
                new Insets(textOffsetV, textOffsetH, textOffsetV, textOffsetH),
                false));

        // we want text on the inside. So it should face in opposite direction
        Rotation textRotation = new Rotation();
        textRotation.setRotation(new Vector3d(0, 1, 0), 180 * TORAD);

        // rotated text
        DataTransformer rotatedText = new DataTransformer();
        rotatedText.setSource(textBand);
        rotatedText.setTransform(textRotation);

        Complement textComplement = new Complement(rotatedText);

        return textComplement;

    }

    /**
     * cross section of ring
     */
    DataSource makeCrossSection(double ringYmin, double ringYmax) {

        ImageBitmap crossSect = new ImageBitmap();
        
        double size = (ringYmax - ringYmin);
        crossSect.setSize(size, ringThickness, Math.PI * innerDiameter);

        double center = -(ringYmin+ringYmax)/2;

        crossSect.setLocation(center, ringThickness / 2, 0);

        printf("cross center: %7.3f mm cross size : %7.3f mm\n", center/MM,size/MM);

        crossSect.setBaseThickness(0.);
        crossSect.setUseGrayscale(false);
        crossSect.setImagePath(crossSectionPath);
        crossSect.setBlurWidth(imageBlurWidth);
        crossSect.setInterpolationType(ImageBitmap.INTERPOLATION_LINEAR);

        CompositeTransform crossTrans = new CompositeTransform();

        Rotation crot1 = new Rotation();
        crot1.setRotation(new Vector3d(0, 0, 1), -90 * TORAD);
        Rotation crot2 = new Rotation();
        crot2.setRotation(new Vector3d(0, 1, 0), -90 * TORAD);
        crossTrans.add(crot1);
        crossTrans.add(crot2);

        // transformed cross section 
        DataTransformer transCross = new DataTransformer();
        transCross.setSource(crossSect);
        transCross.setTransform(crossTrans);

        return transCross;

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
        HashMap<String,String> params = new HashMap<String,String>();

        int LOOPS = 1;

        for(int i=0; i < LOOPS; i++) {
            HostedKernel kernel = new RingPopperKernel();

            System.out.println("***High Resolution");
            params.put("resolution","0.00002");
            params.put("text","");
            params.put("previewQuality","HIGH");
            params.put("threads","4");

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
}
