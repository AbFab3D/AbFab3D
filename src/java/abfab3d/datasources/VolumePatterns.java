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


import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.Vec;


import javax.vecmath.Vector3d;

import static java.lang.Math.floor;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.sinh;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import static abfab3d.util.MathUtil.step10;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Units.MM;


public class VolumePatterns {

    static final boolean DEBUG = false;
    static int debugCount = 100;
    
    public static class Balls  extends TransformableDataSource{
        

        double period;
        double RR, R2;

        public Balls(double period, double radius){

            this.period = period;
            
            RR = radius*radius;
            
            R2 = radius*2;

        }

        public int getDataValue(Vec pnt, Vec data){
            super.transform(pnt);

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

            super.getMaterialDataValue(pnt, data);

            return RESULT_OK;
        }
        
    }

    public static class CubicGrid  extends TransformableDataSource{
        


        double period;
        double RR, R2;

        public CubicGrid(double period, double radius){

            this.period = period;
            
            RR = radius*radius;
            
            R2 = radius*2;

        }

        public int getDataValue(Vec pnt, Vec data){
            
            super.transform(pnt);
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

            super.getMaterialDataValue(pnt, data);

            return RESULT_OK;
        }
        
    } // class CubicGrid


    /**
       approximation to Gyroid 
    */
    public static class Gyroid  extends TransformableDataSource{
        

        private double period = 10*MM;
        private double thickness = 0.1*MM;
        private double level = 0;
        private double offsetX = 0,offsetY = 0,offsetZ = 0;

        private double factor = 0;
        private double voxelScale = 1;

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

        public void setVoxelScale(double voxelScale) {
            this.voxelScale = voxelScale;
        }

        public void setOffset(double offsetX, double offsetY,double offsetZ){
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }
        
        public int initialize(){
            super.initialize();
            this.factor = 2*PI/period;

            return RESULT_OK;
        }

        public int getDataValue(Vec pnt, Vec data){
            
            super.transform(pnt);
            double x = pnt.v[0]-offsetX;
            double y = pnt.v[1]-offsetY;
            double z = pnt.v[2]-offsetZ;
            
            x *= factor;
            y *= factor;
            z *= factor;
            double vs = pnt.getScaledVoxelSize();
            
            double d = abs(( sin(x)*cos(y) + sin(y)*cos(z) + sin(z) * cos(x) - level)/factor) - (thickness + voxelScale*vs);
            
            data.v[0] = step10(d, 0, vs);

            super.getMaterialDataValue(pnt, data);

            return RESULT_OK;
        }
        
    }

    /**
     approximation to Gyroid
     */
    public static class GyroidGradient extends TransformableDataSource{


        private double period = 10*MM;
        private double thickness = 0.1*MM;
        private double level = 0;
        private double offsetX = 0,offsetY = 0,offsetZ = 0;

        private double factor = 0;
        private Vector3d pos;
        private double str;
        private double lengthSq;

        // Scratch var
        private Vector3d v;

        public GyroidGradient(){

        }

        public GyroidGradient(double period, double thickness, Vector3d pos, double str){

            this.period = period;
            this.thickness = thickness;
            this.pos = pos;
            this.str = str;

            v = new Vector3d();
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

            super.initialize();
            this.factor = 2*PI/period;
            double l = pos.length();
            this.lengthSq = l*l * factor;

            return RESULT_OK;
        }

        int cnt = 0;
        public int getDataValue(Vec pnt, Vec data){

            super.transform(pnt);
            double x = pnt.v[0]-offsetX;
            double y = pnt.v[1]-offsetY;
            double z = pnt.v[2]-offsetZ;

            x *= factor;
            y *= factor;
            z *= factor;
            double vs = pnt.getScaledVoxelSize();

            //TODO: remove garbage
            v.x = x;
            v.y = y;
            v.z = z;
            double l = pos.dot(v) / lengthSq;

            double w = sqrt(1.0 + str * (l*l));

            double d = abs(( sin(w*x)*cos(w*y) + sin(w*y)*cos(w*z) + sin(w*z) * cos(w*x) - level)/factor) - (thickness*w + vs);

            data.v[0] = step10(d, 0, vs);

            super.getMaterialDataValue(pnt, data);

            return RESULT_OK;
        }

    }

    public static class Lidinoid extends TransformableDataSource{


        double period;
        double thickness;

        public Lidinoid(double period, double thickness){

            this.period = period;
            this.thickness = thickness;


        }

        public int getDataValue(Vec pnt, Vec data){

            super.transform(pnt);
            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];

            x *= 2*PI/period;
            y *= 2*PI/period;
            z *= 2*PI/period;

            double vs = pnt.getScaledVoxelSize();

            double d = 0.5 * (sin(2*x) * cos(y) * sin(z) + sin(2*y) * cos(z) * sin(z) + sin(2*z)*cos(x) * sin(y)) -
                    0.5 * (cos(2*x) * cos(2*y) + cos(2*y) * cos(2*z) + cos(2*z) * cos(2*x)) + 0.15 - thickness;
            data.v[0] = step10(d, 0, (vs));

