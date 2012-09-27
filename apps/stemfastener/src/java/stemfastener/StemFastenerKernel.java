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

package stemfastener;

// External Imports
import java.io.*;
import java.util.*;
import javax.imageio.*;
import java.awt.image.BufferedImage;

import abfab3d.grid.query.RegionFinder;
import abfab3d.io.output.*;
import abfab3d.mesh.IndexedTriangleSetBuilder;
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

/**
 * Geometry Kernel for the ImageEditor.
 *
 * Some images don't seem to work right, saving them with paint "fixes" this.  Not sure why.
 *    And example of this is the cat.png image.
 *
 * @author Alan Hudson
 */
public class StemFastenerKernel extends HostedKernel {
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

    private int targetTriangles = 0;

    private double stemDiameter;
    private double stemHeight;
    private double headDiameter;
    private double headPercent;
    private double spacing;

    /**
     * Get the parameters for this editor.
     *
     * @return The parameters.
     */
    public Map<String,Parameter> getParams() {
        HashMap<String,Parameter> params = new HashMap<String,Parameter>();

        int seq = 0;
        int step = 0;

        step++;
        seq = 0;

        params.put("resolution", new Parameter("resolution", "Resolution", "How accurate to model the object", "0.0001", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 0.1, null, null)
        );

        params.put("bodyWidth", new Parameter("bodyWidth", "Body Width", "The width of the main body", "0.0762", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0.01, 1, null, null)
        );

        params.put("bodyHeight", new Parameter("bodyHeight", "Body Height", "The height of the main body", "0.0762", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0.01, 1, null, null)
        );

        params.put("bodyDepth", new Parameter("bodyDepth", "Body Depth", "The depth of the main body", "0.002", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0.0007, 1, null, null)
        );

        params.put("triangles", new Parameter("triangles", "Triangles", "Target Triangles", "150000", 1,
                Parameter.DataType.INTEGER, Parameter.EditorType.DEFAULT,
                step, seq++, false, 1, Integer.MAX_VALUE, null, null)
        );

        params.put("stemDiameter", new Parameter("stemDiameter", "Stem Diameter", "The diameter of the stem", "0.0007", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0.0001, 1, null, null)
        );
        params.put("stemHeight", new Parameter("stemHeight", "Stem Height", "The height of the stem", "0.0021", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0.0001, 1, null, null)
        );
        params.put("headDiameter", new Parameter("headDiameter", "Stem Diameter", "The diameter of the head", "0.0014", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0.0001, 1, null, null)
        );

        params.put("headPercent", new Parameter("headPercent", "Head Percent", "The percent of the sphere", "0.5", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        // Likely want this = headDiameter + 1%.  Having expressions would be nice.
        params.put("spacing", new Parameter("spacing", "Stem spacing", "The spacing from each edge", "0.0014", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0.0001, 1, null, null)
        );

        return params;
    }

    /**
     * @param params The parameters
     * @param acc The accuracy to generate the model
     * @param handler The X3D content handler to use
     */
    public KernelResults generate(Map<String,Object> params, Accuracy acc, BinaryContentHandler handler) throws IOException {

        pullParams(params);

        // Calculate maximum bounds

        double max_width = bodyWidth * 1.2;
        double max_height = bodyHeight * 1.2;
        double max_depth = (bodyDepth + stemHeight + headDiameter) * 1.2;

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

        double scx = grid.getWidth() * grid.getVoxelSize() / 2.0;
        double scy = grid.getHeight() * grid.getSliceHeight() / 2.0;
        double scz = grid.getDepth() * grid.getVoxelSize() / 2.0;

        // Make head
        double hcx = scx;
        double hcy = scy + stemHeight / 2.0 + headPercent * headDiameter;
        double hcz = scz;

        SphereCreator sc = new SphereCreator(headDiameter / 2, hcx, hcy, hcz, 0, 1, 0, 0, 1, 1);
        sc.generate(grid);

        // Truncate head
        double th = (1 - headPercent) * headDiameter;
        CubeCreator trunc = new CubeCreator(null, stemDiameter * 6, th, stemDiameter * 6, hcx,hcy - th / 2.0,hcz, 1);
        trunc.generate(grid);

        // Make Stem
        CubeCreator stem = new CubeCreator(null, stemDiameter, stemHeight, stemDiameter, scx,scy,scz, 1);
        stem.generate(grid);

        // Make ref steam
        stem = new CubeCreator(null, stemDiameter, stemHeight, stemDiameter, scx + spacing + stemDiameter / 2.0 + stemDiameter / 2.0,scy,scz, 1);
        stem.generate(grid);

        System.out.println("Writing grid");

        try {
            ErrorReporter console = new PlainTextErrorReporter();
            //writeDebug(grid, handler, console);
            //write(grid, handler, console);
            writeIsosurfaceMaker(grid);
        } catch(Exception e) {
            e.printStackTrace();

            return new KernelResults(false, KernelResults.INTERNAL_ERROR, "Failed Writing Grid", null, null);
        }

        double[] min_bounds = new double[3];
        double[] max_bounds = new double[3];
        grid.getWorldCoords(0,0,0, min_bounds);
        grid.getWorldCoords(grid.getWidth() - 1, grid.getHeight() - 1, grid.getDepth() - 1, max_bounds);


        System.out.println("-------------------------------------------------");
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

            pname = "bodyWidth";
            bodyWidth = ((Double) params.get(pname)).doubleValue();

            pname = "bodyHeight";
            bodyHeight = ((Double) params.get(pname)).doubleValue();

            pname = "stemDiameter";
            stemDiameter = ((Double) params.get(pname)).doubleValue();

            pname = "stemHeight";
            stemHeight = ((Double) params.get(pname)).doubleValue();

            pname = "headDiameter";
            headDiameter = ((Double) params.get(pname)).doubleValue();

            pname = "headPercent";
            headPercent = ((Double) params.get(pname)).doubleValue();

            pname = "spacing";
            spacing = ((Double) params.get(pname)).doubleValue();

            pname = "triangles";
            targetTriangles = ((Integer) params.get(pname)).intValue();

        } catch(Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error parsing: " + pname + " val: " + params.get(pname));
        }
    }

    private void write(Grid grid, BinaryContentHandler handler, ErrorReporter console) {

        // Output File
        //BoxesX3DExporter exporter = new BoxesX3DExporter(type, os, console);
/*
System.out.println("Creating Regions Exporter");
        RegionsX3DExporter exporter = new RegionsX3DExporter(handler, console, true);
        float[] mat_color = new float[] {0.8f,0.8f,0.8f,0};
        HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
        colors.put(new Integer(1), mat_color);

        exporter.write(grid, colors);
        exporter.close();
*/

        // Sadlt this number needs to change based on resolution
//        EdgeCollapseSimplifier reducer = new EdgeCollapseSimplifier(16, 0.71);
        EdgeCollapseSimplifier reducer = new EdgeCollapseSimplifier(500, 0.61);

        // Use Meshlab instead right now.
        reducer = null;

        MarchingCubesX3DExporter exporter = new MarchingCubesX3DExporter(handler, console, true, reducer);

        Map<Integer, float[]> matColors = new HashMap<Integer, float[]>();
        matColors.put(0, new float[] {0.8f,0.8f,0.8f,1f});
        exporter.write(grid, matColors);
        exporter.close();

    }

    private void writeIsosurfaceMaker(Grid grid) throws IOException {
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        double vs = grid.getVoxelSize();
        int smoothSteps = 2;

        double gbounds[] = new double[]{-nx*vs/2,nx*vs/2,-ny*vs/2,ny*vs/2,-nz*vs/2,nz*vs/2};
        double ibounds[] = extendBounds(gbounds, -vs/2);

        IsosurfaceMaker im = new IsosurfaceMaker();
        im.setIsovalue(0.);
        im.setBounds(ibounds);
        im.setGridSize(nx, ny, nz);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

        im.makeIsosurface(new IsosurfaceMaker.SliceGrid(grid, gbounds, smoothSteps), its);
        int[][] faces = its.getFaces();
        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), faces);

