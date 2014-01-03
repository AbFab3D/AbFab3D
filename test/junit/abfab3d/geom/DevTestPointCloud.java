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

package abfab3d.geom;

// External Imports
import javax.vecmath.Vector3d;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;


// Internal Imports
import abfab3d.util.TrianglePrinter;
import abfab3d.io.output.STLWriter;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Units.MM;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;


/**
 * Tests the functionality of PointCloud
 *
 * @author Vladimir Bulatov
 * @version
 */
public class DevTestPointCloud {


    /**
     * 
     */
    public static void makeRing() throws Exception {
	
        double ringRadius = 10*MM;
        double pointSize = 3*MM; 
        int pointCount = 100; 
        double spiral = 0.0003;
        int cycleCount = 10;
        Vector<Vector3d> points = new Vector<Vector3d>();
        for(int i = 0; i < cycleCount*pointCount; i++){

            double a = i * 2*Math.PI/100;
            
            double x = ringRadius * cos(a);
            double y = ringRadius * sin(a);
            double z = spiral*a;

            points.add(new Vector3d(x,y,z));
        }
        printf("points count: %d\n", points.size());
        PointCloud cloud = new PointCloud(points);
        cloud.setPointSize(pointSize);
        
        STLWriter stl = new STLWriter("/tmp/point_cloud.stl");
        cloud.getTriangles(stl);
        stl.close();              

    }


    public static void main(String[] arg) throws Exception {

        makeRing();

    }
}
