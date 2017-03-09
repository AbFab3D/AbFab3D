/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2017
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.shapejs;

import abfab3d.core.Material;
import abfab3d.core.PrintableMaterial;

import java.util.HashMap;
import static abfab3d.core.Output.printf;

/**
 * Created by giles on 3/7/2017.
 */
public class PrintableMaterials {
    private static HashMap<String, PrintableMaterial> mats = new HashMap<>();

    public static void add(String name, PrintableMaterial mat) {
        if (mats.containsKey(name)) {
            throw new IllegalArgumentException("Cannot redefine existing materials");
        }

        mats.put(name,mat);
    }

    public static Material get(String name) {
        printf("Gertting impl for: %s\n",name);
        Material mat = mats.get(name);
        if (mat == null) {
            printf("Cannot find material: %s, mapping to default.",name);
            mat = DefaultMaterial.getInstance();
        }

        return mat;
    }

    /**
     * Get the names of all the materials.
     * @return
     */
    public static String[] getAllNames() {
        // TODO: make real
        return new String[] {"None","White","WSF","SS", "BSF", "RBSF"};
    }

    /**
     * Get the names of all the materials by family names
     * @return
     */
    public static String[] getAllNames(String[] familyNames) {
        // TODO: make real
        return new String[] {"None","White","WSF","SS", "BSF", "RBSF"};
    }
}
