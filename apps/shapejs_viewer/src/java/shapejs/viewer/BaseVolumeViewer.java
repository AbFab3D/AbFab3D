/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2019
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package shapejs.viewer;

import abfab3d.core.AttributeGrid;
import abfab3d.core.GridDataChannel;
import abfab3d.param.*;
import abfab3d.param.editor.EditorFactory;
import abfab3d.param.editor.ParamFrame;
import abfab3d.param.editor.WindowManager;
import abfab3d.shapejs.*;
import abfab3d.util.AbFab3DGlobals;
import org.apache.commons.io.FilenameUtils;
import abfab3d.shapejs.ImageSetupUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static abfab3d.core.Output.*;
import static abfab3d.core.ResultCodes.RESULT_OK;
import static java.awt.AWTEvent.WINDOW_EVENT_MASK;
import static java.lang.Math.max;

/**
 * Real time viewer for Volumes
 *
 * @author Alan Hudson
 */
public class BaseVolumeViewer extends JFrame implements Runnable, FileHandler {
    private static final boolean DEBUG = false;

    static final String NEW_DESIGN = "new design";

    static int sm_cacheTimeout = 2 * 24 * 3600 * 1000; // cache timeout 2 days

    static double PHYSICAL_RENDER_SIZE = 4; //  render window size in inches
    static final int DEFAULT_FRAME_X = 20;
    static final int DEFAULT_FRAME_Y = 20;
    static final int DEFAULT_RENDER_SIZE = 512;
    static final int MAX_TILE_SIZE = 128;

    // Command line arguments
    protected static int fullscreen = -1;
    protected static boolean stereo = false;
    protected static int desiredSamples = 1;
    protected static int numCpus = 0;
    protected static int stereoMode = 0;
    protected static int[] screenSize = null;   // null means not provided
    protected static String backend = "shapejs.ShapeJSExecutorCpu";
    protected static final int MODE_EXAMINE = 0;
    protected static final int MODE_PAN = 1;
    protected static final int MODE_TRACK = 2;
    protected static final int MODE_RESET = 3;
    protected static final int MODE_MEASURE = 4;
    protected static final int MODE_LOOKAT = 5;

    protected static final String TITLE = "ShapeJS";

    static final String EXT_PNG = ".png";
    static final String EXT_SVX = ".svx";
    static final String EXT_STL = ".stl";

    /**
     * Should we use full screen mode
     */
    protected boolean useFullscreen;

    /**
     * The current navigator
     */
    protected Navigator m_navigator;
    protected Navigator m_objNavigator;

    /**
     * The rendering thread
     */
    protected RenderCanvas m_canvas;
    protected SlicePanel m_sliceCanvas;

    /**
     * The status bar
     */
    protected StatusBar m_statusBar;

    /**
     * The content pane for the frame
     */
    protected Container mainPane;

    // The current data source parent
    protected Scene m_scene;
    ShapeJSDesign m_design = new ShapeJSDesign(false);

    //UI
    protected EditSceneAction editSceneAction = null;
    protected EditSourceAction editSourceAction = null;
    protected CustomEditors customEditors;
    protected ParamFrame m_scriptParamEditor = null;
    protected ScriptParamChangedListener m_scriptParamListener;
    protected SceneParamChangedListener m_sceneParamListener;
    protected String m_jobID;  // id to get data from ScriptManager
    protected DiskWatcher m_diskWatcher = null;
    protected boolean m_watchDisk = true;
    protected Console console;
    protected int navMode = MODE_EXAMINE;
    protected int tilesDone;

    boolean m_scriptParamChanged = false;
    // params changed via UI
    LinkedHashMap<String, Object> m_changedParams = null;

    UIParamWatcher m_paramWatcher;

    protected DropTarget m_dropTarget;
    protected JMenu m_historyMenu;

    Font m_font = new Font("Helvetica", Font.PLAIN, 12);

    protected int m_renderWidth = DEFAULT_RENDER_SIZE;
    protected int m_renderHeight = DEFAULT_RENDER_SIZE;
    protected int m_frameX = DEFAULT_FRAME_X;
    protected int m_frameY = DEFAULT_FRAME_Y;
    protected int m_frameWidth = 0;
    protected int m_frameHeight = 0;

    // flag to make that script params were modified and scene need re-render
    protected boolean m_sriptParamsModified = false;

    protected JMenuBar m_menuBar = new JMenuBar();
    protected String initialFile = null;


    /**
     * Typical usage message with program options
     */
    protected static final String USAGE_MSG =
            "Usage: Browser [options] [filename]\n" +
                    "  -help                   Prints out this help message\n" +
                    "  -fullscreen n           Runs the browser in fullscreen exclusive mode on screen n.  n is optional\n" +
                    //"  -stereo quad|alternate  Enables stereo projection output\n" +
                    "  -antialias n            Use n number of multisamples to antialias scene\n" +
                    "  -numCpus n              Select how many cpu's to use.  Defaults to all\n" +
                    "  -nice                   Do not use all the CPU for rendering\n" +
                    "  -screenSize w h         Specify the screen size to use\n";


