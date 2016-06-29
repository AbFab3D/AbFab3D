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
import abfab3d.core.Grid;

/**
 * A voxel enhanced version of a Geometry Kernel.  All kernels
 * must create triangles.  A voxel kernel can generate voxel
 * representations as well.
 *
 * @author Alan Hudson
 */
public interface VoxelKernel extends GeometryKernel {
    /**
     * Generate voxel stream from the specified parameters.
     *
     * @param params The parameters
     * @param accuracy The accuracy to generate the model
     * @param grid The Grid to write to.  May be reallocated if necessary
     * @return KernelResults is at 0, new grid if reallocated at 1
     */
    public Object[] generate(Map<String,Object> params, Accuracy acc, Grid grid) throws IOException;
}

