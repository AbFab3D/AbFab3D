/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.param;

import java.awt.Component;
import java.util.Vector;


/**
 * Editor for a parameter
 *
 * @author Alan Hudson
 */
public interface Editor {
    /**
     * Get the AWT component for editing this item
     * @return
     */
    public Component getComponent();

    /**
     * Get notification of any parameter changes from this editor
     * @param listener
     */
    public void addParamChangedListener(ParamChangedListener listener);

    public void addParamChangedListeners(Vector<ParamChangedListener> listeners);

    public void updateUI();

}
