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

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.vecmath.Vector3d;
import javax.vecmath.AxisAngle4d;

import abfab3d.core.Location;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import abfab3d.core.Color;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;

/**
 * Utilities to read/write to from Json Maps
 *
 * @author Vladimir Bulatov
 */
public class ParamJson {
    
    static final String CLASS = "class";
    static final String PARAMS = "params";

    static final boolean DEBUG = false;

    /**
       convert array of values into form suitable for Json serialization
     */
    public static Object getJsonFromList(ArrayList list, ParameterType type){
        ArrayList vv = new ArrayList();
        if(list != null){
            for(Object item: list){
                vv.add(getJsonValue(item, type));        
            }
        }
        return vv;                
    }
    
    public static Object getJsonFromVector3d(Vector3d v){

        ArrayList vv = new ArrayList(3);
        if(v != null){
            vv.add(new Double(v.x));
            vv.add(new Double(v.y));
            vv.add(new Double(v.z));
        }
        return vv;
    }

    public static Object getJsonFromAxisAngle4d(AxisAngle4d a){
        ArrayList vv = new ArrayList(4);
        if(a != null){
            vv.add(new Double(a.x));
            vv.add(new Double(a.y));
            vv.add(new Double(a.z));
            vv.add(new Double(a.angle));
        }
        return vv;        
    }

    public static Object getJsonFromColor(Color c){
        ArrayList vv = new ArrayList(3);
        if(c != null){
            vv.add(new Double(c.getr()));
            vv.add(new Double(c.getg()));
            vv.add(new Double(c.getb()));
        }
        return vv;
    }

    public static Object getJsonFromLocation(Location l){

        if (l == null) return null;
        
        HashMap<String,Object> ret_val = new HashMap<>();
        Vector3d point = l.getPoint();
        if (point != null) {
            ret_val.put("point",getJsonFromVector3d(point));
        }

        Vector3d normal = l.getNormal();
        if (normal != null) {
            ret_val.put("normal",getJsonFromVector3d(normal));
        }

        return ret_val;
    }

    public static Object getJsonFromVector3dArray(Vector3d a[]){

        ArrayList list = new ArrayList();
        if(a != null){
            for(int i = 0; i < a.length; i++){
                list.add(getJsonFromVector3d(a[i]));
            }
        }
        return list;
    }

    /**
       convert parameter value into form suitable for Json serialization
     */
    public static Object getJsonValue(Object value, ParameterType type){
        
        //Object value = param.getValue();

        switch(type){
        default:
            throw new RuntimeException(fmt("getJsonValue(%s: of type:%s) not implemented\n", value, type));
        case DOUBLE:
        case FLOAT:
        case BYTE:
        case SHORT:
        case BOOLEAN:
        case INTEGER:
        case LONG:
        case ENUM:
        case STRING:
        case URI:
            return value;
        case LOCATION:            
            return getJsonFromLocation((Location)value);
        case VECTOR_3D:
            return getJsonFromVector3d((Vector3d)value);            
        case SNODE:
            return getJson((Parameterizable)value);
        case SNODE_LIST:
            return getJsonFromList((ArrayList)value, ParameterType.SNODE);
        case STRING_LIST:
            return getJsonFromList((ArrayList)value, ParameterType.STRING);
        case URI_LIST:
            return getJsonFromList((ArrayList)value, ParameterType.URI);
        case DOUBLE_LIST:
            return getJsonFromList((ArrayList)value, ParameterType.DOUBLE);
        case FLOAT_LIST:
            return getJsonFromList((ArrayList)value, ParameterType.FLOAT);
        case BYTE_LIST:
            return getJsonFromList((ArrayList)value, ParameterType.BYTE);
        case SHORT_LIST:
            return getJsonFromList((ArrayList)value, ParameterType.SHORT);
        case INTEGER_LIST:
            return getJsonFromList((ArrayList)value, ParameterType.INTEGER);
        case LONG_LIST:
            return getJsonFromList((ArrayList)value, ParameterType.LONG);
        case LOCATION_LIST:
            return getJsonFromList((ArrayList)value, ParameterType.LOCATION);
        case COLOR:
            return getJsonFromColor((Color)value);            
        case AXIS_ANGLE_4D:
            return getJsonFromAxisAngle4d((AxisAngle4d)value);            

        case OBJECT: 
            if(value != null) return value.toString();
            else return null;
        }
    }


