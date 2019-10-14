/*
 * 11/14/2003
 *
 * CloseAllAction.java - Action to close all open documents in RText.
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
 * Action used by an <code>RTextTabbedPane</code> to close all open documents.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class CloseAllAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public CloseAllAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "CloseAllAction");
		setIcon(icon);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		getApplication().getMainView().closeAllDocuments();
	}


}