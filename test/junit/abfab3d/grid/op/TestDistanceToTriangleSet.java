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

import java.util.Random;

import abfab3d.grid.VectorIndexerStructMap;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridShort;
import abfab3d.grid.ArrayAttributeGridInt;

import abfab3d.util.Bounds;
import abfab3d.util.PointMap;
import abfab3d.util.TriangleCollector;
import abfab3d.util.BoundingBoxCalculator;

import abfab3d.distance.DistanceData;
import abfab3d.distance.DistanceDataSphere;

import abfab3d.io.output.STLWriter;
import abfab3d.io.input.STLReader;


import abfab3d.geom.TriangulatedModels;
import abfab3d.geom.PointCloud;
import abfab3d.geom.Octahedron;

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
       testing distance to sphere 
     */
    void makeTestSTL()throws Exception{
        
        if(DEBUG) printf("makeTestSTL()\n");
        String filePath = "/tmp/crab_vs0.2.stl";
        int maxGridDimension = 500;

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
            //new TestDistanceToTriangleSet().makeTestSphere();
            new TestDistanceToTriangleSet().makeTestSTL();
        }        
    }
}
