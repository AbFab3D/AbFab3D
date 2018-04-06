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

import static abfab3d.core.Output.printf;

/**
 * Edits Enum Parameter
 *
 * @author Tony Wong
 */
public class EnumEditor extends BaseEditor implements ActionListener {

    final static boolean DEBUG = false;
    static final int EDITOR_SIZE = 10;

    private EnumParameter m_eparam;
    private JComboBox component;

    public EnumEditor(EnumParameter param) {
        super(param);
        m_eparam = param;
    }

    /**

     @Override
     */
	public void actionPerformed(ActionEvent e) {
	    ValueWrapper wrapper = (ValueWrapper) component.getSelectedItem();

        m_param.setValue(wrapper.value);
        if(DEBUG)printf("EnumEditor.informParamChangedListeners()\n");
        informParamChangedListeners();
        
	}

    /**

     @Override
     */
    public Component getComponent() {

    	if (component != null) return component;
    	
        String[] vals = m_eparam.getValues();
        String[] labels = m_eparam.getLabels();

        ValueWrapper[] wrappers = new ValueWrapper[vals.length];
        ValueWrapper selected = null;
        for(int i=0; i < vals.length; i++) {
            String label = vals[i];
            if (labels != null) {
                label = labels[i];
            }
            wrappers[i] = new ValueWrapper(vals[i],label);
            if (m_eparam.getValue().equals(vals[i])) 
                selected = wrappers[i];
        }

        component = new JComboBox(wrappers);
        component.setSelectedItem(selected);
        component.addActionListener(this);
        
        return component;
    }

    /**

       @Override
     */
    public void updateUI(){
        
        if(DEBUG){
            printf("EnumEditor.updateUI()\n");
            //Thread.currentThread().dumpStack();
        }
	    ValueWrapper selected = (ValueWrapper) component.getSelectedItem();
        String paramValue = m_eparam.getValue();
        if(!selected.value.equals(paramValue)){
            // need to updatw UI
            component.setSelectedItem(new ValueWrapper(m_eparam.getValue(),m_eparam.getLabel()));
        }
        
    }

    static class ValueWrapper {
        private String value;
        private String label;

        public ValueWrapper(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String toString() {
            return label;
        }

        public boolean equals(Object o) {
            if(!(o instanceof ValueWrapper))
                return false;
            else
                return ((ValueWrapper)o).value.equals(value);
        }

        public int hashCode() {
            return value.hashCode();
        }
    }
}
