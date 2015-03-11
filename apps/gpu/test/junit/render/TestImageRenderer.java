/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package render;


import abfab3d.util.Units;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;
import shapejs.EvalResult;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Units.*;

/**
 * Tests the functionality of the ImageRenderer
 *
 * @author Alan Hudson
 */
public class TestImageRenderer extends TestCase {
    private static final boolean DEBUG = true;
    public static final String VERSION = VolumeRenderer.VERSION_OPCODE_V3_DIST;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestImageRenderer.class);
    }

    public void testTranslation() throws IOException {
        int width = 256;
        int height = 256;

        BufferedImage base = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage test = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        ImageRenderer render = new ImageRenderer();
        render.initCL(1, width, height);
        render.setVersion(VERSION);
        render.render(null, getFile("test/scripts/transform_base.js"), new HashMap<String, Object>(), getView(), false, 0.5f, base);
        render.render(null, getFile("test/scripts/translation.js"), new HashMap<String, Object>(), getView(), false, 0.5f, test);

        if (DEBUG) {
            try {
                ImageIO.write(base, "png", new File("/tmp/transform_base.png"));
                ImageIO.write(test, "png", new File("/tmp/translation_test.png"));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        assertFalse("Constant image", ImageUtilTest.isConstantImage(test));
        assertFalse("Same image", ImageUtilTest.isImageEqual(base, test));
    }

    public void testRotation() throws IOException {
        int width = 256;
        int height = 256;

        BufferedImage base = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage test = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        ImageRenderer render = new ImageRenderer();
        render.initCL(1, width, height);
        render.setVersion(VERSION);

        render.render(null, getFile("test/scripts/transform_base.js"), new HashMap<String, Object>(), getView(), false, 0.5f, base);
        render.render(null, getFile("test/scripts/rotation.js"), new HashMap<String, Object>(), getView(), false, 0.5f, test);

        if (DEBUG) {
            try {
                ImageIO.write(base, "png", new File("/tmp/transform_base.png"));
                ImageIO.write(test, "png", new File("/tmp/rotation_test.png"));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        assertFalse("Constant image", ImageUtilTest.isConstantImage(test));
        assertFalse("Same image", ImageUtilTest.isImageEqual(base, test));
    }

    /**
     * Test that changing script params works
     *
     * @throws IOException
     */
    public void testParams() throws IOException {
        int width = 256;
        int height = 256;

        BufferedImage base = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage test = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        ImageRenderer render = new ImageRenderer();
        render.initCL(1, width, height);
        render.setVersion(VERSION);

        HashMap<String,Object> params = new HashMap<String, Object>();
        params.put("period",18);
        params.put("thickness",2);
        render.render(null, getFile("test/scripts/gyrosphere_params.js"), params, getView(), false, 0.5f, base);
        params.put("period", 14);
        render.render(null, getFile("test/scripts/gyrosphere_params.js"), params, getView(), false, 0.5f, test);

        if (DEBUG) {
            try {
                ImageIO.write(base, "png", new File("/tmp/gyrosphere_18.png"));
                ImageIO.write(test, "png", new File("/tmp/gyrosphere_14.png"));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        assertFalse("Constant image", ImageUtilTest.isConstantImage(test));
        assertFalse("Same image", ImageUtilTest.isImageEqual(base, test));
    }

    /**
     * Test that caching improves speed by at least 50%
     *
     * @throws IOException
     */
    public void testCachingSpeed() throws Exception {
        int width = 256;
        int height = 256;

        BufferedImage base = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage test = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        ImageRenderer render = new ImageRenderer();
        render.initCL(1, width, height);
        render.setVersion(VERSION);

        String jobID = UUID.randomUUID().toString();
        HashMap<String,Object> params = new HashMap<String, Object>();
        params.put("period",18);
        params.put("thickness",2);

        // WARM up classes
        render.render(null, getFile("test/scripts/gyrosphere_params.js"), params, getView(), false, 0.5f, base);

        long t0 = System.nanoTime();
        render.render(jobID, getFile("test/scripts/gyrosphere_params.js"), new HashMap<String, Object>(), getView(), true, 0.5f, test);
        long t1 = System.nanoTime() - t0;

        t0 = System.nanoTime();
        render.renderCached(jobID, getView(), 0.5f, base);
        long t2 = System.nanoTime() - t0;

        if (DEBUG) {
            try {
                ImageIO.write(base, "png", new File("/tmp/gyrosphere_first.png"));
                ImageIO.write(test, "png", new File("/tmp/gyrosphere_second.png"));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        assertFalse("Constant image", ImageUtilTest.isConstantImage(test));
        assertTrue("Same image", ImageUtilTest.isImageEqual(base, test));

        printf("T1: %d ms  T2: %d ms\n", (int) (t1 / 1e6), (int) (t2 / 1e6));
        assertTrue("Speed", t1 > 2 * t2);
    }

    /**
     * Test that caching improves speed by at least 50%
     *
     * @throws IOException
     */
    public void testPicking() throws Exception {
        int width = 256;
        int height = 256;

        ImageRenderer render = new ImageRenderer();
        render.initCL(1, width, height);
        render.setVersion(VERSION);

        HashMap<String,Object> params = new HashMap<String, Object>();
        params.put("radius",10);

        Vector3f pos1 = new Vector3f();
        Vector3f normal1 = new Vector3f();

        render.pick(null, getFile("test/scripts/picking.js"), params, true,getView(), 128,128,width,height,pos1,normal1);
        printf("pos1: %s  normal: %s\n", pos1, normal1);

        Vector3f pos2 = new Vector3f();
        Vector3f normal2 = new Vector3f();
        params.put("radius",15);
        render.pick(null, getFile("test/scripts/picking.js"), params, true,getView(), 128,128,width,height,pos2,normal2);

        printf("pos2: %s  normal: %s\n",pos2,normal2);

        double EPS = 0.1f*MM;
        double delta = pos2.z - pos1.z;
        assertTrue("z changed",delta >= (5.0 * Units.MM - EPS));
        assertTrue("x same",Math.abs((pos2.x - pos1.x)) <= EPS);
        assertTrue("y same",Math.abs((pos2.y - pos1.y)) <= EPS);

        params.put("radius", 10);
        render.pick(null, getFile("test/scripts/picking.js"), params, true, getView(), 127, 128, width, height, pos2, normal2);
        assertTrue("x less",pos2.x < pos1.x);

        render.pick(null, getFile("test/scripts/picking.js"), params, true, getView(), 128, 130, width, height, pos2, normal2);
        assertTrue("y more",pos2.y > pos1.y);

        // check outside
        render.pick(null, getFile("test/scripts/picking.js"), params, true, getView(), 0, 0, width, height, pos2, normal2);
        printf("pos2: %s  normal: %s\n", pos2, normal2);

        assertTrue("outside", pos2.x <= 10000);
    }

    public void testSyntaxError() throws IOException {
        int width = 256;
        int height = 256;

        ImageRenderer render = new ImageRenderer();
        render.initCL(1, width, height);
        render.setVersion(VERSION);
        EvalResult result = render.setScene(null, getFile("test/scripts/syntax_error.js"), new HashMap<String, Object>());

        printf("Error log: %s\n",result.getErrorLog());
        assertTrue("Missing error log",result.getErrorLog() != null && result.getErrorLog().length() != 0);
    }

    private String getFile(String file) throws IOException {
        return FileUtils.readFileToString(new File(file));
    }
    
    private Matrix4f getView() {
        float[] DEFAULT_TRANS = new float[]{0, 0, -4};
        float z = DEFAULT_TRANS[2];
        float rotx = 0;
        float roty = 0;

        Vector3f trans = new Vector3f();
        Matrix4f tmat = new Matrix4f();
        Matrix4f rxmat = new Matrix4f();
        Matrix4f rymat = new Matrix4f();

        trans.z = z;
        tmat.set(trans, 1.0f);

        rxmat.rotX(rotx);
        rymat.rotY(roty);

        Matrix4f mat = new Matrix4f();
        mat.mul(tmat, rxmat);
        mat.mul(rymat);

        return mat;
    }

}

