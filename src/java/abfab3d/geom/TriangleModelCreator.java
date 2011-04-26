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

package abfab3d.geom;

// External Imports
import java.util.*;
import java.io.*;
import org.web3d.vrml.sav.ContentHandler;
import org.j3d.geom.*;
import org.web3d.util.spatial.Triangle;
import javax.vecmath.*;

// Internal Imports
import abfab3d.grid.*;

/**
 * Creates a model from a 3D Triangle model.
 *
 * Backed from a 3D Triangle model.  Shows different sourcing of
 * models to get into voxels.
 *
 * Only supports TRIANGLES currently.
 *
 * @author Alan Hudson
 */
public class TriangleModelCreator extends GeometryCreator implements ClassTraverser {
    public static final boolean COLLECT_STATS = true;

    // Marker values for cell type
    public static final byte OUTER_CELL = 1;
    public static final byte INNER_CELL = 2;

    /** The triangle data */
    protected GeometryData geom;

    protected double width;
    protected double height;
    protected double depth;

    // The center position
    protected double x;
    protected double y;
    protected double z;

    // Rotation to apply
    protected double rx;
    protected double ry;
    protected double rz;
    protected double rangle;

    protected byte outerMaterialID;
    protected byte innerMaterialID;

    /** Scratch variables */
    protected double[] minBounds;
    protected double[] maxBounds;
    protected int[] minCoords;
    protected int[] maxCoords;

    protected double voxelSize;
    protected double halfVoxel;
    protected double sliceHeight;
    protected double halfSlice;
    protected boolean fill;

    // Stats Vars
    private int cellsFilled;

    // Scratch Vars
    private Vector3d v0;
    private Vector3d v1;
    private Vector3d v2;
    private Vector3d normal;
    private Vector3d e0;
    private Vector3d e1;
    private Vector3d e2;
    private Vector3d f;
    private Vector3d vpos;
    private Grid grid;

    /**
     * Constructor.
     *
     * @param styles The styles for each face.  Front,Back,Left,Right,Top,Bottom
     * @param fill Should we fill in interior voxels or just leave a shell.
     */
    public TriangleModelCreator(GeometryData geom,
        double x, double y, double z, double rx, double ry, double rz, double rangle,
        byte outerMaterial, byte innerMaterial, boolean fill) {

        this.geom = geom;
        this.x = x;
        this.y = y;
        this.z = z;
        this.outerMaterialID = outerMaterial;
        this.innerMaterialID = innerMaterial;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
        this.rangle = rangle;
        this.fill = fill;

        minBounds = new double[3];
        maxBounds = new double[3];
        minCoords = new int[3];
        maxCoords = new int[3];

        v0 = new Vector3d();
        v1 = new Vector3d();
        v2 = new Vector3d();
        normal = new Vector3d();
        e0 = new Vector3d();
        e1 = new Vector3d();
        e2 = new Vector3d();
        f = new Vector3d();
        vpos = new Vector3d();

        if (geom.geometryType != GeometryData.TRIANGLES) {
            throw new IllegalArgumentException("Unsupported geometryType: " + geom.geometryType);
        }
    }

