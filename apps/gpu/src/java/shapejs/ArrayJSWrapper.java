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
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.List;

/**
 * Wrap a parameter so it can masquerade as a Javascript primitive object if desired
 *
 * @author Alan Hudson
 */
public class ArrayJSWrapper extends ScriptableObject implements JSWrapper {
    protected Parameter param;

    public ArrayJSWrapper() {

    }

    public ArrayJSWrapper(Scriptable scope, Parameter param) {
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
        List list = (List) param.getValue();

        return list.get(index);
    }

    @Override
    public void put(int index, Scriptable start, Object value) {
        List list = (List) param.getValue();

        list.set(index, value);
    }

}
