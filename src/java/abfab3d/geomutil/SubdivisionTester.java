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
   interface to represent tester of distance between two points 
   @author Vladimir Bulatov 
 */
public interface SubdivisionTester {

    /**
       @returns true more subdivison between points is needed 
       @param pnt1 first point 
       @param pnt2 second point 
     */
    public boolean needSubdivision(APnt pnt1, APnt pnt2);
}