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
import java.util.ArrayList;

import abfab3d.io.soap.encoders.DefaultObject;
import abfab3d.io.soap.encoders.Encodeable;

/**
 *
 * Encapsulates the UdesignModel data retrieved from the web service
 *
 * @author Russell Dodds
 * @version $Revision 1.1$
 */
public class PrinterType extends DefaultObject {

    /**
     * Constructor
     */
    public PrinterType() {
        soapElementName = "Printer";
        soapElementType = "Printer";
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Gets the title value for this PrinterType.
     *
     * @return title
     */
    public String getTitle() {
        return (String)getProperty("title");
    }

    /**
     * Sets the title value for this PrinterType.
     *
     * @param title
     */
    public void setTitle(String title) {
        setProperty("title", title);
    }

    /**
     * Gets the volume value for this PrinterType.
     *
     * @return volume
     */
    public float getVolume() {
        return (Float)getProperty("volume");
    }

    /**
     * Sets the volume value for this PrinterType.
     *
     * @param volume
     */
    public void setVolume(float volume) {
        setProperty("volume", volume);
    }

    /**
     * Gets the wallthickness value for this PrinterType.
     *
     * @return wallthickness
     */
    public float getWallthickness() {
        return (Float)getProperty("wallthickness");
    }

    /**
     * Sets the wallthickness value for this PrinterType.
     *
     * @param wallthickness
     */
    public void setWallthickness(float wallthickness) {
        setProperty("wallthickness", wallthickness);
    }

    /**
     * Gets the technology value for this PrinterType.
     *
     * @return technology
     */
    public String getTechnology() {
        return (String)getProperty("technology");
    }

    /**
     * Sets the technology value for this PrinterType.
     *
     * @param technology
     */
    public void setTechnology(String technology) {
        setProperty("technology", technology);
    }

    /**
     * Gets the x_bound_max value for this PrinterType.
     *
     * @return x_bound_max
     */
    public float getXBoundMax() {
        return (Float)getProperty("x_bound_max");
    }

    /**
     * Sets the x_bound_max value for this PrinterType.
     *
     * @param x_bound_max
     */
    public void setXBoundMax(float x_bound_max) {
        setProperty("x_bound_max", x_bound_max);
    }

    /**
     * Gets the x_bound_min value for this PrinterType.
     *
     * @return x_bound_min
     */
    public float getXBoundMin() {
        return (Float)getProperty("x_bound_min");
    }

    /**
     * Sets the x_bound_min value for this PrinterType.
     *
     * @param x_bound_min
     */
    public void setXBoundMin(float x_bound_min) {
        setProperty("x_bound_min", x_bound_min);
    }

    /**
     * Gets the y_bound_max value for this PrinterType.
     *
     * @return y_bound_max
     */
    public float getYBoundMax() {
        return (Float)getProperty("y_bound_max");
    }

    /**
     * Sets the y_bound_max value for this PrinterType.
     *
     * @param y_bound_max
     */
    public void setYBoundMax(float y_bound_max) {
        setProperty("y_bound_max", y_bound_max);
    }

    /**
     * Gets the y_bound_min value for this PrinterType.
     *
     * @return y_bound_min
     */
    public float getYBoundMin() {
        return (Float)getProperty("y_bound_min");
    }

    /**
     * Sets the y_bound_min value for this PrinterType.
     *
     * @param y_bound_min
     */
    public void setYBoundMin(float y_bound_min) {
        setProperty("y_bound_min", y_bound_min);
    }

    /**
     * Gets the z_bound_max value for this PrinterType.
     *
     * @return z_bound_max
     */
    public float getZBoundMax() {
        return (Float)getProperty("z_bound_max");
    }

    /**
     * Sets the z_bound_max value for this PrinterType.
     *
     * @param z_bound_max
     */
    public void setZBoundMax(float z_bound_max) {
        setProperty("z_bound_max", z_bound_max);
    }

    /**
     * Gets the z_bound_min value for this PrinterType.
     *
     * @return z_bound_min
     */
    public float getZBoundMin() {
        return (Float)getProperty("z_bound_min");
    }

    /**
     * Sets the z_bound_min value for this PrinterType.
     *
     * @param z_bound_min
     */
    public void setZBoundMin(float z_bound_min) {
        setProperty("z_bound_min", z_bound_min);
    }

    /**
     * Gets the materials value for this PrinterType.
     *
     * @return materials
     */
    public MaterialType[] getMaterials() {

        ArrayList<Encodeable> list = (ArrayList<Encodeable>)getProperty("materials");

        int len = list.size();
        MaterialType[] materials = new MaterialType[len];

        for (int i = 0; i < len; i++) {
            materials[i] = (MaterialType)list.get(i);
        }

        return materials;

    }

    /**
     * Sets the materials value for this PrinterType.
     *
     * @param materials
     */
    public void setMaterials(MaterialType[] materials) {
        setProperty("materials", materials);
    }

}
