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

package abfab3d.io.output;

// External Imports
import java.util.*;
import java.io.*;

import abfab3d.core.Grid;
import org.web3d.vrml.sav.BinaryContentHandler;
import org.web3d.vrml.export.X3DBinaryRetainedDirectExporter;
import org.web3d.vrml.export.X3DClassicRetainedExporter;
import org.web3d.vrml.export.X3DXMLRetainedExporter;
import org.web3d.vrml.export.X3DBinarySerializer;
import org.web3d.util.ErrorReporter;

// Internal Imports
import abfab3d.grid.*;
import abfab3d.grid.query.BoxRegionFinder;

/**
 * Organizes a grid into regions of exterior voxels.  Writes out
 * those regions.
 *
 * @author Alan Hudson
 */
public class RegionsX3DExporter implements Exporter {
    private static final boolean STATS = true;

    /** The maximum coords to put in a shape */
    private static final int MAX_TRIANGLES_SHAPE = 300000;

    /** X3D Writer */
    private BinaryContentHandler writer;

    /** Error Console */
    private ErrorReporter console;

    /** Is this a complete file export */
    private boolean complete;

    public RegionsX3DExporter(String encoding, OutputStream os, ErrorReporter console) {
        this.console = console;

        complete = true;

        if (encoding.equals("x3db")) {
            writer = new X3DBinaryRetainedDirectExporter(os,
                                 3, 0, console,
                                 X3DBinarySerializer.METHOD_FASTEST_PARSING,
                                 0.001f, true);
        } else if (encoding.equals("x3dv")) {
            writer = new X3DClassicRetainedExporter(os,3,0,console);
        } else if (encoding.equals("x3d")) {
            writer = new X3DXMLRetainedExporter(os,3,0,console);
        } else {
            throw new IllegalArgumentException("Unhandled X3D encoding: " + encoding);
        }

        writer = (BinaryContentHandler) writer;

        ejectHeader();
    }

    /**
     * Constructor.
     *
     * @param exporter The X3D handler to write too.
     * @param console The console
     * @param complete Should we add headers and footers
     */
    public RegionsX3DExporter(BinaryContentHandler exporter, ErrorReporter console, boolean complete) {
        this.console = console;
        this.complete = complete;

        writer = exporter;

        if (complete)
            ejectHeader();
    }

    /**
     * Write a grid to the stream.
     *
     * @param grid The grid to write
     * @param matColors Maps materials to colors.  4 component color
     */
    public void write(Grid grid, Map<Long, float[]> matColors) {
        BoxRegionFinder finder = new BoxRegionFinder();
        Set<Region> regions = finder.execute(grid);

        BoxRegionMerger merger = new BoxRegionMerger(0);
        merger.merge(regions);

        System.out.println("Regions: " + regions.size());

        //writeAsBoxes(grid, regions);
        writeAsIndexedTriangles(grid, regions);
    }

    /**
     * Write a grid to the stream using the grid state
     *
     * @param grid The grid to write
     * @param stateColors Maps states to colors
     * @param stateTransparency Maps states to transparency values.  1 is totally transparent.
     */
    public void writeDebug(Grid grid, Map<Integer, float[]> stateColors,
        Map<Integer, Float> stateTransparency) {
    }

    /**
     * Close the exporter.  Must be called when done.
     */
    public void close() {
        if (complete)
            ejectFooter();
    }

