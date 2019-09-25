/*
 * 11/14/2003
 *
 * ToolBar.java - Toolbar used by RText.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import org.apache.commons.io.FilenameUtils;
import org.fife.ui.CustomizableToolBar;
import org.fife.ui.OS;
import org.fife.ui.rtextarea.IconGroup;
import org.fife.ui.rtextarea.RTextArea;

import abfab3d.shapejs.Project;
import abfab3d.shapejs.ProjectItem;
import abfab3d.shapejs.Variant;
import abfab3d.shapejs.VariantItem;


/**
 * The toolbar used by {@link RText}.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ToolBar extends CustomizableToolBar implements ProjectListener {

	private JButton openButton;
	private JButton saveButton;
	private JButton saveAllButton;
	private JButton closeButton;
	private JButton printButton;
	private JButton printPreviewButton;
	private JButton cutButton;
	private JButton copyButton;
	private JButton pasteButton;
	private JButton deleteButton;
	private JButton findButton;
	private JButton findNextButton;
	private JButton replaceButton;
	private JButton replaceNextButton;
	private JButton undoButton;
	private JButton redoButton;

	private RText owner;
	private Project proj;
	private boolean mouseInNewButton;

	private JComboBox variants;
	private JButton runButton;

	/**
	 * Creates the tool bar.
	 *
	 * @param title The title of this toolbar when it is floating.
	 * @param rtext The main application that owns this toolbar.
	 * @param mouseListener The status bar that displays a status message
	 *        when the mouse hovers over this toolbar.
	 */
	public ToolBar(String title, RText rtext, StatusBar mouseListener) {

		super(title);

		this.owner = rtext;

		// Add the standard buttons.

		openButton = createButton(rtext.getAction(RText.OPEN_ACTION));
		configure(openButton, mouseListener);
		add(openButton);

		saveButton = createButton(rtext.getAction(RText.SAVE_ACTION));
		configure(saveButton, mouseListener);
		add(saveButton);

		saveAllButton = createButton(rtext.getAction(RText.SAVE_ALL_ACTION));
		configure(saveAllButton, mouseListener);
		add(saveAllButton);

		closeButton = createButton(rtext.getAction(RText.CLOSE_ACTION));
		configure(closeButton, mouseListener);
		add(closeButton);

		addSeparator();

		printButton = createButton(rtext.getAction(RText.PRINT_ACTION));
		configure(printButton, mouseListener);
		add(printButton);

		printPreviewButton = createButton(rtext.getAction(RText.PRINT_PREVIEW_ACTION));
		configure(printPreviewButton, mouseListener);
		add(printPreviewButton);

		addSeparator();

		cutButton = createButton(RTextArea.getAction(RTextArea.CUT_ACTION));
		configure(cutButton, mouseListener);
		add(cutButton);

		copyButton = createButton(RTextArea.getAction(RTextArea.COPY_ACTION));
		configure(copyButton, mouseListener);
		add(copyButton);

		pasteButton = createButton(RTextArea.getAction(RTextArea.PASTE_ACTION));
		configure(pasteButton, mouseListener);
		add(pasteButton);

		deleteButton = createButton(RTextArea.getAction(RTextArea.DELETE_ACTION));
		configure(deleteButton, mouseListener);
		add(deleteButton);

		addSeparator();

		findButton = createButton(rtext.getAction(RText.FIND_ACTION));
		configure(findButton, mouseListener);
		add(findButton);

		findNextButton = createButton(rtext.getAction(RText.FIND_NEXT_ACTION));
		configure(findNextButton, mouseListener);
		add(findNextButton);

		replaceButton = createButton(rtext.getAction(RText.REPLACE_ACTION));
		configure(replaceButton, mouseListener);
		add(replaceButton);

		replaceNextButton = createButton(rtext.getAction(RText.REPLACE_NEXT_ACTION));
		configure(replaceNextButton, mouseListener);
		add(replaceNextButton);

		addSeparator();

		undoButton = createButton(RTextArea.getAction(RTextArea.UNDO_ACTION));
		configure(undoButton, mouseListener);
		// Necessary to keep button size from changing when undo text changes.
		undoButton.putClientProperty("hideActionText", Boolean.TRUE);
		add(undoButton);

		redoButton = createButton(RTextArea.getAction(RTextArea.REDO_ACTION));
		configure(redoButton, mouseListener);
		// Necessary to keep button size from changing when undo text changes.
		redoButton.putClientProperty("hideActionText", Boolean.TRUE);
		add(redoButton);

		addSeparator();

		variants = new JComboBox(new String[] {"Variant1","Variant2"});
		Dimension dim = redoButton.getPreferredSize();
		// TODO: Dodgy, is there a better way todo this?
		variants.setMaximumSize(new Dimension(280,(int)dim.getHeight()+2));
		add(variants);

		runButton = createButton(rtext.getAction(RText.RUN_ACTION));
		configure(runButton, mouseListener);
		add(runButton);

		// Make the toolbar have the right-click customize menu.
		makeCustomizable();

		this.owner.addProjectListener(this);
	}

	// TODO: Not sure this is the best way to handle this...
	public DefaultComboBoxModel getVariantModel() {
		return (DefaultComboBoxModel) variants.getModel();
	}


	/**
	 * Checks whether the current icon group has large icons, and if it does,
	 * uses these large icons for the toolbar.
	 */
	void checkForLargeIcons() {
		IconGroup group = owner.getIconGroup();
		if (group.hasSeparateLargeIcons()) {
			Icon icon = group.getLargeIcon("open");
			openButton.setIcon(icon);
			icon = group.getLargeIcon("save");
			saveButton.setIcon(icon);
			icon = group.getLargeIcon("saveall");
			saveAllButton.setIcon(icon);
			icon = group.getLargeIcon("close");
			closeButton.setIcon(icon);
			icon = group.getLargeIcon("print");
			printButton.setIcon(icon);
			icon = group.getLargeIcon("printpreview");
			printPreviewButton.setIcon(icon);
			icon = group.getLargeIcon("cut");
			cutButton.setIcon(icon);
			icon = group.getLargeIcon("copy");
			copyButton.setIcon(icon);
			icon = group.getLargeIcon("paste");
			pasteButton.setIcon(icon);
			icon = group.getLargeIcon("delete");
			deleteButton.setIcon(icon);
			icon = group.getLargeIcon("find");
			findButton.setIcon(icon);
			icon = group.getLargeIcon("findnext");
			findNextButton.setIcon(icon);
			icon = group.getLargeIcon("replace");
			replaceButton.setIcon(icon);
			icon = group.getLargeIcon("replacenext");
			replaceNextButton.setIcon(icon);
			icon = group.getLargeIcon("undo");
			undoButton.setIcon(icon);
			icon = group.getLargeIcon("redo");
			redoButton.setIcon(icon);
		}
	}


	/**
	 * Configures the specified menu bar button.
	 *
	 * @param button The button.
	 * @param mouseListener A mouse listener to add.
	 */
	private final void configure(JButton button, StatusBar mouseListener) {
		// Bug in Windows 1.4 and some 1.5 JRE's - changing LAF to
		// windows LAF causes margin to become much too wide.
		if (owner.getOS()==OS.WINDOWS) {
			button.setMargin(new Insets(0,0,0,0));
		}
		button.addMouseListener(mouseListener);
	}


	/**
	 * This class keeps track of whether or not the mouse position is inside
	 * the "New" button's bounds.  This is part of an elaborate hack to fix
	 * what seems to be a focus issue (bug) in JRE1.4.  Note that in JRE 1.5,
	 * it does not happen.
	 *
	 * What happens is this:  Whenever the user clicks on a tab to change the
	 * current document, the focused component gets switched not to the text
	 * area corresponding to the tab they clicked, but rather the "New" button
	 * on the toolbar (actually, it gets switched to the text area, then to the
	 * New button).  This behavior stops if the user changes the Look and Feel.
	 *
	 * This is the second part of the elaborate focus hack.  Whenever the New
	 * toolbar button gets focus, we check to see if the mouse position is
	 * inside the New button's bounds.  If it isn't, then we assume that the
	 * event was fired as a result of the situation described in
	 * NewButtonMouseListener's blurb, and so we give focus back to the current
	 * text area.
	 */
	private class NewButtonListener extends MouseAdapter
										implements FocusListener {

		@Override
		public void focusGained(FocusEvent e) {
			RTextEditorPane textArea = owner.getMainView().getCurrentTextArea();
			if (!mouseInNewButton && textArea!=null) {
				textArea.requestFocusInWindow();
			}
		}

		@Override
		public void focusLost(FocusEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			mouseInNewButton = true;
		}

		@Override
		public void mouseExited(MouseEvent e) {
			mouseInNewButton = false;
		}

	}

	private void updateVariants() {
		java.util.List<VariantItem> variants = proj.getVariants();

		DefaultComboBoxModel vmodel = getVariantModel();
		vmodel.removeAllElements();

		if (variants.size() > 0) {
			for (VariantItem vi : variants) {
				vmodel.addElement(FilenameUtils.getBaseName(vi.getPath()));
			}
		}
	}

	@Override
	public void projectChanged(Project proj) {
		this.proj = proj;
		updateVariants();
	}

	@Override
	public void resourceAdded(ProjectItem res) {
		if (!res.getPath().endsWith(Project.EXT_VARIANT)) {
			return;
		}

		DefaultComboBoxModel vmodel = getVariantModel();

		updateVariants();

		// Select the newly added variant
		vmodel.setSelectedItem(FilenameUtils.getBaseName(res.getPath()));
	}

	@Override
	public void resourceRemoved(ProjectItem res) {
		if (!res.getPath().endsWith(Project.EXT_VARIANT)) {
			return;
		}

		DefaultComboBoxModel vmodel = getVariantModel();
		String selected = (String) vmodel.getSelectedItem();

		updateVariants();

		int idx = vmodel.getIndexOf(selected);
		if (idx >= 0) {
			vmodel.setSelectedItem(selected);
		}

	}

	@Override
	public void resourceUpdated(ProjectItem res) {
		if (!res.getPath().endsWith(Project.EXT_VARIANT)) {
			return;
		}

		DefaultComboBoxModel vmodel = getVariantModel();
		String selected = (String) vmodel.getSelectedItem();

		updateVariants();

		vmodel.setSelectedItem(selected);
	}

	@Override
	public void runVariant(Variant variant) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {

	}

	@Override
	public void setProjectUpdated(boolean updated) {

	}
	
    @Override
    public void setBusyMode() {

    }

    @Override
    public void setIdleMode() {

    }
}
