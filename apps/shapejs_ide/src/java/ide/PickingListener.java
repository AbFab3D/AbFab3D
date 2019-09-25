package ide;

import java.util.EventListener;

import abfab3d.param.editor.BaseEditor;

/**
 * Notifications about changes in picking.
 *
 * @author Tony Wong
 */
public interface PickingListener extends EventListener {
    public void pickedChanged(BaseEditor editor, int x, int y);
    public void setPickResultListener(PickResultListener l);
}
