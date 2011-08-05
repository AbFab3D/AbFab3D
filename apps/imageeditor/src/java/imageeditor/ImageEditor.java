/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package imageeditor;

// External Imports
import java.io.*;
import java.util.*;

import abfab3d.creator.*;
import abfab3d.creator.shapeways.*;

// Internal Imports

/**
 * An example editor using images.
 *
 * @author Alan Hudson
 */
public class ImageEditor extends HostedCreator {
    private GeometryKernal kernal;

    public ImageEditor() {
    }

    public GeometryKernal getKernal() {
        if (kernal == null) {
            kernal = new ImageEditorKernal();
        }

        return kernal;
    }

    public static void main(String[] args) {
        ImageEditor editor = new ImageEditor();
        GeometryKernal kernal = editor.getKernal();

        HashMap<String,String> params = new HashMap<String,String>();

        try {
            FileOutputStream fos = new FileOutputStream("out.x3db");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
/*
            // Cube pendant Subtract

//            params.put("resolution", "0.00005");
            params.put("resolution", "0.00013");
            params.put("bodyWidth", ".025");
            params.put("bodyHeight", ".04");
            params.put("bodyDepth", ".0032");
//            params.put("bodyGeometry", "NONE");
            params.put("bodyGeometry", "CUBE");
            params.put("bodyImage", "images/cat.png");
//            params.put("bodyImage", "NONE");
            params.put("bodyImageType", "SQUARE");  // Add DETECT
            // Look at supporting:  formula: bodyDepth + 0.002
//            params.put("bodyImageDepth", "-0.0028");
            // This value changes with resolution somewhat :(
            // Should be a token meaning "whole grid"
            params.put("bodyImageDepth", "0.0042");
            params.put("bodyImageInvert", "true");
            params.put("minWallThickness", "0.003");  // Metal

//            params.put("bailStyle","NONE");
            params.put("bailStyle","TORUS");
            params.put("bailInnerRadius",".001");
            params.put("bailOuterRadius",".004");
*/

            // Cube pendant Add

//            params.put("resolution", "0.00005");
            params.put("resolution", "0.0001");
            params.put("bodyWidth", ".025");
            params.put("bodyHeight", ".04");
            params.put("bodyDepth", ".0032");
            params.put("bodyGeometry", "NONE");
            params.put("bodyImage", "images/CoolChip.jpg");
//            params.put("bodyImage", "NONE");
            // Look at supporting:  formula: bodyDepth + 0.002
//            params.put("bodyImageDepth", "-0.0028");
            // This value changes with resolution somewhat :(
            // Should be a token meaning "whole grid"
            params.put("bodyImageDepth", "-0.0042");
            params.put("bodyImageType", "SQUARE");  // Add DETECT
            params.put("bodyImageInvert", "true");
            params.put("minWallThickness", "0.001");

//            params.put("bailStyle","NONE");
            params.put("bailStyle","TORUS");
            params.put("bailInnerRadius",".001");
            params.put("bailOuterRadius",".004");

/*
            // Cylinder Pendant Subtract

//            params.put("resolution", "0.0001");
            params.put("resolution", "0.00013");
            params.put("bodyWidth", ".04");
            params.put("bodyHeight", ".04");
            params.put("bodyDepth", ".0032");
            params.put("bodyGeometry", "CYLINDER");
//            params.put("bodyGeometry", "NONE");
//            params.put("bodyImage", "images/circular_sign.png");
            params.put("bodyImage", "images/circ_rotorbluete.jpg");
            params.put("bodyImageDepth", "0.03");
            params.put("bodyImageType", "CIRCULAR");  // Add DETECT

            params.put("bodyImageInvert", "false");
//            params.put("minWallThickness", "0.003");  // Metal
            params.put("minWallThickness", "0.002");  // Metal

            params.put("bailStyle","TORUS");
            params.put("bailInnerRadius",".001");
            params.put("bailOuterRadius",".004");
*/

            Map<String, Object> parsed_params = parseParams(kernal.getParams(),params);
            kernal.generate(parsed_params,bos);

            bos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static Map<String, Object> parseParams(Map<String,Parameter> defs,
        Map<String,String> vals) {

        Iterator<Parameter> itr = defs.values().iterator();

        HashMap<String, Object>  ret_val = new HashMap<String, Object>();

        while(itr.hasNext()) {
            Parameter p = itr.next();

            String raw_val = vals.get(p.getName());

            if (raw_val == null) {
                raw_val = p.getDefaultValue();
            }

            Object val = null;

System.out.println("Parsing param: " + p.getName() + " val: " + raw_val);
            try {
                switch(p.getDataType()) {
                    case STRING:
                        val = raw_val;
                        break;
                    case DOUBLE:
                        val = Double.parseDouble(raw_val);
                        break;
                    case BOOLEAN:
                        val = Boolean.parseBoolean(raw_val);
                        break;
                    case ENUM:
                        val = raw_val;
                        String[] valid_values = p.getEnumValues();

                        if (valid_values == null) {
                            if (!raw_val.equals("")) {
                                throw new IllegalArgumentException("Error Validating " + p.getName() + " invalid enum value: " + raw_val);
                            }
                            break;
                        }
                        boolean valid = false;

                        for(int i=0; i < valid_values.length; i++) {
                            if (raw_val.equals(valid_values[i])) {
                                valid = true;
                                break;
                            }
                        }

                        if (!valid) {
                            throw new IllegalArgumentException("Error Validating " + p.getName() + " invalid enum value: " + raw_val);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unhandled datatype: " + p.getDataType());
                }
            } catch(Exception e) {
                throw new IllegalArgumentException("Error parsing: " + p.getName() + " value: " + raw_val);
            }

            ret_val.put(p.getName(), val);
        }

System.out.println("ret: " + ret_val);
        return ret_val;
    }
}