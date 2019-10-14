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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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
import abfab3d.shapejs.VariantItem;
import shapejs.viewer.OpenAction;


/**
 * A dialog allowing the user to create a new script file.
 *
 * @author Tony Wong
 */
public class NewProjectDialog extends EscapableDialog {
    private static final String LASTDIR_PROPERTY = "LastOpenDir";
    private static final String TEMPLATE_VARIANT = "{\"scriptPath\":\"../scripts/main" + Project.EXT_SCRIPT + "\",\n\"scriptParams\" : {}\n}";
    private static final String TEMPLATE_SCRIPT = "var uiParams = [\n" +
            "];\n" +
            "\n" +
            "function main(args) {\n" +
            "\n" +
            "  var radius = 25 * MM;\n" +
            "  var sphere = new Sphere(radius);\n" +
            "\n" +
            "  var s = radius + 1*MM;\n" +
            "  var scene = new Scene(sphere, new Bounds(-s, s, -s, s, -s, s), args.voxelSize * MM);\n" +
            "\n" +
            "  return scene;\n" +
            "}\n";
    
	private RText rtext;
	private Listener listener;

    private JPanel panel;
    private JTextField nameField;
    private JTextField authorField;
    private JTextField licenseField;
    private JTextField locationField;
    private JButton finishButton;
    
    
	/**
	 * The maximum width of this dialog.
	 */
	private static final int MAX_WIDTH = 800;


