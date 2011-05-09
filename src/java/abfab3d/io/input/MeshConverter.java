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

package abfab3d.io.input;

//External Imports
import java.util.ArrayList;

//Internal Imports

/**
 * An converter class to assist in IndexedTriangle* conversions
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class MeshConverter {

    /**
     * Hide the constructor
     */
    private MeshConverter() {
    }

    /**
     * Convert the IndexedTriangleStripSet index list into a
     *  IndexedTriangleSet index list
     *
     * @param index The IndexedTriangleStripSet index
     * @return newly ordered IndexedTriangleSet index
     */
    public static int[] convertITSSToITS(int[] index) {
        return convertITSSToITS(index, index.length);
    }

    /**
     * Convert the IndexedTriangleStripSet index list into a
     *  IndexedTriangleSet index list
     *
     * @param index The IndexedTriangleStripSet index
     * @param numIndex The number of valid index values to process
     * @return newly ordered IndexedTriangleSet index
     */
    public static int[] convertITSSToITS(int[] index, int numIndex) {

        ArrayList<Integer> strips = new ArrayList<Integer>();

        // calculate the number of indices for the new geometry
        int stripSize = 0;
        int maxSize = 0;

        for (int i = 0; i < numIndex; i++) {

            // if index is -1 then it is the end of the strip
            if (index[i] == -1) {

                // add to the list of strips
                strips.add(stripSize);

                // update the new index max size
                maxSize += (stripSize - 2) * 3;

                // reset the counter for the next strip
                stripSize = 0;

                continue;
            }

            stripSize++;
        }

        // Account for the last strip not ending with -1
        if(index[numIndex - 1] != -1) {
            // add to the list of strips
            strips.add(stripSize);

            // update the new index max size
            maxSize += (stripSize - 2) * 3;
        }

        int[] triangleIndex = new int[maxSize];

        int count = 0;
        int vertexCount;
        int total = 0;
        int tmp = 0;

        for (int i = 0; i < strips.size(); i++) {

            vertexCount = strips.get(i).intValue();

            for (int j = 2; j < vertexCount; j++) {

                tmp = j + total;
                if (j % 2 == 1) {

                    triangleIndex[count++] = index[tmp];
                    triangleIndex[count++] = index[tmp - 1];
                    triangleIndex[count++] = index[tmp - 2];

                } else {

                    triangleIndex[count++] = index[tmp];
                    triangleIndex[count++] = index[tmp - 2];
                    triangleIndex[count++] = index[tmp - 1];

                }

            }

            total += vertexCount + 1;

        }

        return triangleIndex;

    }

    /**
     * Convert the IndexedTriangleFanSet index list into a
     *  IndexedTriangleSet index list
     *
     * @param index The IndexedTriangleFanSet index
     * @return newly ordered IndexedTriangleSet index
     */
    public static int[] convertITFSToITS(int[] index) {
        return convertITFSToITS(index, index.length);
    }


    /**
     * Convert the IndexedTriangleFanSet index list into a
     *  IndexedTriangleSet index list
     *
     * @param index The IndexedTriangleFanSet index
     * @param numIndex The number of valid index values to process
     * @return newly ordered IndexedTriangleSet index
     */
    public static int[] convertITFSToITS(int[] index, int numIndex) {

        ArrayList<Integer> fans = new ArrayList<Integer>();

        // calculate the number of indices for the new geometry
        int stripSize = 0;
        int maxSize = 0;

        for (int i = 0; i < numIndex; i++) {

            // if index is -1 then it is the end of the strip
            if (index[i] == -1) {

                // add to the list of strips
                fans.add(stripSize);

                // update the new index max size
                maxSize += (stripSize - 2) * 3;

                // reset the counter for the next strip
                stripSize = 0;

                continue;
            }

            stripSize++;

        }

        // Account for the last strip not ending with -1
        if(index[numIndex - 1] != -1) {
            // add to the list of strips
            fans.add(stripSize);

            // update the new index max size
            maxSize += (stripSize - 2) * 3;
        }

        int[] triangleIndex = new int[maxSize];

        int count = 0;
        int vertexCount;
        int total = 0;
        int tmp = 0;

        for (int i = 0; i < fans.size(); i++) {

            vertexCount = fans.get(i).intValue();

            for (int j = 0; j < (vertexCount-2) * 3; j+=3) {

                    triangleIndex[j + tmp] = index[total];
                    triangleIndex[j+1 + tmp] = index[++count];
                    triangleIndex[j+2 + tmp] = index[count+1];
            }

            total += vertexCount + 1;
            tmp += (vertexCount-2) * 3;
            count = total;

        }

        return triangleIndex;
    }
}
