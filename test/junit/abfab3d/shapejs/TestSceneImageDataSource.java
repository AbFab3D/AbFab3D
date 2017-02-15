/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2016
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.shapejs;

// External Imports


import abfab3d.grid.op.ImageMaker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.UUID;

/**
 * Tests the functionality of the SceneImageDataSource
 */
public class TestSceneImageDataSource extends TestCase {
    public static final float[] backgroundColor = new float[]{1, 1, 1};

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSceneImageDataSource.class);
    }

    /**
     * Test basic usage
     */
    public void testBasic() throws IOException {
        int width = 64;
        int height = 64;

        URI uri = new File("test/scripts/gyrosphere_params.js").toURI();
        Script s = new Script(uri);

        ScriptManager sm = ScriptManager.getInstance();
        String jobID = UUID.randomUUID().toString();

        HashMap<String, Object> params = new HashMap<String, Object>();

        ScriptResources sr = sm.prepareScript(jobID, s, params);
        sm.executeScript(sr,params);
        assertTrue("Eval failed", sr.result.isSuccess());
        Scene scene = sr.result.getScene();

        ImageSetup setup = new ImageSetup(width, height, getView(), ImageSetup.IMAGE_JPEG, 0.5f, AntiAliasingType.NONE, false, 0f, 1);
        MatrixCamera camera = new MatrixCamera(getView());
        SceneImageDataSource sids = new SceneImageDataSource(scene,setup,camera);
        ImageMaker renderer = new ImageMaker();
        BufferedImage image = renderer.renderImage(width,height,scene.getBounds(),sids);

        assertFalse("Failed to render basic shape", ImageUtilTest.isConstantImage(image));
    }

    private Matrix4f getView() {
        return getView(-4);
    }

    private Matrix4f getView(double pos) {
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

}
