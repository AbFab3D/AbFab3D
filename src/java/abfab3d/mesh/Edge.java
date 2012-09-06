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
 * Edge defined by 2 half edges.  Includes singled linked list of other edges in the face.
 */
public class Edge {

    public HalfEdge he;

    public Edge next; // just a list

    public String toString() {

        if (he != null)
            return "edge: " + he + ":" + ((he.twin != null) ? he.twin.toString() : "null");
        else
            return "edge [null]";

    }

}
