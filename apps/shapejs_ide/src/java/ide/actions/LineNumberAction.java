/*
 * 11/14/2003
 *
 * LineNumberAction.java - Action to enable/disable line numbers in RText.
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
 * Action used by an <code>AbstractMainView</code> to enable viewing
 * line numbers for the open documents.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class LineNumberAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public LineNumberAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "LineNumberAction");
		setIcon(icon);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		RText owner = getApplication();
		AbstractMainView mainView = owner.getMainView();
		mainView.setLineNumbersEnabled(!mainView.getLineNumbersEnabled());
	}


}