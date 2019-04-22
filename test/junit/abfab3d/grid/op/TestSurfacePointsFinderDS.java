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

package abfab3d.grid.op;

import java.util.Random;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import abfab3d.core.Bounds;

import abfab3d.datasources.Sphere;

import abfab3d.util.PointSet;


import abfab3d.geom.PointCloud;

import static java.lang.Math.round;
import static java.lang.Math.ceil;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;
import static abfab3d.core.MathUtil.L2S;

/**
 * Test the SurfacePointsFinder class.
 *
 * @author Vladimir Bulatov
 */
public class TestSurfacePointsFinderDS extends TestCase {

    private static final boolean DEBUG = false;

    int subvoxelResolution = 100;


    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSurfacePointsFinderDS.class);
    }

    public void testNothing(){

    }


    public void devTestSphere(){
        
        printf("devTestSphere()\n");

        SurfacePointsFinderDS pf = new SurfacePointsFinderDS();
        pf.set("voxelSize", 0.1*MM);
        pf.set("blockSize", 20);
        int threads = 8;


        double r = 50*MM;
        Sphere sphere = new Sphere(r);
        
        double s = r + 1*MM;
        pf.set("threads",threads);
        long t0 = time();
        PointSet points = pf.findSurfacePoints(sphere, new Bounds(-s,s,-s,s,-s,s));
        Vector3d pnt = new Vector3d();
        printf("points count:%d\n",points.size());
        printf("threads:%d\n",threads);
        printf("time:%d ms\n",(time() - t0));

        double maxError = 0;
        for(int i = 0; i < points.size(); i++){
            points.getPoint(i,pnt);
            double len = pnt.length();
            //printf("(%5.2f %5.2f %5.2f)\n",pnt.x/MM,pnt.y/MM,pnt.z/MM);
            double err = abs(len - r);
            if(err > maxError) 
                maxError = err;
        }
        printf("maxError: %10.3e\n",maxError);
    }
    
    public static void main(String arg[]){

        new TestSurfacePointsFinderDS().devTestSphere();
        
    }

}
