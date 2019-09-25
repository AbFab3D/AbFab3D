/*
 * 09/08/2012
 *
 * LogicalFolderNameDialog.java - A dialog that prompts the user for the
 * name of a logical folder.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.project;

import java.awt.BorderLayout;
import java.awt.Container;
import java.net.URL;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import ide.RText;
import ide.plugins.project.tree.NameChecker;


/**
 * A dialog that prompts the user for the name of a new file or folder.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class NewFileOrFolderDialog extends AbstractEnterFileNameDialog {

	private JCheckBox openOnCreateCB;


	public NewFileOrFolderDialog(RText parent, boolean directory,
			NameChecker nameChecker) {
		super(parent, directory, nameChecker);
		String key = "NewFileOrFolderDialog.Title." +
					(directory ? "Folder" : "File");
		setTitle(Messages.getString(key));
	}


	@Override
	protected void addDescPanel() {
		String header = "NewFileOrFolderDialog.Header." +
				(isForFile ? "File" : "Folder");
		String descText = Messages.getString(header);
		String imgName = isForFile ? "page_white_add.png" : "folder_add.png";
		URL url = getClass().getResource("tree/" + imgName);
		Icon icon = new ImageIcon(url);
		setDescription(icon, descText);
	}


	/**
	 * Overridden to add a check box to automatically open the newly created
	 * file, if this dialog is for a file and not a folder.
	 */
	@Override
	protected Container createExtraContent() {

		Container previousContent = super.createExtraContent();
		if (!isForFile) {
			return previousContent;
		}

		Box box = Box.createVerticalBox();
		if (previousContent != null) {
			box.add(previousContent);
			box.add(Box.createVerticalStrut(5));
		}

		JPanel temp = new JPanel(new BorderLayout());
		openOnCreateCB = new JCheckBox(Messages.
				getString("NewFileDialog.Label.OpenImmediately"), true);
		temp.add(openOnCreateCB, BorderLayout.LINE_START);
		box.add(temp);

		box.add(Box.createVerticalGlue());
		return box;

	}


	/**
	 * Returns whether the user wants to open the entered file after it is
	 * created.
	 *
	 * @return Whether to open the file.  If this dialog is for a folder name,
	 *         this method will always return <code>false</code>.
	 */
	public boolean getOpenOnCreate() {
		return openOnCreateCB.isSelected();
	}


}