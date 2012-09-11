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

import static abfab3d.util.Output.fmt;

/**
 * Vertex defined by a point in space.
 */
public class Vertex {
    private static final boolean DEBUG = false;

    private Point3d point;
    private int id;

    private HalfEdge link; //associate each vertex with tail of _some_ edge

    private Vertex next;  // List of all vertices

    public int getID() {
        return id;
    }

    public void setID(int id) {
        if (DEBUG) System.out.println("Setting id:  from: " + this.id + " to: " + id);
        this.id = id;
    }

    public Point3d getPoint() {
        return point;
    }

    public void setPoint(Point3d p) {
        this.point = p;
    }

    public HalfEdge getLink() {
        return link;
    }

    public void setLink(HalfEdge link) {
        this.link = link;
    }

    public Vertex getNext() {
        return next;
    }

    public void setNext(Vertex next) {
        this.next = next;
    }

    public String toString(){
        return fmt("%2d (%10.7f,%10.7f,%10.7f)", id, point.x,point.y,point.z);
    }
}

