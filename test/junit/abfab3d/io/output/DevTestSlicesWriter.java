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


// external imports
import junit.framework.TestCase;


// Internal Imports
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.util.ReflectionGroup;

import abfab3d.core.MathUtil;

import abfab3d.grid.op.GridMaker;

import abfab3d.datasources.Box;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.DataTransformer;
import abfab3d.datasources.Intersection;
import abfab3d.datasources.Union;
import abfab3d.datasources.Triangle;
import abfab3d.datasources.Cylinder;
import abfab3d.datasources.LimitSet;
import abfab3d.datasources.VolumePatterns;

import abfab3d.transforms.Rotation;
import abfab3d.transforms.Translation;


import abfab3d.transforms.*;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;


import static java.lang.Math.cos;
import static java.lang.Math.sqrt;


/**
 * Tests the functionality of SlicesWriter
 *
 * @version
 */
public class DevTestSlicesWriter extends TestCase {

    public static void gyroCube() throws Exception {

        printf("gyroCube()\n");

        double voxelSize = 0.1*MM;
        double margin = 0*voxelSize;

        double s = 10*MM;

        double 
            xmin = 0.*s,
            xmax = 3.*s,
            ymin = -0.*s,
            ymax = 2*s,
            zmin = -0.*s,
            zmax = 2*s;


        double bounds[] = new double[]{xmin, xmax, ymin, ymax, zmin, zmax};
        
        MathUtil.roundBounds(bounds, voxelSize);
        bounds = MathUtil.extendBounds(bounds, margin);
                

        int maxAttributeValue = 63;
        int blockSize = 50;
        double errorFactor = 0.05;
        double smoothWidth = 1;
        int maxDecimationCount= 10;
        int threadsCount = 1;
        double surfareThickness = sqrt(3)/2;//0.5;

        
        double maxDecimationError = errorFactor*voxelSize*voxelSize;

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);
        
        Box block = new Box(0,0,0, 2*s, 2*s, 2*s);
        
        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid();  
        gyroid.setPeriod(s);
        gyroid.setThickness(0.02*s);
        //gyroid.setLevel(0);
        gyroid.setLevel(1.5);
        gyroid.setCenter(new Vector3d(-0.25*s,-0.25*s,0.*s));
       
        //VecTransforms.Rotation rotation = new VecTransforms.Rotation(new Vector3d(1,1,0), Math.PI/10);
        
        Intersection intersection = new Intersection();

        intersection.add(block);
        intersection.add(gyroid);

        GridMaker gm = new GridMaker();  
        gm.setBounds(bounds);
        //gm.setSource(triangle);
        gm.setSource(gyroid);

