/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2011
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.param;

// External Imports



import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Vector3d;
import javax.vecmath.AxisAngle4d;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import abfab3d.core.Color;

import abfab3d.datasources.Sphere;
import abfab3d.datasources.Union;
import abfab3d.datasources.Subtraction;
import abfab3d.datasources.Box;

import abfab3d.transforms.SphereInversion;
import abfab3d.transforms.Rotation;
import abfab3d.transforms.Translation;


import static abfab3d.core.Output.time;
import static abfab3d.core.Output.printf;

/**
 * Tests the functionality of ParamJson
 *
 * @version
 */
public class TestParamJson extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestParamCache.class);
    }

    public void testNothing(){
        
    }
    

    void devTestJsonUnion(){
        
        Sphere sphere = new Sphere(1,2,3,4);
        sphere.addTransform(new SphereInversion(new Vector3d(1,2,3),1));

        Box box = new Box();
        box.addTransform(new Translation(1,2,3));
        box.addTransform(new Rotation(0,1,0, Math.PI));

        Union un = new Union(box, new Subtraction(sphere, box));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String s = gson.toJson(ParamJson.getJson(un));
        printf("-------\n%s\n---\n", s);
                
    }

    void devTestJsonParamArray(){
        
        Parameter param[] = new Parameter[]{
            new DoubleParameter("size", 2),
            new IntParameter("count", 3),            
            new Vector3dParameter("point", new Vector3d(1,2,3)),
            new AxisAngle4dParameter("axisAngle", new AxisAngle4d(1,0,0, Math.PI)),
            new ColorParameter("color", new Color(1,0,0)),            
            new BooleanParameter("check", true),
            new LongParameter("long", 123456789),
            new EnumParameter("enum", new String[]{"item1","item2","item3"}, "item1"),            
            new URIParameter("uri", "c:/temp/something.png"),
            /*
            new StringListParameter("stringList", new String[]{"item1", "item2"}),
            new ObjectParameter("object", this),
            new SNodeParameter("shape", new Sphere(3.)),
            new LocationParameter("location", new Vector3d(2,2,2), new Vector3d(0,1,0)),
            */
        };

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String json = gson.toJson(ParamJson.getJson(param));
        printf("testing param array\n-------\n%s\n---\n", json);

        parseJson(json, param);
                
    }
    
    static void parseJson(String json, Parameter param[]){

        printf("parseJson()\n");
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();
        printf("elem: %s\n",obj.getClass().getName());
        
        for(int i = 0; i < param.length; i++){
            Parameter par = param[i];
            String name = par.getName();
            JsonElement value = obj.get(name);
            JsonPrimitive prim = value.getAsJsonPrimitive();
            printf("%s -> %s:%s bool:%s str:%s, num:%s\n", 
                   name, value.getClass().getName(), value, prim.isBoolean(), prim.isString(),prim.isNumber());
        }
        

    }

    public static void main(String args[]){

        //new TestParamJson().devTestJsonUnion();
        new TestParamJson().devTestJsonParamArray();

    }

}
