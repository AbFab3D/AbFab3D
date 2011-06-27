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

// External Imports
import java.util.ArrayList;

// Internal Imports

/**
 * A listener for loading materials.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public interface MaterialLoaderListener {

    /**
     * A request for materials had begun.
     *
     * @param message The message to send to the end user
     */
    public void startRequest(String message);

    /**
     * A request for materials has completed. The material list
     * is presented in the order given by the server.
     *
     * @param materials The current list of materials
     */
    public void endRequest(ArrayList<MaterialType> materials);

}