    /**
       convert array of Parameter into Map suitable for Json serialization
     */
    public static Map getJson(Parameter params[]){

        LinkedHashMap map = new LinkedHashMap();

        for(int i = 0; i < params.length; i++){
            Parameter param = params[i]; 
            map.put(param.getName(),getJsonValue(param.getValue(),param.getType()));
        }
        return map;
    }

    /**
       convert Parameterizable into Map suitable for Json serialization
     */
    public static Map getJson(Parameterizable par){

        LinkedHashMap map = new LinkedHashMap();
        map.put(CLASS, par.getClass().getName());        
        map.put(PARAMS, getJson(par.getParams()));
        return map;
    } 


    public static Vector3d getVector3dFromJson(JsonElement value){
    	Vector3d ret_val = null;
    	
    	// Handle both incoming value as array or map
    	if (value.isJsonArray()) {
            JsonArray array = value.getAsJsonArray();
            if(array.size() >= 3) {
                Double x = null;
                Double y = null;
                Double z = null;
                
                try {
                    x = array.get(0).getAsDouble();
                    y = array.get(1).getAsDouble();
                    z = array.get(2).getAsDouble();
                } catch (Exception e) {
                	e.printStackTrace();
                	throw new IllegalArgumentException(fmt("Invalid VECTOR_3D value: %s\n", value.toString()));
                }
                
                ret_val = new Vector3d(x,y,z);
            }
    	} else if (value.isJsonObject()) {
        	JsonElement x = null;
        	JsonElement y = null;
        	JsonElement z = null;
    		JsonObject obj = value.getAsJsonObject();
	        x = obj.get("x");
	        y = obj.get("y");
	        z = obj.get("z");
	        
	        if (x == null || y == null || z == null) {
	        	throw new IllegalArgumentException(fmt("Invalid VECTOR_3D value: %s\n", value.toString()));
	        }
	        
	        Double xval = null;
	        Double yval = null;
	        Double zval = null;
	        
	        try {
	            xval = x.getAsDouble();
	            yval = y.getAsDouble();
	            zval = z.getAsDouble();
	            ret_val = new Vector3d(xval,yval,zval);
	        } catch (Exception e) {
	        	e.printStackTrace();
	        	throw new IllegalArgumentException(fmt("Invalid VECTOR_3D value: %s\n", value.toString()));
	        }
    	}
    	
    	return ret_val;
    }

