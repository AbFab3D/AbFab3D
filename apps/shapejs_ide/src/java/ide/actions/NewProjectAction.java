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

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.Icon;

import ide.NewProjectDialog;
import ide.RText;
import org.fife.ui.app.AppAction;


/**
 * Action used by an <code>AbstractMainView</code> to create a new project.
 *
 * @author Tony Wong
 */
class NewProjectAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public NewProjectAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "NewProjectAction");
		setIcon(icon);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		NewProjectDialog dialog = new NewProjectDialog(getApplication());
		dialog.setVisible(true);
	}


}