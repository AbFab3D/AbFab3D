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

import abfab3d.core.*;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.GridIntIntervals;
import abfab3d.grid.GridShortIntervals;
import abfab3d.grid.op.GridMaker;
import abfab3d.grid.op.ImageMaker;
import abfab3d.io.output.GridSaver;
import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.STLWriter;
import abfab3d.param.Parameterizable;
import abfab3d.param.Shape;
import abfab3d.shapejs.*;
import abfab3d.util.AbFab3DGlobals;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

import static abfab3d.core.MaterialType.COLOR_MATERIAL;
import static abfab3d.core.Output.*;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;

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

            SceneImageDataSource sids = new SceneImageDataSource(scene, camera);
            sids.set("shadowsQuality", 10);
            sids.set("raytracingDepth",2);

            ImageMaker im = new ImageMaker();

            im.set("imgRenderer", sids);
            im.set("width", setup.getWidth());
            im.set("height", setup.getHeight());

            im.setBounds(new Bounds(-1, 1, -1, 1, -1, 1));

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
    }

    @Override
    public void renderTriangle(ParamContainer params) {
        try {
            Scene scene = loadContent(params);

            File file = new File(params.getOutput());

            saveModel(scene, file);
        } catch(Exception e) {
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
