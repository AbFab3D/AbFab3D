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

// External imports
import java.util.ArrayList;


// Local imports
import abfab3d.util.ApplicationParams;

/**
 * Threaded handler that will take care of the extended loading requirements
 * needed to grab a materials over a web service.
 * <p>
 *
 * Interacts with the {@link IOManager} to do the external processing request.
 * Each instance is a one-off used to create a model. It is a runnable, meaning
 * that the caller will need to create an appropriate wrapper thread for it.
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class MaterialLoader
    implements Runnable {

    /** The list status listeners */
    private ArrayList<MaterialLoaderListener> statusListeners;

    /** Error message when the model loading fails */
    private static final String MODEL_LOAD_FAIL_MSG =
        "The material loading failed for some unknown reason";

    /** Flat indicating that this should be terminated early */
    private boolean shutdown;

    /**
     * Create a new loader instance that will retrieve all
     * the available materials
     *
     * @param monitor The progress monitor
     * @param reporter an error reporter
     */
    public MaterialLoader() {

        statusListeners = new ArrayList<MaterialLoaderListener>();
        shutdown = false;
    }

    //---------------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------------

    /**
     * Run the loading process as a separate thread now
     */
    public void run() {

        ArrayList<MaterialType> materials = null;

        try {
            if(shutdown)
                return;

            fireStartRequest("Fetch Materials");

            // get the templateId
            int templateId = (Integer)ApplicationParams.get("TEMPLATE_ID");

            // execute the web service call
            IOManager ioManager = IOManager.getIOManager();
            PrinterArrayType p = ioManager.getPrinters();
            PrinterType[] pt = p.getPrinters();

            if((pt != null) && (pt.length > 0)) {
                // get all the materials from all printers
                materials = new ArrayList<MaterialType>();

                for (int i = 0; i < pt.length; i++) {

                    MaterialType[] materialList = pt[i].getMaterials();

                    if(materialList == null) {
                        materials = null;
                    } else {
                        for (int j = 0; j < materialList.length; j++) {
                            MaterialType mat = materialList[j];

                            if(mat != null) {
                                materials.add(mat);
                            }
                        }

                        if(materials.size() <= 0) {
                            materials = null;
                        }
                    }
                }
            }
        } catch (Exception ex) {

        } finally {

            fireEndRequest(materials);

        }

    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Shut down the loading process now.
     */
    void shutdown() {
        shutdown = true;
    }

    /**
     * Add a WebServiceStatusListener. Duplicates are ignored.
     *
     * @param l The listener
     */
    public void addMaterialLoaderListener(MaterialLoaderListener l) {

        if (!statusListeners.contains(l)) {
            statusListeners.add(l);
        }

    }

    /**
     * Remove a WebServiceStatusListener.
     *
     * @param l The listener
     */
    public void removeMaterialLoaderListener(MaterialLoaderListener l) {
        statusListeners.remove(l);
    }

    /**
     * Notify listeners of started request.
     *
     * @param message The message to send to the end user
     */
    public void fireStartRequest(String message) {

        // notify listeners of the change
        for (int i = 0; i < statusListeners.size(); i++) {
            MaterialLoaderListener l = statusListeners.get(i);
            l.startRequest(message);
        }

    }

    /**
     * Notify listeners of ended request.
     *
     * @param materials The list of materials that were loaded
     */
    public void fireEndRequest(ArrayList<MaterialType> materials) {

        if(materials != null) {
            // notify listeners of the change
            for (int i = 0; i < statusListeners.size(); i++) {
                MaterialLoaderListener l = statusListeners.get(i);
                l.endRequest(materials);
            }
        }
    }

}
