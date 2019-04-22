/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2011
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.datasources;

// External Imports


// external imports

import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.Vec;
import abfab3d.grid.op.ImageMaker;
import abfab3d.util.ColorMapper;
import abfab3d.util.ColorMapperDistance;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;

// Internal Imports


/**
 * Tests the functionality of DataSourceGrid
 *
 * @version
 */
public class TestThinLayerDataSource extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestThinLayerDataSource.class);
    }

    public void testNothing() {
    }


    void devTestSphere() throws Exception {

        printf("devTestSphere()\n");
        int imgWidth = 1000;
        int imgHeight = 1000;
        int imgWidthExt = 1000;
        int imgHeightExt = 1000;
        double s = 6 * MM;

        double r = 1.5 * MM;
        double R = 50 * MM;
        Bounds bounds = new Bounds(-s, s, -s, s, -s, s);
        Sphere sphere1 = new Sphere(-R, 0, 0, R);
        Sphere sphere2 = new Sphere(r, 0, 0, r);
        Sphere sphere3 = new Sphere(-R, 0, 0, -(R - 0.5 * MM));

        Union union = new Union(sphere1, sphere2);

        Intersection inter = new Intersection(union, sphere3);
        union.set("blend", 0.3 * MM);
        //DataSource source = union;
        DataSource source = inter;
        //DataSource source = sphere1;

        ThinLayerDataSource thinLayer = new ThinLayerDataSource(source, bounds);
        thinLayer.set("hiVoxelSize", 0.05 * MM);
        thinLayer.set("layerThickness", 0.6 * MM);
        thinLayer.set("lowVoxelFactor", 5);
        thinLayer.setUseCombined(false);

        long t0 = time();

        thinLayer.initialize();
        printf("thin layer initialized: %d ms\n", time() - t0);

        Vec pnt = new Vec(3);
        Vec data1 = new Vec(1);
        Vec data2 = new Vec(1);
        int n = 20;

        if (false) {
            for (int i = 0; i < n; i++) {
                pnt.v[0] = -s + i * MM;
                pnt.v[1] = 1 * MM;
                pnt.v[2] = 1 * MM;
                source.getDataValue(pnt, data1);
                thinLayer.getBaseValue(pnt, data2);
                printf("pnt: %8.5f %8.5f %8.5f -> %8.5f : %8.5f \n", pnt.v[0], pnt.v[1], pnt.v[2], data1.v[0], data2.v[0]);
            }
        }

        ImageMaker im = new ImageMaker();
        //im.setThreadCount(1);

        ColorMapper colorMapper = new ColorMapperDistance(0.1 * MM);
        ColorMapper colorMapperHR = new ColorMapperDistance(0.1 * MM);

        t0 = time();
        BufferedImage img1 = im.renderImage(imgWidth, imgHeight, bounds, new SliceDistanceColorizer(source, colorMapper));
        printf("img1 [%d x %d ] rendered: %d ms\n", img1.getWidth(), img1.getHeight(), time() - t0);

        ImageIO.write(img1, "png", new File("/tmp/00_sphere_dist.png"));

        /*
        thinLayer.setDataType(0);

        t0 = time();
        BufferedImage img2 = im.renderImage(imgWidth, imgHeight, bounds, new SliceDistanceColorizer(thinLayer, colorMapper));
        printf("img2 [%d x %d ] rendered: %d ms\n", img2.getWidth(), img2.getHeight(), time() - t0);
        ImageIO.write(img2, "png", new File("/tmp/01_lowGrid.png"));

        thinLayer.setDataType(1);

        t0 = time();
        BufferedImage img3 = im.renderImage(imgWidth, imgHeight, bounds, new SliceDistanceColorizer(thinLayer, colorMapperHR));
        printf("img3 [%d x %d ] rendered: %d ms\n", img3.getWidth(), img3.getHeight(), time() - t0);
        ImageIO.write(img3, "png", new File("/tmp/02_hiGrid.png"));

        thinLayer.setDataType(2);
        t0 = time();
        BufferedImage img4 = im.renderImage(imgWidthExt, imgHeightExt, bounds, new SliceDistanceColorizer(thinLayer, colorMapper));
        printf("img4 [%d x %d ] rendered: %d ms\n", img4.getWidth(), img4.getHeight(), time() - t0);
        t0 = time();
        ImageIO.write(img4, "png", new File("/tmp/03_fullGrid.png"));
        printf("image written: %d ms\n", time() - t0);
*/
    }

    public static void main(String[] args) throws Exception {
        new TestThinLayerDataSource().devTestSphere();
    }
}