    /**
     * Write out the regions as indexedquads.
     */
    private void writeAsIndexedTriangles(Grid grid, Set<Region> regions) {

        HashMap<WorldCoordinate,Integer> coords = new HashMap<WorldCoordinate,Integer>();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        ArrayList<WorldCoordinate> totalCoords = new ArrayList<WorldCoordinate>();

        Iterator<Region> itr = regions.iterator();

        int[] origin = new int[3];
        int[] size = new int[3];
        double pixel = grid.getVoxelSize();
        double sheight = grid.getSliceHeight();

        double[] wc_origin = new double[3];
        double[] wc_end = new double[3];
        double[] wc_size = new double[3];
        float[] wcf_origin = new float[3];
        float[] wcf_size = new float[3];
        double x,y,z;
        double xhsize, yhsize, zhsize;
        int idx = 0;

        while(itr.hasNext()) {
            BoxRegion r = (BoxRegion) itr.next();

            r.getOrigin(origin);
            r.getSize(size);

//System.out.println("box origin: " + java.util.Arrays.toString(origin) + " size: " + java.util.Arrays.toString(size));
            grid.getWorldCoords(origin[0],origin[1],origin[2], wc_origin);
            grid.getWorldCoords(origin[0] + size[0],origin[1] + size[1],
                origin[2] + size[2], wc_end);

//System.out.println("   origin: " + java.util.Arrays.toString(wc_origin) + " end: " + java.util.Arrays.toString(wc_end));
            wc_size[0] =  wc_end[0] - wc_origin[0];
            wc_size[1] =  wc_end[1] - wc_origin[1];
            wc_size[2] =  wc_end[2] - wc_origin[2];
//System.out.println("   wc_size: " + java.util.Arrays.toString(wc_size));

//wcf_origin = new float[3];
            wcf_origin[0] = (float) (wc_origin[0]);
            wcf_origin[1] = (float) (wc_origin[1]);
            wcf_origin[2] = (float) (wc_origin[2]);

//wcf_size = new float[3];
            wcf_size[0] = (float) wc_size[0];
            wcf_size[1] = (float) wc_size[1];
            wcf_size[2] = (float) wc_size[2];

            x = wc_origin[0] + wc_size[0] / 2.0;
            y = wc_origin[1] + wc_size[1] / 2.0;
            z = wc_origin[2] + wc_size[2] / 2.0;

            xhsize = wc_size[0] / 2.0;
            yhsize = wc_size[1] / 2.0;
            zhsize = wc_size[2] / 2.0;

//System.out.println("   wcf origin: " + java.util.Arrays.toString(wcf_origin) + " size: " + java.util.Arrays.toString(wcf_size));

            WorldCoordinate ubl_coord = new WorldCoordinate((float)(x - xhsize),
                (float)(y + yhsize),(float)(z - zhsize));
            Integer ubl_pos = coords.get(ubl_coord);
            if (ubl_pos == null) {
//System.out.println("ubl added: " + idx);
                ubl_pos = new Integer(idx++);
                coords.put(ubl_coord, ubl_pos);
                totalCoords.add(ubl_coord);
            }

            WorldCoordinate ubr_coord = new WorldCoordinate((float)(x + xhsize),
                (float)(y + yhsize),(float)(z - zhsize));
            Integer ubr_pos = coords.get(ubr_coord);
            if (ubr_pos == null) {
//System.out.println("ubr added: " + idx);
                ubr_pos = new Integer(idx++);
                coords.put(ubr_coord, ubr_pos);
                totalCoords.add(ubr_coord);
            }


            WorldCoordinate lbl_coord = new WorldCoordinate((float)(x - xhsize),
                (float)(y - yhsize),(float)(z - zhsize));
            Integer lbl_pos = coords.get(lbl_coord);
            if (lbl_pos == null) {
//System.out.println("lbl added: " + idx);
                lbl_pos = new Integer(idx++);
                coords.put(lbl_coord, lbl_pos);
                totalCoords.add(lbl_coord);
            }

            WorldCoordinate lbr_coord = new WorldCoordinate((float)(x + xhsize),
                (float)(y - yhsize),(float)(z - zhsize));
            Integer lbr_pos = coords.get(lbr_coord);
            if (lbr_pos == null) {
//System.out.println("lbr added: " + idx);
                lbr_pos = new Integer(idx++);
                coords.put(lbr_coord, lbr_pos);
                totalCoords.add(lbr_coord);
            }

            WorldCoordinate ufl_coord = new WorldCoordinate((float)(x - xhsize),
                (float)(y + yhsize),(float)(z + zhsize));
            Integer ufl_pos = coords.get(ufl_coord);
            if (ufl_pos == null) {
//System.out.println("ufl added: " + idx);
                ufl_pos = new Integer(idx++);
                coords.put(ufl_coord, ufl_pos);
                totalCoords.add(ufl_coord);
            }

            WorldCoordinate ufr_coord = new WorldCoordinate((float)(x + xhsize),
                (float)(y + yhsize),(float)(z + zhsize));
            Integer ufr_pos = coords.get(ufr_coord);
            if (ufr_pos == null) {
//System.out.println("ufr added: " + idx);
                ufr_pos = new Integer(idx++);
                coords.put(ufr_coord, ufr_pos);
                totalCoords.add(ufr_coord);
            }

            WorldCoordinate lfr_coord = new WorldCoordinate((float)(x + xhsize),
                (float)(y - yhsize),(float)(z + zhsize));
            Integer lfr_pos = coords.get(lfr_coord);
            if (lfr_pos == null) {
//System.out.println("lfr added: " + idx);
                lfr_pos = new Integer(idx++);
                coords.put(lfr_coord, lfr_pos);
                totalCoords.add(lfr_coord);
            }

            WorldCoordinate lfl_coord = new WorldCoordinate((float)(x - xhsize),
                (float)(y - yhsize),(float)(z + zhsize));
            Integer lfl_pos = coords.get(lfl_coord);
            if (lfl_pos == null) {
//System.out.println("lfl added: " + idx);
                lfl_pos = new Integer(idx++);
                coords.put(lfl_coord, lfl_pos);
                totalCoords.add(lfl_coord);
            }

            // Create Box

            // Front Face
            indices.add(new Integer(lfr_pos));
            indices.add(new Integer(ufr_pos));
            indices.add(new Integer(ufl_pos));
            indices.add(new Integer(lfr_pos));
            indices.add(new Integer(ufl_pos));
            indices.add(new Integer(lfl_pos));

            // Back Face
            indices.add(new Integer(lbr_pos));
            indices.add(new Integer(ubl_pos));
            indices.add(new Integer(ubr_pos));
            indices.add(new Integer(lbr_pos));
            indices.add(new Integer(lbl_pos));
            indices.add(new Integer(ubl_pos));

            // Right Face
            indices.add(new Integer(lbr_pos));
            indices.add(new Integer(ubr_pos));
            indices.add(new Integer(ufr_pos));
            indices.add(new Integer(lbr_pos));
            indices.add(new Integer(ufr_pos));
            indices.add(new Integer(lfr_pos));

            // Left Face
            indices.add(new Integer(lbl_pos));
            indices.add(new Integer(ufl_pos));
            indices.add(new Integer(ubl_pos));
            indices.add(new Integer(lbl_pos));
            indices.add(new Integer(lfl_pos));
            indices.add(new Integer(ufl_pos));

            // Top Face
            indices.add(new Integer(ufr_pos));
            indices.add(new Integer(ubr_pos));
            indices.add(new Integer(ubl_pos));
            indices.add(new Integer(ufr_pos));
            indices.add(new Integer(ubl_pos));
            indices.add(new Integer(ufl_pos));

            // Bottom Face
            indices.add(new Integer(lfr_pos));
            indices.add(new Integer(lbl_pos));
            indices.add(new Integer(lbr_pos));
            indices.add(new Integer(lfr_pos));
            indices.add(new Integer(lfl_pos));
            indices.add(new Integer(lbl_pos));
        }

        float transparency = 0f;
        ejectShape(writer, totalCoords,indices, new float[] {0.8f,0.8f,0.8f}, transparency);
    }

