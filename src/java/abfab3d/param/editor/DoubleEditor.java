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

import abfab3d.param.DoubleParameter;
import abfab3d.util.Unit;
import abfab3d.util.Units;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Edits Double Parameter
 *
 * @author Alan Hudson
 */
public class DoubleEditor extends BaseEditor implements ChangeListener {

    static final int EDITOR_SIZE = 10;

    private DoubleParameter m_param;
    private SpinnerModel spinnerModel;


    public DoubleEditor(DoubleParameter param) {
        m_param = param;
    }


    @Override
    public void stateChanged(ChangeEvent e) {
        m_param.setValue((Double)spinnerModel.getValue());
        if (m_listener != null) m_listener.paramChanged(m_param);
    }

    /**

     @Override
     */
    public Component getComponent() {
        setSpinnerModel();
        JSpinner ret_val = new JSpinner(spinnerModel);

        ret_val.addChangeListener(this);
        return ret_val;
    }

    private void setSpinnerModel() {
        double def_val = (Double) m_param.getDefaultValue();
        double min_val = m_param.getMinRange();
        double max_val = m_param.getMaxRange();
        max_val = 1000;

        double step = m_param.getStep();

        step = 1 * Units.MM;
        spinnerModel = new SpinnerNumberModel(def_val, min_val, max_val, step);
    }
    
    public SpinnerModel getModel() {
    	return spinnerModel;
    }
}
