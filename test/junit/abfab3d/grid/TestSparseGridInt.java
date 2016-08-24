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

package abfab3d.grid;

// External Imports

import abfab3d.core.AttributeGrid;
import abfab3d.core.Grid;
import abfab3d.core.Bounds;
import abfab3d.core.Vec;

import abfab3d.datasources.Sphere;


import junit.framework.Test;
import junit.framework.TestSuite;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;

// Internal Imports

/**
 * Tests the functionality of a SparceGridInt
 *
 * @author Vladimir Bulatov
 */
public class TestSparseGridInt extends BaseTestAttributeGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestArrayAttributeGridInt.class);
    }

    public void testNothing() {
    }

    public void devTestAttribute(){

        int blockOrder = 4;
        double s = 1000.;
        double vs = 1.;
        SparseGridInt grid = new SparseGridInt(new Bounds(-s, s, -s, s, -s, s), blockOrder, vs);
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();

        printf("grid: [%d x %d x %d\n", nx, ny, nz);
        long t0 = time();
        int w = Math.min(Math.min(nx, ny),nz);
        int w1 = w-1;
        long att = 1000;
        int nt = 1000000;
        for(int i = 0; i < nt; i++){
            double t = Math.PI*2*i/nt;
            int x = (int)(nx * (0.9*Math.cos(t)+1)/2);
            int y = (int)(ny * (0.9*Math.sin(t)+1)/2);
            int z = (int)(nz * (0.9*Math.sin(t)+1)/2);            
            grid.setAttribute(x,y,z, att);
        }
        for(int i = 0; i < nt; i++){
            double t = Math.PI*2*i/nt;
            int x = (int)(nx * (0.9*Math.cos(t)+1)/2);
            int y = (int)(ny * (0.9*Math.sin(t)+1)/2);
            int z = (int)(nz * (0.9*Math.sin(t)+1)/2);            
            long a = grid.getAttribute(x,y,z);
            assertEquals("wrong attribute returned", a, att);
        }        
        printf("memory used: %d ints\n", grid.getDataSize());
        printf("points count: %d\n", nt);
        printf("time: %d ms\n", time() - t0);
        
    }

    public void devTestSphere(){

        int blockOrder = 3;
        double s = 1000.;
        double vs = 1;
        SparseGridInt grid = new SparseGridInt(new Bounds(-s, s, -s, s, -s, s), blockOrder, vs);
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();

        printf("grid: [%d x %d x %d]\n", nx, ny, nz);
        long t0 = time();
        int w = Math.min(Math.min(nx, ny),nz);
        int w1 = w-1;
        long att = 1000;
        int nu = 1000;
        int nv = 1000;
        for(int rr = 0; rr < 10; rr++){
            double R = 0.85 + 0.01*rr;
            for(int u = 0; u < nu; u++){
                for(int v = 0; v < nv; v++){
                    double uu = Math.PI*2*u/nu;
                    double vv = Math.PI*v/nu;
                    int x = (int)(nx * (R*Math.cos(uu)*Math.sin(vv)+1)/2);
                    int y = (int)(ny * (R*Math.sin(uu)*Math.sin(vv)+1)/2);
                    int z = (int)(nz * (R*Math.cos(vv)+1)/2);            
                    grid.setAttribute(x,y,z, att);
                }
            }
        }
        printf("memory used: %8.2f MB\n", 4*grid.getDataSize()*0.000001);
        printf("points count: %d x %d\n", nu, nv);
        printf("writing time: %d ms\n", time() - t0);
        t0 = time();
        for(int rr = 0; rr < 10; rr++){
            double R = 0.85 + 0.01*rr;
            for(int u = 0; u < nu; u++){
                for(int v = 0; v < nv; v++){
                    double uu = Math.PI*2*u/nu;
                    double vv = Math.PI*v/nu;
                    int x = (int)(nx * (R*Math.cos(uu)*Math.sin(vv)+1)/2);
                    int y = (int)(ny * (R*Math.sin(uu)*Math.sin(vv)+1)/2);
                    int z = (int)(nz * (R*Math.cos(vv)+1)/2);            
                    long a = grid.getAttribute(x,y,z);
                }
            }
        }
        printf("reading time: %d ms\n", time() - t0);
        

    }


    public void devTestThinLayer(){

        printf("devTestThinLayer()\n");
        int blockOrder = 4;
        double s = 50*MM;
        double vs = 0.1*MM;
        double maxDist = 10*vs;
        printf("layerThickness: %4.1f vs\n", 2*maxDist/vs);

        Bounds bounds = new Bounds(-s, s, -s, s, -s, s);
        SparseGridInt grid = new SparseGridInt(bounds, blockOrder, vs);
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);
        Vec pnt = new Vec(3);
        Vec data = new Vec(4);
        Sphere sphere = new Sphere(s);
        long att = 1;
        long t0 = time();
        for(int y =0; y < ny; y++){
            for(int x =0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    double xx = bounds.xmin + x*vs;
                    double yy = bounds.ymin + y*vs;
                    double zz = bounds.zmin + z*vs;
                    pnt.v[0] = 
                    pnt.v[1] = yy;
                    pnt.v[2] = zz;
                    sphere.getDataValue(pnt, data);
                    if(Math.abs(data.v[0])< maxDist){
                        grid.setAttribute(x,y,z,att);
                    }
                }
            }
        }
        printf("memory used: %8.2f MB\n", grid.getDataSize()/1.e6);
        printf("writing time: %d ms\n", time() - t0);

    }
    
    public void devTestFullGrid(){

        printf("devFullGrid()\n");
        int blockOrder = 3;
        double s = 30*MM;
        double vs = 1*MM;
        Bounds bounds = new Bounds(-s, s, -s, s, -s, s);
        SparseGridInt grid = new SparseGridInt(bounds, blockOrder, vs);
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);
        printf("writing grid\n");
        for(int y =0; y < ny; y++){
            for(int x =0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    long origAtt = (x + (y << blockOrder)  + (z << (blockOrder *2)));
                    grid.setAttribute(x,y,z,origAtt); 
                    //long att = grid.getAttribute(x,y,z);                    
                    //if(z == nz/2 && y == ny/2) printf("%d-> %d\n", origAtt, att);
                }
            }
        }
        printf("reading back\n");
        long errorCount = 0;
        int debugCount = 1;
        for(int y =0; y < ny; y++){
            for(int x =0; x < nx; x++){
                for(int z = 0; z < nz; z++){
                    long origAtt = (x + (y << blockOrder)  + (z << (blockOrder *2)));
                    long att = grid.getAttribute(x,y,z);                    
                    if(att != origAtt) {
                        if(debugCount-- > 0 ) printf("(%2d %2d %2d) %8x -> %8x\n", x,y,z, origAtt, att);
                        errorCount++;
                    }
                    //if(z == nz/2 && y == ny/2) printf("%d-> %d\n", origAtt, att);
                }
            }
        }                                       
        printf("errorCount: %d\n", errorCount);

    }


    public static void main(String arg[]){
        //new TestSparseGridInt().devTestSphere();
        //new TestSparseGridInt().devTestThinLayer();
        new TestSparseGridInt().devTestFullGrid();
    }

}


