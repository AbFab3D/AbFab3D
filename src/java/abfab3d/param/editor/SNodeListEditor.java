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

import java.awt.Frame;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import static abfab3d.util.Output.printf;

/**
 * Edits SNodeList Parameter
 *
 * @author Vladimir Bulatov
 */
public class SNodeListEditor extends BaseEditor {

    private SNodeListParameter m_param;
    private JComponent m_panel;
    private HashMap<Parameterizable,ParamPanel> children;
    DefaultListModel m_listDataModel;
    JList m_list;
    
    //HashMap<Parameter>
    
    private JButton m_editButton,
        m_newButton,
        m_deleteButton,
        m_movedownButton,
        m_moveupButton;
    
    public SNodeListEditor(SNodeListParameter param) {
        super(param);
        m_param = param;

        children = new HashMap<Parameterizable,ParamPanel>();

        m_panel = makeComponent();


    }
    
    Point getNewLocation1(){

        Frame frame = WindowUtils.getFrame(m_panel);
        Point pnt = frame.getLocationOnScreen();
        Dimension dim = frame.getSize();
        pnt.y += dim.height;
        return pnt;
    }

    Point getNewLocation(){

        Point pnt = m_panel.getLocationOnScreen();
        Dimension dim = m_panel.getSize();
        pnt.x += dim.width;
        return pnt;
    }


    void editNode(int index){
        
        List list = m_param.getValue();
        Parameterizable node = (Parameterizable)list.get(index);

        //printf("Got selection: %s\n", node);
        //WindowManager wm = WindowManager.getInstance();

        ParamPanel panel = children.get(node);
        if(panel == null){
            panel = new ParamPanel(node);
            panel.addParamChangedListeners(getParamChangedListeners());
            children.put(node, panel);
            panel.setLocation(getNewLocation());
        }

        panel.setVisible(true);

        informParamChangedListeners();
    }


    void newNode(int index){
        
        SNodeFactory factory  = m_param.getSNodeFactory();
        String names[] = factory.getNames();
        Parameterizable node = (Parameterizable)factory.createSNode(names[index]);
        m_param.add(node);
        printf("created new: %s\n", node);
        ParamPanel panel = new ParamPanel(node);
        panel.addParamChangedListeners(getParamChangedListeners());
        children.put(node, panel);
        
        panel.setLocation(getNewLocation());
        panel.setVisible(true);

        updateUI();

        informParamChangedListeners();
    }

    void removeNode(int index){

        List list = m_param.getValue();
        Parameterizable node = (Parameterizable)list.get(index);
        list.remove(index);
        ParamPanel panel = children.get(node);
        if(panel != null)
            panel.setVisible(false);

        updateUI();

        informParamChangedListeners();
    }

    void moveUp(int index){
        if(index <=0)
            return;

        List list = m_param.getValue();
        Parameterizable node = (Parameterizable)list.get(index);
        list.remove(index);
        
        list.add(index-1,node);
        m_list.setSelectedIndex(index-1);

        updateUI();

        informParamChangedListeners();
    }

    void moveDown(int index){
        if(index < 0)
            return;

        List list = m_param.getValue();
        if(index >= list.size()-1)
            return;
        
        Parameterizable node = (Parameterizable)list.get(index);
        list.remove(index);
        
        list.add(index+1,node);
        m_list.setSelectedIndex(index+1);

        updateUI();

        informParamChangedListeners();
    }

    /**

       @Override
     */
    public Component getComponent() {

        return m_panel;

    }

    protected JComponent makeComponent(){
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        m_editButton = new JButton(">");
        m_editButton.setToolTipText("edit selected node");
        m_newButton = new JButton("+");
        m_newButton.setToolTipText("create new node");
        m_deleteButton = new JButton("-");
        m_deleteButton.setToolTipText("delete selected");
        m_moveupButton = new JButton("^");
        m_moveupButton.setToolTipText("move node up");
        m_movedownButton = new JButton("v");
        m_movedownButton.setToolTipText("move node down");

        m_editButton.addActionListener(new EditAction());
        m_newButton.addActionListener(new NewAction());
        m_deleteButton.addActionListener(new DeleteAction());
        m_moveupButton.addActionListener(new MoveUpAction());
        m_movedownButton.addActionListener(new MoveDownAction());

        m_listDataModel = new DefaultListModel();
        m_list = new JList(m_listDataModel);
        
        JPanel bpanel = new JPanel();
        
        bpanel.add(m_editButton);
        bpanel.add(m_newButton);
        bpanel.add(m_deleteButton);
        bpanel.add(m_moveupButton);
        bpanel.add(m_movedownButton);
        
        WindowUtils.constrain(panel, m_list, 0,0,1,1, 
                              GridBagConstraints.BOTH, GridBagConstraints.NORTH, 1.,1.,2,2,2,2);
        WindowUtils.constrain(panel, bpanel, 0,1,1,1, 
                              GridBagConstraints.NONE, GridBagConstraints.NORTH, 1.,1.,2,2,2,2);
        
        updateUI();
        return panel;

    }

    protected void updateList(){

        int selectedIndex = m_list.getSelectedIndex();

        List list = m_param.getValue();
        String[] vals = new String[list.size()];
        int len = list.size();
        m_listDataModel.removeAllElements();
        for(int i=0; i < len; i++) {
            Parameterizable field  = (Parameterizable) list.get(i);
            m_listDataModel.addElement(field.getClass().getSimpleName());
        }
        if(selectedIndex >= 0 && selectedIndex < len){
            m_list.setSelectedIndex(selectedIndex);
        }
    }

    protected void updateUI(){

        updateList();
        // 
        List list = m_param.getValue();
        int size = list.size();

        m_editButton.setEnabled((size > 0));
        m_deleteButton.setEnabled((size > 0));
        int namesCount = m_param.getSNodeFactory().getNames().length;
        printf("factory names count: %d \n", namesCount);
        m_newButton.setEnabled((namesCount > 0));
        
        
    }
    
    class EditAction implements ActionListener {

        public void actionPerformed(ActionEvent e){
            editSelectedItem();
        }
        
        void editSelectedItem(){
            int index = m_list.getSelectedIndex();
            if(index >= 0) 
                editNode(index);                
        }
        
        void displayPopup(ActionEvent e){

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

    class MoveUpAction implements ActionListener {
        public void actionPerformed(ActionEvent e){
            moveupSelectedItem();
        }
        
        void moveupSelectedItem(){
            int index = m_list.getSelectedIndex();
            if(index >= 0) 
                moveUp(index);                
        }
    }

    class MoveDownAction implements ActionListener {
        public void actionPerformed(ActionEvent e){
            moveupSelectedItem();
        }
        
        void moveupSelectedItem(){
            int index = m_list.getSelectedIndex();
            if(index >= 0) 
                moveDown(index);                
        }
    }

    class DeleteAction implements ActionListener {
        public void actionPerformed(ActionEvent e){
            removeSelectedItem();
        }

        void removeSelectedItem(){
            int index = m_list.getSelectedIndex();
            if(index >= 0) 
                removeNode(index);                
        }

        void displayPopup(ActionEvent e){

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
