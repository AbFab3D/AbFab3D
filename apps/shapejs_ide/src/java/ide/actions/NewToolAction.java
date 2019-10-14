/*
 * 11/3/2009
 *
 * NewToolAction.java - Action that creates a new user tool
 * Copyright (C) 2009 Robert Futrell
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
 * Action that creates a new user tool.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class NewToolAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public NewToolAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "NewToolAction");
		setIcon(icon);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

//		RText owner = (RText)getApplication();

	}


}