/*
 * 12/14/2015
 *
 * Copyright (C) 2015 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.langsupport.typescript;

import java.io.File;
import javax.swing.SwingUtilities;

import org.fife.io.ProcessRunner;
import ide.RTextEditorPane;
import ide.plugins.langsupport.Plugin;
import org.fife.ui.GUIWorkerThread;


/**
 * A thread that launches <code>tsc</code> and sends its output to the
 * TypeScript warning/error docked window.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class BuildThread extends GUIWorkerThread {

	private volatile Plugin plugin;
	private volatile TypeScriptNoticeWindow window;


	BuildThread(Plugin plugin, TypeScriptNoticeWindow window) {
		this.plugin = plugin;
		this.window = window;
	}


	@Override
	public Object construct() {

		final File tsConfig;
		try {
			tsConfig = getTsConfig();
		} catch (final IllegalArgumentException iae) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					plugin.getRText().displayException(iae);
				}
			});
			return null;
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.setTitleWhileBuilding(tsConfig);
			}
		});

		String[] cmd;
		if (File.separatorChar=='/') {
			cmd = new String[] { "/bin/sh", "-c", "tsc", };
		}
		else {
			cmd = new String[] { "cmd.exe", "/c", "tsc", };
		}
		ProcessRunner pr = new ProcessRunner(cmd);
		pr.setDirectory(tsConfig.getParentFile());
		pr.run();

		return new Result(tsConfig, pr);

	}


	/**
	 * Called on the EDT when this thread completes.  Notifies the dockable
	 * window to parse tsc's output and display the returned warnings and
	 * errors.
	 */
	@Override
	public void finished() {
		Result result = (Result)get();
		window.parseErrors(result.tsConfig.getParentFile(),
				result.pr.getStdout());
	}


	/**
	 * Returns the tsconfig file that's the nearest ancestor to the file
	 * being edited in the active window.
	 *
	 * @return The tsconfig file.
	 * @throws IllegalArgumentException If there is no ancestor tsconfig file.
	 */
	private File getTsConfig() {

		RTextEditorPane textArea = plugin.getRText().getMainView().
				getCurrentTextArea();
		if (!textArea.isLocal()) {
			throw new IllegalArgumentException("TypeScript.Error.NotLocalFile");
		}

		File dir = new File(textArea.getFileFullPath()).getParentFile();
		File tsConfig = new File(dir, "tsconfig.json");
		while (!tsConfig.isFile() && dir.getParentFile() != null) {
			dir = dir.getParentFile();
			tsConfig = new File(dir, "tsconfig.json");
		}

		if (!tsConfig.isFile()) {
			String desc = plugin.getBundle().getString(
					"TypeScript.Error.NoTsConfig");
			throw new IllegalArgumentException(desc);
		}

		return tsConfig;

	}


	/**
	 * The result of building via tsc.
	 */
	private static class Result {

		private File tsConfig;
		private ProcessRunner pr;

		private Result(File tsConfig, ProcessRunner pr) {
			this.tsConfig = tsConfig;
			this.pr = pr;
		}

	}


}