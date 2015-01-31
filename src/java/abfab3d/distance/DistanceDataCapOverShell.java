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


/**
   makes cap over shell defined by distanceData between minDist and maxDist

   @author Vladimir Bulatov
*/
public class DistanceDataCapOverShell implements DistanceData {    
    
    DistanceData shellDistance; 
    double minDist; 
    double maxDist; 
    double capHeight;
    
    public DistanceDataCapOverShell(DistanceData shellDistance, double minDist, double maxDist, double capHeight) {
        
        this.shellDistance = shellDistance;
        this.minDist = minDist; 
        this.maxDist = maxDist; 
        this.capHeight = capHeight; 
    }
    
    public double getDistance(double x, double y, double z){
                
        // this may be positive or negative 
        double dist = shellDistance.getDistance(x,y,z);
        
        double tvalue = capHeight*cap(minDist, maxDist, dist);
        return tvalue;
    }

    // smooth cap over interval xmin, xmax
    static final double cap(double xmin, double xmax, double x){
        x -= xmin;
        x /= (xmax-xmin);
        if(x <= 0) return 0;
        if(x >= 1.) return 0;
        if(x > 0.5) x = 1.-x;
        // x between 0 and 0.5 
        // cubic 
        // c(0) = 0, c(0.5) = 1;
        //
        return 4*x*x*(3-4*x);
    }
    
} // DistanceDataCapOverShell

