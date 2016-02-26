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

import abfab3d.param.DoubleParameter;

import java.awt.TextField;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static abfab3d.util.Output.printf;

/**
 * Edits Double Parameter
 *
 * @author Alan Hudson
 */
public class DoubleEditor extends BaseEditor implements ActionListener {

    static final int EDITOR_SIZE = 10;

    private DoubleParameter m_param;

    TextField  m_textField;
    
    public DoubleEditor(DoubleParameter param) {

        m_param = param;
        m_textField = new TextField(EDITOR_SIZE);
        m_textField.setText(m_param.getValue().toString());
        m_textField.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (m_listener != null) m_listener.paramChanged(m_param);
    }

    /**
       
       @Override
    */
    public Component getComponent() {
        
        return m_textField;
        
    }
}
