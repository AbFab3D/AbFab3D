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


/**
   subdivision tester test distance between points 
 */
public class SubdivisionTesterDistance implements SubdivisionTester {


    protected double maxDistance;

    public SubdivisionTesterDistance(double maxDistance){
        this.maxDistance = maxDistance;
    }
    
    /**
       @returns true more subdivison between points is needed 
       @param pnt1 first point 
       @param pnt2 second point 
     */
    public boolean needSubdivision(APnt pnt1, APnt pnt2){
        return (pnt1.distance(pnt2) > maxDistance);
    }

}