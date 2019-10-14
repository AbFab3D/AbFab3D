package shapejs.viewer;

import java.util.EventListener;
import java.util.Map;


/**
 * Notifications about a pick result.
 *
 * @author Tony Wong
 */
public interface PickResultListener extends EventListener {
    /**
     * Notification of a pick result change.
     * @param result
     */
    public void pickResultChanged(Map<String, Object> result);
}
