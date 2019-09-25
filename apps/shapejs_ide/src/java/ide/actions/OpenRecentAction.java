/*
 * 05/24/2005
 *
 * OpenRemoteAction.java - Action to open a remote file via FTP.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;

import ide.RText;
import ide.RecentFileDialog;
import org.fife.ui.app.AppAction;


/**
 * Displays a dialog allowing the user to select a recently-opened file for
 * re-opening.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class OpenRecentAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public OpenRecentAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "OpenRecentAction");
		setIcon(icon);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		RecentFileDialog rfd = new RecentFileDialog(getApplication());
		rfd.setVisible(true);
	}


}