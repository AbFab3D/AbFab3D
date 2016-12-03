/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.shapejs;

import abfab3d.param.Parameter;
import abfab3d.param.Editor;
import abfab3d.param.ParameterType;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import static abfab3d.core.Output.printf;

/**
 * Wrap a parameter so it can masquerade as a Javascript primitive object if desired
 *
 * @author Alan Hudson
 */
public class ParameterJSWrapper extends ScriptableObject implements JSWrapper, Parameter {
    protected Parameter param;

    public ParameterJSWrapper() {

    }

    public ParameterJSWrapper(Scriptable scope, Parameter param) {
        setParentScope(scope);
        setParameter(param);
    }

    public void setParameter(Parameter param) {
        this.param = param;
    }

    public Parameter getParameter() {
        return param;
    }

    public void addEditor(Editor editor){
        // do nothing 
    }

    @Override
    public Object getDefaultValue(Class hint) {
        return param.getValue();
    }

    /**
     * Is this parameter set at its default value
     * @return
     */
    public boolean isDefaultValue() {
        return (getDefaultValue().equals(getValue()));
    }


    public String getClassName() {
        return param.getType().toString();
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        param.setValue(value);
    }

    @Override
    public Object get(String name, Scriptable start) {
        Context cx = Context.enter();
        try {
            // make the underlying object into a javascript object

            Object o = param.getValue();
            Scriptable st = ScriptRuntime.toObject(cx, start, o);
            return st.get(name, start);
        } finally {
            Context.exit();
        }
    }

    public String toString() {
        Object o = param.getValue();

        if (o == null) return "null string";

        return o.toString();
    }

    @Override
    public ParameterType getType() {
        return param.getType();
    }

    @Override
    public void validate(Object o) {
        param.validate(o);
    }

    @Override
    public String getName() {
        return param.getName();
    }

    @Override
    public void setName(String s) {
        param.setName(s);
    }

    @Override
    public String getDesc() {
        return param.getDesc();
    }

    @Override
    public void setDesc(String s) {
        param.setDesc(s);
    }

    @Override
    public Object getValue() {
        return param.getValue();
    }

    @Override
    public void setDefaultValue(Object o) {
        param.setDefaultValue(o);
    }

    @Override
    public Object getDefaultValue() {
        return param.getDefaultValue();
    }

    @Override
    public void setValue(Object o) {
        param.setValue(o);
    }

    @Override
    public String getOnChange() {
        return param.getOnChange();
    }

    @Override
    public void setOnChange(String s) {
        param.setOnChange(s);
    }
    
    @Override
    public String getGroup() {
        return param.getGroup();
    }

    @Override
    public void setGroup(String s) {
        param.setGroup(s);
    }
    
    @Override
    public String getLabel() {
        return param.getLabel();
    }

    @Override
    public void setLabel(String s) {
        param.setLabel(s);
    }

    public String getParamString() {
        return param.getParamString();
    }

    public void getParamString(StringBuilder sb) {
        param.getParamString(sb);
    }

    public boolean hasChanged() {
        return param.hasChanged();
    }
}
