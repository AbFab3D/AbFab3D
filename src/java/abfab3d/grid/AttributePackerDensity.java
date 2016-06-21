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

package abfab3d.grid;

import abfab3d.util.Vec;

/**
   converts first component of Vec into long voxel attribute 
   using specified resolution

   @author Vladimir Bulatov
 */

public class AttributePackerDensity implements AttributePacker {

    long  m_resolution;
    double m_scale;

    /**
       @param resoulution value whuch corrensponds to density value 1
     */
    public AttributePackerDensity(long resolution){

        m_resolution = resolution;
        m_scale = 1./resolution;
    }
    /**
       convert vector of double into long voxel attribute 
       @override 
     */
    public final long makeAttribute(Vec data){

        long d = (long)(m_resolution * data.v[0] + 0.5);
        // clamp result 
        if(d < 0) d = 0;
        if(d > m_resolution) d = m_resolution;
            
        return d;
        
    }

    public void getData(long attribute, Vec data){
        data.v[0] = m_scale * attribute;

    }

}
