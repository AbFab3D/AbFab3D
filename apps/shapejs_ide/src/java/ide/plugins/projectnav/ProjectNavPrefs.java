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

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.KeyStroke;

import ide.RTextUtilities;
import org.fife.ui.app.Prefs;
import org.fife.ui.dockablewindows.DockableWindow;

import static abfab3d.core.Output.printf;


/**
 * Preferences for the Renderer plugin.
 *
 * @author Alan Hudson
 * @version 1.0
 */
public class ProjectNavPrefs extends Prefs {

	/**
	 * Whether the GUI plugin window is active (visible).
	 */
	public boolean windowVisible;

	/**
	 * The location of the dockable console output window.
	 */
	public int windowPosition;

	/**
	 * Key stroke that toggles the console window's visibility.
	 */
	public KeyStroke windowVisibilityAccelerator;


	/**
	 * Overridden to validate the dockable window position value.
	 */
	@Override
	public void load(InputStream in) throws IOException {
		super.load(in);
		// Ensure window position is valid.
		if (!DockableWindow.isValidPosition(windowPosition)) {
			windowPosition = DockableWindow.LEFT;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaults() {

		windowVisible = true;
		windowPosition = DockableWindow.LEFT;
		windowVisibilityAccelerator = null;
	}
}