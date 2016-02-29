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

import abfab3d.param.Parameterizable;
import abfab3d.param.SNodeListParameter;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import static abfab3d.util.Output.printf;

/**
 * Edits Double Parameter
 *
 * @author Alan Hudson
 */
public class SNodeListEditor extends BaseEditor implements ActionListener {

    static final int EDITOR_SIZE = 10;

    private SNodeListParameter m_param;
    private JComboBox component;
    private ArrayList<ParamPanel> children;

    public SNodeListEditor(SNodeListParameter param) {
        m_param = param;
        children = new ArrayList<ParamPanel>();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        int sel = component.getSelectedIndex();
        List list = m_param.getValue();
        Parameterizable node = (Parameterizable)list.get(sel);

        printf("Got selection: %s\n", node);
        WindowManager wm = WindowManager.getInstance();
        int lastY = wm.getLastY();
        ParamPanel panel = new ParamPanel(node);
        panel.addParamChangedListener(m_listener);
        children.add(panel);

        panel.setLocation(565,lastY);
        panel.setVisible(true);

        if (m_listener != null) m_listener.paramChanged(m_param);

    }

    /**

     @Override
     */
    public Component getComponent() {
        if (component != null) return component;


        List list = m_param.getValue();
        String[] vals = new String[list.size()];
        int len = list.size();
        for(int i=0; i < len; i++) {
            Parameterizable field  = (Parameterizable) list.get(i);
            vals[i] = field.getClass().getName();
        }
        component = new JComboBox(vals);
        component.setSelectedIndex(-1);
        component.addActionListener(this);

        return component;
    }
}
