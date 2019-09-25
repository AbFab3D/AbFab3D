/*
 * 12/17/2010
 *
 * ConsoleWindow.java - Text component for the console.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.console;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import ide.ProjectListener;
import ide.RText;
import ide.RTextUtilities;
import org.fife.ui.RScrollPane;
import org.fife.ui.WebLookAndFeelUtils;
import org.fife.ui.dockablewindows.DockableWindow;
import org.j3d.util.ErrorReporter;
import org.web3d.util.HashSet;
import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.lang.InvalidFieldFormatException;
import org.web3d.vrml.lang.VRMLException;
import org.web3d.vrml.sav.Locator;
import org.web3d.vrml.sav.VRMLParseException;

import abfab3d.shapejs.Project;
import abfab3d.shapejs.ProjectItem;
import abfab3d.shapejs.Variant;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
import static ide.plugins.console.ConsoleTextArea.STYLE_ERROR;
import static ide.plugins.console.ConsoleTextArea.STYLE_MESSAGE;
import static ide.plugins.console.ConsoleTextArea.STYLE_WARNING;


/**
 * A dockable window that acts as a console.
 *
 * @author Alan Hudson
 * @version 1.0
 */
class ConsoleWindow extends DockableWindow implements ErrorReporter, ProjectListener {

//	private Plugin plugin;
	private CardLayout cards;
	private JPanel mainPanel;
	private ShapeJSTextArea textArea;

	private JToolBar toolbar;
	private ClearAction clearAction;

	/**
	 * The set of exceptions to ignore the stack trace for
	 */
	private HashSet<Class<?>> ignoredExceptionTypes;

	/**
	 * Output buffer
	 */
	private StringBuilder buf;

	PrintStream m_printStream;
	/**
	 * Locator used for printing out line/column information
	 */
	private Locator docLocator;



	public ConsoleWindow(RText app, Plugin plugin) {

//		this.plugin = plugin;
		setDockableWindowName(plugin.getString("DockableWindow.Title"));
		setIcon(plugin.getPluginIcon());
		setPosition(DockableWindow.BOTTOM);
		setLayout(new BorderLayout());

		ignoredExceptionTypes = new HashSet<>();
		
		// Create the main panel, containing the shells.
		cards = new CardLayout();
		mainPanel = new JPanel(cards);
		add(mainPanel);

		textArea = new ShapeJSTextArea(plugin);
		setPrimaryComponent(textArea);

		RScrollPane sp = new RScrollPane(textArea);
		RTextUtilities.removeTabbedPaneFocusTraversalKeyBindings(sp);
		mainPanel.add(sp, "System");

		// Create a "toolbar" for the shells.
		toolbar = new JToolBar();
		toolbar.setFloatable(false);

		JLabel label = new JLabel(plugin.getString("Shell"));
		Box temp = new Box(BoxLayout.LINE_AXIS);
		temp.add(label);
		temp.add(Box.createHorizontalStrut(5));

		temp.add(Box.createHorizontalGlue());
		JPanel temp2 = new JPanel(new BorderLayout());
		temp2.add(temp, BorderLayout.LINE_START);
		toolbar.add(temp2);
		toolbar.add(Box.createHorizontalGlue());

		clearAction = new ClearAction(app, Plugin.msg, plugin);
		JButton b = new JButton(clearAction);
		b.setText(null);
		toolbar.add(b);
		WebLookAndFeelUtils.fixToolbar(toolbar);
		add(toolbar, BorderLayout.NORTH);

		buf = new StringBuilder();

		app.addProjectListener(this);
	}


	/**
	 * Clears any text from all consoles.
	 */
	public void clearConsoles() {
		textArea.clear();
	}


	/**
	 * Returns the color used for a given type of text in the consoles.
	 *
	 * @param style The style; e.g. {@link ConsoleTextArea#STYLE_MESSAGE}.
	 * @return The color, or <code>null</code> if the system default color
	 *         is being used.
	 * @see #setForeground(String, Color)
	 */
	public Color getForeground(String style) {
		Color c = null;
		Style s = textArea.getStyle(style);
		if (s!=null) {
			c = StyleConstants.getForeground(s);
		}
		return c;
	}


	/**
	 * Returns whether a special style is used for a given type of text in
	 * the consoles.
	 *
	 * @param style The style of text.
	 * @return Whether a special style is used.
	 */
	public boolean isStyleUsed(String style) {
		return textArea.getStyle(style).isDefined(StyleConstants.Foreground);
		//return getForeground(style)!=null;
	}


