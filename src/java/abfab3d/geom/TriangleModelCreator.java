/*****************************************************************************
 *                        Alan Hudson Copyright (c) 2011
 *                               Java Source
 *
 * This source is private and not licensed for any use.
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
import toxi.geom.Vec3D;
import toxi.math.MathUtils;
import org.web3d.util.spatial.Triangle;
import javax.vecmath.*;

// Internal Imports
import abfab3d.grid.Grid;

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
public class TriangleModelCreator extends GeometryCreator {
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

/*
        if (geom.geometryType != GeometryData.TRIANGLES) {
            throw IllegalArgumentException("Unsupported geometryType: " + geom.geometryType);
        }
*/
    }

    /**
     * Generate the geometry and issue commands to the provided handler.
     *
     * @param handler The stream to issue commands
     */
    public void generate(Grid grid) {

        voxelSize = grid.getVoxelSize();
        halfVoxel = voxelSize / 2.0;
        sliceHeight = grid.getSliceHeight();
        halfSlice = sliceHeight / 2.0;

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

        // Find interior voxels using ray casts.  Color Voxels.


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

        grid.getGridBounds(minGridWorldCoord, minGridWorldCoord);

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

        double[] vcoords = new double[6];
        for(int x = 0; x < len_x; x++) {
            for(int y = 0; y < len_y; y++) {
                for(int z = 0; z < len_z; z++) {
                    i = min[0] + x;
                    j = min[1] + y;
                    k = min[2] + z;


                    grid.getWorldCoords(i,j,k,vcoords);

                    Vec3D v0 = new Vec3D(tri.coords[0], tri.coords[1], tri.coords[2]);
                    Vec3D v1 = new Vec3D(tri.coords[3], tri.coords[4], tri.coords[5]);
                    Vec3D v2 = new Vec3D(tri.coords[6], tri.coords[7], tri.coords[8]);

                    if (intersectsTriangle(v0,v1,v2, vcoords)) {
                        grid.setData(i,j,k,Grid.EXTERIOR,material);

                        if (COLLECT_STATS) {
                            cellsFilled++;
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
    public boolean intersectsTriangle(Vec3D a, Vec3D b, Vec3D c, double[] pos) {
        // use separating axis theorem to test overlap between triangle and box
        // need to test for overlap in these directions:
        //
        // 1) the {x,y,z}-directions (actually, since we use the AABB of the
        // triangle
        // we do not even need to test these)
        // 2) normal of the triangle
        // 3) crossproduct(edge from tri, {x,y,z}-directin)
        // this gives 3x3=9 more tests
        Vec3D v0, v1, v2;
        Vec3D normal, e0, e1, e2, f;

        // move everything so that the boxcenter is in (0,0,0)
        v0 = a.sub((float)pos[0],(float)pos[1],(float)pos[2]);
        v1 = b.sub((float)pos[0],(float)pos[1],(float)pos[2]);
        v2 = c.sub((float)pos[0],(float)pos[1],(float)pos[2]);

        // compute triangle edges
        e0 = v1.sub(v0);

// TODO: Need to change y values to hHeight

        // test the 9 tests first (this was faster)
        f = e0.getAbs();
        if (testAxis(e0.z, -e0.y, f.z, f.y, v0.y, v0.z, v2.y, v2.z, (float) halfVoxel,
                (float) halfVoxel)) {
            return false;
        }
        if (testAxis(-e0.z, e0.x, f.z, f.x, v0.x, v0.z, v2.x, v2.z, (float) halfVoxel,
                (float) halfVoxel)) {
            return false;
        }
        if (testAxis(e0.y, -e0.x, f.y, f.x, v1.x, v1.y, v2.x, v2.y, (float) halfVoxel,
                (float) halfVoxel)) {
            return false;
        }

        e1 = v2.sub(v1);
        f = e1.getAbs();
        if (testAxis(e1.z, -e1.y, f.z, f.y, v0.y, v0.z, v2.y, v2.z, (float) halfVoxel,
                (float) halfVoxel)) {
            return false;
        }
        if (testAxis(-e1.z, e1.x, f.z, f.x, v0.x, v0.z, v2.x, v2.z, (float) halfVoxel,
                (float) halfVoxel)) {
            return false;
        }
        if (testAxis(e1.y, -e1.x, f.y, f.x, v0.x, v0.y, v1.x, v1.y, (float) halfVoxel,
                (float) halfVoxel)) {
            return false;
        }

        e2 = v0.sub(v2);
        f = e2.getAbs();

        if (testAxis(e2.z, -e2.y, f.z, f.y, v0.y, v0.z, v1.y, v1.z, (float) halfVoxel,
                (float) halfVoxel)) {
            return false;
        }
        if (testAxis(-e2.z, e2.x, f.z, f.x, v0.x, v0.z, v1.x, v1.z, (float) halfVoxel,
                (float) halfVoxel)) {
            return false;
        }
        if (testAxis(e2.y, -e2.x, f.y, f.x, v1.x, v1.y, v2.x, v2.y, (float) halfVoxel,
                (float) halfVoxel)) {
            return false;
        }

        // first test overlap in the {x,y,z}-directions
        // find min, max of the triangle each direction, and test for overlap in
        // that direction -- this is equivalent to testing a minimal AABB around
        // the triangle against the AABB

        // test in X-direction
        if (MathUtils.min(v0.x, v1.x, v2.x) > halfVoxel
                || MathUtils.max(v0.x, v1.x, v2.x) < -halfVoxel) {
            return false;
        }

        // test in Y-direction
        if (MathUtils.min(v0.y, v1.y, v2.y) > halfVoxel
                || MathUtils.max(v0.y, v1.y, v2.y) < -halfVoxel) {
            return false;
        }

        // test in Z-direction
        if (MathUtils.min(v0.z, v1.z, v2.z) > halfVoxel
                || MathUtils.max(v0.z, v1.z, v2.z) < -halfVoxel) {
            return false;
        }

        // test if the box intersects the plane of the triangle
        // compute plane equation of triangle: normal*x+d=0
        normal = e0.cross(e1);
        float d = -normal.dot(v0);
        if (!planeBoxOverlap(normal, d, (float) halfVoxel)) {
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
    private boolean planeBoxOverlap(Vec3D normal, float d, float hv) {

// TODO: Need to change to include sheight
        Vec3D vmin = new Vec3D();
        Vec3D vmax = new Vec3D();

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
    private boolean testAxis(float a, float b, float fa, float fb, float va,
            float vb, float wa, float wb, float ea, float eb) {
        float p0 = a * va + b * vb;
        float p2 = a * wa + b * wb;
        float min, max;
        if (p0 < p2) {
            min = p0;
            max = p2;
        } else {
            min = p2;
            max = p0;
        }
        float rad = fa * ea + fb * eb;
        return (min > rad || max < -rad);
    }


}
