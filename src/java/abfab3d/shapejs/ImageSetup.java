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

import javax.vecmath.Matrix4f;

/**
 * Image generation setup parameters.
 *
 * @author Alan Hudson
 */
public class ImageSetup implements Cloneable {
    public static final int IMAGE_JPEG = 0;
    public static final int IMAGE_PNG = 1;
    
    public static final Matrix4f defaultView = new Matrix4f(new float[] {1f,0,0,0, 0,1f,0,0, 0,0,1f,-3, 0,0,0,1f});

    /** Width in pixels of the image */
    public int width;

    /** Height in pixels of the image */
    public int height;

    /** Viewpoint matrix */
    public Matrix4f view;

    /** The type of image IMAGE_* */
    public int imgType;

    /** The quality of the surface marching, 1.0 is highest sampling */
    public float quality;

    /** How to antialias the image */
    public AntiAliasingType aa;

    /** The background, RGB */
    public float[] backgroundColor;

    /** Should we display material bump maps */
    public boolean bumpMaps;

    /** Should lights cast shadows */
    public float shadowQuality;

    /** How many ray samples for lights */
    public int lightSamples;

    public RenderingMaterial renderingMaterial;
    public RenderingStyle renderingStyle;

    public ImageSetup(int width, int height, Matrix4f view, int imgType, float quality,
                      AntiAliasingType aa, float[] backgroundColor, boolean bumpMaps,
                      float shadowQuality, int lightSamples) {
        this.width = width;
        this.height = height;
        this.view = view;
        this.imgType = imgType;
        this.quality = quality;
        this.aa = aa;
        this.backgroundColor = backgroundColor;
        this.bumpMaps = bumpMaps;
        this.shadowQuality = shadowQuality;
        this.lightSamples = lightSamples;
        this.renderingStyle = RenderingStyle.MATERIAL;
    }

    public ImageSetup(int width, int height, Matrix4f view, int imgType, float quality, AntiAliasingType aa,
                      RenderingStyle style, float[] backgroundColor, boolean bumpMaps,
                      float shadowQuality, int lightSamples) {
        this.width = width;
        this.height = height;
        this.view = view;
        this.imgType = imgType;
        this.quality = quality;
        this.aa = aa;
        this.backgroundColor = backgroundColor;
        this.bumpMaps = bumpMaps;
        this.shadowQuality = shadowQuality;
        this.lightSamples = lightSamples;
        this.renderingStyle = style;
    }

    public ImageSetup() {
        width = 512;
        height = 512;
        view = new Matrix4f();
        view.setIdentity();
        imgType = IMAGE_JPEG;
        quality = 0.5f;
        aa = AntiAliasingType.NONE;
        backgroundColor = new float[3];
        bumpMaps = false;
        shadowQuality = 0;
        lightSamples = 1;
        this.renderingStyle = RenderingStyle.MATERIAL;
    }

    /**
     * Get a key for properties that affect OpenCL code
     * @return
     */
    public String getCLKey() {
        return "BM:" + bumpMaps + " Mat:" + renderingMaterial.getClass().getSimpleName();
    }

    public ImageSetup clone() {
        try {
            return (ImageSetup) super.clone();
        } catch(CloneNotSupportedException cns) {
            return null;
        }
    }
}
