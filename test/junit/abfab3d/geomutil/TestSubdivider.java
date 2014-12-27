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

import static abfab3d.util.Output.printf;
import static abfab3d.util.Units.MM;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;


/**
 * Tests the functionality of ParametricSurfaceMaker
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestSubdivider extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSubdivider.class);
    }

    public void testDumb(){
        //this test here is to make Test happy. 
    }

    /**
     * 
     */
    public static void makePolyline() {

	double EPS = 0.1;

        Vector<Vector3d> polyline = new Vector<Vector3d>();
        polyline.add(new Vector3d(0,0,0));
        polyline.add(new Vector3d(1,0,0));
        polyline.add(new Vector3d(1,1,0));
        polyline.add(new Vector3d(1,1,1));

        Vector<Vector3d> points = Subdivider.subdividePolyline(polyline, EPS);
        for(int i = 0; i < points.size(); i++){
            Vector3d p = points.get(i);
            printf("%6.3f %6.3f %6.3f\n",p.x,p.y,p.z);
        }

    }

    public static void makePolyline1() {

	double EPS = 0.1;

        Vector polyline = new Vector();
        polyline.add(new Vector2d(0,0));
        polyline.add(new Vector2d(1,0));
        polyline.add(new Vector2d(1,1));
        polyline.add(new Vector2d(0,1));

        Vector<Vector2d> points = Subdivider.subdividePolyline(polyline, 5, new InterpolatorVector2d(EPS));
        for(int i = 0; i < points.size(); i++){
            Vector2d p = (Vector2d)points.get(i);
            printf("%6.3f %6.3f\n",p.x,p.y);
        }

    }

    public static void makePolyquad() {

	double EPS = 0.1;

        Vector<Vector3d> polyline = new Vector<Vector3d>();
        polyline.add(new Vector3d(1,0,0));
        polyline.add(new Vector3d(0,0,0));
        polyline.add(new Vector3d(0,1,0));

        Vector<Vector3d> points = Subdivider.subdividePolyquad(polyline, EPS);
        for(int i = 0; i < points.size(); i++){
            Vector3d p = points.get(i);
            printf("%6.3f %6.3f %6.3f\n",p.x,p.y,p.z);
        }

    }

    public static void makePolyquad2() {

	double EPS = 0.1;

        Vector<Vector2d> polyline = new Vector<Vector2d>();
        polyline.add(new Vector2d(1,0));
        polyline.add(new Vector2d(0,0));
        polyline.add(new Vector2d(0,1));

        Vector<Vector2d> points = Subdivider.subdividePolyquad(polyline, EPS);
        for(int i = 0; i < points.size(); i++){
            Vector2d p = points.get(i);
            printf("%6.3f %6.3f\n",p.x,p.y);
        }

    }

    public static void makePolycubic() {

	double EPS = 0.01;

        Vector<APnt> polyline = new Vector<APnt>();
        polyline.add(new Pnt3(1,0,0));
        polyline.add(new Pnt3(1,1,0));
        polyline.add(new Pnt3(1,1,0));
        polyline.add(new Pnt3(0,1,0));

        Vector<APnt> points = Subdivider.subdividePolycubic(polyline, EPS);
        for(int i = 0; i < points.size(); i++){
            Pnt3 p = (Pnt3)points.get(i);
            printf("%6.3f %6.3f %6.3f\n",p.x,p.y,p.z);
        }

    }

    public static void main(String[] arg) throws Exception {

        //makePolyline();
        //makePolyline1();
        //makePolyquad();
        makePolyquad2();
        //makePolycubic();

    }
}
