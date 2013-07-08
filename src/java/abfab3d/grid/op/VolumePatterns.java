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


import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.Vec;


import static java.lang.Math.floor;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static abfab3d.grid.op.DataSources.step10;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Units.MM;


public class VolumePatterns {

    static final boolean DEBUG = false;
    static int debugCount = 100;
    
    public static class Balls implements DataSource {
        

        double period;
        double RR, R2;

        public Balls(double period, double radius){

            this.period = period;
            
            RR = radius*radius;
            
            R2 = radius*2;

        }

        public int getDataValue(Vec pnt, Vec data){
            
            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];
            double vs = pnt.getScaledVoxelSize();
            
            x -= period*floor(x/period);
            y -= period*floor(y/period);
            z -= period*floor(z/period);
            
            x -= period/2;
            y -= period/2;
            z -= period/2;
            
            double dist = ((x*x + y*y + z*z) - RR)/R2;
            data.v[0] = step10(dist, 0,vs);

            if(DEBUG && debugCount-- > 0)
                printf("(%10.5f %10.5f %10.5f) -> %10.5f\n", x,y,z,data.v[0]);

            return RESULT_OK;
        }
        
    }

    public static class CubicGrid implements DataSource {
        


        double period;
        double RR, R2;

        public CubicGrid(double period, double radius){

            this.period = period;
            
            RR = radius*radius;
            
            R2 = radius*2;

        }

        public int getDataValue(Vec pnt, Vec data){
            
            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];
            double vs = pnt.getScaledVoxelSize();
            
            x -= period*floor(x/period);
            y -= period*floor(y/period);
            z -= period*floor(z/period);
            
            x -= period/2;
            y -= period/2;
            z -= period/2;
            
            double dxy = step10(((x*x + y*y) - RR)/R2, 0, vs);
            double dyz = step10(((y*y + z*z) - RR)/R2, 0, vs);
            double dzx = step10(((z*z + x*x) - RR)/R2, 0, vs);

            double d = 0;
            if( dxy > d) d = dxy;
            if( dyz > d) d = dyz;
            if( dzx > d) d = dzx;

            data.v[0] = d;

            return RESULT_OK;
        }
        
    } // class CubicGrid


    /**
       approximation to Gyroid 
    */
    public static class Gyroid implements DataSource, Initializable {
        

        private double period = 10*MM;
        private double thickness = 0.1*MM;
        private double level = 0;
        private double offsetX = 0,offsetY = 0,offsetZ = 0;

        private double factor = 0;

        public Gyroid(){
            
        }

        public Gyroid(double period, double thickness){

            this.period = period;
            this.thickness = thickness;
        }
        
        public void setPeriod(double value){
            this.period = value; 
        }
        public void setThickness(double value){
            this.thickness = value;
        }
        public void setLevel(double value){
            this.level = value;
        }

        public void setOffset(double offsetX, double offsetY,double offsetZ){
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }
        
        public int initialize(){

            this.factor = 2*PI/period;

            return RESULT_OK;
        }

        public int getDataValue(Vec pnt, Vec data){
            
            double x = pnt.v[0]-offsetX;
            double y = pnt.v[1]-offsetY;
            double z = pnt.v[2]-offsetZ;
            
            x *= factor;
            y *= factor;
            z *= factor;
            double vs = pnt.getScaledVoxelSize();
            
            double d = abs(( sin(x)*cos(y) + sin(y)*cos(z) + sin(z) * cos(x) - level)/factor) - (thickness + vs);
            
            data.v[0] = step10(d, 0, vs);

            return RESULT_OK;
        }
        
    }

   
}
