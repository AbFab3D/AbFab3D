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
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.vecmath.Vector3d;

// Internal Imports
import abfab3d.util.TrianglePrinter;
import abfab3d.util.TriangulatedSphere;
import abfab3d.io.output.STLWriter;
import abfab3d.io.output.X3DWriter;

import abfab3d.core.TriangleProducer;
import abfab3d.core.AttributedTriangleProducer;
import abfab3d.core.DataSource;

import abfab3d.datasources.Constant;
import abfab3d.datasources.VolumePatterns;
import abfab3d.datasources.Mix;
import abfab3d.datasources.Mask;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.fmt;
import static java.lang.Math.*;


/**
 * Tests the functionality of TriangleModelCreator.
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestTriangulatedModels extends TestCase {

    /**+
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTriangulatedModels.class);
    }


    public void testDumb(){
        //this test here is to make Test happy. 
    }

    /**
     * Test the generation of star
     */
    public void makeStar() throws IOException {
	
        //TriangulatedModels.Star star = new  TriangulatedModels.Star(100, 0.01, 0.12, 0.005, 2., 0.5);
        //TriangulatedModels.Star star = new  TriangulatedModels.Star(20, 1.9*MM, 0.1*MM, 0.1*MM, 20*MM, 10*MM);
        TriangulatedModels.Star star = new  TriangulatedModels.Star(24, 3*MM, 3*MM, 3*MM, 20*MM, 10*MM);
        TrianglePrinter tp = new  TrianglePrinter();
        STLWriter stl = new STLWriter("/tmp/star_24.stl");
        star.getTriangles(stl);
        star.getTriangles(tp);
        stl.close();              

    }

    public void _testParallelepiped() throws IOException {
	
        TriangulatedModels.Parallelepiped pp = new  TriangulatedModels.Parallelepiped(0.25, 0.25, 0.25, 0.75,  0.75,  0.75);
        //TriangulatedModels.Parallelepiped pp = new  TriangulatedModels.Parallelepiped(0.25, 0.25, 0.25, 0.35,  0.45,  0.55);
        STLWriter stl = new STLWriter("/tmp/parallelepiped.stl");
        pp.getTriangles(stl);
        stl.close();              

    }

    public void _testTetrahedronInParallelepiped() throws IOException {
	
        //TriangulatedModels.TetrahedronInParallelepiped pp = new  TriangulatedModels.TetrahedronInParallelepiped(0.05*MM, 0.05*MM, 0.05*MM, 0.45*MM,  0.45*MM,  0.45*MM,1);
        //TriangulatedModels.TetrahedronInParallelepiped pp = new  TriangulatedModels.TetrahedronInParallelepiped(0.55*MM, 0.55*MM, 0.55*MM, 0.95*MM,  0.95*MM,  0.95*MM,1);
        //TriangulatedModels.TetrahedronInParallelepiped pp = new  TriangulatedModels.TetrahedronInParallelepiped(0.05, 0.25, 0.25, 0.75,  0.75,  0.75,0);
        //TriangulatedModels.TetrahedronInParallelepiped pp = new  TriangulatedModels.TetrahedronInParallelepiped(0.01*MM, 0.01*MM, 0.01*MM, 0.24*MM,  0.24*MM, 0.24*MM,0);
        //TriangulatedModels.TetrahedronInParallelepiped pp1 = new  TriangulatedModels.TetrahedronInParallelepiped(0.25, 0.25, 0.25, 0.75,  0.75,  0.75,1);


        double eps = 1.e-6*MM;
        double size = 0.25*MM;
        double shiftx = 0*size;
        double shifty = 2*size;
        double shiftz = 0*size;
        //TriangulatedModels.Parallelepiped pp = new  TriangulatedModels.Parallelepiped(eps+shiftx, eps+shifty, eps+shiftz, size-eps+shiftx, size-eps+shifty, size-eps+shiftz);
        TriangulatedModels.TetrahedronInParallelepiped pp = 
            new  TriangulatedModels.TetrahedronInParallelepiped(eps+shiftx, eps+shifty, eps+shiftz, size-eps+shiftx, size-eps+shifty, size-eps+shiftz,0);

        TriangulatedModels.Star star = new TriangulatedModels.Star(400, 0.002, 0.001, 0.001, 0.6, 0.7);

        STLWriter stl = new STLWriter("/tmp/star_400.stl");
        star.getTriangles(stl);
        //pp1.getTriangles(stl);
        stl.close();              

    }

    public void devTestMakeSpheres()throws Exception {

        //        double c = 0*MM;
        double r = 50*MM;
        double cent[][] = new double[][]
            {
                {0,0,0},
            };

        for(int i = 0; i < 5; i++) {
            makeSpheres(cent, r, fmt("/tmp/1_sphere_%d.stl", i),i);
        }
    }
    
    public static void makeSpheres(double [][] cent, double rad, String name, int subdivide)throws Exception {
        
        STLWriter stl = new STLWriter(name);
        
        for(int i = 0; i < cent.length; i++){
            TriangulatedModels.Sphere s = new  TriangulatedModels.Sphere(rad, new Vector3d(cent[i][0],cent[i][1],cent[i][2]), subdivide);
            s.getTriangles(stl);            
        }

        stl.close();              
        
    }

    public static void makeSphere()throws Exception {
        
        STLWriter stl = new STLWriter("/tmp/sphere_10cm.stl");
        
        int n = 10;

        TriangulatedModels.Sphere s = new  TriangulatedModels.Sphere(50*MM, new Vector3d(0,0,0), 10);
        s.setTolerance(0.001*MM);

        s.getTriangles(stl);

        stl.close();              

    }


    public static void makeChainOfSphere()throws Exception {
        
        STLWriter stl = new STLWriter("/tmp/chain_of_spheres.stl");
        
        int n = 10;

        for(int i = 0; i < 10; i++){

            double r = 10*MM;
            double rr = 30*MM;
            double phi = (2*Math.PI*i)/n;
            double xc = rr*cos(phi);
            double yc = rr*sin(phi);
            double zc = 0;

            TriangulatedModels.Sphere s = new  TriangulatedModels.Sphere(r, new Vector3d(xc, yc, zc), 6);
            s.getTriangles(stl);
        }

        stl.close();              
       
    }

    public void devTestCylinder(String outPath, double r0, double r1, double L, int facetCount)throws Exception {
        
        
        TriangulatedModels.Combiner comb = new TriangulatedModels.Combiner();
        
        comb.append(new  TriangulatedModels.CylinderT(new Vector3d(0, 0,0), new Vector3d(L,0,0), r0, r1, facetCount));        
        comb.append(new  TriangulatedModels.CylinderT(new Vector3d(L, 0,0), new Vector3d(L,L,0), r0, r1, facetCount));
        comb.append(new  TriangulatedModels.CylinderT(new Vector3d(L, L,0), new Vector3d(0,L,0), r0, r1, facetCount));
        comb.append(new  TriangulatedModels.CylinderT(new Vector3d(0, L,0), new Vector3d(0,0,0), r0, r1, facetCount));
        
        comb.append(new  TriangulatedModels.CylinderT(new Vector3d(0, 0,L), new Vector3d(L,0,L), r0, r1, facetCount));
        comb.append(new  TriangulatedModels.CylinderT(new Vector3d(L, 0,L), new Vector3d(L,L,L), r0, r1, facetCount));
        comb.append(new  TriangulatedModels.CylinderT(new Vector3d(L, L,L), new Vector3d(0,L,L), r0, r1, facetCount));
        comb.append(new  TriangulatedModels.CylinderT(new Vector3d(0, L,L), new Vector3d(0,0,L), r0, r1, facetCount));
        
        comb.append(new  TriangulatedModels.CylinderT(new Vector3d(0, 0,0), new Vector3d(0,0,L), r0, r1, facetCount));
        comb.append(new  TriangulatedModels.CylinderT(new Vector3d(L, 0,0), new Vector3d(L,0,L), r0, r1, facetCount));
        comb.append(new  TriangulatedModels.CylinderT(new Vector3d(L, L,0), new Vector3d(L,L,L), r0, r1, facetCount));
        comb.append(new  TriangulatedModels.CylinderT(new Vector3d(0, L,0), new Vector3d(0,L,L), r0, r1, facetCount));
        
        comb.initialize();        

        STLWriter stl = new STLWriter(outPath);
        comb.getTriangles(stl);        
        stl.close();              
       
    }

    
    public void devTestBox(String outPath, double size, Vector3d center, Vector3d offset, double scale, int count)throws Exception {

        
        TriangulatedModels.Combiner model = new TriangulatedModels.Combiner();

        for(int i = 0; i < count; i++){
            model.append(new TriangulatedModels.Box(center.x,center.y,center.z,size, size, size));
            size *=scale;
            //center.x += offset.x;
            //center.y += offset.y;
            //center.z += offset.z;
            center.add(offset);
            offset.scale(scale);

        }
        model.initialize();        
        STLWriter stl = new STLWriter(outPath);
        model.getTriangles(stl);        
        stl.close();              

    }


    public void devTestFlippedTriangles(String outPath, double radius, int subdivisionLevel, Vector3d center, double flipRatio, double noise)throws Exception {

        TriangulatedSphere sphere = new TriangulatedSphere(radius, center, subdivisionLevel);
        TriangulatedModels.TriangleFlipper tf = new TriangulatedModels.TriangleFlipper(flipRatio, 123, sphere);
        TriangulatedModels.TriangleRandomizer tr = new TriangulatedModels.TriangleRandomizer(noise, 231, tf);
        STLWriter stl = new STLWriter(outPath);
        
        tr.getTriangles(stl);

        stl.close();
        
    }

    public void devTestTwistedBand(String outPath, double radius, double bandWidth, double bandThickness, double twist, double spin, 
                                   Vector3d center, double cellSize) throws Exception {

        TriangleProducer tp = new TriangulatedTwistedBand(radius, bandWidth, bandThickness, twist, spin, center, cellSize);

        if(false){
            TrianglePrinter tpr = new TrianglePrinter(true, "%8.5f");    
            tp.getTriangles(tpr);
            printf("triangle count: %d\n", tpr.getCount());
        }
        if(true){
            STLWriter stl = new STLWriter(outPath);        
            tp.getTriangles(stl);
            stl.close();        
        }
    }


    static void devTestColorless()throws Exception {
        
        if(false){
            double a = 10*MM;
            double s = 0.9;        
            new TestTriangulatedModels().devTestBox("/tmp/stackedBoxes_40.stl", 2*a, new Vector3d(a,a,a),new Vector3d((1+s)*a,0,0),s,40);
        }
        if(false){
            double r = 50*MM;
            double flipRatio = 0.1;
            new TestTriangulatedModels().devTestFlippedTriangles("/tmp/flippedNormals.stl", r, 3, new Vector3d(r,r,r), flipRatio, 0.0);
        }

        if(true){
            double r = 9*MM;
            double w = 20*MM;
            double th = 1*MM;
            double cellSize = 1*MM;
            double twist = 0.5;
            double spin = 2;
            new TestTriangulatedModels().devTestTwistedBand("/tmp/twistedBand.stl", r, w, th, twist, spin, new Vector3d(r+w/2,w,r+w/2+th), cellSize);

        }            

    }

    static void makeColoredSphere(String path)throws Exception {

        double rad = 50*MM;
        Vector3d center = new Vector3d(rad, rad, rad);
        int subdiv = 5;
        double flipRatio = 0.05;
        double noise = 0.0*MM;
        double holesRatio = 0.3;

        TriangleProducer sphere = new TriangulatedModels.Sphere(rad, center, subdiv);

        TriangulatedModels.TriangleFlipper tf = new TriangulatedModels.TriangleFlipper(flipRatio, 123, sphere);
        TriangulatedModels.TriangleRandomizer rsphere = new TriangulatedModels.TriangleRandomizer(noise, 231, tf);
        TriangulatedModels.TriangleRemover holedSphere = new TriangulatedModels.TriangleRemover(holesRatio, 123, sphere);
        
        DataSource yellow = new Constant(1,1,0); // yellow 
        DataSource orange = new Constant(0.9,0.5,0); // orange
        DataSource blue = new Constant(0.3,0,0.9); //  
        DataSource gyroid = new VolumePatterns.Gyroid(rad,7*MM);
        DataSource mgyroid = new Mask(gyroid, 0, 7*MM);

        DataSource mix = new Mix(yellow, blue, mgyroid);

        //AttributedTriangleProducer csphere = new TriangleColorizer(rsphere, mix);
        AttributedTriangleProducer csphere = new TriangleColorizer(holedSphere, mix);

        X3DWriter writer = new X3DWriter(path);
        writer.setWriteColoredMesh(true);
        csphere.getAttTriangles(writer);
        writer.close();

    }

    static void devTestColored() throws Exception {

        makeColoredSphere("/tmp/colored_sphere.x3db");

    }
    

    public static void main(String[] arg) throws Exception {

        //new TestTriangulatedModels().devTestMakeSpheres();
        //new TestTriangulatedModels().devTestCylinder("/tmp/cubeFrame.stl", 5*MM, 4*MM, 50*MM, 4);
        //new TestTriangulatedModels().devTestCylinder("/tmp/cubeFrame_100.stl", 5*MM, 4*MM, 50*MM, 100);
        //new TestTriangulatedModels().devTestCylinder("/tmp/cubeFrame_1000.stl", 5*MM, 4*MM, 50*MM, 1000);
        //double a = 10*MM;
        //new TestTriangulatedModels().devTestBox("/tmp/stackedBoxes_4.stl", 2*a, new Vector3d(a,a,a),new Vector3d(2*a,0,0),1.,4);

        //devTestColorless();
        devTestColored();

    }
}