        int fcount = faces.length;
        MeshDecimator md = new MeshDecimator();

        long start_time = System.currentTimeMillis();

        System.out.println("Original face count: " + fcount);
        System.out.println("Target face count: " + targetTriangles);
        md.processMesh(mesh, targetTriangles);

        fcount = mesh.getFaceCount();

        MeshExporter.writeMeshSTL(mesh,fmt("out.stl", fcount));

        System.out.println("Decimate time: " + (System.currentTimeMillis() - start_time));


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
        geom.geometryType = GeometryData.INDEXED_TRIANGLES;
        tg.generate(geom);

        TriangleModelCreator tmc = new TriangleModelCreator(geom,tx,ty,tz,
            rx,ry,rz,ra,mat,mat,filled);

        tmc.generate(grid);
    }

    private void createTorus(Grid grid, double tx, double ty, double tz,
                             double rx, double ry, double rz, double ra,
                             double ir, double or, int zlimit, int facets, int mat, boolean filled) {

        System.out.println("createTorus: " + ir + " or: " + or + " zlimit: " + zlimit + " total: " + grid.getDepth());
        TorusGenerator tg = new TorusGenerator((float)ir, (float)or, facets, facets);
        GeometryData geom = new GeometryData();
        geom.geometryType = GeometryData.INDEXED_TRIANGLES;
        tg.generate(geom);

        TriangleModelCreator tmc = new TriangleModelCreator(geom,tx,ty,tz,
                rx,ry,rz,ra,mat,mat,filled);

        tmc.generate(grid);

        int w = grid.getWidth();
        int h = grid.getHeight();
        int d = grid.getDepth();
        int removed = 0;

        for(int y=0; y < h; y++) {
            for(int x=0; x < w; x++) {
                for(int z=zlimit; z < d; z++) {
                    if (grid.getState(x,y,z) != Grid.OUTSIDE) {
                        removed++;
                        grid.setState(x,y,z, Grid.OUTSIDE);
                    }
                }
            }
        }
        System.out.println("Removed torus: " + removed);
    }

}
