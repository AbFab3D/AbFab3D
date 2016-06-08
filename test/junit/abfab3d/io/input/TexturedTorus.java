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

package abfab3d.io.input;

import abfab3d.util.TriangleProducer2;
import abfab3d.util.TriangleCollector2;
import abfab3d.util.TriangleCollector;
import abfab3d.util.Vec;

import javax.vecmath.Vector3d;

import static java.lang.Math.sin;
import static java.lang.Math.cos;


/**
   test model torus with texture 
   torus is orthogonal to z axis and centered at origin 
   texture coordinates are (x,y) cordinates scaled into (0,1) interval
 */
public class TexturedTorus implements  TriangleProducer2, TriangleCollector2 {
    
    double Rin, Rout;
    int Nin, Nout;
    TriangleCollector tc;
    TexturedTorus(double Rout, double Rin, int Nout, int Nin){
        this.Rin = Rin;
        this.Rout = Rout;
        this.Nin = Nin;
        this.Nout = Nout;
        
    }
    
    /**
       TriangleProducer interface 
    */
    public boolean getTriangles(TriangleCollector tc){
        this.tc = tc;
            getTriangles2(this);
            return true;
        }


        public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2){
            // does nothing. to satisfy TringleCollector
            return true;
        }

        Vector3d // work vectors
            w0 = new Vector3d(),
            w1 = new Vector3d(),
            w2 = new Vector3d();

        /**
           TriangleCollector interface 
        */
        // to convert 
        public boolean addTri2(Vec v0, Vec v1, Vec v2){
            v0.get(w0);
            v1.get(w1);
            v2.get(w2);
            tc.addTri(w0, w1, w2);
            return true;
        }
        

        /**
           TriangleProducer2 interface 
        */
        public boolean getTriangles2(TriangleCollector2 tc){
            Vec 
                p00 = new Vec(6),
                p10 = new Vec(6),
                p01 = new Vec(6),
                p11 = new Vec(6);

            for(int i = 0; i < Nin; i++){

                double u0 = i*2*Math.PI/Nin;
                double u1 = (i+1)*2*Math.PI/Nin;

                for(int j = 0; j < Nout; j++){
                    double v0 = j*2*Math.PI/Nin;
                    double v1 = (j+1)*2*Math.PI/Nin;
                    getPoint(u0,v0,p00);
                    getPoint(u0,v1,p01);
                    getPoint(u1,v0,p10);
                    getPoint(u1,v1,p11);
                    tc.addTri2(p00, p01, p11);
                    tc.addTri2(p00, p11, p10);
                }
            }
            return true;
        }

        /**
           claculatepont on torus surface 
         */
        void getPoint(double u, double v, Vec p){
            double z = Rin*sin(u);
            double r = Rout + Rin*cos(u);
            double x = r*cos(v);
            double y = r*sin(v);
            double tx = (x + (Rin+Rout))/(2*((Rin+Rout)));
            double ty = (y + (Rin+Rout))/(2*((Rin+Rout)));
            double tz = 0; // all texture z are in single plane 
            
            p.v[0] = x;
            p.v[1] = y;
            p.v[2] = z;
            p.v[3] = tx;
            p.v[4] = tx;
            p.v[5] = tz;
            
        }
        
    } // class TexturedTorus 
