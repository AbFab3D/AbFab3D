/*
 * 12/14/2015
 *
 * Copyright (C) 2015 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.langsupport.typescript;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import ide.RText;
import ide.plugins.langsupport.Plugin;
import org.fife.ui.app.AppAction;


/**
 * Toggles the display of the "TypeScript Build Results" dockable window.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ViewTypeScriptBuildResultsAction extends AppAction<RText> {

	/**
	 * The language support plugin.
	 */
	private Plugin plugin;


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param plugin The language support plugin.
	 */
	public ViewTypeScriptBuildResultsAction(final RText owner,
			ResourceBundle msg, Plugin plugin) {
		super(owner, msg, "TypeScript.ViewBuildResultsAction");
		this.plugin = plugin;
	}


	/**
	 * Called when this action is performed.
	 *
	 * @param e The event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		plugin.getTypeScriptSupport().toggleBuildResultsWindowVisible();
	}


}