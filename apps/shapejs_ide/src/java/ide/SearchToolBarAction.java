/*
 * 01/16/2005
 *
 * SearchToolBarAction.java - Toggles the visibility of the QuickSearch toolbar.
 * Copyright (C) 2005 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide;

import java.awt.event.ActionEvent;
import javax.swing.JCheckBoxMenuItem;

import org.fife.ui.app.AppAction;


/**
 * Action used by an <code>RText</code> to toggle the visibility of
 * the QuickSearch toolbar.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class SearchToolBarAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param rtext The parent <code>RText</code> application.
	 */
	public SearchToolBarAction(RText rtext) {
		super(rtext);
	}


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param e The action event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// The source must be the "QuickSearch bar" menu item.
		JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
		RText rtext = getApplication();
		rtext.getSearchToolBar().setVisible(item.isSelected());
	}


}