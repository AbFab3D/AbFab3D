/*
 * 11/14/2003
 *
 * PrintPreviewAction.java - Action to display a print preview in RText.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;

import org.fife.print.PrintPreviewDialog;
import ide.RText;
import org.fife.ui.app.AppAction;


/**
 * Action used by an <code>RTextTabbedPane</code> to show a print preview.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class PrintPreviewAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public PrintPreviewAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "PrintPreviewAction");
		setIcon(icon);
	}


	/**
	 * Callback routine called when user uses this component.
	 *
	 * @param e The action event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		RText rtext = getApplication();
		PrintPreviewDialog printPreviewDialog =
				new PrintPreviewDialog(rtext,
						rtext.getMainView().getCurrentTextArea());
		printPreviewDialog.setLocationRelativeTo(rtext);
		printPreviewDialog.setVisible(true);
	}


}