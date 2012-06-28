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
 * Encapsulates the SWModelPriceType data retrieved from the web service
 *
 * @author Russell Dodds
 * @version $Revision 1.1$
 */
public class SWModelPriceType extends DefaultObject {

    /**
     * Constructor
     */
    public SWModelPriceType() {
        soapElementName = "getModelPriceResponse";
        soapElementType = "getModelPriceResponse";
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Gets the price value for this SWModelPrice.
     *
     * @return price
     */
    public Float getPrice() {
        return (Float)getProperty("price");
    }

    /**
     * Sets the price value for this SWModelPrice.
     *
     * @param price
     */
    public void setPrice(Float price) {
        setProperty("price", price);
    }

    /**
     * Gets the tax value for this SWModelPrice.
     *
     * @return includes_tax
     */
    public boolean getIncludesTax() {
        return (Boolean)getProperty("includes_tax");
    }

    /**
     * Sets the include_tax value for this SWModelPrice.
     *
     * @param includes_tax
     */
    public void setIncludesTax(boolean includes_tax) {
        setProperty("includes_tax", includes_tax);
    }

    /**
     * Gets the include_shipping value for this SWModelPrice.
     *
     * @return includes_shipping
     */
    public boolean getIncludesShipping() {
        return (Boolean)getProperty("includes_shipping");
    }

    /**
     * Sets the include_shipping value for this SWModelPrice.
     *
     * @param includes_shipping
     */
    public void setIncludesShipping(boolean includes_shipping) {
        setProperty("includes_shipping", includes_shipping);
    }

    /**
     * Gets the currency value for this SWModelPrice.
     *
     * @return currency
     */
    public String getCurrency() {
        return (String) getProperty("currency");
    }

    /**
     * Sets the currency value for this SWModelPrice.
     *
     * @param currency
     */
    public void setCurrency(String currency) {
        setProperty("currency", currency);
    }
}
