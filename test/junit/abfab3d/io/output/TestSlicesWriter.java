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
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;


// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridByte;

import abfab3d.geom.TriangulatedModels;

import abfab3d.util.MathUtil;
import abfab3d.util.ImageGray16;

import abfab3d.datasources.Box;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Ring;
import abfab3d.datasources.Torus;
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

import abfab3d.datasources.VolumePatterns;

import abfab3d.grid.op.GridMaker;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;

import static java.lang.Math.sqrt;

/**
 * Tests the functionality of SlicesWriter
 *
 * @version
 */
public class TestSlicesWriter extends TestCase {

    static final double CM = 0.01; // cm -> meters
    static final double MM = 0.001; // mm -> meters

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSliceWriter.class);
    }

    public void testDumb(){
        //this test here is to make Test happy. 
    }

  
    public void _testBall() throws Exception {
        
        printf("testBall()\n");
    
        double voxelSize = 0.1*MM;
        double margin = 0*voxelSize;

        double sizex = 1*CM; 
        double sizey = 1*CM; 
        double sizez = 1*CM;
        double ballRadius = 4.5*MM;
        double surfareThickness = Math.sqrt(3)/2;

        double gridWidth = sizex + 2*margin;
        double gridHeight = sizey + 2*margin;
        double gridDepth = sizez + 2*margin;
        int maxAttributeValue = 1;
        int blockSize = 150;
        double errorFactor = 0.1;
        double smoothWidth = 0.;
        int maxDecimationCount= 10;
        int threadsCount = 1;

        double maxDecimationError = errorFactor*voxelSize*voxelSize;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2,-gridHeight/2,gridHeight/2,-gridDepth/2,gridDepth/2};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        Sphere sphere = new Sphere(0,0,0,ballRadius);
        Torus torus = new Torus(0.34*CM, 0.15*CM);

        VolumePatterns.Balls balls = new VolumePatterns.Balls(0.5*CM, 0.25*CM);  
        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(0.5*CM, 0.05*CM);  

        //VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(0.5*CM, 0.05*CM);  

        Rotation rotation = new Rotation(new Vector3d(1,1,0), Math.PI/10);
        GridMaker gm = new GridMaker();  
        gm.setBounds(bounds);
        //gm.setDataSource(gyroid);
        //gm.setDataSource(torus);
        gm.setSource(sphere);
        // gm.setTransform(rotation);
        gm.setMaxAttributeValue(maxAttributeValue);
        gm.setVoxelSize(voxelSize*surfareThickness);

        
        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        
        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);        
       
        printf("gm.makeGrid() done\n");
        //printf("%s",grid.toStringAttributesSectionZ(nz / 2));
        SlicesWriter slicer = new SlicesWriter();
        slicer.setFilePattern("/tmp/slices/slice_%03d.png");
        slicer.setCellSize(20);
        slicer.setVoxelSize(9);
        
        slicer.setMaxAttributeValue(maxAttributeValue);

        slicer.writeSlices(grid);
        
        /*
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(blockSize);
        meshmaker.setThreadCount(threadsCount);
        meshmaker.setSmoothingWidth(smoothWidth);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(maxDecimationCount);
        meshmaker.setMaxAttributeValue(maxAttributeValue);            
        
        STLWriter stl = new STLWriter("/tmp/slicer.stl");
        meshmaker.makeMesh(grid, stl);
        stl.close();
        */
    }

    public void _testRing() throws Exception {
        
        printf("testBall()\n");
    
        double voxelSize = 0.1*MM;
        double margin = 0*voxelSize;

        double sizex = 1*CM; 
        double sizey = 1*CM; 
        double sizez = 1*CM;
        double ballRadius = 4.5*MM;
        double surfareThickness = Math.sqrt(3)/2;

        double gridWidth = sizex + 2*margin;
        double gridHeight = sizey + 2*margin;
        double gridDepth = sizez + 2*margin;
        int maxAttributeValue = 1;
        int blockSize = 150;
        double errorFactor = 0.1;
        double smoothWidth = 0.;
        int maxDecimationCount= 10;
        int threadsCount = 1;

        double maxDecimationError = errorFactor*voxelSize*voxelSize;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2,-gridHeight/2,gridHeight/2,-gridDepth/2,gridDepth/2};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        Sphere sphere = new Sphere(0,0,0,ballRadius);
        Torus torus = new Torus(0.34*CM, 0.15*CM);

        VolumePatterns.Balls balls = new VolumePatterns.Balls(0.5*CM, 0.25*CM);  
        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(0.5*CM, 0.05*CM);  

        //VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(0.5*CM, 0.05*CM);  

        Rotation rotation = new Rotation(new Vector3d(1,1,0), Math.PI/10);
        GridMaker gm = new GridMaker();  
        gm.setBounds(bounds);
        //gm.setSource(gyroid);
        //gm.setSource(torus);
        gm.setSource(sphere);
        // gm.setTransform(rotation);
        gm.setMaxAttributeValue(maxAttributeValue);
        gm.setVoxelSize(voxelSize*surfareThickness);

        
        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        
        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);        
       
        printf("gm.makeGrid() done\n");
        //printf("%s",grid.toStringAttributesSectionZ(nz / 2));
        SlicesWriter slicer = new SlicesWriter();
        new File("/tmp/slices").mkdirs();
        slicer.setFilePattern("/tmp/slices/slice_%03d.png");
        slicer.setCellSize(20);
        slicer.setVoxelSize(9);
        
        slicer.setMaxAttributeValue(maxAttributeValue);

        slicer.writeSlices(grid);
        
        /*
        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(blockSize);
        meshmaker.setThreadCount(threadsCount);
        meshmaker.setSmoothingWidth(smoothWidth);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(maxDecimationCount);
        meshmaker.setMaxAttributeValue(maxAttributeValue);            
        
        STLWriter stl = new STLWriter("/tmp/slicer.stl");
        meshmaker.makeMesh(grid, stl);
        stl.close();
        */
    }

    public void _testGrid() throws Exception {

        int maxAttributeValue = 63;
        //AttributeGrid grid = readGrid("/tmp/gyroid_32.grid");
        //AttributeGrid grid = readGrid("/tmp/shoe_512.grid",maxAttributeValue);
        //AttributeGrid grid = readGrid("/tmp/star_20arms_1mm_level7.grid",maxAttributeValue);
        //AttributeGrid grid = readGrid("/tmp/star_20arms_1mm_level6.grid",maxAttributeValue);
        //AttributeGrid grid = readGrid("/tmp/star_20arms_2mm_level7.grid",maxAttributeValue);
        //AttributeGrid grid = readGrid("/tmp/star_20arms_2mm_level8.grid",maxAttributeValue);
        //AttributeGrid grid = readGrid("/tmp/star_20arms_2mm_level9.grid",maxAttributeValue);
        //AttributeGrid grid = readGrid("/tmp/star_24_512.grid",maxAttributeValue);
        //AttributeGrid grid = readGrid("/tmp/bad/12.grid",maxAttributeValue);
        AttributeGrid grid = readGrid("/tmp/star_400.grid",maxAttributeValue);
        //AttributeGrid grid = readGrid("/tmp/bad/12_1024.grid",maxAttributeValue);

        double s = 23.3*CM/2;
        grid.setGridBounds(new double[]{-s, s, -s, s,-s, s });
    
        SlicesWriter slicer = new SlicesWriter();
        slicer.setFilePattern("/tmp/slices/slice_%03d.png");
        slicer.setCellSize(1);
        slicer.setVoxelSize(1);
        
        slicer.setMaxAttributeValue(maxAttributeValue);

        slicer.writeSlices(grid);
        
        /*
        int blockSize = 50;
        double errorFactor = 0.05;
        double smoothWidth = 1;
        int maxDecimationCount= 10;
        int threadsCount = 4;
        double voxelSize = 2*s/grid.getWidth();

        double maxDecimationError = errorFactor*voxelSize*voxelSize;

        MeshMakerMT meshmaker = new MeshMakerMT();
        meshmaker.setBlockSize(blockSize);
        meshmaker.setThreadCount(threadsCount);
        meshmaker.setSmoothingWidth(smoothWidth);
        meshmaker.setMaxDecimationError(maxDecimationError);
        meshmaker.setMaxDecimationCount(maxDecimationCount);
        meshmaker.setMaxAttributeValue(maxAttributeValue);            
        
        STLWriter stl = new STLWriter("/tmp/bad/12_1024_repaired.stl");
        meshmaker.makeMesh(grid, stl);
        stl.close();x
        */


    }

    public void _testHeightField() throws Exception {
        
        int maxAttributeValue = 63;
        AttributeGrid grid = readGrid("/tmp/star_20arms_2mm_level7.grid",maxAttributeValue);
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        int scale = 4;
        int sliceZ = nz/2-1;
        
        ImageGray16 slice = getSlice(grid, sliceZ, scale);
        //slice.gaussianBlur(scale*1.0);
        slice.convolute(MathUtil.getBoxKernel(4));

        TriangulatedModels.HeightField hf = new TriangulatedModels.HeightField(slice, maxAttributeValue, 50*MM, 50*MM, 2*MM);
        
        STLWriter stl = new STLWriter("/tmp/slice_hf_box_2.stl");
        hf.getTriangles(stl);
        stl.close();
        
    }

    public void _testWavePropagation() throws Exception {

        int maxAttributeValue = 63;
        //AttributeGrid grid = readGrid("/tmp/star_20arms_2mm_level8.grid",maxAttributeValue);
        AttributeGrid grid = readGrid("/tmp/star_24_512.grid",maxAttributeValue);
        
        int sliceZ = grid.getWidth()/2-1;
        
        ImageGray16 slice0 = getSlice(grid, sliceZ, 1);
        ImageGray16 slice1 = (ImageGray16)slice0.clone();
        ImageGray16 slice2 = (ImageGray16)slice0.clone();
        ImageGray16 slices[] = new ImageGray16[]{slice0, slice1, slice2};
        
        double c = 0.2;
        for( int i=0, k = 0; i < 100; i++){
            int k1 = (k+1)%3;
            int k2 = (k+2)%3;

            makeIteration(c, slices[k], slices[k1], slices[k2]);

            slices[k2].write(fmt("/tmp/wave_512/wave_%03d.png",(i)), 0xFF);

            k = (k+1)%3;

        }
        
    }

    public void _testCircularWave() throws Exception {
        int gridSize  = 512; 
        double radius = 0.4;
        double areaSize = 1;
        int maxAttributeValue = 255;

        //double pixelSize = (sqrt(2)/2)*areaSize/gridSize;
        double pixelSize = 0.5*areaSize/gridSize;

        ImageGray16 slice0 = makeCircle(gridSize, areaSize, radius, pixelSize);
        ImageGray16 slice1 = makeCircle(gridSize, areaSize, radius - 0.0*pixelSize, pixelSize);

        slice0.write(fmt("/tmp/circular_wave_512/wave_%03d.png",(0)), 0xFF);

        if(true) {
            TriangulatedModels.HeightField hf = new TriangulatedModels.HeightField(slice0, maxAttributeValue, 50*MM, 50*MM, 2*MM);        
            STLWriter stl = new STLWriter("/tmp/circular_wave_512/slice_hf.stl");
            hf.getTriangles(stl);
            stl.close();
        }

        //ImageGray16 slice1 = (ImageGray16)slice0.clone();
        ImageGray16 slice2 = (ImageGray16)slice0.clone();
        ImageGray16 slices[] = new ImageGray16[]{slice0, slice1, slice2};
        
        //double c = 0.2;
        double c = 0.3;
        
        for( int i=0, k = 0; i < 20; i++){
            int k1 = (k+1)%3;
            int k2 = (k+2)%3;

            makeIteration(c, slices[k], slices[k1], slices[k2]);

            slices[k1].write(fmt("/tmp/circular_wave_512/wave_%03d.png",(i)), 0xFF);

            k = (k+1)%3;

        }
        
        if(true) {
            TriangulatedModels.HeightField hf = new TriangulatedModels.HeightField(slice0, maxAttributeValue, 50*MM, 50*MM, 2*MM);        
            STLWriter stl = new STLWriter("/tmp/circular_wave_512/slice_hf_final.stl");
            hf.getTriangles(stl);
            stl.close();
        }
    }

    // writes to slice2 result of wave iteration from with given values at 2 previous steps
    static void makeIteration(double c, ImageGray16 slice0, ImageGray16 slice1, ImageGray16 slice2){
        int nx = slice0.getWidth();
        int ny = slice0.getHeight();
        double c2 = c*c;

        for( int y = 0; y < ny; y++){
            for( int x = 0; x < nx; x++){
                
                double v = c2*(slice1.getDataI(x+1, y) + slice1.getDataI(x-1, y) + 
                               slice1.getDataI(x, y+1) + slice1.getDataI(x, y-1) - 4 * slice1.getDataI(x, y)) 
                    + 2 * slice1.getDataI(x, y) - slice0.getDataI(x,y);
                slice2.setData(x,y,(int)(v+0.5));                    
            }
        }
    }
    
    static ImageGray16 getSlice(AttributeGrid grid, int slizeZ, int scale){

        int nx = grid.getWidth()*scale;
        int ny = grid.getHeight()*scale;
        byte data[] = new byte[nx*ny];

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){

                long att = grid.getAttribute(x/scale,y/scale,slizeZ);
                data[x + nx * y] = (byte)(att*4);
            }
        }
        return new ImageGray16(data, nx, ny);
            
    }
    
    static ImageGray16 makeCircle(int nx, double width, double radius, double pixelSize){

        ImageGray16 image = new ImageGray16(nx, nx);
        for(int iy = 0; iy < nx; iy++){
            double y = iy * width / (nx-1) - width/2;
            for(int ix = 0; ix < nx; ix++){
                double x = ix * width / (nx-1) - width/2;
                double d = sqrt(x*x + y*y) - radius;
                if(d <= -pixelSize) d = 1;
                else if(d >= pixelSize) d = 0;
                else d = (-d )/(2*pixelSize) + 0.5;
                image.setData(ix, iy, (int)(d * 0xFF));
            }
        }
        return image;
    }

    static AttributeGrid readGrid(String path, int maxAttributeValue)throws IOException{
        
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path), (1 << 14));
        DataInputStream data = new DataInputStream(bis);


        int nx = readInt(data);
        int ny = readInt(data);
        int nz = readInt(data);

        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        //GridShortIntervals grid = new GridShortIntervals(nx, ny, nz, 0.1*MM, 0.1*MM);
        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, ny, nz, 1., 1.);
        
        for(int z =0; z < nz; z++){
            for(int x =0; x < nx; x++){
                for(int y =0; y < ny; y++){
                    int d = data.readUnsignedByte();
                    grid.setAttribute(x,y, z, (d*maxAttributeValue)/255 );
                }
            }
        }

        return grid;
    }

    public static int readInt(DataInputStream data) throws IOException{
        
        int i = data.readUnsignedByte() | (data.readUnsignedByte()<<8)|
            (data.readUnsignedByte()<<16)|(data.readUnsignedByte()<<24);      
        return i;
    }
  
    /**
       makes a test slices set 
     */
    void makeBall() throws IOException{
        
        printf("makeBall()\n");
    
        double voxelSize = 0.1*MM;
        double margin = 1*voxelSize;

        double sizex = 10*MM; 
        double sizey = 10*MM; 
        double sizez = 10*MM;
        double ballRadius = 5.0*MM;
        double surfareThickness = Math.sqrt(3)/2;

        double gridWidth = sizex + 2*margin;
        double gridHeight = sizey + 2*margin;
        double gridDepth = sizez + 2*margin;
        int subvoxelResolution = 255;

        int threadsCount = 1;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2,-gridHeight/2,gridHeight/2,-gridDepth/2,gridDepth/2};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        Sphere sphere = new Sphere(0,0,0,ballRadius);
        Torus torus = new Torus(0.34*CM, 0.15*CM);
        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(0.5*CM, 0.05*CM);  

        Rotation rotation = new Rotation(new Vector3d(1,1,0), Math.PI/10);
        GridMaker gm = new GridMaker();  
        gm.setBounds(bounds);
        //gm.setDataSource(gyroid);
        //gm.setDataSource(torus);
        gm.setSource(sphere);
        // gm.setTransform(rotation);

        gm.setSubvoxelResolution(subvoxelResolution);
        gm.setVoxelScale(surfareThickness);
        
        ArrayAttributeGridByte grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        
        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);        
       
        printf("gm.makeGrid() done\n");
        SlicesWriter slicer = new SlicesWriter();
        slicer.setFilePattern("/tmp/slices/slice%04d.png");
        slicer.setCellSize(1);
        slicer.setVoxelSize(1);
        slicer.setSubvoxelResolution(subvoxelResolution);
        slicer.writeSlices(grid);
                
    }

    public static void main(String[] args) throws IOException {
        new TestSlicesWriter().makeBall();
    }
}
