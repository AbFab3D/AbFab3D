/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package ringtorus;

// External Imports
import java.io.*;
import java.util.*;
import javax.vecmath.Vector3d;

import abfab3d.io.output.*;
import abfab3d.mesh.WingedEdgeTriangleMesh;

import app.common.RegionPrunner;
import app.common.X3DViewer;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.sav.BinaryContentHandler;

import abfab3d.grid.*;
import abfab3d.grid.op.*;
import abfab3d.creator.*;
import abfab3d.creator.shapeways.*;

//import java.awt.*;

import static abfab3d.util.MathUtil.TORAD;
import static abfab3d.util.Output.printf;
import static java.lang.Math.PI;

import app.common.GridSaver;

/**
 * Geometry Kernel for the RingTorus
 *
 * @author Alan Hudson
 */
public class RingTorusKernel extends HostedKernel {
    /** Debugging level.  0-5.  0 is none */
    private static final int DEBUG_LEVEL = 0;

    enum SymmetryStyle {
        NONE(-1),
        WP_O (0),    // O
        WP_XX(1),   // xx
        WP_SX(2),   // *x
        WP_SS(3),   // **
        WP_632(4),   // 632
        WP_S632(5),   // *632
        WP_333(6),   // 333
        WP_S333(7),   // *333
        WP_3S3(8),   // 3*3
        WP_442(9),   // 442
        WP_S442(10),   // *442
        WP_4S2(11),   // 4*2
        WP_2222(12),   // 2222
        WP_22X(13),   // 22x
        WP_22S(14),   // 22*
        WP_S2222(15),   // *2222
        WP_2S22(16);   // 2*22

        private int code;

