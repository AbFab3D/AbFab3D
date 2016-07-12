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
public class TestCylinder extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestCylinder.class);
    }

    public void testCylinderDistance() {

        printf("testCylinderDistance()\n");
        Vector3d v0 = new Vector3d(0,0,0);
        Vector3d v1 = new Vector3d(1,1,0);
        double r0 = Math.sqrt(2);
        double r1 = 0.;
        Cylinder shape = new Cylinder(v0, v1, r0, r1); 
        shape.set("rounding", 0.);
        printf("cylinder: v0:(%7.5f,%7.5f,%7.5f) v1:(%7.5f,%7.5f,%7.5f),r0: %7.5f, r1: %7.5f\n", v0.x,v0.y,v0.z,v1.x,v1.y,v1.z,r0, r1);
        shape.setDataType(DataSource.DATA_TYPE_DISTANCE);
        shape.initialize();
        Vec pnt = new Vec(3);
        Vec data = new Vec(3);
        double coord[][] = new double[][]{{0,0,0, 0}, // v0
                                          {1,1,0, 0}, // v1
                                          {0.1,0.1,0., -0.1*Math.sqrt(2)}, // inside near v0
                                          {1.1,1.1,0, 0.1*Math.sqrt(2)},   // outside near v1
                                          {0.5,0.5, 0, -0.5}, // center 
                                          {0.9,0.9, 0, -0.1}, // inside near v1
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

    public void testCylinderDensity() {

        printf("testCylinderDensity()\n");

        Vector3d v0 = new Vector3d(0,0,0);
        Vector3d v1 = new Vector3d(1,1,0);
        double r0 = 1;
        double r1 = 0.5;
        Cylinder shape = new Cylinder(v0, v1, r0, r1); 
        printf("cylinder: v0:(%7.5f,%7.5f,%7.5f) v1:(%7.5f,%7.5f,%7.5f),r0: %7.5f, r1: %7.5f\n", v0.x,v0.y,v0.z,v1.x,v1.y,v1.z,r0, r1);
        shape.setDataType(DataSource.DATA_TYPE_DENSITY);
        shape.initialize();
        double vs = 0.01;
        Vec pnt = new Vec(3);
        pnt.voxelSize = vs;
        Vec data = new Vec(3);
        double coord[][] = new double[][]{{0,0,0, 0.5},{1,1,0, 0.5}, {0.1,0.1,0., 1},{1.1,1.1,0, 0},{0.9,0.9, 0, 1},{0.9,1.1, 0, 0.5}};
        
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

    static final double EPS = 1.e-7;

    public static void main(String[] args) {
        new TestCylinder().testCylinderDistance();
        new TestCylinder().testCylinderDensity();
    }

}