    /**
     * Generate the geometry and issue commands to the provided handler.
     *
     * @param handler The stream to issue commands
     */
    public void generate(Grid grid) {
        this.grid = grid;
        voxelSize = grid.getVoxelSize();
        halfVoxel = voxelSize / 2.0;
        sliceHeight = grid.getSliceHeight();
        halfSlice = sliceHeight / 2.0;
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        // TODO: We should move both of these to grid ops for better reuse


        // Find exterior voxels using triangle/voxel overlaps.  Color Voxels.

        Matrix4d mat = new Matrix4d();
        mat.setIdentity();
        mat.setTranslation(new Vector3d(x,y,z));

        if (rangle != 0)
            mat.setRotation(new AxisAngle4d(rx,ry,rz,rangle));

        int idx = 0;
        Triangle tri;
        float[] coords = new float[9];
        int len = geom.vertexCount / 3;

        for(int i=0; i < len; i++ ) {
//System.out.println("Input coord: " + geom.coordinates[idx] + " " + geom.coordinates[idx+1] + " " + geom.coordinates[idx+2]);
//System.out.println("Input coord: " + geom.coordinates[idx+3] + " " + geom.coordinates[idx+4] + " " + geom.coordinates[idx+5]);
//System.out.println("Input coord: " + geom.coordinates[idx+6] + " " + geom.coordinates[idx+7] + " " + geom.coordinates[idx+8]);
            Point3d v = new Point3d(geom.coordinates[idx++],
                geom.coordinates[idx++],geom.coordinates[idx++]);

            mat.transform(v);
            coords[0] = (float) v.x;
            coords[1] = (float) v.y;
            coords[2] = (float) v.z;

            v = new Point3d(geom.coordinates[idx++],
                geom.coordinates[idx++],geom.coordinates[idx++]);
            mat.transform(v);
            coords[3] = (float) v.x;
            coords[4] = (float) v.y;
            coords[5] = (float) v.z;

            v = new Point3d(geom.coordinates[idx++],
                geom.coordinates[idx++],geom.coordinates[idx++]);
            mat.transform(v);
            coords[6] = (float) v.x;
            coords[7] = (float) v.y;
            coords[8] = (float) v.z;

            tri = new Triangle(coords, i);
            insert(tri, grid, (byte) OUTER_CELL);
        }

        if (!fill)
            return;

        Grid result = new SliceGrid(grid.getWidth(),grid.getHeight(),grid.getDepth(),
            grid.getVoxelSize(), grid.getSliceHeight(), false);

//System.out.println("Filling model");
        byte state;
        byte last = Grid.OUTSIDE;
        int cnt = 0;
        int status = 0;  // 0 = outside, 1 == coming into exterior, 2 == coming out inside, 3 == inside

        // Find interior voxels using in/out tests
        // March across XAXIS
        for(int y=0; y < height; y++) {
            for(int z=0; z < depth; z++) {
                status = 0;
                for(int x=0; x < width; x++) {
                    state = grid.getState(x,y,z);

//System.out.println("test: " + x + " " + y + " " + z + " state: " + state + " status: " + status);
                    if (status == 0) {
                        if (state == Grid.EXTERIOR) {
//System.out.println("Found exterior at: " + x + " " + y + " " + z);
                            status = 1;
                        } else if (state == Grid.INTERIOR) {
                            // No exterior voxel found?
                            status = 3;
                        }
                    } else if (status == 1) {
                        if (state == Grid.OUTSIDE) {
//System.out.println("Found inside at1: " + x + " " + y + " " + z);
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterialID);
                            cnt++;
                            status = 3;
                            continue;
                        } else if (state == Grid.INTERIOR) {
//System.out.println("Found inside at2: " + x + " " + y + " " + z);
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterialID);
                            cnt++;
                            status = 3;
                        }
                    } else if (status == 2) {
                        if (state == Grid.OUTSIDE) {
                            status = 0;
                        } else if (state == Grid.INTERIOR) {
                            status = 3;
                        }
                    } else if (status == 3) {
                        if (state == Grid.OUTSIDE) {
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterialID);
                            cnt++;
                            continue;
                        } else if (state == Grid.INTERIOR) {
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterialID);
                            cnt++;
                            continue;
                        } else if (state == Grid.EXTERIOR) {
//System.out.println("Exiting at1: " + x + " " + y + " " + z);
                            status = 2;
                        }
                    }
                }
            }
        }

System.out.println("XAXIS Interior: " + result.findCount(Grid.VoxelClasses.INTERIOR));
        // March across YAXIS
        for(int x=0; x < width; x++) {
            for(int z=0; z < depth; z++) {
                status = 0;
                for(int y=0; y < height; y++) {
                    state = grid.getState(x,y,z);

//System.out.println("test: " + x + " " + y + " " + z + " state: " + state + " status: " + status);

                    if (status == 0) {
                        if (state == Grid.EXTERIOR) {
//System.out.println("Found exterior at: " + x + " " + y + " " + z);
                            status = 1;
                        } else if (state == Grid.INTERIOR) {
                            // No exterior voxel found?
                            status = 3;
                            continue;
                        }
                    } else if (status == 1) {
                        if (state == Grid.OUTSIDE) {
//System.out.println("Found inside at1: " + x + " " + y + " " + z);
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterialID);
                            cnt++;
                            status = 3;
                            continue;
                        } else if (state == Grid.INTERIOR) {
//System.out.println("Found inside at1: " + x + " " + y + " " + z);
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterialID);
                            cnt++;
                            status = 3;
                        }
                    } else if (status == 2) {
                        if (state == Grid.OUTSIDE) {
                            status = 0;
                        } else if (state == Grid.INTERIOR) {
                            status = 3;
                            continue;
                        }
                    } else if (status == 3) {
                        if (state == Grid.OUTSIDE) {
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterialID);
                            cnt++;
                            continue;
                        } else if (state == Grid.INTERIOR) {
                            result.setData(x,y,z,Grid.INTERIOR,innerMaterialID);
                            cnt++;
                            continue;
                        } else if (state == Grid.EXTERIOR) {
//System.out.println("Exiting at1: " + x + " " + y + " " + z);
                            status = 2;
                        }
                    }

                    result.setData(x,y,z,Grid.OUTSIDE,(byte)0);
                }
            }
        }

