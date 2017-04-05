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
import abfab3d.datasources.Image3D;

import abfab3d.transforms.SphereInversion;
import abfab3d.transforms.Rotation;
import abfab3d.transforms.Translation;
import abfab3d.transforms.SymmetryTransform;
import abfab3d.transforms.ReflectionSymmetry;


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
        
        Parameter params[] = new Parameter[]{
            //new DoubleParameter("size", 0.12345678901234567890123456789e10),
            //new DoubleParameter("pi", Math.PI),
            //new IntParameter("count", 325),            
            //new Vector3dParameter("point", new Vector3d(1,2,3)),
            //new AxisAngle4dParameter("axisAngle", new AxisAngle4d(1,0,0, Math.PI)),
            //new ColorParameter("color", new Color(1,0.5,0.3)),            
            //new BooleanParameter("check", true),
            //new LongParameter("long", 123456789),
            //new EnumParameter("enum", new String[]{"item1","item2","item3"}, "item1"),            
            //new URIParameter("uri", "c:/temp/something.png"),            
            //new StringListParameter("stringList", new String[]{"item1", "item2"}),
            //new LocationParameter("location", new Vector3d(2.1,2.2,2.3), new Vector3d(0,1,0)),
            //new ObjectParameter("object", this),
            //new SNodeParameter("shape", new Sphere(3.)),
            new SNodeParameter("shape1", new Sphere(3.).rotate(1,0,0,Math.PI).translate(1,2,3)),
            new SNodeParameter("shape2", new Sphere(3.)),
           
        };

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String json = gson.toJson(ParamJson.getJson(params));
        printf("testing param array\n-------\n%s\n---\n", json);

        ParamJson.getParamValuesFromJson(json, params);

        json = gson.toJson(ParamJson.getJson(params));
        printf("parsed params array\n-------\n%s\n---\n", json);
        
                
    }
    
    void devTestJsonSNode(){
        
        Box box = new Box(1,2,3);
        box.setTransform(new SymmetryTransform());
        box.addTransform(new ReflectionSymmetry());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(ParamJson.getJson(box));
        printf("testing param array\n-------\n%s\n---\n", json);
        
    }


    public static void main(String args[]){

        //new TestParamJson().devTestJsonUnion();
        //new TestParamJson().devTestJsonParamArray();
        new TestParamJson().devTestJsonSNode();

    }

}
