package abfab3d.io.input;

import abfab3d.core.AttributeGrid;
import abfab3d.util.AbFab3DGlobals;

import static abfab3d.core.Units.MM;

/**
 * Rasterize an attributed mesh
 *
 * @author Alan Hudson
 */
public class AttributedMeshRasterizer {
    private double m_margins = 0.2*MM;
    private double m_voxelSize;
    protected double m_maxDistance = 2*MM;
    protected int m_threadCount = 0;

    public AttributeGrid rasterizeMesh(AttributedMesh mesh) {
        GridLoader loader = new GridLoader();
        loader.setThreadCount(0);
        loader.setMaxInDistance(m_maxDistance);
        loader.setMaxOutDistance(m_maxDistance);
        loader.setMargins(m_margins);
        loader.setPreferredVoxelSize(m_voxelSize);

        AttributeGrid grid = loader.rasterizeAttributedTriangles(mesh, mesh.getAttributeCalculator());
        return grid;
    }

    public void setMargins(double margins){
        m_margins = margins;
    }

    public void setVoxelSize(double vs) {
        m_voxelSize = vs;
    }

    /**
     * Set the maximum distance to calculate the distance values
     * @param value
     */
    public void setMaxDistance(double value){
        m_maxDistance = value;
    }

    public void setThreadCount(int count){
        if (count < 1) {
            count = Runtime.getRuntime().availableProcessors();
        }

        int max_threads = ((Number) AbFab3DGlobals.get(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY)).intValue();
        if (count > max_threads)
            count = max_threads;

        m_threadCount = count;
    }

}
