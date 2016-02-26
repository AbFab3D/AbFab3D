package abfab3d.param.editor;

import abfab3d.param.*;

import javax.vecmath.Vector3d;

import static abfab3d.util.Units.MM;

public class DevTestEditors {

    public static void showPanel() {
        TestSphere sphere = new TestSphere();

        ParamPanel panel = new ParamPanel(sphere);
    }

    public static final void main(String[] args) {
        showPanel();
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