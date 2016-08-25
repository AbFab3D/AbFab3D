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
package abfab3d.shapejs;

import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.Initializable;
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

        m_root = (DataSource) m_scene.getSource();

        if (m_setup.bumpMaps) {
            RenderingMaterial rm = m_scene.getRenderingMaterial();
            if (m_scene.getRenderingSource() != null) {
                m_root = (DataSource) m_scene.getRenderingSource();
            } else {
                m_root = rm.getRenderingSource(m_root);
            }
        } else {
            if (m_scene.getRenderingSource() != null) {
                m_root = (DataSource) m_scene.getRenderingSource();
            }
        }
        if (m_root instanceof Initializable) ((Initializable) m_root).initialize();
    }

    @Override
    public int getDataValue(Vec pnt, Vec dataValue) {
        // TODO: Need real value, just return blue for now
        dataValue.v[0] = 0;
        dataValue.v[1] = 0;
        dataValue.v[2] = 1;
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

    @Override
    public void setBounds(Bounds bounds) {

    }
}
