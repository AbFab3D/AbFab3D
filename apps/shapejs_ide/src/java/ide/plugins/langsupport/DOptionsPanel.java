/*
 * 11/28/2014
 *
 * DOptionsPanel.java - Options for D.
 * Copyright (C) 2014 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.langsupport;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;


/**
 * Options panel containing options for D.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class DOptionsPanel extends FoldingOnlyOptionsPanel {


	/**
	 * Constructor.
	 */
	public DOptionsPanel() {
		super("Options.D.Name", "/ide/graphics/file_icons/d.png",
				SyntaxConstants.SYNTAX_STYLE_D);
	}


}