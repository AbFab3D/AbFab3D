/*****************************************************************************
 *                        Copyright Shapeways, Inc. (c) 2011
 *                               Java Source
 *
 * This source is licenses under the Apache License, version 2.0
 *
 * http://www.apache.org/licenses/LICENSE-2.0.html
 *
 *
 ****************************************************************************/

package shapeways.api.robocreator;

// Standard imports

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.google.gson.Gson;
import shapeways.api.JSONMunger;
import shapeways.api.ShapewaysAPI;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Base class for RoboCreator examples
 *
 * @author Alan Hudson
 */
public abstract class BaseRoboCreator extends HttpServlet implements ModelGenerator, SQSQueueListener {
    private static final boolean DEBUG = false;

    private static final int DEFAULT_POLL_FREQUENCY = 5000;
    private static final String QUEUE_PREPEND = "RoboCreator_";

    private String shapewaysHost;

    // Shapeways Credentials
    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessSecret;

    // Internal service queue for threads
    private ThreadPoolExecutor threadPool;

    // Queue name for messages across machines.
    private String serviceQueue;
    private String queueUrl;
    private Integer visibilityTimeout;
    private int pollFrequency;

    protected AmazonSQS sqs;
    private QueueReceiver listener;
    private int threads;

    /**
     * Initialize the servlet. Sets up the base directory properties for finding
     * stuff later on.
     */
    public void init() throws ServletException {
        // DirectorServlet overrides all but this method.  If your making changes here
        // you might need to change that class as well

        ServletConfig config = getServletConfig();
        ServletContext ctx = config.getServletContext();

        serviceQueue = getInitParameter("ServiceQueue");
        if (serviceQueue == null) {
            System.out.println("ServiceQueue is null, add entry to web.xml");
        }

        shapewaysHost = getInitParameter("ShapewaysHost");

        System.out.println("ShapewaysHost: " + shapewaysHost);

        if (shapewaysHost == null) {
            shapewaysHost = ShapewaysAPI.getShapewaysHost();
        }

        String instanceType = getInstanceMetadata("instance-type", "localhost");

        // 0 = number of processors.  > 0 specific number.  Default is 1
        String num_threads_st = getInitParameter("NumThreads");
        Gson gson = new Gson();
        Map<String, Number> threadsMap = new HashMap<String, Number>();
        try {
            threadsMap = gson.fromJson(num_threads_st, Map.class);
        } catch (Exception e) {
            System.out.println("Cannot parse threads: " + serviceQueue + ".  Should be map of instanceType to numThreads");
            System.out.println("numThreads: " + num_threads_st);
            e.printStackTrace();
        }

        threads = threadsMap.get(instanceType).intValue();

        if (threads == 0) {
            threads =  Runtime.getRuntime().availableProcessors();;
        }

        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);

        consumerKey = getInitParameter("ShapewaysConsumerKey");
        consumerSecret = getInitParameter("ShapewaysConsumerSecret");
        accessToken = getInitParameter("ShapewaysAccessToken");
        accessSecret = getInitParameter("ShapewaysAccessSecret");

        String proxyType = getInitParameter("ProxyType");
        String proxyHost = getInitParameter("ProxyHost");
        String proxyPort = getInitParameter("ProxyPort");

        if (proxyHost != null) {
            // TODO: think about selectors as this is per JVM
            System.out.println("Configuring proxy: " + proxyHost + ":" + proxyPort);

            Properties systemSettings =
                    System.getProperties();

            systemSettings.put("proxySet", "true");
            if (proxyType.equalsIgnoreCase("SOCKS")) {
                systemSettings.put("socksProxyHost", proxyHost);
                systemSettings.put("socksProxyPort", proxyPort);
            } else {
                systemSettings.put("http.proxyHost", proxyHost);
                systemSettings.put("http.proxyPort", proxyPort);
            }
        }

        String awsAccessKey = getInitParameter("AWSAccessKey");
        String awsAccessSecret = getInitParameter("AWSAccessSecret");
        String awsRegion = getInitParameter("AWSRegion");
        String st = getInitParameter("AWSSQSVisibilityTimeout");
        if (st != null) {
            visibilityTimeout = Integer.parseInt(st);
        }

        st = getInitParameter("AWSSQSMessagePollFrequency");

        if (st != null) {
            pollFrequency = Integer.parseInt(st);
        } else {
            pollFrequency = DEFAULT_POLL_FREQUENCY;
        }

        // TODO: Not certain we want to do these here as it delays deploy and might stop all deploys if AWS is down.
        // Switch to a threaded runnable or maybe just add the job to the threadPool we have.
        sqs = new AmazonSQSClient(new BasicAWSCredentials(awsAccessKey, awsAccessSecret));

        Region region = RegionUtils.getRegion(awsRegion);
        sqs.setRegion(region);

        threadPool.submit(new SQSCreateQueueTask(sqs,QUEUE_PREPEND + serviceQueue,visibilityTimeout,this));
    }

    @Override
    public void destroy() {
        listener.setTerminate(true);
        threadPool.shutdown();
    }

    @Override
    public void queueCreated(String name, String url) {
        queueUrl = url;
        listener = new QueueReceiver(threads, sqs, queueUrl, pollFrequency, threadPool, this);
        listener.start();
    }

    @Override
    public void queueFailed(String name, Exception e) {
    }

    @Override
    public void messagesReceived(List<Message> messages) {
        for (Message msg : messages) {
            String json = msg.getBody();
            if (DEBUG) {
                System.out.println("Received Msg: " + json);
            }

            Gson gson = new Gson();

            Map params = gson.fromJson(json, Map.class);

            Integer modelId = JSONMunger.toInteger(params.get("modelId"));

            ModelUploader uploader = new ModelUploader(shapewaysHost, consumerKey, consumerSecret, accessToken, accessSecret);

            ModelGeneratorRunner runner = new ModelGeneratorRunner(this, params, uploader, modelId, "cube.x3dv",
                    X3DEncodingType.X3DV, sqs, queueUrl, msg.getReceiptHandle());
            threadPool.submit(runner);
        }
    }

    /**
     * Get the public facing hostname for this machine.  Uses AWS metadata service.
     */
    protected String getInstanceMetadata(String name, String defValue) {
        ByteArrayOutputStream baos = null;
        BufferedOutputStream bout = null;
        String ret_val = null;
        InputStream is = null;

        try {
            URL url = new URL("http://169.254.169.254/latest/meta-data/" + name);

            URLConnection urlConn = url.openConnection();
            urlConn.setConnectTimeout(5000);
            urlConn.setReadTimeout(15000);
            urlConn.setAllowUserInteraction(false);
            urlConn.setDoOutput(true);
            is = new BufferedInputStream(urlConn.getInputStream());

            baos = new ByteArrayOutputStream();
            bout = new BufferedOutputStream(baos, 1024);

            int buffSize = 8 * 1024;
            byte data[] = new byte[buffSize];
            int count;

            while ((count = is.read(data, 0, buffSize)) >= 0) {
                baos.write(data, 0, count);
            }

            ret_val = baos.toString();
        } catch (Exception e) {
            // ignore
            //e.printStackTrace();
        } finally {
            try {
                bout.close();
                is.close();
            } catch (Exception e) {
                // ignore
            }
        }

        if (ret_val == null) {
            ret_val = defValue;
        }

        return ret_val;
    }

}
