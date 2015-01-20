/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.transforms;

import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.util.Vec;
import abfab3d.util.VecTransform;

import javax.vecmath.Vector3d;

/**
 * Performs translation in space
 */
public class Translation extends BaseTransform implements VecTransform {

    protected double tx = 1, ty = 1, tz = 1;

    /**
     * identity transform
     */
    public Translation() {

        this(0, 0, 0);
    }

    /**
     * translation to given point
     *
     * @param p vector of translation
     */
    public Translation(Vector3d p) {
        this(p.x, p.y, p.z);
    }

    /**
     * translation to given point
     *
     * @param tx x component of translation
     * @param ty y component of translation
     * @param tz z component of translation
     */
    public Translation(double tx, double ty, double tz) {
        initParams();
        setTranslation(tx, ty, tz);
    }

    public void initParams() {
        super.initParams();

        Parameter p = new Vector3dParameter("translation");
        params.put(p.getName(), p);
    }

    /**
     * @noRefGuide
     */
    public void setTranslation(double tx, double ty, double tz) {

        this.tx = tx;
        this.ty = ty;
        this.tz = tz;
        ((Vector3dParameter) params.get("translation")).setValue(new Vector3d(tx, ty, tz)); // broken, commented VB
    }

    /**
     * @noRefGuide
     */
    public int transform(Vec in, Vec out) {

        out.set(in);
        out.v[0] = in.v[0] + tx;
        out.v[1] = in.v[1] + ty;
        out.v[2] = in.v[2] + tz;


        return RESULT_OK;
    }

    /**
     * @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {
        out.set(in);
        out.v[0] = in.v[0] - tx;
        out.v[1] = in.v[1] - ty;
        out.v[2] = in.v[2] - tz;

        return RESULT_OK;

    }
} // class Translation
