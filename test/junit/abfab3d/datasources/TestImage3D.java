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

import abfab3d.core.AttributeGrid;
import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.Vec;
import abfab3d.grid.op.GridMaker;
import abfab3d.grid.op.ImageLoader;
import abfab3d.grid.op.SliceMaker;
import abfab3d.util.ColorMapperDistance;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3d;
import java.awt.image.BufferedImage;
import java.io.File;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;
import static abfab3d.datasources.DevTestUtil.makeDistanceGrid;
import static abfab3d.grid.util.GridUtil.writeSlice;

/**
 * Tests the functionality of GridMaker
 */
public class TestImage3D extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestImage3D.class);
    }

    public void testBitmap() {

        printf("testBitmap()\n");
    }

    public void testGrayImageDistance() {

        double sizeZ = 15;
        double sizeX = 20;
        double sizeY = 20;
        double margin = 5;

        //Image3D shape = new Image3D("test/images/circle_blur_20.png", sizeX,sizeY,sizeZ);
        Image3D shape = new Image3D("test/images/box_blur_30.png", sizeX, sizeY, sizeZ);

        shape.set("useGrayscale", true);
        shape.set("blurWidth", 0.);
        shape.set("imagePlace", Image3D.IMAGE_PLACE_TOP);
        shape.setDataType(DataSource.DATA_TYPE_DISTANCE);

        shape.initialize();

        Vec pnt = new Vec(3);
        pnt.setVoxelSize(0.1);
        Vec data = new Vec(3);
        double z0 = sizeZ / 2;
        double rayX[][] = new double[][]{
            {-11, 0, z0, 0}, // exterior
            {-9, 0, z0, 0},
            {-7, 0, z0, 0},
            {-5, 0, z0, 0},
            {-3, 0, z0, 0},
            {-1, 0, z0, 0},
            {1, 0, z0, 0},
            {1, 0, z0 + 1, 0},
            {1, 0, z0 + 2, 0},
            {3, 0, z0, 0},
            {5, 0, z0, 0},
            {7, 0, z0, 0},
            {9, 0, z0, 0},
            {11, 0, z0, 0},

        };
        double rayZ[][] = new double[][]{
            {0, 0, -3.0, 0},
            {0, 0, -2.0, 0},
            {0, 0, -1.0, 0},
            {0, 0, 0., 0},
            {0, 0, 1.0, 0},
            {0, 0, 2.0, 0},
            {0, 0, 3.0, 0},
        };

        double coord[][] = rayX;

        for (int i = 0; i < coord.length; i++) {
            double cc[] = coord[i];
            pnt.v[0] = cc[0];
            pnt.v[1] = cc[1];
            pnt.v[2] = cc[2];
            shape.getDataValue(pnt, data);
            printf("pnt: [%9.5f  %9.5f  %9.5f] data: %9.5f expect: %9.5f\n", cc[0], cc[1], cc[2], data.v[0], cc[3]);
            //assertTrue(fmt("data.v[0]==cc[3]: %18.15e != %18.15e",data.v[0], cc[3]),Math.abs(data.v[0] - cc[3]) < EPS);
        }


    }

    public void devTestGrayImage() throws Exception {

        double sizeZ = 2 * MM;
        double sizeX = 20 * MM;
        double sizeY = 20 * MM;
        double margin = 5 * MM;
        double distanceBand = 1 * MM;
        Image3D img = new Image3D("test/images/blackcircle_blur.png", sizeX, sizeY, sizeZ);
        img.set("useGrayscale", true);
        img.set("tilesX", 1);
        img.set("tilesY", 1);
        img.set("blurWidth", 0.1 * MM);
        img.set("baseThreshold", 0.);
        img.set("rounding", 0. * MM);
        img.set("baseThickness", 0.);
        //img.set("imageType", Image3D.IMAGE_TYPE_ENGRAVED);
        img.set("imageType", Image3D.IMAGE_TYPE_EMBOSSED);
        //img.set("imagePlace", Image3D.IMAGE_PLACE_BOTH);
        img.set("imagePlace", Image3D.IMAGE_PLACE_TOP);
        img.set("voxelSize", 1 * MM);
        img.set("pixelsPerVoxel", 1.);
        img.set("maxDist", 10 * MM);
        Bounds bounds = new Bounds(-sizeX / 2, sizeX / 2, -sizeY / 2, sizeY / 2, -sizeZ / 2, sizeZ / 2);
        bounds.expand(margin);


        int nux = 1000;
        int nvy = (int) (nux * bounds.getSizeY() / bounds.getSizeX());
        int nvz = (int) (nux * bounds.getSizeZ() / bounds.getSizeX());
        double ys = (bounds.ymin + bounds.ymax) / 2; // y coord of xz slice
        double zs = (bounds.ymin + bounds.ymax) / 2; // z coordoinate of xy slice

        SliceMaker sm = new SliceMaker();

        BufferedImage image;
        /*
        img.setVersion(0);
        img.initialize();
        image = sm.renderSlice(nux, nvz, 
                               new Vector3d(bounds.xmin, ys, bounds.zmin),
                               new Vector3d(bounds.xmax, ys, bounds.zmin),
                               new Vector3d(bounds.xmin, ys, bounds.zmax), 
                               img, 0, new ColorMapperDistance(distanceBand));
        ImageIO.write(image, "png", new File("/tmp/00_sliceXZ_v0.png"));
        */
        img.initialize();
        image = sm.renderSlice(nux, nvz,
            new Vector3d(bounds.xmin, ys, bounds.zmin),
            new Vector3d(bounds.xmax, ys, bounds.zmin),
            new Vector3d(bounds.xmin, ys, bounds.zmax),
            img, 0, new ColorMapperDistance(distanceBand));
        ImageIO.write(image, "png", new File("/tmp/00_sliceXZ_v1.png"));

        /*
        img.setVersion(0);
        img.initialize();
        image = sm.renderSlice(nux, nvy, 
                               new Vector3d(bounds.xmin, bounds.ymin,zs),
                               new Vector3d(bounds.xmax, bounds.ymin,zs),
                               new Vector3d(bounds.xmin, bounds.ymax,zs), 
                               img, 0, new ColorMapperDistance(distanceBand));
        ImageIO.write(image, "png", new File("/tmp/00_sliceXY_v0.png"));
        */
        img.initialize();
        image = sm.renderSlice(nux, nvy,
            new Vector3d(bounds.xmin, bounds.ymin, zs),
            new Vector3d(bounds.xmax, bounds.ymin, zs),
            new Vector3d(bounds.xmin, bounds.ymax, zs),
            img, 0, new ColorMapperDistance(distanceBand));
        ImageIO.write(image, "png", new File("/tmp/00_sliceXY_v1.png"));
    }

    public void devGrid2DProducer() throws Exception {

        double sizeZ = 2 * MM;
        double sizeX = 20 * MM;
        double sizeY = 20 * MM;
        double margin = 5 * MM;
        double distanceBand = 1 * MM;
        ImageLoader reader = new ImageLoader("test/images/blackcircle_blur.png");
        Image3D img = new Image3D(reader, sizeX, sizeY, sizeZ);
        img.set("useGrayscale", true);
        img.set("tilesX", 1);
        img.set("tilesY", 1);
        img.set("blurWidth", 0.1 * MM);
        img.set("baseThreshold", 0.);
        img.set("rounding", 0. * MM);
        img.set("baseThickness", 0.);
        //img.set("imageType", Image3D.IMAGE_TYPE_ENGRAVED);
        img.set("imageType", Image3D.IMAGE_TYPE_EMBOSSED);
        //img.set("imagePlace", Image3D.IMAGE_PLACE_BOTH);
        img.set("imagePlace", Image3D.IMAGE_PLACE_TOP);
        img.set("voxelSize", 0.5 * MM);
        //img.set("pixelsPerVoxel", 1.);
        img.set("maxDist", 10 * MM);

        Bounds bounds = new Bounds(-sizeX / 2, sizeX / 2, -sizeY / 2, sizeY / 2, -sizeZ / 2, sizeZ / 2);
        bounds.expand(margin);


        int nux = 1000;
        int nvy = (int) (nux * bounds.getSizeY() / bounds.getSizeX());
        int nvz = (int) (nux * bounds.getSizeZ() / bounds.getSizeX());
        double ys = (bounds.ymin + bounds.ymax) / 2; // y coord of xz slice
        double zs = (bounds.ymin + bounds.ymax) / 2; // z coordoinate of xy slice

        SliceMaker sm = new SliceMaker();

        BufferedImage image;

        img.initialize();
        image = sm.renderSlice(nux, nvz,
            new Vector3d(bounds.xmin, ys, bounds.zmin),
            new Vector3d(bounds.xmax, ys, bounds.zmin),
            new Vector3d(bounds.xmin, ys, bounds.zmax),
            img, 0, new ColorMapperDistance(distanceBand));
        ImageIO.write(image, "png", new File("/tmp/00_sliceXZ_v1.png"));

        img.initialize();
        image = sm.renderSlice(nux, nvy,
            new Vector3d(bounds.xmin, bounds.ymin, zs),
            new Vector3d(bounds.xmax, bounds.ymin, zs),
            new Vector3d(bounds.xmin, bounds.ymax, zs),
            img, 0, new ColorMapperDistance(distanceBand));
        ImageIO.write(image, "png", new File("/tmp/00_sliceXY_v1.png"));
    }


    public void devTestGrayImageDistanceSlice() throws Exception {

        //Image3D shape = new Image3D("test/images/circle_20.png", 20,20,20);
        double sizeZ = 10;
        double sizeX = 20;
        double sizeY = 20;
        double margin = 5;
        double distanceBand = 0.5;
        //Image3D shape = new Image3D("test/images/circle_blur_20.png", sizeX,sizeY,sizeZ);
        Image3D shape = new Image3D("test/images/circle_blur_30.png", sizeX, sizeY, sizeZ);
        //Image3D shape = new Image3D("test/images/box_blur_30.png", sizeX,sizeY,sizeZ);
        shape.set("useGrayscale", false);
        shape.set("tilesX", 3);
        shape.set("tilesY", 3);
        shape.set("blurWidth", 0.);
        shape.set("baseThreshold", 0.5);
        shape.set("rounding", 0.1);
        shape.set("baseThickness", 0.1);
        //shape.set("imagePlace", Image3D.IMAGE_PLACE_BOTH);
        //shape.set("imagePlace", Image3D.IMAGE_PLACE_BOTTOM);
        shape.set("imagePlace", Image3D.IMAGE_PLACE_TOP);
        shape.setDataType(DataSource.DATA_TYPE_DISTANCE);
        shape.initialize();
        Bounds bounds = new Bounds(-sizeX / 2, sizeX / 2, -sizeY / 2, sizeY / 2, -sizeZ / 2, sizeZ / 2);
        bounds.expand(margin);
        double voxelSize = 0.1;
        double maxDist = sizeZ + margin;
        AttributeGrid grid = makeDistanceGrid(bounds, voxelSize, maxDist);

        GridMaker gm = new GridMaker();
        gm.setSource(shape);
        gm.makeGrid(grid);

        //printGridSliceY(grid, grid.getHeight()/2);
        //printGridSliceValueY(grid, grid.getHeight()/2, grid.getDataChannel(), "%4.1f ");
        //printGridSliceValueZ(grid, grid.getDepth()/2, grid.getDataChannel(), "%4.1f ");

        int nux = 1500;
        int nvy = (int) (nux * bounds.getSizeY() / bounds.getSizeX());
        int nvz = (int) (nux * bounds.getSizeZ() / bounds.getSizeX());

        writeSlice(grid, grid.getDataChannel(),
            new ColorMapperDistance(distanceBand),
            new Vector3d(bounds.xmin, (bounds.ymin + bounds.ymax) / 2, bounds.zmin),
            new Vector3d(bounds.getSizeX() / nux, 0, 0),
            new Vector3d(0, 0, bounds.getSizeZ() / nvz),
            nux, nvz, "/tmp/00_sliceY.png");

        writeSlice(grid, grid.getDataChannel(),
            new ColorMapperDistance(distanceBand),
            new Vector3d(bounds.xmin, bounds.ymin, bounds.getCenterZ()),
            new Vector3d(bounds.getSizeX() / nux, 0, 0),
            new Vector3d(0, bounds.getSizeY() / nvy, 0),
            nux, nvy, "/tmp/01_sliceZ.png");


    }

    public void devTestBlackImage() {

        Image3D image = new Image3D("test/images/circle_20.png", 10, 10, 10);
        image.set("useGrayscale", false);
        image.set("blurWidth", 0.2);

        image.initialize();

    }

    public static void devTestLinearMapper() {

        LinearMapper mapper = new LinearMapper(-1000, 1000, -0.001, 0.001);

        double vmin = mapper.getVmin();
        double vmax = mapper.getVmax();

        printf("%vmin: %f vmax: %f\n", vmin, vmax);

        for (int i = -2000; i < 2000; i += 100) {
            long att = i;
            double v = mapper.map(att);
            int vi = (int) (255 * ((v - vmin) / (vmax - vmin))) & 0xFF;
            byte vb = (byte) vi;
            int vii = (vb) & 0xFF;
            double vv = vii * (vmax - vmin) / 255 + vmin;
            printf("%8x %5d -> v:%9.5f vi:%4d vb:%4d vii:%4d vv: %9.5f\n", i, att, v * 1000, vi, vb, vii, vv * 1000);
        }
    }

    static final double EPS = 1.e-8;

    public static void main(String[] args) throws Exception {
        //new TestImage3D().testGrayImageDistance();
        //new TestImage3D().devTestGrayImageDistanceSlice();
        //new TestImage3D().devTestGrayImage();
        new TestImage3D().devGrid2DProducer();
    }
}