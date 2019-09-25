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
package ide.plugins.projectnav;

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

import abfab3d.shapejs.Quality;
import ide.RText;
import ide.RTextMenuBar;
import ide.RTextUtilities;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.GUIPlugin;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.AppAction;
import org.fife.ui.dockablewindows.DockableWindow;

import static abfab3d.core.Output.printf;


/**
 * A plugin that allows the user to render the current Variant
 *
 * @author Alan Hudson
 * @version 1.0
 */
public class Plugin extends GUIPlugin {

	private static final String VERSION	= "2.6.3";
	private static final String DOCKABLE_WINDOW_PROJECT_NAV = "projectNavDockableWindow";

	private RText app;
	private ProjectNavWindow window;
	private Icon icon;

	private static final String MSG = "ide.plugins.projectnav.Plugin";
	protected static final ResourceBundle msg = ResourceBundle.getBundle(MSG);

	private static final String VIEW_PROJECT_NAV_ACTION	= "viewProjectNavAction";


	/**
	 * Constructor.
	 *
	 * @param app The parent application.
	 */
	public Plugin(AbstractPluggableGUIApplication<?> app) {

		this.app = (RText)app;

		// Load the plugin icon.
		URL url = getClass().getResource("application_side_list.png");
		if (url!=null) { // Should always be true
			try {
				icon = new ImageIcon(ImageIO.read(url));
			} catch (IOException ioe) {
				app.displayException(ioe);
			}
		}

		ProjectNavPrefs prefs = loadPrefs();

		AppAction<RText> a = new ViewProjectNavAction(this.app, msg, this);
		a.setAccelerator(prefs.windowVisibilityAccelerator);
		app.addAction(VIEW_PROJECT_NAV_ACTION, a);

		// Window MUST always be created for preference saving on shutdown
		window = new ProjectNavWindow(this.app, this, prefs);

		window.setPosition(prefs.windowPosition);

		window.setActive(prefs.windowVisible);
		putDockableWindow(DOCKABLE_WINDOW_PROJECT_NAV, window);
	}


	/**
	 * Returns the dockable window containing the consoles.
	 *
	 * @return The dockable window.
	 */
	public ProjectNavWindow getDockableWindow() {
		return window;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public PluginOptionsDialogPanel getOptionsDialogPanel() {
		return new ProjectNavOptionPanel(this);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPluginAuthor() {
		return "Tony Wong";
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
						"projectNav.properties");
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
		Action a = rtext.getAction(VIEW_PROJECT_NAV_ACTION);
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
				item.setSelected(isWindowVisible());
			}
		});

		window.clearRenderers(); // Needed to pick up styles

	}


	/**
	 * Returns whether the console window is visible.
	 *
	 * @return Whether the console window is visible.
	 * @see #setRendererWindowVisible(boolean)
	 */
	boolean isWindowVisible() {
		return window!=null && window.isActive();
	}


	/**
	 * Loads saved preferences.  If this is the first time through, default
	 * values will be returned.
	 *
	 * @return The preferences.
	 */
	private ProjectNavPrefs loadPrefs() {
		ProjectNavPrefs prefs = new ProjectNavPrefs();
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
	 * {@inheritDoc}
	 */
	@Override
	public void savePreferences() {

		ProjectNavPrefs prefs = new ProjectNavPrefs();
		prefs.windowPosition = window.getPosition();
		AppAction<?> a = (AppAction<?>)app.getAction(VIEW_PROJECT_NAV_ACTION);
		prefs.windowVisibilityAccelerator = a.getAccelerator();
		prefs.windowVisible = window.isActive();

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
	 * @see #isRendererWindowVisible()
	 */
	void setWindowVisible(boolean visible) {
		if (visible!=isWindowVisible()) {
			getDockableWindow().setActive(visible);
/*
			if (visible && window==null) {
				window = new ProjectNavWindow(app, this);
				app.addDockableWindow(window);
			}
			window.setActive(visible);
*/
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