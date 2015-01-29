package datasources;

import org.apache.commons.io.IOUtils;
import program.ProgramLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Torus datasource
 *
 * @author Alan Hudson
 */
public class OpenCLTorus extends OpenCLDataSource {
    public String getCode(String version) throws IOException {
        InputStream is = ProgramLoader.getStreamFor("torus_" + version + ".cl");
        return IOUtils.toString(is, "UTF-8");
    }
}
