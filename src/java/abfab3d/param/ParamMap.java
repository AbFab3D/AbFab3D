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
package abfab3d.param;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import abfab3d.core.Initializable;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;

/**
 * map of parameters 
 *
 */
public class ParamMap extends BaseParameterizable {

    public ParamMap(Parameter [] params){
        addParams(params);
    }

    
}
