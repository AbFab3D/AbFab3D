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
package abfab3d.core;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;

/**
 * Color data type, 0-1 range.
 *
 * @author Alan Hudson
 */
public class Color implements Cloneable {
    public static final Color black = new Color(0,0,0);
    public static final Color BLACK = black;

    public static final Color white = new Color(1,1,1);
    public static final Color WHITE = white;

    public static final Color red = new Color(1,0,0);
    public static final Color RED = red;

    public static final Color blue = new Color(0,0,1);
    public static final Color BLUE = blue;

    public static final Color green = new Color(0,1,0);
    public static final Color GREEN = green;

    public static final Color yellow = new Color(1,1,0);
    public static final Color YELLOW = yellow;

    public static final Color cyan = new Color(0,1,1);
    public static final Color CYAN = cyan;

    private double r;
    private double g;
    private double b;

    public Color(double r, double g, double b) {
        printf("new color: %f %f %f\n",r,g,b);
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public double getRed() {
        return r;
    }

    public void setRed(double val) {
        r = val;
    }

    public double getGreen() {
        return g;
    }

    public void setGreen(double val) {
        g = val;
    }

    public double getBlue() {
        return b;
    }

    public void setBlue(double val)  {
        b = val;
    }

    /**
     * Convert a hex encoded value to a color.  Null/empty values will be black(0,0,0).
     *
     * @param val
     * @return
     */
    public static Color fromHEX(String val) {
        if (val == null || val.length() == 0) return new Color(0,0,0);

        Integer intval = Integer.decode(val);
        int i = intval.intValue();

        double r = ((i >> 16) & 0xFF) / 255.0;
        double g = ((i >> 8) & 0xFF) / 255.0;
        double b = (i & 0xFF) / 255.0;

        return new Color(r,g,b);
    }

    /**
     * Create a color from an HSV value.
     * @param h Hue value, fractional part is mulitpled by 360 to produce hue angle
     * @param s The saturation, 0-1 value
     * @param v The brightness, 0-1 value
     * @return
     */
    public static Color fromHSV(float h, float s, float v) {
        double r = 0;
        double g = 0;
        double b = 0;

        if (s == 0) {
            r = v;
            g = v;
            b = v;
        } else {
            double hue = (h - Math.floor(h)) * 6.0;
            double f = hue - java.lang.Math.floor(hue);
            double p = v * (1.0 - s);
            double q = v * (1.0 - s * f);
            double t = v * (1.0 - (s * (1.0 - f)));

            switch ((int) hue) {
                case 0:
                    r = v;
                    g = t;
                    b = p;
                    break;
                case 1:
                    r = q;
                    g = v;
                    b = p;
                    break;
                case 2:
                    r = p;
                    g = v;
                    b = t;
                    break;
                case 3:
                    r = p;
                    g = q;
                    b = v;
                    break;
                case 4:
                    r = t;
                    g = p;
                    b = v;
                    break;
                case 5:
                    r = v;
                    g = p;
                    b = q;
                    break;
            }
        }


        return new Color(r,g,b);
    }

    public String toHEX() {
        int ri = (int) Math.round(255 * r);
        int gi = (int) Math.round(255 * g);
        int bi = (int) Math.round(255 * b);

        return fmt("0x%02X%02X%02X",ri,gi,bi);
    }

    public static String toString(Color c) {
        return fmt("Color(%f,%f,%f)",c.getRed(),c.getGreen(),c.getBlue());
    }

    public Color clone() {
        try {
            return (Color) super.clone();
        } catch(CloneNotSupportedException cnse) {
            // Should never happen
            cnse.printStackTrace();
        }

        return null;
    }
}
