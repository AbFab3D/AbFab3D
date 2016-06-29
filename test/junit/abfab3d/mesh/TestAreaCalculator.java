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
import javax.vecmath.Vector3d;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import static abfab3d.core.Output.printf;

/**
 * Tests the functionality of AreaCalculator
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestAreaCalculator extends TestCase {
    
    static final double MM = 1000; // m -> mm conversion TestLaplasianSmooth
    static final double MM3 = 1.e9; // m^3 -> mm^3 conversion 

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestAreaCalculator.class);
    }

    static final double EPSILON = 1.e-14;

    public void testGenericAreaAndVolume() throws Exception {
        
        // octahedron 
        Vector3d v[]
            = new Vector3d[]{
                new Vector3d(1,  0, 0),
                new Vector3d(-1, 0, 0),
                new Vector3d(0,  1, 0),
                new Vector3d(0, -1, 0),
                new Vector3d(0, 0,  1),
                new Vector3d(0, 0, -1),
            };
        int tri[] = new int[]{
            0,2,4, 
            5,2,0,
            1,2,5,
            4,2,1,
            0,4,3,
            5,0,3,
            1,5,3,
            4,1,3,            
        };
        AreaCalculator ac = new AreaCalculator();
        
        for(int i = 0; i < 8; i++){
            ac.addTri(v[tri[3*i]],v[tri[3*i+1]],v[tri[3*i+2]]);
        }

        double area = 4*Math.sqrt(3);
        double volume = 4./3.;

        printf("generic area calculation\n");
        printf("area      exact: %17.15f\n", area);
        printf("area calculated: %17.15f\n", ac.getArea());
        printf("volume      exact: %17.15f\n", volume);
        printf("volume calculated: %17.15f\n", ac.getVolume());

        assertTrue("area of octahedron calculation",Math.abs(area - ac.getArea()) < EPSILON);
        assertTrue("volume of octahedron calculation",Math.abs(volume - ac.getVolume()) < EPSILON);
        

    }

    public void testOctahedronAreaAndVolume() throws Exception {
        // octahedron 
        double coords[]
            = new double[]{
                1,  0, 0,   
                -1, 0, 0,
                0,  1, 0,
                0, -1, 0,
                0, 0,  1,
                0, 0, -1,
            };
        int tri[] = new int[]{
            0,2,4, 
            5,2,0,
            1,2,5,
            4,2,1,
            0,4,3,
            5,0,3,
            1,5,3,
            4,1,3,            
        };
        for(int i = 0; i < 6; i++){
            coords[3*i] += 1.234;
            coords[3*i+1] += 2.34;
            coords[3*i+2] += 3.456;

        }

        double av[] = AreaCalculator.getAreaAndVolume(tri, coords);
        double area = 4*Math.sqrt(3);
        double volume = 4./3.;

        printf("octahedron area calculation\n");
        printf("area      exact: %17.15f\n", area);
        printf("area calculated: %17.15f\n", av[0]);
        printf("volume      exact: %17.15f\n", volume);
        printf("volume calculated: %17.15f\n", av[1]);

        assertTrue("area of octahedron calculation",Math.abs(area - av[0]) < EPSILON);
        assertTrue("volume of octahedron calculation",Math.abs(volume - av[1]) < EPSILON);

    }
        
}

