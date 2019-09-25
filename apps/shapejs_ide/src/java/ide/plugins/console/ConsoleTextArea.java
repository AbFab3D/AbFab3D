/*
 * 12/17/2010
 *
 * ConsoleTextArea.java - Text component for the console.
 * Copyright (C) 2010 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.console;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.text.TextAction;
import javax.swing.text.Utilities;

import ide.RTextUtilities;
import org.fife.ui.OptionsDialog;
import org.fife.util.SubstanceUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.RTextArea;


/**
 * Text component that displays a console of some type.  This component tries
 * to mimic jEdit's "Console" behavior, since that seemed to work pretty well.
 *
 * @author Robert Futrell
 * @version 1.0
 */
abstract class ConsoleTextArea extends JTextPane {

	public static final Color DEFAULT_MESSAGE_FG		= Color.blue;

	public static final Color DEFAULT_WARNING_FG		= Color.yellow;

	public static final Color DEFAULT_ERROR_FG	= Color.red;

	public static final Color DEFAULT_DARK_MESSAGE_FG	= new Color(0, 255, 255);

	public static final Color DEFAULT_DARK_WARNING_FG	= new Color(0xff, 0xFF, 0);

	public static final Color DEFAULT_DARK_ERROR_FG	= new Color(0xff, 0x00, 0x00);


	/**
	 * Property change event fired whenever a process is launched or
	 * completes.
	 */
	public static final String PROPERTY_PROCESS_RUNNING	= "ProcessRunning";

	public static final String STYLE_MESSAGE			= "message";
	public static final String STYLE_WARNING			= "warning";
	public static final String STYLE_ERROR				= "error";

	protected Plugin plugin;
	private JPopupMenu popup;
	private Listener listener;
	private int inputMinOffs;
	private LinkedList<String> cmdHistory;
	private int cmdHistoryIndex;

	/**
	 * Used to syntax highlight the current text being entered by the user.
	 */
	private RSyntaxDocument shDoc;

	/**
	 * The maximum number of lines to display in the console.
	 */
	private static final int MAX_LINE_COUNT				= 1500;


	/**
	 * Constructor.
	 */
	public ConsoleTextArea(Plugin plugin) {
		this.plugin = plugin;
		installDefaultStyles(false);
		fixKeyboardShortcuts();
		listener = new Listener();
		addMouseListener(listener);
		getDocument().addDocumentListener(listener);
		init();
		cmdHistory = new LinkedList<String>();
	}


	/**
	 * Appends text in the given style.  This method is thread-safe.
	 *
	 * @param text The text to append.
	 * @param style The style to use.
	 */
	public void append(String text, String style) {
		if (text==null) {
			return;
		}
		if (!text.endsWith("\n")) {
			text += "\n";
		}
		appendImpl(text, style);
	}


	/**
	 * Handles updating of the text component.  This method is thread-safe.
	 *
	 * @param text The text to append.
	 * @param style The style to apply to the appended text.
	 */
	protected void appendImpl(final String text, final String style) {
		appendImpl(text, style, false);
	}


