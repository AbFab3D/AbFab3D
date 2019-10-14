/*
 * 02/26/2010
 *
 * ViewToolOutputAction.java - Toggles visibility of the tool output window.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.tools;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import ide.RText;
import org.fife.ui.app.AppAction;


/**
 * Toggles the display of the "Tool Output" dockable window.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ViewToolOutputAction extends AppAction<RText> {

	/**
	 * The tools plugin.
	 */
	private ToolPlugin plugin;


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param plugin The tools plugin.
	 */
	public ViewToolOutputAction(RText owner, ResourceBundle msg,
								ToolPlugin plugin) {
		super(owner, msg, "ViewToolOutputAction");
		this.plugin = plugin;
	}


	/**
	 * Called when this action is performed.
	 *
	 * @param e The event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		plugin.setToolOutputWindowVisible(!plugin.isToolOutputWindowVisible());
	}


}