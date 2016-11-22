package abfab3d.param;

import abfab3d.core.Initializable;

/**
 * This object has an expensive initialization.  Caching of its values to result is appropriate
 *
 * @author Alan Hudson
 */
public interface ExpensiveInitializable extends Initializable {
    /**
     * Get the value hash where each parameter's value is used
     * @return
     */
    public String getParamString();

    public void getParamString(StringBuilder sb);
}
