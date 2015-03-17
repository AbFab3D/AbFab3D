/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fxi it.
 *
 ****************************************************************************/

package abfab3d.io.output;

// External Imports


import javax.vecmath.Vector3d;


// external imports
import junit.framework.TestCase;


// Internal Imports
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.util.ReflectionGroup;

import abfab3d.util.MathUtil;

import abfab3d.grid.op.GridMaker;

import abfab3d.datasources.Box;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Ring;
import abfab3d.datasources.DataTransformer;
import abfab3d.datasources.Intersection;
import abfab3d.datasources.Union;
import abfab3d.datasources.Subtraction;
import abfab3d.datasources.Triangle;
import abfab3d.datasources.Cylinder;
import abfab3d.datasources.LimitSet;
import abfab3d.datasources.VolumePatterns;

import abfab3d.transforms.RingWrap;
import abfab3d.transforms.FriezeSymmetry;
import abfab3d.transforms.WallpaperSymmetry;
import abfab3d.transforms.Rotation;
import abfab3d.transforms.CompositeTransform;
import abfab3d.transforms.Scale;
import abfab3d.transforms.SphereInversion;
import abfab3d.transforms.Translation;
import abfab3d.transforms.PlaneReflection;


import abfab3d.transforms.*;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.CM;
import static abfab3d.util.Units.MM;


import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static java.lang.Math.PI;


/**
 * Tests the functionality of SlicesWriter
 *
 * @version
 */
public class DevTestMeshMakerMT {

    public static void makeSphere() throws Exception {
        
        printf("makeIcosahedron()\n");    

        double voxelSize = 0.1*MM;
        double margin = 0;

        double s = 40*MM;


        double bounds[] = new double[]{-s, s, -s, s, -s, s};
        
        MathUtil.roundBounds(bounds, voxelSize);
        bounds = MathUtil.extendBounds(bounds, margin);                

        int maxAttributeValue = 255;
        int blockSize =25;
        double errorFactor = 0.05;
        double smoothWidth = 1;
        int maxDecimationCount= 10;
        int threadsCount = 4;
        int maxTriCount = 10000;
        double surfareThickness = sqrt(3)/2;
        double maxDecimationError = errorFactor*voxelSize*voxelSize;
        int nx[] = MathUtil.getGridSize(bounds, voxelSize);

        printf("grid: [%d x %d x %d]\n", nx[0],nx[1],nx[2]);
        Union union = new Union();

        //union.add(new Cylinder(v5, v3, rs));
        //union.add(new Cylinder(v5, v2, rs));
        //union.add(new Cylinder(v3, v2, rs));
        union.add(new Sphere(new Vector3d(0,0,0), 0.95*s));

        GridMaker gm = new GridMaker();  

        gm.setSource(union);

        gm.setMaxAttributeValue(maxAttributeValue);
        gm.setVoxelSize(voxelSize*surfareThickness);
        
        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx[0], nx[1], nx[2], voxelSize, voxelSize);
        grid.setGridBounds(bounds);

        long t0 = time();
        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done in %d ms\n", (time() - t0));
        
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(blockSize);
        meshmaker.setThreadCount(threadsCount);
        meshmaker.setSmoothingWidth(smoothWidth);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(maxDecimationCount);
        meshmaker.setMaxAttributeValue(maxAttributeValue);            
        meshmaker.setMaxTriangles(maxTriCount);            
        
        STLWriter stl = new STLWriter("/tmp/sphere.stl");
        meshmaker.makeMesh(grid, stl);
        stl.close();
        
    }    

    static void testOctree(){
        
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.makeBlocksOctree(2494, 850, 1204, 136);
        
    }

    public static void main(String[] args) throws Exception {

        //makeSphere();
        testOctree();

    }
}