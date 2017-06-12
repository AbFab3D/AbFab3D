package abfab3d.io.input;

import java.util.HashSet;

/**
 * Base for mesh reading code
 *
 * @author Alan Hudson
 */
public class BaseMeshReader {
    public static final String
            EXT_STL = "stl",
            EXT_OBJ = "obj",
            EXT_X3DB = "x3db",
            EXT_X3D = "x3d",
            EXT_X3DV = "x3dv",
            EXT_WRL = "wrl";

    public static HashSet<String> supportedExt;

    static {
        supportedExt = new HashSet<String>();
        supportedExt.add(EXT_STL);
        supportedExt.add(EXT_X3D);
        supportedExt.add(EXT_X3DB);
        supportedExt.add(EXT_X3DV);
        supportedExt.add(EXT_WRL);
    }


}
