/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2016
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/
package abfab3d.shapejs;

import abfab3d.param.BaseParameterizable;
import abfab3d.param.Parameter;

import java.util.Map;

/**
 * 
 *
 * @author Alan Hudson
 */
public class ParameterizableAdapter extends BaseParameterizable {

    /** Parameters defined in the script */
    private Map<String, Parameter> scriptParams;

    public ParameterizableAdapter(Map<String, Parameter> scriptParams) {
        this.scriptParams = scriptParams;
        setParamMap(scriptParams);
    }

    private void setParamMap(Map<String, Parameter> params) {
        clearParams();
        addParams(params);
    }
}
