package viewer;

// Standard library imports
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import static abfab3d.util.Output.printf;


/**
 * An action that can be used to change the number of evaluation steps
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class StepsAction extends AbstractAction {
    /** The status bar */
    protected StatusBar statusBar;

    protected RenderCanvas canvas;
    private int numSteps;
    private static final int maxSteps = (int) Math.pow(2,14);

    /**
     * Create an instance of the action class.
     *
     */
    public StepsAction(RenderCanvas canvas, StatusBar statusBar) {
        super("");

        this.canvas = canvas;
        this.statusBar = statusBar;

        putValue(SHORT_DESCRIPTION, "Cycles the eval steps");

        numSteps = getDefaultNumberOfSteps();
    }

    //---------------------------------------------------------------
    // Methods defined by ActionListener
    //---------------------------------------------------------------

    /**
     * An action has been performed. This is the result of ALT-A being
     * selected to cycle through antialiasing sample rates that the
     * graphics card can support through cycling AA off.
     *
     * @param evt The event that caused this method to be called.
     */
    public void actionPerformed(ActionEvent evt) {
        String val = evt.getActionCommand();
        printf("Steps action: " + val);

        if (val.equals("Disabled")) {
            numSteps = 1;
        } else if (val.equals("Cycle")) {
            cycleSteps();
            return;
        } else {
            numSteps = Integer.parseInt(val);
        }

        changeSteps();
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    public int getDefaultNumberOfSteps() {
        return (int) Math.pow(2,10);
    }

    /**
     * Get the maximum number of samples.
     *
     * @return the maximum number of samples
     */
    public static int getMaximumNumberOfSteps() {
        return maxSteps;
    }

    /**
     * Set the desired steps.  This will be capped at the current system maximum.
     *
     * @param desired The desired number of samples
     */
    public void setDesiredSteps(int desired) {
        if (desired > maxSteps)
            numSteps = maxSteps;
        else
            numSteps = desired;

        changeSteps();
    }

    /**
     * Cycle through antialiasing options.
     *
     */
    private void cycleSteps() {
        numSteps = numSteps * 2;
        if (numSteps > maxSteps)
            numSteps = 1;

        changeSteps();
    }

    /**
     * Change to the current numSteps.
     */
    private void changeSteps() {
        statusBar.setStatusText("Eval steps: " + numSteps + " out of max: " + maxSteps);
        canvas.setSteps(numSteps);
    }
}
