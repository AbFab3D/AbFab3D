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

import abfab3d.core.MaterialType;
import abfab3d.core.PrintableMaterial;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.EnumParameter;
import abfab3d.param.Parameter;
import abfab3d.param.SNodeParameter;
import abfab3d.core.DataSource;
import abfab3d.param.StringParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base implementation for all Materials
 *
 * @author Alan Hudson
 */
public abstract class BasePrintableMaterial extends BaseParameterizable implements PrintableMaterial {
    protected SNodeParameter mp_renderingParams = new SNodeParameter("renderingParams");
    protected SNodeParameter mp_source = new SNodeParameter("source");
    protected StringParameter m_name = new StringParameter("name","Unnamed Material");
    protected StringParameter m_alternateName = new StringParameter("alternateName", null);
    protected StringParameter m_label = new StringParameter("label","Unlabeled Material");
    protected EnumParameter m_matType = new EnumParameter("materialType", MaterialType.getStringValues(),MaterialType.SINGLE_MATERIAL.toString());

    private Parameter m_aparam[] = new Parameter[]{
            mp_renderingParams, mp_source, m_name, m_alternateName, m_matType
    };

    protected List<String> m_alternateNames = new ArrayList<>();

    public BasePrintableMaterial(String name) {
        m_name.setValue(name);
        addParams(m_aparam);
    }

    public BasePrintableMaterial(String name, String label) {
        m_name.setValue(name);
        m_label.setValue(label);
        addParams(m_aparam);
    }
    
    public BasePrintableMaterial(String name, String alternateName, String label) {
        m_name.setValue(name);
        m_alternateName.setValue(alternateName);
        if (alternateName != null) {
            m_alternateNames.add(alternateName);
        }
        m_label.setValue(label);
        addParams(m_aparam);
    }

    public BasePrintableMaterial(String name, List<String> alternateNames, String label) {
        m_name.setValue(name);
        if (alternateNames != null) {
            m_alternateNames.addAll(alternateNames);
        }
        m_label.setValue(label);
        addParams(m_aparam);
    }

    public String getName() {
        return m_name.getValue();
    }
    public String getLabel() {
        return m_label.getValue();
    }

    public DataSource getRenderingSource(DataSource source) {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof PrintableMaterial)) {
            return false;
        }

        final PrintableMaterial that = (PrintableMaterial) o;

        // TODO: Compare m_alternateName too?
        return Objects.equals(this.m_name, that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name);
    }

    public MaterialType getMaterialType() {
        return MaterialType.valueOf(m_matType.getValue());
    }
    public void setMaterialType(MaterialType val) {
        m_matType.setValue(val);
    }

    public void setName(String val) {
        m_name.setValue(val);
    }
    
    public void setAlternateName(String val) {
        m_alternateName.setValue(val);
    }
    
    public String getAlternateName() {
        return m_alternateName.getValue();
    }

    public void setAlternateNames(List<String> names) {
        if (names != null) {
            m_alternateNames.clear();
            m_alternateNames.addAll(names);
        }
    }
    
    public List<String> getAlternateNames() {
        return m_alternateNames;
    } 
}
