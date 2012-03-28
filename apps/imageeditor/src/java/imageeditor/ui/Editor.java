package imageeditor.ui;

import java.util.*;
import javax.swing.*;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.event.*;
import java.io.*;
import abfab3d.creator.GeometryKernel;
import abfab3d.creator.util.ParameterUtil;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.sav.BinaryContentHandler;
import org.web3d.vrml.export.*;

public class Editor extends JFrame implements ActionListener {
JButton submitButton;
JButton uploadButton;
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


    public Editor(String name) { super(name); }
    public static void main(String[] args) {
        Editor editor = new Editor("AbFab3D Image Creator");
        editor.launch();
    }

    public void launch() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setupUI();
        pack();
        setVisible(true);
    }
    public void setupUI() {
        GridLayout layout = new GridLayout(20,3);
        setLayout(layout);

        JLabel step1 = new JLabel("Step: 1");
        getContentPane().add(step1);
        getContentPane().add(new JLabel("Select your Image"));
        getContentPane().add(new JLabel(""));
        JLabel bodyImageLabel = new JLabel("Body Image");
        bodyImageLabel.setToolTipText("The image to use for the front body");
        Font bodyImageFont = bodyImageLabel.getFont();
        bodyImageLabel.setFont(bodyImageFont.deriveFont(bodyImageFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bodyImageLabel);
        String dir = "images/cat.png";
        bodyImageDialog = new JFileChooser(new File(dir));
        bodyImageButton = new JButton("Browse");
        bodyImageButton.addActionListener(this);
        bodyImageEditor = new JTextField("images/cat.png");
        getContentPane().add(bodyImageEditor);
        getContentPane().add(bodyImageButton);

        JLabel bodyImageInvertLabel = new JLabel("Invert Image");
        bodyImageInvertLabel.setToolTipText("Should we use black for cutting");
        Font bodyImageInvertFont = bodyImageInvertLabel.getFont();
        bodyImageInvertLabel.setFont(bodyImageInvertFont.deriveFont(bodyImageInvertFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bodyImageInvertLabel);
        String[] bodyImageInvertEnums = new String[] {
            "true","false"};
        bodyImageInvertEditor = new JComboBox(bodyImageInvertEnums);
        getContentPane().add(bodyImageInvertEditor);
        getContentPane().add(new JLabel(""));

        JLabel bodyImageTypeLabel = new JLabel("Image Mapping Technique");
        bodyImageTypeLabel.setToolTipText("The type of image");
        Font bodyImageTypeFont = bodyImageTypeLabel.getFont();
        bodyImageTypeLabel.setFont(bodyImageTypeFont.deriveFont(bodyImageTypeFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bodyImageTypeLabel);
        String[] bodyImageTypeEnums = new String[] {
            "SQUARE","CIRCULAR"};
        bodyImageTypeEditor = new JComboBox(bodyImageTypeEnums);
        getContentPane().add(bodyImageTypeEditor);
        getContentPane().add(new JLabel(""));

        JLabel bodyImageStyleLabel = new JLabel("Depth Technique");
        bodyImageStyleLabel.setToolTipText("The image operation");
        Font bodyImageStyleFont = bodyImageStyleLabel.getFont();
        bodyImageStyleLabel.setFont(bodyImageStyleFont.deriveFont(bodyImageStyleFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bodyImageStyleLabel);
        String[] bodyImageStyleEnums = new String[] {
            "ENGRAVED","EMBOSSED"};
        bodyImageStyleEditor = new JComboBox(bodyImageStyleEnums);
        getContentPane().add(bodyImageStyleEditor);
        getContentPane().add(new JLabel(""));

        JLabel bodyImageDepthLabel = new JLabel("Depth Amount");
        bodyImageDepthLabel.setToolTipText("The depth of the image");
        Font bodyImageDepthFont = bodyImageDepthLabel.getFont();
        bodyImageDepthLabel.setFont(bodyImageDepthFont.deriveFont(bodyImageDepthFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bodyImageDepthLabel);
        bodyImageDepthEditor = new JTextField("0.0042");
        getContentPane().add(bodyImageDepthEditor);
        getContentPane().add(new JLabel(""));

        getContentPane().add(new JLabel(""));
        getContentPane().add(new JLabel(""));
        getContentPane().add(new JLabel(""));
        JLabel step2 = new JLabel("Step: 2");
        getContentPane().add(step2);
        getContentPane().add(new JLabel("Select your Main shape"));
        getContentPane().add(new JLabel(""));
        JLabel resolutionLabel = new JLabel("Resolution");
        resolutionLabel.setToolTipText("How accurate to model the object");
        Font resolutionFont = resolutionLabel.getFont();
        resolutionLabel.setFont(resolutionFont.deriveFont(resolutionFont.getStyle() ^ Font.BOLD));
        getContentPane().add(resolutionLabel);
        resolutionEditor = new JTextField("0.00018");
        getContentPane().add(resolutionEditor);
        getContentPane().add(new JLabel(""));

        JLabel minWallThicknessLabel = new JLabel("Minimum WallThickness");
        minWallThicknessLabel.setToolTipText("The minimum wallthickness");
        Font minWallThicknessFont = minWallThicknessLabel.getFont();
        minWallThicknessLabel.setFont(minWallThicknessFont.deriveFont(minWallThicknessFont.getStyle() ^ Font.BOLD));
        getContentPane().add(minWallThicknessLabel);
        minWallThicknessEditor = new JTextField("0.003");
        getContentPane().add(minWallThicknessEditor);
        getContentPane().add(new JLabel(""));

        JLabel bodyWidthLabel = new JLabel("Body Width");
        bodyWidthLabel.setToolTipText("The width of the main body");
        Font bodyWidthFont = bodyWidthLabel.getFont();
        bodyWidthLabel.setFont(bodyWidthFont.deriveFont(bodyWidthFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bodyWidthLabel);
        bodyWidthEditor = new JTextField("0.025");
        getContentPane().add(bodyWidthEditor);
        getContentPane().add(new JLabel(""));

        JLabel bodyHeightLabel = new JLabel("Body Height");
        bodyHeightLabel.setToolTipText("The height of the main body");
        Font bodyHeightFont = bodyHeightLabel.getFont();
        bodyHeightLabel.setFont(bodyHeightFont.deriveFont(bodyHeightFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bodyHeightLabel);
        bodyHeightEditor = new JTextField("0.04");
        getContentPane().add(bodyHeightEditor);
        getContentPane().add(new JLabel(""));

        JLabel bodyDepthLabel = new JLabel("Body Depth");
        bodyDepthLabel.setToolTipText("The depth of the main body");
        Font bodyDepthFont = bodyDepthLabel.getFont();
        bodyDepthLabel.setFont(bodyDepthFont.deriveFont(bodyDepthFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bodyDepthLabel);
        bodyDepthEditor = new JTextField("0.0032");
        getContentPane().add(bodyDepthEditor);
        getContentPane().add(new JLabel(""));

        JLabel bodyGeometryLabel = new JLabel("Body Shape");
        bodyGeometryLabel.setToolTipText("The geometry to use for the body");
        Font bodyGeometryFont = bodyGeometryLabel.getFont();
        bodyGeometryLabel.setFont(bodyGeometryFont.deriveFont(bodyGeometryFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bodyGeometryLabel);
        String[] bodyGeometryEnums = new String[] {
            "CUBE","CYLINDER","NONE"};
        bodyGeometryEditor = new JComboBox(bodyGeometryEnums);
        getContentPane().add(bodyGeometryEditor);
        getContentPane().add(new JLabel(""));

        getContentPane().add(new JLabel(""));
        getContentPane().add(new JLabel(""));
        getContentPane().add(new JLabel(""));
        JLabel step3 = new JLabel("Step: 3");
        getContentPane().add(step3);
        getContentPane().add(new JLabel("Add a Bail/Connector"));
        getContentPane().add(new JLabel(""));
        JLabel bailStyleLabel = new JLabel("Connector Style");
        bailStyleLabel.setToolTipText("The connector(bail) to use");
        Font bailStyleFont = bailStyleLabel.getFont();
        bailStyleLabel.setFont(bailStyleFont.deriveFont(bailStyleFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bailStyleLabel);
        String[] bailStyleEnums = new String[] {
            "TORUS","NONE"};
        bailStyleEditor = new JComboBox(bailStyleEnums);
        getContentPane().add(bailStyleEditor);
        getContentPane().add(new JLabel(""));

        JLabel bailInnerRadiusLabel = new JLabel("Connector Inner Radius");
        bailInnerRadiusLabel.setToolTipText("The inner radius of the bail");
        Font bailInnerRadiusFont = bailInnerRadiusLabel.getFont();
        bailInnerRadiusLabel.setFont(bailInnerRadiusFont.deriveFont(bailInnerRadiusFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bailInnerRadiusLabel);
        bailInnerRadiusEditor = new JTextField("0.001");
        getContentPane().add(bailInnerRadiusEditor);
        getContentPane().add(new JLabel(""));

        JLabel bailOuterRadiusLabel = new JLabel("Connector Outer Radius");
        bailOuterRadiusLabel.setToolTipText("The outer radius of the bail");
        Font bailOuterRadiusFont = bailOuterRadiusLabel.getFont();
        bailOuterRadiusLabel.setFont(bailOuterRadiusFont.deriveFont(bailOuterRadiusFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bailOuterRadiusLabel);
        bailOuterRadiusEditor = new JTextField("0.004");
        getContentPane().add(bailOuterRadiusEditor);
        getContentPane().add(new JLabel(""));

        submitButton = new JButton("Submit");
        getContentPane().add(submitButton);
        submitButton.addActionListener(this);
        getContentPane().add(new JLabel(""));
        uploadButton = new JButton("Upload");
        getContentPane().add(uploadButton);
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
                FileOutputStream fos = new FileOutputStream("out.x3db");
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                PlainTextErrorReporter console = new PlainTextErrorReporter();
                BinaryContentHandler writer = (BinaryContentHandler) new X3DBinaryRetainedDirectExporter(bos, 3, 0, console, X3DBinarySerializer.METHOD_FASTEST_PARSING, 0.001f, true);
                kernel.generate(parsed_params, GeometryKernel.Accuracy.PRINT, writer);
                fos.close();
            } catch(IOException ioe) { ioe.printStackTrace(); }
            System.out.println("Model Done");
        } else if (e.getSource() == uploadButton) {
            System.out.println("Uploading Model");
        } else if (e.getSource() == bodyImageButton) {
            int returnVal = bodyImageDialog.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = bodyImageDialog.getSelectedFile();
                 ((JTextField)bodyImageEditor).setText(file.toString());
            }
        }
    }
}
