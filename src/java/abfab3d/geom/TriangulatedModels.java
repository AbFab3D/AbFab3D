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
import java.util.*;


import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;


import abfab3d.core.Vec;
import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;
import abfab3d.core.Transformer;
import abfab3d.core.Initializable;
import abfab3d.core.ResultCodes;
import abfab3d.util.ImageGray16;
import abfab3d.core.VecTransform;

import abfab3d.transforms.Identity;

import static abfab3d.core.MathUtil.distance;
import static abfab3d.core.MathUtil.midPoint;
import static abfab3d.core.Units.MM;

import static java.lang.Math.sqrt;
import static java.lang.Math.max;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;


// Internal Imports



// static imports 
import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.lang.Math.cos;


/**
 * Creates a bunch of Models as triangle meshes
 *
 * @author Vladimir Bulatov
 */
public class TriangulatedModels {

    //TODO need class TriangleSplitter which splits incomng triangles into smaller pieces 
    // according to the specified precision 
    
    public static final double DEFAULT_PRECISION = 0.001*MM;


    /**
       makes multiple copies of single TriangleProducer by applying array of different VecTransforms to it 
       Produces single stream of triangles
       Triangles are produced in the same order as VecTransforms are added 
     */
    public static class Multiplier implements TriangleProducer, Initializable {

        TriangleProducer source;
        Vector<VecTransform> transforms = new Vector<VecTransform>();

        public Multiplier(TriangleProducer producer){ 
            this.source = producer;
        }
        public int initialize(){
            if(source instanceof Initializable)
                ((Initializable)source).initialize();
            return 0;
        }

        /**
           append transform to the array of transforms 
         */
        public void append(VecTransform transform){
            transforms.add(transform);
        }

        public boolean getTriangles(TriangleCollector collector){

            boolean result = true;
            for(int i = 0; i < transforms.size(); i++){
                VecTransform vt = transforms.get(i);
                if(vt instanceof Initializable)
                    ((Initializable)vt).initialize();

                TC tc = new TC(collector, vt);
                result = result && source.getTriangles(tc);
            }
            return result;

        }
        
        class TC implements TriangleCollector {
            
            TriangleCollector tc;
            VecTransform trans;
            Vec 
                vv0 = new Vec(3),
                vv1 = new Vec(3),
                vv2 = new Vec(3);
                
            TC(TriangleCollector tc, VecTransform trans){
                this.tc = tc;
                this.trans = trans;
            }

