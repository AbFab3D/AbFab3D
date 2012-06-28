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

import java.awt.Component;
import java.awt.GridLayout;

import java.util.ArrayList;

import javax.swing.*;

import org.web3d.util.ErrorReporter;
import org.j3d.geom.GeometryData;

// Local imports

/**
 * Threaded handler that will take care of the extended loading requirements
 * needed to grab a model over a web service.
 * <p>
 *
 * Interacts with the {@link IOManager} to do the external processing request.
 * Each instance is a one-off used to create a model. It is a runnable, meaning
 * that the caller will need to create an appropriate wrapper thread for it.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class MeshSaver
    implements
        Runnable,
        IOManagerListener {

    /** Error message when the model saving starts */
    private static final String MODEL_SAVE_START_MSG =
        "Saving the model...";

    /** Error message when the model saving succeeds */
    private static final String MODEL_SAVE_SUCCESS_MSG =
        "Saving the model...done!";

    /** Error message when the model saving is canceled */
    private static final String MODEL_SAVE_CANCEL_MSG =
        "Saving the model...canceled!";

    /** Error message when the model loading fails */
    private static final String MODEL_SAVE_FAIL_MSG =
        "Saving the model...failed, invalid object.";

    /** The ErrorReporter for messages */
    private final ErrorReporter errorReporter;

    /** The currently managed data */
    private SWModelType model;

    /** Flat indicating that this should be terminated early */
    private boolean shutdown;

    /** Used to communicated to the web services */
    private IOManager ioManager;

    /** What kind of request was this */
    private IORequestType requestType;

    /**
     * Create a new saver instance that will place the
     * model the database
     *
     * @param entity The entity to save
     * @param parent The parent component for the dialog
     * @param reporter an error reporter
     */
    public MeshSaver(
            SWModelType model,
            IORequestType requestType,
            ErrorReporter reporter) {

        this.model = model;
        this.requestType = requestType;
        this.errorReporter = reporter;

        ioManager = IOManager.getIOManager();
        ioManager.addIOManagerListener(this);

        shutdown = false;

    }

    //---------------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------------

    /**
     * Run the loading process as a separate thread now
     */
    public void run() {

        if(shutdown)
            return;

        if(shutdown)
            return;

        // execute the web service call
        String state = ioManager.saveNewModel(model);

        if(shutdown)
            return;

        // TODO: Do we need to save modelID
/*
        // update the ID just in case
        if (entity instanceof MeshEntity) {
            ((MeshEntity)entity).setModelId(modelId);
        } else if (entity instanceof UDesignEntity) {
            ((UDesignEntity)entity).setModelId(modelId);
        }
*/

System.out.println("Need to figure out what success looks like");
        if (state.equals("Success")) {
            //System.out.println("modelId: " + modelId);
            ioManager.fireEndRequest(requestType, state);
        }

    }

    //----------------------------------------------------------
    // Methods required by the IOManagerListener interface
    //----------------------------------------------------------

    /**
     * A request had begun.
     *
     * @param type The type of request being processed
     * @param data The data to send to the end user
     */
    public void startRequest(IORequestType type, Object data) {
    }

    /**
     * A request had been updated but the percentage is to remain.
     *
     * @param type The type of request being processed
     * @param data The data to send to the end user
     */
    public void updateRequest(IORequestType type, Object data) {
    }

    /**
     * A request had been updated.
     *
     * @param type The type of request being processed
     * @param data The data to send to the end user
     * @param percentage The percent complete
     */
    public void updateRequest(IORequestType type, Object data, int precentage) {
    }

    /**
     * A request has completed.
     *
     * @param type The type of request being processed
     * @param data The data to send to the end user
     */
    public void endRequest(IORequestType type, Object data) {

        switch (type) {

            case CANCEL_SAVE_MODEL:
                ioManager.shutdown();
                ioManager.stopRequest();
                shutdown = true;
                break;

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

}
