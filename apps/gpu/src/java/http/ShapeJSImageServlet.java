package http;

import abfab3d.grid.Bounds;

import abfab3d.param.Parameter;
import abfab3d.param.ParameterType;
import com.google.gson.Gson;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import render.*;
import shapejs.EvalResult;
import shapejs.JSWrapper;
import shapejs.ShapeJSEvaluator;
import utils.Utils;

import io.SceneIO;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static abfab3d.util.Output.printf;

/**
 * Creates images from ShapeJS
 *
 * @author Alan Hudson
 */
public class ShapeJSImageServlet extends HttpServlet {
    public static final String VERSION = VolumeRenderer.VERSION_OPCODE_V3_DIST;
    
    private static int MAX_UPLOAD_SIZE = 64000000;
    private static String RESULTS_DIR_PUBLIC = "http://localhost:8080/creator-kernels/results";
    private static String RESULTS_DIR = "/var/www/html/creator-kernels/results";
    private static String TMP_DIR = "/tmp";
    private static int TEMP_DIR_ATTEMPTS = 1000;

    /** Config params in map format */
    protected Map<String, String> config;

    /** The context of this servlet */
    protected ServletContext ctx;

    private ImageRenderer render;
    private Matrix4f viewMatrix = new Matrix4f(); // TODO: need to thread local
    private ConcurrentHashMap<String, SceneCacheEntry> sceneCache;

    /**
     * Initialize the servlet. Sets up the base directory properties for finding
     * stuff later on.
     */
    public void init() throws ServletException {
        ServletConfig sconfig = getServletConfig();
        this.ctx = sconfig.getServletContext();

        config = convertConfig(sconfig);

        sceneCache = new ConcurrentHashMap<String, SceneCacheEntry>();

        initCL(false, 512, 512);
        printf("Init called\n");
    }

    public void initCL(boolean debug, int width, int height) {
        render = new ImageRenderer();
        render.setVersion(VERSION);
        render.initCL(1, width, height);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long t0 = System.nanoTime();
        String command = req.getPathInfo();

        HttpSession session = req.getSession(true);
        String accept = req.getHeader("Accept");

        if (command.contains("/makeImageCached")) {
            handleImageCachedRequest(req, resp, session, accept);
        } else if (command.contains("/makeImage")) {
            handleImageRequest(req, resp, session, accept);
        } else if (command.contains("/pickCached")) {
            handlePickCachedRequest(req, resp, session, accept);
        } else if (command.contains("/pick")) {
            handlePickRequest(req, resp, session, accept);
        } else if (command.contains("/updateScene")) {
            handleSceneRequest(req, resp, session, accept);
        } else if (command.contains("/saveSceneCached")) {
            handleSaveSceneCachedRequest(req, resp, session, accept);
        } else if (command.contains("/loadScene")) {
            handleLoadSceneRequest(req, resp, session, accept);
        } else {
            super.doGet(req, resp);
        }
        printf("request time: %d ms\n",((int)((System.nanoTime() - t0) / 1e6)));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String command = req.getPathInfo();

        HttpSession session = req.getSession(true);   // TODO: do we need this
        String accept = req.getHeader("Accept");

        if (command.contains("/makeImageCached")) {
            handleImageCachedRequest(req, resp, session, accept);
        } else if (command.contains("/makeImage")) {
            handleImageRequest(req, resp, session, accept);
        } else if (command.contains("/pickCached")) {
            handlePickCachedRequest(req, resp, session, accept);
        } else if (command.contains("/pick")) {
            handlePickRequest(req, resp, session, accept);
        } else if (command.contains("/updateScene")) {
            handleSceneRequest(req, resp, session, accept);
        } else if (command.contains("/saveSceneCached")) {
        	handleSaveSceneCachedRequest(req, resp, session, accept);
        } else if (command.contains("/loadScene")) {
            handleLoadSceneRequest(req, resp, session, accept);
        } else {
            super.doGet(req, resp);
        }
    }

