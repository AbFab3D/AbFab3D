/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package shapejs.viewer;
import abfab3d.core.Bounds;
import abfab3d.shapejs.Viewpoint;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * A navigator provides a navigation style.
 *
 * @author Alan Hudson
 */
public interface Navigator {
    /** Have the navigation params changed since last call. */
    boolean hasChanged();

    /** 
     *    Get the view matrix 
     */
    public void getMatrix(Matrix4f mat);

    public double getCameraAngle();

    /** Set the world bounds */
    void setBounds(Bounds bounds);

    /** Initialize AWT component actions */
    void init(Component comp);

    /** Reset the view */
    void reset();
    
    public void reset(boolean setChanged);

    public void setEnabled(boolean val);

    /**
     * Set the position and rotation from a viewpoint definition
     */
    void setViewpoint(Viewpoint vp);
}
