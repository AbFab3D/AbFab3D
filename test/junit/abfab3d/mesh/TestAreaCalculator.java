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

import static abfab3d.util.Output.printf; 
import static abfab3d.util.Output.fmt; 

import static java.lang.System.currentTimeMillis;

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

        printf("octahedron area      exact: %17.15f\n", area);
        printf("octahedron area calculated: %17.15f\n", av[0]);
        printf("octahedron volume      exact: %17.15f\n", volume);
        printf("octahedron volume calculated: %17.15f\n", av[1]);

        assertTrue("area of octahedron calculation",Math.abs(area - av[0]) < EPSILON);
        assertTrue("volume of octahedron calculation",Math.abs(volume - av[1]) < EPSILON);

    }
        
}

