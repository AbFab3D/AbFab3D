package shapejs;

import abfab3d.param.Parameter;
import org.mozilla.javascript.ScriptableObject;

/**
 * Javascript wrapper for Parameters
 *
 * @author Alan Hudson
 */
public interface JSWrapper {
    public void setParameter(Parameter param);

    public Parameter getParameter();
}
