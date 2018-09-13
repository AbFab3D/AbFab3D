/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.datasources;

// External Imports


// external imports

import abfab3d.core.Bounds;
import abfab3d.core.Color;
import abfab3d.core.Vec;
import abfab3d.grid.op.ImageLoader;
import abfab3d.grid.op.ImageMaker;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;

/**
 * Tests the functionality of ImageColorMap
 */
public class TestImageColorMap extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestImageColorMap.class);
    }

    int gridMaxAttributeValue = 127;

    public void testBitmap() {

        printf("testBitmap()\n");
    }

    public void devTestColorImage() {

        double boxWidth = 20;
        double boxHeight = 20;
        double boxDepth = 20;

        //ImageColorMap image = new ImageColorMap("test/images/color_boxes.png", boxWidth,boxHeight,boxDepth);
        ImageColorMap image = new ImageColorMap("test/images/redcircle_20.png", boxWidth, boxHeight, boxDepth);
        image.initialize();

        int w = image.getBitmapWidth();
        int h = image.getBitmapHeight();

        int bitmap[] = new int[w * h];

        image.getBitmapDataInt(bitmap);

        printf("==========================[%d x %d]\n", w, h);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                printf("%8x ", bitmap[x + y * w]);
            }
            printf("\n");
        }
        printf("==========================\n");

        double dx = boxWidth / w;
        double dy = boxHeight / h;
        double dz = dx;

        double x0 = -boxWidth / 2;
        double y0 = -boxHeight / 2;
        double z0 = -boxDepth / 2;

        Vec pnt = new Vec(3);
        Vec value = new Vec(3);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double xx = (x + 0.5) * dx + x0;
                double yy = (y + 0.5) * dy + y0;
                double zz = (0.5) * dz + z0;
                pnt.set(xx, yy, zz);
                image.getDataValue(pnt, value);
                printf("%2d %2d %2d,", (int) (value.v[0] * 10), (int) (value.v[1] * 10), (int) (value.v[2] * 10));
            }
            printf("\n");
        }
        printf("==========================\n");

    }

    public void devTestBadImage() {


        double boxWidth = 20;
        double boxHeight = 20;
        double boxDepth = 20;
        String path = "test/images/image_datafile.jpg";

        ImageColorMap image = new ImageColorMap(path, boxWidth, boxHeight, boxDepth);

        image.initialize();
        int w = image.getBitmapWidth();
        int h = image.getBitmapHeight();
        printf("image %s: [%d x %d]\n", path, w, h);

    }


    //
    //
    //
    public void devTestImageColorMap() throws Exception {

        double sizeZ = 2 * MM;
        double sizeX = 100 * MM;
        double sizeY = 100 * MM;
        double margin = 5 * MM;
        double blur = 0. * MM;
        double distanceBand = 1 * MM;
        String path = "test/images/color_boxes.png";
        ImageLoader reader = new ImageLoader(path);
        reader.set("svgRasterizationWidth", 100);
        reader.set("backgroundColor", new Color(1, 1, 1, 0));

        ImageColorMap img = new ImageColorMap(reader, sizeX, sizeY, sizeZ);

        Bounds bounds = new Bounds(-sizeX / 2, sizeX / 2, -sizeY / 2, sizeY / 2, -sizeZ / 2, sizeZ / 2);
        bounds.expand(margin);

        int nux = 1000;
        int nvy = (int) (nux * bounds.getSizeY() / bounds.getSizeX());
        int nvz = (int) (nux * bounds.getSizeZ() / bounds.getSizeX());
        double ys = (bounds.ymin + bounds.ymax) / 2; // y coord of xz slice
        double zs = (bounds.ymin + bounds.ymax) / 2; // z coordinate of xy slice

        ImageMaker sm = new ImageMaker();
        BufferedImage image;
        img.initialize();
        image = sm.renderImage(nux, nvy, bounds, img);
        ImageIO.write(image, "png", new File("/tmp/00_imageColorMap_XY.png"));
    }

    public static void main(String[] args) throws Exception {
        new TestImageColorMap().devTestImageColorMap();
        //new TestImageColorMap().devTestBadImage();
    }
}