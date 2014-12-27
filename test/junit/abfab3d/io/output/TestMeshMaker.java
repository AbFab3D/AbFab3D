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


import java.io.File;
import javax.vecmath.Vector3d;


// external imports
import abfab3d.grid.*;
import abfab3d.util.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports

import abfab3d.grid.op.GridMaker;

import abfab3d.datasources.TransformableDataSource;
import abfab3d.datasources.DataChannelMixer;
import abfab3d.datasources.Box;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Plane;
import abfab3d.datasources.Ring;
import abfab3d.datasources.ImageBitmap;
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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.CM;
import static abfab3d.util.Units.MM;

import static abfab3d.util.MathUtil.step10;


import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.PI;


/**
 * Tests the functionality of MeshMaker
 *
 * @version
 */
public class TestMeshMaker extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestMeshMaker.class);
    }

    /**
       to make test happy 
     */
    public void testNothing(){
        
    }

    public static void makeColorSphere() throws Exception {
        
        printf("makeColorSphere()\n");    

        double vs = 0.4*MM;
        double margin = vs;
        int subvoxelResolution = 255;
        int threadCount = 4;

        double s = 20*MM;

        double bounds[] = new double[]{-s, s, -s, s, -s, s};
        
        MathUtil.roundBounds(bounds, vs);
        bounds = MathUtil.extendBounds(bounds, margin);                

        double surfareThickness = sqrt(3)/2;

        int nx[] = MathUtil.getGridSize(bounds, vs);

        printf("grid: [%d x %d x %d]\n", nx[0],nx[1],nx[2]);

        //Union union = new Union();
        Intersection density = new Intersection();

        density.add(new Sphere(new Vector3d(0,0,0), s));
        density.add(new Sphere(new Vector3d(0,0,0), -(s-1*MM)));
        density.add(new Plane(new Vector3d(1,0,0), 0));

        
        //Intersection color1 = new Intersection();
        //color1.add(new Plane(new Vector3d(0,1,0), 0.5*MM));
        //color1.add(new Plane(new Vector3d(0,-1,0), 0.5*MM));

        DataSource color1 = new HalfGyroid(0.3*s);

        DataChannelMixer mux = new DataChannelMixer(density, color1);

        AttributeMaker attdens = new AttributeMakerDensity(subvoxelResolution);
        AttributeMaker attmuxer = new AttributeMakerGeneral(new int[]{8,8});
        
        GridMaker gm = new GridMaker();  

        gm.setSource(mux);
        gm.setThreadCount(threadCount);

        gm.setAttributeMaker(attmuxer);
        
        AttributeGrid grid = new ArrayAttributeGridInt(nx[0], nx[1], nx[2], vs, vs);
        grid.setGridBounds(bounds);

        long t0 = time();
        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done in %d ms\n", (time() - t0));
        
        DensityMaker dm1 = new DensityMakerSubvoxel(subvoxelResolution);

        MeshMakerMT meshmaker = new MeshMakerMT();
       
        meshmaker.setThreadCount(threadCount);
        meshmaker.setSmoothingWidth(0.5);
        meshmaker.setMaxDecimationError(0.1*vs*vs);
        meshmaker.setDensityMaker(dm1);

        meshmaker.setDensityMaker(new ChannelDensityMaker(0));

        new File("/tmp/multimat/").mkdirs();

        STLWriter stl = new STLWriter("/tmp/multimat/colorSphere_0.stl");
        meshmaker.makeMesh(grid, stl);
        stl.close();

        meshmaker.setDensityMaker(new ChannelDensityMaker(1));
        stl = new STLWriter("/tmp/multimat/colorSphere_1.stl");
        meshmaker.makeMesh(grid, stl);
        stl.close();



        
    }

    public void testMeshOutput() throws Exception {

        printf("testMeshOutput()\n");    
        double vs = 0.4*MM;
        double margin = vs;
        int subvoxelResolution = 255;
        int threadCount = 4;

        double s = 20*MM;

        double bounds[] = new double[]{-s, s, -s, s, -s, s};

        MathUtil.roundBounds(bounds, vs);
        bounds = MathUtil.extendBounds(bounds, margin);

        double surfareThickness = sqrt(3)/2;

        int nx[] = MathUtil.getGridSize(bounds, vs);

        printf("grid: [%d x %d x %d]\n", nx[0],nx[1],nx[2]);

        //Union union = new Union();
        Intersection density = new Intersection();

        density.add(new Sphere(new Vector3d(0,0,0), s));
        density.add(new Sphere(new Vector3d(0,0,0), -(s-1*MM)));
        density.add(new Plane(new Vector3d(1,0,0), 0));


        //Intersection color1 = new Intersection();
        //color1.add(new Plane(new Vector3d(0,1,0), 0.5*MM));
        //color1.add(new Plane(new Vector3d(0,-1,0), 0.5*MM));

        DataSource color1 = new HalfGyroid(0.3*s);

        DataChannelMixer mux = new DataChannelMixer(density, color1);

        AttributeMaker attdens = new AttributeMakerDensity(subvoxelResolution);
        AttributeMaker attmuxer = new AttributeMakerGeneral(new int[]{8,8});

        GridMaker gm = new GridMaker();

        gm.setSource(mux);
        gm.setThreadCount(threadCount);

        gm.setAttributeMaker(attmuxer);

        AttributeGrid grid = new ArrayAttributeGridInt(nx[0], nx[1], nx[2], vs, vs);
        grid.setGridBounds(bounds);

        long t0 = time();
        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);
        printf("gm.makeGrid() done in %d ms\n", (time() - t0));

        MultiMaterialModelWriter writer = new MultiMaterialModelWriter();
        new File("/tmp/").mkdirs();
        FileOutputStream fos = new FileOutputStream("/tmp/foo.sts");
        //ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ZipOutputStream zos = new ZipOutputStream(bos);
        writer.setOutputStream(zos);
        writer.setOutputFormat("x3db");
        writer.setMaterialMakers(new MaterialMaker[]{new MaterialMaker(new ChannelDensityMaker(0), MaterialURN.SHAPEWAYS_STRONG_AND_FLEXIBLE_PLASTIC),
                new MaterialMaker(new ChannelDensityMaker(1),MaterialURN.SHAPEWAYS_SILVER)});

        writer.execute(grid);
        zos.close();
        fos.close();
    }

    public void testSTSOutput() throws Exception {

        printf("testSTSOutput()\n");    
        double vs = 0.4*MM;
        double margin = vs;
        int subvoxelResolution = 255;
        int threadCount = 8;

        double s = 20*MM;

        double bounds[] = new double[]{-s, s, -s, s, -s, s};

        MathUtil.roundBounds(bounds, vs);
        bounds = MathUtil.extendBounds(bounds, margin);

        int nx[] = MathUtil.getGridSize(bounds, vs);

        printf("grid: [%d x %d x %d]\n", nx[0],nx[1],nx[2]);

        //Union union = new Union();
        Intersection density = new Intersection();

        density.add(new Sphere(new Vector3d(0,0,0), s));
        density.add(new Sphere(new Vector3d(0,0,0), -(s-1*MM)));
        density.add(new Plane(new Vector3d(1,0,0), 0));


        //Intersection color1 = new Intersection();
        //color1.add(new Plane(new Vector3d(0,1,0), 0.5*MM));
        //color1.add(new Plane(new Vector3d(0,-1,0), 0.5*MM));

        DataSource color1 = new HalfGyroid(0.3*s);

        DataChannelMixer mux = new DataChannelMixer(density, color1);

        AttributeMaker attmuxer = new AttributeMakerGeneral(new int[]{8,8});

        GridMaker gm = new GridMaker();

        gm.setSource(mux);
        gm.setThreadCount(threadCount);

        gm.setAttributeMaker(attmuxer);

        AttributeGrid grid = new ArrayAttributeGridInt(nx[0], nx[1], nx[2], vs, vs);
        grid.setGridBounds(bounds);

        long t0 = time();
        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);
        printf("gm.makeGrid() done in %d ms\n", (time() - t0));

        MaterialMaker[] makers = new MaterialMaker[]{new MaterialMaker(new ChannelDensityMaker(0), MaterialURN.SHAPEWAYS_STRONG_AND_FLEXIBLE_PLASTIC),
                new MaterialMaker(new ChannelDensityMaker(1),MaterialURN.SHAPEWAYS_SILVER)};

        new File("/tmp/sts/").mkdirs();
        STSWriter writer = new STSWriter();
        writer.write(grid,makers,new String[] {FinishURN.SHAPEWAYS_POLISHED_HAND},"/tmp/sts/foo2.sts");
    }

    static class ChannelDensityMaker implements DensityMaker {

        int mat;

        ChannelDensityMaker(int mat){

            this.mat = mat;

        }
        
        public double makeDensity(long attribute){

            double dens = (attribute & 0xFF)/255.;
            double mdens = ((attribute >> 8) & 0xFF) / 255.;
            switch(mat){
            case 0:
                break;
            case 1: 
                mdens = 1-mdens;
            }

            if(mdens < dens) 
                return mdens;
            else 
                return dens;
        }
        
    }


    static class HalfGyroid  extends TransformableDataSource{
        
        private double period = 10*MM;
        private double level = 0;
        private double offsetX = 0,offsetY = 0,offsetZ = 0;
        private double factor = 0;

        public HalfGyroid(double period){
            this.period = period;
        }
        
        public void setPeriod(double value){
            this.period = value; 
        }

        public void setLevel(double value){
            this.level = value;
        }

        public void setOffset(double offsetX, double offsetY,double offsetZ){
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }
        
        public int initialize(){
            super.initialize();
            this.factor = 2*PI/period;

            return RESULT_OK;
        }

        public int getDataValue(Vec pnt, Vec data){
            
            super.transform(pnt);
            double x = pnt.v[0]-offsetX;
            double y = pnt.v[1]-offsetY;
            double z = pnt.v[2]-offsetZ;
            
            x *= factor;
            y *= factor;
            z *= factor;

            double vs = pnt.getScaledVoxelSize();
            
            double d = ( sin(x)*cos(y) + sin(y)*cos(z) + sin(z) * cos(x) - level)/factor;
            
            data.v[0] = step10(d, 0, vs);

            return RESULT_OK;
        }
        
    }

    public static void main(String[] args) throws Exception {

        new TestMeshMaker().makeColorSphere();

    }
}