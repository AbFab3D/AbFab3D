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

import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import static abfab3d.core.Output.printf;


/**
 * Edits default parameter 
 *
 * @author Vladimir Bulatov
 */
public class FormattedTextEditor extends BaseEditor implements FocusListener {

    static final int EDITOR_WIDTH = 10;
    static final int EDITOR_HEIGHT = 5;

    TextArea  m_textArea;
    
    public FormattedTextEditor(Parameter param) {
        super(param);
        m_textArea = new TextArea(EDITOR_WIDTH, EDITOR_HEIGHT);
        Object val = m_param.getValue();
        String sval = "";
        if (val != null) {
            sval = val.toString();
        }
        m_textArea.setText(sval);
        m_textArea.addFocusListener(this);
        m_textArea.addTextListener(new MyTextListener());

    }
    
    @Override
    public void focusGained(FocusEvent e) {
    	
    }

    @Override
    public void focusLost(FocusEvent e) {
        updateTextValue();
    }

    void updateTextValue(){
        String newValue = m_textArea.getText();
        m_param.setValue(newValue);
        informParamChangedListeners();
    }
    /**
       
       @Override
    */
    public Component getComponent() {
        
        return m_textArea;
        
    }


    /**
       @Override 
     */
    public void updateUI(){
        
        m_textArea.setText((String)m_param.getValue());
        
    }

    class MyTextListener implements TextListener {
        public void textValueChanged(TextEvent e){
            //updateTextValue();
        }
    }

}
