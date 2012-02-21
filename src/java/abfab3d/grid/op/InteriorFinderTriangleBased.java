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

package abfab3d.grid.op;

// External Imports
import java.util.*;
import java.io.*;

import abfab3d.intersect.TriangleIntersectionDoubleUtil;
import abfab3d.util.MatrixUtil;
import org.web3d.util.spatial.AllRegion;
import org.web3d.util.spatial.GridTrianglePartition;
import org.web3d.util.spatial.TunnelRegion;
import org.web3d.vrml.sav.ContentHandler;
import org.j3d.geom.*;
import org.web3d.util.spatial.Triangle;
import javax.vecmath.*;

// Internal Imports
import abfab3d.grid.*;

/**
 * Find the interior voxels of a grid.  Casts rays to determine the interior state.
 *
 * @author Alan Hudson
 */
public class InteriorFinderTriangleBased implements Operation, ClassTraverser {
    /** Should we use spatial acceleration */
    private static final boolean SPATIAL_ACCEL = true;

    /** The material to process */
    protected int material;

    /** The material to use for new voxels */
    protected int innerMaterial;

    // The center position
    protected double x;
    protected double y;
    protected double z;

    // Rotation to apply
    protected double rx;
    protected double ry;
    protected double rz;
    protected double rangle;

    /** The grid we are operating on */
    private Grid gridOp;

    /** Do we need to transform the points */
    private boolean needTransform;

    /** The triangle geometry */
    private GeometryData geom;
    
    /** The bounds of the geometry */
    private float[] bounds;

    /** Spatial data structure to speed ray / box intersects */
    private GridTrianglePartition spatial;

    /** The temporary result grid */
    private Grid result;

    /** The origin to convert result grid back to real grid */
    private int[] origin;

    /** The number of voxels per side in the spatial structure */
    private int numVoxels;

    private int int_count;
    
    /**
     * Constructor.
     *
     * @param material The materialID of exterior voxels
     * @param newMaterial The materialID to assign new interior voxels
     */
    public InteriorFinderTriangleBased(GeometryData geom, float[] bounds, int material, int newMaterial) {
        this.geom = geom;
        this.material = material;
        this.innerMaterial = newMaterial;
        this.bounds = bounds.clone();

        needTransform = false;
        origin = new int[3];
    }

