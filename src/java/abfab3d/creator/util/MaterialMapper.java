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

package abfab3d.creator.util;

// External Imports
import java.util.*;
import org.web3d.vrml.sav.BinaryContentHandler;

// Internal Imports

// none

/**
 * Maps material specifications to X3D appearance nodes.  Used to
 * commonize the implementation of material and finish technologies.  The
 * stream will start with an Appearance node call.
 *
 * Currently only outputs FIXED shading.
 *
 * Quality is very subjective.  The intent is to allow for the request
 * of lower or higher quality versions based on receiving hardware.
 * Ranges from 1-10.  1 is 2011 cell phone specs. 10 is cluster enabled ray tracer.
 * Sadly of course realtime has different tradeoffs then software so...
 *
 * @author Alan Hudson
 */
public class MaterialMapper {
    /** What type of shading technology should we use */
    public enum Shading {FIXED, SHADER};

    /**
     * Create an X3D appearance node from a material and finish specification.
     *
     * @param material The physical material
     * @param finish The finish steps applied
     * @param shading The shading technology to use for output
     * @param quality A value from 1-10 indicating how much computation resources to use
     * @param handler The X3D content handler to output too.
     */
    public void createAppearance(String material, String[] finish, Shading shading,
        int quality, BinaryContentHandler handler) {

        if (material == null || material.equalsIgnoreCase("White Strong & Flexible")) {
            applyWSFNonPolished(handler,quality);
        } else if (material.equals("Stainless Steel")) {
            boolean glossy = false;     // glossy or matte finish
            int color = 0;  // 0 = steel, 1 = AntiqueBronze, 2 = gold

            if (finish != null) {
                for(int i=0; i < finish.length; i++) {
                    if (finish[i].equalsIgnoreCase("Glossy")) {
                        glossy = true;
                    } else if (finish[i].equalsIgnoreCase("Antique Bronze")) {
                        color = 1;
                    } else if (finish[i].equalsIgnoreCase("Gold")) {
                        color = 2;
                    }
                }
            }

            if (glossy) {
                switch(color) {
                    case 0:
                        applyStainlessSteelGlossy(handler,quality);
                        break;
                    case 1:
                        applyStainlessSteelAntiqueBronzeGlossy(handler,quality);
                        break;
                    case 2:
                        applyStainlessSteelGoldGlossy(handler,quality);
                        break;
                    default:
                        System.out.println("Unknown Stainless Steel color: " + color);
                        applyStainlessSteelGlossy(handler,quality);
                }
            } else {
                switch(color) {
                    case 0:
                        applyStainlessSteelMatte(handler,quality);
                        break;
                    case 1:
                        applyStainlessSteelAntiqueBronzeMatte(handler,quality);
                        break;
                    case 2:
                        applyStainlessSteelGoldMatte(handler,quality);
                        break;
                    default:
                        System.out.println("Unknown Stainless Steel color: " + color);
                        applyStainlessSteelMatte(handler,quality);
                }
            }
        } else if (material.equals("Ceramics")) {
            int color = 0;  // 0 = white

            if (finish != null) {
                for(int i=0; i < finish.length; i++) {
                    if (finish[i].equalsIgnoreCase("White")) {
                        color = 0;
                    }
                }
            }

            switch(color) {
                case 0:
                    applyCeramicsWhite(handler,quality);
                    break;
                default:
                    System.out.println("Unknown Ceramics color: " + color);
                    applyCeramicsWhite(handler,quality);
            }
        } else {
            // apply a default material
            System.out.println("Unknown material: " + material);
            applyWSFNonPolished(handler, quality);
        }
    }

    /**
     * White Strong Flexible with no finishes.
     *
     * @param handler The X3D content handler to output too
     * @param quality A value from 1-10 indicating how much computation resources to use
     */
    public void applyWSFNonPolished(BinaryContentHandler handler, int quality) {
        handler.startNode("Appearance", null);
        handler.startField("material");
        handler.startNode("Material", null);
        handler.startField("diffuseColor");
        handler.fieldValue(new float[] {0.9f, 0.9f, 0.9f},3);
        handler.startField("specularColor");
        handler.fieldValue(new float[] {0.95f, 0.95f, 0.95f},3);
        handler.startField("shininess");
        handler.fieldValue(0.9f);
        handler.endNode();  // Material
        handler.endNode();  // Appearance
    }

    /**
     * Stainless Steel with Glossy Finish
     *
     * @param handler The X3D content handler to output too
     * @param quality A value from 1-10 indicating how much computation resources to use
     */
    public void applyStainlessSteelGlossy(BinaryContentHandler handler, int quality) {
        handler.startNode("Appearance", null);
        handler.startField("material");
        handler.startNode("Material", null);
        handler.startField("diffuseColor");
        handler.fieldValue(new float[] {0.2f, 0.2f, 0.2f},3);
        handler.startField("specularColor");
        handler.fieldValue(new float[] {0.9f, 0.9f, 0.9f},3);
        handler.startField("shininess");
        handler.fieldValue(0.7f);
        handler.endNode();  // Material
        handler.endNode();  // Appearance
    }

