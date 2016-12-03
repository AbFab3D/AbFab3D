/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.param.editor;

import abfab3d.param.Parameter;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;


/**
 * URI Editor
 *
 * @author Alan Hudson
 */
public class URIEditor extends BaseEditor implements ActionListener {

    static final int EDITOR_SIZE = 10;

    TextField  m_textField;
    JButton m_open;
    /** The last directory property */
    private static final String LASTDIR_PROPERTY = "History_";

    /** Parent frame used to handle the file dialog */
    private Component parent;

    /** The dialog used to select the file to open */
    private final JFileChooser fc;

    private JPanel panel;

    public URIEditor(Parameter param) {
        super(param);

        m_textField = new TextField(EDITOR_SIZE);
        Object val = m_param.getValue();
        String sval = "";
        if (val != null) {
            sval = val.toString();
        }
        m_textField.setText(sval);
        m_textField.addActionListener(this);

        m_open = new JButton("Open");
        m_open.setToolTipText("Open File");

        panel = new JPanel(new FlowLayout());
        panel.add(m_open);
        panel.add(m_textField);

        String user_dir = System.getProperty("user.dir");

        Preferences prefs = Preferences.userNodeForPackage(URIEditor.class);

        String last_dir = prefs.get(LASTDIR_PROPERTY, null);
        String dir;

        if (last_dir != null)
            dir = last_dir;
        else
            dir = user_dir;

        fc = new JFileChooser(dir);

        m_open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showDialog(parent,"Open File");
                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    File file = fc.getSelectedFile();

                    String dir = file.getPath();

                    int idx = dir.lastIndexOf(File.separator);

                    if (idx > 0) {
                        dir = dir.substring(0, idx);

                        Preferences prefs = Preferences.userNodeForPackage(URIEditor.class);

                        prefs.put(LASTDIR_PROPERTY, dir);
                    }

                    m_param.setValue(file.getAbsolutePath());
                    informParamChangedListeners();
                }

            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        informParamChangedListeners();
    }

    /**
       
       @Override
    */
    public Component getComponent() {
        
        return panel;
        
    }

    /**
       @Override 
     */
    public void updateUI(){
        //TODO 
    }

}
