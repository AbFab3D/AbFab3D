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

import javax.vecmath.Vector3d;

import static abfab3d.util.Output.fmt;

public class Bounds implements Cloneable {

    public double xmin=0, xmax=1., ymin=0., ymax=1., zmin=0., zmax=1.;
    public int nx = 1, ny = 1, nz = 1;

    public Bounds(){        
    }

    public Bounds(double bounds[]){ 
        this.xmin = bounds[0];
        this.xmax = bounds[1];
        this.ymin = bounds[2];
        this.ymax = bounds[3];
        this.zmin = bounds[4];
        this.zmax = bounds[5];    
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

    public void setGridSize(int width,int height, int depth){
        this.nx = width;
        this.ny = height;
        this.nz = depth;
    }

    public static final int roundSize(double s){        
        return (int)(s + 0.5);
    }

    public void set(Bounds bounds) {
        this.xmin = bounds.xmin;
        this.xmax = bounds.xmax;
        this.ymin = bounds.ymin;
        this.ymax = bounds.ymax;
        this.zmin = bounds.zmin;
        this.zmax = bounds.zmax;
    }

    public Vector3d[] getCorners(){

        Vector3d v[] = new Vector3d[8];
        int c = 0;
        v[c++] = new Vector3d(xmin,ymin,zmin);
        v[c++] = new Vector3d(xmax,ymin,zmin);
        v[c++] = new Vector3d(xmin,ymax,zmin);
        v[c++] = new Vector3d(xmax,ymax,zmin);
        v[c++] = new Vector3d(xmin,ymin,zmax);
        v[c++] = new Vector3d(xmax,ymin,zmax);
        v[c++] = new Vector3d(xmin,ymax,zmax);
        v[c++] = new Vector3d(xmax,ymax,zmax);
        return v;
    }

    /**
       conversion of world cordinates to grid coordinates 
     */
    public void toGridCoord(Vector3d pnt){

        pnt.x = nx*(pnt.x - xmin)/(xmax - xmin);
        pnt.y = ny*(pnt.y - ymin)/(ymax - ymin);
        pnt.z = nz*(pnt.z - zmin)/(zmax - zmin);
        
    }

    /**
       conversion of grid cordinates to world coordinates 
     */
    public void toWorldCoord(Vector3d pnt){

        pnt.x = pnt.x*(xmax-xmin)/nx + xmin;
        pnt.y = pnt.y*(ymax-ymin)/ny + ymin;
        pnt.z = pnt.z*(zmax-zmin)/nz + zmin;
        
    }

    public String toString(){
        return fmt("%9.7f %9.7f %9.7f %9.7f %9.7f %9.7f",xmin, xmax, ymin, ymax, zmin, zmax);
    }

    public Bounds clone() {

        try {
            //Shallow copy is good
            return (Bounds) super.clone();
        } catch(CloneNotSupportedException cnse) {
            cnse.printStackTrace();
        }

        return null;
    }
}