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
import abfab3d.core.AttributeGrid;
import abfab3d.io.output.GridSaver;
import abfab3d.util.BoundingBoxCalculator;
import abfab3d.core.DataSource;
import abfab3d.core.Vec;
import junit.framework.TestCase;

import java.io.IOException;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;


/**
 * Test AttributedMeshReader
 *
 * @author Alan Hudson
 */
public class TestAttributedMeshReader extends TestCase {


    public void testX3DV() throws IOException {

        double voxelSize = 0.1*MM;

        String filePath = "test/models/donut_textured.x3dv";

        AttributedMeshReader reader = new AttributedMeshReader(filePath);
        //TriangleProducer tp = new TriangleProducer2Converter(reader);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        reader.getAttTriangles(bb);
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
    }

    public void testX3DVNonAttributed() throws IOException {

        double voxelSize = 0.1*MM;

        // This cube is really 10mm
        String filePath = "test/models/cube_1mm.x3dv";

        AttributedMeshReader reader = new AttributedMeshReader(filePath);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        reader.getAttTriangles(bb);
        double bounds[] = new double[6];
        bb.getBounds(bounds);

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

        assertTrue("Data dimension should be 3",reader.getDataDimension() == 3);
    }

    /**
     * We support a limited form of IndexedFaceSet, must be all triangles and contain texture coordinates.
     * @throws IOException
     */
    public void testIFS() throws IOException {

        double voxelSize = 0.1*MM;

        String filePath = "test/models/donut_textured_ifs.x3dv";

        AttributedMeshReader reader = new AttributedMeshReader(filePath);
        //TriangleProducer tp = new TriangleProducer2Converter(reader);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        reader.getAttTriangles(bb);
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
    }

    /**
     * Test loading of color specified using Material
     * @throws IOException
     */
    public void testMaterial() throws IOException {

        double voxelSize = 0.1*MM;

        String filePath = "test/models/mat.x3dv";

        AttributedMeshReader reader = new AttributedMeshReader(filePath);
        //TriangleProducer tp = new TriangleProducer2Converter(reader);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        reader.getAttTriangles(bb);
        double bounds[] = bb.getRoundedBounds(voxelSize);

        //size: 0.030008 0.050003998 0.03
        double expectedX = 30 * MM + voxelSize;
        double expectedY = 50.0 * MM + voxelSize;
        double expectedZ = 30 * MM + voxelSize;

        double EPS = 2*voxelSize;

        double boundsX = (bounds[1] - bounds[0]);
        double boundsY = (bounds[3] - bounds[2]);
        double boundsZ = (bounds[5] - bounds[4]);
    }

    /**
     * Test multiple textures per file
     * @throws IOException
     */
    public void testMultTextures() throws IOException {

        double voxelSize = 0.1*MM;

        String filePath = "test/models/textured2.x3db";

        AttributedMeshReader reader = new AttributedMeshReader(filePath);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        reader.getAttTriangles(bb);
        double bounds[] = bb.getRoundedBounds(voxelSize);

        DataSource atts = reader.getAttributeCalculator();
    }

