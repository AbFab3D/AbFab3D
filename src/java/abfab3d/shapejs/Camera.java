/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.shapejs;

import abfab3d.param.BaseParameterizable;

import javax.vecmath.Matrix4f;

/**
 * Camera interface.  All cameras can be backed down to a 4x4 matrix.
 *
 * @author Alan Hudson
 */
public abstract class Camera extends BaseParameterizable {
    /**
     * Returns current inverted view matrix, the matrix should transform view coord into playbox coord
     */
    public abstract void getViewMatrix(Matrix4f invViewMatrix);

    /**
     * TODO: This is a kernel parameter instead of a databuffer right now, so provide a separate method to get it
     * @return
     */
    public abstract double getCameraAngle();
}
