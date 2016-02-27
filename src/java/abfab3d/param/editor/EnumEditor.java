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

import abfab3d.param.EnumParameter;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Edits Enum Parameter
 *
 * @author Tony Wong
 */
public class EnumEditor extends BaseEditor implements ActionListener {

    static final int EDITOR_SIZE = 10;

    private EnumParameter m_param;
    private JComboBox component;

    public EnumEditor(EnumParameter param) {
        m_param = param;
    }

	@Override
	public void actionPerformed(ActionEvent e) {
        m_param.setValue((String) component.getSelectedItem());
        if (m_listener != null) m_listener.paramChanged(m_param);
	}

    /**

     @Override
     */
    public Component getComponent() {
    	if (component != null) return component;
    	
        String[] vals = m_param.getValues();
        component = new JComboBox(vals);
        component.setSelectedItem(m_param.getValue());

        component.addActionListener(this);
        return component;
    }

}
