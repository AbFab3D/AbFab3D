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
import org.web3d.vrml.sav.BinaryContentHandler;
import org.web3d.vrml.export.X3DBinaryRetainedDirectExporter;
import org.web3d.vrml.export.X3DClassicRetainedExporter;
import org.web3d.vrml.export.X3DXMLRetainedExporter;
import org.web3d.vrml.export.X3DBinarySerializer;
import org.web3d.util.ErrorReporter;

// Internal Imports
import abfab3d.grid.*;

/**
 * Uses a box structure to represent a grid.  Exports to an X3D stream.
 *
 * @author Alan Hudson
 */
public class BoxesX3DExporter implements Exporter {
    private static final boolean STATS = true;

    /** Should we use per-vertex color to show materials */
    private static final boolean MAT_COLOR = true;

    /** The maximum coords to put in a shape */
    private static final int MAX_TRIANGLES_SHAPE = 300000;

    /** X3D Writer */
    private BinaryContentHandler writer;

    /** Error Console */
    private ErrorReporter console;

    /** Is this a complete file export */
    private boolean complete;

    public BoxesX3DExporter(String encoding, OutputStream os, ErrorReporter console) {
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

        ejectHeader();
    }

    /**
     * Constructor.
     *
     * @param exporter The X3D handler to write too.
     * @param console The console
     * @param complete Should we add headers and footers
     */
    public BoxesX3DExporter(BinaryContentHandler exporter, ErrorReporter console, boolean complete) {
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
    public void write(Grid grid, Map<Integer, float[]> matColors) {

        if (grid instanceof OctreeAttributeGridByte) {
            ((OctreeAttributeGridByte)grid).write(writer, (OctreeAttributeGridByte)grid, matColors);
            return;
        }

        HashMap<WorldCoordinate,Integer> coords = new HashMap<WorldCoordinate,Integer>();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        ArrayList<WorldCoordinate> lastSlice = new ArrayList<WorldCoordinate>();
        ArrayList<WorldCoordinate> thisSlice = new ArrayList<WorldCoordinate>();
        ArrayList<WorldCoordinate> totalCoords = new ArrayList<WorldCoordinate>();
        ArrayList<float[]> totalColors = new ArrayList<float[]>();

        // TODO: We might want to use a form of run length encoding to
        // reduce the number of boxes

        double x,y,z;
        int idx = 0;
//        float[] color = new float[] {0.8f,0.8f,0.8f};
        float[] def_color = new float[] {34/255.0f,139/255.0f,34/255.0f};
        float def_transparency = 1f;
        float[] color = def_color;
        float transparency = def_transparency;

        if (matColors != null) {
            // support color for material1
            float[] mat_color = matColors.get(new Integer(1));
            if (mat_color != null) {
                color[0] = mat_color[0];
                color[1] = mat_color[1];
                color[2] = mat_color[2];

                transparency = mat_color[3];
            }
        }

        int saved = 0;
        int voxels = 0;
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        double pixelSize = grid.getVoxelSize();
        double hpixelSize = grid.getVoxelSize() / 2.0;
        double sheight = grid.getSliceHeight();
        double hsheight = grid.getSliceHeight() / 2.0;

        writer.startNode("Transform", null);
        writer.startField("translation");
        double tx,ty,tz;
        tx = grid.getWidth() / 2.0 * grid.getVoxelSize();
        ty = grid.getHeight() / 2.0 * grid.getSliceHeight();
        tz = grid.getDepth() / 2.0 * grid.getVoxelSize();

        writer.fieldValue(new float[] {(float)-tx,(float)-ty,(float)-tz}, 3);

        writer.startField("children");

        for(int i=0; i < height; i++) {
            y = i * sheight;

            byte cstate;

            for(int j=0; j < width; j++) {
                x = j * pixelSize;

                for(int k=0; k < depth; k++) {
                    z = k * pixelSize;

                    VoxelData vd = grid.getData(j,i,k);

                    byte state = vd.getState();
                    int mat = vd.getMaterial();

                    if (state == Grid.OUTSIDE)
                        continue;

                    if (matColors != null) {
                        float[] mat_color = matColors.get(mat);

                        if (mat_color != null) {
                            color = mat_color;
//System.out.println("color: " + java.util.Arrays.toString(color) + " for: " + mat);
                        } else {
System.out.println("no color for: " + mat);
                            color = def_color;
                        }
                    }



                    if (STATS) voxels++;

// TODO: Need to map material

//System.out.println("Live one: coord: " + j + " " + i + " " + k);
//System.out.println("   coord(wc): " + x + " " + y + " " + z);
                    WorldCoordinate ubl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y + hsheight),(float)(z - hpixelSize));
                    Integer ubl_pos = coords.get(ubl_coord);
                    if (ubl_pos == null) {
//System.out.println("ubl added: " + idx);
                        ubl_pos = new Integer(idx++);
                        coords.put(ubl_coord, ubl_pos);
                        if (MAT_COLOR) {
                            totalColors.add(color);
                        }
                        thisSlice.add(ubl_coord);
                    }

                    WorldCoordinate ubr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y + hsheight),(float)(z - hpixelSize));
                    Integer ubr_pos = coords.get(ubr_coord);
                    if (ubr_pos == null) {
//System.out.println("ubr added: " + idx);
                        ubr_pos = new Integer(idx++);
                        coords.put(ubr_coord, ubr_pos);
                        if (MAT_COLOR) {
                            totalColors.add(color);
                        }
                        thisSlice.add(ubr_coord);
                    }


                    WorldCoordinate lbl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y - hsheight),(float)(z - hpixelSize));
                    Integer lbl_pos = coords.get(lbl_coord);
                    if (lbl_pos == null) {
//System.out.println("lbl added: " + idx);
                        lbl_pos = new Integer(idx++);
                        coords.put(lbl_coord, lbl_pos);
                        if (MAT_COLOR) {
                            totalColors.add(color);
                        }
                        thisSlice.add(lbl_coord);
                    }

                    WorldCoordinate lbr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y - hsheight),(float)(z - hpixelSize));
                    Integer lbr_pos = coords.get(lbr_coord);
                    if (lbr_pos == null) {
//System.out.println("lbr added: " + idx);
                        lbr_pos = new Integer(idx++);
                        coords.put(lbr_coord, lbr_pos);
                        if (MAT_COLOR) {
                            totalColors.add(color);
                        }
                        thisSlice.add(lbr_coord);
                    }

                    WorldCoordinate ufl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y + hsheight),(float)(z + hpixelSize));
                    Integer ufl_pos = coords.get(ufl_coord);
                    if (ufl_pos == null) {
//System.out.println("ufl added: " + idx);
                        ufl_pos = new Integer(idx++);
                        coords.put(ufl_coord, ufl_pos);
                        if (MAT_COLOR) {
                            totalColors.add(color);
                        }
                        thisSlice.add(ufl_coord);
                    }

                    WorldCoordinate ufr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y + hsheight),(float)(z + hpixelSize));
                    Integer ufr_pos = coords.get(ufr_coord);
                    if (ufr_pos == null) {
//System.out.println("ufr added: " + idx);
                        ufr_pos = new Integer(idx++);
                        coords.put(ufr_coord, ufr_pos);
                        if (MAT_COLOR) {
                            totalColors.add(color);
                        }
                        thisSlice.add(ufr_coord);
                    }

                    WorldCoordinate lfr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y - hsheight),(float)(z + hpixelSize));
                    Integer lfr_pos = coords.get(lfr_coord);
                    if (lfr_pos == null) {
//System.out.println("lfr added: " + idx);
                        lfr_pos = new Integer(idx++);
                        coords.put(lfr_coord, lfr_pos);
                        if (MAT_COLOR) {
                            totalColors.add(color);
                        }
                        thisSlice.add(lfr_coord);
                    }

                    WorldCoordinate lfl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y - hsheight),(float)(z + hpixelSize));
                    Integer lfl_pos = coords.get(lfl_coord);
                    if (lfl_pos == null) {
//System.out.println("lfl added: " + idx);
                        lfl_pos = new Integer(idx++);
                        coords.put(lfl_coord, lfl_pos);
                        if (MAT_COLOR) {
                            totalColors.add(color);
                        }
                        thisSlice.add(lfl_coord);
                    }


                    // Create Box

                    boolean displayFront = true;

                    if (k < depth - 1) {
                        cstate = grid.getState(j,i,k+1);

                        if (cstate == state) {
                            displayFront = false;
                            if (STATS) saved++;
                        }
                    }

                    if (displayFront) {
                        // Front Face
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(ufl_pos));
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(ufl_pos));
                        indices.add(new Integer(lfl_pos));
                    }

                    boolean displayBack = true;
                    if (k > 0) {
                        cstate = grid.getState(j,i,k-1);

                        if (cstate == state) {
                            displayBack = false;
                            if (STATS) saved++;
                        }
                    }

                    if (displayBack) {
                        // Back Face
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(ubr_pos));
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(ubl_pos));
                    }

                    boolean displayRight = true;

                    if (j < width - 1) {
                        cstate = grid.getState(j+1,i,k);

                        if (cstate == state) {
                            displayRight = false;
                            if (STATS) saved++;
                        }
                    }

                    if (displayRight) {
                        // Right Face
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(ubr_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(lfr_pos));
                    }

                    boolean displayLeft = true;

                    if (j > 0) {
                        cstate = grid.getState(j-1,i,k);

                        if (cstate == state) {
                            displayLeft = false;
                            if (STATS) saved++;
                        }
                    }

                    if (displayLeft) {
                        // Left Face
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(ufl_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(lfl_pos));
                        indices.add(new Integer(ufl_pos));
                    }

                    boolean displayTop = true;

                    if (i < height - 1) {
                        cstate = grid.getState(j,i+1,k);

                        if (cstate == state) {
                            displayTop = false;
                            if (STATS) saved++;
                        }
                    }

                    if (displayTop) {
                        // Top Face
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(ubr_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(ufl_pos));
                    }

                    boolean displayBottom = true;

                    if (i > 0) {
                        cstate = grid.getState(j,i-1,k);

                        if (cstate == state) {
                            displayBottom = false;
                            if (STATS) saved++;
                        }
                    }

                    if (displayBottom) {
                        // Bottom Face
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(lfl_pos));
                        indices.add(new Integer(lbl_pos));
                    }
                }
            }

            // Remove n - 2 coordinates
            Iterator<WorldCoordinate> itr = lastSlice.iterator();
            while(itr.hasNext()) {
                coords.remove(itr.next());
            }

            totalCoords.addAll(thisSlice);
            lastSlice = thisSlice;
            thisSlice = new ArrayList<WorldCoordinate>();

            if (indices.size() / 3 >= MAX_TRIANGLES_SHAPE) {
                ejectShape(writer, totalCoords, indices, totalColors, transparency);
//                ejectShape(writer, totalCoords, indices, color, transparency);
                coords.clear();
                totalColors.clear();
                indices.clear();
                lastSlice.clear();
                totalCoords.clear();
                idx = 0;
            }
        }

