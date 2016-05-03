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

import java.util.Vector;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.AxisAngle4d;

import abfab3d.param.AxisAngle4dParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;
import abfab3d.util.Vec;
import abfab3d.util.Initializable;
import abfab3d.util.Symmetry;
import abfab3d.util.ReflectionGroup;
import abfab3d.util.VecTransform;
import abfab3d.util.MathUtil;

import net.jafama.FastMath;

import static java.lang.Math.floor;

import static abfab3d.util.Units.MM;
import static abfab3d.util.Output.printf;

/**
   Periodic tiling of space with copies of interor of a single tile - fundamental domain. 
   The fundamental domain is build on basis vectors. The location of fundamnetal domain is controlled by its origin. 
   Depending on count of basis vectors the fundamental domain is 
   <ul>
   <li> Infinite slab orthogonal to single basis vector</li>
   <embed src="doc-files/PeriodicWrap_1D.svg" type="image/svg+xml"/>
   <li> Infinite prizm with parallelogram base build on two basis vectors</li>
   <embed src="doc-files/PeriodicWrap_2D.svg" type="image/svg+xml"/>
   <li> Parallelepiped build on three basis vectors    </li>
   <embed src="doc-files/PeriodicWrap_3D_fd.svg" type="image/svg+xml"/>
   <ul>
   The transformation works by mapping any point in space into corresponding point in the fundamental domain.    

*/
public class PeriodicWrap extends BaseTransform implements Initializable {
    
    private static final boolean DEBUG = false;

    private Vector3d m_d1,m_d2, m_d3; // dual basis vectors
    private Vector3d m_a1, m_a2, m_a3; // basis vectors 
    private Vector3d m_origin;
    private int m_count = 1;
    // arbitratry non parallel vector 
    private final static Vector3d vrnd = new Vector3d(0.12345,0.23456,0.34567);
    
    protected Vector3dParameter  mp_origin = new Vector3dParameter("origin","origin of the tiling",new Vector3d(0,0,0));
    protected Vector3dParameter  mp_a1 = new Vector3dParameter("a1","first basis vector",new Vector3d(10*MM,0,0));
    protected Vector3dParameter  mp_a2 = new Vector3dParameter("a2","second basis vector",null);
    protected Vector3dParameter  mp_a3 = new Vector3dParameter("a3","third basis vector",null);

    protected Parameter m_aparam[] = new Parameter[]{
        mp_origin,
        mp_a1,
        mp_a2,
        mp_a3,
    };
    
    /**
       periodic tiling with single basis vector.
       Fundamental domain is infinite slabs orthogonal to the basis vector 
       @param a1 single basis vector

     */
    public PeriodicWrap(Vector3d a1){
        initParams();
        mp_a1.setValue(a1);
    }
    /**
       Two-periodic tiling formed by two basis vectors.
       Fundamental domain is infinite prism orthogonal to the plane span by vectors a1 and a2. Base of prism is paralelogram build on basis vectors. 
              
       @param a1 first basis vector
       @param a2 second basis vector
       
     */
    public PeriodicWrap(Vector3d a1,Vector3d a2){
        initParams();
        mp_a1.setValue(a1);
        mp_a2.setValue(a2);
    }

    /**
       Three-periodic tiling formed by three basis vectors 
       Fundamental domain is parallelepiped build on tree basis vectors a1, a2, a3
       
       @param a1 first basis vector
       @param a2 second basis vector
       @param a3 third basis vector
       
     */
    public PeriodicWrap(Vector3d a1,Vector3d a2,Vector3d a3){
        initParams();
        mp_a1.setValue(a1);
        mp_a2.setValue(a2);
        mp_a3.setValue(a3);
    }

    /**
       Set origin of the tiling. It is the point in the vertex of the fundamental domain. 
     */
    public void setOrigin(Vector3d val) {
        mp_origin.setValue(val.clone());
    }

    /**
     @noRefGuide
     */
    public void initParams(){
        super.addParams(m_aparam);
    }


