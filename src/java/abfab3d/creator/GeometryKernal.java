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

package abfab3d.creator;

// External Imports
import java.io.*;
import java.util.*;

// Internal Imports

/**
 * Generates 3D geometry from parameters.  A kernal should require
 * no user-input.
 *
 * All Kernals should have a no parameter constructor.  All params should
 * come through the generate method.
 *
 * Hosted editors will insure that only a single thread will call
 * generate at a time.  This means class variable usage is safe.
 *
 * @author Alan Hudson
 */
public interface GeometryKernal {
    /**
     * Generate X3D binary geometry from the specificed parameters.
     *
     * @param params The parameters
     * @param os The stream to write out the file.
     */
    public void generate(Map<String,Object> params, OutputStream os) throws IOException;

    /**
     * Get the parameters for this editor.
     *
     * @return The parameters.
     */
    public Map<String,Parameter> getParams();
}