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
package ide;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;
import org.fife.ui.EscapableDialog;
import org.fife.ui.UIUtil;

import abfab3d.shapejs.Project;
import abfab3d.shapejs.ProjectItem;
import abfab3d.shapejs.VariantItem;


/**
 * A dialog allowing the user to rename variant file.
 *
 * @author Tony Wong
 */
public class RenameVariantDialog extends EscapableDialog {

	private RText rtext;
	private ProjectItem oldVariantItem;
	private Listener listener;

    private JPanel panel;
    private JTextField nameField;
    
    
	/**
	 * The maximum width of this dialog.
	 */
	private static final int MAX_WIDTH = 800;


	/**
	 * Constructor.
	 *
	 * @param parent The parent application.
	 */
	public RenameVariantDialog(RText parent, ProjectItem variantItem) {

		super(parent, parent.getString("Dialog.NewVariant.Title"), true);
		this.rtext = parent;
		this.oldVariantItem = variantItem;

		createUI();
		pack();
		setLocationRelativeTo(rtext);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

	}

	/**
	 * Creates the content of this dialog.
	 */
	private void createUI() {
		listener = new Listener();

        panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(panel);
        
        JPanel topPanel = new JPanel(new FlowLayout());
        JPanel spacingPanel1 = new JPanel();
        spacingPanel1.setBorder(new EmptyBorder(5, 5, 5, 5));
        topPanel.add(spacingPanel1);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(0, 0, 0, 10));  // Spacing on the right
        JPanel buttonsPanel = new JPanel(new GridLayout(0, 4));

        bottomPanel.add(spacingPanel1, BorderLayout.CENTER);
        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        JPanel spacingPanel2 = new JPanel();
        spacingPanel2.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(spacingPanel2, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
		JPanel springPanel = new JPanel(new SpringLayout());
		JPanel temp = new JPanel(new BorderLayout());
		
		// Variant name
		JLabel nameLabel = new JLabel("Variant Name:");
		nameField = new JTextField(40);
		nameLabel.setLabelFor(nameField);
		
		springPanel.add(nameLabel);
		springPanel.add(nameField);
		
		temp.add(springPanel, BorderLayout.LINE_START);
		
		// Arguments (panel, rows,cols, initial-x, initial-y, x-spacing, y-spacing)
		UIUtil.makeSpringCompactGrid(springPanel, 1,2, 0,0, 5,5);
		
		topPanel.add(temp);
		
		// Add buttons
        JLabel skipButton1 = new JLabel();
        JLabel skipButton2 = new JLabel();
        JButton finishButton = new JButton("Finish");
        finishButton.addActionListener(listener);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(listener);
        buttonsPanel.add(skipButton1);
        buttonsPanel.add(skipButton2);
        buttonsPanel.add(finishButton);
        buttonsPanel.add(cancelButton);

        getRootPane().setDefaultButton(finishButton);
	}

	/**
	 * Overridden to limit this dialog's width.
	 */
	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		if (size != null) {
			size.width = Math.min(MAX_WIDTH, size.width);
		}
		return size;
	}

	/**
	 * Toggles whether this dialog is visible.
	 *
	 * @param visible Whether this dialog should be visible.
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			super.setVisible(true);
		}
		else {
			super.setVisible(false);
		}
	}


	/**
	 * Listens for events in this dialog.
	 */
	private class Listener implements ActionListener {

	    @Override
	    public void actionPerformed(ActionEvent e) {
	        String command = e.getActionCommand();

	        switch(command) {
	            case "Cancel":
	            	escapePressed();
	                break;
	            case "Finish":
	                // Pop up dialog for new name
	            	// Make copy of variant file and addResource
	            	// Delete the old variant with deleteResource

	                Project proj = rtext.getCurrProject();

	                String spath = "variants" + File.separator + nameField.getText();
	                if (!spath.endsWith(Project.EXT_VARIANT)) {
	                	spath += Project.EXT_VARIANT;
	                }
	                
	                File f = new File(proj.getParentDir() + File.separator + spath);
	                
	                // If variant name exists, pop up a warning
	                if (f.exists()) {
	    				//String temp = rtext.getString("FileAlreadyExists", nameField.getText());
	    				String temp = "Variant with name \"" + nameField.getText() + "\" already exist!";
	    				JOptionPane.showMessageDialog(rtext, temp, "Rename Variant", JOptionPane.ERROR_MESSAGE);
	    				return;
	                }
	                
	                File srcFile = new File(oldVariantItem.getPath());
                    try {
                    	// Can't use FileUtils.moveFile because rtext.deleteResource does the file deletion
                    	FileUtils.copyFile(srcFile, f);
                    } catch(IOException ioe) {
                    	abfab3d.core.Output.printf("Failed to rename variant to: %s\n", nameField.getText());
                        ioe.printStackTrace();
                        return;
                    }
                    
                    if (f.exists()) {
                    	VariantItem newVariantItem = new VariantItem(proj.getParentDir(), spath,
                    			proj.getParentDir() + File.separator + spath, null);
                    	rtext.addResource(newVariantItem);
                    	rtext.deleteResource(oldVariantItem);
                    }

	                escapePressed();
	                break;
	            default:
	                abfab3d.core.Output.printf("Unhandled action: %s\n",command);
	        }
	    }
	}

}
