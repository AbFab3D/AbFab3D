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
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.vecmath.Matrix4d;
import java.awt.image.BufferedImage;

import abfab3d.grid.query.RegionFinder;
import abfab3d.io.output.*;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.LaplasianSmooth;
import abfab3d.mesh.MeshDecimator;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import org.j3d.geom.GeometryData;
import org.j3d.geom.*;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.sav.BinaryContentHandler;

import abfab3d.geom.*;
import abfab3d.grid.*;
import abfab3d.grid.op.*;
import abfab3d.creator.*;
import abfab3d.creator.shapeways.*;

//import java.awt.*;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;
import static java.lang.System.currentTimeMillis;

import app.common.GridSaver;

/**
 * Geometry Kernel for the ImageEditor.
 *
 * Some images don't seem to work right, saving them with paint "fixes" this.  Not sure why.
 *    And example of this is the cat.png image.
 *
 * @author Alan Hudson
 */
public class ImagePopperKernel extends HostedKernel {
    /** Debugging level.  0-5.  0 is none */
    private static final int DEBUG_LEVEL = 0;

    /** The horizontal and vertical resolution */
    private double resolution;

    /** The width of the body geometry */
    private double bodyWidth;

    /** The height of the body geometry */
    private double bodyHeight;

    /** The depth of the body geometry */
    private double bodyDepth;

    /** The image filename */
    private String filename;
    private String filename2;

    /** Should we invert the image */
    private boolean invert;

    /** The depth of body image */
    private double bodyImageDepth;
    private double bodyImageDepth2;

