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

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;

import static abfab3d.util.Output.printf;

/**
 * Creates an editing panel for a parameterizable
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov
 */
public class ParamPanel extends Frame {

    static final int SPACE = 2;

    private Parameterizable m_node;
    private static EditorFactory sm_factory;
    private ParamChangedListener m_plistener;
    
    private ArrayList<Editor> editors;
    private boolean closeAllowed;

    public ParamPanel(Parameterizable node) {

        super(node.getClass().getSimpleName());

        editors = new ArrayList<Editor>();
        setLayout(new GridBagLayout());
        m_node = node;
        if(sm_factory == null)
            sm_factory = new EditorFactory();

        Component parametersPanel = makeParamPanel(m_node);
        WindowUtils.constrain(this, parametersPanel, 0,0,1,1, 
                              GridBagConstraints.BOTH, GridBagConstraints.NORTH, 1.,1.,2,2,2,2);

        this.pack();

        WindowManager wm = WindowManager.getInstance();
        wm.addPanel(this);
    }

    /**
     * Get notification of any parameter changes from this editor
     * @param l
     */
    public void addParamChangedListener(ParamChangedListener l) {
        m_plistener = l;

        for(Editor e: editors) {
            e.addChangeListener(m_plistener);
        }
    }

    Component makeParamPanel(Parameterizable node){

        Parameter[] param = node.getParams();
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        for(int i=0; i < param.length; i++){

            double hWeight = (i < param.length-1)? (0.) : (1.);
            
            WindowUtils.constrain(panel, new JLabel(param[i].getName()), 0, i, 1, 1,
                    GridBagConstraints.NONE, GridBagConstraints.NORTHEAST, 0., hWeight, SPACE, SPACE, SPACE, 0);

            Editor editor = sm_factory.createEditor(param[i]);
            editor.addChangeListener(m_plistener);
            editors.add(editor);

            WindowUtils.constrain(panel,editor.getComponent(), 1,i,1,1,
                                  GridBagConstraints.HORIZONTAL,GridBagConstraints.NORTH, 1.,hWeight, SPACE,SPACE,SPACE,0);
            
        }
        return panel;
                
    }

    public boolean isCloseAllowed() {
        return closeAllowed;
    }

    public void setCloseAllowed(boolean val) {
        closeAllowed = val;
    }
}
