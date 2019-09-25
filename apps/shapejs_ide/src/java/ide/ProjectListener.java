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

import abfab3d.shapejs.Project;
import abfab3d.shapejs.ProjectItem;
import abfab3d.shapejs.Variant;

import java.util.EventListener;

/**
 * Notifications about changes in the Project
 *
 * @author Alan Hudson
 */
public interface ProjectListener extends EventListener {
    void projectChanged(Project proj);
    void resourceAdded(ProjectItem res);
    void resourceRemoved(ProjectItem res);
    void resourceUpdated(ProjectItem res);
    
    /**
     * This variant has been selected for running, do it
     * @param variant
     */
    void runVariant(Variant variant);
    
    /**
     * Reset any panel, data, etc.
     */
    void reset();
    
    /**
     * Mark project as updated or not
     * @param updated
     */
    void setProjectUpdated(boolean updated);
    
    void setBusyMode();
    void setIdleMode();
}
