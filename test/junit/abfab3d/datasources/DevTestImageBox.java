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

package abfab3d.datasources;

// External Imports


import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.awt.Font;
import java.awt.Insets;


import java.io.File;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Matrix4d;

// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.GridShortIntervals;

import abfab3d.grid.op.GridMaker;


import abfab3d.util.Vec;
import abfab3d.util.MathUtil;
import abfab3d.util.TextUtil;
import abfab3d.util.Symmetry;
import abfab3d.util.VecTransform;


import abfab3d.datasources.Box;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Ring;
import abfab3d.datasources.ImageBox;
import abfab3d.datasources.DataTransformer;
import abfab3d.datasources.Intersection;
import abfab3d.datasources.Union;
import abfab3d.datasources.Subtraction;

import abfab3d.transforms.RingWrap;
import abfab3d.transforms.FriezeSymmetry;
import abfab3d.transforms.WallpaperSymmetry;
import abfab3d.transforms.Rotation;
import abfab3d.transforms.CompositeTransform;
import abfab3d.transforms.Scale;
import abfab3d.transforms.SphereInversion;
import abfab3d.transforms.Translation;
import abfab3d.transforms.PlaneReflection;

import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.STLWriter;
import abfab3d.io.output.MeshMakerMT;

import abfab3d.util.ImageMipMap;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.MathUtil.TORAD;
import static abfab3d.util.Units.MM;

import static java.lang.System.currentTimeMillis;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static java.lang.Math.PI;


import static abfab3d.util.VecTransform.RESULT_OK;
import static abfab3d.util.MathUtil.normalizePlane;


/**
 * Tests the functionality of GridMaker
 *
 * @version
 */
public class DevTestImageBox{


    int gridMaxAttributeValue = 127;

    public void testBitmapAspectRatio() throws Exception {

        printf("testBitmap()\n");

        double voxelSize = 1.e-4;
        int nx = 100;
        int ny = 100;
        int nz = 100;

        double s = 12*MM/2;
        double bounds[] = new double[]{-s, s, -s, s, -s, s};
        
        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);            
        grid.setGridBounds(bounds);
        
        String imagePath = "test_imageBitmap.stl";

        ImageBox image = new ImageBox("test/images/blackcircle.png", 10*MM, 0, 10*MM, voxelSize);
        image.setUseGrayscale(false);
        image.setBlurWidth(voxelSize);

        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(gridMaxAttributeValue);
        gm.setSource(image);

        gm.makeGrid(grid); 

        writeGrid(grid, "/tmp/test_bitmap.stl");
        
    }

    public void testBitmapGray() throws Exception {

        printf("testBitmap()\n");

        double voxelSize = 1.e-4;
        int nx = 100;
        int ny = 100;
        int nz = 100;

        double s = 12*MM/2;
        double bounds[] = new double[]{-s, s, -s, s, -s, s};
        
        AttributeGrid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);            
        grid.setGridBounds(bounds);
        
        String imagePath = "test_imageBitmap.stl";

        ImageBox image = new ImageBox("test/images/blackcircle_blur.png", 10*MM, 0, 10*MM, voxelSize);
        image.setUseGrayscale(true);
        //image.setBlurWidth(voxelSize);

        GridMaker gm = new GridMaker();
        gm.setMaxAttributeValue(gridMaxAttributeValue);
        gm.setSource(image);

        gm.makeGrid(grid); 

        writeGrid(grid, "/tmp/test_bitmap.stl");
        
    }
    
    void writeGrid(Grid grid, String path) throws Exception {

        MeshMakerMT mmaker = new MeshMakerMT();
        mmaker.setMaxAttributeValue(gridMaxAttributeValue);
        mmaker.setSmoothingWidth(0.);
        mmaker.setBlockSize(50);
        mmaker.setMaxDecimationError(3.e-10);

        STLWriter stl = new STLWriter(path);
        mmaker.makeMesh(grid, stl);
        stl.close();

    }

    public static void main(String[] args) throws Exception {

        DevTestImageBox dt = new DevTestImageBox();
        //dt.testBitmapAspectRatio();
        dt.testBitmapGray();
    }
}
