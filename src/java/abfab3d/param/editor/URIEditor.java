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
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;


/**
 * URI Editor
 *
 * @author Alan Hudson
 */
public class URIEditor extends BaseEditor implements ActionListener, FocusListener {

    static final int EDITOR_SIZE = 10;

    JTextField  m_textField;
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

        m_textField = new JTextField(EDITOR_SIZE);
        Object val = m_param.getValue();
        String sval = "";
        if (val != null) {
            sval = val.toString();
        }
        m_textField.setText(sval);
        m_textField.addActionListener(this);
        m_textField.addFocusListener(this);

        m_open = new JButton("...");
        m_open.setToolTipText("Open File");
        
        panel = new JPanel(new GridBagLayout());
        WindowUtils.constrain(panel, m_textField, 0,0,1,1,GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH, 1.,1.,2,2,2,2);
        WindowUtils.constrain(panel, m_open,      1,0,1,1,GridBagConstraints.NONE, GridBagConstraints.NORTH, 0.,1.,2,2,2,2);

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
                    String path = file.getAbsolutePath();
                    m_param.setValue(path);
                    m_textField.setText(path);
                    informParamChangedListeners();
                }

            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        String newText = m_textField.getText();
        String oldText = (String)m_param.getValue();
        if(!oldText.equals(newText)){
            m_param.setValue(newText);
            informParamChangedListeners();            
        }

    }
    
    /**
       @Override
    */
    public void focusGained(FocusEvent e){
        // do nothing 
    }
    /**
       @Override
    */
    public void focusLost(FocusEvent e){

        String newText = m_textField.getText();
        String oldText = (String)m_param.getValue();
        if(!oldText.equals(newText)){
            m_param.setValue(newText);
            informParamChangedListeners();            
        }
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
        m_textField.setText((String)m_param.getValue());
    }

}
