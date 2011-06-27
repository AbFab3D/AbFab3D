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
public class PrinterArrayType extends DefaultObject {

    /**
     * Constructor
     */
    public PrinterArrayType() {
        soapElementName = "getPrintersResponse";
        soapElementType = "getPrintersResponse";
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Gets the
     *
     * @return
     */
    public PrinterType[] getPrinters() {

        PrinterType[] printers = null;
        ArrayList<Encodeable> list = (ArrayList<Encodeable>)getProperty("getPrintersReturn");

        if (list != null) {

            int len = list.size();
            printers = new PrinterType[len];

            for (int i = 0; i < len; i++) {
                printers[i] = (PrinterType)list.get(i);
            }

        }

        return printers;
    }

    /**
     * Sets the
     *
     * @param
     */
    public void setPrinters(PrinterType[] printers) {
        setProperty("getPrintersReturn", printers);
    }

}
