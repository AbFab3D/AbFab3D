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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;
import org.fife.ui.EscapableDialog;

import abfab3d.shapejs.Project;
import abfab3d.shapejs.ProjectItem;


/**
 * A dialog allowing the user to create a new script file.
 *
 * @author Tony Wong
 */
public class NewScriptDialog extends EscapableDialog {

	private RText rtext;
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
	public NewScriptDialog(RText parent) {

		super(parent, parent.getString("Dialog.NewScript.Title"), true);
		this.rtext = parent;

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
        
        JPanel formPanel = new JPanel(new GridLayout(1,1));

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(formPanel);
        JPanel spacingPanel1 = new JPanel();
        spacingPanel1.setBorder(new EmptyBorder(5, 5, 5, 5));
        topPanel.add(spacingPanel1);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(0, 4));

        bottomPanel.add(spacingPanel1, BorderLayout.CENTER);
        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);


        panel.add(topPanel, BorderLayout.NORTH);
        JPanel spacingPanel2 = new JPanel();
        spacingPanel2.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(spacingPanel2, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        JLabel nameLabel = new JLabel("Script Name:");
        nameField = new JTextField(40);
        replaceBorder(nameField);

        JPanel namePanel = new JPanel(new BorderLayout());
        JPanel nameSpacingPanel = new JPanel();
        nameSpacingPanel.setBorder(new EmptyBorder(5,5,5,5));
        namePanel.add(nameLabel,BorderLayout.WEST);
        namePanel.add(nameSpacingPanel,BorderLayout.CENTER);
        namePanel.add(nameField,BorderLayout.EAST);
        formPanel.add(namePanel);

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
     * Replace bordering with some south spacing
     * @param tf
     */
    private void replaceBorder(JTextField tf) {
        Border origBorder = tf.getBorder();
        if (origBorder instanceof CompoundBorder) {
            CompoundBorder cb = (CompoundBorder) origBorder;
            Border ib = cb.getInsideBorder();
            Border ob = cb.getOutsideBorder();

            Color bg = panel.getBackground();
            tf.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,5,0,bg),ob));
        }
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
	 * Opens the file selected in the file list.
	 *
	private void openSelectedFile() {
		FileLocation loc = (FileLocation)list.getSelectedValue();
		if (loc != null) {
			if (loc.isLocalAndExists()) {
				rtext.openFile(loc.getFileFullPath());
			}
			else {
				// TODO: Support opening remote FileLocations once
				// RSyntaxTextArea bug #94 is fixed.  Probably have to modify
				// RemoteFileChooser to be pre-filled in but prompt just for
				// password.
				UIManager.getLookAndFeel().provideErrorFeedback(list);
			}
			escapePressed();
		}
		else {
			UIManager.getLookAndFeel().provideErrorFeedback(list);
		}
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
	                // Create new script, add to project

	                Project proj = rtext.getCurrProject();
	                String spath = "scripts" + File.separator + nameField.getText();
	                if (!spath.endsWith(Project.EXT_SCRIPT)) {
	                	spath += Project.EXT_SCRIPT;
	                }
	                File f = new File(proj.getParentDir() + File.separator + spath);

	                if (!f.exists()) {
	                    try {
	                        FileUtils.writeStringToFile(f, "");
	                    } catch(IOException ioe) {
	                    	abfab3d.core.Output.printf("Failed to create new script: %s\n", f.getAbsoluteFile());
	                        ioe.printStackTrace();
	                        return;
	                    }
	                }
	                ProjectItem script = new ProjectItem(spath,proj.getParentDir() + File.separator + spath,null);
	                rtext.addResource(script);
	                escapePressed();
	                break;
	            default:
	            	 abfab3d.core.Output.printf("Unhandled action: %s\n",command);
	        }
	    }
/*
		@Override
		public void actionPerformed(ActionEvent e) {

			String command = e.getActionCommand();

			if ("Finish".equals(command)) {
//				openSelectedFile();
			}

			else if ("Cancel".equals(command)) {
				escapePressed();
			}

		}
*/
	}

}
