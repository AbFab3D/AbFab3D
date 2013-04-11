package abfab3d.geom;

import abfab3d.grid.*;
import abfab3d.io.input.ZBuffer;
import abfab3d.util.BoundingBoxUtilsFloat;
import abfab3d.util.MatrixUtil;
import org.j3d.geom.GeometryData;
import xj3d.filter.FilterExitCodes;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import static abfab3d.util.Output.printf;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public class TriangleModelCreator2 {
    static final boolean m_debug = true;  // print debug info
    static final boolean m_stats = false; // print crossings statistics

    // small shift to break possible xy symmetry
    static final double EPSILON_SHIFT = 1.2345e-10;

    public static final double MM = 1000.;
    double m_sx, m_sy, m_sz, m_tx, m_ty, m_tz; // scale and shift to fit geometry to grid
    double m_rx, m_ry, m_rz, m_rangle;
    double m_nx,m_ny,m_nz;
    boolean m_rotate;
    double m_voxelSize, m_voxelHeight;

    ZBuffer m_zbuffer;
    /** Has this buffer been used */
    private boolean m_zbuffer_used;

    /**
     * rasterizer for given grid size and
     * given transfromation from model coordinates to grid coordinates
     */
    public TriangleModelCreator2(int nx, int ny, int nz,
                                 double voxelSize,
                                 double voxelHeight,
                                 double tx, double ty, double tz
    ) {

        m_voxelSize = voxelSize;
        m_voxelHeight = voxelHeight;


        m_nx = nx;
        m_ny = ny;
        m_nz = nz;

        m_tx = tx + EPSILON_SHIFT;
        m_ty = ty;
        m_tz = tz;

        m_zbuffer = new ZBuffer(nx, ny, nz);
        m_rotate = false;
        m_zbuffer_used = false;
    }

    /**
     * rasterizer for given grid size and
     * given transfromation from model coordinates to grid coordinates
     */
    public TriangleModelCreator2(int nx, int ny, int nz,
                                 double voxelSize,
                                 double voxelHeight,
                                 double tx, double ty, double tz,
                                 double rx, double ry, double rz, double rangle
    ) {

        m_voxelSize = voxelSize;
        m_voxelHeight = voxelHeight;


        m_nx = nx;
        m_ny = ny;
        m_nz = nz;

        m_tx = tx + EPSILON_SHIFT;
        m_ty = ty;
        m_tz = tz;

        m_rx = rx;
        m_ry = ry;
        m_rz = rz;
        m_rangle = rangle;
        m_rotate = true;

        m_zbuffer = new ZBuffer(nx, ny, nz);
        m_zbuffer_used = false;
    }

    private void calcTransform(Grid grid, GeometryData geom) {
        float[] minmax = new float[6];

        BoundingBoxUtilsFloat bc = new BoundingBoxUtilsFloat();

        switch (geom.geometryType) {
            case GeometryData.TRIANGLES:
                //double[] minmaxd = getBounds(null,geom.coordinates);
                bc.computeMinMax(geom.coordinates, geom.coordinates.length / 3, minmax);

                break;
            case GeometryData.INDEXED_TRIANGLES:
                //minmax = getBounds(null,geom.coordinates,geom.indexes);
                bc.computeMinMax(geom.coordinates, geom.indexes, minmax);
                break;
            default:
                throw new IllegalArgumentException("Unsupported geometryType: " + geom.geometryType);
        }

        double
                min_x = minmax[0],
                max_x = minmax[1],
                min_y = minmax[2],
                max_y = minmax[3],
                min_z = minmax[4],
                max_z = minmax[5];

        int padding = 1;
        // scale to transform from model space into grid space
        m_sx = ((max_x - min_x)/grid.getVoxelSize() - 2*padding) / (max_x - min_x);
        m_sy = ((max_y - min_y)/grid.getSliceHeight() -2*padding) / (max_y - min_y);
        m_sz = ((max_z - min_z)/grid.getVoxelSize() -2*padding) / (max_z - min_z);
    }

    public void rasterize(GeometryData geom, Grid grid) {

        if (m_zbuffer_used) {
            m_zbuffer.clear();
        }
        calcTransform(grid, geom);

        switch (geom.geometryType) {
            case GeometryData.TRIANGLES:
                if (m_rotate) {
                    rasterizeTrianglesRotated(geom);
                } else {
                    rasterizeTriangles(geom);
                }
                break;
            case GeometryData.INDEXED_TRIANGLES:
                if (m_rotate) {
                    rasterizeIndexedTrianglesRotated(geom);
                } else {
                    rasterizeIndexedTriangles(geom);
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported geometryType: " + geom.geometryType);
        }

        m_zbuffer.sort();

        if (grid instanceof GridIntervals) {
            fillGridIntervals((GridIntervals)grid);
        } else {
            fillGridDefault(grid);
        }

        m_zbuffer_used = true;
    }

    public void rasterizeTriangles(GeometryData geom) {

        int len = geom.vertexCount / 3;
        //Point3d v = new Point3d();
        double x1, y1, z1, x2, y2, z2, x3, y3, z3;
        int idx = 0;
        for (int i = 0; i < len; i++) {

            x1 = m_sx * geom.coordinates[idx++] + m_tx;
            y1 = m_sy * geom.coordinates[idx++] + m_ty;
            z1 = m_sz * geom.coordinates[idx++] + m_tz;

            x2 = m_sx * geom.coordinates[idx++] + m_tx;
            y2 = m_sy * geom.coordinates[idx++] + m_ty;
            z2 = m_sz * geom.coordinates[idx++] + m_tz;

            x3 = m_sx * geom.coordinates[idx++] + m_tx;
            y3 = m_sy * geom.coordinates[idx++] + m_ty;
            z3 = m_sz * geom.coordinates[idx++] + m_tz;

            m_zbuffer.fillTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3);

        }
    }
    public void rasterizeTrianglesRotated(GeometryData geom) {

        Matrix4d mat = MatrixUtil.createMatrix(
                new double[]{0, 0, 0},
                new double[]{m_sx,m_sy,m_sz}, new double[]{m_rx, m_ry, m_rz, m_rangle}, new double[]{m_tx, m_ty, m_tz},
                new double[]{0, 0, 1, 0});

        int len = geom.vertexCount / 3;
        Point3d v = new Point3d();
        double x1, y1, z1, x2, y2, z2, x3, y3, z3;
        int idx = 0;
        for (int i = 0; i < len; i++) {

            v.x = geom.coordinates[idx++];
            v.y = geom.coordinates[idx++];
            v.z = geom.coordinates[idx++];

            mat.transform(v);
            x1 = v.x;
            y1 = v.y;
            z1 = v.z;

            v.x = geom.coordinates[idx++];
            v.y = geom.coordinates[idx++];
            v.z = geom.coordinates[idx++];

            mat.transform(v);
            x2 = v.x;
            y2 = v.y;
            z2 = v.z;

            v.x = geom.coordinates[idx++];
            v.y = geom.coordinates[idx++];
            v.z = geom.coordinates[idx++];

            mat.transform(v);
            x3 = v.x;
            y3 = v.y;
            z3 = v.z;

            m_zbuffer.fillTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3);

        }
    }

    public void rasterizeIndexedTriangles(GeometryData geom) {
        Matrix4d mat = MatrixUtil.createMatrix(
                new double[]{0, 0, 0},
                new double[]{m_sx,m_sy,m_sz}, new double[]{m_rx, m_ry, m_rz, m_rangle}, new double[]{m_tx, m_ty, m_tz},
                new double[]{0, 0, 1, 0});


        int len = geom.indexesCount / 3;

        double coords[] = new double[9];
        double x1, y1, z1, x2, y2, z2, x3, y3, z3;

        //if(m_debug) printf("rasterizeIndexedTriangles(tricount:%d)\n", len);

        int idx = 0;
        for (int i = 0; i < len; i++) {

            int off = geom.indexes[idx++] * 3;

            x1 = m_sx * geom.coordinates[off++] + m_tx;
            y1 = m_sy * geom.coordinates[off++] + m_ty;
            z1 = m_sz * geom.coordinates[off++] + m_tz;

            off = geom.indexes[idx++] * 3;
            x2 = m_sx * geom.coordinates[off++] + m_tx;
            y2 = m_sy * geom.coordinates[off++] + m_ty;
            z2 = m_sz * geom.coordinates[off++] + m_tz;

            off = geom.indexes[idx++] * 3;
            x3 = m_sx * geom.coordinates[off++] + m_tx;
            y3 = m_sy * geom.coordinates[off++] + m_ty;
            z3 = m_sz * geom.coordinates[off++] + m_tz;

            m_zbuffer.fillTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3);
        }
    }

    public void rasterizeIndexedTrianglesRotated(GeometryData geom) {
        Matrix4d mat = MatrixUtil.createMatrix(
                new double[]{0, 0, 0},
                new double[]{m_sx,m_sy,m_sz}, new double[]{m_rx, m_ry, m_rz, m_rangle}, new double[]{m_tx, m_ty, m_tz},
                new double[]{0, 0, 1, 0});


        int len = geom.indexesCount / 3;

        double coords[] = new double[9];
        double x1, y1, z1, x2, y2, z2, x3, y3, z3;
        Point3d v = new Point3d();

        //if(m_debug) printf("rasterizeIndexedTriangles(tricount:%d)\n", len);

        int idx = 0;
        for (int i = 0; i < len; i++) {

            int off = geom.indexes[idx++] * 3;

            v.x = geom.coordinates[off++];
            v.y = geom.coordinates[off++];
            v.z = geom.coordinates[off++];

            mat.transform(v);
            x1 = v.x;
            y1 = v.y;
            z1 = v.z;

            off = geom.indexes[idx++] * 3;

            v.x = geom.coordinates[off++];
            v.y = geom.coordinates[off++];
            v.z = geom.coordinates[off++];

            mat.transform(v);
            x2 = v.x;
            y2 = v.y;
            z2 = v.z;

            off = geom.indexes[idx++] * 3;

            v.x = geom.coordinates[off++];
            v.y = geom.coordinates[off++];
            v.z = geom.coordinates[off++];

            mat.transform(v);
            x3 = v.x;
            y3 = v.y;
            z3 = v.z;

            m_zbuffer.fillTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3);
        }
    }

    void fillGridDefault(Grid grid){

        int m_ny = grid.getHeight();
        int m_nx = grid.getWidth();

        for(int y = 0; y < m_ny; y++){

            for(int x = 0; x < m_nx; x++){

                int len = m_zbuffer.getCount(x,y);
                if(len < 2)
                    continue;

                float zray[] = m_zbuffer.getRay(x,y);

                len = (len & 0xFFFE); // make it even

                for(int c = len-2; c >= 0; c-=2 ){
                    //for(int c = len-1; c < len; ){
                    int z1 = (int)Math.ceil(zray[c]);
                    int z2 = (int)Math.floor(zray[c+1]);
                    //fillSegment_direct(grid, x,y,z1,z2);
                    fillSegment_reverse(grid, x,y,z1,z2);
                }
                // release ray memory
                m_zbuffer.setRay(x,y, null);
            }

        }
    }

    void fillSegment_reverse(Grid grid, int x, int y, int z1, int z2){

        for(int z = z2; z >= z1; z--){
            grid.setState(x, y, z, Grid.INTERIOR);
        }
    }

    void fillGridIntervals(GridIntervals grid){

        int arrayLength = 2;
        ArrayInt intervals = new ArrayInt(arrayLength);
        ArrayInt values = new ArrayInt(arrayLength);
        int intervals_arr[] = new int[arrayLength];
        int values_arr[] = new int[arrayLength];
        int maxIntervalsSize = 0;

        for(int y = 0; y < m_ny; y++){

            for(int x = 0; x < m_nx; x++){

                int len = m_zbuffer.getCount(x,y);
                if(len < 2)
                    continue;

                intervals.clear();
                values.clear();

                float zray[] = m_zbuffer.getRay(x,y);

                for(int i = 0; i < len; i++){

                    int state = (i & 1);

                    if(state == 0){
                        // start inside
                        int z = (int)Math.ceil(zray[i]);
                        if(intervals.hasLast() && intervals.getLast() == z){
                            // new interval has zero length - remove it
                            intervals.removeLast();
                            values.removeLast();
                        } else {
                            intervals.add(z);
                            values.add(Grid.INTERIOR);
                        }
                    }  else { // state == 1
                        // start outside
                        int z = (int)Math.floor(zray[i])+1;
                        if(intervals.hasLast() && intervals.getLast() == z){
                            // new interval has zero length - remove it
                            intervals.removeLast();
                            values.removeLast();
                        } else {
                            intervals.add(z);
                            values.add(Grid.OUTSIDE);
                        }
                    }
                }
                int intsize = intervals.size();
                if(intsize > 0){

                    if(intsize > maxIntervalsSize)
                        maxIntervalsSize = intsize;

                    if(intsize > intervals_arr.length){
                        printf("realloc intsize: %d\n ",intsize );
                        intervals_arr = new int[intsize*2];
                        values_arr = new int[intsize*2];
                    }
                    //int count = intervals.size();
                    grid.setIntervals(x,y, intervals.toArray(intervals_arr), values.toArray(values_arr),intsize);
                }

                // TODO: is it removeal?
                // release ray memory
                //m_zbuffer.setRay(x,y, null);
            }
        }

        //printf("maxIntervalSize: %d\n",maxIntervalsSize);

    }
}
