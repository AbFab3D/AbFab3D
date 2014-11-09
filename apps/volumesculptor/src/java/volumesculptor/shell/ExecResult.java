package volumesculptor.shell;

import abfab3d.grid.Model;

/**
 * Result container for execute commands
 *
 * @author Alan Hudson
 */
public class ExecResult {
    private Model model;
    private String errors;
    private String prints;

    public ExecResult(Model model, String errors, String prints) {
        this.model = model;
        this.errors = errors;
        this.prints = prints;
    }

    public Model getModel() {
        return model;
    }

    /*
    public TriangleMesh getMesh() {
        try {
            return model.getMesh();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        return null;
    }
    */
    public String getErrors() {
        return errors;
    }

    public String getPrints() {
        return prints;
    }
}