    // TODO: stop doing this
    synchronized private void handleImageRequest(HttpServletRequest req,
                                                 HttpServletResponse resp,
                                                 HttpSession session,
                                                 String accept)
            throws IOException {

        int width = 512, height = 512;
        String jobID = null;
        String script = null;
        float[] tviewMatrix = null;
        int frames = 0;
        int framesX = 6;
        String imgType = "JPG";
        float[] view;
        float rotX = 0;
        float rotY = 0;
        float zoom = -4;
        float quality = 0.5f;  // samples,maxSteps,shadowSteps,softShadows

        boolean isMultipart = ServletFileUpload.isMultipartContent(req);

        Map<String, String[]> params = null;
        
        if (isMultipart) {
//    		System.out.println("==> multipart form post");
            params = new HashMap<String, String[]>();
            mapParams(req, params, MAX_UPLOAD_SIZE, TMP_DIR);
        } else {
//    		System.out.println("==> not multipart form post");
            params = req.getParameterMap();
        }

        String[] widthSt = params.get("width");
        if (widthSt != null && widthSt.length > 0) {
            width = Integer.parseInt(widthSt[0]);
        }

        String[] heightSt = params.get("height");
        if (heightSt != null && heightSt.length > 0) {
            height = Integer.parseInt(heightSt[0]);
        }

        String[] framesSt = params.get("frames");
        if (framesSt != null && framesSt.length > 0) {
            frames = Integer.parseInt(framesSt[0]);
        }

        String[] framesXSt = params.get("framesX");
        if (framesXSt != null && framesXSt.length > 0) {
            framesX = Integer.parseInt(framesXSt[0]);
        }

        String[] jobIDSt = params.get("jobID");
        if (jobIDSt != null && jobIDSt.length > 0) {
            jobID = jobIDSt[0];
        }

        String[] imgTypeSt = params.get("imgType");
        if (imgTypeSt != null && imgTypeSt.length > 0) {
            imgType = imgTypeSt[0];
        }

        String[] scriptSt = params.get("script");
        if (scriptSt != null && scriptSt.length > 0) {
            script = scriptSt[0];
        }


        String[] viewSt = params.get("view");
        String[] axisAngleSt = params.get("axisAngle");
        if (viewSt != null && viewSt.length > 0) {
            String[] vals = viewSt[0].split(",");
            view = new float[vals.length];
            for(int i=0; i < vals.length; i++) {
                view[i] = Float.parseFloat(vals[i]);
            }

            if (view.length != 16) {
                throw new IllegalArgumentException("ViewMatrix must be 16 values");
            }


            viewMatrix.set(view);
        } else if (axisAngleSt != null && axisAngleSt.length > 0) {
        	String[] vals = axisAngleSt[0].split(",");
            float[] axisAngle = new float[vals.length];
            
            for(int i=0; i < vals.length; i++) {
            	axisAngle[i] = Float.parseFloat(vals[i]);
            }
            
            String[] zoomSt = params.get("zoom");
            if (zoomSt != null && zoomSt.length > 0) {
                zoom = Float.parseFloat(zoomSt[0]);
            }

            if (axisAngle.length != 4) {
                throw new IllegalArgumentException("Axis angle must be 4 values");
            }
        	getViewFromAxisAngle(axisAngle, zoom, viewMatrix);
        } else {
            String[] rotXSt = params.get("rotX");
            if (rotXSt != null && rotXSt.length > 0) {
                rotX = Float.parseFloat(rotXSt[0]);
            }

            String[] rotYSt = params.get("rotY");
            if (rotYSt != null && rotYSt.length > 0) {
                rotY = Float.parseFloat(rotYSt[0]);
            }

            String[] zoomSt = params.get("zoom");
            if (zoomSt != null && zoomSt.length > 0) {
                zoom = Float.parseFloat(zoomSt[0]);
            }

            getView(rotX,rotY,zoom,viewMatrix);
        }

        if (script == null) {
            throw new IllegalArgumentException("Script is required");
        }

        String[] qualitySt = params.get("quality");
        if (qualitySt != null && qualitySt.length > 0) {
            quality = Float.parseFloat(qualitySt[0]);
        }

        Map<String,Object> sparams = new HashMap<String,Object>();
        for(Map.Entry<String,String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("shapeJS_")) {
                key = key.substring(8);
                //printf("Adding param: %s -> %s\n",key,entry.getValue()[0]);
                sparams.put(key, entry.getValue()[0]);
            }

        }
        long t0 = System.nanoTime();

        OutputStream os = resp.getOutputStream();

        int size = 0;
        int itype = 0;
        if (imgType.equalsIgnoreCase("PNG")) {
            itype = ImageRenderer.IMAGE_PNG;
            resp.setContentType("image/png");
        } else if (imgType.equalsIgnoreCase("JPG")) {
            itype = ImageRenderer.IMAGE_JPEG;
            resp.setContentType("image/jpeg");
        }
        if (frames == 0 || frames == 1) {
            size = render.render(jobID, script, sparams,viewMatrix, true, itype,quality, resp.getOutputStream());
        } else {
            throw new IllegalArgumentException("Need to reimplement");
            //size = render.renderImages(jobID, script, sparams,viewMatrix, frames, framesX, true, itype,resp.getOutputStream());
        }

