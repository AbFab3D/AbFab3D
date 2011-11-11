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
import java.io.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.text.NumberFormat;

import javax.vecmath.Vector3f;

import org.j3d.util.ErrorReporter;
import org.j3d.geom.GeometryData;

// Internal Imports
import abfab3d.io.soap.SOAPException;
import abfab3d.io.soap.SOAPFault;
import abfab3d.io.soap.envelope.Envelope;
import abfab3d.io.soap.envelope.Call;
import abfab3d.io.soap.transport.Transport;
import abfab3d.io.soap.transport.HTTPTransport;
import abfab3d.io.soap.transport.PercentageProgressMonitor;

import abfab3d.util.*;

/**
 * A manager class to handle the server communications.
 *
 * @author Russell Dodds
 * @version $Revision: 1.6 $
 */
public class IOManager {

    /**
     * The download speed to base our upload time calcs in.
     * In megabytes per second.
     */
    private static final float UPLOAD_SPEED = 0.125f;

    /** Messages to display during the loading process */
    private static final String MSG_SAVING_MODEL = "Saving the Model";

    /** Message when creating the raw X3D file from the mesh entity */
    private static final String MSG_GENERATING_X3D = "Generating your model.";

    /** Message at the initial message generation. */
    private static final String MSG_CONNECTING = "Connecting to the server.";

    /** First part of message once we begin the final write to the server */
    private static final String MSG_SENDING_LINE_1 =
        "Uploading your model to the server.";

    private static final String MSG_SENDING_LINE_2 =
        "The model file size is ";

    /** Second part of the upload message */
    private static final String MSG_SENDING_LINE_3 =
        "On a 1Mbit connection this will take approximately ";

    /** Message prepended section before SOAP failures in fetchModel */
    private static final String FETCH_MODEL_SOAP_FAIL_MSG =
        "Error during the SOAP model fetch webservice processing: ";

    /** Message prepended section before generic failures in fetchModel */
    private static final String FETCH_MODEL_OTHER_FAIL_MSG =
        "Unexpected error during the model fetch webservice processing: ";

    /** Message prepended section before SOAP failures in fetchModelErrors */
    private static final String FETCH_ERROR_SOAP_FAIL_MSG =
        "Error during the SOAP error data fetch webservice processing: ";

    /** Message prepended section before generic failures in fetchModelErrors */
    private static final String FETCH_ERROR_OTHER_FAIL_MSG =
        "Unexpected error during the error data fetch webservice processing: ";

    /** The number of chances to login */
    private static final int AUTH_ATTEMPTS = 3;

    private static final String SERVICE_URN = "urn:udesign.wsdl";

    private static final String SOAP_ACTION = "";

    /** The singleton class */
    private static IOManager ioManager;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /** Number formatter for error message generation */
    private NumberFormat megabyteFormatter;

    /** Number formatter for error message generation */
    private NumberFormat secondsFormatter;

    /** debug flag */
    private boolean debug;

    /** The current sessionId */
    private String sessionId;

    /** The list IOManager listeners */
    private ArrayList<IOManagerListener> ioListeners;

    /** The single sessionListener */
    private WebServiceListener webServiceListener;

    /** The SOAP service URL */
    private String soapService;

    /** The WSDL service URL */
    private String wsdlService;

    /** The URN of the service */
    private String urnService;

    /** Boolean to indicate whether to create the x3d file or nor */
    private boolean generateFile;

    /** The list of possible materials */
    private HashMap<Integer, MaterialType> materials;

    /** The current count of auth tries */
    private int currentAuthAttempts;

    /** Has the model been saved */
    private boolean saved;

    /** flag used to stop the request if necessary */
    private boolean shutdown;

