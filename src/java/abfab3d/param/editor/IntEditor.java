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

import abfab3d.param.IntParameter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Edits Double Parameter
 *
 * @author Alan Hudson
 */
public class IntEditor extends BaseEditor implements ChangeListener {

    static final int EDITOR_SIZE = 10;

    private IntParameter m_param;
    private SpinnerModel spinnerModel;


    public IntEditor(IntParameter param) {
        super(param);
        m_param = param;
    }


    @Override
    public void stateChanged(ChangeEvent e) {
        m_param.setValue(spinnerModel.getValue());
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

        int def_val = m_param.getValue();
        int min_val = m_param.getMinRange();
        int max_val = m_param.getMaxRange();
        int step = 1;
        spinnerModel = new SpinnerNumberModel(def_val, min_val, max_val, step);
    }
    
    public SpinnerModel getModel() {
    	return spinnerModel;
    }

    /**
       @Override 
     */
    public void updateUI(){

        spinnerModel.setValue(m_param.getValue()); 

    }
}
