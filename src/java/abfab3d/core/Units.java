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

package abfab3d.core;

/**
 * various conversion coefficients 
 *
 * @author Vladimir Bulatov
 */
public class Units{
    static public final double CM = 0.01; // cm -> meters
    static public final double MM = 0.001; // mm -> meters
    static public final double MM3 = 1.E-9; // mm^3 -> meters^3
    static public final double CM3 = 1.E-6; // cm^3 -> meters^3
    static public final double MM2 = 1.E-6; // mm^2 -> meters^2
    static public final double CM2 = 1.E-4; // cm^2 -> meters^2
    static public final double FT = 0.304; // ft -> meters
    static public final double IN = 0.0254; // inches -> meters
    static public final double UM = 1.e-6; // micron -> meters
    static public final double PT = IN/72; // points -> meters
    static public final double TORADIANS = Math.PI/180; // degree to radians
    static public final double TODEGREE = 180/Math.PI; // degree to radians    
    static public final double NS2MS = 1.e-6; // conversion from nano to milliseconds

    /**
     * Helper for converting units
     *
     * @param val  The value in inUnit units
     * @param inUnit
     * @param outUnit
     * @return The value in outUnit units
     */
    public double convertToUnit(double val, UnitName inUnit, UnitName outUnit) {
        double mval;

        switch(inUnit) {
            case M:
                mval = val;
                break;
            case CM:
                mval = val / CM;
                break;
            case MM:
                mval = val / MM;
                break;
            case M2:
                mval = val;
                break;
            case CM2:
                mval = val / CM2;
                break;
            case M3:
                mval = val;
                break;
            case MM3:
                mval = val / MM3;
                break;
            case CM3:
                mval = val / CM3;
                break;
            default: throw new IllegalArgumentException("Unsupported unit: " + inUnit);
        }

        switch(outUnit) {
            case M:
                return mval;
            case CM:
                return mval * CM;
            case MM:
                return mval * MM;
            case M2:
                return mval;
            case CM2:
                return mval * CM2;
            case M3:
                return mval;
            case MM3:
                return mval * MM3;
            case CM3:
                return mval * CM3;
            default: throw new IllegalArgumentException("Unsupported unit: " + inUnit);
        }
    }
}
