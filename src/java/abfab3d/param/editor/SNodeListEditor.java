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
import abfab3d.param.SNodeListParameter;

import javax.swing.*;

import java.awt.Dimension;
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

    private SNodeListParameter m_param;
    private JComboBox cbox;
    private JComponent m_panel;
    private ArrayList<ParamPanel> children;
        
    private JButton m_editButton,
        m_newButton,
        m_deleteButton;
    
    public SNodeListEditor(SNodeListParameter param) {
        super(param);
        m_param = param;

        children = new ArrayList<ParamPanel>();

        m_panel = makeComponent();

    }


    @Override
    public void actionPerformed(ActionEvent e) {

        int sel = cbox.getSelectedIndex();
        editNode(sel);

    }

    void editNode(int index){
        
        List list = m_param.getValue();
        Parameterizable node = (Parameterizable)list.get(index);

        printf("Got selection: %s\n", node);
        WindowManager wm = WindowManager.getInstance();
        int lastY = wm.getLastY();

        //TODO reuse existing editor 
        ParamPanel panel = new ParamPanel(node);
        panel.addParamChangedListener(m_listener);
        children.add(panel);

        panel.setLocation(565,lastY);
        panel.setVisible(true);

        informListeners();
    }

    void newNode(int index){
        
        SNodeFactory factory  = m_param.getSNodeFactory();
        String names[] = factory.getNames();
        Parameterizable node = (Parameterizable)factory.createSNode(names[index]);
        m_param.add(node);
        printf("created new: %s\n", node);
        WindowManager wm = WindowManager.getInstance();
        int lastY = wm.getLastY();
        ParamPanel panel = new ParamPanel(node);
        panel.addParamChangedListener(m_listener);
        children.add(panel);

        panel.setLocation(565,lastY);
        panel.setVisible(true);

        enableButtons();

        informListeners();
    }

    void removeNode(int index){

        //TODO remove editor panel 
        List list = m_param.getValue();
        list.remove(index);
                
        enableButtons();
        informListeners();
    }

    /**

     @Override
     */
    public Component getComponent() {
        return m_panel;

    }

    protected JComponent makeComponent(){
        
        JPanel panel = new JPanel();
        m_editButton = new JButton("edit");
        m_newButton = new JButton("new");
        m_deleteButton = new JButton("delete");

        m_editButton.addActionListener(new EditAction());
        m_newButton.addActionListener(new NewAction());
        m_deleteButton.addActionListener(new DeleteAction());

        panel.add(m_editButton);
        panel.add(m_newButton);
        panel.add(m_deleteButton);
        
        enableButtons();
        return panel;

    }

    protected void enableButtons(){
        List list = m_param.getValue();
        int size = list.size();

        m_editButton.setEnabled((size > 0));
        m_deleteButton.setEnabled((size > 0));
        int namesCount = m_param.getSNodeFactory().getNames().length;
        printf("factory names count: %d \n", namesCount);
        m_newButton.setEnabled((namesCount > 0));

    }

    
    protected JComponent makeCombobox(){                
        
        List list = m_param.getValue();
        String[] vals = new String[list.size()];
        int len = list.size();
        for(int i=0; i < len; i++) {
            Parameterizable field  = (Parameterizable) list.get(i);
            vals[i] = field.getClass().getSimpleName();
        }
        JComboBox cbox = new JComboBox(vals);
        cbox.setSelectedIndex(-1);
        cbox.addActionListener(this);

        return cbox;
        
    }

    class EditAction implements ActionListener {

        public void actionPerformed(ActionEvent e){

            JPopupMenu menu = new JPopupMenu();
            printf("Edit\n");
            List list = m_param.getValue();
            Object nodes[] = list.toArray();
            
            for(int i=0; i < nodes.length; i++) {
                Parameterizable node  = (Parameterizable) nodes[i];
                String name = node.getClass().getSimpleName();
                JMenuItem item = new JMenuItem(name);
                item.addActionListener(new EditItemAction(i));
                menu.add(item);                
            }
            Component c = (Component)e.getSource();
            menu.show(c, 0, c.getSize().height);
            
        }
    }

    class EditItemAction  implements ActionListener {
        int index;
        EditItemAction(int index){
            this.index = index;
        }
        public void actionPerformed(ActionEvent e){
            printf("editItem: %d\n", index);
            editNode(index);
        }
        
    }

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
    }

    class NewItemAction  implements ActionListener {

        int index;
        NewItemAction(int index){
            this.index = index;
        }
        public void actionPerformed(ActionEvent e){
            printf("newNode: %d\n", index);
            newNode(index);
        }
        
    }

    class DeleteAction implements ActionListener {
        public void actionPerformed(ActionEvent e){
            JPopupMenu menu = new JPopupMenu();
            printf("Delete\n");
            List list = m_param.getValue();
            Object nodes[] = list.toArray();
            
            for(int i=0; i < nodes.length; i++) {
                Parameterizable node  = (Parameterizable) nodes[i];
                String name = node.getClass().getSimpleName();
                JMenuItem item = new JMenuItem(name);
                item.addActionListener(new RemoveItemAction(i));
                menu.add(item);                
            }
            Component c = (Component)e.getSource();
            menu.show(c, 0, c.getSize().height);
        }
    }   

    class RemoveItemAction  implements ActionListener {

        int index;
        RemoveItemAction(int index){
            this.index = index;
        }
        public void actionPerformed(ActionEvent e){
            printf("removeNode: %d\n", index);
            removeNode(index);
        }
        
    }

}
