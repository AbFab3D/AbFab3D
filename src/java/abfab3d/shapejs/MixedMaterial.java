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

import java.util.ArrayList;

import abfab3d.core.Color;
import abfab3d.core.DataSource;
import abfab3d.core.Material;
import abfab3d.core.MaterialShader;
import abfab3d.core.MaterialType;

import abfab3d.param.EnumParameter;
import abfab3d.param.Parameter;
import abfab3d.param.StringParameter;

/**
 * Mixed material. 
 * Contains an array of physical material with different properties 
 *
 * @author Vladimir Bulatov 
 */
public class MixedMaterial extends BasePrintableMaterial{

    ArrayList<Material> m_components = new ArrayList<Material>();

    static final String NAME = "MixedMaterial";

    public MixedMaterial(Material baseMaterial) {

        this(NAME,baseMaterial);

    }

    public MixedMaterial(String name, Material baseMaterial) {
        this(name,NAME,baseMaterial);
    }

    public MixedMaterial(String name, String label, Material baseMaterial) {

        super(name,label);
        setMaterialType(MaterialType.MIXED_MATERIAL);
        addComponent(baseMaterial);
        
    }

    public int getComponentCount(){
        return m_components.size();
    }

    public Material getComponent(int index){
        return m_components.get(index);
    }

    public void addComponent(Material material){
        m_components.add(material);
    }

    public MaterialShader getShader() {

        return m_components.get(0).getShader();

    }

}