    public static Color getColorFromJson(JsonElement value){
    	JsonElement r = null;
    	JsonElement g = null;
    	JsonElement b = null;
    	JsonElement a = null;
    	
    	if (value.isJsonArray()) {
    		JsonArray array = value.getAsJsonArray();
	        if (array.size() >= 3) {
		        r = array.get(0);
		        g = array.get(1);
		        b = array.get(2);
	        }
	        if (array.size() >= 4) {
	        	a = array.get(3);
	        }
    	} else if (value.isJsonObject()) {
    		JsonObject obj = value.getAsJsonObject();
	        r = obj.get("r");
	        g = obj.get("g");
	        b = obj.get("b");
	        a = obj.get("a");
    	}
    	
        if (r == null || g == null || b == null) {
        	throw new IllegalArgumentException(fmt("Invalid Color value: %s\n", value.toString()));
        }
        
        Double rval = null;
        Double gval = null;
        Double bval = null;
        Double aval = 1.0;
        
        try {
        	rval = r.getAsDouble();
        	gval = g.getAsDouble();
        	bval = b.getAsDouble();
        	if (a != null) {
        		aval = a.getAsDouble();
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new IllegalArgumentException(fmt("Invalid Color value: %s\n", value.toString()));
        }

        return new Color(rval,gval,bval,aval);
    }

    public static Location getLocationFromJson(JsonElement value){
        JsonObject obj = value.getAsJsonObject();
        JsonElement pointObj = obj.get("point");
        Vector3d point = null;
        if (pointObj != null) {
            point = getVector3dFromJson(pointObj);
        }
        JsonElement normalObj = obj.get("normal");
        Vector3d normal = null;
        if (normalObj != null) {
            normal = getVector3dFromJson(normalObj);
        }

        return new  Location(point,normal);

    }

    public static AxisAngle4d getAxisAngle4dFromJson(JsonElement value){
    	JsonElement x = null;
    	JsonElement y = null;
    	JsonElement z = null;
    	JsonElement a = null;
    	
    	if (value.isJsonArray()) {
	        JsonArray array = value.getAsJsonArray();
	        if (array.size() >= 4) {
		        x = array.get(0);
		        y = array.get(1);
		        z = array.get(2);
		        a = array.get(3);
	        }
    	} else if (value.isJsonObject()) {
    		JsonObject obj = value.getAsJsonObject();
	        x = obj.get("x");
	        y = obj.get("y");
	        z = obj.get("z");
	        a = obj.get("angle");
    	}
    	
        if (x == null || y == null || z == null || a == null) {
        	throw new IllegalArgumentException(fmt("Invalid AXIS_ANGLE_4D value: %s\n", value.toString()));
        }
        
        Double xval = null;
        Double yval = null;
        Double zval = null;
        Double aval = null;
        
        try {
            xval = x.getAsDouble();
            yval = y.getAsDouble();
            zval = z.getAsDouble();
            aval = a.getAsDouble();
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new IllegalArgumentException(fmt("Invalid AXIS_ANGLE_4D value: %s\n", value.toString()));
        }
        
        return new AxisAngle4d(xval,yval,zval,aval);
    }

    public static ArrayList getStringListFromJson(JsonElement value){
        JsonArray array = value.getAsJsonArray();
        ArrayList str = new ArrayList(array.size());
        for(int i = 0; i < array.size(); i++){
            str.add(array.get(i).getAsString());
        } 
        return str;
    }

    
    public static Vector3d[] getVector3dArrayFromJson(JsonElement value, int expectedCount){

        JsonArray array = value.getAsJsonArray();        
        if(array.size() < expectedCount) 
            return null;

        Vector3d vect[] = new Vector3d[array.size()];        
        for(int i = 0; i < array.size(); i++){
            vect[i] = getVector3dFromJson(array.get(i));
        } 
        return vect;
    }

    public static Vector3d[] getVector3dArrayFromJson(JsonElement value){
        JsonArray array = value.getAsJsonArray();        
        Vector3d vect[] = new Vector3d[array.size()];
        for(int i = 0; i < array.size(); i++){
            vect[i] = getVector3dFromJson(array.get(i));
        } 
        return vect;
    }

    public static SNode getSNodeFromJson(JsonElement value){

        SNode snode = null;
        try {
            JsonObject obj = value.getAsJsonObject();
            String name = obj.get(CLASS).getAsString();
            snode = (SNode)(Class.forName(name).newInstance());
            Parameter params[] = ((Parameterizable)snode).getParams();
            JsonObject paramObj = obj.getAsJsonObject(PARAMS);
            getParamValuesFromJson(paramObj, params);            
        } catch(Exception e){
            e.printStackTrace();
        }
        return snode;
    }

    public static ArrayList getSNodeListFromJson(JsonElement value){
        JsonArray array = value.getAsJsonArray();
        ArrayList str = new ArrayList(array.size());
        for(int i = 0; i < array.size(); i++){
            str.add(getSNodeFromJson(array.get(i)));
        } 
        return str;
    }

    public static Map getUserDefinedFromJson(JsonObject value,UserDefinedParameter param) {
        HashMap<String,Parameter> ret_val = new HashMap<>();

        Map<String,Parameter> types = param.getProperties();

        for(Parameter p : types.values()) {
            JsonElement el = value.get(p.getName());

            if (el == null) continue;
            getParamValueFromJson(el,p);  // This feels weird to write the value into the types...
            ret_val.put(p.getName(),p);
        }

        return ret_val;
    }

    public static void getParamValueFromJson(JsonElement value, Parameter param){

        if(DEBUG) printf("parseJson(%s -> %s)\n", value, param);
        Object o = getObjectValueFromJson(value,param);
        param.setValue(o);
    }

    public static Object getObjectValueFromJson(JsonElement value, Parameter param) {

        if(DEBUG) printf("parseJson(%s -> %s)\n", value, param);
        switch(param.getType()){
            default:
                throw new RuntimeException(fmt("getJsonValue(%s, type:%s) not implemented\n", value.getClass().getName(), param.getType()));
            case BOOLEAN:
                return new Boolean(value.getAsJsonPrimitive().getAsBoolean());
            case DOUBLE:
                return new Double(value.getAsJsonPrimitive().getAsDouble());
            case FLOAT:
                return new Float(value.getAsJsonPrimitive().getAsFloat());
            case BYTE:
                return new Byte(value.getAsJsonPrimitive().getAsByte());
            case SHORT:
                return new Short(value.getAsJsonPrimitive().getAsShort());
            case INTEGER:
                return new Integer(value.getAsJsonPrimitive().getAsInt());
            case LONG:
                return new Long(value.getAsJsonPrimitive().getAsLong());
            case STRING:
                return value.getAsString();
            case STRING_LIST:
                return getStringListFromJson(value);
            case LOCATION:
                return getLocationFromJson(value);
            case ENUM:
                return value.getAsString();
            case URI:
                return value.getAsString();
            case SNODE:
                return getSNodeFromJson(value);
            case SNODE_LIST:
                return getSNodeListFromJson(value);
            case VECTOR_3D:
                return getVector3dFromJson(value);
            case AXIS_ANGLE_4D:
                return getAxisAngle4dFromJson(value);
            case COLOR:
                return getColorFromJson(value);
            case USERDEFINED:
                return getUserDefinedFromJson((JsonObject)value,(UserDefinedParameter)param);
        }
    }

    public static void getParamValuesFromJson(JsonObject obj, Parameter params[]){

        if(DEBUG) printf("parseJson(%s -> %s)\n", obj, params);
        
        for(int i = 0; i < params.length; i++){
            Parameter param = params[i];
            String name = param.getName();
            JsonElement value = obj.get(name);
            if(value != null)
                getParamValueFromJson(value, param);
        }
        
    }


    public static void getParamValuesFromJson(String json, Parameter params[]){

        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();
        //printf("elem: %s\n",obj.getClass().getName());
        getParamValuesFromJson(obj, params);
        
    }

    public static void getParamValuesFromJson(JsonObject obj, Map<String,Parameter> params){

        if(DEBUG) printf("parseJson(%s -> %s)\n", obj, params);

        for(Parameter param : params.values()){
            String name = param.getName();
            JsonElement value = obj.get(name);
            if(value != null)
                getParamValueFromJson(value, param);
        }

    }


    public static void getParamValuesFromJson(String json, Map<String,Parameter> params){

        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();
        //printf("elem: %s\n",obj.getClass().getName());
        getParamValuesFromJson(obj, params);

    }

    /**
       parses and set value of parameter from json string 
     */
    public static void getParamValueFromJson(String json, Parameter param){

        JsonParser parser = new JsonParser();
        JsonElement obj = parser.parse(json);
        getParamValueFromJson(obj, param);
        
    }



    public static SNode getSNodeFromJson(String json){

        JsonParser parser = new JsonParser();        
        JsonObject obj = parser.parse(json).getAsJsonObject();
        return getSNodeFromJson(obj);        
    }

    /**
       return value of parameter as JSON string 
     */
    public static String getValueAsJsonString(Parameter param){

        Gson gson = new GsonBuilder().create();
        return gson.toJson(getJsonValue(param.getValue(), param.getType()));  

    }

    /**
     parses and get's value of parameter from json string
     */
    public static Object getValueFromJson(String json, Parameter param) {

        if (json == null) return null;

        // Handle null value setting.  JSON doesn't really make this concept clear
        switch(param.getType()) {
            case URI:
            case STRING:
                break;
            default:
                if (json.equals("null") || json.equals("\"null\"")) {
                    return null;
                }
        }
        
        JsonParser parser = new JsonParser();
        JsonElement obj = parser.parse(json);
        return getObjectValueFromJson(obj, param);
    }
    
    /**
     * Get parameter value from map and type.
     * 
     * @param map The value map
     * @param param The parameter definition
     * @return A parameter specific value
     */
    public static Object getValueFromMap(Map<String, Object> map, Parameter param) {
    	if (map == null || map.size() == 0) return null;
    	
        switch(param.getType()) {
            case LOCATION:
            	return getLocationFromMap(map);
            case AXIS_ANGLE_4D:
            	return getAxisAngle4DFromMap(map);
            case VECTOR_3D:
            	return getVector3DFromMap(map);
            default:
            	return null;
        }
    }
    
    /**
     * Take a map of points and normals and return a Location.
     * 
     * @param value Map of points and normals
     * @return
     */
    public static Location getLocationFromMap(Map<String, Object> value){
    	Object pntObj = value.get("point");
    	Object norObj = value.get("normal");
    	
    	if (pntObj == null || norObj == null) return null;
    	
    	Vector3d point = null;
    	Vector3d normal = null;
    	
    	if (pntObj instanceof List) {
    		// point and normal as list
        	List<Double> pntList = (List) pntObj;
        	List<Double> norList = (List) norObj;
            
            if (pntList.size() >= 3) {
                point = new Vector3d(pntList.get(0), pntList.get(1), pntList.get(2));
            }
            if (norList.size() >= 3) {
                normal = new Vector3d(norList.get(0), norList.get(1), norList.get(2));
            }
    	} else if (pntObj instanceof Double[]) {
    		// Point and normal as array
        	Double[] pntArr = (Double[]) pntObj;
        	Double[] norArr = (Double[]) norObj;
            
            if (pntArr.length >= 3) {
                point = new Vector3d(pntArr[0], pntArr[1], pntArr[2]);
            }
            if (norArr.length >= 3) {
                normal = new Vector3d(norArr[0], norArr[1], norArr[2]);
            }
    	} else if (pntObj instanceof Map) {
    		// Point and normal as map (Vector3d)
    		Map<String, Double> pntMap = (Map<String, Double>) pntObj;
    		Map<String, Double> norMap = (Map<String, Double>) norObj;
    		
    		if (pntMap.get("x") == null || pntMap.get("y") == null || pntMap.get("z") == null ||
    			norMap.get("x") == null || norMap.get("y") == null || norMap.get("z") == null) {
    			throw new IllegalArgumentException(fmt("Invalid Location value: %s\n", value.toString()));
    		}
    		
    		point = new Vector3d(pntMap.get("x"), pntMap.get("y"), pntMap.get("z"));
    		normal = new Vector3d(norMap.get("x"), norMap.get("y"), norMap.get("z"));
    	}

        return new  Location(point,normal);
    }

    /**
     * Take a map containing values for x, y, z, and angle, and convert it to an AxisAngle4d.
     * @param value The map containing values for x, y, z, and angle
     * @return An AxisAngle4d
     */
    public static AxisAngle4d getAxisAngle4DFromMap(Map<String, Object> value){
    	Object xObj = value.get("x");
    	Object yObj = value.get("y");
    	Object zObj = value.get("z");
    	Object aObj = value.get("angle");
    	
    	if (xObj == null || yObj == null || zObj == null || aObj == null) {
    		throw new IllegalArgumentException(fmt("Invalid AXIS_ANGLE_4D value: %s\n", value.toString()));
    	}
    	
    	Double x = (Double) xObj;
    	Double y = (Double) yObj;
    	Double z = (Double) yObj;
    	Double angle = (Double) aObj;
    	
    	return new AxisAngle4d(x,y,z,angle);
    }
    
    /**
     * Take a map containing values for x, y, z and convert it to an Vector3d.
     * @param value The map containing values for x, y, z
     * @return A Vector3d
     */
    public static Vector3d getVector3DFromMap(Map<String, Object> value){
    	Object xObj = value.get("x");
    	Object yObj = value.get("y");
    	Object zObj = value.get("z");
    	
    	if (xObj == null || yObj == null || zObj == null) {
    		throw new IllegalArgumentException(fmt("Invalid VECTOR_3D value: %s\n", value.toString()));
    	}
    	
    	Double x = (Double) xObj;
    	Double y = (Double) yObj;
    	Double z = (Double) yObj;
    	
    	return new Vector3d(x,y,z);
    }
}
