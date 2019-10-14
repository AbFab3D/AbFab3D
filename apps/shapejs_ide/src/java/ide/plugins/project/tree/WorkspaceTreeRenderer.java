/*
 * 08/28/2012
 *
 * WorkspaceTreeRenderer.java - Renderer for workspace tree nodes.
 * Copyright (C) 2012 Robert Futrell
 * http://fifesoft.com/rtext
 * Licensed under a modified BSD license.
 * See the included license file for details.
 */
package ide.plugins.project.tree;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.fife.util.SubstanceUtil;


/**
 * The renderer for workspace tree views.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class WorkspaceTreeRenderer extends DefaultTreeCellRenderer {


	/**
	 * Returns a tree cell renderer for workspace trees.  This may not be
	 * an instance of this class (or a subclass).  Some Look and Feels
	 * unfortunately require inheritance to work properly...
	 * 
	 * @return The renderer.
	 */
	public static TreeCellRenderer create() {
		if (SubstanceUtil.isSubstanceInstalled()) {
			return new SubstanceWorkspaceTreeRenderer();
		}
		return new WorkspaceTreeRenderer();
	}


	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean focused) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
				row, focused);
		// Not true the first time through!
		if (value instanceof AbstractWorkspaceTreeNode) {
			AbstractWorkspaceTreeNode node = (AbstractWorkspaceTreeNode)value;
			setIcon(node.getIcon());
		}
		return this;
	}


}