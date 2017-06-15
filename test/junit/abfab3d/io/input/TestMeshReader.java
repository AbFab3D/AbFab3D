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

package abfab3d.io.input;

import junit.framework.TestCase;

import javax.vecmath.Vector3d;

import abfab3d.core.TriangleCollector;
import abfab3d.core.Bounds;
import abfab3d.util.BoundingBoxCalculator;
import abfab3d.transforms.Scale;
import abfab3d.io.output.STLWriter;


import java.io.IOException;

import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;


/**
 * Test MeshReader
 *
 * @author Alan Hudson
 */
public class TestMeshReader extends TestCase {


    public void testBinarySTLfile() throws IOException {
                
        double voxelSize = 0.1*MM;

        String filePath = "test/models/sphere_10cm_5K_tri.stl";

        MeshReader reader = new MeshReader(filePath);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        reader.getTriangles(bb);

        double bounds[] = bb.getRoundedBounds(voxelSize);

        double expected = 100 * MM + voxelSize;
        double EPS = 2*voxelSize;

        double boundsX = (bounds[1] - bounds[0]);
        double boundsY = (bounds[3] - bounds[2]);
        double boundsZ = (bounds[5] - bounds[4]);
        printf("Bounds: %4.2f %4.2f %4.2f mm",boundsX/MM,boundsY/MM,boundsZ/MM);
        assertTrue("BoundsX",(Math.abs(boundsX - expected) < EPS));
        assertTrue("BoundsY",(Math.abs(boundsY - expected) < EPS));
        assertTrue("BoundsZ",(Math.abs(boundsZ - expected) < EPS));
    }

    public void testX3DV() throws IOException {

        double voxelSize = 0.1*MM;

        // This cube is really 10mm
        String filePath = "test/models/cube_1mm.x3dv";

        MeshReader reader = new MeshReader(filePath);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        reader.getTriangles(bb);
        double bounds[] = bb.getRoundedBounds(voxelSize);

        double expected = 10 * MM + voxelSize;
        double EPS = 2*voxelSize;

        double boundsX = (bounds[1] - bounds[0]);
        double boundsY = (bounds[3] - bounds[2]);
        double boundsZ = (bounds[5] - bounds[4]);
        printf("Bounds: %4.2f %4.2f %4.2f mm",boundsX/MM,boundsY/MM,boundsZ/MM);
        assertTrue("BoundsX",(Math.abs(boundsX - expected) < EPS));
        assertTrue("BoundsY",(Math.abs(boundsY - expected) < EPS));
        assertTrue("BoundsZ",(Math.abs(boundsZ - expected) < EPS));
    }

    public void testX3DB() throws IOException {

        double voxelSize = 0.1*MM;

        String filePath = "test/models/coffee_maker.x3db";

        MeshReader reader = new MeshReader(filePath);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        reader.getTriangles(bb);
        double bounds[] = bb.getRoundedBounds(voxelSize);

        double expectedX = 77.6 * MM + voxelSize;
        double expectedY = 64.0 * MM + voxelSize;
        double expectedZ = 69 * MM + voxelSize;

        double EPS = 2*voxelSize;

        double boundsX = (bounds[1] - bounds[0]);
        double boundsY = (bounds[3] - bounds[2]);
        double boundsZ = (bounds[5] - bounds[4]);
        printf("Bounds: %4.2f %4.2f %4.2f mm",boundsX/MM,boundsY/MM,boundsZ/MM);
        assertTrue("BoundsX",(Math.abs(boundsX - expectedX) < EPS));
        assertTrue("BoundsY",(Math.abs(boundsY - expectedY) < EPS));
        assertTrue("BoundsZ",(Math.abs(boundsZ - expectedZ) < EPS));
    }

    public void testX3D() throws IOException {

        double voxelSize = 0.1*MM;

        String filePath = "test/models/sphere_10cm_rough.x3d";

        MeshReader reader = new MeshReader(filePath);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        reader.getTriangles(bb);
        double bounds[] = bb.getRoundedBounds(voxelSize);

        double expectedX = 200 * MM + voxelSize;
        double expectedY = 200 * MM + voxelSize;
        double expectedZ = 200 * MM + voxelSize;

        double EPS = 2*voxelSize;

        double boundsX = (bounds[1] - bounds[0]);
        double boundsY = (bounds[3] - bounds[2]);
        double boundsZ = (bounds[5] - bounds[4]);
        printf("Bounds: %4.2f %4.2f %4.2f mm",boundsX/MM,boundsY/MM,boundsZ/MM);
        assertTrue("BoundsX",(Math.abs(boundsX - expectedX) < EPS));
        assertTrue("BoundsY",(Math.abs(boundsY - expectedY) < EPS));
        assertTrue("BoundsZ",(Math.abs(boundsZ - expectedZ) < EPS));
    }

    
    public void testOBJReader() throws IOException {

        double EPS = 1.e-10;
        // cube is 1 x 1 x 1 
        String path = "test/models/unit_cube.obj";
        OBJReader reader = new OBJReader();
        // scale box to 30M
        Scale s = new Scale(30);
        s.initialize();
        reader.setTransform(s);
        
        BoundingBoxCalculator bb = new BoundingBoxCalculator();

        printf("testOBJReader(%s)\n",path);
        reader.read(path, bb);
        Bounds bounds = bb.getBounds();
        printf("triCount: %d\n", bb.getTriangleCount());
        printf("bounds: %s\n", bounds);
        assertTrue("BoundsX",(Math.abs(bounds.getSizeX() - 30*MM) < EPS));
        assertTrue("BoundsY",(Math.abs(bounds.getSizeY() - 30*MM) < EPS));
        assertTrue("BoundsZ",(Math.abs(bounds.getSizeZ() - 30*MM) < EPS));
        assertTrue("triCount",(bb.getTriangleCount() == 12));
       
    }

    public void devTestOBJ() throws IOException {

        OBJReader reader = new OBJReader();
        Scale s = new Scale(30);
        s.initialize();
        reader.setTransform(s);
        
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        String path = "test/models/unit_cube.obj";
        reader.read(path, bb);
        Bounds bounds = bb.getBounds();
        printf("triCount: %d\n", bb.getTriangleCount());
        printf("bounds: %s\n", bounds);
        STLWriter writer = new STLWriter("/tmp/cube_30mm.stl");
        reader.read(path, writer);
        writer.close();
       
    }

    static class TrianglePrinter implements TriangleCollector {
        public boolean addTri(Vector3d v0,Vector3d v1,Vector3d v2){
            printf("tri: [[%7.5f, %7.5f, %7.5f],[%7.5f, %7.5f, %7.5f],[%7.5f, %7.5f, %7.5f]\n", 
                   v0.x,v0.y,v0.z,v1.x,v1.y,v1.z,v2.x,v2.y,v2.z);
            return true;
        }
    }

    public static void main(String arg[]) throws Exception {
        //new TestMeshReader().devTestOBJ();
        new TestMeshReader().testOBJReader();
    }

}
