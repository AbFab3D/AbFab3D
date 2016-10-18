package abfab3d.param;

import abfab3d.core.DataSource;
import abfab3d.core.Initializable;
import abfab3d.core.Material;
import abfab3d.core.MaterialShader;
import abfab3d.core.ResultCodes;
import abfab3d.core.VecTransform;
import abfab3d.transforms.CompositeTransform;
import abfab3d.transforms.TransformsFactory;

import java.util.List;

import static abfab3d.core.Output.printf;

/**
 * A ShapeJS Shape.  A shape contains a datasource to generate the geometry, a material specification
 * and an optional Shader for rendering.
 *
 * @author Alan Hudson
 */
public class Shape extends BaseParameterizable implements Initializable {
    private DataSource m_source;
    private Material m_material;
    private MaterialShader m_shader;
    private int m_id;
    // transformation which is applied to the data point before the calculation of data value
    protected VecTransform m_transform = null;

    protected SNodeParameter mp_source = new SNodeParameter("source");
    protected SNodeParameter mp_material = new SNodeParameter("material");
    protected SNodeParameter mp_shader = new SNodeParameter("shader");
    SNodeListParameter mp_transform = new SNodeListParameter("transform", new BaseSNodeFactory(TransformsFactory.getNames(), TransformsFactory.getClassNames()));


    Parameter m_aparam[] = new Parameter[]{
            mp_source, mp_material, mp_shader
    };

    public Shape(DataSource source, Material material) {
        this(source,material,null);
    }

    public Shape(DataSource source, Material material, MaterialShader shader) {
        m_source = source;
        m_material = material;
        m_shader = shader;

        initParams();
    }

    protected void initParams() {
        super.addParams(m_aparam);
    }

    public DataSource getSource() {
        return m_source;
    }

    public void setSource(DataSource source) {
        m_source = source;
    }

    public Material getMaterial() {
        return m_material;
    }

    public void setMaterial(Material material) {
        m_material = material;
    }

    public MaterialShader getShader() {
        return m_shader;
    }

    public void setShader(MaterialShader shader) {
        m_shader = shader;
    }

    public void setMaterialID(int id) {
        m_id = id;
    }

    public int getMaterialID() {
        return m_id;
    }

    /**
     * Transform the data source
     * @noRefGuide
     * @param transform General transformation to apply to the object before it is rendered
     */
    public void setTransform(VecTransform transform){
        mp_transform.set((Parameterizable)transform);
    }

    /**
     * @noRefGuide
     @return Transformation the object
     */
    public VecTransform getTransform() {
        return m_transform;
    }

    public VecTransform makeTransform() {

        List list = mp_transform.getValue();

        Object tr[] = list.toArray();
        if(tr.length == 0){
            return null;
        } else if(tr.length == 1){
            return (VecTransform)tr[0];
        } else {
            CompositeTransform ct = new CompositeTransform();
            for(int k = 0 ; k < tr.length; k++){
                ct.add((VecTransform)tr[k]);
            }
            return ct;
        }
    }

    @Override
    public int initialize() {
        if (m_source instanceof Initializable) {
            ((Initializable)m_source).initialize();
        }

        m_transform = makeTransform();
        if(m_transform != null && m_transform  instanceof Initializable){
            ((Initializable)m_transform).initialize();
        }

        return ResultCodes.RESULT_OK;
    }
}
