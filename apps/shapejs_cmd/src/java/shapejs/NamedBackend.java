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

import java.awt.image.BufferedImage;
import java.io.OutputStream;

import static abfab3d.core.Output.printf;

/**
 * GPU based ShapeJS command backend.  Uses the Shapeways internal OpenCL implementation.  Make sure
 * the provided shapejsRT is placed in the lib directory.
 *
 * @author Alan Hudson
 */
public class NamedBackend implements CommandBackend {
    private CommandBackend impl;

    public NamedBackend(String className) {
        /*  // Debug code to help with DLL loading problems
        String wd = System.getProperty("user.dir");
        String libPath = wd + "\\natives\\Windows 10\\amd64\\gluegen-rt.dll";
        printf("Loading gluegen: %s\n",libPath);
        System.load(libPath);

        libPath = wd + "\\natives\\Windows 10\\amd64\\jocl.dll";
        printf("Loading jocl: %s\n",libPath);
        System.load(libPath);
        */
        try {
            Class backend = Class.forName(className);
            impl = (CommandBackend) backend.newInstance();
        } catch(Exception cnfe) {
            cnfe.printStackTrace();
        }
    }

    @Override
    public void renderImage(ParamContainer params, OutputStream os, String format) {
        impl.renderImage(params,os, format);
    }

    @Override
    public void renderImage(ParamContainer params, BufferedImage img) {
        impl.renderImage(params,img);
    }

    /**
     * Render a scene to a triangle based format
     * @param params
     */
    public void renderTriangle(ParamContainer params, OutputStream os, String format) {
        impl.renderTriangle(params, os, format);
    }

    @Override
    public void renderPolyJet(ParamContainer params) {
        impl.renderPolyJet(params);
    }

    @Override
    public void exec(ParamContainer params) {
        impl.exec(params);
    }
}