    /** The connection protocall */
    private final ThreadLocal<HTTPTransport> transport;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private IOManager() {

        ioListeners = new ArrayList<IOManagerListener>();

        errorReporter = new SysErrorReporter();

        soapService =
            (String)ApplicationParams.get("SOAP_SERVER") +
            (String)ApplicationParams.get("SOAP_PATH");

        wsdlService =
            (String)ApplicationParams.get("WSDL_SERVER") +
            (String)ApplicationParams.get("WSDL_PATH");

        urnService = (String) ApplicationParams.get("SERVICE_URN");

        currentAuthAttempts = 0;
        shutdown = false;
        saved = false;

        transport = new SOAPDataThreaded();

        megabyteFormatter = NumberFormat.getNumberInstance();
        megabyteFormatter.setMinimumIntegerDigits(1);
        megabyteFormatter.setMinimumFractionDigits(0);
        megabyteFormatter.setMaximumFractionDigits(2);

        secondsFormatter = NumberFormat.getNumberInstance();
        secondsFormatter.setMinimumIntegerDigits(1);
        secondsFormatter.setMinimumFractionDigits(0);
        secondsFormatter.setMaximumIntegerDigits(2);
        secondsFormatter.setMaximumFractionDigits(0);

    }

    /**
     * Get the singleton IOManager.
     *
     * @return The IOManager
     */
    public static IOManager getIOManager() {
        if (ioManager == null) {
            ioManager = new IOManager();
        }

        return ioManager;
    }


    // ----------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------

    /**
     * Shut down the IO manager as the application is exiting. This is
     * a one-way trip so should be called ith caution. Calling this will
     * prevent the IO manager from starting ever again.
     */
    public void shutdown() {
        shutdown = true;
    }

    /**
     * Stop whatever request is currently being processed
     */
    public void stopRequest() {
        HTTPTransport tpt = transport.get();;
        tpt.stopRequest();
    }

    /**
     * Set the debug state
     *
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Set the current sessionID
     *
     * @param sessionID
     */
    public void setSessionID(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Get the current sessionID
     *
     * @return sessionID
     */
    public String getSessionID() {
        return sessionId;
    }

    /**
     * Sets the map of materials available.
     *
     * @param mats The new list of materials to set
     */
    public void setMaterials(ArrayList<MaterialType> mats) {

        materials = new HashMap<Integer, MaterialType>();

        // Build a map of materials
        for(int i = 0; i < mats.size(); i++) {
            MaterialType m = mats.get(i);
            materials.put(m.getId(), m);
        }
    }

    /**
     * Returns the map of materials available.  Should only be called after recieving
     * notification of the materials being loaded.
     *
     * @return
     */
    public HashMap<Integer, MaterialType> getMaterials() {
        return materials;
    }

    /**
     * Get the available printers.
     *
     */
    public PrinterArrayType getPrinters() {

        boolean fetched = false;

        PrinterArrayType printers = new PrinterArrayType();

        currentAuthAttempts = 0;
        shutdown = false;

        // get the templateId
        Integer templateId = (Integer)ApplicationParams.get("TEMPLATE_ID");

        while (!fetched && !shutdown) {

            try {
                Envelope requestEnvelope = new Envelope();

                requestEnvelope.addAttribute("xmlns:udesign", wsdlService);

                Call theCall = new Call(requestEnvelope);
                if (debug)
                    theCall.enableMessageDebug(true);

                theCall.setMethodName("udesign:getPrinters");
                theCall.setTargetObjectURI(urnService);
                theCall.addParameter("sessId", sessionId);
                if (templateId != null)
                    theCall.addParameter("templateId", templateId);

                Transport tpt = transport.get();
                tpt.initialize(soapService, SOAP_ACTION);

                theCall.invoke(tpt, printers);

                fetched = true;

                // Clean up
                requestEnvelope = null;
                theCall = null;

            } catch (SOAPFault sf) {

                processFault(sf);

            } catch (SOAPException se) {

                // unknown error occured
                webServiceListener.requestFailed(se.getMessage());
                shutdown = true;

            } catch (Exception e) {

                // unknown error occured
                webServiceListener.requestFailed("Unknown error making SOAP call");
                shutdown = true;

            }

        }

        return printers;

    }

    /**
     * Fetch just the model properties
     *
     * @param modelID The ID of the model
     * @return UdesignModel the java object representing the model properties
     */
    public SWModelType fetchModelProperties(String modelID) {

        boolean fetched = false;
        SWModelType modelReturn = new SWModelType();
        currentAuthAttempts = 0;
        shutdown = false;

        while (!fetched && !shutdown) {

            try {
                Envelope requestEnvelope = new Envelope();

                requestEnvelope.addAttribute("xmlns:udesign", wsdlService);

                Call theCall = new Call(requestEnvelope);
                if (debug)
                    theCall.enableMessageDebug(true);

                theCall.setMethodName("udesign:getModelProperties");
                theCall.setTargetObjectURI(urnService);
                theCall.addParameter("sessId", sessionId);
                theCall.addParameter("modelId", modelID);
                theCall.addParameter("include_x3d", false);

                Transport tpt = transport.get();
                tpt.initialize(soapService, SOAP_ACTION);

                theCall.invoke(tpt, modelReturn);

                fetched = true;

                // Clean up
                requestEnvelope = null;
                theCall = null;

            } catch (SOAPFault sf) {

                processFault(sf);

            } catch (SOAPException se) {

                // unknown error occured
                webServiceListener.requestFailed(se.getMessage());
                shutdown = true;

            } catch (Exception e) {

                // unknown error occured
                webServiceListener.requestFailed(e.getMessage());
                shutdown = true;

            }

        }

        return modelReturn;

    }

    /**
     * Fetch a model.
     * TODO: Not exposed to public API currently
     *
     * @param fileLocation http location of a file
     * @return a URL connection to the file
     */
    public URLConnection fetchModel(String fileLocation){

        URLConnection retVal = null;
        try {
            URL url = new URL(fileLocation);
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) connection;
            httpConn.setRequestMethod("GET");

            connection.connect();
            retVal = connection;

        } catch (Exception e) {
            String err = e.getMessage();

            if((err == null) || err.length() == 0)
                err = e.getClass().getName();

            errorReporter.messageReport(err + "\nUnable to access file at " +
                    fileLocation);
        }
        return retVal;
    }


