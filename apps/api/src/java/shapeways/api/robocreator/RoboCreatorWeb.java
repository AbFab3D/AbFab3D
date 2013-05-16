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
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Web front end for Robocreators.
 *
 * @author Alan Hudson
 */
public class RoboCreatorWeb extends HttpServlet implements SQSQueueListener {
    private static final boolean DEBUG = true;
    private static final String QUEUE_PREPEND = "RoboCreator_";

    // What AWS instanceType are we on
    private String instanceType;

    // AWS Credentials
    private String awsAccessKey;
    private String awsAccessSecret;
    private String awsRegion;

    // Internal service queue for threads
    private ThreadPoolExecutor threadPool;

    // Queue name for messages across machines.
    private String kernels;
    private String queueUrl;
    private Integer visibilityTimeout;

    protected AmazonSQS sqs;
    protected Map<String,String> queUrlMap;

    /**
     * Initialize the servlet. Sets up the base directory properties for finding
     * stuff later on.
     */
    public void init() throws ServletException {

        if (DEBUG) {
            System.out.println("Starting RoboCreatorWeb");
        }

        ServletConfig config = getServletConfig();
        ServletContext ctx = config.getServletContext();

        kernels = getInitParameter("Kernels");
        if (kernels == null) {
            System.out.println("ServiceQueue is null, add entry to web.xml");
        }

        System.out.println("Handling Kernels: " + kernels);
        instanceType = getInstanceMetadata("instance-type", "localhost");

        // 0 = number of processors.  > 0 specific number.  Default is 1
        String num_threads_st = getInitParameter("NumThreads");
        Gson gson = new Gson();
        Map<String, Number> threadsMap = new HashMap<String, Number>();
        try {
            threadsMap = gson.fromJson(num_threads_st, Map.class);
        } catch(Exception e) {
            System.out.println("Cannot parse threads in RoboCreatorWeb.  Should be map of instanceType to numThreads");
            System.out.println("numThreads: " + num_threads_st);
            e.printStackTrace();
        }

        int threads = threadsMap.get(instanceType).intValue();

        if (threads == 0) {
            int cores = Runtime.getRuntime().availableProcessors();
            threads = cores;
        }

        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);

        System.out.println("ThreadPool: " + threadPool);
        awsAccessKey = getInitParameter("AWSAccessKey");
        awsAccessSecret = getInitParameter("AWSAccessSecret");
        awsRegion = getInitParameter("AWSRegion");
        String st = getInitParameter("AWSSQSVisibilityTimeout");
        if (st != null) {
            visibilityTimeout = Integer.parseInt(st);
        }

        // TODO: Not certain we want to do these here as it delays deploy and might stop all deploys if AWS is down.
        // Switch to a threaded runnable or maybe just add the job to the threadPool we have.
        sqs = new AmazonSQSClient(new BasicAWSCredentials(awsAccessKey, awsAccessSecret));

        Region region = RegionUtils.getRegion(awsRegion);
        sqs.setRegion(region);


        queUrlMap = new HashMap<String,String>();

