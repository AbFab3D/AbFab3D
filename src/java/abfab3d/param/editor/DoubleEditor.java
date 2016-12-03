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
import abfab3d.core.Units;

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

    private DoubleParameter m_dparam;
    private SpinnerModel spinnerModel;


    public DoubleEditor(DoubleParameter param) {
        super(param);
        m_dparam = param;
    }


    @Override
    public void stateChanged(ChangeEvent e) {
        m_param.setValue((Double)spinnerModel.getValue());
        informParamChangedListeners();
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
        double min_val = m_dparam.getMinRange();
        double max_val = m_dparam.getMaxRange();
        max_val = 1000;

        double step = m_dparam.getStep();

        step = 0.1 * Units.MM;
        spinnerModel = new SpinnerNumberModel(def_val, min_val, max_val, step);
    }

    /**
       @override 
     */
    public void updateUI(){
        //TODO 
    }
    
    public SpinnerModel getModel() {
    	return spinnerModel;
    }
}
