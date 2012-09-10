/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.mesh;

import javax.vecmath.Point3d;

/**
 * Vertex defined by a point in space.
 */
public class Vertex {
    private static final boolean DEBUG = false;

    public Point3d p;
    private int id;

    public HalfEdge link; //associate each vertex with tail of _some_ edge

    public Vertex next;  // List of all vertices

    public int getID() {
        return id;
    }

    public void setID(int id) {
        if (DEBUG) System.out.println("Setting id:  from: " + this.id + " to: " + id);
        this.id = id;
    }
}

