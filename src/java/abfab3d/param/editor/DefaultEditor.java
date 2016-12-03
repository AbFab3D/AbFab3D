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

import java.awt.TextField;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static abfab3d.core.Output.printf;


/**
 * Edits default parameter 
 *
 * @author Vladimir Bulatov
 */
public class DefaultEditor extends BaseEditor implements ActionListener {

    static final int EDITOR_SIZE = 10;

    TextField  m_textField;
    
    public DefaultEditor(Parameter param) {
        super(param);
        m_textField = new TextField(EDITOR_SIZE);
        Object val = m_param.getValue();
        String sval = "";
        if (val != null) {
            sval = val.toString();
        }
        m_textField.setText(sval);
        m_textField.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String newValue = m_textField.getText();
        m_param.setValue(newValue);

        informParamChangedListeners();
    }

    /**
       
       @Override
    */
    public Component getComponent() {
        
        return m_textField;
        
    }


    /**
       @Override 
     */
    public void updateUI(){
        //TODO 
    }

}
