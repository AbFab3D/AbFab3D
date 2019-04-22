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

import javax.vecmath.Vector3d;

import abfab3d.param.BaseParameterizable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import abfab3d.core.Bounds;

import abfab3d.util.ColorMapperDistance;

import abfab3d.grid.op.SliceMaker;
import abfab3d.grid.op.ImageLoader;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;


/**
 * Tests the functionality of ImageMap
 *
 * @version
 */
public class TestImageMap extends TestCase {

    static final boolean DEBUG_VIZ = true;
    static final boolean DEBUG = false;

    
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestImageMap.class);
    }

    public void testVhash() {
        Text2D text = new Text2D("test");
        double size = 10*MM;
        ImageMap map = new ImageMap(text,size,size,size);

        String vhash = map.getDataLabel();
        if(DEBUG)printf("vhash: %s\n",vhash);

        assertFalse("has memory reference",vhash.contains("@"));

    }

    public void devTestImageMap() throws Exception{
        
        double sizeZ = 2*MM;
        double sizeX = 100*MM;
        double sizeY = 100*MM;
        double margin = 5*MM;
        double blur = 0.*MM;
        double distanceBand = 1*MM;
        //String path = "test/images/circular_gradient_20.png";
        //String path = "test/images/letter_S.svg";
        //String path = "test/images/letter_S_500.png";
        //String path = "test/images/snowflake.svg";
        String path = "test/images/square.svg";
        ImageLoader reader = new ImageLoader(path);
        reader.set("svgRasterizationWidth", 100);
        //ImageMap img = new ImageMap(reader, sizeX,sizeY,sizeZ);
        FormattedText2D text = new FormattedText2D("Hello<br/>World");
        text.set("width", 5*MM);
        text.set("height", 5*MM);
        ImageMap img = new ImageMap(text, sizeX,sizeY,sizeZ);

        img.set("whiteDisplacement", -1*MM);
        img.set("blackDisplacement", 1*MM);
        img.set("repeatX", false);
        img.set("repeatY", false);
        img.set("blurWidth", blur);

        Bounds bounds = new Bounds(-sizeX/2, sizeX/2, -sizeY/2, sizeY/2, -sizeZ/2,sizeZ/2);
        bounds.expand(margin);
                                
        int nux = 1000;
        int nvy = (int)(nux * bounds.getSizeY()/bounds.getSizeX());
        int nvz = (int)(nux * bounds.getSizeZ()/bounds.getSizeX());
        double ys = (bounds.ymin + bounds.ymax)/2; // y coord of xz slice
        double zs = (bounds.ymin + bounds.ymax)/2; // z coordinate of xy slice

        SliceMaker sm = new SliceMaker();        
        BufferedImage image;
        img.initialize();
        image = sm.renderSlice(nux, nvy, 
                               new Vector3d(bounds.xmin, bounds.ymin,zs),
                               new Vector3d(bounds.xmax, bounds.ymin,zs),
                               new Vector3d(bounds.xmin, bounds.ymax,zs), 
                               img, 0, new ColorMapperDistance(distanceBand));
        ImageIO.write(image, "png", new File("/tmp/00_imageMap_XY.png"));
   }


    public static void main(String[] args) throws Exception {
        new TestImageMap().devTestImageMap();
    }
}