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
package abfab3d.shapejs;

import abfab3d.param.Parameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Resources for a script.
 *
 * @author Alan Hudson
 */
public class ScriptResources {
    public String jobID;
    public EvaluatedScript evaluatedScript;
    public String script;
    public Map<String,Object> params;
    public float quality;
    public ShapeJSEvaluator eval;
    public boolean firstCreate;

    public ScriptResources() {
        params = new HashMap<String,Object>();
    }

    public void clear() {
        if (params != null)
            params.clear();

        evaluatedScript = null;
        eval = null;
    }

    /**
       return array of script parameters (if exist)
     */
    public Parameter[] getParams(){

        if(params == null) 
            return new Parameter[0];
        
        Parameter aparam[] = new Parameter[params.size()];
        int idx = 0;
        for(Object o: params.values()) {
            aparam[idx++] = (Parameter)o;
        }
        return aparam;
    }

}