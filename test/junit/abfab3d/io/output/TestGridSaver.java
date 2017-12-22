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

package abfab3d.io.output;

// External Imports


import javax.vecmath.Vector3d;


import java.io.File;
import java.io.IOException;


// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
import abfab3d.core.AttributePacker;
import abfab3d.core.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.GridIntIntervals;
import abfab3d.grid.GridShortIntervals;
import abfab3d.core.GridDataDesc;
import abfab3d.core.GridDataChannel;

import abfab3d.core.Bounds;
import abfab3d.core.Initializable;
import abfab3d.core.Vec;


import abfab3d.distance.DistanceDataSphere;
import abfab3d.distance.DistanceDataUnion;
import abfab3d.distance.DataSourceFromDistance;

import abfab3d.core.DataSource;
import abfab3d.datasources.DataSourceMixer;
import abfab3d.datasources.Intersection;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Plane;
import abfab3d.datasources.Torus;
import abfab3d.datasources.VolumePatterns;
import abfab3d.datasources.Complement;
import abfab3d.datasources.Constant;
import abfab3d.datasources.Union;

import abfab3d.transforms.PeriodicWrap;

import abfab3d.grid.op.GridMaker;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;

/**
 * Tests the functionality of GridSaver
 *
 * @version
 */
public class TestGridSaver extends TestCase {

