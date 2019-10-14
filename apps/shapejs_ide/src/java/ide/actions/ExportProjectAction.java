/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package ide.actions;

import ide.RText;
import org.fife.ui.app.AppAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;


/**
 * Action used by an <code>RTextTabbedPane</code> to export a project
 * open documents.
 *
 * @author Alan Hudson
 * @version 1.0
 */
class ExportProjectAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public ExportProjectAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "ExportProjectAction");
		setIcon(icon);
	}


	/**
	 * Called when this action is performed.
	 *
	 * @param e The event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		getApplication().exportProject();
	}


}