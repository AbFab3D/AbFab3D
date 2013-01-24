package imageeditor.ui;

import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.event.*;
import java.io.*;
import java.util.prefs.*;
import abfab3d.creator.GeometryKernel;
import abfab3d.creator.KernelResults;
import abfab3d.creator.util.ParameterUtil;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.sav.BinaryContentHandler;
import org.web3d.vrml.export.*;
import app.common.*;
import app.common.upload.shapeways.oauth.*;

public class Editor implements ActionListener {
    private static final String LAST_DIR = "LAST_DIR";
    private static final String DEFAULT_DIR = "/tmp";
    private static final String FILE_EXTENSION = "apf";
    private static final int MAX_RECENT_FILES = 4;
    private String fname;
    private JFrame frame;
    private Preferences prefs;
    private RecentFiles recentFiles;
    private JMenuItem[] recentFilesMenuItems;
    JMenu filemenu;
    JButton submitButton;
    JButton printButton;
    JButton uploadButton;
    JMenuItem fileOpen;
    JMenuItem fileSave;
    JFileChooser openDialog;
    JFileChooser saveDialog;
    /** The image operation Field */
    protected String bodyImageStyle;
    /** The image operation Editor */
    protected JComponent bodyImageStyleEditor;

    /** The width of the main body Field */
    protected String bodyWidth;
    /** The width of the main body Editor */
    protected JComponent bodyWidthEditor;

    /** How accurate to model the object Field */
    protected String resolution;
    /** How accurate to model the object Editor */
    protected JComponent resolutionEditor;

    /** The geometry to use for the body Field */
    protected String bodyGeometry;
    /** The geometry to use for the body Editor */
    protected JComponent bodyGeometryEditor;

    /** The minimum wallthickness Field */
    protected String minWallThickness;
    /** The minimum wallthickness Editor */
    protected JComponent minWallThicknessEditor;

    /** The image to use for the front body Field */
    protected String bodyImage;
    /** The image to use for the front body Editor */
    protected JComponent bodyImageEditor;

    protected JButton bodyImageButton;
    protected JFileChooser bodyImageDialog;
    /** The type of image Field */
    protected String bodyImageType;
    /** The type of image Editor */
    protected JComponent bodyImageTypeEditor;

    /** The height of the main body Field */
    protected String bodyHeight;
    /** The height of the main body Editor */
    protected JComponent bodyHeightEditor;

    /** The depth of the main body Field */
    protected String bodyDepth;
    /** The depth of the main body Editor */
    protected JComponent bodyDepthEditor;

    /** The outer radius of the bail Field */
    protected String bailOuterRadius;
    /** The outer radius of the bail Editor */
    protected JComponent bailOuterRadiusEditor;

    /** The inner radius of the bail Field */
    protected String bailInnerRadius;
    /** The inner radius of the bail Editor */
    protected JComponent bailInnerRadiusEditor;

    /** Should we use black for cutting Field */
    protected String bodyImageInvert;
    /** Should we use black for cutting Editor */
    protected JComponent bodyImageInvertEditor;

    /** The connector(bail) to use Field */
    protected String bailStyle;
    /** The connector(bail) to use Editor */
    protected JComponent bailStyleEditor;

    /** The depth of the image Field */
    protected String bodyImageDepth;
    /** The depth of the image Editor */
    protected JComponent bodyImageDepthEditor;


    public Editor(String name) { this.fname = name; }
    public static void main(String[] args) {
        Editor editor = new Editor("AbFab3D Image Creator");
        if (args.length > 0) {
            editor.generate(args[0]);
            return;
        }
        editor.launch();
    }

