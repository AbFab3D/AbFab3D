package shapejs;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

/**
 * Created by giles on 3/16/2015.
 */
public class SandboxWrapFactory extends WrapFactory {
    @Override
    public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class staticType) {
        return new SandboxNativeJavaObject(scope, javaObject, staticType);
    }
}
