/*
 * 11/14/2003
 *
 * NewAction.java - Action used to open a new document in RText.
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
 * Action used by an <code>AbstractMainView</code> to open a new document.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class NewAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public NewAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "NewAction");
		setIcon(icon);
	}


	@Override
	public void actionPerformed(ActionEvent e) {

		RText owner = getApplication();
		AbstractMainView mainView = owner.getMainView();

		// First, remove old listeners.
		if (mainView.getCurrentTextArea() != null) {
			mainView.getCurrentTextArea().removeCaretListener(owner);
		}

		// Create a new RTextDocument for an empty file with a default name.
		mainView.addNewEmptyUntitledFile();

		// Now, since the new tab is guaranteed to be a new number from the
		// old active tab, the stateChanged() callback below will take care
		// of updating all listeners.

	}


}