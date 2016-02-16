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

package abfab3d.util;

import javax.vecmath.Vector3d;

import static abfab3d.util.Output.fmt;
import static abfab3d.util.Units.MM;

/**
 * Axis aligned bounds object.
 *
 * @author Alan Hudson
 */
public class Bounds implements Cloneable {

    public static final Bounds INFINITE = new Bounds(-1000,1000,-1000,1000,-1000,1000);

    public double xmin=0, xmax=1., ymin=0., ymax=1., zmin=0., zmax=1.;

    protected double m_voxelSize = 0.1*MM;

    // size of grid with given voxel size;
    // it is updated if other bpounds or voxel size are changed 
    protected int nx = 1, ny = 1, nz = 1;


    public Bounds(){        
    }

    public Bounds(Bounds bounds){  

        this.xmin = bounds.xmin;
        this.xmax = bounds.xmax;
        this.ymin = bounds.ymin;
        this.ymax = bounds.ymax;
        this.zmin = bounds.zmin;
        this.zmax = bounds.zmax;
        this.nx = bounds.nx;
        this.ny = bounds.ny;
        this.nz = bounds.nz;
        this.m_voxelSize = bounds.m_voxelSize;
        init();

    }

    public Bounds(double bounds[]){ 
        this.xmin = bounds[0];
        this.xmax = bounds[1];
        this.ymin = bounds[2];
        this.ymax = bounds[3];
        this.zmin = bounds[4];
        this.zmax = bounds[5];    
        init();
    }

    public Bounds(double sizex, double sizey, double sizez){

        this.xmin = 0;
        this.ymin = 0;
        this.zmin = 0;
        this.xmax = sizex;
        this.ymax = sizey;
        this.zmax = sizez;
        init();
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
        init();
    }

    public Bounds(double xmin, double xmax, 
                  double ymin, double ymax, 
                  double zmin, double zmax, double voxelSize){ 
        this.xmin = xmin;
        this.xmax = xmax;
        this.ymin = ymin;
        this.ymax = ymax;
        this.zmin = zmin;
        this.zmax = zmax;
        m_voxelSize = voxelSize;
        init();
    }

    public Bounds(Vector3d center, Vector3d size) {
        this.xmin = center.x - size.x / 2;
        this.xmax = center.x + size.x / 2;
        this.ymin = center.y - size.y / 2;
        this.ymax = center.y + size.y / 2;
        this.zmin = center.z - size.z / 2;
        this.zmax = center.z + size.z / 2;
    }

    /**
     * Returns volume of the bounds box in m^3
     */
    public double getVolume(){

        return (xmax-xmin)*(ymax -ymin)*(zmax-zmin);

    }


    public int getGridWidth(){
        return roundSize((xmax-xmin)/m_voxelSize);
    }

    public int getGridHeight(){
        return roundSize((ymax-ymin)/m_voxelSize);
    }

    public int getGridDepth(){
        return roundSize((zmax-zmin)/m_voxelSize);
    }

    public int[] getGridSize(){

        return new int[]{
            getGridWidth(),
            getGridHeight(),
            getGridDepth()
        };

    }

    public double getVoxelSize(){
        return m_voxelSize;
    }

    /**
       @return width of bounds in voxels 
     */
    public int getWidthVoxels(double voxelSize){
        return roundSize((xmax-xmin)/voxelSize);
    }

    /**
       @return height of bounds in voxels 
     */
    public int getHeightVoxels(double voxelSize){
        return roundSize((ymax-ymin)/voxelSize);
    }

    /**
       @return depth of bounds in voxels 
     */
    public int getDepthVoxels(double voxelSize){
        return roundSize((zmax-zmin)/voxelSize);
    }    


    public Vector3d getSize(){
        return new Vector3d((xmax-xmin),(ymax-ymin),(zmax-zmin));
    }
    public Vector3d getCenter(){
        return new Vector3d((xmax+xmin)/2.,(ymax+ymin)/2.,(zmax+zmin)/2.);
    }

    /**
       @return width of bounds 
     */
    public double getSizeX(){
        return (xmax-xmin);
    }

    /**
       @return height of bounds 
     */
    public double getSizeY(){
        return (ymax-ymin);
    }

    /**
     * Returns the maximum width, height or depth
     * @return
     */
    public double getSizeMax() {
        return Math.max(Math.max(getSizeX(),getSizeY()),getSizeZ());
    }

    /**
     * Returns the maximum width and depth
     */
    public double getSliceMax() {
        return Math.max(getSizeX(),getSizeZ());
    }

    /**
       @return depth of bounds 
     */
    public double getSizeZ(){
        return (zmax-zmin);
    }

    /**
       @return  center x
     */
    public double getCenterX(){
        return (xmax+xmin)/2;
    }

    /**
       @return  center y
     */
    public double getCenterY(){
        return (ymax+ymin)/2;
    }

    /**
       @return center z
     */
    public double getCenterZ(){
        return (zmax+zmin)/2;
    }

    /**
       set voxel size to be used for this bounds 
     */
    public void setVoxelSize(double voxelSize){
        m_voxelSize = voxelSize;
        init();

    }

    /*
    public void _setGridSize(int width,int height, int depth){

        this.nx = width;
        this.ny = height;
        this.nz = depth;
        this.m_voxelSize = (xmax - xmin)/nx;
        
    }
    */

    public static final int roundSize(double s){        
        return (int)(s + 0.5);
    }

    public double [] getArray() {
        return new double[]{xmin,xmax, ymin, ymax, zmin, zmax};
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
       conversion of grid coordinates to world coordinates 
     */
    public void toWorldCoord(Vector3d pnt){

        pnt.x = pnt.x*(xmax-xmin)/nx + xmin;
        pnt.y = pnt.y*(ymax-ymin)/ny + ymin;
        pnt.z = pnt.z*(zmax-zmin)/nz + zmin;
        
    }

    /**
       expand bounds by given margins 
       @param margin amount of expansion. if (margin < 0) bounds shrink
     */
    public void expand(double margin){

        xmin -= margin;
        xmax += margin;
        ymin -= margin;
        ymax += margin;
        zmin -= margin;
        zmax += margin;
        init();
    }

    /**
       round bounds using current voxel size 
     */
    public void roundBounds(){

        roundBounds(m_voxelSize);

    }

    /**
       round bounds using given voxel size 
     */
    public void roundBounds(double voxelSize){

        xmin = voxelSize*Math.floor(xmin/voxelSize);
        ymin = voxelSize*Math.floor(ymin/voxelSize);
        zmin = voxelSize*Math.floor(zmin/voxelSize);
        xmax = voxelSize*Math.ceil(xmax/voxelSize);
        ymax = voxelSize*Math.ceil(ymax/voxelSize);
        zmax = voxelSize*Math.ceil(zmax/voxelSize);

        init();
    }

    protected void init(){

        nx = getGridWidth();
        ny = getGridHeight();
        nz = getGridDepth();

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