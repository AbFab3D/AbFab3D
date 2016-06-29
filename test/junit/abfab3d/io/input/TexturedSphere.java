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

import abfab3d.core.AttributedTriangleProducer;
import abfab3d.core.AttributedTriangleCollector;
import abfab3d.core.TriangleCollector;
import abfab3d.core.Vec;

import static java.lang.Math.sin;
import static java.lang.Math.cos;


/**
   test model sphere with texture 
   texture coordinates are (x,y) cordinates scaled into (0,1) interval
 */
public class TexturedSphere implements  AttributedTriangleProducer{
    
    double R;
    int Nphi, Ntheta;
    TriangleCollector tc;
    TexturedSphere(double R, int Nphi, int Ntheta){
        this.R = R;
        this.Nphi = Nphi;
        this.Ntheta = Ntheta;        
    }
    
    public int getDataDimension() {
        // may be 5 or 6 
        return 6; 
    }

    
    /**
       TriangleProducer2 interface 
    */
    public boolean getAttTriangles(AttributedTriangleCollector tc){
        Vec 
            p00 = new Vec(6),
            p10 = new Vec(6),
            p01 = new Vec(6),
            p11 = new Vec(6);
        
        for(int i = 0; i < Ntheta; i++){
            
            double u0 = i*Math.PI/Ntheta;
            double u1 = (i+1)*Math.PI/Ntheta;
            
            for(int j = 0; j < Nphi; j++){
                double v0 = j*2*Math.PI/Nphi;
                double v1 = (j+1)*2*Math.PI/Nphi;
                getPoint(u0,v0,p00);
                getPoint(u0,v1,p01);
                getPoint(u1,v0,p10);
                getPoint(u1,v1,p11);
                tc.addAttTri(p00, p01, p11);
                tc.addAttTri(p00, p11, p10);
            }
        }
        return true;
    }
    
    /**
       calculates point on torus surface 
    */
    void getPoint(double u, double v, Vec p){

        double z = R*cos(u);
        double r = R*sin(u);
        double x = r*cos(v);
        double y = r*sin(v);

        // assign texture values in (0,1) 
        double tx = (x + R)/(2*R);
        double ty = (y + R)/(2*R);
        double tz = 0; // single texture case 
        
        p.v[0] = x;
        p.v[1] = y;
        p.v[2] = z;
        p.v[3] = tx;
        p.v[4] = ty;
        p.v[5] = tz;
        
    }
    
} // class TexturedTorus 
