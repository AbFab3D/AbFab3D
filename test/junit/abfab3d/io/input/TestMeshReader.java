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

import abfab3d.util.BoundingBoxCalculator;
import junit.framework.TestCase;

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

}