    /**
     * Fetch the X3D content from the server
     *
     * @param model_ID The ID of the model
     * @return InputStream The stream of data
     */
    public URLConnection fetchModel(int model_ID) {

        boolean fetched = false;
        currentAuthAttempts = 0;
        shutdown = false;
        URLConnection retVal = null;
        // get the modelID
        String modelID = String.valueOf(model_ID);

        while (!fetched && !shutdown) {

            try {

                Envelope requestEnvelope = new Envelope();
                SWModelType modelReturn = new SWModelType();

                requestEnvelope.addAttribute("xmlns:udesign", wsdlService);

                Call theCall = new Call(requestEnvelope);
                if (debug)
                    theCall.enableMessageDebug(true);

                theCall.setMethodName("udesign:getModelProperties");
                theCall.setTargetObjectURI(urnService);
                theCall.addParameter("sessId", sessionId);
                theCall.addParameter("modelId", modelID);
                theCall.addParameter("include_x3d", false);

                Transport tpt = transport.get();
                tpt.initialize(soapService, SOAP_ACTION);

                theCall.invoke(tpt, modelReturn);

                String fileURI = modelReturn.getFileURI();

                URL url = new URL(fileURI);

                URLConnection connection = url.openConnection();
                HttpURLConnection httpConn = (HttpURLConnection) connection;
                httpConn.setRequestMethod("GET");

                connection.connect();

                retVal = connection;

                fetched = true;

                // Clean up
                modelReturn = null;
                requestEnvelope = null;
                theCall = null;

            } catch (SOAPFault sf) {
                processFault(sf);
            } catch (SOAPException se) {
                String msg = FETCH_MODEL_SOAP_FAIL_MSG + se.getMessage();
                webServiceListener.requestFailed(msg);
                shutdown = true;
            } catch (Exception e) {
                String err = e.getMessage();

                if((err == null) || err.length() == 0)
                    err = e.getClass().getName();

                String msg = FETCH_MODEL_OTHER_FAIL_MSG + err;
                webServiceListener.requestFailed(msg);
                shutdown = true;
            }
        }

        if (shutdown) {
            fireEndRequest(IORequestType.REQUEST_LOAD_MODEL, "");
            return null;
        }

        return retVal;

    }

