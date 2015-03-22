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

import javax.vecmath.Vector3d;

import abfab3d.param.Parameter;
import abfab3d.param.IntParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Vector3dParameter;


import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;
import abfab3d.util.SimplexNoise;
import abfab3d.util.SimplexNoisePerlin;
import abfab3d.util.PerlinNoise3D;


import static abfab3d.util.Units.MM;

/**
   generates Perlin noise of given period and frequency
   
   noise is controlled by gradients vectors defined at the grid of a given size and dimension
   noise value between grid nodes is smoothly interpolated from values and gradients 
   the values are periodicaly replicated to the whole space
 */
public class Noise extends TransformableDataSource {  // Periodic noise in 3D 

    protected double 
        m_scaleX,
        m_scaleY,
        m_scaleZ,
        m_offset,
        m_factor;
        
    protected double m_gradients[];

    //SimplexNoisePerlin m_noise;
    //SimplexNoise m_noise;
    PerlinNoise3D m_noise;

    IntParameter mp_nx = new IntParameter("nx","x-dimension of grid",1);
    IntParameter mp_ny = new IntParameter("ny","y-dimension of grid",1);
    IntParameter mp_nz = new IntParameter("nz","z-dimension of grid",1);
    IntParameter mp_seed = new IntParameter("seed","seed of random number generator",11);
    DoubleParameter mp_offset = new DoubleParameter("offset","offset in (result = value*factor+offset) ",0.);
    DoubleParameter mp_factor = new DoubleParameter("factor","factor in (result = value*factor+offset)",1.);
    Vector3dParameter mp_size = new Vector3dParameter("size","size of grid",new Vector3d(10*MM,10*MM,10*MM));

    Parameter m_aparam[] = new Parameter[]{
        mp_nx,
        mp_ny,
        mp_nz,
        mp_size,
        mp_seed,
        mp_factor,
        mp_offset,
    };

    public Noise(Vector3d size, int nx,int ny,int nz){
        super.addParams(m_aparam);

        mp_size.setValue(size);
        mp_nx.setValue(nx);
        mp_ny.setValue(ny);
        mp_nz.setValue(nz);
    }

    public Noise(Vector3d size, int nx,int ny,int nz, double gradients[]){
        super.addParams(m_aparam);

        mp_size.setValue(size);
        mp_nx.setValue(nx);
        mp_ny.setValue(ny);
        mp_nz.setValue(nz);

        m_gradients = gradients;
    }

    public int initialize(){
        
        super.initialize();

        Vector3d size = (Vector3d)mp_size.getValue();
        int nx = mp_nx.getValue();
        int ny = mp_ny.getValue();
        int nz = mp_nz.getValue();

        m_scaleX = nx/size.x;
        m_scaleY = ny/size.y;
        m_scaleZ = nz/size.z;
        m_offset = mp_offset.getValue();
        m_factor = mp_factor.getValue();

        int seed = mp_seed.getValue();
        
        if(m_gradients != null)
            m_noise = new PerlinNoise3D(nx, ny, nz, m_gradients);
        else 
            m_noise = new PerlinNoise3D(nx, ny, nz, seed);
        
        
        return RESULT_OK;

    }
    
    public double[] getGradients(){
        return m_noise.getGradients();
    }

    public int getDataValue(Vec pnt, Vec value){
        super.transform(pnt);
        double x = pnt.v[0]*m_scaleX;
        double y = pnt.v[1]*m_scaleY;
        double z = pnt.v[2]*m_scaleZ;
        
        //double v = m_noise.turbulence(x,y,z, 4, 0.5);

        double v = (m_noise.get(x,y,z)*m_factor + m_offset);
        value.v[0] = v;
        
        return RESULT_OK;
    }

}

