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

package abfab3d.grid;

// External Imports
import java.util.*;
import java.io.*;
import org.web3d.vrml.sav.BinaryContentHandler;

/**
 * Uses a box structure to represent a grid.  Exports to an X3D format.
 *
 * @author Alan Hudson
 */
public class BoxesX3DExporter {
    /** The maximum coords to put in a shape */
    private static final int MAX_TRIANGLES_SHAPE = 300000;

    /**
     * Create an X3D file from the grid.  This should be an exact
     * repsentation of the grid values.
     *
     * @param stream The stream to write too
     * @param colors Maps materialID's to colors
     */
    public void toX3D(Grid grid, BinaryContentHandler stream, Map<Byte, float[]> colors) {
        HashMap<WorldCoordinate,Integer> coords = new HashMap<WorldCoordinate,Integer>();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        ArrayList<WorldCoordinate> lastSlice = new ArrayList<WorldCoordinate>();
        ArrayList<WorldCoordinate> thisSlice = new ArrayList<WorldCoordinate>();
        ArrayList<WorldCoordinate> totalCoords = new ArrayList<WorldCoordinate>();

        // TODO: We might want to use a form of run length encoding to
        // reduce the number of boxes

        double x,y,z;
        int idx = 0;
        float[] color = new float[] {0.8f,0.8f,0.8f};
        float transparency = 0;
        int saved = 0;
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        double pixelSize = grid.getVoxelSize();
        double hpixelSize = grid.getVoxelSize() / 2.0;
        double sheight = grid.getSliceHeight();
        double hsheight = grid.getSliceHeight() / 2.0;

        for(int i=0; i < height; i++) {
            y = i * sheight;

            byte cstate;

            for(int j=0; j < width; j++) {
                x = j * pixelSize;

                for(int k=0; k < depth; k++) {
                    z = k * pixelSize;

                    byte state = grid.getState(j,i,k);

                    if (state == Grid.OUTSIDE)
                        continue;



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
System.out.println("Coords: " + totalCoords.size() + " indices: " + indices.size());
                ejectShape(stream, totalCoords, indices, color, transparency);
                coords.clear();
                indices.clear();
                lastSlice.clear();
                totalCoords.clear();
                idx = 0;
            }
        }

System.out.println("Saved: " + saved);
        ejectShape(stream, totalCoords, indices, color, transparency);
    }

    /**
     * Create an X3D file from the grid.  This should be an exact
     * repsentation of the grid values.
     *
     * @param stream The stream to write too
     * @param colors Maps materialID's to colors
     */
    public void toX3DDebug(Grid grid, BinaryContentHandler stream, Map<Byte, float[]> colors, Map<Byte, Float> transparency) {
        float[] color = colors.get(Grid.OUTSIDE);
        Float transF = transparency.get(Grid.OUTSIDE);
        float trans = 1;

        if (color != null) {
            if (transF != null) {
                trans = transF;
            }
            outputState(grid, stream, Grid.OUTSIDE, color, trans);
        }

        color = colors.get(Grid.EXTERIOR);
        transF = transparency.get(Grid.EXTERIOR);
        trans = 1;

        if (color != null) {
            if (transF != null) {
                trans = transF;
            }
            outputState(grid, stream, Grid.EXTERIOR, color, trans);
        }

        color = colors.get(Grid.INTERIOR);
        transF = transparency.get(Grid.INTERIOR);
        trans = 1;

        if (color != null) {
            if (transF != null) {
                trans = transF;
            }
            outputState(grid, stream, Grid.INTERIOR, color, trans);
        }

    }

    private void outputState(Grid grid, BinaryContentHandler stream, byte display, float[] color, float transparency) {
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

        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();
        double pixelSize = grid.getVoxelSize();
        double hpixelSize = grid.getVoxelSize() / 2.0;
        double sheight = grid.getSliceHeight();
        double hsheight = grid.getSliceHeight() / 2.0;

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
        int[] allIndices = new int[(int) (indices.size() * 4 / 3)];
        for(int i=0; i < indices.size(); ) {
            allIndices[idx++] = indices.get(i++);
            allIndices[idx++] = indices.get(i++);
            allIndices[idx++] = indices.get(i++);
            allIndices[idx++] = -1;
        }

        stream.startNode("Shape", null);
        stream.startField("appearance");
        stream.startNode("Appearance", null);
        stream.startField("material");
        stream.startNode("Material",null);
        stream.startField("diffuseColor");
        stream.fieldValue(color,3);
        stream.startField("transparency");
        stream.fieldValue(transparency);
        stream.endNode();  //  Material
        stream.endNode();  //  Appearance
        stream.startField("geometry");
        stream.startNode("IndexedFaceSet", null);
        stream.startField("coord");
        stream.startNode("Coordinate", null);
        stream.startField("point");
        stream.fieldValue(allCoords, allCoords.length);
        stream.endNode();  // WorldCoordinate
        stream.startField("coordIndex");
        stream.fieldValue(allIndices, allIndices.length);
        stream.endNode();  // IndexedFaceSet
        stream.endNode();  // Shape\
    }
}

