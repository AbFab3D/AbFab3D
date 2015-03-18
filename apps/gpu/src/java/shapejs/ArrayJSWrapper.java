/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package shapejs;

import abfab3d.param.Parameter;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Wrap a parameter so it can masquerade as a Javascript primitive object if desired
 *
 * @author Alan Hudson
 */
public class ArrayJSWrapper extends ScriptableObject implements JSWrapper {
    protected Parameter param;
    protected ScriptableObject base;

    public ArrayJSWrapper() {

    }

    public ArrayJSWrapper(Scriptable scope, Parameter param) {
        base = (ScriptableObject) Context.javaToJS(param.getValue(),scope);
        setParentScope(scope);
        setParameter(param);
    }

    public void setParameter(Parameter param) {
        this.param = param;
    }

    public Parameter getParameter() {
        return param;
    }

    @Override
    public Object getDefaultValue(Class hint) {
        return param.getValue();
    }

    public String getClassName() {
        return param.getType().toString();
    }


    @Override
    public Object get(int index, Scriptable start) {
        return base.get(index,start);
    }

    @Override
    public Object get(String name, Scriptable start) {
        return base.get(name,start);
    }

    @Override
    public void put(int index, Scriptable start, Object value) {
        base.put(index,start,value);
    }
/*
        List list = (List) param.getValue();

        try {
        ParameterJSWrapper wrapper = (ParameterJSWrapper) list.get(index);
        if (wrapper != null) {
            wrapper.getParameter().setValue(value);
        } else {
            Parameter p = (Parameter) param.clone();
            wrapper = new ParameterJSWrapper(getParentScope(),p);
            list.set(index,wrapper);
        }
    }
*/


}
