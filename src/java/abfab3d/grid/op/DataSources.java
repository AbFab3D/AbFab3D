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


import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
//import java.awt.image.Raster;

import java.io.File;
import java.util.Vector;

import javax.imageio.ImageIO;


import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;
import abfab3d.util.ImageMipMapGray16;

import abfab3d.util.ImageUtil;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;

import static abfab3d.util.ImageUtil.getRed;
import static abfab3d.util.ImageUtil.getGreen;
import static abfab3d.util.ImageUtil.getBlue;
import static abfab3d.util.ImageUtil.getAlpha;
import static abfab3d.util.ImageUtil.RED;
import static abfab3d.util.ImageUtil.GREEN;
import static abfab3d.util.ImageUtil.BLUE;
import static abfab3d.util.ImageUtil.ALPHA;

import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.ImageUtil.us2i;

/**
   
   a collection of various DataSource 

   @author Vladimir Bulatov

 */
public class DataSources {
   
    /**

       makes solid block of given size
       
     */
    public static class Block implements DataSource, Initializable {

        private double m_sizeX=0.1, m_sizeY=0.1, m_sizeZ=0.1, m_centerX=0, m_centerY=0, m_centerZ=0;             
        
        private double 
            xmin, 
            xmax,
            ymin,
            ymax,
            zmin, zmax;

        protected boolean
            m_hasSmoothBoundaryX = false,
            m_hasSmoothBoundaryY = false,
            m_hasSmoothBoundaryZ = false;
        
        public Block(){
        }
        public void setSmoothBoundaries(boolean boundaryX,boolean boundaryY,boolean boundaryZ){
            m_hasSmoothBoundaryX = boundaryX;
            m_hasSmoothBoundaryY = boundaryY;
            m_hasSmoothBoundaryZ = boundaryZ;
        }

        /**
           makes block with given center and size 
         */
        public Block(double x, double y, double z, double sx, double sy, double sz){

            setLocation(x,y,z);
            setSize(sx, sy, sz);

        }

        /**
           
         */
        public void setSize(double sx, double sy, double sz){
            m_sizeX = sx;
            m_sizeY = sy;
            m_sizeZ = sz;
        }

        /**
           
         */
        public void setLocation(double x, double y, double z){
            m_centerX = x;
            m_centerY = y;
            m_centerZ = z;
        }

        /**
           
         */
        public int initialize(){
            
            xmin = m_centerX - m_sizeX/2;
            xmax = m_centerX + m_sizeX/2;

            ymin = m_centerY - m_sizeY/2;
            ymax = m_centerY + m_sizeY/2;

            zmin = m_centerZ - m_sizeZ/2;
            zmax = m_centerZ + m_sizeZ/2;

            return RESULT_OK;

        }

        /**
         * returns 1 if pnt is inside of block of given size and location
         * returns 0 otherwise
         */
        public int getDataValue(Vec pnt, Vec data) {

            double res = 1.;
            double 
                x = pnt.v[0],
                y = pnt.v[1],
                z = pnt.v[2];

            double vs = pnt.voxelSize;

            if(vs == 0.){
                // zero voxel size 
                if(x < xmin || x > xmax ||
                   y < ymin || y > ymax ||
                   z < zmin || z > zmax ){
                    data.v[0] = 0.;
                    return RESULT_OK;                
                } else {
                    data.v[0] = 1.;
                    return RESULT_OK;                                    
                }                
            } else {
                
                // finite voxel size 
                
                if(x <= xmin - vs || x >= xmax + vs || 
                   y <= ymin - vs || y >= ymax + vs || 
                   z <= zmin - vs || z >= zmax + vs ){
                    data.v[0] = 0.;
                    return RESULT_OK;                      
                }
                double finalValue = 1;
                
                if(m_hasSmoothBoundaryX)
                    finalValue = Math.min(finalValue, intervalCap(x, xmin, xmax, vs));
                if(m_hasSmoothBoundaryY)
                    finalValue = Math.min(finalValue, intervalCap(y, ymin, ymax, vs));
                if(m_hasSmoothBoundaryZ)
                    finalValue = Math.min(finalValue, intervalCap(z, zmin, zmax, vs));

                data.v[0] = finalValue;
                return RESULT_OK;                      
            }
        }  
        
    }  // class Block 

    /**
       Ball with given location and radius 
     */
    public static class Ball implements DataSource {
        
        private double R, R2, RR;
        
        private double x0, y0, z0;
        
        public Ball(double x0, double y0, double z0, double r){
            R = r;
            R2 = 2*r;
            RR = r*r;
        }
        
