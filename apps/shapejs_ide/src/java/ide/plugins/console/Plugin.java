/*
 * 12/17/2010
 *
 * Plugin.java - Embeds a console-line window in RText.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.console;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import ide.RText;
import ide.RTextMenuBar;
import ide.RTextUtilities;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.GUIPlugin;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.AppAction;


/**
 * A plugin that allows the user to have a command prompt embedded in RText.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class Plugin extends GUIPlugin {

	private static final String VERSION					= "2.6.3";
	private static final String DOCKABLE_WINDOW_CONSOLE	= "consoleDockableWindow";

	private RText app;
	private ConsoleWindow window;
	private Icon icon;

	private static final String MSG = "ide.plugins.console.Plugin";
	protected static final ResourceBundle msg = ResourceBundle.getBundle(MSG);

	private static final String VIEW_CONSOLE_ACTION	= "viewConsoleAction";


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	public Plugin(AbstractPluggableGUIApplication<?> app) {

		this.app = (RText)app;

		// Load the plugin icon.
		URL url = getClass().getResource("monitor.png");
		if (url!=null) { // Should always be true
			try {
				icon = new ImageIcon(ImageIO.read(url));
			} catch (IOException ioe) {
				app.displayException(ioe);
			}
		}

		ConsolePrefs prefs = loadPrefs();

		AppAction<RText> a = new ViewConsoleAction(this.app, msg, this);
		a.setAccelerator(prefs.windowVisibilityAccelerator);
		app.addAction(VIEW_CONSOLE_ACTION, a);

		// Window MUST always be created for preference saving on shutdown
		window = new ConsoleWindow(this.app, this);
		window.setPosition(prefs.windowPosition);
		window.setActive(prefs.windowVisible);
		putDockableWindow(DOCKABLE_WINDOW_CONSOLE, window);

		window.setForeground(ConsoleTextArea.STYLE_MESSAGE, prefs.messageFG);
		window.setForeground(ConsoleTextArea.STYLE_WARNING, prefs.warningFG);
		window.setForeground(ConsoleTextArea.STYLE_ERROR, prefs.errorFG);
	}


	/**
	 * Returns the dockable window containing the consoles.
	 *
	 * @return The dockable window.
	 */
	public ConsoleWindow getDockableWindow() {
		return window;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public PluginOptionsDialogPanel getOptionsDialogPanel() {
		return new ConsoleOptionPanel(this);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPluginAuthor() {
		return "Robert Futrell";
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getPluginIcon() {
		return icon;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPluginName() {
		return msg.getString("Plugin.Name");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPluginVersion() {
		return VERSION;
	}


	/**
	 * Returns the file preferences for this plugin are saved in.
	 *
	 * @return The file.
	 */
	private static final File getPrefsFile() {
		return new File(RTextUtilities.getPreferencesDirectory(),
						"console.properties");
	}


	/**
	 * Returns the parent application.
	 *
	 * @return The parent application.
	 */
	public RText getRText() {
		return app;
	}


	/**
	 * Returns a localized message.
	 *
	 * @param key The key.
	 * @param params Any parameters for the message.
	 * @return The localized message.
	 */
	public String getString(String key, String... params) {
		String temp = msg.getString(key);
		return MessageFormat.format(temp, (Object[])params);
	}


	@Override
	public void install(AbstractPluggableGUIApplication<?> app) {

		RText rtext = (RText)app;
		RTextMenuBar mb = (RTextMenuBar)app.getJMenuBar();

		// Add an item to the "View" menu to toggle console visibility
		final JMenu menu = mb.getMenuByName(RTextMenuBar.MENU_DOCKED_WINDOWS);
		Action a = rtext.getAction(VIEW_CONSOLE_ACTION);
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(a);
		item.setToolTipText(null);
		item.applyComponentOrientation(app.getComponentOrientation());
		menu.add(item);
		JPopupMenu popup = menu.getPopupMenu();
		popup.pack();
		// Only needed for pre-1.6 support
		popup.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				item.setSelected(isConsoleWindowVisible());
			}
		});

		window.clearConsoles(); // Needed to pick up styles

	}


	/**
	 * Returns whether the console window is visible.
	 *
	 * @return Whether the console window is visible.
	 * @see #setConsoleWindowVisible(boolean)
	 */
	boolean isConsoleWindowVisible() {
		return window!=null && window.isActive();
	}

	void clearText() {
		window.clearConsoles();
	}

	/**
	 * Loads saved preferences.  If this is the first time through, default
	 * values will be returned.
	 *
	 * @return The preferences.
	 */
	private ConsolePrefs loadPrefs() {
		ConsolePrefs prefs = new ConsolePrefs();
		File prefsFile = getPrefsFile();
		if (prefsFile.isFile()) {
			try {
				prefs.load(prefsFile);
			} catch (IOException ioe) {
				app.displayException(ioe);
				// (Some) defaults will be used
			}
		}
		return prefs;
	}


	/**
	 * Changes all consoles to use the default colors for the current
	 * application theme.
	 */
	public void restoreDefaultColors() {
		window.restoreDefaultColors();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void savePreferences() {

		ConsolePrefs prefs = new ConsolePrefs();
		prefs.windowPosition = window.getPosition();
		AppAction<?> a = (AppAction<?>)app.getAction(VIEW_CONSOLE_ACTION);
		prefs.windowVisibilityAccelerator = a.getAccelerator();
		prefs.windowVisible = window.isActive();

		prefs.errorFG = window.getForeground(ConsoleTextArea.STYLE_ERROR);
		prefs.messageFG = window.getForeground(ConsoleTextArea.STYLE_MESSAGE);
		prefs.warningFG = window.getForeground(ConsoleTextArea.STYLE_WARNING);

		File prefsFile = getPrefsFile();
		try {
			prefs.save(prefsFile);
		} catch (IOException ioe) {
			app.displayException(ioe);
		}

	}


	/**
	 * Sets the visibility of the console window.
	 *
	 * @param visible Whether the window should be visible.
	 * @see #isConsoleWindowVisible()
	 */
	void setConsoleWindowVisible(boolean visible) {
		if (visible!=isConsoleWindowVisible()) {
			if (visible && window==null) {
				window = new ConsoleWindow(app, this);
				app.addDockableWindow(window);
			}
			window.setActive(visible);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean uninstall() {
		return true;
	}


}