package ide;

import java.util.EventListener;

/**
 * Notifications about changes in the render engine and server
 *
 * @author Tony Wong
 */
public interface RenderEngineListener extends EventListener {
    void renderEngineChanged(String engine);
    void serverChanged(String server);
}
