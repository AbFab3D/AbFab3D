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

import abfab3d.param.BaseParameterizable;
import abfab3d.param.SNodeListParameter;

import java.util.List;

/**
 * Scene viewpoint information
 *
 * @author Alan Hudson
 */

public class SceneViewpoints extends BaseParameterizable {
    protected SNodeListParameter mp_viewpoints = new SNodeListParameter("viewpoints");

    public SceneViewpoints() {
        addParam(mp_viewpoints);
    }

    public void setViewpoints(Viewpoint[] viewpoints) {
        mp_viewpoints.clear();

        for (int i = 0; i < viewpoints.length; i++) {
            mp_viewpoints.add(viewpoints[i]);
        }

        clearParams();
        addParam(mp_viewpoints);
    }

    public List<Viewpoint> getViewpoints() {
        return mp_viewpoints.getValue();
    }

    public int getNumViewpoints() {
        return mp_viewpoints.getValue().size();
    }
}
