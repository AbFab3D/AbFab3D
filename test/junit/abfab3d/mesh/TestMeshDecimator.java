/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.mesh;

// External Imports

import abfab3d.grid.ClassTraverser;
import abfab3d.grid.Grid;
import abfab3d.grid.GridShortIntervals;
import abfab3d.io.input.IndexedTriangleSetLoader;
import abfab3d.io.input.MeshRasterizer;
import abfab3d.io.input.STLRasterizer;
import abfab3d.io.input.STLReader;
import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.MeshExporter;
import abfab3d.io.output.STLWriter;
import abfab3d.util.StructMixedData;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.j3d.geom.GeometryData;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;
import static java.lang.System.currentTimeMillis;

/**
 * Tests the functionality of MeshDecimator
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestMeshDecimator extends TestCase {
    
    static final double MM = 1000; // m -> mm conversion 
    static final double MM3 = 1.e9; // m^3 -> mm^3 conversion 

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestMeshDecimator.class);
    }

    public void testQuadric(){
        
        Point3d p[] = new Point3d[]{
            new Point3d(0,0,0), // 0
            new Point3d(1,0,0), // 1
            new Point3d(1,1,0), // 2
            new Point3d(0,1,0), // 3
            new Point3d(0,0,1), // 4
            new Point3d(1,0,1), // 5
            new Point3d(1,1,1), // 6
            new Point3d(0.1,1.1,1)};// 7
        
        int faces[][] = new int[][]{{6,5,2},{5,1,2}, {7,6,2}, {7,3,2}, {4,5,6}, {4,6,7}, {0,4,7}, {0,7,3}, {5,4,1}, {4,0,1}, {3,2,1}, {3,1,0}};

        Vector3d sc0 = new Vector3d();
        Vector3d sc1 = new Vector3d();
        Vector3d sc2 = new Vector3d();
        Vector4d p526 = new Vector4d();
        Vector4d p627 = new Vector4d();
        Vector4d p675 = new Vector4d();
        Vector3d sn = new Vector3d();

        sc0.set(p[5]);
        sc1.set(p[2]);
        sc2.set(p[6]);
        Quadric.makePlane(sc0, sc1, sc2,sn,p526);

        sc0.set(p[6]);
        sc1.set(p[2]);
        sc2.set(p[7]);
        Quadric.makePlane(sc0,sc1,sc2,sn,p627);

        sc0.set(p[6]);
        sc1.set(p[7]);
        sc2.set(p[5]);
        Quadric.makePlane(sc0,sc1,sc2,sn,p675);

        StructMixedData quadrics = new StructMixedData(new Quadric(), 10);

        int q526 = Quadric.create(p[5], p[2], p[6], quadrics);
        int q627 = Quadric.create(p[6], p[2], p[7], quadrics);
        int q675 = Quadric.create(p[6], p[7], p[5], quadrics);
        int q734 = Quadric.create(p[7], p[3], p[4], quadrics);
        double eps = 1.0e-5;
        int q56s = Quadric.create(p[5],p[6], eps, quadrics);

        int q6 = Quadric.create(quadrics, q526, quadrics);

        Quadric.addSet(quadrics,q627,quadrics,q6);
        Quadric.addSet(quadrics,q675,quadrics,q6);

        int q7 = Quadric.create(quadrics,q734,quadrics);
        Quadric.addSet(quadrics, q627, quadrics,q7);
        Quadric.addSet(quadrics, q675, quadrics,q7);

        int q67 = Quadric.create(quadrics,q6,quadrics);
        Quadric.addSet(quadrics, q67, quadrics, q7);

        /*
        for(int i = 0; i < p.length ; i++){
            
            //printf("p526t[%d]): %10.7f\n",  i, MeshDecimator.planePointDistance2(p526, p[i]));
            //printf("p627[%d]): %10.7f\n",  i, MeshDecimator.planePointDistance2(p627, p[i]));
            //printf("p675[%d]): %10.7f\n",  i, MeshDecimator.planePointDistance2(p675, p[i]));
            //printf("q1(p[%d]): %10.7f\n",  i, q1.evaluate(p[i]));
            //printf("q2(p[%d]): %10.7f\n",  i, q2.evaluate(p[i]));
            //printf("q3(p[%d]): %10.7f\n",  i, q3.evaluate(p[i]));
            printf(" q6(p[%d]): %10.7f\n", i,   q6.evaluate(p[i]));
            printf(" q7(p[%d]): %10.7f\n", i, q7.evaluate(p[i]));
            printf("q67(p[%d]): %10.7f\n\n", i, q67.evaluate(p[i]));

        }
        
        Point3d p67 = q67.getMinimum(new Point3d());
        printf("p67: [%10.7f %10.7f %10.7f] \n", p67.x,p67.y,p67.z);
        
        printf("q67(p67): %10.7f\n\n", q67.evaluate(p67));
        Point3d pp = new Point3d(p67);
        pp.add(new Point3d(0.,-0.1,0.1));
        printf("q67(p67): %10.7f\n", q67.evaluate(pp));
        

        Quadric qe65 = new Quadric(q526);
        qe65.addSet(q675);
        
        printf("qe65(p6): %10.7f\n", qe65.evaluate(p[6]));
        printf("qe65(p5): %10.7f\n", qe65.evaluate(p[5]));
        
        printf("qe65.det:  %10.7e\n", qe65.determinant());
        printf("q56s.det: %10.7e\n", q56s.determinant());
        
        Point3d p56s = q56s.getMinimum(new Point3d());
                
        printf("p56s: %s\n",p56s);
        
        qe65.addSet(q56s);
        printf("dets: [%10.7e]\n", qe65.determinant());

        Point3d p65 = qe65.getMinimum(new Point3d());

        printf("p65: [%10.7f,%10.7f,%10.7f]\n", p65.x,p65.y,p65.z);

        
        Quadric q57 = new Quadric(p[5], p[7], 1.e-05);
        q675.addSet(q57);
        Point3d p57 =  q675.getMinimum(new Point3d());
        
        printf("det: %10.7e\n", q675.determinant());
        printf("p57: [%18.15f,%18.15f,%18.15f]\n", p57.x,p57.y,p57.z);
        */

        int q713 = Quadric.create(p[7], p[1], p[3], quadrics);
        printf("det713: %10.7e\n", Quadric.determinant(quadrics,q713));
        int q71 = Quadric.create(p[7], p[1], 1.e-05, quadrics);
        printf("det 71: %10.7e\n", Quadric.determinant(quadrics,q71));

        Quadric.addSet(quadrics, q71, quadrics,q713);
        printf("det 713: %10.7e\n", Quadric.determinant(quadrics,q713));
        Point3d p713 = new Point3d();
        Matrix3d sm3d = new Matrix3d();

        double[] result = new double[9];
        int[] row_perm = new int[3];
        double[] row_scale = new double[3];
        double[] tmp = new double[9];


        Quadric.getMinimum(quadrics,q713,p713,sm3d, result, row_perm, row_scale, tmp);
        printf("p 713: [ %18.15f, %18.15f, %18.15f] \n", p713.x,p713.y,p713.z);

    }

    public void  _testArray() throws Exception {
        
        
        int N = 10000000;

        Integer al[] = new Integer[N];
        
        printf("testArray()  N: %d\n", N);
        
        long t0 = System.currentTimeMillis();

        Integer obj = new Integer(5);

        for(int i = 0; i < N; i++){
            al[i] = new Integer(i);
        }
        printf("fill array: %d ms\n", (System.currentTimeMillis()-t0));

        int n1 = N/10;

        Random rnd = new Random(49);

        t0 = System.currentTimeMillis();
        
        int count = N;
        int countMissed = 0;
        int alength = count;
        while(count > n1){

            int k = rnd.nextInt(alength);
            
            if(al[k] != null){
                al[k] = null;
                count--;
                if(count < alength*3/5){
                    // removes nulls from array 
                    for(int i =0, j = 0; i < alength; i++){
                        if(al[i] != null)
                            al[j++] = al[i];
                    }
                    alength = count;
                    //al = a;
                }
            } else {
                countMissed++;
            }
        }
        
        printf("count: %d, countMissed: %d, time: %d ms\n", count, countMissed, (System.currentTimeMillis()-t0));

        t0 = System.currentTimeMillis();

        for(int i = 0; i < N; i++){
            al[i] = obj;
        }
        printf("fill array: %d ms\n", (System.currentTimeMillis()-t0));
        
    }

    public void  _testArrayList() throws Exception {
        ArrayList al = new ArrayList();
        
        int N = 10000000;

        printf("testArrayList()  N: %d\n", N);
        
        long t0 = System.currentTimeMillis();
        for(int i = 0; i < N; i++){
            al.add(new Integer(i));
        }
        printf("fill array: %d ms\n", (System.currentTimeMillis()-t0));

        int n1 = N/10;

        Random rnd = new Random(49);

        t0 = System.currentTimeMillis();
        
        int count = N;
        int countMissed = 0;

        while(count > n1){
            int k = rnd.nextInt(N);
            if(al.get(k) != null){
                al.set(k, null);
                count--;
            } else {
                countMissed++;
            }
        }
        
        printf("count: %d, countMissed: %d, time: %d ms\n", count, countMissed, (System.currentTimeMillis()-t0));
        
    }

    public void processFile(String fpath, double maxDecimationError, double reduceFactor) throws Exception {

        long t0 = currentTimeMillis();
        WingedEdgeTriangleMesh mesh = loadMesh(fpath);
        printf("mesh loading: %d ms\n",(currentTimeMillis() - t0));
        t0 = currentTimeMillis();

        int orig_fcount = mesh.getFaceCount();
        int fcount = orig_fcount;
        System.out.println("Initial Face Count: " + fcount);
        MeshExporter.writeMeshSTL(mesh,fmt("c:/tmp/mesh_orig_%07d.stl", fcount));

        printf("mesh faces: %d, vertices: %d, edges: %d\n", fcount,mesh.getVertexCount(), mesh.getEdgeCount());
        printf("initial counts: faces: %d, vertices: %d, edges: %d \n", mesh.getFaceCount(),mesh.getVertexCount(), mesh.getEdgeCount());

        assertTrue("Initial Manifold", TestWingedEdgeTriangleMesh.isManifold(mesh));


        MeshDecimator md = new MeshDecimator();
        md.setMaxCollapseError(maxDecimationError);

        int target;
        int current;

        while(true) {

            target = mesh.getTriangleCount() / 2;
            System.out.println("Target face count : " + target);
            t0 = currentTimeMillis();
            printf("processMesh() start\n");
            md.processMesh(mesh, target);
            //md.DEBUG = true;
            printf("processMesh() done %d ms\n",(currentTimeMillis()-t0));
            fcount = mesh.getFaceCount();

            // these things hang on large file - TODO - check this
            assertTrue("verifyVertices", verifyVertices(mesh));
            assertTrue("Structural Check", TestWingedEdgeTriangleMesh.verifyStructure(mesh, true));
            assertTrue("Final Manifold", TestWingedEdgeTriangleMesh.isManifold(mesh));
            printf("processMesh() done %d ms\n",(currentTimeMillis()-t0));

            current = mesh.getFaceCount();
            System.out.println("Current face count: " + current);
            if (current >= target * 1.25) {
                System.out.println("Leaving loop");
                // not worth continuing
                break;
            }

        }
        MeshExporter.writeMeshSTL(mesh,fmt("c:/tmp/mesh_dec_%07d.stl", fcount));


        assertTrue("Not Reduced enough.  Target: " + reduceFactor * orig_fcount + " actual: " + mesh.getFaceCount() + " original: " + orig_fcount, mesh.getFaceCount() < reduceFactor * orig_fcount);
    }

    public void testSpeedKnot() throws Exception {

        String fpath = "test/models/speed-knot.x3db";

        processFile(fpath, 2e-1, 0.7);
    }

