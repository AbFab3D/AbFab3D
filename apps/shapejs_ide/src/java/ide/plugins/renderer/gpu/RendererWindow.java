/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2019
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package ide.plugins.renderer.gpu;

import abfab3d.param.ParamChangedListener;
import abfab3d.param.Parameter;
import abfab3d.param.ParameterType;
import abfab3d.param.editor.BaseEditor;
import abfab3d.param.editor.EditorFactory;
import abfab3d.param.editor.ParamPanel;
import abfab3d.shapejs.*;
import abfab3d.util.AbFab3DGlobals;
import shapejs.viewer.PickResultListener;
import shapejs.viewer.PickingListener;
import ide.ProjectListener;
import ide.RText;
import ide.RenderEngineListener;
import ide.SaveModelListener;
import ide.ViewListener;

import org.apache.commons.io.FilenameUtils;

import org.apache.commons.io.FileUtils;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.rtextfilechooser.RTextFileChooser;
import org.fife.ui.rtextfilechooser.filters.ExtensionFileFilter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import shapejs.viewer.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
import static abfab3d.core.ResultCodes.RESULT_OK;


/**
 * A dockable window that acts as a renderer.  Includes the Param Editing as well in a SplitPane because the docking
 * system doesn't really support vertical right docking well
 *
 * @author Alan Hudson
 * @version 1.0
 */
