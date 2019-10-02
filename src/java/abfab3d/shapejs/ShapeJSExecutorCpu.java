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

import abfab3d.core.Bounds;
import abfab3d.datasources.Union;
import abfab3d.grid.op.ImageMaker;
import abfab3d.intersect.DataSourceIntersector;
import abfab3d.param.Parameterizable;
import abfab3d.param.Shape;
import abfab3d.util.AbFab3DGlobals;

import javax.imageio.ImageIO;
import javax.vecmath.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.OutputStream;
import java.util.List;

import static abfab3d.core.Output.*;
import static abfab3d.core.Units.MM;

/**
 * CPU based ShapeJS command backend.  Uses the abfab3d level ShapeJS implementation
 *
 * @author Alan Hudson
 */
public class ShapeJSExecutorCpu extends BaseShapeJSExecutor implements ShapeJSExecutor {
    private static final boolean DEBUG = false;
    private long lastRenderTime;
    private long lastImageEncodeTime;

    public ShapeJSExecutorCpu() {
        AbFab3DGlobals.put(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY, Runtime.getRuntime().availableProcessors());
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
            if (DEBUG) {
                Matrix4f mat = new Matrix4f();
                camera.getViewMatrix(mat);
                printf("Mat:\n%s\n", mat);
                printf("Angle: %6.2f\n", camera.getCameraAngle());
            }

            SceneImageDataSource sids = new SceneImageDataSource(scene, camera);
            sids.set("shadowsQuality", (int) Math.round(10 * setup.shadowQuality));
            sids.set("raytracingDepth", setup.maxRayBounces);

            ImageMaker im = new ImageMaker();

            im.set("imgRenderer", sids);
            im.set("width", setup.getWidth());
            im.set("height", setup.getHeight());

            im.setBounds(new Bounds(-1, 1, -1, 1, -1, 1));

            BufferedImage image = im.getImage();
            lastRenderTime = nanoTime() - stime;
            stime = nanoTime();

            // TODO: We should use the faster jpeg encoder here?
            ImageIO.write(image, format, os);
            lastImageEncodeTime = nanoTime() - stime;
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            if (DEBUG) {
                Matrix4f mat = new Matrix4f();
                camera.getViewMatrix(mat);
                printf("Mat:\n%s\n", mat);
                printf("Angle: %6.2f\n", camera.getCameraAngle());
            }

            SceneImageDataSource sids = new SceneImageDataSource(scene, camera);
            sids.set("shadowsQuality", (int) Math.round(10 * setup.shadowQuality));
            sids.set("raytracingDepth", setup.maxRayBounces);

            ImageMaker im = new ImageMaker();

            im.set("imgRenderer", sids);
            im.set("width", setup.getWidth());
            im.set("height", setup.getHeight());

            im.setBounds(new Bounds(-1, 1, -1, 1, -1, 1));

            DataBufferInt db = (DataBufferInt) img.getRaster().getDataBuffer();
            int[] imageData = db.getData();

            im.renderImage(imageData);
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
        try {
            saveModel(scene, os, format);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        DataSourceIntersector dsi = new DataSourceIntersector();
        double vs = 0.5 * MM;
        double maxDist = 4 + scene.getBounds().getSizeMax();
        dsi.set("voxelSize", vs);
        dsi.set("maxDistance", maxDist);
        dsi.set("maxStep", 5 * MM);
        dsi.set("maxStepsCount", maxDist / vs);

        Vector3d start = new Vector3d();
        Vector3d dir = new Vector3d();
        Vector2d uv = new Vector2d();
        Matrix4f invvm = new Matrix4f();
        camera.getViewMatrix(invvm);

        getUV(pixX, pixY, width, height, uv);
        getEyeOrigin(invvm, start);
        double cameraDepth = 1.0 / Math.tan(camera.getCameraAngle());
        getEyeDirection(invvm, cameraDepth, uv, dir);

        // Use the union of all sources
        List<Parameterizable> sources = scene.getSource();
        Union result = new Union();
        for (Parameterizable p : sources) {
            Shape shape = (Shape) p;
            result.add(shape.getSource());
        }

        result.initialize();

        DataSourceIntersector.Result res = dsi.getShapeRayIntersection(result, start, dir);

        Vector3d loc = res.getLocation();
        Vector3d n = res.getContact();

        pos.x = (float) loc.x;
        pos.y = (float) loc.y;
        pos.z = (float) loc.z;

        normal.x = (float) n.x;
        normal.y = (float) n.y;
        normal.z = (float) n.z;
    }
/*
    @Override
    public void exec(ParamContainer params) {

    }
*/
    public RenderStat getRenderStats() {
        return new CPUTimeStat(lastRenderTime, lastImageEncodeTime);
    }


    private void getUV(int x, int y, int imageW, int imageH, Vector2d vec) {

        vec.x = x * 2. / imageW - 1.;
        vec.y = y * 2. / imageW - (float) imageH / imageW;
    }

    private void getEyeOrigin(Matrix4f invvm, Vector3d eye) {
        // translational part of view
        eye.x = invvm.m03;
        eye.y = invvm.m13;
        eye.z = invvm.m23;
    }

    //
    // return view direction is playbox units
    //
    private void getEyeDirection(Matrix4f invvm, double cameraDepth, Vector2d uv, Vector3d dir) {
        Vector4d vec = new Vector4d();
        vec.x = uv.x;
        vec.y = uv.y;
        vec.z = -cameraDepth;

        vec.normalize();

        Vector4d dest = new Vector4d();
        mulMatVec4(invvm, vec, dest);

        dir.x = dest.x;
        dir.y = dest.y;
        dir.z = dest.z;
    }

    private void mulMatVec4(Matrix4f mat, Vector4d vec, Vector4d dest) {
        dest.x = mat.m00;
        dest.y = mat.m01;
        dest.z = mat.m02;
        dest.w = mat.m03;
        double f0 = vec.dot(dest);

        dest.x = mat.m10;
        dest.y = mat.m11;
        dest.z = mat.m12;
        dest.w = mat.m13;
        double f1 = vec.dot(dest);

        dest.x = mat.m20;
        dest.y = mat.m21;
        dest.z = mat.m22;
        dest.w = mat.m23;
        double f2 = vec.dot(dest);

        dest.x = f0;
        dest.y = f1;
        dest.z = f2;
        dest.w = 0;
    }

    class CPUTimeStat implements RenderStat {
        private long render;
        private long imageEncode;

        public CPUTimeStat(long render, long imageEncode) {
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
