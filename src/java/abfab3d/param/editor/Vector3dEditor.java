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
import abfab3d.param.Vector3dParameter;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Vector3d;

/**
 * Edits Vector3d Parameter
 *
 * @author Tony Wong
 */
public class Vector3dEditor extends BaseEditor implements ChangeListener {

    static final int EDITOR_SIZE = 10;

    private Vector3dParameter m_param;
    private JPanel mainPanel;
    
    private DoubleEditor x_editor;
    private DoubleEditor y_editor;
    private DoubleEditor z_editor;
    
    public Vector3dEditor(Vector3dParameter param) {
        m_param = param;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
    	double x = (Double) x_editor.getModel().getValue();
    	double y = (Double) y_editor.getModel().getValue();
    	double z = (Double) z_editor.getModel().getValue();
    	m_param.setValue(new Vector3d(x, y, z));
    	
        if (m_listener != null) m_listener.paramChanged(m_param);
    }
    
    /**
       
       @Override
    */
    public Component getComponent() {
    	Vector3d val = m_param.getValue();
      	
    	if (mainPanel == null) {
    		mainPanel = new JPanel();
    	}

    	DoubleParameter xParam = new DoubleParameter("X", "X Position", val.x);
    	DoubleParameter yParam = new DoubleParameter("Y", "Y Position", val.y);
    	DoubleParameter zParam = new DoubleParameter("Z", "Z Position", val.z);
    	x_editor = new DoubleEditor(xParam);
    	y_editor = new DoubleEditor(yParam);
    	z_editor = new DoubleEditor(zParam);
    	
    	Component x_comp = x_editor.getComponent();
    	Component y_comp = y_editor.getComponent();
    	Component z_comp = z_editor.getComponent();
    	((JSpinner)x_comp).addChangeListener(this);
    	((JSpinner)y_comp).addChangeListener(this);
    	((JSpinner)z_comp).addChangeListener(this);
    	
    	mainPanel.removeAll();
    	mainPanel.add(x_comp);
    	mainPanel.add(y_comp);
    	mainPanel.add(z_comp);
    	
    	return mainPanel;
    }
    

}
