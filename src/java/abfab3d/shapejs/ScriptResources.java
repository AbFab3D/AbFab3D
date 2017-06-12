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
    //public Map<String,Object> params;  // Get this from eval.getParameters
    public float quality;
    public ShapeJSEvaluator eval;
    public boolean sensitiveData = false;
    public boolean sensitiveScript = false;


    public ScriptResources() {
    }

    public void clear() {
        evaluatedScript = null;
        eval = null;
    }

    public Map<String,Parameter> getParams() {
        return eval.getParams();
    }

}