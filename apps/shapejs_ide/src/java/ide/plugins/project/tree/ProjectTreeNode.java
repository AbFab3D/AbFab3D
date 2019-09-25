/*
 * 08/28/2012
 *
 * ProjectTreeNode.java - A tree node for projects.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.project.tree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;

import ide.RText;
import ide.plugins.project.Messages;
import ide.plugins.project.PopupContent;
import ide.plugins.project.ProjectPlugin;
import ide.plugins.project.RenameDialog;
import ide.plugins.project.model.Project;
import ide.plugins.project.model.Workspace;


/**
 * Tree node for projects.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ProjectTreeNode extends AbstractWorkspaceTreeNode {

	private Project project;
	private static Icon icon;


	public ProjectTreeNode(ProjectPlugin plugin, Project project) {
		super(plugin);
		this.project = project;
	}


	@Override
	public String getDisplayName() {
		return project.getName();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Icon getIcon() {
		return getProjectIcon();
	}


	@Override
	public List<PopupContent> getPopupActions() {
		List<PopupContent> actions = new ArrayList<PopupContent>();
		actions.add(new AddFileAction(project, this));
		actions.add(new AddFolderAction(project, this));
		actions.add(new AddLogicalFolderAction(project, this));
		actions.add(null);
		actions.add(new MoveToTopAction());
		actions.add(new MoveUpAction());
		actions.add(new MoveDownAction());
		actions.add(new MoveToBottomAction());
		actions.add(null);
		actions.add(new DeleteAction());
		actions.add(null);
		actions.add(new RenameAction());
		actions.add(null);
		actions.add(new PropertiesAction(false));
		return actions;
	}


	/**
	 * Returns the project being represented.
	 *
	 * @return The project represented.
	 */
	public Project getProject() {
		return project;
	}


	/**
	 * Returns the icon shared amongst all project tree nodes.
	 *
	 * @return The shared icon.
	 */
	public static Icon getProjectIcon() {
		return icon;
	}


	@Override
	public String getToolTipText() {
		return null;
	}


	@Override
	protected void handleDelete() {

		String text = Messages.getString("Action.DeleteProject.Confirm",
				getProject().getName());
		RText rtext = plugin.getRText();
		String title = rtext.getString("ConfDialogTitle");

		int rc = JOptionPane.showConfirmDialog(rtext, text, title,
				JOptionPane.YES_NO_OPTION);
		if (rc==JOptionPane.YES_OPTION) {
			project.removeFromWorkspace();
			((DefaultTreeModel)plugin.getTree().getModel()).
					removeNodeFromParent(this);
		}

	}


	@Override
	protected void handleProperties() {
		// Do nothing
	}


	@Override
	protected void handleRename() {
		RText rtext = plugin.getRText();
		String type = Messages.getString("ProjectPlugin.Project");
		RenameDialog dialog = new RenameDialog(rtext, false, type,
				new ProjectNameChecker(project.getWorkspace()));
		dialog.setFileName(project.getName());
		dialog.setVisible(true);
		String newName = dialog.getFileName();
		if (newName!=null) {
			project.setName(newName);
			plugin.getTree().nodeChanged(this);
		}
	}


	@Override
	public boolean moveProjectEntityDown(boolean toBottom) {
		Workspace workspace = project.getWorkspace();
		return workspace.moveProjectDown(project, toBottom);
	}


	@Override
	public boolean moveProjectEntityUp(boolean toTop) {
		Workspace workspace = project.getWorkspace();
		return workspace.moveProjectUp(project, toTop);
	}


	/**
	 * Ensures that proposed project names are valid.
	 */
	public static class ProjectNameChecker implements NameChecker {

		private Workspace workspace;

		public ProjectNameChecker(Workspace workspace) {
			this.workspace = workspace;
		}

		@Override
		public String isValid(String text) {
			int length = text.length();
			if (length==0) {
				return "empty";
			}
			if (workspace.containsProjectNamed(text)) {
				return "projectAlreadyExists";
			}
			return null;
		}

	}


	static {
		icon = new ImageIcon(ProjectTreeNode.class.
				getResource("application.png"));
	}


}