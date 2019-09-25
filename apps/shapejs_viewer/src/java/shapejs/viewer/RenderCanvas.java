/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package shapejs.viewer;

import abfab3d.shapejs.*;
import org.j3d.util.ErrorReporter;
import shapejs.*;

import javax.vecmath.Matrix4f;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.WeakHashMap;

import static abfab3d.core.Output.*;

/**
 * Threaded renderer to an AWT component.
 *
 * Handles several behaviors:
 *     Navigation using mouse movements
 *     Moving and Still rendering parameters
 *     Maximum width/height interpolation
 *     Expose rendering statistics
 *
 * @author Alan Hudson
 */
public class RenderCanvas implements Runnable, ComponentListener, FPSCounter {
    private boolean DEBUG = true;

    // Should we perform a HQ render after navigation ends
    private boolean HQ_RENDERING = true;
    private static final int FINAL_RENDERING_TIME = 1000;

    // Actual rendering width and height
    private int m_width;
    private int m_height;
    
    // Maximum width and height.  Will interpolate rendered values above this
    private int m_maxWidth = Integer.MAX_VALUE;
    private int m_maxHeight = Integer.MAX_VALUE;

    private Navigator m_navigator;
    private Navigator m_objNavigator;

    private transient boolean m_windowChanged = false;
    private ShapeJSExecutor m_renderer;

    private ImageSetup m_setup;
    private MatrixCamera m_camera;
    private Scene m_scene;

    private boolean m_graphicsInitialized = false;
    private boolean m_sceneLoaded = false;
    private transient boolean m_rendering;
    private volatile boolean m_pauseRendering = false;
    private volatile boolean m_terminate = false;
    private boolean m_finalRendered;

    private float m_lastFrameTime;
    private long m_lastRender;

    private Thread m_runner;
    private BufferedImage m_image;
    private final ImagePanel m_canvas;
    
    private String m_backend;
    private HashMap m_backendParams = new HashMap();
    private RenderOptions m_movingRenderOptions;
    private RenderOptions m_stillRenderOptions;
    private RenderOptions m_saveImageRenderOptions;
    private ErrorReporter m_statusReporter;

    private WeakHashMap<String,BufferedImage> imageCache = new WeakHashMap<>();

    // Scratch vars
    private Matrix4f m_viewTransform = new Matrix4f();
    private Matrix4f m_objTrans = new Matrix4f();



    public RenderCanvas(Navigator navigator, Navigator objNavigator, String backend) {

        m_navigator = navigator;
        m_objNavigator = objNavigator;
        m_backend = backend;

        m_movingRenderOptions = new RenderOptions();
        m_stillRenderOptions = new RenderOptions();
        m_saveImageRenderOptions = new RenderOptions(3,Quality.SUPER_FINE,0,Quality.DRAFT,1.0);

        m_canvas = new ImagePanel(m_image);
        m_canvas.addComponentListener(this);

        m_navigator.init(m_canvas);
        m_objNavigator.init(m_canvas);

        m_camera = new MatrixCamera();
        m_setup = new ImageSetup();

        init();
        
        m_runner = new Thread(this);
        m_runner.start();
    }

    public void setStatusReporter(ErrorReporter val) {
        m_statusReporter = val;
    }

    public void clear() {
        m_canvas.clear();
    }

    public void setOptimizeCode(boolean val) {
        m_backendParams.put("optimizeCode", val);
        m_renderer.configure(m_backendParams);
    }

    public void setPaused(boolean val) {
        m_pauseRendering = val;
    }

    public void setHQRendering(boolean val) {
        HQ_RENDERING = val;
    }

