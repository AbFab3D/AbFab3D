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
import abfab3d.util.DataSource;
import abfab3d.util.TriangleProducer;
import abfab3d.util.TriangleProducer2Converter;
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
public class TestAttributedMeshReader extends TestCase {


    public void testX3DV() throws IOException {

        double voxelSize = 0.1*MM;

        // This cube is really 10mm
        String filePath = "test/models/donut_textured.x3dv";

        AttributedMeshReader reader = new AttributedMeshReader(filePath);
        //TriangleProducer tp = new TriangleProducer2Converter(reader);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        reader.getTriangles2(bb);
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

    public void testAttributes() throws IOException {

        double voxelSize = 0.1*MM;

        // This cube is really 10mm
        String filePath = "test/models/square.x3dv";

        AttributedMeshReader reader = new AttributedMeshReader(filePath);
        //TriangleProducer tp = new TriangleProducer2Converter(reader);
        BoundingBoxCalculator bb = new BoundingBoxCalculator();
        reader.getTriangles2(bb);
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

        DataSource atts = reader.getAttributeData();
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

}
