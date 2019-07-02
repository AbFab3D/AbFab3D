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

package abfab3d.spatial;

import org.junit.Assert;
import org.junit.Test;

import javax.vecmath.Vector3d;

public class TestTriangle {

    @Test
    public void testGoodTriangle() {
        Vector3d v0 = new Vector3d(1,1,1);
        Vector3d v1 = new Vector3d(1,2,1);
        Vector3d v2 = new Vector3d(2,1,1);

        Triangle t = new Triangle(v0,v1,v2);

        Assert.assertFalse("Good triangle via Area",t.isDegenerateAreaMethod());

        Assert.assertFalse("Good triangle via Length",t.isDegenerateLengthMethod());
    }

}
