/*
 * 12/17/2010
 *
 * StopAction.java - Stops the currently running process, if any.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.console;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import ide.RText;
import org.fife.ui.app.AppAction;

import static abfab3d.core.Output.printf;


/**
 * Stops the currently running process, if any.
 *
 * @author Alan Hudson
 * @version 1.0
 */
public class ClearAction extends AppAction<RText> {

	/**
	 * The parent plugin.
	 */
	private Plugin plugin;


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param plugin The parent plugin.
	 */
	public ClearAction(RText owner, ResourceBundle msg, Plugin plugin) {
		super(owner, msg, "Action.ClearConsole");
		setIcon("stop.png");  // TODO: Need a clear icon
		setEnabled(true);
		this.plugin = plugin;
	}


	/**
	 * Called when this action is performed.
	 *
	 * @param e The event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		plugin.clearText();
	}


}
