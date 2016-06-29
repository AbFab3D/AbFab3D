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

import abfab3d.geom.TriangulatedModels;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;

/**
 * Tests the functionality of AreaCalculator
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestTriangleQualityCalculator extends TestCase {
    
    //static final double MM = 1000; // m -> mm conversion TestLaplasianSmooth
    //static final double MM3 = 1.e9; // m^3 -> mm^3 conversion 

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTriangleQualityCalculator.class);
    }

    static final double EPSILON = 1.e-14;

    public void testOctahedronQuality(){
        
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
        TriangleQualityCalculator tq = new TriangleQualityCalculator(10);
        
        for(int i = 0; i < 8; i++){
            tq.addTri(v[tri[3*i]],v[tri[3*i+1]],v[tri[3*i+2]]);
        }
        tq.printStat();
        
        
        assertTrue("min Quality of octahedron ",Math.abs(tq.getMinQuality() - 1.0) < EPSILON);
        assertTrue("max Quality of octahedron ",Math.abs(tq.getMaxQuality() - 1.0) < EPSILON);
        assertTrue("average Quality of octahedron ",Math.abs(tq.getAverageQuality() - 1.0) < EPSILON);
        
    }
    
    public void testSphereQuality(){
        
        TriangulatedModels.Sphere s = new  TriangulatedModels.Sphere(10*MM, new Vector3d(0,0,0), 10, 0.01*MM);

        TriangleQualityCalculator tq = new TriangleQualityCalculator(10);

        s.getTriangles(tq);
        //tq.printStat();
        double actual = tq.getMinQuality();
        double expected = 0.577698095806363;
        assertTrue(fmt("min quality of sphere. Expected value: %18.15f Actual value: %18.15f",expected, actual),Math.abs(actual - expected) < EPSILON);
    }

    
    public static void main(String arg[]){
        //new TestTriangleQualityCalculator().testOctahedronQuality();
        new TestTriangleQualityCalculator().testSphereQuality();
    }
        
}

