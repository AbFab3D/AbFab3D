/*
 * 12/22/2010
 *
 * ConsoleOptionPanel.java - Option panel for managing the Console plugin.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import ide.RTextUtilities;
import org.fife.ui.RColorSwatchesButton;
import org.fife.ui.UIUtil;
import org.fife.ui.app.PluginOptionsDialogPanel;


/**
 * Options panel for managing the Console plugin.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ConsoleOptionPanel extends PluginOptionsDialogPanel
			implements ActionListener, ItemListener, PropertyChangeListener {

	/**
	 * ID used to identify this option panel.
	 */
	public static final String OPTION_PANEL_ID = "ConsoleOptionPanel";

	private JCheckBox visibleCB;
	private JLabel locationLabel;
	private JComboBox locationCombo;
	private JCheckBox messageCB;
	private JCheckBox warningCB;
	private JCheckBox errorCB;
	private RColorSwatchesButton messageButton;
	private RColorSwatchesButton warningButton;
	private RColorSwatchesButton errorButton;
	private JButton defaultsButton;

	private static final String PROPERTY = "Property";


	/**
	 * Constructor.
	 *
	 * @param plugin The plugin.
	 */
	public ConsoleOptionPanel(Plugin plugin) {

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

		// Add the "colors" option panel.
		Container colorsPanel = createColorsPanel();
		topPanel.add(colorsPanel);
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
	 * Called when the user toggles various properties in this panel.
	 *
	 * @param e The event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		Object source = e.getSource();

		if (visibleCB==source) {
			setVisibleCBSelected(visibleCB.isSelected());
			hasUnsavedChanges = true;
			boolean visible = visibleCB.isSelected();
			firePropertyChange(PROPERTY, !visible, visible);
		}

		else if (errorCB==source) {
			boolean selected = errorCB.isSelected();
			errorButton.setEnabled(selected);
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
		}

		else if (messageCB==source) {
			boolean selected = messageCB.isSelected();
			messageButton.setEnabled(selected);
			hasUnsavedChanges = true;
			firePropertyChange(PROPERTY, !selected, selected);
		}

		else if (defaultsButton==source) {
			if (notDefaults()) {
				restoreDefaults();
				hasUnsavedChanges = true;
				firePropertyChange(PROPERTY, false, true);
			}
		}

	}


	/**
	 * Creates the "Colors" section of options for this plugin.
	 *
	 * @return A panel with the "color" options.
	 */
	private Container createColorsPanel() {

		Box temp = Box.createVerticalBox();

		Plugin plugin = (Plugin)getPlugin();
		temp.setBorder(new OptionPanelBorder(
									plugin.getString("Options.Colors")));

		messageCB = createColorActivateCB(plugin.getString("Color.Message"));
		messageButton = createColorSwatchesButton();
		warningCB = createColorActivateCB(plugin.getString("Color.Warning"));
		warningButton = createColorSwatchesButton();
		errorCB = createColorActivateCB(plugin.getString("Color.Error"));
		errorButton = createColorSwatchesButton();

		JPanel sp = new JPanel(new SpringLayout());
		if (getComponentOrientation().isLeftToRight()) {
			sp.add(messageCB);     sp.add(messageButton);
			sp.add(warningCB);     sp.add(warningButton);
			sp.add(errorCB); sp.add(errorButton);
		}
		else {
			sp.add(messageButton);     sp.add(messageCB);
			sp.add(warningButton);     sp.add(warningCB);
			sp.add(errorButton); sp.add(errorCB);
		}
		UIUtil.makeSpringCompactGrid(sp, 3,2, 0,0, 5,5);

		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(sp, BorderLayout.LINE_START);
		temp.add(temp2);
		temp.add(Box.createVerticalGlue());

		return temp;

	}


	/**
	 * Returns a check box used to toggle whether a color in a console uses
	 * a special color.
	 *
	 * @param label The label for the check box.
	 * @return The check box.
	 */
	private JCheckBox createColorActivateCB(String label) {
		JCheckBox cb = new JCheckBox(label);
		cb.addActionListener(this);
		return cb;
	}


	/**
	 * Creates a color picker button we're listening for changes on.
	 *
	 * @return The button.
	 */
	private RColorSwatchesButton createColorSwatchesButton() {
		RColorSwatchesButton button = new RColorSwatchesButton();
		button.addPropertyChangeListener(
				RColorSwatchesButton.COLOR_CHANGED_PROPERTY, this);
		return button;
	}


	/**
	 * Creates the "General" section of options for this plugin.
	 *
	 * @return A panel with the "general" options.
	 */
	private Container createGeneralPanel() {

		Plugin plugin = (Plugin)getPlugin();
		ResourceBundle gpb = ResourceBundle.getBundle(
										"org.fife.ui.app.GUIPlugin");

		Box temp = Box.createVerticalBox();
		temp.setBorder(new OptionPanelBorder(
									plugin.getString("Options.General")));

		// A check box toggling the plugin's visibility.
		visibleCB = new JCheckBox(gpb.getString("Visible"));
		visibleCB.addActionListener(this);
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(visibleCB, BorderLayout.LINE_START);
		temp.add(temp2);
		temp.add(Box.createVerticalStrut(5));

		// A combo in which to select the dockable window's placement.
		Box locationPanel = createHorizontalBox();
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
		locationPanel.add(locationLabel);
		locationPanel.add(Box.createHorizontalStrut(5));
		locationPanel.add(locationCombo);
		locationPanel.add(Box.createHorizontalGlue());
		temp.add(locationPanel);

		temp.add(Box.createVerticalGlue());
		return temp;

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doApplyImpl(Frame owner) {

		Plugin plugin = (Plugin)getPlugin();
		ConsoleWindow window = plugin.getDockableWindow();
		window.setActive(visibleCB.isSelected());
		window.setPosition(locationCombo.getSelectedIndex());

		Color c = errorCB.isSelected() ? errorButton.getColor() : null;
		window.setForeground(ConsoleTextArea.STYLE_ERROR, c);
		c = messageCB.isSelected() ? messageButton.getColor() : null;
		window.setForeground(ConsoleTextArea.STYLE_MESSAGE, c);
		c = warningCB.isSelected() ? warningButton.getColor() : null;
		window.setForeground(ConsoleTextArea.STYLE_WARNING, c);
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
	 * Called when the user changes the desired location of the dockable
	 * window.
	 *
	 * @param e The event.
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource()==locationCombo &&
				e.getStateChange()==ItemEvent.SELECTED) {
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

		boolean isDark = RTextUtilities.isDarkLookAndFeel();
		Color defaultMessage = isDark ? ConsoleTextArea.DEFAULT_MESSAGE_FG :
			ConsoleTextArea.DEFAULT_MESSAGE_FG;
		Color defaultWarning = isDark ? ConsoleTextArea.DEFAULT_WARNING_FG :
			ConsoleTextArea.DEFAULT_WARNING_FG;
		Color defaultError = isDark ? ConsoleTextArea.DEFAULT_ERROR_FG :
				ConsoleTextArea.DEFAULT_ERROR_FG;

		return !visibleCB.isSelected() ||
			locationCombo.getSelectedIndex()!=2 ||
			!defaultMessage.equals(messageButton.getColor()) ||
			!defaultWarning.equals(warningButton.getColor()) ||
			!defaultError.equals(errorButton.getColor()) ||
			!ConsoleTextArea.DEFAULT_ERROR_FG.equals(errorButton.getColor());
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
		hasUnsavedChanges = true;
		firePropertyChange(PROPERTY, false, true);
	}


	/**
	 * Changes all consoles to use the default colors for the current
	 * application theme.
	 */
	private void restoreDefaultColors() {
		Plugin plugin = (Plugin)getPlugin();
		plugin.restoreDefaultColors();
		setValues(((Plugin)getPlugin()).getRText());
	}


	/**
	 * Restores all properties on this panel to their default values.
	 */
	private void restoreDefaults() {

		setVisibleCBSelected(true);
		locationCombo.setSelectedIndex(2);
		messageCB.setSelected(true);
		warningCB.setSelected(true);
		errorCB.setSelected(true);

		boolean isDark = RTextUtilities.isDarkLookAndFeel();
		if (isDark) {
			messageButton.setColor(ConsoleTextArea.DEFAULT_DARK_MESSAGE_FG);
			warningButton.setColor(ConsoleTextArea.DEFAULT_DARK_WARNING_FG);
			errorButton.setColor(ConsoleTextArea.DEFAULT_ERROR_FG);
		}
		else {
			messageButton.setColor(ConsoleTextArea.DEFAULT_MESSAGE_FG);
			warningButton.setColor(ConsoleTextArea.DEFAULT_WARNING_FG);
			errorButton.setColor(ConsoleTextArea.DEFAULT_ERROR_FG);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setValuesImpl(Frame owner) {

		Plugin plugin = (Plugin)getPlugin();
		ConsoleWindow window = plugin.getDockableWindow();
		visibleCB.setSelected(window.isActive());
		locationCombo.setSelectedIndex(window.getPosition());

		messageCB.setSelected(window.isStyleUsed(ConsoleTextArea.STYLE_MESSAGE));
		messageButton.setEnabled(window.isStyleUsed(ConsoleTextArea.STYLE_MESSAGE));
		warningCB.setSelected(window.isStyleUsed(ConsoleTextArea.STYLE_WARNING));
		warningButton.setEnabled(window.isStyleUsed(ConsoleTextArea.STYLE_WARNING));
		errorCB.setSelected(window.isStyleUsed(ConsoleTextArea.STYLE_ERROR));
		errorButton.setEnabled(window.isStyleUsed(ConsoleTextArea.STYLE_ERROR));

		messageButton.setColor(window.getForeground(ConsoleTextArea.STYLE_MESSAGE));
		warningButton.setColor(window.getForeground(ConsoleTextArea.STYLE_WARNING));
		errorButton.setColor(window.getForeground(ConsoleTextArea.STYLE_ERROR));

	}


	private void setVisibleCBSelected(boolean selected) {
		visibleCB.setSelected(selected);
		locationLabel.setEnabled(selected);
		locationCombo.setEnabled(selected);
	}


}