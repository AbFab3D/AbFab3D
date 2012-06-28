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
 * Encapsulates the Orientation data retrieved from the web service
 *
 * @author Russell Dodds
 * @version $Revision 1.1$
 */
public class OrientationType extends DefaultObject {

    /**
     * Constructor
     */
    public OrientationType() {
        soapElementName = "Orientation";
        soapElementType = "Orientation";
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Gets the x-axis value for this Orientation.
     *
     * @return x
     */
    public Float getX() {
        return (Float)getProperty("x");
    }

    /**
     * Sets the x-axis value for this Orientation.
     *
     * @param desc
     */
    public void setX(Float x) {
        setProperty("x", x);
    }

    /**
     * Gets the y-axis value for this Orientation.
     *
     * @return y
     */
    public Float getY() {
        return (Float)getProperty("y");
    }

    /**
     * Sets the y-axis value for this Orientation.
     *
     * @param desc
     */
    public void setY(Float y) {
        setProperty("y", y);
    }

    /**
     * Gets the z-axis value for this Orientation.
     *
     * @return z
     */
    public Float getZ() {
        return (Float)getProperty("z");
    }

    /**
     * Sets the z-axis value for this Orientation.
     *
     * @param z
     */
    public void setZ(Float z) {
        setProperty("z", z);
    }

    /**
     * Gets the angle value for this Orientation.
     *
     * @return z
     */
    public Float getAngle() {
        return (Float)getProperty("a");
    }

    /**
     * Sets the angle value for this Orientation.
     *
     * @param angle
     */
    public void setAngle(Float angle) {
        setProperty("a", angle);
    }

}
