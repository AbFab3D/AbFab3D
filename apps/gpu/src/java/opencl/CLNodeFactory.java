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

package opencl; 

import java.util.Map;
import java.util.HashMap;

import abfab3d.param.Parameterizable;

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;

public class CLNodeFactory {
    
    static Map<String,String> sm_map = new HashMap<String,String>();
    static {
        sm_map.put("Union", "opencl.CLUnion");
        sm_map.put("Intersection", "opencl.CLIntersection");
        sm_map.put("Subtraction", "opencl.CLSubtraction");
        sm_map.put("Sphere", "opencl.CLSphere");
        sm_map.put("Gyroid", "opencl.CLGyroid");
        sm_map.put("SchwarzP", "opencl.CLSchwarzP");
        sm_map.put("SchwarzD", "opencl.CLSchwarzD");
        sm_map.put("Lidinoid", "opencl.CLLidinoid");
        sm_map.put("Torus", "opencl.CLTorus");
        sm_map.put("Box", "opencl.CLBox");
        sm_map.put("Translation", "opencl.CLTranslation");
        sm_map.put("Rotation", "opencl.CLRotation");
        sm_map.put("Scale", "opencl.CLScale");
        sm_map.put("CompositeTransform", "opencl.CLCompositeTransform");
        sm_map.put("Embossing", "opencl.CLEmbossing");
        sm_map.put("Image3D", "opencl.CLImage3D");
        sm_map.put("ImageMap", "opencl.CLImageMap");
        sm_map.put("DataSourceGrid", "opencl.CLGrid");

        sm_map.put("Text", "opencl.CLTextMap");
    }
    
    public CLNodeFactory(){        
    }
    
    public static CLCodeGenerator getCLNode(Parameterizable node){

        String name = node.getClass().getSimpleName();
        String sname = sm_map.get(name);        
        if(sname != null){
            try {
                return (CLCodeGenerator) Class.forName(sname).newInstance();
            } catch(Exception e){
                e.printStackTrace();
                throw new RuntimeException(fmt("can't instantiate %s", sname));
            }            
        } else {
            throw new RuntimeException(fmt("can't find CLCodeGenerator for %s", node.getClass().getName()));
        } 
    }    
}