	/**
	 * Constructor.
	 *
	 * @param parent The parent application.
	 */
	public NewProjectDialog(RText parent) {

		super(parent, parent.getString("Dialog.NewProject.Title"), true);
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
        
        JPanel formPanel = new JPanel(new GridLayout(4,1));

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

        JLabel nameLabel = new JLabel("Project Name:");
        nameField = new JTextField(40);
        replaceBorder(nameField);

        JPanel namePanel = new JPanel(new BorderLayout());
        JPanel nameSpacingPanel = new JPanel();
        namePanel.setBorder(new EmptyBorder(2,2,2,2));
        namePanel.add(nameLabel,BorderLayout.WEST);
        namePanel.add(nameSpacingPanel,BorderLayout.CENTER);
        namePanel.add(nameField,BorderLayout.EAST);
        formPanel.add(namePanel);

        JLabel authorLabel = new JLabel("Project Author:");
        authorField = new JTextField(40);
        replaceBorder(authorField);
        JPanel authorPanel = new JPanel(new BorderLayout());
        JPanel authorSpacingPanel = new JPanel();
        authorPanel.setBorder(new EmptyBorder(2,2,2,2));
        authorPanel.add(authorLabel,BorderLayout.WEST);
        authorPanel.add(authorSpacingPanel,BorderLayout.CENTER);
        authorPanel.add(authorField,BorderLayout.EAST);
        formPanel.add(authorPanel);

        JLabel licenseLabel = new JLabel("Project License:");
        licenseField = new JTextField(40);
        replaceBorder(licenseField);
        JPanel licensePanel = new JPanel(new BorderLayout());
        JPanel licenseSpacingPanel = new JPanel();
        licensePanel.setBorder(new EmptyBorder(2,2,2,2));
        licensePanel.add(licenseLabel,BorderLayout.WEST);
        licensePanel.add(licenseSpacingPanel,BorderLayout.CENTER);
        licensePanel.add(licenseField,BorderLayout.EAST);
        formPanel.add(licensePanel);

        EmptyBorder noBorder = new EmptyBorder(0,0,0,0);
        Insets noInset = new Insets(0,0,0,0);

        // Project location
        JPanel locationPanel = new JPanel(new BorderLayout());
        locationPanel.setBorder(new EmptyBorder(2,2,2,2));
        
        JLabel locationLabel = new JLabel("Project Location:");
        locationField = new JTextField(37);
        locationField.setEditable(false);

        JButton locationButton = new JButton(" ... ");
        locationButton.setActionCommand("Location");
        locationButton.addActionListener(listener);
        locationButton.setBorder(noBorder);
        locationButton.setMargin(noInset);

        JPanel locationValPanel = new JPanel(new BorderLayout());
        locationValPanel.setBorder(noBorder);

        locationValPanel.add(locationField,BorderLayout.WEST);
        //locationValPanel.setBorder(BorderFactory.createLineBorder(Color.black));  // debug
//        JPanel locationValPanelSpacing = new JPanel();
//        locationValPanel.setBorder(new EmptyBorder(2,2,2,2));
//        locationValPanel.add(locationValPanelSpacing,BorderLayout.CENTER);
        locationValPanel.add(locationButton,BorderLayout.EAST);

        locationPanel.add(locationLabel, BorderLayout.WEST);
//        JPanel locationSpacingPanel = new JPanel();
//        locationSpacingPanel.setBorder(new EmptyBorder(2,2,2,2));
//        locationPanel.add(locationSpacingPanel, BorderLayout.CENTER);
        locationPanel.add(locationValPanel,BorderLayout.EAST);

        formPanel.add(locationPanel);

        JLabel skipButton1 = new JLabel();
        JLabel skipButton2 = new JLabel();
        finishButton = new JButton("Finish");
        finishButton.addActionListener(listener);
        finishButton.setEnabled(false);
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
	            case "Location":
	                String dir = null;
	                String user_dir = System.getProperty("user.dir");
	
	                Preferences prefs = Preferences.userNodeForPackage(OpenAction.class);
	
	                String last_dir = prefs.get(LASTDIR_PROPERTY, null);
	
	                if (last_dir != null)
	                    dir = last_dir;
	                else
	                    dir = user_dir;
	
	                JFileChooser dialog = new JFileChooser(dir);
	                dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	
	                int ret = dialog.showOpenDialog(panel);
	                if (ret == JFileChooser.APPROVE_OPTION) {
	                    File sf = dialog.getSelectedFile();
	                    locationField.setText(sf.toString());
	                }
	
	                finishButton.setEnabled(true);
	                break;
	            case "Cancel":
	            	escapePressed();
	                break;
	            case "Finish":
	                // Create new project from template
	                String parentDir = locationField.getText() + File.separator + nameField.getText();
	                Project p = new Project();
	                p.setParentDir(parentDir);
	                p.setName(nameField.getText());
	                p.setAuthor(authorField.getText());
	                p.setLicense(licenseField.getText());

                	// Main directory
//                	String mdirStr = parentDir + File.separator + nameField.getText();
                	
	                try {
	                	File pdir = new File(parentDir);
	                	pdir.mkdirs();
	                	
	                    File sdir = new File(parentDir + File.separator + "scripts");
	                    sdir.mkdirs();

	                    File vdir = new File(parentDir + File.separator + "variants");
	                    vdir.mkdirs();

	                    File rdir = new File(parentDir + File.separator + "resources");
	                    rdir.mkdirs();

	                    String spath = "scripts" + File.separator + "main" + Project.EXT_SCRIPT;
	                    File sf = new File(parentDir + File.separator + spath);

	                    if (!sf.exists()) {
	                        FileUtils.writeStringToFile(sf, TEMPLATE_SCRIPT);
	                    }

	                    ProjectItem script = new ProjectItem(spath,sf.getAbsolutePath(), null);
	                    ArrayList<ProjectItem> scripts = new ArrayList<>();
	                    scripts.add(script);
	                    p.setScripts(scripts);

	                    String vpath = "variants" + File.separator + "default" + Project.EXT_VARIANT;
	                    File vf = new File(parentDir + File.separator + vpath);
	                    if (!vf.exists()) {
	                        FileUtils.writeStringToFile(vf, TEMPLATE_VARIANT);
	                    }

	                    VariantItem variant = new VariantItem(vpath,parentDir + File.separator + vpath,null);
	                    variant.setMainScript("scripts/main" + Project.EXT_SCRIPT);
	                    ArrayList<VariantItem> variants = new ArrayList<>();
	                    variants.add(variant);
	                    p.setVariants(variants);
	                } catch(IOException ioe) {
	                    ioe.printStackTrace();
	                }

	                try {
	                    p.save(parentDir);
	                } catch(IOException ioe) {
	                    ioe.printStackTrace();
	                }

	                String filename = parentDir + File.separator + "manifest" + Project.EXT_MANIFEST;
	                abfab3d.core.Output.printf("Loading project: %s\n",filename);
	                
	                // Open the project
	                rtext.openProject(filename);
	                
	                escapePressed();
	                break;
	            default:
	            	 abfab3d.core.Output.printf("Unhandled action: %s\n",command);
	        }
	    }
	}

}
