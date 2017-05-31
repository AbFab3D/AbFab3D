/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2011
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.transforms;

import abfab3d.core.ResultCodes;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.core.Initializable;
import abfab3d.core.Vec;
import abfab3d.core.VecTransform;

import javax.vecmath.Vector3d;


/**
 * Performs scaling by given factor
 */
public class Scale extends BaseTransform implements VecTransform, Initializable {

    protected double sx = 1., sy = 1., sz = 1.;
    protected double cx = 0., cy = 0., cz = 0.;
    protected double averageScale = 1.;

    protected Vector3dParameter mp_center = new Vector3dParameter("center", "center of scale", new Vector3d(0, 0, 0));
    protected Vector3dParameter mp_scale = new Vector3dParameter("scale", "amount of scale", new Vector3d(1, 1, 1));

    protected Parameter m_aparam[] = new Parameter[]{
            mp_scale,
            mp_center
    };


    /**
     * Identity scale
     */
    public Scale() {
        this(1, 1, 1);
    }

    /**
     * Uniform scaling
     * @param s uniform scaling factor
     */
    public Scale(double s) {
        this(s, s, s);
    }

    /**
     * Non uniform scaling
     * @param sx x axis scaling factor
     * @param sy y axis scaling factor
     * @param sz z axis scaling factor
     */
    public Scale(double sx, double sy, double sz) {
        addParams(m_aparam);
        setScale(sx, sy, sz);
    }

    /**
     * Arbitary Scale
     *
     * @param p vector of scale
     */
    public Scale(Vector3d p) {
        addParams(m_aparam);
        setScale(p.x, p.y, p.z);
    }

    /**
     * Set a uniform scale
     * @param s
     */
    public void setScale(double s) {

        mp_scale.setValue(new Vector3d(s, s, s));
    }

    /**
     * Set an arbitrary scale
     * @param sx
     * @param sy
     * @param sz
     */
    public void setScale(double sx, double sy, double sz) {

        mp_scale.setValue(new Vector3d(sx, sy, sz));

    }

    /**
     * Set an arbitrary scale
     * @param s
     */
    public void setScale(Vector3d s) {
        mp_scale.setValue(s.clone());
    }

    /**
     * @noRefGuide
     * @return
     */
    public int initialize() {

        Vector3d scale = mp_scale.getValue();
        this.sx = scale.x;
        this.sy = scale.y;
        this.sz = scale.z;

        Vector3d center = mp_center.getValue();
        this.cx = center.x;
        this.cy = center.y;
        this.cz = center.z;

        this.averageScale = Math.pow(Math.abs(sx * sy * sz), 1. / 3);
        return ResultCodes.RESULT_OK;
    }

    /**
     @noRefGuide
     */
    public int transform(Vec in, Vec out) {

        out.set(in);

        out.v[0] = in.v[0] * sx;
        out.v[1] = in.v[1] * sy;
        out.v[2] = in.v[2] * sz;

        out.mulScale(averageScale);

        return ResultCodes.RESULT_OK;
    }

    /**
     @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {

        out.set(in);

        double x = in.v[0];
        double y = in.v[1];
        double z = in.v[2];

        x -= cx;
        y -= cy;
        z -= cz;

        x /= sx;
        y /= sy;
        z /= sz;

        x += cx;
        y += cy;
        z += cz;

        out.v[0] = x;
        out.v[1] = y;
        out.v[2] = z;

        out.mulScale(1 / averageScale);

        return ResultCodes.RESULT_OK;

    }

} // class Scale
