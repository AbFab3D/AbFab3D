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

// External imports
import xj3d.filter.FilterExitCodes;

// Internal imports
// none

/**
 * Collection of the exit codes for the project.
 *
 * If everything is working as intended then exit code is equal to zero, or
 * {@link #SUCCESS}.  If we encounter any abnormalities, errors, or exceptions,
 * then a non-zero exit code should be used.  In general, the lower the value
 * of the non-zero exit code, the more we prioritize catching the error.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class ExitCodes extends FilterExitCodes {

    /** Surface is non manifold. Exit code used when an edge does not
     * have exactly two (2) edges. */
    public static final int NON_MANIFOLD = 3;

    /** Invalid triangulation of the input, meaning the geometry is
     * not connected or indexed. <br> Example: No coordinate node
     * defined prior to starting triangle node. */
    public static final int INVALID_TRIANGULATION = 4;

    /** The model contained valid content but nothing was usable as a model.
     * <br> For example, it contained material definitions by no geometry,
     * or an Indexed*Set did not have coordinate indices defined. */
    public static final int NO_GEOMETRY = 8;

    /** The file contained more triangles than the currently
     * acceptable upper limit. */
    public static final int TOO_MANY_TRIANGLES = 9;

    /** The file contained some good content and some content that
     * cannot be converted to useable geometry. <br> Example:
     * X3D Switch nodes or LODs. */
    public static final int NOT_ALL_GEOMETRY_IS_CONVERTABLE = 10;

    /** Exit code when mixed winding orders are found */
    public static final int MIXED_WINDING = 11;

    /** This 'too large' exit code is used in ScaleCalculation.  It means
     * that object size is bigger than the maximum bounds specified. */
    public static final int MODEL_TOO_LARGE = 40;

    /** A texture that was defined in the file was not
     * findable in the same directory as this X3D file. */
    public static final int TEXTURE_FILE_NOT_FOUND = 50;

    /** This exit code is used by filters that search for something
     * particular in a file.<br> Example: ColourCheckFilter uses this exit
     * code to indicate that a Texture, Material, or Color node has been
     * found in the file.     */
    public static final int FOUND_IN_FILE = 90;

    /** File contains content that is not directly convertible with
     * the simple filters, but may be with more complex processing.<br>
     * Example: multiple shapes within a file */
    public static final int USE_CORRECTION_FILTER = 100;

//  This is the full list, including exit codes at the Xj3D level:
//
//    /** Exit code used when no exceptions or errors have occurred */
//    public static final int SUCCESS = 0;
//
//    /** Input file not found.  Used when we can't read input file. */
//    public static final int FILE_NOT_FOUND = 1;
//
//    /** Invalid input file format. <br> Example: structural problem with
//     * the geometry format, meaning the X3D is incorrectly defined, such as
//     * geometry without a shape node. <br> Example: field is out of range. */
//    public static final int INVALID_INPUT_FILE = 2;
//
//    /** Surface is non manifold. Exit code used when an edge does not
//     * have exactly two (2) edges. */
//    public static final int NON_MANIFOLD = 3;
//
//    /** Invalid triangulation of the input, meaning the geometry is
//     * not connected or indexed. <br> Example: No coordinate node
//     * defined prior to starting triangle node. */
//    public static final int INVALID_TRIANGULATION = 4;
//
//    /** Unable to write output file.  <br> Example: Output file type
//     * unknown, or there is a write permission error or similar problem. */
//    public static final int CANNOT_WRITE_OUTPUT_FILE = 5;
//
//    /** Invalid filter arguments provided other than file name.<br>
//     * Example: arguments are out-of-bounds, or formatted incorrectly. */
//    public static final int INVALID_ARGUMENTS = 6;
//
//    /** Invalid filter specified.<br>
//     * For debugging purposes only on initial deployment. */
//    public static final int INVALID_FILTER_SPECIFIED = 7;
//
//    /** The model contained valid content but nothing was usable as a model.
//     * <br> For example, it contained material definitions by no geometry,
//     * or an Indexed*Set did not have coordinate indices defined. */
//    public static final int NO_GEOMETRY = 8;
//
//    /** The file contained more triangles than the currently
//     * acceptable upper limit. */
//    public static final int TOO_MANY_TRIANGLES = 9;
//
//    /** The file contained some good content and some content that
//     * cannot be converted to useable geometry. <br> Example:
//     * X3D Switch nodes or LODs. */
//    public static final int NOT_ALL_GEOMETRY_IS_CONVERTABLE = 10;
//
//    /** Exit code when mixed winding orders are found */
//    public static final int MIXED_WINDING = 11;
//
//    /** This 'too large' exit code is used in ScaleCalculation.  It means
//     * that object size is bigger than the maximum bounds specified. */
//    public static final int MODEL_TOO_LARGE = 40;
//
//    /** A texture that was defined in the file was not
//     * findable in the same directory as this X3D file. */
//    public static final int TEXTURE_FILE_NOT_FOUND = 50;
//
//    /** This exit code is used by filters that search for something
//     * particular in a file.<br> Example: ColourCheckFilter uses this exit
//     * code to indicate that a Texture, Material, or Color node has been
//     * found in the file.     */
//    public static final int FOUND_IN_FILE = 90;
//
//    /** Exit code to use when application has timed out, exceeding the
//     * runtime specified by a -maxRunTime parameter */
//     public static final int MAX_RUN_TIME_EXCEEDED = 99;
//
//    /** File contains content that is not directly convertible with
//     * the simple filters, but may be with more complex processing.<br>
//     * Example: multiple shapes within a file */
//    public static final int USE_CORRECTION_FILTER = 100;
//
//    /** The software crashed abnormally.
//     * Probably error is due to a programming issue */
//    public static final int ABNORMAL_CRASH = 101;
//
//    /** The software failed due to lack of memory. */
//    public static final int OUT_OF_MEMORY = 102;
//
//    /** The software failed due to an exceptional error condition
//     * that is outside ordinary failure modes.<br> Example:
//     * failure to find native libraries or other configuration error. */
//    public static final int EXCEPTIONAL_ERROR = 103;

}
