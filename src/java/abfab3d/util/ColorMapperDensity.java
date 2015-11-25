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
   class to calculate color fro given densitry value
   densty is expacted in the range (0,1)
   the density is shown as stripes of changing colors from color0 to color1
   
*/
public class ColorMapperDensity implements ColorMapper {

    int m_color0;
    int m_color1;
    double m_stripeWidth;
    
    public ColorMapperDensity(int color0, int color1, double stripeWidth){

        m_color0 = color0;
        m_color1 = color1;
        m_stripeWidth = stripeWidth;

    }

    public int getColor(double value){

        value = MathUtil.clamp(value, 0., 1)/m_stripeWidth;
        
        return ImageUtil.lerpColors(m_color0, m_color1, value);

    }
}

