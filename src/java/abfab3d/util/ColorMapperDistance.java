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


import abfab3d.core.MathUtil;

/**
   class to calculate color for given densitry value
   densty is expacted in the range (0,1)
   the density is shown as stripes of changing colors from color0 to color1
   
*/
public class ColorMapperDistance implements ColorMapper {

    int m_intColor0;
    int m_intColor1;

    int m_extColor0;
    int m_extColor1;

    double m_stripeWidth;

    /**
       makes color mapper to color distance function
       @param intColor0  first color of stripe for interior (negative) distances 
       @param intColor1  second color of stripe for interior (negative) distances 
       @param extColor0  first color of stripe for exteror (posituve) distances 
       @param extColor1  second color of stripe for exterior (positive) distances 
       @param stripeWidth widh of color stripe (in meters) 
     */
    public ColorMapperDistance(int intColor0, int intColor1, int extColor0, int extColor1, double stripeWidth){

        m_intColor0 = intColor0;
        m_intColor1 = intColor1;
        m_extColor0 = extColor0;
        m_extColor1 = extColor1;

        m_stripeWidth = stripeWidth;

    }

    public int getColor(double value){

        double v = value / m_stripeWidth;
        
        if(v >= 0.0) {
            return ImageUtil.lerpColors(m_extColor0, m_extColor1, MathUtil.frac(v));
        } else {
            return ImageUtil.lerpColors(m_intColor0, m_intColor1, MathUtil.frac(-v));
        }
        
    }
}

