package abfab3d.io.output;

import abfab3d.core.Grid;
import toxi.geom.mesh.WETriangleMesh;

/**
 * Simplified meshes
 *
 * @author Alan Hudson
 */
public interface MeshSimplifier {
    /**
     * Execute the simplification algorithm
     *
     * @param mesh
     */
    public void execute(WETriangleMesh mesh, Grid grid);
}
