/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2019
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

import abfab3d.param.ParamMap;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Map;

/**
 * Execute ShapeJS commands.  Allows us to easily swap out ShapeJS implementations such as CPU verses GPU.
 * If your interested in licensing a GPU powered backend which is 10X faster than our CPU backend contact alan@shapeways.com
 *
 * @author Alan Hudson
 */
public interface ShapeJSExecutor {
    /**
     * Configure the executor.  Can be called at anytime to change values.
     *
     * @param params
     */
    public void configure(Map params);

    /**
     * Render a ShapeJS project into a raster image
     *
     * @param scene  The scene to render
     * @param camera The camera
     * @param setup  The image setup
     * @param os     The stream to write the output
     * @param format The image format to use.  Supports any formats ImageIO supports(jpeg,png,bmp,wbmp,gif).
     */
    public void renderImage(Scene scene, Camera camera, ImageSetup setup, OutputStream os, String format);

    /**
     * Render a ShapeJS project into a raster image
     *
     * @param scene  The scene to render
     * @param camera The camera
     * @param setup  The image setup
     */
    public void renderImage(Scene scene, Camera camera, ImageSetup setup, BufferedImage img);

    /**
     * Render a scene to a triangle based format
     *
     * @param scene The scene to render
     */
    public void renderTriangle(Scene scene, OutputStream os, String format);

    /**
     * Render a scene to a Stratsys PolyJet suitable image stack
     *
     * @param scene The scene to render
     */
    public void renderPolyJet(Scene scene, ParamMap params, String filePath);

    /**
     * Render a scene to a SVX suitable image stack
     *
     * @param scene The scene to render
     */
    public void renderSVX(Scene scene, OutputStream os);

    /**
     * Execute a ShapeJS script.  The script may perform any number of output commands.
     *
     * @param params
     */
/*  // TBD, not sure of param space yet
    public void exec();

 */

    /**
     * Cast a ray into the scene and get the position and normal at the picked position.
     *
     * @param camera
     * @param objTrans
     * @param pixX
     * @param pixY
     * @param width
     * @param height
     * @param pos
     * @param normal
     * @param quality
     */
    public void pick(Scene scene, Camera camera, Matrix4f objTrans, int pixX, int pixY, int width, int height,
                     Vector3f pos, Vector3f normal, float quality);

    public RenderStat getRenderStats();

    /**
     * Shutdown the executor.
     */
    public void shutdown();
}
