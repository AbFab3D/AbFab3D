/*
 * 11/14/2003
 *
 * RText.java - A syntax highlighting programmer's text editor written in Java.
 * Copyright (C) 2003 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.text.Element;

import abfab3d.param.Parameter;
import abfab3d.shapejs.*;
import abfab3d.util.SysErrorReporter;
import org.apache.commons.io.FilenameUtils;
import org.fife.help.HelpDialog;
import org.fife.jgoodies.looks.common.ShadowPopupFactory;
import org.fife.rsta.ui.CollapsibleSectionPanel;
import ide.actions.ActionFactory;
import org.fife.ui.CustomizableToolBar;
import org.fife.ui.OptionsDialog;
import org.fife.ui.SplashScreen;
import org.fife.ui.StandardAction;
import org.fife.ui.app.*;
import org.fife.util.SubstanceUtil;
import org.fife.ui.UIUtil;
import org.fife.ui.WebLookAndFeelUtils;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowConstants;
import org.fife.ui.dockablewindows.DockableWindowPanel;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.fife.ui.rsyntaxtextarea.FileLocation;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rtextarea.IconGroup;
import org.fife.ui.rtextfilechooser.FileChooserOwner;
import org.fife.ui.rtextfilechooser.RTextFileChooser;
import org.fife.util.TranslucencyUtil;
import org.j3d.util.ErrorReporter;

import static abfab3d.core.Output.printf;


/**
 * An instance of the RText text editor.  <code>RText</code> is a programmer's
 * text editor with the following features:
 *
 * <ul>
 *   <li>Syntax highlighting for 40+ languages.
 *   <li>Code folding.
 *   <li>Edit multiple documents simultaneously.
 *   <li>Auto-indent.
 *   <li>Find/Replace/Find in Files, with regular expression functionality.
 *   <li>Printing and Print Preview.
 *   <li>Online help.
 *   <li>Intelligent source browsing via Exuberant Ctags.
 *   <li>Macros.
 *   <li>Code templates.
 *   <li>Many other features.
 * </ul>
 *
 * At the heart of this program is
 * {@link org.fife.ui.rsyntaxtextarea.RSyntaxTextArea}, a fully-featured,
 * syntax highlighting text component.  That's where most of the meat is.
 * All text areas are contained in a subclass of
 * {@link ide.AbstractMainView}, which keeps the state of all of the
 * text areas in synch (fonts used, colors, etc.).  This class (RText) contains
 * an instance of a subclass of {@link ide.AbstractMainView} (which
 * contains all of the text areas) as well as the menu, source browser, and
 * status bar.
 *
 * @author Robert Futrell
 * @version 2.6.3
 */
