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

package abfab3d.geomutil;

import static abfab3d.core.Output.fmt;


/**
   representation of 3d point 
   
 */
public class Pnt3 implements APnt{

    
    public double x,y,z;

    public Pnt3(){
    }
    
    public Pnt3(double x,double y,double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    /**
       returns midpoint between pnt and this point 
       @param pnt point at another end of interval 
     */
    public APnt midpoint(APnt apnt){
        Pnt3 pnt = (Pnt3)apnt;
        return new Pnt3(0.5*(x + pnt.x),0.5*(y + pnt.y),0.5*(z + pnt.z));
    }
    
    public double distance(APnt apnt){
        Pnt3 pnt = (Pnt3)apnt;
        double 
            dx = pnt.x - x,
            dy = pnt.y - y,
            dz = pnt.z - z;
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    public String toString(){
        return fmt("%10.7f %10.7f %10.7f",x,y,z);
    }
}