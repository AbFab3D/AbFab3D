package volumesculptor.shell;

import abfab3d.mesh.TriangleMesh;

/**
 * Result container for execute commands
 *
 * @author Alan Hudson
 */
public class ExecResult {
    private TriangleMesh mesh;
    private String errors;
    private String prints;

    public ExecResult(TriangleMesh mesh, String errors, String prints) {
        this.mesh = mesh;
        this.errors = errors;
        this.prints = prints;
    }

    public TriangleMesh getMesh() {
        return mesh;
    }

    public String getErrors() {
        return errors;
    }

    public String getPrints() {
        return prints;
    }
}
