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

import abfab3d.param.*;
import java.util.List;

/**
 * Scene lighting information
 *
 * @author Alan Hudson
 */

public class SceneLights extends BaseParameterizable {
    protected SNodeListParameter mp_lights = new SNodeListParameter("lights");

    public SceneLights() {
        addParam(mp_lights);
    }

    public void setLights(Light[] lights) {
        mp_lights.clear();

        for (int i = 0; i < lights.length; i++) {
            mp_lights.add(lights[i]);
        }

        clearParams();
        addParam(mp_lights);
    }

    public List<Light> getLights() {
        return mp_lights.getValue();
    }

    public int getNumLights() {
        return mp_lights.getValue().size();
    }
}
