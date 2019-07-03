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
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.UUID;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;

/**
 * CPU based ShapeJS command backend.  Uses the abfab3d level ShapeJS implementation
 *
 * @author Alan Hudson
 */
public class CPUBackend implements CommandBackend {
    public CPUBackend() {
        AbFab3DGlobals.put(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY, Runtime.getRuntime().availableProcessors());
    }

    @Override
    public void renderImage(ParamContainer params, OutputStream os) {
        try {
            String scriptPath = params.getScript();
            String baseDir = null;

            ScriptManager sm = ScriptManager.getInstance();

            String jobID = UUID.randomUUID().toString();
            HashMap<String, Object> sparams = new HashMap<String, Object>();

            String script = IOUtils.toString(new FileInputStream(scriptPath));

            ScriptResources sr = sm.prepareScript(jobID, baseDir, script, sparams, false);

            sm.executeScript(sr);

            Scene scene = (Scene) sr.evaluatedScript.getResult();

            ImageSetup setup = params.getImageSetup();
            Camera camera = params.getCamera();

            SceneImageDataSource sids = new SceneImageDataSource(scene, camera);
            sids.set("shadowsQuality", 10);
            sids.set("raytracingDepth",2);

            ImageMaker im = new ImageMaker();

            im.set("imgRenderer", sids);
            im.set("width", setup.getWidth());
            im.set("height", setup.getHeight());

            im.setBounds(new Bounds(-1, 1, -1, 1, -1, 1));

            BufferedImage image = im.getImage();

            ImageIO.write(image, "png", os);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ScriptManager.getInstance().shutdown();
        }
    }

    @Override
    public void renderImage(ParamContainer params, BufferedImage img) {

    }

    @Override
    public void renderTriangle(ParamContainer params) {

    }

    @Override
    public void renderPolyJet(ParamContainer params) {

    }

    @Override
    public void exec(ParamContainer params) {

    }
}
