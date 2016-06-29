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

import static abfab3d.core.Units.*;
import static abfab3d.core.Output.printf;

/**
 * Loads 3D models
 *
 * @author Alan Hudson
 */
public class ModelLoader {
    private static final boolean DEBUG = true;

    private double m_vs = 0.2*MM;
    private String m_path;
    private double m_margins =  2*MM;
    private boolean m_attributeLoading = false;
    private int m_densityBitCount = 8;
    private int m_distanceBitCount = 8;
    private double m_maxInDistance = 2*MM;
    private double m_maxOutDistance = 2*MM;
    private AttributedMesh m_mesh;
    private AttributeGrid m_grid;
    private AttributedMeshReader m_reader;
    private abfab3d.shapejs.MaterialType m_materialType;
    private long m_maxGridSize = 1000l * 1000 * 1000;


    public ModelLoader(String path) {
        m_path = path;
    }

    public ModelLoader(String path, double vs, double margins) {
        m_path = path;
        m_vs = vs;
        m_margins = margins;
    }

    public void setVoxelSize(double vs) {
        m_vs = vs;
    }

    public void setMargins(double margins) {
        m_margins = margins;
    }

    public void setAttributeLoading(boolean val) {
        m_attributeLoading = val;
    }

    public AttributedMesh getMesh() {
        if (m_mesh != null) return null;

        m_reader = new AttributedMeshReader(m_path);
        m_mesh = new AttributedMesh();
        m_reader.getAttTriangles(m_mesh);

        m_mesh.setDataDimension(m_reader.getDataDimension());

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

        return m_mesh;
    }

    public void setDensityBitCount(int val) {
        m_densityBitCount = val;
    }

    public void setDistanceBitCount(int val) {
        m_distanceBitCount = val;
    }

    public void setMaxInDistance(double val) {
        m_maxInDistance = val;
    }

    public void setMaxOutDistance(double val) {
        m_maxOutDistance = val;
    }

    public void setMaxGridSize(long val) {
        m_maxGridSize = val;
    }

    public AttributeGrid getGrid() {
        if (m_grid != null) return m_grid;

        GridLoader loader = new GridLoader();
        loader.setMaxGridSize(m_maxGridSize);
        loader.setDensityBitCount(m_densityBitCount);
        loader.setDistanceBitCount(m_distanceBitCount);
        loader.setPreferredVoxelSize(m_vs);
        loader.setDensityAlgorithm(GridLoader.RASTERIZER_DISTANCE2);
        loader.setMaxInDistance(m_maxInDistance);
        loader.setMaxOutDistance(m_maxOutDistance);
        loader.setMargins(m_margins);
        loader.setShellHalfThickness(2);
        loader.setThreadCount(0);

        int dim;

        if (DEBUG) printf("Getting grid: %s  attributed: %b\n",m_path,m_attributeLoading);
        if (m_attributeLoading) {
            if (m_mesh != null) {
                m_grid = loader.rasterizeAttributedTriangles(m_mesh, m_reader.getAttributeCalculator());
                dim = m_reader.getDataDimension();
            } else {
                AttributedMeshReader reader = new AttributedMeshReader(m_path);
                m_grid = loader.rasterizeAttributedTriangles(reader);
                dim = reader.getDataDimension();
            }
        } else {
            dim = 3;
            if (m_mesh != null) {
                m_grid = loader.loadDistanceGrid(m_mesh);
            } else {
                m_grid = loader.loadDistanceGrid(m_path);
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
        return m_grid;
    }

    public abfab3d.shapejs.MaterialType getMaterialType() {
        if (DEBUG) printf("MaterialType: %s\n",m_materialType);
        return m_materialType;
    }
}
