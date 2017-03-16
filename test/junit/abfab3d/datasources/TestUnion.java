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
import abfab3d.transforms.Translation;
import abfab3d.transforms.Rotation;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;

/**
 * Tests the functionality of Union
 *
 */
public class TestUnion extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestUnion.class);
    }

    public void testUnionDistance() {

        printf("testUnionDistance()\n");
        Sphere s1 = new Sphere(new Vector3d(1,0,0), 1.);
        Sphere s2 = new Sphere(new Vector3d(-1,0,0), 1.);
        Union union = new Union(s1, s2);
        union.set("blend", 0.1);
        union.initialize();
        Vec pnt = new Vec(3);
        Vec data = new Vec(3);
        double coord[][] = new double[][]{{0,0,0, -0.025},{0.5,0,0, -0.5},{1.5,0,0, -0.5},{2.5,0,0, 0.5},{-2.5,0,0, 0.5},};
        
        for(int i = 0; i < coord.length; i++){
            double cc[] = coord[i];
            pnt.v[0] = cc[0];
            pnt.v[1] = cc[1];
            pnt.v[2] = cc[2];
            union.getDataValue(pnt, data);
            printf("pnt: [%7.5f  %7.5f  %7.5f] data: %7.5f expect: %7.5f\n", cc[0], cc[1], cc[2], data.v[0], cc[3]);            
            assertTrue(fmt("data.v[0]==cc[3]: %18.15e != %18.15e",data.v[0], cc[3]),Math.abs(data.v[0] - cc[3]) < EPS);
        }
    }

    public void testUnionDensity() {

        printf("testUnionDensity()\n");
        Sphere s1 = new Sphere(new Vector3d(1,0,0), 1.);
        Sphere s2 = new Sphere(new Vector3d(-1,0,0), 1.);
        Union union = new Union(s1, s2);

        union.set("blend", 0.1);

        s1.setDataType(DataSource.DATA_TYPE_DENSITY);
        s2.setDataType(DataSource.DATA_TYPE_DENSITY);
        union.setDataType(DataSource.DATA_TYPE_DENSITY);

        union.initialize();
        Vec pnt = new Vec(3);
        double voxelSize = 0.1;
        pnt.setVoxelSize(voxelSize);     
        Vec data = new Vec(3);
        double coord[][] = new double[][]{{0,0,0, 0.5},{0.5,0,0, 1},{1.5,0,0, 1},{2.,0,0, 0.5},{-2.,0,0, 0.5},};
        
        for(int i = 0; i < coord.length; i++){
            double cc[] = coord[i];
            pnt.v[0] = cc[0];
            pnt.v[1] = cc[1];
            pnt.v[2] = cc[2];
            union.getDataValue(pnt, data);
            printf("pnt: [%7.5f  %7.5f  %7.5f] data: %7.5f expect: %7.5f\n", cc[0], cc[1], cc[2], data.v[0], cc[3]);            
            assertTrue(fmt("data.v[0]==cc[3]: %18.15e != %18.15e",data.v[0], cc[3]),Math.abs(data.v[0] - cc[3]) < EPS);
        }
    }

    static final double EPS = 1.e-12;

    void devTestParamString(){
        Sphere s = new Sphere(1);
        Torus t = new Torus(1, 0.5);
        s.addTransform(new Rotation(1,2,3,4));
        s.addTransform(new Rotation(3,4,5,6));
        t.addTransform(new Rotation(3,4,5,6));
        t.addTransform(new Rotation(3,4,5,6));
        Union u = new Union(s, t);
        u.setTransform(new Translation(1,2,3));
        u.addTransform(new Rotation(1,2,3,4));
        u.initialize();
        
        String str = u.getParamString();
        printf("paramString:\n%s\n", str);
    }


    public static void main(String[] args) {
        //new TestUnion().testUnionDistance();
        //new TestUnion().testUnionDensity();
        new TestUnion().devTestParamString();

    }

}