    /**
     @noRefGuide
     */
    public Vector3d getOrigin(){
        return m_origin;
    }
    /**
     @noRefGuide
     */
    public Vector3d getA1(){
        return m_a1;
    }
    /**
     @noRefGuide
     */
    public Vector3d getA2(){
        return m_a2;
    }
    /**
     @noRefGuide
     */
    public Vector3d getA3(){
        return m_a3;
    }
    /**
     @noRefGuide
     */
    public Vector3d getD1(){
        return m_d1;
    }
    /**
     @noRefGuide
     */
    public Vector3d getD2(){
        return m_d2;
    }
    /**
     @noRefGuide
     */
    public Vector3d getD3(){
        return m_d3;
    }    
    /**
     @noRefGuide
     */
    public int getCount(){
        return m_count;
    }

    /**
       @noRefGuide
     */
    public int initialize(){

        m_origin = mp_origin.getValue();

        m_a1 = mp_a1.getValue();
        m_a2 = mp_a2.getValue();
        m_a3 = mp_a3.getValue();
        
        m_count = 1;
        if(m_a2 != null){
            m_count = 2;
            if(m_a3 != null){
                m_count = 3;
            }
        }
        switch(m_count) {
        case 1: 
            m_a2 = new Vector3d();
            m_a2.cross(vrnd,m_a1);
            m_a2.normalize();
            // no break here 
        case 2: 
            m_a3 = new Vector3d(); 
            m_a3.cross(m_a1, m_a2); 
            m_a3.normalize();
        default: 
            // do nothing 
        }
                       
        // volume of fundamental domain
        double norm = 1./MathUtil.tripleProduct(m_a1, m_a2, m_a3);
        // dual basis vectors
        m_d1 = new Vector3d();
        m_d1.cross(m_a2, m_a3);
        m_d1.scale(norm);
        m_d2 = new Vector3d();
        m_d2.cross(m_a3, m_a1);
        m_d2.scale(norm);
        m_d3 = new Vector3d();
        m_d3.cross(m_a1, m_a2);
        m_d3.scale(norm);
        if(DEBUG){
            printf("dimension: %d\n", m_count);
            printf("a1: (%7.5f,%7.5f,%7.5f)\n", m_a1.x,m_a1.y,m_a1.z);
            printf("a1: (%7.5f,%7.5f,%7.5f)\n", m_a1.x,m_a1.y,m_a1.z);
            printf("a2: (%7.5f,%7.5f,%7.5f)\n", m_a2.x,m_a2.y,m_a2.z);
            printf("a3: (%7.5f,%7.5f,%7.5f)\n", m_a3.x,m_a3.y,m_a3.z);
            printf("d1: (%7.5f,%7.5f,%7.5f)\n", m_d1.x,m_d1.y,m_d1.z);
            printf("d2: (%7.5f,%7.5f,%7.5f)\n", m_d2.x,m_d2.y,m_d2.z);
            printf("d3: (%7.5f,%7.5f,%7.5f)\n", m_d3.x,m_d3.y,m_d3.z);
        }
            
        return RESULT_OK;
    }
    
    /**
       @noRefGuide
     */
    public int transform(Vec in, Vec out) {
        
        out.set(in);               
        return RESULT_OK;
    }
    
    /**
       @noRefGuide
     */
    public int inverse_transform(Vec in, Vec out) {
        
        out.set(in);

        double v[] = out.v;

        double 
            x = v[0] - m_origin.x,
            y = v[1] - m_origin.y,
            z = v[2] - m_origin.z;
        double 
            c1 = (x*m_d1.x + y*m_d1.y + z*m_d1.z),
            c2 = (x*m_d2.x + y*m_d2.y + z*m_d2.z),
            c3 = (x*m_d3.x + y*m_d3.y + z*m_d3.z);            
        
        // move point to fundamental domain in each dimension 
        switch(m_count){
        case 3: 
            c3 -= floor(c3); 
            // no break here 
        case 2: 
            c2 -= floor(c2);
            // no break here 
        case 1: 
            c1 -= floor(c1);            
        }
        
        v[0] = c1*m_a1.x + c2*m_a2.x + c3*m_a3.x + m_origin.x;
        v[1] = c1*m_a1.y + c2*m_a2.y + c3*m_a3.y + m_origin.y;
        v[2] = c1*m_a1.z + c2*m_a2.z + c3*m_a3.z + m_origin.z;

        return RESULT_OK;
        
    }
    
} // class Rotation
