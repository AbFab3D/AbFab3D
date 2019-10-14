/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package ide;

import java.io.File;
import java.util.EventListener;

import abfab3d.shapejs.Variant;

/**
 * Notifications to save model.
 *
 * @author Tony Wong
 */
public interface SaveModelListener extends EventListener {
    public void saveModel(Variant v, File file, boolean cached);
    public void saveAllVariantModels();
}
