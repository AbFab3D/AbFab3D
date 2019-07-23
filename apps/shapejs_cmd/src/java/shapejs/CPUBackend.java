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

package shapejs;

import abfab3d.core.Bounds;
import abfab3d.grid.op.ImageMaker;
import abfab3d.shapejs.*;
import abfab3d.util.AbFab3DGlobals;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4f;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static abfab3d.core.Output.printf;

/**
 * CPU based ShapeJS command backend.  Uses the abfab3d level ShapeJS implementation
 *
 * @author Alan Hudson
 */
public class CPUBackend extends BaseCommandBackend implements CommandBackend {
    private static final boolean DEBUG = true;

    private List<String> libDirs = new ArrayList<>();

    public CPUBackend() {
        AbFab3DGlobals.put(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY, Runtime.getRuntime().availableProcessors());
    }

    public void setLibDirs(List<String> libs) {
        libDirs.clear();
        libDirs.addAll(libs);
    }

    @Override
    public void renderImage(ParamContainer params, OutputStream os, String format) {
        try {
            Scene scene = loadContent(params);

            ImageSetup setup = params.getImageSetup();
            Camera camera = params.getCamera();

            if (DEBUG) {
                Matrix4f mat = new Matrix4f();
                camera.getViewMatrix(mat);
                printf("Mat:\n%s\n", mat);
                printf("Angle: %6.2f\n", camera.getCameraAngle());
            }

            SceneImageDataSource sids = new SceneImageDataSource(scene, camera);
            sids.set("shadowsQuality", 10);
            sids.set("raytracingDepth", 2);

            ImageMaker im = new ImageMaker();

            im.set("imgRenderer", sids);
            im.set("width", setup.getWidth());
            im.set("height", setup.getHeight());

            im.setBounds(new Bounds(-1, 1, -1, 1, -1, 1));
//            im.setBounds(new Bounds(-0.5, 0.5, -0.5, 0.5, -0.5, 0.5));

            BufferedImage image = im.getImage();

            ImageIO.write(image, format, os);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ScriptManager.getInstance().shutdown();
        }
    }

    @Override
    public void renderImage(ParamContainer params, BufferedImage img) {
        try {
            Scene scene = loadContent(params);

            ImageSetup setup = params.getImageSetup();
            Camera camera = params.getCamera();

            SceneImageDataSource sids = new SceneImageDataSource(scene, camera);
            sids.set("shadowsQuality", 10);
            sids.set("raytracingDepth", 2);

            ImageMaker im = new ImageMaker();

            im.set("imgRenderer", sids);
            im.set("width", setup.getWidth());
            im.set("height", setup.getHeight());

            im.setBounds(new Bounds(-1, 1, -1, 1, -1, 1));
            DataBufferInt db = (DataBufferInt)img.getRaster().getDataBuffer();
            int[] imageData = db.getData();

            im.renderImage(imageData);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ScriptManager.getInstance().shutdown();
        }
    }

    /**
     * Render a scene to a triangle based format
     *
     * @param params
     */
    public void renderTriangle(ParamContainer params, OutputStream os, String format) {
        try {
            Scene scene = loadContent(params);

            saveModel(scene, os, format);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ScriptManager.getInstance().shutdown();
        }
    }

    @Override
    public void renderPolyJet(ParamContainer params) {

    }

    @Override
    public void exec(ParamContainer params) {

    }
}
