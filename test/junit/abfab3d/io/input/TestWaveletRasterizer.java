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


    /**
       makes a set of boxes and checks volume 
     */
    public void testBox() throws Exception {

        printf("testBox()\n");

        boolean writeFiles = false;
        //int maxAttribute = 10031;  // can be used for GridShortIntervals        
        int maxAttribute = 63; // max value for AttributeGridByte
        double precision = 1; // this is relative presision of volume calculations 
        // volume presizion is within (voxelSize / maxAttribute)
        //AttributeGrid gridType = new ArrayAttributeGridByte(1,1,1,0.1, 0.1);
        AttributeGrid gridType = new GridShortIntervals(1,1,1,0.1, 0.1);

        for(int level = 6; level <=9; level++){

            int gridSize = 1 << level;
            double bounds[] = new double[]{0.,2.,0.,2.,0.,2.};
            double originX = 0.1, originY = 0.3, originZ = 0.35;  double boxSizeX = 0.7, boxSizeY = 0.85, boxSizeZ = 1.2;
            //double originX = 0.1234, originY = 0.35657, originZ = 0.35555;  double boxSizeX = 0.76456456, boxSizeY = 0.854566, boxSizeZ = 1.2236457;
            
            double voxelSize = (bounds[1]- bounds[0])/gridSize;
            double voxelVolume = voxelSize*voxelSize*voxelSize;
            
            printf("grid size: %d\n", gridSize);
            
            printf("running WaveletRasterizer\n");
            WaveletRasterizer rasterizer = new WaveletRasterizer(bounds, gridSize, gridSize, gridSize);        
            rasterizer.setMaxAttributeValue(maxAttribute);

            TriangulatedModels.Parallelepiped box = new  TriangulatedModels.Parallelepiped(originX, originY, originZ, originX+boxSizeX, originY+boxSizeY, originZ+boxSizeZ);  
            long t0 = time();
            box.getTriangles(rasterizer);
            printf("WaveletRasterizer octree build time: %d ms\n", (time() - t0));
            t0 = time();
            AttributeGrid grid1 = (AttributeGrid)gridType.createEmpty(gridSize,gridSize,gridSize,voxelSize, voxelSize);
            grid1.setGridBounds(bounds);
            
            rasterizer.getRaster(grid1);
            printf("WaveletRasterizer rasterization time: %d ms\n", (time() - t0));
            
            double volumeWR = voxelVolume*getAttributeGridVolume(grid1, maxAttribute);
            double exactVolume = boxSizeX * boxSizeY * boxSizeZ;
            double differentceWR = ((volumeWR-exactVolume)/voxelSize)*maxAttribute;
            printf("WRvolume: %18.15f  WRdiff: %18.15f\n", volumeWR, differentceWR);
                                                                                                              
            assertTrue("Test of WR grid volume presision", Math.abs(differentceWR) < precision);

            if(writeFiles){
                MeshMakerMT meshmaker1 = new MeshMakerMT();
                meshmaker1.setBlockSize(30);
                meshmaker1.setThreadCount(1);
                meshmaker1.setSmoothingWidth(0);
                meshmaker1.setMaxDecimationError(0);
                meshmaker1.setMaxDecimationCount(0);
                meshmaker1.setMaxAttributeValue(maxAttribute);            
                STLWriter stlw1 = new STLWriter(fmt("/tmp/grid1_%03d.stl", gridSize));
                meshmaker1.makeMesh(grid1, stlw1);
                stlw1.close();
            }
            
            printf("running MeshRasterizer\n");
            t0 = time();

            MeshRasterizer rasterizer2 = new MeshRasterizer(bounds, gridSize, gridSize, gridSize);
            
            TriangulatedModels.Parallelepiped box2 = new  TriangulatedModels.Parallelepiped(originX, originY, originZ, originX+boxSizeX, originY+boxSizeY, originZ+boxSizeZ);  
            box2.getTriangles(rasterizer2);

            AttributeGrid grid2 = (AttributeGrid)gridType.createEmpty(gridSize,gridSize,gridSize,voxelSize, voxelSize);
            grid2.setGridBounds(bounds);
            rasterizer2.getRaster(grid2);
            printf("MeshRasterizer rasterization time: %d ms\n", (time() - t0));
            
            double volumeZB = voxelVolume*getGridVolume(grid2);
            double differenceZB = ((volumeZB-exactVolume)/voxelSize);
            printf("ZBvolume: %18.15f  ZBdiff: %18.15f\n", volumeZB, differenceZB);

            assertTrue("Test of ZB grid volume presision", Math.abs(differentceWR) < precision);

            if(writeFiles){
                MeshMakerMT meshmaker = new MeshMakerMT();
                meshmaker.setBlockSize(30);
                meshmaker.setThreadCount(4);
                meshmaker.setSmoothingWidth(0);
                meshmaker.setMaxDecimationError(0.1*voxelSize*voxelSize);
                meshmaker.setMaxDecimationCount(5);
                meshmaker.setMaxAttributeValue(0);            
                
                STLWriter stlw = new STLWriter(fmt("/tmp/gridWR_%03d.stl", gridSize));
                meshmaker.makeMesh(grid2, stlw);
                stlw.close();
            }
            
        }
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


    public void _testSTLfile() throws Exception {
        
        
        //int level = 9;
        int maxAttribute = 63;
        //double bounds[] = new double[]{0.,1.,0.,1.,0.,1.};
        double voxelSize = 0.1*MM;
        

        //String filePath = "/tmp/00_image_1x1.stl";
        String filePath = "/tmp/dodecahedron_1a_100mm.stl";
        //String filePath = "/tmp/00_image_4x4_bad.stl";
        //String filePath = "/tmp/star_400.stl";
        
        
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


    static double getAttributeGridVolume(AttributeGrid grid, int maxAttribute){
        
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        double cnt = 0;
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    cnt += grid.getAttribute(x,y,z);
                }
            }
        }
        return cnt / maxAttribute;
    }

    static double getGridVolume(Grid grid){
        
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        double cnt = 0;
        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    byte s  = grid.getState(x,y,z);
                    if(s == Grid.INTERIOR)
                        cnt++;
                }
            }
        }
        return cnt;
    }

    public static void main(String[] args) throws Exception{

        new TestWaveletRasterizer().testBox();
    }
}
