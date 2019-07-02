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

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;

/**
 * Root of the spatial structure.  Uses a fixed cell size so that multiple threads can populate the structure
 *
 * @author Alan Hudson
 */
public class SpatialRoot implements TriangleCollector {
    private static final boolean DEBUG = false;
    private SpatialNode root;
    private Intersector isect = new Intersector();
    private boolean removeDegenerates = true;

    // stats
    private Stats stats = new Stats();
    private boolean statsCalculated;
    private int maxTri = 1000;

    private int triId = 0;

    public SpatialRoot() {

    }

    public SpatialRoot(Bounds bounds, int maxTri) {
        this.maxTri = maxTri;
        root = new SpatialNode(bounds.getCenterX(),bounds.getCenterY(),bounds.getCenterZ(),bounds.getSizeMax());

        statsCalculated = false;
    }

    public boolean addTri(Vector3d v0,Vector3d v1, Vector3d v2) {
        Triangle tri = new Triangle(v0,v1,v2,triId++);
        addTri(tri,isect);

        return true;
    }

    public void addTri(Triangle tri, Intersector isect) {
        stats.inputTris++;

        // TODO: This is not working right
/*
        if (tri.isDegenerateAreaMethod()) {
            stats.degenerateTris++;
            if (removeDegenerates) return;
        }
*/
        root.addTriangle(tri,isect,maxTri);
    }

    /**
     * Find what cells a triangle is in for debugging
     * @param id
     */
    public void findTri(int id) {
        SpatialNode[] cells = getCells();

        boolean triShown = false;

        printf("Finding tri: %d\n",id);
        for(int i=0; i < cells.length; i++) {
            List<Triangle> tris = cells[i].getTriangles();
            for(Triangle t : tris) {
                if (t.getId() == id) {
                    if (!triShown) {
                        printf("Tri: %s\n",t.toString(MM));
                        triShown = true;
                    }
                    printf("%s -> center: %6.2f %6.2f %6.2f  size: %6.2f\n",
                            cells[i],cells[i].getCenter()[0]/MM,cells[i].getCenter()[1]/MM,cells[i].getCenter()[2]/MM,
                            cells[i].getSize()/MM);
                }
            }
        }
    }

    public Stats getStats() {
        if (!statsCalculated) {
            statsCalculated = true;
            stats.totalCells = 0;

            collectStats(root);

            stats.avgTrianglesPerCell = ((float)stats.triEntries) / stats.totalCells;
        }
        return stats;
    }

    private void collectStats(SpatialNode node) {
        stats.totalCells++;
        int tris = node.getTriangles().size();
        stats.triEntries += tris;
        if (tris == 0) {
            stats.emptyCells++;
        }
        if (tris > stats.maxTrianglesPerCell) {
            stats.maxTrianglesPerCell = tris;
        }

        SpatialNode[] children = node.getChildren();

        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                collectStats(children[i]);
            }
        }

        stats.avgTrianglesPerCell = stats.triEntries / stats.totalCells;
    }

    public SpatialNode[] getCells() {
        getStats();

        SpatialNode[] cells = new SpatialNode[stats.totalCells];
        collectCells(root,0,cells);

        return cells;
    }

    private int collectCells(SpatialNode cell, int idx, SpatialNode[] cells) {
        if (cell == null) return idx;

        cells[idx++] = cell;
        //printf("Cell: %d -> %s\n",idx-1,cell);
        SpatialNode[] children = cell.getChildren();

        if (children == null) return idx;

        for(int i=0; i < children.length; i++) {
            idx = collectCells(children[i],idx,cells);
        }

        return idx;
    }

    public static class Stats {
        public int inputTris;
        public int degenerateTris;
        public int triEntries;
        public int totalCells;
        public int emptyCells;
        public float avgTrianglesPerCell;
        public int maxTrianglesPerCell;
        public AtomicInteger outsideTris = new AtomicInteger(0);

        public String toString(double unit) {
            return fmt("Root Stats:\ninput tris: %d  degenerate: %d expansion: %4.2fX  outside: %d\ncells: %d  empty: %6.2f%% avg: %6.0f max: %d\n",
            inputTris,degenerateTris,((float)triEntries)/inputTris,outsideTris.get(),totalCells,((float)emptyCells)/totalCells*100,avgTrianglesPerCell,maxTrianglesPerCell);

        }
    }
}
