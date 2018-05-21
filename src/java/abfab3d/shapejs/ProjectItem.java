/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2018
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.shapejs;

/**
 * An item that can be included in a Project
 *
 * @author Alan Hudson
 */
public class ProjectItem {
    protected String m_path;
    protected String m_thumbnail;
    protected String m_origPath;

    public ProjectItem(String origPath,String fullPath, String thumbnail) {
        this.m_path = fullPath;
        this.m_origPath = origPath;
        this.m_thumbnail = thumbnail;
    }

    public void setThumbnail(String path) {
        m_thumbnail = path;
    }

    public String getThumbnail() {
        return m_thumbnail;
    }

    public void setPath(String path) {
        m_path = path;
    }

    public String getPath() {
        return m_path;
    }

    public String getOrigPath() {
        return m_origPath;
    }

    public void setOrigPath(String val) {
        m_origPath = val;
    }
}
