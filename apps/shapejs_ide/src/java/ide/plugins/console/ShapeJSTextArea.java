/*
 * 12/22/2010
 *
 * SystemShellTextArea.java - Text component simulating a system shell.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.console;

import org.fife.ui.OS;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 * A text area simulating a system shell.
 *
 * @author Alan Hudson
 * @version 1.0
 */
class ShapeJSTextArea extends ConsoleTextArea {

    private final boolean isWindows;

    public ShapeJSTextArea(Plugin plugin) {
        super(plugin);
        isWindows = plugin.getRText().getOS() == OS.WINDOWS;
    }


    @Override
    protected void fixKeyboardShortcuts() {

        super.fixKeyboardShortcuts();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSyntaxStyle() {
        return plugin.getRText().getOS() == OS.WINDOWS ?
                SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH :
                SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUsageNote() {
        return plugin.getString("Usage.Note.SystemShell");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void init() {
    }
}
