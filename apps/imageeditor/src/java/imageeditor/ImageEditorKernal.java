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

import abfab3d.geom.*;
import abfab3d.grid.*;
import abfab3d.io.output.BoxesX3DExporter;
import abfab3d.grid.op.*;
import abfab3d.creator.GeometryKernal;
import abfab3d.creator.shapeways.*;

import javax.vecmath.*;

//import java.awt.*;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.*;
import java.awt.geom.*;

/**
 * Geometry Kernal for the ImageEditor.
 *
 * Some images don't seem to work right, saving them with paint "fixes" this.  Not sure why.
 *    And example of this is the cat.png image.
 *
 * @author Alan Hudson
 */
public class ImageEditorKernal implements GeometryKernal {
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
     * @return The parameter names.
     */
    public List<String> getParams() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("resolution");
        list.add("bodyWidth");
        list.add("bodyHeight");
        list.add("bodyDepth");
        list.add("bodyGeometry");
        list.add("bodyImage");
        list.add("bodyImageInvert");
        list.add("bodyImageType");
        list.add("bailStyle");
        list.add("bailInnerRadius");
        list.add("bailOuterRadius");

        return list;
    }

    /**
     * Generate X3D binary geometry from the specificed parameters.
     *
     * @param params The parameters
     * @param os The stream to write out the file.
     */
    public void generate(Map<String,String> params, OutputStream os)
        throws IOException {

        parseParams(params);

        // Calculate maximum bounds

        // TODO: We should be able to accurately calculate this
        double max_width = bodyWidth * 1.2;
        double max_height = (bodyHeight + bailOuterRadius) * 1.2;
        double max_depth = 0;

        if (Math.abs(bodyImageDepth) > bodyDepth) {
            max_depth = Math.abs(bodyImageDepth) * 1.2;
        } else {
            max_depth = bodyDepth * 1.2;
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

        if (bigIndex) {
            grid = new ArrayGridByteIndexLong(voxelsX, voxelsY, voxelsZ, resolution, resolution);
        } else {
            grid = new ArrayGridByte(voxelsX, voxelsY, voxelsZ, resolution, resolution);
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

        if (geometry.equalsIgnoreCase("CUBE")) {
            bodyImageWidthPixels = bodyWidthPixels - 2 * bodyImageMarginWidthPixels;
            bodyImageHeightPixels = bodyHeightPixels - 2 * bodyImageMarginHeightPixels;

            picTxPixels = ((int) Math.ceil((body_cx - bodyWidth / 2.0) / resolution)) + bodyImageMarginWidthPixels;
            picTyPixels = ((int) Math.ceil((body_cy - bodyHeight / 2.0) / resolution)) + bodyImageMarginHeightPixels;
            picTzPixels = 0;   // Start outside
        } else if (geometry.equalsIgnoreCase("CYLINDER")) {
            double radius = bodyWidth / 2.0f;

            if (bodyImageType.equalsIgnoreCase("SQUARE")) {
                // This calc is for whole image
                bodyImageWidthPixels = (int) (2 * Math.floor((((radius - minWallThickness) * Math.sin(0.785398163))/resolution)));
                bodyImageHeightPixels = bodyImageWidthPixels;

                picTxPixels = (int) ((body_cx - ((radius - minWallThickness) * Math.sin(0.785398163))) / resolution);
                picTyPixels = (int) ((body_cy - ((radius - minWallThickness) * Math.sin(0.785398163))) / resolution);
                picTzPixels = 0;      // Ideally this would be just outside bodyDepth

            } else if (bodyImageType.equalsIgnoreCase("CIRCULAR")) {
                bodyImageWidthPixels = (int) (2 * Math.floor((((radius - minWallThickness))/resolution)));
                bodyImageHeightPixels = bodyImageWidthPixels;

                picTxPixels = (int) ((body_cx - ((radius - minWallThickness))) / resolution);
                picTyPixels = (int) ((body_cy - ((radius - minWallThickness))) / resolution);
                picTzPixels = 0;
            }
        }

        int threshold = 75;
        int bodyImageDepthPixels = (int) Math.ceil(bodyImageDepth / resolution);
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
                threshold, invert, (int) Math.abs(bodyImageDepthPixels), removeStray, mat);

            op.execute(grid2);

            if (bodyImageDepth > 0)
                op = new Subtract(grid2, 0, 0, 0, 1);
            else
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
    System.out.println("Putting into Octree");
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
            write(grid,"x3db", os, console);
        } catch(Exception e) {
            e.printStackTrace();
        }
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
System.out.println("width: " + metrics.stringWidth(text) + " x: " + x);
        int y = h - 80;

System.out.println("putting text at: " + x + " " + y);
        g.drawString(text, x, y);

        return cell_img;

    }

    /**
     * Parse and validate the parameters.  Issues an IllegalArgumentException on errors.
     *
     * @param params The parameters
     */
    private void parseParams(Map<String,String> params) {
        String pname = "resolution";
        String param = params.get(pname);

        try {
            if (param == null) {
                throw new IllegalArgumentException("Required param: " + pname + " not found");
            }
            resolution = Double.parseDouble(param);

            pname = "bodyWidth";
            param = params.get(pname);

            if (param == null) {
                throw new IllegalArgumentException("Required param: " + pname + " not found");
            }
            bodyWidth = Double.parseDouble(param);

            pname = "bodyHeight";
            param = params.get(pname);

            if (param == null) {
                throw new IllegalArgumentException("Required param: " + pname + " not found");
            }
            bodyHeight = Double.parseDouble(param);

            pname = "bodyDepth";
            param = params.get(pname);

            if (param == null) {
                throw new IllegalArgumentException("Required param: " + pname + " not found");
            }
            bodyDepth = Double.parseDouble(param);

            pname = "minWallThickness";
            param = params.get(pname);

            if (param == null) {
                throw new IllegalArgumentException("Required param: " + pname + " not found");
            }
            minWallThickness = Double.parseDouble(param);


            pname = "bodyImage";
            param = params.get(pname);

            if (param == null) {
                throw new IllegalArgumentException("Required param: " + pname + " not found");
            }
            filename = param;

            if (!filename.equals("NONE") && !new File(filename).exists()) {
                throw new IllegalArgumentException(pname + " not found: " + filename);
            }

            pname = "bodyGeometry";
            param = params.get(pname);

            if (param == null) {
                throw new IllegalArgumentException("Required param: " + pname + " not found");
            }

            geometry = param;
            if (!geometry.equalsIgnoreCase("CUBE") &&
                !geometry.equalsIgnoreCase("CYLINDER") &&
                !geometry.equalsIgnoreCase("NONE")) {

                throw new IllegalArgumentException("Unsupported: " + pname + " value: " + param);
            }

            pname = "bodyImageInvert";
            param = params.get(pname);

            if (param != null) {
                invert = Boolean.parseBoolean(param);
            }

            pname = "bodyImageType";
            param = params.get(pname);

            if (param == null) {
                throw new IllegalArgumentException("Required param: " + pname + " not found");
            }

            // TODO: We could autodetect this from the image
            bodyImageType = param;
            if (!bodyImageType.equalsIgnoreCase("SQUARE") &&
                !bodyImageType.equalsIgnoreCase("CIRCULAR")) {

                throw new IllegalArgumentException("Unsupported: " + pname + " value: " + param);
            }

            pname ="bailStyle";
            param = params.get(pname);
            bailStyle = param;
            if (!bailStyle.equalsIgnoreCase("TORUS") &&
                !bailStyle.equalsIgnoreCase("NONE")) {
                throw new IllegalArgumentException("Unsupported: " + pname + " value: " + param);
            }

            pname ="bailInnerRadius";
            param = params.get(pname);

            if (param == null) {
                throw new IllegalArgumentException("Required param: " + pname + " not found");
            }

            bailInnerRadius = Double.parseDouble(param);

            pname ="bailOuterRadius";
            param = params.get(pname);

            if (param == null) {
                throw new IllegalArgumentException("Required param: " + pname + " not found");
            }

            bailOuterRadius = Double.parseDouble(param);

            pname = "bodyImageDepth";
            param = params.get(pname);

            if (param == null) {
                throw new IllegalArgumentException("Required param: " + pname + " not found");
            }
            bodyImageDepth = Double.parseDouble(param);

        } catch(Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error parsing: " + pname + " value: " + param);
        }
    }

    private void write(Grid grid, String type, OutputStream os, ErrorReporter console) {
        // Output File
        BoxesX3DExporter exporter = new BoxesX3DExporter(type, os, console);

        exporter.write(grid, null);
        exporter.close();
    }

    private void writeDebug(Grid grid, String type, OutputStream os, ErrorReporter console) {
        // Output File

        BoxesX3DExporter exporter = new BoxesX3DExporter(type, os, console);

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