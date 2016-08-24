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

import abfab3d.core.Color;
import abfab3d.param.*;

import javax.vecmath.Vector3d;

/**
 * Light for rendering.  Can be either a PointLight or an AreaLight based on the radius.
 *
 * @author Alan Hudson
 */
public class Light extends BaseParameterizable {
    private Vector3dParameter mp_position = new Vector3dParameter("position","Position",new Vector3d(0,0,0));
    private ColorParameter mp_color = new ColorParameter("color","Color",new Color(1,1,1));
    private DoubleParameter mp_intensity = new DoubleParameter("intensity","How intense", 1);
    private DoubleParameter mp_ambientIntensity = new DoubleParameter("ambientIntensity","Ambient light", 0);
    private BooleanParameter mp_castShadows = new BooleanParameter("castShadows","CastShadows",false);
    private IntParameter mp_samples = new IntParameter("samples","Samples", 1);
    private DoubleParameter mp_radius = new DoubleParameter("radius","Radius", 1);

    private Parameter m_aparam[] = new Parameter[]{
            mp_position,
            mp_color,
            mp_intensity,
            mp_ambientIntensity,
            mp_castShadows,
            mp_samples,
            mp_radius
    };

    public Light() {
        initParams();
    }

    public Light(Vector3d position, Color color, double ambientIntensity, double intensity) {
        initParams();
        setPosition(position);
        setColor(color);
        setAmbientIntensity(ambientIntensity);
        setIntensity(intensity);
    }

    protected void initParams(){
        super.addParams(m_aparam);
    }

    public Vector3d getPosition() {
        return mp_position.getValue();
    }

    public void setPosition(Vector3d position) {
        mp_position.setValue((Vector3d) position.clone());
    }

    public Color getColor() {
        return mp_color.getValue();
    }

    public void setColor(Color color) {
        mp_color.setValue((Color) color.clone());
    }

    public double getAmbientIntensity() {
        return mp_ambientIntensity.getValue();
    }

    public void setAmbientIntensity(double ambientIntensity) {
        mp_ambientIntensity.setValue(ambientIntensity);
    }

    public double getIntensity() {
        return mp_intensity.getValue();
    }

    public void setIntensity(double intensity) {
        mp_intensity.setValue(intensity);
    }

    public boolean getCastShadows() {
        return mp_castShadows.getValue();
    }

    public void setCastShadows(boolean castShadows) {
        this.mp_castShadows.setValue(castShadows);
    }

    public void setSamples(int samples) {
        mp_samples.setValue(samples);
    }

    public int getSamples() {
        return mp_samples.getValue();
    }

    public void setRadius(double radius) {
        mp_radius.setValue(radius);
    }

    public double getRadius() {
        return mp_radius.getValue();
    }
}
