/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Random;
import abfab3d.io.output.STLWriter;

import static abfab3d.core.Output.printf;


/**


 */
public class TestPointSetArray extends TestCase {

    static final boolean DEBUG = false;

    
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestPointSetArray.class);
    }


    public void testNothing(){

    }

    public void devTestExport() throws Exception {
        PointSetArray ps = new PointSetArray();
        ps.addPoint(1,0,0);
        ps.addPoint(-1,0,0);
        ps.setPointSize(2.);
        ps.setSubdivisionLevel(3);
        ps.setShapeType(PointSetArray.SHAPE_SPHERE);
        STLWriter stl = new STLWriter("/tmp/points.stl");
        ps.getTriangles(stl);
        stl.close();

    }

    public static void main(String arg[])throws Exception {

        new TestPointSetArray().devTestExport();
                
    }
}
