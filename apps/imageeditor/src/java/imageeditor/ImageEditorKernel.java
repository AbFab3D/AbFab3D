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

package imageeditor;

// External Imports
import java.io.*;
import java.util.*;
import javax.imageio.*;
import java.awt.image.BufferedImage;
import org.j3d.geom.GeometryData;
import org.j3d.geom.*;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.sav.BinaryContentHandler;

import abfab3d.geom.*;
import abfab3d.grid.*;
import abfab3d.io.output.BoxesX3DExporter;
import abfab3d.io.output.RegionsX3DExporter;
import abfab3d.grid.op.*;
import abfab3d.creator.*;
import abfab3d.creator.shapeways.*;

import javax.vecmath.*;

//import java.awt.*;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.*;
import java.awt.geom.*;

/**
 * Geometry Kernel for the ImageEditor.
 *
 * Some images don't seem to work right, saving them with paint "fixes" this.  Not sure why.
 *    And example of this is the cat.png image.
 *
 * @author Alan Hudson
 */
public class ImageEditorKernel implements GeometryKernel {
    /** Debugging level.  0-5.  0 is none */
    private static final int DEBUG_LEVEL = 5;

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

    /** The base geometry type: CUBE, CYLINDER */
    private String geometry;

    /** Should we invert the image */
    private boolean invert;

    /** The depth of body image */
    private double bodyImageDepth;

    /** The body image style:  EMBOSSED, ENGRAVED */
    private String bodyImageStyle;

    /** The body image type:  SQUARE, CIRCULAR */
    private String bodyImageType;

    private double minWallThickness;

    /** The bail style */
    private String bailStyle;

    /** The bail inner radius */
    private double bailInnerRadius;

    /** The bail outer radius */
    private double bailOuterRadius;

