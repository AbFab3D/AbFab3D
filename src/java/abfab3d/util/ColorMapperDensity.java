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
   class to calculate color from given densitry value
   densty is expacted in the range (0,1)
   the density is shown as stripes of changing colors from color0 to color1
   value 0.0 corresponds to color0
   value 1.0 corresponds to color1
   if stripeWidth is supplied the values changes will be shown with stripes of stripe width 




    |       /      /       color1
    |     / |    / |  
    |   /   |  /   |
    | /     |/     |       color0  
    |.......|......|..........................
      stripe 
       width 

*/
public class ColorMapperDensity implements ColorMapper {

    static final int COLOR_BLACK = 0xFF000000;
    static final int COLOR_WHITE = 0xFFFFFFFF;

    int m_color0;
    int m_color1;
    double m_stripeWidth;
    
    
    public ColorMapperDensity(){
        this(COLOR_BLACK, COLOR_WHITE, 1.);
    }

    public ColorMapperDensity(int color0, int color1){
        this(color0, color1, 1.);
    }

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

