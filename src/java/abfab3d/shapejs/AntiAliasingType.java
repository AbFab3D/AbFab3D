/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.shapejs;

/**
 * Types of antialiasing available
 *
 * @author Alan Hudson
 */
public enum AntiAliasingType {
    NONE, SUPER_2X2, SUPER_2X2ROT, SUPER_3X3, SUPER_4X4, SUPER_5X5, SUPER_6X6, SUPER_7X7, SUPER_8X8;

    public static int getNumSamples(AntiAliasingType type) {
        switch(type) {
            case NONE:
                return 1;
            case SUPER_2X2:
            case SUPER_2X2ROT:
                return 2;
            case SUPER_3X3:
                return 3;
            case SUPER_4X4:
                return 4;
            case SUPER_5X5:
                return 5;
            case SUPER_6X6:
                return 6;
            case SUPER_7X7:
                return 7;
            case SUPER_8X8:
            default:
                return 8;
        }
    }
}
