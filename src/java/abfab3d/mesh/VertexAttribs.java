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
 * Vertex defined by a point in space with attributes
 */
public class VertexAttribs implements Vertex {
    private static final boolean DEBUG = true;

    private Point3d point;
    private int id;

    private HalfEdge link; //associate each vertex with tail of _some_ edge

    private Vertex next;  // List of all vertices
    private Vertex prev;

    /** per-vertex attribute information */
    private float[][] attribs;

    private Object userData;
    private int userDataPos;

    // TODO: Debug var, remove
    private boolean removed;

    public VertexAttribs(int numAttribs) {
        attribs = new float[numAttribs][];
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void setID(int id) {
        if (DEBUG) {
            //printf("vertex: setID() from: %s to %s\n", this.id, id);            
            //Thread.currentThread().dumpStack();
        }
        this.id = id;
    }

    @Override
    public Point3d getPoint() {
        return point;
    }

    @Override
    public void setPoint(Point3d p) {
        this.point = p;
    }

    @Override
    public HalfEdge getLink() {
        return link;
    }

    @Override
    public void setLink(HalfEdge link) {
        this.link = link;
    }

    @Override
    public Vertex getNext() {
        return next;
    }

    @Override
    public void setNext(Vertex next) {
        this.next = next;
    }

    @Override
    public Vertex getPrev() {
        return prev;
    }

    @Override
    public void setPrev(Vertex prev) {
        this.prev = prev;
    }

    public String toString(){
        String dead = "";
        if (removed) {
            dead = " DEAD";
        }

        //return fmt("%3d (%10.7f,%10.7f,%10.7f%s)", id, point.x,point.y,point.z, dead);

        return fmt("%3s (%10.7f,%10.7f,%10.7f%s)", userData, point.x,point.y,point.z, dead);

    }

    @Override
    public Object getUserData() {
        return userData;
    }

    @Override
    public void setUserData(Object userData) {
        this.userData = userData;
    }

    @Override
    public int getUserDataPos() {
        return userDataPos;
    }

    @Override
    public void setUserDataPos(int userData) {
        this.userDataPos = userData;
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }

    @Override
    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public void setAttribs(float[][] attribs) {
        this.attribs = attribs;
    }

    public float[][] getAttribs() {
        return attribs;
    }
    public float[] getAttribs(int channel) {
        return attribs[channel];
    }
}

