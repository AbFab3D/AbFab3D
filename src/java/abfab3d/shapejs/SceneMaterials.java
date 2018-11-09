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
package abfab3d.shapejs;

import abfab3d.core.Material;
import abfab3d.param.*;

import java.util.ArrayList;
import java.util.List;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;

/**
 * Scene material information
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov 
 */
public class SceneMaterials extends BaseParameterizable {

    //public static final int MAX_MATERIALS = 4;
    protected SNodeListParameter mp_materials = new SNodeListParameter("materials");

    public SceneMaterials() {

        ArrayList<Material> mats = new ArrayList<Material>();
        mp_materials.setValue(mats);
        addParam(mp_materials);

    }

    /**
       @return index of that material 
     */
    public int addMaterial(Material material) {

        for(int i = 0; i <  mp_materials.size(); i++){
            // found old 
            if(mp_materials.get(i) == material) return i;
        }
        // not found - new material 
        mp_materials.add((Parameterizable)material);

        return mp_materials.size()-1;
    }

    public void setMaterials(ArrayList<Material> mats) {

        mp_materials.clear();

        for (int i = 0; i < mats.size(); i++) {
            mp_materials.add((Parameterizable)mats.get(i));
        }
    }
    
    public void clear(){

        mp_materials.clear();

    }

    /**
     * Removes a material.  Empty slots will be filled with DefaultMaterial
     * @param mat
     */
    public void removeMaterial(Material mat) {
        ArrayList<Material> mats =(ArrayList<Material>) mp_materials.getValue();

        mats.remove(mat);

    }

    public List<Material> getMaterials() {
        return mp_materials.getValue();
    }

    public int getNumMaterials() {
        return mp_materials.getValue().size();
    }
}
