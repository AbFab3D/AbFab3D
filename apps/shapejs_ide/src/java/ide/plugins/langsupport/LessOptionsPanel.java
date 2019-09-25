/*
 * 12/18/2011
 *
 * CSharpOptionsPanel.java - Options for C#.
 * Copyright (C) 2011 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.langsupport;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for CSS.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class LessOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 */
	public LessOptionsPanel() {
		super("Options.Less.Name", "less.png",
				SyntaxConstants.SYNTAX_STYLE_LESS);
	}


}