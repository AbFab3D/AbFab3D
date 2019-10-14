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
import static abfab3d.core.Output.fmt;

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
    private Matrix4f view;
    private Matrix4f objTrans;

    /** The type of image IMAGE_* */
    public int imgType;

    /** The quality of the surface marching, 1.0 is highest sampling */
    public float quality;

    /** How many aa samples to use */
    public int aa;

    /** Should we display material bump maps */
    public boolean bumpMaps;

    /** Should lights cast shadows */
    public float shadowQuality;

    public int maxRayBounces;
    public RenderingStyle renderingStyle;
    /** Should we flip the image vertically */
    public boolean flipImage;

    public ImageSetup(int width, int height, Matrix4f view, int imgType, float quality,
                      int aa, boolean bumpMaps,
                      float shadowQuality) {
        this.width = width;
        this.height = height;
        this.view = view;
        this.imgType = imgType;
        this.quality = quality;
        this.aa = aa;
        this.bumpMaps = bumpMaps;
        this.shadowQuality = shadowQuality;
        this.renderingStyle = RenderingStyle.MATERIAL;
        this.maxRayBounces = 0;
        this.objTrans = new Matrix4f();
        objTrans.setIdentity();
    }

    public ImageSetup(int width, int height, Matrix4f view, int imgType, float quality,
                      AntiAliasingType aa, boolean bumpMaps,
                      float shadowQuality) {
        this.width = width;
        this.height = height;
        this.view = view;
        this.imgType = imgType;
        this.quality = quality;
        this.aa = AntiAliasingType.getNumSamples(aa);
        this.bumpMaps = bumpMaps;
        this.shadowQuality = shadowQuality;
        this.renderingStyle = RenderingStyle.MATERIAL;
        this.maxRayBounces = 0;
        this.objTrans = new Matrix4f();
        objTrans.setIdentity();
    }

    public ImageSetup(int width, int height, Matrix4f view, int imgType, float quality, AntiAliasingType aa,
                      RenderingStyle style, boolean bumpMaps,
                      float shadowQuality) {
        this.width = width;
        this.height = height;
        this.view = view;
        this.imgType = imgType;
        this.quality = quality;
        this.aa = AntiAliasingType.getNumSamples(aa);
        this.bumpMaps = bumpMaps;
        this.shadowQuality = shadowQuality;
        this.renderingStyle = style;
        this.maxRayBounces = 0;
        this.objTrans = new Matrix4f();
        objTrans.setIdentity();
    }

    public ImageSetup(int width, int height, Matrix4f view, int imgType, float quality,
                      AntiAliasingType aa, boolean bumpMaps,
                      float shadowQuality, int maxRayBounces) {
        this.width = width;
        this.height = height;
        this.view = view;
        this.imgType = imgType;
        this.quality = quality;
        this.aa = AntiAliasingType.getNumSamples(aa);
        this.bumpMaps = bumpMaps;
        this.shadowQuality = shadowQuality;
        this.renderingStyle = RenderingStyle.MATERIAL;
        this.maxRayBounces = maxRayBounces;
        this.objTrans = new Matrix4f();
        objTrans.setIdentity();
    }

    public ImageSetup(int width, int height, Matrix4f view, int imgType, float quality,
                      int aa, boolean bumpMaps,
                      float shadowQuality, int maxRayBounces) {
        this.width = width;
        this.height = height;
        this.view = view;
        this.imgType = imgType;
        this.quality = quality;
        this.aa = aa;
        this.bumpMaps = bumpMaps;
        this.shadowQuality = shadowQuality;
        this.renderingStyle = RenderingStyle.MATERIAL;
        this.maxRayBounces = maxRayBounces;
        this.objTrans = new Matrix4f();
        objTrans.setIdentity();
    }

    public ImageSetup(int width, int height, Matrix4f view, int imgType, float quality, AntiAliasingType aa,
                      RenderingStyle style, boolean bumpMaps,
                      float shadowQuality, int maxRayBounces) {
        this.width = width;
        this.height = height;
        this.view = view;
        this.imgType = imgType;
        this.quality = quality;
        this.aa = AntiAliasingType.getNumSamples(aa);
        this.bumpMaps = bumpMaps;
        this.shadowQuality = shadowQuality;
        this.renderingStyle = style;
        this.maxRayBounces = maxRayBounces;
        this.objTrans = new Matrix4f();
        objTrans.setIdentity();
    }

    public ImageSetup() {
        width = 512;
        height = 512;
        view = new Matrix4f();
        view.setIdentity();
        imgType = IMAGE_JPEG;
        quality = 0.5f;
        this.aa = AntiAliasingType.getNumSamples(AntiAliasingType.NONE);
        bumpMaps = false;
        shadowQuality = 0;
        this.renderingStyle = RenderingStyle.MATERIAL;
        maxRayBounces = 0;
        this.objTrans = new Matrix4f();
        objTrans.setIdentity();
    }

    public void setFlipImage(boolean val) {
        flipImage = val;
    }

    public void setViewTransform(Matrix4f transform){
        this.view = transform;
    }

    public Matrix4f getViewTransform(){
        return this.view;
    }

    public void setObjectTransform(Matrix4f transform){
        this.objTrans = transform;
    }

    public Matrix4f getObjectTransform(){
        return this.objTrans;
    }

    /**
     * Get a key for properties that affect OpenCL code
     * @return
     */
    public String getCLKey() {
        return "BM:" + bumpMaps;
    }

    public ImageSetup clone() {
        try {
            return (ImageSetup) super.clone();
        } catch(CloneNotSupportedException cns) {
            return null;
        }
    }

    public static String toString(ImageSetup setup) {
        return fmt("ImageSetup  w: %d h: %d quality: %4.2f aa: %d bumpMaps: %b shadows: %4.2f maxRay: %d",
                setup.width,setup.height,setup.quality,setup.aa,setup.bumpMaps,setup.shadowQuality,setup.maxRayBounces);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setView(Matrix4f view) {
        this.view = view;
    }

    public void setObjTrans(Matrix4f objTrans) {
        this.objTrans = objTrans;
    }

    public void setImgType(int imgType) {
        this.imgType = imgType;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }

    public void setAa(int aa) {
        this.aa = aa;
    }

    public void setBumpMaps(boolean bumpMaps) {
        this.bumpMaps = bumpMaps;
    }

    public void setShadowQuality(float shadowQuality) {
        this.shadowQuality = shadowQuality;
    }

    public void setMaxRayBounces(int maxRayBounces) {
        this.maxRayBounces = maxRayBounces;
    }

    public void setRenderingStyle(RenderingStyle renderingStyle) {
        this.renderingStyle = renderingStyle;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Matrix4f getView() {
        return view;
    }

    public Matrix4f getObjTrans() {
        return objTrans;
    }

    public int getImgType() {
        return imgType;
    }

    public float getQuality() {
        return quality;
    }

    public int getAa() {
        return aa;
    }

    public boolean isBumpMaps() {
        return bumpMaps;
    }

    public float getShadowQuality() {
        return shadowQuality;
    }

    public int getMaxRayBounces() {
        return maxRayBounces;
    }

    public RenderingStyle getRenderingStyle() {
        return renderingStyle;
    }

    public boolean isFlipImage() {
        return flipImage;
    }
}
