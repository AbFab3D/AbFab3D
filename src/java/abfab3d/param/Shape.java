package abfab3d.param;

import abfab3d.core.DataSource;
import abfab3d.core.Initializable;
import abfab3d.core.Material;
import abfab3d.core.MaterialShader;
import abfab3d.core.ResultCodes;
import abfab3d.core.VecTransform;

import java.util.List;

import static abfab3d.core.Output.printf;

/**
 * A ShapeJS Shape.  A shape contains a datasource to generate the geometry, a material specification
 * and an optional Shader for rendering.
 *
 * @author Alan Hudson
 */
public class Shape extends BaseParameterizable implements Initializable {
    //private DataSource m_source;
    //private Material m_material;
    //private MaterialShader m_shader;
    //private int m_id;
    // transformation which is applied to the data point before the calculation of data value
    //protected VecTransform m_transform = null;

    protected SNodeParameter mp_source = new SNodeParameter("source");
    protected SNodeParameter mp_material = new SNodeParameter("material");
    protected SNodeParameter mp_shader = new SNodeParameter("shader");
    protected IntParameter mp_id = new IntParameter("id",0);
    //SNodeListParameter mp_transform = new SNodeListParameter("transform", "Transform");


    Parameter m_aparam[] = new Parameter[]{
        mp_source, mp_material, mp_shader, mp_id
    };

    public Shape(DataSource source, Material material) {
        this(source,material,null);
    }

    public Shape(DataSource source, Material material, MaterialShader shader) {
        initParams();

        mp_source.setValue(source);
        mp_material.setValue(material);
        mp_shader.setValue(shader);

    }

    protected void initParams() {
        super.addParams(m_aparam);
    }

    public DataSource getSource() {
        return (DataSource)mp_source.getValue();
    }

    public void setSource(DataSource source) {
        mp_source.setValue(source);
    }

    public Material getMaterial() {
        return (Material)mp_material.getValue();
    }

    public void setMaterial(Material material) {
        mp_material.setValue(material);
    }

    public MaterialShader getShader() {
        return (MaterialShader)mp_shader.getValue();
    }

    public void setShader(MaterialShader shader) {
        mp_shader.setValue(shader);
    }

    public void setMaterialID(int id) {
        mp_id.setValue(id);
    }

    public int getMaterialID() {
        return mp_id.getValue();
    }

    /**
     * Transform the data source
     * @noRefGuide
     * @param transform General transformation to apply to the object before it is rendered
     */
    
    public void setTransform(VecTransform transform){
        //mp_transform.set((Parameterizable)transform);
    }

    /**
     * @noRefGuide
     @return Transformation the object
     */
   
    public VecTransform getTransform() {
        return null;//m_transform;
    }

    public VecTransform makeTransform() {
        /*
        List list = mp_transform.getValue();

        Object tr[] = list.toArray();
        if(tr.length == 0){
            return null;
        } else if(tr.length == 1){
            return (VecTransform)tr[0];
        } else {
            throw new IllegalArgumentException("Composite transforms not supported");
        }
        */
        return null;
    }
    
    
    @Override
    public int initialize() {
        DataSource source = getSource();
        if (source instanceof Initializable) {
            ((Initializable)source).initialize();
        }
        /*
        m_transform = makeTransform();
        if(m_transform != null && m_transform  instanceof Initializable){
            ((Initializable)m_transform).initialize();
        }
        */
        return ResultCodes.RESULT_OK;
    }
}
