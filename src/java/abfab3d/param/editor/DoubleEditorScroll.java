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

import java.util.Vector;

import java.awt.Dimension;
import java.awt.Component;

import javax.swing.JButton;

import abfab3d.param.DoubleParameter;

import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.Output.printf;


public class DoubleEditorScroll extends BaseEditor { 
  
    
    DoubleParameter m_dparam;

    Vector valueListeners=null;
    ScrollTextField m_textField;
    JButton buttonUp, buttonDown;

    double m_currentIncrement = 0.0001;
    
    int m_length;

    NumberScroller m_scroller;

    final Dimension sliderDimension = new Dimension(15,25);
    
    double m_minRange = -Double.MAX_VALUE;
    double m_maxRange = Double.MAX_VALUE;
    
    static final int DEFAULT_LENGTH = 10;
    
    public DoubleEditorScroll(DoubleParameter parameter){
        this(parameter, DEFAULT_LENGTH);
    }

    public DoubleEditorScroll(DoubleParameter parameter, int length){
        
        super(parameter);
        m_dparam = parameter;
        
        m_scroller = new NumberScroller(m_dparam.getValue(),m_dparam.getMinRange(),m_dparam.getMaxRange(),  0.);
        m_scroller.addChangedListener(new NumberChangedListener());
        updateUI();
        
    }
    
    public Component getComponent(){
        return m_scroller;
    }
    
    
    public void setParam(DoubleParameter parameter){
        
        m_dparam = parameter;
        updateUI();
        
    }
           

    public void updateUI(){

        //TODO update range 
        m_scroller.setValue(m_dparam.getValue().doubleValue()); 

    }
    

    /**
       listener of value changed in NumberScroller
     */
    class NumberChangedListener implements ChangedListener {

        public void valueChanged(Object value){
            double newValue = m_scroller.getValue();
            m_dparam.setValue(newValue);
            informParamChangedListeners();
        }
    }

}
