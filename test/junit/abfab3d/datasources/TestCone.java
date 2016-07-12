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
public class TestCone extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestCone.class);
    }

    public void testConeDistance() {

        printf("testConeDistance()\n");
        Vector3d apex = new Vector3d(1,1,0);
        Vector3d axis = new Vector3d(-1,-1,0);
        double angle = Math.PI/4;
        Cone shape = new Cone(apex, axis, angle); 
        shape.set("rounding", 0.);
        printf("cone: apex:(%7.5f,%7.5f,%7.5f) axis:(%7.5f,%7.5f,%7.5f),angle: %7.5f deg\n", apex.x,apex.y,apex.z,axis.x,axis.y,axis.z,180*angle/Math.PI);
        shape.setDataType(DataSource.DATA_TYPE_DISTANCE);
        shape.initialize();
        Vec pnt = new Vec(3);
        Vec data = new Vec(3);
        double coord[][] = new double[][]{{0,0,0, -1}, // inside
                                          {1,1,0, 0}, // apex
                                          {1,0.5,0, 0}, // surface
                                          {0.9,0.5,0, -0.1}, // inside
                                          {1.1,0.5,0, 0.1}, // outside
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

    public void testConeDensity() {

        printf("testConeDensity()\n");

        double angle = Math.PI/4;
        Vector3d apex = new Vector3d(1,1,0);
        Vector3d axis = new Vector3d(-1,-1,0);
        Cone shape = new Cone(apex, axis, angle); 
        shape.set("rounding", 0.);
        printf("cone: apex:(%7.5f,%7.5f,%7.5f) axis:(%7.5f,%7.5f,%7.5f),angle: %7.5f deg\n", apex.x,apex.y,apex.z,axis.x,axis.y,axis.z,180*angle/Math.PI);
        shape.setDataType(DataSource.DATA_TYPE_DENSITY);
        shape.initialize();
        double vs = 0.01;
        Vec pnt = new Vec(3);
        pnt.voxelSize = vs;
        Vec data = new Vec(3);
        double coord[][] = new double[][]{{0,0,0, 1}, // inside
                                          {1,1,0, 0.5}, // apex
                                          {1,0.5,0,0.5}, // surface
                                          {0.9,0.5,0, 1.}, // inside
                                          {1.1,0.5,0, 0.}, // outside
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
        //new TestCone().testConeDistance();
        new TestCone().testConeDensity();
    }

}