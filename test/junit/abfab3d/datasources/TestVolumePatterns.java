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


// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Vector3d;

// Internal Imports

import abfab3d.core.Vec;
import abfab3d.core.DataSource;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;

/**
 * Tests the functionality of Sphere
 *
 * @version
 */
public class TestVolumePatterns extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestVolumePatterns.class);
    }

    public void testGyroidDistance() {

        printf("testGyroidDistance()\n");
        double period = 1;
        double thickness = 0.1;        
        VolumePatterns.Gyroid shape = new VolumePatterns.Gyroid(period, thickness); 
        shape.setDataType(DataSource.DATA_TYPE_DISTANCE);
        shape.initialize();
        Vec pnt = new Vec(3);
        Vec data = new Vec(3);
        double f = Math.PI*2/period;
        double p = period;
        double e = (Math.asin(2*f*0.1/3))/(2*f);
        double coord[][] = new double[][]{{0,0,0, -0.1}, // inside
                                          {p,0,0, -0.1}, // inside
                                          {0,p,0, -0.1}, // inside
                                          {0.1,0.1,0.1, 0.1270480185921971}, // outside
                                          {-0.1,-0.1,-0.1, 0.1270480185921971}, // outside
                                          {e,e,e, 0}, // surface
                                          };
        
        for(int i = 0; i < coord.length; i++){
            double cc[] = coord[i];
            pnt.v[0] = cc[0];
            pnt.v[1] = cc[1];
            pnt.v[2] = cc[2];
            shape.getDataValue(pnt, data);
            printf("pnt: [%8.5f %8.5f %8.5f] data: %8.5f expect: %8.5f\n", cc[0], cc[1], cc[2], data.v[0], cc[3]);            
            assertTrue(fmt("data.v[0]==cc[3]: %18.15e != %18.15e",data.v[0], cc[3]),Math.abs(data.v[0] - cc[3]) < EPS);
        }
    }

    public void testGyroidDensity() {

        printf("testGyroidDensity()\n");

        double period = 1;
        double thickness = 0.1;        
        VolumePatterns.Gyroid shape = new VolumePatterns.Gyroid(period, thickness); 
        shape.setDataType(DataSource.DATA_TYPE_DENSITY);
        shape.initialize();
        double vs = 0.01;
        Vec pnt = new Vec(3);
        pnt.voxelSize = vs;
        Vec data = new Vec(3);
        double f = Math.PI*2/period;
        double p = period;
        double e = (Math.asin(2*f*0.1/3))/(2*f);
        double coord[][] = new double[][]{{0,0,0, 1}, // inside
                                          {p,0,0, 1}, // inside
                                          {0,p,0, 1}, // inside
                                          {0.1,0.1,0.1, 0}, // outside
                                          {-0.1,-0.1,-0.1, 0}, // outside
                                          {e,e,e, 0.5}, // surface
                                          };
        
        for(int i = 0; i < coord.length; i++){
            double cc[] = coord[i];
            pnt.v[0] = cc[0];
            pnt.v[1] = cc[1];
            pnt.v[2] = cc[2];
            shape.getDataValue(pnt, data);
            printf("pnt: [%8.5f %8.5f %8.5f] data: %8.5f expect: %8.5f\n", cc[0], cc[1], cc[2], data.v[0], cc[3]);            
            //assertTrue(fmt("data.v[0]==cc[3]: %18.15e != %18.15e",data.v[0], cc[3]),Math.abs(data.v[0] - cc[3]) < EPS);
        }
   }

    static final double EPS = 1.e-7;

    public static void main(String[] args) {
        //new TestVolumePatterns().testGyroidDistance();
        new TestVolumePatterns().testGyroidDensity();
    }

}