//        ejectShape(writer, totalCoords, indices, color, transparency);
        ejectShape(writer, totalCoords, indices, totalColors, transparency);

        // End Centering Transform
        writer.endField();
        writer.endNode();

        if (STATS) System.out.println("voxel: " + voxels + " sides: " + (voxels * 6) + " saved: " + saved + " %: " + ((float)saved / voxels * 6));

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

        float[] color = stateColors.get(new Integer(Grid.OUTSIDE));
        Float transF = stateTransparency.get(new Integer(Grid.OUTSIDE));
        float trans = 1;

        writer.startNode("Transform", null);
        writer.startField("translation");
        double tx,ty,tz;
        tx = grid.getWidth() / 2.0 * grid.getVoxelSize();
        ty = grid.getHeight() / 2.0 * grid.getSliceHeight();
        tz = grid.getDepth() / 2.0 * grid.getVoxelSize();

        writer.fieldValue(new float[] {(float)-tx,(float)-ty,(float)-tz}, 3);

        writer.startField("children");

        if (color != null) {
            if (transF != null) {
                trans = transF;
            }
            outputState(grid, writer, Grid.OUTSIDE, color, trans);
        }

        color = stateColors.get(new Integer(Grid.EXTERIOR));
        transF = stateTransparency.get(new Integer(Grid.EXTERIOR));
        trans = 1;

        if (color != null) {
            if (transF != null) {
                trans = transF;
            }
            outputState(grid, writer, Grid.EXTERIOR, color, trans);
        }

        color = stateColors.get(new Integer(Grid.INTERIOR));
        transF = stateTransparency.get(new Integer(Grid.INTERIOR));
        trans = 1;

        if (color != null) {
            if (transF != null) {
                trans = transF;
            }
            outputState(grid, writer, Grid.INTERIOR, color, trans);
        }

        // End Centering Transform
        writer.endField();
        writer.endNode();
    }

    /**
     * Close the exporter.  Must be called when done.
     */
    public void close() {
        if (complete)
            ejectFooter();
    }

    private void outputState(Grid grid, BinaryContentHandler stream, byte display,
        float[] color, float transparency) {

        HashMap<WorldCoordinate,Integer> coords = new HashMap<WorldCoordinate,Integer>();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        ArrayList<WorldCoordinate> lastSlice = new ArrayList<WorldCoordinate>();
        ArrayList<WorldCoordinate> thisSlice = new ArrayList<WorldCoordinate>();
        ArrayList<WorldCoordinate> totalCoords = new ArrayList<WorldCoordinate>();

        // TODO: We might want to use a form of run length encoding to
        // reduce the number of boxes

        double x,y,z;
        int idx = 0;
        int saved = 0;
        int voxels = 0;

        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        double pixelSize = grid.getVoxelSize();
        double hpixelSize = grid.getVoxelSize() / 2.0;
        double sheight = grid.getSliceHeight();
        double hsheight = grid.getSliceHeight() / 2.0;

        if (STATS)
            System.out.println("Output State: " + display);
        // TODO: change to iterator for faster speed

        for(int i=0; i < height; i++) {
            y = i * sheight;

            byte cstate;


            for(int j=0; j < width; j++) {
                x = j * pixelSize;

                for(int k=0; k < depth; k++) {
                    z = k * pixelSize;

                    byte state = grid.getState(j,i,k);

                    if (state != display)
                        continue;


                    if (STATS) voxels++;

// TODO: Need to map material

//System.out.println("Live one: coord: " + j + " " + i + " " + k);
//System.out.println("   coord(wc): " + x + " " + y + " " + z);
                    WorldCoordinate ubl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y + hsheight),(float)(z - hpixelSize));
                    Integer ubl_pos = coords.get(ubl_coord);
                    if (ubl_pos == null) {
//System.out.println("ubl added: " + idx);
                        ubl_pos = new Integer(idx++);
                        coords.put(ubl_coord, ubl_pos);
                        thisSlice.add(ubl_coord);
                    }

                    WorldCoordinate ubr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y + hsheight),(float)(z - hpixelSize));
                    Integer ubr_pos = coords.get(ubr_coord);
                    if (ubr_pos == null) {
//System.out.println("ubr added: " + idx);
                        ubr_pos = new Integer(idx++);
                        coords.put(ubr_coord, ubr_pos);
                        thisSlice.add(ubr_coord);
                    }


                    WorldCoordinate lbl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y - hsheight),(float)(z - hpixelSize));
                    Integer lbl_pos = coords.get(lbl_coord);
                    if (lbl_pos == null) {
//System.out.println("lbl added: " + idx);
                        lbl_pos = new Integer(idx++);
                        coords.put(lbl_coord, lbl_pos);
                        thisSlice.add(lbl_coord);
                    }

                    WorldCoordinate lbr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y - hsheight),(float)(z - hpixelSize));
                    Integer lbr_pos = coords.get(lbr_coord);
                    if (lbr_pos == null) {
//System.out.println("lbr added: " + idx);
                        lbr_pos = new Integer(idx++);
                        coords.put(lbr_coord, lbr_pos);
                        thisSlice.add(lbr_coord);
                    }

                    WorldCoordinate ufl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y + hsheight),(float)(z + hpixelSize));
                    Integer ufl_pos = coords.get(ufl_coord);
                    if (ufl_pos == null) {
//System.out.println("ufl added: " + idx);
                        ufl_pos = new Integer(idx++);
                        coords.put(ufl_coord, ufl_pos);
                        thisSlice.add(ufl_coord);
                    }

                    WorldCoordinate ufr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y + hsheight),(float)(z + hpixelSize));
                    Integer ufr_pos = coords.get(ufr_coord);
                    if (ufr_pos == null) {
//System.out.println("ufr added: " + idx);
                        ufr_pos = new Integer(idx++);
                        coords.put(ufr_coord, ufr_pos);
                        thisSlice.add(ufr_coord);
                    }

                    WorldCoordinate lfr_coord = new WorldCoordinate((float)(x + hpixelSize),
                        (float)(y - hsheight),(float)(z + hpixelSize));
                    Integer lfr_pos = coords.get(lfr_coord);
                    if (lfr_pos == null) {
//System.out.println("lfr added: " + idx);
                        lfr_pos = new Integer(idx++);
                        coords.put(lfr_coord, lfr_pos);
                        thisSlice.add(lfr_coord);
                    }

                    WorldCoordinate lfl_coord = new WorldCoordinate((float)(x - hpixelSize),
                        (float)(y - hsheight),(float)(z + hpixelSize));
                    Integer lfl_pos = coords.get(lfl_coord);
                    if (lfl_pos == null) {
//System.out.println("lfl added: " + idx);
                        lfl_pos = new Integer(idx++);
                        coords.put(lfl_coord, lfl_pos);
                        thisSlice.add(lfl_coord);
                    }


                    // Create Box
                    boolean displayFront = true;

                    if (k < depth - 1) {
                        cstate = grid.getState(j,i,k+1);

                        if (cstate == state) {
                            displayFront = false;
                            saved++;
                        }
                    }

                    if (displayFront) {
                        // Front Face
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(ufl_pos));
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(ufl_pos));
                        indices.add(new Integer(lfl_pos));
                    }

                    boolean displayBack = true;
                    if (k > 0) {
                        cstate = grid.getState(j,i,k-1);

                        if (cstate == state) {
                            displayBack = false;
                            saved++;
                        }
                    }

                    if (displayBack) {
                        // Back Face
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(ubr_pos));
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(ubl_pos));
                    }

                    boolean displayRight = true;

                    if (j < width - 1) {
                        cstate = grid.getState(j+1,i,k);

                        if (cstate == state) {
                            displayRight = false;
                            saved++;
                        }
                    }

                    if (displayRight) {
                        // Right Face
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(ubr_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(lfr_pos));
                    }

                    boolean displayLeft = true;

                    if (j > 0) {
                        cstate = grid.getState(j-1,i,k);

                        if (cstate == state) {
                            displayLeft = false;
                            saved++;
                        }
                    }

                    if (displayLeft) {
                        // Left Face
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(ufl_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(lfl_pos));
                        indices.add(new Integer(ufl_pos));
                    }

                    boolean displayTop = true;

                    if (i < height - 1) {
                        cstate = grid.getState(j,i+1,k);

                        if (cstate == state) {
                            displayTop = false;
                            saved++;
                        }
                    }

                    if (displayTop) {
                        // Top Face
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(ubr_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(ufr_pos));
                        indices.add(new Integer(ubl_pos));
                        indices.add(new Integer(ufl_pos));
                    }

                    boolean displayBottom = true;

                    if (i > 0) {
                        cstate = grid.getState(j,i-1,k);

                        if (cstate == state) {
                            displayBottom = false;
                            saved++;
                        }
                    }

                    if (displayBottom) {
                        // Bottom Face
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(lbl_pos));
                        indices.add(new Integer(lbr_pos));
                        indices.add(new Integer(lfr_pos));
                        indices.add(new Integer(lfl_pos));
                        indices.add(new Integer(lbl_pos));
                    }
                }
            }

            // Remove n - 2 coordinates
            Iterator<WorldCoordinate> itr = lastSlice.iterator();
            while(itr.hasNext()) {
                coords.remove(itr.next());
            }

            totalCoords.addAll(thisSlice);
            lastSlice = thisSlice;
            thisSlice = new ArrayList<WorldCoordinate>();

            if (indices.size() / 3 >= MAX_TRIANGLES_SHAPE) {
                ejectShape(stream, totalCoords, indices, color, transparency);
                coords.clear();
                indices.clear();
                lastSlice.clear();
                totalCoords.clear();
                idx = 0;
            }
        }

        ejectShape(stream, totalCoords, indices, color, transparency);

        if (STATS) System.out.println("voxel: " + voxels + " sides: " + (voxels * 6) + " saved: " + saved + " %: " + ((float)saved / voxels * 6));
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
     */
    private void ejectFooter() {
        writer.endDocument();
    }
}

