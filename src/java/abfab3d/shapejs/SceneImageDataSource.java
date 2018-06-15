/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2016
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/
package abfab3d.shapejs;

import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.ResultCodes;
import abfab3d.core.Vec;

/**
 *  Take (x,y) point on physical screen and return (r,g,b) color for that point
 *
 *  @author Alan Hudson
 */
public class SceneImageDataSource implements DataSource {
    private Scene m_scene;
    private ImageSetup m_setup;
    private Camera m_camera;
    private DataSource m_root;

    public SceneImageDataSource(Scene scene, ImageSetup setup, Camera camera) {
        m_scene = scene;
        m_camera = camera;
        m_setup = setup;

        m_root = (DataSource) m_scene.getRenderingSource(!m_setup.bumpMaps);
    }

    @Override
    public int getDataValue(Vec pnt, Vec dataValue) {
        // TODO: Need real value, just return point for now
        dataValue.v[0] = pnt.v[0];
        dataValue.v[1] = pnt.v[1];
        dataValue.v[2] = pnt.v[2];
        dataValue.v[3] = 1;
        return ResultCodes.RESULT_OK;
    }

    @Override
    public int getChannelsCount() {
        return 4;
    }

    @Override
    public Bounds getBounds() {
        return null;
    }
}