        /**
         * returns 1 if pnt is inside of ball
         * returns intepolated value if poiunt is within voxel size to the boundary 
         * returns 0 if pnt is outside the ball
         */
        public int getDataValue(Vec pnt, Vec data) {

            double res = 1.;
            double 
                x = pnt.v[0]-x0,
                y = pnt.v[1]-y0,
                z = pnt.v[2]-z0;

            // good approximation to the distance to the surface of the ball).x 
            double dist = ((x*x + y*y + z*z) - RR)/(R2);
            //double dist = (Math.sqrt(x*x + y*y + z*z) - R);//)/(R2);

            double vs = pnt.voxelSize;
            if(dist <= -vs){
                data.v[0] = 1;
            } else if(dist >= vs){
                data.v[0] = 0;                
            } else {// we are near the surface - return interpolation 
                data.v[0] = interpolate_linear(dist/vs);
                //data.v[0] = interpolate_cubic(dist/vs);
            }
                
            
            return RESULT_OK;
        }                
    }  // class Ball 



    /**
       return 1 if any of input data sources is 1, return 0 if all data sources are 0 
       can be used to make union of few shapes        
     */
    public static class Union implements DataSource, Initializable {

        Vector<DataSource> dataSources = new Vector<DataSource>();
        // fixed vector for calculations 
        DataSource vDataSources[]; 


        public Union(){  

        }

        /**
           add items to set of data sources 
         */
        public void addDataSource(DataSource ds){

            dataSources.add(ds);

        }

        public int initialize(){
            
            vDataSources = (DataSource[])dataSources.toArray(new DataSource[dataSources.size()]);
           
            for(int i = 0; i < vDataSources.length; i++){
                
                DataSource ds = vDataSources[i];
                if(ds instanceof Initializable){
                    ((Initializable)ds).initialize();
                }
            }      
            return RESULT_OK;
            
        }
        

        /**
         * calculates values of all data sources and return maximal value
         * can be used to make union of few shapes 
         */
        public int getDataValue(Vec pnt, Vec data) {

            int len = vDataSources.length;
            DataSource dss[] = vDataSources;

            double value = 0.;

            for(int i = 0; i < len; i++){
                
                DataSource ds = dss[i];
                int res = ds.getDataValue(pnt, data);                

                if(res != RESULT_OK){
                    // outside of domain 
                    continue;
                }
                double v = data.v[0];
                if(v >= 1.){
                    data.v[0] = 1;
                    return RESULT_OK;                                        
                }

                //value += v *(1 - value); //1-(1-value)*(1-v);
                value += v;
                if(value > 1) value = 1;

                //if( v > value) value = v;
            }

            data.v[0] = value; 

            return RESULT_OK;
        }        

    } // class Union


    /**
       does boolean complement 
     */
    public static class Complement implements DataSource, Initializable {

        DataSource dataSource = null;
        
        public Complement(){  

        }

        /**
           add items to set of data sources 
         */
        public void setDataSource(DataSource ds){

            dataSource = ds;

        }

        public int initialize(){
            
            if(dataSource instanceof Initializable){
                ((Initializable)dataSource).initialize();
            }
            
            return RESULT_OK;
            
        }
        

        /**
         * calculates complement of given data 
           replaces 1 to 0 and 0 to 1
         */
        public int getDataValue(Vec pnt, Vec data) {

            int res = dataSource.getDataValue(pnt, data);
            if(res != RESULT_OK){
                data.v[0] = 1;
                return res;
            } else {
                // we have good result
                // do complement 
                data.v[0] = 1-data.v[0];            
                return RESULT_OK;
            }        
        }
    } // class Complement

    
    /**
       Intersection of multiple data sourrces 
       return 1 if all data sources return 1
       return 0 otherwise
     */
    public static class Intersection implements DataSource, Initializable {

        Vector<DataSource> dataSources = new Vector<DataSource>();
        // fixed vector for calculations 
        DataSource vDataSources[];

        public Intersection(){  

        }

        /**
           add items to set of data sources 
         */
        public void addDataSource(DataSource ds){

            dataSources.add(ds);

        }

        public int initialize(){

            vDataSources = (DataSource[])dataSources.toArray(new DataSource[dataSources.size()]);
           
            for(int i = 0; i < vDataSources.length; i++){
                
                DataSource ds = vDataSources[i];
                if(ds instanceof Initializable){
                    ((Initializable)ds).initialize();
                }
            }      
            return RESULT_OK;
            
        }
        