        SymmetryStyle(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private String[] availableMaterials = new String[] {"White Strong & Flexible", "White Strong & Flexible Polished",
        "Silver", "Silver Glossy", "Stainless Steel","Gold Plated Matte", "Gold Plated Glossy","Antique Bronze Matte",
        "Antique Bronze Glossy", "Alumide", "Polished Alumide"};

    // Physical Sizing
    private double innerDiameter;
    private double ringThickness;
    private double ringWidth;

    /** How much to scale in the z direction to flatten the ring  */
    private double flatten;

    /** The horizontal and vertical resolution */
    private double resolution;
    private int smoothSteps;
    private double maxDecimationError;

    /** The image filename */
    private String image;

    private int tilingX;
    private int tilingY;
    private SymmetryStyle symmetryStyle;

    private String material;

    /** How many regions to keep */
    private RegionPrunner.Regions regions;

    /**
     * Get the parameters for this editor.
     *
     * @return The parameters.
     */
    public Map<String,Parameter> getParams() {
        HashMap<String,Parameter> params = new HashMap<String,Parameter>();

        int seq = 0;
        int step = 0;

        // Image based params
        params.put("image", new Parameter("image", "Image", "The image to use", "images/Tile_dilate8_unedged.png", 1,
            Parameter.DataType.STRING, Parameter.EditorType.FILE_DIALOG,
            step, seq++, false, 0, 0.1, null, null)
        );

        params.put("tilingX", new Parameter("tilingX", "Tiling X", "The tiling along left/right of the ring", "20", 1,
                Parameter.DataType.INTEGER, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 50, null, null)
        );

        params.put("tilingY", new Parameter("tilingY", "Tiling Y", "The tiling along up/down of the ring", "3", 1,
                Parameter.DataType.INTEGER, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 50, null, null)
        );

        params.put("symmetryStyle", new Parameter("symmetryStyle", "Symmetry Style", "Whether to put lines on the band", SymmetryStyle.WP_O.toString(), 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, enumToStringArray(symmetryStyle.values()))
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

        params.put("flatten", new Parameter("flatten", "Flatten", "How much to flatten the ring", "0.5", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
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

        params.put("maxDecimationError", new Parameter("maxDecimationError", "MaxDecimationError", "Maximum error during decimation", "5e-10", 1,
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
     * @param params The parameters
     * @param acc The accuracy to generate the model
     * @param handler The X3D content handler to use
     */
    public KernelResults generate(Map<String,Object> params, Accuracy acc, BinaryContentHandler handler) throws IOException {

        long start = System.currentTimeMillis();

        pullParams(params);

        if (acc == Accuracy.VISUAL) {
            // TODO: not sure I like this, could have two params:  visualResolution, printResolution
            resolution = resolution * 1.5;
        }

        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab
        double margin = 1*resolution;

        double CM = 0.01; // cm -> meters

        //double ringR = innerDiameter / Math.PI;
        //double ringr = ringR / 4.0;

        double ringR = 3*CM;
        double ringr = 0.5*CM;
        double ringThickness = 0.2*CM;

        int tilesR = tilingX;
        int tilesr = tilingY;

        double gridWidth =2*(ringR + ringr) + 2*margin;
        double gridHeight  = 2*(ringr) + 2*margin;
        double gridDepth = gridWidth;

        double tileWidth = 2*PI*ringr/tilesr;
        double tileHeight = 2*PI*(ringR)/tilesR;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/resolution);
        int ny = (int)((bounds[3] - bounds[2])/resolution);
        int nz = (int)((bounds[5] - bounds[4])/resolution);

        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap bitmap = new DataSources.ImageBitmap();

        bitmap.setSize(tileWidth, tileHeight, ringThickness);
        bitmap.setLocation(0,0,-ringThickness);
        bitmap.setBaseThickness(0.);
        bitmap.setTiles(1,1);
        bitmap.setImageType(DataSources.ImageBitmap.IMAGE_POSITIVE);
        bitmap.setImagePath(image);

        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();

        VecTransforms.WallpaperSymmetry wps = new VecTransforms.WallpaperSymmetry();
        wps.setSymmetryType(symmetryStyle.getCode());
        wps.setDomainWidth(tileWidth);
        wps.setDomainHeight(tileHeight);
        wps.setDomainSkew(0);

        VecTransforms.RingWrap rw1 = new VecTransforms.RingWrap();
        rw1.setRadius(ringr);

        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.setRotation(new Vector3d(0,0,1), 90*TORAD);

        VecTransforms.Scale scale = new VecTransforms.Scale();
        scale.setScale(1, 1, flatten);

        VecTransforms.RingWrap rw2 = new VecTransforms.RingWrap();
        rw2.setRadius(ringR);

        compTrans.add(wps);
        compTrans.add(rw1);
        compTrans.add(rot);
        if (Math.abs(flatten - 1.0) > 0.0001) {
            compTrans.add(scale);
        }
        compTrans.add(rw2);


        GridMaker gm = new GridMaker();

        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(bitmap);

        Grid grid = new BlockBasedGridByte(nx, ny, nz, resolution, resolution, 5);


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
    private void pullParams(Map<String,Object> params) {
        String pname = null;

        try {
            pname = "resolution";
            resolution = ((Double) params.get(pname)).doubleValue();

            pname = "maxDecimationError";
            maxDecimationError = ((Double) params.get(pname)).doubleValue();

            pname = "innerDiameter";
            innerDiameter = ((Double) params.get(pname)).doubleValue();

            pname = "ringThickness";
            ringThickness = ((Double) params.get(pname)).doubleValue();

            pname = "ringWidth";
            ringWidth = ((Double) params.get(pname)).doubleValue();

            pname = "flatten";
            flatten = ((Double) params.get(pname)).doubleValue();

            pname = "image";
            image = (String) params.get(pname);

            pname = "tilingX";
            tilingX = ((Integer) params.get(pname)).intValue();

            pname = "tilingY";
            tilingY = ((Integer) params.get(pname)).intValue();

            pname = "symmetryStyle";
            symmetryStyle = symmetryStyle.valueOf((String) params.get(pname));

            pname = "material";
            material = ((String) params.get(pname));

            pname = "smoothSteps";
            smoothSteps = ((Integer) params.get(pname)).intValue();

            pname = "regions";
            regions = RegionPrunner.Regions.valueOf((String) params.get(pname));

        } catch(Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error parsing: " + pname + " val: " + params.get(pname));
        }
    }

    /**
     return bounds extended by given margin
     */
    static double[] extendBounds(double bounds[], double margin){
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

        BoxesX3DExporter exporter = new BoxesX3DExporter(handler, console,true);

        HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
        colors.put(new Integer(Grid.INSIDE), new float[] {1,0,0});
        colors.put(new Integer(Grid.OUTSIDE), new float[] {0,0,1});

        HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
        transparency.put(new Integer(Grid.INSIDE), new Float(0));
        transparency.put(new Integer(Grid.OUTSIDE), new Float(0.98));

        exporter.writeDebug(grid, colors, transparency);
        exporter.close();
    }

    public static <T extends Enum<T>> String[] enumToStringArray(T[] values) {
        int i = 0;
        String[] result = new String[values.length];
        for (T value: values) {
            result[i++] = value.name();
        }
        return result;
    }
}
