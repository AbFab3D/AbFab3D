package viewer;

import abfab3d.grid.Bounds;

import javax.vecmath.Matrix4f;
import java.awt.*;

/**
 * A navigator provides a navigation style.
 *
 * @author Alan Hudson
 */
public interface Navigator {
    /** Have the navigation params changed since last call. */
    public boolean hasChanged();

    /** Get the navigation params as a view matrix */
    public void getViewMatrix(Matrix4f mat);

    /** Set the world bounds */
    public void setBounds(Bounds bounds, double vs);

    /** Initialize AWT component actions */
    public void init(Component comp);
}
