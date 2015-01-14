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

import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;

import static java.lang.Math.sqrt;
import static java.lang.Math.abs;


/**
   returns the distance to intersection of several distance data sources

   @author Vladimir Bulatov
 */
public class DistanceDataIntersection implements DistanceData {
            
    Vector<DistanceData> m_vcomponents = new Vector<DistanceData>();
    DistanceData m_components[]; 

    public DistanceDataIntersection(){
    }

    public DistanceDataIntersection(DistanceData distData1, DistanceData distData2){
        m_vcomponents.add(distData1);
        m_vcomponents.add(distData2);
        initArray();
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

        double dist = Double.MIN_VALUE;

        for(int i = 0; i < m_components.length; i++){
            double d = m_components[i].getDistance(x,y,z);
            if(d > dist) 
                dist = d;
        }

        return dist;
    }
}