            public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2){
                if(trans != null){
                    vv0.set(v0);
                    vv1.set(v1);
                    vv2.set(v2);
                    trans.transform(vv0, vv0);
                    trans.transform(vv1, vv1);
                    trans.transform(vv2, vv2);
                    vv0.get(v0);
                    vv1.get(v1);
                    vv2.get(v2);
                }
                return tc.addTri(v0, v1, v2);
            }
        }        
    } // Multiplier 

    /**
       combines several TriangleProducers into single TriangleProducer 
       
     */
    public static class Combiner implements TriangleProducer, TriangleCollector, Initializable, Transformer {

        VecTransform transform = new Identity();
        Vector<TriangleProducer> producers = new Vector<TriangleProducer>(); 

        public Combiner(){
        }

        public int initialize(){
            if(transform != null && transform instanceof Initializable){
                ((Initializable)transform).initialize();
            }
            
            for(int i = 0; i < producers.size(); i++){
                TriangleProducer tp = producers.get(i);
                if(tp instanceof Initializable){
                    ((Initializable)tp).initialize();
                }
                
            }  
            return ResultCodes.RESULT_OK;
        }
        
        public void setTransform(VecTransform trans){
            transform = trans;
        }
        /**
           appends producer to the arrays of triangle sources 
         */
        public void append(TriangleProducer producer){
            producers.add(producer);
        }

        // interface TriangleProducer
        public boolean getTriangles(TriangleCollector tc){
            
            m_tc = tc;

            for(int i = 0; i < producers.size(); i++){
                TriangleProducer tp = producers.get(i);
                tp.getTriangles(this);
            }            
            return true;
        }

        // currently used triangle collector
        TriangleCollector m_tc; 

        // this called by each TriangleProducer         
        public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2){
            
            Vec in = new Vec(3);
            Vec out = new Vec(3);
            Vector3d 
                tv0 = new Vector3d(v0),
                tv1 = new Vector3d(v1),
                tv2 = new Vector3d(v2);
            
            in.set(v0);
            transform.transform(in, out);
            out.get(tv0);
            
            in.set(v1);
            transform.transform(in, out);
            out.get(tv1);
            in.set(v2);
            transform.transform(in, out);
            out.get(tv2);

            m_tc.addTri(tv0,tv1,tv2);
            
            return true;
        }
        
    } // class Transformer


    // class to generate stars with parameters
    // parameters are illustrated in file docs/images/mesh_star.svg
    public static class Star  implements TriangleProducer {

        double armCount;
        double armBaseWidth;
        double armEndWidth;
        double spaceWidth;
        double armLength;
        double thickness;
        
        public Star(int armCount, 
                    double armBaseWidth, 
                    double armEndWidth, 
                    double spaceWidth, 
                    double armLenght,
                    double thickness){
            this.armCount = armCount;
            this.armBaseWidth = armBaseWidth;
            this.armEndWidth = armEndWidth;
            this.spaceWidth = spaceWidth;
            this.armLength = armLenght;
            this.thickness = thickness;
            
            
        }
        public boolean getTriangles(TriangleCollector tc){
            double a = PI/armCount;

            double Cy = armBaseWidth/2;
            double Cx = (Cy * cos(a) + spaceWidth/2 )/sin(a);
            double Dy = armEndWidth/2;
            double Dx = Cx + armLength;
            Matrix3d rot = new Matrix3d();
            rot.set(new AxisAngle4d(0,0,1,2*a));
                
            Vector3d 
                v0 = new Vector3d(0, 0, 0),
                v1 = new Vector3d(Cx, -Cy, 0),
                v2 = new Vector3d(Dx, -Dy, 0),
                v3 = new Vector3d(Dx, Dy, 0),
                v4 = new Vector3d(Cx, Cy, 0),
                v5 = new Vector3d(Cx, -Cy, 0);
            rot.transform(v5);
                
            Vector3d v0t = new Vector3d(),
                v1t = new Vector3d(), 
                v2t = new Vector3d(), 
                v3t = new Vector3d(), 
                v4t = new Vector3d(),
                v5t = new Vector3d();
            
            Vector3d zoffset = new Vector3d(0, 0, -thickness);
            for(int i = 0; i < armCount; i++){    
            //for(int i = 0; i < 1; i++){    
                //Matrix3d rot = new Matrix3d();
                rot.set(new AxisAngle4d(0,0,1,2*a*i));
                rot.transform(v0, v0t);
                rot.transform(v1, v1t);
                rot.transform(v2, v2t);
                rot.transform(v3, v3t);
                rot.transform(v4, v4t);
                rot.transform(v5, v5t);

                //tc.addTri(v0t, v1t, v4t);
                //tc.addTri(v1t, v2t, v3t);
                //tc.addTri(v1t, v3t, v4t);
                //tc.addTri(v0t, v4t, v5t);

                addBases(tc, new Vector3d[]{v0t, v1t, v4t}, zoffset);
                addBases(tc, new Vector3d[]{v1t, v2t, v3t}, zoffset);
                addBases(tc, new Vector3d[]{v1t, v3t, v4t}, zoffset);
                addBases(tc, new Vector3d[]{v0t, v4t, v5t}, zoffset);
                
                addBand(tc, new Vector3d[]{v1t, v2t, v3t, v4t, v5t}, zoffset);
                //addBand(tc, new Vector3d[]{v1t, v2t}, zoffset); addBand(tc, new Vector3d[]{v3t, v4t, v5t}, zoffset);
                //addBand(tc, new Vector3d[]{v1t, v2t, v3t, v4t}, zoffset); 

            }
            
            return true;
            
        }
    } // class Star  

    // adds band of triangles along path with given offset 
    public static void addBand(TriangleCollector tc, Vector3d[] path, Vector3d offset){

        for(int i = 0; i < path.length-1; i++){
            Vector3d v0 = path[i];
            Vector3d v1 = path[i+1];
            Vector3d v0t = new Vector3d(v0);
            Vector3d v1t = new Vector3d(v1);
            v0t.add(offset);
            v1t.add(offset);
            
            tc.addTri(v0, v0t, v1t);
            tc.addTri(v0, v1t, v1);
        }
    }
    // adds two base triangles, original and translated by offset 
    
    public static void addBases(TriangleCollector tc, Vector3d[] tri, Vector3d offset){
        
        
            Vector3d v0t = new Vector3d(tri[0]);
            Vector3d v1t = new Vector3d(tri[1]);
            Vector3d v2t = new Vector3d(tri[2]);
            
            v0t.add(offset);
            v1t.add(offset);
            v2t.add(offset);

            tc.addTri(tri[0],tri[1],tri[2]);
            tc.addTri(v1t, v0t, v2t);

        
    }

    
    /**
       
       creates height field of triangles from 2D grid 
       
    */
    public static class HeightField  implements TriangleProducer {

        ImageGray16  image;
        int nx,  ny;
        int maxDataValue;
        double sx, sy, sz;

        public HeightField(ImageGray16 image, int maxDataValue, double sx, double sy, double sz){
            
            this.image = image;
            this.nx = image.getWidth();
            this.ny = image.getHeight();
            this.maxDataValue = maxDataValue;
            this.sx = sx;
            this.sy = sy;
            this.sz = sz;          

        }
        
        public boolean getTriangles(TriangleCollector tc){
            
            double dx = sx / nx;
            double dy = sy / ny;
            
            
            for(int iy = 0; iy < ny-1; iy++){
                
                double y0 = iy * sy / ny;
                double y1 = y0 + dy;
                
                for(int ix = 0; ix < nx-1; ix++){
                    
                    double x0 = ix * sx / nx;
                    double x1 = x0 + dx;
                    double d00 = getHeight(ix,iy);
                    double d10 = getHeight(ix+1,iy);
                    double d01 = getHeight(ix,iy+1);
                    double d11 = getHeight(ix+1,iy+1);
                    
                    double dc = (d00 + d10 + d01 + d11)/4.;
                    Vector3d 
                        v00 = new Vector3d(x0, y0, d00),                    
                        v10 = new Vector3d(x1, y0, d10),
                        v01 = new Vector3d(x0, y1, d01),
                        v11 = new Vector3d(x1, y1, d11),
                        vc = new Vector3d((x0+x1)/2, (y0 + y1)/2, dc);
                    
                    tc.addTri(v00, v10, vc);
                    tc.addTri(v10, v11, vc);
                    tc.addTri(v11, v01, vc);
                    tc.addTri(v01, v00, vc);
                }            
            }
            return true;
        }

        double getHeight(int ix, int iy){

            double d = image.getDataI(ix, iy);
            return (d * sz / maxDataValue);
        }

    } // class HeightField 


    /**
       Parallelepiped with given coordinates of main diagonal 
    */
    public static class Parallelepiped   implements TriangleProducer {

        double x0, y0, z0;
        double x1, y1, z1;


        public Parallelepiped(double x0, double y0, double z0, double x1, double y1, double z1){

            this.x0 = x0;
            this.x1 = x1;

            this.y0 = y0;
            this.y1 = y1;

            this.z0 = z0;
            this.z1 = z1;

        }
        
        public boolean getTriangles(TriangleCollector tc){
            Vector3d 
                v000 = new Vector3d(x0,y0,z0),
                v100 = new Vector3d(x1,y0,z0),
                v010 = new Vector3d(x0,y1,z0),
                v110 = new Vector3d(x1,y1,z0),
                v001 = new Vector3d(x0,y0,z1),
                v101 = new Vector3d(x1,y0,z1),
                v011 = new Vector3d(x0,y1,z1),
                v111 = new Vector3d(x1,y1,z1);
                
            tc.addTri(v001,v101,v111);
            tc.addTri(v001,v111,v011);
            tc.addTri(v101,v100,v110);
            tc.addTri(v101,v110,v111);
            tc.addTri(v100,v000,v010);
            tc.addTri(v100,v010,v110);
            tc.addTri(v000,v001,v011);
            tc.addTri(v000,v011,v010);
            tc.addTri(v011,v111,v110);
            tc.addTri(v011,v110,v010);
            tc.addTri(v000,v100,v101);
            tc.addTri(v000,v101,v001);

            return true;
        }
        
    } // class Parallelepiped

    public static class Box   implements TriangleProducer {

        double x0, y0, z0;
        double x1, y1, z1;

        // box with given center and size 
        public Box(double xc, double yc, double zc, double sx, double sy, double sz){

            this.x0 = xc - sx/2;
            this.x1 = xc + sx/2;
            this.y0 = yc - sy/2;
            this.y1 = yc + sy/2;
            this.z0 = zc - sz/2;
            this.z1 = zc + sz/2;

        }
        
        public boolean getTriangles(TriangleCollector tc){
            Vector3d 
                v000 = new Vector3d(x0,y0,z0),
                v100 = new Vector3d(x1,y0,z0),
                v010 = new Vector3d(x0,y1,z0),
                v110 = new Vector3d(x1,y1,z0),
                v001 = new Vector3d(x0,y0,z1),
                v101 = new Vector3d(x1,y0,z1),
                v011 = new Vector3d(x0,y1,z1),
                v111 = new Vector3d(x1,y1,z1);
                
            tc.addTri(v001,v101,v111);
            tc.addTri(v001,v111,v011);
            tc.addTri(v101,v100,v110);
            tc.addTri(v101,v110,v111);
            tc.addTri(v100,v000,v010);
            tc.addTri(v100,v010,v110);
            tc.addTri(v000,v001,v011);
            tc.addTri(v000,v011,v010);
            tc.addTri(v011,v111,v110);
            tc.addTri(v011,v110,v010);
            tc.addTri(v000,v100,v101);
            tc.addTri(v000,v101,v001);

            return true;
        }
        
    } // class Box


    /**
       TetrahedronInParallelepiped 
        makes tetrahedron inscibed in parallelepiped with given corners 
    */
    public static class TetrahedronInParallelepiped implements TriangleProducer {

        double x0, y0, z0;
        double x1, y1, z1;
        int type = 0; // two possible types 0 and 1 
        
        /**
           x0, y0, z0 - coordinates of first cornwer of parallelepiped 
           x1, y1, z1 - coordinates of second cornwer of parallelepiped 

         */
        public TetrahedronInParallelepiped(double x0, double y0, double z0, double x1, double y1, double z1, int type){

            this.x0 = x0;
            this.x1 = x1;

            this.y0 = y0;
            this.y1 = y1;

            this.z0 = z0;
            this.z1 = z1;
            this.type = type;
        }
        
        public boolean getTriangles(TriangleCollector tc){
            Vector3d 
                v000 = new Vector3d(x0,y0,z0),
                v100 = new Vector3d(x1,y0,z0),
                v010 = new Vector3d(x0,y1,z0),
                v110 = new Vector3d(x1,y1,z0),
                v001 = new Vector3d(x0,y0,z1),
                v101 = new Vector3d(x1,y0,z1),
                v011 = new Vector3d(x0,y1,z1),
                v111 = new Vector3d(x1,y1,z1);
                
            if(type == 0){
                tc.addTri(v000, v011, v110);
                tc.addTri(v000, v101, v011);
                tc.addTri(v000, v110, v101);
                tc.addTri(v101, v110, v011);
            } else {
                tc.addTri(v111, v100, v010);
                tc.addTri(v111, v010, v001);
                tc.addTri(v111, v001, v100);
                tc.addTri(v001, v010, v100);
            }  
            return true;
        }      
  
    } // class TetrahedronInParallelepiped

    /**
       makes triangulated sphere of given radius and center and subdivion level 
     */
    public static class Sphere  implements TriangleProducer {

        Vector3d center = new Vector3d();
        double radius; 
        int subdivision;
        double m_tolerance = 0.;
        
        // corner vertices of octahedron 
        Vector3d 
            v100 = new Vector3d(1,0,0),
            v_100 = new Vector3d(-1,0,0),
            v010 = new Vector3d(0,1,0),
            v0_10 = new Vector3d(0,-1,0),
            v001 = new Vector3d(0,0,1),
            v00_1 = new Vector3d(0,0,-1);
        
        
        public Sphere(double radius, Vector3d center, int subdivision){
            
            this.center.set(center);

            this.radius = radius; 
            this.subdivision = subdivision;
            
        }

        public void setTolerance(double tolerance){
            m_tolerance = tolerance;
        }
       
        public boolean getTriangles(TriangleCollector tc){
            
            splitTriangle(tc, v100, v010, v001, subdivision);
            splitTriangle(tc, v100, v001, v0_10, subdivision);
            splitTriangle(tc, v100, v0_10, v00_1, subdivision);
            splitTriangle(tc, v100, v00_1, v010, subdivision);

            splitTriangle(tc, v_100, v001,v010,  subdivision);
            splitTriangle(tc, v_100, v0_10,v001,  subdivision);
            splitTriangle(tc, v_100, v00_1,v0_10,  subdivision);
            splitTriangle(tc, v_100, v010,v00_1,  subdivision);

            return true;
        }      
        
        

        protected void splitTriangle(TriangleCollector tc, Vector3d v0, Vector3d v1, Vector3d v2, int subdiv){
           
            if(subdiv <= 0){
                addTri(tc, v0, v1, v2);
                return;                
            }

            Vector3d v01 = null, v12=null, v20=null;

            int selector = 0;
            if(needSubdivision(v0, v1)) { selector += 1;v01 = getSpherePoint(v0, v1);}
            if(needSubdivision(v1, v2)) { selector += 2;v12 = getSpherePoint(v1, v2); }
            if(needSubdivision(v2, v0)) { selector += 4;v20 = getSpherePoint(v2, v0); }
            
            subdiv--;
      
            switch(selector){                
                
            case 0: // no splits 
                addTri(tc, v0, v1, v2);
                break;            
            case 1: // split 01   
                splitTriangle(tc, v0, v01, v2, subdiv);
                splitTriangle(tc, v01, v1, v2, subdiv);
                break;
                
            case 2:  // split 12         
                splitTriangle(tc, v0, v1, v12, subdiv);
                splitTriangle(tc, v0, v12, v2, subdiv);
                break;
                
            case 4:  // split 20 
                splitTriangle(tc, v1, v2, v20,subdiv);
                splitTriangle(tc, v1, v20, v0, subdiv);
                break;        
                
            case 3:  // split 01, 12         
                splitTriangle(tc, v1, v12, v01,subdiv);
                
                if(distance(v01, v2) < distance(v0, v12)) {
                    splitTriangle(tc, v01, v12, v2, subdiv);
                    splitTriangle(tc, v0, v01, v2, subdiv);
                } else {
                    splitTriangle(tc, v01, v12, v0,subdiv);
                    splitTriangle(tc, v0, v12, v2,subdiv);                
                }
                break;
                
                
            case 6:  //split 12 20 
                
                splitTriangle(tc, v12,v2, v20,subdiv );
                if(distance(v0, v12) < distance(v1, v20)) {
                    splitTriangle(tc, v0, v12,v20,subdiv);
                    splitTriangle(tc, v0, v1, v12,subdiv);
                } else {
                    splitTriangle(tc, v0, v1, v20,subdiv);
                    splitTriangle(tc, v1, v12, v20,subdiv);
                }
                break;
                
            case 5:  // split 01, 20 
                splitTriangle(tc, v0, v01, v20, subdiv);
                if(distance(v01, v2) < distance(v1, v20)){
                    splitTriangle(tc, v01, v2, v20,subdiv);
                    splitTriangle(tc, v01, v1, v2, subdiv);
                } else {
                    splitTriangle(tc, v01, v1, v20,subdiv);                
                    splitTriangle(tc, v1, v2, v20, subdiv);                
                }
                break; // split s0, s2       
                
            case 7: // split 01, 12, 20       
                
                splitTriangle(tc, v0, v01, v20,subdiv);
                splitTriangle(tc, v1, v12, v01,subdiv);
                splitTriangle(tc, v2, v20, v12,subdiv);
                splitTriangle(tc, v01, v12, v20, subdiv);
                break;                  
            } // switch()
                                   
        } // void splitTriangle()

        void addTri(TriangleCollector tc, Vector3d v0, Vector3d v1, Vector3d v2){
            
            tc.addTri(getScaledTri(v0),getScaledTri(v1),getScaledTri(v2));
        }

        boolean needSubdivision(Vector3d v0, Vector3d v1, Vector3d v2){
            
            return 
                needSubdivision(v0, v1) || 
                needSubdivision(v1, v2) || 
                needSubdivision(v2, v0);

        }

        boolean needSubdivision(Vector3d v0, Vector3d v1){

            return (radius* distance(midPoint(v0, v1), getSpherePoint(v0, v1)) > m_tolerance);
        }
        
        
        Vector3d getSpherePoint(Vector3d v1, Vector3d v2){
            
            double x = v1.x + v2.x;
            double y = v1.y + v2.y;
            double z = v1.z + v2.z;
            double r = sqrt(x*x + y*y + z*z);
            return new Vector3d(x/r, y/r, z/r);
            
        }
        
        
        Vector3d getScaledTri(Vector3d v){

            return new Vector3d(v.x * radius + center.x, v.y * radius + center.y, v.z * radius + center.z);

        }
        
    } // class Sphere 


    /**
      Torus of given sizes 
     */
    public static class Torus  implements TriangleProducer {
            
        double m_rin, m_rout;
        double m_precision;

        public Torus(double r, double R){
            this(r, R, DEFAULT_PRECISION);
        }

        public Torus(double r, double R, double precision){
            m_rin = r;
            m_rout = R;
            m_precision = precision;
        }

        public boolean getTriangles(TriangleCollector tc){

            ParametricSurfaces.Torus torus = new ParametricSurfaces.Torus(m_rin, m_rout);
            ParametricSurfaceMaker maker = new ParametricSurfaceMaker(torus, m_precision);
            return maker.getTriangles(tc);
            
        }
    } // Torus     
    
    //
    // makes cylnder with given ends and radius 
    //
    public static class CylinderT  implements TriangleProducer, Initializable {        

        static final Vector3d Yaxis = new Vector3d(0,1,0);
        static final double EPSILON  = 1.e-10;

        //TODO 
        double m_defTriangleSize = 1.e-3; // 1mm 
        double r0;
        double r1; 
        Vector3d v0 = new Vector3d();
        Vector3d v1 = new Vector3d();
        double h; // cylinder height 
        Vector3d center; // cylinder center 
        Matrix3d rotation; // rotation to bring cylinder into canonical position 

        static final int DEFAULT_FACETS_COUNT = 20;

        int nfacets = DEFAULT_FACETS_COUNT; // count of facets at the cylinder side 
        // working vectors
        Vector3d tv0 = new Vector3d(),tv1 = new Vector3d(),tv2 = new Vector3d();

        public CylinderT(Vector3d v0, Vector3d v1, double radius) {
            this(v0, v1, radius, radius, DEFAULT_FACETS_COUNT);
        }

        public CylinderT(Vector3d v0, Vector3d v1, double r0, double r1) {

            this(v0, v1, r0, r1, DEFAULT_FACETS_COUNT);
        }

        public CylinderT(Vector3d v0, Vector3d v1, double r0, double r1, int facetCount) {

            this.v0.set(v0);
            this.v1.set(v1);
            this.r0 = r0;
            this.r1 = r1;
            this.nfacets = facetCount;
        }

        public int initialize(){
            //TODO make transform 
            Vector3d s = new Vector3d();
            s.sub(v0, v1);
            
            this.h = s.length();

            this.center = new Vector3d(v0);
            this.center.add(v1);
            this.center.scale(0.5);

            Vector3d caxis = new Vector3d(v1); // cylinder axis 
            caxis.sub(center);
            double len = caxis.length();
            if(h > 0)
                caxis.normalize();
            else 
                caxis.set(0,1,0);

            // rotation axis 
            Vector3d raxis = new Vector3d();
            raxis.cross(caxis, Yaxis);
            double sina = raxis.length();
            double cosa = Yaxis.dot(caxis);
            if (abs(sina) < EPSILON) {
                //TODO do something smarter 
                raxis = new Vector3d(1, 0, 0);
            }
            raxis.normalize();
            double angle = atan2(sina, cosa);
            this.rotation = new Matrix3d();
            rotation.set(new AxisAngle4d(raxis, -angle));

            return 0;
        }

        public boolean getTriangles(TriangleCollector tc){ 
            double h2 = h/2;
            Vector3d c0 = new Vector3d(0,-h2,0);
            Vector3d c1 = new Vector3d(0,h2,0);            
            Vector3d base0[] = new Vector3d[nfacets];
            Vector3d base1[] = new Vector3d[nfacets];

            double delta = 2*Math.PI/nfacets;

            for(int i = 0; i < nfacets; i++){
                double angle = i * delta;

                double cosa = Math.cos(angle);
                double sina = Math.sin(angle);

                base0[i] = new Vector3d(r0*cosa, -h2, -r0*sina);
                base1[i] = new Vector3d(r1*cosa, h2, -r1*sina);
            }
            for(int i = 0; i < nfacets; i++){
                int i1 = (i+1)%nfacets;

                // bottom                
                addTri(tc,c0,base0[i1],base0[i]);
                // top
                addTri(tc,c1,base1[i],base1[i1]);
                // side 
                if(h > 0) {
                    // has non zero length
                    addTri(tc,base0[i], base0[i1], base1[i1]);                
                    addTri(tc,base0[i], base1[i1], base1[i]);                
                }
            } 
            return true;
        }        
        void addTri(TriangleCollector tc, Vector3d v0, Vector3d v1, Vector3d v2){
            
            // TODO transform triangle
            tv0.set(v0);
            tv2.set(v2);
            tv1.set(v1);
            canonicalTransform(tv0);
            canonicalTransform(tv1);
            canonicalTransform(tv2);

            
            tc.addTri(tv0,tv1,tv2);

        }                

        void canonicalTransform(Vector3d pnt){
            
            rotation.transform(pnt);
            pnt.add(center);
            
        }

    }
} // class TriangulatedModels
