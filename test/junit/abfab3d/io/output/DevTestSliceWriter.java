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
import abfab3d.util.PointToTriangleDistance;

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

import abfab3d.util.ImageMipMap;

import abfab3d.grid.op.DataSources;
import abfab3d.grid.op.VecTransforms;
import abfab3d.grid.op.VolumePatterns;
import abfab3d.grid.op.GridMaker;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.CM;
import static abfab3d.util.Units.MM;


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
public class DevTestSliceWriter extends TestCase {

 
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
        
        //DataSources.Triangle triangle = new DataSources.Triangle(v0, v1, v2, thickness);
        DataSources.Triangle triangle = new DataSources.Triangle(v0, v1, v2, thickness);
        
        VolumePatterns.Balls balls = new VolumePatterns.Balls(0.5*CM, 0.25*CM);  
        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(12*MM, 0.8*MM);  

        VecTransforms.Rotation rotation = new VecTransforms.Rotation(new Vector3d(1,1,0), Math.PI/10);
        GridMaker gm = new GridMaker();  
        gm.setBounds(bounds);
        
        DataSources.Intersection intersection = new DataSources.Intersection();
        intersection.add(triangle);
        intersection.add(gyroid);

        //gm.setDataSource(triangle);
        gm.setDataSource(intersection);
        
        //gm.setDataSource(balls);
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
        //DataSources.Triangle triangle = new DataSources.Triangle(v0, v1, v2, thickness);
        DataSources.Triangle triangle = new DataSources.Triangle(v0, v1, v2, thickness);
        
        VolumePatterns.Balls balls = new VolumePatterns.Balls(0.5*CM, 0.25*CM);  
        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(12*MM, 0.8*MM);  

        VecTransforms.Rotation rotation = new VecTransforms.Rotation(new Vector3d(1,1,0), Math.PI/10);
        GridMaker gm = new GridMaker();  
        gm.setBounds(bounds);
        
        DataSources.Intersection intersection = new DataSources.Intersection();
        intersection.add(triangle);
        intersection.add(gyroid);

        //gm.setDataSource(triangle);
        gm.setDataSource(intersection);
        
        //gm.setDataSource(balls);
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

  
    public static void main(String[] args) throws Exception {

        triangularShape();

    }
}
