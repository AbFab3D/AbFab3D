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

package abfab3d.util;

/**
 * various conversion coefficients 
 *
 * @author Vladimir Bulatov
 */
public class Units{
    
    static public final double CM = 0.01; // cm -> meters
    static public final double MM = 0.001; // mm -> meters
    static public final double MM3 = 1.E-9; // mm^3 -> meters^3
    static public final double FT = 0.304; // ft -> meters
    static public final double IN = 0.0254; // inches -> meters
    static public final double UM = 1.e-6; // micron -> meters
    static public final double PT = IN/72; // points -> meters
    static public final double TORADIANS = Math.PI/180; // degree to radians
    static public final double TODEGREE = 180/Math.PI; // degree to radians    

}