    /**
     * Get the parameters for this editor.
     *
     * @return The parameters.
     */
    public Map<String,Parameter> getParams() {
        HashMap<String,Parameter> params = new HashMap<String,Parameter>();

        int seq = 0;
        int step = 0;

        params.put("bodyImage", new Parameter("bodyImage", "Body Image", "The image to use for the front body", "images/cat.png", 1,
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

        params.put("bodyImageStyle", new Parameter("bodyImageStyle", "Depth Technique", "The image operation", "ENGRAVED", 1,
            Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
            step, seq++, false, -1, 1, null, new String[] {"ENGRAVED","EMBOSSED"})
        );

        params.put("bodyImageDepth", new Parameter("bodyImageDepth", "Depth Amount", "The depth of the image", "0.0042", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0, 1, null, null)
        );

        step++;
        seq = 0;

        params.put("resolution", new Parameter("resolution", "Resolution", "How accurate to model the object", "0.00018", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, true, 0, 0.1, null, null)
        );

        params.put("minWallThickness", new Parameter("minWallThickness", "Minimum WallThickness", "The minimum wallthickness", "0.003", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, true, 0, 1, null, null)
        );

        params.put("bodyWidth", new Parameter("bodyWidth", "Body Width", "The width of the main body", "0.025", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0.03, 1, null, null)
        );

        params.put("bodyHeight", new Parameter("bodyHeight", "Body Height", "The height of the main body", "0.04", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0.03, 1, null, null)
        );

        params.put("bodyDepth", new Parameter("bodyDepth", "Body Depth", "The depth of the main body", "0.0032", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0, 0.1, null, null)
        );

        params.put("bodyGeometry", new Parameter("bodyGeometry", "Body Shape", "The geometry to use for the body", "CUBE", 1,
            Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0, 0.1, null, new String[] {"CUBE","CYLINDER","NONE"})
        );

        step++;
        seq = 0;

        params.put("bailStyle", new Parameter("bailStyle", "Connector Style", "The connector(bail) to use", "TORUS", 1,
            Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0, 0.1, null, new String[] {"TORUS","NONE"})
        );

        params.put("bailInnerRadius", new Parameter("bailInnerRadius", "Connector Inner Radius", "The inner radius of the bail", "0.001", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0, 0.01, null, null)
        );

        params.put("bailOuterRadius", new Parameter("bailOuterRadius", "Connector Outer Radius", "The outer radius of the bail", "0.004", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0, 0.01, null, null)
        );

        return params;
    }

    /**
     * @param params The parameters
     * @param accuracy The accuracy to generate the model
     * @param handler The X3D content handler to use
     */
    public KernelResults generate(Map<String,Object> params, Accuracy acc, BinaryContentHandler handler) throws IOException {

        pullParams(params);

        if (geometry.equalsIgnoreCase("CYLINDER")) {

            if (bodyWidth > bodyHeight) {
                bodyHeight = bodyWidth;
            } else {
                bodyWidth = bodyHeight;
            }
        }

        // Calculate maximum bounds

        // TODO: We should be able to accurately calculate this
        double max_width = bodyWidth * 1.2;
        double max_height = (bodyHeight + bailOuterRadius) * 1.2;
        double max_depth = 0;

        if (bodyImageStyle.equalsIgnoreCase("EMBOSSED")) {
            max_depth = 2 * bodyImageDepth + bodyDepth;
        } else {
            if (Math.abs(bodyImageDepth) > bodyDepth) {
                max_depth = Math.abs(bodyImageDepth) * 1.2;
            } else {
                max_depth = bodyDepth * 1.2;
            }
        }

        if (geometry.equalsIgnoreCase("CYLINDER")) {
            double max = max_width;
            if (max_height > max)
                max = max_height;
            if (max_depth > max)
                max = max_depth;

            // Make dimensions as large as largest dimension to allow for rotation
            max_width = max;
            max_height = max;
            max_depth = max;
        }

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

        boolean useBlockBased = false;

        if (bigIndex) {
            grid = new ArrayGridByteIndexLong(voxelsX, voxelsY, voxelsZ, resolution, resolution);
        } else {
            if (useBlockBased) {
                grid = new BlockBasedGridByte(voxelsX, voxelsY, voxelsZ, resolution, resolution);
            } else {
                grid = new ArrayGridByte(voxelsX, voxelsY, voxelsZ, resolution, resolution);
            }
        }

        if (DEBUG_LEVEL > 0) grid = new RangeCheckWrapper(grid);

        // Create Base
        /*
        double tx = bodyWidth / 2.0;
        double ty = bodyHeight / 2.0;
        double tz = bodyDepth / 2.0;
        */

        double body_cx = max_width / 2.0;
        double body_cy = max_height / 2.0;
        double body_cz = max_depth / 2.0;
        int mat = 1;

        if (geometry.equalsIgnoreCase("CUBE")) {
            createCube(grid, body_cx, body_cy, body_cz, bodyWidth, bodyHeight, bodyDepth,mat);
        } else if (geometry.equalsIgnoreCase("CYLINDER")) {
System.out.println("Creating cylinder: " + body_cx + " " + body_cy + " " + body_cz);

            // TODO: Remove me
            grid = new RangeCheckWrapper(grid);

            createCylinder(grid, body_cx, body_cy, body_cz, 1, 0, 0, 1.57075,
                                   bodyDepth, bodyWidth / 2.0f, 64,mat);


/*
            createCylinder(grid, tx, ty, tz, 1, 0, 0, 0,
                                   bodyHeight, bodyWidth / 2.0f, 64,mat);


            Matrix4d tmatrix = TransformPosition.createMatrix(new double[] {tx,ty,tz},
                new double[] {1,1,1}, new double[] {1,0,0,1.57075}, new double[] {0,0,0},
                new double[] {0,0,1,0});


            Operation op = new TransformPosition(tmatrix);
            grid = op.execute(grid);
*/
        }

        int bodyWidthPixels = (int) Math.ceil(bodyWidth / resolution);
        int bodyHeightPixels = (int) Math.ceil(bodyHeight / resolution);
        int bodyDepthPixels = (int) Math.ceil(bodyDepth / resolution);

        System.out.println("body pixels: " + bodyWidthPixels + " " + bodyHeightPixels + " depth: " + bodyDepthPixels);
        // Insure minimum wallThickness achieved
        int bodyImageMarginWidthPixels = (int) Math.ceil(minWallThickness / resolution);
        int bodyImageMarginHeightPixels = (int) Math.ceil(minWallThickness / resolution);

System.out.println("marginWidth: " + bodyImageMarginWidthPixels);

        int bodyImageWidthPixels = 0;
        int bodyImageHeightPixels = 0;
        int picTxPixels = 0;
        int picTyPixels = 0;
        int picTzPixels = 0;
        int bodyImageDepthPixels = 0;

        if (geometry.equalsIgnoreCase("CUBE")) {
            bodyImageWidthPixels = bodyWidthPixels - 2 * bodyImageMarginWidthPixels;
            bodyImageHeightPixels = bodyHeightPixels - 2 * bodyImageMarginHeightPixels;

            picTxPixels = ((int) Math.ceil((body_cx - bodyWidth / 2.0) / resolution)) + bodyImageMarginWidthPixels;
            picTyPixels = ((int) Math.ceil((body_cy - bodyHeight / 2.0) / resolution)) + bodyImageMarginHeightPixels;

            if (bodyImageStyle.equalsIgnoreCase("EMBOSSED")) {
                picTzPixels = (int) ((body_cz + bodyDepth / 2.0) / resolution);
                bodyImageDepthPixels = (int) Math.ceil(bodyImageDepth / resolution);
            } else {
                picTzPixels = 0;   // Start outside
                bodyImageDepthPixels = (int) Math.ceil(max_depth / resolution);
            }
    System.out.println("cube image tx: " + picTxPixels + " " + picTyPixels + " " + picTzPixels);

        } else if (geometry.equalsIgnoreCase("CYLINDER")) {
            double radius = bodyWidth / 2.0;

            if (bodyImageType.equalsIgnoreCase("SQUARE")) {
                // This calc is for whole image
                bodyImageWidthPixels = (int) (2 * Math.floor((((radius - minWallThickness) * Math.sin(0.785398163))/resolution)));
                bodyImageHeightPixels = bodyImageWidthPixels;

                picTxPixels = (int) ((body_cx - ((radius - minWallThickness) * Math.sin(0.785398163))) / resolution);
                picTyPixels = (int) ((body_cy - ((radius - minWallThickness) * Math.sin(0.785398163))) / resolution);

                if (bodyImageStyle.equalsIgnoreCase("EMBOSSED")) {
                    picTzPixels = (int) ((body_cz + bodyDepth / 2.0) / resolution);
                    bodyImageDepthPixels = (int) Math.ceil(bodyImageDepth / resolution);
                } else {
                    picTzPixels = 0;   // Start outside
                    bodyImageDepthPixels = (int) Math.ceil(max_depth / resolution);
                }
            } else if (bodyImageType.equalsIgnoreCase("CIRCULAR")) {
                bodyImageWidthPixels = (int) (2 * Math.floor((((radius - minWallThickness))/resolution)));
                bodyImageHeightPixels = bodyImageWidthPixels;

                picTxPixels = (int) ((body_cx - ((radius - minWallThickness))) / resolution);
                picTyPixels = (int) ((body_cy - ((radius - minWallThickness))) / resolution);

                if (bodyImageStyle.equalsIgnoreCase("EMBOSSED")) {
                    picTzPixels = (int) ((body_cz + bodyDepth / 2.0) / resolution);
                    bodyImageDepthPixels = (int) Math.ceil(bodyImageDepth / resolution);
                } else {
                    picTzPixels = 0;   // Start outside

                    // Desired code
                    bodyImageDepthPixels = (int) Math.ceil(max_depth / resolution);
                }
            }
        } else if (geometry.equalsIgnoreCase("NONE")) {
            bodyImageWidthPixels = bodyWidthPixels;
            bodyImageHeightPixels = bodyHeightPixels;

            picTxPixels = ((int) Math.ceil((body_cx - bodyWidth / 2.0) / resolution));
            picTyPixels = ((int) Math.ceil((body_cy - bodyHeight / 2.0) / resolution));
            picTzPixels = 0;   // Start outside
            bodyImageDepthPixels = (int) Math.ceil(bodyImageDepth / resolution);

            bodyImageStyle = "EMBOSSED";
        }


        int threshold = 75;

        boolean removeStray = true;

        Grid grid2 = null;
        Grid grid3 = null;
        Operation op = null;

        if (!filename.equalsIgnoreCase("NONE")) {
            BufferedImage image = ImageIO.read(new File(filename));

            grid2 = grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(),
                grid.getVoxelSize(), grid.getSliceHeight());


            if (DEBUG_LEVEL > 0) grid2 = new RangeCheckWrapper(grid2);

    System.out.println("image tx: " + picTxPixels + " " + picTyPixels + " " + picTzPixels);
    System.out.println("image size: " + bodyImageWidthPixels + " " + bodyImageHeightPixels + " " + ((int) Math.abs(bodyImageDepthPixels)));
    // TODO: Not sure why this needs to be 2X
    //picTx = picTx*2;
    //picTy = picTy*2;

            op = new ApplyImage(image,picTxPixels,picTyPixels,picTzPixels,bodyImageWidthPixels, bodyImageHeightPixels,
                threshold, invert, bodyImageDepthPixels, removeStray, mat);

            op.execute(grid2);

            if (bodyImageStyle.equalsIgnoreCase("ENGRAVED")) {
                op = new Subtract(grid2, 0, 0, 0, 1);
            } else
                op = new Union(grid2, 0, 0, 0, 1);

            op.execute(grid);
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

        if (bailStyle.equalsIgnoreCase("TORUS")) {
            double bail_tx = bodyWidth / 2.0f;
            double bail_ty = bodyHeight;
            double bail_tz = bodyDepth / 2.0f;

System.out.println("Old Creating bail: " + bail_tx + " " + bail_ty + " " + bail_tz);

            bail_tx = body_cx;
            bail_ty = body_cy + bodyHeight / 2.0;
            bail_tz = body_cz;

System.out.println("New Creating bail: " + bail_tx + " " + bail_ty + " " + bail_tz);

            grid2 = grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(),
                grid.getVoxelSize(), grid.getSliceHeight());

            if (DEBUG_LEVEL > 0) grid2 = new RangeCheckWrapper(grid2);

            createTorus(grid2, bail_tx,bail_ty,bail_tz,1,0,0,1.5707f, bailInnerRadius, bailOuterRadius, 64, mat, true);

            grid3 = grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(),
                grid.getVoxelSize(), grid.getSliceHeight());


            if (geometry.equalsIgnoreCase("CYLINDER")) {
                // Fudged for Cylinder
                createCube(grid3, bail_tx, bail_ty - (bailOuterRadius + bailInnerRadius) / 2.0 - bailInnerRadius, bail_tz, (bailOuterRadius + bailInnerRadius) * 2, (bailOuterRadius + bailInnerRadius), bodyDepth,mat);
            } else {
                // Real math
                createCube(grid3, bail_tx, bail_ty - (bailOuterRadius + bailInnerRadius) / 2.0, bail_tz, (bailOuterRadius + bailInnerRadius) * 2, (bailOuterRadius + bailInnerRadius), bodyDepth,mat);
            }

            op = new Subtract(grid3, 0, 0, 0, 1);
            //op = new Union(grid3, 0, 0, 0, 1);
            op.execute(grid2);

            op = new Union(grid2, 0, 0, 0, 1);

            op.execute(grid);
        }

if (1==0) {
    // Sadly NetFabb doesn't like my Octree Output
    System.out.println("***Putting into Octree");
    grid2 = new OctreeGridByte(grid.getWidth(), grid.getHeight(), grid.getDepth(),
            grid.getVoxelSize(), grid.getSliceHeight());
    Operation op2 = new Copy(grid2, 0,0,0);
    op2.execute(grid);
    grid = grid2;
}
        System.out.println("Writing grid");

        try {
            ErrorReporter console = new PlainTextErrorReporter();
            //writeDebug(grid, "x3db", fos, console);
            write(grid, handler, console);
        } catch(Exception e) {
            e.printStackTrace();

            return new KernelResults(false, KernelResults.INTERNAL_ERROR, "Failed Writing Grid");
        }

        double[] min_bounds = new double[3];
        double[] max_bounds = new double[3];
        grid.getWorldCoords(0,0,0, min_bounds);
        grid.getWorldCoords(grid.getWidth() - 1, grid.getHeight() - 1, grid.getDepth() - 1, max_bounds);

        return new KernelResults(true, min_bounds, max_bounds);
    }