    static {
        // global initialization of cache
        ScriptManager.setCacheTimeout(sm_cacheTimeout);
    }


    public BaseVolumeViewer() {

        super(TITLE);
    }

    public void init() {

        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        if (DEBUG) printf("VolumeViewer()  screen dpi: %d\n", dpi);
        Container cont = this.getContentPane();
        Insets ins = cont.getInsets();
        if (DEBUG) printf(" insets: [%d %d %d %d]\n", ins.left, ins.top, ins.right, ins.bottom);

        // 6" of rendering area seems like a good size
        int pixels = (int) Math.round(dpi * PHYSICAL_RENDER_SIZE);

        m_renderWidth = pixels;
        m_renderHeight = pixels;
        if (DEBUG) printf(" renderSize: [%d x %d]\n", m_renderWidth, m_renderHeight);

        // Enlarge fonts for readability on hi-res display
        if (dpi > 110) {
            try {
                setMinimumFontSize(18);
                m_font = new Font("Helvetica", Font.PLAIN, 18);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        m_dropTarget = new DropTarget(this, new ViewerDropTarget(this));

        GraphicsEnvironment env =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice dev;

        enableEvents(WINDOW_EVENT_MASK);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        if (fullscreen >= 0) {
            useFullscreen = true;
            GraphicsDevice[] gs = env.getScreenDevices();
            if (gs.length < fullscreen + 1) {
                System.out.println("Invalid fullscreen device.  Using default");
                dev = env.getDefaultScreenDevice();

                Dimension dmn = Toolkit.getDefaultToolkit().getScreenSize();
                m_frameWidth = (int) dmn.getWidth();
                m_frameHeight = (int) dmn.getHeight();
            } else {
                dev = gs[fullscreen];
                DisplayMode dm = dev.getDisplayMode();
                m_frameWidth = dm.getWidth();
                m_frameHeight = dm.getHeight();
            }
        } else {
            dev = env.getDefaultScreenDevice();
        }

        if (useFullscreen && !dev.isFullScreenSupported()) {
            System.out.println("Fullscreen not supported");
            useFullscreen = false;
        }

        if (useFullscreen) {
            DisplayMode currentMode = dev.getDisplayMode();
            DisplayMode prefDisplayMode =
                    new DisplayMode(m_frameWidth,
                            m_frameHeight,
                            currentMode.getBitDepth(),
                            DisplayMode.REFRESH_RATE_UNKNOWN);

            setUndecorated(true);
            dev.setFullScreenWindow(this);

            if (dev.isDisplayChangeSupported()) {
                dev.setDisplayMode(prefDisplayMode);
            } else {
                printf("Fullscreen supported but display mode change is not\n");
            }
        }

        // TODO: Not sure how achieve this yet, we want to keep the real material rendering code private
//        ShapewaysSetup setup = ShapewaysSetup.getInstance();

        Runtime system_runtime = Runtime.getRuntime();
        system_runtime.addShutdownHook(new Thread(this));

        m_navigator = new ExamineNavigator();
        m_objNavigator = new ObjectNavigator();
//        m_canvas = new WindowRenderCanvas(m_navigator, m_objNavigator);
        printf("Using ShapeJSExecutor: %s\n",backend);
        m_canvas = new RenderCanvas(m_navigator, m_objNavigator, backend);

        printf("Calling makeUI\n");
        createUI();

        m_paramWatcher = new UIParamWatcher();
        new Thread(m_paramWatcher).start();

        //m_canvas.setStatusBar(m_statusBar);  // TODO: ADH: Do we really need this?

        Component canvasComp = m_canvas.getComponent();
        canvasComp.setPreferredSize(new Dimension(m_renderWidth, m_renderHeight));
        mainPane.add(canvasComp, BorderLayout.CENTER);
        this.pack();
        //add(m_canvas.getComponent());
        //setSize(m_frameWidth, m_frameHeight);
        Dimension frameDim = this.getSize();
        m_frameWidth = frameDim.width;
        m_frameHeight = frameDim.height;
        if (DEBUG) printf(" frameSize: [%d x %d]\n", m_frameWidth, m_frameHeight);

        setVisible(true);

        if (!useFullscreen && screenSize == null) {
            setLocation(m_frameX, m_frameY);
        }

        console = new Console();
        console.setLocation(m_frameX, m_frameY + m_frameHeight);
        console.setVisible(true);
        ViewerConfig.getInstance().setConsole(console);
        //console.redirectSystemMessages();
        setIconImage(new ImageIcon("classes/images/shapejs_icon_32.png").getImage());

        m_canvas.reshape(m_canvas.getComponent().getWidth(), m_canvas.getComponent().getHeight());

        customEditors = new CustomEditors(m_canvas.getComponent(), m_navigator, m_objNavigator);
        EditorFactory.addCreator(customEditors);

        m_navigator.setEnabled(true);
        Utils.setFont(this, m_font);
        updateHistoryMenu();

        int cores = Runtime.getRuntime().availableProcessors();
        AbFab3DGlobals.put(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY, new Integer(cores));
    }

    public void parseArgs(String[] args) {
        printf("Parsing args: %s\n",Arrays.toString(args));
        int lastUsed = -1;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if (args[i].equals("-fullscreen")) {
                    fullscreen = 0;
                    lastUsed = i;

                    try {
                        String val = args[i + 1];
                        fullscreen = Integer.valueOf(val).intValue();
                        lastUsed = i + 1;
                    } catch (Exception e) {
                    }
                } else if (args[i].equals("-screenSize")) {
                    lastUsed = i;

                    screenSize = new int[2];

                    try {
                        String val = args[i + 1];
                        screenSize[0] = Integer.valueOf(val).intValue();
                        screenSize[1] = Integer.valueOf(val).intValue();
                        lastUsed = i + 1;
                    } catch (Exception e) {
                        System.out.println("Invalid screen size");

                        screenSize = null;
                    }
                } else if (args[i].equals("-impl")) {
                    lastUsed = i + 1;

                    backend = args[i + 1];
                    printf("Backend arg: %s\n",backend);
                    if (!ShapeJSExecutorImpl.exists(backend)) {
                        throw new IllegalArgumentException(fmt("Backend not found: %s", backend));
                    }

                    if (!backend.contains("Cpu")) {
                        // Adjust default size up for GPU backed
                        PHYSICAL_RENDER_SIZE *= 1.5;
                    }
                } else if (args[i].equals("-stereo")) {
                    stereo = true;
                    String val = args[++i];
                    lastUsed = i;

                    throw new IllegalArgumentException("Stereo not supported yet");
                    /*
                    if (val.equalsIgnoreCase("quad")) {
                        //stereoMode = GraphicsOutputDevice.QUAD_BUFFER_STEREO;
                    } else if (val.equalsIgnoreCase("alternate")) {
                        //stereoMode = GraphicsOutputDevice.ALTERNATE_FRAME_STEREO;
                    } else {
                        System.out.println("Unknown stereo mode: " + val);
                    }
                    */
                } else if (args[i].equals("-help")) {
                    System.out.println(USAGE_MSG);
                    return;
                } else if (args[i].equals("-antialias")) {
                    String val = args[++i];
                    desiredSamples = Integer.valueOf(val).intValue();
                    lastUsed = i;
                } else if (args[i].equals("-numCpus")) {
                    String val = args[++i];
                    numCpus = Integer.valueOf(val).intValue();
                    lastUsed = i;
                } else if (args[i].startsWith("-")) {
                    System.out.println("Unknown flag: " + args[i]);
                    lastUsed = i;
                }
            }
        }


        // The last argument is the filename parameter

        if ((args.length > 0) && (lastUsed + 1 < args.length)) {
            initialFile = args[args.length - 1];
        }
    }


