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

package abfab3d.transforms;

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

import abfab3d.util.Vec;
import abfab3d.util.MathUtil;
import abfab3d.util.TextUtil;
import abfab3d.util.Symmetry;
import abfab3d.util.VecTransform;

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

/**
 * Tests the functionality of PeriodicWrap
 *
 * @version
 */
public class TestPeriodicWrap extends TestCase {

    
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestPeriodicWrap.class);
    }

    public void testSimple() {

        //PeriodicWrap pw = new PeriodicWrap(new Vector3d(1,0,0),new Vector3d(0,2,0),new Vector3d(0,0,3));
        PeriodicWrap pw = new PeriodicWrap(new Vector3d(1,1,0),new Vector3d(-1,1,0),new Vector3d(0,0,1));
        pw.initialize();

        Vec v = new Vec(0.1,2.,7.3);
        Vec out = new Vec(3);

        pw.inverse_transform(v, out);

        printf("(%7.5f,%7.5f,%7.5f) ->  (%7.5f,%7.5f,%7.5f)\n", v.v[0],v.v[1],v.v[2], out.v[0],out.v[1],out.v[2]);

    }
   
    public static void main(String[] args) {
        new TestPeriodicWrap().testSimple();
    }
}