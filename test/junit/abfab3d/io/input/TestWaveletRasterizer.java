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

package abfab3d.io.input;

// External Imports


import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.vecmath.Vector3d;

import java.awt.Font;
import java.awt.Insets;


import java.io.File;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;


// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.GridShortIntervals;

import abfab3d.geom.TriangulatedModels;

import abfab3d.util.Vec;
import abfab3d.util.VecTransform;
import abfab3d.util.MathUtil;
import abfab3d.util.TextUtil;
import abfab3d.util.Symmetry;
import abfab3d.util.ImageGray16;

import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.STLWriter;
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.SlicesWriter;

import abfab3d.util.ImageMipMap;
import abfab3d.util.BoundingBoxCalculator;
import abfab3d.util.MathUtil;

import abfab3d.grid.op.DataSources;
import abfab3d.grid.op.VecTransforms;
import abfab3d.grid.op.VolumePatterns;
import abfab3d.grid.op.GridMaker;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.MathUtil.TORAD;

import static java.lang.System.currentTimeMillis;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static java.lang.Math.PI;

/**
 * Tests the functionality of SlicesWriter
 *
 * @version
 */
public class TestWaveletRasterizer extends TestCase {

    static final double CM = 0.01; // cm -> meters
    static final double MM = 0.001; // mm -> meters

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestWaveletRasterizer.class);
    }

  
    public void _testTriangle() throws Exception {

        double bounds[] = new double[]{0.,1.,0.,1.,0.,1.};

        int level = 1;
        int maxAttribute = 63;
        
        int nx = 1<<level, ny = 1<<level, nz = 1<<level;
        
        WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);
        
        // set maximal value of data item to be stored in the grid voxels
        rasterizer.setMaxAttributeValue(63); 
        
        double z = 0.1;

        rasterizer.addTri(new Vector3d(0.1,0.1,z),new Vector3d(0.6,0.1,z),new Vector3d(0.6,0.8,0.7));

        
    }
        
    public void _testParalelepiped() throws Exception {
        
        
        int level = 1;
        int maxAttribute = 63;
        int nx = 1<<level, ny = 1<<level, nz = 1<<level;
        double bounds[] = new double[]{0.,1.,0.,1.,0.,1.};
        
        TriangulatedModels.Parallelepiped pp = new  TriangulatedModels.Parallelepiped(0.25, 0.25, 0.25, 0.75,  0.75,  0.75);
        //TriangulatedModels.Parallelepiped pp = new  TriangulatedModels.Parallelepiped(0.55, 0.55, 0.55, 0.95,  0.95,  0.95);
        
        WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);

        pp.getTriangles(rasterizer);
        
    }

    public void _testParalelepiped1() throws Exception {
        
        
        int level = 9;
        int maxAttribute = 63;
        int nx = 1<<level, ny = 1<<level, nz = 1<<level;
        //double bounds[] = new double[]{0.,1.,0.,1.,0.,1.};
        double voxelSize = 0.5;

        printf("wavelet rasterization grid: [%d x %d x %d]\n", nx, ny, nz);

        //TriangulatedModels.TetrahedronInParallelepiped pp = new  TriangulatedModels.TetrahedronInParallelepiped(0.25, 0.25, 0.25, 0.75,  0.75,  0.75,0);
        //TriangulatedModels.TetrahedronInParallelepiped pp = new  TriangulatedModels.TetrahedronInParallelepiped(0.55, 0.55, 0.55, 0.95,  0.95,  0.95,0);
        //TriangulatedModels.TetrahedronInParallelepiped pp = new  TriangulatedModels.TetrahedronInParallelepiped(0.05, 0.05, 0.05, 0.45,  0.45,  0.45,1);
        //TriangulatedModels.TetrahedronInParallelepiped pp = new  TriangulatedModels.TetrahedronInParallelepiped(0.01, 0.01, 0.01, 0.24,  0.24, 0.24,0);
        //TriangulatedModels.Parallelepiped pp = new  TriangulatedModels.Parallelepiped(0.00001, 0.00001, 0.00001, 0.249999,  0.24999,  0.24999);
        double eps = 0.12;//1.e-6;
        double size = 0.25;
        double shiftx = 0*size;
        double shifty = 0*size;
        double shiftz = 0*size;
        //TriangulatedModels.Parallelepiped pp = new  TriangulatedModels.Parallelepiped(eps+shiftx, eps+shifty, eps+shiftz, 4*size-eps+shiftx, 4*size-eps+shifty, 4*size-eps+shiftz);
        TriangulatedModels.TetrahedronInParallelepiped pp = 
            new  TriangulatedModels.TetrahedronInParallelepiped(eps+shiftx, eps+shifty, eps+shiftz, 4*size-eps+shiftx, 3*size-eps+shifty, 2*size-eps+shiftz, 0);
        TriangulatedModels.TetrahedronInParallelepiped pp1 = 
            new  TriangulatedModels.TetrahedronInParallelepiped(eps+shiftx, eps+shifty, eps+shiftz, 2*size-eps+shiftx, 4*size-eps+shifty, 4*size-eps+shiftz, 0);

        double bounds[] = new double[]{-1.,1.,-1.,1.,-1.,1.};
        TriangulatedModels.Star star = new TriangulatedModels.Star(400, 0.002, 0.001, 0.001, 0.6, 0.7);
        
        WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);
        rasterizer.setMaxAttributeValue(maxAttribute);
        
        long t0 = time();

        star.getTriangles(rasterizer);
        //pp.getTriangles(rasterizer);
        //pp1.getTriangles(rasterizer);
        printf("tree calculation: %d ms\n", (time() - t0));
        
        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        t0 = time();

        rasterizer.getRaster(grid);
        
        printf("rasterization: %d ms\n", (time() - t0));

        SlicesWriter slicer = new SlicesWriter();
        slicer.setFilePattern("/tmp/slices/slice_%03d.png");
        slicer.setCellSize(2);
        slicer.setVoxelSize(2);
        
        slicer.setMaxAttributeValue(maxAttribute);

        //slicer.writeSlices(grid);

    }


    public void testSTLfile() throws Exception {
        
        
        //int level = 9;
        int maxAttribute = 63;
        //double bounds[] = new double[]{0.,1.,0.,1.,0.,1.};
        double voxelSize = 0.1*MM;
        

        //String filePath = "/tmp/00_image_1x1.stl";
        String filePath = "/tmp/dodecahedron_1a_100mm.stl";
        //String filePath = "/tmp/00_image_4x4_bad.stl";
        
        printf("reading file: %s\n", filePath);

        STLReader stl = new STLReader();
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        stl.read(filePath, bb);        
        double bounds[] = new double[6];
        bb.getBounds(bounds);
        printf(" bounds:[(%7.2f %7.2f) (%7.2f %7.2f) (%7.2f %7.2f)] MM \n", bounds[0]/MM,bounds[1]/MM,bounds[2]/MM,bounds[3]/MM,bounds[4]/MM,bounds[5]/MM);
        MathUtil.roundBounds(bounds, voxelSize);
        printf("rbounds:[(%7.2f %7.2f) (%7.2f %7.2f) (%7.2f %7.2f)] MM \n", bounds[0]/MM,bounds[1]/MM,bounds[2]/MM,bounds[3]/MM,bounds[4]/MM,bounds[5]/MM);
        bounds = MathUtil.extendBounds(bounds, 1*voxelSize);
        printf("ebounds:[(%7.2f %7.2f) (%7.2f %7.2f) (%7.2f %7.2f)] MM \n", bounds[0]/MM,bounds[1]/MM,bounds[2]/MM,bounds[3]/MM,bounds[4]/MM,bounds[5]/MM);
        int nx = (int)Math.round((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)Math.round((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)Math.round((bounds[5] - bounds[4])/voxelSize);

        printf("grid size: [%d x %d x %d]\n", nx, ny, nz);
        
        WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);
        
        rasterizer.setMaxAttributeValue(maxAttribute);
        
        long t0 = time();

        stl.read(filePath, rasterizer); 

        printf("octree calculation: %d ms\n", (time() - t0));
        
        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        grid.setGridBounds(bounds);
        //AttributeGrid grid = new ArrayAttributeGridByte(64, 64, 64, voxelSize, voxelSize);
        t0 = time();

        rasterizer.getRaster(grid);
        
        printf("rasterization: %d ms\n", (time() - t0));

        SlicesWriter slicer = new SlicesWriter();
        slicer.setFilePattern("/tmp/slices/slice_%03d.png");
        slicer.setCellSize(1);
        slicer.setVoxelSize(1);
        
        slicer.setMaxAttributeValue(maxAttribute);

        //slicer.writeSlices(grid);
        
        int blockSize = 50;
        double errorFactor = 0.1;
        double smoothWidth = 1;
        int maxDecimationCount= 10;
        int threadsCount = 4;
        //double voxelSize = 2*s/grid.getWidth();

        double maxDecimationError = errorFactor*voxelSize*voxelSize;

        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(blockSize);
        meshmaker.setThreadCount(threadsCount);
        meshmaker.setSmoothingWidth(smoothWidth);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(maxDecimationCount);
        meshmaker.setMaxAttributeValue(maxAttribute);            
        
        STLWriter stlw = new STLWriter("/tmp/raster_to_voxels.stl");
        meshmaker.makeMesh(grid, stlw);
        stlw.close();
        
    }


    public static void main(String[] args) {

    }
}