    /**
     * Stainless Steel with Matte Finish
     *
     * @param handler The X3D content handler to output too
     * @param quality A value from 1-10 indicating how much computation resources to use
     */
    public void applyStainlessSteelMatte(BinaryContentHandler handler, int quality) {
        handler.startNode("Appearance", null);
        handler.startField("material");
        handler.startNode("Material", null);
        handler.startField("diffuseColor");
        handler.fieldValue(new float[] {0.2f, 0.2f, 0.2f},3);
        handler.startField("specularColor");
        handler.fieldValue(new float[] {0.9f, 0.9f, 0.9f},3);
        handler.startField("shininess");
        handler.fieldValue(0.7f);
        handler.endNode();  // Material
        handler.endNode();  // Appearance
    }

    /**
     * Stainless Steel with Antique Bronze and Glossy Finish
     *
     * @param handler The X3D content handler to output too
     * @param quality A value from 1-10 indicating how much computation resources to use
     */
    public void applyStainlessSteelAntiqueBronzeGlossy(BinaryContentHandler handler, int quality) {
        handler.startNode("Appearance", null);
        handler.startField("material");
        handler.startNode("Material", null);
        handler.startField("diffuseColor");
        handler.fieldValue(new float[] {0.2f, 0.2f, 0.2f},3);
        handler.startField("specularColor");
        handler.fieldValue(new float[] {0.75f, 0.55f, 0.173f},3);
        handler.startField("shininess");
        handler.fieldValue(0.5f);
        handler.endNode();  // Material
        handler.endNode();  // Appearance
    }

    /**
     * Stainless Steel with Antique Bronze and Matte Finish
     *
     * @param handler The X3D content handler to output too
     * @param quality A value from 1-10 indicating how much computation resources to use
     */
    public void applyStainlessSteelAntiqueBronzeMatte(BinaryContentHandler handler, int quality) {
        handler.startNode("Appearance", null);
        handler.startField("material");
        handler.startNode("Material", null);
        handler.startField("diffuseColor");
        handler.fieldValue(new float[] {0.2f, 0.2f, 0.2f},3);
        handler.startField("specularColor");
        handler.fieldValue(new float[] {0.75f, 0.55f, 0.173f},3);
        handler.startField("shininess");
        handler.fieldValue(0.5f);
        handler.endNode();  // Material
        handler.endNode();  // Appearance
    }

    /**
     * Stainless Steel with Gold and Glossy Finish
     *
     * @param handler The X3D content handler to output too
     * @param quality A value from 1-10 indicating how much computation resources to use
     */
    public void applyStainlessSteelGoldGlossy(BinaryContentHandler handler, int quality) {
        handler.startNode("Appearance", null);
        handler.startField("material");
        handler.startNode("Material", null);
        handler.startField("ambientIntensity");
        handler.fieldValue(0.4f);
        handler.startField("diffuseColor");
        handler.fieldValue(new float[] {0.22f, 0.15f, 0f},3);
        handler.startField("specularColor");
        handler.fieldValue(new float[] {0.71f, 0.70f, 0.56f},3);
        handler.startField("shininess");
        handler.fieldValue(0.4f);
        handler.endNode();  // Material
        handler.endNode();  // Appearance
    }

    /**
     * Stainless Steel with Gold and Matte Finish
     *
     * @param handler The X3D content handler to output too
     * @param quality A value from 1-10 indicating how much computation resources to use
     */
    public void applyStainlessSteelGoldMatte(BinaryContentHandler handler, int quality) {
        handler.startNode("Appearance", null);
        handler.startField("material");
        handler.startNode("Material", null);
        handler.startField("ambientIntensity");
        handler.fieldValue(0.4f);
        handler.startField("diffuseColor");
        handler.fieldValue(new float[] {0.22f, 0.15f, 0f},3);
        handler.startField("specularColor");
        handler.fieldValue(new float[] {0.71f, 0.70f, 0.56f},3);
        handler.startField("shininess");
        handler.fieldValue(0.16f);
        handler.endNode();  // Material
        handler.endNode();  // Appearance
    }

    /**
     * Ceramics with white finish.
     *
     * @param handler The X3D content handler to output too
     * @param quality A value from 1-10 indicating how much computation resources to use
     */
    public void applyCeramicsWhite(BinaryContentHandler handler, int quality) {
        handler.startNode("Appearance", null);
        handler.startField("material");
        handler.startNode("Material", null);
        handler.startField("diffuseColor");
        handler.fieldValue(new float[] {0.9f, 0.9f, 0.9f},3);
        handler.startField("specularColor");
        handler.fieldValue(new float[] {0.95f, 0.95f, 0.95f},3);
        handler.startField("shininess");
        handler.fieldValue(0.2f);
        handler.endNode();  // Material
        handler.endNode();  // Appearance
    }
}