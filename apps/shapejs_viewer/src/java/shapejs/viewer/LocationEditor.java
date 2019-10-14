/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package shapejs.viewer;

import abfab3d.core.Location;
import abfab3d.param.ParamJson;
import abfab3d.param.LocationParameter;
import abfab3d.param.Parameter;
import abfab3d.param.editor.BaseEditor;
import abfab3d.param.editor.WindowUtils;

import abfab3d.shapejs.MatrixCamera;
import abfab3d.shapejs.Scene;
import abfab3d.shapejs.ShapeJSExecutor;

import javax.swing.*;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static abfab3d.core.Output.printf;

/**
 * Editor for Location parameters
 *
 * @author Alan Hudson
 */
public class LocationEditor extends BaseEditor implements ActionListener, MouseListener {
    static final int EDITOR_SIZE = 36;
    static public boolean DEBUG = true;
    private Scene m_scene;
    private TextField  m_textField;
    private JButton m_button;
    private JPanel m_panel;
    private Component m_parent;
    private final MouseListener m_handler = this;
    private ShapeJSExecutor m_picker;
    private Navigator m_examineNav;
    private Navigator m_objNav;
    private int m_width;
    private int m_height;


    public LocationEditor(Parameter param, Scene scene, ShapeJSExecutor backend, final Component parent, Navigator examineNav, Navigator objNav, int width, int height) {
        super(param);


        if (examineNav == null) throw new IllegalArgumentException("ExamineNav cannot be null");
        if (objNav == null) throw new IllegalArgumentException("ObjectNav cannot be null");

        m_picker = backend;
        m_scene = scene;
        m_examineNav= examineNav;
        m_objNav = objNav;
        m_parent = parent;
        m_width = width;
        m_height = height;

        m_textField = new TextField(EDITOR_SIZE);

        m_textField.addActionListener(this);

        m_button = new JButton("...");
        m_button.setToolTipText("Select Location");

        m_panel = new JPanel();

        m_panel.setLayout(new GridBagLayout());
        WindowUtils.constrain(m_panel, m_textField, 0,0,1,1,gbc.HORIZONTAL, gbc.CENTER, 1., 0.1, 0,0,0,0); 
        WindowUtils.constrain(m_panel, m_button,    1,0,1,1,    gbc.NONE,       gbc.CENTER,  0., 0.1, 0,0,0,0); 

        m_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_parent.addMouseListener(m_handler);
            }
        });
        updateUI();

    }

    public void setScene(Scene scene) {
        m_scene = scene;
    }

    public void setWindowSize(int w, int h) {
        m_width = w;
        m_height = h;
    }

    @Override
    public Component getComponent() {
        return m_panel;
    }

    /**
       @Override
    */
    public void updateUI() {
        String json = ParamJson.getValueAsJsonString((LocationParameter) m_param);
        m_textField.setText(json);
    }

    /**
       
     */
    public void actionPerformed(ActionEvent e) {
        //user edited text directly
        ParamJson.getParamValueFromJson(m_textField.getText(), m_param);
        updateUI();
        informParamChangedListeners();
        
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        MatrixCamera camera = new MatrixCamera();
        Matrix4f mat = new Matrix4f();
        m_examineNav.getMatrix(mat);

        mat.invert();
        camera.setViewMatrix(mat);

        Matrix4f omat = new Matrix4f();
        m_objNav.getMatrix(omat);
        Vector3f pos = new Vector3f();
        Vector3f normal = new Vector3f();

        if(DEBUG)printf("calling picker\n");

        m_picker.pick(m_scene,camera,omat,e.getX(),m_height-e.getY(),m_width,m_height,pos,normal,0.5f);
        
        if(DEBUG)printf("picked pos: %s  normal: %s\n",pos,normal);

        LocationParameter lp = (LocationParameter) m_param;
        Location loc = new Location(new Vector3d(pos), new Vector3d(normal));
        lp.setValue(loc);

        updateUI();

        m_parent.removeMouseListener(m_handler);
        informParamChangedListeners();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    static private GridBagConstraints gbc = new GridBagConstraints();

}