class RendererWindow extends DockableWindow implements ProjectListener, RenderEngineListener,
	ActionListener, SaveModelListener, PickingListener, ViewListener {

	private static final int MAX_DIGITS = 6;

	// TODO: Get server from options
	//private String server = "http://localhost:8080";
    private String server = "https://gpu-us-west-2b.shapeways.com";

    //	private Plugin plugin;
    private JSplitPane splitPane;
    private JPanel renderPanel;
    private JPanel renderStatsPanel;
    private Thread renderStatsThread;
    private JLabel fpsLabel;
    private Project currProj;
    private Variant currVariant;
    private Navigator navigator;
    private Navigator objNavigator;
    private RenderCanvas canvas;
    private BaseCustomEditors customEditors;

    private JPanel paramPanel;
    private ParamPanel editor;
    private ScriptParamChangedListener scriptParamListener;
    private JButton saveButton;
    private JButton saveAsButton;
    private JButton resetButton;
    private JButton saveModelButton;
    private JButton saveRenderButton;
    private JPanel buttonPanel;

    private RText app;
    private String engine = "gpu";
    private Gson gson;

    private RenderOptions stillRenderOptions;
    private RenderOptions movingRenderOptions;
    private RenderOptions saveImageRenderOptions;
    private int saveWidth;
    private int saveHeight;
    private int maxWidth = 10000;
    private int maxHeight = 10000;
    private String libPath;

    private String lastSaveModelDir;
    private String lastSaveRenderDir;

    private String jobID;
    private boolean projectUpdated;

    private PickResultListener pickResultListener;

    /** Parameter change that was skipped */
    private Parameter lastSkippedParamChange;


    public RendererWindow(RText app, Plugin plugin, String engine) {
        this.app = app;
        this.engine = engine;
        this.projectUpdated = false;
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        this.stillRenderOptions = new RenderOptions();
        this.movingRenderOptions = new RenderOptions();
        this.saveImageRenderOptions = new RenderOptions();

//		this.plugin = plugin;
        setDockableWindowName(plugin.getString("DockableWindow.Title"));
        setIcon(plugin.getPluginIcon());
        setPosition(DockableWindow.RIGHT);
        setLayout(new BorderLayout());

        // Render Panel
        renderPanel = new JPanel(new BorderLayout());
        renderStatsPanel = new JPanel(new BorderLayout());
        fpsLabel = new JLabel("100 FPS");
        renderStatsPanel.add(fpsLabel, BorderLayout.EAST);
        renderPanel.add(renderStatsPanel, BorderLayout.SOUTH);
        navigator = new ExamineNavigator();
        objNavigator = new ObjectNavigator();

        //setRenderEngine(engine);

/*        customEditors = new CustomEditors(canvas.getCLEnv(), canvas.getComponent(), navigator, objNavigator);
        canvas.setCustomEditors(customEditors);
        EditorFactory.addCreator(customEditors);

        navigator.setEnabled(true);

        Component canvasComp = canvas.getComponent();
        //canvasComp.setPreferredSize(new Dimension(width,height));
        renderPanel.add(canvasComp, BorderLayout.CENTER);
*/
        int cores = Runtime.getRuntime().availableProcessors();
        AbFab3DGlobals.put(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY, new Integer(cores));

        // Param Editing

        paramPanel = new JPanel(new BorderLayout());

        // TODO: Get button names from properties
        saveButton = new JButton("Save Variant");
        saveAsButton = new JButton("Save Variant As");
        resetButton = new JButton("Reset");
        saveModelButton = new JButton("Save Model");
        saveRenderButton = new JButton("Save Render");

        saveButton.addActionListener(this);
        saveAsButton.addActionListener(this);
        resetButton.addActionListener(this);
        saveModelButton.addActionListener(this);
        saveRenderButton.addActionListener(this);

        buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(saveAsButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(saveModelButton);
        buttonPanel.add(saveRenderButton);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, renderPanel, paramPanel);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.25);
        splitPane.setBorder(new EmptyBorder(0,0,0,0));

        add(splitPane);

        setPrimaryComponent(splitPane);

        app.addProjectListener(this);
        app.addRenderEngineListener(this);
        app.addSaveModelListener(this);
        app.addViewListener(this);

        // TODO: Should we move materials public.  Also this contains a csv mapper which I think we want
        //ShapewaysSetup setup = ShapewaysSetup.getInstance();

        if (renderStatsThread == null) {
            renderStatsThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        while(true) {
                            updateRenderStats();

                            try { Thread.sleep(100); } catch(Exception e) {

                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });

            printf("***Starting renderStatsThread\n");
            renderStatsThread.start();
        }
    }

    public void setRenderEngine(String engine) {
        this.engine = engine.toLowerCase();

        Component canvasComp = null;
        if (canvas != null) {
        	canvasComp = canvas.getComponent();
        	if (canvasComp != null) renderPanel.remove(canvasComp);

            navigator = new ExamineNavigator();
            objNavigator = new ObjectNavigator();
            EditorFactory.clearAllCreators();
        }

        printf("RenderEngine is: %s.  server is: %s\n",engine,server);
        String backend = null;
        switch(this.engine) {
            case "gpu":
                backend = "clworkers.ShapeJSExecutorOpenCL";
                //customEditors = new CustomEditors(canvas.getComponent(), navigator, objNavigator);
                break;
            case "network":
                throw new IllegalArgumentException("Need to reimplement");
                //canvas = new RemoteRenderCanvas(navigator, objNavigator, server);
                //customEditors = new CustomEditorsRemote(canvas.getComponent(), navigator, objNavigator, this);
            case "cpu":
                backend = "shapejs.ShapeJSExecutorCpu";
                break;
            default:
                throw new IllegalArgumentException("Unknown render engine: " + engine);

        }
        canvas = new RenderCanvas(navigator,objNavigator,backend);
        customEditors = new CustomEditorsCommandBackend(canvas.getComponent(), navigator, objNavigator, this);

        //canvas.setJobID(jobID);  // TODO:  ADH: I think the underlying canvas should manage this
        canvas.setStillRenderOptions(stillRenderOptions);
        canvas.setMovingRenderOptions(movingRenderOptions);
        canvas.setSaveImageRenderOptions(saveImageRenderOptions);
        canvas.setMaxWidth(maxWidth);
        canvas.setMaxHeight(maxHeight);

        // TODO: ADH: Need to add component listeners at this level
        //canvas.setCustomEditors(customEditors);
        EditorFactory.addCreator(customEditors);

        navigator.setEnabled(true);

        canvasComp = canvas.getComponent();
        renderPanel.add(canvasComp, BorderLayout.CENTER);

    }

    public void setSaveWidth(int saveWidth) {
        this.saveWidth = saveWidth;
    }

    public void setSaveHeight(int saveHeight) {
        this.saveHeight = saveHeight;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        if (canvas != null) {
            canvas.setMaxWidth(maxWidth);
        }
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        if (canvas != null) {
            canvas.setMaxHeight(maxHeight);
        }
    }

    public int getSaveWidth() {
        return saveWidth;
    }

    public int getSaveHeight() {
        return saveHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setShapeJSServer(String server) {
        this.server = server;
        setRenderEngine(engine);
    }

    public String getLibPath() {
        return libPath;
    }

    public String getRenderEngine() {
        return engine;
    }

    public String getShapeJSServer() {
        return server;
    }

    public void libPathChanged(String libPath) {
        this.libPath = libPath;

        app.libPathChanged(libPath);
    }

    public void renderOptionsChanged(
    		Integer movingAaSamples,
    		Quality movingQuality,
    		Quality movingShadowQuality,
    		Integer movingRayBounces,
    		Double movingRenderScale,
    		Integer stillAaSamples,
            Quality stillQuality,
            Quality stillShadowQuality,
            Integer stillRayBounces,
            Double stillRenderScale,
            Integer saveAaSamples,
            Quality saveQuality,
            Quality saveShadowQuality,
            Integer saveRayBounces,
            Integer saveWidth,
            Integer saveHeight,
            Integer maxWidth,
            Integer maxHeight
            ) {

        if (movingAaSamples != null) movingRenderOptions.aaSamples = movingAaSamples;
        if (movingQuality != null) movingRenderOptions.quality = movingQuality;
        if (movingShadowQuality != null) movingRenderOptions.shadowQuality = movingShadowQuality;
        if (movingRayBounces != null) movingRenderOptions.rayBounces = movingRayBounces;
        if (movingRenderScale != null) movingRenderOptions.renderScale = movingRenderScale;

        if (stillAaSamples != null) stillRenderOptions.aaSamples = stillAaSamples;
        if (stillQuality != null) stillRenderOptions.quality = stillQuality;
        if (stillShadowQuality != null) stillRenderOptions.shadowQuality = stillShadowQuality;
        if (stillRayBounces != null) stillRenderOptions.rayBounces = stillRayBounces;
        if (stillRenderScale != null) stillRenderOptions.renderScale = stillRenderScale;

        if (saveAaSamples != null) saveImageRenderOptions.aaSamples = saveAaSamples;
        if (saveQuality != null) saveImageRenderOptions.quality = saveQuality;
        if (saveShadowQuality != null) saveImageRenderOptions.shadowQuality = saveShadowQuality;
        if (saveRayBounces != null) saveImageRenderOptions.rayBounces = saveRayBounces;
        if (saveWidth != null) this.saveWidth = saveWidth;
        if (saveHeight != null) this.saveHeight = saveHeight;

        if (maxWidth != null) setMaxWidth(maxWidth);
        if (maxHeight != null) setMaxHeight(maxHeight);

        if (canvas != null) {
            canvas.forceRender();
        }
    }

    public RenderOptions getStillRenderOptions() {
        return stillRenderOptions;
    }

    public RenderOptions getMovingRenderOptions() {
        return movingRenderOptions;
    }

    public RenderOptions getSaveImageRenderOptions() {
        return saveImageRenderOptions;
    }

    /**
     * Clears any text from all consoles.
     */
    public void clearRenderers() {
    }


    @Override
    public void updateUI() {
    }

    @Override
    public void projectChanged(Project proj) {
        this.currProj = proj;

        /*  ADH: I think the remote backend should manage this shall see
		// New job ID on project load
		jobID = utils.Utils.createShortId();
		canvas.setJobID(jobID);
*/
        // Reset the view point
        resetView(false);

        canvas.clear();
        paramPanel.removeAll();
    }

    public void resourceAdded(ProjectItem res) {
        // A saveVariantAs automatically switches the main app to the new variant
        // Need to update the renderer to the new variant also
        currVariant = app.getCurrVariant();
    }

    public void resourceRemoved(ProjectItem res) {

    }

	@Override
	public void resourceUpdated(ProjectItem res) {

	}

    /**
     * This variant has been selected for running, do it
     * @param variant
     */
    public void runVariant(Variant variant) {
        if (currVariant != null && !currVariant.getScriptName().equals(variant.getScriptName())) {
            // Reset the view point
            resetView(false);
        }

        // Set the current variant and update scene
        currVariant = variant;
        executeProject(currVariant);

        if (editor != null) {
            paramPanel.remove(editor);
            editor.clearParamChangedListeners();
        }

        // Remake the param editor panel
        editor = new ParamPanel(new ParameterizableAdapter(variant.getEvaluatedScript().getScriptParams()));
        paramPanel.add(buttonPanel, BorderLayout.NORTH);
        paramPanel.add(editor, BorderLayout.CENTER);
        scriptParamListener = new ScriptParamChangedListener();
        editor.addParamChangedListener(scriptParamListener);

        // Hack, not sure why this is necessary
        Container c = splitPane.getParent();
        c.revalidate();

    }

    public void executeProject(Variant variant) {
        try {
            updateScene(currVariant);
            /*
            if (canvas instanceof RemoteRenderCanvas) {
                String projZip = null;
                if (projectUpdated) {
                    printf("Project updated. Exporting project for upload.\n");
                    // Zip up the project
                    File zipDir = new File("/tmp/shapejs-ide-remote");
                    if (!zipDir.exists()) {
                        zipDir.mkdirs();
                    }

                    projZip = currProj.exportProject(zipDir);
                    printf("Project exported to: %s\n", zipDir.getAbsolutePath());
                }

                RequestResult result = canvas.executeProject(projZip, variant);
                handleExecuteProjectResponse(variant.getScene(), result, false);

            } else {
                updateScene(currVariant);
            }
            */
        } finally {
            setProjectUpdated(false);
        }
    }

    /**
     * TODO: Move this to GpuRenderCanvas
     * @param variant
     */
    public void updateScene(Variant variant) {
        /*
        if (canvas instanceof RemoteRenderCanvas) {
            printf("Use executeProject for Network renderer!\n");
            return;
        }
*/

        try {
            variant.executeDesign();
        } catch(NotCachedException nce) {
            nce.printStackTrace();
            app.reportVariantError(variant, nce);
            return;
        } catch (Exception e) {
            EvaluatedScript es = variant.getEvaluatedScript();
            String[] prints = es.getPrintLogs();

            if (prints != null && prints.length > 0) {
                StringBuilder print = new StringBuilder();
                print.append("Prints:\n");
                for (int i = 0; i < prints.length; i++) {
                    print.append(prints[i]);
                }
                app.messageReport(print.toString());
            }

            e.printStackTrace();
            app.reportVariantError(variant, e);
            return;
        }

        applySceneUpdate(variant);
    }

    private void applySceneUpdate(Variant variant) {
        Scene scene = variant.getScene();

        if (scene == null) {
            printf("Scene is null?\n");
            return;
        }

        EvaluatedScript es = variant.getEvaluatedScript();

        printLogs(es.getPrintLogs());

        // TODO: ADH: Look at this for remote
        //canvas.updateScene(variant);
        canvas.setScene(scene);

        customEditors.setScene(scene);
        navigator.setBounds(scene.getBounds());

        int w = renderPanel.getWidth();
        int h = renderPanel.getHeight();

        canvas.reshape(w, h);

        // Hack
        Container c = renderPanel.getParent();
        c.revalidate();

        canvas.forceRender();

    }

    private void updateRenderStats() {
        if (canvas == null) return;

        RenderStat stats = canvas.getRenderStat();
        String label = stats.toString(1e-6,"ms", 1e-4,"kb");
        fpsLabel.setText(label);
    }

    private void handleExecuteProjectResponse(Scene scene, RequestResult result, Boolean cached) {
    	handleSceneUpdateResponse(scene, result, cached);
    }

    private void handleSceneUpdateResponse(Scene scene, RequestResult result, Boolean cached) {
        if (!result.success) {
        	String errorStr = "";

        	// TODO: Handle other responds in the 400 range
        	if (result.errorCode == 410) {
        		errorStr = "Failed " + result.endpoint + " call. Job not cached\n";
        		printf("*** %s\n", errorStr);
        		app.errorReport(errorStr, null);

        	} else if (result.errorCode >= 500 && result.errorCode <= 599) {
        		errorStr = "Failed " + result.endpoint + " call. Response code: " + result.errorCode + "\n";
        		if (result.responseData != null) {
        		    errorStr += new String(result.responseData);
        		}
        		printf("*** %s\n", errorStr);
        		app.errorReport(errorStr, null);

        	} else {
            	String responseStr = new String(result.responseData);
            	printf("*** Failed " + result.endpoint + " call:\n    %s\n", responseStr);

            	Map<String, Object> responseMap = gson.fromJson(responseStr, Map.class);
            	List<Map<String, String>> errors = (List<Map<String, String>>) responseMap.get("errors");

            	printErrors(errors);
        	}

        	return;
        }

        String responseStr = new String(result.responseData);
        Map<String, Object> responseMap = gson.fromJson(responseStr, Map.class);
        List<String> prints = (List<String>) responseMap.get("prints");

        printLogs(prints);

        if (!cached) {
            canvas.setScene(scene);

            customEditors.setScene(scene);

            int w = renderPanel.getWidth();
            int h = renderPanel.getHeight();

            canvas.reshape(w, h);

            // Hack
            Container c = renderPanel.getParent();
            c.revalidate();
        }

        canvas.forceRender();
    }

    public void updateSceneCached(Parameter parameter) throws NotCachedException {
        // TODO: ADH: Ignore for now
        /*
        RequestResult result = canvas.updateSceneCached(parameter);

        if (!result.success) {
        	String errorStr = "";

            // TODO: Handle other responds in the 400 range
            if (result.errorCode == 410) {
                throw new NotCachedException();

            } else if (result.errorCode >= 500 && result.errorCode <= 599) {
                errorStr = "Failed " + result.endpoint + " call. Response code: " + result.errorCode + "\n";
                if (result.responseData != null) {
                    errorStr += new String(result.responseData);
                }
                printf("*** %s\n", errorStr);
                app.errorReport(errorStr, null);

            } else {
                String responseStr = new String(result.responseData);
                printf("*** Failed " + result.endpoint + " call:\n    %s\n", responseStr);

                Map<String, Object> responseMap = gson.fromJson(responseStr, Map.class);
                List<Map<String, String>> errors = (List<Map<String, String>>) responseMap.get("errors");

                printErrors(errors);
            }
        } else {
            String responseStr = new String(result.responseData);
            Map<String, Object> responseMap = gson.fromJson(responseStr, Map.class);
            List<String> prints = (List<String>) responseMap.get("prints");
            printLogs(prints);

        	canvas.forceRender();
        }
        */
    }

    public void saveRender() {
        File tmp = new File(lastSaveRenderDir + File.separator + currVariant.getFileName() + ".png");
        final File chosenFile = chooseSaveFile(tmp.getAbsolutePath(), "png");

        if (chosenFile == null) return;

        canvas.setPaused(true);
        app.messageReport("Saving render...");
        app.setBusy();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try (
                        FileOutputStream fos = new FileOutputStream(chosenFile);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                ) {
                    ShapeJSExecutor renderer = canvas.getShapeJSExecutor();



                    String ext = FilenameUtils.getExtension(chosenFile.toString()).toLowerCase();
                    ImageSetup setup = new ImageSetup();
                    ImageSetupUtils.configureSetup(setup,false,saveImageRenderOptions);
                    setup.width = saveWidth;
                    setup.height = saveHeight;
                    renderer.renderImage(canvas.getScene(),canvas.getCamera(),setup,bos,ext);

                    app.messageReport("Saving render done");

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    app.errorReport("Failed to save render to " + chosenFile.getAbsolutePath(), ioe);
                } finally {
                    app.setIdle();

                    canvas.setPaused(false);
                    lastSaveRenderDir = chosenFile.getParent();

                }
            }
        });
    }

    private synchronized void saveVariant() {
    	Variant v = app.getCurrVariant();
    	String relativeScriptPath = "../scripts/" + v.getScriptName() + Project.EXT_SCRIPT;
    	String relativeVariantpath = "variants/" + v.getFileName() + Project.EXT_VARIANT;

    	Map<String, Object> variantParams = new HashMap<String, Object>();
    	Map<String, Object> variantScriptParams = new TreeMap<String, Object>();  // Sort the script params by key
    	Map<String, Parameter> scriptParams = v.getEvaluatedScript().getScriptParams();

    	for (Map.Entry<String, Parameter> entry : scriptParams.entrySet()) {
    		String pname = entry.getKey();
    		Parameter param = entry.getValue();
    		Object pval = param.getValue();

    		if (param.getType() == ParameterType.URI) {
    			String val = (String) pval;
    			if (val == null) continue;

    			if (val.startsWith("http:") || val.startsWith("https:")) {
    				variantScriptParams.put(pname, pval);
    			} else {
    				// If local file path is mapped to a stock urn, keep it in the form of "urn:shapeways:"
    				if (ShapeJSGlobal.getStockUrn(val) != null) {
    					variantScriptParams.put(pname, ShapeJSGlobal.getStockUrn(val));
    				} else {
        				// Value is a local file path, change to relative path from project
        				File varDir = new File(currProj.getParentDir() + "/variants");
        				File uriPath = new File(val);
                        String relativePath =  null;
                        try {
                            relativePath = getRelativePath(varDir, uriPath);
                        } catch(IllegalArgumentException iae) {
                            printf("Cannot find relative path for: %s\n",uriPath);
                            //iae.printStackTrace();
                            relativePath = uriPath.getAbsoluteFile().toPath().toString();
                        }
        				variantScriptParams.put(pname, relativePath);
    				}
    			}

    		} else if (param.getType() == ParameterType.DOUBLE) {
    			double val = round((Double) pval, MAX_DIGITS);
    			variantScriptParams.put(pname, val);

    		} else if (param.getType() == ParameterType.FLOAT) {
    			float val = round((Float) pval, MAX_DIGITS);
    			variantScriptParams.put(pname, val);

    		} else {
    			variantScriptParams.put(pname, pval);
    		}
    	}

    	variantParams.put("scriptPath", FilenameUtils.separatorsToUnix(relativeScriptPath));
    	variantParams.put("scriptParams", variantScriptParams);

    	String prettyJson = gson.toJson(variantParams);
    	File variantFile = new File(v.getPath());

    	try {
    		FileUtils.writeStringToFile(variantFile, prettyJson);
    	} catch (IOException ioe) {
    		printf("*** Error saving variant file: " + variantFile.getAbsolutePath());
    		ioe.printStackTrace();
    	}

    	VariantItem vi = new VariantItem(currProj.getParentDir(), relativeVariantpath,currProj.getParentDir() + File.separator + relativeVariantpath, null);
    	app.updateResource(vi);
    }


    public double round(double val, int digits) {
        String sigDigits = "1";

        for (int i=0; i<digits; i++) {
            sigDigits = sigDigits + "0";
        }

        //TODO: Value can be larger that Double max for large significant digits
        double dig = Double.valueOf(sigDigits);

        return (double)Math.round(val * dig) / dig;
    }

    public static float round(float val, int digits) {
        String sigDigits = "1";

        for (int i=0; i<digits; i++) {
            sigDigits = sigDigits + "0";
        }

        // TODO: Value can be larger that Float max for large significant digits
        float dig = Float.valueOf(sigDigits);

        return (float)(Math.round(val * dig)) / dig;
    }

	/**
	 * Attempts to save the currently active file.  The user will be prompted
	 * for a new file name to save with.
	 *
	 * @return <code>true</code> if the save is successful, <code>false</code>
	 *         if the user cancels the save operation or an IO error occurs.
	 */
    private synchronized boolean saveVariantAs() {
    	Variant v = app.getCurrVariant();
    	String relativeScriptPath = "../scripts/" + v.getScriptName() + Project.EXT_SCRIPT;

    	Map<String, Object> variantParams = new HashMap<String, Object>();
    	Map<String, Object> variantScriptParams = new TreeMap<String, Object>();  // Sort the script params by key
    	Map<String, Parameter> scriptParams = v.getEvaluatedScript().getScriptParams();

    	for (Map.Entry<String, Parameter> entry : scriptParams.entrySet()) {
    		String pname = entry.getKey();
    		Parameter param = entry.getValue();
    		Object pval = param.getValue();

    		if (param.getType() == ParameterType.URI) {
    			String val = (String) pval;

    			if (val == null) continue;

    			if (val.startsWith("http:") || val.startsWith("https:") || val.startsWith("urn:")) {
    				variantScriptParams.put(pname, pval);
    			} else {
    				// If local file path is mapped to a stock urn, keep it in the form of "urn:shapeways:"
    				if (ShapeJSGlobal.getStockUrn(val) != null) {
    					variantScriptParams.put(pname, ShapeJSGlobal.getStockUrn(val));
    				} else {
        				// Value is a local file path, change to relative path from project
        				File varDir = new File(currProj.getParentDir() + "/variants");
        				File uriPath = new File(val);
        				String relativePath =  null;
        				try {
                            relativePath = getRelativePath(varDir, uriPath);
                        } catch(IllegalArgumentException iae) {
        				    printf("Cannot find relative path for: %s\n",uriPath);
        				    //iae.printStackTrace();
        				    relativePath = uriPath.getAbsoluteFile().toPath().toString();
                        }
        				variantScriptParams.put(pname, relativePath);
    				}
    			}

    		} else if (param.getType() == ParameterType.DOUBLE) {
    			double val = round((Double) pval, MAX_DIGITS);
    			variantScriptParams.put(pname, val);

    		} else if (param.getType() == ParameterType.FLOAT) {
    			float val = round((Float) pval, MAX_DIGITS);
    			variantScriptParams.put(pname, val);

    		} else {
    			variantScriptParams.put(pname, pval);
    		}
    	}

    	variantParams.put("scriptPath", relativeScriptPath);
    	variantParams.put("scriptParams", variantScriptParams);
    	String prettyJson = gson.toJson(variantParams);

		File chosenFile = chooseSaveFile(v.getPath(), "shapevar");

		if (chosenFile == null) return false;

    	File variantFile = chosenFile;

    	// Create new file with proper extension if user provided one is incorrect
    	if (!variantFile.getName().endsWith(Project.EXT_VARIANT)) {
    		variantFile = new File(chosenFile.getAbsolutePath() + Project.EXT_VARIANT);
    	}

    	String relativeVariantpath = "variants" + File.separator + variantFile.getName();

    	try {
    		FileUtils.writeStringToFile(variantFile, prettyJson);
    	} catch (IOException ioe) {
    		printf("*** Error saving variant file: " + variantFile.getAbsolutePath());
    		ioe.printStackTrace();
    	}

    	VariantItem vi = new VariantItem(currProj.getParentDir(),relativeVariantpath, currProj.getParentDir() + File.separator + relativeVariantpath, null);
    	app.addResource(vi);

    	return true;
	}

    /**
     * Reset the params in the UI.
     */
	private void resetParams() {
		// TODO: Reset params without having to run variant (which also recreate the param panel)

        app.resetVariant(currVariant.getFileName());
	}

	/**
	 * Reset the view point.
	 */
	private void resetView(boolean updateRender) {
        navigator.reset(updateRender);
        objNavigator.reset(updateRender);
	}

	/**
	 * Choose file for saving.
	 *
	 * @param defaultPath The initial file path
	 * @return
	 */
	private File chooseSaveFile(String defaultPath, String extFilter) {
		//========= Copied dialog from AbstractMainView, saveCurrentFileAs ==========/

		// Get the new filename they'd like to use.
		RTextFileChooser chooser = app.getFileChooser();
		chooser.setMultiSelectionEnabled(false);	// Disable multiple file selection.

		if (defaultPath != null) {
			File initialSelection = new File(defaultPath);
			chooser.setSelectedFile(initialSelection);
		}

		chooser.setOpenedFiles(app.getOpenFiles());

		ExtensionFileFilter filter = new ExtensionFileFilter("File filter", extFilter);
		chooser.setFileFilter(filter);

		int returnVal = chooser.showSaveDialog(app);

		// If they entered a new filename and clicked "OK", save the flie!
		if(returnVal == RTextFileChooser.APPROVE_OPTION) {

			File chosenFile = chooser.getSelectedFile();
			String chosenFileName = chosenFile.getName();
			String chosenFilePath = chosenFile.getAbsolutePath();
			//String encoding = chooser.getEncoding();

			// If the current file filter has an obvious extension
			// associated with it, use it if the specified filename has
			// no extension.  Get the extension from the filter by
			// checking whether the filter is of the form
			// "Foobar Files (*.foo)", and it if is, use the ".foo"
			// extension.
			String extension = chooser.getFileFilter().getDescription();
			int leftParen = extension.indexOf("(*");
			if (leftParen>-1) {
				int start = leftParen + 2; // Skip "(*".
				int end = extension.indexOf(')', start);
				int comma = extension.indexOf(',', start);
				if (comma>-1 && comma<end)
					end = comma;
				if (end>start+1) { // Ensure a ')' or ',' was found.
					extension = extension.substring(start, end);
					// If the file name they entered has no extension,
					// add this extension to it.
					if (chosenFileName.indexOf('.')==-1) {
						chosenFileName = chosenFileName + extension;
						chosenFilePath = chosenFilePath + extension;
						chosenFile = new File(chosenFilePath);
					}
				}
			}

			// If the file already exists, prompt them to see whether
			// or not they want to overwrite it.
			if (chosenFile.exists()) {
				String temp = app.getString("FileAlreadyExists", chosenFile.getName());
				if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(
						app, temp, app.getString("ConfDialogTitle"),
						JOptionPane.YES_NO_OPTION)) {
					return null;
				}
			}

			return chosenFile;
		}

		return null;
	}

    /**
     * processor of ParamChangedListener events
     * coming from script params editor
     */
    class ScriptParamChangedListener implements ParamChangedListener {

        public ScriptParamChangedListener() {
        }

        @Override
        public void paramChanged(final Parameter parameter) {
/*
            if (parameter.getType() == ParameterType.URI) {
                m_diskWatcher.updateParam(parameter);
            }
*/
            lastSkippedParamChange = null;

            // Side pocket the parameter change if system is busy
            if (app.isBusy()) {
                lastSkippedParamChange = parameter;
                return;
            }

            app.setBusy();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        try {
                            if (currVariant.onScriptParamChanged(parameter) == RESULT_OK) {
                                applySceneUpdate(currVariant);
                            } else {
                                app.reportVariantError(currVariant, null);
                            }
                        } catch (NotCachedException nce) {
                            String errorStr = "Failed param change. Job not cached.  Retrying...";
                            printf("*** %s\n", errorStr);
                            app.errorReport(errorStr, null);

                            // Need to run the variant again
                            app.run();
                        }
/*
                        else if (canvas instanceof RemoteRenderCanvas) {
                            //printf("Need to poke the renderer\n");
                            try {
                                updateSceneCached(parameter);
                            } catch (NotCachedException nce) {
                                String errorStr = "Failed updateSceneCached call. Job not cached.\n  Retrying...";
                                printf("*** %s\n", errorStr);
                                app.errorReport(errorStr, null);

                                // Force sending the project zip again
                                setProjectUpdated(true);

                                executeProject(currVariant);

                                // Still need to update the variant with param change
                                try {
                                    // TODO: Will only update the last parameter change. Previous UI changes are missing.
                                    updateSceneCached(parameter);
                                } catch (NotCachedException e) {
                                    // Should not get NotCachedException again after an executeProject
                                    errorStr = "Failed retry. Job still not cached.\n";
                                    printf("*** %s\n", errorStr);
                                    app.errorReport(errorStr, null);
                                }

                            }
                        }
                        */

                    } finally {
                        canvas.setPaused(false);
                        app.setIdle();

                        // Run the side pocketed param change
                        if (lastSkippedParamChange != null) {
                            paramChanged(lastSkippedParamChange);
                        }
                    }
                }
            });
        }
    } // class ScriptParamListener

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch(command) {
            case "Save Variant":
            	saveVariant();
                break;
            case "Save Variant As":
            	saveVariantAs();
                break;
            case "Reset":
            	resetParams();
                break;
            case "Save Model":
            	File tmp = new File(lastSaveModelDir + File.separator + currVariant.getFileName() + ".stl");
        		File chosenFile = chooseSaveFile(tmp.getAbsolutePath(), "stl");
        		saveModel(currVariant, chosenFile, true);
                break;
            case "Save Render":
            	saveRender();
                break;
            default:
            	 abfab3d.core.Output.printf("Unhandled action: %s\n",command);
        }
    }

	@Override
	public void renderEngineChanged(String engine) {
		setRenderEngine(engine);
	}

	@Override
	public void serverChanged(String server) {

	}

    public String getLastSaveModelDir() {
		return lastSaveModelDir;
	}

	public void setLastSaveModelDir(String lastSaveModelDir) {
		this.lastSaveModelDir = lastSaveModelDir;
	}

	public String getLastSaveRenderDir() {
		return lastSaveRenderDir;
	}

	public void setLastSaveRenderDir(String lastSaveRenderDir) {
		this.lastSaveRenderDir = lastSaveRenderDir;
	}

	/**
	 * Relative path from one path to another
	 * @param f1
	 * @param f2
	 * @return
	 */
    private String getRelativePath(File f1, File f2){
        Path p1 = f1.getAbsoluteFile().toPath();
        Path p2 = f2.getAbsoluteFile().toPath();
        //printf("getRelativePath(\'%s\', \'%s\')\n", f1, f2);
        Path prel = p1.relativize(p2);  // Will throw exception if p2 is in another drive
        //File f = prel.toFile();
        //return f.getCanonicalName();
        String str = prel.toString();
        str = str.replace('\\','/');
        //printf("path1: %s, path2: %s rel: %s\n", f1, f2, str);
        return str;
    }

    @Override
    public void reset() {

    }

	@Override
	public void setProjectUpdated(boolean updated) {
		projectUpdated = updated;
	}

	@Override
	public void saveModel(final Variant variant, final File file, final boolean cached) {
		if (file == null) return;

		app.messageReport("Saving model " + variant.getFileName() + "...");
    	app.setBusy();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try (
                        FileOutputStream fos = new FileOutputStream(file);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                ) {
                    ShapeJSExecutor renderer = canvas.getShapeJSExecutor();



                    String ext = FilenameUtils.getExtension(file.toString()).toLowerCase();
                    renderer.renderTriangle(canvas.getScene(),bos,ext);

                    app.messageReport("Saving model done");

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    app.errorReport("Failed to save render to " + file.getAbsolutePath(), ioe);
                } finally {
                    app.setIdle();

                    canvas.setPaused(false);
                    lastSaveModelDir = file.getParent();

                }
            }
        });
	}

	@Override
	public void saveAllVariantModels() {
        /*
		// Choose directory for saving
		final File targetDir = app.chooseDirectory(lastSaveModelDir);

		if (targetDir == null) return;

		app.setBusy();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                List<String> failed = null;

                try {
                    List<VariantItem> variants = currProj.getVariants();
                    ArrayList<String> libDirs = new ArrayList<>();
                    libDirs.add(currProj.getParentDir());
                    libDirs.addAll(app.getLibDirs());

                    if (variants.size() > 0) {
                        failed = new ArrayList<String>();

                        for (VariantItem vi : variants) {
                            Variant v = new Variant();

                            try {
                                v.readDesign(libDirs, vi.getPath(), false);
                                v.setVariantParams(vi.getParams());

                                try {
                                    String path = targetDir.getAbsolutePath() + File.separator + v.getFileName() + ".stl";
                                    canvas.saveModel(v, new File(path), false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } catch(IOException | NotCachedException ioe) {
                                failed.add(v.getFileName());
                                ioe.printStackTrace();
                                app.reportVariantError(v, ioe);
                            } catch (Throwable rt) {
                                failed.add(v.getFileName());
                                app.reportVariantError(v, rt);
                            }
                        }
                    }
                } finally {
                    if (failed.size() == 0) {
                        app.messageReport("All variant models saved successfully.");
                    } else {
                        String msg = "Failed to save the following variant models:\n";
                        for (String vname : failed) {
                            msg += "  " + vname;
                        }
                        app.errorReport(msg, null);
                    }

                    app.setIdle();
                    lastSaveModelDir = targetDir.getAbsolutePath();
                }
            }
        });

*/
	}

    @Override
    public void pickedChanged(BaseEditor editor, int x, int y) {
        pickModel(editor, x, y);
    }

    private void pickModel(BaseEditor editor, int x, int y) {
        /*
        // Only valid for the RemoteRenderCanvas
        if (!(canvas instanceof RemoteRenderCanvas)) return;

        RemoteRenderCanvas rcanvas = (RemoteRenderCanvas) canvas;
        RequestResult result = rcanvas.pickCached(x, y);

        if (!result.success) {
            String responseStr = new String(result.responseData);
            printf("*** Failed pickCached:\n    %s\n", responseStr);

            String errorStr = "";
            Map<String, Object> responseMap = gson.fromJson(responseStr, Map.class);
            List<Map<String, String>> errors = (List<Map<String, String>>) responseMap.get("errors");

            printErrors(errors);

        } else {
            String responseStr = new String(result.responseData);
            Map<String, Object> responseMap = gson.fromJson(responseStr, Map.class);

            if (!validPick(responseMap)) {
                app.errorReport("Invalid pick at pos (" + x + ", " + y + ")\n", null);
                return;
            }

            pickResultListener.pickResultChanged(responseMap);
            canvas.forceRender();
        }
        */
    }

    @Override
    public void setPickResultListener(PickResultListener l) {
        pickResultListener = l;
    }

    private boolean validPick(Map<String, Object> pickResult) {
        List<Double> pos = (List<Double>) pickResult.get("point");
        return (pos.get(0) != -10000);
    }

    @Override
    public void resetView() {
        resetView(true);
    }

    @Override
    public void setBusyMode() {
        //final Cursor scursor = getCursor();
        canvas.setPaused(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    @Override
    public void setIdleMode() {
        //final Cursor scursor = getCursor();
        canvas.setPaused(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void printLogs(String[] logs) {
        if (logs != null && logs.length > 0) {
            StringBuilder sb = new StringBuilder();

            for(int i=0; i < logs.length; i++) {
                sb.append(logs[i]);
            }

            app.messageReport("Prints:");
            app.messageReport(sb.toString());
        }
    }

    private void printLogs(List<String> logs) {
        if (logs != null && logs.size() > 0) {
            StringBuilder sb = new StringBuilder();

            for (String log : logs) {
                sb.append(log);
            }

            app.messageReport("Prints:");
            app.messageReport(sb.toString());
        }
    }

    private void printErrors(List<Map<String, String>> errors) {
        if (errors != null && errors.size() > 0) {
            StringBuilder sb = new StringBuilder();

            for (Map<String, String> emsg : errors) {
                for (String key : emsg.keySet()) {
                    //printf(" map:%d key: %s value: {%s}\n",i, key, map.get(key));
                    sb.append(key + ": " + emsg.get(key));
                }
            }

            app.messageReport("Errors:");
            app.errorReport(sb.toString(), null);
        }
    }
}
