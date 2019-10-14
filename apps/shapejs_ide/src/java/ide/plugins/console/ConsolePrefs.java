/*
 * 01/04/2011
 *
 * ConsolePrefs.java - Preferences for the console plugin.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.console;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.KeyStroke;

import ide.RTextUtilities;
import org.fife.ui.app.Prefs;
import org.fife.ui.dockablewindows.DockableWindow;


/**
 * Preferences for the Console plugin.
 *
 * @author Alan Hudson
 * @version 1.0
 */
public class ConsolePrefs extends Prefs {

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
	 * The color used for stdout in consoles.
	 */
	public Color messageFG;

	/**
	 * The color used for warnings in consoles.
	 */
	public Color warningFG;

	/**
	 * The color used for exceptions in consoles.
	 */
	public Color errorFG;


	/**
	 * Overridden to validate the dockable window position value.
	 */
	@Override
	public void load(InputStream in) throws IOException {
		super.load(in);
		// Ensure window position is valid.
		if (!DockableWindow.isValidPosition(windowPosition)) {
			windowPosition = DockableWindow.BOTTOM;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaults() {

		windowVisible = true;
		windowPosition = DockableWindow.BOTTOM;
		windowVisibilityAccelerator = null;

		boolean isDark = RTextUtilities.isDarkLookAndFeel();
		if (isDark) {
			messageFG = ConsoleTextArea.DEFAULT_DARK_MESSAGE_FG;
			warningFG = ConsoleTextArea.DEFAULT_DARK_WARNING_FG;
			errorFG = ConsoleTextArea.DEFAULT_DARK_ERROR_FG;
		}
		else {
			messageFG = ConsoleTextArea.DEFAULT_MESSAGE_FG;
			warningFG = ConsoleTextArea.DEFAULT_WARNING_FG;
			errorFG = ConsoleTextArea.DEFAULT_ERROR_FG;
		}
	}
}