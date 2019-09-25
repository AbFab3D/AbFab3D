/*
 * 11/14/2003
 *
 * FindInFilesAction.java - Action for finding text in a group of files.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;
import ide.AbstractMainView;
import ide.RText;
import org.fife.ui.app.AppAction;


/**
 * Action used by an <code>AbstractMainView</code> to find text in files.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class FindInFilesAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public FindInFilesAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "FindInFilesAction");
		setIcon(icon);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		RText rtext = getApplication();
		AbstractMainView mainView = rtext.getMainView();
		mainView.getFindInFilesDialog().setVisible(true);
	}


}