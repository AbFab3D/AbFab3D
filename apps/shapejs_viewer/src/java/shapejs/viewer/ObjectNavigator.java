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
import javax.vecmath.Vector3f;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import static abfab3d.core.Output.printf;

/**
 * Object mode navigation.  Middle mouse scaled in and out.  Left rotates around
 * the model.
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov
 */
public class ObjectNavigator implements Navigator {

    private static final int MAX_SKIP = 2000000;

    private static final float DEFAULT_SCALE = 1;
    private float m_currentScale = DEFAULT_SCALE;

    private AxisAngle4d aa = new AxisAngle4d();
    private Point dragstart;
    private boolean indrag = false;

    private enum MOUSE_MODE {DRAG_ROTATE, DRAG_ZOOM, DRAG_PAN}

    private MOUSE_MODE dragmode = MOUSE_MODE.DRAG_ROTATE;
    private transient boolean hasChanged = false;
    private transient boolean doneChanging = false;

    private Vector3f m_trans = new Vector3f();
    private Matrix4f m_scaleMat = new Matrix4f();
    private Matrix4f m_rotMat = new Matrix4f();
    private Matrix4f rotIncrement = new Matrix4f();
    // radius of rotator ball (in screen pixels)
    private double ballRadius = 200;

    private int skippedCount = 0;
    private MouseMotionListener mml;
    private MouseWheelListener mwl;
    private MouseListener ml;
    private Component comp;

    public ObjectNavigator() {
        m_scaleMat.setIdentity();
        m_rotMat.setIdentity();
    }

    public void init(Component component) {
        initMouseListeners(component);
    }

    public void setEnabled(boolean val) {
        if (val) {
            comp.addMouseMotionListener(mml);
            comp.addMouseWheelListener(mwl);
            comp.addMouseListener(ml);
        } else {
            comp.removeMouseMotionListener(mml);
            comp.removeMouseWheelListener(mwl);
            comp.removeMouseListener(ml);
        }
    }

    private void initMouseListeners(Component component) {
        comp = component;
        mml = new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {

                if (dragstart != null && indrag) {
                    switch (dragmode) {
                        case DRAG_ROTATE:
                            double ax = (e.getY() - dragstart.getY());
                            double ay = (e.getX() - dragstart.getX());
                            double angle = Math.sqrt(ax * ax + ay * ay) / ballRadius;
                            aa.set(ax, ay, 0, angle);
                            rotIncrement.set(aa);
                            m_rotMat.mul(rotIncrement, m_rotMat);
                            setHasChanged();
                            break;
                        case DRAG_ZOOM:
                            m_currentScale -= (e.getY() - dragstart.getY()) / 5.0f;
                            setHasChanged();
                            break;
                        case DRAG_PAN:
                            double tx = (e.getY() - dragstart.getY());
                            double ty = (e.getX() - dragstart.getX());
                            m_trans.x += ty / 100.0;
                            m_trans.y -= tx / 100.0;
                            setHasChanged();
                    }
                }

                dragstart = e.getPoint();
            }
        };
        mwl = new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                m_currentScale -= e.getWheelRotation() * 0.03;
                doneChanging = false;
                setHasChanged();
            }

        };

        ml = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) return;

                switch (e.getButton()) {
                    case (MouseEvent.BUTTON1):
                        dragmode = MOUSE_MODE.DRAG_ROTATE;
                        indrag = true;
                        doneChanging = false;
                        break;
                    case (MouseEvent.BUTTON2):
                        dragmode = MOUSE_MODE.DRAG_PAN;
                        indrag = true;
                        doneChanging = false;
                        break;
                    case (MouseEvent.BUTTON3):
                        dragmode = MOUSE_MODE.DRAG_ZOOM;
                        indrag = true;
                        doneChanging = false;
                        break;

                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) return;

                switch (e.getButton()) {
                    case (MouseEvent.BUTTON1):
                        dragmode = MOUSE_MODE.DRAG_ZOOM;
                        indrag = false;
                        break;
                    case (MouseEvent.BUTTON2):
                        dragmode = MOUSE_MODE.DRAG_PAN;
                        indrag = false;
                        break;
                    case (MouseEvent.BUTTON3):
                        dragmode = MOUSE_MODE.DRAG_ROTATE;
                        indrag = false;
                        break;
                }

                dragstart = null;
            }
        };
    }


    @Override
    public boolean hasChanged() {
        boolean ret_val = hasChanged;

        if (!hasChanged) {
            /*
            skippedCount++;
            if (skippedCount > MAX_SKIP) {
                hasChanged = false;
                skippedCount = 0;
                return true;
            }
            */
        } else {
            skippedCount = 0;
        }
        hasChanged = false;

        return ret_val;
    }

    @Override
    public double getCameraAngle() {
        // it really has no camera angle 
        return 1;
    }

    @Override
    public void getMatrix(Matrix4f mat) {

        // set value of transform to given translation
        m_scaleMat.set(m_currentScale);
        // full transform is composition of rotation and translation 
        mat.mul(m_scaleMat, m_rotMat);
    }

    @Override
    public void setBounds(Bounds bounds) {
        //reset();
    }

    public void reset() {
        m_currentScale = DEFAULT_SCALE;
        m_rotMat.setIdentity();
        setHasChanged();
    }
    
    public void reset(boolean setChanged) {
        m_currentScale = DEFAULT_SCALE;
        m_rotMat.setIdentity();
        if (setChanged) {
        	setHasChanged();
        }
    }

    private void setHasChanged() {
        hasChanged = true;
    }

    @Override
    public void setViewpoint(Viewpoint vp) {
        throw new IllegalArgumentException("Not implemented");
    }
}
