/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2016
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/
package abfab3d.param.editor;

import abfab3d.param.FunctionParameter;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 
 *
 * @author Vladimir Bulatov
 */
public class FunctionEditor extends BaseEditor implements ActionListener {

    private FunctionParameter m_param;
    private JButton m_button;

    public FunctionEditor(FunctionParameter param) {
        super(param);
        m_param = param;
    }

	@Override
	public void actionPerformed(ActionEvent e) {
        m_param.informListeners();
        //informParamChangedListeners();
	}

    /**

     @Override
     */
    public Component getComponent() {

    	if (m_button == null) {    	
            m_button = new JButton(m_param.getLabel());
            m_button.addActionListener(this);
        }
        return m_button;
    }

    /**
       @Override 
     */
    public void updateUI(){

    }

}
