/*
 * 09/08/2012
 *
 * ProjectEntryTreeNode.java - Base class for project entry tree nodes.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.project.tree;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeModel;

import ide.RText;
import ide.plugins.project.BaseAction;
import ide.plugins.project.Messages;
import ide.plugins.project.ProjectPlugin;
import ide.plugins.project.model.LogicalFolderProjectEntry;
import ide.plugins.project.model.ProjectEntry;
import ide.plugins.project.model.ProjectEntryParent;


/**
 * Base class for project entry tree nodes.
 *
 * @author Robert Futrell
 * @version 1.0
 */
abstract class ProjectEntryTreeNode extends AbstractWorkspaceTreeNode {

	protected ProjectEntry entry;


	protected ProjectEntryTreeNode(ProjectPlugin plugin, ProjectEntry entry) {
		super(plugin);
		this.entry = entry;
	}


	/**
	 * Prompts for verification, then removes this project entry from its
	 * parent project.
	 */
	public void handleRemove() {

		RText rtext = plugin.getRText();
		String title = rtext.getString("ConfDialogTitle");
		String selectedEntry = entry.getSaveData();
		String key = (entry instanceof LogicalFolderProjectEntry) ?
				"Action.RemoveLogicalProjectEntry.Confirm" :
				"Action.RemoveProjectEntry.Confirm";
		String text = Messages.getString(key, selectedEntry);

		int rc = JOptionPane.showConfirmDialog(rtext, text, title,
				JOptionPane.YES_NO_OPTION);
		if (rc==JOptionPane.YES_OPTION) {
			entry.removeFromParent();
			((DefaultTreeModel)plugin.getTree().getModel()).removeNodeFromParent(this);
		}

	}


	@Override
	public boolean moveProjectEntityDown(boolean toBottom) {
		ProjectEntryParent parent = entry.getParent();
		return parent.moveProjectEntryDown(entry, toBottom);
	}


	@Override
	public boolean moveProjectEntityUp(boolean toTop) {
		ProjectEntryParent parent = entry.getParent();
		return parent.moveProjectEntryUp(entry, toTop);
	}


	/**
	 * Removes this entry from its parent.
	 */
	protected class RemoveAction extends BaseAction {

		public RemoveAction() {
			super("Action.RemoveProjectEntry");
			KeyStroke delete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
			putValue(ACCELERATOR_KEY, delete);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			handleRemove();
		}

	}


}