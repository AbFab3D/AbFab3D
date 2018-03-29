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

import abfab3d.param.Parameterizable;
import abfab3d.param.Parameter;
import abfab3d.param.ParamChangedListener;
import abfab3d.param.Editor;

import java.awt.*;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.*;

import java.util.HashMap;

import static abfab3d.core.Output.printf;

/**
 * Creates an editing panel for a parameterizable
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov
 */
public class ParamPanel extends JPanel {

    static final int SPACE = 2;

    private java.util.List<Parameterizable> m_node;
    private static EditorFactory sm_factory;
    private Vector<ParamChangedListener> m_plisteners;
    
    private ArrayList<Editor> editors;
    private boolean closeAllowed;
    private JScrollPane scrollPane;
    private JTabbedPane tabbedPane;
    private JPanel parametersPanel;


    public ParamPanel(Parameterizable node) {
        this(node.getClass().getSimpleName(), node);
    }

    public ParamPanel(String title, Parameterizable node) {

        m_node = new ArrayList<>();
        m_node.add(node);

        buildUI(node.getParams());

    }

    public ParamPanel(String name, Parameter params[]) {

        buildUI(params);
    }

    public void setParams(Parameterizable node) {
        m_node = new ArrayList<>();
        m_node.add(node);

        buildUI(node.getParams());
    }


    protected void buildUI( Parameter params[]){
        
        editors = new ArrayList<Editor>();
        setLayout(new GridBagLayout());
        
        createFactory();

        Vector<ParamGroup> groups = makeGroups(params);

        if(groups.size() == 1) {
            if (scrollPane == null) {
                parametersPanel = makeParamPanel(params);
                scrollPane = new JScrollPane(parametersPanel);
            } else {
                makeParamPanel(params,parametersPanel);
            }
            WindowUtils.constrain(this, scrollPane, 0,0,1,1,
                                  GridBagConstraints.BOTH, GridBagConstraints.NORTH, 1.,1.,2,2,2,2);
            
        } else {
            if (tabbedPane == null) {
                tabbedPane = new JTabbedPane();
            } else {
                tabbedPane.removeAll();
            }

            for(int k = 0; k < groups.size(); k++){
                ParamGroup group = groups.get(k);
                String gname = group.name;
                Parameter gpar[] = group.getParamArray(k);
                Component panel = makeParamPanel(gpar);
                scrollPane = new JScrollPane(panel);
                tabbedPane.addTab(gname, scrollPane);                
            }            
            WindowUtils.constrain(this,tabbedPane, 0,0,1,1,
                                  GridBagConstraints.BOTH, GridBagConstraints.NORTH, 1.,1.,2,2,2,2);
        }

        WindowManager wm = WindowManager.getInstance();
        wm.addPanel(this);
    }

    public ParamPanel(java.util.List<Parameterizable> nodes) {

        editors = new ArrayList<Editor>();
        setLayout(new GridBagLayout());
        m_node = nodes;
        createFactory();

        Component parametersPanel = makeParamPanel(m_node);
        scrollPane = new JScrollPane(parametersPanel);

        WindowUtils.constrain(this, scrollPane, 0,0,1,1,
                              GridBagConstraints.BOTH, GridBagConstraints.NORTH, 1.,1.,2,2,2,2);

        WindowManager wm = WindowManager.getInstance();
        wm.addPanel(this);
    }

    public void clearParamChangedListeners() {
        if (m_plisteners != null) {
            m_plisteners.clear();
        }
    }

    /**
     * Get notification of any parameter changes from this editor
     * @param listener
     */
    public void addParamChangedListener(ParamChangedListener listener) {
        if(m_plisteners == null){
            m_plisteners = new Vector<ParamChangedListener>();
        }
        m_plisteners.add(listener);
        
        for(Editor e: editors) {
            e.addParamChangedListener(listener);
        }
    }

    public void addParamChangedListeners(Vector<ParamChangedListener> listeners) {
        if(listeners == null)
            return;
        for(int i = 0; i < listeners.size(); i++){
            addParamChangedListener(listeners.get(i));
        }
    }


