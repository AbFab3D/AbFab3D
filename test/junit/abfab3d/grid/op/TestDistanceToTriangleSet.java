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

import java.io.File;
import java.io.IOException;
import java.util.Random;

import abfab3d.grid.*;
import abfab3d.io.input.X3DFileLoader;
import abfab3d.io.input.X3DLoader;
import abfab3d.io.input.X3DReader;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.MeshDistance;
import abfab3d.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import abfab3d.distance.DistanceData;
import abfab3d.distance.DistanceDataSphere;

import abfab3d.io.output.STLWriter;
import abfab3d.io.input.STLReader;


import abfab3d.geom.TriangulatedModels;
import abfab3d.geom.PointCloud;
import abfab3d.geom.Octahedron;
import org.apache.commons.io.FilenameUtils;

import static java.lang.Math.round;
import static java.lang.Math.ceil;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;
import static abfab3d.util.MathUtil.L2S;

/**
 * Test the DistanceToTriangleSet class.
 *
 * @author Vladimir Bulatov
 */
public class TestDistanceToTriangleSet extends TestCase {

    private static final boolean DEBUG = true;
    static final double INF = ClosestPointIndexer.INF;

    int subvoxelResolution = 100;
    

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDistanceToTriangleSet.class);
    }

    void testNothing()throws Exception{
        // to make tester happy 
    }

    /**
     * Test that the mesh is close to the input mesh
     */
    public void testMeshDistance() {
        String path = "test/models";

        String[] file = new String[] {
//                "gyroid.stl",
                "holes.stl",
//                "cube_10mm.x3dv"
        };
        double[] vs = new double[] {
//                0.1 * MM,
                0.3 * MM,
//                0.1 * MM
         };

        for(int i=0; i < file.length; i++) {
            double d = calcMeshDistance(path,file[i], vs[i],0);

            assertTrue(file + " contains too much error", (d < 2.0 * vs[i]));
        }
    }

    double calcMeshDistance(String path, String filePath, double minVoxelSize, int post) {

        if(DEBUG) printf("makeTestSTL()\n");
        int maxGridDimension = 2000;
//        double minVoxelSize = 0.06*MM;

        printf("loading file: %s\n", filePath);

        TriangleProducer loader = null;

        if (filePath.endsWith("stl")) {
            loader = new STLReader(path + File.separator + filePath);
        } else {
            loader = new X3DReader(path + File.separator + filePath);
        }

        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        loader.getTriangles(bb);
        Bounds bounds = new Bounds(bb.getBounds());
        printf("bounds: %s\n", bounds);
        double maxSize = max(max(bounds.getSizeX(),bounds.getSizeY()),bounds.getSizeZ());
        printf("max size: %7.2f mm\n", maxSize/MM);
        double maxOutDistance =  maxSize*0.1;
        double maxInDistance =  maxOutDistance;
        int subvoxelResolution = 10;

        double vs = (maxSize+2*maxOutDistance)/maxGridDimension;
        if (vs < minVoxelSize) vs = minVoxelSize;

        printf("voxel size: %7.2f mm\n", vs/MM);

        bounds.expand(maxOutDistance);

        AttributeGrid distGrid = new ArrayAttributeGridShort(bounds, vs, vs);
        printf("distanceGrid:[%d x %d x %d]\n",distGrid.getWidth(),distGrid.getHeight(), distGrid.getDepth());
        long t0 = time();

        DistanceToTriangleSet dts = new DistanceToTriangleSet(maxInDistance, maxOutDistance,subvoxelResolution);
        dts.setTriangleProducer(loader);
        dts.setIterationsCount(post);
        distGrid = dts.execute(distGrid);
        printf("distance ready %d ms\n", (time() - t0));

        DensityGridExtractor dge = new DensityGridExtractor(-maxInDistance,0, distGrid,maxInDistance,maxOutDistance,subvoxelResolution);
        AttributeGrid surface = (AttributeGrid) distGrid.createEmpty(distGrid.getWidth(), distGrid.getHeight(),
                distGrid.getDepth(), distGrid.getSliceHeight(), distGrid.getVoxelSize());
        surface.setGridBounds(bounds);

        surface = dge.execute(surface);

        // free memory
        dts = null;
        dge = null;
        distGrid = null;

        MeshMakerMT mmaker = new MeshMakerMT();
        mmaker.setMaxAttributeValue(subvoxelResolution);
        mmaker.setSmoothingWidth(0.25);
        mmaker.setBlockSize(50);
        mmaker.setMaxDecimationError(3.e-10);
        //mmaker.setMaxDecimationCount(0);


        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
        MeshDistance md = new MeshDistance();
        md.setTriangleSplit(true);
        md.setHashDistanceValues(true);
        md.setUseTriBuckets(true);
        md.setTriBucketSize(0.3 * MM);
        mmaker.makeMesh(surface, its);

        printf("Calculating difference...\n");
        md.measure(loader, its);

        t0 = time();
        printf("  HDF distance: %6.4f mm\n", md.getHausdorffDistance() / MM);
        printf("  L_1 distance: %6.4f mm\n", md.getL1Distance() / MM);
        printf("  L_2 distance: %6.4f mm\n", md.getL2Distance()/MM);
        printf("  min distance: %6.4f mm\n", md.getMinDistance() / MM);
        printf("  measure time: %d ms\n", (time() - t0));

        if (DEBUG) {
            try {
                writeGrid(surface, "/tmp/" + FilenameUtils.getBaseName(filePath) + "_rt.stl", subvoxelResolution);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        return md.getHausdorffDistance();
    }

    /**
       testing distance to sphere 
     */
    void makeTestSTL()throws Exception{
        
        if(DEBUG) printf("makeTestSTL()\n");
        String filePath = "/tmp/crab_vs0.2.stl";
//        int maxGridDimension = 500;
        int maxGridDimension = 1000;

        printf("loading file: %s\n", filePath);
        STLReader stl = new STLReader(filePath);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        stl.getTriangles(bb);
        Bounds bounds = new Bounds(bb.getBounds());
        printf("bounds: %s\n", bounds);
        double maxSize = max(max(bounds.getSizeX(),bounds.getSizeY()),bounds.getSizeZ());
        printf("max size: %7.2f mm\n", maxSize/MM);  
        double maxOutDistance =  maxSize*0.1;
        double maxInDistance =  maxOutDistance;
        int subvoxelResolution = 10;
                
        double vs = (maxSize+2*maxOutDistance)/maxGridDimension;                
        printf("voxel size: %7.2f mm\n", vs/MM);  
        
        bounds.expand(maxOutDistance);

        AttributeGrid distGrid = new ArrayAttributeGridShort(bounds, vs, vs);
        printf("distanceGrid:[%d x %d x %d]\n",distGrid.getWidth(),distGrid.getHeight(), distGrid.getDepth());
        long t0 = time();

        DistanceToTriangleSet dts = new DistanceToTriangleSet(maxInDistance, maxOutDistance,subvoxelResolution);        
        dts.setTriangleProducer(stl);
        distGrid = dts.execute(distGrid);        
        printf("distance ready %d ms\n", (time() - t0));

        DensityGridExtractor dge = new DensityGridExtractor(-maxInDistance,vs, distGrid,maxInDistance,maxOutDistance,subvoxelResolution);
        AttributeGrid surface = (AttributeGrid) distGrid.createEmpty(distGrid.getWidth(), distGrid.getHeight(),
                distGrid.getDepth(), distGrid.getSliceHeight(), distGrid.getVoxelSize());
        surface.setGridBounds(bounds);

        surface = dge.execute(surface);

        try {
            writeGrid(surface, "/tmp/crab.stl", subvoxelResolution);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     testing distance to sphere
     */
    void makeGyroidSTL()throws Exception{

        if(DEBUG) printf("makeTestSTL()\n");
        String filePath = "test/models/gyroid.stl";
//        int maxGridDimension = 500;
        int maxGridDimension = 1000;
        double minVoxelSize = 0.1*MM;

        printf("loading file: %s\n", filePath);
        STLReader stl = new STLReader(filePath);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        stl.getTriangles(bb);
        Bounds bounds = new Bounds(bb.getBounds());
        printf("bounds: %s\n", bounds);
        double maxSize = max(max(bounds.getSizeX(),bounds.getSizeY()),bounds.getSizeZ());
        printf("max size: %7.2f mm\n", maxSize/MM);
        double maxOutDistance =  maxSize*0.1;
        double maxInDistance =  maxOutDistance;
        int subvoxelResolution = 10;

        double vs = (maxSize+2*maxOutDistance)/maxGridDimension;
        if (vs < minVoxelSize) vs = minVoxelSize;

        printf("voxel size: %7.2f mm\n", vs/MM);

        bounds.expand(maxOutDistance);

        AttributeGrid distGrid = new ArrayAttributeGridShort(bounds, vs, vs);
        printf("distanceGrid:[%d x %d x %d]\n",distGrid.getWidth(),distGrid.getHeight(), distGrid.getDepth());
        long t0 = time();

        DistanceToTriangleSet dts = new DistanceToTriangleSet(maxInDistance, maxOutDistance,subvoxelResolution);
        dts.setTriangleProducer(stl);
        distGrid = dts.execute(distGrid);
        printf("distance ready %d ms\n", (time() - t0));

        DensityGridExtractor dge = new DensityGridExtractor(-maxInDistance,vs, distGrid,maxInDistance,maxOutDistance,subvoxelResolution);
        AttributeGrid surface = (AttributeGrid) distGrid.createEmpty(distGrid.getWidth(), distGrid.getHeight(),
                distGrid.getDepth(), distGrid.getSliceHeight(), distGrid.getVoxelSize());
        surface.setGridBounds(bounds);

        surface = dge.execute(surface);

        try {
            writeGrid(surface, "/tmp/gyroid_rt.stl", subvoxelResolution);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Test a thin 0.15mm model
     */
    void makeThin()throws Exception{

        if(DEBUG) printf("makeTestSTL()\n");
        String filePath = "test/models/2855479.x3db";
//        int maxGridDimension = 500;
        int maxGridDimension = 1000;
        double minVoxelSize = 0.1*MM;

        printf("loading file: %s\n", filePath);
        X3DReader loader = new X3DReader(filePath);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        loader.getTriangles(bb);
        Bounds bounds = new Bounds(bb.getBounds());
        printf("bounds: %s\n", bounds);
        double maxSize = max(max(bounds.getSizeX(),bounds.getSizeY()),bounds.getSizeZ());
        printf("max size: %7.2f mm\n", maxSize/MM);
        double maxOutDistance =  maxSize*0.1;
        double maxInDistance =  maxOutDistance;
        int subvoxelResolution = 10;
        int postIterations = 0;

        double vs = (maxSize+2*maxOutDistance)/maxGridDimension;
        if (vs < minVoxelSize) vs = minVoxelSize;

        printf("voxel size: %7.2f mm\n", vs/MM);

        bounds.expand(maxOutDistance);

        AttributeGrid distGrid = new ArrayAttributeGridShort(bounds, vs, vs);
        printf("distanceGrid:[%d x %d x %d]\n",distGrid.getWidth(),distGrid.getHeight(), distGrid.getDepth());
        long t0 = time();

        DistanceToTriangleSet dts = new DistanceToTriangleSet(maxInDistance, maxOutDistance,subvoxelResolution);
        dts.setTriangleProducer(loader);
        dts.setIterationsCount(postIterations);
        distGrid = dts.execute(distGrid);
        printf("distance ready %d ms\n", (time() - t0));

        DensityGridExtractor dge = new DensityGridExtractor(-maxInDistance,vs, distGrid,maxInDistance,maxOutDistance,subvoxelResolution);
        AttributeGrid surface = (AttributeGrid) distGrid.createEmpty(distGrid.getWidth(), distGrid.getHeight(),
                distGrid.getDepth(), distGrid.getSliceHeight(), distGrid.getVoxelSize());
        surface.setGridBounds(bounds);

        surface = dge.execute(surface);

        try {
            writeGrid(surface, "/tmp/2855479_rt.stl", subvoxelResolution);
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    void writeGrid(Grid grid, String path, int gridMaxAttributeValue) throws IOException {

        MeshMakerMT mmaker = new MeshMakerMT();
        mmaker.setMaxAttributeValue(gridMaxAttributeValue);
        mmaker.setSmoothingWidth(0.5);
        mmaker.setBlockSize(50);
        mmaker.setMaxDecimationError(3.e-10);

        STLWriter stl = new STLWriter(path);
        mmaker.makeMesh(grid, stl);
        stl.close();

    }

    void makeTestSphere()throws Exception{
        
        if(DEBUG) printf("makeTestSphere()\n");
        double w = 5*MM;        
        double xmin = -w, xmax = w, ymin = -w, ymax = w, zmin = -w, zmax = w;
        double vs = 0.5*MM;
        double maxInDistance = 7*vs;
        double maxOutDistance = 7*vs;
        int subvoxelResolution = 10;
        AttributeGrid distGrid = new ArrayAttributeGridShort(new Bounds(xmin,xmax,ymin,ymax,zmin,zmax), vs, vs);
        printf("grid:[%d x %d x %d]\n",distGrid.getWidth(),distGrid.getHeight(), distGrid.getDepth());
        
        DistanceToTriangleSet dts = new DistanceToTriangleSet(maxInDistance, maxOutDistance,subvoxelResolution);
        
        TriangulatedModels.Sphere sphere = new TriangulatedModels.Sphere(3*MM, new Vector3d(0.5*vs,0.5*vs, 0.5*vs), 4); 
        dts.setTriangleProducer(sphere);                
        
        distGrid = dts.execute(distGrid);        
        
        //printInterior(distGrid);
        //printIndices(distGrid);
        printDistances(distGrid);

    }


    static void printInterior(AttributeGrid grid){
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();

        printf("Interior \n");
        
        for(int z = 0; z < nz; z++){
            printf("z: %d\n", z);
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    int a = (int)grid.getAttribute(x,y,z);
                    //printf("%2d", a);
                    if(a == 0) printf(". ");
                    else  printf("X ");
                }
                printf("\n");
            }
        }
    }

    static void printIndices(AttributeGrid grid){
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        
        printf("Indices\n");
        for(int z = 0; z < nz; z++){
            printf("z: %d\n", z);
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    int a = (int)grid.getAttribute(x,y,z);
                    if(a == 0) printf("   . ");
                    else  printf("%4d ", a);
                }
                printf("\n");
            }
        }
    }

    static void printDistances(AttributeGrid grid){
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        
        printf("Distances: \n");
        for(int z = 0; z < nz; z++){
            printf("z: %d\n", z);
            for(int y = 0; y < ny; y++){
                for(int x = 0; x < nx; x++){
                    int a = L2S(grid.getAttribute(x,y,z));
                    printf("%4d ", a);
                }
                printf("\n");
            }
        }
    }


    public static void main(String arg[]) throws Exception {

        for(int i = 0; i < 1; i++){
            new TestDistanceToTriangleSet().makeTestSphere();
            //new TestDistanceToTriangleSet().testMeshDistance();
        }        
    }
}
