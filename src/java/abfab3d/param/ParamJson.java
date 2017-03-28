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

import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;


/**
 * Utilites to read/write to from Json Maps 
 *
 * @author Vladimir Bulatov
 */
public class ParamJson {
    

    /**
       convert parameter value into form suitable for Json serialization
     */
    public static Object getJsonValue(Object value){

        if(value instanceof Parameterizable) {
            return getJson((Parameterizable)value);
        } else if(value instanceof ArrayList) {
            Object arr[] = ((ArrayList)value).toArray();
            ArrayList array = new ArrayList();
            for(int i = 0; i < arr.length; i++){
                array.add(getJsonValue(arr[i]));
            }
            return array;
        } else {
            //printf("value: %s\n", value.getClass().getSimpleName());
            //return value.getClass().getName() + ":" + value.toString();
            return value.toString();
        }
    }


    /**
       convert array of Parameter into Map suitable for Json serialization
     */
    public static Map getJson(Parameter params[]){

        LinkedHashMap map = new LinkedHashMap();

        for(int i = 0; i < params.length; i++){
            map.put(params[i].getName(),getJsonValue(params[i].getValue()));
        }
        return map;
    }

    /**
       convert Parameterizable into Map suitable for Json serialization
     */
    public static Map getJson(Parameterizable par){

        LinkedHashMap map = new LinkedHashMap();
        map.put("snode", par.getClass().getName());        
        map.put("param", getJson(par.getParams()));
        return map;
    } 



}
