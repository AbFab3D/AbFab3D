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

package abfab3d.geomutil;

// External Imports
import java.util.Vector;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector2d;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
import abfab3d.util.PointSetArray;

import static abfab3d.core.Output.printf;
import static abfab3d.core.MathUtil.str;


/**
 *
 * @version
 */
public class TestTriangles extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTriangles.class);
    }

    public void testDumb(){
        //this test here is to make Test happy. 
    }


    void devTestTriangle(){
        /*
        Vector3d v0 = new Vector3d(-31.711105, -5.934341, 6.293507);
        Vector3d v1 = new Vector3d(-31.711105, -6.167607, 6.293507);
        Vector3d v2 = new Vector3d(-31.724144, -6.167607, 6.249674);
        Vector3d u0 = new Vector3d(-31.711105, -6.517508, 6.293507);
        Vector3d u1 = new Vector3d(-31.724144, -6.400874, 6.249674);
        Vector3d u2 = new Vector3d(-31.711105, -6.400874, 6.293507);
        */
        Vector3d v0 = new Vector3d(0.003008513478562236, 0.019930001348257065, -9.187476098304614E-5);
        Vector3d v1 = new Vector3d(0.003000000026077032, 0.020170001313090324, 5.230851862371355E-19);
        Vector3d v2 = new Vector3d(0.003008513478562236, 0.020170001313090324, -9.187476098304614E-5);
        Vector3d u0 = new Vector3d(0.003008513478562236, 9.300000383518636E-4, -9.187476098304614E-5);
        Vector3d u1 = new Vector3d(0.003000000026077032, 0.0011700000613927841, 5.230851862371355E-19);
        Vector3d u2 = new Vector3d(0.003008513478562236, 0.0011700000613927841, -9.187476098304614E-5);

        String f = "%18.15f";

        printf("getNormal(t1)\n");
        Vector3d nu = getNormal(v0, v1, v2);
        printf("getNormal(t2)\n");

        Vector3d nv = getNormal(u0, u1, u2);
        printf("nu:%s\nnv:%s\n", str(f,nu),str(f,nv));
        
        double n12 = nu.dot(nv);
        Vector3d c12 = new Vector3d();
        c12.cross(nu, nv);

        printf("c12:%s\n", str(f,c12));
        
        printf("dv0:%18.15f  dv1:%18.15f dv2:%18.15f du0:%18.15f du1:%18.15f du2:%18.15f\n", 
               nu.dot(v0),nu.dot(v1),nu.dot(v2),nu.dot(u0),nu.dot(u1),nu.dot(u2));
        
        nu.sub(nv);
        printf("nu-nv: %s\n", str("%10.3e",nu));
        
    }

    static Vector3d getNormal(Vector3d u0,Vector3d u1,Vector3d u2 ){

        Vector3d v1 = new Vector3d(u1);
        Vector3d v2 = new Vector3d(u2);
        String f = "%10.7f";

        v1.sub(u0);
        v2.sub(u0);
        //printf("w1:%s w2:%s\n", str(f,v1),str(f,v2));
        v1.normalize();
        v2.normalize();
        printf("normalized:\n");
        printf("w1:%s w2:%s\n", str(f,v1),str(f,v2));

        double cosa = v1.dot(v2);
        Vector3d v12 = new Vector3d();
        v12.cross(v1,v2);
        double sina = v12.length();
        v12.normalize();
        return v12;
    }

    public static void main(String[] arg) throws Exception {

        new TestTriangles().devTestTriangle();

    }
}
