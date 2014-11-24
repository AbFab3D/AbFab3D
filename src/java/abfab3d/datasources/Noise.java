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

import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;
import abfab3d.util.SimplexNoise;
import abfab3d.util.SimplexNoisePerlin;


import static abfab3d.util.Units.MM;

/**
   generates noise of a given scale and value  
 */
public class Noise extends TransformableDataSource {  // Simplex noise in 2D, 3D and 4D

    protected double m_scale = 1*MM;
    protected int m_seed;
    SimplexNoisePerlin m_noise;
    //SimplexNoise m_noise;
    
    public Noise(double scale, int seed){

        m_scale = scale;
        m_noise = new SimplexNoisePerlin(seed);
        //m_noise = new SimplexNoise(seed);
    }

    public int getDataValue(Vec pnt, Vec value){
        
        super.transform(pnt);
        double x = pnt.v[0]/m_scale;
        double y = pnt.v[1]/m_scale;
        double z = pnt.v[2]/m_scale;

        //double v = m_noise.noise(x,y,z);
        //double v = m_noise.noise(x,y,z);
        double v = m_noise.turbulence(x,y,z, 4, 0.5);

        double v0 = 0;
        double vscale = 10;

        value.v[0] = (v-v0)*vscale;
        
        return RESULT_OK;
    }

}
