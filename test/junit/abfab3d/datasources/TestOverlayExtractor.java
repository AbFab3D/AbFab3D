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

/**
 * Tests the functionality of Union
 *
 */
public class TestOverlayExtractor extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestOverlayExtractor.class);
    }

    void devTestPoints(){
        
        //ext.set("type", "darker");
        //Color background = new Color(1,1,1);
        //Color background = new Color(0,0,0);
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

    void devTestImage(){
        
        int count = 1;
        Color background = new Color(1,1,1);

        for(int i = 0; i < count; i++){
            String path = fmt("/tmp/slice/RGB with LUT baked%04d.png", i);
            ImageLoader loader = new ImageLoader(path);
            double width = loader.getWidth();
            double height = loader.getHeight();
            printf("image loaded: %s [%5.0f x %5.0f]\n", path, width, height);
            ImageColorMap map = new ImageColorMap(loader, width, height, 2);
            OverlayExtractor extractor = new OverlayExtractor(background);
            extractor.set("type","any");
            CompositeSource cmap = new CompositeSource(map, extractor);
            
            cmap.initialize();

            Vec pnt = new Vec(0.,0.,0.);
            Vec out = new Vec(0.,0.,0.,0.);
            
            ImageMaker im = new ImageMaker();
            im.set("threadCount", 8);

            map.getDataValue(pnt, out);
            printf("(%7.1f, %7.1f) -> (%7.1f, %7.1f, %7.1f )\n", pnt.v[0],pnt.v[1], out.v[0],out.v[1],out.v[2],out.v[3]);

            Bounds bounds = new Bounds(-width/2,width/2,-height/2,height/2,-1,1);
            BufferedImage image = im.renderImage((int)width, (int)height, bounds, cmap);

            String outPath = fmt("/tmp/slice_out/slice%04d.png", i);
            try {
                ImageIO.write(image, "png", new File(outPath));
            } catch(Exception e){
                e.printStackTrace();
            }
            
        }
        
        
    }


    public static void main(String[] args) {

        //new TestOverlayExtractor().devTestPoints();
        new TestOverlayExtractor().devTestImage();

    }

}