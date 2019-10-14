/*
 * 07/23/2011
 *
 * EditMacrosAction.java - Action that opens the Options dialog to the "macros"
 * panel.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.macros;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import ide.RText;
import org.fife.ui.OptionsDialog;
import org.fife.ui.app.AppAction;


/**
 * Action that opens the Options dialog to the "Macros" panel.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class EditMacrosAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 */
	public EditMacrosAction(RText owner, ResourceBundle msg) {
		super(owner, msg, "EditMacrosAction");
		setIcon("cog_edit.png");
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		RText owner = getApplication();
		OptionsDialog od = owner.getOptionsDialog();
		ResourceBundle msg = MacroPlugin.msg;
		od.setSelectedOptionsPanel(msg.getString(MacroOptionPanel.TITLE_KEY));
		od.initialize();
		od.setVisible(true);
	}


}