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

import abfab3d.grid.Model;
import abfab3d.grid.ModelWriter;
import abfab3d.io.output.MeshExporter;
import abfab3d.mesh.AreaCalculator;
import abfab3d.util.TriangleMesh;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;
import volumesculptor.shell.ExecResult;
import volumesculptor.shell.Main;

import java.io.File;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.fmt;

/**
 * Tests the functionality of a VolumeSculptor
 *
 * TODO: This class needs to get updated for new system
 *
 * @author Alan Hudson
 */
public class TestVolumeSculptor extends TestCase {
    private static final boolean DEBUG = false;
    private static final String IMGS_DIR =  "images/";
    private static final String MODELS_DIR =  "models/";
    private static final String SCRIPTS_DIR =  "scripts/";

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestVolumeSculptor.class);
    }

    public void testCoinExample() throws Exception {
        System.out.println("Dir: " + new File("foo.txt").getAbsolutePath());
        String[] script_args = new String[] {".001", IMGS_DIR + "r5-bird.png", IMGS_DIR + "r5-circle.png" , IMGS_DIR + "r4-unicorn.png"};

        try {
            File f = new File(SCRIPTS_DIR + "examples/coin_01.vss");

            String[] args = new String[] {f.toString()};

            ExecResult result = Main.execMesh(args, script_args);
            Model model = result.getModel();
            ModelWriter writer = model.getWriter();
            TriangleMesh mesh = writer.getGeneratedMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

            AreaCalculator ac = new AreaCalculator();
            mesh.getTriangles(ac);

            //var width = 38*MM;
            //var height = 38*MM;
            //var layerHeight = 1*MM;

            // If ImageBox worked then the final volume should < 3 full layers of material
            double max_volume = 38 * MM * 38 * MM * 1 * MM * 3;

            assertTrue("Volume", ac.getVolume() < max_volume * 0.9);
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }

    public void _testLightswitchExample() throws Exception {
        String[] script_args = new String[] {MODELS_DIR + "Light_Switch_Plate1.stl",IMGS_DIR + "chinese_lightswitch.png"};

        try {
            File f = new File(SCRIPTS_DIR + "examples/lightswitch.vss");

            assertTrue("Script not found", f.exists());
            String[] args = new String[] {f.toString()};

            ExecResult result = Main.execMesh(args, script_args);
            Model model = result.getModel();
            ModelWriter writer = model.getWriter();
            TriangleMesh mesh = writer.getGeneratedMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

            AreaCalculator ac = new AreaCalculator();
            mesh.getTriangles(ac);

            //var width = 38*MM;
            //var height = 38*MM;
            //var layerHeight = 1*MM;

            double expected_volume = 2.12E-5;

            // I can't see the volume of this changing more then 10% without something being broken
            double volume = ac.getVolume();
            double diff = Math.abs(volume - expected_volume);

            if (DEBUG) {
                MeshExporter exporter = new MeshExporter();
                exporter.writeMesh(mesh, "/tmp/lightswitch.x3db");
            }
            assertTrue(fmt("wrong volume: expected:%9.7f got:%9.7f",expected_volume, volume), diff < (expected_volume * 0.10));
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }

    public void _testRingExample() throws Exception {
        String[] script_args = new String[] {IMGS_DIR + "sw_logo.png"};

        try {
            File f = new File(SCRIPTS_DIR + "examples/ring_06.vss");

            String[] args = new String[] {f.toString()};

            ExecResult result = Main.execMesh(args, script_args);
            Model model = result.getModel();
            ModelWriter writer = model.getWriter();
            TriangleMesh mesh = writer.getGeneratedMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

            AreaCalculator ac = new AreaCalculator();
            mesh.getTriangles(ac);

            System.out.println("Volume: " + ac.getVolume());
            double expected_volume = 5.818707682613973E-7;

            // I can't see the volume of this changing more then 10% without something being broken
            double diff = Math.abs(ac.getVolume() - expected_volume);

            assertTrue("Volume", diff < (expected_volume * 0.1));
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }

    public void _testDiceExample() throws Exception {
        String[] script_args = new String[] {Double.toString(16 * MM)};

        try {
            File f = new File(SCRIPTS_DIR + "examples/dice.vss");

            String[] args = new String[] {f.toString()};

            ExecResult result = Main.execMesh(args, script_args);
            Model model = result.getModel();
            ModelWriter writer = model.getWriter();
            TriangleMesh mesh = writer.getGeneratedMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

            AreaCalculator ac = new AreaCalculator();
            mesh.getTriangles(ac);

            System.out.println("Volume: " + ac.getVolume());
            double expected_volume = 3.804570609736634E-6;

            // I can't see the volume of this changing more then 10% without something being broken
            double diff = Math.abs(ac.getVolume() - expected_volume);

            assertTrue("Volume", diff < (expected_volume * 0.10));
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }

    public void testGyroidizeExample() throws Exception {
        String[] script_args = new String[] {MODELS_DIR + "sphere.stl"};

        try {
            File f = new File(SCRIPTS_DIR + "examples/gyroidize.vss");

            String[] args = new String[] {f.toString()};

            ExecResult result = Main.execMesh(args, script_args);
            Model model = result.getModel();
            ModelWriter writer = model.getWriter();
            TriangleMesh mesh = writer.getGeneratedMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

            AreaCalculator ac = new AreaCalculator();
            mesh.getTriangles(ac);

            System.out.println("Volume: " + ac.getVolume());
            double expected_volume = 1.8076682897585747E-6;

            // I can't see the volume of this changing more then 10% without something being broken
            double diff = Math.abs(ac.getVolume() - expected_volume);

            assertTrue("Volume", diff < (expected_volume * 0.10));
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }

    public void _testPendantCircularExample() throws Exception {
        String[] script_args = new String[] {IMGS_DIR + "unicursal.png"};

        try {
            File f = new File(SCRIPTS_DIR + "examples/pendant_circular_01.vss");

            String[] args = new String[] {f.toString()};

            ExecResult result = Main.execMesh(args, script_args);
            Model model = result.getModel();
            ModelWriter writer = model.getWriter();
            TriangleMesh mesh = writer.getGeneratedMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

            AreaCalculator ac = new AreaCalculator();
            mesh.getTriangles(ac);

            System.out.println("Volume: " + ac.getVolume());
            double expected_volume = 1.504196129274097E-6;

            // I can't see the volume of this changing more then 10% without something being broken
            double diff = Math.abs(ac.getVolume() - expected_volume);

            assertTrue("Volume", diff < (expected_volume * 0.10));
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }

    public void _testTealightExample() throws Exception {
        String[] script_args = new String[] {IMGS_DIR + "icosa_sphere_part.png"};

        try {
            File f = new File(SCRIPTS_DIR + "examples/tealight.vss");

            String[] args = new String[] {f.toString()};

            ExecResult result = Main.execMesh(args, script_args);
            Model model = result.getModel();
            ModelWriter writer = model.getWriter();
            TriangleMesh mesh = writer.getGeneratedMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

            AreaCalculator ac = new AreaCalculator();
            mesh.getTriangles(ac);

            double expected_volume = 6.9837122827302415E-6;

            // I can't see the volume of this changing more then 10% without something being broken
            double diff = Math.abs(ac.getVolume() - expected_volume);

            assertTrue("Volume", diff < (expected_volume * 0.10));
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }

    public void testSymmetry09Example() throws Exception {
        String[] script_args = new String[] {};

        try {
            File f = new File(SCRIPTS_DIR + "examples/symmetry_09.vss");

            String[] args = new String[] {f.toString()};

            ExecResult result = Main.execMesh(args, script_args);
            Model model = result.getModel();
            ModelWriter writer = model.getWriter();
            TriangleMesh mesh = writer.getGeneratedMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

            AreaCalculator ac = new AreaCalculator();
            mesh.getTriangles(ac);

            System.out.println("Volume: " + ac.getVolume());
            double expected_volume = 4.06281920623781E-6;

            // I can't see the volume of this changing more then 10% without something being broken
            double diff = Math.abs(ac.getVolume() - expected_volume);

            assertTrue("Volume", diff < (expected_volume * 0.1));
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }

    public void _testSphere() {
        String script = "importPackage(Packages.abfab3d.grid.op);\n" +
                "importPackage(Packages.abfab3d.grid);\n" +
                "importPackage(Packages.abfab3d.datasources);\n" +
                "\n" +
                "function main(args) {\n" +
                "\tvar radius = args[0];" +
                "\tvar grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,.1*MM);\n" +
                "\tvar sphere = new Sphere(radius);\n" +
                "\tvar maker = new GridMaker();\n" +
                "\tmaker.setSource(sphere);\n" +
                "\tmaker.makeGrid(grid);\n" +
                "\t\n" +
                "\treturn grid;\n" +
                "}";

        String[] script_args = new String[] {"0.005"};


        try {
            File f = File.createTempFile("script","vss");

            String[] args = new String[] {f.toString()};
            FileUtils.write(f,script);

            ExecResult result = Main.execMesh(args, script_args);
            Model model = result.getModel();
            ModelWriter writer = model.getWriter();
            TriangleMesh mesh = writer.getGeneratedMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }

    public void _testDecimationParams() {
        String script1 = "meshErrorFactor = 0.25;\n" +
                "function main(args) {\n" +
                "\tvar radius = args[0];" +
                "\tvar grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,.1*MM);\n" +
                "\tvar sphere = new Sphere(radius);\n" +
                "\tvar maker = new GridMaker();\n" +
                "\tmaker.setSource(sphere);\n" +
                "\tmaker.makeGrid(grid);\n" +
                "\t\n" +
                "\treturn grid;\n" +
                "}";
        String script2 = "meshErrorFactor = 1;\n" +
                "function main(args) {\n" +
                "\tvar radius = args[0];" +
                "\tvar grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,.1*MM);\n" +
                "\tvar sphere = new Sphere(radius);\n" +
                "\tvar maker = new GridMaker();\n" +
                "\tmaker.setSource(sphere);\n" +
                "\tmaker.makeGrid(grid);\n" +
                "\t\n" +
                "\treturn grid;\n" +
                "}";

        String[] script_args = new String[] {"0.005"};


        int tri_count1 = 0;
        int tri_count2 = 0;

        try {
            File f = File.createTempFile("script","vss");

            String[] args = new String[] {f.toString()};
            FileUtils.write(f,script1);

            ExecResult result = Main.execMesh(args, script_args);
            Model model = result.getModel();
            ModelWriter writer = model.getWriter();
            TriangleMesh mesh = writer.getGeneratedMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);
            tri_count1 = mesh.getFaceCount();
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }

        try {
            File f = File.createTempFile("script","vss");

            String[] args = new String[] {f.toString()};
            FileUtils.write(f,script2);

            ExecResult result = Main.execMesh(args, script_args);
            Model model = result.getModel();
            ModelWriter writer = model.getWriter();
            TriangleMesh mesh = writer.getGeneratedMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);
            tri_count2 = mesh.getFaceCount();
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }

        System.out.println("Tri Count1: " + tri_count1 + " Tri Count2: " + tri_count2);
        assertTrue("tri count not smaller", tri_count1 > tri_count2);
    }

    public void testGyroid() {
        String script = "importPackage(Packages.abfab3d.grid.op);\n" +
                "importPackage(Packages.abfab3d.grid);\n" +
                "importPackage(Packages.abfab3d.datasources);\n" +
                "\n" +
                "function main(args) {\n" +
                "\n" +
                "\tvar baseFile = args[0];\n" +
                "\tvar grid = load(baseFile);      \n" +
                "\tvar intersect = new Intersection();\n" +
                "\tintersect.add(new DataSourceGrid(grid, 255));\n" +
                "\tintersect.add(new VolumePatterns.Gyroid(10*MM, 1*MM));\n" +
                "\n" +
                "\tvar maker = new GridMaker();\n" +
                "\tmaker.setSource(intersect);\n" +
                "\n" +
                "\tvar dest = createGrid(grid);\n" +
                "\tmaker.makeGrid(dest);\n" +
                "\t\n" +
                "\treturn grid;\n" +
                "}";

        File mf = new File(MODELS_DIR);
        String file = mf.getAbsolutePath();
        String[] script_args = new String[] {file + File.separator + "sphere.stl"};

        try {
            File f = File.createTempFile("script","vss");

            String[] args = new String[] {f.toString()};
            FileUtils.write(f,script);

            ExecResult result = Main.execMesh(args, script_args);
            Model model = result.getModel();
            ModelWriter writer = model.getWriter();
            TriangleMesh mesh = writer.getGeneratedMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }

    public void testCompileError() {

        String script = "importPackage(Packages.abfab3d.grid.op);\n" +
                "importPackage(Packages.abfab3d.grid);\n" +
                "importPackage(Packages.abfab3d.datasources);\n" +
                "\n" +
                "function main(baseFile) {\n" +
                "\n" +
                "\tvar baseFile = args[0];\n" +
                "\tvar grid = loadBad(baseFile);      \n" +
                "\tvar intersect = new Intersection();\n" +
                "\tintersect.add(new DataSourceGrid(grid, 255));\n" +
                "\tintersect.add(new VolumePatterns.Gyroid(10*MM, 1*MM));\n" +
                "\n" +
                "\tvar maker = new GridMaker();\n" +
                "\tmaker.setSource(intersect);\n" +
                "\n" +
                "\tvar dest = createGrid(grid);\n" +
                "\tmaker.makeGrid(dest);\n" +
                "\t\n" +
                "\treturn dest;\n" +
                "}";

        File mf = new File(MODELS_DIR);
        String file = mf.getAbsolutePath();
        String[] script_args = new String[] {file + File.separator + "sphere.stl"};

        try {
            File f = File.createTempFile("script","vss");

            String[] args = new String[] {f.toString()};
            FileUtils.write(f,script);

            ExecResult result = Main.execMesh(args, script_args);
            Model model = result.getModel();
            if (model != null) {
                ModelWriter writer = model.getWriter();
                TriangleMesh mesh = writer.getGeneratedMesh();
            }
            String error = result.getErrors();
            System.out.println("Error String: " + error);
            assertTrue("Error String not empty",error.length() > 0);

        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }

    public void testSecurity() {
        String script = "importPackage(Packages.abfab3d.grid.op);\n" +
                "importPackage(Packages.abfab3d.grid);\n" +
                "importPackage(Packages.abfab3d.datasources);\n" +
                "\n" +
                "function main(args) {\n" +
                "\tvar radius = args[0];" +
                "var f = new java.io.File(\"c:/tmp/foo.txt\");" +
                "\tvar grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,.1*MM);\n" +
                "\tvar sphere = new Sphere(radius);\n" +
                "\tvar maker = new GridMaker();\n" +
                "\tmaker.setSource(sphere);\n" +
                "\tmaker.makeGrid(grid);\n" +
                "\t\n" +
                "\treturn grid;\n" +
                "}";

        String[] script_args = new String[] {"0.005"};

        try {
            File f = File.createTempFile("script","vss");

            String[] args = new String[] {f.toString()};
            FileUtils.write(f,script);

            ExecResult result = Main.execMesh(args, script_args);
            Model model = result.getModel();
            if (model != null) {
                ModelWriter writer = model.getWriter();
                TriangleMesh mesh = writer.getGeneratedMesh();
            }
            String error = result.getErrors();
            System.out.println("Error String: " + error);
            assertTrue("Error String not empty",error.length() > 0);

        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }
}