    /**
     * Save the model to the server
     *
     * @param file The file to save
     * @param modelType The type of model in the file
     * @return The state
     */
    public String saveNewModel(SWModelType model) {
        boolean status = false;
        currentAuthAttempts = 0;
        shutdown = false;
        int modelID = -1;
        saved = false;
        byte[] bytes = new byte[0];
        float downloadSize;
        String state = "Unknown";

        // get the templateId

        Integer templateId = (Integer) ApplicationParams.get("TEMPLATE_ID");
        String applicationId = (String) ApplicationParams.get("APPLICATION_ID");

        fireStartRequest(IORequestType.REQUEST_SAVE_MODEL, MSG_SAVING_MODEL);

        while (!status && !shutdown) {

            try {
                downloadSize = model.getFile().length / (1024.0f * 1024.0f);

                fireUpdateRequest(IORequestType.REQUEST_SAVE_MODEL_IN_PROGRESS, MSG_CONNECTING);

                Envelope requestEnvelope = new Envelope();
                SubmitModelResponse modelReturn = new SubmitModelResponse();

                requestEnvelope.addAttribute("xmlns:udesign", wsdlService);

                Call theCall = new Call(requestEnvelope);
                if (debug)
                    theCall.enableMessageDebug(true);

                theCall.setMethodName("udesign:submitModel");
                theCall.setTargetObjectURI(urnService);

                theCall.addParameter("session_id", sessionId);

                theCall.addParameter("model", model);

                if (templateId != null)
                    theCall.addParameter("templateId", templateId);

                if (applicationId != null)
                    theCall.addParameter("application_id", applicationId);

                int uploadTime = (int)(downloadSize / UPLOAD_SPEED);
                int uploadMinutes = (int)Math.floor(uploadTime / 60);
                float uploadSeconds = uploadTime % 60;

                StringBuffer line2 = new StringBuffer(MSG_SENDING_LINE_2);
                line2.append(megabyteFormatter.format(downloadSize));
                line2.append("MB.");

                StringBuffer line3 = new StringBuffer(MSG_SENDING_LINE_3);
                line3.append(uploadMinutes);
                line3.append("m ");
                line3.append(secondsFormatter.format(uploadSeconds));
                line3.append("s.");

                String[] big_msg = {
                    MSG_SENDING_LINE_1,
                    line2.toString(),
                    line3.toString(),
                };

                fireUpdateRequest(IORequestType.REQUEST_SAVE_MODEL_IN_PROGRESS,
                                  big_msg);


                Transport tpt = transport.get();
                tpt.initialize(soapService, SOAP_ACTION);
                tpt.setProgressMonitor(
                        new SaveNewModelProgressMonitor( this,
                                IORequestType.REQUEST_SAVE_MODEL_IN_PROGRESS,
                                MSG_SENDING_LINE_1) );

                theCall.invoke(tpt, modelReturn);

                // clear monitor since iomanager stores transport
                tpt.setProgressMonitor(null);

                if (!shutdown) {
//                    state = modelReturn.getState();
                    state = modelReturn.getResponse();
                }

                status = true;
                saved = true;
            } catch (SOAPFault sf) {
                processFault(sf);
            } catch (SOAPException se) {

                // unknown error occured
                webServiceListener.requestFailed(se.getMessage());
                shutdown = true;
            } catch (Exception e) {
e.printStackTrace();
                // unknown error occured
                webServiceListener.requestFailed(e.getMessage());
                shutdown = true;

            }

        }

        if (shutdown) {
            modelID = -1;
            fireEndRequest(IORequestType.CANCEL_SAVE_MODEL, "");
        }

        return state;
    }

    /**
     * A progress monitor while models are saved.
     */
    private static class SaveNewModelProgressMonitor extends PercentageProgressMonitor {

        private final IOManager manager;
        private final IORequestType requestType;
        private final String message;

        /**
         * Create a progress monitor which reports on transfers as a percentage
         * of total bytes transferred.
         *
         * @param requestType type of request being monitored
         * @param message data to send to the end user
         */
        private SaveNewModelProgressMonitor(
                IOManager manager, IORequestType requestType, String message) {
            this.manager = manager;
            this.requestType = requestType;
            this.message = message;
        }

        /**
         * Update any {@link IOManagerListener} objects of the transfer status.
         *
         * @param percentage the current percentage of bytes transferred
         */
        @Override
        public void updatePercentage(int percentage) {
            this.manager.fireUpdateRequest(
                    this.requestType, this.message, percentage);
        }
    }

