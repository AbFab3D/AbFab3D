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

package abfab3d.grid;


public class Bounds {

    double xmin=0, xmax=1., ymin=0., ymax=1., zmin=0., zmax=1.;

    public Bounds(){        
    }

    public Bounds(double sizex, double sizey, double sizez){

        this.xmin = 0;
        this.ymin = 0;
        this.zmin = 0;
        this.xmax = sizex;
        this.ymax = sizey;
        this.zmax = sizez;
    }

    public Bounds(double xmin, double xmax, 
                  double ymin, double ymax, 
                  double zmin, double zmax){ 
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
        this.zmin = zmin;
        this.zmax = zmax;
    }
    /**
       @return width of bounds in voxels 
     */
    public int getWidth(double voxel){
        return roundSize((xmax-xmin)/voxel);
    }

    /**
       @return height of bounds in voxels 
     */
    public int getHeight(double voxel){
        return roundSize((ymax-ymin)/voxel);
    }

    /**
       @return depth of bounds in voxels 
     */
    public int getDepth(double voxel){
        return roundSize((zmax-zmin)/voxel);
    }    

    public static final int roundSize(double s){        
        return (int)(s + 0.5);
    }
}