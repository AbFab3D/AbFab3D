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

package abfab3d.io.soap.transport;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import java.net.*;
import java.util.zip.*;
import sun.misc.BASE64Encoder;

import abfab3d.io.soap.*;
import abfab3d.util.ApplicationParams;

/**
 * Class used to make SOAP calls over HTTP
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public class HTTPTransport implements Transport {


    private static final String CONNECTION_ERROR =
        "<html><center>Internet connection problems.  Any work done may not be saved.</center></html>";

    /** The taregt URL */
    private String soapURL;

    /** The soap action using in the HTTP params */
    private String soapAction;

    /** The monitor to be notified during calls **/
    private TransportProgressMonitor monitor;

    /** flag used to stop the request if necessary */
    private boolean shutdown;

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Creates a default instance of HTTPTransport. The real data should
     * be filled in with the initialise method.
     */
    public HTTPTransport() {
        shutdown = false;
    }

    //----------------------------------------------------------
    // Methods required by Transport
    //----------------------------------------------------------

    /**
     * Initialize the transport to the given URL and action.
     *
     * @param soapURL the URL to connect to.
     * @param soapAction the value for SOAPAction HTTP header, may be null
     */
    public void initialize(String soapURL, String soapAction) {
        this.soapURL = soapURL;
        this.soapAction = soapAction;
    }

    /* (non-Javadoc)
     * @see abfab3d.io.soap.transport.Transport#setProgressMonitor(abfab3d.io.soap.transport.TransportProgressMonitor)
     */
    public void setProgressMonitor(TransportProgressMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Method to open a connection to a server and send the request.
     *
     * @param request The SOAP message encoded into bytes.
     * @return A byte stream from the SOAP server representing the response;
     * null if no response is returned.
     *
     * @throws SOAPFault if any faults are returned by the web service.
     * @throws SOAPException if response is not XML or other SOAP parsing issues.
     * @throws IOException if any error occurs while connecting to the server.
     */
    public byte[] call(byte[] request)
        throws SOAPFault, SOAPException, IOException {

        // vars which need be accessible from try, catch, finally blocks
        HttpURLConnection httpConn = null;
        OutputStream httpOutputStream = null;
        InputStream httpInputStream = null;

        boolean use_gzip = false;

        try {

            // Set the proxy info if necessary
            ProxySelector proxyInfo = ProxySelector.getDefault();

            List<Proxy> proxyList = null;
            try {
                proxyList = proxyInfo.select(new URI(soapURL));
            } catch (Exception e) {

            }

            if (proxyList != null && proxyList.size() > 0) {

                InetSocketAddress address = (InetSocketAddress)proxyList.get(0).address();

                if (address != null) {

                    Properties systemSettings = System.getProperties();
                    systemSettings.put("http.proxySet", true);
                    systemSettings.put("http.proxyHost", address.getHostName());
                    systemSettings.put("http.proxyPort", address.getPort());
                    System.setProperties(systemSettings);

                }

            }

            // Create the connection where we're going to send the file.
            URL url = new URL(soapURL);

            if (use_gzip) {
                int in = request.length;
                request = gzipFile(request);
                int out = request.length;
System.out.println("Gzipped file: in: " + in + " out: " + out + " percent: " + ((float)out/in));
            }

            httpConn = (HttpURLConnection) url.openConnection();

            httpConn.setFixedLengthStreamingMode(request.length);
            httpConn.setUseCaches(false);

            // set the connection properties
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setRequestMethod("POST");
//            httpConn.setConnectTimeout(15000);
            httpConn.setConnectTimeout(90000);

            // define the standard headers to be sent
            httpConn.setRequestProperty("User-Agent", "Hosted Creators");
            httpConn.setRequestProperty("Content-Language", "en-US");
            httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            httpConn.setRequestProperty("Accept", "text/xml");
            
            // set the authorization properties
            String user = (String) ApplicationParams.get("AUTHORIZED_USER");
            String password = (String) ApplicationParams.get("AUTHORIZED_PASSWORD");

            if (user != null && password != null) {
                byte[] encodedPassword = ( user + ":" + password ).getBytes();
                BASE64Encoder encoder = new BASE64Encoder();
                httpConn.setRequestProperty( "Authorization",
                		"Basic " + encoder.encode( encodedPassword ) );
            }

            // TODO: Seems we can't accept gzip encoded data
            if (use_gzip) httpConn.setRequestProperty("Accept-Encoding", "gzip");

            httpConn.setRequestProperty("Content-Length",
                    Integer.toString(request.length) );
            httpConn.setRequestProperty("SOAPAction", soapAction);

            // spool the input stream into the http connection output
            httpOutputStream = httpConn.getOutputStream();

            OutputStream os = null;


            // copy the bytes onto the http stream, notify if set up
            copy(new ByteArrayInputStream(request), httpOutputStream,
                    null != this.monitor);


            httpInputStream = httpConn.getInputStream();

            String encoding = httpConn.getContentEncoding();

            if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                httpInputStream = new GZIPInputStream(httpInputStream);
            } else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
                httpInputStream = new InflaterInputStream(httpInputStream, new Inflater(true));
            }

            // sanity check before moving on
            if (!(httpConn.getContentType().startsWith("text/xml"))) {

                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest( new String(readBytes(httpInputStream)) );
                }

                throw new SOAPException(CONNECTION_ERROR);
            }

            byte[] response = readBytes(httpInputStream);

            if (!shutdown) {
                return response;
            } else {
                return null;
            }

        } catch (IOException ioe) {

            if (shutdown) {
                return null;
            }

            // if it is a HTTP 500 error, then it might be
            // a session timeout issue
            if (httpConn.getResponseCode() == 500) {

                httpInputStream = httpConn.getErrorStream();

                String encoding = httpConn.getContentEncoding();

                if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                    httpInputStream = new GZIPInputStream(httpInputStream);
                } else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
                    httpInputStream = new InflaterInputStream(httpInputStream, new Inflater(true));
                }

                // sanity check before moving on, just pass on other HTTP errors
                if (!(httpConn.getContentType().startsWith("text/xml"))) {
                    //throw ioe;
                }

                throw new SOAPFault("SOAP Error", readBytes(httpInputStream));

            } else {

                IOException io = new IOException(CONNECTION_ERROR);
                io.setStackTrace(ioe.getStackTrace());

                throw io;
            }

        } finally {
            if (httpInputStream != null) {
                httpInputStream.close();
                httpInputStream = null;
            }

            if (httpOutputStream != null) {
                httpOutputStream.close();
                httpOutputStream = null;
            }

            if (httpConn != null) {
                httpConn.disconnect();
                httpConn = null;
            }

        }

    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Set the SOAPAction
     *
     * @param soapAction the new soapAction.
     */
    public void setSOAPAction(String soapAction) {
        this.soapAction=soapAction;
    }

    /**
     * Stop whatever request is currently being processed
     */
    public void stopRequest() {
System.out.println("**** Stop requested");
new Exception().printStackTrace();

        shutdown = true;
    }

    public byte[] gzipFile(byte[] bytes) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream out = new GZIPOutputStream(baos);

            out.write(bytes, 0, bytes.length);

            // Complete the GZIP file
            out.finish();
            out.close();

            return baos.toByteArray();
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Copy everything from an InputStream into an OutputStream; terminate
     * early if shutdown is called.  Always flush the output stream.
     *
     * @param is the stream to read from
     * @param os the stream to write to
     * @param notify should the monitor be notified of progress
     * @return the number of bytes copied
     * @throws IOException reading and/or writing went bad
     */
    private int copy(InputStream is, OutputStream os, boolean notify)
            throws IOException {
        if (notify && null == this.monitor) {
            this.logger.warning("Notify requested but monitor isn't set;"
                    + " no notification will be sent");
            notify = false;
        }

        byte[] buff = new byte[DEFAULT_BUFFER_SIZE];
        int count = 0;
        int read = 0;

        if (notify) {
            int available = is.available();

            if (available < 1) {
                if (this.logger.isLoggable(Level.FINE)) {
                    this.logger.fine(is.toString() + " reports available bytes "
                            + available + " < 1; progress monitor disabled");
                }
                notify = false;
            } else {
                this.monitor.setBytesToTransfer(available);
            }
        }

        while (!this.shutdown && -1 != (read = is.read(buff))) {
            os.write(buff, 0, read);
            count += read;

            if (notify) {
                this.monitor.bytesTransferred(count);
            }
        }

        os.flush();

        if (notify) {
            this.monitor.transferComplete();
        }

        return count;
    }

    /**
     * Return all the contents of an InputStream as an array of bytes.
     *
     * @param is the InputStream to read from
     * @return an array of bytes read from the stream
     * @throws IOException problem reading the stream
     */
    private byte[] readBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
        copy(is, baos, false);
        return baos.toByteArray();
    }
}
