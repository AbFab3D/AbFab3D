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
package abfab3d.io.input;

import abfab3d.core.AttributeGrid;
import abfab3d.core.GridProducer;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.BooleanParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.LongParameter;
import abfab3d.param.ObjectParameter;
import abfab3d.param.ParamCache;
import abfab3d.param.Parameter;
import abfab3d.param.Parameterizable;
import abfab3d.shapejs.MaterialType;

import java.util.HashMap;

import static abfab3d.core.Units.*;
import static abfab3d.core.Output.printf;

/**
 * Loads 3D models
 *
 * @author Alan Hudson
 */
public class ModelLoader extends BaseParameterizable implements GridProducer, Parameterizable {
    private static final boolean DEBUG = true;


    DoubleParameter mp_voxelSize = new DoubleParameter("voxelSize","size of voxels", 0.1*MM);
    ObjectParameter mp_source = new ObjectParameter("source","model source",null);
    DoubleParameter mp_margins = new DoubleParameter("margins","size of margins", 2*MM);
    BooleanParameter mp_attributeLoading = new BooleanParameter("attributeLoading", "Load attribute data",false);
    IntParameter mp_densityBitCount = new IntParameter("densityBitCount","Density Bit Count", 8);
    IntParameter mp_distanceBitCount = new IntParameter("distanceBitCount","Distance Bit Count", 8);
    DoubleParameter mp_maxInDistance = new DoubleParameter("maxInDistance","Maximum in distance", 2*MM);
    DoubleParameter mp_maxOutDistance = new DoubleParameter("maxOutDistance","Maximum out distance", 2*MM);
    LongParameter mp_maxGridSize = new LongParameter("maxGridSize","Max grid size", 1000l * 1000 * 1000);
    LongParameter mp_minGridSize = new LongParameter("minGridSize","Min grid size", 0);

    protected String m_path;
    protected AttributedMeshReader m_reader;
    protected abfab3d.shapejs.MaterialType m_materialType = MaterialType.SINGLE_MATERIAL;
    protected String m_vhash;

    Parameter m_aparam[] = new Parameter[]{
            mp_source,
            mp_voxelSize,
            mp_margins,
            mp_attributeLoading,
            mp_densityBitCount,
            mp_distanceBitCount,
            mp_maxInDistance,
            mp_maxOutDistance,
            mp_maxGridSize,
            mp_minGridSize
    };

    public ModelLoader(String path) {
        initParams();

        setSource(path);
    }

    public ModelLoader(String path, double vs, double margins) {
        initParams();

        setSource(path);
        setVoxelSize(vs);
        setMargins(margins);
    }

    /**
     * @noRefGuide
     */
    protected void initParams(){
        super.addParams(m_aparam);
    }

    public void setSource(String path) {
        mp_source.setValue(path);
        m_path = path;
        m_vhash = null;
    }

    public void setVoxelSize(double vs) {
        mp_voxelSize.setValue(vs);
        m_vhash = null;
    }

    public void setMargins(double margins) {
        mp_margins.setValue(margins);
        m_vhash = null;
    }

    public void setAttributeLoading(boolean val) {
        mp_attributeLoading.setValue(val);
        m_vhash = null;
    }

    public AttributedMesh getMesh() {
        ModelCacheEntry co = (ModelCacheEntry) ParamCache.getInstance().get(getValueHash());
        AttributedMesh mesh = null;

        if (co != null) {
            if (co.mesh != null) {
                m_materialType = co.materialType;
                return co.mesh;
            }
        }

        m_reader = new AttributedMeshReader(m_path);
        mesh = new AttributedMesh();
        m_reader.getAttTriangles(mesh);

        mesh.setDataDimension(m_reader.getDataDimension());

        int dim = m_reader.getDataDimension();

        switch(dim) {
            case 3:
                m_materialType = abfab3d.shapejs.MaterialType.SINGLE_MATERIAL;
                break;
            case 5:
                m_materialType = abfab3d.shapejs.MaterialType.COLOR_MATERIAL;
                break;
            case 6:
                m_materialType = abfab3d.shapejs.MaterialType.COLOR_MATERIAL;
                break;
            default:
                m_materialType = abfab3d.shapejs.MaterialType.COLOR_MATERIAL;
        }

        ParamCache.getInstance().put(getValueHash(),new ModelCacheEntry(null,mesh,m_materialType));

        return mesh;
    }

