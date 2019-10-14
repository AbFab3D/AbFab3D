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


// External imports

import abfab3d.core.Initializable;
import abfab3d.core.Units;
import abfab3d.datasources.Sphere;
import abfab3d.shapejs.ColorMaterial;
import abfab3d.shapejs.MatrixCamera;
import abfab3d.shapejs.Scene;
import abfab3d.shapejs.ShapeJSExecutor;

import javax.swing.*;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;

/**
 * An action that measures the distance between two points
 * <p/>
 * Once active it waits for 2 right mouse clicks and then displays the result
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class MeasureAction extends AbstractAction implements MouseListener {
    private static final boolean DEBUG = false;

    private BaseVolumeViewer m_viewer;
    private Component m_comp;
    private int m_clickCount;
    private Vector3d m_start;
    private Vector3d m_end;
    private StatusBar m_sbar;
    private ShapeJSExecutor m_picker;
    private Navigator m_navigator;
    private Navigator m_objNavigator;
    private int matIdx = -1;
    private ColorMaterial redMat = new ColorMaterial(1, 0, 0);

    /**
     * Create an instance of the action class.
     */
    public MeasureAction(Icon icon, String modeString, KeyStroke accelKey, BaseVolumeViewer viewer, ShapeJSExecutor impl) {
        m_picker = impl;
        if (icon != null) {
            putValue(Action.SMALL_ICON, icon);
        } else {
            putValue(Action.NAME, modeString);
        }
        putValue(SHORT_DESCRIPTION, "Measure");
        putValue(ACCELERATOR_KEY, accelKey);

        m_comp = viewer.getRenderComponent();
        m_viewer = viewer;
        m_sbar = viewer.m_statusBar;
    }

    public void setActive(boolean val) {
        if (val) {
            m_comp.addMouseListener(this);
            m_clickCount = 0;
            m_sbar.setStatusText("Measure started, right click start point.");
        } else {
            m_comp.removeMouseListener(this);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (DEBUG) printf("Measure click: %s\n", e);
        switch (e.getButton()) {
            case (MouseEvent.BUTTON3):
                if (m_clickCount == 0) {
                    m_start = getWorldCoord(e.getX(), e.getY());
                    if (DEBUG) printf("Start: %s\n", m_start);
                    if (m_start == null) {
                        m_sbar.setStatusText("Missed object, right click start point");
                        return;
                    }
                    Scene scene = m_viewer.getScene();
                    Sphere obj = new Sphere(m_start, 1 * Units.MM);
                    abfab3d.param.Shape shape = new abfab3d.param.Shape(obj, redMat);
                    ((Initializable) shape).initialize();
                    scene.addShape(shape);

                    m_viewer.getRenderCanvas().forceRender();

                    m_sbar.setStatusText(fmt("Start is: %6.2f %6.2f %6.2f mm.  Right click end point.", m_start.x / Units.MM, m_start.y / Units.MM, m_start.z / Units.MM));
                    m_clickCount++;
                } else if (m_clickCount == 1) {
                    m_end = getWorldCoord(e.getX(), e.getY());
                    if (m_end == null) {
                        m_sbar.setStatusText("Missed object, right click end point");
                        return;
                    }
                    Vector3d len = new Vector3d(m_end);
                    len.sub(m_start);
                    m_sbar.setStatusText(fmt("Measurement: %6.2f mm\n", (len.length() / Units.MM)));
                    m_clickCount = 0;

                    Sphere obj = new Sphere(m_end, 1 * Units.MM);
                    abfab3d.param.Shape shape = new abfab3d.param.Shape(obj, redMat);
                    ((Initializable) shape).initialize();
                    Scene scene = m_viewer.getScene();
                    scene.addShape(shape);
                    m_viewer.getRenderCanvas().forceRender();
                }
                break;
            case (MouseEvent.BUTTON1):
                break;
            case (MouseEvent.BUTTON2):
                break;

        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private Vector3d getWorldCoord(int x, int y) {
        int w = m_comp.getWidth();
        int h = m_comp.getHeight();
        MatrixCamera camera = new MatrixCamera();
        Matrix4f mat = new Matrix4f();
        m_navigator = m_viewer.getViewNavigator();
        m_navigator.getMatrix(mat);
        mat.invert();
        camera.setViewMatrix(mat);

        Matrix4f omat = new Matrix4f();
        m_objNavigator = m_viewer.getObjNavigator();
        m_objNavigator.getMatrix(omat);

        Vector3f pos = new Vector3f();
        Vector3f normal = new Vector3f();
        // TODO: Get screen size
        m_picker.pick(m_viewer.getScene(),camera, omat, x, h - y, w, h, pos, normal, 0.5f);

        if (pos.x <= -10000) {
            // missed
            return null;
        }

        return new Vector3d(pos);
    }

    /**
     * An action has been performed. This is the Go button being pressed.
     * Grab the URL and check with the file to see if it exists first as
     * a local file, and then try to make a URL of it. Finally, if this all
     * works, call the abstract gotoLocation method.
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {
        setActive(true);
    }

    public void close() {
    }
}
