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
import abfab3d.util.MathUtil;

/**
   converts mltiple components of Vec into single long attribute 
   each channel uses custom number of bits supplied via bits[] 
   input double values are clamped into [0, 1] interval and are mapped into [0, (1<< bits[])-1] range 
   
   @author Vladimir Bulatov
 */

public class AttributePackerGeneral implements AttributePacker {
    
    int m_length; // length of channels array 
    long  m_resolution[];
    int m_shift[];
    
    // if this is true, and value of first channel is 0, all channels are set to 0 
    // this is usefull to optimize memory used by grid in case if first channel is density or signed distance
    boolean m_controlByDensity = true;
    /**
       
     */
    public AttributePackerGeneral(int bits[]){
        this(bits, true);
    }

    public AttributePackerGeneral(int bits[], boolean controlByDensity){
        m_controlByDensity =  controlByDensity;
        m_length = bits.length;

        m_resolution = new long[m_length];
        m_shift = new int[m_length];

        for(int i = 0, shift = 0; i < m_length; i++){
            m_resolution[i] = MathUtil.getBitMask(bits[i]);
            m_shift[i] = shift;
            shift += bits[i];
        }
    }

    /**
       convert vector of double into long voxel attribute 
       @override 
     */
    public final long makeAttribute(Vec data){

        double v[] = data.v;
        
        // first special channel
        long d = (long)(m_resolution[0] * v[0] + 0.5) ;
        // clamp result 
        if(d < 0) d = 0;
        if(d > m_resolution[0]) d = m_resolution[0];
        

        if(m_controlByDensity && d == 0){
            return 0;
        }
        
        long res = d;
        
        for(int k = 1; k < m_length; k++){
            long rr = m_resolution[k];
            d = (long)(rr * v[k] + 0.5) ;
            // clamp result 
            if(d < 0) d = 0;
            if(d > rr) d = rr;
            
            res |= (d << m_shift[k]);
        }
            
            
        return res;
        
    }

    
    public void getData(long attribute, Vec data){
        throw new RuntimeException("not implemented");
    }


}
