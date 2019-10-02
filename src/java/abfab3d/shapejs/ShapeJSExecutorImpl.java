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

import static abfab3d.core.Output.printf;

/**
 * GPU based ShapeJS implementation.  Uses the Shapeways internal OpenCL implementation.  Make sure
 * the provided shapejsRT is placed in the lib directory.
 *
 * @author Alan Hudson
 */
public class ShapeJSExecutorImpl implements ShapeJSExecutor {
    private static final boolean DEBUG = false;
    private ShapeJSExecutor impl;

    public ShapeJSExecutorImpl(String className) {
        if (DEBUG) {
            String wd = System.getProperty("user.dir");
            String libPath = wd + "\\natives\\Windows 10\\amd64\\gluegen-rt.dll";
            printf("Loading gluegen: %s\n", libPath);
            System.load(libPath);

            libPath = wd + "\\natives\\Windows 10\\amd64\\jocl.dll";
            printf("Loading jocl: %s\n", libPath);
            System.load(libPath);
        }

        try {
            Class implClass = Class.forName(className);
            impl = (ShapeJSExecutor) implClass.newInstance();
        } catch(Exception cnfe) {
            printf("Problem loading ShapeJSExecutorImpl: %s\n",className);
            cnfe.printStackTrace();
        }
    }

    public static boolean exists(String className) {
        try {
            Class backend = Class.forName(className);
            Object impl = backend.newInstance();

            return true;
        } catch(Exception cnfe) {
            //cnfe.printStackTrace();
        }
        return false;
    }

    public ShapeJSExecutor getImpl() {
        return impl;
    }

    /**
     * Configure the backend.  Can be called at anytime to change values.
     * @param params
     */
    public void configure(Map params) {
        impl.configure(params);
    }

    @Override
    public void renderImage(Scene scene, Camera camera, ImageSetup setup, OutputStream os, String format) {
        impl.renderImage(scene,camera,setup,os,format);
    }

    @Override
    public void renderImage(Scene scene, Camera camera, ImageSetup setup, BufferedImage img) {
        impl.renderImage(scene,camera,setup,img);
    }

    @Override
    public void renderTriangle(Scene scene, OutputStream os, String format) {
        impl.renderTriangle(scene,os,format);
    }

    @Override
    public void renderPolyJet(Scene scene, ParamMap params, String filePath) {
        impl.renderPolyJet(scene,params,filePath);
    }

    @Override
    public void renderSVX(Scene scene, OutputStream os) {
        impl.renderSVX(scene,os);
    }

    /*
    @Override
    public void exec(ParamContainer params) {
        impl.exec(params);
    }
*/

    @Override
    public void pick(Scene scene, Camera camera, Matrix4f objTrans, int pixX, int pixY, int width, int height, Vector3f pos, Vector3f normal, float quality) {
        impl.pick(scene,camera,objTrans,pixX,pixY,width,height,pos,normal,quality);
    }

    @Override
    public RenderStat getRenderStats() {
        return impl.getRenderStats();
    }

    @Override
    public void shutdown() {
        impl.shutdown();
    }
}
