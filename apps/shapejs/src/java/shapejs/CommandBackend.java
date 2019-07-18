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
 * Backend for executing commands.  Allows us to easily swap out ShapeJS implementations such as CPU verses GPU.
 * If your interested in licensing a GPU powered backend which is 10X faster than our CPU backend contact alan@shapeways.com
 *
 * @author Alan Hudson
 */
public interface CommandBackend {
    /**
     * Render a ShapeJS project into a raster image
     * @param params
     */
    public void renderImage(ParamContainer params, OutputStream os, String format);

    /**
     * Render a ShapeJS project into a buffered image
     * @param params
     */
    public void renderImage(ParamContainer params, BufferedImage img);

    /**
     * Render a scene to a triangle based format
     * @param params
     */
    public void renderTriangle(ParamContainer params);

    /**
     * Render a scene to a Stratsys PolyJet suitable image stack
     * @param params
     */
    public void renderPolyJet(ParamContainer params);

    /**
     * Execute a ShapeJS script.  The script may perform any number of output commands.
     * @param params
     */
    public void exec(ParamContainer params);
}
