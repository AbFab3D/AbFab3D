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

import abfab3d.datasources.Constant;
import abfab3d.datasources.DataSourceGrid;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.GridDataChannelFloat;
import abfab3d.grid.GridDataDesc;
import abfab3d.io.output.GridSaver;
import abfab3d.util.BoundingBoxCalculator;
import abfab3d.util.DataSource;
import abfab3d.util.Vec;
import junit.framework.TestCase;

import java.io.IOException;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Units.MM;


/**
 * Test AttributedMeshReader
 *
 * @author Alan Hudson
 */
public class TestModelLoader extends TestCase {


    public void testStripAtts() throws IOException {

        double voxelSize = 0.1*MM;

        String filePath = "test/models/donut_textured.x3dv";

        ModelLoader loader = new ModelLoader(filePath);
        loader.setAttributeLoading(false);
        AttributedMesh mesh = loader.getMesh();

        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        mesh.getAttTriangles(bb);
        double bounds[] = bb.getRoundedBounds(voxelSize);

        //size: 0.030008 0.050003998 0.03
        double expectedX = 30 * MM + voxelSize;
        double expectedY = 50.0 * MM + voxelSize;
        double expectedZ = 30 * MM + voxelSize;

        double EPS = 2*voxelSize;

        double boundsX = (bounds[1] - bounds[0]);
        double boundsY = (bounds[3] - bounds[2]);
        double boundsZ = (bounds[5] - bounds[4]);
        printf("Bounds: %4.2f %4.2f %4.2f mm",boundsX/MM,boundsY/MM,boundsZ/MM);
        assertTrue("BoundsX",(Math.abs(boundsX - expectedX) < EPS));
        assertTrue("BoundsY",(Math.abs(boundsY - expectedY) < EPS));
        assertTrue("BoundsZ",(Math.abs(boundsZ - expectedZ) < EPS));

        AttributeGrid grid = loader.getGrid();
        GridDataDesc gdd = grid.getDataDesc();

        assertEquals("Channels should be 1",1,gdd.size());

    }

    public void testX3DV() throws IOException {

        double voxelSize = 0.1*MM;

        String filePath = "test/models/donut_textured.x3dv";

        ModelLoader loader = new ModelLoader(filePath);
        loader.setAttributeLoading(true);
        AttributedMesh mesh = loader.getMesh();

        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        mesh.getAttTriangles(bb);
        double bounds[] = bb.getRoundedBounds(voxelSize);

        //size: 0.030008 0.050003998 0.03
        double expectedX = 30 * MM + voxelSize;
        double expectedY = 50.0 * MM + voxelSize;
        double expectedZ = 30 * MM + voxelSize;

        double EPS = 2*voxelSize;

        double boundsX = (bounds[1] - bounds[0]);
        double boundsY = (bounds[3] - bounds[2]);
        double boundsZ = (bounds[5] - bounds[4]);
        printf("Bounds: %4.2f %4.2f %4.2f mm",boundsX/MM,boundsY/MM,boundsZ/MM);
        assertTrue("BoundsX",(Math.abs(boundsX - expectedX) < EPS));
        assertTrue("BoundsY",(Math.abs(boundsY - expectedY) < EPS));
        assertTrue("BoundsZ",(Math.abs(boundsZ - expectedZ) < EPS));

        AttributeGrid grid = loader.getGrid();
        GridDataDesc gdd = grid.getDataDesc();

        assertEquals("Channels should be 4",4,gdd.size());

    }

    public void testX3DVNonAttributed() throws IOException {

        double voxelSize = 0.1*MM;

        // This cube is really 10mm
        String filePath = "test/models/cube_1mm.x3dv";

        ModelLoader loader = new ModelLoader(filePath);
        loader.setAttributeLoading(true);
        AttributedMesh mesh = loader.getMesh();

        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        mesh.getAttTriangles(bb);
        double bounds[] = bb.getRoundedBounds(voxelSize);

        mesh.getAttTriangles(bb);

        double expectedX = 10 * MM;
        double expectedY = 10 * MM;
        double expectedZ = 10 * MM;

        double EPS = 2*voxelSize;

        double boundsX = (bounds[1] - bounds[0]);
        double boundsY = (bounds[3] - bounds[2]);
        double boundsZ = (bounds[5] - bounds[4]);
        printf("Bounds: %4.2f %4.2f %4.2f mm",boundsX/MM,boundsY/MM,boundsZ/MM);
        assertTrue("BoundsX",(Math.abs(boundsX - expectedX) < EPS));
        assertTrue("BoundsY",(Math.abs(boundsY - expectedY) < EPS));
        assertTrue("BoundsZ",(Math.abs(boundsZ - expectedZ) < EPS));

        AttributeGrid grid = loader.getGrid();

        GridDataDesc gdd = grid.getDataDesc();

        assertEquals("Channels should be 1",1,gdd.size());

        DataSourceGrid dsg = new DataSourceGrid(grid);
        dsg.initialize();

        GridDataDesc dataDesc = dsg.getBufferDataDesc();
        int gridDataTypeSize = dsg.getBufferTypeSize(dataDesc);
        printf("channels: %d  gridDataTypeSize:%d\n", dataDesc.size(), gridDataTypeSize);
        int channelCount = dataDesc.size();

    }

    public void testX3DVNonAttributed2() throws IOException {

        double voxelSize = 0.1*MM;

        // This cube is really 10mm
        String filePath = "test/models/cube_1mm.x3dv";

        ModelLoader loader = new ModelLoader(filePath);
        loader.setAttributeLoading(true);

        AttributeGrid grid = loader.getGrid();

        GridDataDesc gdd = grid.getDataDesc();

        assertEquals("Channels should be 1",1,gdd.size());
    }

    public void testBinarySTLfile() throws IOException {

        double voxelSize = 0.5*MM;

        String filePath = "test/models/sphere_10cm_5K_tri.stl";

        ModelLoader loader = new ModelLoader(filePath);
        loader.setAttributeLoading(true);
        loader.setVoxelSize(voxelSize);
        AttributedMesh mesh = loader.getMesh();
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        mesh.getAttTriangles(bb);

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

        AttributeGrid grid = loader.getGrid();
        GridDataDesc gdd = grid.getDataDesc();

        assertEquals("Channels should be 1", 1, gdd.size());
        assertTrue("attributeCalculator should be null",mesh.getAttributeCalculator() == null);

        int bits = gdd.getAttributePacker().getBitCount();
        assertEquals("Bits should be 8", 8, bits);
        DataSourceGrid dsg = new DataSourceGrid(grid);

    }
}
