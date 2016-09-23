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

import abfab3d.core.AttributeGrid;
import abfab3d.core.GridDataDesc;
import abfab3d.core.Bounds;

import javax.vecmath.Vector3d;

import abfab3d.datasources.DataSourceGrid;

import abfab3d.io.output.SVXWriter;

import abfab3d.util.BoundingBoxCalculator;
import abfab3d.util.ColorMapperDistance;
import abfab3d.util.ColorMapper;


import junit.framework.TestCase;

import java.io.IOException;

import static abfab3d.grid.util.GridUtil.writeSlice;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;


/**
 * Test AttributedMeshReader
 *
 * @author Alan Hudson
 */
public class TestModelLoader extends TestCase {


    public void testSVX() throws IOException {
        double voxelSize = 0.2*MM;

        String filePath = "test/models/donut_textured.x3dv";

        long t0 = time();
        long lt;
        long wt;
        long rt;

        ModelLoader loader = new ModelLoader(filePath);
        loader.setVoxelSize(voxelSize);
        loader.setAttributeLoading(true);
        //loader.setAttributeLoading(false);

        AttributeGrid grid = loader.getGrid();
        lt = time() - t0;

        t0 = time();
        String f = "/tmp/donut.svx";
        new SVXWriter().write(grid, f);
        wt = time() - t0;

        t0 = time();
        SVXReader sreader = new SVXReader();
        AttributeGrid grid2 = sreader.load(f);
        rt = time() - t0;

        printf("load time: %d ms\n",lt);
        printf("write time: %d ms\n",wt);
        printf("read time: %d ms\n",rt);
    }
/*
    public void testSVX2() throws IOException, ClassNotFoundException {
        double voxelSize = 0.2*MM;

//        String filePath = "test/models/donut_textured.x3dv";
//        String filePath = "test/models/coffee_maker.x3db";
        String filePath = "test/models/flufee.x3db";

        long t0 = time();
        long lt=0;
        long wt=0;
        long rt=0;

        ModelLoader loader = new ModelLoader(filePath);
        loader.setVoxelSize(voxelSize);
        loader.setAttributeLoading(true);
        //loader.setAttributeLoading(false);

        lt = time() - t0;

        t0 = time();
        AttributeGrid grid = loader.getGrid();
        DataSourceGrid dsg = new DataSourceGrid(grid);

        int data[] = new int[dsg.getBufferSize()];
        dsg.getBuffer(data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(baos);
        ByteBuffer bdata = null;

        int dsize = 4;
        bdata = ByteBuffer.allocate(data.length * dsize);
        int dlen = data.length;
        for (int i = 0; i < dlen; i++) {
            bdata.putInt(data[i]);
        }
        gos.write(bdata.array());
        gos.flush();


        byte[] cbytes = baos.toByteArray();
        wt = time() - t0;


        t0 = time();

        byte[] ucbytes = new byte[dlen*dsize];
        ByteArrayInputStream bais = new ByteArrayInputStream(cbytes);
        GZIPInputStream gis = new GZIPInputStream(bais);
        gis.read(ucbytes);
        rt = time() - t0;

        printf("grid size: %d %d %d\n",grid.getWidth(),grid.getHeight(),grid.getDepth());
        printf("load time: %d ms\n",lt);
        printf("compress time: %d ms\n",wt);
        printf("decompress time: %d ms\n",rt);
        printf("size  original: %d  compressed: %d\n",0,cbytes.length);
    }
*/
    public void testSVX_singleChannel() throws IOException {
        double voxelSize = 0.05*MM;

        String filePath = "test/models/donut_textured.x3dv";
        String svxFile = "/tmp/donut_distrgb.svx";
        
        long t0 = time();
        long 
            lt=0, wt = 0, rt = 0;
        
        ModelLoader loader = new ModelLoader(filePath);
        loader.setVoxelSize(voxelSize);
        loader.setAttributeLoading(true);

        AttributeGrid grid = loader.getGrid();
        lt = time() - t0;
        printf("model loading time: %d ms\n", lt);
        grid.setDataDesc(GridDataDesc.getDistBGRcomposite(0.001));
        t0 = time();
        new SVXWriter().write(grid, svxFile);
        wt = time() - t0;
        printf("SVX writing time: %d ms\n", wt);
        
        
        t0 = time();
        
        SVXReader sreader = new SVXReader();
        AttributeGrid grid2 = sreader.load(svxFile);
        rt = time() - t0;
        
        printf("Model load time: %d ms\n",lt);
        printf("SVX write time: %d ms\n",wt);
        printf("SVX read time: %d ms\n",rt);
    }


    public void testStripAtts() throws IOException {

        double voxelSize = 0.2*MM;

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

    public void devTestMultiPass() throws IOException {

        double voxelSize = 0.1*MM;
        double distanceBand = 0.5*MM;
        String filePath = "test/models/boxITS_4x4x0.4cm_slanted.x3d";

        ModelLoader loader = new ModelLoader(filePath);
        loader.setUseCaching(true);
        loader.setAttributeLoading(false);
        loader.setVoxelSize(voxelSize);
        loader.setUseMultiPass(false);
        AttributeGrid grid = loader.getGrid();
        Bounds bounds = grid.getGridBounds();
        printf("loaded grid: [%d x %d x %d]\n", grid.getWidth(), grid.getHeight(), grid.getDepth());
        int nv = 3000;
        int nu = (int)(nv * bounds.getSizeZ()/bounds.getSizeY());
        Vector3d eu = new Vector3d(0, 0, bounds.getSizeZ()/nu);
        Vector3d ev = new Vector3d(0, bounds.getSizeY()/nv, 0);
        Vector3d sliceOrigin = new Vector3d(bounds.getCenterX(), bounds.ymin, bounds.zmin);
        ColorMapper colorMapper = new ColorMapperDistance(distanceBand);

        //writeSlice(grid,grid.getDataChannel(),colorMapper, sliceOrigin, eu, ev,nu, nv, "/tmp/00_outModelSliceX.png");
        
    }

    public static void main(String arg[]) throws Exception {

        for(int i = 0; i < 5; i++)
            new TestModelLoader().devTestMultiPass();
        //new TestModelLoader().testBinarySTLfile();
        
    }

}