    public void launch() {
        frame = new JFrame(fname);
        prefs = Preferences.userNodeForPackage(this.getClass());
        recentFiles = new RecentFiles(MAX_RECENT_FILES, prefs);
        recentFilesMenuItems = new JMenuItem[MAX_RECENT_FILES];
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setupUI();
        frame.pack();
        frame.setVisible(true);
    }
    public void setupUI() {
        JMenuBar menubar = new JMenuBar();
        filemenu = new JMenu("File");
        fileOpen = new JMenuItem("Open");
        fileSave = new JMenuItem("Save");
        fileOpen.addActionListener(this);
        fileSave.addActionListener(this);
        filemenu.add(fileOpen);
        filemenu.add(fileSave);
        menubar.add(filemenu);
        frame.setJMenuBar(menubar);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("AbFab3D Parameters File", FILE_EXTENSION);
        openDialog = new JFileChooser(new File(prefs.get(LAST_DIR, DEFAULT_DIR)));
        openDialog.setFileFilter(filter);
        saveDialog = new JFileChooser(new File(prefs.get(LAST_DIR, DEFAULT_DIR)));
        saveDialog.setFileFilter(filter);
        filemenu.addSeparator();
        int size = recentFiles.size();
        for (int i=0; i<size; i++) {
            File f = new File(recentFiles.get(i));
            recentFilesMenuItems[i] = new JMenuItem(f.getAbsolutePath());
            recentFilesMenuItems[i].addActionListener(this);
            filemenu.add(recentFilesMenuItems[i]);
        }
        GridLayout layout = new GridLayout(20,3);
        frame.setLayout(layout);

        JLabel step1 = new JLabel("Step: 1");
        frame.getContentPane().add(step1);
        frame.getContentPane().add(new JLabel("Select your Image"));
        frame.getContentPane().add(new JLabel(""));
        JLabel bodyImageLabel = new JLabel("Body Image");
        bodyImageLabel.setToolTipText("The image to use for the front body");
        Font bodyImageFont = bodyImageLabel.getFont();
        bodyImageLabel.setFont(bodyImageFont.deriveFont(bodyImageFont.getStyle() ^ Font.BOLD));
        frame.getContentPane().add(bodyImageLabel);
        String dir_bodyImage = "images/cat.png";
        bodyImageDialog = new JFileChooser(new File(dir_bodyImage));
        bodyImageButton = new JButton("Browse");
        bodyImageButton.addActionListener(this);
        bodyImageEditor = new JTextField("images/cat.png");
        frame.getContentPane().add(bodyImageEditor);
        frame.getContentPane().add(bodyImageButton);

        JLabel bodyImageInvertLabel = new JLabel("Invert Image");
        bodyImageInvertLabel.setToolTipText("Should we use black for cutting");
        Font bodyImageInvertFont = bodyImageInvertLabel.getFont();
        bodyImageInvertLabel.setFont(bodyImageInvertFont.deriveFont(bodyImageInvertFont.getStyle() ^ Font.BOLD));
        frame.getContentPane().add(bodyImageInvertLabel);
        String[] bodyImageInvertEnums = new String[] {
            "true","false"};
        bodyImageInvertEditor = new JComboBox(bodyImageInvertEnums);
((JComboBox)bodyImageInvertEditor).setSelectedIndex(0);
        frame.getContentPane().add(bodyImageInvertEditor);
        frame.getContentPane().add(new JLabel(""));

        JLabel bodyImageTypeLabel = new JLabel("Image Mapping Technique");
        bodyImageTypeLabel.setToolTipText("The type of image");
        Font bodyImageTypeFont = bodyImageTypeLabel.getFont();
        bodyImageTypeLabel.setFont(bodyImageTypeFont.deriveFont(bodyImageTypeFont.getStyle() ^ Font.BOLD));
        frame.getContentPane().add(bodyImageTypeLabel);
        String[] bodyImageTypeEnums = new String[] {
            "SQUARE","CIRCULAR"};
        bodyImageTypeEditor = new JComboBox(bodyImageTypeEnums);
((JComboBox)bodyImageTypeEditor).setSelectedIndex(0);
        frame.getContentPane().add(bodyImageTypeEditor);
        frame.getContentPane().add(new JLabel(""));

        JLabel bodyImageStyleLabel = new JLabel("Depth Technique");
        bodyImageStyleLabel.setToolTipText("The image operation");
        Font bodyImageStyleFont = bodyImageStyleLabel.getFont();
        bodyImageStyleLabel.setFont(bodyImageStyleFont.deriveFont(bodyImageStyleFont.getStyle() ^ Font.BOLD));
        frame.getContentPane().add(bodyImageStyleLabel);
        String[] bodyImageStyleEnums = new String[] {
            "ENGRAVED","EMBOSSED"};
        bodyImageStyleEditor = new JComboBox(bodyImageStyleEnums);
((JComboBox)bodyImageStyleEditor).setSelectedIndex(0);
        frame.getContentPane().add(bodyImageStyleEditor);
        frame.getContentPane().add(new JLabel(""));

        JLabel bodyImageDepthLabel = new JLabel("Depth Amount");
        bodyImageDepthLabel.setToolTipText("The depth of the image");
        Font bodyImageDepthFont = bodyImageDepthLabel.getFont();
        bodyImageDepthLabel.setFont(bodyImageDepthFont.deriveFont(bodyImageDepthFont.getStyle() ^ Font.BOLD));
        frame.getContentPane().add(bodyImageDepthLabel);
        bodyImageDepthEditor = new JTextField("0.0042");
        frame.getContentPane().add(bodyImageDepthEditor);
        frame.getContentPane().add(new JLabel(""));

        frame.getContentPane().add(new JLabel(""));
        frame.getContentPane().add(new JLabel(""));
        frame.getContentPane().add(new JLabel(""));
        JLabel step2 = new JLabel("Step: 2");
        frame.getContentPane().add(step2);
        frame.getContentPane().add(new JLabel("Select your Main shape"));
        frame.getContentPane().add(new JLabel(""));
        JLabel resolutionLabel = new JLabel("Resolution");
        resolutionLabel.setToolTipText("How accurate to model the object");
        Font resolutionFont = resolutionLabel.getFont();
        resolutionLabel.setFont(resolutionFont.deriveFont(resolutionFont.getStyle() ^ Font.BOLD));
        frame.getContentPane().add(resolutionLabel);
        resolutionEditor = new JTextField("0.00018");
        frame.getContentPane().add(resolutionEditor);
        frame.getContentPane().add(new JLabel(""));

        JLabel minWallThicknessLabel = new JLabel("Minimum WallThickness");
        minWallThicknessLabel.setToolTipText("The minimum wallthickness");
        Font minWallThicknessFont = minWallThicknessLabel.getFont();
        minWallThicknessLabel.setFont(minWallThicknessFont.deriveFont(minWallThicknessFont.getStyle() ^ Font.BOLD));
        frame.getContentPane().add(minWallThicknessLabel);
        minWallThicknessEditor = new JTextField("0.003");
        frame.getContentPane().add(minWallThicknessEditor);
        frame.getContentPane().add(new JLabel(""));

        JLabel bodyWidthLabel = new JLabel("Body Width");
        bodyWidthLabel.setToolTipText("The width of the main body");
        Font bodyWidthFont = bodyWidthLabel.getFont();
        bodyWidthLabel.setFont(bodyWidthFont.deriveFont(bodyWidthFont.getStyle() ^ Font.BOLD));
        frame.getContentPane().add(bodyWidthLabel);
        bodyWidthEditor = new JTextField("0.025");
        frame.getContentPane().add(bodyWidthEditor);
        frame.getContentPane().add(new JLabel(""));

        JLabel bodyHeightLabel = new JLabel("Body Height");
        bodyHeightLabel.setToolTipText("The height of the main body");
        Font bodyHeightFont = bodyHeightLabel.getFont();
        bodyHeightLabel.setFont(bodyHeightFont.deriveFont(bodyHeightFont.getStyle() ^ Font.BOLD));
        frame.getContentPane().add(bodyHeightLabel);
        bodyHeightEditor = new JTextField("0.04");
        frame.getContentPane().add(bodyHeightEditor);
        frame.getContentPane().add(new JLabel(""));

        JLabel bodyDepthLabel = new JLabel("Body Depth");
        bodyDepthLabel.setToolTipText("The depth of the main body");
        Font bodyDepthFont = bodyDepthLabel.getFont();
        bodyDepthLabel.setFont(bodyDepthFont.deriveFont(bodyDepthFont.getStyle() ^ Font.BOLD));
        frame.getContentPane().add(bodyDepthLabel);
        bodyDepthEditor = new JTextField("0.0032");
        frame.getContentPane().add(bodyDepthEditor);
        frame.getContentPane().add(new JLabel(""));

        JLabel bodyGeometryLabel = new JLabel("Body Shape");
        bodyGeometryLabel.setToolTipText("The geometry to use for the body");
        Font bodyGeometryFont = bodyGeometryLabel.getFont();
        bodyGeometryLabel.setFont(bodyGeometryFont.deriveFont(bodyGeometryFont.getStyle() ^ Font.BOLD));
        frame.getContentPane().add(bodyGeometryLabel);
        String[] bodyGeometryEnums = new String[] {
            "CUBE","CYLINDER","NONE"};
        bodyGeometryEditor = new JComboBox(bodyGeometryEnums);
((JComboBox)bodyGeometryEditor).setSelectedIndex(0);
        frame.getContentPane().add(bodyGeometryEditor);
        frame.getContentPane().add(new JLabel(""));

        frame.getContentPane().add(new JLabel(""));
        frame.getContentPane().add(new JLabel(""));
        frame.getContentPane().add(new JLabel(""));
        JLabel step3 = new JLabel("Step: 3");
        frame.getContentPane().add(step3);
        frame.getContentPane().add(new JLabel("Add a Bail/Connector"));
        frame.getContentPane().add(new JLabel(""));
        JLabel bailStyleLabel = new JLabel("Connector Style");
        bailStyleLabel.setToolTipText("The connector(bail) to use");
        Font bailStyleFont = bailStyleLabel.getFont();
        bailStyleLabel.setFont(bailStyleFont.deriveFont(bailStyleFont.getStyle() ^ Font.BOLD));
        frame.getContentPane().add(bailStyleLabel);
        String[] bailStyleEnums = new String[] {
            "TORUS","NONE"};
        bailStyleEditor = new JComboBox(bailStyleEnums);
((JComboBox)bailStyleEditor).setSelectedIndex(0);
        frame.getContentPane().add(bailStyleEditor);
        frame.getContentPane().add(new JLabel(""));

        JLabel bailInnerRadiusLabel = new JLabel("Connector Inner Radius");
        bailInnerRadiusLabel.setToolTipText("The inner radius of the bail");
        Font bailInnerRadiusFont = bailInnerRadiusLabel.getFont();
        bailInnerRadiusLabel.setFont(bailInnerRadiusFont.deriveFont(bailInnerRadiusFont.getStyle() ^ Font.BOLD));
        frame.getContentPane().add(bailInnerRadiusLabel);
        bailInnerRadiusEditor = new JTextField("0.001");
        frame.getContentPane().add(bailInnerRadiusEditor);
        frame.getContentPane().add(new JLabel(""));

        JLabel bailOuterRadiusLabel = new JLabel("Connector Outer Radius");
        bailOuterRadiusLabel.setToolTipText("The outer radius of the bail");
        Font bailOuterRadiusFont = bailOuterRadiusLabel.getFont();
        bailOuterRadiusLabel.setFont(bailOuterRadiusFont.deriveFont(bailOuterRadiusFont.getStyle() ^ Font.BOLD));
        frame.getContentPane().add(bailOuterRadiusLabel);
        bailOuterRadiusEditor = new JTextField("0.004");
        frame.getContentPane().add(bailOuterRadiusEditor);
        frame.getContentPane().add(new JLabel(""));

        submitButton = new JButton("Generate");
        frame.getContentPane().add(submitButton);
        submitButton.addActionListener(this);
        printButton = new JButton("Check Printability");
        frame.getContentPane().add(printButton);
        printButton.addActionListener(this);
        uploadButton = new JButton("Upload");
        frame.getContentPane().add(uploadButton);
        uploadButton.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            // Get all params to global string vars
            bodyImageStyle = (String) ((JComboBox)bodyImageStyleEditor).getSelectedItem();
            bodyWidth = ((JTextField)bodyWidthEditor).getText();
            resolution = ((JTextField)resolutionEditor).getText();
            bodyGeometry = (String) ((JComboBox)bodyGeometryEditor).getSelectedItem();
            minWallThickness = ((JTextField)minWallThicknessEditor).getText();
            bodyImage = ((JTextField)bodyImageEditor).getText();
            bodyImageType = (String) ((JComboBox)bodyImageTypeEditor).getSelectedItem();
            bodyHeight = ((JTextField)bodyHeightEditor).getText();
            bodyDepth = ((JTextField)bodyDepthEditor).getText();
            bailOuterRadius = ((JTextField)bailOuterRadiusEditor).getText();
            bailInnerRadius = ((JTextField)bailInnerRadiusEditor).getText();
            bodyImageInvert = (String) ((JComboBox)bodyImageInvertEditor).getSelectedItem();
            bailStyle = (String) ((JComboBox)bailStyleEditor).getSelectedItem();
            bodyImageDepth = ((JTextField)bodyImageDepthEditor).getText();

            imageeditor.ImageEditorKernel kernel = new imageeditor.ImageEditorKernel();
            HashMap<String,String> params = new HashMap<String,String>();
            params.put("bodyImageStyle", bodyImageStyle);
            params.put("bodyWidth", bodyWidth);
            params.put("resolution", resolution);
            params.put("bodyGeometry", bodyGeometry);
            params.put("minWallThickness", minWallThickness);
            params.put("bodyImage", bodyImage);
            params.put("bodyImageType", bodyImageType);
            params.put("bodyHeight", bodyHeight);
            params.put("bodyDepth", bodyDepth);
            params.put("bailOuterRadius", bailOuterRadius);
            params.put("bailInnerRadius", bailInnerRadius);
            params.put("bodyImageInvert", bodyImageInvert);
            params.put("bailStyle", bailStyle);
            params.put("bodyImageDepth", bodyImageDepth);
            Map<String,Object> parsed_params = ParameterUtil.parseParams(kernel.getParams(), params);
            try {
                String filename = "/tmp/out.x3d";
                FileOutputStream fos = new FileOutputStream(filename);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                PlainTextErrorReporter console = new PlainTextErrorReporter();
                BinaryContentHandler writer = (BinaryContentHandler) new X3DXMLRetainedExporter(fos, 3, 0, console, 6);
                writer.startDocument("","", "utf8", "#X3D", "V3.2", "");
                writer.profileDecl("Immersive");
                KernelResults results = kernel.generate(parsed_params, GeometryKernel.Accuracy.VISUAL, writer);
                writer.startNode("NavigationInfo", null);
                writer.startField("avatarSize");
                writer.fieldValue(new float[] {0.01f, 1.6f, 0.75f}, 3);
                writer.startField("headlight");
                writer.fieldValue(true);
                writer.endNode();
                writer.endDocument();
                fos.close();
                double[] bounds_min = results.getMinBounds();
                double[] bounds_max = results.getMaxBounds();
                double max_axis = Math.max(bounds_max[0] - bounds_min[0], bounds_max[1] - bounds_min[1]);
                max_axis = Math.max(max_axis, bounds_max[2] - bounds_min[2]);
                double z = 2 * max_axis / Math.tan(Math.PI / 4);
                float[] pos = new float[] {0,0,(float) z};
                X3DViewer.viewX3DOM("out.x3d",pos);
            } catch(IOException ioe) { ioe.printStackTrace(); }
            System.out.println("Model Done");
        } else if (e.getSource() == printButton) {
            // Get all params to global string vars
            bodyImageStyle = (String) ((JComboBox)bodyImageStyleEditor).getSelectedItem();
            bodyWidth = ((JTextField)bodyWidthEditor).getText();
            resolution = ((JTextField)resolutionEditor).getText();
            bodyGeometry = (String) ((JComboBox)bodyGeometryEditor).getSelectedItem();
            minWallThickness = ((JTextField)minWallThicknessEditor).getText();
            bodyImage = ((JTextField)bodyImageEditor).getText();
            bodyImageType = (String) ((JComboBox)bodyImageTypeEditor).getSelectedItem();
            bodyHeight = ((JTextField)bodyHeightEditor).getText();
            bodyDepth = ((JTextField)bodyDepthEditor).getText();
            bailOuterRadius = ((JTextField)bailOuterRadiusEditor).getText();
            bailInnerRadius = ((JTextField)bailInnerRadiusEditor).getText();
            bodyImageInvert = (String) ((JComboBox)bodyImageInvertEditor).getSelectedItem();
            bailStyle = (String) ((JComboBox)bailStyleEditor).getSelectedItem();
            bodyImageDepth = ((JTextField)bodyImageDepthEditor).getText();

            imageeditor.ImageEditorKernel kernel = new imageeditor.ImageEditorKernel();
            HashMap<String,String> params = new HashMap<String,String>();
            params.put("bodyImageStyle", bodyImageStyle);
            params.put("bodyWidth", bodyWidth);
            params.put("resolution", resolution);
            params.put("bodyGeometry", bodyGeometry);
            params.put("minWallThickness", minWallThickness);
            params.put("bodyImage", bodyImage);
            params.put("bodyImageType", bodyImageType);
            params.put("bodyHeight", bodyHeight);
            params.put("bodyDepth", bodyDepth);
            params.put("bailOuterRadius", bailOuterRadius);
            params.put("bailInnerRadius", bailInnerRadius);
            params.put("bodyImageInvert", bodyImageInvert);
            params.put("bailStyle", bailStyle);
            params.put("bodyImageDepth", bodyImageDepth);
            Map<String,Object> parsed_params = ParameterUtil.parseParams(kernel.getParams(), params);
            try {
                parsed_params.put("visRemovedRegions",new Boolean(false));
                String filename = "/tmp/out.x3db";
                System.out.println("Outputing analytical file:" + filename);
                FileOutputStream fos = new FileOutputStream(filename);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                PlainTextErrorReporter console = new PlainTextErrorReporter();
                BinaryContentHandler writer = (BinaryContentHandler) new X3DBinaryRetainedDirectExporter(bos, 3, 0, console, X3DBinarySerializer.METHOD_FASTEST_PARSING, 0.001f, true);
                writer.startDocument("","", "utf8", "#X3D", "V3.2", "");
                writer.profileDecl("Immersive");
                KernelResults results = kernel.generate(parsed_params, GeometryKernel.Accuracy.PRINT, writer);
                writer.startNode("NavigationInfo", null);
                writer.startField("avatarSize");
                writer.fieldValue(new float[] {0.01f, 1.6f, 0.75f}, 3);
                writer.startField("headlight");
                writer.fieldValue(true);
                writer.endNode();
                writer.endDocument();
                bos.flush();
                bos.close();
                fos.close();
                kernel = null;
                System.out.println("Clearing memory");
                System.gc();
                System.gc();
                WallThicknessRunner wtr = new WallThicknessRunner();
                String material = (String) parsed_params.get("material");
                WallThicknessResult res = wtr.runWallThickness("/tmp/out.x3db", material);
                double[] bounds_min = results.getMinBounds();
                double[] bounds_max = results.getMaxBounds();
                double max_axis = Math.max(bounds_max[0] - bounds_min[0], bounds_max[1] - bounds_min[1]);
                max_axis = Math.max(max_axis, bounds_max[2] - bounds_min[2]);
                double z = 2 * max_axis / Math.tan(Math.PI / 4);
                float[] pos = new float[] {0,0,(float) z};
                String viz = res.getVisualization();
                if (viz != null) viz = viz.replace("/tmp/","");
                String gap_viz = res.getGapVisualization();
                if (gap_viz != null) gap_viz = gap_viz.replace("/tmp/","");
                int cnt = 0;
                if (viz != null) cnt++;
                if (gap_viz != null) cnt++;
                if (cnt > 0) {String[] files = new String[cnt];
                int idx = 0;
                if (viz != null) files[idx++] = viz;
                if (gap_viz != null) files[idx++] = gap_viz;
                X3DViewer.viewX3DOM(files,pos);
            }} catch(IOException ioe) { ioe.printStackTrace(); }
            System.out.println("Printability Done");
        } else if (e.getSource() == uploadButton) {
            bodyImageStyle = (String) ((JComboBox)bodyImageStyleEditor).getSelectedItem();
            bodyWidth = ((JTextField)bodyWidthEditor).getText();
            resolution = ((JTextField)resolutionEditor).getText();
            bodyGeometry = (String) ((JComboBox)bodyGeometryEditor).getSelectedItem();
            minWallThickness = ((JTextField)minWallThicknessEditor).getText();
            bodyImage = ((JTextField)bodyImageEditor).getText();
            bodyImageType = (String) ((JComboBox)bodyImageTypeEditor).getSelectedItem();
            bodyHeight = ((JTextField)bodyHeightEditor).getText();
            bodyDepth = ((JTextField)bodyDepthEditor).getText();
            bailOuterRadius = ((JTextField)bailOuterRadiusEditor).getText();
            bailInnerRadius = ((JTextField)bailInnerRadiusEditor).getText();
            bodyImageInvert = (String) ((JComboBox)bodyImageInvertEditor).getSelectedItem();
            bailStyle = (String) ((JComboBox)bailStyleEditor).getSelectedItem();
            bodyImageDepth = ((JTextField)bodyImageDepthEditor).getText();

            imageeditor.ImageEditorKernel kernel = new imageeditor.ImageEditorKernel();
            HashMap<String,String> params = new HashMap<String,String>();
            params.put("bodyImageStyle", bodyImageStyle);
            params.put("bodyWidth", bodyWidth);
            params.put("resolution", resolution);
            params.put("bodyGeometry", bodyGeometry);
            params.put("minWallThickness", minWallThickness);
            params.put("bodyImage", bodyImage);
            params.put("bodyImageType", bodyImageType);
            params.put("bodyHeight", bodyHeight);
            params.put("bodyDepth", bodyDepth);
            params.put("bailOuterRadius", bailOuterRadius);
            params.put("bailInnerRadius", bailInnerRadius);
            params.put("bodyImageInvert", bodyImageInvert);
            params.put("bailStyle", bailStyle);
            params.put("bodyImageDepth", bodyImageDepth);
            Map<String,Object> parsed_params = ParameterUtil.parseParams(kernel.getParams(), params);
            try {
                System.out.println("Generating Model");
                String filename = "/tmp/out.x3db";
                FileOutputStream fos = new FileOutputStream(filename);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                PlainTextErrorReporter console = new PlainTextErrorReporter();
                BinaryContentHandler writer = (BinaryContentHandler) new X3DBinaryRetainedDirectExporter(bos, 3, 0, console, X3DBinarySerializer.METHOD_FASTEST_PARSING, 0.001f, true);
                writer.startDocument("","", "utf8", "#X3D", "V3.2", "");
                writer.profileDecl("Immersive");
                KernelResults results = kernel.generate(parsed_params, GeometryKernel.Accuracy.PRINT, writer);
                writer.startNode("NavigationInfo", null);
                writer.startField("avatarSize");
                writer.fieldValue(new float[] {0.01f, 1.6f, 0.75f}, 3);
                writer.startField("headlight");
                writer.fieldValue(true);
                writer.endNode();
                writer.endDocument();
                fos.close();
                System.out.println("Uploading Model");
                Integer modelId = null;
                Float scale = 1.0f;
                String title = "Image Popper Model";
                String description = "Generated by the ImagePopper creator";
                Integer isPublic = null;
                Integer viewState = null;
                ModelUploadOauthRunner uploader = new ModelUploadOauthRunner();
                try {
                    String jsonResponse = uploader.uploadModel(filename, modelId, scale, title, description, isPublic, viewState);
                    uploader.isSuccess(jsonResponse);
                } catch(Exception ex) { ex.printStackTrace(); }
            } catch(IOException ioe) { ioe.printStackTrace(); }
        } else if (e.getSource() == fileOpen) {
            String lastDir = prefs.get(LAST_DIR, DEFAULT_DIR);
            openDialog.setCurrentDirectory(new File(lastDir));
            int returnVal = openDialog.showOpenDialog(frame);
            try {
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = openDialog.getSelectedFile();
                    if (selectedFile.exists()) {
                        FileInputStream fis = new FileInputStream(selectedFile);
                        prefs.put(LAST_DIR, selectedFile.getParent());
                        recentFiles.add(selectedFile.getAbsolutePath());
                        for (int i=0; i<recentFilesMenuItems.length; i++) {
                            if (recentFilesMenuItems[i] != null) {
                                filemenu.remove(recentFilesMenuItems[i]);
                                recentFilesMenuItems[i] = null;
                            }
                        }
                        int size = recentFiles.size();
                        for (int i=0; i<size; i++) {
                            File f = new File(recentFiles.get(i));
                            recentFilesMenuItems[i] = new JMenuItem(f.getAbsolutePath());
                            recentFilesMenuItems[i].addActionListener(this);
                            filemenu.add(recentFilesMenuItems[i]);
                        }
                        Properties props = new Properties();
                        props.load(fis);
                        Enumeration en = props.propertyNames();
                        while(en.hasMoreElements()) {
                            String key = (String) en.nextElement();
                            String val = (String) props.getProperty(key);
                            if (key.equals("bodyImageStyle")) {
                                int count = ((JComboBox)bodyImageStyleEditor).getItemCount();
                                for (int i=0; i<count; i++) {
                                    String item = (String) ((JComboBox)bodyImageStyleEditor).getItemAt(i);
                                    if (item.equals(val)) {
                                        ((JComboBox)bodyImageStyleEditor).setSelectedIndex(i);
                                        break;
                                    }
                                }
                            }
                            else if (key.equals("bodyWidth")) {
                                ((JTextField)bodyWidthEditor).setText(val);
                            }
                            else if (key.equals("resolution")) {
                                ((JTextField)resolutionEditor).setText(val);
                            }
                            else if (key.equals("bodyGeometry")) {
                                int count = ((JComboBox)bodyGeometryEditor).getItemCount();
                                for (int i=0; i<count; i++) {
                                    String item = (String) ((JComboBox)bodyGeometryEditor).getItemAt(i);
                                    if (item.equals(val)) {
                                        ((JComboBox)bodyGeometryEditor).setSelectedIndex(i);
                                        break;
                                    }
                                }
                            }
                            else if (key.equals("minWallThickness")) {
                                ((JTextField)minWallThicknessEditor).setText(val);
                            }
                            else if (key.equals("bodyImage")) {
                                ((JTextField)bodyImageEditor).setText(val);
                            }
                            else if (key.equals("bodyImageType")) {
                                int count = ((JComboBox)bodyImageTypeEditor).getItemCount();
                                for (int i=0; i<count; i++) {
                                    String item = (String) ((JComboBox)bodyImageTypeEditor).getItemAt(i);
                                    if (item.equals(val)) {
                                        ((JComboBox)bodyImageTypeEditor).setSelectedIndex(i);
                                        break;
                                    }
                                }
                            }
                            else if (key.equals("bodyHeight")) {
                                ((JTextField)bodyHeightEditor).setText(val);
                            }
                            else if (key.equals("bodyDepth")) {
                                ((JTextField)bodyDepthEditor).setText(val);
                            }
                            else if (key.equals("bailOuterRadius")) {
                                ((JTextField)bailOuterRadiusEditor).setText(val);
                            }
                            else if (key.equals("bailInnerRadius")) {
                                ((JTextField)bailInnerRadiusEditor).setText(val);
                            }
                            else if (key.equals("bodyImageInvert")) {
                                int count = ((JComboBox)bodyImageInvertEditor).getItemCount();
                                for (int i=0; i<count; i++) {
                                    String item = (String) ((JComboBox)bodyImageInvertEditor).getItemAt(i);
                                    if (item.equals(val)) {
                                        ((JComboBox)bodyImageInvertEditor).setSelectedIndex(i);
                                        break;
                                    }
                                }
                            }
                            else if (key.equals("bailStyle")) {
                                int count = ((JComboBox)bailStyleEditor).getItemCount();
                                for (int i=0; i<count; i++) {
                                    String item = (String) ((JComboBox)bailStyleEditor).getItemAt(i);
                                    if (item.equals(val)) {
                                        ((JComboBox)bailStyleEditor).setSelectedIndex(i);
                                        break;
                                    }
                                }
                            }
                            else if (key.equals("bodyImageDepth")) {
                                ((JTextField)bodyImageDepthEditor).setText(val);
                            }
                        }
                    }
                }
            } catch(IOException ioe) { ioe.printStackTrace(); }
        } else if (e.getSource() == fileSave) {
            String lastDir = prefs.get(LAST_DIR, DEFAULT_DIR);
            saveDialog.setCurrentDirectory(new File(lastDir));
            int returnVal = saveDialog.showSaveDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String properties = "";
                String val = null;
                properties += "bodyImageStyle=" + (String) ((JComboBox)bodyImageStyleEditor).getSelectedItem() + "\n";
                val = ((JTextField)bodyWidthEditor).getText().replaceAll("\\\\+", "/");
                properties += "bodyWidth=" + val + "\n";
                val = ((JTextField)resolutionEditor).getText().replaceAll("\\\\+", "/");
                properties += "resolution=" + val + "\n";
                properties += "bodyGeometry=" + (String) ((JComboBox)bodyGeometryEditor).getSelectedItem() + "\n";
                val = ((JTextField)minWallThicknessEditor).getText().replaceAll("\\\\+", "/");
                properties += "minWallThickness=" + val + "\n";
                val = ((JTextField)bodyImageEditor).getText().replaceAll("\\\\+", "/");
                properties += "bodyImage=" + val + "\n";
                properties += "bodyImageType=" + (String) ((JComboBox)bodyImageTypeEditor).getSelectedItem() + "\n";
                val = ((JTextField)bodyHeightEditor).getText().replaceAll("\\\\+", "/");
                properties += "bodyHeight=" + val + "\n";
                val = ((JTextField)bodyDepthEditor).getText().replaceAll("\\\\+", "/");
                properties += "bodyDepth=" + val + "\n";
                val = ((JTextField)bailOuterRadiusEditor).getText().replaceAll("\\\\+", "/");
                properties += "bailOuterRadius=" + val + "\n";
                val = ((JTextField)bailInnerRadiusEditor).getText().replaceAll("\\\\+", "/");
                properties += "bailInnerRadius=" + val + "\n";
                properties += "bodyImageInvert=" + (String) ((JComboBox)bodyImageInvertEditor).getSelectedItem() + "\n";
                properties += "bailStyle=" + (String) ((JComboBox)bailStyleEditor).getSelectedItem() + "\n";
                val = ((JTextField)bodyImageDepthEditor).getText().replaceAll("\\\\+", "/");
                properties += "bodyImageDepth=" + val + "\n";
                File selectedFile = saveDialog.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();
                prefs.put(LAST_DIR, selectedFile.getParent());
                if (!filePath.endsWith("." + FILE_EXTENSION)) {
                    filePath += "." + FILE_EXTENSION;
                }
                recentFiles.add(filePath);
                for (int i=0; i<recentFilesMenuItems.length; i++) {
                    if (recentFilesMenuItems[i] != null) {
                        filemenu.remove(recentFilesMenuItems[i]);
                        recentFilesMenuItems[i] = null;
                    }
                }
                int size = recentFiles.size();
                for (int i=0; i<size; i++) {
                    File f = new File(recentFiles.get(i));
                    recentFilesMenuItems[i] = new JMenuItem(f.getAbsolutePath());
                    recentFilesMenuItems[i].addActionListener(this);
                    filemenu.add(recentFilesMenuItems[i]);
                }
                BufferedWriter bw = null;
                try {
                    FileWriter fw = new FileWriter(new File(filePath));
                    bw = new BufferedWriter(fw);
                    bw.write(properties);
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                } finally {
                    try {
                        if (bw != null) bw.close();
                    } catch (IOException ioe) { }
                }
            }
        } else if (e.getSource() == bodyImageButton) {
            String lastDir = prefs.get("LAST_BODYIMAGE_DIR", DEFAULT_DIR);
            bodyImageDialog.setCurrentDirectory(new File(lastDir));
            int returnVal = bodyImageDialog.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = bodyImageDialog.getSelectedFile();
                if (file.exists()) {
                    prefs.put("LAST_BODYIMAGE_DIR", file.getParent());
                    ((JTextField)bodyImageEditor).setText(file.toString());
                }
            }
        } else {
            for (int h=0; h<recentFilesMenuItems.length; h++) {
                if (recentFilesMenuItems[h] != null) {
                    if (e.getSource() == recentFilesMenuItems[h]) {
                        String filePath = recentFilesMenuItems[h].getText();
                        File selectedFile = new File(filePath);
                        try {
                            if (selectedFile.exists()) {
                                FileInputStream fis = new FileInputStream(selectedFile);
                                recentFiles.add(selectedFile.getAbsolutePath());
                                for (int i=0; i<recentFilesMenuItems.length; i++) {
                                    if (recentFilesMenuItems[i] != null) {
                                        filemenu.remove(recentFilesMenuItems[i]);
                                        recentFilesMenuItems[i] = null;
                                    }
                                }
                                int size = recentFiles.size();
                                for (int i=0; i<size; i++) {
                                    File f = new File(recentFiles.get(i));
                                    recentFilesMenuItems[i] = new JMenuItem(f.getAbsolutePath());
                                    recentFilesMenuItems[i].addActionListener(this);
                                    filemenu.add(recentFilesMenuItems[i]);
                                }
                                Properties props = new Properties();
                                props.load(fis);
                                Enumeration en = props.propertyNames();
                                while(en.hasMoreElements()) {
                                    String key = (String) en.nextElement();
                                    String val = (String) props.getProperty(key);
                                    if (key.equals("bodyImageStyle")) {
                                        int count = ((JComboBox)bodyImageStyleEditor).getItemCount();
                                        for (int i=0; i<count; i++) {
                                            String item = (String) ((JComboBox)bodyImageStyleEditor).getItemAt(i);
                                            if (item.equals(val)) {
                                                ((JComboBox)bodyImageStyleEditor).setSelectedIndex(i);
                                                break;
                                            }
                                        }
                                    }
                                    else if (key.equals("bodyWidth")) {
                                        ((JTextField)bodyWidthEditor).setText(val);
                                    }
                                    else if (key.equals("resolution")) {
                                        ((JTextField)resolutionEditor).setText(val);
                                    }
                                    else if (key.equals("bodyGeometry")) {
                                        int count = ((JComboBox)bodyGeometryEditor).getItemCount();
                                        for (int i=0; i<count; i++) {
                                            String item = (String) ((JComboBox)bodyGeometryEditor).getItemAt(i);
                                            if (item.equals(val)) {
                                                ((JComboBox)bodyGeometryEditor).setSelectedIndex(i);
                                                break;
                                            }
                                        }
                                    }
                                    else if (key.equals("minWallThickness")) {
                                        ((JTextField)minWallThicknessEditor).setText(val);
                                    }
                                    else if (key.equals("bodyImage")) {
                                        ((JTextField)bodyImageEditor).setText(val);
                                    }
                                    else if (key.equals("bodyImageType")) {
                                        int count = ((JComboBox)bodyImageTypeEditor).getItemCount();
                                        for (int i=0; i<count; i++) {
                                            String item = (String) ((JComboBox)bodyImageTypeEditor).getItemAt(i);
                                            if (item.equals(val)) {
                                                ((JComboBox)bodyImageTypeEditor).setSelectedIndex(i);
                                                break;
                                            }
                                        }
                                    }
                                    else if (key.equals("bodyHeight")) {
                                        ((JTextField)bodyHeightEditor).setText(val);
                                    }
                                    else if (key.equals("bodyDepth")) {
                                        ((JTextField)bodyDepthEditor).setText(val);
                                    }
                                    else if (key.equals("bailOuterRadius")) {
                                        ((JTextField)bailOuterRadiusEditor).setText(val);
                                    }
                                    else if (key.equals("bailInnerRadius")) {
                                        ((JTextField)bailInnerRadiusEditor).setText(val);
                                    }
                                    else if (key.equals("bodyImageInvert")) {
                                        int count = ((JComboBox)bodyImageInvertEditor).getItemCount();
                                        for (int i=0; i<count; i++) {
                                            String item = (String) ((JComboBox)bodyImageInvertEditor).getItemAt(i);
                                            if (item.equals(val)) {
                                                ((JComboBox)bodyImageInvertEditor).setSelectedIndex(i);
                                                break;
                                            }
                                        }
                                    }
                                    else if (key.equals("bailStyle")) {
                                        int count = ((JComboBox)bailStyleEditor).getItemCount();
                                        for (int i=0; i<count; i++) {
                                            String item = (String) ((JComboBox)bailStyleEditor).getItemAt(i);
                                            if (item.equals(val)) {
                                                ((JComboBox)bailStyleEditor).setSelectedIndex(i);
                                                break;
                                            }
                                        }
                                    }
                                    else if (key.equals("bodyImageDepth")) {
                                        ((JTextField)bodyImageDepthEditor).setText(val);
                                    }
                                }
                            }
                        } catch(IOException ioe) { ioe.printStackTrace(); }
                    }
                }
            }
        }
    }
    public void generate(String filename) {
        imageeditor.ImageEditorKernel kernel = new imageeditor.ImageEditorKernel();
        try{
        FileInputStream fis = new FileInputStream(filename);
        Properties props = new Properties();
        props.load(fis);
        Enumeration en = props.propertyNames();
        while(en.hasMoreElements()) {
            String key = (String) en.nextElement();
            String val = (String) props.getProperty(key);
            if (key.equals("bodyImageStyle")) {
                                bodyImageStyle = val;
                                }
            else if (key.equals("bodyWidth")) {
                                bodyWidth = val;
                                }
            else if (key.equals("resolution")) {
                                resolution = val;
                                }
            else if (key.equals("bodyGeometry")) {
                                bodyGeometry = val;
                                }
            else if (key.equals("minWallThickness")) {
                                minWallThickness = val;
                                }
            else if (key.equals("bodyImage")) {
                                bodyImage = val;
                                }
            else if (key.equals("bodyImageType")) {
                                bodyImageType = val;
                                }
            else if (key.equals("bodyHeight")) {
                                bodyHeight = val;
                                }
            else if (key.equals("bodyDepth")) {
                                bodyDepth = val;
                                }
            else if (key.equals("bailOuterRadius")) {
                                bailOuterRadius = val;
                                }
            else if (key.equals("bailInnerRadius")) {
                                bailInnerRadius = val;
                                }
            else if (key.equals("bodyImageInvert")) {
                                bodyImageInvert = val;
                                }
            else if (key.equals("bailStyle")) {
                                bailStyle = val;
                                }
            else if (key.equals("bodyImageDepth")) {
                                bodyImageDepth = val;
                                }
                                }
            HashMap<String,String> params = new HashMap<String,String>();
            params.put("bodyImageStyle", bodyImageStyle);
            params.put("bodyWidth", bodyWidth);
            params.put("resolution", resolution);
            params.put("bodyGeometry", bodyGeometry);
            params.put("minWallThickness", minWallThickness);
            params.put("bodyImage", bodyImage);
            params.put("bodyImageType", bodyImageType);
            params.put("bodyHeight", bodyHeight);
            params.put("bodyDepth", bodyDepth);
            params.put("bailOuterRadius", bailOuterRadius);
            params.put("bailInnerRadius", bailInnerRadius);
            params.put("bodyImageInvert", bodyImageInvert);
            params.put("bailStyle", bailStyle);
            params.put("bodyImageDepth", bodyImageDepth);
            Map<String,Object> parsed_params = ParameterUtil.parseParams(kernel.getParams(), params);
                String outfile = "/tmp/out.x3db";
                FileOutputStream fos = new FileOutputStream(outfile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                PlainTextErrorReporter console = new PlainTextErrorReporter();
                BinaryContentHandler writer = (BinaryContentHandler) new X3DBinaryRetainedDirectExporter(bos, 3, 0, console, X3DBinarySerializer.METHOD_FASTEST_PARSING, 0.001f, true);
                KernelResults results = kernel.generate(parsed_params, GeometryKernel.Accuracy.PRINT, writer);
                fos.close();
            } catch(IOException ioe) { ioe.printStackTrace(); }
            System.out.println("Model Done");
        }
}
