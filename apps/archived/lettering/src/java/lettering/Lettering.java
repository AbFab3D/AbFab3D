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
import java.awt.image.BufferedImage;

import abfab3d.core.Grid;
import abfab3d.core.VoxelClasses;
import abfab3d.io.input.IndexedTriangleSetLoader;
import abfab3d.io.output.*;
import org.j3d.geom.GeometryData;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;

import abfab3d.geom.*;
import abfab3d.grid.*;
import abfab3d.grid.op.*;

import java.awt.*;

// Internal Imports

/**
 * Example of using Java Fonts to create solid lettering
 *
 * @author Alan Hudson
 */
public class Lettering {
    public static final double SCALE = 1;

    public static final int EXT_MAT = 1;
    public static final int INT_MAT = 1;

    // Input model is in mm's

    /**  Resolution of the printer in mm's.  */
    public static final double RESOLUTION = 0.15 * SCALE;

    public Lettering() {
    }

    public void generate(String base, String filename) {
        double resolution = RESOLUTION;

        double bodyWidth = 62 * SCALE;
        double bodyHeight = 120 * SCALE;
        double bodyDepth = 15 * SCALE;

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

System.out.println("Main Voxels: " + voxelsX + " " + voxelsY + " " + voxelsZ);
        Grid grid = null;

        if (bigIndex) {
            grid = new ArrayAttributeGridByteIndexLong(voxelsX, voxelsY, voxelsZ, resolution, resolution);
        } else {
            grid = new ArrayAttributeGridByte(voxelsX, voxelsY, voxelsZ, resolution, resolution);
        }

        // TODO: Remove me, debug
        grid = new RangeCheckWrapper(grid, true);

        long start = System.currentTimeMillis();
        loadFile(grid, base, SCALE);
        System.out.println("load time: " + (System.currentTimeMillis() - start));

        String text = "12345678 2:19";

        double labelWidth = 61 * SCALE;
        double labelHeight = 8 * SCALE;
//        double labelDepth = 0.5;
        double labelDepth = 1 * SCALE;

        int labelWidthPixels = (int) Math.ceil(labelWidth / resolution);
        int labelHeightPixels = (int) Math.ceil(labelHeight / resolution);
        int labelDepthPixels = (int) Math.ceil(labelDepth / resolution);

        System.out.println("label pixels: " + labelWidthPixels + " " + labelHeightPixels + " " + labelDepthPixels);


        int threshold = 75;
        boolean invert = false;
        boolean removeStray = false;


        if (1==0) {
            BufferedImage image = createImage(labelWidthPixels, labelHeightPixels, text, "Pump Demi Bold LET", Font.BOLD, -1);
/*
            int picTx = (int) (labelWidthPixels / 2.0);
            int picTy = (int) labelWidthPixels;
            int picTz = 0;
*/
//            int picTx = 20;
            int picTx = (int) (80 * SCALE);     // Y in base world
            int picTy = (int) (15 * SCALE);     // Z in base world
            int picTz = (int) (25 * SCALE);      // X in base world

            // Make height double so we can rotate.
/*
            Grid grid2 = grid.createEmpty(labelWidthPixels * 2, labelWidthPixels * 2, labelDepthPixels * 2,
                grid.getVoxelSize(), grid.getSliceHeight());
 */
            Grid grid2 = grid.createEmpty(grid.getWidth(), grid.getHeight(), grid.getDepth(),
                    grid.getVoxelSize(), grid.getSliceHeight());

            System.out.println("Creating image grid: " + grid2.getWidth() + " " + grid2.getHeight() + " " + grid.getDepth());
            System.out.println("image tx: " + picTx + " " + picTy + " " + picTz);
            // TODO: remove me
            grid2 = new RangeCheckWrapper(grid2, false);

            Operation op = new ApplyImage(image,picTx,picTy,picTz,
                    HalfAxis.Z_POSITIVE,HalfAxis.X_POSITIVE,HalfAxis.Y_POSITIVE,
                    labelWidthPixels, labelHeightPixels, threshold, invert, labelDepthPixels, removeStray, EXT_MAT);
            op.execute(grid2);

            //debugGrid(grid2, "text1.x3db");
            op = new Subtract(grid2, 0, 0, 0, 1);
            //op = new Union(grid2, 0, 0, 0, 1);

            op.execute(grid);

        }

if (1==0) {
    System.out.println("Putting into Octree");
    Grid grid2 = new OctreeAttributeGridByte(grid.getWidth(), grid.getHeight(), grid.getDepth(),
            grid.getVoxelSize(), grid.getSliceHeight());
    Operation op2 = new Copy(grid, 0,0,0);
    grid2 = op2.execute(grid2);
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

    private void debugGrid(Grid grid, String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".")+1);

            ErrorReporter console = new PlainTextErrorReporter();
            writeDebug(grid, "x3db", fos, console);
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
            System.out.println("final font size: " + fs);

        } else {
            g.setFont(f);
        }



//        int x = w - metrics.stringWidth(text)/2;
        //int x = metrics.stringWidth(text)/2;
        int x = 0;
System.out.println("width: " + metrics.stringWidth(text) + " x: " + x);
//        int y = h - 80;       // TODO: what's this number mean?
        int y = h;       // TODO: what's this number mean?

System.out.println("putting text at: " + x + " " + y);
        g.drawString(text, x, y);

