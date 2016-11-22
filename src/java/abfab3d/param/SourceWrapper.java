package abfab3d.param;

/**
 * Wraps a source so it can be tracked.  It's paramString value will be the source until the source is modified.
 *
 * @author Alan Hudson
 */
public interface SourceWrapper {
    /**
     * Set the source for this grid.  This will be returned as the getParamString for this object until a setter is called.
     */
    public void setSource(String val);

    public String getParamString();
    public void getParamString(StringBuilder sb);
}
