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

package abfab3d.util;

// External Imports
import javax.vecmath.Matrix4f;

/**
 * Utilities for calculating bounding boxes.
 *
 * @author Alan Hudson
 */
public class BoundingBoxUtilsFloat {
    /**
     * Compute the min and max points.
     *
     * @param coords The coordinates to use
     * @param vcount The number of valid vertices
     * @param result An axis aligned bounds, minx,maxx,miny,maxy,minz,maxz
     */
    public void computeMinMax(final float[] coords, int vcount, float[] result) {
        float minx = Float.POSITIVE_INFINITY;
        float miny = Float.POSITIVE_INFINITY;
        float minz = Float.POSITIVE_INFINITY;
        float maxx = Float.NEGATIVE_INFINITY;
        float maxy = Float.NEGATIVE_INFINITY;
        float maxz = Float.NEGATIVE_INFINITY;

        float x,y,z;
        float ix, iy, iz;
        int idx = 0;

        for (int i=0; i < vcount; i++) {
            x = coords[idx++];
            y = coords[idx++];
            z = coords[idx++];

            if (x < minx) minx = x;
            if (y < miny) miny = y;
            if (z < minz) minz = z;

            if (x > maxx) maxx = x;
            if (y > maxy) maxy = y;
            if (z > maxz) maxz = z;
        }

        result[0] = minx;
        result[1] = maxx;
        result[2] = miny;
        result[3] = maxy;
        result[4] = minz;
        result[5] = maxz;
    }

    /**
     * Compute the min and max points.
     *
     * @param coords The coordinates to use
     * @param indices Indices, used to determine which coordinates are used
     * @param result An axis aligned bounds, minx,maxx,miny,maxy,minz,maxz
     */
    public void computeMinMax(final float[] coords, int[] indices, float[] result) {
        float minx = Float.POSITIVE_INFINITY;
        float miny = Float.POSITIVE_INFINITY;
        float minz = Float.POSITIVE_INFINITY;
        float maxx = Float.NEGATIVE_INFINITY;
        float maxy = Float.NEGATIVE_INFINITY;
        float maxz = Float.NEGATIVE_INFINITY;

        float x,y,z;
        float ix, iy, iz;
        int index;
        int len = indices.length;

        for(int i=0; i < len; i++) {
            index = indices[i];

            // get coords
            x = coords[index*3];
            y = coords[index*3+1];
            z = coords[index*3+2];

            if (x < minx) minx = x;
            if (y < miny) miny = y;
            if (z < minz) minz = z;

            if (x > maxx) maxx = x;
            if (y > maxy) maxy = y;
            if (z > maxz) maxz = z;
        }

        result[0] = minx;
        result[1] = maxx;
        result[2] = miny;
        result[3] = maxy;
        result[4] = minz;
        result[5] = maxz;
    }

    /**
     * Compute the min and max points of the transformed object.
     *
     * @param coords The coordinates to use
     * @param vcount The number of valid vertices
     * @param mat The transform matrix
     * @param result An axis aligned bounds, minx,maxx,miny,maxy,minz,maxz
     */
    public void computeMinMax(final float[] coords, int vcount, Matrix4f mat, float[] result) {
        float minx = Float.POSITIVE_INFINITY;
        float miny = Float.POSITIVE_INFINITY;
        float minz = Float.POSITIVE_INFINITY;
        float maxx = Float.NEGATIVE_INFINITY;
        float maxy = Float.NEGATIVE_INFINITY;
        float maxz = Float.NEGATIVE_INFINITY;

        float x,y,z;
        float ix, iy, iz;
        int idx = 0;

        for (int i=0; i < vcount; i++) {
            x = coords[idx++];
            y = coords[idx++];
            z = coords[idx++];

            // Inlined inverse rotate translate

            ix = x - mat.m30;
            iy = y - mat.m31;
            iz = z - mat.m32;

            // Multiply inverse-translated source vector by inverted rotation transform

            x = (mat.m00 * ix) + (mat.m01 * iy) + (mat.m02 * iz);
            y = (mat.m10 * ix) + (mat.m11 * iy) + (mat.m12 * iz);
            z = (mat.m20 * ix) + (mat.m21 * iy) + (mat.m22 * iz);

            if (x < minx) minx = x;
            if (y < miny) miny = y;
            if (z < minz) minz = z;

            if (x > maxx) maxx = x;
            if (y > maxy) maxy = y;
            if (z > maxz) maxz = z;
        }

        result[0] = minx;
        result[1] = maxx;
        result[2] = miny;
        result[3] = maxy;
        result[4] = minz;
        result[5] = maxz;
    }

    /**
     * Compute the min and max points of the transformed object.
     *
     * @param coords The coordinates to use
     * @param indices Indices, used to determine which coordinates are used
     * @param result An axis aligned bounds, minx,maxx,miny,maxy,minz,maxz
     */
    public void computeMinMax(final float[] coords, int[] indices, Matrix4f mat, float[] result) {
        float minx = Float.POSITIVE_INFINITY;
        float miny = Float.POSITIVE_INFINITY;
        float minz = Float.POSITIVE_INFINITY;
        float maxx = Float.NEGATIVE_INFINITY;
        float maxy = Float.NEGATIVE_INFINITY;
        float maxz = Float.NEGATIVE_INFINITY;

        float x,y,z;
        float ix, iy, iz;
        int index;
        int len = indices.length;

        for(int i=0; i < len; i++) {
            index = indices[i];

            // get coords
            x = coords[index*3];
            y = coords[index*3+1];
            z = coords[index*3+2];

            // Inlined inverse rotate translate

            ix = x - mat.m30;
            iy = y - mat.m31;
            iz = z - mat.m32;

            // Multiply inverse-translated source vector by inverted rotation transform

            x = (mat.m00 * ix) + (mat.m01 * iy) + (mat.m02 * iz);
            y = (mat.m10 * ix) + (mat.m11 * iy) + (mat.m12 * iz);
            z = (mat.m20 * ix) + (mat.m21 * iy) + (mat.m22 * iz);

            if (x < minx) minx = x;
            if (y < miny) miny = y;
            if (z < minz) minz = z;

            if (x > maxx) maxx = x;
            if (y > maxy) maxy = y;
            if (z > maxz) maxz = z;
        }

        result[0] = minx;
        result[1] = maxx;
        result[2] = miny;
        result[3] = maxy;
        result[4] = minz;
        result[5] = maxz;
    }
}