        printf("Image size: %d\n",size);
        resp.setContentLength(size);

        os.close();
    }

    synchronized private void handleImageCachedRequest(HttpServletRequest req,
                                                 HttpServletResponse resp,
                                                 HttpSession session,
                                                 String accept)
            throws IOException {

        int width = 512, height = 512;
        String jobID = null;
        float[] tviewMatrix = null;
        int frames = 0;
        int framesX = 6;
        String imgType = "JPG";
        float[] view;
        float rotX = 0;
        float rotY = 0;
        float zoom = -4;
        float quality = 0.5f;  // samples,maxSteps,shadowSteps,softShadows

        boolean isMultipart = ServletFileUpload.isMultipartContent(req);

        Map<String, String[]> params = req.getParameterMap();

        String[] widthSt = params.get("width");
        if (widthSt != null && widthSt.length > 0) {
            width = Integer.parseInt(widthSt[0]);
        }

        String[] heightSt = params.get("height");
        if (heightSt != null && heightSt.length > 0) {
            height = Integer.parseInt(heightSt[0]);
        }

        String[] jobIDSt = params.get("jobID");
        if (jobIDSt != null && jobIDSt.length > 0) {
            jobID = jobIDSt[0];
        }
        
        String[] imgTypeSt = params.get("imgType");
        if (imgTypeSt != null && imgTypeSt.length > 0) {
            imgType = imgTypeSt[0];
        }

        String[] viewSt = params.get("view");
        String[] axisAngleSt = params.get("axisAngle");
        if (viewSt != null && viewSt.length > 0) {
            String[] vals = viewSt[0].split(",");
            view = new float[vals.length];
            for(int i=0; i < vals.length; i++) {
                view[i] = Float.parseFloat(vals[i]);
            }

            if (view.length != 16) {
                throw new IllegalArgumentException("ViewMatrix must be 16 values");
            }


            viewMatrix.set(view);
        } else if (axisAngleSt != null && axisAngleSt.length > 0) {
        	String[] vals = axisAngleSt[0].split(",");
            float[] axisAngle = new float[vals.length];
            
            for(int i=0; i < vals.length; i++) {
            	axisAngle[i] = Float.parseFloat(vals[i]);
            }
            
            String[] zoomSt = params.get("zoom");
            if (zoomSt != null && zoomSt.length > 0) {
                zoom = Float.parseFloat(zoomSt[0]);
            }

            if (axisAngle.length != 4) {
                throw new IllegalArgumentException("Axis angle must be 4 values");
            }
        	getViewFromAxisAngle(axisAngle, zoom, viewMatrix);
        } else {
            String[] rotXSt = params.get("rotX");
            if (rotXSt != null && rotXSt.length > 0) {
                rotX = Float.parseFloat(rotXSt[0]);
            }

            String[] rotYSt = params.get("rotY");
            if (rotYSt != null && rotYSt.length > 0) {
                rotY = Float.parseFloat(rotYSt[0]);
            }

            String[] zoomSt = params.get("zoom");
            if (zoomSt != null && zoomSt.length > 0) {
                zoom = Float.parseFloat(zoomSt[0]);
            }

            getView(rotX,rotY,zoom,viewMatrix);
        }

        String[] qualitySt = params.get("quality");
        if (qualitySt != null && qualitySt.length > 0) {
            quality = Float.parseFloat(qualitySt[0]);
        }

        long t0 = System.nanoTime();

        OutputStream os = resp.getOutputStream();

        int size = 0;
        int itype = 0;
        if (imgType.equalsIgnoreCase("PNG")) {
            itype = ImageRenderer.IMAGE_PNG;
            resp.setContentType("image/png");
        } else if (imgType.equalsIgnoreCase("JPG")) {
            itype = ImageRenderer.IMAGE_JPEG;
            resp.setContentType("image/jpeg");
        }

        try {
            size = render.renderCached(jobID, viewMatrix, itype, quality, resp.getOutputStream());
        } catch(ImageRenderer.NotCachedException nce) {
            resp.sendError(410,"Job not cached");
            return;
        }

        printf("Image size: %d\n",size);
        resp.setContentLength(size);

        os.close();
    }

    // TODO: stop doing this
    synchronized private void handlePickCachedRequest(HttpServletRequest req,
                                                 HttpServletResponse resp,
                                                 HttpSession session,
                                                 String accept)
            throws IOException {

        String jobID = null;
        float[] view;
        float rotX = 0;
        float rotY = 0;
        float zoom = -4;
        int x = 0;
        int y = 0;

        boolean isMultipart = ServletFileUpload.isMultipartContent(req);

        Map<String, String[]> params = req.getParameterMap();

        String[] jobIDSt = params.get("jobID");
        if (jobIDSt != null && jobIDSt.length > 0) {
            jobID = jobIDSt[0];
        }

        String[] viewSt = params.get("view");
        String[] axisAngleSt = params.get("axisAngle");
        if (viewSt != null && viewSt.length > 0) {
            String[] vals = viewSt[0].split(",");
            view = new float[vals.length];
            for(int i=0; i < vals.length; i++) {
                view[i] = Float.parseFloat(vals[i]);
            }

            if (view.length != 16) {
                throw new IllegalArgumentException("ViewMatrix must be 16 values");
            }


            viewMatrix.set(view);
        } else if (axisAngleSt != null && axisAngleSt.length > 0) {
        	String[] vals = axisAngleSt[0].split(",");
            float[] axisAngle = new float[vals.length];
            
            for(int i=0; i < vals.length; i++) {
            	axisAngle[i] = Float.parseFloat(vals[i]);
            }
            
            String[] zoomSt = params.get("zoom");
            if (zoomSt != null && zoomSt.length > 0) {
                zoom = Float.parseFloat(zoomSt[0]);
            }

            if (axisAngle.length != 4) {
                throw new IllegalArgumentException("Axis angle must be 4 values");
            }
        	getViewFromAxisAngle(axisAngle, zoom, viewMatrix);
        } else {
            String[] rotXSt = params.get("rotX");
            if (rotXSt != null && rotXSt.length > 0) {
                rotX = Float.parseFloat(rotXSt[0]);
            }

            String[] rotYSt = params.get("rotY");
            if (rotYSt != null && rotYSt.length > 0) {
                rotY = Float.parseFloat(rotYSt[0]);
            }

            String[] zoomSt = params.get("zoom");
            if (zoomSt != null && zoomSt.length > 0) {
                zoom = Float.parseFloat(zoomSt[0]);
            }

            getView(rotX,rotY,zoom,viewMatrix);
        }

        String[] xSt = params.get("x");
        if (xSt != null && xSt.length > 0) {
            x = Integer.parseInt(xSt[0]);
        }
        String[] ySt = params.get("y");
        if (ySt != null && ySt.length > 0) {
            y = Integer.parseInt(ySt[0]);
        }

        long t0 = System.nanoTime();


        // TODO: garbage
        Vector3f pos = new Vector3f();
        Vector3f normal = new Vector3f();
        Gson gson = new Gson();
        HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            render.pickCached(jobID, viewMatrix, x, y, 512, 512, pos, normal);
        } catch(ImageRenderer.NotCachedException nce) {
            resp.sendError(410,"Job not cached");
            return;
        }
        OutputStream os = resp.getOutputStream();
        resp.setContentType("application/json");

        result.put("point",new float[] {pos.x,pos.y,pos.z});
        result.put("normal",new float[] {normal.x,normal.y,normal.z});

        String st = gson.toJson(result);
        os.write(st.getBytes());

        os.close();
    }

    // TODO: stop doing this
    synchronized private void handlePickRequest(HttpServletRequest req,
                                                      HttpServletResponse resp,
                                                      HttpSession session,
                                                      String accept)
            throws IOException {

        String jobID = null;
        String script = null;
        float[] tviewMatrix = null;
        float[] view;
        float rotX = 0;
        float rotY = 0;
        float zoom = -4;
        int x = 0;
        int y = 0;

        Map<String, String[]> params = req.getParameterMap();

        String[] jobIDSt = params.get("jobID");
        if (jobIDSt != null && jobIDSt.length > 0) {
            jobID = jobIDSt[0];
        }

        String[] scriptSt = params.get("script");
        if (scriptSt != null && scriptSt.length > 0) {
            script = scriptSt[0];
        }

        String[] viewSt = params.get("view");
        String[] axisAngleSt = params.get("axisAngle");
        if (viewSt != null && viewSt.length > 0) {
            String[] vals = viewSt[0].split(",");
            view = new float[vals.length];
            for(int i=0; i < vals.length; i++) {
                view[i] = Float.parseFloat(vals[i]);
            }

            if (view.length != 16) {
                throw new IllegalArgumentException("ViewMatrix must be 16 values");
            }


            viewMatrix.set(view);
        } else if (axisAngleSt != null && axisAngleSt.length > 0) {
        	String[] vals = axisAngleSt[0].split(",");
            float[] axisAngle = new float[vals.length];
            
            for(int i=0; i < vals.length; i++) {
            	axisAngle[i] = Float.parseFloat(vals[i]);
            }
            
            String[] zoomSt = params.get("zoom");
            if (zoomSt != null && zoomSt.length > 0) {
                zoom = Float.parseFloat(zoomSt[0]);
            }

            if (axisAngle.length != 4) {
                throw new IllegalArgumentException("Axis angle must be 4 values");
            }
        	getViewFromAxisAngle(axisAngle, zoom, viewMatrix);
        } else {
            String[] rotXSt = params.get("rotX");
            if (rotXSt != null && rotXSt.length > 0) {
                rotX = Float.parseFloat(rotXSt[0]);
            }

            String[] rotYSt = params.get("rotY");
            if (rotYSt != null && rotYSt.length > 0) {
                rotY = Float.parseFloat(rotYSt[0]);
            }

            String[] zoomSt = params.get("zoom");
            if (zoomSt != null && zoomSt.length > 0) {
                zoom = Float.parseFloat(zoomSt[0]);
            }

            getView(rotX,rotY,zoom,viewMatrix);
        }

        String[] xSt = params.get("x");
        if (xSt != null && xSt.length > 0) {
            x = Integer.parseInt(xSt[0]);
        }
        String[] ySt = params.get("y");
        if (ySt != null && ySt.length > 0) {
            y = Integer.parseInt(ySt[0]);
        }

        if (script == null) {
            throw new IllegalArgumentException("Script is required");
        }

        Map<String,Object> sparams = new HashMap<String,Object>();
        for(Map.Entry<String,String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("shapeJS_")) {
                key = key.substring(8);
                //printf("Adding param: %s -> %s\n",key,entry.getValue()[0]);
                sparams.put(key, entry.getValue()[0]);
            }

        }
        long t0 = System.nanoTime();


        // TODO: garbage
        Vector3f pos = new Vector3f();
        Vector3f normal = new Vector3f();
        Gson gson = new Gson();
        HashMap<String, Object> result = new HashMap<String, Object>();
        render.pick(jobID,script,sparams,true,viewMatrix, x, y, 512, 512, pos, normal);

        OutputStream os = resp.getOutputStream();
        resp.setContentType("application/json");

        result.put("pos",new float[] {pos.x,pos.y,pos.z});
        result.put("normal",new float[] {normal.x,normal.y,normal.z});

        String st = gson.toJson(result);
        os.write(st.getBytes());

        os.close();
    }

    // TODO: stop doing this
    synchronized private void handleSceneRequest(HttpServletRequest req,
                                                 HttpServletResponse resp,
                                                 HttpSession session,
                                                 String accept)
            throws IOException {

        String jobID = null;
        String script = null;

        boolean isMultipart = ServletFileUpload.isMultipartContent(req);

        Map<String, String[]> params = null;

        if (isMultipart) {
//    		System.out.println("==> multipart form post");
            params = new HashMap<String, String[]>();
            mapParams(req, params, MAX_UPLOAD_SIZE, TMP_DIR);
        } else {
//    		System.out.println("==> not multipart form post");
            params = req.getParameterMap();
        }

        String[] jobIDSt = params.get("jobID");
        if (jobIDSt != null && jobIDSt.length > 0) {
            jobID = jobIDSt[0];
        }

        String[] scriptSt = params.get("script");
        if (scriptSt != null && scriptSt.length > 0) {
            script = scriptSt[0];
        }

        printf("UpdateScene:\n");
        Map<String,Object> sparams = new HashMap<String,Object>();
        for(Map.Entry<String,String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            String val = null;
            if (key.startsWith("shapeJS_")) {
                key = key.substring(8);
                val = entry.getValue()[0];
                if (val.equals("undefined")) continue;
                printf("Adding param: %s -> %s\n",key,val);
                sparams.put(key, val);
            }
        }

        Gson gson = new Gson();
        HashMap<String, Object> result = new HashMap<String, Object>();
        EvalResult eval = updateScene(jobID,script,sparams);

        if (result == null) {
            result.put("success",false);
        } else {
            result.put("success",eval.isSuccess());
            result.put("printLog",eval.getPrintLog());
            result.put("errorLog",eval.getErrorLog());
            result.put("evalTime",eval.getEvalTime());
            result.put("opCount",eval.getOpCount());
            result.put("opSize",eval.getOpSize());
            result.put("dataSize",eval.getDataSize());
        }

        OutputStream os = resp.getOutputStream();
        resp.setContentType("application/json");

        String st = gson.toJson(result);
        os.write(st.getBytes());

        os.close();

    }
    /*
        synchronized private void handleSaveSceneRequest(HttpServletRequest req,
                HttpServletResponse resp,
                HttpSession session,
                String accept)
                        throws IOException {

            String jobID = null;
            String script = null;

            boolean isMultipart = ServletFileUpload.isMultipartContent(req);

            Map<String, String[]> params = null;

            if (isMultipart) {
                //System.out.println("==> multipart form post");
                params = new HashMap<String, String[]>();
                mapParams(req, params, MAX_UPLOAD_SIZE, TMP_DIR);
            } else {
                //System.out.println("==> not multipart form post");
                params = req.getParameterMap();
            }

            String[] jobIDSt = params.get("jobID");
            if (jobIDSt != null && jobIDSt.length > 0) {
                jobID = jobIDSt[0];
            }

            String[] scriptSt = params.get("script");
            if (scriptSt != null && scriptSt.length > 0) {
                script = scriptSt[0];
            }

            if (script == null) {
                throw new IllegalArgumentException("Script is required");
            }

            Map<String,Object> sparams = new HashMap<String,Object>();
            for(Map.Entry<String,String[]> entry : params.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("shapeJS_")) {
                    key = key.substring(8);
                //printf("Adding param: %s -> %s\n",key,entry.getValue()[0]);
                    sparams.put(key, entry.getValue()[0]);
                }
            }

            SceneCacheEntry sce = new SceneCacheEntry(jobID,script,sparams);
            saveScene(sce, resp);
            resp.flushBuffer();
        }
    */
    synchronized private void handleSaveSceneCachedRequest(HttpServletRequest req,
                                                           HttpServletResponse resp,
                                                           HttpSession session,
                                                           String accept)
            throws IOException {

        String jobID = null;

        Map<String, String[]> params = req.getParameterMap();

        String[] jobIDSt = params.get("jobID");
        if (jobIDSt != null && jobIDSt.length > 0) {
            jobID = jobIDSt[0];
        }

        if (jobID != null) {
            SceneCacheEntry sce = sceneCache.get(jobID);

            if (sce == null) {
                resp.sendError(410,"Job not cached");
                return;
            }

            resp.setContentType("application/zip");
            resp.setHeader("Content-Disposition","attachment;filename=\"shapeJS.zip\"");
            
            OutputStream os = resp.getOutputStream();
            
            SceneIO.saveScene(sce.getScript(), sce.getParams(), os);
            os.close();
        } else {
            resp.sendError(410,"Job not cached");
            return;
        }
    }

    synchronized private void handleLoadSceneRequest(HttpServletRequest req,
                                                     HttpServletResponse resp,
                                                     HttpSession session,
                                                     String accept)
            throws IOException {

        Map<String, Object> result = new HashMap<String, Object>();

        boolean isMultipart = ServletFileUpload.isMultipartContent(req);
        Map<String, String[]> params = null;

        if (isMultipart) {
            //System.out.println("==> multipart form post");
            params = new HashMap<String, String[]>();
            mapParams(req, params, MAX_UPLOAD_SIZE, TMP_DIR);

            result = SceneIO.loadScene(params);
        } else {
            result.put("success",false);
            result.put("errorLog","Missing scene file");
        }

        Gson gson = new Gson();
        OutputStream os = resp.getOutputStream();
        resp.setContentType("application/json");

        String st = gson.toJson(result);
        os.write(st.getBytes());

        os.close();
    }

    private void saveScene(SceneCacheEntry sce, HttpServletResponse resp) throws IOException {
        String script = sce.getScript();
        Map<String, Object> sceneParams = sce.getParams();

        Bounds bounds = new Bounds();
        ShapeJSEvaluator evaluator = new ShapeJSEvaluator();
        EvalResult result = evaluator.evalScript(script, null,bounds, sceneParams);
        Map<String, Parameter> evalParams = result.getUIParams();

        System.out.println("*** Script:\n" + script);
        System.out.println("*** Params:");
        String workingDirName = Utils.createTempDir(TMP_DIR);
        String workingDirPath = TMP_DIR + "/" + workingDirName;
        Map<String, Object> params = new HashMap<String, Object>();

        // Write the script to file
        File scriptFile = new File(workingDirPath + "/script.js");
        FileUtils.writeStringToFile(scriptFile, script, "UTF-8");

        // Loop through params and create key/pair entries
        for (Map.Entry<String, Object> entry : sceneParams.entrySet()) {
            String name = entry.getKey();
            Object val = entry.getValue();
            ParameterType type = null;//evalParams.get(name).getType();
            
            Object pval = null;
            if (val instanceof JSWrapper) {
            	pval = ((JSWrapper) val).getParameter().getValue();
            	type = ((JSWrapper) val).getParameter().getType();
            } else {
            	pval = val;
            	type = ParameterType.STRING;
            }

            switch(type) {
                case URI:
                    File f = new File((String) pval);
                    String fileName = f.getName();
                    params.put(name, fileName);

                    // Copy the file to working directory
                    FileUtils.copyFile(f, new File(workingDirPath + "/" + fileName), true);
                    break;
                case LOCATION:
                	Vector3d[] v = (Vector3d[]) pval;
                	double[] point = {v[0].x, v[0].y, v[0].z};
                	double[] normal = {v[1].x, v[1].y, v[1].z};
                	Map<String, double[]> loc = new HashMap<String, double[]>();
                	loc.put("point", point);
                	loc.put("normal", normal);
                    params.put(name, loc);
                    break;
                case DOUBLE:
                    params.put(name, (Double) pval);
                    break;
                case INTEGER:
                    params.put(name, (Integer) pval);
                    break;
                case STRING:
                	params.put(name, (String) pval);
                    break;
                default:
                	params.put(name, pval);
            }
        }

        Gson gson = new Gson();
        String paramsJson = gson.toJson(params);
        File paramFile = new File(workingDirPath + "/" + "params.json");
        FileUtils.writeStringToFile(paramFile, paramsJson, "UTF-8");

//		String resultDirPath = RESULTS_DIR + "/" + workingDirName;
//		File resultDir = new File(resultDirPath);

//		String zipFile = workingDirPath + "/shapejs.zip";
//    	FileOutputStream fos = new FileOutputStream(zipFile);

        resp.setContentType("application/zip");
        resp.setHeader("Content-Disposition","attachment;filename=\"shapeJS.zip\"");
        OutputStream os = resp.getOutputStream();
        ZipOutputStream zos = new ZipOutputStream(os);

        try {
            byte[] buffer = new byte[1024];

            File[] files = (new File(workingDirPath)).listFiles();
            System.out.println("*** Num files to zip: " + files.length);
            for (int i=0; i<files.length; i++) {
                if (files[i].getName().endsWith(".zip")) continue;

                System.out.println("*** Adding file: " + files[i].getName());
                ZipEntry ze = new ZipEntry(files[i].getName());
                zos.putNextEntry(ze);

                FileInputStream fis = new FileInputStream(files[i]);

                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            zos.closeEntry();
            zos.close();
//	    	fos.close();
            os.close();
        }

/*
    	System.out.println("*** Move " + workingDirPath + " to " + RESULTS_DIR + "/" + workingDirName);
    	FileUtils.moveDirectory(new File(workingDirPath), new File(RESULTS_DIR + "/" + workingDirName));

        Gson gson = new Gson();
        HashMap<String, Object> response = new HashMap<String, Object>();

        OutputStream os = resp.getOutputStream();
        resp.setContentType("application/json");
        response.put("uri", "http://localhost:8080/creator-kernels/results/" + workingDirName + "/shapejs.zip");

        String st = gson.toJson(response);
        os.write(st.getBytes());
        os.close();
*/
    }
    
    private void getView(float rotX, float rotY, float zoom, Matrix4f mat) {
        float[] DEFAULT_TRANS = new float[]{0, 0, zoom};
        float z = DEFAULT_TRANS[2];

        Vector3f trans = new Vector3f();
        Matrix4f tmat = new Matrix4f();
        Matrix4f rxmat = new Matrix4f();
        Matrix4f rymat = new Matrix4f();

        trans.z = z;
        tmat.set(trans, 1.0f);

        rxmat.rotX(-rotX);
        rymat.rotY(rotY);

        mat.setIdentity();
        mat.mul(tmat, rxmat);
        mat.mul(rymat);
    }
    
    private void getViewFromAxisAngle(float[] axisAngle, float zoom, Matrix4f mat) {
        float[] DEFAULT_TRANS = new float[]{0, 0, zoom};
        float z = DEFAULT_TRANS[2];

        AxisAngle4f aa = new AxisAngle4f(axisAngle);
        Vector3f trans = new Vector3f();
        Matrix4f tmat = new Matrix4f();

        trans.z = z;
        tmat.set(trans, 1.0f);
        
        mat.setIdentity();
        mat.setTranslation(trans);
        mat.setScale(1.0f);
        mat.setRotation(aa);
    }

    /**
     * Convert a ServletConfig to Map.  ServletConfig will override ServletContext values.
     * UserData parameters will trump all.
     */
    protected Map<String, String> convertConfig(ServletConfig config) {
        ServletContext ctx = config.getServletContext();

        HashMap<String, String> ret_val = new HashMap<String, String>();

        Enumeration e = ctx.getInitParameterNames();

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            ret_val.put(key, ctx.getInitParameter(key));
        }

        e = config.getInitParameterNames();

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();

            ret_val.put(key, config.getInitParameter(key));
        }

        return ret_val;
    }

    /**
     * Parse a request for multipart form post fields and map them to kernel params.
     * Regular form fields are expected to be in json format. File fields are not.
     *
     * NOTE: This method is duplicated in ExecutePipelineServiceServlet with special
     * logic to move the multipart upload files to the result dir. Make sure any
     * changes here is reflected to the same method in ExecutePipelineServiceServlet.
     *
     * @param req The request to parse
     * @param params The param map
     * @param maxUploadSize The max request size limit
     * @return ServiceResults with error reason, or null otherwise
     */
    protected boolean mapParams(HttpServletRequest req, Map params, int maxUploadSize, String baseDir) {
        boolean success = false;
        Gson gson = new Gson();

        try {
            // Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory(
                    0, new File(System.getProperty("java.io.tmpdir")));

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);

            // Set overall request size constraint
            upload.setSizeMax(maxUploadSize);

            // Parse the request
            List<FileItem> items = upload.parseRequest(req);

            String ext = null;
            String upload_dir = Utils.createTempDir(baseDir);

            // Process the uploaded items
            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();

                if (item.isFormField()) {
                    // Process regular form field values
                    String name = item.getFieldName();
                    String[] value = {item.getString()};
                    params.put(name, value);
                } else {
                    // Process the field field values
                    String fieldName = item.getFieldName();
                    String fileName = item.getName();

                    if (fileName == null || fileName.trim().equals("")) {
                        throw new Exception("Missing upload file for field: " + fieldName);
                    }
/*
                    int idx = fileName.lastIndexOf(".");

                    if (idx > -1) {
                        ext = fileName.substring(idx);
                    }
*/
                    String baseName = FilenameUtils.getBaseName(fileName);
                    ext = FilenameUtils.getExtension(fileName);

//                    String contentType = item.getContentType();
//                    boolean isInMemory = item.isInMemory();
                    long sizeInBytes = item.getSize();

//                    File uploadedFile = File.createTempFile(prefix, ext, new File(uploadDir));
                    File uploadedFile = Utils.createTempFile(baseDir + "/" + upload_dir, fieldName, baseName, ext);
                    item.write(uploadedFile);

                    // TODO: Schedule the uploaded file for deletion
                    
                    // JSON the path to the uploaded file
//                    System.out.println("==>fieldName: " + fieldName);
//                    System.out.println("==>fileName: " + fileName);
//                    System.out.println("write file: " + uploadedFile + " bytes: " + sizeInBytes);
                    String[] file = {uploadedFile.getAbsolutePath()};
                    params.put(fieldName, file);
                }
            }
            
            success = true;
        } catch(Exception e) {
            e.printStackTrace();
            String reason = "Failed to parse or write upload file";
            System.out.println(reason);
            success = false;
        }

        return success;
    }

    /**
     * Updates the scene for the current script and params.  Currently ignores deleted params.
     *
     * @param script
     * @param params
     */
    private EvalResult updateScene(String sceneID, String script, Map<String,Object> params) {
        SceneCacheEntry sce = sceneCache.get(sceneID);
        if (sce == null) {
            sce = new SceneCacheEntry(sceneID,script,params);
            sceneCache.put(sceneID,sce);
        } else {
            if (script != null) {
                sce.setScript(script);
            }
            if (params != null) {
                sce.updateParams(params);
            }
        }

        printf("Update Scene: %s\n",params);
        EvalResult result = render.updateScene(sceneID,sce.getScript(),params);

        return result;
    }

    
    public static class SceneCacheEntry {
        private String sceneID;
        private String script;
        private Map<String,Object> params;
        private long lastUpdateTime;

        public SceneCacheEntry(String sceneID, String script, Map<String,Object> params) {
            this.sceneID = sceneID;
            this.script = script;
            this.params = new HashMap<String,Object>();
            this.params.putAll(params);
            lastUpdateTime = System.currentTimeMillis();
        }

        public String getSceneID() {
            return sceneID;
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
            lastUpdateTime = System.currentTimeMillis();
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public void updateParams(Map<String, Object> params) {
            this.params.putAll(params);
            lastUpdateTime = System.currentTimeMillis();
        }

        public long getLastUpdateTime() {
            return lastUpdateTime;
        }
    }
}