    public void testAttributes() throws IOException {

        double voxelSize = 0.1*MM;

        String filePath = "test/models/square.x3dv";

        AttributedMeshReader reader = new AttributedMeshReader(filePath);
        //TriangleProducer tp = new TriangleProducer2Converter(reader);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        reader.getAttTriangles(bb);
        double bounds[] = bb.getRoundedBounds(voxelSize);

        //size: 0.030008 0.050003998 0.03
        double expectedX = 20 * MM + voxelSize;
        double expectedY = 20.0 * MM + voxelSize;
        double expectedZ = 0 * MM + voxelSize;

        double EPS = 2*voxelSize;

        double boundsX = (bounds[1] - bounds[0]);
        double boundsY = (bounds[3] - bounds[2]);
        double boundsZ = (bounds[5] - bounds[4]);
        printf("Bounds: %4.2f %4.2f %4.2f mm\n",boundsX/MM,boundsY/MM,boundsZ/MM);
        assertTrue("BoundsX", (Math.abs(boundsX - expectedX) < EPS));
        assertTrue("BoundsY",(Math.abs(boundsY - expectedY) < EPS));
        assertTrue("BoundsZ",(Math.abs(boundsZ - expectedZ) < EPS));

        DataSource atts = reader.getAttributeCalculator();
        Vec uv = new Vec(3);
        Vec color = new Vec(3);

        // upper left white
        Vec expected = new Vec(3);
        expected.v[0] = 1.0;
        expected.v[1] = 1.0;
        expected.v[2] = 1.0;

        EPS = 0.1;

        uv.v[0] = 0;
        uv.v[1] = 0;
        atts.getDataValue(uv,color);
        printf("u: %4.2f v: %4.2f  color: %4.2f %4.2f %4.2f\n",uv.v[0],uv.v[1],color.v[0],color.v[1],color.v[2]);
        assertTrue("r wrong",Math.abs(color.v[0] - expected.v[0]) < EPS);

        // upper middle black
        expected.v[0] = 0.0;
        expected.v[1] = 0.0;
        expected.v[2] = 0.0;

        EPS = 0.1;

        uv.v[0] = 0.5;
        uv.v[1] = 0;
        atts.getDataValue(uv,color);
        printf("u: %4.2f v: %4.2f  color: %4.2f %4.2f %4.2f\n",uv.v[0],uv.v[1],color.v[0],color.v[1],color.v[2]);
        assertTrue("r wrong",Math.abs(color.v[0] - expected.v[0]) < EPS);

        // lower right white
        expected.v[0] = 1.0;
        expected.v[1] = 1.0;
        expected.v[2] = 1.0;

        EPS = 0.1;

        uv.v[0] = 1;
        uv.v[1] = 1;
        atts.getDataValue(uv,color);
        printf("u: %4.2f v: %4.2f  color: %4.2f %4.2f %4.2f\n",uv.v[0],uv.v[1],color.v[0],color.v[1],color.v[2]);
        assertTrue("r wrong",Math.abs(color.v[0] - expected.v[0]) < EPS);

    }

    public void testColorWriting() throws IOException {

        double voxelSize = 0.1*MM;

        //String filePath = "test/models/R2D2_m.x3dv";
        //String filePath = "test/models/donut_textured_ifs.x3dv";
        //String filePath = "test/models/cube_textured.x3dv";
        String filePath = "test/models/cube_perface.x3dv";
        //String filePath = "test/models/flufee.x3db";

        AttributedMeshReader reader = new AttributedMeshReader(filePath);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        reader.getAttTriangles(bb);
        double bounds[] = bb.getRoundedBounds(voxelSize);

        GridSaver writer = new GridSaver();
        writer.setWriteTexturedMesh(true);
        writer.setTexPixelSize(1);
//        writer.setMeshSmoothingWidth(1);
        writer.setMeshSmoothingWidth(voxelSize);
        writer.setTexTriExt(2);
        writer.setTexTriGap(1.5);
        //String outPath = "/tmp/tex/box.svx";
        String outPath = "/tmp/tex/donut.x3d";

        GridLoader loader = new GridLoader();
        loader.setThreadCount(0);
        loader.setMaxInDistance(2*MM);
        loader.setMaxOutDistance(2*MM);
        loader.setMargins(0.2*MM);
        loader.setPreferredVoxelSize(0.1*MM);
        loader.setSurfaceVoxelSize(0.5);

        Constant colorizer = new Constant(0.9,0.2,0.0);
        colorizer.initialize();

        //DataSource ac = (DataSource) colorizer;
        DataSource ac = reader.getAttributeCalculator();
        AttributeGrid grid = loader.rasterizeAttributedTriangles(reader, ac);


        Vec expected = new Vec(3);
        Vec uv = new Vec(3);
        Vec color = new Vec(3);
        double EPS = 0.1;

        expected.v[0] = 0;
        expected.v[1] = 1;
        expected.v[2] = 0;

        uv.v[0] = 0;
        uv.v[1] = 1;
        ac.getDataValue(uv,color);
        printf("u: %4.2f v: %4.2f  color: %4.2f %4.2f %4.2f\n",uv.v[0],uv.v[1],color.v[0],color.v[1],color.v[2]);
        /*
        assertTrue("r wrong",Math.abs(color.v[0] - expected.v[0]) < EPS);
        assertTrue("g wrong",Math.abs(color.v[1] - expected.v[1]) < EPS);
        assertTrue("b wrong",Math.abs(color.v[2] - expected.v[2]) < EPS);
        */
        writer.write(grid, outPath);

    }

