/*
 * 12/08/2004
 *
 * FilePropertiesAction.java - Action to display the information on the
 * current text area.
 * Copyright (C) 2004 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.fife.rsta.ui.TextFilePropertiesDialog;
import ide.RText;
import org.fife.ui.app.AppAction;


/**
 * Action to display information on the current text area.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FilePropertiesAction extends AppAction<RText> {


	/**
	 * Creates a new <code>FilePropertiesAction</code>.
	 *
	 * @param rtext The parent application.
	 * @param msg The resource bundle to use for localization.
	 */
	public FilePropertiesAction(RText rtext, ResourceBundle msg) {
		super(rtext, msg, "FilePropertiesAction");
	}


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param e The action event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		RText rtext = getApplication();
		TextFilePropertiesDialog dialog = new TextFilePropertiesDialog(
							rtext, rtext.getMainView().getCurrentTextArea());
		dialog.setVisible(true);
	}


}