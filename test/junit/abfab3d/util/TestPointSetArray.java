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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Random;

import javax.vecmath.Vector3d;

import abfab3d.core.TriangleProducer;
import abfab3d.core.TriangleCollector;

import abfab3d.io.output.STLWriter;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;
import static java.lang.Math.*;


/**


 */
public class TestPointSetArray extends TestCase {

    static final boolean DEBUG = false;

    
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestPointSetArray.class);
    }


    public void testNothing(){

    }

    public void devTest2shells() throws Exception {
        PointSetArray ps = new PointSetArray();
        double a = 10*MM;
        ps.addPoint(a,0,0);
        ps.addPoint(-a,0,0);
        ps.setPointSize(2.1*a);
        ps.setSubdivisionLevel(3);
        ps.setShapeType(PointSetArray.SHAPE_SPHERE);
        STLWriter stl = new STLWriter("/tmp/points2shells.stl");
        ps.getTriangles(stl);
        stl.close();

    }

    /**
       makes beads arranged in a ring 
     */
    public void makeBeadedRing(int beadCount, double ringRadius, double beadRadius) throws Exception {

        PointSetArray ps = new PointSetArray(beadCount);

        ps.setPointSize(beadRadius*2);
        ps.setSubdivisionLevel(3);
        ps.setShapeType(PointSetArray.SHAPE_SPHERE);

        double phi = 2*Math.PI/beadCount;
        for(int i = 0; i < beadCount; i++){

            double x = ringRadius*cos(i*phi);
            double y = ringRadius*sin(i*phi);
            double z = beadRadius;
            ps.addPoint(x,y,z);

        }
        STLWriter stl = new STLWriter("/tmp/beadedRing.stl");
        ps.getTriangles(stl);
        stl.close();
    }

    public void makeCubicLattice(int cellCount, double cellSize, double beadRadius) throws Exception {

        PointSetArray ps = new PointSetArray(cellCount*cellCount*cellCount);

        ps.setPointSize(beadRadius*2);
        ps.setSubdivisionLevel(3);
        ps.setShapeType(PointSetArray.SHAPE_SPHERE);

        for(int ix = 0; ix < cellCount; ix++){
            for(int iy = 0; iy < cellCount; iy++){
                for(int iz = 0; iz < cellCount; iz++){
                    
                    double x = ix*cellSize;
                    double y = iy*cellSize;
                    double z = iz*cellSize;
                    ps.addPoint(x,y,z);
                }
            }
        }
        
        STLWriter stl = new STLWriter("/tmp/cubic_lattice.stl");
        ps.getTriangles(stl);
        stl.close();
    }

    void makeNoisySphere(double radius, int subdivisionLevel, double noice) throws Exception{
        
        TriangulatedSphere sphere = new TriangulatedSphere(radius, new Vector3d(radius,radius,radius), subdivisionLevel);
        TriangleRandomizer tr = new TriangleRandomizer(noice, sphere);
        STLWriter stl = new STLWriter("/tmp/noisy_sphere.stl");
        
        tr.getTriangles(stl);

        stl.close();
        
    }

    void makePuncturedSphere(double radius, int subdivisionLevel, double holesRatio) throws Exception{
        
        TriangulatedSphere sphere = new TriangulatedSphere(radius, new Vector3d(radius,radius,radius), subdivisionLevel);
        TriangleRemover tr = new TriangleRemover(holesRatio, sphere);
        STLWriter stl = new STLWriter("/tmp/punctured_sphere.stl");
        
        tr.getTriangles(stl);

        stl.close();
        
    }

    static class TriangleRandomizer implements TriangleProducer, TriangleCollector {

        double m_noice;
        TriangleProducer m_triProducer;
        TriangleCollector m_triCollector;
        Random m_random;

        TriangleRandomizer(double noice, TriangleProducer tp){

            m_triProducer = tp;
            m_noice = noice;
            m_random = new Random(123); // seed 
        }
        
        public boolean getTriangles(TriangleCollector tc){

            m_triCollector = tc;
            return m_triProducer.getTriangles(this);

        }

        public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2){

            return m_triCollector.addTri(getRandomV(v0),getRandomV(v1),getRandomV(v2));
            
        }

        Vector3d getRandomV(Vector3d v){
            return new Vector3d(v.x + noice(),v.y + noice(),v.z + noice());
        }

        double noice(){
            return m_noice*(2*m_random.nextDouble()-1);
        }

    } // static class TriangleRandomizer


    /**
       discards random triangles from inpur
     */
    static class TriangleRemover implements TriangleProducer, TriangleCollector {

        double m_ratio;
        TriangleProducer m_triProducer;
        TriangleCollector m_triCollector;
        Random m_random;

        TriangleRemover(double ratio, TriangleProducer tp){

            m_triProducer = tp;
            m_ratio = ratio;
            m_random = new Random(123); // seed 
        }
        
        public boolean getTriangles(TriangleCollector tc){

            m_triCollector = tc;
            return m_triProducer.getTriangles(this);

        }

        public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2){

            if(m_random.nextDouble() > m_ratio) 
                return m_triCollector.addTri(v0,v1,v2);
            else 
                return true;
        }
    } // static class TriangleRemover

    public static void main(String arg[])throws Exception {

        //new TestPointSetArray().devTest2shells();
        //new TestPointSetArray().makeBeadedRing(2400, 80*MM, 1*MM);
        //new TestPointSetArray().makeCubicLattice(20, 8*MM, 4.1*MM);
        //new TestPointSetArray().makeNoisySphere(25*MM, 9, 0.01*MM);
        new TestPointSetArray().makePuncturedSphere(25*MM, 9, 0.1);
                
    }
}
