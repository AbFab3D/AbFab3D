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


/**
   represent solid color 
 */
public class SolidColor implements DataSource {

    // color components 
    protected double m_r;
    protected double m_g;
    protected double m_b;
    protected double m_a;
    protected int m_channels = 0;


    /**
       solid color with red green blue components 
    */
    public SolidColor(double r, double g, double b) {
        m_r = r;
        m_g = g;
        m_b = b;
        m_channels = 3;
    }

    /**
       solid color with red green blue alpha components 
     */
    public SolidColor(double r, double g, double b, double a) {
        m_r = r;
        m_g = g;
        m_b = b;
        m_a = a;
        m_channels = 4;
    }

    
    public int getChannelsCount(){

        return m_channels;
        
    }

    /**
       
       @override 
       
     */
    public final int getDataValue(Vec pnt, Vec dataValue){
        
        switch(m_channels){
        case 4: 
            dataValue.v[3] = m_a;
            // no break here 
        case 3:
            dataValue.v[0] = m_r;
            dataValue.v[1] = m_g;
            dataValue.v[2] = m_b;
        }
        return RESULT_OK;
    }

}