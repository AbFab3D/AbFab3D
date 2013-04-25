package shapeways.api.robocreator.cube;

import shapeways.api.robocreator.BaseRoboCreator;
import shapeways.api.robocreator.X3DEncodingType;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Map;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public class CubeServlet extends BaseRoboCreator {
    public byte[] generateModel(Map params, X3DEncodingType encoding) {
        System.out.println("Generating Cube");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);

        pw.println("#X3D V3.0 utf8");
        pw.println("PROFILE Immersive");
        pw.println("Shape { geometry Box { size 0.05 0.05 0.05 } }");

        pw.close();

        return baos.toByteArray();
    }

}
