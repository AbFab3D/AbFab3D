package abfab3d.param.editor;

import abfab3d.param.Parameter;

/**
 * Base code for all parameter editors
 *
 * @author Alan Hudson
 */
public abstract class BaseEditor implements Editor {
    protected ParamChangedListener m_listener;
    protected Parameter m_param;
    public BaseEditor(Parameter param){
        m_param = param;
    }

    /**
     * Get notification of any parameter changes from this editor
     * @param l
     */
    public void addChangeListener(ParamChangedListener l) {
        m_listener = l;
    }

    public void informListeners(){
        if(m_listener != null)
            m_listener.paramChanged(m_param);
    }

}