    /**
     * Create an image from a text string.
     *
     * @param w The width of the textual space
     * @param h The height of the textual space
     * @param text The text string
     * @param font The font to use
     * @param style The font style to use
     * @param size The font size to use, or -1 till fill the space
     */
    private BufferedImage createImage(int w, int h, String text, String font, int style, int size) {
        Font f = null;
        FontMetrics metrics = null;

        BufferedImage cell_img =
            new BufferedImage(w, h,
                              BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D g = (Graphics2D)cell_img.getGraphics();

        if (size <= 0) {
            size = 10;
            f = new Font(font, style, size);
            g.setFont(f);

            metrics = g.getFontMetrics();
            int fs = metrics.stringWidth(text);

            while(fs < w) {
                size++;
                f = new Font(font, style, size);
                g.setFont(f);

                metrics = g.getFontMetrics();
                fs = metrics.stringWidth(text);
            }

            size--;
            f = new Font(font, style, size);
            g.setFont(f);
            metrics = g.getFontMetrics();
            fs = metrics.stringWidth(text);
            System.out.println("final size: " + fs);

        } else {
            g.setFont(f);
        }



//        int x = w - metrics.stringWidth(text)/2;
        //int x = metrics.stringWidth(text)/2;
        int x = 0;

        int y = h - 80;

        g.drawString(text, x, y);

        return cell_img;

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

            pname = "minWallThickness";
            minWallThickness = ((Double) params.get(pname)).doubleValue();

            pname = "bodyWidth";
            bodyWidth = ((Double) params.get(pname)).doubleValue();

            pname = "bodyHeight";
            bodyHeight = ((Double) params.get(pname)).doubleValue();

            pname = "bodyDepth";
            bodyDepth = ((Double) params.get(pname)).doubleValue();

            pname = "bodyImage";
            filename = (String) params.get(pname);

            pname = "bodyGeometry";
            geometry = (String) params.get(pname);

            pname = "bodyImageInvert";
            invert = ((Boolean) params.get(pname)).booleanValue();

            pname = "bodyImageType";
            bodyImageType = (String) params.get(pname);

            pname = "bodyImageStyle";
            bodyImageStyle = (String) params.get(pname);

            pname ="bailStyle";
            bailStyle = (String) params.get(pname);

            pname ="bailInnerRadius";
            bailInnerRadius = ((Double) params.get(pname)).doubleValue();

            pname ="bailOuterRadius";
            bailOuterRadius = ((Double) params.get(pname)).doubleValue();

            pname = "bodyImageDepth";
            bodyImageDepth = ((Double) params.get(pname)).doubleValue();

        } catch(Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error parsing: " + pname + " val: " + params.get(pname));
        }
    }

