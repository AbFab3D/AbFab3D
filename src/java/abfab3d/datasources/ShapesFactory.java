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

package abfab3d.datasources;

import abfab3d.param.BaseSNodeFactory;
import abfab3d.param.SNodeFactory;

public class ShapesFactory extends BaseSNodeFactory {

    static final String sm_names[] = new String[]{"Sphere", "Box", "Cylinder", "Cone", "Image3D", "Torus", "Union", "Intersection", "Subtraction", "Complement"};

    static String sm_classNames[];

    static String packName = "abfab3d.datasources.";

    static {
        sm_classNames = new String[sm_names.length];
        for(int i = 0; i < sm_names.length; i++){
            sm_classNames[i] = packName + sm_names[i];
        }
    }

    static ShapesFactory sm_instance;

    public ShapesFactory(){
        super(sm_names,sm_classNames);
    }

    public static SNodeFactory getInstance(){
        if(sm_instance == null) 
            sm_instance = new ShapesFactory();

        return sm_instance;
    }

}