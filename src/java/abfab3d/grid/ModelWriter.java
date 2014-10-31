package abfab3d.grid;

import abfab3d.util.TriangleMesh;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes a model out.
 *
 * @author Alan Hudson
 */
public interface ModelWriter {
    /**
     * Set the desired output format.  Will throw an IllegalArgumentException if its not supported.
     *
     * @param fileEnding The format file ending
     */
    public void setOutputFormat(String fileEnding);

    /**
     * Set the output destination.  It's recommended this is wrapped with a BufferedOutputStream.
     * Caller is responsible for closing under ordinary and exceptional conditions.
     *
     * @param os
     */
    public void setOutputStream(OutputStream os);

    /**
     * Write the model to the requested output.
     */
    public void execute(AttributeGrid grid) throws IOException;

    /**
     * Generates a mesh for the model if its been generated, null otherwise
     * @return
     */
    public TriangleMesh getGeneratedMesh();

    /**
     * Get a string name for this writer.
     * @return
     */
    public String getStyleName();
}