System.out.println("YAXIS Interior: " + result.findCount(Grid.VoxelClasses.INTERIOR));

//System.out.println("*****");

        status = 0;
        // March across ZAXIS
        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                status = 0;
                for(int z=0; z < depth; z++) {
                    state = grid.getState(x,y,z);

//System.out.println("test: " + x + " " + y + " " + z + " state: " + state + " status: " + status);

                    if (status == 0) {
                        if (state == Grid.EXTERIOR) {
//System.out.println("Found exterior at: " + x + " " + y + " " + z);
                            status = 1;
                        } else if (state == Grid.INTERIOR) {
                            // No exterior voxel found?
                            status = 3;
                        }
                    } else if (status == 1) {
                        if (state == Grid.OUTSIDE) {
//System.out.println("Found inside at1: " + x + " " + y + " " + z);
                            if (result.getState(x,y,z) == Grid.INTERIOR) {
                                cnt++;
                                status = 3;
                                continue;
                            }
                        } else if (state == Grid.INTERIOR) {
//System.out.println("Found inside at1: " + x + " " + y + " " + z);
                            if (result.getState(x,y,z) == Grid.INTERIOR) {
                                cnt++;
                                status = 3;
                            }
                        }
                    } else if (status == 2) {
                        if (state == Grid.OUTSIDE) {
                            status = 0;
                        } else if (state == Grid.INTERIOR) {
                            status = 3;
                            continue;
                        }
                    } else if (status == 3) {
                        if (state == Grid.OUTSIDE) {
                            if (result.getState(x,y,z) == Grid.INTERIOR) {
                                result.setData(x,y,z,Grid.INTERIOR,innerMaterialID);
                                cnt++;
                                continue;
                            }
                        } else if (state == Grid.INTERIOR) {
                            if (result.getState(x,y,z) == Grid.INTERIOR) {
                                result.setData(x,y,z,Grid.INTERIOR,innerMaterialID);
                                cnt++;
                                continue;
                            }
                        } else if (state == Grid.EXTERIOR) {
//System.out.println("Exiting at1: " + x + " " + y + " " + z);
                            status = 2;
                        }
                    }

                    result.setData(x,y,z,Grid.OUTSIDE,(byte)0);
                }
            }
        }

