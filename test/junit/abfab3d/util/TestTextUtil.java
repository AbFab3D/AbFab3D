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
import java.awt.Insets;
import java.awt.image.BufferedImage;

import static java.lang.Math.abs;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

/**
 */
public class TestTextUtil extends TestCase {
    public static final boolean DEBUG = false;

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTextUtil.class);
    }

    public void testRenderText() throws Exception{
        BufferedImage img = TextUtil.createTextImage(1000, 100, "0123456789Ajlg", new Font("Times New Roman", Font.PLAIN, 10), new Insets(1,1,1,1), false, 0.2);
        if (DEBUG) ImageIO.write(img, "png", new File("/tmp/text.png"));
    } 

    public static void main(String arg[]) throws Exception {

        new TestTextUtil().testRenderText();
        
    }
    
}
