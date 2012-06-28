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

package abfab3d.io.shapeways;

//External Imports
//none

//Internal Imports
import abfab3d.io.soap.encoders.DefaultObject;

/**
 *
 * Encapsulates the UdesignModel data retrieved from the web service
 *
 * @author Russell Dodds
 * @version $Revision 1.1$
 */
public class MaterialType extends DefaultObject {

    /** Defaults */
    private static final String DEFAULT_COLOR = "0.8 0.8 0.8";
    private static final int DEFAULT_SHININESS = 64;
    private static final float DEFAULT_TRANSPARANCY = 1;
    private static final float DEFAULT_REFRACTIVE = 1;

    /**
     * Constructor
     */
    public MaterialType() {
        soapElementName = "Material";
        soapElementType = "Material";
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Gets the id value for this UdesignModel.
     *
     * @return id
     */
    public int getId() {
        return (Integer)getProperty("id");
    }

    /**
     * Sets the id value for this UdesignModel.
     *
     * @param id
     */
    public void setId(Integer id) {
        setProperty("id", id);
    }

    /**
     * Gets the title value for this UdesignModel.
     *
     * @return title
     */
    public String getTitle() {
        return (String)getProperty("title");
    }

    /**
     * Sets the title value for this UdesignModel.
     *
     * @param title
     */
    public void setTitle(String title) {
        setProperty("title", title);
    }

    /**
     * Gets the desc value for this UdesignModel.
     *
     * @return desc
     */
    public String getDescription() {
        return (String)getProperty("description");
    }

    /**
     * Sets the desc value for this UdesignModel.
     *
     * @param desc
     */
    public void setDescription(String description) {
        setProperty("description", description);
    }

    /**
     * Gets the cost_per_cc value for this PrinterType.
     *
     * @return cost_per_cc
     */
    public float getCostPerCC() {
        return (Float)getProperty("cost_per_cc");
    }

    /**
     * Sets the cost_per_cc value for this PrinterType.
     *
     * @param cost_per_cc
     */
    public void setCostPerCC(float cost_per_cc) {
        setProperty("cost_per_cc", cost_per_cc);
    }

    /**
     * Gets the base_color value for this UdesignModel.
     *
     * @return base_color
     */
    public String getBaseColor() {

        String retVal = (String)getProperty("base_color");
        if (retVal == null) {
            retVal = DEFAULT_COLOR;
        }

        return retVal;

    }

    /**
     * Sets the base_color value for this UdesignModel.
     *
     * @param base_color
     */
    public void setBaseColor(String base_color) {
        setProperty("base_color", base_color);
    }

    /**
     * Gets the specular_color value for this UdesignModel.
     *
     * @return specular_color
     */
    public String getSpecularColor() {

        String retVal = (String)getProperty("specular_color");
        if (retVal == null) {
            retVal = DEFAULT_COLOR;
        }

        return retVal;

    }

    /**
     * Sets the specular_color value for this UdesignModel.
     *
     * @param specular_color
     */
    public void setSpecularColor(String specular_color) {
        setProperty("specular_color", specular_color);
    }

    /**
     * Gets the shineness value for this UdesignModel.
     *
     * @return shineness
     */
    public int getShineness() {

        Integer retVal = (Integer)getProperty("shineness");
        if (retVal == null) {
            retVal = DEFAULT_SHININESS;
        }

        return retVal;

    }

    /**
     * Sets the shineness value for this UdesignModel.
     *
     * @param shineness
     */
    public void setShineness(Integer shineness) {
        setProperty("shineness", shineness);
    }

    /**
     * Gets the opacity value for this UdesignModel.
     *
     * @return opacity
     */
    public float getOpacity() {

        Float retVal = (Float)getProperty("opacity");
        if (retVal == null) {
            retVal = DEFAULT_TRANSPARANCY;
        }

        return retVal;

    }

    /**
     * Sets the opacity value for this UdesignModel.
     *
     * @param opacity
     */
    public void setOpacity(Float opacity) {
        setProperty("opacity", opacity);
    }

    /**
     * Gets the translucency value for this UdesignModel.
     *
     * @return translucency
     */
    public float getTranslucency() {

        Float retVal = (Float)getProperty("translucency");
        if (retVal == null) {
            retVal = DEFAULT_TRANSPARANCY;
        }

        return retVal;

    }

    /**
     * Sets the translucency value for this UdesignModel.
     *
     * @param translucency
     */
    public void setTranslucency(Float translucency) {
        setProperty("translucency", translucency);
    }

    /**
     * Gets the refractive_index value for this UdesignModel.
     *
     * @return refractive_index
     */
    public float getRefractiveIndex() {

        Float retVal = (Float)getProperty("refractive_index");
        if (retVal == null) {
            retVal = DEFAULT_REFRACTIVE;
        }

        return retVal;

    }

    /**
     * Sets the refractive_index value for this UdesignModel.
     *
     * @param refractive_index
     */
    public void setRefractiveIndex(Float refractive_index) {
        setProperty("refractive_index", refractive_index);
    }

    /**
     * A String representing this object
     */
    public String toString() {
        return getTitle();
    }


}
