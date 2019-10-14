/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package shapejs.viewer;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;


public class Utils {

    public static void setFont(Component comp, Font font){
        
        comp.setFont(font);
        if(comp instanceof Container){            
            Component comps[] = ((Container)comp).getComponents();
            for(int i = 0; i < comps.length; i++){
                setFont(comps[i], font);
            }
        }
    }



}
