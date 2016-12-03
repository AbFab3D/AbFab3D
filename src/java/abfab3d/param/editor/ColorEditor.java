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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;


/**
 * Color Editor
 *
 * @author Vladimir Bulatov
 */
public class ColorEditor extends BaseEditor implements ActionListener {

    static final int EDITOR_SIZE = 10;

    JColorChooser chooser;
    TextField  m_textField;
    JButton m_open;
    JPanel panel;

    public ColorEditor(final Parameter param) {
        super(param);

        chooser = new JColorChooser();
        m_textField = new TextField(EDITOR_SIZE);
        abfab3d.core.Color val = (abfab3d.core.Color) m_param.getValue();
        String sval = "";
        if (val != null) {
            sval = val.toHEX();
        }
        m_textField.setText(sval);
        m_textField.addActionListener(this);

        m_open = new JButton("Choose");
        m_open.setToolTipText("Choose Color");

        panel = new JPanel(new FlowLayout());
        panel.add(m_open);
        panel.add(m_textField);

        m_open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color c = JColorChooser.showDialog(null, param.getName(), Color.WHITE);
                if (c != null) {
                    abfab3d.core.Color ac = abfab3d.core.Color.fromColor(c);
                    m_param.setValue(ac);
                    m_textField.setText(ac.toHEX());
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
