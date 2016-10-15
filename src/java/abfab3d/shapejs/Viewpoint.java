/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2016
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.shapejs;

import abfab3d.param.AxisAngle4dParameter;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.Parameter;
import abfab3d.param.StringParameter;
import abfab3d.param.Vector3dParameter;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Vector3d;

/**
 * A viewpoint for setting camera settings.
 *
 * @author Alan Hudson
 */
public class Viewpoint extends BaseParameterizable {
    private StringParameter mp_name = new StringParameter("name", "Default Camera");
    private Vector3dParameter mp_position = new Vector3dParameter("position", "Position", new Vector3d(0, 0, -3));
    private AxisAngle4dParameter mp_rotation = new AxisAngle4dParameter("rotation", "Rotation", new AxisAngle4d(0, 0, 1, 0));

    private Parameter m_aparam[] = new Parameter[]{
            mp_name,
            mp_position,
            mp_rotation
    };

    public Viewpoint() {
        initParams();
    }

    public Viewpoint(String name, Vector3d position, AxisAngle4d rotation) {
        initParams();
        setName(name);
        setPosition(position);
        setRotation(rotation);
    }

    protected void initParams() {
        super.addParams(m_aparam);
    }

    public Vector3d getPosition() {
        return mp_position.getValue();
    }

    public void setPosition(Vector3d position) {
        mp_position.setValue((Vector3d) position.clone());
    }

    public AxisAngle4d getRotation() {
        return mp_rotation.getValue();
    }

    public void setRotation(AxisAngle4d rotation) {
        mp_rotation.setValue((AxisAngle4d) rotation.clone());
    }

    public void setName(String name) {
        mp_name.setValue(name);
    }

    public String getName() {
        return mp_name.getValue();
    }

}
