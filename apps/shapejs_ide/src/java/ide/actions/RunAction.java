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
import javax.swing.JOptionPane;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import ide.RText;
import ide.RTextEditorPane;
import org.fife.ui.app.AppAction;


/**
 * Action used to run a variant
 *
 * @author Alan Hudson
 * @version 1.0
 */
class RunAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public RunAction(RText owner, ResourceBundle msg, Icon icon) {
		super(owner, msg, "RunAction");
		setIcon(icon);
	}


	@Override
	public void actionPerformed(ActionEvent e) {

		RText owner = getApplication();

		owner.run();
	}


}