/*
 * 12/19/2014
 *
 * RecentFileManager.java - Keeps a list of the files opened in RText.
 * Copyright (C) 2014 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import abfab3d.shapejs.Project;
import abfab3d.shapejs.ProjectItem;
import abfab3d.shapejs.Variant;
import org.fife.ui.rsyntaxtextarea.FileLocation;

import static abfab3d.core.Output.printf;


/**
 * Listens for files being opened in RText, so anyone interested can easily
 * get this list.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class RecentFileManager implements ProjectListener {

	private RText rtext;
	private List<FileLocation> files;

	/**
	 * The number of files we remember.
	 */
	private static final int MAX_FILE_COUNT = 75;


	/**
	 * Constructor.
	 *
	 * @param rtext The parent application.
	 */
	public RecentFileManager(RText rtext) {

		rtext.addProjectListener(this);
		this.rtext = rtext;
		files = new ArrayList<FileLocation>();

		List<String> history = ((RTextMenuBar)rtext.getJMenuBar()).
				getFileHistory();
		for (int i=history.size() - 1; i>=0; i--) {
			addFile(history.get(i));
		}

	}


	@Override
	public void projectChanged(Project proj) {
		String fullpath = proj.getParentDir() + File.separator + "manifest.json"; // TODO: Should we be able to ask the project this?
		addFile(fullpath);
	}

	@Override
	public void resourceAdded(ProjectItem res) {

	}

	@Override
	public void resourceRemoved(ProjectItem res) {

	}
	
	@Override
	public void resourceUpdated(ProjectItem res) {

	}

	@Override
	public void runVariant(Variant variant) {

	}

	/**
	 * Adds a file to the list of recent files.
	 *
	 * @param file The file to add.
	 */
	private void addFile(String file) {

		if (file == null) {
			return;
		}

		// If we already are remembering this file, move it to the "top."
		int index = indexOf(file);
		if (index > -1) {
			FileLocation loc = files.remove(index);
			files.add(0, loc);
			return;
		}

		// Add our new file to the "top" of remembered files.
		// TODO: Simplify when RSyntaxTextArea bug #94 is fixed
		FileLocation loc = null;
		try {
			loc = FileLocation.create(file);
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace(); // Malformed URL, shouldn't happen.
			return;
		}

		if (loc.isLocal() && !loc.isLocalAndExists()) {
			// When adding from saved history, some files may no longer
			// exist
			return;
		}
		files.add(0, loc);

		// Too many files?  Oust the file in history added least recently.
		if (files.size() > MAX_FILE_COUNT) {
			files.remove(files.size() - 1);
		}

	}


	/**
	 * Returns the current index of the specified file in this history.
	 *
	 * @param file The file to look for.
	 * @return The index of the file, or <code>-1</code> if it is not
	 *         currently in the list.
	 */
	private int indexOf(String file) {
		for (int i=0; i<files.size(); i++) {
			FileLocation loc = files.get(i);
			if (file.equals(loc.getFileFullPath())) {
				return i;
			}
		}
		return -1;
	}


	/**
	 * Returns the list of recent files.
	 *
	 * @return The list of recent files.
	 */
	public List<FileLocation> getRecentFiles() {
		return files;
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
