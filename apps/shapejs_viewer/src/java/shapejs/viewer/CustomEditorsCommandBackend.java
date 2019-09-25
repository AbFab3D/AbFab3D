package shapejs.viewer;

import abfab3d.param.Editor;
import abfab3d.param.Parameter;
import abfab3d.shapejs.Scene;

import java.awt.*;

/**
 * Custom editors using the CommandBackend impl
 *
 * @author Alan Hudson
 */
public class CustomEditorsCommandBackend extends BaseCustomEditors {
    private PickingListener pickListener;


    public CustomEditorsCommandBackend(Component parent, Navigator examineNav, Navigator objNav, PickingListener l) {
        super(parent, examineNav, objNav);
        
        // TODO: Won't work for adding different listeners to different param editors
        pickListener = l;
    }

    public void setScene(Scene scene) {
        // ignored
    }
    
    public void setWindowSize(int w, int h) {
        m_width = w;
        m_height = h;

        /*
        // TODO: ADH: No local scene for remote rendering?
        for(Editor e: sceneEditors) {
            ((LocationEditorRemote)e).setWindowSize(w,h);
        }
        */
    }

    @Override
    public Editor createEditor(Parameter param) {
        switch(param.getType()) {
            case LOCATION:
                // TODO: ADH: Fix
                /*
                LocationEditorCommandBackend ler = new LocationEditorCommandBackend(param, m_parent, m_examineNav,
                        m_objNav, m_width, m_height, pickListener);

                return ler;
                */
        }

        return null;
    }
}
