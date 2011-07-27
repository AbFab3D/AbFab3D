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

package lettering;

// External Imports
import java.io.*;
import java.util.HashMap;
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

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

// Internal Imports

/**
 * Example of using Java Fonts to create solid lettering
 *
 * @author Alan Hudson
 */
public class Lettering {
    /**  Resolution of the printer in meters.  */
    public static final double RESOLUTION = 0.000025;
//    public static final double RESOLUTION = 0.0004;

    public Lettering() {
    }

    public void generate(String filename) {
        double resolution = RESOLUTION;

        double bodyWidth = 0.1;
        double bodyHeight = 0.05;
        double bodyDepth = 0.0025;

        // Calculate maximum bounds

        double max_width = bodyWidth * 1.2;
        double max_height = bodyHeight * 1.2;
        double max_depth = bodyDepth * 1.2;

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


        // Create Base
        double tx = bodyWidth / 2.0;
        double ty = bodyHeight / 2.0;
        double tz = bodyDepth / 2.0;
        int mat = 1;

        int bodyWidthPixels = (int) Math.ceil(bodyWidth / resolution);
        int bodyHeightPixels = (int) Math.ceil(bodyHeight / resolution);
        int bodyDepthPixels = (int) Math.ceil(bodyDepth / resolution);

        System.out.println("body pixels: " + bodyWidthPixels + " " + bodyHeightPixels + " depth: " + bodyDepthPixels);
        int bodyImageWidthPixels = (int) Math.floor((bodyWidth * 0.99) / resolution);
        int bodyImageHeightPixels = (int) Math.floor((bodyHeight * 0.99) / resolution);
        int picTx = (int) ((bodyWidthPixels - bodyImageWidthPixels) / 2.0f);
        int picTy = (int) ((bodyHeightPixels - bodyImageHeightPixels) / 2.0f);
        int picTz = (int) ((bodyDepth / 2.0 / resolution));

        picTx = 0;
        picTy = 0;
        picTz = 0;

        int threshold = 75;
        int bodyImageDepth = bodyDepthPixels;
        //int bodyImageDepth = -picTz;
        boolean invert = false;
        boolean removeStray = false;


if (1==1) {
        BufferedImage image = createImage(bodyWidthPixels, bodyHeightPixels, "AbFab3D", "Pump Demi Bold LET", Font.BOLD, -1);

        Operation op = new ApplyImage(image,0,0,0,bodyImageWidthPixels, bodyImageHeightPixels, threshold, invert, bodyImageDepth, removeStray, mat);
        op.execute(grid);

        op = new Union(grid, 0, 0, 0, 1);

        op.execute(grid);
}

if (1==1) {
    System.out.println("Putting into Octree");
    Grid grid2 = new OctreeGridByte(grid.getWidth(), grid.getHeight(), grid.getDepth(),
            grid.getVoxelSize(), grid.getSliceHeight());
    Operation op2 = new Copy(grid2, 0,0,0);
    op2.execute(grid);
    grid = grid2;
}
        System.out.println("Writing grid");

        try {
            FileOutputStream fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".")+1);

            ErrorReporter console = new PlainTextErrorReporter();
            //writeDebug(grid, "x3db", fos, console);
            write(grid,"x3db", fos, console);
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

    public static void main(String[] args) {
        Lettering c = new Lettering();
        c.generate("out.x3db");
    }
}