    /**
    * @param x The x translation applied before voxelization
    * @param y The y translation applied before voxelization
    * @param z The z translation applied before voxelization
    * @param rx The x rotation applied before voxelization
    * @param ry The y rotation applied before voxelization
    * @param rz The z rotation applied before voxelization
    * @param rangle The angle rotation applied before voxelization
    */
    public InteriorFinderTriangleBased(GeometryData geom, float[] bounds,
                                       double x, double y, double z, double rx, double ry, double rz, double rangle,
                                       int material, int newMaterial) {
        this.geom = geom;
        this.material = material;
        this.innerMaterial = newMaterial;
        this.bounds = bounds.clone();

        this.x = x;
        this.y = y;
        this.z = z;
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
        this.rangle = rangle;

        needTransform = true;   // TODO: should check that transform is non identity
        origin = new int[3];
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid A.
     * @return The new grid
     */
    public Grid execute(Grid grid) {
        gridOp = grid;
        
        // TODO: Assume we have enough material entries to count num interiors
        result = grid.createEmpty(grid.getWidth(),grid.getHeight(),grid.getDepth(),
                grid.getVoxelSize(), grid.getSliceHeight());

        // TODO: Debug, remove me
        result = new RangeCheckWrapper(result);

        if (needTransform) {
            float[] coords = new float[geom.coordinates.length];
            
            Matrix4d mat = MatrixUtil.createMatrix(
                    new double[]{0, 0, 0},
                    new double[]{1, 1, 1}, new double[]{rx, ry, rz, rangle}, new double[]{x, y, z},
                    new double[]{0, 0, 1, 0});

            Point3d v = new Point3d();

            if (geom.geometryType == GeometryData.TRIANGLES) {
                int len = geom.vertexCount;
            
                int idx = 0;
                for(int i=0; i < len; i++) {
                    v.x = geom.coordinates[idx];
                    v.y = geom.coordinates[idx+1];
                    v.z = geom.coordinates[idx+2];

                    mat.transform(v);
                    coords[idx++] = (float) v.x;
                    coords[idx++] = (float) v.y;
                    coords[idx++] = (float) v.z;
                }

                // Be safe and copy over structures
                GeometryData new_geom = new GeometryData();
                new_geom.geometryType = GeometryData.TRIANGLES;
                new_geom.coordinates = coords;
                new_geom.vertexCount = geom.vertexCount;

                geom = new_geom;

            } else if (geom.geometryType == GeometryData.INDEXED_TRIANGLES) {
                int len = geom.indexesCount;

                int idx = 0;
                int loc = 0;
                
                for(int i=0; i < len; i++) {
                
                    loc = geom.indexes[idx++] * 3;
                    v.x = geom.coordinates[loc];
                    v.y = geom.coordinates[loc + 1];
                    v.z = geom.coordinates[loc + 2];
    
                    mat.transform(v);

                    coords[loc] = (float) v.x;
                    coords[loc + 1] = (float) v.y;
                    coords[loc + 2] = (float) v.z;
                }
                // Be safe and copy over structures
                GeometryData new_geom = new GeometryData();
                new_geom.geometryType = GeometryData.INDEXED_TRIANGLES;
                new_geom.coordinates = coords;
                new_geom.vertexCount = geom.vertexCount;
                new_geom.indexes = geom.indexes.clone();
                new_geom.indexesCount = geom.indexesCount;

                geom = new_geom;

            }


            // Transform bounds
            Point3d min = new Point3d(bounds[0], bounds[2], bounds[4]);
            Point3d max = new Point3d(bounds[1], bounds[3], bounds[5]);
            
            mat.transform(min);
            mat.transform(max);
            
            bounds[0] = (float) min.x;
            bounds[2] = (float) min.y;
            bounds[4] = (float) min.z;
            bounds[1] = (float) max.x;
            bounds[3] = (float) max.y;
            bounds[5] = (float) max.z;
        }
        
        int[] min = new int[3];
        int[] max = new int[3];
        
        grid.getGridCoords(bounds[0], bounds[2], bounds[4], min);
        grid.getGridCoords(bounds[1], bounds[3], bounds[5], max);

        spatial = initSpatial(grid,geom,grid.getVoxelSize());

        int[] spat_min = new int[3];
        int[] spat_max = new int[3];
        spatial.findGridCoordsFromWorldCoords(new float[] {bounds[0], bounds[2], bounds[4]}, spat_min);
        spatial.findGridCoordsFromWorldCoords(new float[] {bounds[1], bounds[3], bounds[5]}, spat_max);

        double[] pos = new double[3];
        int[] tris = null;

        // TODO: force whole range.  Should be able to use model bounds but not working

        min[0] = 0;
        min[1] = 0;
        min[2] = 0;
        max[0] = grid.getWidth();
        max[1] = grid.getHeight();
        max[2] = grid.getDepth();
/*
        if (min[0] > 0)
            min[0]--;
        if (min[1] > 0)
            min[1]--;
        if (min[2] > 0)
            min[2]--;
        if (max[0] < grid.getWidth())
            max[0]++;
        if (max[1] < grid.getHeight())
            max[1]++;
        if (max[2] < grid.getDepth())
            max[2]++;
  */

        int sloca = spatial.findGridCoordsFromWorldCoords(0);
        int slocb = spatial.findGridCoordsFromWorldCoords(0);

        // Cast rays from z direction
        for(int y=min[1]; y < max[1]; y++) {
            for(int x=min[0]; x < max[0]; x++) {

                grid.getWorldCoords(x,y,0,pos);
                pos[0] -= grid.getVoxelSize() / 2.0;
                pos[1] -= grid.getSliceHeight() / 2.0;
                pos[2] = grid.getDepth() + grid.getVoxelSize();

//                System.out.println("ray posz: " + java.util.Arrays.toString(pos));

                if (SPATIAL_ACCEL) {
                    int sloc1 = spatial.findGridCoordsFromWorldCoords((float)pos[0]);
                    int sloc2 = spatial.findGridCoordsFromWorldCoords((float)pos[1]);

//System.out.println("sloc1: " + sloc1 + " sloc2: " + sloc2);
                    TunnelRegion region = new TunnelRegion(TunnelRegion.Axis.Z, sloc1, sloc2, 0);
                    tris = spatial.getObjects(region);
                } else {
                    tris = spatial.getObjects(new AllRegion());
                }

                if (tris == null)  {
                    continue;
                }

                //System.out.println("tris: " + tris.length);

                findInterior(TunnelRegion.Axis.Z, pos[0], pos[1], pos[2], tris, grid);
            }
        }

        // Cast rays from y direction
        for(int x=min[0]; x < max[0]; x++) {
            for(int z=min[2]; z < max[2]; z++) {

                grid.getWorldCoords(x,0,z,pos);
                pos[0] -= grid.getVoxelSize() / 2.0;
                pos[2] -= grid.getVoxelSize() / 2.0;
                pos[1] = grid.getHeight() + grid.getSliceHeight();

//System.out.println("ray posy: " + java.util.Arrays.toString(pos));
                if (SPATIAL_ACCEL) {
                    int sloc1 = spatial.findGridCoordsFromWorldCoords((float)pos[0]);
                    int sloc2 = spatial.findGridCoordsFromWorldCoords((float)pos[2]);

                    TunnelRegion region = new TunnelRegion(TunnelRegion.Axis.Y, sloc1, sloc2, 0);
                    tris = spatial.getObjects(region);
                } else {
                    tris = spatial.getObjects(new AllRegion());
                }

                if (tris == null)  {
                    continue;
                }


                findInterior(TunnelRegion.Axis.Y, pos[0], pos[1], pos[2], tris, grid);
            }
        }

        // Cast rays from x direction
        for(int y=min[1]; y < max[1]; y++) {
            for(int z=min[2]; z < max[2]; z++) {

                grid.getWorldCoords(0,y,z,pos);
                pos[1] -= grid.getSliceHeight() / 2.0;
                pos[2] -= grid.getVoxelSize() / 2.0;
                pos[0] = grid.getWidth() + grid.getVoxelSize();

                //System.out.println("ray posx: " + java.util.Arrays.toString(pos));

                if (SPATIAL_ACCEL) {
                    int sloc1 = spatial.findGridCoordsFromWorldCoords((float)pos[1]);
                    int sloc2 = spatial.findGridCoordsFromWorldCoords((float)pos[2]);

                    TunnelRegion region = new TunnelRegion(TunnelRegion.Axis.X, sloc1, sloc2, 0);
                    tris = spatial.getObjects(region);
                } else {
                    tris = spatial.getObjects(new AllRegion());
                }

                if (tris == null)  {
                    continue;
                }


                findInterior(TunnelRegion.Axis.X, pos[0], pos[1], pos[2], tris, grid);
            }
        }
        
        // Any voxel with material = 3 should be an interior voxel.
        result.find(3, this);

        gridOp = null;
        result = null;
        
        return grid;
    }

    /**
     * Cast a ray along the specified axis and find interior voxels.
     *
     * @param axis The axis to cast along
     * @param rayX The ray x origin
     * @param rayY The ray y origin
     * @param rayZ The ray z origin
     */
    private void findInterior(TunnelRegion.Axis axis, double rayX, double rayY, double rayZ,
                            int[] tris, Grid grid) {

//System.out.println("findInterior: " + axis + " rx: " + rayX + " " + rayY + " " + rayZ);

        boolean[] t_result = new boolean[numVoxels];

        float[] pos = new float[3];
        float[] workingTri;
        TriangleIntersectionDoubleUtil intersectTester = new TriangleIntersectionDoubleUtil();
        ArrayList hits = new ArrayList(tris.length);

        double rx = rayX;
        double ry = rayY;
        double rz = rayZ;

        for(int j=0; j < tris.length; j++) {
            int id = tris[j];
            workingTri = spatial.getTriangle(id);

            switch(axis) {
                case X:
                    if(intersectTester.xAxisRayTriangle(rx, ry, rz, workingTri)) {
                        hits.add(new HitRecord(axis,
                                intersectTester.getLastIntersectionPoint(),rx,ry,rz,workingTri));
                    }
                    break;
                case Y:
                    if(intersectTester.yAxisRayTriangle(rx, ry, rz, workingTri)) {
                        hits.add(new HitRecord(axis,
                                intersectTester.getLastIntersectionPoint(),rx,ry,rz,workingTri));
                    }
                    break;
                case Z:
//System.out.println("cast ray: " + rx + " " + ry + " " + rz + " tri: " + java.util.Arrays.toString(workingTri));
                    if(intersectTester.zAxisRayTriangle(rx, ry, rz, workingTri)) {
                        hits.add(new HitRecord(axis,
                                intersectTester.getLastIntersectionPoint(),rx,ry,rz,workingTri));
                    }
                    break;
            }
        }

        // TODO: Should we use TimSort?
        Collections.sort(hits);

    //if (hits.size() > 0) System.out.println("hits: " + hits.size());
        int hlen = hits.size() - 1;
        HitRecord a;
        HitRecord b;

        int idx = 0;
        double av;
        double bv;
        double dist;
        double EPSILON = 0.00000001;
        int[] minCoords = new int[3];
        int[] maxCoords = new int[3];
        boolean show_details = false;

        while(idx < hlen) {
            a = (HitRecord) hits.get(idx++);
            b = (HitRecord) hits.get(idx++);

            av = a.getPosition();
            bv = b.getPosition();

            dist = bv - av;
            if (show_details) {
                //System.out.println("Hits: " + hits);
                System.out.println("a: " + a);
                System.out.println("Shape { geometry TriangleSet { solid FALSE coord Coordinate { point " + java.util.Arrays.toString(a.tricoords) + "}} }");
                System.out.println("b: " + b);
                System.out.println("Shape { geometry TriangleSet { solid FALSE coord Coordinate { point " + java.util.Arrays.toString(b.tricoords) + "}} }");
                System.out.println("dist: " + dist);
            }

            pos[0] = (float) a.getX();
            pos[1] = (float) a.getY();
            pos[2] = (float) a.getZ();

            spatial.findGridCoordsFromWorldCoords(pos, minCoords);
//System.out.println("min pos: " + java.util.Arrays.toString(pos));
            pos[0] = (float) b.getX();
            pos[1] = (float) b.getY();
            pos[2] = (float) b.getZ();
            //System.out.println("max pos: " + java.util.Arrays.toString(pos));

            spatial.findGridCoordsFromWorldCoords(pos, maxCoords);
            //System.out.println("minCoords: " + java.util.Arrays.toString(minCoords) + " maxCoords: " + java.util.Arrays.toString(maxCoords));

            // Shorten by one to keep inside voxels inside
            switch(axis) {
                case X:
                    minCoords[0] += 1;
                    maxCoords[0] -= 1;
                    break;
                case Y:
                    minCoords[1] += 1;
                    maxCoords[1] -= 1;
                    break;
                case Z:
                    minCoords[2] += 1;
                    maxCoords[2] -= 1;
                    break;
            }
//if (show_details)System.out.println("mark safe: " + java.util.Arrays.toString(minCoords) + " " + java.util.Arrays.toString(maxCoords));
            fillCells(true, t_result, origin, axis, minCoords, maxCoords);

//                System.out.println("vals: " + java.util.Arrays.toString(result));
        }

        hits.clear();


        // Copy interiors.  upper right quadrant is only valid area

        int[] coords = new int[3];
        pos[0] = (float) rayX;
        pos[1] = (float) rayY;
        pos[2] = (float) rayZ;


        spatial.findGridCoordsFromWorldCoords(pos, coords);

/*
        coords[0] = origin[0] - coords[0];
        coords[1] = origin[1] - coords[1];
        coords[2] = origin[2] - coords[2];
 */

//System.out.println("origin: " + java.util.Arrays.toString(origin));
        if (axis == TunnelRegion.Axis.X) {
            int len = t_result.length;
            int start = 0;

/*
            System.out.println("x results: ");
            for(int i=start; i < len; i++) {
                System.out.print(t_result[i] + " ");
            }
  */
            for(int i=start; i < len; i++) {
                if (t_result[i] == true) {
                    //System.out.println("setx: " + i + " " + coords[1] + " " + coords[2]);

                    result.setData(i - origin[0],coords[1] - origin[1],coords[2] - origin[2], Grid.INTERIOR,
                            result.getMaterial(i-origin[0],coords[1] - origin[1],coords[2] - origin[2]) + 1);
                }
            }
        } else if (axis == TunnelRegion.Axis.Y) {
            int len = t_result.length;
            int start = 0;

/*
            System.out.println("y results: ");
            for(int i=start; i < len; i++) {
                System.out.print(t_result[i] + " ");
            }
            System.out.println();
 */
            for(int i=start; i < len; i++) {
                if (t_result[i] == true) {
                    //System.out.println("sety: " + coords[0] + " " + i + " " + coords[2]);

                    result.setData(coords[0] - origin[0],i - origin[1],coords[2] - origin[2], Grid.INTERIOR,
                            result.getMaterial(coords[0] - origin[0],i - origin[1],coords[2] - origin[2]) + 1);
                }
            }
        } else if (axis == TunnelRegion.Axis.Z) {
            //System.out.println("Copying z result");
            int len = t_result.length;
            int start = 0;

/*
            System.out.println("z results: ");
            for(int i=start; i < len; i++) {
                System.out.print(t_result[i] + " ");
            }
            System.out.println();
*/
            for(int i=start; i < len; i++) {
                if (t_result[i] == true) {
//System.out.println("setz: " + (coords[0] - origin[0]) + " " + (coords[1] - origin[1]) + " " + (i - origin[2]));
                    result.setData(coords[0] - origin[0],coords[1] - origin[1],i - origin[2], Grid.INTERIOR,
                            result.getMaterial(coords[0] - origin[0],coords[1] - origin[1],i - origin[2]) + 1);
                }
            }
        }
    }

    /**
     * Fill cells with the specified value along an axis.
     *
     * @param val The value
     * @param result The cube to fill
     * @param axis
     */
    private void fillCells(boolean val, boolean[] result, int[] origin, TunnelRegion.Axis axis, int[] min, int[] max) {
        int start = 0;
        int len = 0;
        
        switch(axis) {
            case X:
                len = max[0] - min[0] + 1;
                start = min[0];
                break;
            case Y:
                len = max[1] - min[1] + 1;
                start = min[1];
                break;
            case Z:
                len = max[2] - min[2] + 1;
                start = min[2];
                break;
        }

        //System.out.println("fill cells: size: " + result.length + " start: " + start + " len: " + len);
        for(int i=0; i < len; i++) {
// TODO: I dont think origin is needed here
//            result[start + i - orig] = val;
            result[start + i] = val;
        }
    }
    
    /**
     * Initialize the spatial grid for accelerating ray / voxel intersections
     * 
     * @param grid
     * @param geom
     * @param voxelSize
     * @return
     */
    private GridTrianglePartition initSpatial(Grid grid, GeometryData geom, double voxelSize) {
        // Sadly this grid requires equal voxels per side and its 0,0,0 centered.
        // We'll allocate 8X the size but only use upper right quadrant

        int w = 2 * grid.getWidth();
        int h = 2 * grid.getHeight();
        int d = 2* grid.getDepth();

        numVoxels = (int) Math.max(Math.max(w,h),d);
        int num_tris = 0;

        origin[0] = numVoxels / 2;
        origin[1] = numVoxels / 2;
        origin[2] = numVoxels / 2;

        System.out.println("real grid size: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());
        if (geom.geometryType == GeometryData.TRIANGLES) {
            num_tris = geom.coordinates.length / 3;
        } else {
            num_tris = geom.indexes.length / 3;
        }

        System.out.println("Init spatial: voxels: " + numVoxels + " tris: " + num_tris);

        GridTrianglePartition ret_val = new GridTrianglePartition(voxelSize, numVoxels, num_tris);

        float[] tri = new float[9];

        int t = 0;

        if (geom.geometryType == GeometryData.TRIANGLES) {
            for(int i=0; i < num_tris / 3; i++) {
                tri[0] = geom.coordinates[t * 3  ];
                tri[1] = geom.coordinates[t * 3+1];
                tri[2] = geom.coordinates[t * 3+2];
                t++;
                tri[3] = geom.coordinates[t * 3  ];
                tri[4] = geom.coordinates[t * 3+1];
                tri[5] = geom.coordinates[t * 3+2];
                t++;
                tri[6] = geom.coordinates[t * 3  ];
                tri[7] = geom.coordinates[t * 3+1];
                tri[8] = geom.coordinates[t * 3+2];
                t++;

                Triangle poly = new Triangle(tri, i);
                ret_val.insert(poly, false);
            }

        } else {
            for(int i=0; i < num_tris; i++) {
                tri[0] = geom.coordinates[geom.indexes[t  ] * 3  ];
                tri[1] = geom.coordinates[geom.indexes[t  ] * 3+1];
                tri[2] = geom.coordinates[geom.indexes[t++] * 3+2];
                tri[3] = geom.coordinates[geom.indexes[t  ] * 3  ];
                tri[4] = geom.coordinates[geom.indexes[t  ] * 3+1];
                tri[5] = geom.coordinates[geom.indexes[t++] * 3+2];
                tri[6] = geom.coordinates[geom.indexes[t  ] * 3  ];
                tri[7] = geom.coordinates[geom.indexes[t  ] * 3+1];
                tri[8] = geom.coordinates[geom.indexes[t++] * 3+2];

                Triangle poly = new Triangle(tri, i);

                ret_val.insert(poly, false);
            }
        }

        System.gc();    // gc as this can generate a huge amount

        return ret_val;
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
        // TODO: can remove
        int_count++;
        
        gridOp.setData(x,y,z,Grid.INTERIOR, innerMaterial);

    }

    /**
     * A voxel of the class requested has been found.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param vd The voxel data
     */
    public boolean foundInterruptible(int x, int y, int z, VoxelData vd) {
        // ignore
        return true;
    }
}

class HitRecord implements Comparable {
    public int axis;
    public double[] hit;
    public double rx,ry,rz;     // TODO: not needed for production
    public float[] tricoords;

    public HitRecord(TunnelRegion.Axis axis, Point3d hit) {
        switch(axis) {
            case X:
                this.axis = 0;
                break;
            case Y:
                this.axis = 1;
                break;
            case Z:
                this.axis = 2;
                break;
        }

        this.hit = new double[3];
        this.hit[0] = hit.x;
        this.hit[1] = hit.y;
        this.hit[2] = hit.z;
    }

    public HitRecord(TunnelRegion.Axis axis, Point3d hit, double rx, double ry, double rz, float[] tri) {
        switch(axis) {
            case X:
                this.axis = 0;
                break;
            case Y:
                this.axis = 1;
                break;
            case Z:
                this.axis = 2;
                break;
        }

        this.hit = new double[3];
        this.hit[0] = hit.x;
        this.hit[1] = hit.y;
        this.hit[2] = hit.z;

        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
        this.tricoords = tri;
    }

    public double getPosition() {
        return hit[axis];
    }

    public double getX() {
        return hit[0];
    }

    public double getY() {
        return hit[1];
    }

    public double getZ() {
        return hit[2];
    }

    public String toString() {
//        return hit[axis] + " -> " + hit[0] + " " + hit[1] + " " + hit[2] + " tri: " + tri.id;
        return hit[axis] + " -> tri: " + tricoords;
    }

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    public boolean equals(Object o)
    {
        if(!(o instanceof HitRecord))
            return false;
        else
            return equals((HitRecord)o);
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param ha The geometry instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(HitRecord ha)
    {
        return (ha.hit[axis] == this.hit[axis]);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The objec to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    public int compareTo(Object o)
            throws ClassCastException
    {
        HitRecord rec = (HitRecord)o;
        return compareTo(rec);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param ta The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(HitRecord ta)
    {

        if(ta == null)
            return 1;

        if(ta == this) {
            return 0;
        }

        return (this.hit[axis] < ta.hit[axis] ? -1 : 1);
    }

}