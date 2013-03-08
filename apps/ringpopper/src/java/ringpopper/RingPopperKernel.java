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
import abfab3d.grid.BlockBasedGridByte;
import abfab3d.grid.Grid;
import abfab3d.grid.GridShortIntervals;
import abfab3d.grid.op.DataSources;
import abfab3d.grid.op.GridMaker;
import abfab3d.grid.op.VecTransforms;
import abfab3d.io.output.BoxesX3DExporter;
import abfab3d.io.output.SAVExporter;
import abfab3d.mesh.AreaCalculator;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import abfab3d.util.DataSource;
import abfab3d.util.TextUtil;
import app.common.GridSaver;
import app.common.RegionPrunner;
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
import static java.lang.System.currentTimeMillis;

//import java.awt.*;

/**
 * Geometry Kernel for the RingPopper
 *
 * @author Alan Hudson
 */
public class RingPopperKernel extends HostedKernel {
    private static final boolean USE_MIP_MAPPING = false;

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
    private double edgeWidth;

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
    private EdgeStyle edgeStyle;
    private SymmetryStyle symmetryStyle;

    private String material;
    private String text = "MADE IN THE FUTURE";
    private double textDepth;
    private String fontName = "Times New Roman";
    private boolean fontBold = false;
    private boolean fontItalic = false;
    private int fontSize = 20;

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

