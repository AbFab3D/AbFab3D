package abfab3d.shapejs;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static org.junit.Assert.fail;

/**
 * Test the CPU implementation of ShapeJS
 * 
 * @author Alan Hudson
 */
public class TestShapeJSExecutorCpu {
    private static final boolean DEBUG = true;

    @Test
    public void testRenderImageBufferedImage() {

        ShapeJSExecutor impl = new ShapeJSExecutorCpu();
        try {
            Scene scene = loadScript("test/scripts/gyrosphere_params.js", null, false);
            int w = 512;
            int h = 512;

            MatrixCamera camera = new MatrixCamera(getView());

            ImageSetup setup = new ImageSetup(w, h, getView(), ImageSetup.IMAGE_JPEG, 0.5f, AntiAliasingType.NONE, false, 0f, 1);

            BufferedImage image1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            impl.renderImage(scene, camera, setup, image1);

            if (DEBUG) {
                String out = "/tmp/renderImageBufferedImage1.png";
                File fout = new File(out);
                fout.delete();

                ImageIO.write(image1, "png", fout);
            }

            Assert.assertFalse("Image is constant",ImageUtilTest.isConstantImage(image1));

            // Change the params and make sure the results are different

            HashMap params = new HashMap();
            params.put("period",14);
            Scene scene2 = loadScript("test/scripts/gyrosphere_params.js", params, false);

            BufferedImage image2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            impl.renderImage(scene2, camera, setup, image2);

            if (DEBUG) {
                String out = "/tmp/renderImageBufferedImage2.png";
                File fout = new File(out);
                fout.delete();

                ImageIO.write(image2, "png", fout);
            }

            Assert.assertFalse("Image is constant",ImageUtilTest.isImageEqual(image1,image2));

        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("IOException: " + ioe.getMessage());
        }
    }

    @Test
    public void testRenderImage() {

        ShapeJSExecutor impl = new ShapeJSExecutorCpu();
        try {
            Scene scene = loadScript("test/scripts/gyrosphere_params.js", null, false);
            int w = 512;
            int h = 512;

            MatrixCamera camera = new MatrixCamera(getView());

            ImageSetup setup = new ImageSetup(w, h, getView(), ImageSetup.IMAGE_JPEG, 0.5f, AntiAliasingType.NONE, false, 0f, 1);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos);

            impl.renderImage(scene, camera, setup, bos,"png");
            bos.close();

            byte[] bytes = baos.toByteArray();

            InputStream bis = new BufferedInputStream(new ByteArrayInputStream(bytes));
            BufferedImage image = ImageIO.read(bis);

            if(DEBUG) {
                String out = "/tmp/renderImage.png";
                File fout = new File(out);
                fout.delete();
                ImageIO.write(image,"png",fout);
            }

            Assert.assertFalse("Image is constant",ImageUtilTest.isConstantImage(image));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("IOException: " + ioe.getMessage());
        }
    }

    @Test
    public void testSaveModel() {

        ShapeJSExecutor impl = new ShapeJSExecutorCpu();
        try {
            HashMap params = new HashMap();
            params.put("radius",10);
            Scene scene = loadScript("test/scripts/gyrosphere_params.js", params, false);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos);

            impl.renderTriangle(scene,bos,"stl");

            baos.close();
            byte[] bytes = baos.toByteArray();

            if (DEBUG) {
                String out = "/tmp/saveModel.stl";
                File fout = new File(out);
                fout.delete();

                FileUtils.writeByteArrayToFile(fout,bytes);
            }

            Assert.assertFalse("File is too small",bytes.length < 1000);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("IOException: " + ioe.getMessage());
        }
    }
    
    public void testRenderSpeed() {

        ShapeJSExecutor impl = new ShapeJSExecutorCpu();
        try {
            Scene scene = loadScript("test/scripts/gyrosphere_params.js", null, false);
            int w = 576;
            int h = 576;

            MatrixCamera camera = new MatrixCamera(getView());

            ImageSetup setup = new ImageSetup(w, h, getView(), ImageSetup.IMAGE_JPEG, 0.5f, AntiAliasingType.NONE, false, 0f, 1);

            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            int warmup = 50;
            int run = 50;

            printf("Running warmup\n");
            // warmup
            for (int i = 0; i < warmup; i++) {
                impl.renderImage(scene, camera, setup, image);
            }

            printf("Running timing\n");
            // run
            long tot = 0;
            long max = 0;
            long min = Long.MAX_VALUE;
            for (int i = 0; i < run; i++) {
                long stime = time();
                impl.renderImage(scene, camera, setup, image);
                long t = time() - stime;
                printf("time: %d\n", t);
                tot += t;
                if (t > max) max = t;
                if (t < min) min = t;
                //ImageIO.write(image, "png", new File("/tmp/renderedScene.png"));
            }

            printf("Avg time: %d ms  min: %d ms  max: %d ms\n", (int) (((double) tot) / run), min, max);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("IOException: " + ioe.getMessage());
        }


    }

    @Test
    public void testPicking() {
        ShapeJSExecutor impl = new ShapeJSExecutorCpu();
        try {

            Scene scene = loadScript("test/scripts/picking.js", null, false);
            int w = 512;
            int h = 512;

            MatrixCamera camera = new MatrixCamera(getView());

            Vector3f pos = new Vector3f();
            Vector3f normal = new Vector3f();

//            impl.pick(scene,camera,null,256,256,w,h,pos,normal,0.5f);
            impl.pick(scene, camera, null, 256, 252, w, h, pos, normal, 0.5f);

            printf("pos: %s  normal: %s\n", pos, normal);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("IOException: " + ioe.getMessage());
        }
    }

    static Matrix4f getView() {
        return getView(3);
    }

    static Matrix4f getView(double pos) {
        float[] DEFAULT_TRANS = new float[]{0, 0, (float) pos};
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

    private Scene loadScript(String scriptPath, Map<String, Object> params, boolean sandboxed) throws IOException {
        String baseDir = null;

        ScriptManager sm = ScriptManager.getInstance();

        String jobID = UUID.randomUUID().toString();

        String script = IOUtils.toString(new FileInputStream(scriptPath));

        ScriptResources sr = sm.prepareScript(jobID, baseDir, script, params, sandboxed);

        sm.executeScript(sr);

        Scene scene = (Scene) sr.evaluatedScript.getResult();

        return scene;
    }

}
