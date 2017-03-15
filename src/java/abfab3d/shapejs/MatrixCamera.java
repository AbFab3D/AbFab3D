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

import abfab3d.param.DoubleParameter;
import abfab3d.param.ObjectParameter;
import abfab3d.param.Parameter;

import javax.vecmath.Matrix4f;

import static abfab3d.core.Output.printf;

/**
 * Camera matrix specified by a 4x4.
 *
 * @author Alan Hudson
 */
public class MatrixCamera extends Camera {

    protected ObjectParameter mp_viewMatrix = new ObjectParameter("viewMatrix", "Inverted View matrix", new Matrix4f());
    protected DoubleParameter mp_cameraAngle = new DoubleParameter("cameraAngle", "cameraAngle", Math.atan(0.5));
    private Parameter m_aparam[] = new Parameter[]{
            mp_viewMatrix, mp_cameraAngle
    };

    public MatrixCamera() {

        addParams(m_aparam);

        Matrix4f mat = new Matrix4f();
        mat.setIdentity();
        setViewMatrix(mat);
    }

    public MatrixCamera(Matrix4f view) {
        addParams(m_aparam);
        
        mp_viewMatrix.setValue(view);
    }

    public MatrixCamera(Matrix4f view, double ang) {
        addParams(m_aparam);

        setViewMatrix(view);
        setCameraAngle(ang);
    }

    /**
     * Sets the inverted view matrix.
     *
     * @param mat
     */
    public void setViewMatrix(Matrix4f mat) {
        mp_viewMatrix.setValue(mat);
    }

    /**
     * Returns current inverted view matrix, the matrix should transform view coord into playbox coord
     */
    public void getViewMatrix(Matrix4f invViewMatrix) {
        invViewMatrix.set((Matrix4f) mp_viewMatrix.getValue());
    }

    public double getCameraAngle() {
        return mp_cameraAngle.getValue();
    }

    public void setCameraAngle(double val) {
        mp_cameraAngle.setValue(val);
    }
}
