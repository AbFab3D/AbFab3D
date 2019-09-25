/*
 * 12/14/2015
 *
 * Copyright (C) 2015 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.langsupport;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for TypeScript.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class TypeScriptOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 */
	public TypeScriptOptionsPanel() {
		super("Options.TypeScript.Name", "ts.png",
				SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT);
	}


}