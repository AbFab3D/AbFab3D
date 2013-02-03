/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.mesh;

// External Imports

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.Iterator;

/**
 * Tests the functionality of IndexedTriangleSetBuilder
 *
 * @author Alan Hudson
 * @version
 */
public class TestIndexedTriangleSetBuilder extends TestCase {
    
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestIndexedTriangleSetBuilder.class);
    }

    public void testBasic() {
        IndexedTriangleSetBuilder its1 = new IndexedTriangleSetBuilder();

        Vector3d[] points = new Vector3d[] {
                new Vector3d(0,0,0), new Vector3d(1,0,0), new Vector3d(0,1,0),
                new Vector3d(0,0,0), new Vector3d(1,0,0), new Vector3d(-1,0,0),
                new Vector3d(0,0,0), new Vector3d(-1,0,0), new Vector3d(0,-1,0),
                new Vector3d(0,0,0), new Vector3d(0,-1,0), new Vector3d(1,0,0)
        };

        for(int i=0; i < points.length / 3; i++) {
            its1.addTri(points[i*3], points[i*3+1], points[i*3+2]);
        }

        double[] verts1 = its1.getVertices();

        assertEquals("Vertex length", 5*3, verts1.length);

        int[] faces1 = its1.getFaces();
        System.out.println("Faces: " + java.util.Arrays.toString(faces1));
        assertEquals("Face length", 4*3, faces1.length);
        for(int i=0; i < faces1.length / 3; i++) {
            System.out.println(faces1[i*3+0] + "," + faces1[i*3 + 1] + "," + faces1[i*3 + 2]);
        }
    }

}

