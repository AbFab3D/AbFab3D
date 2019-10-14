package ide;

import java.util.EventListener;
import java.util.Map;


/**
 * Notifications about a pick result.
 *
 * @author Tony Wong
 */
public interface ViewListener extends EventListener {
    /**
     * Notification to reset the view.
     */
    public void resetView();
}
