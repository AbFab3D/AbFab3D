package ide.plugins.renderer.gpu;

import abfab3d.core.Location;
import abfab3d.param.ParamJson;
import abfab3d.param.LocationParameter;
import abfab3d.param.Parameter;
import abfab3d.param.editor.BaseEditor;
import abfab3d.param.editor.WindowUtils;

import abfab3d.shapejs.MatrixCamera;
import ide.PickResultListener;
import ide.PickingListener;
import shapejs.viewer.Navigator;

import javax.swing.*;
import javax.vecmath.Matrix4f;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;

import static abfab3d.core.Output.printf;

/**
 * Editor for Location parameters
 *
 * @author Tony Wong
 */
public class LocationEditorRemote extends BaseEditor implements ActionListener, MouseListener, PickResultListener {
    static final int EDITOR_SIZE = 36;
    static public boolean DEBUG = true;
    private TextField  m_textField;
    private JButton m_button;
    private JPanel m_panel;
    private Component m_parent;
    private final MouseListener m_handler = this;
    private Navigator m_examineNav;
    private Navigator m_objNav;
    private int m_width;
    private int m_height;
    
    private PickingListener pickingListener;
    private int pickX;
    private int pickY;


    public LocationEditorRemote(Parameter param, final Component parent, Navigator examineNav,
            Navigator objNav, int width, int height, PickingListener l) {
        
        super(param);

        if (examineNav == null) throw new IllegalArgumentException("ExamineNav cannot be null");
        if (objNav == null) throw new IllegalArgumentException("ObjectNav cannot be null");

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

        pickingListener = l;
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
    
    /**
     * Notification of a pick result change.
     * @param result
     */
    @Override
    public void pickResultChanged(Map<String, Object> pickResult) {
        if (pickResult == null) return;

        LocationParameter lp = (LocationParameter) m_param;
        Location loc = ParamJson.getLocationFromMap(pickResult);
        lp.setValue(loc);

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

        if(DEBUG)printf("calling remote picker\n");
        
        pickX = e.getX();
        pickY = m_height - e.getY();

        // Make this editor a listener for the picking result
        pickingListener.setPickResultListener(this);
        
        // Notify the picking listener of a new (x, y) pick
        pickingListener.pickedChanged(this, pickX, pickY);

        m_parent.removeMouseListener(m_handler);
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
