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

package abfab3d.geom;

// External Imports
import java.util.*;
import java.io.*;

// Internal Imports
import abfab3d.grid.Grid;

/**
 * Creator of 3D geometry.
 *
 * For now this library will concentrate on geometry.  Only very
 * simple support will be added normals and colors.
 *
 * Geometry streams assume they are combined so do not generate
 * profile/component declarations.
 *
 * @author Alan Hudson
 *
 * Coordinate System(X3D):
 *
 *           -------    --->X(width)
 *           |     |    |
 *           |     |    Z(depth)
 *           -------
 */
public abstract class GeometryCreator {
    /**
     * Generate the geometry and issue commands to the provided handler.
     *
     * @param handler The stream to issue commands
     */
    public abstract void generate(Grid grid);

    // TODO: Might want a generate that tests for overlaping voxels to
    // help in debugging.
}