        /**
         * calculates intersection of all values
         * 
         */
        public int getDataValue(Vec pnt, Vec data) {
            
            DataSource dss[] = vDataSources;
            int len = dss.length;

            double value = 1;

            for(int i = 0; i < len; i++){
                
                DataSource ds = dss[i];
                //int res = ds.getDataValue(pnt, workPnt);
                int res = ds.getDataValue(pnt, data);
                if(res != RESULT_OK){
                    data.v[0] = 0.;
                    return res;
                }

                double v = data.v[0];

                if(v <= 0.){
                    data.v[0] = 0;
                    return RESULT_OK;                    
                }
                //value *= v;

                if(v < value)
                    value = v;
                    
            }

            data.v[0] = value; 
            return RESULT_OK;
        }        

    } // class Intersection


    /**
       subtracts (dataSource1 - dataSource2)       
       can be used for boolean difference 
     */
    public static class Subtraction implements DataSource, Initializable {

        DataSource dataSource1;
        DataSource dataSource2;
        
        public Subtraction(){     

        }

        public void setDataSources(DataSource ds1, DataSource ds2){

            dataSource1 = ds1;
            dataSource2 = ds2;

        }
        

        public int initialize(){

            if(dataSource1 != null && dataSource1 instanceof Initializable){
                ((Initializable)dataSource1).initialize();
            }
            if(dataSource2 != null && dataSource2 instanceof Initializable){
                ((Initializable)dataSource2).initialize();
            }
            return RESULT_OK;
            
        }
        
        /**
         * calculates values of all data sources and return maximal value
         * can be used to make union of few shapes 
         */
        public int getDataValue(Vec pnt, Vec data) {
            
            double v1 = 0, v2 = 0;

            int res = dataSource1.getDataValue(pnt, data);
            if(res != RESULT_OK){
                data.v[0] = 0.0;
                return res;
            }

            v1 = data.v[0];

            if(v1 <= 0.){
                data.v[0] = 0.0;
                return RESULT_OK;
            }

            // we are here if v1 > 0
            
            res = dataSource2.getDataValue(pnt, data);

            if(res != RESULT_OK){
                data.v[0] = v1;
                return res;
            }

            v2 = data.v[0];            
            if(v2 >= 1.){
                data.v[0] = 0.;
                return RESULT_OK;
            } 
            //TODO better calculation 
            data.v[0] = v1*(1-v2);

            return RESULT_OK;
        }        

    } // class Subtraction

    
    /**
       class to accept generic DataSource and VecTransform 
       
       in getDataValue() it applued inverse_transform to the point and calcylates data value in 
       transformed point
       
     */
    public static class DataTransformer implements DataSource, Initializable {
        
        protected DataSource dataSource;
        protected VecTransform transform;
        
        public DataTransformer(){  
        }

        public void setDataSource(DataSource ds){
            dataSource = ds;
        }

        public void setTransform(VecTransform vt){
            transform = vt;
        }

        public int initialize(){
            
            if(dataSource != null && dataSource instanceof Initializable){
                ((Initializable)dataSource).initialize();
            }
            if(transform != null && transform instanceof Initializable){
                ((Initializable)transform).initialize();
            }               
            return RESULT_OK;            
        }
        

        /**
         * 
         * 
         */
        public int getDataValue(Vec pnt, Vec data) {

            // TODO - garbage generation
            Vec workPnt = new Vec(pnt);
            
            if(transform != null){
                int res = transform.inverse_transform(pnt, workPnt);
                if(res != RESULT_OK){
                    data.v[0] = 0;                    
                    return res;
                }
            } 
           
            if(dataSource != null){
                return dataSource.getDataValue(workPnt, data);
            } else {
                data.v[0] = 1.;
                return RESULT_OK;
            }
        }        

    } // class DataTransformer


    /**
       ring in XZ plane of given radius, width and thickness 
       
     */
    public static class Ring implements DataSource{

        double ymin, ymax;
        double innerRadius2;
        double innerRadius;
        double exteriorRadius;        
        double exteriorRadius2;
        
        public Ring(double innerRadius, double thickness, double ymin, double ymax){  
            
            this.ymin = ymin; 
            this.ymax = ymax; 
            
            this.innerRadius = innerRadius;
            this.exteriorRadius = innerRadius + thickness;

            this.innerRadius2 = innerRadius*innerRadius;

            this.exteriorRadius2 = exteriorRadius*exteriorRadius;
            
        }

        public Ring(double innerRadius, double thickeness, double width){  

            this(innerRadius, thickeness, -width/2, width/2);            
        }