    static final double CM = 0.01; // cm -> meters
    static final double MM = 0.001; // mm -> meters

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestGridSaver.class);
    }

    public void testNothing(){
        //this test here is to make Test happy. 
    }

    void devTestDistanceGrid() throws IOException{

        printf("devTestDistanceGrid()\n");  
        double voxelSize = 0.2*MM;
        double margin = 2*MM;
        double sizex = 10*MM; 
        double sizey = 10*MM; 
        double sizez = 10*MM;
        double ballRadius = 10.0*MM;
        
        double w = 30*MM;
        
        int threadsCount = 1;
        Bounds bounds = new Bounds(-w/2,w/2,-w/2,w/2,-w/2,w/2);

        DistanceDataSphere s0 = new DistanceDataSphere(7*MM, new Vector3d(0,0,0));
        DistanceDataSphere s1 = new DistanceDataSphere(7*MM, new Vector3d(5*MM,0,0));
        DistanceDataSphere s2 = new DistanceDataSphere(7*MM, new Vector3d(-5*MM,0,0));
        DistanceDataUnion s12 = new DistanceDataUnion(s1, s2);
        double blend = 1*MM;
        s12.setBlendWidth(blend);
        GridMaker gm = new GridMaker();

 
        gm.setSource(new DataSourceFromDistance(s0));  
        //gm.setSource(new DataSourceFromDistance(s12));  
        AttributeGrid grid = new ArrayAttributeGridByte(bounds, voxelSize, voxelSize);
        GridDataChannel distChannel = new GridDataChannel(GridDataChannel.DISTANCE, "dist", 8, 0, 2*voxelSize,-2*voxelSize);
        grid.setDataDesc(new GridDataDesc(distChannel));
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        GridSaver gridSaver = new GridSaver();
        double ds = 0.99*voxelSize;
        for(int i = 0; i < 5; i++){
            double surface = (i-2)*ds;
            gridSaver.setSurfaceLevel(surface);
            gridSaver.write(grid,fmt("/tmp/grid_distance_%d.stl",i));
            printf("writing mesh done\n");
        }
    }


    /**
       testing grid wityh density and BGR channels
     */
    void devGridDensBGR() throws IOException{

        printf("devGridDensBGR()\n");  
        int voxelCount = 100;
        double radius = 30.0*MM;
        double w = 31*MM;
        double period = 10*MM;
        double voxelSize = 2*w/voxelCount;
                
        int threadsCount = 1;

        Bounds bounds = new Bounds(-w,w,-w,w,-w,w, voxelSize);

        GridMaker gm = new GridMaker();
        DataSource ds = makeStripedSphere(radius, period);

        //printRay(ds, new Vector3d(-w, 0,0),new Vector3d(w, 0,0),voxelCount, voxelSize, GridDataDesc.getDensBGR().getAttributeMaker());
        

        gm.setSource(ds); 

        AttributeGrid grid = createDensBGRGrid(bounds);
        //AttributeGrid grid = createDensityGrid(bounds);
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");


        GridSaver gridSaver = new GridSaver();
        //gridSaver.write(grid,"/tmp/color/grid_DensBGR.stl");
        gridSaver.write(grid,"/tmp/tex/grid_DensBGR.x3dv");
        
        grid.setDataDesc(GridDataDesc.getDensBGRcomposite());
        gridSaver.write(grid,"/tmp/tex/grid_DensBGR.svx");

        printf("writing done\n");    
        
    }
    
    /**
       tesing making mesh with texture 
     */
    void devTestTexturedMesh() throws IOException{

        printf("devTestTexturedMesh()\n");  
        int voxelCount = 400;
        double radius = 30.0*MM;
        double w = 31*MM;
        double period = 10*MM;
        double voxelSize = 2*w/voxelCount;
                
        int threadsCount = 1;

        Bounds bounds = new Bounds(-w,w,-w,w,-w,w, voxelSize);

        GridMaker gm = new GridMaker();
        //DataSource ds = makeStripedSphere(radius, period);
        DataSource ds = makeStripedGyroid(radius, period);

        gm.setSource(ds); 

        AttributeGrid grid = createDensBGRGrid(bounds);
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");

        GridSaver gsaver = new GridSaver();        

        new File("/tmp/tex").mkdirs();
        gsaver.setWriteTexturedMesh(true);
        gsaver.setTexPixelSize(1);
        //gsaver.setTexturePixelSize(0.5);
        gsaver.write(grid,"/tmp/tex/texturedGyroid_400.x3dv");  
        //gsaver.write(grid,"/tmp/tex/texturedMesh.x3db");  
        //gsaver.write(grid,"/tmp/tex/texturedMesh.x3d");  
        printf("writing done\n");    
        
    }

    /**
       make models for test print
     */
    void devTestPrintModels() throws IOException{

        printf("devTestPrintModels()\n");  
        double voxelSize = 0.2*MM;
        //double rin = 5.0*MM; double rout = 10.0*MM; // makes model 50mm tall 
        double rin = 10*MM; double rout = 20.0*MM; // makes model 100mm tall 
        double period = 5*MM; // period of stripes 
        double lineWidth = 1*MM; // width of stripes 
        double height = 2*rin + 4*rout;

        int dotCount = 0; double texPixelSize = 0.2;
        //int dotCount = 1; double texPixelSize = 0.5;
        //int dotCount = 2; double texPixelSize = 1.;
        //int dotCount = 3; double texPixelSize = 2.;
        //int dotCount = 4; double texPixelSize = 3.;
        
        double triGap = 3;
        double triExt = 1.5;
        String baseDir = "/tmp/tex/print/";
        String fname = fmt("printModel_%03d_mm_ps%3.1f.x3dv", (int)(height/MM), texPixelSize);

        double wx = (rin + rout)+1*MM;
        double wy = height/2+1*MM;


        //double voxelSize = 2*wy/voxelCount;
                
        int threadsCount = 1;

        Bounds bounds = new Bounds(-wx,wx,-wy,wy,-wx,wx, voxelSize);

        GridMaker gm = new GridMaker();
        DataSource ds = makeDoubleTorus(rin, rout, period, lineWidth, dotCount);

        gm.setSource(ds); 

        AttributeGrid grid = createDensBGRGrid(bounds);
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");

        GridSaver gsaver = new GridSaver();         
        gsaver.setWriteTexturedMesh(true);
        gsaver.setTexTriGap(triGap);
        gsaver.setTexTriExt(triExt);
        gsaver.setTexPixelSize(texPixelSize);
        String path = baseDir+fname;
        gsaver.write(grid,path);  

        printf("writing done\n");    
        
    }

    static void printRay(DataSource source, Vector3d v0, Vector3d v1, int count, double vs, AttributePacker attMaker){
        if(source instanceof Initializable) ((Initializable)source).initialize();
        Vector3d v = new Vector3d();
        double delta = 1./count;
        Vec in = new Vec(3);
        Vec out = new Vec(4);
        in.setVoxelSize(vs);
        for(int i = 0; i <= count; i++){
            v.interpolate(v0, v1, i*delta);
            in.set(v);
            source.getDataValue(in, out);            
            long att = attMaker.makeAttribute(out);
            printf("(%8.5f,%8.5f,%8.5f)->(%8.5f,%8.5f,%8.5f,%8.5f)%8x\n",v.x, v.y, v.z,out.v[0],out.v[1],out.v[2],out.v[3], att);
            
        }
    }

    static DataSource makeStripedSphere(double radius, double period){
        
        Sphere sphere = new Sphere(new Vector3d(0,0,0),radius);

        Plane p1 = new Plane(new Vector3d(1,0,0),3*period/4);
        Plane p2 = new Plane(new Vector3d(-1,0,0),-period/4);
        Intersection pattern = new Intersection(p1, p2);
        pattern.setTransform(new PeriodicWrap(new Vector3d(period,0,0)));
        //pattern.addTransform(new Rotation(0,1,0,Math.PI/2));
        //Mask redColor = new Mask(pattern, 0., 0.1*MM);
        //Mask greenColor = new Mask(new Mul(pattern,new Constant(-1)), 0., 0.1*MM);	
        DataSource redColor = pattern;
        DataSource greenColor = new Complement(pattern);
        DataSource blueColor = new Sphere(radius, 0,0,radius*Math.sqrt(2));
        DataSourceMixer colorizer = new DataSourceMixer(redColor, greenColor, blueColor);
	
        //DataSourceMixer csphere = new DataSourceMixer(sphere, colorizer);
        DataSourceMixer csphere = new DataSourceMixer(sphere, colorizer);
        return csphere;

    }

    static DataSource makeDoubleTorus(double rin, double rout, double period, double lineWidth, int dotCount){

        Torus t1 = new Torus(new Vector3d(0, rout,0),new Vector3d(0,0,1),rout, rin);
        Torus t2 = new Torus(new Vector3d(0,-rout,0),new Vector3d(1,0,0),rout, rin);        
        Union u12 = new Union(t1, t2);
        //Union u12 = new Union();
        for(int i = 0; i < dotCount; i++){
            double rs = rin*0.3;
            double Rs = rout - rin;
            double a= i*Math.PI/2;
            double sx = Rs*Math.cos(a);
            double sy = rout + Rs*Math.sin(a);
            u12.add(new Sphere(sx, sy, 0, rs));
        }
        u12.initialize();
        Plane p1 = new Plane(new Vector3d(1,0,0),(period + lineWidth)/2 );
        Plane p2 = new Plane(new Vector3d(-1,0,0),-(period - lineWidth)/2);
        Intersection p12 = new Intersection(p1, p2);
        p12.setTransform(new PeriodicWrap(new Vector3d(period,0,0)));

        Plane p3 = new Plane(new Vector3d(0,1,0),(period + lineWidth)/2 );
        Plane p4 = new Plane(new Vector3d(0,-1,0),-(period - lineWidth)/2);
        Intersection p34 = new Intersection(p3, p4);
        p34.setTransform(new PeriodicWrap(new Vector3d(0,period,0)));

        DataSource blue = new Constant(1);
        //DataSource background = new Constant(1,1,1);
        
        DataSourceMixer colorizer = new DataSourceMixer(p12, p34, blue);
        
        
        DataSourceMixer cu12 = new DataSourceMixer(u12, colorizer);
        return cu12;
        
    }

    static DataSource makeStripedGyroid(double radius, double period){
        
        Sphere sphere = new Sphere(new Vector3d(0,0,0),radius);

        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid();
        gyroid.set("period", 30*MM);
        gyroid.set("thickness", 2*MM);
        gyroid.set("center", new Vector3d(0*MM,0*MM,0*MM));
        gyroid.set("level", 0);

        Intersection gyrosphere = new Intersection(sphere, gyroid);

        Plane p1 = new Plane(new Vector3d(1,0,0),3*period/4);
        Plane p2 = new Plane(new Vector3d(-1,0,0),-period/4);
        Intersection pattern = new Intersection(p1, p2);
        pattern.setTransform(new PeriodicWrap(new Vector3d(period,0,0)));
        //pattern.addTransform(new Rotation(0,1,0,Math.PI/2));
        //Mask redColor = new Mask(pattern, 0., 0.1*MM);
        //Mask greenColor = new Mask(new Mul(pattern,new Constant(-1)), 0., 0.1*MM);	
        DataSource redColor = pattern;
        DataSource greenColor = new Complement(pattern);
        DataSource blueColor = new Sphere(radius, 0,0,radius*Math.sqrt(2));
        DataSourceMixer colorizer = new DataSourceMixer(redColor, greenColor, blueColor);
	
        DataSourceMixer csphere = new DataSourceMixer(gyrosphere, colorizer);
        return csphere;

    }
    
    static AttributeGrid createDensBGRGrid(Bounds bounds) {

        double vs = bounds.getVoxelSize();
        long nx = bounds.getWidthVoxels(vs);
        long ny = bounds.getHeightVoxels(vs);
        long nz = bounds.getDepthVoxels(vs);
        AttributeGrid grid;

        if (nx * ny * nz * 4 < Integer.MAX_VALUE)
            grid = new ArrayAttributeGridInt(bounds, vs, vs);
        else
            grid = new GridIntIntervals(bounds, vs, vs);

        grid.setDataDesc(GridDataDesc.getDensBGR());

        return grid;
    }

    AttributeGrid createDensityGrid(Bounds bounds) {

        double vs = bounds.getVoxelSize();
        long nx = bounds.getWidthVoxels(vs);
        long ny = bounds.getHeightVoxels(vs);
        long nz = bounds.getDepthVoxels(vs);

        if (nx * ny * nz < Integer.MAX_VALUE)
            return new ArrayAttributeGridByte(bounds, vs, vs);
        else
            return new GridShortIntervals(bounds, vs, vs);
    }


   
    public static void main(String[] args) throws IOException {

        new TestGridSaver().devTestPrintModels();
        //new TestGridSaver().devTestTexturedMesh();
        //new TestGridSaver().devTestDistanceGrid();
        //new TestGridSaver().devGridDensBGR();
    }
    
}
