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


// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Vector3d;

// Internal Imports

import abfab3d.core.Vec;
import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.AttributeGrid;

import abfab3d.util.ColorMapperDistance;
import abfab3d.util.ColorMapper;

import abfab3d.grid.op.GridMaker;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;
import static abfab3d.grid.util.GridUtil.writeSlice;


/**
 * Tests the functionality of DistranceToSurface
 *
 * @version
 */
public class TestDistanceToSurface extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDistanceToSurface.class);
    }

    public void devTestSphere() {

        printf("devTestSphere()\n");

        double r = 1;
        double s = 2.1*r;
        double vs = 0.01;

        Sphere sphere = new Sphere(new Vector3d(0,0,0), r);        
        sphere.initialize();

        DistanceToSurface ds = new DistanceToSurface(sphere, new Bounds(-s,s,-s,s,-s,s));
        ds.set("voxelSize",vs);
        ds.initialize();
        Vec pnt = new Vec(3);
        Vec data = new Vec(3);
        Vec data1 = new Vec(3);
        
        Vec p0 = new Vec(0,0,0);
        double w = 2*r/Math.sqrt(3);
        Vec p1 = new Vec(w,w,w);
        
        int N = 10;
        for(int i = 0; i <= N; i++){
            double t = ((double)i)/N;
            Vec.lerp(p0,p1,t,pnt);
            sphere.getDataValue(pnt, data);            
            ds.getDataValue(pnt, data1);            
            printf("pnt: [%8.5f  %8.5f  %8.5f] data: %8.5f data1:  %8.5f\n", pnt.v[0],pnt.v[1],pnt.v[2],data.v[0], data1.v[0]);
        }
    }

    public static void main(String[] args) throws Exception {
        new TestDistanceToSurface().devTestSphere();
        
    }

}