	/**
	 * Changes all consoles to use the default colors for the current
	 * application theme.
	 */
	public void restoreDefaultColors() {
		textArea.restoreDefaultColors();
	}


	/**
	 * Sets the color used for a given type of text in the consoles.
	 *
	 * @param style The style; e.g. {@link ConsoleTextArea#STYLE_MESSAGE}.
	 * @param fg The new foreground color to use, or <code>null</code> to
	 *        use the system default foreground color.
	 * @see #getForeground(String)
	 */
	public void setForeground(String style, Color fg) {
		setForegroundImpl(style, fg, textArea);
	}


	/**
	 * Sets a color for a given type of a text in a single console.
	 *
	 * @param style
	 * @param fg
	 * @param textArea
	 */
	private static final void setForegroundImpl(String style, Color fg,
									ConsoleTextArea textArea) {
		Style s = textArea.getStyle(style);
		if (s!=null) {
			if (fg!=null) {
				StyleConstants.setForeground(s, fg);
			}
			else {
				s.removeAttribute(StyleConstants.Foreground);
			}
		}
	}


	@Override
	public void updateUI() {
		super.updateUI();
		if (toolbar!=null) {
			WebLookAndFeelUtils.fixToolbar(toolbar);
		}
	}

	//----------------------------------------------------------
	// Methods defined by ErrorReporter
	//----------------------------------------------------------

	/**
	 * Notification of an partial message from the system. When being written
	 * out to a display device, a partial message does not have a line
	 * termination character appended to it, allowing for further text to
	 * appended on that same line.
	 *
	 * @param msg The text of the message to be displayed
	 */
	public void partialReport(String msg) {
		textArea.append(msg, STYLE_MESSAGE);
	}

	/**
	 * Notification of an informational message from the system. For example,
	 * it may issue a message when a URL cannot be resolved.
	 *
	 * @param msg The text of the message to be displayed
	 */
	public final void messageReport(String msg) {

		//buf.append('\n'); ??
		buf.append(msg);
		//buf.append('\n');

		textArea.append(buf.toString(), STYLE_MESSAGE);

		// Clear
		buf.setLength(0);
	}

	/**
	 * print array of messages
	 */
	public final void messageReport(String msg[]) {
		for (int i = 0; i < msg.length; i++) {
			messageReport(fmt("%s\n", msg[i]));
		}
	}

	/**
	 * Notification of a warning in the way the system is currently operating.
	 * This is a non-fatal, non-serious error. For example you will get an
	 * warning when a value has been set that is out of range.
	 *
	 * @param msg The text of the message to be displayed
	 * @param e   The exception that caused this warning. May be null
	 */
	public void warningReport(String msg, Exception e) {
		warningReport(msg, (Throwable) e);
	}

	/**
	 * Notification of a warning in the way the system is currently operating.
	 * This is a non-fatal, non-serious error. For example you will get an
	 * warning when a value has been set that is out of range.
	 *
	 * @param msg The text of the message to be displayed
	 * @param e   The exception that caused this warning. May be null
	 */
	public void warningReport(String msg, Throwable e) {
		buf.append("Warning: ");

		if (e instanceof FieldException) {
			FieldException fe = (FieldException) e;

			String name = fe.getFieldName();
			if (name != null) {
				buf.append("Field name: ");
				buf.append(name);
			}
		}

		if (e instanceof VRMLParseException) {
			buf.append(" Line: ");
			buf.append(((VRMLParseException) e).getLineNumber());
			buf.append(" Column: ");
			buf.append(((VRMLParseException) e).getColumnNumber());
			buf.append('\n');
		} else if (e instanceof InvalidFieldFormatException) {
			buf.append(" Field name: ");
			buf.append(" Line: ");
			buf.append(((InvalidFieldFormatException) e).getLineNumber());
			buf.append(" Column: ");
			buf.append(((InvalidFieldFormatException) e).getColumnNumber());
			buf.append('\n');
		}

		if (msg != null) {
			buf.append(msg);
			buf.append('\n');
		}

		if (e != null) {
			String txt = e.getMessage();
			if (txt == null)
				txt = e.getClass().getName();

			buf.append(txt);
			buf.append('\n');

			if (!ignoredExceptionTypes.contains(e.getClass())) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				buf.append(sw.toString());
			}
		}

