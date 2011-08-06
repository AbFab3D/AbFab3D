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

package abfab3d.creator.shapeways;

// External Imports
import java.io.InputStream;

// Internal Imports

/**
 * Local version of the services API.  Returns a positive response for all queries.
 * Mimics hosted responses but doesn't really implement them.
 *
 * @author Alan Hudson
 */
public class LocalServicesAPI implements ServicesAPI {
    private static ServicesAPI instance;

    private LocalServicesAPI() {
    }

    /**
     * Checks whether a file is printable.  Implements some parts of the model
     * checking pipeline.  Attemps to give a fast check on whether a model is
     * good to print.
     *
     * @param is The stream to check
     * @param materials The materials to check for.  Use ID returned from ShapewaysAPI.getPrinters
     * @return The status
     */
    public PrintableStatus[] isPrintable(InputStream is, int[] materials) {
        int len = materials.length;

        PrintableStatus[] ret_val = new PrintableStatus[materials.length];

        for(int i=0; i < len; i++) {
            ret_val[i] = PrintableStatus.OK;
        }

        return ret_val;
    }

    public static ServicesAPI getInstance() {
        if (instance == null) {
            instance = new LocalServicesAPI();
        }

        return instance;
    }
}