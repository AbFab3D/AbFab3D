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


public class LinearMapper {

    double xmin, xmax, vmin, vmax;
    double scale; 
    
    boolean clamp; 

    public LinearMapper(double xmin, double xmax, double vmin, double vmax){
        this(xmin, xmax, vmin, vmax, false);
    }
    
    public LinearMapper(double xmin, double xmax, double vmin, double vmax, boolean clamp){
        this.clamp = clamp;
        this.xmin = xmin;
        this.xmax = xmax;
        this.vmin = vmin;
        this.vmax = vmax;        
        this.scale = (vmax - vmin)/(xmax - xmin);
    }
    
    public double map(double x){

        if(clamp){
            if(x <= xmin) return vmin;
            if(x >= xmax) return vmax; 
        }

        return (x - xmin)*scale + vmin;

    }
    

    public double getXmin(){
        return xmin;
    }
    public double getXmax(){
        return xmax;
    }
    public double getVmin(){
        return vmin;
    }
    public double getVmax(){
        return vmax;
    }

}