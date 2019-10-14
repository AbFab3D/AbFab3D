/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package ide.plugins.projectnav;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import ide.AbstractMainView;
import ide.ProjectListener;
import ide.RText;
import ide.RenameVariantDialog;

import org.apache.commons.io.FilenameUtils;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowScrollPane;
import org.fife.ui.rtextfilechooser.RTextFileChooser;

import abfab3d.shapejs.Project;
import abfab3d.shapejs.ProjectItem;
import abfab3d.shapejs.Variant;
import abfab3d.shapejs.VariantItem;

import static abfab3d.core.Output.printf;


/**
 * A dockable window that acts as a project resource navigator.
 *
 * @author Tony Wong
 * @version 1.0
 */
class ProjectNavWindow extends DockableWindow implements ProjectListener, ActionListener,
        TreeSelectionListener, MouseListener {

	private RText app;
	private Project proj;
	private Plugin plugin;
	
    private JTree tree;
    
    private AbstractMainView mainView;
    private DefaultMutableTreeNode topTree;
    private DefaultMutableTreeNode scriptTree;
    private DefaultMutableTreeNode variantTree;
    private DefaultMutableTreeNode resourcesTree;

    private ProjectItemWrapper currentRightClick;

	public ProjectNavWindow(AbstractPluggableGUIApplication<?> app, Plugin plugin, ProjectNavPrefs prefs) {

		this.app = (RText) app;
		this.plugin = plugin;
		setDockableWindowName(plugin.getString("DockableWindow.Title"));
		setIcon(plugin.getPluginIcon());
		setPosition(DockableWindow.LEFT);
		setLayout(new BorderLayout());

        DefaultMutableTreeNode top =
                new DefaultMutableTreeNode("Project");

        tree = new JTree(top);
        DockableWindowScrollPane scroll = new DockableWindowScrollPane(tree);

        tree.addTreeSelectionListener(this);
        tree.addMouseListener(this);

        expandTree(tree);
        
        add(scroll);
        setPrimaryComponent(scroll);
		setPosition(prefs.windowPosition);
		setActive(prefs.windowVisible);
		
        mainView = this.app.getMainView();
        this.app.addProjectListener(this);
	}

    /**
     * Clears any text from all consoles.
     */
    public void clearRenderers() {
    }

    /**
     * Rebuild the tree as things have changed
     *
     * // TODO: Redo the tree logic to handle user made arbitrary directories
     */
    public void update() {
        topTree = new DefaultMutableTreeNode("Project");

        DefaultMutableTreeNode item = null;

        scriptTree = new DefaultMutableTreeNode("scripts");
        topTree.add(scriptTree);

        List<ProjectItem> scripts = proj.getScripts();

        String parent = proj.getParentDir() + File.separator + "scripts";

        for(ProjectItem pi: scripts) {
            item = new DefaultMutableTreeNode(pi.getPath());
            item.setUserObject(new ProjectItemWrapper(pi,parent));
            scriptTree.add(item);
        }

        variantTree = new DefaultMutableTreeNode("variants");
        topTree.add(variantTree);

        parent = proj.getParentDir() + File.separator + "variants";
        List<VariantItem> variants = proj.getVariants();

        for(VariantItem pi: variants) {
            item = new DefaultMutableTreeNode(pi.getPath());
            item.setUserObject(new ProjectItemWrapper(pi,parent));
            variantTree.add(item);
        }

        resourcesTree = new DefaultMutableTreeNode("resources");
        topTree.add(resourcesTree);

        parent = proj.getParentDir() + File.separator + "resources";
        List<ProjectItem> resources = proj.getResources();

        for(ProjectItem pi: resources) {
            item = new DefaultMutableTreeNode(pi.getPath());
            item.setUserObject(new ProjectItemWrapper(pi,parent));
            resourcesTree.add(item);
        }

        tree.setModel(new DefaultTreeModel(topTree,false));
        expandTree(tree);

/*
        // Open the first variants mainScript
        if (variants.size() > 0) {
            VariantItem vi = variants.get(0);
            ide.getCodeEditor().addTab(proj.getParentDir(),vi.getMainScript());
        }
*/
    }
    
    private void expandTree(JTree tree) {
        for(int i=0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }
    
    @Override
    public void projectChanged(Project proj) {
        this.proj = proj;
        update();
    }

    @Override
    public void resourceAdded(ProjectItem res) {
//    	update();
    	
    	// Add resource to current tree
    	if (res.getPath().endsWith(Project.EXT_SCRIPT)) {
    	    String parent = proj.getParentDir() + File.separator + "scripts";
    	    DefaultMutableTreeNode item = new DefaultMutableTreeNode(res.getPath());
            item.setUserObject(new ProjectItemWrapper(res, parent));
            scriptTree.add(item);
    	} else if (res.getPath().endsWith(Project.EXT_VARIANT)) {
    	    String parent = proj.getParentDir() + File.separator + "variants";
    	    DefaultMutableTreeNode item = new DefaultMutableTreeNode(res.getPath());
            item.setUserObject(new ProjectItemWrapper(res, parent));
            variantTree.add(item);
    	}
    	
    	DefaultTreeModel tm = (DefaultTreeModel) tree.getModel();
    	tm.reload();
    	expandTree(tree);
    }

    @Override
    public void resourceRemoved(ProjectItem res) {
        // TODO: We need to update tree
    	update();
    }

	@Override
	public void resourceUpdated(ProjectItem res) {
		update();
	}
	
    /**
     * This variant has been selected for running, do it
     * @param variant
     */
    public void runVariant(Variant variant) {
        // Nothing todo
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command =  e.getActionCommand();
        switch(command) {
            case "Delete":
                app.deleteResource(currentRightClick.getProjectItem());
                break;
            case "Rename":
                RenameVariantDialog dialog = new RenameVariantDialog(app, currentRightClick.getProjectItem());
                dialog.setVisible(true);
                break;
        }

    }
    
	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		// TODO Auto-generated method stub
		
		TreePath path = tree.getSelectionPath();
		
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();

        if (node == null)
            //Nothing is selected.
            return;

        Object udata = node.getUserObject();
        if (!(udata instanceof ProjectItemWrapper)) {
            // ignore category labels
            return;
        }
        ProjectItemWrapper wrapper = (ProjectItemWrapper) node.getUserObject();
        ProjectItem pi = wrapper.getProjectItem();
        
        if (node.isLeaf()) {
        	String fullPath = pi.getPath();
        	int idx = mainView.getFileIndex(fullPath);
        	
        	if (idx >= 0) {
        		mainView.setSelectedIndex(idx);
        	} else {
        		mainView.openFile(fullPath,RTextFileChooser.getDefaultEncoding());
        	}
        	
        	printf("TODO: Decide if we want this to change/run the selected variant\n");
/*
        	if (pi.getPath().endsWith(Project.EXT_VARIANT)) {

        		Map<String, Variant> variants = app.getProjVariants();
        		Variant v = variants.get(ilenameUtils.getBaseName(pi.getPath()));

        		// Update all variant listeners
        		EventListenerList listeners = app.getProjectListeners();
        		
    			for(ProjectListener l : listeners.getListeners(ProjectListener.class)) {
    				l.selectedVariantChanged(v);
    			}
        	}
*/
        }
	}

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        handleContextMenu(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        handleContextMenu(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void reset() {

    }

    @Override
    public void setProjectUpdated(boolean updated) {

    }

    private void handleContextMenu(MouseEvent mouseEvent)
    {
        if (mouseEvent.isPopupTrigger())
        {
            int x = mouseEvent.getX();
            int y = mouseEvent.getY();
            JTree tree = (JTree)mouseEvent.getSource();
            TreePath path = tree.getPathForLocation(x, y);
            if (path == null)
                return;

            tree.setSelectionPath(path);

            DefaultMutableTreeNode obj = (DefaultMutableTreeNode) path.getLastPathComponent();
            currentRightClick = (ProjectItemWrapper) obj.getUserObject();

            ContextMenu contextMenu = new ContextMenu(this);

            contextMenu.show(mouseEvent.getComponent(),
                    mouseEvent.getX(),
                    mouseEvent.getY());
        }
    }

	//============================================================
	
    static class ContextMenu extends JPopupMenu {
        JMenuItem delete;
        JMenuItem rename;

        public ContextMenu(ActionListener actor) {
            delete = new JMenuItem("Delete  ");
            delete.addActionListener(actor);
            delete.setActionCommand("Delete");
            add(delete);

            rename = new JMenuItem("Rename...  ");
            rename.addActionListener(actor);
            rename.setActionCommand("Rename");
            add(rename);

        }

    }
    
    static class ProjectItemWrapper {
        ProjectItem pi;
        String pdir;
        String label;

        public ProjectItemWrapper(ProjectItem pi,String pdir) {
            this.pi = pi;
            this.pdir = pdir;

            Path pbase = Paths.get(pdir);
            String path = pi.getPath();

            if (path == null) {
                printf("Null path on ProjectItem.  orig: %s\n",pi.getOrigPath());
            }
            if (path.endsWith("*")) path = path.substring(0,path.length()-1);
            Path pabs = Paths.get(path);
            try {
                Path prel = pbase.relativize(pabs);
                label = prel.toString();
            } catch(IllegalArgumentException iae) {
                printf("Cannot relativize path.  base: %s  abs: %s  err: %s\n",pbase,path,iae.toString());
                label = path;
            }
        }

        public String toString() {
            return label;
        }

        public ProjectItem getProjectItem() {
            return pi;
        }

    }

    @Override
    public void setBusyMode() {

    }

    @Override
    public void setIdleMode() {

    }
}
