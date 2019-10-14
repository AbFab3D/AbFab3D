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

import javax.swing.*;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4d;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4d;

import java.awt.*;
import java.awt.event.*;

import static abfab3d.core.Output.printf;
import static abfab3d.core.MathUtil.clamp;
import static java.lang.Math.*;

/**
 * Examine mode navigation.  Middle mouse zooms in and out.  Left rotates around
 * the model.  Right pans.
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov
 */
public class ExamineNavigator implements Navigator {

    static final boolean DEBUG = true;

    private static final int MAX_SKIP = 2000000;

    private static final double DEFAULT_Z = -1.25;//-2.5;
    private double m_currentZ = DEFAULT_Z/tan(DEFAULT_CAMERA_ANGLE);

    static final double DEFAULT_CAMERA_ANGLE = atan(0.5);
    static final double MAX_CAMERA_ANGLE = atan(1);
    static final double MIN_CAMERA_ANGLE = 0.01;

    private double m_cameraAngle = DEFAULT_CAMERA_ANGLE;
    private AxisAngle4d aa = new AxisAngle4d();
    private Point dragstart;
    private boolean indrag = false;

    private double m_cameraAngleIncrement = 0.01;

    private enum MOUSE_MODE {DRAG_ROTATE, DRAG_ZOOM, DRAG_PAN}

    private MOUSE_MODE dragmode = MOUSE_MODE.DRAG_ROTATE;
    private transient boolean hasChanged = false;
    private transient boolean doneChanging = false;

    private Vector3f m_trans = new Vector3f();
    private Matrix4f m_transMat = new Matrix4f();
    private Matrix4f m_rotMat = new Matrix4f();
    private Matrix4f rotIncrement = new Matrix4f();
    // radius of rotator ball (in screen pixels) 
    private double ballRadius = 200;

    private int skippedCount = 0;
    private MouseMotionListener mml;
    private MouseWheelListener mwl;
    private MouseListener ml;
    private Component comp;

    public ExamineNavigator() {
        m_transMat.setIdentity();
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
                            m_currentZ -= (e.getY() - dragstart.getY()) / 5.0f;
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

        mwl = new MyMouseWheelListener();


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
        return m_cameraAngle;
    }

    @Override
    public void getMatrix(Matrix4f mat) {

        m_trans.z = (float)m_currentZ;
        // set value of transform to given translation 
        m_transMat.set(m_trans, 1.0f);
        // full transform is composition of rotation and translation 
        mat.mul(m_transMat, m_rotMat);

        Quat4f quat = new Quat4f();
        AxisAngle4d daa = new AxisAngle4d();
        m_rotMat.get(quat);
        daa.set(quat);

    }

    @Override
    public void setBounds(Bounds bounds) {
        //reset();
    }

    public void reset() {
        m_currentZ = DEFAULT_Z/tan(m_cameraAngle);
        m_rotMat.setIdentity();
        setHasChanged();
    }
    
    public void reset(boolean setChanged) {
        m_currentZ = DEFAULT_Z/tan(m_cameraAngle);
        m_rotMat.setIdentity();
        if (setChanged) {
        	setHasChanged();
        }
    }

    @Override
    public void setViewpoint(Viewpoint vp) {
        Vector3d pos = vp.getPosition();
        AxisAngle4d rot = vp.getRotation();

        m_trans.set(pos);
        m_currentZ = (float) pos.z;
        m_rotMat.set(rot);

    }

    private void setHasChanged() {
        hasChanged = true;
    }
    
    //
    // preocess mouse wheel events
    //
    class MyMouseWheelListener implements MouseWheelListener {
        
        public void mouseWheelMoved(MouseWheelEvent e) {

            if(e.isControlDown()){

                // change camera angle 
                double oldAngle = m_cameraAngle;
                m_cameraAngle -= m_cameraAngleIncrement*e.getWheelRotation();
                m_cameraAngle = clamp(m_cameraAngle, MIN_CAMERA_ANGLE, MAX_CAMERA_ANGLE);
                m_currentZ = m_currentZ*tan(oldAngle)/tan(m_cameraAngle);
                
                if(DEBUG) printf("cameraAngle: %5.3f\n", m_cameraAngle);
            } else {
                m_currentZ -= e.getWheelRotation() * 0.03 / tan(m_cameraAngle);
            }
            doneChanging = false;
            setHasChanged();
        }
        
    } // class MyMouseWhellListener

}
