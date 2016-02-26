package abfab3d.param.editor;

import abfab3d.param.*;

import javax.swing.*;
import javax.vecmath.Vector3d;

import java.awt.*;

import static abfab3d.util.Units.MM;
import static java.awt.AWTEvent.WINDOW_EVENT_MASK;

public class DevTestEditors extends JFrame {

    public DevTestEditors() {
        super("Parameter Editor");

        int width = 512;
        int height = 512;

        GraphicsEnvironment env =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev;

        enableEvents(WINDOW_EVENT_MASK);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        Component panel = createPanel();

        add(panel);

        setSize(width, height);
        setVisible(true);
    }

    public Component createPanel() {
        TestSphere sphere = new TestSphere();

        ParamPanel panel = new ParamPanel(sphere);

        return panel;
    }

    public static final void main(String[] args) {
        DevTestEditors tester = new DevTestEditors();
    }
}

class TestSphere extends BaseParameterizable {
    Vector3dParameter mp_center = new Vector3dParameter("center", "Center", new Vector3d(0, 0, 0));
    private DoubleParameter mp_radius = new DoubleParameter("radius", "radius of the sphere", 1. * MM);

    Parameter m_aparam[] = new Parameter[]{
            mp_center,
            mp_radius
    };

    public TestSphere() {
        initParams();
    }

    protected void initParams(){
        super.addParams(m_aparam);
    }
}