public class RText extends AbstractPluggableGUIApplication<RTextPrefs>
			implements ActionListener, CaretListener, PropertyChangeListener,
						RTextActionInfo, FileChooserOwner, ProjectModel, ErrorReporter {

	// Constants specifying the current view style.
	public static final int TABBED_VIEW				= 0;
	public static final int SPLIT_PANE_VIEW				= 1;
	public static final int MDI_VIEW					= 2;

	// Properties fired.
	private static final String ICON_STYLE_PROPERTY		= "RText.iconStyle";
	private static final String MAIN_VIEW_STYLE_PROPERTY	= "RText.mainViewStyle";
	
	/**
	 * System property that, if set, causes RText to print timing information
	 * while it is starting up.
	 */
	private static final String PROPERTY_PRINT_START_TIMES = "printStartTimes";

	public static final String VERSION_STRING		= "2.6.4.BETA-????????";

	private Map<String, IconGroup> iconGroupMap;

	private RTextMenuBar menuBar;

	private OptionsDialog optionsDialog;

	private CollapsibleSectionPanel csp; // Contains the AbstractMainView
	private AbstractMainView mainView;	// Component showing all open documents.
	private int mainViewStyle;

	private RTextFileChooser chooser;
	private RemoteFileChooser rfc;

	private HelpDialog helpDialog;

	private SpellingErrorWindow spellingWindow;

	private SyntaxScheme colorScheme;

	private IconGroup iconGroup;

	private String workingDirectory;	// The directory for new empty files.

	private String newFileName;		// The name for new empty text files.

	private SearchToolBar searchBar;

	private boolean showHostName;

	/**
	 * Whether <code>searchWindowOpacityListener</code> has been attempted to be
	 * created yet. This is kept in a variable instead of checking for
	 * <code>null</code> because the creation is done via reflection (since
	 * we're 1.4-compatible), so it is a fairly common case that creation is
	 * attempted but fails.
	 */
	private boolean windowListenersInited;

	/**
	 * Listens for focus events of certain child windows (those that can
	 * be made translucent on focus lost).
	 */
	private ChildWindowListener searchWindowOpacityListener;

	/**
	 * Whether the Find and Replace dialogs can have their opacity changed.
	 */
	private boolean searchWindowOpacityEnabled;

	/**
	 * The opacity with which to render unfocused child windows that support
	 * opacity changes.
	 */
	private float searchWindowOpacity;

	/**
	 * The rule used for making certain unfocused child windows translucent.
	 */
	private int searchWindowOpacityRule;

	/**
	 * The (lazily created) name of localhost.  Do not access this field
	 * directly; instead, use {@link #getHostName()}.
	 */
	private String hostName;

	private RecentFileManager recentFileManager;

	/**
	 * Used as a "hack" to re-load the Options dialog if the user opens it
	 * too early, before all plugins have added their options to it.
	 */
	private int lastPluginCount;

	/** Has any project resource (script, variant, file resource been updated? */
	private boolean projectUpdated;

	/**
	 * Notifications about project changes
	 */
	private EventListenerList projectListeners;
	private Project currProj;
	private Variant currVariant;
	
	private Map<String, VariantItem> projVariants;
	private ErrorReporter errorReporter;
	
	private EventListenerList renderEngineListeners;
	private EventListenerList saveModelListeners;
	private String lastOpenProjectFile;
	private String lastExportProjectDir;
	
	private EventListenerList viewListeners;

    private ArrayList<String> libDirs = new ArrayList<>();
    
    private boolean busy;


	/**
	 * Creates an instance of the <code>RText</code> editor.
	 *
	 * @param filesToOpen Array of <code>java.lang.String</code>s containing
	 *        the files we want to open initially.  This can be
	 *        <code>null</code> if no files are to be opened.
	 */
	public RText(String[] filesToOpen) {
		super("ShapeJS IDE");
		init(filesToOpen);
		projVariants = new HashMap<String, VariantItem>();
		projectUpdated = false;
		busy = false;
	}


	/**
	 * Creates an instance of the <code>RText</code> editor.
	 *
	 * @param filesToOpen Array of <code>java.lang.String</code>s containing
	 *        the files we want to open initially.  This can be
	 *        <code>null</code> if no files are to be opened.
	 * @param preferences The preferences with which to initialize this RText.
	 */
	public RText(String[] filesToOpen, RTextPrefs preferences) {
		super("ShapeJS IDE", preferences);
		init(filesToOpen);
		projVariants = new HashMap<String, VariantItem>();
		projectUpdated = false;
		busy = false;
	}


	// What to do when user does something.
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (command.equals("TileVertically")) {
			((RTextMDIView)mainView).tileWindowsVertically();
		}

		else if (command.equals("TileHorizontally")) {
			((RTextMDIView)mainView).tileWindowsHorizontally();
		}

		else if (command.equals("Cascade")) {
			((RTextMDIView)mainView).cascadeWindows();
		}

	}


	public List<String> getLibDirs() {
	    return libDirs;
    }

	// TODO
	public void addDockableWindow(DockableWindow wind) {
		((DockableWindowPanel)mainContentPanel).addDockableWindow(wind);
	}


	/**
	 * Returns whether or not tabs are emulated with spaces (i.e. "soft" tabs).
	 * This simply calls <code>mainView.areTabsEmulated</code>.
	 *
	 * @return <code>true</code> if tabs are emulated with spaces;
	 *         <code>false</code> if they aren't.
	 */
	public boolean areTabsEmulated() {
		return mainView.areTabsEmulated();
	}


	/**
	 * Called when cursor in text editor changes position.
	 *
	 * @param e The caret event.
	 */
	@Override
	public void caretUpdate(CaretEvent e) {

		// NOTE: e may be "null"; we do this sometimes to force caret
		// updates to update e.g. the current line highlight.
		RTextEditorPane textArea = mainView.getCurrentTextArea();
		int dot = textArea.getCaretPosition();//e.getDot();

		// Update row/column information in status field.
		Element map = textArea.getDocument().getDefaultRootElement();
		int line = map.getElementIndex(dot);
		int lineStartOffset = map.getElement(line).getStartOffset();
		((StatusBar)getStatusBar()).setRowAndColumn(
									line+1, dot-lineStartOffset+1);

	}


	/**
	 * Converts all instances of a number of spaces equal to a tab in all open
	 * documents into tabs.
	 *
	 * @see #convertOpenFilesTabsToSpaces
	 */
	private void convertOpenFilesSpacesToTabs() {
		mainView.convertOpenFilesSpacesToTabs();
	}


	/**
	 * Converts all tabs in all open documents into an equivalent number of
	 * spaces.
	 *
	 * @see #convertOpenFilesSpacesToTabs
	 */
	private void convertOpenFilesTabsToSpaces() {
		mainView.convertOpenFilesTabsToSpaces();
	}


	/**
	 * Returns the About dialog for this application.
	 *
	 * @return The About dialog.
	 */
	@Override
	protected JDialog createAboutDialog() {
		return new AboutDialog(this);
	}


	/**
	 * Creates the array of actions used by this RText.
	 *
	 * @param prefs The RText properties for this RText instance.
	 */
	@Override
	protected void createActions(RTextPrefs prefs) {
		ActionFactory.addActions(this, prefs);
		loadActionShortcuts(getShortcutsFile());
	}


	/**
	 * Creates and returns the menu bar used in this application.
	 *
	 * @param prefs This GUI application's preferences.
	 * @return The menu bar.
	 */
	@Override
	protected JMenuBar createMenuBar(RTextPrefs prefs) {

		//splashScreen.updateStatus(msg.getString("CreatingMenuBar"), 75);

		// Create the menu bar.
		menuBar = new RTextMenuBar(this, UIManager.getLookAndFeel().getName(),
				prefs);
		menuBar.setWindowMenuVisible(prefs.mainView==MDI_VIEW);

		return menuBar;

	}


	/**
	 * Returns the splash screen to display while this GUI application is
	 * loading.
	 *
	 * @return The splash screen.  If <code>null</code> is returned, no
	 *         splash screen is displayed.
	 */
	@Override
	protected SplashScreen createSplashScreen() {
		String img = "ide/graphics/" + getString("Splash");

		return new SplashScreen(img, getString("Initializing"));
	}


	/**
	 * Returns the status bar to be used by this application.
	 *
	 * @param prefs This GUI application's preferences.
	 * @return The status bar.
	 */
	@Override
	protected org.fife.ui.StatusBar createStatusBar(RTextPrefs prefs) {
		StatusBar sb = new StatusBar(this, getString("Ready"),
					!prefs.wordWrap, 1,1,
					prefs.textMode==RTextEditorPane.OVERWRITE_MODE);
		sb.setStyle(prefs.statusBarStyle);
		return sb;
	}


	/**
	 * Creates and returns the toolbar to be used by this application.
	 *
	 * @param prefs This GUI application's preferences.
	 * @return The toolbar.
	 */
	@Override
	protected CustomizableToolBar createToolBar(RTextPrefs prefs) {

		ToolBar toolBar = new ToolBar("rtext - Toolbar", this,
								(StatusBar)getStatusBar());

		// Make the toolbar use the large versions of the icons if available.
		// FIXME:  Make toggle-able.
		toolBar.checkForLargeIcons();

		return toolBar;

	}


	/**
	 * Overridden so we can syntax highlight the Java exception displayed.
	 *
	 * @param owner The dialog that threw the Exception.
	 * @param t The exception/throwable that occurred.
	 * @param desc A short description of the error.  This can be
	 *        <code>null</code>.
	 */
	@Override
	public void displayException(Dialog owner, Throwable t, String desc) {
		ExceptionDialog ed = new ExceptionDialog(owner, t);
		if (desc!=null) {
			ed.setDescription(desc);
		}
		ed.setLocationRelativeTo(owner);
		ed.setTitle(getString("ErrorDialogTitle"));
		ed.setVisible(true);
	}


	/**
	 * Overridden so we can syntax highlight the Java exception displayed.
	 *
	 * @param owner The child frame that threw the Exception.
	 * @param t The exception/throwable that occurred.
	 * @param desc A short description of the error.  This can be
	 *        <code>null</code>.
	 */
	@Override
	public void displayException(Frame owner, Throwable t, String desc) {
		ExceptionDialog ed = new ExceptionDialog(owner, t);
		if (desc!=null) {
			ed.setDescription(desc);
		}
		ed.setLocationRelativeTo(owner);
		ed.setTitle(getString("ErrorDialogTitle"));
		ed.setVisible(true);
	}


	/**
	 * Called when the user attempts to close the application, whether from
	 * an "Exit" menu item, closing the main application window, or any other
	 * means.  The user is prompted to save any dirty documents, and this
	 * RText instance is closed.
	 */
	@Override
	public void doExit() {

		// Attempt to close all open documents.
		boolean allDocumentsClosed = getMainView().closeAllDocuments();

		// Assuming all documents closed okay (ie, the user
		// didn't click "Cancel")...
		if (allDocumentsClosed) {

			// If there will be no more rtext's running, stop the JVM.
			if (StoreKeeper.getInstanceCount()==1) {
				savePreferences();
				boolean saved = RTextEditorPane.saveTemplates();
				if (!saved) {
					String title = getString("ErrorDialogTitle");
					String text = getString("TemplateSaveError");
					JOptionPane.showMessageDialog(this, text, title,
										JOptionPane.ERROR_MESSAGE);
				}
				// Save file chooser "Favorite Directories".  It is
				// important to check that the chooser exists here, as
				// if it doesn't, there's no need to do this!  If we
				// don't, the saveFileChooseFavorites() method will
				// create the file chooser itself just to save the
				// favorites!
				if (chooser!=null) {
					RTextUtilities.saveFileChooserFavorites(this);
				}
				AWTExceptionHandler.shutdown();
				System.exit(0);
			}

			// If there will still be some RText instances running, just
			// stop this instance.
			else {
				setVisible(false);
				StoreKeeper.removeRTextInstance(this);
				this.dispose();
			}

		}

	}


	/**
	 * Focuses the specified dockable window group.  Does nothing if there
	 * are no dockable windows at the location specified.
	 *
	 * @param group The dockable window group to focus.
	 */
	public void focusDockableWindowGroup(int group) {
		DockableWindowPanel dwp = (DockableWindowPanel)mainContentPanel;
		if (!dwp.focusDockableWindowGroup(group)) { // Should never happen
			UIManager.getLookAndFeel().provideErrorFeedback(this);
		}
	}


	/**
	 * Returns the filename used for newly created, empty text files.  This
	 * value is locale-specific.
	 *
	 * @return The new text file name.
	 */
	public String getNewFileName() {
		return newFileName;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public OptionsDialog getOptionsDialog() {

		int pluginCount = getPlugins().length;

		// Check plugin count and re-create dialog if it has changed.  This
		// is because the user can be quick and open the Options dialog before
		// all plugins have loaded.  A real solution is to have some sort of
		// options manager that plugins can add options panels to.
		if (optionsDialog==null || pluginCount!=lastPluginCount) {
			optionsDialog = new ide.optionsdialog.
												OptionsDialog(this);
			optionsDialog.setLocationRelativeTo(this);
		}

		return optionsDialog;

	}


	/**
	 * Returns the application's "collapsible section panel;" that is, the
	 * panel containing the main view and possible find/replace tool bars.
	 *
	 * @return The collapsible section panel.
	 * @see #getMainView()
	 */
	CollapsibleSectionPanel getCollapsibleSectionPanel() {
		return csp;
	}


	/**
	 * Returns the file chooser being used by this RText instance.
	 *
	 * @return The file chooser.
	 * @see #getRemoteFileChooser()
	 */
	@Override
	public RTextFileChooser getFileChooser() {
		if (chooser==null) {
			chooser = RTextUtilities.createFileChooser(this);
		}
		return chooser;
	}


	/**
	 * Returns the focused dockable window group.
	 *
	 * @return The focused window group, or <code>-1</code> if no dockable
	 *         window group is focused.
	 * @see DockableWindowConstants
	 */
	public int getFocusedDockableWindowGroup() {
		DockableWindowPanel dwp = (DockableWindowPanel)mainContentPanel;
		return dwp.getFocusedDockableWindowGroup();
	}


	/**
	 * Returns the Help dialog for RText.
	 *
	 * @return The Help dialog.
	 * @see org.fife.ui.app.GUIApplication#getHelpDialog
	 */
	@Override
	public HelpDialog getHelpDialog() {
		// Create the help dialog if it hasn't already been.
		if (helpDialog==null) {
			String contentsPath = getInstallLocation() + "/doc/";
			String helpPath = contentsPath + getLanguage() + "/";
			// If localized help does not exist, default to English.
			File test = new File(helpPath);
			if (!test.isDirectory())
				helpPath = contentsPath + "en/";
			helpDialog = new HelpDialog(this,
						contentsPath + "HelpDialogContents.xml",
						helpPath);
			helpDialog.setBackButtonIcon(iconGroup.getIcon("back"));
			helpDialog.setForwardButtonIcon(iconGroup.getIcon("forward"));
		}
		helpDialog.setLocationRelativeTo(this);
		return helpDialog;
	}


	/**
	 * Returns the name of the local host.  This is lazily discovered.
	 *
	 * @return The name of the local host.
	 */
	private synchronized String getHostName() {
		if (hostName==null) {
			try {
				hostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException uhe) { // Should never happen
				hostName = "Unknown";
			}
		}
		return hostName;
	}


	/**
	 * Returns the icon group being used for icons for actions.
	 *
	 * @return The icon group.
	 */
	public IconGroup getIconGroup() {
		return iconGroup;
	}


	/**
	 * Returns the icon groups available to RText.
	 *
	 * @return The icon groups.
	 */
	public Map<String, IconGroup> getIconGroupMap() {
		return iconGroupMap;
	}


	/**
	 * Returns the actual main view.
	 *
	 * @return The main view.
	 * @see #getMainViewStyle()
	 * @see #setMainViewStyle(int)
	 */
	public AbstractMainView getMainView() {
		return mainView;
	}


	/**
	 * Returns the main view style.
	 *
	 * @return The main view style, one of {@link #TABBED_VIEW},
	 *         {@link #SPLIT_PANE_VIEW} or {@link #MDI_VIEW}.
	 * @see #setMainViewStyle(int)
	 * @see #getMainView()
	 */
	public int getMainViewStyle() {
		return mainViewStyle;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getPreferencesClassName() {
		return "ide.RTextPrefs";
	}


	/**
	 * Returns the list of most recently opened files, least-recently opened
	 * first.
	 *
	 * @return The list of files.  This may be empty but will never be
	 *         <code>null</code>.
	 */
	java.util.List<FileLocation> getRecentFiles() {
		return recentFileManager.getRecentFiles();
	}


	/**
	 * Returns the file chooser used to select remote files.
	 *
	 * @return The file chooser.
	 * @see #getFileChooser()
	 */
	public RemoteFileChooser getRemoteFileChooser() {
		if (rfc==null) {
			rfc = new RemoteFileChooser(this);
		}
		return rfc;
	}


	/**
	 * Returns the fully-qualified class name of the resource bundle for this
	 * application.  This is used by {@link #getResourceBundle()} to locate
	 * the class.
	 *
	 * @return The fully-qualified class name of the resource bundle.
	 * @see #getResourceBundle()
	 */
	@Override
	public String getResourceBundleClassName() {
		return "ide.RText";
	}


	/**
	 * Returns the QuickSearch toolbar.
	 *
	 * @return The QuickSearch toolbar.
	 * @see #isSearchToolBarVisible
	 */
	public SearchToolBar getSearchToolBar() {
		if (searchBar==null) {
			searchBar = new SearchToolBar("Search", this,
						(ide.StatusBar)getStatusBar());
			searchBar.setVisible(false);
			addToolBar(searchBar, BorderLayout.SOUTH);
		}
		return searchBar;
	}


	/**
	 * Returns the opacity with which to render unfocused child windows, if
	 * this option is enabled.
	 *
	 * @return The opacity.
	 * @see #setSearchWindowOpacity(float)
	 */
	public float getSearchWindowOpacity() {
		return searchWindowOpacity;
	}


	/**
	 * Returns the rule used for making certain child windows translucent.
	 *
	 * @return The rule.
	 * @see #setSearchWindowOpacityRule(int)
	 * @see #getSearchWindowOpacity()
	 */
	public int getSearchWindowOpacityRule() {
		return searchWindowOpacityRule;
	}


	/**
	 * Returns the file in which to load and save user-customized keyboard
	 * shortcuts.
	 *
	 * @return The shortcuts file.
	 */
	private static File getShortcutsFile() {
		return new File(RTextUtilities.getPreferencesDirectory(),
				"shortcuts.properties");
	}


	/**
	 * Returns whether the hostname should be shown in the title of the
	 * main RText window.
	 *
	 * @return Whether the hostname should be shown.
	 * @see #setShowHostName(boolean)
	 */
	public boolean getShowHostName() {
		return showHostName;
	}


	/**
	 * Returns the syntax highlighting color scheme being used.
	 *
	 * @return The syntax highlighting color scheme.
	 * @see #setSyntaxScheme(SyntaxScheme)
	 */
	public SyntaxScheme getSyntaxScheme() {
		return colorScheme;
	}


	/**
	 * Returns the tab size (in spaces) currently being used.
	 *
	 * @return The tab size (in spaces) currently being used.
	 * @see #setTabSize(int)
	 */
	private int getTabSize() {
		return mainView.getTabSize();
	}


	/**
	 * Returns the title of this window, less any "header" information
	 * (e.g. without the leading "<code>rtext - </code>").
	 *
	 * @return The title of this window.
	 * @see #setTitle(String)
	 */
	@Override
	public String getTitle() {
		String title = super.getTitle();
		int hyphen = title.indexOf("- ");
		if (hyphen>-1) { // Should always be true
			title = title.substring(hyphen+2);
		}
		return title;
	}


	/**
	 * Returns the version string for this application.
	 *
	 * @return The version string.
	 */
	@Override
	public String getVersionString() {
		return VERSION_STRING;
	}


	/**
	 * Returns the "working directory;" that is, the directory that new, empty
	 * files are created in.
	 *
	 * @return The working directory.  There will be no trailing '/' or '\'.
	 * @see #setWorkingDirectory
	 */
	public String getWorkingDirectory() {
		return workingDirectory;
	}


	/**
	 * Does the dirty work of actually installing a plugin.  This method
	 * ensures the current text area retains focus even after a GUI plugin
	 * is added.
	 *
	 * @param plugin The plugin to install.
	 */
	@Override
	protected void handleInstallPlugin(Plugin plugin) {
		// Normally we don't have to check currentTextArea for null, but in
		// this case, we do.  Plugins are installed at startup, after the main
		// window is displayed.  If the user passes in a filename to open, but
		// that file doesn't exist, RText will prompt with "File XXX does not
		// exist, create it?", and in that time, currentTextArea will be null.
		// Plugins, in the meantime, will try to load and find the null value.
		RTextEditorPane textArea = getMainView().getCurrentTextArea();
		if (textArea!=null) {
			textArea.requestFocusInWindow();
		}
	}


	/**
	 * Returns whether dockable windows are at the specified location.
	 *
	 * @param group A constant from {@link DockableWindowConstants}
	 * @return Whether dockable windows are at the specified location.
	 */
	public boolean hasDockableWindowGroup(int group) {
		DockableWindowPanel dwp = (DockableWindowPanel)mainContentPanel;
		return dwp.hasDockableWindowGroup(group);
	}


	/**
	 * Called at the end of RText constructors.  Does common initialization
	 * for RText.
	 *
	 * @param filesToOpen Any files to open.  This can be <code>null</code>.
	 */
	private void init(String[] filesToOpen) {
		lastPluginCount = -1;
		projectListeners = new EventListenerList();
		renderEngineListeners = new EventListenerList();
		saveModelListeners = new EventListenerList();
		viewListeners = new EventListenerList();

		/*
        // TODO: Add config option
        libDirs.add(new File("/cygwin64/home/giles/projs/shapeways/git/customco").getAbsolutePath());
*/
		openFiles(filesToOpen);
	}

	/**
	 * Adds a project listener.
	 *
	 * @param l The listener to add.
	 * @see #removeProjectListener
	 */
	public void addProjectListener(ProjectListener l) {
		projectListeners.add(ProjectListener.class, l);
	}

	/**
	 * Removes a project listener.
	 *
	 * @param l The listener to remove.
	 * @see #addProjectListener
	 */
	public void removeProjectListener(ProjectListener l) {
		projectListeners.remove(ProjectListener.class, l);
	}

	public EventListenerList getProjectListeners() {
		return projectListeners;
	}
	
	/**
	 * Adds a server listener.
	 *
	 * @param l The listener to add.
	 */
	public void addRenderEngineListener(RenderEngineListener l) {
		renderEngineListeners.add(RenderEngineListener.class, l);
	}
	
	public EventListenerList getRenderEngineListeners() {
		return renderEngineListeners;
	}
	
	/**
	 * Adds a server listener.
	 *
	 * @param l The listener to add.
	 */
	public void addSaveModelListener(SaveModelListener l) {
		saveModelListeners.add(SaveModelListener.class, l);
	}
	
	public EventListenerList getSaveModelListeners() {
		return saveModelListeners;
	}
	
	/**
	 * Add a viewpoint listener.
	 * 
	 * @param l The listener to add.
	 */
	public void addViewListener(ViewListener l) {
	    viewListeners.add(ViewListener.class, l);
	}
	
    public EventListenerList getViewListeners() {
        return viewListeners;
    }
	
	public Map<String, VariantItem> getProjVariants() {
		return projVariants;
	}
	
	public File[] getOpenFiles() {
		return mainView.getOpenFiles();
	}
	
	public boolean isFileOpen(String name) {
		File[] files = mainView.getOpenFiles();
		for (File f : files) {
			if (f.getName().equals(name)) return true;
		}
		
		return false;
	}
	
	/**
	 * Get the current loaded project.
	 * @return
	 */
	public Project getCurrProject() {
		return currProj;
	}
	
	/**
	 * Get the current loaded variant.
	 * @return
	 */
	public Variant getCurrVariant() {
		return currVariant;
	}

	/**
	 * Returns whether or not the QuickSearch toolbar is visible.  This
	 * method should be used over <code>getSearchToolBar().isVisible()</code>
	 * because the latter will allocate the toolbar if it isn't already
	 * created, but this method won't.
	 *
	 * @return Whether or not the QuickSearch toolbar is visible.
	 * @see #getSearchToolBar
	 */
	public boolean isSearchToolBarVisible() {
		return searchBar != null && searchBar.isVisible();
	}


	/**
	 * Returns whether search window opacity is enabled.
	 *
	 * @return Whether search window opacity is enabled.
	 * @see #setSearchWindowOpacityEnabled(boolean)
	 */
	public boolean isSearchWindowOpacityEnabled() {
		return searchWindowOpacityEnabled;
	}


	/**
	 * Returns whether the spelling window is visible.
	 *
	 * @return Whether the spelling window is visible.
	 * @see #setSpellingWindowVisible(boolean)
	 */
	public boolean isSpellingWindowVisible() {
		return spellingWindow!=null && spellingWindow.isActive();
	}


	/**
	 * Loads and validates the icon groups available to RText.
	 */
	private void loadPossibleIconGroups() {
		iconGroupMap = IconGroupLoader.loadIconGroups(this,
					getInstallLocation() + "/icongroups/ExtraIcons.xml");
	}


	/**
	 * Thanks to Java Bug ID 5026829, JMenuItems (among other Swing components)
	 * don't update their accelerators, etc. when the properties on which they
	 * were created update them.  Thus, we have to do this manually.  This is
	 * still broken as of 1.5.
	 */
	public void menuItemAcceleratorWorkaround() {
		menuBar.menuItemAcceleratorWorkaround();
	}


	/**
	 * Opens the specified files.
	 *
	 * @param filesToOpen The files to open.  This can be <code>null</code>.
	 * @see #openFile
	 */
	private void openFiles(String[] filesToOpen) {
		int count = filesToOpen==null ? 0 : filesToOpen.length;
		for (int i=0; i<count; i++) {
			openFile(filesToOpen[i]);
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void preDisplayInit(RTextPrefs prefs, SplashScreen splashScreen) {

		long start = System.currentTimeMillis();

		// Some stuff down the line may assume this directory exists!
		File prefsDir = RTextUtilities.getPreferencesDirectory();
		if (!prefsDir.isDirectory()) {
			prefsDir.mkdirs();
		}

		// Install any plugins.
		super.preDisplayInit(prefs, splashScreen);

		if (prefs.searchToolBarVisible) {
			addToolBar(getSearchToolBar(), BorderLayout.SOUTH);
			searchBar.setVisible(true);
		}

		splashScreen.updateStatus(getString("AddingFinalTouches"), 90);

		// If the user clicks the "X" in the top-right of the window, do nothing.
		// (We'll clean up in our window listener).
		addWindowListener( new RTextWindowListener(this) );
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		mainView.setLineNumbersEnabled(prefs.lineNumbersVisible);

		// Enable templates in text areas.
		if (RTextUtilities.enableTemplates(this, true)) {
			// If there are no templates, assume this is the user's first
			// time in RText and add some "standard" templates.
			CodeTemplateManager ctm = RTextEditorPane.getCodeTemplateManager();
			if (ctm.getTemplateCount()==0) {
				RTextUtilities.addDefaultCodeTemplates();
			}
		}

		setSearchWindowOpacityEnabled(prefs.searchWindowOpacityEnabled);
		setSearchWindowOpacity(prefs.searchWindowOpacity);
		setSearchWindowOpacityRule(prefs.searchWindowOpacityRule);

		recentFileManager = new RecentFileManager(this);

		if (Boolean.getBoolean(PROPERTY_PRINT_START_TIMES)) {
			System.err.println("preDisplayInit: " + (System.currentTimeMillis()-start));
		}

		RTextUtilities.setDropShadowsEnabledInEditor(prefs.dropShadowsInEditor);

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void preMenuBarInit(RTextPrefs prefs, SplashScreen splashScreen) {

		long start = System.currentTimeMillis();

		// Make the split pane positions same as last time.
		setSplitPaneDividerLocation(TOP, prefs.dividerLocations[TOP]);
		setSplitPaneDividerLocation(LEFT, prefs.dividerLocations[LEFT]);
		setSplitPaneDividerLocation(BOTTOM, prefs.dividerLocations[BOTTOM]);
		setSplitPaneDividerLocation(RIGHT, prefs.dividerLocations[RIGHT]);

		// Show any docked windows
		setSpellingWindowVisible(prefs.viewSpellingList);

		setShowHostName(prefs.showHostName);

		if (Boolean.getBoolean(PROPERTY_PRINT_START_TIMES)) {
			System.err.println("preMenuBarInit: " + (System.currentTimeMillis()-start));
		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void preStatusBarInit(RTextPrefs prefs,
							SplashScreen splashScreen) {

		long start = System.currentTimeMillis();

		final String[] filesToOpen = null;

		// Initialize our "new, empty text file" name.
		newFileName = getString("NewFileName");

		splashScreen.updateStatus(getString("SettingSHColors"), 10);
		setSyntaxScheme(prefs.colorScheme);

		setWorkingDirectory(prefs.workingDirectory);

		splashScreen.updateStatus(getString("CreatingView"), 20);

		// Initialize our view object.
		switch (prefs.mainView) {
			case TABBED_VIEW:
				mainViewStyle = TABBED_VIEW;
				mainView = new RTextTabbedPaneView(RText.this, filesToOpen, prefs);
				break;
			case SPLIT_PANE_VIEW:
				mainViewStyle = SPLIT_PANE_VIEW;
				mainView = new RTextSplitPaneView(RText.this, filesToOpen, prefs);
				break;
			default:
				mainViewStyle = MDI_VIEW;
				mainView = new RTextMDIView(RText.this, filesToOpen, prefs);
				break;
		}

		csp = new CollapsibleSectionPanel(false);
		csp.add(mainView);
		getContentPane().add(csp);

		splashScreen.updateStatus(getString("CreatingStatusBar"), 25);

		if (Boolean.getBoolean(PROPERTY_PRINT_START_TIMES)) {
			System.err.println("preStatusBarInit: " + (System.currentTimeMillis()-start));
		}

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void preToolBarInit(RTextPrefs prefs, SplashScreen splashScreen) {

		long start = System.currentTimeMillis();

		StatusBar statusBar = (StatusBar)getStatusBar();
		mainView.addPropertyChangeListener(statusBar);

		loadPossibleIconGroups();
		try {
			setIconGroupByName(prefs.iconGroupName);
		} catch (InternalError ie) {
			displayException(ie);
			System.exit(0);
		}

		splashScreen.updateStatus(getString("CreatingToolBar"), 60);
		if (Boolean.getBoolean(PROPERTY_PRINT_START_TIMES)) {
			System.err.println("preToolbarInit: " + (System.currentTimeMillis()-start));
		}

	}


	/**
	 * Called whenever a property changes for a component we are registered
	 * as listening to.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {

		String propertyName = e.getPropertyName();

		// If the file's path is changing (must be caused by the file being saved(?))...
		if (propertyName.equals(RTextEditorPane.FULL_PATH_PROPERTY)) {
			setTitle((String)e.getNewValue());
		}

		// If the file's modification status is changing...
		else if (propertyName.equals(RTextEditorPane.DIRTY_PROPERTY)) {
			String oldTitle = getTitle();
			boolean newValue = (Boolean)e.getNewValue();
			if (!newValue) {
				setTitle(oldTitle.substring(0,oldTitle.length()-1));
			}
			else {
				setTitle(oldTitle + '*');
			}
		}
		
		else if (propertyName.equals("RenderEngine")) {
			renderEngineChanged((String)e.getNewValue());
		}

	}


	void registerChildWindowListeners(Window w) {

		if (!windowListenersInited) {
			windowListenersInited = true;
			if (TranslucencyUtil.get().isTranslucencySupported(false)) {
				searchWindowOpacityListener = new ChildWindowListener(this);
				searchWindowOpacityListener.setTranslucencyRule(
												searchWindowOpacityRule);
			}
		}

		if (searchWindowOpacityListener!=null) {
			w.addWindowFocusListener(searchWindowOpacityListener);
			w.addComponentListener(searchWindowOpacityListener);
		}

	}


	// TODO
	void removeDockableWindow(DockableWindow wind) {
		((DockableWindowPanel)mainContentPanel).removeDockableWindow(wind);
	}


	/**
	 * Makes all actions use default accelerators.
	 */
	public void restoreDefaultAccelerators() {
		for (Action a : getActions()) {
			if (a instanceof StandardAction) {
				((StandardAction)a).restoreDefaultAccelerator();
			}
		}
		menuItemAcceleratorWorkaround();
	}


	/**
	 * Saves the uesr's preferences.
	 */
	public void savePreferences() {

		// Save preferences for RText itself.
		new RTextPrefs().populate(this).save();
		saveActionShortcuts(getShortcutsFile());

		// Save preferences for any plugins.
		Plugin[] plugins = getPlugins();
		int count = plugins.length;
		for (int i=0; i<count; i++) {
			plugins[i].savePreferences();
		}

		// Save the file chooser's properties, if it has been instantiated.
		if (chooser!=null)
			chooser.savePreferences();

	}


	/**
	 * Changes the style of icons used by <code>rtext</code>.<p>
	 *
	 * This method fires a property change of type
	 * <code>ICON_STYLE_PROPERTY</code>.
	 *
	 * @param name The name of the icon group to use.  If this name is not
	 *        recognized, a default icon set will be used.
	 */
	public void setIconGroupByName(String name) {

		IconGroup newGroup = iconGroupMap.get(name);
		if (newGroup==null)
			newGroup = iconGroupMap.get(
							IconGroupLoader.DEFAULT_ICON_GROUP_NAME);
		if (newGroup==null)
			throw new InternalError("No icon groups!");
		if (iconGroup!=null && iconGroup.equals(newGroup))
			return;

		Dimension size = getSize();
		IconGroup old = iconGroup;
		iconGroup = newGroup;

		Icon icon = iconGroup.getIcon("open");
		getAction(OPEN_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("save");
		getAction(SAVE_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("saveall");
		getAction(SAVE_ALL_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("openinnewwindow");
		getAction(OPEN_NEWWIN_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("saveas");
		getAction(SAVE_AS_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("options");
		getAction(OPTIONS_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("help");
		getAction(HELP_ACTION_KEY).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("about");
		getAction(ABOUT_ACTION_KEY).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("close");
		getAction(CLOSE_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("find");
		getAction(FIND_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("findnext");
		getAction(FIND_NEXT_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("replace");
		getAction(REPLACE_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("replacenext");
		getAction(REPLACE_NEXT_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("print");
		getAction(PRINT_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("printpreview");
		getAction(PRINT_PREVIEW_ACTION).putValue(Action.SMALL_ICON, icon);
		icon = iconGroup.getIcon("closeall");
		getAction(CLOSE_ALL_ACTION).putValue(Action.SMALL_ICON, icon);

		// Change all RTextAreas' open documents' icon sets.
		RTextEditorPane.setIconGroup(iconGroup);


		// The toolbar uses the large versions of the icons, if available.
		// FIXME:  Make this toggle-able.
		ToolBar toolBar = (ToolBar)getToolBar();
		if (toolBar!=null)
			toolBar.checkForLargeIcons();

		// Do this because the toolbar has changed it's size.
		if (isDisplayable()) {
			pack();
			setSize(size);
		}

		// Make the help dialog use appropriate "back" and "forward" icons.
		if (helpDialog!=null) {
			helpDialog.setBackButtonIcon(iconGroup.getIcon("back"));
			helpDialog.setForwardButtonIcon(iconGroup.getIcon("forward"));
		}

		firePropertyChange(ICON_STYLE_PROPERTY, old, iconGroup);

	}


	/**
	 * Sets the main view style.  This method fires a property change of type
	 * {@link #MAIN_VIEW_STYLE_PROPERTY}.
	 *
	 * @param viewStyle One of {@link #TABBED_VIEW}, {@link #SPLIT_PANE_VIEW}
	 *        or {@link #MDI_VIEW}.  If this value is invalid, nothing happens.
	 * @see #getMainViewStyle()
	 */
	public void setMainViewStyle(int viewStyle) {

		if ((viewStyle==TABBED_VIEW || viewStyle==SPLIT_PANE_VIEW ||
				viewStyle==MDI_VIEW) && viewStyle!=mainViewStyle) {

			int oldMainViewStyle = mainViewStyle;
			mainViewStyle = viewStyle;
			AbstractMainView fromView = mainView;

			RTextPrefs prefs = new RTextPrefs().populate(this);

			// Create the new view.
			switch (viewStyle) {
				case TABBED_VIEW:
					mainView = new RTextTabbedPaneView(this, null, prefs);
					menuBar.setWindowMenuVisible(false);
					break;
				case SPLIT_PANE_VIEW:
					mainView = new RTextSplitPaneView(this, null, prefs);
					menuBar.setWindowMenuVisible(false);
					break;
				case MDI_VIEW:
					mainView = new RTextMDIView(this, null, prefs);
					menuBar.setWindowMenuVisible(true);
					break;
			}

			// Update property change listeners.
			PropertyChangeListener[] propertyChangeListeners =
								fromView.getPropertyChangeListeners();
			for (PropertyChangeListener listener : propertyChangeListeners) {
				fromView.removePropertyChangeListener(listener);
				mainView.addPropertyChangeListener(listener);
			}

			// Keep find/replace dialogs working, if they've been created.
			// Make the new dialog listen to actions from the find/replace
			// dialogs.
			// NOTE:  The find and replace dialogs will be moved to mainView
			// in the copyData method below.
			mainView.getSearchManager().changeSearchListener(fromView);

			// Make mainView have all the properties of the old panel.
			mainView.copyData(fromView);

			// If we have switched to a tabbed view, artificially
			// fire stateChanged if the last document is selected,
			// because it isn't fired naturally if this is so.
			if ((mainView instanceof RTextTabbedPaneView) &&
				mainView.getSelectedIndex()==mainView.getNumDocuments()-1)
				((RTextTabbedPaneView)mainView).stateChanged(new ChangeEvent(mainView));


			// Physically replace the old main view with the new one.
			// NOTE: We need to remember previous size and restore it
			// because center collapses if changed to MDI otherwise.
			Dimension size = getSize();
			csp.remove(fromView);
			csp.add(mainView);
			fromView.dispose();
			//contentPane.add(mainView, BorderLayout.CENTER);
			pack();
			setSize(size);

			// For some reason we have to reselect the currently-selected
			// window to have it actually active in an MDI view.
			if (mainView instanceof RTextMDIView)
				mainView.setSelectedIndex(mainView.getSelectedIndex());


			firePropertyChange(MAIN_VIEW_STYLE_PROPERTY, oldMainViewStyle,
												mainViewStyle);

		} // End of if ((viewStyle==TABBED_VIEW || ...

	}


	/**
	 * This method changes both the active file name in the title bar, and the
	 * status message in the status bar.
	 *
	 * @param fileFullPath Full path to the text file currently being edited
	 *        (to be displayed in the window's title bar).  If
	 *        <code>null</code>, the currently displayed message is not
	 *        changed.
	 * @param statusMessage The message to be displayed in the status bar.
	 *        If <code>null</code>, the status bar message is not changed.
	 */
	public void setMessages(String fileFullPath, String statusMessage) {
		if (fileFullPath != null)
			setTitle(fileFullPath);
		StatusBar statusBar = (StatusBar)getStatusBar();
		if (statusBar!=null && statusMessage != null)
			statusBar.setStatusMessage(statusMessage);
	}


	/**
	 * Enables or disables the row/column indicator in the status bar.
	 *
	 * @param isVisible Whether or not the row/column indicator should be
	 *        visible.
	 */
	public void setRowColumnIndicatorVisible(boolean isVisible) {
		((StatusBar)getStatusBar()).setRowColumnIndicatorVisible(isVisible);
	}


	/**
	 * Sets whether the hostname should be shown in the title of the main
	 * RText window.
	 *
	 * @param show Whether the hostname should be shown.
	 * @see #getShowHostName()
	 */
	public void setShowHostName(boolean show) {
		if (this.showHostName!=show) {
			this.showHostName = show;
			setTitle(getTitle()); // Cause title to refresh.
		}
	}


	/**
	 * Sets whether the read-only indicator in the status bar is enabled.
	 *
	 * @param enabled Whether or not the read-only indicator is enabled.
	 */
	public void setStatusBarReadOnlyIndicatorEnabled(boolean enabled) {
		((StatusBar)getStatusBar()).setReadOnlyIndicatorEnabled(enabled);
	}


	/**
	 * Sets the syntax highlighting color scheme being used.
	 *
	 * @param colorScheme The new color scheme to use.  If
	 *        <code>null</code>, nothing changes.
	 * @see #getSyntaxScheme()
	 */
	public void setSyntaxScheme(SyntaxScheme colorScheme) {
		if (colorScheme!=null && !colorScheme.equals(this.colorScheme)) {
			// Make a deep copy for our copy.  We must be careful to do this
			// and pass our newly-created deep copy to mainView so that we
			// do not end up with the same copy passed to us (which could be
			// in the process of being edited in an options dialog).
			this.colorScheme = (SyntaxScheme)colorScheme.clone();
			if (mainView!=null)
				mainView.setSyntaxScheme(this.colorScheme);
		}
	}


	/**
	 * Changes whether or not tabs should be emulated with spaces
	 * (i.e., soft tabs).
	 * This simply calls <code>mainView.setTabsEmulated</code>.
	 *
	 * @param areEmulated Whether or not tabs should be emulated with spaces.
	 */
	public void setTabsEmulated(boolean areEmulated) {
		mainView.setTabsEmulated(areEmulated);
	}


	/**
	 * Sets the tab size to be used on all documents.
	 *
	 * @param newSize The tab size to use.
	 * @see #getTabSize()
	 */
	private void setTabSize(int newSize) {
		mainView.setTabSize(newSize);
	}


	/**
	 * Sets the title of the application window.  This title is prefixed
	 * with the application name.
	 *
	 * @param title The new title.
	 * @see #getTitle()
	 */
	@Override
	public void setTitle(String title) {
		if (getShowHostName()) {
			title = "rtext (" + getHostName() + ") - " + title;
		}
		else {
			title = "rtext - " + title;
		}
		super.setTitle(title);
	}


	/**
	 * Sets the opacity with which to render unfocused child windows, if this
	 * option is enabled.
	 *
	 * @param opacity The opacity.  This should be between <code>0</code> and
	 *        <code>1</code>.
	 * @see #getSearchWindowOpacity()
	 * @see #setSearchWindowOpacityRule(int)
	 */
	public void setSearchWindowOpacity(float opacity) {
		searchWindowOpacity = Math.max(0, Math.min(opacity, 1));
		if (windowListenersInited && isSearchWindowOpacityEnabled()) {
			searchWindowOpacityListener.refreshTranslucencies();
		}
	}


	/**
	 * Toggles whether search window opacity is enabled.
	 *
	 * @param enabled Whether search window opacity should be enabled.
	 * @see #isSearchWindowOpacityEnabled()
	 */
	public void setSearchWindowOpacityEnabled(boolean enabled) {
		if (enabled!=searchWindowOpacityEnabled) {
			searchWindowOpacityEnabled = enabled;
			// Toggled either on or off
			// Must check searchWindowOpacityListener since in pre 6u10,
			// we'll be inited, but listener isn't created.
			if (windowListenersInited &&
					searchWindowOpacityListener!=null) {
				searchWindowOpacityListener.refreshTranslucencies();
			}
		}
	}


	/**
	 * Toggles whether certain child windows should be made translucent.
	 *
	 * @param rule The new opacity rule.
	 * @see #getSearchWindowOpacityRule()
	 * @see #setSearchWindowOpacity(float)
	 */
	public void setSearchWindowOpacityRule(int rule) {
		if (rule!=searchWindowOpacityRule) {
			searchWindowOpacityRule = rule;
			if (windowListenersInited) {
				searchWindowOpacityListener.setTranslucencyRule(rule);
			}
		}
	}


	/**
	 * Toggles whether the spelling error window is visible.
	 *
	 * @param visible Whether the spelling error window is visible.
	 * @see #isSpellingWindowVisible()
	 */
	public void setSpellingWindowVisible(boolean visible) {
		if (visible) {
			if (spellingWindow==null) {
				spellingWindow = new SpellingErrorWindow(this);
				DockableWindowPanel dwp = (DockableWindowPanel)mainContentPanel;
				dwp.addDockableWindow(spellingWindow);
			}
			else {
				spellingWindow.setActive(true);
			}
		}
		else {
			if (spellingWindow!=null) {
				spellingWindow.setActive(false);
			}
		}
	}


	/**
	 * Sets the "working directory;" that is, the directory in which
	 * new, empty files are placed.
	 *
	 * @param directory The new working directory.  If this directory does
	 *        not exist, the Java property "user.dir" is used.
	 * @see #getWorkingDirectory
	 */
	public void setWorkingDirectory(String directory) {
		File test = new File(directory);
		if (test.isDirectory())
			workingDirectory = directory;
		else
			workingDirectory = System.getProperty("user.dir");
	}

	public void renderEngineChanged(String engine) {
		// Set projectUpdated to true when changing renderer to ensure new renderer gets the project zip
		setProjectUpdated(true);
		
		for(RenderEngineListener l : renderEngineListeners.getListeners(RenderEngineListener.class)) {
			l.renderEngineChanged(engine);
		}
	}
	
    public void resetView() {
        for(ViewListener l : viewListeners.getListeners(ViewListener.class)) {
            l.resetView();
        }
    }

	public void libPathChanged(String libsSt) {
		if (libsSt == null || libsSt.length() == 0) return;

		String[] libs = libsSt.split(";");
		libDirs.clear();

		for(String lib: libs) {
			libDirs.add(lib);
		}

		// A little messy but needed for loadUrl inside scripts to work
		ShapeJSGlobal.configureLibDirs(libDirs);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateLookAndFeel(LookAndFeel lnf) {

		super.updateLookAndFeel(lnf);

		try {

			Dimension size = this.getSize();

			// Update all components in this frame.
			SwingUtilities.updateComponentTreeUI(this);
			this.pack();
			this.setSize(size);

			// So mainView knows to update it's popup menus, etc.
			mainView.updateLookAndFeel();

			// Update any dialogs.
			if (optionsDialog != null) {
				SwingUtilities.updateComponentTreeUI(optionsDialog);
				optionsDialog.pack();
			}
			if (helpDialog != null) {
				SwingUtilities.updateComponentTreeUI(helpDialog);
				helpDialog.pack();
			}

			if (chooser!=null) {
				SwingUtilities.updateComponentTreeUI(chooser);
				chooser.updateUI(); // So the popup menu gets updated.
	 		}
			if (rfc!=null) {
				SwingUtilities.updateComponentTreeUI(rfc);
				rfc.updateUI(); // Not JDialog API; specific to this class
			}

		} catch (Exception f) {
			displayException(f);
		}

	}


	/**
	 * 1.5.2004/pwy: The following two functions are called from the
	 * OSXAdapter and provide the hooks for the functions from the standard
	 * Apple application menu.  The "about()" OSX hook is in
	 * AbstractGUIApplication.
	 */
	@Override
	public void preferences() {
		getAction(OPTIONS_ACTION).actionPerformed(new ActionEvent(this,0,"unused"));
	}

	@Override
	public void openFile(final String filename) {
		//gets called when we receive an open event from the finder on OS X
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// null encoding means check for Unicode before using
				// system default encoding.
				mainView.openFile(filename, null, true);
			}
		});
	}

	/**
	 * Open a project for editing
	 * @param filename
	 */
	public void openProject(String filename) {
	    setBusy();
	    
		try {
	        for(ProjectListener l : projectListeners.getListeners(ProjectListener.class)) {
	            l.reset();
	        }
	        
			if (currProj != null) {
				// TODO: Need to save current or ask?
				mainView.closeAllDocuments();
			}

            ArrayList<String> libs = new ArrayList<>();
            libs.add(FilenameUtils.getFullPathNoEndSeparator(filename));
            List<String> pdirs = getLibDirs();
            libs.addAll(pdirs);

			currProj = Project.load(filename,libs);

			lastOpenProjectFile = filename;
			
			// Indicates project zip will have to be included in the request
			projectUpdated = true;

			List<ProjectItem> scripts = currProj.getScripts();
			for(ProjectItem pi : scripts) {
				String fullPath = pi.getPath();
				mainView.openFile(fullPath,RTextFileChooser.getDefaultEncoding());
			}

			java.util.List<VariantItem> variants = currProj.getVariants();

			if (variants.size() > 0) {
				// Store variants in an easily accessible map
				int count = 0;
				for (VariantItem vi : variants) {
					Variant v = new Variant();
					if (count == 0) {
						try {
							v.readDesign(libs, vi.getPath(), false);

						} catch (IOException | NotCachedException ioe) {
							ioe.printStackTrace();
							reportVariantError(v, ioe);
						} catch (Throwable rt) {
							reportVariantError(v, rt);
						}
					}
					v.setVariantParams(vi.getParams());
					projVariants.put(FilenameUtils.getBaseName(vi.getPath()), vi);

					if (count == 0) {
						currVariant = v;
					}
					count++;
				}
			}

			for(ProjectListener l : projectListeners.getListeners(ProjectListener.class)) {
				l.projectChanged(currProj);
			}

		} catch(IOException ioe) {
			ioe.printStackTrace();
		} finally {
		    setIdle();
		}

	}

	/**
	 * Attempts to export the current project.  The user will be prompted
	 * for a new file name to save with.
	 *
	 * @return <code>true</code> if the save is successful, <code>false</code>
	 *         if the user cancels the save operation or an IO error occurs.
	 *
	public synchronized boolean exportProjectAs() {
		
		// TODO: Export all files in scripts/, varaiants/, resources/ dir?
		//       Or parse the manifest files for scripts, variantes, and resources?
		
		String name = currProj.getName();
		String projDirPath = currProj.getParentDir();
		File projDir = new File(projDirPath);
		String exportPath = projDir.getParent() + File.separator + name + ".zip";

		// Ensures text area gets focus after save for saves that don't bring
		// up an extra window (Save As, etc.).  Without this, the text area
		// would lose focus.
//		currentTextArea.requestFocusInWindow();

		// Get the new filename they'd like to use.
		RTextFileChooser chooser = getFileChooser();
		chooser.setMultiSelectionEnabled(false);	// Disable multiple file selection.
		File initialSelection = new File(exportPath);
		chooser.setSelectedFile(initialSelection);
		chooser.setOpenedFiles(getOpenFiles());
		// Set encoding to what it was read-in or last saved as.
//		chooser.setEncoding(currentTextArea.getEncoding());

		int returnVal = chooser.showSaveDialog(this);

		// If they entered a new filename and clicked "OK", save the flie!
		if(returnVal == RTextFileChooser.APPROVE_OPTION) {

			File chosenFile = chooser.getSelectedFile();
			String chosenFileName = chosenFile.getName();
			String chosenFilePath = chosenFile.getAbsolutePath();
			String encoding = chooser.getEncoding();

			// If the current file filter has an obvious extension
			// associated with it, use it if the specified filename has
			// no extension.  Get the extension from the filter by
			// checking whether the filter is of the form
			// "Foobar Files (*.foo)", and it if is, use the ".foo"
			// extension.
			String extension = chooser.getFileFilter().getDescription();
			int leftParen = extension.indexOf("(*");
			if (leftParen>-1) {
				int start = leftParen + 2; // Skip "(*".
				int end = extension.indexOf(')', start);
				int comma = extension.indexOf(',', start);
				if (comma>-1 && comma<end)
					end = comma;
				if (end>start+1) { // Ensure a ')' or ',' was found.
					extension = extension.substring(start, end);
					// If the file name they entered has no extension,
					// add this extension to it.
					if (chosenFileName.indexOf('.')==-1) {
						chosenFileName = chosenFileName + extension;
						chosenFilePath = chosenFilePath + extension;
						chosenFile = new File(chosenFilePath);
					}
				}
			}

			// If the file already exists, prompt them to see whether
			// or not they want to overwrite it.
			if (chosenFile.exists()) {
				String temp = this.getString("FileAlreadyExists",
										chosenFile.getName());
				if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(
						this, temp, this.getString("ConfDialogTitle"),
						JOptionPane.YES_NO_OPTION)) {
					return false;
				}
			}

			// If necessary, change the current file's encoding.
			String oldEncoding = currentTextArea.getEncoding();
			if (encoding!=null && !encoding.equals(oldEncoding))
				currentTextArea.setEncoding(encoding);

			// Try to save the file with a new name.
			return saveCurrentFileAs(FileLocation.create(chosenFilePath));

		} // End of if(returnVal == RTextFileChooser.APPROVE_OPTION).

		// If they cancel the save...
		return false;

	}
*/
	/**
	 * Close the current project.
	 */
	public void closeProject() {

	}

	/**
	 * Add a resource to this project
	 * @param res
	 */
	public void addResource(ProjectItem res) {
		// Update the project
		// Update the manifest file
		// Open the file in mainView if script or variant
		
		// TODO: Better way to get type of resource
		if (res.getPath().endsWith(Project.EXT_SCRIPT)) {
			currProj.addScript(res);
		} else if (res.getPath().endsWith(Project.EXT_VARIANT)) {
			VariantItem vi = (VariantItem) res;
			currProj.addVariant((VariantItem) vi);
			
			// Make a variant from the variant item, add it to the projVariants
			Variant v = new Variant();

            ArrayList<String> libs = new ArrayList<>();
            libs.add(currProj.getParentDir());
            List<String> pdirs = getLibDirs();
            libs.addAll(pdirs);

			try {
				v.readDesign(libs, vi.getPath(), false);

			} catch(IOException | NotCachedException ioe) {
				ioe.printStackTrace();
				reportVariantError(v, ioe);
			} catch (Exception e) {
				e.printStackTrace();
				reportVariantError(v, e);
			} catch (Throwable rt) {
				reportVariantError(v, rt);
			} finally {
				v.setVariantParams(vi.getParams());
				currVariant = v;
				projVariants.put(FilenameUtils.getBaseName(vi.getPath()), vi);
			}
		}
		
		// Update listeners
        for(ProjectListener l : projectListeners.getListeners(ProjectListener.class)) {
            l.resourceAdded(res);
        }
        
        // Save the current project. Adds new resource to the manifest
        // TODO: What happens if saving current project fails?
        try {
        	currProj.save(currProj.getParentDir());
        } catch (IOException ioe) {
        	ioe.printStackTrace();
        }
        
        projectUpdated = true;
        
        // Open the file in editor
        String fullPath = res.getPath();
        mainView.openFile(fullPath,RTextFileChooser.getDefaultEncoding());
        
        // Switch to the new variant as the current variant
        // TODO: Switch to new variant without having to run again?
        //run();
	}

	/**
	 * Delete a resource from this project.
	 * @param res
	 */
	public void deleteResource(ProjectItem res) {
        File rf = new File(res.getPath());
        if (!rf.delete()) {
            printf("Cannot delete item: %s\n", rf.getAbsolutePath());
            warningReport("Cannot delete item: " + rf.getAbsolutePath(), null);
            return;
        }

        currProj.remove(res);

		// Update listeners
		for(ProjectListener l : projectListeners.getListeners(ProjectListener.class)) {
			l.resourceRemoved(res);
		}

        // Save the current project. Adds new resource to the manifest
        // TODO: What happens if saving current project fails?
        try {
        	currProj.save(currProj.getParentDir());
        } catch (IOException ioe) {
        	ioe.printStackTrace();
        }
        
        projectUpdated = true;
	}

	/**
	 * Notification that a resource has been updated.  This can be from internal saving or disk based
	 * @param res
	 */
	public void updateResource(ProjectItem res) {
		if (!res.getPath().endsWith(Project.EXT_VARIANT)) {
			return;
		}

		printf("Updating resource: %s\n",res);
		VariantItem vi = (VariantItem) res;
		String resName = FilenameUtils.getBaseName(vi.getPath());
        ArrayList<String> libs = new ArrayList<>();
        libs.add(currProj.getParentDir());
        List<String> pdirs = getLibDirs();
        libs.addAll(pdirs);

		for (Map.Entry<String, VariantItem> entry : projVariants.entrySet()) {
			String name = entry.getKey();
			
			if (name.equals(resName)) {
				Variant v = new Variant();

				try {
					v.readDesign(libs, vi.getPath(), false);

				} catch(IOException | NotCachedException ioe) {
					ioe.printStackTrace();
					reportVariantError(v, ioe);
				} catch (Exception e) {
					e.printStackTrace();
					reportVariantError(v, e);
				} catch (Throwable rt) {
					reportVariantError(v, rt);
				} finally {
					v.setVariantParams(vi.getParams());
					projVariants.put(FilenameUtils.getBaseName(vi.getPath()), vi);
				}
			}

		}

		// TODO: Not sure if we should have a concept of currentVariant
/*
		try {
			currVariant = new Variant();
			currVariant.readDesign(currProj.getParentDir(),currProj.getParentDir() + File.separator + vi.getPath());
			currVariant.setVariantParams(vi.getParams());
		} catch(IOException | NotCachedException ioe) {
			ioe.printStackTrace();
		} catch (Throwable rt) {
			String[] errors = currVariant.getErrorMessages();
			//printf("*** Error parsing variant: %s\n", rt.getMessage());
			for (int i=0; i<errors.length; i++) {
				printf("    %s\n", errors[i]);
			}
		}
*/
		projectUpdated = true;
		
		// Update listeners
		for(ProjectListener l : projectListeners.getListeners(ProjectListener.class)) {
			l.resourceUpdated(res);
		}
	}

	/**
	 * Runs the project.  Saves all modified resources and then runs the project
//	 * @param variant The variant to run or null to run the main script
	 */
	public void run() {
		// Save all opened files
		mainView.saveAllFiles();

		// Set to busy mode
        setBusy();
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    for(ProjectListener l : projectListeners.getListeners(ProjectListener.class)) {
                        l.reset();
                    }
                    
                    ToolBar tb = (ToolBar) getToolBar();
                    DefaultComboBoxModel vmodel = tb.getVariantModel();
                    String vname = (String) vmodel.getSelectedItem();

                    // Reread everything from disk merge with current param values

                    // Variant param values change as user interacts with the UI. When running a variant from the 
                    // drop down, we want run with the original variant values

                    VariantItem vi = projVariants.get(vname);
                    Map<String, Object> vparams = vi.getParams();
                    Variant v = new Variant();

                    ArrayList<String> libs = new ArrayList<>();
                    libs.add(currProj.getParentDir());
                    List<String> pdirs = getLibDirs();
                    libs.addAll(pdirs);

                    boolean success = false;

                    try {
                        // TODO: Can't really assume these are in the variants directory
                        // Selecting a variant and running it has to load the original param values, so load them again
                        v.readDesign(libs,currProj.getParentDir() + File.separator + "variants" + File.separator + vname + Project.EXT_VARIANT,false);
                        success = true;

                    } catch(IOException | NotCachedException ioe) {
                        ioe.printStackTrace();
                        reportVariantError(v, ioe);
                    } catch (Throwable rt) {
                        reportVariantError(v, rt);
                    } finally {
                        v.setVariantParams(vparams);
                    }

                    if (success) {
                        if (currVariant == null) {
                            currVariant = v;
                        } else {
                            // Put currVariant param values into new variant only if they use the same script
                            if (currVariant.getScriptName().equals(v.getScriptName())) {
                                currVariant = mergeVariant(currVariant,v);
                            } else {
                                currVariant = v;
                            }
                        }

                        for(ProjectListener l : projectListeners.getListeners(ProjectListener.class)) {
                            l.setProjectUpdated(projectUpdated);
                            l.runVariant(currVariant);
                        }
                    }
                } finally {
                    setProjectUpdated(false);
                    
                    // Set back to idle mode after running variant
                    setIdle();
                }
            }
        });
	}

	public void openVariant(final String vpath) {
		setBusy();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					for (ProjectListener l : projectListeners.getListeners(ProjectListener.class)) {
						l.reset();
					}

					Variant v = new Variant();

					ArrayList<String> libs = new ArrayList<>();
					libs.add(currProj.getParentDir());
					List<String> pdirs = getLibDirs();
					libs.addAll(pdirs);

					boolean success = false;

					try {
						// TODO: Can't really assume these are in the variants directory
						// Selecting a variant and running it has to load the original param values, so load them again
						v.readDesign(libs, vpath, false);
						success = true;
					} catch (IOException | NotCachedException ioe) {
						ioe.printStackTrace();
						reportVariantError(v, ioe);
					} catch (Throwable rt) {
						reportVariantError(v, rt);
					}

					if (success) {
						if (currVariant == null) {
							currVariant = v;
						} else {
							// Put currVariant param values into new variant only if they use the same script
							if (currVariant.getScriptName().equals(v.getScriptName())) {
								currVariant = mergeVariant(currVariant, v);
							} else {
								currVariant = v;
							}
						}

						for (ProjectListener l : projectListeners.getListeners(ProjectListener.class)) {
							l.setProjectUpdated(projectUpdated);
							l.runVariant(currVariant);
						}
					}
				} finally {
					setIdle();
				}
			};
		});
	}

	/**
	 * Resets the current variant to the values in the original variant file.
	 * 
	 * @param vname The name of the variant to reset
	 */
	public void resetVariant(String vname) {
        for(ProjectListener l : projectListeners.getListeners(ProjectListener.class)) {
            l.reset();
        }
        
        VariantItem vi = projVariants.get(vname);
        
        if (vi == null) return;
        
        Map<String, Object> vparams = vi.getParams();
        Variant v = new Variant();
        boolean success = false;

        ArrayList<String> libs = new ArrayList<>();
        libs.add(currProj.getParentDir());
        List<String> pdirs = getLibDirs();
        libs.addAll(pdirs);

        try {
            // TODO: Can't really assume these are in the variants directory
        	// Selecting a variant and running it has to load the original param values, so load them again
            v.readDesign(libs,currProj.getParentDir() + File.separator + "variants" + File.separator + vname + Project.EXT_VARIANT,false);
            success = true;

        } catch(IOException | NotCachedException ioe) {
            ioe.printStackTrace();
            reportVariantError(v, ioe);
        } catch (Throwable rt) {
        	reportVariantError(v, rt);
        } finally {
        	v.setVariantParams(vparams);
        }

        if (success) {
        	currVariant = v;
        	
            for(ProjectListener l : projectListeners.getListeners(ProjectListener.class)) {
                l.runVariant(currVariant);
            }
        }
	}

    /**
     * Merge data values from an existing variant into a newly loaded variant.
     *
     * Removed items will have their value removed
     * Range changed items will have their range values clipped
     * Newly added params will have default values
     *
     * @param current
     * @param loaded
     * @return
     */
	private Variant mergeVariant(Variant current, Variant loaded) {
		// If a different variant has been selected, used the selected variant as is
		if ( !loaded.getFileName().equals(current.getFileName()) ) {
			return loaded;
		}
		
		// Merge the current param values into the loaded variant

        Map<String, Parameter> cparams = currVariant.getEvaluatedScript().getScriptParams();
        Map<String, Parameter> lparams = loaded.getEvaluatedScript().getScriptParams();

        for (Map.Entry<String, Parameter> entry : lparams.entrySet()) {
        	String name = entry.getKey();
        	Parameter lparam = entry.getValue();
        	Parameter cparam = cparams.get(name);

        	// Loaded variant has new parameter that is not in current variant, skip
        	if (cparam == null) continue;

        	// If the types are different, skip
        	if (cparam.getType() != lparam.getType()) continue;

        	// Replace the loaded variant's param value with the current variant's param value 
        	// TODO: Do not replace if values are the same?
        	
        	Object cval = cparam.getValue();
        	lparam.setValue(cval);
        }
        
        // Need to update to apply parameter changes to the variant's scene
        loaded.onScriptParamChanged(lparams);
        
        return loaded;
    }
	
	public void saveAllVariantModels() {
		// TODO: Each SaveModelListener chooses the directory to save, which is bad
		//       But if choose directory here, there is no access to the last save dir
		//       OK for now since only the Renderer plugin can save models
		
		for(SaveModelListener l : saveModelListeners.getListeners(SaveModelListener.class)) {
			try {
				l.saveAllVariantModels();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Export a project into a transmital zip
	 */
	public void exportProject() {
        // Choose directory for saving
        File targetDir = chooseDirectory(lastExportProjectDir);
        lastExportProjectDir = targetDir.getAbsolutePath();

		currProj.exportProject(targetDir);
	}
	
	public void setProjectUpdated(boolean updated) {
		projectUpdated = updated;
	}
	
	public boolean getProjectUpdated() {
		return projectUpdated;
	}

	/**
	 * Choose file for saving.
	 *
	 * @param defaultDir The initial file path
	 * @return
	 */
	public File chooseDirectory(String defaultDir) {
		// Problems selecting directories with RTextFileChooser, so use JFileChooser
		
        JFileChooser dialog = new JFileChooser(defaultDir);
        dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int ret = dialog.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File sf = dialog.getSelectedFile();
            return sf;
        }

	    return null;
	}
	
    public String getLastOpenProjectFile() {
        return lastOpenProjectFile;
    }

    public void setLastOpenProjectFile(String lastOpenProjectDir) {
        this.lastOpenProjectFile = lastOpenProjectDir;
    }
    
    /**
     * Change cursor of app and components to wait cursor.
     */
    public void setBusy() {
        busy = true;
        
        // Busy mode for each ProjectListener
        for(ProjectListener l : projectListeners.getListeners(ProjectListener.class)) {
            l.setBusyMode();
        }

        // Busy mode for RText
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Busy mode for the mainView and its text area
        mainView.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        mainView.getCurrentTextArea().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    
    /**
     * Set cursor of app and components back to original cursor
     */
    public void setIdle() {
        busy = false;
        
        // Idle mode for RText
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
        // Idle mode for mainView and its text area
        mainView.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        mainView.getCurrentTextArea().setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        
        // Idle mode for each ProjectListener
        for(ProjectListener l : projectListeners.getListeners(ProjectListener.class)) {
            l.setIdleMode();
        }
    }
    
    public boolean isBusy() {
        return busy;
    }

	/**
	 * Program entry point.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(final String[] args) {

		// 1.5.2004/pwy: Setting this property makes the menu appear on top
		// of the screen on Apple Mac OS X systems. It is ignored by all other
		// other Java implementations.
		System.setProperty("apple.laf.useScreenMenuBar","true");

		// Make Darcula and Metal not use bold fonts
		UIManager.put("swing.boldMetal", Boolean.FALSE);

		// Catch any uncaught Throwables on the EDT and log them.
		AWTExceptionHandler.register();

		// 1.5.2004/pwy: Setting this property defines the standard
		// Application menu name on Apple Mac OS X systems. It is ignored by
		// all other Java implementations.
		// NOTE: Although you can set the useScreenMenuBar property above at
		// runtime, it appears that for this one, you must set it before
		// (such as in your *.app definition).
		//System.setProperty("com.apple.mrj.application.apple.menu.about.name", "RText");

		// Swing stuff should always be done on the EDT...
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				String lafName = RTextPrefs.getLookAndFeelToLoad();

				// Allow Substance to paint window titles, etc.  We don't allow
				// Metal (for example) to do this, because setting these
				// properties to "true", then toggling to a LAF that doesn't
				// support this property, such as Windows, causes the
				// OS-supplied frame to not appear (as of 6u20).
				if (SubstanceUtil.isASubstanceLookAndFeel(lafName)) {
					JFrame.setDefaultLookAndFeelDecorated(true);
					JDialog.setDefaultLookAndFeelDecorated(true);
				}

				String rootDir = AbstractGUIApplication.getLocationOfJar();
				ThirdPartyLookAndFeelManager lafManager =
					new ThirdPartyLookAndFeelManager(rootDir);

				try {
					ClassLoader cl = lafManager.getLAFClassLoader();
					// Set these properties before instantiating WebLookAndFeel
					if (WebLookAndFeelUtils.isWebLookAndFeel(lafName)) {
						WebLookAndFeelUtils.installWebLookAndFeelProperties(cl);
					}
					else {
						ShadowPopupFactory.install();
					}
					// Must set UIManager's ClassLoader before instantiating
					// the LAF.  Substance is so high-maintenance!
					UIManager.getLookAndFeelDefaults().put("ClassLoader", cl);
					Class<?> clazz;
					try {
						clazz = cl.loadClass(lafName);
					} catch (UnsupportedClassVersionError ucve) {
						// A LookAndFeel requiring Java X or later, but we're
						// now restarting with a Java version earlier than X
						lafName = UIManager.getSystemLookAndFeelClassName();
						clazz = cl.loadClass(lafName);
					}
					LookAndFeel laf = (LookAndFeel)clazz.newInstance();
					UIManager.setLookAndFeel(laf);
					UIManager.getLookAndFeelDefaults().put("ClassLoader", cl);
					UIUtil.installOsSpecificLafTweaks();
				} catch (RuntimeException re) { // FindBugs
					throw re;
				} catch (Exception e) {
					e.printStackTrace();
				}

				// The default speed of Substance animations is too slow
				// (200ms), looks bad moving through JMenuItems quickly.
				if (SubstanceUtil.isSubstanceInstalled()) {
					try {
						SubstanceUtil.setAnimationSpeed(100);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (lafName.contains(".Darcula")) {
					UIManager.getLookAndFeelDefaults().put("Tree.rendererFillBackground", Boolean.FALSE);
				}
				else {
					UIManager.getLookAndFeelDefaults().put("Tree.rendererFillBackground", null);
				}

				RText rtext = new RText(args);
				rtext.setLookAndFeelManager(lafManager);

				// For some reason, when using MDI_VIEW, the first window
				// isn't selected (although it is activated)...
				// INVESTIGATE ME!!
				if (rtext.getMainViewStyle()==MDI_VIEW) {
					rtext.getMainView().setSelectedIndex(0);
				}

				// We currently have one RText instance running.
				StoreKeeper.addRTextInstance(rtext);

			}
		});

	}

	
	public void reportVariantError(Variant v, Throwable rt) {
		//printf("*** Error parsing variant: %s\n", rt.getMessage());
		String[] errors = v.getErrorMessages();
		String errorsMsgs = "";

		// Include the Throwable error message
		if (rt != null && rt.getMessage() != null) {
			errorsMsgs += rt.getMessage() + "\n";
		}

		for (int i = 0; i < errors.length; i++) {
			//printf("    %s\n", errors[i]);
			errorsMsgs += errors[i] + "\n";
		}

		if (errorsMsgs.length() > 0) {
			// Pass null instead of the Throwable so stack trace of variant errors are not displayed
			// TODO: Need to use a exclusion list here not just removing it
			errorReport(errorsMsgs, rt);
		} else {
			// Send along throwable for debugging if their is no message
			errorReport(errorsMsgs, rt);
		}
	}

	// ErrorReporter Interface

	@Override
	public void partialReport(String s) {
		getErrorReporter().partialReport(s);
	}

	@Override
	public void messageReport(String s) {
		getErrorReporter().messageReport(s);
	}

	@Override
	public void warningReport(String s, Exception e) {
		getErrorReporter().warningReport(s,e);

	}

	@Override
	public void warningReport(String s, Throwable throwable) {
		getErrorReporter().warningReport(s,throwable);
	}

	@Override
	public void errorReport(String s, Exception e) {
		getErrorReporter().errorReport(s,e);
	}

	@Override
	public void errorReport(String s, Throwable throwable) {
		getErrorReporter().errorReport(s,throwable);
	}

	@Override
	public void fatalErrorReport(String s, Exception e) {
		getErrorReporter().errorReport(s,e);
	}

	@Override
	public void fatalErrorReport(String s, Throwable throwable) {
		getErrorReporter().errorReport(s,throwable);
	}

	private ErrorReporter getErrorReporter() {
		if (errorReporter == null) {
			Plugin[] plugins = getPlugins(); // Guaranteed non-null.
			int count = plugins.length;


			for (int i=0; i<count; i++) {
				if (plugins[i].getPluginName().equals("Console")) {
					final String DOCKABLE_WINDOW_CONSOLE	= "consoleDockableWindow";

					if (plugins[i] instanceof GUIPlugin) {
						errorReporter = (ErrorReporter) (((GUIPlugin)(plugins[i])).getDockableWindow(DOCKABLE_WINDOW_CONSOLE));
						return errorReporter;
					}
				}
			}
			// See if the Console exists yet, otherwise we'll drop messages
			return new SysErrorReporter();
		} else {
			return errorReporter;
		}
	}
}
