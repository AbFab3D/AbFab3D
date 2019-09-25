/*
 * 11/14/2003
 *
 * SaveAsAction.java - Action to save the current document with a new file
 * name in RText.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;

import ide.RText;
import org.fife.ui.app.AppAction;


/**
 * Action used by an <code>RText</code> to save the current
 * document with a new file name.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SaveAsAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public SaveAsAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "SaveAsAction");
		setIcon(icon);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		RText app = getApplication();
		app.getMainView().saveCurrentFileAs();
		app.setProjectUpdated(true);
	}


}