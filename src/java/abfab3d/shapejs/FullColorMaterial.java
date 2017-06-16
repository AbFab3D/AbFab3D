/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2017
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

import abfab3d.core.Color;
import abfab3d.core.DataSource;
import abfab3d.core.Material;
import abfab3d.core.MaterialShader;
import abfab3d.core.MaterialType;
import abfab3d.param.EnumParameter;
import abfab3d.param.Parameter;
import abfab3d.param.StringParameter;

/**
 * A material which shows all the colors
 *
 * @author Alan Hudson
 */
public class FullColorMaterial extends BasePrintableMaterial implements MaterialShader {
    private PhongParams sparams = new PhongParams();
    private static Material instance = null;
    static final String FULL_COLOR = "FullColor";


    public FullColorMaterial() {
        this(FULL_COLOR);
    }

    public FullColorMaterial(String name) {
        this(name,FULL_COLOR);
    }

    public FullColorMaterial(String name, String label) {
        super(name,label);

//        sparams.setDiffuseColor(new Color(241f/255,241f/255,234f/255)); // #F1F1EA
        sparams.setDiffuseColor(new Color(0.97,0.97,0.97)); // #F1F1EA
        sparams.setSpecularColor(new Color(1,1,1)); // #000000
        sparams.setAlbedo(new Color(0,0,0)); // #000000
        sparams.setShininess(0);
        sparams.setAmbientIntensity(0.1);
//        sparams.setRoughness(0.2);
        mp_renderingParams.setValue(sparams);

        setMaterialType(MaterialType.COLOR_MATERIAL);
    }


    public MaterialShader getShader() {
        return this;
    }

    public DataSource getRenderingSource(DataSource source) {
        return source;
    }

    public RenderingParams getShaderParams() {
        return sparams;
    }

    public static Material getInstance() {
        if (instance != null) return instance;
        instance = new FullColorMaterial();

        return instance;
    }
}