    /**
     * Write out the regions as indexedquads.
     */
    private void writeAsIndexedQuads(Grid grid, Set<Region> regions) {

        HashMap<WorldCoordinate,Integer> coords = new HashMap<WorldCoordinate,Integer>();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        ArrayList<WorldCoordinate> totalCoords = new ArrayList<WorldCoordinate>();

        Iterator<Region> itr = regions.iterator();

        int[] origin = new int[3];
        int[] size = new int[3];
        double pixel = grid.getVoxelSize();
        double sheight = grid.getSliceHeight();

        double[] wc_origin = new double[3];
        double[] wc_end = new double[3];
        double[] wc_size = new double[3];
        float[] wcf_origin = new float[3];
        float[] wcf_size = new float[3];

        int idx = 0;

        while(itr.hasNext()) {
            BoxRegion r = (BoxRegion) itr.next();

            r.getOrigin(origin);
            r.getSize(size);

//System.out.println("box origin: " + java.util.Arrays.toString(origin) + " size: " + java.util.Arrays.toString(size));
            grid.getWorldCoords(origin[0],origin[1],origin[2], wc_origin);
            grid.getWorldCoords(origin[0] + size[0],origin[1] + size[1],
                origin[2] + size[2], wc_end);

//System.out.println("   origin: " + java.util.Arrays.toString(wc_origin) + " end: " + java.util.Arrays.toString(wc_end));
            wc_size[0] =  wc_end[0] - wc_origin[0];
            wc_size[1] =  wc_end[1] - wc_origin[1];
            wc_size[2] =  wc_end[2] - wc_origin[2];
//System.out.println("   wc_size: " + java.util.Arrays.toString(wc_size));

//wcf_origin = new float[3];
            wcf_origin[0] = (float) (wc_origin[0]);
            wcf_origin[1] = (float) (wc_origin[1]);
            wcf_origin[2] = (float) (wc_origin[2]);

//wcf_size = new float[3];
            wcf_size[0] = (float) wc_size[0];
            wcf_size[1] = (float) wc_size[1];
            wcf_size[2] = (float) wc_size[2];

//System.out.println("   wcf origin: " + java.util.Arrays.toString(wcf_origin) + " size: " + java.util.Arrays.toString(wcf_size));

            WorldCoordinate lfl_coord = new WorldCoordinate(wcf_origin[0],
                wcf_origin[1], wcf_origin[2]);
            Integer lfl_pos = coords.get(lfl_coord);
            if (lfl_pos == null) {
//System.out.println("lfl added: " + idx);
                lfl_pos = new Integer(idx++);
                coords.put(lfl_coord, lfl_pos);
                totalCoords.add(lfl_coord);
            }

            WorldCoordinate lfr_coord = new WorldCoordinate((float)(wcf_origin[0] + wcf_size[0]),
                wcf_origin[1], wcf_origin[2]);
            Integer lfr_pos = coords.get(lfr_coord);
            if (lfr_pos == null) {
//System.out.println("lfr added: " + idx);
                lfr_pos = new Integer(idx++);
                coords.put(lfr_coord, lfr_pos);
                totalCoords.add(lfr_coord);
            }

            WorldCoordinate ufr_coord = new WorldCoordinate((float)(wcf_origin[0] + wcf_size[0]),
                (float)(wcf_origin[1] + wcf_size[1]), wcf_origin[2]);
            Integer ufr_pos = coords.get(ufr_coord);
            if (ufr_pos == null) {
//System.out.println("ufr added: " + idx);
                ufr_pos = new Integer(idx++);
                coords.put(ufr_coord, ufr_pos);
                totalCoords.add(ufr_coord);
            }

            WorldCoordinate ufl_coord = new WorldCoordinate(wcf_origin[0],
                (float)(wcf_origin[1] + wcf_size[1]), wcf_origin[2]);
            Integer ufl_pos = coords.get(ufl_coord);
            if (ufl_pos == null) {
//System.out.println("ufl added: " + idx);
                ufl_pos = new Integer(idx++);
                coords.put(ufl_coord, ufl_pos);
                totalCoords.add(ufl_coord);
            }


            WorldCoordinate lbl_coord = new WorldCoordinate(wcf_origin[0],
                wcf_origin[1], (float) (wcf_origin[2] + wc_size[2]));
            Integer lbl_pos = coords.get(lbl_coord);
            if (lbl_pos == null) {
//System.out.println("lbl added: " + idx);
                lbl_pos = new Integer(idx++);
                coords.put(lbl_coord, lbl_pos);
                totalCoords.add(lbl_coord);
            }

            WorldCoordinate lbr_coord = new WorldCoordinate((float)(wcf_origin[0] + wcf_size[0]),
                wcf_origin[1], (float)(wcf_origin[2] + wc_size[2]));
            Integer lbr_pos = coords.get(lbr_coord);
            if (lbr_pos == null) {
//System.out.println("lbr added: " + idx);
                lbr_pos = new Integer(idx++);
                coords.put(lbr_coord, lbr_pos);
                totalCoords.add(lbr_coord);
            }

            WorldCoordinate ubr_coord = new WorldCoordinate((float)(wcf_origin[0] + wcf_size[0]),
                (float)(wcf_origin[1] + wcf_size[1]), (float)(wcf_origin[2] + wc_size[2]));
            Integer ubr_pos = coords.get(ubr_coord);
            if (ubr_pos == null) {
//System.out.println("ubr added: " + idx);
                ubr_pos = new Integer(idx++);
                coords.put(ubr_coord, ubr_pos);
                totalCoords.add(ubr_coord);
            }

            WorldCoordinate ubl_coord = new WorldCoordinate(wcf_origin[0],
                (float)(wcf_origin[1] + wcf_size[1]), (float)(wcf_origin[2] + wc_size[2]));
            Integer ubl_pos = coords.get(ubl_coord);
            if (ubl_pos == null) {
//System.out.println("ubl added: " + idx);
                ubl_pos = new Integer(idx++);
                coords.put(ubl_coord, ubl_pos);
                totalCoords.add(ubl_coord);
            }

            // Create Box

            // TODO: Would like to use IndexedQuadArray but its not implemented
            // Front

            indices.add(new Integer(lfl_pos));
            indices.add(new Integer(lfr_pos));
            indices.add(new Integer(ufr_pos));
            indices.add(new Integer(lfl_pos));
            indices.add(new Integer(ufr_pos));
            indices.add(new Integer(ufl_pos));

            // Back Face
            indices.add(new Integer(lbl_pos));
            indices.add(new Integer(ubr_pos));
            indices.add(new Integer(lbr_pos));
            indices.add(new Integer(lbl_pos));
            indices.add(new Integer(ubl_pos));
            indices.add(new Integer(ubr_pos));

            // Left Face
            indices.add(new Integer(lbl_pos));
            indices.add(new Integer(lfl_pos));
            indices.add(new Integer(ufl_pos));
            indices.add(new Integer(lbl_pos));
            indices.add(new Integer(ufl_pos));
            indices.add(new Integer(ubl_pos));

            // Right Face
            indices.add(new Integer(lbr_pos));
            indices.add(new Integer(ufr_pos));
            indices.add(new Integer(lfr_pos));
            indices.add(new Integer(lbr_pos));
            indices.add(new Integer(ubr_pos));
            indices.add(new Integer(ufr_pos));

            // Top Face
            indices.add(new Integer(ufl_pos));
            indices.add(new Integer(ubr_pos));
            indices.add(new Integer(ufr_pos));
            indices.add(new Integer(ufl_pos));
            indices.add(new Integer(ubl_pos));
            indices.add(new Integer(ubr_pos));

            // Bottom Face
            indices.add(new Integer(lfl_pos));
            indices.add(new Integer(lfr_pos));
            indices.add(new Integer(lbr_pos));
            indices.add(new Integer(lfl_pos));
            indices.add(new Integer(lbr_pos));
            indices.add(new Integer(lbl_pos));
        }

        float transparency = 0f;
        ejectShape(writer, totalCoords,indices, new float[] {0.8f,0.8f,0.8f}, transparency);
    }