	/**
	 * Handles updating of the text component.  This method is thread-safe.
	 *
	 * @param text The text to append.
	 * @param style The style to apply to the appended text.
	 * @param treatAsUserInput Whether to treat the text as user input.  This
	 *        determine whether the user can use backspace to remove this text
	 *        or not.
	 */
	protected void appendImpl(final String text, final String style,
			final boolean treatAsUserInput) {

		// Ensure the meat of this method is done on the EDT, to prevent
		// concurrency errors.
		if (SwingUtilities.isEventDispatchThread()) {

			Document doc = getDocument();
			int end = doc.getLength();
			try {
				doc.insertString(end, text, getStyle(style));
			} catch (BadLocationException ble) { // Never happens
				ble.printStackTrace();
			}
			setCaretPosition(doc.getLength());
			if (!treatAsUserInput) {
				inputMinOffs = getCaretPosition();
			}

			// Don't let the console's text get too long
			Element root = doc.getDefaultRootElement();
			int lineCount = root.getElementCount();
			if (lineCount>MAX_LINE_COUNT) {
				int toDelete = lineCount - MAX_LINE_COUNT;
				int endOffs = root.getElement(toDelete-1).getEndOffset();
				try {
					doc.remove(0, endOffs);
				} catch (BadLocationException ble) { // Never happens
					ble.printStackTrace();
				}
			}

		}

		else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					appendImpl(text, style, treatAsUserInput);
				}
			});
		}

	}


	/**
	 * Clears this console.  This should only be called on the EDT.
	 */
	public void clear() {
		Document doc = getDocument();
		setSelectionStart(0);
		setSelectionEnd(doc.getLength());
		super.replaceSelection(null);
		append(getUsageNote(), STYLE_MESSAGE);
	}


	/**
	 * Fixes the keyboard shortcuts for this text component so the user cannot
	 * accidentally delete any stdout or stderr, only stdin.
	 */
	protected void fixKeyboardShortcuts() {

		InputMap im = getInputMap();
		ActionMap am = getActionMap();
		int ctrl = getToolkit().getMenuShortcutKeyMask();

		// backspace
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "backspace");
		Action delegate = am.get(DefaultEditorKit.deletePrevCharAction);
		am.put("backspace", new BackspaceAction(delegate));

		// Just remove "delete previous word" for now, since DefaultEditorKit
		// doesn't expose the delegate for us to call into. 
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, ctrl),
				"deletePreviousWord");
		am.put("deletePreviousWord", new DeletePreviousWordAction());

		// delete
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		delegate = am.get(DefaultEditorKit.deleteNextCharAction);
		am.put("delete", new DeleteAction(delegate));

		// Just remove "delete next word" for now, since DefaultEditorKit
		// doesn't expose the delegate for us to call into. 
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ctrl), "invalid");

		// up - previous command in history
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
		am.put("up", new CommandHistoryAction(-1));

		// down - next command in history
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
		am.put("down", new CommandHistoryAction(1));

		// Home - go to start of input area (right after prompt)
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "home");
		delegate = am.get(DefaultEditorKit.beginLineAction);
		am.put("home", new HomeAction(delegate, false));

		// Shift+Home - Select to start of input area (right after prompt)
		int shift = InputEvent.SHIFT_MASK;
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, shift), "shiftHome");
		delegate = am.get(DefaultEditorKit.selectionBeginLineAction);
		am.put("shiftHome", new HomeAction(delegate, true));

		// Ctrl+A - Select all text entered after the prompt
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ctrl), "ctrlA");
		delegate = am.get(DefaultEditorKit.selectAllAction);
		am.put("ctrlA", new SelectAllAction(delegate));
	}


	/**
	 * Returns the currently entered text.
	 *
	 * @return The currently entered text.
	 */
	protected String getCurrentInput() {
		int startOffs = inputMinOffs;
		Document doc = getDocument();
		int len = doc.getLength() - startOffs;
		try {
			return doc.getText(startOffs, len);
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return null;
		}
	}


	/**
	 * Returns the syntax style that should be used for syntax highlighting
	 * input in this text area.
	 *
	 * @return The syntax style.
	 */
	protected abstract String getSyntaxStyle();


	/**
	 * Returns a usage note for this particular shell.
	 *
	 * @return The usage note.
	 */
	protected abstract String getUsageNote();


	/**
	 * Allows constructors to do stuff before the initial {@link #clear()}
	 * call is made.  The default implementation does nothing.
	 */
	protected void init() {
	}


	/**
	 * Installs the styles used by this text component.
	 * 
	 * @param checkForSubstance Whether to work around a Substance oddity
	 *        (Insubstantial 7.2.1).
	 */
	private void installDefaultStyles(boolean checkForSubstance) {

		Font font = RTextArea.getDefaultFont();
		if (!SubstanceUtil.isSubstanceInstalled()) {
			// If we do this with a SubstanceLookAndFeel installed, we go into
			// an infinite loop of updateUI()'s called (in calls to
			// SwingUtilities.invokeLater()).  For some reason, Substance has
			// to update JTextPaneUI's whenever the font changes.  Sigh...
			setFont(font);
		}

		restoreDefaultColors();
		setTabSize(4); // Do last

	}


	/**
	 * Replaces the command entered thus far with another one.  This is used
	 * when the user cycles through the command history.  This method should
	 * only be called on the EDT.
	 *
	 * @param command The command to replace with.
	 */
	private void replaceCommandEntered(String command) {
		setSelectionStart(inputMinOffs);
		setSelectionEnd(getDocument().getLength());
		replaceSelection(command);
	}


	/**
	 * Overridden to only allow the user to edit text they have entered (i.e.
	 * they can only edit "stdin").
	 *
	 * @param text The text to replace the selection with.
	 */
	@Override
	public void replaceSelection(String text) {

		int start = getSelectionStart();
		StyledDocument doc = (StyledDocument)getDocument();

		// Don't let the user remove any text they haven't typed (stdin).
		if (start<inputMinOffs) {
			setCaretPosition(doc.getLength());
		}

		// JUST IN CASE we aren't an AbstractDocument (paranoid), use remove()
		// and insertString() separately.
		try {
			start = getSelectionStart();
			doc.remove(start, getSelectionEnd()-start);
			doc.insertString(start, text, getStyle(STYLE_MESSAGE));
		} catch (BadLocationException ble) {
			UIManager.getLookAndFeel().provideErrorFeedback(this);
			ble.printStackTrace();
		}

	}


	/**
	 * Sets the tab size in this text pane.
	 *
	 * @param tabSize The new tab size, in characters.
	 */
	private void setTabSize(int tabSize) {

		FontMetrics fm = getFontMetrics(getFont());
		int charWidth = fm.charWidth('m');
		int tabWidth = charWidth * tabSize;

		// NOTE: Array length is arbitrary, represents the maximum number of
		// tabs handled on a single line.
		TabStop[] tabs = new TabStop[50];
		for (int j=0; j<tabs.length; j++) {
			tabs[j] = new TabStop((j+1)*tabWidth);
		}

		TabSet tabSet = new TabSet(tabs);
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setTabSet(attributes, tabSet);

		int length = getDocument().getLength();
		getStyledDocument().setParagraphAttributes(0, length, attributes, true);

	}


	/**
	 * Displays this text area's popup menu.
	 *
	 * @param e The location at which to display the popup.
	 */
	private void showPopupMenu(MouseEvent e) {

		if (popup==null) {
			popup = new JPopupMenu();
			popup.add(new JMenuItem(new CopyAllAction()));
			popup.addSeparator();
			popup.add(new JMenuItem(new ClearAllAction()));
			popup.addSeparator();
			popup.add(new JMenuItem(new ConfigureAction()));
		}

		popup.show(this, e.getX(), e.getY());

	}


	/**
	 * Called when the user toggles whether or not to syntax highlight user
	 * input in the options dialog.  This method changes the style of
	 * <b>only</b> the current user input to match the new preference.  Any
	 * previously submitted commands are not re-highlighted.
	 */
	void refreshUserInputStyles() {

		// If there's no partial user input, bail early.
		int start = inputMinOffs;
		StyledDocument doc = getStyledDocument();
		int end = doc.getLength();
		if (end==start) {
			return;
		}

		// Otherwise, change all current input to default "input" color.
		Style style = getStyle(STYLE_MESSAGE);
		doc.setCharacterAttributes(start, end, style, true);

	}


	/**
	 * Changes all consoles to use the default colors for the current
	 * application theme.
	 */
	public void restoreDefaultColors() {

		Font font = RTextArea.getDefaultFont();
		boolean isDark = RTextUtilities.isDarkLookAndFeel();

		Style defaultStyle = getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(defaultStyle, font.getFamily());
		StyleConstants.setFontSize(defaultStyle, font.getSize());

		/*Style stdin = */addStyle(STYLE_MESSAGE, defaultStyle);

		Style stdout = addStyle(STYLE_MESSAGE, defaultStyle);
		Color stdoutColor = isDark ? DEFAULT_DARK_MESSAGE_FG : DEFAULT_MESSAGE_FG;
		StyleConstants.setForeground(stdout, stdoutColor);

		Style stderr = addStyle(STYLE_WARNING, defaultStyle);
		Color stderrColor = isDark ? DEFAULT_DARK_WARNING_FG : DEFAULT_WARNING_FG;
		StyleConstants.setForeground(stderr, stderrColor);

		Style exception = addStyle(STYLE_ERROR, defaultStyle);
		StyleConstants.setForeground(exception, DEFAULT_ERROR_FG);
	}


	/**
	 * Overridden to also update the UI of the popup menu.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		installDefaultStyles(true);
		if (popup!=null) {
			SwingUtilities.updateComponentTreeUI(popup);
		}
	}


	/**
	 * Clears all text from this text area.
	 */
	private class ClearAllAction extends AbstractAction {

		public ClearAllAction() {
			putValue(NAME, plugin.getString("Action.ClearAll"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			clear();
		}
	}


	/**
	 * Brings up the options dialog panel for this plugin.
	 */
	private class ConfigureAction extends AbstractAction {

		public ConfigureAction() {
			putValue(NAME, plugin.getString("Action.Configure"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			OptionsDialog od = plugin.getRText().getOptionsDialog();
			od.initialize();
			od.setSelectedOptionsPanel(plugin.getString("Plugin.Name"));
			od.setVisible(true);
		}

	}


	private class CommandHistoryAction extends AbstractAction {

		private int amt;

		public CommandHistoryAction(int amt) {
			this.amt = amt;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int temp = cmdHistoryIndex + amt;
			if (temp<0 || temp>=cmdHistory.size()) {
				UIManager.getLookAndFeel().provideErrorFeedback(
												ConsoleTextArea.this);
				return;
			}
			cmdHistoryIndex = temp;
			String command = cmdHistory.get(cmdHistoryIndex);
			replaceCommandEntered(command);
		}

	}


	/**
	 * Copies all text from this text area.
	 */
	private class CopyAllAction extends AbstractAction {

		public CopyAllAction() {
			putValue(NAME, plugin.getString("Action.CopyAll"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int dot = getSelectionStart();
			int mark = getSelectionEnd();
			setSelectionStart(0);
			setSelectionEnd(getDocument().getLength());
			copy();
			setSelectionStart(dot);
			setSelectionEnd(mark);
		}
	}


	/**
	 * Action performed when backspace is pressed.
	 */
	private class BackspaceAction extends TextAction {

		/**
		 * DefaultEditorKit's DeletePrevCharAction.
		 */
		private Action delegate;

		public BackspaceAction(Action delegate) {
			super("backspace");
			this.delegate = delegate;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int start = getSelectionStart();
			int end = getSelectionEnd();
			if (start>=inputMinOffs && end!=start) {
				replaceSelection(null);
			}
			else if (start<=inputMinOffs) {
				UIManager.getLookAndFeel().
							provideErrorFeedback(ConsoleTextArea.this);
				if (start<inputMinOffs) {
					// Don't jump to the end of input if we were at the start
					setCaretPosition(getDocument().getLength());
				}
			}
			else {
				delegate.actionPerformed(e);
			}
		}

	}


	/**
	 * Action performed when delete is pressed.
	 */
	private class DeleteAction extends TextAction {

		/**
		 * DefaultEditorKit's DeleteNextCharAction.
		 */
		private Action delegate;

		public DeleteAction(Action delegate) {
			super("delete");
			this.delegate = delegate;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int start = getSelectionStart();
			if (start<inputMinOffs) {
				UIManager.getLookAndFeel().
							provideErrorFeedback(ConsoleTextArea.this);
			}
			else {
				delegate.actionPerformed(e);
			}
		}

	}


	/**
	 * Deletes the previous word.
	 */
	private class DeletePreviousWordAction extends TextAction {

		public DeletePreviousWordAction() {
			super("deletePreviousWord");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int start = getSelectionStart();
			int end = getSelectionEnd();
			if (start>=inputMinOffs && end!=start) {
				replaceSelection(null);
				return;
			}
			else if (start<=inputMinOffs) {
				UIManager.getLookAndFeel().
							provideErrorFeedback(ConsoleTextArea.this);
				return;
			}
			try {
				end = Utilities.getPreviousWord(ConsoleTextArea.this,start);
				if (end>=inputMinOffs) { // Should always be true
					int offs = Math.min(start, end);
					int len = Math.abs(end - start);
					getDocument().remove(offs, len);
				}
			} catch (BadLocationException ble) {
				ble.printStackTrace(); // Never happens
			}
		}

	}


	/**
	 * Moves the caret to the beginning of the input area.
	 */
	private class HomeAction extends AbstractAction {

		private Action delegate;
		private boolean select;

		public HomeAction(Action delegate, boolean select) {
			this.delegate = delegate;
			this.select = select;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (select) {
				int dot = getCaretPosition();
				if (dot>=inputMinOffs) {
					moveCaretPosition(inputMinOffs);
				}
				else { // Gotta do something - just do default
					delegate.actionPerformed(e);
				}
			}
			else {
				setCaretPosition(inputMinOffs);
			}
		}

	}


	/**
	 * Listens for events in this text area.
	 */
	private class Listener extends MouseAdapter implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent e) {
		}

		private void handleDocumentEvent(DocumentEvent e) {
		}

		private void handleMouseEvent(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			handleMouseEvent(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			handleMouseEvent(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			handleMouseEvent(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			handleDocumentEvent(e);
		}

	}


	/**
	 * Called when the user presses Ctrl+A.
	 */
	private class SelectAllAction extends TextAction {

		/**
		 * DefaultEditorKit's SelectAllAction;
		 */
		private Action delegate;

		public SelectAllAction(Action delegate) {
			super("SelectAll");
			this.delegate = delegate;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int start = getSelectionStart();
			if (start>=inputMinOffs) { // Select current command only
				setSelectionStart(inputMinOffs);
				setSelectionEnd(getDocument().getLength());
			}
			else { // Not after the prompt - just select everything
				delegate.actionPerformed(e);
			}
		}

	}


}