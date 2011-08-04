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
import java.util.HashMap;

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
            params.put("bodyImageInvert", "false");
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
            kernal.generate(params,bos);

            bos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}