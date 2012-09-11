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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterate through all the faces of a WingedEdgeTriangleMesh
 *
 * @author Alan Hudson
 */
public class FaceIterator implements Iterator<Face> {
    private Face list;

    protected FaceIterator(Face list) {
        this.list = list;
    }

    @Override
    public boolean hasNext() {
        return (list != null);
    }

    @Override
    public Face next() {
        Face ret_val = list;

        if (ret_val == null) {
            throw new NoSuchElementException();
        }

        list = list.getNext();

        return ret_val;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not implemented");
    }
}