    /**
     * Write out the regions as boxes.  Usually used for debug
     * purposes as its not rendering effecient.
     */
    private void writeAsBoxes(Grid grid, Set<Region> regions) {
        Iterator<Region> itr = regions.iterator();

        int[] origin = new int[3];
        int[] size = new int[3];
        double pixel = grid.getVoxelSize();
        double sheight = grid.getSliceHeight();

        double[] wc_origin = new double[3];
        double[] wc_end = new double[3];
        double[] wc_size = new double[3];
        float[] wcf_origin = new float[3];
        float[] wcf_size = new float[3];

        float[][] colors = new float[][] {
           {1,0,0}, {0,1,0}, {0,0,1},
           {1,1,0}, {1,0,1}, {0,1,1},
           {1,1,1},
           {1,0.5f,0}, {1,0,0.5f},
           {0.5f,1,0}, {0.5f,0,1},
           {0, 0.5f, 1}, {0, 1, 0.5f}
        };

        float[] color1 = new float[] {1,0,0};
        float[] color2 = new float[] {0,0,1};
        float[] colo = new float[] {0,0,1};

        while(itr.hasNext()) {
            BoxRegion r = (BoxRegion) itr.next();

            r.getOrigin(origin);
            r.getSize(size);

//System.out.println("box origin: " + java.util.Arrays.toString(origin) + " size: " + java.util.Arrays.toString(size));
            grid.getWorldCoords(origin[0],origin[1],origin[2], wc_origin);
            grid.getWorldCoords(origin[0] + size[0],origin[1] + size[1],
                origin[2] + size[2], wc_end);

//System.out.println("   origin: " + java.util.Arrays.toString(wc_origin) + " end: " + java.util.Arrays.toString(wc_end));
            wc_size[0] =  wc_end[0] - wc_origin[0];
            wc_size[1] =  wc_end[1] - wc_origin[1];
            wc_size[2] =  wc_end[2] - wc_origin[2];
//System.out.println("   wc_size: " + java.util.Arrays.toString(wc_size));

//wcf_origin = new float[3];
            wcf_origin[0] = (float) (wc_origin[0] + wc_size[0] / 2.0);
            wcf_origin[1] = (float) (wc_origin[1] + wc_size[1] / 2.0);
            wcf_origin[2] = (float) (wc_origin[2] + wc_size[2] / 2.0);

//wcf_size = new float[3];
            wcf_size[0] = (float) wc_size[0];
            wcf_size[1] = (float) wc_size[1];
            wcf_size[2] = (float) wc_size[2];

//System.out.println("   wcf origin: " + java.util.Arrays.toString(wcf_origin) + " size: " + java.util.Arrays.toString(wcf_size));

            writer.startNode("Transform",null);
            writer.startField("translation");
            writer.fieldValue(wcf_origin,3);
            writer.startField("children");
            writer.startNode("Shape",null);
            writer.startField("appearance");
            writer.startNode("Appearance",null);
            writer.startField("material");
            writer.startNode("Material",null);
/*
            if (vol - 1 < colors.length) {
                writer.startField("diffuseColor");
                writer.fieldValue(colors[vol - 1],3);
            } else {
System.out.println("No color for: " + vol);
            }
*/

            writer.startField("transparency");
            writer.fieldValue(0.5f);
            writer.endNode();
            writer.endNode();
            writer.startField("geometry");
            writer.startNode("Box",null);
            writer.startField("size");
//System.out.println("final_size: " + java.util.Arrays.toString(wcf_size));
            writer.fieldValue(wcf_size,3);
            writer.endNode();  // Box
            writer.endNode();  // Shape
            writer.endField();  // children
            writer.endNode(); // Transform
        }
    }

