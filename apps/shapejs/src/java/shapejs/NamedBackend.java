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

/**
 * GPU based ShapeJS command backend.  Uses the Shapeways internal OpenCL implementation.  Make sure
 * the provided shapejsRT is placed in the lib directory.
 *
 * @author Alan Hudson
 */
public class NamedBackend implements CommandBackend {
    private CommandBackend impl;

    public NamedBackend(String className) {
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

    @Override
    public void renderTriangle(ParamContainer params) {
        impl.renderTriangle(params);
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
