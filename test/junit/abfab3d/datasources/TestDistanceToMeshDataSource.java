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
import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.AttributeGrid;

import abfab3d.util.ColorMapperDistance;
import abfab3d.util.ColorMapper;
import abfab3d.geom.Octahedron;


import abfab3d.grid.op.GridMaker;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;

/**
 * Tests the functionality of Sphere
 *
 * @version
 */
public class TestDistanceToMeshDataSource extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSphere.class);
    }

    public void testNothing() {
        printf("testNothing()\n");
    }


    void devTestOcta() throws Exception {
        
        Octahedron octa = new Octahedron(10*MM);
        
        DistanceToMeshDataSource dmds = new DistanceToMeshDataSource(octa);
        dmds.set("margins", 5*MM);
        dmds.initialize();

        double s = 15*MM;


        int N = 30;
        Vec pnt = new Vec(3);
        Vec data = new Vec(3);

        double  y = 0, z = 0;
        for(int i = 0; i <= N; i++){
            double x = -s + 2*i*s/N;
            pnt.v[0] = x;
            pnt.v[1] = y;
            pnt.v[2] = z;
            dmds.getDataValue(pnt, data);
            printf("(%7.3f, %7.3f, %7.3f) -> %7.3f \n", x, y,z, data.v[0]);
        }

        
    }


    public static void main(String[] args) throws Exception {

        new TestDistanceToMeshDataSource().devTestOcta();
        
    }

}