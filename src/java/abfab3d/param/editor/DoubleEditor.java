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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * Edits Double Parameter
 *
 * @author Alan Hudson
 */
public class DoubleEditor extends BaseEditor implements ActionListener {

    static final int EDITOR_SIZE = 10;

    private DoubleParameter m_param;
    private SpinnerModel spinnerModel;

    
    public DoubleEditor(DoubleParameter param) {
        m_param = param;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (m_listener != null) m_listener.paramChanged(m_param);
    }

    /**
       
       @Override
    */
    public Component getComponent() {
      setSpinnerModel();
      return new JSpinner(spinnerModel);
    }
    
    private void setSpinnerModel() {
    	double def_val = (Double) m_param.getDefaultValue();
    	double min_val = m_param.getMinRange();
    	double max_val = m_param.getMaxRange();
    	double step = m_param.getStep();
    	spinnerModel = new SpinnerNumberModel(def_val, min_val, max_val, step);
    }
}
