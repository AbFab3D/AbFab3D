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
import abfab3d.shapejs.RenderOptions;

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

	private static final String VERSION					= "2.6.3";
	private static final String DOCKABLE_WINDOW_RENDERER	= "rendererGpuDockableWindow";

	private RText app;
	private RendererWindow window;
	private Icon icon;

	private static final String MSG = "ide.plugins.renderer.gpu.Plugin";
	protected static final ResourceBundle msg = ResourceBundle.getBundle(MSG);

	private static final String VIEW_RENDERER_ACTION	= "viewRendererGpuAction";


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

		RendererPrefs prefs = loadPrefs();

		AppAction<RText> a = new ViewRendererAction(this.app, msg, this);
		a.setAccelerator(prefs.windowVisibilityAccelerator);
		app.addAction(VIEW_RENDERER_ACTION, a);

		// Window MUST always be created for preference saving on shutdown
		window = new RendererWindow(this.app, this,prefs.renderEngine);

		printf("Startup.  libPath is: %s\n",prefs.libPath);
		window.libPathChanged(prefs.libPath);
		// RenderOptions needs to be set before changing render engine and server
		RenderOptions ro = window.getStillRenderOptions();
		ro.aaSamples = prefs.stillAaSamples;
		ro.quality = Quality.valueOf(prefs.stillQuality);
		ro.shadowQuality = Quality.valueOf(prefs.stillShadowQuality);
		ro.rayBounces = prefs.stillRayBounces;
		ro.renderScale = prefs.stillRenderScale;

		ro = window.getMovingRenderOptions();
		ro.aaSamples = prefs.movingAaSamples;
		ro.quality = Quality.valueOf(prefs.movingQuality);
		ro.shadowQuality = Quality.valueOf(prefs.movingShadowQuality);
		ro.rayBounces = prefs.movingRayBounces;
		ro.renderScale = prefs.movingRenderScale;

		RenderOptions savero = window.getSaveImageRenderOptions();
		savero.aaSamples = prefs.saveAaSamples;
		savero.quality = Quality.valueOf(prefs.saveQuality);
		savero.shadowQuality = Quality.valueOf(prefs.saveShadowQuality);
		savero.rayBounces = prefs.saveRayBounces;
		window.setSaveWidth(prefs.saveWidth);
		window.setSaveHeight(prefs.saveHeight);

		window.setPosition(prefs.windowPosition);
		window.setShapeJSServer(prefs.shapeJSServer);

		window.setLastSaveModelDir(prefs.lastSaveModelDir);
		window.setLastSaveRenderDir(prefs.lastSaveRenderDir);

		window.setActive(prefs.windowVisible);

		putDockableWindow(DOCKABLE_WINDOW_RENDERER, window);
	}


	/**
	 * Returns the dockable window containing the consoles.
	 *
	 * @return The dockable window.
	 */
	public RendererWindow getDockableWindow() {
		return window;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public PluginOptionsDialogPanel getOptionsDialogPanel() {
		return new RendererOptionPanel(this);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPluginAuthor() {
		return "Alan Hudson";
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
						"rendererGpu.properties");
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
		Action a = rtext.getAction(VIEW_RENDERER_ACTION);
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
				item.setSelected(isRendererWindowVisible());
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
	boolean isRendererWindowVisible() {
		return window!=null && window.isActive();
	}


	/**
	 * Loads saved preferences.  If this is the first time through, default
	 * values will be returned.
	 *
	 * @return The preferences.
	 */
	private RendererPrefs loadPrefs() {
		RendererPrefs prefs = new RendererPrefs();
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

		RendererPrefs prefs = new RendererPrefs();
		prefs.windowPosition = window.getPosition();
		AppAction<?> a = (AppAction<?>)app.getAction(VIEW_RENDERER_ACTION);
		prefs.windowVisibilityAccelerator = a.getAccelerator();
		prefs.windowVisible = window.isActive();
		prefs.renderEngine = window.getRenderEngine();
		prefs.shapeJSServer = window.getShapeJSServer();
		prefs.saveWidth = window.getSaveWidth();
		prefs.saveHeight = window.getSaveHeight();
		prefs.maxWidth = window.getMaxWidth();
		prefs.maxHeight = window.getMaxHeight();

		RenderOptions ro = window.getStillRenderOptions();
		prefs.stillAaSamples = ro.aaSamples;
		prefs.stillQuality = String.valueOf(ro.quality);
		prefs.stillRayBounces = ro.rayBounces;
		prefs.stillRenderScale = ro.renderScale;

		ro = window.getMovingRenderOptions();
		prefs.movingAaSamples = ro.aaSamples;
		prefs.movingQuality = String.valueOf(ro.quality);
		prefs.movingRayBounces = ro.rayBounces;
		prefs.movingRenderScale = ro.renderScale;

		RenderOptions savero = window.getSaveImageRenderOptions();
		prefs.saveAaSamples = savero.aaSamples;
		prefs.saveQuality = String.valueOf(savero.quality);
		prefs.saveRayBounces = savero.rayBounces;

		prefs.libPath = window.getLibPath();
		prefs.lastSaveModelDir = window.getLastSaveModelDir();
		prefs.lastSaveRenderDir = window.getLastSaveRenderDir();

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
	void setRendererWindowVisible(boolean visible) {
		if (visible!=isRendererWindowVisible()) {
			if (visible && window==null) {
				RendererPrefs prefs = loadPrefs();

				window = new RendererWindow(app, this,prefs.renderEngine);
				window.setShapeJSServer(prefs.shapeJSServer);
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