            super.getMaterialDataValue(pnt, data);
            return RESULT_OK;
        }

    }

    /**
     * Schwarz Primitive as defined here: http://en.wikipedia.org/wiki/Schwarz_minimal_surface#Schwarz_P_.28.22Primitive.22.29
     *
     * @author Alan Hudson
     */
    public static class SchwarzPrimitive extends TransformableDataSource{


        double period;
        double thickness;

        public SchwarzPrimitive(double period, double thickness){

            this.period = period;
            this.thickness = thickness;
        }

        public int getDataValue(Vec pnt, Vec data){

            super.transform(pnt);
            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];

            x *= 2*PI/period;
            y *= 2*PI/period;
            z *= 2*PI/period;

            double vs = pnt.getScaledVoxelSize();
            double d = cos(x) + cos(y) + cos(z) - thickness;

            data.v[0] = step10(d, 0, (vs));
            super.getMaterialDataValue(pnt, data);

            return RESULT_OK;
        }
    }

    /**
     * Schwarz Diamond as defined here: http://en.wikipedia.org/wiki/Schwarz_minimal_surface#Schwarz_P_.28.22Primitive.22.29
     *
     * @author Alan Hudson
     */
    public static class SchwarzDiamond extends TransformableDataSource {


        double period;
        double thickness;
        private double factor = 0;
        private double voxelScale = 1;

        public SchwarzDiamond(double period, double thickness){

            this.period = period;
            this.thickness = thickness;
            voxelScale = 1;
        }

        public void setVoxelScale(double voxelScale) {
            this.voxelScale = voxelScale;
        }

        public int initialize(){

            super.initialize();
            this.factor = 2*PI/period;

            return RESULT_OK;
        }

        public int getDataValue(Vec pnt, Vec data){

            super.transform(pnt);
            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];

            x *= factor;
            y *= factor;
            z *= factor;

            double vs = pnt.getScaledVoxelSize();
            double d = sin(x) * sin(y) * sin(z) + sin(x) * cos(y) * cos(z) + cos(x) * sin(x) * cos(z) + cos(x) * cos(y) * sin(z) - (thickness + voxelScale * vs);

            data.v[0] = step10(d, 0, (vs));
            super.getMaterialDataValue(pnt, data);

            return RESULT_OK;
        }
    }

    /**
     * Scherk Second Surface as defined here: http://en.wikipedia.org/wiki/Scherk_surface
     *
     * @author Alan Hudson
     */
    public static class ScherkSecondSurface extends TransformableDataSource {


        double period;
        double thickness;

        public ScherkSecondSurface(double period, double thickness){

            this.period = period;
            this.thickness = thickness;
        }

        public int getDataValue(Vec pnt, Vec data){

            super.transform(pnt);
            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];

            x *= 2*PI/period;
            y *= 2*PI/period;
            z *= 2*PI/period;

            double d = sin(z) - sinh(x)*sinh(y) - thickness;
            double vs = pnt.getScaledVoxelSize();

            data.v[0] = step10(d, 0, (vs));

            super.getMaterialDataValue(pnt, data);

            return RESULT_OK;
        }
    }

    /**
     * Enneper Surface as describe here:  http://en.wikipedia.org/wiki/Enneper_surface
     *
     * @author Alan Hudson
     */
    public static class Enneper extends TransformableDataSource{


        double period;
        double thickness;

        public Enneper(double period, double thickness){

            this.period = period;
            this.thickness = thickness;
        }

        public int getDataValue(Vec pnt, Vec data){

            super.transform(pnt);
            double x = pnt.v[0];
            double y = pnt.v[1];
            double z = pnt.v[2];

            x *= 2*PI/period;
            y *= 2*PI/period;
            z *= 2*PI/period;

            double d = 64 * pow(z,9) - 128*pow(z,7) + 64 * pow(z,5) - 702 * x*x*y*y*z*z*z -
                    18 * x*x *y*y*z + 144*(y*y * pow(z,6) - x*x*pow(z,6)) + 162 * (pow(y,4) * z*z - pow(x,4)*z*z) +
                    27 * (pow(y,6) - pow(x,6)) + 9 * (pow(x,4) * z + pow(y,4) * z) + 48*(x*x*pow(z,3) + y*y * pow(z,3)) -
                    432 * (x*x*pow(z,5) + y*y*pow(z,5)) + 81 * (pow(x,4)*y*y - x*x*pow(x,4)) + 240 * (y*y*pow(z,4) -x*x+pow(z,4))
                    -135 *(pow(x,4)*pow(z,3) + pow(y,4)*pow(z,3)) - thickness;
            double vs = pnt.getScaledVoxelSize();

            data.v[0] = step10(d, 0, (vs));

            super.getMaterialDataValue(pnt, data);

            return RESULT_OK;
        }
    }

}
