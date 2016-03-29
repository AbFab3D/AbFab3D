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

import abfab3d.param.BooleanParameter;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Edits Enum Parameter
 *
 * @author Tony Wong
 * @author Vladimir Bulatov
 */
public class BooleanEditor extends BaseEditor implements ActionListener {

    static final int EDITOR_SIZE = 10;

    private BooleanParameter m_bparam;
    private JCheckBox m_cbox;

    public BooleanEditor(BooleanParameter param) {
        super(param);
        m_bparam = param;
    }

	@Override
	public void actionPerformed(ActionEvent e) {
        m_param.setValue(new Boolean(m_cbox.isSelected()));
        informParamChangedListeners();
	}

    /**

     @Override
     */
    public Component getComponent() {
    	if (m_cbox != null) return m_cbox;
    	
        m_cbox = new JCheckBox();
        m_cbox.setSelected(m_bparam.getValue());
        m_cbox.addActionListener(this);
        return m_cbox;
    }

}
