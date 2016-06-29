/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.util;

// External Imports
import javax.imageio.ImageIO;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.awt.Font;
import java.awt.image.BufferedImage;

import static java.lang.Math.abs;

import static abfab3d.core.Output.printf;

/**
 */
public class TestTextUtil extends TestCase {
    public static final boolean DEBUG = true;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTextUtil.class);
    }

    public void testRenderText() throws Exception{

        String text = "0jlg";
        int width = 500;
        int height = 100;
        Font font = new Font("Times New Roman", Font.PLAIN, 10);
        Insets2 insets = new Insets2(1,1,1,1);
        double spacing = -0.1;
        boolean aspect = true;
        //int fitStyle = TextUtil.FIT_HORIZONTAL;
        //int fitStyle = TextUtil.FIT_VERTICAL;
        int fitStyle = TextUtil.FIT_BOTH;
        
        int valign[] = new int[]{TextUtil.ALIGN_TOP,TextUtil.ALIGN_CENTER,TextUtil.ALIGN_BOTTOM};
        int halign[] = new int[]{TextUtil.ALIGN_LEFT,TextUtil.ALIGN_CENTER,TextUtil.ALIGN_RIGHT};
        String haligns[] = new String[]{"left","center", "right"};
        String valigns[] = new String[]{"top","center", "bottom"};

        BufferedImage img;
        for(int m = 0; m < 3; m++){
            for(int k = 0;k < 3; k++){
                img = TextUtil.createTextImage(width, height, text, font, spacing, insets,  aspect, fitStyle, halign[k], valign[m]);
                if (DEBUG) ImageIO.write(img, "png", new File("/tmp/text_"+valigns[m] + "_" + haligns[k] + ".png"));
            }
        }
    } 

    public static void main(String arg[]) throws Exception {


        new TestTextUtil().testRenderText();
        
    }
    
}