    /**
     * Save the model to the cart for purchase
     *
     * @param entity The GeometryData that contains the coordinates
     * and other geometry information.
     */
/*
    public void saveToCart(GeometryData entity) {

        boolean status = false;
        currentAuthAttempts = 0;
        shutdown = false;

        fireStartRequest(IORequestType.REQUEST_SAVE_TO_CART, "");

        while (!status && !shutdown) {

            try {

                Envelope requestEnvelope = new Envelope();
                AddModelToCartResponse addToCartResponse = new AddModelToCartResponse();

                requestEnvelope.addAttribute("xmlns:udesign", wsdlService);

                abfab3d.io.soap.envelope.Call theCall = new abfab3d.io.soap.envelope.Call(
                        requestEnvelope);

                theCall.setMethodName("udesign:addModelToCart");
                theCall.setTargetObjectURI(urnService);
                theCall.addParameter("sessId", sessionId);
                theCall.addParameter("modelId", entity.getModelId());
                theCall.addParameter("materialId", entity.getMaterialId());

                Transport tpt = transport.get();
                tpt.initialize(soapService, SOAP_ACTION);

                theCall.invoke(tpt, addToCartResponse);

                status = true;

            } catch (SOAPFault sf) {
                processFault(sf);
            } catch (SOAPException se) {

                // unknown error occured
                webServiceListener.requestFailed(se.getMessage());
                shutdown = true;

            } catch (Exception e) {

                // unknown error occured
                webServiceListener.requestFailed(e.getMessage());
                shutdown = true;

            }

        }

        if (shutdown) {
            fireEndRequest(IORequestType.CANCEL_SAVE_TO_CART, "");
        } else {
            fireEndRequest(IORequestType.REQUEST_SAVE_TO_CART, "");
        }

    }
*/
    /**
     * The the price of a mesh
     *
     * @param volume The volume.
     * @param materialID The material to price
     */
    public SWModelPriceType getModelPrice(float volume, int material) {
        boolean status = false;
        currentAuthAttempts = 0;
        shutdown = false;
        SWModelPriceType priceResponse = new SWModelPriceType();

        // get the templateId
        Integer templateId = (Integer)ApplicationParams.get("TEMPLATE_ID");

        while (!status && !shutdown) {

            try {

                Envelope requestEnvelope = new Envelope();

                requestEnvelope.addAttribute("xmlns:udesign", wsdlService);

                Call theCall = new Call(requestEnvelope);
                if (debug)
                    theCall.enableMessageDebug(true);
                theCall.setMethodName("udesign:getModelPrice");
                theCall.setTargetObjectURI(urnService);
                theCall.addParameter("sessId", sessionId);
                theCall.addParameter("modelVolume", volume);
                theCall.addParameter("materialId", material);
                if (templateId != null)
                    theCall.addParameter("templateId", templateId);

                Transport tpt = transport.get();
                tpt.initialize(soapService, SOAP_ACTION);

                theCall.invoke(tpt, priceResponse);

                status = true;

            } catch (SOAPFault sf) {
                processFault(sf);
            } catch (SOAPException se) {

                // unknown error occured
                webServiceListener.requestFailed(se.getMessage());
                shutdown = true;

            } catch (Exception e) {

                // unknown error occured
                webServiceListener.requestFailed(e.getMessage());
                shutdown = true;

            }

        }

        return priceResponse;

    }

    /**
     * Sends the user name and password to the server for authentication.
     *
     * @param username
     * @param password
     */
    public void login(String username, String password) {

        try {

            Envelope requestEnvelope = new Envelope();
            LoginResponse loginReturn = new LoginResponse();

            requestEnvelope.addAttribute("xmlns:udesign", wsdlService);

            Call theCall = new Call(requestEnvelope);
            if (debug)
                theCall.enableMessageDebug(true);

            theCall.setMethodName("udesign:login");
            theCall.setTargetObjectURI(urnService);
            theCall.addParameter("username", username);
            theCall.addParameter("password", password);

            Transport tpt = transport.get();
            tpt.initialize(soapService, SOAP_ACTION);

            theCall.invoke(tpt, loginReturn);

            sessionId = loginReturn.getSessionId();

            currentAuthAttempts = 0;

        } catch (SOAPFault sf) {
            processFault(sf);
        } catch (SOAPException se) {

            // unknown error occured
            webServiceListener.requestFailed(se.getMessage());
            shutdown = true;

        } catch (Exception e) {
e.printStackTrace();
            // unknown error occured
            webServiceListener.requestFailed(e.getMessage());
            shutdown = true;

        }

    }

