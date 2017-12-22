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

package abfab3d.util;

// External Imports
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Vector3d;

// Internal Imports
import abfab3d.util.TrianglePrinter;
import abfab3d.io.output.STLWriter;
import abfab3d.io.output.X3DWriter;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.fmt;
import static java.lang.Math.sin;
import static java.lang.Math.cos;


/**
 * Tests the functionality of TrianglulatedSphere
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestTriangulatedSphere extends TestCase {

    /**+
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTriangulatedSphere.class);
    }


    public void testDumb(){
        //this test here is to make Test happy. 
    }


    public static void devTestMakeSTLSphere()throws Exception {
        
        STLWriter stl = new STLWriter("/tmp/sphere_10cm.stl");
        
        int n = 5;

        TriangulatedSphere s = new  TriangulatedSphere(50*MM, new Vector3d(0,0,0), n);
        s.setTolerance(0.001*MM);

        s.getTriangles(stl);

        stl.close();              

    }

    public static void devTestMakeX3DVSphere()throws Exception {
        
        X3DWriter wrl = new X3DWriter("/tmp/sphere_7.x3dv", "texture.png");
        
        TriangulatedSphere s = new  TriangulatedSphere(500*MM, new Vector3d(0,0,0), 7);
        s.setTolerance(0.05*MM);

        //s.getTriangles(wrl);
        s.getAttTriangles(wrl);

        wrl.close();              

    }


    public static void main(String[] arg) throws Exception {

        new TestTriangulatedSphere().devTestMakeX3DVSphere();        

    }
}
