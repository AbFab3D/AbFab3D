/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.validate;

//External Imports
import javax.vecmath.Vector3f;
import javax.vecmath.Point3f;

//Internal Imports

/**
 * Stores all pertinant information about a face
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class MeshFace {

    /** The first vertex of the face */
    protected int index1;

    /** The second vertex of the face */
    protected int index2;

    /** The third vertex of the face */
    protected int index3;

    /** The first coord of the face */
    protected Point3f coord1;

    /** The second coord of the face */
    protected Point3f coord2;

    /** The third coord of the face */
    protected Point3f coord3;

    /** The averaged normal of the face */
    protected Vector3f faceNormal;

    /**
     * Default constructor
     */
    protected MeshFace() {}

    //----------------------------------------------------------
    // Methods overridden from Object
    //----------------------------------------------------------

    /**
     * Overridden equals that compares the 3 indexes
     *  to determine equality
     */
    public boolean equals(Object obj) {

        if(this == obj) {
            return true;
        }

        if((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        MeshFace check = (MeshFace)obj;

        if (check.index1 == index1 &&
                check.index2 == index2 &&
                check.index3 == index3) {

            return true;

        }

        return false;

    }

    /**
     * Generate a custom hashcode for this object
     * @return
     */
    public int hashcode() {

        int hash = 7;
        hash = 31 * hash + index1;
        hash = 31 * hash + index2;
        hash = 31 * hash + index3;

        return hash;
    }
}
