package shapejs;

import abfab3d.param.Parameter;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

/**
 * Created by giles on 3/16/2015.
 */
public class ComplexJSWrapper extends NativeJavaObject implements JSWrapper {
    protected Parameter param;

    public ComplexJSWrapper(Scriptable scope, Parameter param) {
        super(scope,param,param.getClass());
        setParentScope(scope);
        setParameter(param);
    }

    public void setParameter(Parameter param) {
        this.param = param;
    }

    public Parameter getParameter() {
        return param;
    }
}
