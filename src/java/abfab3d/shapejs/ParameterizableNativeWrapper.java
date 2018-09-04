/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2018
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.shapejs;

import abfab3d.param.Parameter;
import abfab3d.param.Parameterizable;
import org.mozilla.javascript.NativeObject;

import java.util.Map;

public class ParameterizableNativeWrapper implements Parameterizable {
    private NativeObject obj;

    public ParameterizableNativeWrapper(NativeObject no) {
        this.obj = no;
    }

    public NativeObject getImpl() {
        return obj;
    }

    @Override
    public Parameter getParam(String param) {
        return null;
    }

    @Override
    public Parameter[] getParams() {
        return new Parameter[0];
    }

    @Override
    public String getParamString() {
        return null;
    }

    @Override
    public void getParamString(StringBuilder sb) {

    }

    @Override
    public Map<String, Parameter> getParamMap() {
        return null;
    }

    @Override
    public Object get(String param) {
        return null;
    }

    @Override
    public void set(String param, Object value) {

    }

    @Override
    public void getDataLabel(StringBuilder sb) {

    }

    @Override
    public String getDataLabel() {
        return null;
    }

    public NativeObject getObject() {
        return obj;
    }
}
