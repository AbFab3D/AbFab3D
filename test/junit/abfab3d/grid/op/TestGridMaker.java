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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.vecmath.Vector3d;

import org.j3d.geom.GeometryData;
import org.j3d.geom.TorusGenerator;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;

// external imports 
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
import abfab3d.grid.Grid;
import abfab3d.grid.ArrayAttributeGridByte;

import abfab3d.util.Vec;
import abfab3d.util.VecTransform;
import abfab3d.util.MathUtil;

import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.STLWriter;


import static abfab3d.util.Output.printf;
import static abfab3d.util.MathUtil.TORAD;

import static java.lang.Math.sin;
import static java.lang.Math.cos;

/**
 * Tests the functionality of GridMaker
 *
 * @version
 */
public class TestGridMaker extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestGridMaker.class);
    }
   
    /**

     */
    public void _testImageRing1() {
        
        printf("testImageRing1()\n");
        double voxelSize = 2.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 4*voxelSize;

        double ringDiameter = 0.05; // 5cm 
        double ringWidth = 0.01; // 1cm 
        double ringThickness = 0.002; // 2mm 

        double gridWidth = ringDiameter + 2*Math.hypot(ringThickness,ringWidth) + 2*margin;
        double gridHeight  = Math.hypot(ringThickness,ringWidth) + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.m_sizeX = ringDiameter*Math.PI;
        image.m_sizeY = ringWidth;
        image.m_sizeZ = ringThickness;
        image.m_centerZ = ringThickness/2;
        image.m_baseThickness = 0.4;
        image.m_imagePath = "docs/images/numbers_1.png";
        
        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.m_radius = ringDiameter/2;

        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.m_axis = new Vector3d(1,0,0);
        rot.m_angle = 90*TORAD;
        
        compTrans.add(rot);
        compTrans.add(rw);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(image);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/ring_numbers.stl");
        printf("writeIsosurface() done\n");
        
    }

    /**

     */
    public void _testImageRingTiled() {
        
        printf("testImageTiled()\n");
        double voxelSize = 1.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 4*voxelSize;

        double ringDiameter = 0.05; // 5cm 
        double ringWidth = 0.01; // 1cm 
        double ringThickness = 0.002; // 2mm 

        double gridWidth = ringDiameter + 2*Math.hypot(ringThickness,ringWidth) + 2*margin;
        double gridHeight  = Math.hypot(ringThickness,ringWidth) + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.m_sizeX = ringDiameter*Math.PI;
        image.m_sizeY = ringWidth;
        image.m_sizeZ = ringThickness;
        image.m_centerZ = ringThickness/2;
        image.m_baseThickness = 0.5;
        image.m_xTilesCount = 12;
        image.m_yTilesCount = 1;
        image.m_imagePath = "docs/images/Tile_DecorativeCeiling_2.png";
        
        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.m_radius = ringDiameter/2;

        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.m_axis = new Vector3d(1,0,0);
        rot.m_angle = 0*TORAD;
        
        compTrans.add(rot);
        compTrans.add(rw);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(image);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/ring_tiled.stl");
        printf("writeIsosurface() done\n");
        
    }

    public void testRingWithBand() {
        
        printf("testImageTiled()\n");
        double voxelSize = 1.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double bandWidth = 0.002;// 1mm 
        double bandThickness = 0.004;// 4mm 
        
        double margin = 4*voxelSize;

        double ringDiameter = 0.05; // 5cm 
        double ringWidth = 0.01; // 1cm 
        double ringThickness = 0.002; // 2mm 

        double gridWidth = ringDiameter + 2*bandThickness + 2*margin;
        double gridHeight  = ringWidth + 2*bandWidth + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.setSize(ringDiameter*Math.PI,ringWidth,ringThickness);
        image.setLocation(0,0,ringThickness/2);
        image.setBaseThickness(0.5);
        image.setTiles(12, 1);
        image.setImagePath("docs/images/Tile_DecorativeCeiling_1k_h.png");

        DataSources.Block topBand = new DataSources.Block();
        topBand.setSize(ringDiameter*Math.PI,bandWidth, bandThickness);
        topBand.setLocation(0, ringWidth/2, bandThickness/2);

        //DataSources.Block bottomBand = new DataSources.Block();
        DataSources.ImageBitmap bottomBand = new DataSources.ImageBitmap();
        bottomBand.setSize(ringDiameter*Math.PI,bandWidth, bandThickness);
        bottomBand.setLocation(0, -ringWidth/2, bandThickness/2);
        bottomBand.setTiles((int)(ringDiameter*Math.PI/bandWidth), 1);
        bottomBand.setImagePath("docs/images/circle.png");

        DataSources.Maximum union = new DataSources.Maximum();
        
        union.addDataSource(image);
        union.addDataSource(topBand);
        union.addDataSource(bottomBand);

        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.m_radius = ringDiameter/2;

        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.m_axis = new Vector3d(1,0,0);
        rot.m_angle = 0*TORAD;
        
        compTrans.add(rot);
        compTrans.add(rw);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(union);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/ring_with_band.stl");
        printf("writeIsosurface() done\n");
        
    }

    /**
       ring with fancy profile
     */
    public void _testImageRing2() {
        
        printf("testImageRingTiled()\n");
        double voxelSize = 2.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 4*voxelSize;

        double ringDiameter = 0.05; // 5cm 
        double ringWidth = 0.01; // 1cm 
        double ringThickness = 0.01; // 1cm

        double gridWidth = ringDiameter + 2*Math.hypot(ringThickness,ringWidth) + 2*margin;
        double gridHeight  = Math.hypot(ringThickness,ringWidth) + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.m_sizeX = ringWidth;
        image.m_sizeY = ringWidth;
        image.m_sizeZ = ringDiameter*Math.PI;
        image.m_centerX = 0;
        image.m_centerY = 0;
        image.m_centerZ = 0;
        image.m_baseThickness = 0.;
        image.m_xTilesCount = 1;
        image.m_imagePath = "docs/images/star_4_arms_1.png";
        
        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.m_axis = new Vector3d(0,1,0);
        rot.m_angle = 90*TORAD;

        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.m_radius = ringDiameter/2;
        
        compTrans.add(rot);
        compTrans.add(rw);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(image);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/ring_star_profile.stl");
        printf("writeIsosurface() done\n");
        
    }

    /**
       spherical inversion
     */
    public void _testSphericalInversion() {
        
        printf("testSphericalInversion()\n");
        double voxelSize = 2.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 4*voxelSize;

        double sphereDiameter = 0.05; // 5cm 
        double sphereThickness = 0.002; // 2cm

        double gridWidth = sphereDiameter + 2*sphereThickness + 2*margin;
        double gridHeight  = gridWidth;
        double gridDepth = sphereDiameter/2 + sphereThickness + 2*margin;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,0,gridDepth};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        

        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.m_sizeX = sphereDiameter;
        image.m_sizeY = sphereDiameter;
        image.m_sizeZ = sphereThickness;

        image.m_centerX = 0;
        image.m_centerY = 0;
        image.m_centerZ = 0;
        image.m_baseThickness = 0.;
        //image.m_imagePath = "docs/images/star_4_arms_1.png";
        image.m_imagePath = "docs/images/spiral.png";
        
        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.PlaneReflection pr = new VecTransforms.PlaneReflection();
        pr.m_pointOnPlane = new Vector3d(0,0,0);
        pr.m_planeNormal = new Vector3d(0,0,1);

        VecTransforms.SphereInversion si = new VecTransforms.SphereInversion();
        si.m_radius = (sphereDiameter/2)*(Math.sqrt(3));
        si.m_center = new Vector3d(0,0,-sphereDiameter/2*Math.sqrt(2));
                
        compTrans.add(pr);
        compTrans.add(si);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(image);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/sphere_star.stl");
        printf("writeIsosurface() done\n");
        
    }

    /**

     */
    public void _testPlainRing() {
        
        printf("testBasic()\n");
        double voxelSize = 2.e-4;
        int smoothSteps = 0;
        double margin = 2*voxelSize;

        double ringDiameter = 0.05; // 5cm 
        double ringWidth = 0.01; // 1cm 
        double ringThickness = 0.001; // 1mm 

        double gridWidth = ringDiameter + 2*ringThickness + 2*margin;
        double gridHeight  = ringWidth + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2,-gridHeight/2,gridHeight/2,-gridDepth/2,gridDepth/2};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.Block box = new DataSources.Block();
        box.m_sizeX = ringDiameter*Math.PI;
        box.m_sizeY = ringWidth;
        box.m_sizeZ = ringThickness;
        box.m_centerZ = ringThickness/2;
        
        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.m_radius = ringDiameter/2;
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(rw);
        gm.setDataSource(box);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/ring_plain.stl");
        printf("writeIsosurface() done\n");
        
    }

        
    public void _testTransform() {

        double r = 0.1;
        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.m_radius = r;

        Vec ringPnt = new Vec(3);
        Vec bandPnt = new Vec(3);
        int N = 10;

        for(int i = 0; i < N; i++){
            double phi = 2*Math.PI*i/N - Math.PI;
            double z = (r + 0.01)* cos(phi);
            double x = (r + 0.01)* sin(phi);
            double y = 0.1;

            ringPnt.set(x, y, z);
            rw.inverse_transform(ringPnt, bandPnt);
            printf("xyz: (%7.5f,%7.5f,%7.5f) -> (%7.5f,%7.5f,%7.5f)\n", x, y, z, bandPnt.v[0],bandPnt.v[1],bandPnt.v[2]);
            
        }

        
    }


    /**
       
     */
    void writeIsosurface(Grid grid, double bounds[], double voxelSize, int smoothSteps, String fpath){

        IsosurfaceMaker im = new IsosurfaceMaker();

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();

        im.setIsovalue(0.);
        im.setBounds(MathUtil.extendBounds(bounds, -voxelSize/2));
        im.setGridSize(nx, ny, nz);

        IsosurfaceMaker.SliceGrid fdata = new IsosurfaceMaker.SliceGrid(grid, bounds, smoothSteps);
        
        try {
            STLWriter stlwriter = new STLWriter(fpath);
            im.makeIsosurface(fdata, stlwriter);
            stlwriter.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }
}