        return cell_img;

    }

    /**
     *  Load a 3D file into the grid
     *
     * @param file
     */
    private void loadFile(Grid grid, String file, double scale) {
        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);
        loader.processFile(new File(file));

        GeometryData geom = new GeometryData();
        geom.geometryType = GeometryData.INDEXED_TRIANGLES;
        geom.coordinates = loader.getCoords();
        geom.indexes = loader.getVerts();
        geom.indexesCount = geom.indexes.length;

        loader.computeModelBounds();
        float[] bounds = loader.getBounds();

        int len = geom.coordinates.length;

        for(int i=0; i < len; i++)  {
            geom.coordinates[i] = (float) ((double) geom.coordinates[i] * scale);
        }

        for(int i=0; i < bounds.length; i++) {
            bounds[i] = (float) ((double)bounds[i] * scale);
        }

System.out.println("bounds: " + java.util.Arrays.toString(bounds));
/*
        double x = grid.getWidth() / 2.0 / RESOLUTION;
        double y = grid.getHeight() / 2.0 / RESOLUTION;
        double z = grid.getDepth() / 2.0 / RESOLUTION;
*/
        double x = -bounds[0] * 1.1;
        double y = -bounds[2] * 1.1;
        double z = -bounds[4] * 1.1;

System.out.println("translate: " + x + " " + y + " " + z);
        TriangleModelCreator tmc = null;

        double rx = 0,ry = 1,rz = 0,rangle = 0;

        // TODO: using a transform with InteriorFinder duplicates geometry transform

/*
        tmc = new TriangleModelCreator(geom,x,y,z,
                rx,ry,rz,rangle,EXT_MAT,INT_MAT,true);
*/

        tmc = new TriangleModelCreator(geom,x,y,z,
                rx,ry,rz,rangle,EXT_MAT,INT_MAT,true,
                new InteriorFinderTriangleBased(geom,bounds, x,y,z,rx,ry,rz,rangle,INT_MAT));

        tmc.generate(grid);

    }
    private void write(Grid grid, String type, OutputStream os, ErrorReporter console) {
        System.out.println("exterior voxels: " + grid.findCount(VoxelClasses.INSIDE));
        // Output File
//        BoxesX3DExporter exporter = new BoxesX3DExporter(type, os, console);
//        RegionsX3DExporter exporter = new RegionsX3DExporter(type, os, console);
//        BoxSimplifiedX3DExporter exporter = new BoxSimplifiedX3DExporter(type, os, console);
//        BoxBatcherX3DExporter exporter = new BoxBatcherX3DExporter(type, os, console);
        EdgeCollapseSimplifier reducer = new EdgeCollapseSimplifier(16, 0.71);

        //reducer = null;

        MarchingCubesX3DExporter exporter = new MarchingCubesX3DExporter(type, os, console, reducer);

        exporter.write(grid, null);
        exporter.close();
    }

    private void writeDebug(Grid grid, String type, OutputStream os, ErrorReporter console) {
        // Output File

        BoxesX3DExporter exporter = new BoxesX3DExporter(type, os, console);

        HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
        colors.put(new Integer(Grid.INSIDE), new float[] {0,1,0});
        colors.put(new Integer(Grid.OUTSIDE), new float[] {0,0,1});

        HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
        transparency.put(new Integer(Grid.INSIDE), new Float(0));
        transparency.put(new Integer(Grid.OUTSIDE), new Float(0.95));

        exporter.writeDebug(grid, colors, transparency);
        exporter.close();
    }

    public static void main(String[] args) {
        Lettering c = new Lettering();
        c.generate("/cygwin/home/giles/projs/shapeways/code/trunk/service/creator/soundwave_cover/src/models/soundwave_cover/x3d/IPHONE4.x3db",
                "out.x3db");
    }
}