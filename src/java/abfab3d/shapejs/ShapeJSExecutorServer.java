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

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.nanoTime;

/**
 * Uses a ShapeJS Server to execute ShapeJS commands
 *
 * @author Alan Hudson
 */
public class ShapeJSExecutorServer extends BaseShapeJSExecutor implements ShapeJSExecutor {
    private static final boolean DEBUG = false;

    public static final String PARAM_PROJECT_FILE = "projectFile";
    public static final String PARAM_VARIANT_NAME = "variantName";
    public static final String PARAM_VARIANT_JOB_ID = "jobID";

    private long lastRenderTime;
    private long lastImageEncodeTime;

    public ShapeJSExecutorServer() {
    }

    /**
     * Render a ShapeJS project into a raster image
     *
     * @param scene  The scene
     * @param camera The camera
     * @param setup  The image setup
     * @param os     The stream to write the output
     * @param format The image format to use.  Supports any formats ImageIO supports(jpeg,png,bmp,wbmp,gif).
     */
    public void renderImage(Scene scene, Camera camera, ImageSetup setup, OutputStream os, String format) {
        long stime = nanoTime();

        try {
        } catch (Exception e) {
            e.printStackTrace();
        }

        lastRenderTime = nanoTime() - stime;
    }

    /**
     * Render a ShapeJS project into a raster image
     *
     * @param scene  The scene
     * @param camera The camera
     * @param setup  The image setup
     * @param img    The stream to write the output
     */
    public void renderImage(Scene scene, Camera camera, ImageSetup setup, BufferedImage img) {
        long stime = nanoTime();

        try {
        } catch (Exception e) {
            e.printStackTrace();
        }

        lastRenderTime = nanoTime() - stime;
    }


    /**
     * Render a scene to a triangle based format
     *
     * @param scene The scene
     */
    public void renderTriangle(Scene scene, OutputStream os, String format) {
    }


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
                     Vector3f pos, Vector3f normal, float quality) {

    }

    /*
        @Override
        public void exec(ParamContainer params) {

        }
    */
    public RenderStat getRenderStats() {
        return new ServerTimeStat(lastRenderTime, lastImageEncodeTime);
    }


    class ServerTimeStat implements RenderStat {
        private long render;
        private long imageEncode;

        public ServerTimeStat(long render, long imageEncode) {
            this.render = render;
            this.imageEncode = imageEncode;
        }

        public long getRender() {
            return render;
        }

        public void setRender(long render) {
            this.render = render;
        }

        public long getImageEncode() {
            return imageEncode;
        }

        public void setImageEncode(long imageEncode) {
            this.imageEncode = imageEncode;
        }

        @Override
        public String toString(double timeUnit, String timeLabel, double sizeUnit, String sizeLabel) {
            String label = fmt("Render: %4d %s ImageEncode: %d %s", (int) (render * timeUnit), timeLabel, (int) (imageEncode * timeUnit), timeLabel);

            return label;
        }
    }
}
