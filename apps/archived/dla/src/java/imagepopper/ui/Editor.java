package imagepopper.ui;

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
    /** The image to use for the front body Field */
    protected String bodyImage;
    /** The image to use for the front body Editor */
    protected JComponent bodyImageEditor;

    protected JButton bodyImageButton;
    protected JFileChooser bodyImageDialog;
    /** The image to use for the front body Field */
    protected String bodyImage2;
    /** The image to use for the front body Editor */
    protected JComponent bodyImage2Editor;

    protected JButton bodyImage2Button;
    protected JFileChooser bodyImage2Dialog;
    /** The depth of the image Field */
    protected String bodyImageDepth2;
    /** The depth of the image Editor */
    protected JComponent bodyImageDepth2Editor;

    /** The type of image Field */
    protected String bodyImageType;
    /** The type of image Editor */
    protected JComponent bodyImageTypeEditor;

    /** The height of the main body Field */
    protected String bodyHeight;
    /** The height of the main body Editor */
    protected JComponent bodyHeightEditor;

    /** The width of the main body Field */
    protected String bodyWidth;
    /** The width of the main body Editor */
    protected JComponent bodyWidthEditor;

    /** Should we use black for cutting Field */
    protected String bodyImageInvert;
    /** Should we use black for cutting Editor */
    protected JComponent bodyImageInvertEditor;

    /** How accurate to model the object Field */
    protected String resolution;
    /** How accurate to model the object Editor */
    protected JComponent resolutionEditor;

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
        GridLayout layout = new GridLayout(15,3);
        setLayout(layout);

        JLabel step1 = new JLabel("Step: 1");
        getContentPane().add(step1);
        getContentPane().add(new JLabel("Select your Image"));
        getContentPane().add(new JLabel(""));
        JLabel bodyImageLabel = new JLabel("Image Layer 1");
        bodyImageLabel.setToolTipText("The image to use for the front body");
        Font bodyImageFont = bodyImageLabel.getFont();
        bodyImageLabel.setFont(bodyImageFont.deriveFont(bodyImageFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bodyImageLabel);
        String dir_bodyImage = "images/leaf/5.png";
        bodyImageDialog = new JFileChooser(new File(dir_bodyImage));
        bodyImageButton = new JButton("Browse");
        bodyImageButton.addActionListener(this);
        bodyImageEditor = new JTextField("images/leaf/5.png");
        getContentPane().add(bodyImageEditor);
        getContentPane().add(bodyImageButton);

        JLabel bodyImage2Label = new JLabel("Image Layer 2");
        bodyImage2Label.setToolTipText("The image to use for the front body");
        Font bodyImage2Font = bodyImage2Label.getFont();
        bodyImage2Label.setFont(bodyImage2Font.deriveFont(bodyImage2Font.getStyle() ^ Font.BOLD));
        getContentPane().add(bodyImage2Label);
        String dir_bodyImage2 = "NONE";
        bodyImage2Dialog = new JFileChooser(new File(dir_bodyImage2));
        bodyImage2Button = new JButton("Browse");
        bodyImage2Button.addActionListener(this);
        bodyImage2Editor = new JTextField("NONE");
        getContentPane().add(bodyImage2Editor);
        getContentPane().add(bodyImage2Button);

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

        JLabel bodyImageDepthLabel = new JLabel("Depth Amount - Layer 1");
        bodyImageDepthLabel.setToolTipText("The depth of the image");
        Font bodyImageDepthFont = bodyImageDepthLabel.getFont();
        bodyImageDepthLabel.setFont(bodyImageDepthFont.deriveFont(bodyImageDepthFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bodyImageDepthLabel);
        bodyImageDepthEditor = new JTextField("0.0013");
        getContentPane().add(bodyImageDepthEditor);
        getContentPane().add(new JLabel(""));

        JLabel bodyImageDepth2Label = new JLabel("Depth Amount - Layer 2");
        bodyImageDepth2Label.setToolTipText("The depth of the image");
        Font bodyImageDepth2Font = bodyImageDepth2Label.getFont();
        bodyImageDepth2Label.setFont(bodyImageDepth2Font.deriveFont(bodyImageDepth2Font.getStyle() ^ Font.BOLD));
        getContentPane().add(bodyImageDepth2Label);
        bodyImageDepth2Editor = new JTextField("0.0008");
        getContentPane().add(bodyImageDepth2Editor);
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
        resolutionEditor = new JTextField("0.000075");
        getContentPane().add(resolutionEditor);
        getContentPane().add(new JLabel(""));

        JLabel bodyWidthLabel = new JLabel("Body Width");
        bodyWidthLabel.setToolTipText("The width of the main body");
        Font bodyWidthFont = bodyWidthLabel.getFont();
        bodyWidthLabel.setFont(bodyWidthFont.deriveFont(bodyWidthFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bodyWidthLabel);
        bodyWidthEditor = new JTextField("0.055330948");
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
            bodyImage = ((JTextField)bodyImageEditor).getText();
            bodyImage2 = ((JTextField)bodyImage2Editor).getText();
            bodyImageDepth2 = ((JTextField)bodyImageDepth2Editor).getText();
            bodyImageType = (String) ((JComboBox)bodyImageTypeEditor).getSelectedItem();
            bodyHeight = ((JTextField)bodyHeightEditor).getText();
            bodyWidth = ((JTextField)bodyWidthEditor).getText();
            bodyImageInvert = (String) ((JComboBox)bodyImageInvertEditor).getSelectedItem();
            resolution = ((JTextField)resolutionEditor).getText();
            bodyImageDepth = ((JTextField)bodyImageDepthEditor).getText();

            dla.SnowFlakeKernel kernel = new dla.SnowFlakeKernel();
            HashMap<String,String> params = new HashMap<String,String>();
            params.put("bodyImage", bodyImage);
            params.put("bodyImage2", bodyImage2);
            params.put("bodyImageDepth2", bodyImageDepth2);
            params.put("bodyImageType", bodyImageType);
            params.put("bodyHeight", bodyHeight);
            params.put("bodyWidth", bodyWidth);
            params.put("bodyImageInvert", bodyImageInvert);
            params.put("resolution", resolution);
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
        } else if (e.getSource() == bodyImage2Button) {
            int returnVal = bodyImage2Dialog.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = bodyImage2Dialog.getSelectedFile();
                 ((JTextField)bodyImage2Editor).setText(file.toString());
            }
        }
    }
}
