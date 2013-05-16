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

import abfab3d.util.Vec;
import abfab3d.util.VecTransform;
import abfab3d.util.MathUtil;
import abfab3d.util.TextUtil;
import abfab3d.util.Symmetry;

import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.STLWriter;
import abfab3d.io.output.MeshMakerMT;

import abfab3d.util.ImageMipMap;

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
public class TestSliceWriter extends TestCase {

    static final double CM = 0.01; // cm -> meters
    static final double MM = 0.001; // mm -> meters

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSliceWriter.class);
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

        DataSources.Ball ball = new DataSources.Ball(0,0,0,ballRadius);  
        DataSources.Torus torus = new DataSources.Torus(0.34*CM, 0.15*CM);

        VolumePatterns.Balls balls = new VolumePatterns.Balls(0.5*CM, 0.25*CM);  
        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(0.5*CM, 0.05*CM);  

        //VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(0.5*CM, 0.05*CM);  

        VecTransforms.Rotation rotation = new VecTransforms.Rotation(new Vector3d(1,1,0), Math.PI/10);
        GridMaker gm = new GridMaker();  
        gm.setBounds(bounds);
        //gm.setDataSource(gyroid);
        //gm.setDataSource(torus);
        gm.setDataSource(ball);
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

    public void testRing() throws Exception {
        
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

        DataSources.Ball ball = new DataSources.Ball(0,0,0,ballRadius);  
        DataSources.Torus torus = new DataSources.Torus(0.34*CM, 0.15*CM);

        VolumePatterns.Balls balls = new VolumePatterns.Balls(0.5*CM, 0.25*CM);  
        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(0.5*CM, 0.05*CM);  

        //VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(0.5*CM, 0.05*CM);  

        VecTransforms.Rotation rotation = new VecTransforms.Rotation(new Vector3d(1,1,0), Math.PI/10);
        GridMaker gm = new GridMaker();  
        gm.setBounds(bounds);
        //gm.setDataSource(gyroid);
        //gm.setDataSource(torus);
        gm.setDataSource(ball);
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
        AttributeGrid grid = readGrid("/tmp/shoe_512.grid",maxAttributeValue);

        double s = 23.3*CM/2;
        grid.setGridBounds(new double[]{-s, s, -s, s,-s, s });
    
        SlicesWriter slicer = new SlicesWriter();
        slicer.setFilePattern("/tmp/slices/slice_%03d.png");
        slicer.setCellSize(1);
        slicer.setVoxelSize(1);
        
        slicer.setMaxAttributeValue(maxAttributeValue);

        //slicer.writeSlices(grid);


        int blockSize = 50;
        double errorFactor = 0.1;
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
        
        STLWriter stl = new STLWriter("/tmp/slicer.stl");
        meshmaker.makeMesh(grid, stl);
        stl.close();



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
        
        for(int x =0; x < nx; x++){
            for(int y =0; y < ny; y++){
                for(int z =0; z < nz; z++){
                    int d = data.readUnsignedByte();
                    grid.setAttribute(x,y,z, (d*maxAttributeValue)/255 );
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

  


    public static void main(String[] args) {

    }
}
