package ide.plugins.renderer.gpu;

import abfab3d.param.Parameter;
import abfab3d.param.Editor;
import abfab3d.shapejs.Scene;
import ide.PickingListener;
import shapejs.viewer.BaseCustomEditors;
import shapejs.viewer.Navigator;

import java.awt.*;

/**
 * Custom editors
 *
 * @author Tony Wong
 */
public class CustomEditorsRemote extends BaseCustomEditors {
    private Scene m_scene;
    private PickingListener pickListener;
    
    
    public CustomEditorsRemote(Component parent, Navigator examineNav, Navigator objNav, PickingListener l) {
        super(parent, examineNav, objNav);
        
        // TODO: Won't work for adding different listeners to different param editors
        pickListener = l;
    }

    public void setScene(Scene scene) {
        m_scene = scene;
    }
    
    public void setWindowSize(int w, int h) {
        m_width = w;
        m_height = h;
        
        // TODO: No local scene for remote rendering?
        for(Editor e: sceneEditors) {
            ((LocationEditorRemote)e).setWindowSize(w,h);
        }
    }

    @Override
    public Editor createEditor(Parameter param) {
        switch(param.getType()) {
            case LOCATION:
                LocationEditorRemote ler = new LocationEditorRemote(param, m_parent, m_examineNav,
                        m_objNav, m_width, m_height, pickListener);

                return ler;
        }

        return null;
    }
}
