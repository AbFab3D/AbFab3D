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

package abfab3d.transforms;


/**
   list nodes whiuch can be used as for Warp nodes
*/
public class WarpFactory {

    static final String sm_names[] = new String[]{"Noise"};

    static String sm_classNames[];

    static String packName = "abfab3d.datasources.";

    static {
        sm_classNames = new String[sm_names.length];
        for(int i = 0; i < sm_names.length; i++){
            sm_classNames[i] = packName + sm_names[i];
        }
    }


    public static String[] getNames(){
        return sm_names;
    }
    public static String[] getClassNames(){
        return sm_classNames;
    }
}