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

import abfab3d.param.SNodeFactory;
import abfab3d.param.Parameterizable;
import abfab3d.param.SNodeParameter;

import javax.swing.*;

import java.awt.Point;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;

/**
 * Edits SNode Parameter
 *
 * @author Vladimir Bulatov
 */
public class SNodeEditor extends BaseEditor  {

    private SNodeParameter m_param;
    private JComponent m_panel;
    private ParamPanel child;
        
    private JTextField m_textField;
 
    private JButton 
        m_editButton,
        m_newButton;
    
    public SNodeEditor(SNodeParameter param) {
        super(param);
        m_param = param;

        m_panel = makeComponent();

    }
    
    Point getNewLocation(){

        Point pnt = m_panel.getLocationOnScreen();
        Dimension dim = m_panel.getSize();
        pnt.x += dim.width;
        return pnt;
    }

    void editNode(){

        Object p = m_param.getValue();

        if(!(p instanceof Parameterizable)){
            throw new RuntimeException(fmt("can't edit param: %s\n", p));
        }

        Parameterizable node = (Parameterizable)p;

        ParamPanel panel = child;
        if(panel == null){
            panel = new ParamPanel(node);
            panel.addParamChangedListeners(getParamChangedListeners());
            child = panel;
            panel.setLocation(getNewLocation());
        }

        panel.setVisible(true);

        informParamChangedListeners();
    }


    void newNode(int index){
        
        SNodeFactory factory  = m_param.getSNodeFactory();
        String names[] = factory.getNames();
        Parameterizable node = (Parameterizable)factory.createSNode(names[index]);
        m_param.setValue(node);

        m_textField.setText(node.getClass().getSimpleName());

        ParamPanel panel = new ParamPanel(node);
        panel.addParamChangedListeners(getParamChangedListeners());
        if(child != null)
            child.setVisible(false);

        child = panel;
        
        panel.setLocation(getNewLocation());
        panel.setVisible(true);

        updateUI();

        informParamChangedListeners();
    }

    /**

       @Override
     */
    public Component getComponent() {

        return m_panel;

    }

    String makeDisplayString(){
        Object value = m_param.getValue();
        if(value != null) return value.getClass().getSimpleName();        
        else return "null";
    }

    protected JComponent makeComponent(){
        
        JPanel panel = new JPanel();

        panel.setLayout(new GridBagLayout());
        m_textField = new JTextField(10);
        m_textField.setEditable(false);
        m_textField.setText(makeDisplayString());

        m_editButton = new JButton(">");
        m_editButton.setToolTipText("edit node");
        m_newButton = new JButton("+");
        m_newButton.setToolTipText("create/replace node");

        m_editButton.addActionListener(new EditAction());
        m_newButton.addActionListener(new NewAction());
                        
        WindowUtils.constrain(panel, m_textField, 0,0,1,1, 
                              GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH, 1.,1.,2,2,2,2);
        WindowUtils.constrain(panel, m_editButton, 1,0,1,1, 
                              GridBagConstraints.NONE, GridBagConstraints.NORTH, 1.,1.,2,2,2,2);
        WindowUtils.constrain(panel, m_newButton, 2,0,1,1, 
                              GridBagConstraints.NONE, GridBagConstraints.NORTH, 1.,1.,2,2,2,2);
        
        updateUI();
        return panel;

    }

    public void updateUI(){
                
    }

    class EditAction implements ActionListener {

        public void actionPerformed(ActionEvent e){
            editSelectedItem();
        }
        
        void editSelectedItem(){
            editNode();
        }        
    } // class EditAction 

    class NewAction implements ActionListener {

        public void actionPerformed(ActionEvent e){
            printf("New\n");
            JPopupMenu menu = new JPopupMenu();            
            String names[]= m_param.getSNodeFactory().getNames();
            if(names.length < 1) 
                return;
                
            for(int i=0; i < names.length; i++) {
                JMenuItem item = new JMenuItem(names[i]);
                item.addActionListener(new NewItemAction(i));
                menu.add(item);                
            }
            Component c = (Component)e.getSource();
            menu.show(c, 0, c.getSize().height);
        }
    } // class NewAction 

    class NewItemAction  implements ActionListener {

        int index;
        NewItemAction(int index){
            this.index = index;
        }
        public void actionPerformed(ActionEvent e){
            printf("newNode: %d\n", index);
            newNode(index);
        }        
    } // class NewItemAction
}