    /**
     * Eject a shape into the stream.
     *
     * @param stream The stream to use
     * @param totalCoords The coords to use
     * @param indices The indices to use
     */
    private void ejectShape(BinaryContentHandler stream, ArrayList<WorldCoordinate> totalCoords,
        ArrayList<Integer> indices, float[] color, float transparency) {

        int idx = 0;
        float[] allCoords = new float[totalCoords.size() * 3];
        Iterator<WorldCoordinate> itr = totalCoords.iterator();
        while(itr.hasNext()) {
            WorldCoordinate c = itr.next();
            allCoords[idx++] = c.x;
            allCoords[idx++] = c.y;
            allCoords[idx++] = c.z;
        }

        idx = 0;
//        int[] allIndices = new int[(int) (indices.size() * 4 / 3)];
        int[] allIndices = new int[(int) (indices.size())];
        for(int i=0; i < indices.size(); ) {
            allIndices[idx++] = indices.get(i++);
            allIndices[idx++] = indices.get(i++);
            allIndices[idx++] = indices.get(i++);
            //allIndices[idx++] = -1;
        }

        stream.startNode("Shape", null);
        stream.startField("appearance");
        stream.startNode("Appearance", null);
        stream.startField("material");
        stream.startNode("Material",null);
        stream.startField("diffuseColor");
//        stream.startField("emissiveColor");
        stream.fieldValue(color,3);
        stream.startField("transparency");
        stream.fieldValue(transparency);
        stream.endNode();  //  Material
        stream.endNode();  //  Appearance
        stream.startField("geometry");
//        stream.startNode("IndexedFaceSet", null);
        stream.startNode("IndexedTriangleSet", null);
        stream.startField("coord");
        stream.startNode("Coordinate", null);
        stream.startField("point");
        stream.fieldValue(allCoords, allCoords.length);
        stream.endNode();  // Coordinate
//        stream.startField("coordIndex");
        stream.startField("index");
        stream.fieldValue(allIndices, allIndices.length);
        stream.endNode();  // IndexedFaceSet
        stream.endNode();  // Shape
    }

