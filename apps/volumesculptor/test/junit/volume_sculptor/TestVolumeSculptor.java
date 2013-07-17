/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package volume_sculptor;

// External Imports

import abfab3d.mesh.TriangleMesh;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;
import volumesculptor.shell.Main;

import java.io.File;

/**
 * Tests the functionality of a VolumeSculptor
 *
 * @author Alan Hudson
 */
public class TestVolumeSculptor extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestVolumeSculptor.class);
    }

    public void testSphere() {
        String script = "importPackage(Packages.abfab3d.grid.op);\n" +
                "importPackage(Packages.abfab3d.grid);\n" +
                "importPackage(Packages.abfab3d.datasources);\n" +
                "\n" +
                "function main(radius) {\n" +
                "\tvar grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,.1*MM);\n" +
                "\tvar sphere = new Sphere(radius);\n" +
                "\tmaker.setDataSource(sphere);\n" +
                "\tmaker.makeGrid(grid);\n" +
                "\t\n" +
                "\treturn grid;\n" +
                "}";

        String[] files = new String[0];
        String[] params = new String[] {"0.005"};


        try {
            File f = File.createTempFile("script","vss");

            String[] args = new String[] {f.toString()};
            FileUtils.write(f,script);

            TriangleMesh mesh = Main.execMesh(args, files, params);

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }
    public void testGyroid() {
        String script = "importPackage(Packages.abfab3d.grid.op);\n" +
                "importPackage(Packages.abfab3d.grid);\n" +
                "importPackage(Packages.abfab3d.datasources);\n" +
                "\n" +
                "function main(baseFile) {\n" +
                "\n" +
                "\tvar grid = load(baseFile);      \n" +
                "\tvar intersect = new Intersection();\n" +
                "\tintersect.addDataSource(new DataSourceGrid(grid, 255));\n" +
                "\tintersect.addDataSource(new VolumePatterns.Gyroid(10*MM, 1*MM));\n" +
                "\n" +
                "\tmaker.setDataSource(intersect);\n" +
                "\n" +
                "\tvar dest = createGrid(grid);\n" +
                "\tmaker.makeGrid(dest);\n" +
                "\t\n" +
                "\treturn grid;\n" +
                "}";

        String[] files = new String[] {"C:\\cygwin\\home\\giles\\projs\\abfab3d\\code\\trunk\\apps\\volumesculptor\\models\\sphere.stl"};
        String[] params = new String[] {};


        try {
            File f = File.createTempFile("script","vss");

            String[] args = new String[] {f.toString()};
            FileUtils.write(f,script);

            TriangleMesh mesh = Main.execMesh(args, files, params);

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }
}
