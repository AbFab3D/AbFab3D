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

import abfab3d.param.*;

import javax.vecmath.*;

import static abfab3d.core.Output.printf;

/**
 * Simple camera model.  Supports some view helpers or a direct setting of the view matrix.  A direct set
 * matrix will trump the simple camera params.
 *
 * @author Alan Hudson
 */
public class SimpleCamera extends Camera {
    private static final boolean DEBUG = false;

    protected DoubleParameter mp_viewDistance = new DoubleParameter("viewDistance", "viewDistance");
    protected DoubleParameter mp_cameraAngle = new DoubleParameter("cameraAngle", "cameraAngle");
    protected AxisAngle4dParameter mp_rotation = new AxisAngle4dParameter("rotation","rotation");

    private Parameter m_aparam[] = new Parameter[]{
            mp_viewDistance, mp_cameraAngle,mp_rotation
    };

    // rotation of scene around axis via world center
    protected AxisAngle4d m_sceneRotation = new AxisAngle4d(1,0,0,0);
    protected float m_viewPointZ = 2.5f; // location of viewpoint
    protected double m_cameraAngle = (float) Math.atan(0.5);

    // scratch variables
    protected Matrix4f rot = new Matrix4f();
    protected Matrix4f trans = new Matrix4f();
    protected Vector3f vec = new Vector3f();

    public SimpleCamera() {
        addParams(m_aparam);
    }

    public void setCameraAngle(float angle){
        m_cameraAngle = angle;
    }

    public void setCameraAngle(double angle){
        m_cameraAngle = angle;
    }

    /**
     * Set distance from scene center to viewpoint
     */
    public void setViewpointDistance(double distance){
        m_viewPointZ = (float)distance;
    }

    /**
     * Set distance from scene center to viewpoint
     */
    public void setViewpointDistance(float distance){
        m_viewPointZ = distance;
    }

    /**
     set scene rotation
     */
    public void setRotation(AxisAngle4f rotation){
        if(DEBUG)printf("setRotation(%7.4f,%7.4f,%7.4f,%7.4f)\n", rotation.x, rotation.y, rotation.z, rotation.angle);
        m_sceneRotation.set(rotation);
    }

    public void setRotation(AxisAngle4d rotation){
        if(DEBUG)printf("setRotation(%7.4f,%7.4f,%7.4f,%7.4f)\n", rotation.x, rotation.y, rotation.z, rotation.angle);
        m_sceneRotation.set(rotation);
    }

    /**
     * Returns current inverted view matrix, the matrix should transform view coord into playbox coord
     */
    public void getViewMatrix(Matrix4f invViewMatrix) {
        rot.set(m_sceneRotation);
        vec.set(0,0,m_viewPointZ);
        trans.set(vec);
        invViewMatrix.mul(rot, trans);
    }

    public double getCameraAngle() {
        return m_cameraAngle;
    }
}
