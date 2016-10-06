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
   class to return color for given index value
   input vaue is rounded to neares int and color from internal array is returned  for that index
   
   
*/
public class ColorMapperIndex implements ColorMapper {

    int colors[] = new int[]{0xFFFF0000,0xFF00FF00,0xFF0000FF,0xFFFF00FF,0xFF00FFFF,0xFF77777,0xFF0077FF,0xFF77FF00,0xFFF0077};
    
    /**
     */
    public ColorMapperIndex(){

    }

    public ColorMapperIndex(int colors[]){
        this.colors = colors;
    }

    public int getColor(double value){

        int v = (int)Math.round(Math.abs(value));
        v %= colors.length;
        return colors[v];
        
    }
}