    public void testBinarySTLfile() throws IOException {

        double voxelSize = 0.1*MM;

        String filePath = "test/models/sphere_10cm_5K_tri.stl";

        AttributedMeshReader reader = new AttributedMeshReader(filePath);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        reader.getAttTriangles(bb);

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

        assertTrue("dimension should be 3",reader.getDataDimension() == 3);
        assertTrue("attributeCalculator should be null",reader.getAttributeCalculator() == null);
    }

    /**
     * My understanding of how to test this might be off.  Or it could be broken.  I'll wait till we have some
     * visual results to help debug.
     * @throws IOException
     */
    public void _testTextureTransform() throws IOException {

        double voxelSize = 0.1*MM;

        String filePath = "test/models/texture_transform.x3dv";

        AttributedMeshReader reader = new AttributedMeshReader(filePath);
        //TriangleProducer tp = new TriangleProducer2Converter(reader);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        reader.getAttTriangles(bb);
        double bounds[] = bb.getRoundedBounds(voxelSize);

        //size: 0.030008 0.050003998 0.03
        double expectedX = 20 * MM + voxelSize;
        double expectedY = 20.0 * MM + voxelSize;
        double expectedZ = 0 * MM + voxelSize;

        double EPS = 2*voxelSize;

        double boundsX = (bounds[1] - bounds[0]);
        double boundsY = (bounds[3] - bounds[2]);
        double boundsZ = (bounds[5] - bounds[4]);
        printf("Bounds: %4.2f %4.2f %4.2f mm\n",boundsX/MM,boundsY/MM,boundsZ/MM);
        assertTrue("BoundsX", (Math.abs(boundsX - expectedX) < EPS));
        assertTrue("BoundsY",(Math.abs(boundsY - expectedY) < EPS));
        assertTrue("BoundsZ",(Math.abs(boundsZ - expectedZ) < EPS));

        DataSource atts = reader.getAttributeCalculator();
        Vec uv = new Vec(3);
        Vec color = new Vec(3);

        // translate known values x + 0.333
        double tx = 0.333333;

        // upper left white
        Vec expected = new Vec(3);
        expected.v[0] = 1.0;
        expected.v[1] = 1.0;
        expected.v[2] = 1.0;

        EPS = 0.1;

        uv.v[0] = 0 + tx;
        uv.v[1] = 0;
        atts.getDataValue(uv,color);
        printf("u: %4.2f v: %4.2f  color: %4.2f %4.2f %4.2f\n",uv.v[0],uv.v[1],color.v[0],color.v[1],color.v[2]);
        assertTrue("r wrong",Math.abs(color.v[0] - expected.v[0]) < EPS);

        // upper middle black
        expected.v[0] = 0.0;
        expected.v[1] = 0.0;
        expected.v[2] = 0.0;

        EPS = 0.1;

        uv.v[0] = 0.5 + tx;
        uv.v[1] = 0;
        atts.getDataValue(uv,color);
        printf("u: %4.2f v: %4.2f  color: %4.2f %4.2f %4.2f\n",uv.v[0],uv.v[1],color.v[0],color.v[1],color.v[2]);
        assertTrue("r wrong",Math.abs(color.v[0] - expected.v[0]) < EPS);

        // lower right white
        expected.v[0] = 1.0;
        expected.v[1] = 1.0;
        expected.v[2] = 1.0;

        EPS = 0.1;

        uv.v[0] = 1 + tx;
        uv.v[1] = 1;
        atts.getDataValue(uv,color);
        printf("u: %4.2f v: %4.2f  color: %4.2f %4.2f %4.2f\n",uv.v[0],uv.v[1],color.v[0],color.v[1],color.v[2]);
        assertTrue("r wrong",Math.abs(color.v[0] - expected.v[0]) < EPS);

    }

}