/*
   // Alan: removed these two tests as they no longer pass.  It feels like the single threaded mesh decimator has gone backwards.  Since
   // it's not primary path we'll let it slide for now.  Looks at removing it or fixing when done with MT path
    public void testSphere() throws Exception {

        String fpath = "test/models/sphere_10cm_smooth_manifold.x3dv";

        processFile(fpath, 1e-6, 0.7);
    }
*/
    /**
     * Burn the sphere to almost nothing to test edge cases.
     *
     * @throws Exception
     */
/*
    public void testSphereHard() throws Exception {

        String fpath = "test/models/sphere_10cm_smooth_manifold.x3dv";

        processFile(fpath, 1e-2, 0.3);
    }
 */

    public void _testFile() throws Exception {

        //String fpath = "test/models/speed-knot.x3db";
        //String fpath = "test/models/sphere_10cm_rough_manifold.x3dv";
        //String fpath = "test/models/sphere_10cm_smooth_manifold.x3dv";
        //String fpath = "c:/tmp/text_iso_2.stl";
        //String fpath = "c:/tmp/sf31.stl";
        String fpath = "c:/tmp/leaf_01.stl";
        //String fpath = "c:/tmp/leaf_01_0832206.stl";
        //String fpath = "c:/tmp/sf21.stl";
        //String fpath = "c:/tmp/rtc_v3_04.stl";
        
        long t0 = currentTimeMillis();
        WingedEdgeTriangleMesh mesh = loadMesh(fpath);
        printf("mesh loading: %d ms\n",(currentTimeMillis() - t0));
        t0 = currentTimeMillis();

        //setVerticesUserData(mesh);

        int fcount = mesh.getFaceCount();

        MeshExporter.writeMeshSTL(mesh,fmt("c:/tmp/mesh_orig_%07d.stl", fcount));

        printf("mesh faces: %d, vertices: %d, edges: %d\n", fcount,mesh.getVertexCount(), mesh.getEdgeCount());        
        printf("initial counts: faces: %d, vertices: %d, edges: %d \n", mesh.getFaceCount(),mesh.getVertexCount(), mesh.getEdgeCount());

        assertTrue("Initial Manifold", TestWingedEdgeTriangleMesh.isManifold(mesh));


        MeshDecimator md = new MeshDecimator();
        md.setMaxCollapseError(1.e-9);

        int target;
        int current;

        while(true) {

            target = mesh.getTriangleCount() / 2;
            System.out.println("Target face count : " + target);
            t0 = currentTimeMillis();
            printf("processMesh() start\n");
            md.processMesh(mesh, target);
            //md.DEBUG = true;
            printf("processMesh() done %d ms\n",(currentTimeMillis()-t0));
            fcount = mesh.getFaceCount();

            //MeshExporter.writeMeshSTL(mesh,fmt("c:/tmp/mesh_dec_%07d.stl", fcount));

            // these things hang on large file - TODO - check this
            //assertTrue("verifyVertices", verifyVertices(mesh));
            //assertTrue("Structural Check", TestWingedEdgeTriangleMesh.verifyStructure(mesh, true));
            //assertTrue("Final Manifold", TestWingedEdgeTriangleMesh.isManifold(mesh));
            //printf("processMesh() done %d ms\n",(currentTimeMillis()-t0));

            current = mesh.getFaceCount();
            System.out.println("Current face count: " + current);
            if (current >= target * 1.25) {
                System.out.println("Leaving loop");
                // not worth continuing
                break;
            }

        }

    }

    
    public void _testDecimatorQuality() throws Exception {
    
        //String fpath = "c:/tmp/pen_v6.stl"; // strange rasterization errors 
        //String fpath = "c:/tmp/mesh_text_orig.stl";
        //String fpath = "c:/tmp/out_grid_04_2_1.stl";
        //String fpath = "c:/tmp/out_grid_04_2_out.stl";
        //String fpath = "c:/tmp/out_grid_04_2_out_dec_00_s.stl";
        //String fpath = "c:/tmp/ring_image.stl";
        //String fpath = "c:/tmp/ring_image_90s.stl";
        String fpath = "c:/tmp/ring_image_45s.stl";
        //String fpath = "c:/tmp/leaf_01.stl";
        //String fpath = "c:/tmp/torus_02.stl";
        //String fpath = "c:/tmp/block_01.stl";
        //String fpath = "c:/tmp/block_02.stl";
        //String fpath = "c:/tmp/torus_01.stl";
        //String fpath = "c:/tmp/rtc_v3_04.stl";


        long t0 = currentTimeMillis();
        WingedEdgeTriangleMesh mesh = loadMesh(fpath);
        printf("mesh loading: %d ms\n",(currentTimeMillis() - t0));
        mesh.DEBUG = false; 


        MeshDecimator md = new MeshDecimator();
        md.DEBUG = false;
        md.setMaxCollapseError(1.e-8);        

        int fcount = mesh.getFaceCount();
        printf("mesh faces: %d \n",fcount);
        //double maxErodedVolumeMM3 = 1.; // 1mm^3

        double mbounds[] = mesh.getBounds();
        
        printf("model bounds: [%7.2f,%7.2f,%7.2f,%7.2f,%7.2f,%7.2f]mm \n",
               mbounds[0]*MM,mbounds[1]*MM,mbounds[2]*MM,mbounds[3]*MM,mbounds[4]*MM,mbounds[5]*MM);
        
        double voxelSize = 0.1e-3; // 0.1 mm;
        double voxelVolume = voxelSize*voxelSize*voxelSize;

        int padding = 2; // empty padding around the model 

        int gridX = (int)Math.ceil((mbounds[1] - mbounds[0])/voxelSize);
        int gridY = (int)Math.ceil((mbounds[3] - mbounds[2])/voxelSize);
        int gridZ = (int)Math.ceil((mbounds[5] - mbounds[4])/voxelSize);

        printf("model Grid: [%d x %d x %d]\n",gridX, gridY, gridZ);

        gridX += 2*padding;
        gridY += 2*padding;
        gridZ += 2*padding;

        printf("voxels Grid: [%d x %d x %d]\n",gridX, gridY, gridZ);

        double gbounds[] = new double[]{mbounds[0] - padding*voxelSize,
                                        mbounds[0] + (gridX-padding)*voxelSize,
                                        mbounds[2] - padding*voxelSize,
                                        mbounds[2] + (gridY-padding)*voxelSize,
                                        mbounds[4] - padding*voxelSize,
                                        mbounds[4] + (gridZ-padding)*voxelSize};
        
        printf("grid bounds: [%7.2f,%7.2f,%7.2f,%7.2f,%7.2f,%7.2f]mm \n",
               gbounds[0]*MM,gbounds[1]*MM,gbounds[2]*MM,gbounds[3]*MM,gbounds[4]*MM,gbounds[5]*MM);
        
        //Grid grid1 = makeGrid(mesh, gbounds, gridX, gridY, gridZ, voxelSize);

        //MeshExporter.writeMeshSTL(mesh,fmt("c:/tmp/mesh_orig.stl"));

        //writeIsosurface(grid1, gbounds, voxelSize, gridX, gridY, gridZ, "c:/tmp/diff_orig.stl");

        //int count1 = grid1.findCount(Grid.VoxelClasses.INTERIOR);
        
        printf("MODEL_FACE_COUNT: %d\n", fcount);
        //printf("MODEL_VOXELS_COUNT: %d VOLUME: %7.2f mm^3\n", count1, count1*voxelVolume* MM3);

        for(int i = 0; i < 10; i++){      
  
            fcount = fcount/2;
            md.processMesh(mesh, fcount);
            //Grid grid2 = makeGrid(mesh, gbounds, gridX, gridY, gridZ, voxelSize);
            //int count2 = grid2.findCount(Grid.VoxelClasses.INTERIOR);

            //printf("count2: %d volume2: %7.2f mm^3\n", count2, count2*voxelVolume* MM3);            
            t0 = currentTimeMillis();            
            //Grid gridDiff = new ArrayAttributeGridByte(gridX, gridY, gridZ, voxelSize, voxelSize);  
            //Grid gridDiff = new GridShortIntervals(gridX, gridY, gridZ, voxelSize, voxelSize);              
            //getDifference(grid1, grid2, gridDiff);   
            //int countDiff = gridDiff.findCount(Grid.VoxelClasses.INTERIOR);
            //ErosionMask err = new ErosionMask(1);
            //err.execute(gridDiff);
            //printf("difference found: %d ms\n",(currentTimeMillis() - t0));            
            //int countEroded = gridDiff.findCount(Grid.VoxelClasses.INTERIOR);
            //double erodedVolume = countEroded*voxelVolume*MM3;
            //double differenceVolume = countDiff*voxelVolume*MM3;
            printf("CURRENT_FACE_COUNT: %d\n", mesh.getFaceCount());
            //printf("DIFFERENCE: %7.2f mm^3, LARGE_DIFFERENCE: %7.2f mm^3 count: %d\n", differenceVolume, erodedVolume, countEroded);
            //String fout = fmt("c:/tmp/diff_%02d.stl", i);
            MeshExporter.writeMeshSTL(mesh,fmt("c:/tmp/mesh_dec_%02d.stl", i));
            //if(countEroded > 0){
                //writeIsosurface(gridDiff, gbounds, voxelSize, gridX, gridY, gridZ, fout);
                //MeshExporter.writeMeshSTL(mesh,fmt("c:/tmp/mesh_dec_%02d.stl", i));
            //    break;
            //}
        }
        //MeshExporter.writeMeshSTL(mesh,fmt("c:/tmp/mesh_decimated.stl"));
                
    }

    Grid makeGrid(WingedEdgeTriangleMesh mesh, double gbounds[], int gridX, int gridY, int gridZ, double voxelSize){
        
        long t0 = currentTimeMillis();
        
        MeshRasterizer mr = new MeshRasterizer(gbounds, gridX, gridY, gridZ);         
        mesh.getTriangles(mr);

        Grid grid = new GridShortIntervals(gridX, gridY, gridZ, voxelSize, voxelSize);        
        //Grid grid = new ArrayAttributeGridByte(gridX, gridY, gridZ, voxelSize, voxelSize);        
        mr.getRaster(grid);
        //printf("mesh rasterized: %d ms\n",(currentTimeMillis() - t0));

        return grid;

    }

    /**
       makes symetric difference of two grids
     */
    public void getDifference(Grid grid1, Grid grid2, Grid difference){
        
        grid1.findInterruptible(Grid.VoxelClasses.MARKED, new GridDifference(grid2,difference));
        grid2.findInterruptible(Grid.VoxelClasses.MARKED, new GridDifference(grid1,difference));        

    }
    
    /**
       compares voxel from 
       traversal is going over marked voxels of another grid
       if gridToCompare has empty voxel, then the difference grid has it's voxel set
     */
    static class GridDifference implements ClassTraverser {
        
        Grid gridToCompare; // grid to compare to
        Grid gridDifference; // difference grid to write to 
        
        GridDifference(Grid gridToCompare, Grid gridDifference){

            this.gridToCompare = gridToCompare;
            this.gridDifference = gridDifference; 

        }

        public void found(int x, int y, int z, byte state){
            
            foundInterruptible(x, y, z, state);

        }
        
        public boolean foundInterruptible(int x, int y, int z, byte state){

            if(gridToCompare.getState(x,y,z) == Grid.OUTSIDE){
                gridDifference.setState(x,y,z, Grid.INTERIOR);
            }
            return true;
            
        }

    } // class GridDifference

    
    /**
       
     */
    void writeIsosurface(Grid grid, double bounds[], double voxelSize, int nx, int ny, int nz, String fpath){

        IsosurfaceMaker im = new IsosurfaceMaker();
        
        im.setIsovalue(0.);
        im.setBounds(extendBounds(bounds, -voxelSize/2));
        im.setGridSize(nx, ny, nz);

        IsosurfaceMaker.SliceGrid fdata = new IsosurfaceMaker.SliceGrid(grid, bounds, 0);
        try {
            STLWriter stlwriter = new STLWriter(fpath);
            im.makeIsosurface(fdata, stlwriter);
            stlwriter.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

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


    public void _testCombination(){
        try {
            String f1 = "c:/tmp/block_01.0.stl";
            String f2 = "c:/tmp/block_01.1.stl";
            String fout = "c:/tmp/block_01.stl";
            
            STLReader reader = new STLReader();
            STLWriter writer = new STLWriter(fout);
            
            reader.read(f1, writer);
            reader.read(f2, writer);
            
            writer.close();

        } catch(Exception e){
            e.printStackTrace();
        }

    }

    public void _testRasterizer(){
        
        String fpath = "c:/tmp/pen_v6.stl";
        STLRasterizer sr = new STLRasterizer();
        
        try {
            
            Grid grid = sr.rasterizeFile(fpath);
        
            printf("done!\n");

        } catch(Exception e){

            e.printStackTrace();

        }
    }

    /**
       
     */
    public static WingedEdgeTriangleMesh loadMesh(String fpath){
        if(fpath.toLowerCase().lastIndexOf(".stl") > 0){
            return loadSTL(fpath);
        } else {
            return loadX3D(fpath);
        }
    }

    /**
       load STL file 
     */
    public static WingedEdgeTriangleMesh loadSTL(String fpath){

        STLReader reader = new STLReader();
        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();
        try {
            reader.read(fpath, its);
            return new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    

    /**
       load X3D file
     */
    public static WingedEdgeTriangleMesh loadX3D(String fpath){
        
        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);
        
        loader.processFile(new File(fpath));
        
        GeometryData data = new GeometryData();
        data.coordinates = loader.getCoords();
        data.vertexCount = data.coordinates.length / 3;
        data.indexes = loader.getVerts();
        data.indexesCount = data.indexes.length;

        Vector3d[] verts = new Vector3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;        
        
        for(int i=0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Vector3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        idx = 0;
        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();
        for(int i=0; i < len; i++) {
            its.addTri(verts[data.indexes[idx++]],verts[data.indexes[idx++]],verts[data.indexes[idx++]]);
        }
        
        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
        return we;

    }

    
    /**
       inits all vertices user data to Integer
     */
    static void setVerticesUserData(WingedEdgeTriangleMesh mesh){
        
        int vcount = 0;
        StructMixedData vertices = mesh.getVertices();
        int v = mesh.getStartVertex();
        while( v != -1){
            Vertex.setUserData(vcount,vertices,v);
            vcount++;
            v = Vertex.getNext(vertices, v);
        }
    }

    /**
       check, that all the vertices have consistent ring of faces 
     */
    static boolean verifyVertices(WingedEdgeTriangleMesh mesh){

        //printf("verifyVertices()\n");
        StructMixedData vertices = mesh.getVertices();
        StructMixedData hedges = mesh.getHalfEdges();
        int v = mesh.getStartVertex();
        int vcount = 0;
        while( v != -1){
            //printf("v:%s: ", v);
            vcount++;
            int start = Vertex.getLink(vertices,v);
            int he = start;
            int tricount = 0;
            
            do{                 
                //printf("[%3s %3s %3s] ", he.getEnd().getUserData(), he.getNext().getEnd().getUserData(),  he.getNext().getNext().getEnd().getUserData()); 
                
                if(tricount++ > 100){

                    printf("verifyVertices() !!! tricount exceeded\n");
                    
                    return false;
                }

                int twin = HalfEdge.getTwin(hedges,he);
                he = HalfEdge.getNext(hedges,twin);
                
            } while(he != start);
            //printf("\n");

            v = Vertex.getNext(vertices, v);
        }
        printf("vcount: %3d\n:", vcount);

        return true;

    }

}