        /**
         * calculates values of all data sources and return maximal value
         * can be used to make union of few shapes 
         */
        public int getDataValue(Vec pnt, Vec data) {

            double y = pnt.v[1];
            double vs = pnt.voxelSize;
            //double w2 = width2 + vs;

            double yvalue = 1.;

            if(y < ymin-vs || y > ymax+vs){

                data.v[0] = 0;
                return RESULT_OK;

            } else if(y < (ymin + vs)){
                // interpolate lower rim 
                
                yvalue = (y - (ymin - vs))/(2*vs);

            } else if(y > (ymax - vs)){

                // interpolate upper rim 
                yvalue = ((ymax + vs)-y)/(2*vs);                

            } 
                        
            
            double x = pnt.v[0];
            double z = pnt.v[2];
            double r = Math.sqrt(x*x + z*z);

            double rvalue = 1;
            if(r < (innerRadius-vs) || r > (exteriorRadius+vs)){
                data.v[0] = 0;
                return RESULT_OK;                

            } else if(r < (innerRadius+vs)){
                // interpolate interior surface 
                rvalue = (r-(innerRadius-vs))/(2*vs);
                
            } else if(r > (exteriorRadius - vs)){

                rvalue = ((exteriorRadius + vs) - r)/(2*vs);
                // interpolate exterior surface 
                
            } 
            
            //data.v[0] = (rvalue < yvalue)? rvalue : yvalue;
            if(rvalue < yvalue)
                data.v[0] = rvalue;
            else 
                data.v[0] = yvalue;
            
            return RESULT_OK;             
        }        

    } // class Ring


    // linear intepolation 
    // x < -1 return 1;
    // x >  1 returns 0    
    public static final double interpolate_linear(double x){

        return 0.5*(1 - x);

    }

    /**
       x < 0 return 0
       x > 1 return 1
       return x inside (0.,1.)

    1                          _____________________
                              /
                             /
                            /
                           /
     0 ___________________/

                         0     1
     */
    public static final double step(double x){
        if(x < 0.)    
            return 0.;
        else if( x > 1.)
            return 1.;
        else 
            return x;
    }

    /*
      step from 0 to 1
      
    1                          _____________________
                              /
                             /
                            .
                           /.
     0 ___________________/ .

                            x0
     */
    public static final double step01(double x, double x0, double vs){
        
        if(x <= x0 - vs)
            return 0.;

        if(x >= x0 + vs)
            return 1.;
        
        return (x-(x0-vs))/(2*vs);

    }

    /*
      step from 1 to 0
      
    1     _________
                   \
                    \
                     .
                      \
     0               . \_______________

                     x0
    */
    public static final double step10(double x, double x0, double vs){
        
        if(x <= x0 - vs)
            return 1.;

        if(x >= x0 + vs)
            return 0.;
        
        return ((x0+vs)-x)/(2*vs);

    }

    /*
    1                          _________
                              /         \
                             /           \
                            .             .
                           /               \
     0 ___________________/ .             . \_______________

                           xmin          xmax
    
    
       return 1 inside of interval and 0 outside of intervale with linear transition at the boundaries
     */
    public static final double intervalCap(double x, double xmin, double xmax, double vs){
        
        if(xmin >= xmax-vs)
            return 0;
        
        double vs2 = vs*2;
        double vxi = step((x-(xmin-vs))/(vs2));
        double vxa = step(((xmax+vs)-x)/(vs2));
        
        return vxi*vxa;

    }
    
    // linear intepolation 
    // x < -1 return 1;
    // x >  1 returns 0    
    // smoth cubic polynom between 
    public static final double interpolate_cubic(double x){

        return 0.25*x*(x*x - 3.) + 0.5;
        
    }
    
    public final static double getBox(double x, double y, double z, 
                               double xmin, double xmax, 
                               double ymin, double ymax, 
                               double zmin, double zmax, 
                               double vs){
        
        if(xmin >= xmax || ymin >= ymax || zmin >= zmax ){
            // empty box 
            return 0.;
        }
                
        double vs2 = 2*vs; 
        double vxi = step((x-(xmin-vs))/(vs2));
        double vxa = step(((xmax+vs)-x)/(vs2));
        double vyi = step((y-(ymin-vs))/(vs2));
        double vya = step(((ymax+vs)-y)/(vs2));
        double vzi = step((z-(zmin-vs))/(vs2));
        double vza = step(((zmax+vs)-z)/(vs2));
            
        vxi *= vxa;
        vyi *= vya;
        vzi *= vza;
        
        return vxi*vyi*vzi;
    }
        
}

