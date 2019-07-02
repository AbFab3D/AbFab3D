/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2019
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.spatial;

import abfab3d.core.Bounds;
import abfab3d.core.TriangleCollector;
import abfab3d.core.Units;
import abfab3d.io.input.MeshEvaluator;
import org.j3d.geom.GeometryData;
import org.j3d.geom.SphereGenerator;
import org.junit.Assert;
import org.junit.Test;

import javax.vecmath.Vector3d;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.CM;
import static abfab3d.core.Units.MM;

public class TestSpatialRoot {
    private void getTriangles(GeometryData gd, TriangleCollector tc) {
        Vector3d v0 = new Vector3d();
        Vector3d v1 = new Vector3d();
        Vector3d v2 = new Vector3d();

        int len = gd.indexesCount;

        for (int i = 0; i < len; ) {
            int index = gd.indexes[i++];

            // get coords
            v0.x = gd.coordinates[index * 3];
            v0.y = gd.coordinates[index * 3 + 1];
            v0.z = gd.coordinates[index * 3 + 2];

            index = gd.indexes[i++];
            v1.x = gd.coordinates[index * 3];
            v1.y = gd.coordinates[index * 3 + 1];
            v1.z = gd.coordinates[index * 3 + 2];

            index = gd.indexes[i++];
            v2.x = gd.coordinates[index * 3];
            v2.y = gd.coordinates[index * 3 + 1];
            v2.z = gd.coordinates[index * 3 + 2];

            tc.addTri(v0, v1, v2);
        }
    }

    @Test
    public void testSplit() {
        float ir = (float) (1 * CM);
        float or = (float) (3 * CM);
        int facets = 64;

        //BoxGenerator tg = new BoxGenerator(or,or,or);
        SphereGenerator tg = new SphereGenerator(or / 2, facets);
        GeometryData geom = new GeometryData();
        geom.geometryType = GeometryData.INDEXED_TRIANGLES;
        tg.generate(geom);

        long t0 = System.currentTimeMillis();

        MeshEvaluator eval = new MeshEvaluator();
        getTriangles(geom, eval);
        printf("Bounds: %s\n", eval.getStats().bounds.toString(MM));

        MeshEvaluator.Stats mstats = eval.getStats();
        printf("%s\n", mstats.toString(Units.MM));

        Bounds bounds = mstats.bounds;

        SpatialRoot root = new SpatialRoot(bounds, 1024);

        Vector3d v0 = new Vector3d();
        Vector3d v1 = new Vector3d();
        Vector3d v2 = new Vector3d();

        int len = geom.indexesCount;

        for (int i = 0; i < len; ) {
            int index = geom.indexes[i++];

            // get coords
            v0.x = geom.coordinates[index * 3];
            v0.y = geom.coordinates[index * 3 + 1];
            v0.z = geom.coordinates[index * 3 + 2];

            index = geom.indexes[i++];
            v1.x = geom.coordinates[index * 3];
            v1.y = geom.coordinates[index * 3 + 1];
            v1.z = geom.coordinates[index * 3 + 2];

            index = geom.indexes[i++];
            v2.x = geom.coordinates[index * 3];
            v2.y = geom.coordinates[index * 3 + 1];
            v2.z = geom.coordinates[index * 3 + 2];

            root.addTri(v0, v1, v2);
        }

        SpatialRoot.Stats stats = root.getStats();
        printf("%s\n", stats.toString(MM));

        SpatialNode[] cells = root.getCells();
        Assert.assertNotNull(cells);
        for (int i = 0; i < cells.length; i++) {
            Assert.assertNotNull("Cell null", cells[i]);
        }
    }

    private void printNode(SpatialNode cell) {
        printf("Cell: %d\n", cell.getTriangles().size());
        SpatialNode[] children = cell.getChildren();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                printNode(children[i]);
            }
        }
    }

}
