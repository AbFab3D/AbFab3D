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

/**
 * Face defined by half edges.  Includes single linked list traversal.
 */
public class Face {

    private HalfEdge he;
    private Face next;  // List of faces
    private Face prev;

    public String toString() {
        String st = "";

        HalfEdge the = getHe();
        HalfEdge start = the;

        while(the != null) {
            st += the.getHead().getID() + "->" + the.getTail().getID() + ", ";
            the = the.getNext();

            if (the == start) {
                break;
            }
        }

        return "face: " + st;
    }

    public HalfEdge getHe() {
        return he;
    }

    public void setHe(HalfEdge he) {
        this.he = he;
    }

    public Face getNext() {
        return next;
    }

    public void setNext(Face next) {
        this.next = next;
    }

    public Face getPrev() {
        return prev;
    }

    public void setPrev(Face prev) {
        this.prev = prev;
    }
}
