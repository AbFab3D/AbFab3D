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

import abfab3d.shapejs.ProjectItem;
import abfab3d.shapejs.Variant;

/**
 * Project Data Model operations.
 *
 * Maintains a listener list of interested parties.  They will be handed live representations of shared state.
 *
 *
 * Project Management/Metadata saving is implicit
 * Resource Saving is explicit
 * A Runner determines when to reload resources into the Renderer
 *
 * @author Alan Hudson
 */
public interface ProjectModel {
    /**
     * Open a project for editing
     * @param filename
     */
    void openProject(String filename);

    /**
     * Close the current project.
     */
    void closeProject();

    /**
     * Add a resource to this project
     * @param res
     */
    void addResource(ProjectItem res);

    /**
     * Delete a resource from this project.
     * @param res
     */
    void deleteResource(ProjectItem res);

    /**
     * Notification that a resource has been updated.  This can be from internal saving or disk based
     * @param res
     */
    void updateResource(ProjectItem res);

    /**
     * Runs the project.  Saves all modified resources and then runs the project
     * @param variant The variant to run or null to run the main script
     */
    void run();

    /**
     * Adds a listener for project change.  Duplicates shall be ignored
     * @param l
     */
    void addProjectListener(ProjectListener l);

    /**
     * Removes a listener for project changes.  Unknown listeners will be ignored
     * @param l
     */
    void removeProjectListener(ProjectListener l);
}
