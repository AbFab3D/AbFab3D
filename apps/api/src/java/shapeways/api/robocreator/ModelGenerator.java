package shapeways.api.robocreator;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public interface ModelGenerator {
    public byte[] generateModel(Map params, X3DEncodingType encoding);
}
