/*
 * 11/14/2003
 *
 * OpenAction.java - Action to open an old text file in RText.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.Icon;

import abfab3d.shapejs.Project;
import abfab3d.shapejs.ProjectItem;
import ide.AbstractMainView;
import ide.RText;
import org.apache.commons.io.FilenameUtils;
import org.fife.ui.app.AppAction;
import org.fife.ui.rtextfilechooser.RTextFileChooser;
import org.fife.ui.rtextfilechooser.filters.ExtensionFileFilter;


/**
 * Action used by an <code>AbstractMainView</code> to open a document
 * from a file on disk.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class OpenAction extends AppAction<RText> {


	/**
	 * Constructor.
	 *
	 * @param owner The parent RText instance.
	 * @param msg The resource bundle to use for localization.
	 * @param icon The icon associated with the action.
	 */
	public OpenAction(RText owner, ResourceBundle msg, Icon icon) {
		//super(owner, text, icon, desc, mnemonic, accelerator);
		super(owner, msg, "OpenAction");
		setIcon(icon);
	}


	@Override
	public void actionPerformed(ActionEvent e) {

		RText owner = getApplication();
		RTextFileChooser chooser = owner.getFileChooser();

		// Without this, the user can press "Ctrl+O" twice really fast
		// and get two file choosers up.  The first one behaves normally
		// but the second one has no components painted on it and is not
		// responsive, effectively hanging the program.
		if (!chooser.isShowing()) {

			// Initialize the file chooser to be an Open dialog.
			chooser.setMultiSelectionEnabled(false);
			chooser.setOpenedFiles(owner.getMainView().getOpenFiles());
			chooser.setEncoding(RTextFileChooser.getDefaultEncoding());
			
			ExtensionFileFilter filter = new ExtensionFileFilter("File filter", "json");
			chooser.setFileFilter(filter);
			
			if (owner.getLastOpenProjectFile() != null) {
				chooser.setSelectedFile(new File(owner.getLastOpenProjectFile()));
			}

			int returnVal = chooser.showOpenDialog(owner);

			// If they selected a file and clicked "OK", open the flie!
			if (returnVal == RTextFileChooser.APPROVE_OPTION) {

				AbstractMainView mainView = owner.getMainView();
				String encoding = chooser.getEncoding();

				File file = chooser.getSelectedFile();

				if (file.getName().equals("manifest.json")) {
					getApplication().openProject(file.getAbsolutePath());
				} else if (file.getName().endsWith(".shapevar")) {
					// Load the variant into the current project

					//ProjectNavWindow.runVariant
					getApplication().openVariant(file.getAbsolutePath());
				}
/*
				// Add each file, one at a time.
				File [] selectedFiles = chooser.getSelectedFiles();
				for (int i=0; i<selectedFiles.length; i++) {
					String fileFullPath = selectedFiles[i].getAbsolutePath();
					mainView.openFile(fileFullPath, encoding);
				}
*/
			} // End of if (returnVal == RFileChooser.APPROVE_OPTION).

		} // End of if (!chooser.isShowing()).

	}


}