    /**
     * Determine how to notify the user of a SOAP exception
     *
     * @param sf
     */
    private void processFault(SOAPFault sf) {

        // catch SOAP Faults
        String faultString = sf.getFaultString();
        int faultCode = sf.getFaultCode();

        switch (faultCode) {
            case 1:
            case 8:
                // session timeout
                if (currentAuthAttempts >= AUTH_ATTEMPTS) {
                    webServiceListener.requestFailed(faultString);
                    shutdown = true;
                    return;
                }

                currentAuthAttempts++;

                Object[] retVal = webServiceListener.sessionTimeout(faultString);

                if (retVal != null && !shutdown) {

                    String username = (String) retVal[0];
                    char[] passVal = (char[]) retVal[1];
                    StringBuilder password = new StringBuilder();
                    for (int i = 0; i < passVal.length; i++) {
                        password.append(passVal[i]);
                    }
                    login(username, password.toString());

                }

                break;
            case 2:
                // invalid model
            case 3:
                // failed manifold check
            case 4:
                // failed wall thickness check
            default:
                // unknown error occured
                webServiceListener.requestFailed(faultString);
                shutdown = true;
                break;
        }

    }

    /**
     * Notify listeners of started request.
     *
     * @param type The type of request being processed
     * @param data The data to send to the end user
     */
    public void fireStartRequest(IORequestType type, Object data) {

        // notify listeners of the change
        for (int i = 0; i < ioListeners.size(); i++) {
            IOManagerListener l = ioListeners.get(i);
            l.startRequest(type, data);
        }

    }

    /**
     * Notify listeners a request had been updated.
     *
     * @param type The type of request being processed
     * @param data The data to send to the end user
     */
    public void fireUpdateRequest(IORequestType type, Object data) {

        // notify listeners of the change
        for (int i = 0; i < ioListeners.size(); i++) {
            IOManagerListener l = ioListeners.get(i);
            l.updateRequest(type, data);
        }

    }

    /**
     * Notify listeners a request had been updated.
     *
     * @param type The type of request being processed
     * @param data The data to send to the end user
     * @param percentage The percent complete
     */
    public void fireUpdateRequest(IORequestType type, Object data, int percentage) {

        // notify listeners of the change
        for (int i = 0; i < ioListeners.size(); i++) {
            IOManagerListener l = ioListeners.get(i);
            l.updateRequest(type, data, percentage);
        }

    }


    /**
     * Notify listeners of started request.
     *
     * @param type The type of request being processed
     * @param data The data to send to the end user
     */
    public void fireEndRequest(IORequestType type, Object data) {

        // notify listeners of the change
        for (int i = 0; i < ioListeners.size(); i++) {
            IOManagerListener l = ioListeners.get(i);
            l.endRequest(type, data);
        }

    }

    /**
     * Notify listeners of a failed model lookup
     *
     * @param message The message to send to the end user
     */
    public void fireRequestFailed(String message) {

        if (webServiceListener != null) {
            webServiceListener.requestFailed(message);
        }

    }

    /**
     * Set the sessionListener, should only be one
     *
     * @param l
     */
    public void setWebServiceListener(WebServiceListener l) {
        webServiceListener = l;
    }

    /**
     * Add a IOManagerListener. Duplicates are ignored.
     *
     * @param l The listener
     */
    public void addIOManagerListener(IOManagerListener l) {

        if (!ioListeners.contains(l)) {
            ioListeners.add(l);
        }

    }

    /**
     * Remove a IOManagerListener.
     *
     * @param l The listener
     */
    public void removeIOManagerListener(IOManagerListener l) {
        ioListeners.remove(l);
    }

    /**
     * Register an error reporter with the command instance so that any errors
     * generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;
    }

    /**
     * Clean up all values
     */
    public void clear() {
        ioManager = null;
        ioListeners = null;
        webServiceListener = null;
        sessionId = null;
        soapService = null;
        wsdlService = null;
        urnService = null;
        errorReporter = null;
    }

    /**
     * Read a file into a byte array.
     *
     * @param file The file
     */
    public byte[] readFile(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));

        // Get the size of the file
        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("File too large");
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        // Close the input stream and return bytes
        is.close();

        return bytes;
    }
}
