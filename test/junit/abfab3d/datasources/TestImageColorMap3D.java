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
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;


// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Vector3d;

// Internal Imports

import abfab3d.core.Bounds;
import abfab3d.core.Vec;
import abfab3d.core.Color;
import abfab3d.core.DataSource;

import abfab3d.transforms.Translation;
import abfab3d.transforms.Rotation;

import abfab3d.grid.op.ImageLoader;
import abfab3d.grid.op.ImageMaker;


import static abfab3d.util.ImageUtil.premult;
import static abfab3d.util.ImageUtil.overlay;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Units.TORADIANS;

/**
 * Tests the functionality of ImageColorMap3D
 *
 */
public class TestImageColorMap3D extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestImageColorMap3D.class);
    }

    void devTestPoints(){
        
        Color background = new Color(0.1,0.1,0.1);
        String type = "brighter";
        //String type = "any";

        OverlayExtractor ext = new OverlayExtractor(background);
        ext.set("type", type);

        ext.initialize();

        printf("background:(%5.3f %5.3f %5.3f)\n", background.getr(),background.getg(),background.getb());
        int N = 10;
        for(int i = 0; i <= N; i++){

            double x = (double)i/N;
            Vec cc = new Vec(0.8,0.7,x);
            Vec ce = new Vec(4);

            ext.getDataValue(cc, ce);
            printf("color:(%5.3f %5.3f %5.3f) -> (%5.2f %5.2f %5.2f; %5.2f)", cc.v[0],cc.v[1],cc.v[2],ce.v[0],ce.v[1],ce.v[2],ce.v[3]);

            double cb[] = new double[4];
            background.getValue(cb);
            premult(ce.v);

            overlay(cb, ce.v);

            printf(" ->(%5.3f %5.3f %5.3f)\n", cb[0], cb[1], cb[2]);
            
        }
    }

    /*
        double y = 0;
        double z = 0;
        int N = 10;
        for(int i = 0; i < N; i++){

            double x = i*0.1*MM;
            Vec pnt = new Vec(x,y,z);
            Vec data = new Vec(4);
            
            cm.getDataValue(pnt, data);
            printf("(%7.3f %7.3f %7.3f) -> (%7.3f %7.3f %7.3f %7.3f)\n", pnt.v[0]/MM,pnt.v[1]/MM,pnt.v[2]/MM,data.v[0],data.v[1],data.v[2],data.v[3] );
            
        }
    */

    void devTestSlices(){
        
        String pathTemplate = "/tmp/slices+alpha/slice%04d.png";

        int count = 275;
        int width = 1250;
        int height = 500;

        double vs = 0.09*MM;
        double sizeX = width*vs;
        double sizeY = height*vs;
        double sizeZ = count*vs;
        
        ImageColorMap3D cm = new ImageColorMap3D(pathTemplate, 0, count, sizeX, sizeY, sizeZ);
        //cm.set("repeatX", true);
        //cm.set("repeatY", true);
        //cm.set("repeatZ", true);
        
        for(int i = 0; i < 360; i++){

            cm.setTransform(new Rotation(0,1,0,i*TORADIANS));        
            cm.initialize();
            
            ImageMaker im = new ImageMaker();
            im.set("threadCount", 8);
            
            Bounds bounds = new Bounds(-sizeX/2,sizeX/2,-sizeY/2,sizeY/2,-sizeZ/2,sizeZ/2);
            BufferedImage image = im.renderImage(width, height, bounds, cm);        
            String outPath = fmt("/tmp/slices/slice%04d.png", i);
            try {
                ImageIO.write(image, "png", new File(outPath));
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        
    }


    public static void main(String[] args) {

        new TestImageColorMap3D().devTestSlices();

    }

}