    private void write(Grid grid, BinaryContentHandler handler, ErrorReporter console) {

        // Output File
        //BoxesX3DExporter exporter = new BoxesX3DExporter(type, os, console);
System.out.println("Creating Regions Exporter");
        RegionsX3DExporter exporter = new RegionsX3DExporter(handler, console, true);
        float[] mat_color = new float[] {0.8f,0.8f,0.8f,0};
        HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
        colors.put(new Integer(1), mat_color);

        exporter.write(grid, colors);
        exporter.close();
    }

    private void writeDebug(Grid grid, BinaryContentHandler handler, ErrorReporter console) {
        // Output File

        BoxesX3DExporter exporter = new BoxesX3DExporter(handler, console,true);

        HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
        colors.put(new Integer(Grid.INTERIOR), new float[] {1,0,0});
        colors.put(new Integer(Grid.EXTERIOR), new float[] {0,1,0});
        colors.put(new Integer(Grid.OUTSIDE), new float[] {0,0,1});

        HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
        transparency.put(new Integer(Grid.INTERIOR), new Float(0));
        transparency.put(new Integer(Grid.EXTERIOR), new Float(0.5));
        transparency.put(new Integer(Grid.OUTSIDE), new Float(0.98));

        exporter.writeDebug(grid, colors, transparency);
        exporter.close();
    }

