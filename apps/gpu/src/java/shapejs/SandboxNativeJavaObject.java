package shapejs;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

/**
 * Wrapper for NativeJava objects to stop getClass.  This avoids scripts using reflection to defeat security.
 *
 * @author Alan Hudson
 */
public class SandboxNativeJavaObject extends NativeJavaObject {
    public SandboxNativeJavaObject(Scriptable scope, Object javaObject, Class staticType) {
        super(scope, javaObject, staticType);
    }

    @Override
    public Object get(String name, Scriptable start) {
        if (name.equals("getClass")) {
            return NOT_FOUND;
        }

        return super.get(name, start);
    }
}