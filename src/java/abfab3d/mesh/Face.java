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

    public HalfEdge he;
    public Face next;  // List of faces
    public Face prev;

    public String toString() {
        String st = "";

        HalfEdge the = he;
        HalfEdge start = the;

        while(the != null) {
            st += the.getHead().getID() + "->" + the.getTail().getID() + ", ";
            the = the.next;

            if (the == start) {
                break;
            }
        }

        return "face: " + st;
    }
}
