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
   return linear combination of two distance data cA * distA + cB * distB
   @author Vladimir Bulatov
 */
public class DistanceDataCombination implements DistanceData {
        
    DistanceData distA, distB; 
    double cA, cB;

    public DistanceDataCombination(DistanceData distA, DistanceData distB, double cA, double cB){
        this.cA  = cA;
        this.cB  = cB;
        this.distA  = distA;
        this.distB  = distB;
    }
    //
    //
    public double getDistance(double x, double y, double z){
        double da = distA.getDistance(x,y,z);
        double db = distB.getDistance(x,y,z);
        return da * cA + db * cB;
    }
}