		textArea.append(buf.toString(), STYLE_WARNING);

		// Clear
		buf.setLength(0);
	}

	/**
	 * Notification of a recoverable error. This is a serious, but non-fatal
	 * error, for example trying to add a route to a non-existent node or the
	 * use of a node that the system cannot find the definition of.
	 *
	 * @param msg The text of the message to be displayed
	 * @param e   The exception that caused this warning. May be null
	 */
	public void errorReport(String msg, Exception e) {
		errorReport(msg, (Throwable) e);
	}

	/**
	 * Notification of a recoverable error. This is a serious, but non-fatal
	 * error, for example trying to add a route to a non-existent node or the
	 * use of a node that the system cannot find the definition of.
	 *
	 * @param msg The text of the message to be displayed
	 * @param e   The exception that caused this warning. May be null
	 */
	public void errorReport(String msg, Throwable e) {
		buf.append("Error: ");

		if (e instanceof FieldException) {
			FieldException fe = (FieldException) e;

			String name = fe.getFieldName();
			if (name != null) {
				buf.append("Field name: ");
				buf.append(name);
			}
		}

		if (e instanceof VRMLParseException) {
			buf.append(" Line: ");
			buf.append(((VRMLParseException) e).getLineNumber());
			buf.append(" Column: ");
			buf.append(((VRMLParseException) e).getColumnNumber());
			buf.append('\n');
		} else if (e instanceof InvalidFieldFormatException) {
			buf.append(" Line: ");
			buf.append(((InvalidFieldFormatException) e).getLineNumber());
			buf.append(" Column: ");
			buf.append(((InvalidFieldFormatException) e).getColumnNumber());
			buf.append('\n');
		}

		if (msg != null) {
			buf.append(msg);
			buf.append('\n');
		}

		if (e != null) {
			String txt = e.getMessage();
			if (txt == null)
				txt = e.getClass().getName();

			buf.append(txt);
			buf.append('\n');

			if (!ignoredExceptionTypes.contains(e.getClass())) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				buf.append(sw.toString());
			}
		}

		textArea.append(buf.toString(), STYLE_ERROR);

		// Clear
		buf.setLength(0);
	}

	/**
	 * Notification of a non-recoverable error that halts the entire system.
	 * After you receive this report the runtime system will no longer
	 * function - for example a non-recoverable parsing error. The best way
	 * out is to reload the file or restart the application internals.
	 *
	 * @param msg The text of the message to be displayed
	 * @param e   The exception that caused this warning. May be null
	 */
	public void fatalErrorReport(String msg, Exception e) {
		fatalErrorReport(msg, (Throwable) e);
	}

	/**
	 * Notification of a non-recoverable error that halts the entire system.
	 * After you receive this report the runtime system will no longer
	 * function - for example a non-recoverable parsing error. The best way
	 * out is to reload the file or restart the application internals.
	 *
	 * @param msg The text of the message to be displayed
	 * @param e   The exception that caused this warning. May be null
	 */
	public void fatalErrorReport(String msg, Throwable e) {
		buf.append("Fatal Error: ");

		if (e instanceof FieldException) {
			FieldException fe = (FieldException) e;

			String name = fe.getFieldName();
			if (name != null) {
				buf.append("Field name: ");
				buf.append(name);
			}
		}

		if (e instanceof VRMLParseException) {
			buf.append(" Line: ");
			buf.append(((VRMLParseException) e).getLineNumber());
			buf.append(" Column: ");
			buf.append(((VRMLParseException) e).getColumnNumber());
			buf.append('\n');
		} else if (e instanceof InvalidFieldFormatException) {
			buf.append(" Line: ");
			buf.append(((InvalidFieldFormatException) e).getLineNumber());
			buf.append(" Column: ");
			buf.append(((InvalidFieldFormatException) e).getColumnNumber());
			buf.append('\n');
		}

		if (msg != null) {
			buf.append(msg);
			buf.append('\n');
		}

		if (e != null) {
			String txt = e.getMessage();
			if (txt == null)
				txt = e.getClass().getName();

			buf.append(txt);
			buf.append('\n');

			if (!ignoredExceptionTypes.contains(e.getClass())) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				buf.append(sw.toString());
			}
		}

		textArea.append(buf.toString(), STYLE_ERROR);

		// Clear
		buf.setLength(0);
	}

	/**
	 * Redirect system messages to the console.
	 */
	public void redirectSystemMessages() {
		// TODO: Need to reimpl if desired

		/*
		PrintStream out =  new PrintStream(new SwingTextAreaOutputStream("System.out: ", errorField));
		System.setOut(out);

		PrintStream err =  new PrintStream(new SwingTextAreaOutputStream("System.err: ", errorField));
		System.setErr(err);
		*/
	}

	/**
	 * Send a warning message to the screen with the option of using the
	 * docLocator to present line number info.The string will have the prefix
	 * "Warning:" prepended to it.
	 *
	 * @param msg        The message to be written.
	 * @param useLocator true if line number information should be used
	 */
	private void warning(String msg, boolean useLocator) {

		buf.append("Warning: ");

		if (useLocator) {
			buf.append(" Line: ");
			buf.append(docLocator.getLineNumber());
			buf.append(" Column: ");
			buf.append(docLocator.getColumnNumber());
			buf.append(" ");
		}

		buf.append(msg);
		buf.append("\n");
		textArea.append(buf.toString(), STYLE_WARNING);

		// Clear
		buf.setLength(0);
	}

	/**
	 * Send an error message to the screen with the option of using the
	 * docLocator to present line number info.The string will have the prefix
	 * "Error:" prepended to it.
	 *
	 * @param msg        The message to be written
	 * @param useLocator true if line number information should be used
	 */
	private void error(String msg, boolean useLocator) {

		buf.append("Error: ");

		if (useLocator) {
			buf.append(" Line: ");
			buf.append(docLocator.getLineNumber());
			buf.append(" Column: ");
			buf.append(docLocator.getColumnNumber());
			buf.append(" ");
		}

		buf.append(msg);
		buf.append("\n");
		textArea.append(buf.toString(), STYLE_ERROR);

		// Clear
		buf.setLength(0);
	}

	//----------------------------------------------------------
	// Methods defined by ErrorHandler
	//----------------------------------------------------------

	/**
	 * Set the document locator that can be used by the implementing code to
	 * find out information about the current line information. This method
	 * is called by the parser to your code to give you a locator to work with.
	 * If this has not been set by the time <CODE>startDocument()</CODE> has
	 * been called, you can assume that you will not have one available.
	 *
	 * @param loc The locator instance to use
	 */
	public void setDocumentLocator(Locator loc) {
		docLocator = loc;
	}

	/**
	 * Notification of a warning in the way the code has been handled. The
	 * parser will continue through the file after this. Throw another
	 * exception if we want the parser to halt as a result.
	 *
	 * @param vpe The exception that caused this warning
	 * @throws VRMLException Create a further warning condition
	 */
	public void warning(VRMLException vpe) throws VRMLException {
		warning(vpe.getMessage(), true);
	}

	/**
	 * Notification of a recoverable error in the parsing. The parser will
	 * continue to keep parsing after this error. Throw another exception if
	 * we really want the parser to stop at this point.
	 *
	 * @param vpe The exception that caused this warning
	 * @throws VRMLException Create a further warning condition
	 */
	public void error(VRMLException vpe) throws VRMLException {
		error(vpe.getMessage(), true);
	}

	/**
	 * Notification of a non-recoverable error. The parser will not continue
	 * after calling this method. Throw another exception if we really want
	 * to make note of this, the parser will stop anyway.
	 *
	 * @param vpe The exception that caused this warning
	 * @throws VRMLException Create a further warning condition
	 */
	public void fatalError(VRMLException vpe) throws VRMLException {
		buf.append("Fatal Error: ");
		buf.append(" Line: ");
		buf.append(docLocator.getLineNumber());
		buf.append(" Column: ");
		buf.append(docLocator.getColumnNumber());
		buf.append(" ");
		buf.append(vpe.getMessage());
		buf.append("\n");

		textArea.append(buf.toString(), STYLE_ERROR);

		// Clear
		buf.setLength(0);

		throw vpe;
	}

    @Override
    public void setBusyMode() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        textArea.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    @Override
    public void setIdleMode() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        textArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }
    
	@Override
	public void projectChanged(Project proj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resourceAdded(ProjectItem res) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resourceRemoved(ProjectItem res) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resourceUpdated(ProjectItem res) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runVariant(Variant variant) {

	}

	@Override
	public void reset() {
		clearConsoles();
	}

	@Override
	public void setProjectUpdated(boolean updated) {

	}
}
