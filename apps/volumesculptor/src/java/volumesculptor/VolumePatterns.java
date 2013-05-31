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

package volumesculptor;


import abfab3d.util.DataSource;
import abfab3d.util.Vec;


import static java.lang.Math.floor;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static abfab3d.grid.op.DataSources.step10;
import static abfab3d.util.Output.printf;


public class VolumePatterns {


    
    public static class Balls implements DataSource {
        

        int debugCount = 100;

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
            double vs = pnt.voxelSize;
            
            x -= period*floor(x/period);
            y -= period*floor(y/period);
            z -= period*floor(z/period);
            
            x -= period/2;
            y -= period/2;
            z -= period/2;
            
            double dist = ((x*x + y*y + z*z) - RR)/R2;
            data.v[0] = step10(dist, 0,(pnt.voxelSize));

            if(debugCount-- > 0)
                printf("(%10.5f %10.5f %10.5f) -> %10.5f\n", x,y,z,data.v[0]);

            return RESULT_OK;
        }
        
    }

    public static class CubicGrid implements DataSource {
        

        int debugCount = 100;

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
            double vs = pnt.voxelSize;
            
            x -= period*floor(x/period);
            y -= period*floor(y/period);
            z -= period*floor(z/period);
            
            x -= period/2;
            y -= period/2;
            z -= period/2;
            
            double dxy = step10(((x*x + y*y) - RR)/R2, 0, (pnt.voxelSize));
            double dyz = step10(((y*y + z*z) - RR)/R2, 0, (pnt.voxelSize));
            double dzx = step10(((z*z + x*x) - RR)/R2, 0, (pnt.voxelSize));

            double d = 0;
            if( dxy > d) d = dxy;
            if( dyz > d) d = dyz;
            if( dzx > d) d = dzx;

            data.v[0] = d;

            return RESULT_OK;
        }
        
    }

    public static class Gyroid implements DataSource {
        

        int debugCount = 100;

        double period;
        double thickness;

        public Gyroid(double period, double thickness){

            this.period = period;
            this.thickness = thickness;


        }

        public int getDataValue(Vec pnt, Vec data){
            
            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];
            
            x *= 2*PI/period;
            y *= 2*PI/period;
            z *= 2*PI/period;
            
            double d = abs((sin(x)*cos(y) + sin(y)*cos(z) + sin(z) * cos(x))*(period/(2*PI))) - thickness;
            
            data.v[0] = step10(d, 0, (pnt.voxelSize));

            return RESULT_OK;
        }
        
    }

    public static class Lidinoid implements DataSource {


        double period;
        double thickness;

        public Lidinoid(double period, double thickness){

            this.period = period;
            this.thickness = thickness;


        }

        public int getDataValue(Vec pnt, Vec data){

            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];

            x *= 2*PI/period;
            y *= 2*PI/period;
            z *= 2*PI/period;

            double d = 0.5 * (sin(2*x) * cos(y) * sin(z) + sin(2*y) * cos(z) * sin(z) + sin(2*z)*cos(x) * sin(y)) -
                    0.5 * (cos(2*x) * cos(2*y) + cos(2*y) * cos(2*z) + cos(2*z) * cos(2*x)) + 0.15 - thickness;
            data.v[0] = step10(d, 0, (pnt.voxelSize));

            return RESULT_OK;
        }

    }

    /**
     * Schwarz Primitive as defined here: http://en.wikipedia.org/wiki/Schwarz_minimal_surface#Schwarz_P_.28.22Primitive.22.29
     *
     * @author Alan Hudson
     */
    public static class SchwarzPrimitive implements DataSource {


        double period;
        double thickness;

        public SchwarzPrimitive(double period, double thickness){

            this.period = period;
            this.thickness = thickness;
        }

        public int getDataValue(Vec pnt, Vec data){

            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];

            x *= 2*PI/period;
            y *= 2*PI/period;
            z *= 2*PI/period;

            double d = cos(x) + cos(y) + cos(z) - thickness;

            data.v[0] = step10(d, 0, (pnt.voxelSize));

            return RESULT_OK;
        }
    }

    /**
     * Schwarz Diamond as defined here: http://en.wikipedia.org/wiki/Schwarz_minimal_surface#Schwarz_P_.28.22Primitive.22.29
     *
     * @author Alan Hudson
     */
    public static class SchwarzDiamond implements DataSource {


        double period;
        double thickness;

        public SchwarzDiamond(double period, double thickness){

            this.period = period;
            this.thickness = thickness;
        }

        public int getDataValue(Vec pnt, Vec data){

            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];

            x *= 2*PI/period;
            y *= 2*PI/period;
            z *= 2*PI/period;

            double d = sin(x) * sin(y) * sin(z) + sin(x) * cos(y) * cos(z) + cos(x) * sin(x) * cos(z) + cos(x) * cos(y) * sin(z) - thickness;

            data.v[0] = step10(d, 0, (pnt.voxelSize));

            return RESULT_OK;
        }
    }

}
