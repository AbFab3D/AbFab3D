package abfab3d.io.output;

import abfab3d.grid.*;
import org.web3d.util.ErrorReporter;
import org.web3d.util.spatial.VolumetricSpaceArrayTriangle;
import org.web3d.vrml.export.X3DBinaryRetainedDirectExporter;
import org.web3d.vrml.export.X3DBinarySerializer;
import org.web3d.vrml.export.X3DClassicRetainedExporter;
import org.web3d.vrml.export.X3DXMLRetainedExporter;
import org.web3d.vrml.sav.BinaryContentHandler;
import toxi.geom.Vec3D;
import toxi.geom.mesh.*;
import toxi.volume.ArrayIsoSurface;
import toxi.volume.HashIsoSurface;
import toxi.volume.IsoSurface;
import toxi.volume.VolumetricSpaceArray;

import java.io.OutputStream;
import java.util.*;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public class MarchingCubesX3DExporter {
    private static final boolean SIMPLIFY = false;
    private static final boolean STATS = true;
    private static final boolean raw_output = false;


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

    /** The mesh */
    private WETriangleMesh mesh;

    /** The mesh reducer */
    private MeshSimplifier simplifier;

    private ArrayList<VoxelCoordinate> frontFaces;
    private ArrayList<VoxelCoordinate> backFaces;
    private ArrayList<VoxelCoordinate> leftFaces;
    private ArrayList<VoxelCoordinate> rightFaces;
    private ArrayList<VoxelCoordinate> topFaces;
    private ArrayList<VoxelCoordinate> bottomFaces;

    // Scratch coords
    private double[] ul;
    private double[] lr;
    double hpixelSize;
    double sheight;
    double hsheight;
    private int width;
    private int height;
    private int depth;

    public MarchingCubesX3DExporter(String encoding, OutputStream os, ErrorReporter console) {
        this(encoding, os, console, null);
    }

    public MarchingCubesX3DExporter(String encoding, OutputStream os, ErrorReporter console, MeshSimplifier simplifier) {
        this.console = console;
        this.simplifier = simplifier;

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
    public MarchingCubesX3DExporter(BinaryContentHandler exporter, ErrorReporter console, boolean complete) {
        this(exporter, console, complete, null);
    }

    /**
     * Constructor.
     *
     * @param exporter The X3D handler to write too.
     * @param console The console
     * @param complete Should we add headers and footers
     */
    public MarchingCubesX3DExporter(BinaryContentHandler exporter, ErrorReporter console, boolean complete, MeshSimplifier simplifier) {
        this.console = console;
        this.simplifier = simplifier;
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

System.out.println("writing grid: " + grid);
        int idx = 0;
//        float[] color = new float[] {0.8f,0.8f,0.8f};
        float[] def_color = new float[] {34/255.0f,139/255.0f,34/255.0f};
        float def_transparency = 0.5f;
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
        width = grid.getWidth();
        height = grid.getHeight();
        depth = grid.getDepth();
        double pixelSize = grid.getVoxelSize();

        writer.startNode("Transform", null);
        writer.startField("translation");
        double tx,ty,tz;
        tx = grid.getWidth() / 2.0 * grid.getVoxelSize();
        ty = grid.getHeight() / 2.0 * grid.getSliceHeight();
        tz = grid.getDepth() / 2.0 * grid.getVoxelSize();

//System.out.println("Hardcoded centering off");
//tx = 0; ty = 0; tz = 0;
        writer.fieldValue(new float[] {(float)-tx,(float)-ty,(float)-tz}, 3);

        writer.startField("children");

        frontFaces = new ArrayList<VoxelCoordinate>();
        backFaces = new ArrayList<VoxelCoordinate>();
        topFaces = new ArrayList<VoxelCoordinate>();
        bottomFaces = new ArrayList<VoxelCoordinate>();
        leftFaces = new ArrayList<VoxelCoordinate>();
        rightFaces = new ArrayList<VoxelCoordinate>();
        ul = new double[3];
        lr = new double[3];

        hpixelSize = grid.getVoxelSize() / 2.0;
        sheight = grid.getSliceHeight();
        hsheight = grid.getSliceHeight() / 2.0;

System.out.println("Creating mesh");
        mesh = new WETriangleMesh();

/*        
        VolumetricSpaceArray vol = new VolumetricSpaceArray(
                new Vec3D((float)grid.getVoxelSize(),(float)grid.getSliceHeight(),(float)grid.getVoxelSize()),
                grid.getWidth(), grid.getHeight(), grid.getDepth());
*/        
        
        double[] min = new double[3];
        double[] max = new double[3];
        
        grid.getGridBounds(min,max);
        double w = max[0] - min[0];
        double h = max[1] - min[1];
        double d = max[2] - min[2];
        
        double max_dim = Math.max(Math.max(w,h),d);


        // TODO: new method fails on iphone case.
        boolean oldway = true;

System.out.println("Creating vol space array");        
        VolumetricSpaceArray vol = null;

        if (oldway) {
/*            
            vol = new VolumetricSpaceArray(
                    new Vec3D((float) ((grid.getWidth() + 2) * grid.getVoxelSize()),(float)((grid.getHeight() + 2) * grid.getSliceHeight()),(float)((grid.getDepth() + 2) * grid.getVoxelSize())),
                    grid.getWidth() + 2, grid.getHeight() + 2, grid.getDepth() + 2);
*/                  
            int border = 1;
            
            vol = new VolumetricSpaceArray(
                    new Vec3D((float)(grid.getWidth() * grid.getVoxelSize()),(float)((grid.getHeight() * grid.getSliceHeight())),(float)((grid.getDepth() * grid.getVoxelSize()))),
                    grid.getWidth() + border, grid.getHeight() + border, grid.getDepth() + border);
        }

/*
        VolumetricSpaceArray vol = new VolumetricSpaceArray(
                new Vec3D((float) ((grid.getWidth() + 0) * grid.getVoxelSize()),(float)((grid.getHeight() + 0) * grid.getSliceHeight()),(float)((grid.getDepth() + 0) * grid.getVoxelSize())),
                grid.getWidth() + 0, grid.getHeight() + 0, grid.getDepth() + 0);
 */
        
        byte state = 0;
        int mat = 0;
        int cellIndex = 0;

        // TODO: What about structures with > int vertices?
        int num_cells = grid.getWidth() * grid.getHeight() * grid.getDepth();

        Vec3D[] edgeVertices = new Vec3D[3 * num_cells];

        int resX1 = width - 1;
        int resY1 = height - 1;
        int resZ1 = depth - 1;
        int sliceSize = width * depth;
        float t = 0.5f;  // use mid points for edge lines
        double[] wcoords = new double[3];
        int[] gcoords = new int[3];

        System.out.println("res: " + resX1 + " " + resY1 + " " + resZ1);
        // TODO: Swap to outer y when debugged

        double off_x, off_y, off_z;

        loop: for(int i=0; i < resZ1; i++) {
            off_z = i * pixelSize;
            
            for(int j=0; j < resY1; j++) {
                off_y = j * sheight;

                for(int k=0; k < resX1; k++) {
                    off_x = k * pixelSize;

                    VoxelData vd = grid.getData(k,j,i);

                    state = vd.getState();
                    mat = vd.getMaterial();

/*
                    if (state != Grid.OUTSIDE) {
                        grid.getWorldCoords(k,j,i,wcoords);
                        System.out.println("loc: " + k + " " + j + " " + i + " wc: " + java.util.Arrays.toString(wcoords));
                        System.out.println("off_x: " + off_x + " " + off_y + " " + off_z);
                    }
*/
                    if (oldway) {
                        if (vd.getState() != Grid.OUTSIDE) {
                            vol.setVoxelAt(k,j,i,0.5f);
                        }

                        continue;
                    }
                    
                    cellIndex = getCellIndex(grid,k,j,i);

                    if (cellIndex > 0 && cellIndex < 255) {
                        final int edgeFlags = MarchingCubesIndex.edgesToCompute[cellIndex];
                        if (edgeFlags > 0 && edgeFlags < 255) {
                            int edgeOffsetIndex = (i * sliceSize + j * depth + k) * 3;
//System.out.println("edgeoffset: " + edgeOffsetIndex);
                                if ((edgeFlags & 1) > 0) {
                                    //System.out.println("x: " + (off_x + hpixelSize) + " y: " + off_y + " z: " + off_z);

                                    if (edgeVertices[edgeOffsetIndex] == null) {

                                        edgeVertices[edgeOffsetIndex] = new Vec3D(
                                                (float) (off_x + hpixelSize), (float) off_y, (float)off_z);
/*
                                        grid.getGridCoords(edgeVertices[edgeOffsetIndex].x, edgeVertices[edgeOffsetIndex].y, edgeVertices[edgeOffsetIndex].z, gcoords);
                                        System.out.println("grid coords1: " + java.util.Arrays.toString(gcoords));
*/
/*
                                        edgeVertices[edgeOffsetIndex] = new Vec3D(
                                                (float) (off_x), (float) off_y, (float)off_z);
*/
                                    }
                                }
                                if ((edgeFlags & 2) > 0) {
                                    //System.out.println("x: " + (off_x + hpixelSize) + " y: " + off_y + " z: " + off_z);
                                    if (edgeVertices[edgeOffsetIndex + 1] == null) {
                                        edgeVertices[edgeOffsetIndex + 1] = new Vec3D(
                                                (float) off_x,
                                                ((float) (off_y + hsheight)), (float)off_z);

/*
                                        grid.getGridCoords(edgeVertices[edgeOffsetIndex+1].x, edgeVertices[edgeOffsetIndex+1].y, edgeVertices[edgeOffsetIndex+1].z, gcoords);
                                        System.out.println("grid coords2: " + java.util.Arrays.toString(gcoords));
*/
/*
                                        edgeVertices[edgeOffsetIndex] = new Vec3D(
                                                (float) (off_x), (float) off_y, (float)off_z);
*/
                                    }
                                }
                                if ((edgeFlags & 4) > 0) {
                                    if (edgeVertices[edgeOffsetIndex + 2] == null) {

                                        edgeVertices[edgeOffsetIndex + 2] = new Vec3D(
                                                (float) off_x, (float) off_y, (float) (off_z + hpixelSize));
/*
                                        grid.getGridCoords(edgeVertices[edgeOffsetIndex+2].x, edgeVertices[edgeOffsetIndex+2].y, edgeVertices[edgeOffsetIndex+2].z, gcoords);
                                        System.out.println("grid coords3: " + java.util.Arrays.toString(gcoords));
*/
/*
                                        edgeVertices[edgeOffsetIndex] = new Vec3D(
                                                (float) (off_x), (float) off_y, (float)off_z);
*/
                                    }
                                }
                        }
                    }

                }
            }
        }

        if (!oldway) {
            //System.out.println("verts: " + java.util.Arrays.toString(edgeVertices));
        final int[] face = new int[16];
        for (int z = 0; z < resZ1; z++) {
            for (int y = 0; y < resY1; y++) {
                for (int x = 0; x < resX1; x++) {
                    cellIndex = getCellIndex(grid, x, y, z);
                    if (cellIndex > 0 && cellIndex < 255) {
                        int num = 0;
                        int edgeIndex;
                        final int[] cellTriangles = MarchingCubesIndex.cellTriangles[cellIndex];
                        while ((edgeIndex = cellTriangles[num]) != -1) {
                            int[] edgeOffsetInfo = MarchingCubesIndex.edgeOffsets[edgeIndex];
                            face[num] = ((x + edgeOffsetInfo[0]) + grid.getWidth()
                                    * (y + edgeOffsetInfo[1]) + sliceSize
                                    * (z + edgeOffsetInfo[2]))
                                    * 3 + edgeOffsetInfo[3];
                            num++;
                        }
                        for (int n = 0; n < num; n += 3) {
                            final Vec3D va = edgeVertices[face[n + 1]];
                            final Vec3D vb = edgeVertices[face[n + 2]];
                            final Vec3D vc = edgeVertices[face[n]];
                            if (va != null && vb != null && vc != null) {
System.out.println("Add face: " + va + " " + vb + " " + vc);
                                mesh.addFace(va, vb, vc);
                            }
                        }
                    }
                }
            }
        }

        System.out.println("faces: " + mesh.getNumFaces() + " verts: " + mesh.getNumVertices());
        }

        HashMap<String,Object> params = new HashMap<String, Object>();
        params.put(SAVExporter.EXPORT_NORMALS, false);
        params.put(SAVExporter.COMPACT_VERTICES, true);



        if (oldway) {
            //vol.closeSides();
//            IsoSurface surface = new HashIsoSurface(vol);

System.out.println("Running debugiso");            
            // TODO: this is working, has center movement backed in.
            IsoSurface surface = new DebugIsoSurface(vol);
System.out.println("Running computeSurface");
            surface.computeSurfaceMesh(mesh, 0.4f);
        }

        vol = null;
        System.gc();

        boolean checkManifold = false;

        System.out.println("Running flipverts");
        
        // TODO: Fix this
        mesh.flipVertexOrder();

        int orig_faces = mesh.getNumFaces();
        
        System.out.println("Initial Mesh: faces: " + mesh.getNumFaces() + " verts: " + mesh.getNumVertices());
        boolean manifold = true;

        if (checkManifold) {
            System.out.println("Checking manifold output from initial stage: " );
            manifold = isManifold(mesh);
            System.out.println("manifold: " + manifold);

        } else {
            manifold = true;
        }



        if (simplifier != null) {
            System.out.println("Simplifying mesh");
            
            if (!manifold) {
                System.out.println("WARNING: Non manifold mesh in simplifier.");
            }
            simplifier.execute(mesh, grid);
            System.out.println("Checking manifold output from simplification stage: " );

            if (checkManifold) {
                System.out.println("manifold: " + isManifold(mesh));
            }
        }


        SAVExporter exporter = new SAVExporter();
        exporter.outputX3D(mesh, params, writer);
        
        System.out.println("Mesh: faces: " + mesh.getFaces().size() + " verts: " + mesh.getNumVertices());
        
        System.out.println("Orig faces: " + orig_faces + " final: " + mesh.getFaces().size() + " %: " +
        ((float)mesh.getFaces().size() / orig_faces));
        // End Centering Transform
        writer.endField();
        writer.endNode();

    }

    /**
     * Get the vertices to edges
     * @param grid
     * @param x
     * @param y
     * @param z
     * @return
     */
    protected final int getCellIndex(Grid grid, int x, int y, int z) {
        int cellIndex = 0;
        byte state;

        // Vertex 0
        state = grid.getState(x,y,z);
        //System.out.print("state: " + state);
        if (state == Grid.OUTSIDE) {
            cellIndex |= 1;
        }

        // Vertex 3

        if (z >= depth) {
            cellIndex |= 8;
        } else {
            state = grid.getState(x,y,z+1);
            //System.out.print(" " + state);
            if (state == Grid.OUTSIDE) {
                cellIndex |= 8;
            }
        }

        // Vertex 4
        if (y >= height) {
            cellIndex |= 16;
        } else {
            state = grid.getState(x,y+1,z);
            //System.out.print(" " + state);
            if (state == Grid.OUTSIDE) {
                cellIndex |= 16;
            }
        }

        if (y >= height) {
            cellIndex |= 128;
        } else {
            // Vertex 7
            state = grid.getState(x,y+1,z+1);
            //System.out.print(" " + state);
            if (state == Grid.OUTSIDE) {
                cellIndex |= 128;
            }
        }

        x = x + 1;

        if (x >= width) {
            cellIndex |= 102;
            return cellIndex;
        }

        // Vertex 1
        state = grid.getState(x,y,z);
        //System.out.print(" " + state);
        if (state == Grid.OUTSIDE) {
            cellIndex |= 2;
        }

        // Vertex 2
        state = grid.getState(x,y,z+1);
        //System.out.print(" " + state);
        if (state == Grid.OUTSIDE) {
            cellIndex |= 4;
        }

        // Vertex 5
        state = grid.getState(x,y+1,z);
        //System.out.print(" " + state);
        if (state == Grid.OUTSIDE) {
            cellIndex |= 32;
        }

        // Vertex 6
        state = grid.getState(x,y+1,z+1);
        //System.out.println(" " + state);
        if (state == Grid.OUTSIDE) {
            cellIndex |= 64;
        }

        //System.out.println("gci: " + x + " " + y + " " + z + " cellIndex: " + cellIndex + " bits: " + Integer.toBinaryString(cellIndex));
        return cellIndex;
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

        BoxSimplifiedX3DExporter exporter = new BoxSimplifiedX3DExporter(writer,console,complete);
        
        exporter.writeDebug(grid, stateColors, stateTransparency);
    }

    /**
     * Close the exporter.  Must be called when done.
     */
    public void close() {
        if (complete)
            ejectFooter();
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
    
    private boolean isManifold(WETriangleMesh mesh) {
        boolean manifold = true;

        for(WingedEdge edge : mesh.getEdges()) {
            int size = edge.getFaces().size();

            if (size != 2 && size != 0)  {
                System.out.println("Non manifold: " + edge);
                manifold = false;
            }
        }

        return manifold;
    }
}