    /**
     * Create a cube.
     */
    private void createCube(Grid grid, double tx, double ty, double tz, double w, double h, double d, int mat) {
        // Create the base structure

        CubeCreator cg = null;
        cg = new CubeCreator(null, w, h, d, tx,ty,tz,mat);
        cg.generate(grid);

    }

    /**
     * Create a cylinder.
     */
    private void createCylinder(Grid grid, double tx, double ty, double tz,
        double rx, double ry, double rz, double ra,
        double height, double radius, int facets, int mat) {

        CylinderCreator cg = null;
        cg = new CylinderCreator(height, radius, tx,ty,tz,rx,ry,rz,ra,mat);
        cg.generate(grid);
    }

    /**
     * Create a torus.
     */
    private void createTorus(Grid grid, double tx, double ty, double tz,
        double rx, double ry, double rz, double ra,
        double ir, double or, int facets, int mat, boolean filled) {

System.out.println("createTorus: " + ir + " or: " + or);
        TorusGenerator tg = new TorusGenerator((float)ir, (float)or, facets, facets);
        GeometryData geom = new GeometryData();
        geom.geometryType = GeometryData.TRIANGLES;
        tg.generate(geom);

        TriangleModelCreator tmc = new TriangleModelCreator(geom,tx,ty,tz,
            rx,ry,rz,ra,mat,mat,filled);

        tmc.generate(grid);
    }


}