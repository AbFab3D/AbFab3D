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
 * Options panel containing options for C#.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class CSharpOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 */
	public CSharpOptionsPanel() {
		super("Options.CSharp.Name", "page_white_csharp.png",
				SyntaxConstants.SYNTAX_STYLE_CSHARP);
	}


}