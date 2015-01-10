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
import java.awt.*;
import java.awt.event.*;

import static abfab3d.util.Output.printf;

/**
 * Examine mode navigation.  Middle mouse zooms in and out.  Left rotates around
 * the model.  Right pans.
 *
 * @author Alan Hudson
 */
public class ExamineNavigator implements Navigator {
    private static final float[] DEFAULT_TRANS = new float[] {0,0,-4};
    private static final float[] DEFAULT_ROT = new float[] {0,0,0,0};
    private float z = DEFAULT_TRANS[2];
    private float rotx = 0;
    private float roty = 0;

    private Point dragstart;
    private enum MOUSE_MODE { DRAG_ROTATE, DRAG_ZOOM }
    private MOUSE_MODE dragmode = MOUSE_MODE.DRAG_ROTATE;
    private transient boolean hasChanged = false;

    private Vector3f trans = new Vector3f();
    private Matrix4f tmat = new Matrix4f();
    private Matrix4f rxmat = new Matrix4f();
    private Matrix4f rymat = new Matrix4f();

    public void init(Component component) {
        initMouseListeners(component);
    }

    private void initMouseListeners(Component component) {
        component.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {

                if (dragstart != null) {
                    switch (dragmode) {
                        case DRAG_ROTATE:
                            rotx += (e.getY() - dragstart.getY()) / 20.f;
                            roty += (e.getX() - dragstart.getX()) / 20.f;
                            hasChanged = true;
                            break;
                        case DRAG_ZOOM:
                            z += (e.getY() - dragstart.getY()) / 5.0f;
                            hasChanged = true;
                            break;
                    }
                }

                dragstart = e.getPoint();
            }
        });
        component.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                z += e.getWheelRotation()*0.03;
                hasChanged = true;
            }

        });
        component.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                switch (e.getButton()) {
                    case (MouseEvent.BUTTON1):
                        dragmode = MOUSE_MODE.DRAG_ROTATE;
                        break;
                    case (MouseEvent.BUTTON2):
                        dragmode = MOUSE_MODE.DRAG_ZOOM;
                        break;
                    case (MouseEvent.BUTTON3):
                        dragmode = MOUSE_MODE.DRAG_ZOOM;
                        break;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                switch (e.getButton()) {
                    case (MouseEvent.BUTTON1):
                        dragmode = MOUSE_MODE.DRAG_ZOOM;
                        break;
                    case (MouseEvent.BUTTON2):
                        dragmode = MOUSE_MODE.DRAG_ROTATE;
                        break;
                    case (MouseEvent.BUTTON3):
                        dragmode = MOUSE_MODE.DRAG_ROTATE;
                        break;
                }

                dragstart = null;
            }
        });

        component.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == 'r') {
                    printf("Reseting view\n");
                    // reset navigation
                    z = DEFAULT_TRANS[2];
                    rotx = DEFAULT_ROT[0];
                    roty = DEFAULT_ROT[1];
                    hasChanged = true;
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }


    @Override
    public boolean hasChanged() {
        boolean ret_val = hasChanged;

        hasChanged = false;

        return ret_val;
    }

    @Override
    public void getViewMatrix(Matrix4f mat) {
        trans.setZ(z);
        tmat.set(trans,1.0f);

        rxmat.rotX(rotx);
        rymat.rotY(roty);

        mat.mul(tmat,rxmat);
        mat.mul(rymat);
    }

    @Override
    public void setBounds(Bounds bounds, double vs) {

    }}
