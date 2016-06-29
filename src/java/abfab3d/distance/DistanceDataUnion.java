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

package abfab3d.distance;

import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.max;
import static abfab3d.core.Output.fmt;

/**
   returns the distance to union of several distance data sources

   @author Vladimir Bulatov
 */
public class DistanceDataUnion implements DistanceData {
            
    Vector<DistanceData> m_vcomponents = new Vector<DistanceData>();
    DistanceData m_components[]; 

    double blendWidth = 0.;

    public DistanceDataUnion(){
    }

    public DistanceDataUnion(DistanceData distData1, DistanceData distData2){
        m_vcomponents.add(distData1);
        m_vcomponents.add(distData2);
        initArray();
    }

    public void setBlendWidth(double value){

        this.blendWidth = value;
    }

    public void add(DistanceData distData){
        m_vcomponents.add(distData);
        initArray();
    }

    protected void initArray(){
        m_components = m_vcomponents.toArray(new DistanceData[m_vcomponents.size()]);
    }

    //
    // return distance to the spherical shell in 3D 
    //
    public double getDistance(double x, double y, double z){

        if(m_components.length != 2) 
            throw new RuntimeException(fmt("upsupported component count: %d in %s",m_components.length,this.getClass()));
        double dist = Double.MAX_VALUE;
        double d0 = m_components[0].getDistance(x,y,z);
        double d1 = m_components[1].getDistance(x,y,z);
        double dd = min(d0, d1);

        double w = blendWidth;

        if(w <= 0.) 
            return dd;
        
        double d = abs(d0-d1);
        if( d < 2*w) return dd - (d - 2*w)*(d - 2*w)/(8*w);
        else return dd;

        /*
        for(int i = 0; i < m_components.length; i++){
            double d = m_components[i].getDistance(x,y,z);
            if(d < dist) 
                dist = d;
        }
        
        return dist;
        */
    }
}

