/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.mesh;

// External Imports
import javax.vecmath.Vector3d;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import abfab3d.geom.TriangulatedModels;

import static abfab3d.util.Units.MM; 
import static abfab3d.util.Output.printf; 
import static abfab3d.util.Output.fmt; 

import static java.lang.System.currentTimeMillis;


/**
 * Tests the functionality of MeshDistance
 *
 * @author Vladimir Bulatov
 * @version
 */
public class DevTestMeshDistance extends TestCase {
    

    public static void testSimpleMeshes() throws Exception {
        
        
        TriangulatedModels.Sphere source = new TriangulatedModels.Sphere(10*MM, new Vector3d(0,0,0), 6, 0.1*MM);
        TriangulatedModels.Sphere target = new TriangulatedModels.Sphere(11*MM, new Vector3d(0,0,0), 6, 0.1*MM);
        MeshDistance md = new MeshDistance();

        md.measure(source, target);
        
    }

    public static void main(String arg[]) throws Exception {
        testSimpleMeshes();
    }

       
}