        System.out.println("Creating Queues");
        String[] queues = kernels.split(" ");
        for(int i=0; i < queues.length; i++) {
            threadPool.submit(new SQSCreateQueueTask(sqs,QUEUE_PREPEND + queues[i],visibilityTimeout,this));
        }
    }

    @Override
    public void queueCreated(String name, String url) {
        System.out.println("Queue created: " + name + " --> " + url);
        queUrlMap.put(name, url);
    }

    @Override
    public void queueFailed(String name, Exception e) {
        System.out.println("Queue failed: " + name);
    }

    @Override
    public void messagesReceived(List<Message> messages) {

    }

    @Override
    public void destroy() {
        threadPool.shutdown();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getPathInfo();
        System.out.println("Path: " + url);

        if (url.contains("/order")) {
            handlePreview(req,resp);
        } else if (url.contains("/preview")) {
            handleOrder(req,resp);
        }
    }

    protected void handleOrder(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        Gson gson = new Gson();

        BufferedReader reader = req.getReader();
        String msg = IOUtils.toString(reader);
        System.out.println("Msg: " + msg);

        Map<String,Object> params = gson.fromJson(msg, Map.class);
        String url = req.getPathInfo();
        System.out.println("Path: " + url);

        params.put(QUEUE_PREPEND + "action", "order");

        int idx = url.indexOf("/",1);
        String kernel_name = url.substring(1,idx);

        System.out.println("kernel: " + kernel_name);
        System.out.println("que: " + QUEUE_PREPEND + kernel_name);
        String qurl = queUrlMap.get(QUEUE_PREPEND + kernel_name);

        if (qurl == null) {
            System.out.println("Unknown que");
            issueError("Unknown queue: " + kernel_name,resp);
            return;
        }

        // TODO: Need to insure < 64K
        msg = gson.toJson(params);
        SQSEnqueueTask task = new SQSEnqueueTask(sqs,qurl,msg);
        threadPool.submit(task);

        Map<String,Object> result = new HashMap<String,Object>();

        result.put("result","success");

        String json = gson.toJson(result);

        resp.setContentLength(json.length());
        resp.setStatus(HttpServletResponse.SC_OK);

        ServletOutputStream sos = resp.getOutputStream();
        byte[] ba = json.getBytes();
        sos.write(ba, 0, ba.length);

        resp.flushBuffer();

    }

    protected void handlePreview(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        Gson gson = new Gson();

        BufferedReader reader = req.getReader();
        String msg = IOUtils.toString(reader);
        System.out.println("Msg: " + msg);

        Map<String,Object> params = gson.fromJson(msg, Map.class);
        String url = req.getPathInfo();
        System.out.println("Path: " + url);

        params.put(QUEUE_PREPEND + "action", "preview");

        int idx = url.indexOf("/",1);
        String kernel_name = url.substring(1,idx);

        System.out.println("kernel: " + kernel_name);
        System.out.println("que: " + QUEUE_PREPEND + kernel_name);
        String qurl = queUrlMap.get(QUEUE_PREPEND + kernel_name);

        if (qurl == null) {
            System.out.println("Unknown que");
            issueError("Unknown queue: " + kernel_name,resp);
            return;
        }

        // TODO: Need to insure < 64K
        msg = gson.toJson(params);
        SQSEnqueueTask task = new SQSEnqueueTask(sqs,qurl,msg);
        threadPool.submit(task);

        Map<String,Object> result = new HashMap<String,Object>();

        result.put("result","success");

        String json = gson.toJson(result);

        resp.setContentLength(json.length());
        resp.setStatus(HttpServletResponse.SC_OK);

        ServletOutputStream sos = resp.getOutputStream();
        byte[] ba = json.getBytes();
        sos.write(ba, 0, ba.length);

        resp.flushBuffer();
    }

    private void issueError(String reason,HttpServletResponse resp) throws IOException {
        HashMap<String,Object> result = new HashMap<String, Object>();
        Gson gson = new Gson();

        result.put("result","failure");
        result.put("reason",reason);

        String json = gson.toJson(result);

        resp.setContentLength(json.length());
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        ServletOutputStream sos = resp.getOutputStream();
        byte[] ba = json.getBytes();
        sos.write(ba, 0, ba.length);

        resp.flushBuffer();
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
            bout = new BufferedOutputStream(baos,1024);

            int buffSize = 8 * 1024;
            byte data[] = new byte[buffSize];
            int count;

            while((count = is.read(data,0,buffSize)) >= 0) {
                baos.write(data,0,count);
            }

            ret_val = baos.toString();
        } catch(Exception e) {
            // ignore
            //e.printStackTrace();
        } finally {
            try {
                bout.close();
                is.close();
            } catch(Exception e) {
                // ignore
            }
        }

        if (ret_val == null) {
            ret_val = defValue;
        }

        return ret_val;
    }
}