    protected Component makeParamPanel(java.util.List<Parameterizable> nodes){

        int tot = 0;

        for(Parameterizable node : nodes) {
            Parameter[] param = node.getParams();
            tot += param.length;
        }
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        int cnt = 0;
        for(Parameterizable node : nodes) {
            // TODO: Having some visual separator would be nice
            Parameter[] param = node.getParams();

            for (int i = 0; i < param.length; i++) {

                double hWeight = (i < tot - 1) ? (0.) : (1.);

                WindowUtils.constrain(panel, new JLabel(param[i].getName()), 0, cnt, 1, 1,
                        GridBagConstraints.NONE, GridBagConstraints.NORTHEAST, 0., hWeight, SPACE, SPACE, SPACE, 0);

                Editor editor = sm_factory.createEditor(param[i]);
                editor.addParamChangedListeners(m_plisteners);
                editors.add(editor);

                WindowUtils.constrain(panel, editor.getComponent(), 1, cnt, 1, 1,
                        GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH, 1., hWeight, SPACE, SPACE, SPACE, 0);

                cnt++;

            }
        }
        return panel;
                
    }

    protected JPanel makeParamPanel(Parameter param[]){

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        for(int i=0; i < param.length; i++) {
            
            double hWeight = (i < param.length - 1) ? (0.) : (1.);
            
            WindowUtils.constrain(panel, new JLabel(param[i].getName()), 0, i, 1, 1,
                                  GridBagConstraints.NONE, GridBagConstraints.NORTHEAST, 0., hWeight, SPACE, SPACE, SPACE, 0);
            
            Editor editor = sm_factory.createEditor(param[i]);
            editor.addParamChangedListeners(m_plisteners);
            editors.add(editor);            
            WindowUtils.constrain(panel, editor.getComponent(), 1, i, 1, 1,
                                  GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH, 1., hWeight, SPACE, SPACE, SPACE, 0);
        }
        return panel;
                
    }

    protected void makeParamPanel(Parameter param[],JPanel panel) {

        panel.removeAll();
        panel.setLayout(new GridBagLayout());

        printf("***Adding params to panel: %d\n",param.length);
        for(int i=0; i < param.length; i++) {

            double hWeight = (i < param.length - 1) ? (0.) : (1.);

            WindowUtils.constrain(panel, new JLabel(param[i].getName()), 0, i, 1, 1,
                    GridBagConstraints.NONE, GridBagConstraints.NORTHEAST, 0., hWeight, SPACE, SPACE, SPACE, 0);

            Editor editor = sm_factory.createEditor(param[i]);
            editor.addParamChangedListeners(m_plisteners);
            editors.add(editor);
            WindowUtils.constrain(panel, editor.getComponent(), 1, i, 1, 1,
                    GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH, 1., hWeight, SPACE, SPACE, SPACE, 0);
        }
    }

    public static Component makePanel(Parameter param[], ParamChangedListener listener){

        createFactory();
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        for (int i = 0; i < param.length; i++) {
            
            double hWeight = (i <  param.length - 1) ? (0.) : (1.);
            
            WindowUtils.constrain(panel, new JLabel(param[i].getName()), 0, i, 1, 1,
                                  GridBagConstraints.NONE, GridBagConstraints.NORTHEAST, 0., hWeight, SPACE, SPACE, SPACE, 0);
            
            Editor editor = sm_factory.createEditor(param[i]);
            editor.addParamChangedListener(listener);
            
            WindowUtils.constrain(panel, editor.getComponent(), 1, i, 1, 1,
                                  GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTH, 1., hWeight, SPACE, SPACE, SPACE, 0);
                        
        }    
        return panel;
                
    }

    protected static void createFactory(){
        if(sm_factory == null)
            sm_factory = new EditorFactory();
    }


    public void closeWithChildren(){
        //TODO 
        // close all children 
        setVisible(false);
        
    }

    public boolean isCloseAllowed() {
        return closeAllowed;
    }

    public void setCloseAllowed(boolean val) {
        closeAllowed = val;
    }
    
    public ArrayList<Editor> getEditors() {
    	return editors;
    }

    static final String NOGROUP = "none";

    public static Vector<ParamGroup> makeGroups(Parameter param[]){

        HashMap<String,ParamGroup> map = new HashMap<String,ParamGroup>();
        Vector<ParamGroup> groups = new Vector<ParamGroup>();
        for(int i =0; i < param.length; i++){
            Parameter par = param[i];
            String gname = par.getGroup();
            if(gname == null) gname = NOGROUP;
            ParamGroup pg = map.get(gname);
            if(pg == null) {
                pg = new ParamGroup(gname);
                map.put(gname, pg);
                groups.add(pg);
            }
            pg.add(par);
        }
        return groups;
    } 

    static class ParamGroup {

        String name;
        Vector<Parameter> param = new Vector<Parameter>();

        ParamGroup(String name){
            this.name = name;
        }

        void add(Parameter par){
            param.add(par);
        }
        Parameter[] getParamArray(int index){
            Parameter par[] = new Parameter[param.size()];
            param.copyInto(par);
            return par;
        }

    }    
}
