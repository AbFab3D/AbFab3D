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


import abfab3d.param.BaseParameterizable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;

// external imports
// Internal Imports

/**
 * Tests the functionality of Text2D
 *
 * @version
 */
public class TestImageMap extends TestCase {

    
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

        String vhash = BaseParameterizable.getParamString("test", map);
        printf("vhash: %s\n",vhash);

        assertFalse("has memory reference",vhash.contains("@"));

    }
}