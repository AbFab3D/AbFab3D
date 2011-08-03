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

package abfab3d.util;

// External Imports
import java.util.Iterator;
import javax.vecmath.*;

// Internal Imports
import abfab3d.grid.*;

/**
 * Matrix utilities.
 *
 * @author Alan Hudson
 */
public class MatrixUtil {
    private static final boolean DEBUG = false;

    /** High-Side epsilon double = 0 */
    private static final double ZEROEPS = 0.0001f;

    /**
     * Helper method for creating Transforms from components.
     */
    public static Matrix4d createMatrix(double[] vfCenter, double[] vfScale,
        double[] vfRotation, double[] vfTranslation, double[] vfScaleOrientation) {
        Vector3d tempVec = new Vector3d();
        AxisAngle4d tempAxis = new AxisAngle4d();
        Matrix4d tempMtx1 = new Matrix4d();
        Matrix4d tempMtx2 = new Matrix4d();
        Matrix4d tmatrix = new Matrix4d();

        if (DEBUG) {
        System.out.println("Create Matrix: ");
        System.out.println("center: " + java.util.Arrays.toString(vfCenter));
        System.out.println("translation: " + java.util.Arrays.toString(vfTranslation));
        System.out.println("rotation: " + java.util.Arrays.toString(vfRotation));
        System.out.println("scale: " + java.util.Arrays.toString(vfScale));
        }

        tempVec.x = -vfCenter[0];
        tempVec.y = -vfCenter[1];
        tempVec.z = -vfCenter[2];

        tmatrix.setIdentity();
        tmatrix.setTranslation(tempVec);

        if (DEBUG) System.out.println("center: \n" + tmatrix);
        double scaleVal = 1.0;

        if (floatEq(vfScale[0], vfScale[1]) &&
            floatEq(vfScale[0], vfScale[2])) {

            scaleVal = vfScale[0];
            tempMtx1.set(scaleVal);
            if (DEBUG) System.out.println("S\n" + tempMtx1);

        } else {
            // non-uniform scale
            //System.out.println("Non Uniform Scale");
            tempAxis.x = vfScaleOrientation[0];
            tempAxis.y = vfScaleOrientation[1];
            tempAxis.z = vfScaleOrientation[2];
            tempAxis.angle = -vfScaleOrientation[3];

            double tempAxisNormalizer =
                1 / Math.sqrt(tempAxis.x * tempAxis.x +
                              tempAxis.y * tempAxis.y +
                              tempAxis.z * tempAxis.z);

            tempAxis.x *= tempAxisNormalizer;
            tempAxis.y *= tempAxisNormalizer;
            tempAxis.z *= tempAxisNormalizer;

            tempMtx1.set(tempAxis);
            tempMtx2.mul(tempMtx1, tmatrix);

            // Set the scale by individually setting each element
            tempMtx1.setIdentity();
            tempMtx1.m00 = vfScale[0];
            tempMtx1.m11 = vfScale[1];
            tempMtx1.m22 = vfScale[2];

            tmatrix.mul(tempMtx1, tempMtx2);

            tempAxis.x = vfScaleOrientation[0];
            tempAxis.y = vfScaleOrientation[1];
            tempAxis.z = vfScaleOrientation[2];
            tempAxis.angle = vfScaleOrientation[3];
            tempMtx1.set(tempAxis);
        }

        tempMtx2.mul(tempMtx1, tmatrix);

        if (DEBUG) System.out.println("Sx-C\n" + tempMtx2);
        double magSq = vfRotation[0] * vfRotation[0] +
                      vfRotation[1] * vfRotation[1] +
                      vfRotation[2] * vfRotation[2];

        if(magSq < ZEROEPS) {
            tempAxis.x = 0;
            tempAxis.y = 0;
            tempAxis.z = 1;
            tempAxis.angle = 0;
        } else {
            if ((magSq > 1.01) || (magSq < 0.99)) {

                double mag = (double)(1 / Math.sqrt(magSq));
                tempAxis.x = vfRotation[0] * mag;
                tempAxis.y = vfRotation[1] * mag;
                tempAxis.z = vfRotation[2] * mag;
            } else {
                tempAxis.x = vfRotation[0];
                tempAxis.y = vfRotation[1];
                tempAxis.z = vfRotation[2];
            }

            tempAxis.angle = vfRotation[3];
        }

        tempMtx1.set(tempAxis);
        if (DEBUG) System.out.println("R\n" + tempMtx1);

        tmatrix.mul(tempMtx1, tempMtx2);
        if (DEBUG) System.out.println("RxSx-C\n" + tmatrix);

        tempVec.x = vfCenter[0];
        tempVec.y = vfCenter[1];
        tempVec.z = vfCenter[2];

        tempMtx1.setIdentity();
        tempMtx1.setTranslation(tempVec);
        //System.out.println("C" + tempMtx1);

        tempMtx2.mul(tempMtx1, tmatrix);
        if (DEBUG) System.out.println("CxRxSx-C\n" + tempMtx2);

        tempVec.x = vfTranslation[0];
        tempVec.y = vfTranslation[1];
        tempVec.z = vfTranslation[2];

        tempMtx1.setIdentity();
        tempMtx1.setTranslation(tempVec);

        tmatrix.mul(tempMtx1, tempMtx2);

        if (DEBUG) System.out.println(tmatrix);
        return tmatrix;

    }

    /**
     * Compares to floats to determine if they are equal or very close
     *
     * @param val1 The first value to compare
     * @param val2 The second value to compare
     * @return True if they are equal within the given epsilon
     */
    private static boolean floatEq(double val1, double val2) {
        double diff = val1 - val2;

        if(diff < 0)
            diff *= -1;

        return (diff < ZEROEPS);
    }
}