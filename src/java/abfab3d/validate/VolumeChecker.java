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

// External Imports
import java.text.NumberFormat;
import java.util.ArrayList;

import java.io.File;

import javax.vecmath.*;

import org.web3d.vrml.sav.ErrorHandler;

//Internal Imports
import abfab3d.io.input.IndexedTriangleSetLoader;
import abfab3d.util.SysErrorReporter;
import xj3d.filter.DeathTimer;

/**
 * Calculate the volume of a mesh. Compute the volume as a sum of signed
 * volumes of tetrahedra.
 *
 * Technique borrowed from this paper: Implicit Fairing of Irregular
 *    Meshes Using Diffusion and Curvature Flow
 *
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
public class VolumeChecker extends IndexedTriangleSetLoader {
    /**
     * Constructor
     *
     * @param errorHandler The place to report errors
     */
    public VolumeChecker(ErrorHandler errorHandler){
        super(errorHandler);
    }

    //-----------------------------------------------------------------------
    // Local Methods
    //-----------------------------------------------------------------------

    /**
     * Calculate the volume of the indexed triangle set.
     *
     * @param scale The scale, 1 equals meters
     */
    public float calculateVolume(float scale) {

        float volume = 0;

        int index;
        Point3f point;

        Vector3f v1 = new Vector3f(0, 0, 0);
        Vector3f v2 = new Vector3f(0, 0, 0);
        Vector3f v3 = new Vector3f(0, 0, 0);

        ArrayList<MeshFace> faceList = new ArrayList<MeshFace>();

        MeshFace face = new MeshFace();

        // set the bounds using the first coordinate found
        float cx = coordinates[vertices[0]  ];
        float cy = coordinates[vertices[0]+1];
        float cz = coordinates[vertices[0]+2];

        // create the faces
        for(int i=0; i < vertices.length; i++) {
            index = vertices[i];

            // get coords
            cx = coordinates[ index ];
            cy = coordinates[index+1];
            cz = coordinates[index+2];

            // first vector
            if (i % 3 == 0) {
                point = new Point3f(cx, cy, cz);
                v1 = new Vector3f(cx, cy, cz);

                face = new MeshFace();
                face.index1 = index;
                face.coord1 = point;
            }

            // second vector
            if (i % 3 == 1) {
                point = new Point3f(cx, cy, cz);
                v2 = new Vector3f(cx, cy, cz);

                face.index2 = index;
                face.coord2 = point;
            }

            // third vector
            if (i % 3 == 2) {
                point = new Point3f(cx, cy, cz);
                v3 = new Vector3f(cx, cy, cz);

                face.index3 = index;
                face.coord3 = point;

                // calculate volume
                v2.cross(v2, v3);
                volume += v1.dot(v2);

                // calculate the face normal
                v1 = new Vector3f(
                        face.coord2.x - face.coord1.x,
                        face.coord2.y - face.coord1.y,
                        face.coord2.z - face.coord1.z);

                v2 = new Vector3f(
                        face.coord3.x - face.coord1.x,
                        face.coord3.y - face.coord1.y,
                        face.coord3.z - face.coord1.z);

                Vector3f normal = new Vector3f();
                normal.cross(v1, v2);

                face.faceNormal = normal;

                // add the data to the list
                faceList.add(face);
            }
        }

        volume /= 6;

        // Scale volume to requested units
        volume = Math.abs(volume * scale * scale * scale);

        return volume;
    }
}