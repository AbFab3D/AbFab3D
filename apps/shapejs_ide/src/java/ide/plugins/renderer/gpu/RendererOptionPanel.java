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
package ide.plugins.renderer.gpu;

import abfab3d.shapejs.Quality;
import abfab3d.shapejs.RenderOptions;
import abfab3d.shapejs.ShapeJSExecutorImpl;
import ide.RText;
import org.fife.ui.UIUtil;
import org.fife.ui.app.PluginOptionsDialogPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.ResourceBundle;


/**
 * Options panel for managing the Renderer plugin.
 *
 * @author Alan Hudson
 * @version 1.0
 */
public class RendererOptionPanel extends PluginOptionsDialogPanel
        implements ActionListener, ItemListener, PropertyChangeListener {

    /**
     * ID used to identify this option panel.
     */
    public static final String OPTION_PANEL_ID = "RendererGpuOptionPanel";

    private final int[] aaSamples = {1, 2, 3, 4, 5, 6, 7, 8, 12, 16};
    private final int[] rayBounces = {0, 1, 2, 3, 4, 5, 6, 7};

    private JCheckBox visibleCB;
    private JLabel locationLabel;
    private JComboBox locationCombo;
    private JButton defaultsButton;
    private JLabel renderEngineLabel;
    private JComboBox renderEngineCombo;
    private JComboBox shapeJSServerCombo;

    private JComboBox stillAaCombo;
    private JComboBox stillQualityCombo;
    private JComboBox stillShadowQualityCombo;
    private JComboBox stillRayBouncesCombo;
    private JTextField stillRenderScaleField;

    private JComboBox movingAaCombo;
    private JComboBox movingQualityCombo;
    private JComboBox movingShadowQualityCombo;
    private JComboBox movingRayBouncesCombo;
    private JTextField movingRenderScaleField;

    private ArrayList<JLabel> labels = new ArrayList<>();
    private ArrayList<JComponent> fields = new ArrayList<>();

    private JComboBox saveAaCombo;
    private JComboBox saveQualityCombo;
    private JComboBox saveShadowQualityCombo;
    private JComboBox saveRayBouncesCombo;
    private JTextField saveWidthField;
    private JTextField saveHeightField;

    private JTextField maxWidthField;
    private JTextField maxHeightField;

    private JLabel libPathLabel;
    private JTextField libPathField;

    private static final String PROPERTY = "Property";


    /**
     * Constructor.
     *
     * @param plugin The plugin.
     */
    public RendererOptionPanel(Plugin plugin) {

        super(plugin);
        setId(OPTION_PANEL_ID);
        setName(plugin.getString("Options.Title"));
        setId(OPTION_PANEL_ID);
        ComponentOrientation o = ComponentOrientation.
                getOrientation(getLocale());

        // Set up our border and layout.
        setBorder(UIUtil.getEmpty5Border());
        setLayout(new BorderLayout());
        Box topPanel = Box.createVerticalBox();

        // Add the "general" options panel.
        Container generalPanel = createGeneralPanel();
        topPanel.add(generalPanel);
        topPanel.add(Box.createVerticalStrut(5));

        // Add a "Restore Defaults" button
        defaultsButton = new JButton(plugin.getString("RestoreDefaults"));
        defaultsButton.setActionCommand("RestoreDefaults");
        defaultsButton.addActionListener(this);
        addLeftAligned(topPanel, defaultsButton);

        // Put it all together!
        topPanel.add(Box.createVerticalGlue());
        add(topPanel, BorderLayout.NORTH);
        applyComponentOrientation(o);

    }

    /**
     * Creates the "General" section of options for this plugin.
     *
     * @return A panel with the "general" options.
     */
    private Container createGeneralPanel() {

        Plugin plugin = (Plugin) getPlugin();
        ResourceBundle gpb = ResourceBundle.getBundle(
                "org.fife.ui.app.GUIPlugin");

        Box everything = Box.createVerticalBox();
//		everything.setBorder(new OptionPanelBorder(
//									plugin.getString("Options.General")));

        // A check box toggling the plugin's visibility.
        visibleCB = new JCheckBox(gpb.getString("Visible"));
        visibleCB.addActionListener(this);
        JPanel temp = new JPanel(new BorderLayout());
        temp.setBorder(new OptionPanelBorder(plugin.getString("Options.General")));

        temp.add(visibleCB, BorderLayout.LINE_START);
        everything.add(temp);
        everything.add(Box.createVerticalStrut(5));

        // A combo in which to select the dockable window's placement.
        JPanel springPanel = new JPanel(new SpringLayout());
        temp = new JPanel(new BorderLayout());

        locationCombo = new JComboBox();
        UIUtil.fixComboOrientation(locationCombo);
        locationCombo.addItem(gpb.getString("Location.top"));
        locationCombo.addItem(gpb.getString("Location.left"));
        locationCombo.addItem(gpb.getString("Location.bottom"));
        locationCombo.addItem(gpb.getString("Location.right"));
        locationCombo.addItem(gpb.getString("Location.floating"));
        locationCombo.addItemListener(this);
        locationLabel = new JLabel(gpb.getString("Location.title"));
        locationLabel.setLabelFor(locationCombo);

        // A combo in which to select the dockable window's placement.
        renderEngineCombo = new JComboBox();
        UIUtil.fixComboOrientation(renderEngineCombo);
        String gpuBackend = "clworkers.ShapeJSExecutorOpenCL";

        if (ShapeJSExecutorImpl.exists(gpuBackend)) {
            renderEngineCombo.addItem(plugin.getString("RenderEngine.gpu"));
        }
        renderEngineCombo.addItem(plugin.getString("RenderEngine.cpu"));
        renderEngineCombo.addItem(plugin.getString("RenderEngine.net"));
//		renderEngineCombo.addItemListener(this);
        renderEngineCombo.addActionListener(this);
        renderEngineCombo.setActionCommand("RenderEngine");
        renderEngineLabel = new JLabel(plugin.getString("RenderEngine.title"));
        renderEngineLabel.setLabelFor(renderEngineCombo);

        springPanel.add(locationLabel);
        springPanel.add(locationCombo);
        springPanel.add(renderEngineLabel);
        springPanel.add(renderEngineCombo);

        temp.add(springPanel, BorderLayout.LINE_START);
        UIUtil.makeSpringCompactGrid(springPanel, 2, 2, 8, 0, 5, 5);

        everything.add(temp);

        // Still Render options
        JPanel springPanel2 = new JPanel(new SpringLayout());
        temp = new JPanel(new BorderLayout());
        temp.setBorder(new OptionPanelBorder("Still Render Options:"));

        stillAaCombo = createAAComboBox(plugin, springPanel2, "AntiAlias", "AA.Title");
        stillQualityCombo = createQualityComboBox(plugin, springPanel2, "RenderQuality", "Quality.Title");
        stillShadowQualityCombo = createShadowQualityComboBox(plugin, springPanel2, "ShadowQuality", "ShadowQuality.Title");
        stillRayBouncesCombo = createRayBouncesComboBox(plugin, springPanel2, "RayBounces", "RayBounces.Title");
        stillRenderScaleField = createTextField(plugin, springPanel2, "RenderScale", "RenderScale.Title");

        temp.add(springPanel2, BorderLayout.LINE_START);

        // Arguments (panel, rows,cols, initial-x, initial-y, x-spacing, y-spacing)
        UIUtil.makeSpringCompactGrid(springPanel2, 5, 2, 0, 0, 5, 5);

        everything.add(temp);

        // Still Render options
        JPanel springPanel5 = new JPanel(new SpringLayout());
        temp = new JPanel(new BorderLayout());
        temp.setBorder(new OptionPanelBorder("Moving Render Options:"));

        movingAaCombo = createAAComboBox(plugin, springPanel5, "AntiAlias", "AA.Title");
        movingQualityCombo = createQualityComboBox(plugin, springPanel5, "RenderQuality", "Quality.Title");
        movingShadowQualityCombo = createShadowQualityComboBox(plugin, springPanel5, "ShadowQuality", "ShadowQuality.Title");
        movingRayBouncesCombo = createRayBouncesComboBox(plugin, springPanel5, "RayBounces", "RayBounces.Title");
        movingRenderScaleField = createTextField(plugin, springPanel5, "RenderScale", "RenderScale.Title");

        temp.add(springPanel5, BorderLayout.LINE_START);

        // Arguments (panel, rows,cols, initial-x, initial-y, x-spacing, y-spacing)
        UIUtil.makeSpringCompactGrid(springPanel5, 5, 2, 0, 0, 5, 5);

        everything.add(temp);


        // Save Render options
        JPanel springPanel3 = new JPanel(new SpringLayout());
        temp = new JPanel(new BorderLayout());
        temp.setBorder(new OptionPanelBorder("Save Render Options:"));

        saveAaCombo = createAAComboBox(plugin, springPanel3, "SaveAntiAlias", "AA.Title");
        saveQualityCombo = createQualityComboBox(plugin, springPanel3, "SaveRenderQuality", "Quality.Title");
        saveShadowQualityCombo = createQualityComboBox(plugin, springPanel3, "SaveRenderShadowQuality", "ShadowQuality.Title");
        saveRayBouncesCombo = createRayBouncesComboBox(plugin, springPanel3, "SaveRayBounces", "SaveRayBounces.Title");
        saveWidthField = createTextField(plugin, springPanel3, "SaveWidth", "SaveWidth.Title");
        saveHeightField = createTextField(plugin, springPanel3, "SaveHeight", "SaveHeight.Title");

        temp.add(springPanel3, BorderLayout.LINE_START);

        // Arguments (panel, rows,cols, initial-x, initial-y, x-spacing, y-spacing)
        UIUtil.makeSpringCompactGrid(springPanel3, 6, 2, 0, 0, 5, 5);

        everything.add(temp);

        // Performance options
        JPanel springPanel6 = new JPanel(new SpringLayout());
        temp = new JPanel(new BorderLayout());
        temp.setBorder(new OptionPanelBorder("Performance Options:"));

        maxWidthField = createTextField(plugin, springPanel6, "MaxWidth", "MaxWidth.Title");
        maxHeightField = createTextField(plugin, springPanel6, "MaxHeight", "MaxHeight.Title");

        temp.add(springPanel6, BorderLayout.LINE_START);

        // Arguments (panel, rows,cols, initial-x, initial-y, x-spacing, y-spacing)
        UIUtil.makeSpringCompactGrid(springPanel6, 2, 2, 0, 0, 5, 5);

        everything.add(temp);

        // Libpath options

        JPanel springPanel4 = new JPanel(new SpringLayout());
        temp = new JPanel(new BorderLayout());
        temp.setBorder(new OptionPanelBorder("ShapeJS:"));

        libPathField = new JTextField(60);
        libPathField.addActionListener(this);
        libPathLabel = new JLabel(plugin.getString("LibPath.Title"));
        libPathLabel.setLabelFor(libPathField);

        springPanel4.add(libPathLabel);
        springPanel4.add(libPathField);

        temp.add(springPanel4, BorderLayout.LINE_START);

        // Arguments (panel, rows,cols, initial-x, initial-y, x-spacing, y-spacing)
        UIUtil.makeSpringCompactGrid(springPanel4, 1, 2, 0, 0, 5, 5);

        everything.add(temp);

        everything.add(Box.createVerticalGlue());

        return everything;
    }

    private JComboBox createAAComboBox(Plugin plugin, JPanel panel, String action, String labelKey) {
        JComboBox aaCombo = new JComboBox();
        UIUtil.fixComboOrientation(aaCombo);

        for (int i = 0; i < aaSamples.length; i++) {
            aaCombo.addItem(aaSamples[i]);
        }

        aaCombo.addActionListener(this);
        aaCombo.setActionCommand(action);
        JLabel aaLabel = new JLabel(plugin.getString(labelKey));
        aaLabel.setLabelFor(aaCombo);
        labels.add(aaLabel);
        fields.add(aaCombo);

        panel.add(aaLabel);
        panel.add(aaCombo);

        return aaCombo;
    }

    private JComboBox createQualityComboBox(Plugin plugin, JPanel panel, String action, String labelKey) {
        JComboBox qualityCombo = new JComboBox();
        UIUtil.fixComboOrientation(qualityCombo);

        qualityCombo.addItem(plugin.getString("Quality.Draft"));
        qualityCombo.addItem(plugin.getString("Quality.Normal"));
        qualityCombo.addItem(plugin.getString("Quality.Fine"));
        qualityCombo.addItem(plugin.getString("Quality.SuperFine"));

        qualityCombo.addActionListener(this);
        qualityCombo.setActionCommand(action);
        JLabel qualityLabel = new JLabel(plugin.getString(labelKey));
        qualityLabel.setLabelFor(qualityCombo);
        labels.add(qualityLabel);
        fields.add(qualityCombo);

        panel.add(qualityLabel);
        panel.add(qualityCombo);

        return qualityCombo;
    }

    private JComboBox createShadowQualityComboBox(Plugin plugin, JPanel panel, String action, String labelKey) {
        JComboBox qualityCombo = new JComboBox();
        UIUtil.fixComboOrientation(qualityCombo);

        qualityCombo.addItem(plugin.getString("ShadowQuality.Draft"));
        qualityCombo.addItem(plugin.getString("ShadowQuality.Normal"));
        qualityCombo.addItem(plugin.getString("ShadowQuality.Fine"));
        qualityCombo.addItem(plugin.getString("ShadowQuality.SuperFine"));

        qualityCombo.addActionListener(this);
        qualityCombo.setActionCommand(action);
        JLabel qualityLabel = new JLabel(plugin.getString(labelKey));
        qualityLabel.setLabelFor(qualityCombo);
        labels.add(qualityLabel);
        fields.add(qualityCombo);

        panel.add(qualityLabel);
        panel.add(qualityCombo);

        return qualityCombo;
    }

    private JComboBox createRayBouncesComboBox(Plugin plugin, JPanel panel, String action, String labelKey) {
        JComboBox rayBouncesCombo = new JComboBox();
        UIUtil.fixComboOrientation(rayBouncesCombo);

        for (int i = 0; i < rayBounces.length; i++) {
            rayBouncesCombo.addItem(rayBounces[i]);
        }

        rayBouncesCombo.addActionListener(this);
        rayBouncesCombo.setActionCommand("RayBounces");
        JLabel rayBouncesLabel = new JLabel(plugin.getString("RayBounces.Title"));
        rayBouncesLabel.setLabelFor(rayBouncesCombo);
        labels.add(rayBouncesLabel);
        fields.add(rayBouncesCombo);

        panel.add(rayBouncesLabel);
        panel.add(rayBouncesCombo);

        return rayBouncesCombo;
    }

    private JTextField createTextField(Plugin plugin, JPanel panel, String action, String labelKey) {
        JTextField field = new JTextField(6);

        field.addActionListener(this);
        field.setActionCommand(action);
        JLabel label = new JLabel(plugin.getString(labelKey));
        label.setLabelFor(field);
        labels.add(label);
        fields.add(field);

        panel.add(label);
        panel.add(field);

        return field;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doApplyImpl(Frame owner) {
        Plugin plugin = (Plugin) getPlugin();
        RendererWindow window = plugin.getDockableWindow();
        window.setActive(visibleCB.isSelected());
        window.setPosition(locationCombo.getSelectedIndex());

        // Set the render engine if changed
        applyRenderEngine(plugin.getRText(), window);

        // Apply rendering option changes
        applyRenderOptions(window);

        // Apply libPath changes
        applyLibPath(window);
    }

    private void applyRenderEngine(RText rtext, RendererWindow window) {
        String curEngine = window.getRenderEngine();
        String newEngine = (String) renderEngineCombo.getSelectedItem();

        if (!newEngine.equals(curEngine)) {
            rtext.renderEngineChanged(newEngine);
        }
    }

    private void applyLibPath(RendererWindow window) {
        String curLibPath = window.getLibPath();
        String newLibPath = libPathField.getText();

        if (!newLibPath.equals(curLibPath)) {
            window.libPathChanged(newLibPath);
        }
    }

    private void applyRenderOptions(RendererWindow window) {
        Integer newStillAA = null;
        Quality newStillQuality = null;
        Quality newStillShadowQuality = null;
        Integer newStillRayBounces = null;
        Double newStillRenderScale = null;

        Integer newMovingAA = null;
        Quality newMovingQuality = null;
        Quality newMovingShadowQuality = null;
        Integer newMovingRayBounces = null;
        Double newMovingRenderScale = null;

        Integer newSaveAA = null;
        Quality newSaveQuality = null;
        Quality newSaveShadowQuality = null;
        Integer newSaveRayBounces = null;
        Integer newSaveWidth = null;
        Integer newSaveHeight = null;

        Integer newMaxHeight = null;
        Integer newMaxWidth = null;


        RenderOptions ro = window.getStillRenderOptions();
        int curStillAA = ro.aaSamples;
        Quality curStillQuality = ro.quality;
        Quality curStillShadowQuality = ro.shadowQuality;
        int curStillRayBounces = ro.rayBounces;
        double curStillRenderScale = ro.renderScale;

        ro = window.getMovingRenderOptions();
        int curMovingAA = ro.aaSamples;
        Quality curMovingQuality = ro.quality;
        Quality curMovingShadowQuality = ro.shadowQuality;
        int curMovingRayBounces = ro.rayBounces;
        double curMovingRenderScale = ro.renderScale;

        RenderOptions savero = window.getSaveImageRenderOptions();
        int curSaveAA = savero.aaSamples;
        Quality curSaveQuality = savero.quality;
        Quality curSaveShadowQuality = savero.shadowQuality;
        int curSaveRayBounces = ro.rayBounces;
        int curSaveWidth = window.getSaveWidth();
        int curSaveHeight = window.getSaveHeight();

        int curMaxWidth = window.getMaxWidth();
        int curMaxHeight = window.getMaxHeight();

        int selStillAA = (Integer) stillAaCombo.getSelectedItem();
        Quality selStillQuality = Quality.valueOf(((String) stillQualityCombo.getSelectedItem()).toUpperCase());
        Quality selStillShadowQuality = Quality.valueOf(((String) stillShadowQualityCombo.getSelectedItem()).toUpperCase());
        int selStillRayBounces = (Integer) stillRayBouncesCombo.getSelectedItem();
        double selStillRenderScale = 1.0;
        try {
            selStillRenderScale = (Double) Double.parseDouble(stillRenderScaleField.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }

        int selMovingAA = (Integer) movingAaCombo.getSelectedItem();
        Quality selMovingQuality = Quality.valueOf(((String) movingQualityCombo.getSelectedItem()).toUpperCase());
        Quality selMovingShadowQuality = Quality.valueOf(((String) movingShadowQualityCombo.getSelectedItem()).toUpperCase());
        int selMovingRayBounces = (Integer) movingRayBouncesCombo.getSelectedItem();
        double selMovingRenderScale = 1.0;
        try {
            selMovingRenderScale = (Double) Double.parseDouble(movingRenderScaleField.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }

        int selSaveAA = (Integer) saveAaCombo.getSelectedItem();
        Quality selSaveQuality = Quality.valueOf(((String) saveQualityCombo.getSelectedItem()).toUpperCase());
        Quality selSaveShadowQuality = Quality.valueOf(((String) saveShadowQualityCombo.getSelectedItem()).toUpperCase());
        int selSaveRayBounces = (Integer) saveRayBouncesCombo.getSelectedItem();
        int selSaveWidth = 1024;
        try {
            selSaveWidth = (Integer) Integer.parseInt(saveWidthField.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
        int selSaveHeight = 1024;
        try {
            selSaveHeight = (Integer) Integer.parseInt(saveHeightField.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }

        int selMaxWidth = 10000;
        try {
            selMaxWidth = (Integer) Integer.parseInt(maxWidthField.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
        int selMaxHeight = 10000;
        try {
            selMaxHeight = (Integer) Integer.parseInt(maxHeightField.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean changed = false;

        if (selStillAA != curStillAA) {
            changed = true;
            newStillAA = selStillAA;
        }
        if (selStillQuality != curStillQuality) {
            changed = true;
            newStillQuality = selStillQuality;
        }
        if (selStillShadowQuality != curStillShadowQuality) {
            changed = true;
            newStillShadowQuality = selStillShadowQuality;
        }
        if (selStillRayBounces != curStillRayBounces) {
            changed = true;
            newStillRayBounces = selStillRayBounces;
        }
        if (selStillRenderScale != curStillRenderScale) {
            changed = true;
            newStillRenderScale = selStillRenderScale;
        }

        if (selMovingAA != curMovingAA) {
            changed = true;
            newMovingAA = selMovingAA;
        }
        if (selMovingQuality != curMovingQuality) {
            changed = true;
            newMovingQuality = selMovingQuality;
        }
        if (selMovingShadowQuality != curMovingShadowQuality) {
            changed = true;
            newMovingShadowQuality = selMovingShadowQuality;
        }
        if (selMovingRayBounces != curMovingRayBounces) {
            changed = true;
            newMovingRayBounces = selMovingRayBounces;
        }
        if (selMovingRenderScale != curMovingRenderScale) {
            changed = true;
            newMovingRenderScale = selMovingRenderScale;
        }

        if (selSaveAA != curSaveAA) {
            changed = true;
            newSaveAA = selSaveAA;
        }
        if (selSaveQuality != curSaveQuality) {
            changed = true;
            newSaveQuality = selSaveQuality;
        }
        if (selSaveShadowQuality != curSaveShadowQuality) {
            changed = true;
            newSaveShadowQuality = selSaveShadowQuality;
        }
        if (selSaveRayBounces != curSaveRayBounces) {
            changed = true;
            newSaveRayBounces = selSaveRayBounces;
        }

        if (selSaveWidth != curSaveWidth) {
            changed = true;
            newSaveWidth = selSaveWidth;
        }
        if (selSaveHeight != curSaveHeight) {
            changed = true;
            newSaveHeight = selSaveHeight;
        }

        if (selMaxWidth != curMaxWidth) {
            changed = true;
            newMaxWidth = selMaxWidth;
        }
        if (selMaxHeight != curMaxHeight) {
            changed = true;
            newMaxHeight = selMaxHeight;
        }

        if (changed) {
            window.renderOptionsChanged(newMovingAA, newMovingQuality, newMovingShadowQuality, newMovingRayBounces, newMovingRenderScale,
                    newStillAA, newStillQuality, newStillShadowQuality, newStillRayBounces, newStillRenderScale, newSaveAA, newSaveQuality, newSaveShadowQuality, newSaveRayBounces,
                    newSaveWidth, newSaveHeight, newMaxWidth, newMaxHeight);
        }
    }

    /**
     * Always returns <code>null</code>, as the user cannot enter invalid
     * input on this panel.
     *
     * @return <code>null</code> always.
     */
    @Override
    protected OptionsPanelCheckResult ensureValidInputsImpl() {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent getTopJComponent() {
        return visibleCB;
    }

    /**
     * Called when the user toggles various properties in this panel.
     *
     * @param e The event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        Object source = e.getSource();

        if (visibleCB == source) {
            setVisibleCBSelected(visibleCB.isSelected());
            hasUnsavedChanges = true;
            boolean visible = visibleCB.isSelected();
            firePropertyChange(PROPERTY, !visible, visible);
        } else if (defaultsButton == source) {
            if (notDefaults()) {
                restoreDefaults();
                hasUnsavedChanges = true;
                firePropertyChange(PROPERTY, false, true);
            }
        } else if (renderEngineCombo == source) {
            hasUnsavedChanges = true;
            String engine = (String) renderEngineCombo.getSelectedItem();
            firePropertyChange("RenderEngine", null, engine);
        } else if (stillAaCombo == source) {
            hasUnsavedChanges = true;
            int aa = (Integer) stillAaCombo.getSelectedItem();
            firePropertyChange("StillAntiAlias", -1, aa);
        } else if (stillQualityCombo == source) {
            hasUnsavedChanges = true;
            String quality = (String) stillQualityCombo.getSelectedItem();
            firePropertyChange("StillRenderQuality", -1, quality);
        } else if (stillShadowQualityCombo == source) {
            hasUnsavedChanges = true;
            String quality = (String) stillShadowQualityCombo.getSelectedItem();
            firePropertyChange("StillShadowQuality", -1, quality);
        } else if (stillRayBouncesCombo == source) {
            hasUnsavedChanges = true;
            int rayBounces = (Integer) stillRayBouncesCombo.getSelectedItem();
            firePropertyChange("StillRayBounces", -1, rayBounces);
        } else if (stillRenderScaleField == source) {
            hasUnsavedChanges = true;
            double renderScale = 1.0;
            try {
                renderScale = Double.parseDouble(stillRenderScaleField.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            firePropertyChange("StillRenderScale", -1, renderScale);
        } else if (movingAaCombo == source) {
            hasUnsavedChanges = true;
            int aa = (Integer) movingAaCombo.getSelectedItem();
            firePropertyChange("MovingAntiAlias", -1, aa);
        } else if (movingQualityCombo == source) {
            hasUnsavedChanges = true;
            String quality = (String) movingQualityCombo.getSelectedItem();
            firePropertyChange("MovingRenderQuality", -1, quality);
        } else if (movingShadowQualityCombo == source) {
            hasUnsavedChanges = true;
            String quality = (String) movingShadowQualityCombo.getSelectedItem();
            firePropertyChange("MovingShadowQuality", -1, quality);
        } else if (movingRayBouncesCombo == source) {
            hasUnsavedChanges = true;
            int rayBounces = (Integer) movingRayBouncesCombo.getSelectedItem();
            firePropertyChange("MovingRayBounces", -1, rayBounces);
        } else if (movingRenderScaleField == source) {
            hasUnsavedChanges = true;
            double renderScale = 1.0;
            try {
                renderScale = Double.parseDouble(movingRenderScaleField.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            firePropertyChange("MovingRenderScale", -1, renderScale);
        } else if (saveRayBouncesCombo == source) {
            hasUnsavedChanges = true;
            int rayBounces = (Integer) saveRayBouncesCombo.getSelectedItem();
            firePropertyChange("RayBounces", -1, rayBounces);
        } else if (saveAaCombo == source) {
            hasUnsavedChanges = true;
            int aa = (Integer) saveAaCombo.getSelectedItem();
            firePropertyChange("SaveAntiAlias", -1, aa);
        } else if (saveQualityCombo == source) {
            hasUnsavedChanges = true;
            String quality = (String) saveQualityCombo.getSelectedItem();
            firePropertyChange("RenderQuality", -1, quality);
        } else if (saveShadowQualityCombo == source) {
            hasUnsavedChanges = true;
            String quality = (String) saveShadowQualityCombo.getSelectedItem();
            firePropertyChange("SaveShadowQuality", -1, quality);
        } else if (saveWidthField == source) {
            hasUnsavedChanges = true;
            int maxWidth = (Integer) Integer.parseInt(saveWidthField.getText());
            firePropertyChange("SaveWidth", null, maxWidth);
        } else if (saveHeightField == source) {
            hasUnsavedChanges = true;
            int maxHeight = (Integer) Integer.parseInt(saveHeightField.getText());
            firePropertyChange("SaveHeight", null, maxHeight);
        } else if (libPathField == source) {
            hasUnsavedChanges = true;
            String libPath = (String) libPathField.getText();
            firePropertyChange("LibPath", null, libPath);
        } else if (maxWidthField == source) {
            hasUnsavedChanges = true;
            int maxWidth = (Integer) Integer.parseInt(maxWidthField.getText());
            firePropertyChange("MaxWidth", null, maxWidth);
        } else if (maxHeightField == source) {
            hasUnsavedChanges = true;
            int maxHeight = (Integer) Integer.parseInt(maxHeightField.getText());
            firePropertyChange("MaxHeight", null, maxHeight);
        }

    }

    /**
     * Called when the user changes the desired location of the dockable
     * window.
     *
     * @param e The event.
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == locationCombo && e.getStateChange() == ItemEvent.SELECTED) {
            hasUnsavedChanges = true;
            int placement = locationCombo.getSelectedIndex();
            firePropertyChange(PROPERTY, -1, placement);
        }
    }


    /**
     * Returns whether something on this panel is NOT set to its default value.
     *
     * @return Whether some property in this panel is NOT set to its default
     * value.
     */
    private boolean notDefaults() {

        return !visibleCB.isSelected() ||
                locationCombo.getSelectedIndex() != 2;
    }


    /**
     * Overridden to set all colors to values appropriate for the current Look
     * and Feel.
     *
     * @param event the broadcasted event.
     */
    @Override
    public void optionsEvent(String event) {
        restoreDefaultColors();
        super.optionsEvent(event);
    }


    /**
     * Called when one of our color picker buttons is modified.
     *
     * @param e The event.
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
//		hasUnsavedChanges = true;
//		firePropertyChange(PROPERTY, false, true);
    }


    /**
     * Changes all consoles to use the default colors for the current
     * application theme.
     */
    private void restoreDefaultColors() {
        Plugin plugin = (Plugin) getPlugin();
        setValues(((Plugin) getPlugin()).getRText());
    }


    /**
     * Restores all properties on this panel to their default values.
     */
    private void restoreDefaults() {
        setVisibleCBSelected(true);
        locationCombo.setSelectedIndex(2);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void setValuesImpl(Frame owner) {
        Plugin plugin = (Plugin) getPlugin();
        RendererWindow window = plugin.getDockableWindow();
        visibleCB.setSelected(window.isActive());
        locationCombo.setSelectedIndex(window.getPosition());
        renderEngineCombo.setSelectedItem(window.getRenderEngine());

        RenderOptions ro = window.getStillRenderOptions();
        stillAaCombo.setSelectedItem(ro.aaSamples);
        stillQualityCombo.setSelectedItem(String.valueOf(ro.quality));
        stillShadowQualityCombo.setSelectedItem(String.valueOf(ro.shadowQuality));
        stillRayBouncesCombo.setSelectedItem(ro.rayBounces);
        stillRenderScaleField.setText("" + ro.renderScale);

        ro = window.getMovingRenderOptions();
        movingAaCombo.setSelectedItem(ro.aaSamples);
        movingQualityCombo.setSelectedItem(String.valueOf(ro.quality));
        movingShadowQualityCombo.setSelectedItem(String.valueOf(ro.shadowQuality));
        movingRayBouncesCombo.setSelectedItem(ro.rayBounces);
        movingRenderScaleField.setText("" + ro.renderScale);

        RenderOptions savero = window.getSaveImageRenderOptions();
        saveAaCombo.setSelectedItem(savero.aaSamples);
        saveQualityCombo.setSelectedItem(String.valueOf(savero.quality));
        saveShadowQualityCombo.setSelectedItem(String.valueOf(savero.shadowQuality));
        saveRayBouncesCombo.setSelectedItem(savero.rayBounces);
        saveWidthField.setText("" + window.getSaveWidth());
        saveHeightField.setText("" + window.getSaveHeight());

        maxWidthField.setText("" + window.getMaxWidth());
        maxHeightField.setText("" + window.getMaxHeight());

        libPathField.setText(window.getLibPath());
    }


    private void setVisibleCBSelected(boolean selected) {
        for (JLabel label : labels) {
            label.setEnabled(selected);
        }
        for (JComponent field : fields) {
            field.setEnabled(selected);
        }

        visibleCB.setSelected(selected);

        libPathField.setEditable(selected);
        libPathLabel.setEnabled(selected);
    }
}
