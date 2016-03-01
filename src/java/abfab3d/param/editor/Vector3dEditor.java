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
import abfab3d.param.Parameter;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.vecmath.Vector3d;

/**
 * Edits Vector3d Parameter
 *
 * @author Tony Wong
 */
public class Vector3dEditor extends BaseEditor implements ParamChangedListener {

    static final int EDITOR_SIZE = 10;

    private Vector3dParameter m_param;
    private JPanel mainPanel;
    
    private DoubleParameter 
        m_xparam,
        m_yparam,
        m_zparam;
    
    public Vector3dEditor(Vector3dParameter param) {
        super(param);
        m_param = param;
    }

    @Override
        public void paramChanged(Parameter p) {

        double x = m_xparam.getValue();
        double y = m_yparam.getValue();
        double z = m_zparam.getValue();

    	m_param.setValue(new Vector3d(x, y, z));
    	
        informListeners();

    }
    
    /**
       
       @Override
    */
    public Component getComponent() {
    	Vector3d val = m_param.getValue();
      	
    	if (mainPanel == null) {
    		mainPanel = new JPanel();
    	}
        
    	m_xparam = new DoubleParameter("X", "X Position", val.x);
    	m_yparam = new DoubleParameter("Y", "Y Position", val.y);
    	m_zparam = new DoubleParameter("Z", "Z Position", val.z);
        EditorFactory factory = EditorFactory.getInstance();
        
    	Editor x_editor = factory.createEditor(m_xparam);
    	Editor y_editor = factory.createEditor(m_yparam);
    	Editor z_editor = factory.createEditor(m_zparam);
    	
    	Component x_comp = x_editor.getComponent();
    	Component y_comp = y_editor.getComponent();
    	Component z_comp = z_editor.getComponent();
        x_editor.addChangeListener(this);
        y_editor.addChangeListener(this);
        z_editor.addChangeListener(this);

    	mainPanel.removeAll();
        mainPanel.setLayout(new GridLayout(1,3));
    	mainPanel.add(x_comp);
    	mainPanel.add(y_comp);
    	mainPanel.add(z_comp);
    	
    	return mainPanel;
    }
    

}
