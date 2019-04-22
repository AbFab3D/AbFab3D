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
import java.util.LinkedHashMap;

import static abfab3d.core.Output.printf;

/**
 * Factory for all the materials in the system
 *
 * @author Alan Hudson
 */
public class Materials {
    static final boolean DEBUG = false;
    private static LinkedHashMap<String, Material> mats = new LinkedHashMap<>();
    private static String[] allMaterialNames;
    private static String[] allMaterialLabels;

    public static void add(String name, Material mat) {
        if (mats.containsKey(name)) {
            throw new IllegalArgumentException("Cannot redefine existing materials");
        }

        mats.put(name,mat);
    }

    public static Material get(String name) {
        Material mat = mats.get(name);
        if (mat == null) {
            if(DEBUG)printf("Cannot find material: %s, mapping to default.\n",name);
            mat = DefaultMaterial.getInstance();
        }

        return mat;
    }

    /**
     * Get the names of all the materials.
     * @return
     */
    public static String[] getAllNames() {
        if (allMaterialNames == null) {
            allMaterialNames = new String[mats.size() + 4];

            int idx =0;
            allMaterialNames[idx++] = "None";
            allMaterialNames[idx++] = "SingleColor";
            allMaterialNames[idx++] = "FullColor";
            allMaterialNames[idx++] = "MixedMaterial";

            for(String st : mats.keySet()) {
                allMaterialNames[idx++] = st;
            }
        }

        return allMaterialNames;
    }

    /**
     * Get the names of all the materials.
     * @return
     */
    public static String[] getAllLabels() {
        if (allMaterialLabels == null) {
            allMaterialLabels = new String[mats.size() + 1];

            allMaterialLabels[0] = "None";

            int idx = 1;
            for(Material st : mats.values()) {
                allMaterialLabels[idx++] = st.getLabel();
            }
        }

        return allMaterialLabels;
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