    /**
     * Eject a shape into the stream.
     *
     * @param stream The stream to use
     * @param totalCoords The coords to use
     * @param indices The indices to use
     */
    private void ejectShape(BinaryContentHandler stream, ArrayList<WorldCoordinate> totalCoords,
        ArrayList<Integer> indices, ArrayList<float[]> totalColors, float transparency) {

        int idx = 0;
        float[] allCoords = new float[totalCoords.size() * 3];
        Iterator<WorldCoordinate> itr = totalCoords.iterator();
        while(itr.hasNext()) {
            WorldCoordinate c = itr.next();
            allCoords[idx++] = c.x;
            allCoords[idx++] = c.y;
            allCoords[idx++] = c.z;
        }

        idx = 0;
        float[] allColors = new float[totalColors.size() * 3];
        Iterator<float[]> itr2 = totalColors.iterator();
        while(itr2.hasNext()) {
            float[] c = itr2.next();
            allColors[idx++] = c[0];
            allColors[idx++] = c[1];
            allColors[idx++] = c[2];
        }

        if (allCoords.length != allColors.length) {
            throw new IllegalArgumentException("Colors != Coords length");
        }

        idx = 0;
//        int[] allIndices = new int[(int) (indices.size() * 4 / 3)];
        int[] allIndices = new int[(int) (indices.size())];
        for(int i=0; i < indices.size(); ) {
            allIndices[idx++] = indices.get(i++);
            allIndices[idx++] = indices.get(i++);
            allIndices[idx++] = indices.get(i++);
            //allIndices[idx++] = -1;
        }

        stream.startNode("Shape", null);
        stream.startField("appearance");
        stream.startNode("Appearance", null);
        stream.startField("material");
        stream.startNode("Material",null);
        stream.startField("transparency");
        stream.fieldValue(transparency);
        stream.endNode();  //  Material
        stream.endNode();  //  Appearance
        stream.startField("geometry");
//        stream.startNode("IndexedFaceSet", null);
        stream.startNode("IndexedTriangleSet", null);
        stream.startField("coord");
        stream.startNode("Coordinate", null);
        stream.startField("point");
        stream.fieldValue(allCoords, allCoords.length);
        stream.endNode();  // Coordinate
        stream.startField("color");
        stream.startNode("Color", null);
        stream.startField("color");
        stream.fieldValue(allColors, allColors.length);
        stream.endNode();  // Coordinate
//        stream.startField("coordIndex");
        stream.startField("index");
        stream.fieldValue(allIndices, allIndices.length);
        stream.endNode();  // IndexedFaceSet
        stream.endNode();  // Shape
    }