    public void run() {
        while (!m_terminate) {
            boolean rendered = false;
            if (m_graphicsInitialized && !m_pauseRendering) {
                rendered = display();
            }
            if (!rendered) {
                try {
                    // Save some power when not rendering
                    Thread.sleep(10);
                } catch(InterruptedException ie) {}
            }
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        int w = m_canvas.getWidth();
        int h = m_canvas.getHeight();
        reshape(w,h);
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    public Component getComponent() {
        return m_canvas;
    }

    public FPSCounter getCounter() {
        return this;
    }

    public void setMovingRenderOptions(RenderOptions ro) {
        m_movingRenderOptions = ro;
    }

    public void setStillRenderOptions(RenderOptions ro) {
        m_stillRenderOptions = ro;
    }

    public void setSaveImageRenderOptions(RenderOptions ro) {
        m_saveImageRenderOptions = ro;
    }

    public RenderOptions getMovingRenderOptions() {
        return m_movingRenderOptions;
    }

    public RenderOptions getStillRenderOptions() {
        return m_stillRenderOptions;
    }

    public RenderOptions getSaveImageRenderOptions() {
        return m_saveImageRenderOptions;
    }

    public void setMaxWidth(int val) {
        m_maxWidth = val;
        if (DEBUG) printf("Set Max Width: %d  -> %d\n",val,m_width);

        if (m_graphicsInitialized) reshape(m_width,m_height);
    }

    public void setMaxHeight(int val) {

        m_maxHeight = val;
        if (DEBUG) printf("Set Max Height: %d  -> %d\n",val,m_width);

        if (m_graphicsInitialized) reshape(m_width,m_height);
    }

    public void setScene(Scene scene) {
        m_scene = scene;

        m_sceneLoaded = true;
    }

    public Scene getScene() {
        return m_scene;
    }

    public RenderStat getRenderStat() {
        return m_renderer.getRenderStats();
    }

    public ShapeJSExecutor getShapeJSExecutor() {
        return m_renderer;
    }

    public void init() {

        if (m_graphicsInitialized) return;


        if (m_backend.equals("shapejs.ShapeJSExecutorCpu")) {
            // Avoid wrapper calls just directly instantiate
            m_renderer = new ShapeJSExecutorCpu();
        } else {
            m_renderer = new ShapeJSExecutorImpl(m_backend);
        }

        m_graphicsInitialized = true;
    }

    private void initPixelBuffer(int width, int height) {
        if (width <= 0) width = 4;
        if (height <= 0) height = 4;

        printf("InitPixel: %d x %d\n",width,height);
        String key = width + "x" + height;

        if (imageCache.containsKey(key)) {
            m_image = imageCache.get(key);
            m_setup.width = width;
            m_setup.height = height;
            return;
        }

        printf("Creating new pixelBuffer: %d x %d\n",width,height);

        m_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        m_setup.width = width;
        m_setup.height = height;
        imageCache.put(key,m_image);
    }

    /**
     * Return the last renderer image
     * @return
     */
    public BufferedImage getImage(){
        return m_image;
    }

    public boolean display() {
        if (!m_graphicsInitialized || !m_sceneLoaded || m_rendering) {
            return false;
        }

        long t0 = time();
        boolean navChanged = m_navigator.hasChanged() || m_objNavigator.hasChanged();
        boolean doFinalRender = (!m_finalRendered && time() - m_lastRender > FINAL_RENDERING_TIME);
        boolean doHQRender = (HQ_RENDERING && doFinalRender);

        if (!navChanged && !m_windowChanged && !doHQRender) {
            return false;
        } else {
            //printf("Rendering: wc: %b\n",windowChanged);
        }

        m_rendering = true;
        m_windowChanged = false;

        m_navigator.getMatrix(m_viewTransform);
        m_viewTransform.invert();
        m_objNavigator.getMatrix(m_objTrans);

        RenderOptions currOptions;
        if (doHQRender) currOptions = m_stillRenderOptions;
        else currOptions = m_movingRenderOptions;

        int width = m_width;
        int height = m_height;

        if (!doFinalRender && currOptions.renderScale < 1.0) {
            width = (int) Math.ceil(m_width * currOptions.renderScale);
            height = (int) Math.ceil(m_height * currOptions.renderScale);
        }
        m_setup.width = width;
        m_setup.height = height;

        initPixelBuffer(width,height);

        printf("Display.  size: %d x %d  scale: %f\n",m_width,m_height,currOptions.renderScale);
        ImageSetupUtils.configureSetup(m_setup,!doHQRender,currOptions);
        m_camera.setViewMatrix(m_viewTransform);
        m_camera.setCameraAngle(m_navigator.getCameraAngle());

        // view transform used to render scene
        m_setup.setViewTransform(m_viewTransform);
        m_setup.setObjectTransform(m_objTrans);
        m_setup.setFlipImage(true);

        m_renderer.renderImage(m_scene,m_camera, m_setup,m_image);

        m_canvas.updateImage(m_image);

        if (doFinalRender) m_finalRendered = true;
        else m_finalRendered = false;
        m_lastRender = time();
        m_lastFrameTime = (m_lastRender - t0);

        m_rendering = false;
        return true;
    }

    public void reshape(int rwidth, int rheight) {
        if (DEBUG) printf("Reshape called: %d x %d  maxWidth: %d  maxHeight: %d\n",rwidth,rheight,m_maxWidth,m_maxHeight);
        while (m_rendering) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ie) {
            }
        }

        if (m_width == rwidth && m_height == rheight && !((long)rwidth * rheight > (long) m_maxWidth * m_maxHeight)) {
            // ignore, not sure why we get these
            return;
        }


        // If image panel size is greater than max render size, calculate a smaller size
        // Compare by number of pixels
        if ((long)rwidth * rheight > (long) m_maxWidth * m_maxHeight) {
            Dimension d = getRenderDimension(rwidth, rheight, m_maxWidth, m_maxHeight);
            rwidth = d.width;
            rheight = d.height;

            if (DEBUG) printf("Larger than max size.  capped to: %d x %d\n",rwidth,rheight);
        }

        m_width = rwidth;
        m_height = rheight;

        m_graphicsInitialized = false;
        if (m_statusReporter != null) m_statusReporter.messageReport(fmt("[%d x %d]",m_width, m_height));

        initPixelBuffer(m_width, m_height);

        m_windowChanged = true;
        m_graphicsInitialized = true;
    }

