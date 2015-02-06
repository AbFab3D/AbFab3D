package http;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import render.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static abfab3d.util.Output.printf;

/**
 * Creates images from ShapeJS
 *
 * @author Alan Hudson
 */
public class ShapeJSImageServlet extends HttpServlet {
    public static final String VERSION = VolumeRenderer.VERSION_OPCODE_V2_DIST;

    /** Config params in map format */
    protected Map<String, String> config;

    /** The context of this servlet */
    protected ServletContext ctx;

    private ImageRenderer render;
    private Matrix4f viewMatrix = new Matrix4f(); // TODO: need to thread local

    /**
     * Initialize the servlet. Sets up the base directory properties for finding
     * stuff later on.
     */
    public void init() throws ServletException {
        // DirectorServlet overrides all but this method.  If your making changes here
        // you might need to change that class as well

        ServletConfig sconfig = getServletConfig();
        this.ctx = sconfig.getServletContext();

        config = convertConfig(sconfig);

        initCL(false, 512, 512);
        printf("Init called\n");
    }

    public void initCL(boolean debug, int width, int height) {
        render = new ImageRenderer();
        render.initCL(1,width,height);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long t0 = System.nanoTime();
        String command = req.getPathInfo();

        HttpSession session = req.getSession(true);
        String accept = req.getHeader("Accept");

        if (command.contains("/makeImage")) {
            handleRequest(req, resp, session, accept);
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

        if (command.contains("/makeImage")) {
            handleRequest(req, resp, session, accept);
        } else {
            super.doGet(req, resp);
        }
    }

    private void handleRequest(HttpServletRequest req,
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
            script = "function main(args) {\n" +
                    "    var radius = 25 * MM;\n" +
                    "    var grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,0.1*MM);\n" +
                    "    var sphere = new Sphere(radius);\n" +
                    "    var gyroid = new VolumePatterns.Gyroid(25*MM, 2*MM);\n" +
                    "    var intersect = new Intersection();\n" +
                    "    intersect.add(sphere);\n" +
                    "    intersect.add(gyroid);\n" +
                    "    var maker = new GridMaker();\n" +
                    "    maker.setSource(intersect);\n" +
                    "    maker.makeGrid(grid);\n" +
                    "\n" +
                    "    return grid;\n" +
                    "}";
        }

        long t0 = System.nanoTime();

        OutputStream os = resp.getOutputStream();

        int size = 0;
        int itype = 0;
        if (imgType.equals("PNG")) {
            itype = ImageRenderer.IMAGE_PNG;
            resp.setContentType("image/png");
        } else if (imgType.equals("JPG")) {
            itype = ImageRenderer.IMAGE_JPEG;
            resp.setContentType("image/jpeg");
        }
        if (frames == 0 || frames == 1) {
            size = render.render(jobID, script, viewMatrix, true, itype,resp.getOutputStream());
        } else {
            size = render.renderImages(jobID, script, viewMatrix, frames, framesX, true, itype,resp.getOutputStream());
        }

        printf("Image size: %d\n",size);
        resp.setContentLength(size);

        os.close();
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

        rxmat.rotX(rotX);
        rymat.rotY(rotY);

        mat.setIdentity();
        mat.mul(tmat, rxmat);
        mat.mul(rymat);
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
     * @param uploadDir The directory to save the upload files to
     * @return ServiceResults with error reason, or null otherwise
     */
    /*
    protected ServiceResult mapUploadParams(HttpServletRequest req, Map params, int maxUploadSize, String uploadDir) {
        ServiceResult result = null;
        Gson gson = DefaultGsonBuilder.getBuilder().create();

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
                        throw new Exception("Missing upload file");
                    }

                    int idx = fileName.lastIndexOf(".");

                    if (idx > -1) {
                        ext = fileName.substring(idx);
                    }

                    String contentType = item.getContentType();
                    boolean isInMemory = item.isInMemory();
                    long sizeInBytes = item.getSize();

                    String prefix = "uploaded";

                    File uploadedFile = File.createTempFile(prefix, ext, new File(uploadDir));
                    item.write(uploadedFile);

                    // Schedule the uploaded file for deletion
                    tempFiles.get(serviceName).get().add(uploadedFile);
                    // JSON the path to the uploaded file
//                    System.out.println("==>fieldName: " + fieldName);
//                    System.out.println("==>fileName: " + fileName);
//                    System.out.println("write file: " + uploadedFile + " bytes: " + sizeInBytes);
                    String[] file = {gson.toJson(uploadedFile.getAbsolutePath())};
                    params.put(fieldName, file);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            String reason = "Failed to parse or write upload file";
            System.out.println(reason);
            result = new ServiceResult(ServiceResult.ErrorCode.INVALID_PARAMS, reason);
        }

        return result;
    }
     */
}

