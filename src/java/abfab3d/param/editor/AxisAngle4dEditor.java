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
import abfab3d.param.AxisAngle4dParameter;
import abfab3d.param.Parameter;
import abfab3d.param.ParamChangedListener;
import abfab3d.param.Editor;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.vecmath.AxisAngle4d;

/**
 * Edits AxisAngle4d Parameter
 *
 * @author Vladimkir Bulatov
 */
public class AxisAngle4dEditor extends BaseEditor implements ParamChangedListener {

    private AxisAngle4dParameter m_param;
    private JPanel mainPanel;
    
    private DoubleParameter 
        m_xparam,
        m_yparam,
        m_zparam,
        m_aparam;
    
    public AxisAngle4dEditor(AxisAngle4dParameter param) {
        super(param);
        m_param = param;
    }

    @Override
        public void paramChanged(Parameter p) {

        double x = m_xparam.getValue();
        double y = m_yparam.getValue();
        double z = m_zparam.getValue();
        double a = m_aparam.getValue();

    	m_param.setValue(new AxisAngle4d(x, y, z,a));
    	
        informParamChangedListeners();

    }
    
    /**
       
       @Override
    */
    public Component getComponent() {

    	AxisAngle4d val = m_param.getValue();
      	
    	if (mainPanel == null) {
    		mainPanel = new JPanel();
    	}
        
    	m_xparam = new DoubleParameter("X", "X Position", val.x);
    	m_yparam = new DoubleParameter("Y", "Y Position", val.y);
    	m_zparam = new DoubleParameter("Z", "Z Position", val.z);
    	m_aparam = new DoubleParameter("Angle", "Angle", val.angle);

        EditorFactory factory = EditorFactory.getInstance();
        
    	Editor x_editor = factory.createEditor(m_xparam);
    	Editor y_editor = factory.createEditor(m_yparam);
    	Editor z_editor = factory.createEditor(m_zparam);
    	Editor a_editor = factory.createEditor(m_aparam);
    	
    	Component x_comp = x_editor.getComponent();
    	Component y_comp = y_editor.getComponent();
    	Component z_comp = z_editor.getComponent();
    	Component a_comp = a_editor.getComponent();

        x_editor.addParamChangedListener(this);
        y_editor.addParamChangedListener(this);
        z_editor.addParamChangedListener(this);
        a_editor.addParamChangedListener(this);

    	mainPanel.removeAll();
        mainPanel.setLayout(new GridLayout(4,1));
    	mainPanel.add(x_comp);
    	mainPanel.add(y_comp);
    	mainPanel.add(z_comp);
    	mainPanel.add(a_comp);
    	
    	return mainPanel;
    }
    
    /**
       @Override 
     */
    public void updateUI(){
        //TODO 
    }

}
