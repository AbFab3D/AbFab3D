/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package viewer;

import abfab3d.grid.Bounds;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import static abfab3d.util.Output.printf;

/**
 * Examine mode navigation.  Middle mouse zooms in and out.  Left rotates around
 * the model.  Right pans.
 *
 * @author Alan Hudson
 */
public class ExamineNavigator implements Navigator, MouseMotionListener, MouseWheelListener {
    private Vector3f trans = new Vector3f();
    private AxisAngle4f rot = new AxisAngle4f();
    private double vs;

    /** The grid bounds */
    private Bounds bounds;

    public void setTranslation(Vector3f trans) {
        this.trans = trans;
    }

    public void setRotation(AxisAngle4f rot) {
        this.rot = rot;
    }

    public void setBounds(Bounds bounds, double vs) {
        this.bounds = bounds.clone();
        this.vs = vs;

        // we'll rotate around the center of the grid.  Initial position is .5 -z size
        trans.x = bounds.getWidth(vs) / 2;
        trans.y = bounds.getHeight(vs) / 2;
        trans.z = -bounds.getDepth(vs) / 2;
    }

    @Override
    public void getViewMatrix(Matrix4f mat) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public boolean hasChanged() {
        return true;
    }
}
