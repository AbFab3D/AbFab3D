/*****************************************************************************
 *                        Copyright Shapeways BV (c) 2005 - 2007
 *                               Java Source
 *
 * All rights reserved.
 *
 *
 *
 *
 *
 ****************************************************************************/

package abfab3d.io.shapeways;

//External Imports
//none

//Internal Imports
import abfab3d.io.soap.encoders.DefaultObject;

/**
 *
 * Encapsulates the SWModel data structure.
 *
 * @author Alan Hudson
 * @version $Revision 1.1$
 */
public class SWModelType extends DefaultObject {

    /**
     * Constructor
     */
    public SWModelType() {
        soapElementName = "SWModel";
        soapElementType = "SWModel";
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Gets the desc value for this SWModel.
     *
     * @return desc
     */
    public String getDesc() {
        return (String)getProperty("desc");
    }

    /**
     * Sets the desc value for this SWModel.
     *
     * @param desc
     */
    public void setDesc(String desc) {
        setProperty("desc", desc);
    }

    /**
     * Gets the file_uri value for this SWModel.
     *
     * @return file
     */
    public String getFileURI() {
        return (String)getProperty("file_uri");
    }


    /**
     * Sets the file_uri value for this SWModel.
     *
     * @param file_uri
     */
    public void setFileURI(String fileURI) {
        setProperty("file_uri", fileURI);
    }

    /**
     * Gets the filename value for this SWModel.
     *
     * @return file
     */
    public String getFilename() {
        return (String)getProperty("filename");
    }


    /**
     * Sets the filename value for this SWModel.
     *
     * @param filename
     */
    public void setFilename(String filename) {
        setProperty("filename", filename);
    }

    /**
     * Gets the file value for this SWModel.
     *
     * @return file
     */
    public byte[] getFile() {
        return (byte[])getProperty("file");
    }

    /**
     * Sets the file value for this SWModel.
     *
     * @param file
     */
    public void setFile(byte[] file) {
        setProperty("file", file);
    }

    /**
     * Gets the availability value for this SWModel.
     *
     * @return availability
     */
    public int getAvailability() {
        return (Integer)getProperty("availability");
    }

    /**
     * Sets the availability value for this SWModel.
     * 0=Owner, 1 Everyone
     *
     * @param availability
     */
    public void setAvailability(Integer availability) {
        setProperty("availability", availability);
    }

    /**
     * Gets the title value for this SWModel.
     *
     * @return title
     */
    public String getTitle() {
        return (String)getProperty("title");
    }

    /**
     * Sets the title value for this SWModel.
     *
     * @param title
     */
    public void setTitle(String title) {
        setProperty("title", title);
    }

    /**
     * Gets the model type for this SWModel.
     *
     * @return model type
     */
    public String getModelType() {
        return (String)getProperty("modeltype");
    }

    /**
     * Sets the model type for this SWModel.
     *
     * @param val The new value
     */
    public void setModelType(String val) {
        setProperty("modeltype", val);
    }

    /**
     * Gets the view state for this SWModel.
     *
     * @return view state(0-2)
     */
    public String getViewState() {
        // TODO: WSDL says String, docs say Integer
        return (String)getProperty("view_state");
    }

    /**
     * Sets the view state for this SWModel.
     *
     * @param view state(0-2)
     */
    public void setViewState(String viewState) {
        setProperty("viewState", viewState);
    }

    /**
     * Gets the tags value for this SWModel.  Comma
     * delimeted string,
     *
     * @return tags
     */
    public String getTags() {
        return (String)getProperty("tags");
    }

    /**
     * Sets the tags value for this SWModel.
     *
     * @param tags
     */
    public void setTags(String tags) {
        setProperty("tags", tags);
    }

    /**
     * Gets the markup value for this SWModel.
     *
     * @return The value
     */
    public float getMarkup() {
        return (Float)getProperty("markup");
    }

    /**
     * Sets the markup
     *
     * @param val The value
     */
    public void setMarkup(float val) {
        setProperty("markup", val);
    }

    /**
     * Sets the HasColor for this SWModelType.
     *
     * @param val
     */
    public void setHasColor(Boolean val) {
        setProperty("has_color", val);
    }

    /**
     * Gets the has color value for this SWModelType.
     *
     * @return hasColor
     */
    public boolean getHasColor() {
        return (Boolean)getProperty("has_color");
    }

    /**
     * Sets the scale value for this SWModelType.
     *
     * @param volume
     */
    public void setScale(Float scale) {
        setProperty("scale", scale);
    }

    /**
     * Gets the scale value for this SWModelType.
     *
     * @return volume
     */
    public float getScale() {
        return (Float)getProperty("scale");
    }

    // TODO: The following properites come back from getModel.
    // Perhaps they should be separated

    /**
     * Gets the volume value for this SWModelType.
     *
     * @return volume
     */
    public float getVolume() {
        return (Float)getProperty("volume");
    }

    /**
     * Sets the volume value for this SWModelType.
     *
     * @param volume
     */
    public void setVolume(Float volume) {
        setProperty("volume", volume);
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

}
