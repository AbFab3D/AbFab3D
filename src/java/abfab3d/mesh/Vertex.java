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
    
    private Object userData;

    // TODO: Debug var, remove
    private boolean removed;

    public Vertex() {
    }

    public Vertex(Vertex c) {
        this.point = new Point3d(c.point);
        this.id = c.id;
        this.link = c.link;
        this.next = c.next;
        this.userData = c.userData;
    }

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
        String dead = "";
        if (removed) {
            dead = " DEAD";
        }

        return fmt("%3d (%10.7f,%10.7f,%10.7f%s)", id, point.x,point.y,point.z, dead);
    }

    public Object getUserData() {
        return userData;
    }

    public void setUserData(Object userData) {
        this.userData = userData;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

}

