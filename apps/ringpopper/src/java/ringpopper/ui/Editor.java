package ringpopper.ui;

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
    /** The ring width(Finger Length) Field */
    protected String ringWidth;
    /** The ring width(Finger Length) Editor */
    protected JComponent ringWidthEditor;

    /** The thickness percent of the ring base Field */
    protected String baseThickness;
    /** The thickness percent of the ring base Editor */
    protected JComponent baseThicknessEditor;

    /** The thickness of the ring Field */
    protected String ringThickness;
    /** The thickness of the ring Editor */
    protected JComponent ringThicknessEditor;

    /** How smooth to make the object Field */
    protected String smoothSteps;
    /** How smooth to make the object Editor */
    protected JComponent smoothStepsEditor;

    /** Whether to put lines on the band Field */
    protected String bandStyle;
    /** Whether to put lines on the band Editor */
    protected JComponent bandStyleEditor;

    /** The tiling along left/right of the ring Field */
    protected String tilingX;
    /** The tiling along left/right of the ring Editor */
    protected JComponent tilingXEditor;

    /** The image to use Field */
    protected String image;
    /** The image to use Editor */
    protected JComponent imageEditor;

    protected JButton imageButton;
    protected JFileChooser imageDialog;
    /** How accurate to model the object Field */
    protected String resolution;
    /** How accurate to model the object Editor */
    protected JComponent resolutionEditor;

    /** The tiling along up/down of the ring Field */
    protected String tilingY;
    /** The tiling along up/down of the ring Editor */
    protected JComponent tilingYEditor;

    /** The inner diameter Field */
    protected String innerDiameter;
    /** The inner diameter Editor */
    protected JComponent innerDiameterEditor;

    /** What material to design for Field */
    protected String material;
    /** What material to design for Editor */
    protected JComponent materialEditor;

    /** The width of the bands Field */
    protected String bandWidth;
    /** The width of the bands Editor */
    protected JComponent bandWidthEditor;


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
        JLabel imageLabel = new JLabel("Image");
        imageLabel.setToolTipText("The image to use");
        Font imageFont = imageLabel.getFont();
        imageLabel.setFont(imageFont.deriveFont(imageFont.getStyle() ^ Font.BOLD));
        getContentPane().add(imageLabel);
        String dir_image = "images/Tile_dilate8_unedged.png";
        imageDialog = new JFileChooser(new File(dir_image));
        imageButton = new JButton("Browse");
        imageButton.addActionListener(this);
        imageEditor = new JTextField("images/Tile_dilate8_unedged.png");
        getContentPane().add(imageEditor);
        getContentPane().add(imageButton);

        JLabel tilingXLabel = new JLabel("Tiling X");
        tilingXLabel.setToolTipText("The tiling along left/right of the ring");
        Font tilingXFont = tilingXLabel.getFont();
        tilingXLabel.setFont(tilingXFont.deriveFont(tilingXFont.getStyle() ^ Font.BOLD));
        getContentPane().add(tilingXLabel);
        tilingXEditor = new JTextField("8");
        getContentPane().add(tilingXEditor);
        getContentPane().add(new JLabel(""));

        JLabel tilingYLabel = new JLabel("Tiling Y");
        tilingYLabel.setToolTipText("The tiling along up/down of the ring");
        Font tilingYFont = tilingYLabel.getFont();
        tilingYLabel.setFont(tilingYFont.deriveFont(tilingYFont.getStyle() ^ Font.BOLD));
        getContentPane().add(tilingYLabel);
        tilingYEditor = new JTextField("1");
        getContentPane().add(tilingYEditor);
        getContentPane().add(new JLabel(""));

        JLabel bandStyleLabel = new JLabel("Band Style");
        bandStyleLabel.setToolTipText("Whether to put lines on the band");
        Font bandStyleFont = bandStyleLabel.getFont();
        bandStyleLabel.setFont(bandStyleFont.deriveFont(bandStyleFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bandStyleLabel);
        String[] bandStyleEnums = new String[] {
            "NONE","TOP","BOTTOM","BOTH"};
        bandStyleEditor = new JComboBox(bandStyleEnums);
        getContentPane().add(bandStyleEditor);
        getContentPane().add(new JLabel(""));

        getContentPane().add(new JLabel(""));
        getContentPane().add(new JLabel(""));
        getContentPane().add(new JLabel(""));
        JLabel step2 = new JLabel("Step: 2");
        getContentPane().add(step2);
        getContentPane().add(new JLabel("Select your size"));
        getContentPane().add(new JLabel(""));
        JLabel innerDiameterLabel = new JLabel("Inner Diameter");
        innerDiameterLabel.setToolTipText("The inner diameter");
        Font innerDiameterFont = innerDiameterLabel.getFont();
        innerDiameterLabel.setFont(innerDiameterFont.deriveFont(innerDiameterFont.getStyle() ^ Font.BOLD));
        getContentPane().add(innerDiameterLabel);
        innerDiameterEditor = new JTextField("0.02118");
        getContentPane().add(innerDiameterEditor);
        getContentPane().add(new JLabel(""));

        JLabel ringWidthLabel = new JLabel("Ring Width");
        ringWidthLabel.setToolTipText("The ring width(Finger Length)");
        Font ringWidthFont = ringWidthLabel.getFont();
        ringWidthLabel.setFont(ringWidthFont.deriveFont(ringWidthFont.getStyle() ^ Font.BOLD));
        getContentPane().add(ringWidthLabel);
        ringWidthEditor = new JTextField("0.005");
        getContentPane().add(ringWidthEditor);
        getContentPane().add(new JLabel(""));

        JLabel ringThicknessLabel = new JLabel("Ring Thickness");
        ringThicknessLabel.setToolTipText("The thickness of the ring");
        Font ringThicknessFont = ringThicknessLabel.getFont();
        ringThicknessLabel.setFont(ringThicknessFont.deriveFont(ringThicknessFont.getStyle() ^ Font.BOLD));
        getContentPane().add(ringThicknessLabel);
        ringThicknessEditor = new JTextField("0.001");
        getContentPane().add(ringThicknessEditor);
        getContentPane().add(new JLabel(""));

        JLabel baseThicknessLabel = new JLabel("Base Thickness");
        baseThicknessLabel.setToolTipText("The thickness percent of the ring base");
        Font baseThicknessFont = baseThicknessLabel.getFont();
        baseThicknessLabel.setFont(baseThicknessFont.deriveFont(baseThicknessFont.getStyle() ^ Font.BOLD));
        getContentPane().add(baseThicknessLabel);
        baseThicknessEditor = new JTextField("0");
        getContentPane().add(baseThicknessEditor);
        getContentPane().add(new JLabel(""));

        JLabel bandWidthLabel = new JLabel("Band Width");
        bandWidthLabel.setToolTipText("The width of the bands");
        Font bandWidthFont = bandWidthLabel.getFont();
        bandWidthLabel.setFont(bandWidthFont.deriveFont(bandWidthFont.getStyle() ^ Font.BOLD));
        getContentPane().add(bandWidthLabel);
        bandWidthEditor = new JTextField("0.001");
        getContentPane().add(bandWidthEditor);
        getContentPane().add(new JLabel(""));

        getContentPane().add(new JLabel(""));
        getContentPane().add(new JLabel(""));
        getContentPane().add(new JLabel(""));
        JLabel step3 = new JLabel("Step: 3");
        getContentPane().add(step3);
        getContentPane().add(new JLabel("Select your Material"));
        getContentPane().add(new JLabel(""));
        JLabel materialLabel = new JLabel("Material");
        materialLabel.setToolTipText("What material to design for");
        Font materialFont = materialLabel.getFont();
        materialLabel.setFont(materialFont.deriveFont(materialFont.getStyle() ^ Font.BOLD));
        getContentPane().add(materialLabel);
        String[] materialEnums = new String[] {
            "White Strong & Flexible","White Strong & Flexible Polished","Silver","Silver Glossy","Stainless Steel","Gold Plated Matte","Gold Plated Glossy","Antique Bronze Matte","Antique Bronze Glossy","Alumide","Polished Alumide"};
        materialEditor = new JComboBox(materialEnums);
        getContentPane().add(materialEditor);
        getContentPane().add(new JLabel(""));

        getContentPane().add(new JLabel(""));
        getContentPane().add(new JLabel(""));
        getContentPane().add(new JLabel(""));
        JLabel step4 = new JLabel("Step: 4");
        getContentPane().add(step4);
        getContentPane().add(new JLabel("Advanced Parameters"));
        getContentPane().add(new JLabel(""));
        JLabel resolutionLabel = new JLabel("Resolution");
        resolutionLabel.setToolTipText("How accurate to model the object");
        Font resolutionFont = resolutionLabel.getFont();
        resolutionLabel.setFont(resolutionFont.deriveFont(resolutionFont.getStyle() ^ Font.BOLD));
        getContentPane().add(resolutionLabel);
        resolutionEditor = new JTextField("0.00006");
        getContentPane().add(resolutionEditor);
        getContentPane().add(new JLabel(""));

        JLabel smoothStepsLabel = new JLabel("Smooth Steps");
        smoothStepsLabel.setToolTipText("How smooth to make the object");
        Font smoothStepsFont = smoothStepsLabel.getFont();
        smoothStepsLabel.setFont(smoothStepsFont.deriveFont(smoothStepsFont.getStyle() ^ Font.BOLD));
        getContentPane().add(smoothStepsLabel);
        smoothStepsEditor = new JTextField("3");
        getContentPane().add(smoothStepsEditor);
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
            ringWidth = ((JTextField)ringWidthEditor).getText();
            baseThickness = ((JTextField)baseThicknessEditor).getText();
            ringThickness = ((JTextField)ringThicknessEditor).getText();
            smoothSteps = ((JTextField)smoothStepsEditor).getText();
            bandStyle = (String) ((JComboBox)bandStyleEditor).getSelectedItem();
            tilingX = ((JTextField)tilingXEditor).getText();
            image = ((JTextField)imageEditor).getText();
            resolution = ((JTextField)resolutionEditor).getText();
            tilingY = ((JTextField)tilingYEditor).getText();
            innerDiameter = ((JTextField)innerDiameterEditor).getText();
            material = (String) ((JComboBox)materialEditor).getSelectedItem();
            bandWidth = ((JTextField)bandWidthEditor).getText();

            ringpopper.RingPopperKernel kernel = new ringpopper.RingPopperKernel();
            HashMap<String,String> params = new HashMap<String,String>();
            params.put("ringWidth", ringWidth);
            params.put("baseThickness", baseThickness);
            params.put("ringThickness", ringThickness);
            params.put("smoothSteps", smoothSteps);
            params.put("bandStyle", bandStyle);
            params.put("tilingX", tilingX);
            params.put("image", image);
            params.put("resolution", resolution);
            params.put("tilingY", tilingY);
            params.put("innerDiameter", innerDiameter);
            params.put("material", material);
            params.put("bandWidth", bandWidth);
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
        } else if (e.getSource() == imageButton) {
            int returnVal = imageDialog.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = imageDialog.getSelectedFile();
                 ((JTextField)imageEditor).setText(file.toString());
            }
        }
    }
}
