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

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.xj3d.ui.awt.widgets.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;

// Local imports

/**
 * A toolbar for the Volume Viewer
 *
 * @author Alan Hudson
 */
public class Toolbar extends JPanel {
    /**
     * Default examine button image
     */
    private static final String DEFAULT_EXAMINE_BUTTON =
            "images/navigation/ButtonExamine.gif";

    /**
     * Default pan button image
     */
    private static final String DEFAULT_PAN_BUTTON =
            "images/navigation/ButtonPan.gif";

    /**
     * Default track button image
     */
    private static final String DEFAULT_TRACK_BUTTON =
            "images/navigation/ButtonTExamine.gif";

    /**
     * Default track button image
     */
    private static final String DEFAULT_RESET_BUTTON =
            "images/navigation/ButtonHome.gif";

    /**
     * Default track button image
     */
    private static final String DEFAULT_LOOKAT_BUTTON =
            "images/navigation/ButtonLookat.gif";

    /**
     * Default track button image
     */
    private static final String DEFAULT_MEASURE_BUTTON =
            "classes/images/navigation/ButtonMeasure.gif";

    /**
     * Reporter instance for handing out errors
     */
    private ErrorReporter reporter;

    /**
     * Button group holding the navigation state buttons
     */
    private ButtonGroup navStateGroup;

    /**
     * Button representing the pan navigation state
     */
    private JToggleButton panButton;

    /**
     * Button representing the examine navigation state
     */
    private JToggleButton examineButton;

    /**
     * Button representing the track navigation state
     */
    private JToggleButton trackButton;

    /**
     * Button representing the track navigation state
     */
    private JToggleButton resetButton;
    private JToggleButton lookatButton;
    private JToggleButton measureButton;

    /**
     * The actions
     */
    private AbstractAction examineAction;
    private AbstractAction panAction;
    private AbstractAction trackAction;
    private AbstractAction resetAction;
    private AbstractAction lookatAction;
    private AbstractAction measureAction;


    /**
     * Create a new navigation toolbar with an empty list of viewpoints but
     * controllable direction for the buttons. The user selection is disabled.
     */
    public Toolbar(BaseVolumeViewer viewer) {
        setLayout(new GridLayout(1, 3));
        reporter = DefaultErrorReporter.getDefaultReporter();

        navStateGroup = new ButtonGroup();

        String img_name = DEFAULT_PAN_BUTTON;
        ImageIcon icon = IconLoader.loadIcon(img_name, reporter);
        panAction = new ToolbarAction(icon, "PAN", null, viewer);
        panAction.setEnabled(true);

        panButton = new JToggleButton(panAction);

        panButton.setMargin(new Insets(0, 0, 0, 0));
        panButton.setToolTipText("Pan");
        panButton.setEnabled(true);
        navStateGroup.add(panButton);
        add(panButton);

        img_name = DEFAULT_TRACK_BUTTON;
        icon = IconLoader.loadIcon(img_name, reporter);

        trackAction = new ToolbarAction(icon, "TRACK", null, viewer);
        trackAction.setEnabled(false);

        trackButton = new JToggleButton(trackAction);

        trackButton.setMargin(new Insets(0, 0, 0, 0));
        trackButton.setToolTipText("Track");
        trackButton.setEnabled(true);
        navStateGroup.add(trackButton);
        add(trackButton);

        img_name = DEFAULT_EXAMINE_BUTTON;
        icon = IconLoader.loadIcon(img_name, reporter);
        KeyStroke acc_key = KeyStroke.getKeyStroke(KeyEvent.VK_E,
                KeyEvent.CTRL_MASK);

        examineAction = new ToolbarAction(icon, "EXAMINE", acc_key, viewer);
        examineAction.setEnabled(false);

        examineButton = new JToggleButton(examineAction);

        examineButton.setMargin(new Insets(0, 0, 0, 0));
        examineButton.setToolTipText("Examine");
        examineButton.setEnabled(true);
        navStateGroup.add(examineButton);
        add(examineButton);


        img_name = DEFAULT_RESET_BUTTON;
        icon = IconLoader.loadIcon(img_name, reporter);

        resetAction = new ToolbarAction(icon, "RESET", null, viewer);
        resetAction.setEnabled(true);

        resetButton = new JToggleButton(resetAction);

        resetButton.setMargin(new Insets(0, 0, 0, 0));
        resetButton.setToolTipText("Reset View");
        resetButton.setEnabled(true);
        navStateGroup.add(resetButton);
        add(resetButton);

        img_name = DEFAULT_LOOKAT_BUTTON;
        icon = IconLoader.loadIcon(img_name, reporter);

        lookatAction = new ToolbarAction(icon, "LOOKAT", null, viewer);
        lookatAction.setEnabled(true);

        lookatButton = new JToggleButton(lookatAction);

        lookatButton.setMargin(new Insets(0, 0, 0, 0));
        lookatButton.setToolTipText("Lookat");
        lookatButton.setEnabled(true);
        navStateGroup.add(lookatButton);
        add(lookatButton);

        img_name = DEFAULT_MEASURE_BUTTON;
//        icon = IconLoader.loadIcon(img_name, reporter);
        icon = loadIcon(img_name);

        //measureAction = new ToolbarAction(icon, "MEASURE", null, viewer);
        measureAction = new MeasureAction(icon, "MEASURE", null, viewer, null);
        measureAction.setEnabled(true);

        measureButton = new JToggleButton(measureAction);

        measureButton.setMargin(new Insets(0, 0, 0, 0));
        measureButton.setToolTipText("Measure");
        measureButton.setEnabled(true);
        navStateGroup.add(measureButton);
        add(measureButton);

    }

    public ImageIcon loadIcon(String name) {
        ImageIcon ret_val = null;

        Image img = loadImage(name);

        if (img != null) {
            ret_val = new ImageIcon(img, name);
        }

        return ret_val;
    }

    public Image loadImage(String name) {
        Image ret_val = null;

        URL url = ClassLoader.getSystemResource(name);
        Toolkit tk = Toolkit.getDefaultToolkit();

        if (url != null)
            ret_val = tk.createImage(url);

        // Fallback for WebStart
        if (ret_val == null) {
            url = Toolbar.class.getClassLoader().getResource(name);

            if (url != null)
                ret_val = tk.createImage(url);

        }

        if (ret_val == null) {
            ret_val = tk.createImage(name);
        }

        return ret_val;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the Examine Action.  Suitable for use in a menu.
     *
     * @return Returns the action
     */
    public AbstractAction getExamineAction() {
        return examineAction;
    }

    /**
     * Get the Pan Action.  Suitable for use in a menu.
     *
     * @return Returns the action
     */
    public AbstractAction getPanAction() {
        return panAction;
    }

    /**
     * Get the Track Action.  Suitable for use in a menu.
     *
     * @return Returns the action
     */
    public AbstractAction getTrackAction() {
        return trackAction;
    }
}
