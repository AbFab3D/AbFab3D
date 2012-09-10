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
 * Key used in maps for half edges.  Allows access via either direction in map searches.
 *
 */
public class HalfEdgeKey {

    Object o1, o2;

    public HalfEdgeKey() {
    }

    public HalfEdgeKey(Object o1, Object o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    public int hashCode() {

        return o1.hashCode() + 119 * o2.hashCode();

    }

    public boolean equals(Object obj) {

        HalfEdgeKey hk = (HalfEdgeKey) obj;
        return (hk.o1 == o1) && (hk.o2 == o2);

    }

}
