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
 * Tests the functionality of Complement
 *
 * @version
 */
public class TestComplement extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestComplement.class);
    }

    public void testComplementDistance() {

        printf("testComplementDistance()\n");
        Sphere sphere = new Sphere(new Vector3d(1,0,0), 1.);
        Complement comp = new Complement(sphere);
        comp.initialize();
        Vec pnt = new Vec(3);
        Vec data = new Vec(3);
        double coord[][] = new double[][]{{0,0,0, 0},{0.5,0,0, 0.5},{1.5,0,0, 0.5},{2.5,0,0, -0.5},};
        
        for(int i = 0; i < coord.length; i++){
            double cc[] = coord[i];
            pnt.v[0] = cc[0];
            pnt.v[1] = cc[1];
            pnt.v[2] = cc[2];
            comp.getDataValue(pnt, data);
            printf("pnt: [%7.5f  %7.5f  %7.5f] data: %7.5f expect: %7.5f\n", cc[0], cc[1], cc[2], data.v[0], cc[3]);            
            assertTrue(fmt("data.v[0]==cc[3]: %18.15e != %18.15e",data.v[0], cc[3]),Math.abs(data.v[0] - cc[3]) < EPS);
        }
    }

    public void testComplementDensity() {

        printf("testComplementDensity()\n");
        double voxelSize = 0.1;
        Sphere sphere = new Sphere(new Vector3d(1,0,0), 1.);
        sphere.setDataType(DataSource.DATA_TYPE_DENSITY);
        Complement comp = new Complement(sphere);
        comp.setDataType(DataSource.DATA_TYPE_DENSITY);
        comp.initialize();
        Vec pnt = new Vec(3);
        pnt.setVoxelSize(voxelSize);
        Vec data = new Vec(3);
        double coord[][] = new double[][]{{0,0,0, 0.5},{0.5,0,0, 0},{1.5,0,0, 0},{2.5,0,0, 1.},{voxelSize/2,0,0, 0.25},{-voxelSize/2,0,0, 0.75}};
        
        for(int i = 0; i < coord.length; i++){
            double cc[] = coord[i];
            pnt.v[0] = cc[0];
            pnt.v[1] = cc[1];
            pnt.v[2] = cc[2];
            comp.getDataValue(pnt, data);
            printf("pnt: [%8.5f  %8.5f  %8.5f] data: %8.5f expect: %8.5f\n", cc[0], cc[1], cc[2], data.v[0], cc[3]);            
            assertTrue(fmt("data.v[0]==cc[3]: %18.15e != %18.15e",data.v[0], cc[3]),Math.abs(data.v[0] - cc[3]) < EPS);
            
        }
    }

    static final double EPS = 1.e-12;

    public static void main(String[] args) {
        new TestComplement().testComplementDistance();
        new TestComplement().testComplementDensity();
    }

}