    /**
     * Get the parameters for this editor.
     *
     * @return The parameters.
     */
    public Map<String,Parameter> getParams() {
        HashMap<String,Parameter> params = new HashMap<String,Parameter>();

        int seq = 0;
        int step = 0;

        params.put("bodyImage", new Parameter("bodyImage", "Image Layer 1", "The image to use for the front body", "images/leaf/5.png", 1,
            Parameter.DataType.STRING, Parameter.EditorType.FILE_DIALOG,
            step, seq++, false, 0, 0.1, null, null)
        );
        params.put("bodyImage2", new Parameter("bodyImage2", "Image Layer 2", "The image to use for the front body", "NONE", 1,
                Parameter.DataType.STRING, Parameter.EditorType.FILE_DIALOG,
                step, seq++, false, 0, 0.1, null, null)
        );

        params.put("bodyImageInvert", new Parameter("bodyImageInvert", "Invert Image", "Should we use black for cutting", "true", 1,
            Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0, 0.1, null, null)
        );

        params.put("bodyImageType", new Parameter("bodyImageType", "Image Mapping Technique", "The type of image", "SQUARE", 1,
            Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
            step, seq++, true, 0, 0.1, null, new String[] {"SQUARE","CIRCULAR"})
        );

        params.put("bodyImageDepth", new Parameter("bodyImageDepth", "Depth Amount - Layer 1", "The depth of the image", "0.0013", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0, 1, null, null)
        );

        params.put("bodyImageDepth2", new Parameter("bodyImageDepth2", "Depth Amount - Layer 2", "The depth of the image", "0.0008", 1,
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
        params.put("resolution", new Parameter("resolution", "Resolution", "How accurate to model the object", "0.0001", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 0.1, null, null)
        );

        params.put("bodyWidth", new Parameter("bodyWidth", "Body Width", "The width of the main body", "0.055330948", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0.01, 1, null, null)
        );

        params.put("bodyHeight", new Parameter("bodyHeight", "Body Height", "The height of the main body", "0.04", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0.01, 1, null, null)
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

        // Calculate maximum bounds

        // TODO: We should be able to accurately calculate this
        double max_width = bodyWidth * 1.2;
        double max_height = bodyHeight * 1.3;
        double max_depth = (bodyImageDepth + bodyImageDepth2) * 1.2;

        // Setup Grid
        boolean bigIndex = false;
        int voxelsX = (int) Math.ceil(max_width / resolution);
        int voxelsY = (int) Math.ceil(max_height / resolution);
        int voxelsZ = (int) Math.ceil(max_depth / resolution);

        if ((long) voxelsX * voxelsY * voxelsZ > Math.pow(2,31)) {
            bigIndex = true;
        }

System.out.println("Voxels: " + voxelsX + " " + voxelsY + " " + voxelsZ);
        Grid grid = null;

        boolean useBlockBased = true;

        if (bigIndex) {
            grid = new ArrayAttributeGridByteIndexLong(voxelsX, voxelsY, voxelsZ, resolution, resolution);
        } else {
            if (useBlockBased) {
                grid = new BlockBasedAttributeGridByte(voxelsX, voxelsY, voxelsZ, resolution, resolution);
            } else {
                grid = new ArrayAttributeGridByte(voxelsX, voxelsY, voxelsZ, resolution, resolution);
            }
        }

        if (DEBUG_LEVEL > 0) grid = new RangeCheckWrapper(grid);

        double body_cx = max_width / 2.0;
        double body_cy = max_height / 2.0;
        double body_cz = max_depth / 2.0;
        int mat = 1;

        int bodyWidthPixels = (int) Math.ceil(bodyWidth / resolution);
        int bodyHeightPixels = (int) Math.ceil(bodyHeight / resolution);
        int bodyDepthPixels = (int) Math.ceil(bodyDepth / resolution);

        System.out.println("body pixels: " + bodyWidthPixels + " " + bodyHeightPixels + " depth: " + bodyDepthPixels);

        int bodyImageWidthPixels = 0;
        int bodyImageHeightPixels = 0;
        int picTxPixels = 0;
        int picTyPixels = 0;
        int picTzPixels = 0;
        int bodyImageDepthPixels = 0;
        int bodyImageDepthPixels2 = 0;

        bodyImageWidthPixels = bodyWidthPixels;
        bodyImageHeightPixels = bodyHeightPixels;

        picTxPixels = ((int) Math.ceil((body_cx - bodyWidth / 2.0) / resolution));
        picTyPixels = ((int) Math.ceil((body_cy - bodyHeight / 2.0) / resolution));
        picTzPixels = 0;   // Start outside
        bodyImageDepthPixels = (int) Math.ceil(bodyImageDepth / resolution);
        bodyImageDepthPixels2 = (int) Math.ceil(bodyImageDepth2 / resolution);

        int threshold = 240;

        boolean removeStray = true;

        Grid grid2 = null;
        Grid grid3 = null;
        Operation op = null;


        if (!filename.equalsIgnoreCase("NONE")) {
System.out.println("read file: " + filename);
            BufferedImage image = ImageIO.read(new File(filename));

            grid2 = grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(),
                grid.getVoxelSize(), grid.getSliceHeight());


            if (DEBUG_LEVEL > 0) grid2 = new RangeCheckWrapper(grid2);

    System.out.println("image tx: " + picTxPixels + " " + picTyPixels + " " + picTzPixels);
    System.out.println("image size: " + bodyImageWidthPixels + " " + bodyImageHeightPixels + " " + ((int) Math.abs(bodyImageDepthPixels)));

            // silver used 55
            picTzPixels = 1;
            op = new ApplyImage(image,picTxPixels,picTyPixels,picTzPixels,bodyImageWidthPixels, bodyImageHeightPixels,
                threshold, invert, bodyImageDepthPixels, removeStray, mat);

            op.execute(grid2);

            op = new Union(grid2, 0, 0, 0, 1);

            op.execute(grid);
        }

        if (!filename2.equalsIgnoreCase("NONE")) {
            System.out.println("read file: " + filename2);
            BufferedImage image = ImageIO.read(new File(filename2));

            grid2 = grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(),
                    grid.getVoxelSize(), grid.getSliceHeight());


            if (DEBUG_LEVEL > 0) grid2 = new RangeCheckWrapper(grid2);

            picTzPixels += bodyImageDepthPixels;

            System.out.println("image2 tx: " + picTxPixels + " " + picTyPixels + " " + picTzPixels);
            System.out.println("image2 size: " + bodyImageWidthPixels + " " + bodyImageHeightPixels + " " + ((int) Math.abs(bodyImageDepthPixels)));


            op = new ApplyImage(image,picTxPixels,picTyPixels,picTzPixels,bodyImageWidthPixels, bodyImageHeightPixels,
                    threshold, invert, bodyImageDepthPixels2, false, mat);

            op.execute(grid2);

            op = new Union(grid2, 0, 0, 0, 1);

            op.execute(grid);
        }

        System.out.println("Finding Regions: ");
        // Remove all but the largest region
        RegionFinder finder = new RegionFinder();
        List<Region> regions = finder.execute(grid);
        Region largest = regions.get(0);

        System.out.println("Regions: " + regions.size());
        for(Region r : regions) {
            if (r.getVolume() > largest.getVolume()) {
                largest = r;
            }
            //System.out.println("Region: " + r.getVolume());
        }

        System.out.println("Largest Region: " + largest);
        RegionClearer clearer = new RegionClearer(grid);
        System.out.println("Clearing regions: ");
        for(Region r : regions) {
            if (r != largest) {
                //System.out.println("   Region: " + r.getVolume());
                r.traverse(clearer);
            }
        }

/*
if (1==1) {
        BufferedImage image = createImage(bodyWidthPixels, bodyHeightPixels, "AbFab3D", "Pump Demi Bold LET", Font.BOLD, -1);

        Operation op = new ApplyImage(image,0,0,0,bodyImageWidthPixels, bodyImageHeightPixels, threshold, invert, bodyImageDepth, removeStray, mat);
        op.execute(grid);

        op = new Union(grid, 0, 0, 0, 1);

        op.execute(grid);
}
*/

        System.out.println("Writing grid");

        try {
            ErrorReporter console = new PlainTextErrorReporter();
            //writeDebug(grid, handler, console);
            //write(grid, handler, console);
            GridSaver.writeIsosurfaceMakerSTL("out.stl", grid,3, 1e-9);
//            writeIsosurfaceMaker(grid,3, 0);
        } catch(Exception e) {
            e.printStackTrace();

            return new KernelResults(false, KernelResults.INTERNAL_ERROR, "Failed Writing Grid", null, null);
        }

        double[] min_bounds = new double[3];
        double[] max_bounds = new double[3];
        grid.getWorldCoords(0,0,0, min_bounds);
        grid.getWorldCoords(grid.getWidth() - 1, grid.getHeight() - 1, grid.getDepth() - 1, max_bounds);


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

            pname = "bodyWidth";
            bodyWidth = ((Double) params.get(pname)).doubleValue();

            pname = "bodyHeight";
            bodyHeight = ((Double) params.get(pname)).doubleValue();

            pname = "bodyImage";
            filename = (String) params.get(pname);

            pname = "bodyImage2";
            filename2 = (String) params.get(pname);

            pname = "bodyImageInvert";
            invert = ((Boolean) params.get(pname)).booleanValue();

            pname = "bodyImageDepth";
            bodyImageDepth = ((Double) params.get(pname)).doubleValue();

            pname = "bodyImageDepth2";
            bodyImageDepth2 = ((Double) params.get(pname)).doubleValue();

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
        colors.put(new Integer(Grid.INTERIOR), new float[] {1,0,0});
        colors.put(new Integer(Grid.EXTERIOR), new float[]{0, 1, 0});
        colors.put(new Integer(Grid.OUTSIDE), new float[] {0,0,1});

        HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
        transparency.put(new Integer(Grid.INTERIOR), new Float(0));
        transparency.put(new Integer(Grid.EXTERIOR), new Float(0.5));
        transparency.put(new Integer(Grid.OUTSIDE), new Float(0.98));

        exporter.writeDebug(grid, colors, transparency);
        exporter.close();
    }

}

class RegionClearer implements RegionTraverser {
    private Grid grid;

    public RegionClearer(Grid grid) {
        this.grid = grid;
    }
    @Override
    public void found(int x, int y, int z) {
        grid.setState(x,y,z,Grid.OUTSIDE);
    }

    @Override
    public boolean foundInterruptible(int x, int y, int z) {
        grid.setState(x,y,z,Grid.OUTSIDE);

        return true;
    }
}