        params.put("edgeStyle", new Parameter("edgeStyle", "Edge Style", "Whether to put lines on the band", edgeStyle.BOTH.toString(), 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, enumToStringArray(edgeStyle.values()))
        );

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
        params.put("edgeWidth", new Parameter("edgeWidth", "Edge Width", "The width of the bands", "0.001", 1,
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

        params.put("smoothSteps", new Parameter("smoothSteps", "Smooth Steps", "How smooth to make the object", "5", 1,
                Parameter.DataType.INTEGER, Parameter.EditorType.DEFAULT,
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

        long start = System.currentTimeMillis();

        pullParams(params);

        if (acc == Accuracy.VISUAL) {
            resolution = resolution * previewQuality.getFactor();
        }

        if (maxDecimationError > 0) {
            if (acc == Accuracy.VISUAL) {
                maxDecimationError = 0.1 * resolution * resolution;
            } else {
                // Models looked too blocky with .1
                maxDecimationError = 0.025 * resolution * resolution;
            }
        }

        printf("Res: %10.3g  maxDec: %10.3g\n", resolution, maxDecimationError);

        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab

        double margin = 1 * resolution;

        double gridWidth = (innerDiameter + 2 * ringThickness + 2 * margin);
        double gridHeight = (ringWidth + 2 * edgeWidth + 2 * margin);
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth / 2, gridWidth / 2 + EPS, -gridHeight / 2, gridHeight / 2 + EPS, -gridDepth / 2, gridDepth / 2 + EPS};

        int nx = (int) ((bounds[1] - bounds[0]) / resolution);
        int ny = (int) ((bounds[3] - bounds[2]) / resolution);
        int nz = (int) ((bounds[5] - bounds[4]) / resolution);
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);


        DataSource image_band = makeImageBand();

        DataSources.Intersection complete_band = new DataSources.Intersection();
        complete_band.addDataSource(image_band);

        // add Text 
        if (text != null && text.length() > 0) {
            complete_band.addDataSource(makeTextComplement());
        }

        // add crossSectionImage to complete_band
        if (crossSectionPath != null && crossSectionPath.length() > 0 && !crossSectionPath.equals("NONE")) {
            complete_band.addDataSource(makeCrossSection());
        }


        VecTransforms.RingWrap ringWrap = new VecTransforms.RingWrap();
        ringWrap.setRadius(innerDiameter / 2);

        DataSources.DataTransformer ring = new DataSources.DataTransformer();
        ring.setDataSource(complete_band);
        ring.setTransform(ringWrap);

        GridMaker gm = new GridMaker();

        gm.setBounds(bounds);
        gm.setDataSource(ring);

        // Seems BlockBased better for this then Array.
        // BlockBasedGridByte is not MT safe

        Grid grid = null;
/*
        if (threadCount == 1) {
            grid = new BlockBasedGridByte(nx, ny, nz, resolution, resolution, 5);
        } else {
            grid = new GridShortIntervals(nx, ny, nz, resolution, resolution);
        }
*/
//        grid = new ArrayAttributeGridByte(nx, ny, nz, resolution, resolution);

        grid = new GridShortIntervals(nx, ny, nz, resolution, resolution);

        printf("gm.makeGrid(), threads: %d\n", threadCount);
        long t0 = currentTimeMillis();
        gm.setThreadCount(threadCount);
        gm.makeGrid(grid);
        printf("gm.makeGrid() done %d ms\n", (currentTimeMillis() - t0));

        int regions_removed = 0;
        int min_volume = 10;

        if (false) {
        //if (regions != RegionPrunner.Regions.ALL) {
            t0 = currentTimeMillis();
            if (visRemovedRegions) {
                regions_removed = RegionPrunner.reduceToOneRegion(grid, handler, bounds, min_volume);
            } else {
                regions_removed = RegionPrunner.reduceToOneRegion(grid, min_volume);
            }
            printf("Regions removed: %d\n", regions_removed);
            printf("regions removal done %d ms\n", (currentTimeMillis() - t0));

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

        GridSaver.writeIsosurfaceMaker(mesh, gw, gh, gd, vs, sh, handler, params, maxDecimationError, true, (regions != RegionPrunner.Regions.ALL));

        AreaCalculator ac = new AreaCalculator();
        mesh.getTriangles(ac);
        double volume = ac.getVolume();
        double surface_area = ac.getArea();

        t0 = System.nanoTime();
        printf("final surface area: %7.8f CM^2\n", surface_area * 1.e4);
        printf("final volume: %7.8f CM^3 (%5.3f ms)\n", volume * 1.e6, (System.nanoTime() - t0) * 1.e-6);


        System.out.println("Total Time: " + (System.currentTimeMillis() - start));
        System.out.println("-------------------------------------------------");
        return new KernelResults(true, min_bounds, max_bounds, volume, surface_area, regions_removed);

    }


    /**
     * long horizontal unwrapped image
     */
    DataSource makeImageBand() {

        DataSources.ImageBitmap image_src = new DataSources.ImageBitmap();

        image_src.setUseGrayscale(true);
        image_src.setLocation(0, 0, ringThickness / 2);
        image_src.setImagePath(imagePath);
        image_src.setBaseThickness(baseThickness);
        image_src.setUseGrayscale(useGrayscale);
        if (imageInvert) {
            image_src.setImageType(DataSources.ImageBitmap.IMAGE_NEGATIVE);
        }

        if (USE_MIP_MAPPING) {
            image_src.setInterpolationType(DataSources.ImageBitmap.INTERPOLATION_MIPMAP);
            image_src.setPixelWeightNonlinearity(1.0);  // 0 - linear, 1. - black pixels get more weight
            image_src.setProbeSize(resolution * 2.);
        }

        if (symmetryStyle == SymmetryStyle.NONE) {

            // image spans the whole ring length
            image_src.setSize(innerDiameter * Math.PI, ringWidth, ringThickness);
            image_src.setTiles(tilingX, tilingY);

        } else {

            // image spans only one tile
            image_src.setSize(innerDiameter * Math.PI / tilingX, ringWidth, ringThickness);
            image_src.setTiles(1, tilingY);

        }

        DataSource image_band = image_src;

        if (symmetryStyle != SymmetryStyle.NONE) {

            DataSources.DataTransformer image_frieze = new DataSources.DataTransformer();
            image_frieze.setDataSource(image_src);

            VecTransforms.FriezeSymmetry fs = new VecTransforms.FriezeSymmetry();
            fs.setFriezeType(symmetryStyle.getCode());
            double tileWidth = innerDiameter * Math.PI / tilingX;
            fs.setDomainWidth(tileWidth);

            image_frieze.setTransform(fs);
            image_frieze.setDataSource(image_src);

            image_band = image_frieze;
        }

        if (edgeStyle == edgeStyle.NONE) {
            return image_band;
        }


        DataSources.Union union = new DataSources.Union();

        union.addDataSource(image_band);

        if (edgeStyle == edgeStyle.TOP || edgeStyle == edgeStyle.BOTH) {
            DataSources.Block top_band = new DataSources.Block();
            top_band.setSize(innerDiameter * Math.PI, edgeWidth, ringThickness);
            top_band.setLocation(0, ringWidth / 2 + edgeWidth / 2, ringThickness / 2);
            union.addDataSource(top_band);
        }

        if (edgeStyle == edgeStyle.BOTTOM || edgeStyle == edgeStyle.BOTH) {
            DataSources.Block bottom_band = new DataSources.Block();
            bottom_band.setSize(innerDiameter * Math.PI, edgeWidth, ringThickness);
            bottom_band.setLocation(0, -ringWidth / 2 - edgeWidth / 2, ringThickness / 2);
            union.addDataSource(bottom_band);
        }

        return union;

    }


    /**
     * complement of text to make text engraving
     */
    DataSource makeTextComplement() {

        int maxFontSize = (int) (ringWidth / POINT_SIZE);
        if (fontSize > maxFontSize) {
            fontSize = maxFontSize;
            System.err.printf("EXTMSG: Font is too large. Reduced to %d points\n", fontSize);
        }

        // we need to create font of specified size
        // we assume, that the font size is the maximal height of the text string 
        double textHeightM = (fontSize * POINT_SIZE);

        int textBitmapHeight = (int) (ringWidth / TEXT_RENDERING_PIXEL_SIZE);
        int textBitmapWidth = (int) (textBitmapHeight * (Math.PI * innerDiameter) / ringWidth);
        // height of text string in pixels
        int textHeightPixels = (int) (textBitmapHeight * textHeightM / ringWidth);
        // we use Insets to have empty space around centered text 
        // it also will make text of specitfied height
        int textOffsetV = (textBitmapHeight - textHeightPixels) / 2;
        int textOffsetH = 10;
        printf("text bitmap size: (%d x %d) pixels\n", textBitmapWidth, textBitmapHeight);
        printf("text height: %d pixels\n", textHeightPixels);
        printf("vertical text offset: %d pixels\n", textOffsetV);

        DataSources.ImageBitmap textBand = new DataSources.ImageBitmap();

        textBand.setSize(Math.PI * innerDiameter, ringWidth, textDepth);
        // text is offset in opposite z-direction because we have to rotate it 180 deg around Y-axis 
        textBand.setLocation(0, 0, -textDepth / 2);
        textBand.setBaseThickness(0.);
        textBand.setImageType(DataSources.ImageBitmap.IMAGE_POSITIVE);
        textBand.setTiles(1, 1);
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
        VecTransforms.Rotation textRotation = new VecTransforms.Rotation();
        textRotation.setRotation(new Vector3d(0, 1, 0), 180 * TORAD);

        // rotated text
        DataSources.DataTransformer rotatedText = new DataSources.DataTransformer();
        rotatedText.setDataSource(textBand);
        rotatedText.setTransform(textRotation);

        DataSources.Complement textComplement = new DataSources.Complement();
        textComplement.setDataSource(rotatedText);

        return textComplement;

    }

    /**
     * cross section of ring
     */
    DataSource makeCrossSection() {

        DataSources.ImageBitmap crossSect = new DataSources.ImageBitmap();
        double totalWidth = ringWidth;
        if (edgeStyle != edgeStyle.NONE)
            totalWidth += 2 * edgeWidth;

        crossSect.setSize(totalWidth, ringThickness, Math.PI * innerDiameter);
        crossSect.setLocation(0, ringThickness / 2, 0);
        crossSect.setBaseThickness(0.);
        crossSect.setUseGrayscale(false);
        crossSect.setImagePath(crossSectionPath);

        VecTransforms.CompositeTransform crossTrans = new VecTransforms.CompositeTransform();

        VecTransforms.Rotation crot1 = new VecTransforms.Rotation();
        crot1.setRotation(new Vector3d(0, 0, 1), -90 * TORAD);
        VecTransforms.Rotation crot2 = new VecTransforms.Rotation();
        crot2.setRotation(new Vector3d(0, 1, 0), -90 * TORAD);
        crossTrans.add(crot1);
        crossTrans.add(crot2);

        // transformed cross section 
        DataSources.DataTransformer transCross = new DataSources.DataTransformer();
        transCross.setDataSource(crossSect);
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

            pname = "edgeWidth";
            edgeWidth = ((Double) params.get(pname)).doubleValue();

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

            pname = "edgeStyle";
            edgeStyle = edgeStyle.valueOf((String) params.get(pname));

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

            pname = "threads";
            threadCount = ((Integer) params.get(pname)).intValue();

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

        int LOOPS = 3;

        for(int i=0; i < LOOPS; i++) {
            HostedKernel kernel = new RingPopperKernel();

            params.put("innerDiameter","0.06");
            params.put("threadCount","1");

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
