package abfab3d.param;

import abfab3d.core.DataSource;
import abfab3d.core.Initializable;
import abfab3d.core.Material;
import abfab3d.core.MaterialShader;
import abfab3d.core.ResultCodes;

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

    protected SNodeParameter mp_source = new SNodeParameter("source");
    protected SNodeParameter mp_material = new SNodeParameter("material");
    protected SNodeParameter mp_shader = new SNodeParameter("shader");

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

    @Override
    public int initialize() {
        if (m_source instanceof Initializable) {
            ((Initializable)m_source).initialize();
        }

        return ResultCodes.RESULT_OK;
    }
}
