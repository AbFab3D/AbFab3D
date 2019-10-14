/*
 * 08/28/2012
 *
 * ViewProjectsAction.java - Toggles visibility of the projects dockable window.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.project;

import java.awt.event.ActionEvent;

import ide.RText;
import org.fife.ui.app.AppAction;


/**
 * Toggles visibility of the projects dockable window.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ViewProjectsAction extends AppAction<RText> {

	/**
	 * The parent plugin.
	 */
	private ProjectPlugin plugin;


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param plugin The parent plugin.
	 */
	public ViewProjectsAction(RText owner, ProjectPlugin plugin) {
		super(owner, Messages.getBundle(), "Action.ViewProjects");
		this.plugin = plugin;
	}


	/**
	 * Called when this action is performed.
	 *
	 * @param e The event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		plugin.setProjectWindowVisible(!plugin.isProjectWindowVisible());
	}


}