System.out.println("ZAXIS Interior: " + result.findCount(Grid.VoxelClasses.INTERIOR));

        result.find(Grid.VoxelClasses.INTERIOR, this);

        grid = null;
    }

    /**
     * A voxel of the class requested has been found.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public void found(int x, int y, int z, VoxelData vd) {
        grid.setData(x,y,z,Grid.INTERIOR, innerMaterialID);
    }

    /**
     * Insert an object into the structure.
     *
     * @param tri The triangle
     * @param grid The grid to use
     */
    public void insert(Triangle tri, Grid grid, byte type) {

        tri.calcBounds(minBounds, maxBounds);

        double[] minGridWorldCoord = new double[3];
        double[] maxGridWorldCoord = new double[3];

        grid.getGridBounds(minGridWorldCoord, maxGridWorldCoord);

/*
System.out.println("Grid Min: " + java.util.Arrays.toString(minGridWorldCoord));
System.out.println("Grid Max: " + java.util.Arrays.toString(maxGridWorldCoord));
*/
        grid.getGridCoords(minBounds[0], minBounds[1], minBounds[2], minCoords);
        grid.getGridCoords(maxBounds[0], maxBounds[1], maxBounds[2], maxCoords);

/*
System.out.println("Triangle: " + java.util.Arrays.toString(tri.coords));
System.out.println("minBounds: " + java.util.Arrays.toString(minBounds));
System.out.println("maxBounds: " + java.util.Arrays.toString(maxBounds));
System.out.flush();
*/

        // Handle on voxel boundary issues
        if (minBounds[0] % voxelSize == 0) {
            minBounds[0] -= halfVoxel;
            if (minBounds[0] < minGridWorldCoord[0]) {
                minBounds[0] = minGridWorldCoord[0];
            }
        }

        if (minBounds[2] % voxelSize == 2) {
            minBounds[2] -= halfVoxel;
            if (minBounds[2] < minGridWorldCoord[2]) {
                minBounds[2] = minGridWorldCoord[2];
            }
        }

        if (minBounds[1] % sliceHeight == 0) {
            minBounds[1] -= halfSlice;
            if (minBounds[1] < minGridWorldCoord[1]) {
                minBounds[1] = minGridWorldCoord[1];
            }
        }

        if (maxBounds[0] % voxelSize == 0) {
            maxBounds[0] += halfVoxel;
            if (maxBounds[0] > maxGridWorldCoord[0]) {
                maxBounds[0] = maxGridWorldCoord[0];
            }
        }

        if (maxBounds[2] % voxelSize == 2) {
            maxBounds[2] += halfVoxel;
            if (maxBounds[2] > minGridWorldCoord[2]) {
                maxBounds[2] = minGridWorldCoord[2];
            }
        }

        if (maxBounds[1] % sliceHeight == 0) {
            maxBounds[1] += halfSlice;
            if (maxBounds[1] > maxGridWorldCoord[1]) {
                maxBounds[1] = maxGridWorldCoord[1];
            }
        }

/*
System.out.println("after bounds check");
System.out.println("minBounds: " + java.util.Arrays.toString(minBounds));
System.out.println("maxBounds: " + java.util.Arrays.toString(maxBounds));
System.out.flush();
*/

        grid.getGridCoords(minBounds[0], minBounds[1], minBounds[2], minCoords);
        grid.getGridCoords(maxBounds[0], maxBounds[1], maxBounds[2], maxCoords);
/*
System.out.println("minCoords: " + java.util.Arrays.toString(minCoords));
System.out.println("maxCoords: " + java.util.Arrays.toString(maxCoords));
System.out.flush();
*/
        fillCellsExact(minCoords, maxCoords, tri, grid, type);
    }

    /**
     * Fill in the grid given a box of grid coordinates.
     *
     * @param min The min bounds in cell coords
     * @param max The max bounds in cell coords
     * @param id The ID to fill in
     */
    protected void fillCellsExact(int[] min, int[] max, Triangle tri, Grid grid, byte material) {
        final int len_x = max[0] - min[0] + 1;
        final int len_y = max[1] - min[1] + 1;
        final int len_z = max[2] - min[2] + 1;

        int i,j,k;

        Vector3d v0 = new Vector3d(tri.coords[0], tri.coords[1], tri.coords[2]);
        Vector3d v1 = new Vector3d(tri.coords[3], tri.coords[4], tri.coords[5]);
        Vector3d v2 = new Vector3d(tri.coords[6], tri.coords[7], tri.coords[8]);

        double[] vcoords = new double[3];
        for(int x = 0; x < len_x; x++) {
            for(int y = 0; y < len_y; y++) {
                for(int z = 0; z < len_z; z++) {
                    i = min[0] + x;
                    j = min[1] + y;
                    k = min[2] + z;


                    grid.getWorldCoords(i,j,k,vcoords);
                    if (intersectsTriangle(v0,v1,v2, vcoords)) {
//System.out.println("Set data: " + i + " " + j + " " + k);
                        grid.setData(i,j,k,Grid.EXTERIOR,material);

                        if (COLLECT_STATS) {
                            cellsFilled ++;
                        }

                    }
                }
            }
        }
    }

    /**
     * Does triangle overlap a voxel.
     *
     * From paper: Fast 3D Triangle-Box Overlap Testing
     * TODO: this paper mentions having errors with long thin polygons
     *
     * @param tri The triangle
     * @param pos The voxel center position
     */
    public boolean intersectsTriangle(Vector3d a, Vector3d b, Vector3d c, double[] pos) {
        // use separating axis theorem to test overlap between triangle and box
        // need to test for overlap in these directions:
        //
        // 1) the {x,y,z}-directions (actually, since we use the AABB of the
        // triangle
        // we do not even need to test these)
        // 2) normal of the triangle
        // 3) crossproduct(edge from tri, {x,y,z}-directin)
        // this gives 3x3=9 more tests

        // move everything so that the boxcenter is in (0,0,0)
        Vector3d vpos = new Vector3d(pos[0],pos[1],pos[2]);
        v0.sub(a,vpos);
        v1.sub(b,vpos);
        v2.sub(c,vpos);

        // compute triangle edges
        e0.sub(v1,v0);

// TODO: Need to change y values to hHeight

        // test the 9 tests first (this was faster)
        f.absolute(e0);
        if (testAxis(e0.z, -e0.y, f.z, f.y, v0.y, v0.z, v2.y, v2.z, halfVoxel,
                 halfVoxel)) {
            return false;
        }
        if (testAxis(-e0.z, e0.x, f.z, f.x, v0.x, v0.z, v2.x, v2.z,  halfVoxel,
                 halfVoxel)) {
            return false;
        }
        if (testAxis(e0.y, -e0.x, f.y, f.x, v1.x, v1.y, v2.x, v2.y,  halfVoxel,
                 halfVoxel)) {
            return false;
        }

        e1.sub(v2,v1);
        f.absolute(e1);
        if (testAxis(e1.z, -e1.y, f.z, f.y, v0.y, v0.z, v2.y, v2.z,  halfVoxel,
                 halfVoxel)) {
            return false;
        }
        if (testAxis(-e1.z, e1.x, f.z, f.x, v0.x, v0.z, v2.x, v2.z,  halfVoxel,
                 halfVoxel)) {
            return false;
        }
        if (testAxis(e1.y, -e1.x, f.y, f.x, v0.x, v0.y, v1.x, v1.y,  halfVoxel,
                 halfVoxel)) {
            return false;
        }

        e2.sub(v0,v2);
        f.absolute(e2);

        if (testAxis(e2.z, -e2.y, f.z, f.y, v0.y, v0.z, v1.y, v1.z,  halfVoxel,
                 halfVoxel)) {
            return false;
        }
        if (testAxis(-e2.z, e2.x, f.z, f.x, v0.x, v0.z, v1.x, v1.z,  halfVoxel,
                 halfVoxel)) {
            return false;
        }
        if (testAxis(e2.y, -e2.x, f.y, f.x, v1.x, v1.y, v2.x, v2.y,  halfVoxel,
                 halfVoxel)) {
            return false;
        }

        // first test overlap in the {x,y,z}-directions
        // find min, max of the triangle each direction, and test for overlap in
        // that direction -- this is equivalent to testing a minimal AABB around
        // the triangle against the AABB

        // test in X-direction
        if (min(v0.x, v1.x, v2.x) > halfVoxel
                || max(v0.x, v1.x, v2.x) < -halfVoxel) {
            return false;
        }

        // test in Y-direction
        if (min(v0.y, v1.y, v2.y) > halfVoxel
                || max(v0.y, v1.y, v2.y) < -halfVoxel) {
            return false;
        }

        // test in Z-direction
        if (min(v0.z, v1.z, v2.z) > halfVoxel
                || max(v0.z, v1.z, v2.z) < -halfVoxel) {
            return false;
        }

        // test if the box intersects the plane of the triangle
        // compute plane equation of triangle: normal*x+d=0
        normal.cross(e0,e1);
        double d = -normal.dot(v0);
        if (!planeBoxOverlap(normal, d,  halfVoxel)) {
            return false;
        }

        return true;
    }

    /**
     * Does a plane and box overlap.
     *
     * @param normal Normal to the plane
     * @param d Distance
     * @param hv Half voxel size
     */
    private boolean planeBoxOverlap(Vector3d normal, double d, double hv) {

// TODO: Need to change to include sheight
        Vector3d vmin = new Vector3d();
        Vector3d vmax = new Vector3d();

        if (normal.x > 0.0f) {
            vmin.x = -hv;
            vmax.x = hv;
        } else {
            vmin.x = hv;
            vmax.x = -hv;
        }

        if (normal.y > 0.0f) {
            vmin.y = -hv;
            vmax.y = hv;
        } else {
            vmin.y = hv;
            vmax.y = -hv;
        }

        if (normal.z > 0.0f) {
            vmin.z = -hv;
            vmax.z = hv;
        } else {
            vmin.z = hv;
            vmax.z = -hv;
        }
        if (normal.dot(vmin) + d > 0.0f) {
            return false;
        }
        if (normal.dot(vmax) + d >= 0.0f) {
            return true;
        }
        return false;
    }

    /**
     * Test and axis intersection.
     */
    private boolean testAxis(double a, double b, double fa, double fb, double va,
            double vb, double wa, double wb, double ea, double eb) {

        double p0 = a * va + b * vb;
        double p2 = a * wa + b * wb;
        double min, max;
        if (p0 < p2) {
            min = p0;
            max = p2;
        } else {
            min = p2;
            max = p0;
        }
        double rad = fa * ea + fb * eb;

        return (min > rad || max < -rad);
    }

    /**
     * Calculate the minimum of 3 values.
     *
     * @param a Value 1
     * @param b Value 2
     * @param c Value 3
     *
     * @return The min value
     */
    private double min(double a, double b, double c) {
        return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
    }

    /**
     * Calculate the maximum of 3 values.
     *
     * @param a Value 1
     * @param b Value 2
     * @param c Value 3
     *
     * @return The min value
     */
    public double max(double a, double b, double c) {
        return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
    }
}