    /**
     * Eject a header appropriate for the file.  This is all stuff that's not
     * specific to a grid.  In X3D terms this is PROFILE/COMPONENT and ant
     * NavigationInfo/Viewpoints desired.
     *
     */
    private void ejectHeader() {
        writer.startDocument("","", "utf8", "#X3D", "V3.0", "");
        writer.profileDecl("Immersive");
        writer.startNode("NavigationInfo", null);
        writer.startField("avatarSize");
        writer.fieldValue(new float[] {0.01f, 1.6f, 0.75f}, 3);
        writer.endNode(); // NavigationInfo

        // TODO: This should really be a lookat to bounds calc of the grid
        // In theory this would need all grids to calculate.  Not all
        // formats allow viewpoints to be intermixed with geometry

        writer.startNode("Viewpoint", null);
        writer.startField("position");
        writer.fieldValue(new float[] {-0.005963757f,-5.863309E-4f,0.06739192f},3);
        writer.startField("orientation");
        writer.fieldValue(new float[] {-0.9757987f,0.21643901f,0.031161053f,0.2929703f},4);
        writer.endNode(); // Viewpoint
    }

    /**
     * Eject a footer for the file.
     *
     * @param stream The stream to write too
     * @param grid The first grid written
     */
    private void ejectFooter() {
        writer.endDocument();
    }
}