    public void setDensityBitCount(int val) {
        mp_densityBitCount.setValue(val);
        m_vhash = null;
    }

    public void setDistanceBitCount(int val) {
        mp_distanceBitCount.setValue(val);
        m_vhash = null;
    }

    public void setMaxInDistance(double val) {
        mp_maxInDistance.setValue(val);
        m_vhash = null;
    }

    public void setMaxOutDistance(double val) {
        mp_maxOutDistance.setValue(val);
        m_vhash = null;
    }

    public void setMaxGridSize(long val) {
        mp_maxGridSize.setValue(val);
        m_vhash = null;
    }

    public void setMinGridSize(long val) {
        mp_minGridSize.setValue(val);
        m_vhash = null;
    }

    public AttributeGrid getGrid() {
        ModelCacheEntry co = (ModelCacheEntry) ParamCache.getInstance().get(getValueHash());
        AttributedMesh mesh = null;
        AttributeGrid grid = null;

        if (co != null) {
            if (co.grid != null) {
                if (DEBUG) printf("Found cached grid: %s\n",getValueHash());
                m_materialType = co.materialType;
                return co.grid;
            } else if (co.mesh != null) {
                mesh = co.mesh;
            }
        } else {
            if (DEBUG) printf("No cache for grid: %s\n",getValueHash());
        }

        GridLoader loader = new GridLoader();
        loader.setMaxGridSize(mp_maxGridSize.getValue());
        loader.setMinGridSize(mp_minGridSize.getValue());
        loader.setDensityBitCount(mp_densityBitCount.getValue());
        loader.setDistanceBitCount(mp_distanceBitCount.getValue());
        loader.setPreferredVoxelSize(mp_voxelSize.getValue());
        loader.setDensityAlgorithm(GridLoader.RASTERIZER_DISTANCE2);
        loader.setMaxInDistance(mp_maxInDistance.getValue());
        loader.setMaxOutDistance(mp_maxOutDistance.getValue());
        loader.setMargins(mp_margins.getValue());
        loader.setShellHalfThickness(2);
        loader.setThreadCount(0);

        int dim;

        if (DEBUG) printf("Getting grid: %s  attributed: %b\n",m_path,mp_attributeLoading.getValue());
        if (mp_attributeLoading.getValue()) {
            if (mesh != null) {
                grid = loader.rasterizeAttributedTriangles(mesh, m_reader.getAttributeCalculator());
                dim = m_reader.getDataDimension();
            } else {
                AttributedMeshReader reader = new AttributedMeshReader(m_path);
                grid = loader.rasterizeAttributedTriangles(reader);
                dim = reader.getDataDimension();
            }
        } else {
            dim = 3;
            if (mesh != null) {
                grid = loader.loadDistanceGrid(mesh);
            } else {
                grid = loader.loadDistanceGrid(m_path);
            }
        }

        switch(dim) {
            case 3:
                m_materialType = abfab3d.shapejs.MaterialType.SINGLE_MATERIAL;
                break;
            case 5:
                m_materialType = abfab3d.shapejs.MaterialType.COLOR_MATERIAL;
                break;
            case 6:
                m_materialType = abfab3d.shapejs.MaterialType.COLOR_MATERIAL;
                break;
            default:
                m_materialType = abfab3d.shapejs.MaterialType.COLOR_MATERIAL;
        }

        if (DEBUG) printf("Dim: %d\n",dim);

        ParamCache.getInstance().put(getValueHash(), new ModelCacheEntry(grid,mesh,m_materialType));

        return grid;
    }

    public abfab3d.shapejs.MaterialType getMaterialType() {
        if (DEBUG) printf("MaterialType: %s\n",m_materialType);
        return m_materialType;
    }

    protected String getValueHash() {
        if (m_vhash != null) return m_vhash;

        m_vhash = BaseParameterizable.getParamString(getClass().getSimpleName(), m_aparam);
        return m_vhash;
    }

    static class ModelCacheEntry {
        AttributeGrid grid;
        AttributedMesh mesh;
        MaterialType materialType;

        public ModelCacheEntry(AttributeGrid grid, AttributedMesh mesh, MaterialType materialType) {
            this.grid = grid;
            this.mesh = mesh;
            this.materialType = materialType;
        }
    }
}
