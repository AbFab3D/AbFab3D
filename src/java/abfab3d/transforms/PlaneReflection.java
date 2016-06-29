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

import javax.vecmath.Vector3d;

import abfab3d.core.ResultCodes;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;

import abfab3d.core.Vec;
import abfab3d.core.Initializable;
import abfab3d.core.VecTransform;

import static abfab3d.core.Output.printf;
import static abfab3d.util.Symmetry.toFundamentalDomain;



/**
   Reflection in a plane 
   
*/
public class PlaneReflection  extends BaseTransform implements VecTransform, Initializable  {
    
    private double m_nx, m_ny, m_nz, m_dist;    

    Vector3dParameter  mp_normal = new Vector3dParameter("normal","plane's external normal",new Vector3d(1,0,0));
    DoubleParameter  mp_dist = new DoubleParameter("dist","dsitance to plane from origin",0);

    Parameter m_aparam[] = new Parameter[]{
        mp_dist,
        mp_normal,
    };

    
    /**
       Plane via origin is defined via external normal. 
     */
    public PlaneReflection(Vector3d normal){

        this(normal,0);

    }

    /**
     * Plane is defined via external normal and distance along normal from origin.
     *
     * @param normal The normal to the plane
     * @param distance The distance to the plane
     */
    public PlaneReflection(Vector3d normal, double distance){
        initParams();
        normal.normalize();
        mp_normal.setValue(normal);
        mp_dist.setValue(distance);

    }
    
    /**
     * Plane is defined via external normal and a point, which lies in the plane
     *
     * @param normal The normal to the plane
     * @param pointOnPlane the point on the plane
     */
    public PlaneReflection(Vector3d normal, Vector3d pointOnPlane){
        initParams();

        normal.normalize();
        mp_normal.setValue(normal);
        mp_dist.setValue(normal.dot(pointOnPlane));

    }

    /**
     * Plane is defined via 3 points, which lie in the plane. 
     External normal points into direction from which points pnt0, pnt1, pnt2 look oriented counter clock wise
     *
     * @param pnt0 point in the plane
     * @param pnt1 point in the plane
     * @param pnt2 point in the plane
     */
    public PlaneReflection(Vector3d pnt0, Vector3d pnt1, Vector3d pnt2 ){
        initParams();

        Vector3d v1 = new Vector3d(pnt1);
        Vector3d v2 = new Vector3d(pnt2);
        v1.sub(pnt0);
        v2.sub(pnt0);
        Vector3d nn = new Vector3d();
        nn.cross(v1, v2);
        nn.normalize();

        mp_normal.setValue(nn);
        mp_dist.setValue(nn.dot(pnt0));

    }

    /**
     * Plane is defined via components of normal and distance from origin
     *
     * @param nx x component of normal 
     * @param ny y component of normal 
     * @param nz z component of normal 

     * @param dist distance from plane to origin
     */
    public PlaneReflection(double nx, double ny, double nz, double dist){

        this(new Vector3d(nx, ny, nz), dist);
        
    }


    /**
       @noRefGuide
     */
    protected void initParams(){
        super.addParams(m_aparam);
    }

    /**
       @noRefGuide
     */
    public int initialize(){

        Vector3d normal = mp_normal.getValue();

        m_nx = normal.x;
        m_ny = normal.y;
        m_nz = normal.z;
        m_dist = mp_dist.getValue();

        return ResultCodes.RESULT_OK;
    }
    
    
    /**
       @noRefGuide
     */
    public int transform(Vec in, Vec out) {

        out.set(in);
        double x = in.v[0];
        double y = in.v[1];
        double z = in.v[2];
                
        double dot = 2*((x-m_nx*m_dist)*m_nx + (y-m_ny*m_dist)*m_ny + (z-m_nz*m_dist)*m_nz);
        
        x -= dot*m_nx;
        y -= dot*m_ny;
        z -= dot*m_nz;
                
        out.v[0] = x;
        out.v[1] = y;
        out.v[2] = z;
        
        return ResultCodes.RESULT_OK;
    }                
    
    /**
       @noRefGuide
     *  inverse transform is identical to direct transform
     */
    public int inverse_transform(Vec in, Vec out) {
        
        return transform(in, out);
        
    }
} // class PlaneReflection

