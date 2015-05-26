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

package abfab3d.datasources;


import java.util.Vector;

import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;


import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;

import abfab3d.util.PointToTriangleDistance;

import static java.lang.Math.sqrt;
import static java.lang.Math.atan2;
import static java.lang.Math.abs;

import static abfab3d.util.Output.printf;


import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.intervalCap;
import static abfab3d.util.MathUtil.step10;
import static abfab3d.util.MathUtil.step01;

import static abfab3d.util.Units.MM;


/**

   return constant data value
   
   @author Vladimir Bulatov

 */
public class Constant extends TransformableDataSource {
    
    private double m_value;
    
    private DoubleParameter  mp_value = new DoubleParameter("value","value of the constant", 0);

    Parameter m_aparam[] = new Parameter[]{
        mp_value,
    };


    /**
     * contant of given vaue
     */
    public Constant(double value){
        addParams(m_aparam);
        mp_value.setValue(value);
    }

    /**
       @noRefGuide
     */
    public int initialize(){
        super.initialize();
        m_value = mp_value.getValue();

        return RESULT_OK;
    }
    /**
     * @noRefGuide

     * returns 1 if pnt is inside of ball
     * returns intepolated value if poiunt is within voxel size to the boundary
     * returns 0 if pnt is outside the ball
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        data.v[0] = m_value;
        super.getMaterialDataValue(pnt, data);
        return RESULT_OK;
    }
    
}  // class Constant

