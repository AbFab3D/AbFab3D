package shapejs.viewer;

// Standard library imports

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * An action that can be used to change whether to optimize the scene
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class OptimizeAction extends AbstractAction {
    /** The status bar */
    protected StatusBar statusBar;
    protected RenderCanvas canvas;

    /**
     * Create an instance of the action class.
     *
     */
    public OptimizeAction(RenderCanvas canvas, StatusBar statusBar) {
        super("");

        this.canvas = canvas;
        this.statusBar = statusBar;

        putValue(SHORT_DESCRIPTION, "Optimize Code");
    }

    //---------------------------------------------------------------
    // Methods defined by ActionListener
    //---------------------------------------------------------------

    /**
     * An action has been performed.
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {
        String val = evt.getActionCommand();

        if (val.equalsIgnoreCase("TRUE")) {
            canvas.setOptimizeCode(true);
        } else {
            canvas.setOptimizeCode(false);
        }

        canvas.forceRender();
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

}