    public double getScaleFactor(int size, int targetSize) {
        double dScale = 1;

        if (size != targetSize) {
            dScale = (double) targetSize / (double) size;
        }

        return dScale;
    }

    public Dimension getRenderDimension(int origWidth, int origHeight, int maxWidth, int maxHeight) {
        // Scale factor from image panel size to max size
        double dScaleWidth = getScaleFactor(origWidth, maxWidth);
        double dScaleHeight = getScaleFactor(origHeight, maxHeight);

        int width = origWidth;
        int height = origHeight;
        double panelWidthToHeightRatio = (double) origWidth / (double) origHeight;

        if (dScaleWidth < dScaleHeight) {
            // Larger difference from image width to max width
            width = maxWidth;
            height = (int) Math.round(width / panelWidthToHeightRatio);
        } else if (dScaleHeight < dScaleWidth) {
            // Larger difference from image height to max height
            height = maxHeight;
            width = (int) Math.round(height * panelWidthToHeightRatio);
        }

        // If scale size is less than the max size number of pixels, scale it up to max pixels
        double pixelRatio = Math.sqrt((double) (maxWidth * maxHeight) / (double) (width * height));
        if (pixelRatio > 1) {
            width = (int) Math.round(width * pixelRatio);
            height = (int) Math.round(height * pixelRatio);
        }

        return new Dimension(width, height);
    }

    public void forceRender() {
        m_windowChanged = true;
    }

    public Dimension getSize() {
        // Returns the image dimensions, not the image panels dimension

        return new Dimension(m_width, m_height);
    }

    /**
     * @Override
     */
    public void setNavigator(Navigator nav) {
        printf("Ignoring setNavigator for now\n");
        // this.nav = nav;
    }

    public void terminate() {
        System.out.flush();

        m_terminate = true;

        try {
            Thread.sleep(200);
        } catch (InterruptedException ie) {

        }

        m_renderer.shutdown();
    }


    public float getLastFPS() {
        return (float)(1 / (m_lastFrameTime/1000.));
    }


    public MatrixCamera getCamera(){

        return m_camera;

    }

}

