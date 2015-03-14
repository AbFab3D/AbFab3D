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

/**
 * Wrap a parameter so it can masquerade as a Javascript primitive object if desired
 *
 * @author Alan Hudson
 */
public class ParameterJSWrapper extends ScriptableObject {
    private Parameter param;

    public ParameterJSWrapper() {

    }

    public ParameterJSWrapper(Scriptable scope, Parameter param) {
        setParentScope(scope);
        this.param = param;
    }

    @Override
    public Object getDefaultValue(Class hint) {
        return param.getValue();
    }

    public String getClassName() {
        return param.getType().toString();
    }
}
