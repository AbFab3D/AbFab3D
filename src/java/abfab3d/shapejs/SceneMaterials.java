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

import abfab3d.param.*;

import java.util.ArrayList;
import java.util.List;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;

/**
 * Scene material information
 *
 * @author Alan Hudson
 */
public class SceneMaterials extends BaseParameterizable {
    protected static final int MAX_MATERIALS = 4;
    protected SNodeListParameter mp_materials = new SNodeListParameter("materials");

    public SceneMaterials() {
        ArrayList<RenderingMaterial> mats = new ArrayList<RenderingMaterial>(MAX_MATERIALS);

        for (int i = 0; i < MAX_MATERIALS; i++) {
            mats.add(new DefaultMaterial());
        }
        mp_materials.setValue(mats);

        addParam(mp_materials);
    }

    public RenderingMaterial getRenderingMaterial() {
        return (RenderingMaterial) mp_materials.get(0);
    }

    public void setRenderingMaterial(RenderingMaterial mat) {
        mp_materials.set(0, mat);
    }

    /**
     * set rendering material for given index
     */
    public void setMaterial(int index, RenderingMaterial mat) {

        if (index < 0 || index >= MAX_MATERIALS)
            throw new RuntimeException(fmt("material index: %d is out of range[0,%d]", index, MAX_MATERIALS - 1));

        mp_materials.set(index, mat);
    }

    public void setMaterials(RenderingMaterial[] mats) {
        mp_materials.clear();

        for (int i = 0; i < mats.length; i++) {
            mp_materials.add(mats[i]);
        }
    }

    public List<RenderingMaterial> getMaterials() {
        return mp_materials.getValue();
    }

    public int getNumMaterials() {
        return mp_materials.getValue().size();
    }
}
