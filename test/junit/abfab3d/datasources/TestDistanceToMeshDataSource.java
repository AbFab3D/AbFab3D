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

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import javax.vecmath.Vector3d;

// Internal Imports

import abfab3d.core.Vec;
import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.AttributeGrid;
import abfab3d.core.TriangleProducer;

import abfab3d.util.ColorMapperDistance;
import abfab3d.util.ColorMapper;

import abfab3d.geom.Octahedron;
import abfab3d.geom.TriangulatedModels;

import abfab3d.grid.op.ImageMaker;


import abfab3d.grid.op.GridMaker;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;

/**
 * Tests the functionality of Sphere
 *
 * @version
 */
public class TestDistanceToMeshDataSource extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSphere.class);
    }

    public void testNothing() {
        printf("testNothing()\n");
    }


    void devTestSlice() throws Exception {
        
        //Octahedron producer = new Octahedron(10*MM);
        double sx = 1.*MM; 
        double sy = 1.*MM; 
        double sz = 2.*MM; 
        double margins = 10*MM;
        double radius = 10*MM;
        int imgWidth = 1000;
        int imgHeight = 1000;
        double voxelSize = 1*MM;
        double bandWidth = voxelSize;
        //TriangleProducer producer = new TriangulatedModels.Box(0.4*voxelSize,0.4*voxelSize,0.4*voxelSize,sx, sy, sz);
        TriangleProducer producer = new TriangulatedModels.Sphere(radius, new Vector3d(0,0,0), 6);
        
        DistanceToMeshDataSource dmds = new DistanceToMeshDataSource(producer);
        dmds.set("margins", margins);
        dmds.set("useMultiPass", true);
        dmds.set("voxelSize", voxelSize);

        dmds.set("surfaceVoxelSize", 1./3);
        dmds.set("interpolationType", DistanceToMeshDataSource.INTERPOLATION_LINEAR);
        dmds.set("shellHalfThickness", 2.6);
        dmds.set("maxDistance", voxelSize*3);

        dmds.initialize();

        //double s = radius + margins;
        //Bounds bounds = new Bounds(-s, s, -s, s, 0*MM, 0*MM);
        double s = 20*voxelSize;
        Bounds bounds = new Bounds(radius-s, radius+s, -s, s, 0*MM, 0*MM);

        int N = 30;
        Vec pnt = new Vec(3);
        Vec data = new Vec(3);
        if(false){
            for(int i = 0; i <= N; i++){
                double 
                    y = -s + 2*i*s/N,
                    z = 0,
                    x = 0;
                
                pnt.v[0] = x;
                pnt.v[1] = y;
                pnt.v[2] = z;
                dmds.getDataValue(pnt, data);
                printf("(%7.2f, %7.2f, %7.2f) -> %7.2f mm\n", x/MM, y/MM,z/MM, data.v[0]/MM);
            }
        }
        ImageMaker im = new ImageMaker();

        ColorMapper colorMapper = new ColorMapperDistance(bandWidth);        
        BufferedImage img1 = im.renderImage(imgWidth, imgHeight, bounds, new SliceDistanceColorizer(dmds, colorMapper));        
        ImageIO.write(img1, "png", new File("/tmp/00_sphere_dist.png"));        
        
    }

    public static void main(String[] args) throws Exception {

        new TestDistanceToMeshDataSource().devTestSlice();
        
    }
    
}