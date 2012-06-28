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
public class SubmitModelResponse extends DefaultObject {

    /**
     * Constructor
     */
    public SubmitModelResponse() {
        soapElementName = "submitModelResponse";
        soapElementType = "submitModelResponse";
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Gets the state value
     *
     * @return state
     */
    public String getState() {
        return (String)getProperty("State");
    }

    /**
     * Sets the state value.
     *
     * @param val
     */
    public void setState(String val) {
        setProperty("state", val);
    }

    /**
     * Gets the state value
     *
     * @return state
     */
    public String getResponse() {
        return (String)getProperty("response");
    }

    /**
     * Sets the state value.
     *
     * @param val
     */
    public void setResponse(String val) {
        setProperty("response", val);
    }

    // TODO: No longer have modelID returned?
    /**
     * Gets the desc value for this UdesignModel.
     *
     * @return desc
     */
/*
    public int getId() {
        return (Integer)getProperty("modelId");
    }
*/

    /**
     * Sets the desc value for this UdesignModel.
     *
     * @param desc
     */
/*
    public void setId(Integer id) {
        setProperty("modelId", id);
    }
*/
}