    protected void updateTitle() {

        setTitle(fmt(" %s [ %s ]", m_design.getFileName(), m_design.getScriptName()));

    }

    public void setToolbarMode(String val) {
        if (val.equalsIgnoreCase("EXAMINE")) {
            navMode = MODE_EXAMINE;
            m_objNavigator.setEnabled(false);
            m_navigator.setEnabled(true);
        } else if (val.equalsIgnoreCase("PAN")) {
            navMode = MODE_PAN;
            m_objNavigator.setEnabled(false);
            m_navigator.setEnabled(true);
        } else if (val.equalsIgnoreCase("TRACK")) {
            navMode = MODE_TRACK;
            m_objNavigator.setEnabled(true);
            m_navigator.setEnabled(false);
        } else if (val.equalsIgnoreCase("MEASURE")) {
            // now looking for 2 right clicks to
            navMode = MODE_MEASURE;
        } else if (val.equalsIgnoreCase("LOOKAT")) {
            navMode = MODE_LOOKAT;
        } else if (val.equalsIgnoreCase("RESET")) {
            m_navigator.reset();
            m_objNavigator.reset();
        }
    }


    //---------------------------------------------------------------
    // Methods defined by Runnable
    //---------------------------------------------------------------

    /**
     * Run method for the shutdown hook. This is to deal with someone using
     * ctrl-C to kill the application. Makes sure that all the resources
     * are cleaned up properly.
     */
    public void run() {
        shutdownApp();
    }

    /**
     * Close down the application safely by destroying all the resources
     * currently in use.
     */
    public void shutdownApp() {
        m_canvas.terminate();
        if (m_diskWatcher != null) {
            m_diskWatcher.shutdown();
        }
        if (m_paramWatcher != null)
            m_paramWatcher.shutdown();
        ViewerConfig.getInstance().save();
        //try { Thread.sleep(3000); } catch(Exception e) {}
    }

    void updateHistoryMenu() {

        History hist = ViewerConfig.getInstance().getOpenFileHistory();

        //hist.print();

        m_historyMenu.removeAll();
        int count = hist.size();
        for (int i = count - 1; i >= 0; i--) {
            String path = hist.getItem(i);
            String fname = new File(path).getName();
            JMenuItem mi = new JMenuItem(fname);
            mi.addActionListener(new OpenRecentAction(this, path));
            mi.setToolTipText(path);
            m_historyMenu.add(mi);
        }
        m_historyMenu.addSeparator();
        JMenuItem miClear = new JMenuItem("clear history");
        miClear.addActionListener(new ClearHistoryAction(this));
        m_historyMenu.add(miClear);

    }

