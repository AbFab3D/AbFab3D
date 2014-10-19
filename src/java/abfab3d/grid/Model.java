package abfab3d.grid;

import abfab3d.util.TriangleMesh;

import java.io.IOException;

/**
 * Everything necessary to make a real object from a voxel grid.
 *
 * @author Alan Hudson
 */
public class Model {
    private AttributeGrid grid;
    private ModelWriter writer;
    private TriangleMesh mesh;

    public AttributeGrid getGrid() {
        return grid;
    }

    public void setGrid(AttributeGrid grid) {
        this.grid = grid;
    }

    public ModelWriter getWriter() {
        return writer;
    }

    public void setWriter(ModelWriter writer) {
        this.writer = writer;
    }

    /**
     * Get a mesh representation of the Model.  For some versions of the model this may require creating a mesh.  When
     * possible a cached version of the mesh will be returned.
     * @return The mesh or null if we cannot generate one
     */
    /*
    public TriangleMesh getMesh() throws IOException {
        if (mesh != null) {
            return mesh;
        }

        mesh = writer.getMesh(grid);
        return mesh;
    }
    */
}
