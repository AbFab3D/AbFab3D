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

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors; 
import java.util.concurrent.TimeUnit;


import java.util.Random;
import java.util.Vector;


import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.GridShortIntervals;
import abfab3d.grid.Grid;

import abfab3d.grid.op.GridMaker;

import abfab3d.io.input.STLReader;

import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.MeshExporter;
import abfab3d.io.output.STLWriter;
import abfab3d.io.output.MeshMakerMT;


import abfab3d.util.DataSource;
import abfab3d.util.Vec;
import abfab3d.util.MathUtil;
import abfab3d.util.TriangleCounter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// External Imports

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static java.lang.System.nanoTime;

import static abfab3d.mesh.TestMeshDecimator.loadMesh;

/**
 * Tests the functionality of MeshDecimatorMT 
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestShellFinder extends TestCase {
    
    static final double MM = 0.001; // mm -> m conversion 

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestShellFinder.class);
    }

    public void _testOpenBox() throws Exception {

        double vert[] = new double[]{
                1, -1, 1,
                1, 1, 1,
                -1, 1, 1,
                -1, -1, 1,
                1, -1, -1,
                1, 1, -1,
                -1, 1, -1,
                -1, -1, -1
        };
        int indexes[] = new int[]{
                2, 3, 0,
                1, 2, 0,

                //5, 1, 0,
                //4, 5, 0,

                //5, 6, 2,
                //5, 2, 1,

                //2, 6, 7,
                //2, 7, 3,

                6, 5, 4,
                7, 6, 4,

                //3, 7, 4,
                //3, 4, 0
        };

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(vert, indexes);

        ShellFinder sf = new ShellFinder();
        
        ShellFinder.ShellInfo si[] = sf.findShells(mesh);
                
    }

    public void testFile()  throws Exception {

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

        STLReader sr = new STLReader();
        sr.read("/tmp/multishell_test.stl", its);

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
        
        ShellFinder sf = new ShellFinder();

        ShellFinder.ShellInfo si[] = sf.findShells(mesh);

        printf("shells count: %d\n", si.length);

        for(int i =0; i < si.length; i++){
            
            printf("face: %7d count: %7d\n", si[i].startFace, si[i].faceCount);
            STLWriter stl = new STLWriter(fmt("/tmp/00_shell_%05d.stl",i));
            sf.getShell(mesh, si[i].startFace, stl);
            stl.close();
        }

    }


    public void _testSpheres()  throws Exception {
        for(int i =0; i < 5; i++)
            runSpheres();
    }
    
    public void runSpheres()  throws Exception {
        
        long t00 = time();
        int nx = 100; // grid dimension 
        double voxelSize = 0.1*MM; 
        double radius = 0.97;

        double bodySize = nx * voxelSize; 
        double gridBounds[] = new double[]{0,bodySize, 0,bodySize,0,bodySize};

        double cellSize = bodySize/2;

        printf("grid: [%d x %d x %d]\n", nx, nx, nx);
        
        ArrayOfSpheres spheres = new ArrayOfSpheres(cellSize, radius);

        long t0 = time();

        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx,nx,nx,voxelSize, voxelSize);
        
        GridMaker gridMaker = new GridMaker();
        
        gridMaker.setBounds(gridBounds);
        gridMaker.setDataSource(new ArrayOfSpheres(cellSize, radius));
        
        gridMaker.makeGrid(grid); 

        printf("grid made: %d ms\n", (time() - t0));        
        
        t0 = time();
        double ibounds[] = MathUtil.extendBounds(gridBounds, -voxelSize/2);
        
        IsosurfaceMaker im = new IsosurfaceMaker();
        im.setIsovalue(0.);
        im.setBounds(ibounds);
        im.setGridSize(nx, nx, nx);

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();

        im.makeIsosurface(new IsosurfaceMaker.SliceGrid(grid, gridBounds, 0), its);
        printf("isosurface made: %d ms\n", (time() - t0));  
        t0 = time();
        
        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        printf("mesh created: %d ms\n", (time() - t0));
        
        //MeshExporter.writeMeshSTL(mesh, "/tmp/spheres.stl");
        
        ShellFinder sf = new ShellFinder();

        t0 = time();

        ShellFinder.ShellInfo si[] = sf.findShells(mesh);
        printf("ShellFinder time: %d ms\n", (time() - t0));

        printf("shells count: %d\n", si.length);

        for(int i =0; i < si.length; i++){
            
            printf("face: %7d count: %7d\n", si[i].startFace, si[i].faceCount);
            STLWriter stl = new STLWriter(fmt("/tmp/00_shell_%05d.stl",i));
            sf.getShell(mesh, si[i].startFace, stl);
            stl.close();
        }
        
    }
    

    static class ArrayOfSpheres implements DataSource {
        
        double r = 0.97;
        double r2 = r*r;
        double period;

        ArrayOfSpheres(double period, double radius){
            this.period = period; 
            this.r = radius;
            r2 = r*r;
        }
        
        public int getDataValue(Vec pnt, Vec data) {
            
            double x = pnt.v[0]/period;
            double y = pnt.v[1]/period;
            double z = pnt.v[2]/period;
            x = 2*(x - Math.floor(x)-0.5);
            y = 2*(y - Math.floor(y)-0.5);
            z = 2*(z - Math.floor(z)-0.5);

            // x, y, z are in [-1,1]
            data.v[0] = 10*(r2 - (x*x + y*y + z*z));
            
            return RESULT_OK;
            
        }                
        
    } // class ArrayOfSpheres   
}

