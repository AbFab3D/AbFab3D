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
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.GridShortIntervals;

import abfab3d.util.Vec;
import abfab3d.util.MathUtil;
import abfab3d.util.TextUtil;
import abfab3d.util.Symmetry;
import abfab3d.util.VecTransform;

import abfab3d.datasources.Box;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Ring;
import abfab3d.datasources.ImageBitmap;
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
public class TestImageBitmap extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestImageBitmap.class);
    }

    int gridMaxAttributeValue = 127;

    public void testBitmap() {

        printf("testBitmap()\n");
        
    }


    static void testLinearMapper(){

        LinearMapper mapper = new LinearMapper(-1000, 1000, -0.001, 0.001);

        double vmin = mapper.getVmin();
        double vmax = mapper.getVmax();
        
        printf("%vmin: %f vmax: %f\n", vmin, vmax);

        for(int i = -2000; i < 2000; i += 100) {
            long att = i;
            double v = mapper.map(att);
            int vi = (int)(255*((v - vmin)/(vmax - vmin)))&0xFF;
            byte vb = (byte)vi;
            int vii = (vb) & 0xFF;
            double vv = vii*(vmax - vmin)/255 + vmin;
            printf("%8x %5d -> v:%9.5f vi:%4d vb:%4d vii:%4d vv: %9.5f\n", i, att, v*1000, vi, vb, vii, vv*1000);
        }
    }
    
    public static void main(String[] args) {
        testLinearMapper();
    }
}