        gm.setThreadCount(threadsCount);
        gm.setMaxAttributeValue(maxAttributeValue);
        gm.setVoxelSize(voxelSize*surfareThickness);
        
        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        grid.setGridBounds(bounds);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);        
        printf("gm.makeGrid() done\n");
        
        if(false){
            //printf("%s",grid.toStringAttributesSectionZ(nz / 2));
            SlicesWriter slicer = new SlicesWriter();
            slicer.setFilePattern("/tmp/slices/slice_%03d.png");
            slicer.setCellSize(5);
            slicer.setVoxelSize(4);
        
            slicer.setMaxAttributeValue(maxAttributeValue);
            //slicer.writeSlices(grid);
        }
        
        if(true){ 

            MeshMakerMT meshmaker = new MeshMakerMT();
            meshmaker.setBlockSize(blockSize);
            meshmaker.setThreadCount(threadsCount);
            meshmaker.setSmoothingWidth(smoothWidth);
            meshmaker.setMaxDecimationError(maxDecimationError);
            meshmaker.setMaxDecimationCount(maxDecimationCount);
            meshmaker.setMaxAttributeValue(maxAttributeValue);            
            
            STLWriter stl = new STLWriter("/tmp/gyro_cube.stl");
            meshmaker.makeMesh(grid, stl);
            stl.close();
        }

    }
 
    public static void triangularShape() throws Exception {
        
        printf("triangularShape()\n");
    
        double voxelSize = 0.1*MM;
        double margin = 1*voxelSize;

        double s = 30*MM;
        double thickness = 4.*MM;

        double xmin = -thickness;
        double xmax = s + thickness;


        double bounds[] = new double[]{xmin, xmax, xmin, xmax, xmin, xmax};
        
        MathUtil.roundBounds(bounds, voxelSize);
        bounds = MathUtil.extendBounds(bounds, margin);
                

        int maxAttributeValue = 63;
        int blockSize = 50;
        double errorFactor = 0.05;
        double smoothWidth = 1;
        int maxDecimationCount= 10;
        int threadsCount = 4;
        double surfareThickness = sqrt(3)/2;//0.5;

        double maxDecimationError = errorFactor*voxelSize*voxelSize;

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);
        Vector3d 
            v0 = new Vector3d(s,0,0),
            v1 = new Vector3d(0,s,0),
            v2 = new Vector3d(0,0,s);
        
        //Triangle triangle = new Triangle(v0, v1, v2, thickness);
        Triangle triangle = new Triangle(v0, v1, v2, thickness);
        
        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(12*MM, 0.8*MM);

        Rotation rotation = new Rotation(new Vector3d(1,1,0), Math.PI/10);
        GridMaker gm = new GridMaker();  
        gm.setBounds(bounds);
        
        Intersection intersection = new Intersection();
        intersection.add(triangle);
        intersection.add(gyroid);

        //gm.setDataSource(triangle);
        gm.setSource(intersection);
        
        //gm.setSource(balls);
        // gm.setTransform(rotation);
        gm.setMaxAttributeValue(maxAttributeValue);
        gm.setVoxelSize(voxelSize*surfareThickness);
        
        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        grid.setGridBounds(bounds);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);        
       
        printf("gm.makeGrid() done\n");
        //printf("%s",grid.toStringAttributesSectionZ(nz / 2));
        SlicesWriter slicer = new SlicesWriter();
        slicer.setFilePattern("/tmp/slices/slice_%03d.png");
        slicer.setCellSize(5);
        slicer.setVoxelSize(4);
        
        slicer.setMaxAttributeValue(maxAttributeValue);
        //slicer.writeSlices(grid);
        
        
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(blockSize);
        meshmaker.setThreadCount(threadsCount);
        meshmaker.setSmoothingWidth(smoothWidth);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(maxDecimationCount);
        meshmaker.setMaxAttributeValue(maxAttributeValue);            
        
        STLWriter stl = new STLWriter("/tmp/triangle.stl");
        meshmaker.makeMesh(grid, stl);
        stl.close();
        
    }

   public static void twoTriangles() throws Exception {
        
        printf("triangularShape()\n");
    
        double voxelSize = 0.1*MM;
        double margin = 1*voxelSize;

        double s = 25*MM;
        double thickness = 4.*MM;

        double xmin = -thickness;
        double xmax = s + thickness;


        double bounds[] = new double[]{xmin, xmax, xmin, xmax, xmin, xmax};
        
        MathUtil.roundBounds(bounds, voxelSize);
        bounds = MathUtil.extendBounds(bounds, margin);
                

        int maxAttributeValue = 63;
        int blockSize = 50;
        double errorFactor = 0.05;
        double smoothWidth = 1;
        int maxDecimationCount= 10;
        int threadsCount = 4;
        double surfareThickness = sqrt(3)/2;//0.5;

        double maxDecimationError = errorFactor*voxelSize*voxelSize;

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);
        Vector3d 
            v0 = new Vector3d(s,0,0),
            v1 = new Vector3d(0,s,0),
            v2 = new Vector3d(0,0,s);
        /*
        v0 = new Vector3d(s,s,0),
            v1 = new Vector3d(0,s,0),
            v2 = new Vector3d(0,0,s);
        */
        //Triangle triangle = new Triangle(v0, v1, v2, thickness);
        Triangle triangle = new Triangle(v0, v1, v2, thickness);
        
        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(12*MM, 0.8*MM);

        Rotation rotation = new Rotation(new Vector3d(1,1,0), Math.PI/10);
        GridMaker gm = new GridMaker();  
        gm.setBounds(bounds);
        
        Intersection intersection = new Intersection();
        intersection.add(triangle);
        //intersection.add(gyroid);

        //gm.setSource(triangle);
        gm.setSource(triangle);
        
        //gm.setSource(balls);
        // gm.setTransform(rotation);
        gm.setMaxAttributeValue(maxAttributeValue);
        gm.setVoxelSize(voxelSize*surfareThickness);
        
        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        grid.setGridBounds(bounds);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);        
       
        printf("gm.makeGrid() done\n");
        //printf("%s",grid.toStringAttributesSectionZ(nz / 2));
        SlicesWriter slicer = new SlicesWriter();
        slicer.setFilePattern("/tmp/slices/slice_%03d.png");
        slicer.setCellSize(5);
        slicer.setVoxelSize(4);
        
        slicer.setMaxAttributeValue(maxAttributeValue);
        //slicer.writeSlices(grid);
        
        
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(blockSize);
        meshmaker.setThreadCount(threadsCount);
        meshmaker.setSmoothingWidth(smoothWidth);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(maxDecimationCount);
        meshmaker.setMaxAttributeValue(maxAttributeValue);            
        
        STLWriter stl = new STLWriter("/tmp/triangle.stl");
        meshmaker.makeMesh(grid, stl);
        stl.close();
        
    }


   public static void hyperBall() throws Exception {
        
        printf("hyperBall()\n");
    
        double voxelSize = 0.1*MM;
        double margin = 1*voxelSize;

        double s = 25*MM;
        int threadCount = 4;
        double xmin = -s;
        double xmax = s;
        double ymin = -s;
        double ymax = s;
        double zmin = -s;
        double zmax = s;
        

        double bounds[] = new double[]{xmin, xmax, ymin, ymax, zmin, zmax};        
        MathUtil.roundBounds(bounds, voxelSize);
        bounds = MathUtil.extendBounds(bounds, margin);        

        int maxAttributeValue = 63;
        int blockSize = 50;
        double errorFactor = 0.05;
        double smoothWidth = 1.5;
        int maxDecimationCount= 10;
        int threadsCount = 4;
        double surfareThickness = sqrt(3)/2;//0.5;

        boolean makeSTL = true;
        boolean makeSlices = false;

        double maxDecimationError = errorFactor*voxelSize*voxelSize;

        int nx = (int)((bounds[1] - bounds[0])/voxelSize + 0.5);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize + 0.5);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize + 0.5);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        //Sphere sphere = new Sphere(4*MM,4*MM,4*MM, 5*MM);
        Sphere sphere = new Sphere(0*MM,0*MM,0*MM, 0.75*MM);
        //Cylinder cylinder = new Cylinder(new Vector3d(0,0,0), new Vector3d(15*MM,15*MM,15*MM), 0.75*MM);
        Cylinder cylinder = new Cylinder(new Vector3d(0,0,0), new Vector3d(0,0,20*MM), 0.75*MM);
        //Cylinder cylinder = new Cylinder(new Vector3d(0,3*MM,0), new Vector3d(6*MM,4*MM,0), 0.75*MM);
        //cylinder.setScaleFactor(1);

        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(8*MM, 0.8*MM);  

        Rotation rotation = new Rotation(new Vector3d(1,1,0), Math.PI/10);


        GridMaker gm = new GridMaker();  
        gm.setBounds(bounds);
        
        ReflectionSymmetry symm = new ReflectionSymmetry();
        //symm.setRiemannSphereRadius(15*MM);
        symm.setRiemannSphereRadius(10*MM);

        LimitSet limitSet = new LimitSet(0.1*MM, 0.01);
        
        Union union = new Union();
        union.add(sphere);
        union.add(cylinder);
        union.add(limitSet);

        //symm.setGroup(getTwoPlanes(0, 5*MM));
        //symm.setGroup(getTwoSpheres(8*MM, 10*MM));
        //symm.setGroup(getPlaneAndSphere(15*MM, 14.99*MM));
        //symm.setGroup(getPlaneAndSphere(15*MM, 14.99*MM));
        //symm.setGroup(getPlaneAndSphere(10*MM, -20*MM));
        //symm.setGroup(getQuad(20*MM, 20*MM, 28*MM, PI/3));
        //symm.setGroup(getQuad1(20*MM, 25*MM, PI/3));
        //symm.setGroup(getXYZ(20*MM, PI/3,PI/3,PI/8));
        //symm.setGroup(getXYZ(20*MM, PI/3,PI/3,PI/5));
        gm.setThreadCount(threadCount);
        gm.setMaxAttributeValue(maxAttributeValue);
        gm.setVoxelSize(voxelSize*surfareThickness);
        
        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        grid.setGridBounds(bounds);


        Intersection intersection = new Intersection();
        DataTransformer fractal = new DataTransformer();
        fractal.setSource(union);
        fractal.setTransform(symm);

        intersection.add(fractal);
        intersection.add(new Cylinder(new Vector3d(0, 0*MM, 0),new Vector3d(0, -4*MM, 0),20*MM));       
        
        //union.add(new Sphere(8*MM,2*MM,-3*MM, 3*MM));
        //union.add(new Sphere(4*MM,0.*MM,-2*MM, 2*MM));
        //union.add(new Sphere(0*MM,0.*MM,-3*MM, 3.*MM));
        //union.add(intersection);
        //union.add(limitSet);
        
        printf("gm.makeGrid()\n");

        //gm.setSource(sphere);
        gm.setSource(intersection);
        //gm.setSource(union);        
        //gm.setSource(limitSet);

        //gm.setSource(gyroid);
        //gm.setTransform(symm);
        gm.makeGrid(grid);   
       
        printf("gm.makeGrid() done\n");
        //printf("%s",grid.toStringAttributesSectionZ(nz / 2));

        if(makeSlices){

            SlicesWriter slicer = new SlicesWriter();
            slicer.setFilePattern("/tmp/slices/slice_%03d.png");
            slicer.setCellSize(1);
            slicer.setVoxelSize(1);
            
            slicer.setMaxAttributeValue(maxAttributeValue);
            slicer.writeSlices(grid);

        }

        if(makeSTL){

            MeshMakerMT meshmaker = new MeshMakerMT();
            meshmaker.setBlockSize(blockSize);
            meshmaker.setThreadCount(threadsCount);
            meshmaker.setSmoothingWidth(smoothWidth);
            meshmaker.setMaxDecimationError(maxDecimationError);
            meshmaker.setMaxDecimationCount(maxDecimationCount);
            meshmaker.setMaxAttributeValue(maxAttributeValue);            
            
            STLWriter stl = new STLWriter("/tmp/hyperBall.stl");
            meshmaker.makeMesh(grid, stl);
            stl.close();

        }
    }

    static ReflectionGroup getTwoPlanes(double x1, double x2){
        
        ReflectionGroup.SPlane[] s = new ReflectionGroup.SPlane[] {
            new ReflectionGroup.Plane(new Vector3d(1,0,0), x1), // right of  plane 1
            new ReflectionGroup.Plane(new Vector3d(-1,0,0), -x2), // left of plane 2
        };
        return new ReflectionGroup(s);

    }


    static ReflectionGroup getQuad1(double r, double r1, double alpha){
        double r2 = r*r/(r1*cos(alpha));
        double x1 = sqrt(r1*r1 + r*r);
        double y2 = sqrt(r2*r2 + r*r);
        
        ReflectionGroup.SPlane[] s = new ReflectionGroup.SPlane[] {
            new ReflectionGroup.Plane(new Vector3d(1,0,0), 0.), // 
            new ReflectionGroup.Plane(new Vector3d(0,1,0), 0.), // 
            new ReflectionGroup.Sphere(new Vector3d(x1,0,0), -r1), // outside of sphere  
            new ReflectionGroup.Sphere(new Vector3d(0,y2,0), -r2), // outside of sphere              
        };   
        return new ReflectionGroup(s);
        
    }

    static ReflectionGroup getQuad(double r1, double r2, double x1, double alpha){

        double y2 = sqrt(r1*r1 + r2*r2 + 2*r1*r2*cos(alpha) - x1*x1);
        
        printf("r1: %7.5f x1: %7.5f r2: %7.5f y2: %7.5f\n", r1, x1, r2, y2);

        ReflectionGroup.SPlane[] s = new ReflectionGroup.SPlane[] {
            new ReflectionGroup.Plane(new Vector3d(1,0,0), 0.), // 
            new ReflectionGroup.Plane(new Vector3d(0,1,0), 0.), // 
            new ReflectionGroup.Sphere(new Vector3d(x1,0,0), -r1), // outside of sphere  
            new ReflectionGroup.Sphere(new Vector3d(0,y2,0), -r2), // outside of sphere              
        };   
        return new ReflectionGroup(s);
    }

    static ReflectionGroup getTwoSpheres(double r1, double r2){
        
        if(r1 > r2) {
            double t = r2;
            r2 = r1;
            r1 = t;
        }

        ReflectionGroup.SPlane[] s = new ReflectionGroup.SPlane[] {
            new ReflectionGroup.Sphere(new Vector3d(0,0,0), -r1), // outside of smaller sphere 
            new ReflectionGroup.Sphere(new Vector3d(0,0,0), r2), // inside of larger sphere  
        };
        return new ReflectionGroup(s);
    }
    
    static ReflectionGroup getPlaneAndSphere(double x, double r){
        double n = (r > 0)? (1) : (-1);
        ReflectionGroup.SPlane[] s = new ReflectionGroup.SPlane[] {
            new ReflectionGroup.Plane(new Vector3d(n,0,0), 0.), // right of yz plane
            new ReflectionGroup.Sphere(new Vector3d(x,0,0), -r), // outside of sphere  
        };
        return new ReflectionGroup(s);        
    }

    static ReflectionGroup getIcosahedralKaleidoscope(){
        double t = (sqrt(5)+1)/2;

        Vector3d v5 = new Vector3d(1,0,t); // vertex of icosahedron 
        Vector3d v3 = new Vector3d(0,1/t,t); // vertex of dodecahedron 
        Vector3d p35 = new Vector3d(); p35.cross(v5,v3); p35.normalize();

        ReflectionGroup.SPlane[] s = new ReflectionGroup.SPlane[] {
            new ReflectionGroup.Plane(new Vector3d(1,0,0), 0.), 
            new ReflectionGroup.Plane(new Vector3d(0,1,0), 0.), 
            //new ReflectionGroup.Plane(new Vector3d(0,0,1), 0.), 
            new ReflectionGroup.Plane(p35, 0.),
        };
        return new ReflectionGroup(s);          
        
    }

    static ReflectionGroup getXYZ(double r, double ax,double ay, double az){
        double dx = r*cos(ax);
        double dy = r*cos(ay);
        double dz = r*cos(az);
        
        ReflectionGroup.SPlane[] s = new ReflectionGroup.SPlane[] {
            new ReflectionGroup.Plane(new Vector3d(1,0,0), 0.), // 
            new ReflectionGroup.Plane(new Vector3d(0,1,0), 0.), // 
            new ReflectionGroup.Plane(new Vector3d(0,0,1), 0.), // 
            new ReflectionGroup.Sphere(new Vector3d(dx, dy, dz), -r), // outside of sphere  
        };   
        return new ReflectionGroup(s);
        
    }

    public static void cylinderTest() throws Exception {
        
        printf("cylinderTest()\n");    

        double voxelSize = 0.1*MM;
        double margin = 1*voxelSize;

        double s = 16*MM;

        double xmin = -s;
        double xmax = s;

        double bounds[] = new double[]{xmin, xmax, xmin, xmax, xmin, xmax};
        
        MathUtil.roundBounds(bounds, voxelSize);
        bounds = MathUtil.extendBounds(bounds, margin);                


        int maxAttributeValue = 63;
        int blockSize = 50;
        double errorFactor = 0.05;
        double smoothWidth = 1;
        int maxDecimationCount= 10;
        int threadsCount = 4;
        double surfareThickness = sqrt(3)/2;//0.5;
        double maxDecimationError = errorFactor*voxelSize*voxelSize;
        int nx[] = MathUtil.getGridSize(bounds, voxelSize);

        double ss = 13*MM;
        double rs = 1*MM;

        printf("grid: [%d x %d x %d]\n", nx[0],nx[1],nx[2]);
        Union union = new Union();

        union.add(new Sphere(ss, ss, ss, rs));
        union.add(new Sphere(-ss, ss, ss, rs));
        union.add(new Sphere(-ss, -ss, ss, rs));
        union.add(new Sphere(ss, -ss, ss, rs));
        union.add(new Sphere(ss, ss, -ss, rs));
        union.add(new Sphere(-ss, ss, -ss, rs));
        union.add(new Sphere(-ss, -ss, -ss, rs));
        union.add(new Sphere(ss, -ss, -ss, rs));

        union.add(new Cylinder(new Vector3d(-ss,-ss,-ss), new Vector3d(ss, ss, ss), rs));
        union.add(new Cylinder(new Vector3d(-ss,ss,-ss), new Vector3d(ss, -ss, ss), rs));
        union.add(new Cylinder(new Vector3d(-ss,-ss,ss), new Vector3d(ss, ss, -ss), rs));
        union.add(new Cylinder(new Vector3d(ss,-ss,-ss), new Vector3d(-ss, ss, ss), rs));

        GridMaker gm = new GridMaker();  
        gm.setBounds(bounds);
        
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
        
        STLWriter stl = new STLWriter("/tmp/cylinder.stl");
        meshmaker.makeMesh(grid, stl);
        stl.close();
        
    }    
  

    public static void makeIcosahedron() throws Exception {
        
        printf("makeIcosahedron()\n");    

        double voxelSize = 0.1*MM;
        double margin = 1*voxelSize;

        double s = 16*MM;

        double xmin = -s;
        double xmax = s;

        double bounds[] = new double[]{xmin, xmax, xmin, xmax, xmin, xmax};
        
        MathUtil.roundBounds(bounds, voxelSize);
        bounds = MathUtil.extendBounds(bounds, margin);                


        int maxAttributeValue = 63;
        int blockSize = 50;
        double errorFactor = 0.05;
        double smoothWidth = 1;
        int maxDecimationCount= 10;
        int threadsCount = 4;
        double surfareThickness = sqrt(3)/2;//0.5;
        double maxDecimationError = errorFactor*voxelSize*voxelSize;
        int nx[] = MathUtil.getGridSize(bounds, voxelSize);

        double ss = 7*MM;
        double rs = 1*MM;
        double T = (sqrt(5) + 1)/2;
        printf("grid: [%d x %d x %d]\n", nx[0],nx[1],nx[2]);
        Union union = new Union();
        Vector3d v5 = new Vector3d(ss, 0,   ss*T);
        Vector3d v3 = new Vector3d(0, ss/T, ss*T);
        Vector3d v2 = new Vector3d(0, 0,    ss*T);
        

        //union.add(new Sphere(0, ss/T, ss*T, rs));
        //union.add(new Cylinder(v2, v3, rs));
        union.add(new Cylinder(v5, v3, rs));
        //union.add(new Cylinder(v5, v2, rs));
        //union.add(new Cylinder(v3, v2, rs));
        union.add(new Sphere(v3, rs));
        union.add(new Sphere(v5, rs));

        ReflectionSymmetry symm = new ReflectionSymmetry();
        //symm.setRiemannSphereRadius(15*MM);
        //symm.setGroup(getIcosahedralKaleidoscope());
        
        double sg = 10*MM;
        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid();  
        gyroid.setPeriod(sg);
        gyroid.setThickness(0.03*sg);
        //gyroid.setLevel(0);
        gyroid.setLevel(1.5);
        gyroid.setCenter(new Vector3d(0.25*sg,-0.2*sg,0.*sg));

        Intersection intersection = new Intersection();
        intersection.add(new Sphere(0,0,0,15*MM));
        intersection.add(gyroid);        

        GridMaker gm = new GridMaker();  
        gm.setBounds(bounds);
        
        //gm.setSource(union);
        gm.setSource(intersection);

        gm.setTransform(symm);
        
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
        
        STLWriter stl = new STLWriter("/tmp/dodecahedron.stl");
        meshmaker.makeMesh(grid, stl);
        stl.close();
        
    }    


    public static void testTransformableSphere() throws Exception {
        
        double voxelSize = 0.1*MM;
        double margin = 1*voxelSize;

        double s = 16*MM;

        double xmin = -s;
        double xmax = s;

        double bounds[] = new double[]{xmin, xmax, xmin, xmax, xmin, xmax};
        
        MathUtil.roundBounds(bounds, voxelSize);
        bounds = MathUtil.extendBounds(bounds, margin);                


        int maxAttributeValue = 63;
        int blockSize = 50;
        double errorFactor = 0.05;
        double smoothWidth = 1;
        int maxDecimationCount= 10;
        int threadsCount = 0;
        double surfareThickness = sqrt(3)/2;
        double maxDecimationError = errorFactor*voxelSize*voxelSize;
        int nx[] = MathUtil.getGridSize(bounds, voxelSize);

        double sr = 2*MM;

        Sphere sphere = new Sphere();
        sphere.setRadius(sr);
        sphere.setTransform(new Translation(2.*MM,0,0));
        
        GridMaker gm = new GridMaker();  
        gm.setBounds(bounds);
        
        gm.setMaxAttributeValue(maxAttributeValue);
        gm.setVoxelSize(voxelSize*surfareThickness);        
        gm.setSource(sphere);        
        
        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx[0], nx[1], nx[2], voxelSize, voxelSize);
        grid.setGridBounds(bounds);
        
        long t0 = time();
        printf("gm.makeGrid()\n");
        if(false){
            for(int i = -3; i < 3; i++){
                sphere.setTransform(new Translation(i*3*MM,0,0));
                gm.makeGrid(grid);               
            }
        }
        if(true){
            Union union = new Union();
            for(int i = -3; i < 3; i++){
                Sphere sph = new Sphere();
                sph.setTransform(new Translation(i*3*MM,0,0));
                union.add(sphere);
            }            
            gm.setSource(union);
            gm.makeGrid(grid);               
        }

        printf("gm.makeGrid() done in %d ms\n", (time() - t0));
        
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(blockSize);
        meshmaker.setThreadCount(threadsCount);
        meshmaker.setSmoothingWidth(smoothWidth);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(maxDecimationCount);
        meshmaker.setMaxAttributeValue(maxAttributeValue);            
        
        STLWriter stl = new STLWriter("/tmp/sphere.stl");
        meshmaker.makeMesh(grid, stl);
        stl.close();
        
    }

    public static void testLevels()throws Exception {

        printf("testLevels()");

        double voxelSize = 0.1*MM;
        double margin = 1*voxelSize;

        double s = 51*MM;
        double rs = 50*MM;
        double surfareThickness = sqrt(3)/2;

        double xmin = -s;
        double xmax = s;

        double bounds[] = new double[]{xmin, xmax, xmin, xmax, xmin, xmax};
        
        MathUtil.roundBounds(bounds, voxelSize);
        bounds = MathUtil.extendBounds(bounds, margin);                
        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        int cSize = 1, vSize = 1;
        int maxAttributeValue = 0;
        double surfLevel = maxAttributeValue/2.;
        double levels[] = new double[]{surfLevel};

        printf("grid: [%d x %d x %d]\n", nx,ny, nz);
        Union union = new Union();
        double vs2 = voxelSize/2;
        union.add(new Sphere(vs2,vs2, vs2,rs));
        //union.add(new Cylinder(new Vector3d(-ss,-ss,-ss), new Vector3d(ss, ss, ss), rs));

        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        grid.setGridBounds(bounds);

        GridMaker gm = new GridMaker();  
        gm.setSource(union);        
        gm.setMaxAttributeValue(maxAttributeValue);
        gm.setVoxelSize(voxelSize*surfareThickness);
        
        long t0 = time();
        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done in %d ms\n", (time() - t0));

        SlicesWriter slicer = new SlicesWriter();
        slicer.setFilePattern("/tmp/levels/slice_%03d.png");
        //slicer.setCellSize(30); slicer.setVoxelSize(29);        
        slicer.setCellSize(cSize); 
        slicer.setVoxelSize(vSize);        
        slicer.setBackgroundColor(0xFFFFFF);
        slicer.setMaxAttributeValue(maxAttributeValue);
        int z = nz/2;
        slicer.setBounds(0, nx, 0, ny, z, z+1);
        slicer.setWriteLevels(false);
        slicer.setWriteVoxels(true);
        //slicer.setLevels(new double[]{0.25, 0.5, 0.75});
        //slicer.setLevels(new double[]{30.01, 50.,70.01 });
        slicer.setLevels(levels);
        //slicer.setLevels(new double[]{40.1, 50.1, 60.1});
        slicer.writeSlices(grid);

        
    }


    public static void main(String[] args) throws Exception {

        //gyroCube();
        //triangularShape();
        //hyperBall();

        //makeIcosahedron();
        //testTransformableSphere();
        testLevels();
    }
}