    //---------------------------------------------------------------
    // Methods defined by Window
    //---------------------------------------------------------------

    @Override
    protected void processWindowEvent(WindowEvent e) {

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            // Too many threads running, so, calling shutdown does nothing
            System.exit(0);

        } else if (e.getID() == WindowEvent.WINDOW_CLOSED) {
            System.out.println("Shutting down Xj3D");
        }

        // Pass along all other events
        super.processWindowEvent(e);

    }

    public static void setMinimumFontSize(int size) {
        Set<Object> keySet = UIManager.getLookAndFeelDefaults().keySet();
        Object[] keys = keySet.toArray(new Object[keySet.size()]);

        for (Object key : keys) {

            if (key != null && key.toString().toLowerCase().contains("font")) {

                //System.out.println(key + " -> " + UIManager.getLookAndFeelDefaults().get(key));
                Font font = UIManager.getDefaults().getFont(key);
                if (font != null && font.getSize() < size) {
                    font = font.deriveFont((float) size);
                    UIManager.put(key, font);
                }

            }

        }

    }

    /**
     * Create the window contents now.
     */
    public void createUI() {
        List<Action> actionList = new ArrayList<Action>();

        m_statusBar = new StatusBar(m_canvas, true, true);

        // Create the menu bar
        JMenuItem menuItem;
        JRadioButtonMenuItem rbItem;

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        m_menuBar.add(fileMenu);

        // Open Item
        m_historyMenu = new JMenu("Open Recent");

        OpenAction openAction = new OpenAction(this, null, m_historyMenu);
        actionList.add(openAction);
        fileMenu.add(new JMenuItem(openAction));
        fileMenu.add(m_historyMenu);

        // save design
        Action saveDesign = new SaveDesignAction(this);
        actionList.add(saveDesign);
        fileMenu.add(new JMenuItem(saveDesign));

        Action saveDesignAs = new SaveDesignAsAction(this);
        actionList.add(saveDesignAs);
        fileMenu.add(new JMenuItem(saveDesignAs));

        // render image
        Action renderImageAction = new RenderImageAction(this);
        actionList.add(renderImageAction);
        fileMenu.add(new JMenuItem(renderImageAction));

        Action saveModelAction = new SaveModelAction(this);
        actionList.add(saveModelAction);
        fileMenu.add(new JMenuItem(saveModelAction));

        Action saveModelPJAction = new SaveModelPolyJetAction(this);
        actionList.add(saveModelPJAction);
        fileMenu.add(new JMenuItem(saveModelPJAction));

        /*
        // render channel
        Action renderChannelAction = new RenderChannelAction(this);
        actionList.add(renderChannelAction);
        fileMenu.add(new JMenuItem(renderChannelAction));

        // render grid
        Action renderGridAction = new RenderGridAction(this);
        actionList.add(renderGridAction);
        fileMenu.add(new JMenuItem(renderGridAction));
*/
        // clear cache
        Action clearCacheAction = new ClearCacheAction(this);
        actionList.add(clearCacheAction);
        fileMenu.add(new JMenuItem(clearCacheAction));

        ExitAction exitAction = new ExitAction(this);
        actionList.add(exitAction);

        // Exit Item
        fileMenu.add(new JMenuItem(exitAction));

        // View Menu
        JMenu viewMenu = new JMenu("View");
        //menuBar.add(viewMenu);

        // Render Style SubMenu
        JMenu renderStyle = new JMenu("Render Style");

        ButtonGroup rsGroup = new ButtonGroup();

        JMenu navMenu = new JMenu("Navigation");

        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic(KeyEvent.VK_O);

        AntialiasingAction antialiasingAction = new AntialiasingAction(m_canvas, m_statusBar);
        actionList.add(antialiasingAction);

        JMenu antialiasingMenu = new JMenu("Anti-Aliasing");
        ButtonGroup antialiasingGroup = new ButtonGroup();

        rbItem = new JRadioButtonMenuItem("Disabled");
        if (desiredSamples <= 1)
            rbItem.setSelected(true);
        rbItem.setActionCommand("Disabled");
        rbItem.addActionListener(antialiasingAction);
        antialiasingMenu.add(rbItem);
        antialiasingGroup.add(rbItem);

        int maxSamples = AntialiasingAction.getMaximumNumberOfSteps();

        int n = 2;
        while (n <= maxSamples) {
            rbItem = new JRadioButtonMenuItem(n + " Samples", n == AntialiasingAction.getDefaultNumberOfSteps());

            rbItem.addActionListener(antialiasingAction);
            rbItem.setActionCommand(Integer.toString(n - 1));
            antialiasingMenu.add(rbItem);
            antialiasingGroup.add(rbItem);

            if (n >= 8) n = n + 4;
            else n++;
        }
        optionsMenu.add(antialiasingMenu);

        QualityAction qualityAction = new QualityAction(m_canvas, m_statusBar,Quality.NORMAL);
        actionList.add(qualityAction);

        JMenu qualityMenu = new JMenu("Quality");
        ButtonGroup qualityGroup = new ButtonGroup();

        rbItem = new JRadioButtonMenuItem("Draft", false);
        rbItem.addActionListener(qualityAction);
        rbItem.setActionCommand(Quality.DRAFT.toString());
        qualityMenu.add(rbItem);
        qualityGroup.add(rbItem);

        rbItem = new JRadioButtonMenuItem("Normal", true);
        rbItem.addActionListener(qualityAction);
        rbItem.setActionCommand(Quality.NORMAL.toString());
        qualityMenu.add(rbItem);
        qualityGroup.add(rbItem);

        rbItem = new JRadioButtonMenuItem("Fine", false);
        rbItem.addActionListener(qualityAction);
        rbItem.setActionCommand(Quality.FINE.toString());
        qualityMenu.add(rbItem);
        qualityGroup.add(rbItem);

        rbItem = new JRadioButtonMenuItem("Super Fine", false);
        rbItem.addActionListener(qualityAction);
        rbItem.setActionCommand(Quality.SUPER_FINE.toString());
        qualityMenu.add(rbItem);
        qualityGroup.add(rbItem);

        optionsMenu.add(qualityMenu);

        ShadowQualityAction shadowQualityAction = new ShadowQualityAction(m_canvas, m_statusBar, Quality.DRAFT);
        actionList.add(shadowQualityAction);

        JMenu shadowQualityMenu = new JMenu("Shadow Quality");
        ButtonGroup shadowQualityGroup = new ButtonGroup();

        rbItem = new JRadioButtonMenuItem("Disabled", true);
        rbItem.addActionListener(shadowQualityAction);
        rbItem.setActionCommand(Quality.DRAFT.toString());
        shadowQualityMenu.add(rbItem);
        shadowQualityGroup.add(rbItem);

        rbItem = new JRadioButtonMenuItem("Normal", false);
        rbItem.addActionListener(shadowQualityAction);
        rbItem.setActionCommand(Quality.NORMAL.toString());
        shadowQualityMenu.add(rbItem);
        shadowQualityGroup.add(rbItem);

        rbItem = new JRadioButtonMenuItem("Fine", false);
        rbItem.addActionListener(shadowQualityAction);
        rbItem.setActionCommand(Quality.FINE.toString());
        shadowQualityMenu.add(rbItem);
        shadowQualityGroup.add(rbItem);

        rbItem = new JRadioButtonMenuItem("Super Fine", false);
        rbItem.addActionListener(shadowQualityAction);
        rbItem.setActionCommand(Quality.SUPER_FINE.toString());
        shadowQualityMenu.add(rbItem);
        shadowQualityGroup.add(rbItem);

        optionsMenu.add(shadowQualityMenu);

        RayBouncesAction rayBouncesAction = new RayBouncesAction(m_canvas, m_statusBar);
        actionList.add(rayBouncesAction);

        JMenu rayBouncesMenu = new JMenu("Ray Bounces");
        ButtonGroup rayBouncesGroup = new ButtonGroup();

        int maxBounces = RayBouncesAction.getMaximumNumberOfBounces();

        n = 0;
        while (n < maxBounces) {
            rbItem = new JRadioButtonMenuItem(n + " Bounces", n == RayBouncesAction.getDefaultNumberOfBounces());

            rbItem.addActionListener(rayBouncesAction);
            rbItem.setActionCommand(Integer.toString(n));
            rayBouncesMenu.add(rbItem);
            rayBouncesGroup.add(rbItem);

            n++;
        }
        optionsMenu.add(rayBouncesMenu);

        HQRenderAction hqAction = new HQRenderAction(m_canvas, m_statusBar);
        actionList.add(hqAction);

        JMenu hqMenu = new JMenu("HQ Render");
        ButtonGroup hqGroup = new ButtonGroup();

        rbItem = new JRadioButtonMenuItem("Enabled", true);
        rbItem.addActionListener(hqAction);
        rbItem.setActionCommand("TRUE");
        hqMenu.add(rbItem);
        hqGroup.add(rbItem);

        rbItem = new JRadioButtonMenuItem("Disabled", false);
        rbItem.addActionListener(hqAction);
        rbItem.setActionCommand("FALSE");
        hqMenu.add(rbItem);
        hqGroup.add(rbItem);
        optionsMenu.add(hqMenu);

        OptimizeAction optimizeAction = new OptimizeAction(m_canvas, m_statusBar);
        actionList.add(optimizeAction);
        JMenu optimizeMenu = new JMenu("Optimize Scene");
        ButtonGroup optimizeGroup = new ButtonGroup();

        rbItem = new JRadioButtonMenuItem("Enabled", true);
        rbItem.addActionListener(optimizeAction);
        rbItem.setActionCommand("TRUE");
        optimizeMenu.add(rbItem);
        optimizeGroup.add(rbItem);

        rbItem = new JRadioButtonMenuItem("Disabled", false);
        rbItem.addActionListener(optimizeAction);
        rbItem.setActionCommand("FALSE");
        optimizeMenu.add(rbItem);
        optimizeGroup.add(rbItem);
        optionsMenu.add(optimizeMenu);


        EditOptionsAction editOptionsAction = new EditOptionsAction();
        actionList.add(editOptionsAction);
        optionsMenu.add(new JMenuItem(editOptionsAction));

        m_menuBar.add(optionsMenu);

        // File Menu
        JMenu sceneEdit = new JMenu("Scene");

        m_menuBar.add(sceneEdit);

        // Edit Scene
        editSceneAction = new EditSceneAction(null, m_canvas);
        actionList.add(editSceneAction);
        sceneEdit.add(new JMenuItem(editSceneAction));

        editSourceAction = new EditSourceAction(null, m_canvas);
        actionList.add(editSourceAction);
        sceneEdit.add(new JMenuItem(editSourceAction));

        // TODO: Need to provide a way to add this at the GPU level
/*
        JMenu profileMenu = new JMenu("Profile");
        String[] metrics = ProfilePanel.Metric.names();
        ButtonGroup metricGroup = new ButtonGroup();
        for (int i = 0; i < metrics.length; i++) {
            rbItem = new JRadioButtonMenuItem(metrics[i], (i == 0));
            rbItem.addActionListener(m_canvas.getProfileViewer());
            metricGroup.add(rbItem);
            profileMenu.add(rbItem);
        }
        menuBar.add(profileMenu);
*/


        m_sliceCanvas = new SlicePanel();

        JMenu sliceMenu = new JMenu("Slice");
        ButtonGroup sliceGroup = new ButtonGroup();
        rbItem = new JRadioButtonMenuItem("none", true);
        sliceMenu.add(rbItem);
        sliceGroup.add(rbItem);
//        rbItem.addActionListener(m_canvas.getSliceViewer());
        rbItem.addActionListener(m_sliceCanvas);
        rbItem = new JRadioButtonMenuItem("Distance", true);
        sliceMenu.add(rbItem);
        sliceGroup.add(rbItem);
//        rbItem.addActionListener(m_canvas.getSliceViewer());
        rbItem.addActionListener(m_sliceCanvas);
        m_menuBar.add(sliceMenu);

        mainPane = getContentPane();

        printf("mainPane: %s\n",mainPane);

        Toolbar toolbar = new Toolbar(this);

        if (!useFullscreen) {
            JPanel p2 = new JPanel(new BorderLayout());
            p2.add(toolbar, BorderLayout.NORTH);
            p2.add(m_statusBar, BorderLayout.SOUTH);
            mainPane.add(p2, BorderLayout.SOUTH);

            setJMenuBar(m_menuBar);


        } else {
            // Need to register all actions with canvas manually
            JComponent comp = (JComponent) getContentPane();
            KeyStroke ks;
            String actionName;

            Iterator<Action> itr = actionList.iterator();
            Action action;

            while (itr.hasNext()) {
                action = itr.next();

                ks = (KeyStroke) action.getValue(AbstractAction.ACCELERATOR_KEY);
                actionName = (String) action.getValue(AbstractAction.SHORT_DESCRIPTION);

                comp.getInputMap().put(ks, actionName);
                comp.getActionMap().put(actionName, action);
            }
        }
    }

    public void onClearCache() {
        ParamCache.getInstance().invalidateAll();

        // TODO: How to handle this?
        //GPUCache.getInstance().clearAll();
    }


    public void loadFile(String path, boolean resetView) {

        onReadDesign(path);

    }

    protected void initDiskWatcher() {

        if (DEBUG) printf("initDiskWatcher()\n");

        if (m_diskWatcher != null) {
            m_diskWatcher.shutdown();
        }

        m_diskWatcher = new DiskWatcher();

        m_diskWatcher.addParamChangedListener(new URIParameter("script", m_design.getScriptPath()), new ScriptFileChangedListener());

        Parameter params[] = m_design.getEvaluatedScript().getResult().getParams();
        for (int i = 0; i < params.length; i++) {
            if (params[i].getType() == ParameterType.URI) {
                m_diskWatcher.addParamChangedListener((URIParameter) params[i], new ScriptParamChangedListener(m_canvas, m_jobID));
            }
        }
        new Thread(m_diskWatcher).start();

    }


    /**
     * saves current design into a file together with thumbnail
     */
    public void onSaveDesignAs(String path) throws IOException {

        if (m_design.write(path) != RESULT_OK)
            return;

        ViewerConfig.getInstance().getOpenFileHistory().add(m_design.getHistoryPath());
        updateHistoryMenu();

        updateTitle();
        saveThumbnail();
    }

    /**
     * save design to current path
     */
    public void saveDesign() throws IOException {

        if (m_design.hasPath()) {
            m_design.save();
            saveThumbnail();
            console.messageReport(fmt("saved:%s", m_design.getPath()));
        } else {
            console.messageReport(fmt("design has no file, use SaveAs\n"));
        }
    }

    public void saveThumbnail() {
        try {
            BufferedImage image = m_canvas.getImage();
            String tpath = m_design.getPath() + EXT_PNG;
            ImageIO.write(image, "png", new File(tpath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveThumbnail(String path) {
        try {
            BufferedImage image = m_canvas.getImage();
            String tpath = path + EXT_PNG;
            ImageIO.write(image, "png", new File(tpath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ShapeJSDesign getDesign() {
        return m_design;
    }

    /**
     * read design params from a file
     */
    public void onReadDesign(String path) {

        console.messageReport(fmt("loading: %s\n", path));
        try {
            if (m_design.read(path) != RESULT_OK) {
                console.messageReport("design loading failed\n");
                console.messageReport(m_design.getErrorMessages());
                initDiskWatcher();
                refreshUI();
                return;
            } else {
                console.messageReport(fmt("loaded: %s(%s)\n", m_design.getPath(), m_design.getScriptName()));
            }
        } catch (Exception e) {
            console.messageReport("exception during script loading\n");
            console.messageReport(m_design.getErrorMessages());
            console.messageReport(fmt("exception: %s[%s]\n", e.getClass().getName(), e.getMessage()));
            e.printStackTrace(console.getPrintStream());
        }

        console.messageReport(fmt("design loaded: %s\n", path));
        initDiskWatcher();
        refreshUI();

        // Bind the first viewpoint if its provided (when we need that?)
        if (false) {
            List<Viewpoint> vp = m_design.getScene().getViewpoints();
            m_navigator.reset();
            if (vp.size() > 0) {
                Viewpoint svp = vp.get(0);
                m_navigator.setViewpoint(svp);
            }
        }
        m_canvas.forceRender();

        ViewerConfig.getInstance().getOpenFileHistory().add(m_design.getHistoryPath());
        updateHistoryMenu();
        // restore cursor
        //setCursor(savedCursor);
        updateTitle();

    }


    /**
     * called in case if scrip twas changed on disk
     */
    public void reloadScript() {

        console.messageReport(fmt("reloading script: %s\n", m_design.getPath()));
        if (m_design.reloadScript() != RESULT_OK) {
            console.messageReport("script reloading failed\n");
            console.messageReport(m_design.getErrorMessages());
            initDiskWatcher();
            refreshUI();
            return;
        }

        console.messageReport("script reloaded\n");

        initDiskWatcher();
        refreshUI();


    }

    /**
     * re-init UI after script reload
     */
    void refreshUI() {

        WindowManager.getInstance().closeAll();
        //setScene(m_design.getScene());

        m_scriptParamEditor = new ParamFrame(new ParameterizableAdapter(m_design.getEvaluatedScript().getScriptParams()));

        m_scriptParamListener = new ScriptParamChangedListener(m_canvas, m_jobID);
        m_scriptParamEditor.getPanel().addParamChangedListener(m_scriptParamListener);

        //m_design.addURIParamChangedListener(m_scriptParamListener);

        Dimension dim = getSize();
        Point pnt = getLocationOnScreen();
        pnt.x += dim.width;
        if (DEBUG) printf("ParamFrame location:[%d,%d]\n", pnt.x, pnt.y);
        m_scriptParamEditor.setLocation(pnt.x, pnt.y);
        m_scriptParamEditor.setVisible(true);

        setScriptParamChanged();
    }

    /**
     * render image to a file
     */
    public void onRenderImage(final String filePath, int width, int height) {
        final BufferedImage base = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        tilesDone = 0;
        console.messageReport(fmt("saving image[ %d x %d ] into %s\n", width, height, filePath));
        final Cursor scursor = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                long t0 = time();
                try {
                    ShapeJSExecutor renderer = m_canvas.getShapeJSExecutor();

                    RenderOptions ro = m_canvas.getSaveImageRenderOptions();
                    ImageSetup is = new ImageSetup();
                    ImageSetupUtils.configureSetup(is,false,ro);
                    is.flipImage = true;
                    is.width = width;
                    is.height = height;
                    renderer.renderImage(m_scene, m_canvas.getCamera(), is, base);
                    try {
                        ImageIO.write(base, "png", new File(filePath));
                        console.messageReport(fmt("Saving image done %d ms\n", (time() - t0)));
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                } finally {
                    setCursor(scursor);
                }
            }
        });
    }

    /**
     * save model into 3d file
     */
    public void onSaveModel(final String filePath) {
        console.clear();

        m_canvas.setPaused(true);
        final Cursor scursor = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try (
                        FileOutputStream fos = new FileOutputStream(filePath);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                ) {
                    ShapeJSExecutor renderer = m_canvas.getShapeJSExecutor();


                    String ext = FilenameUtils.getExtension(filePath).toLowerCase();

                    if (ext.equals("svx")) {
                        renderer.renderSVX(m_scene, bos);
                    } else {
                        renderer.renderTriangle(m_scene, bos, FilenameUtils.getExtension(filePath));
                    }

                    saveThumbnail(filePath);

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } finally {
                    console.messageReport("Saving model done");
                    m_canvas.setPaused(false);
                    setCursor(scursor);
                }
            }
        });
    }

    /**
     * save model into 3d file
     */
    public void onSaveModelPolyJet(final String filePath, final ParamMap params) {

        final long startTime = time();
        console.messageReport(fmt("Saving PolyJet slices:%s\n", filePath));

        Thread saverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    if (!new File(filePath).mkdirs())
                        return;

                    saveThumbnail(filePath);

                    ShapeJSExecutor renderer = m_canvas.getShapeJSExecutor();
                    renderer.renderPolyJet(m_scene, params, filePath);
                } catch (Exception e) {
                    console.messageReport(fmt("exception while writing slices:%s\n", filePath));
                    e.printStackTrace();
                }
            }
        });
        saverThread.start();
    }

    public Scene getScene() {
        return m_scene;
    }

    public Navigator getViewNavigator() {
        return m_navigator;
    }

    public Navigator getObjNavigator() {
        return m_objNavigator;
    }

    public Component getRenderComponent() {
        return m_canvas.getComponent();
    }

    public RenderCanvas getRenderCanvas() {
        return m_canvas;
    }

    private void setScene(Scene scene) {
        if (scene == null) {
            printf("Scene is null?");
            return;
        }
        m_scene = scene;

        m_canvas.setScene(scene);
        m_sliceCanvas.setScene(scene);
        customEditors.setScene(scene);

        customEditors.setScene(scene);
        editSceneAction.setScene(scene);
        editSourceAction.setScene(scene);
        m_navigator.setBounds(scene.getBounds());
        m_canvas.forceRender();
    }


    public void tileDone(int tilex, int tiley, int totalTiles, long time) {
        tilesDone++;
        //printf("here: %d/%d\n",tilesDone,totalTiles);   // not sure why this isn't updating status bar till end
        if (totalTiles > 1) {
            m_statusBar.setStatusText(fmt("Tile finished.  done: (%d/%d) time: %d ms", tilesDone, totalTiles, time));
        }
    }

    /**
     * return HashMap with scrip param values
     */
    LinkedHashMap<String, Object> getScriptParamsMap(EvaluatedScript escript) {

        if (escript == null)
            return null;

        LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
        Parameter[] ap = escript.getResult().getParams();
        for (int i = 0; i < ap.length; i++) {
            params.put(ap[i].getName(), ap[i].getValue());
        }
        return params;
    }


    /**
     * processor of ParamChangedListener events coming from scene param editors
     */
    class SceneParamChangedListener implements ParamChangedListener {

        public void paramChanged(Parameter parameter) {
            m_canvas.forceRender();
        }
    } // class SceneParamListener 


    /**
     * processor of ParamChangedListener events
     * coming from script params editor
     */
    class ScriptParamChangedListener implements ParamChangedListener {

        private RenderCanvas canvas;
        private String jobID;

        public ScriptParamChangedListener(RenderCanvas canvas, String jobID) {
            this.canvas = canvas;
            this.jobID = jobID;
        }

        @Override
        public void paramChanged(Parameter parameter) {

            if (parameter.getType() == ParameterType.URI) {
                m_diskWatcher.updateParam(parameter);
            }
            setScriptParamChanged(true, parameter);
        }
    } // class ScriptParamListener


    void setScriptParamChanged(boolean value, Parameter parameter) {

        if (m_changedParams == null) m_changedParams = new LinkedHashMap<String, Object>();

        m_changedParams.put(parameter.getName(), parameter.getValue());
        m_scriptParamChanged = value;
    }

    void setScriptParamChanged() {
        m_scriptParamChanged = true;
    }

    void clearScriptParamChanged() {
        m_changedParams = null;
        m_scriptParamChanged = false;
    }


    boolean getScriptParamChanged() {
        return m_scriptParamChanged;
    }

    /**
     * retunr map of (name, value) pairs of changed parameters
     */
    LinkedHashMap<String, Object> getChangedParams() {
        return m_changedParams;
    }

    /**
     * class watches for UI events and does update scene on separate thread
     */
    class UIParamWatcher implements Runnable {

        private volatile boolean terminate = false;
        private long sleepTime = 10; //sleep time in MS 

        public void shutdown() {
            terminate = true;
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ie) {
                }
                if (terminate) return;

                if (getScriptParamChanged()) {

                    LinkedHashMap<String, Object> changedParams = getChangedParams();
                    clearScriptParamChanged();
                    if (changedParams != null) {
                        try {
                            int result = m_design.updateScriptParams(changedParams);
                            if (result != RESULT_OK) {
                                printf("Failed to load scene: ");
                            }
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }


                    if (DEBUG) printf("param changed -> scene render\n");
                    long t0 = time();
                    setScene(m_design.getScene());
                    if (DEBUG) printf("                scene render time: %d\n", time() - t0);
                }
            }
        }
    }

    public static void printSlice(AttributeGrid grid, int z, int ch) {
        printSlice(grid, z, ch, 1.);

    }

    public static void printSlice(AttributeGrid grid, int z, int ch, double unit) {

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int inc = max(1, nx / 40);

        GridDataChannel channel = grid.getDataDesc().getChannel(ch);
        for (int y = 0; y < ny; y += inc) {
            for (int x = 0; x < nx; x += inc) {
                double value = channel.getValue(grid.getAttribute(x, y, z));
                printf("%4.1f ", value / unit);
            }
            printf("\n");
        }
    }

    /**
     * listeter for script file changes
     */
    class ScriptFileChangedListener implements ParamChangedListener {
        public void paramChanged(Parameter param) {
            reloadScript();
        }
    } // class ScriptChangedListener
}