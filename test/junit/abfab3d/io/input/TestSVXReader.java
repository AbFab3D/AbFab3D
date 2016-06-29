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
import abfab3d.io.output.MeshMakerMT;
import abfab3d.io.output.STLWriter;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import junit.framework.TestCase;

import java.io.IOException;


/**
 * Test SVXReader
 *
 * @author Alan Hudson
 */
public class TestSVXReader extends TestCase {


    public void testManifestParsing() throws IOException {
                
        String filePath = "test/models/sphere.svx";

        SVXReader svx = new SVXReader();
        AttributeGrid grid = svx.load(filePath);
        SVXManifest mf = svx.getManifest();

        assertNotNull("Grid", grid);
        assertEquals("nx",grid.getWidth(),102);
        assertEquals("ny",grid.getWidth(),102);
        assertEquals("nz",grid.getWidth(),102);

        double sw = 0.2;
        double ef = 0.1;
        double mv = 0;
        double vs = grid.getVoxelSize();

        double maxDecimationError = ef * vs * vs;
        // Write out the grid to an STL file
        MeshMakerMT meshmaker = new MeshMakerMT();
        int max_threads = 8;

        meshmaker.setThreadCount(max_threads);
        meshmaker.setSmoothingWidth(sw);
        meshmaker.setMaxDecimationError(maxDecimationError);
        //meshmaker.setMaxDecimationCount(ShapeJSGlobal.maxDecimationCountDefault);
        meshmaker.setMaxAttributeValue((int)Math.pow(2,mf.getSubvoxelBits() - 1));

        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder(160000);
        meshmaker.makeMesh(grid, its);

        System.out.println("Vertices: " + its.getVertexCount() + " faces: " + its.getFaceCount());

        WingedEdgeTriangleMesh mesh = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());

        STLWriter stl = new STLWriter("sphere.stl");
        mesh.getTriangles(stl);
        stl.close();

        /*
        if (mv > 0) {
            ShellResults sr = app.common.GridSaver.getLargestShells(mesh, mp, mv);
            mesh = sr.getLargestShell();
            int regions_removed = sr.getShellsRemoved();
            System.out.println("Regions removed: " + regions_removed);
        }
        */
    }
}
