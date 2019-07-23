package volumesculptor.shell;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

import static abfab3d.core.Output.printf;

/**
 * Created by giles on 3/16/2015.
 */
public class SandboxWrapFactory extends WrapFactory {
    @Override
    public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class staticType) {
        return new SandboxNativeJavaObject(scope, javaObject, staticType);
    }
}
