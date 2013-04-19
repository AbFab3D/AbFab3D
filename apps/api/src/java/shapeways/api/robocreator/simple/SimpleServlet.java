package shapeways.api.robocreator.simple;

import com.google.gson.Gson;
import org.scribe.utils.MapUtils;
import shapeways.api.*;
import shapeways.api.robocreator.ModelGenerator;
import shapeways.api.robocreator.ModelGeneratorRunner;
import shapeways.api.robocreator.ModelUploader;
import shapeways.api.robocreator.X3DEncodingType;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple creator example.  Creates cubes in all sizes and no colors.
 * Simple example of RoboCreator, don't add anything fancy.
 * <p/>
 * Only supports X3DV encoding to make it simple.
 *
 * @author Alan Hudson
 */
public class SimpleServlet extends HttpServlet implements ModelGenerator {
    private String shapewaysHost;

    // Shapeways Credentials
    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessSecret;

    // Proxy Settings
    private String proxyType;
    private String proxyHost;
    private String proxyPort;

    // Internal service queue for threads
    private ExecutorService threadPool;

    @Override
    public void init() throws ServletException {
        ServletConfig config = getServletConfig();
        ServletContext ctx = config.getServletContext();

        threadPool = Executors.newFixedThreadPool(2);

        System.out.println("Params:");
        Enumeration e = getInitParameterNames();
        while(e.hasMoreElements()) {
            System.out.println(e.nextElement());
        }

        shapewaysHost = getInitParameter("ShapewaysHost");
        System.out.println("Shapeways Host: " + shapewaysHost);
        if (shapewaysHost == null) {
            shapewaysHost = ShapewaysAPI.getShapewaysHost();
        }

        consumerKey = getInitParameter("ShapewaysConsumerKey");
        consumerSecret = getInitParameter("ShapewaysConsumerSecret");
        accessToken = getInitParameter("ShapewaysAccessToken");
        accessSecret = getInitParameter("ShapewaysAccessSecret");

        proxyType = getInitParameter("ProxyType");
        proxyHost = getInitParameter("ProxyHost");
        proxyPort = getInitParameter("ProxyPort");

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

    }

    @Override
    public void destroy() {
        threadPool.shutdown();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();

         // Likely code needed to take JSON request params
        BufferedReader reader = req.getReader();

        Map params = gson.fromJson(reader, Map.class);
        System.out.println("Got request: " + params);


        Map<String,Object> result = new HashMap<String,Object>();

        // verify params
        Object p = params.get("modelId");
        if (p == null) {
            String reason = "Missing modelId parameter";
            System.out.println("Parameter Error: " + reason);
            result.put("result","failure");
            result.put("reason",reason);

            String json = gson.toJson(result);

            resp.setContentLength(json.length());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            ServletOutputStream sos = resp.getOutputStream();
            byte[] ba = json.getBytes();
            sos.write(ba, 0, ba.length);

            resp.flushBuffer();

            return;
        }

        Integer modelId = JSONMunger.toInteger(p);

        ModelUploader uploader = new ModelUploader(shapewaysHost, consumerKey, consumerSecret, accessToken, accessSecret);

        ModelGeneratorRunner runner = new ModelGeneratorRunner(this,req.getParameterMap(),uploader,modelId,"cube.x3dv", X3DEncodingType.X3DV);
        threadPool.submit(runner);

        result.put("result","success");

        String json = gson.toJson(result);

        resp.setContentLength(json.length());
        resp.setStatus(HttpServletResponse.SC_OK);

        ServletOutputStream sos = resp.getOutputStream();
        byte[] ba = json.getBytes();
        sos.write(ba, 0, ba.length);

        resp.flushBuffer();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    public byte[] generateModel(Map params, X3DEncodingType encoding) {
        System.out.println("Generating Cube");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);

        pw.println("#X3D V3.0 utf8");
        pw.println("PROFILE Immersive");
        pw.println("Shape { geometry Box { size 0.05 0.05 0.05 } }");

        pw.close();

        return baos.toByteArray();
    }
}
