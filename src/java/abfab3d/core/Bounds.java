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

package abfab3d.core;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;

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

    public Bounds(double bounds[], double voxelSize){
        this.xmin = bounds[0];
        this.xmax = bounds[1];
        this.ymin = bounds[2];
        this.ymax = bounds[3];
        this.zmin = bounds[4];
        this.zmax = bounds[5];
        this.m_voxelSize = voxelSize;
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
        return round((xmax-xmin)/m_voxelSize);
    }
    public int getGridWidth(double voxelSize){
        return round((xmax-xmin)/voxelSize);
    }

    public int getGridHeight(){
        return round((ymax-ymin)/m_voxelSize);
    }

    public int getGridHeight(double voxelSize){
        return round((ymax-ymin)/voxelSize);
    }

    public int getGridDepth(){
        return round((zmax-zmin)/m_voxelSize);
    }

    public int getGridDepth(double voxelSize){
        return round((zmax-zmin)/voxelSize);
    }

    public int[] getGridSize(){

        return new int[]{
            getGridWidth(),
            getGridHeight(),
            getGridDepth()
        };

    }

    public int[] getGridSize(double voxelSize){

        return new int[]{
            getGridWidth(voxelSize),
            getGridHeight(voxelSize),
            getGridDepth(voxelSize)
        };

    }

    public double getVoxelSize(){
        return m_voxelSize;
    }

    /**
       @return width of bounds in voxels 
     */
    public int getWidthVoxels(double voxelSize){
        return round((xmax-xmin)/voxelSize);
    }

    /**
       @return height of bounds in voxels 
     */
    public int getHeightVoxels(double voxelSize){
        return round((ymax-ymin)/voxelSize);
    }

    /**
       @return depth of bounds in voxels 
     */
    public int getDepthVoxels(double voxelSize){
        return round((zmax-zmin)/voxelSize);
    }    


    public Vector3d getSize(){
        Vector3d size = new Vector3d();
        getSize(size);
        return size;
    }
    public void getSize(Vector3d size){
        size.x = (xmax-xmin);
        size.y = (ymax-ymin);
        size.z = (zmax-zmin);
    }

    public Vector3d getCenter(){
        return new Vector3d((xmax+xmin)/2.,(ymax+ymin)/2.,(zmax+zmin)/2.);
    }
    public void getCenter(Vector3d center){
        center.x = (xmax+xmin)/2.;
        center.y = (ymax+ymin)/2.;
        center.z = (zmax+zmin)/2.;
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
       @return depth of bounds 
     */
    public double getSizeZ(){
        return (zmax-zmin);
    }

    /**
     * Returns the minimum width, height or depth
     * @return
     */
    public double getSizeMin() {
        return Math.min(Math.min(getSizeX(),getSizeY()),getSizeZ());
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

    public static final int round(double s){        
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
    public final void toGridCoord(Vector3d pnt){

        pnt.x = nx*(pnt.x - xmin)/(xmax - xmin);
        pnt.y = ny*(pnt.y - ymin)/(ymax - ymin);
        pnt.z = nz*(pnt.z - zmin)/(zmax - zmin);
        
    }

    /**
       conversion of grid coordinates to world coordinates 
     */
    public final void toWorldCoord(Vector3d pnt){

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
       translate bounds by given amount 
       @param tx x-component of transltion 
       @param ty y-component of transltion 
       @param tz z-component of transltion 
     */
    public void translate(double tx, double ty, double tz){
        xmin += tx;
        xmax += tx;
        ymin += ty;
        ymax += ty;
        zmin += tz;
        zmax += tz;
    }

    public boolean isPointInside(Point3d p) {
        if (p.x >= xmin && p.x <= xmax &&
            p.y >= ymin && p.y <= ymax &&
            p.z >= zmin && p.z <= zmax) {
            return true;
        }

        return false;
    }

    /**
     * Test whether two bounds objects intersect.  Includes objects completely inside
     * @param box
     * @return
     */
    public boolean intersects(Bounds box) {
        return box.xmin <= xmax && box.ymin <= ymax &&
                box.zmin <= zmax && box.xmax >= xmin &&
                box.ymax >= ymin && box.zmax >= zmin;
    }

    /**
     * Test whether a bounds is inside another
     *
     * TODO: I suspect there is a more optimized way todo this using separating axis theorem
     * @param box
     * @return
     */
    public boolean isBoundsInsideAllPoints(Bounds box, Vector3d center) {
        Point3d pos = new Point3d();  // TODO: Garbage

        pos.set(center.x + box.xmin,center.y + box.ymin,center.z + box.zmin);
        if (!isPointInside(pos)) return false;

        pos.set(center.x + box.xmin,center.y + box.ymin,center.z + box.zmax);
        if (!isPointInside(pos)) return false;

        pos.set(center.x + box.xmin,center.y + box.ymax,center.z + box.zmin);
        if (!isPointInside(pos)) return false;

        pos.set(center.x + box.xmin,center.y + box.ymax,center.z + box.zmax);
        if (!isPointInside(pos)) return false;

        pos.set(center.x + box.xmax,center.y + box.ymin,center.z + box.zmin);
        if (!isPointInside(pos)) return false;

        pos.set(center.x + box.xmax,center.y + box.ymin,center.z + box.zmax);
        if (!isPointInside(pos)) return false;

        pos.set(center.x + box.xmax,center.y + box.ymax,center.z + box.zmin);
        if (!isPointInside(pos)) return false;

        pos.set(center.x + box.xmax,center.y + box.ymax,center.z + box.zmax);
        if (!isPointInside(pos)) return false;


        return true;
    }

    public boolean isBoundsInside(Bounds box, Vector3d pos) {
        if (!((pos.x + box.xmin) >= xmin && (pos.x + box.xmin) <= xmax &&
                (pos.y + box.ymin) >= ymin && (pos.y + box.ymin) <= ymax &&
                (pos.z + box.zmin) >= zmin && (pos.z + box.zmin) <= zmax)) {
            return false;
        }

        if (!((pos.x + box.xmax) >= xmin && (pos.x + box.xmax) <= xmax &&
                (pos.y + box.ymax) >= ymin && (pos.y + box.ymax) <= ymax &&
                (pos.z + box.zmax) >= zmin && (pos.z + box.zmax) <= zmax)) {
            return false;
        }

        return true;
    }

    /**
     * Check for the given ray intersecting this bounds. The line is
     * described as a starting point and a vector direction.
     *
     * @param pos The start location of the ray
     * @param dir The direction vector of the ray
     * @param coord The intersection coordinate
     * @return true if the ray intersects this bounds
     */
    public boolean checkIntersectionRay(double[] pos, double[] dir, double[] coord)
    {
        // This is based on the Graphics Gems code by Andrew Woo.
        // http://www1.acm.org/pubs/tog/GraphicsGems/gems/RayBox.c
        // Since the original code always used fixed-length loops, I've
        // unrolled the loops here and used local variables instead of
        // arrays.

        boolean inside = true;
        boolean quadrant_0, quadrant_1, quadrant_2;
        double max_t_x, max_t_y, max_t_z;
        double c_plane_x = 0;
        double c_plane_y = 0;
        double c_plane_z = 0;

        // Find candidate planes; Unrolled loop
        if(pos[0] < xmin)
        {
            quadrant_0 = false;
            c_plane_x = xmin;
            inside = false;
        }
        else if(pos[0] > xmax)
        {
            quadrant_0 = false;
            c_plane_x = xmax;
            inside = false;
        }
        else
        {
            quadrant_0 = true;
        }

        if(pos[1] < ymin)
        {
            quadrant_1 = false;
            c_plane_y = ymin;
            inside = false;
        }
        else if(pos[1] > ymax)
        {
            quadrant_1 = false;
            c_plane_y = ymax;
            inside = false;
        }
        else
        {
            quadrant_1 = true;
        }

        if(pos[2] < zmin)
        {
            quadrant_2 = false;
            c_plane_z = zmin;
            inside = false;
        }
        else if(pos[2] > zmax)
        {
            quadrant_2 = false;
            c_plane_z = zmax;
            inside = false;
        }
        else
        {
            quadrant_2 = true;
        }

        // Ray origin inside bounding box - exit now.
        if(inside) {
            coord[0] = pos[0];
            coord[1] = pos[1];
            coord[2] = pos[2];
            return true;
        }

        // Calculate T distances to candidate planes
        if(!quadrant_0 && dir[0] != 0)
            max_t_x = (c_plane_x - pos[0]) / dir[0];
        else
            max_t_x = -1;

        if(!quadrant_1 && dir[1] != 0)
            max_t_y = (c_plane_y - pos[1]) / dir[1];
        else
            max_t_y = -1;

        if(!quadrant_2 && dir[2] != 0)
            max_t_z = (c_plane_z - pos[2]) / dir[2];
        else
            max_t_z = -1;

        // Get largest of the max_t's for final choice of intersection
        double max_t = max_t_x;
        int plane = 0;

        if(max_t < max_t_y)
        {
            plane = 1;
            max_t = max_t_y;
        }

        if(max_t < max_t_z)
        {
            plane = 2;
            max_t = max_t_z;
        }

        // Check final candidate actually inside box
        boolean intersect = true;

        if(max_t < 0)
            intersect = false;
        else
        {
            if(plane != 0)
            {
                coord[0] = (pos[0] + max_t * dir[0]);

                if(coord[0] < xmin || coord[0] > xmax)
                    intersect = false;
            } else {
                coord[0] = c_plane_x;
            }

            if(plane != 1 && intersect)
            {
                coord[1] = (pos[1] + max_t * dir[1]);

                if(coord[1] < ymin || coord[1] > ymax)
                    intersect = false;
            } else {
                coord[1] = c_plane_y;
            }

            if(plane != 2 && intersect)
            {
                coord[2] = (pos[2] + max_t * dir[2]);

                if(coord[2] < zmin || coord[2] > zmax)
                    intersect = false;
            } else {
                coord[2] = c_plane_z;
            }
        }

        return intersect;              /* ray hits box */
    }

    /**
     * Check for the given ray intersecting this bounds. The line is
     * described as a starting point and a vector direction.
     *
     * @param pos The start location of the ray
     * @param dir The direction vector of the ray
     * @param coord The intersection coordinate
     * @return true if the ray intersects this bounds
     */
    public boolean checkIntersectionRay(Vector3d pos, Vector3d dir, Vector3d coord) {
        boolean inside = true;
        boolean quadrant_0, quadrant_1, quadrant_2;
        double max_t_x, max_t_y, max_t_z;
        double c_plane_x = 0;
        double c_plane_y = 0;
        double c_plane_z = 0;

        // Find candidate planes; Unrolled loop
        if(pos.x < xmin)
        {
            quadrant_0 = false;
            c_plane_x = xmin;
            inside = false;
        }
        else if(pos.x > xmax)
        {
            quadrant_0 = false;
            c_plane_x = xmax;
            inside = false;
        }
        else
        {
            quadrant_0 = true;
        }

        if(pos.y < ymin)
        {
            quadrant_1 = false;
            c_plane_y = ymin;
            inside = false;
        }
        else if(pos.y > ymax)
        {
            quadrant_1 = false;
            c_plane_y = ymax;
            inside = false;
        }
        else
        {
            quadrant_1 = true;
        }

        if(pos.z < zmin)
        {
            quadrant_2 = false;
            c_plane_z = zmin;
            inside = false;
        }
        else if(pos.z > zmax)
        {
            quadrant_2 = false;
            c_plane_z = zmax;
            inside = false;
        }
        else
        {
            quadrant_2 = true;
        }

        // Ray origin inside bounding box - exit now.
        if(inside) {
            coord.x = pos.x;
            coord.y = pos.y;
            coord.z = pos.z;
            return true;
        }

        // Calculate T distances to candidate planes
        if(!quadrant_0 && dir.x != 0)
            max_t_x = (c_plane_x - pos.x) / dir.x;
        else
            max_t_x = -1;

        if(!quadrant_1 && dir.y != 0)
            max_t_y = (c_plane_y - pos.y) / dir.y;
        else
            max_t_y = -1;

        if(!quadrant_2 && dir.z != 0)
            max_t_z = (c_plane_z - pos.z) / dir.z;
        else
            max_t_z = -1;

        // Get largest of the max_t's for final choice of intersection
        double max_t = max_t_x;
        int plane = 0;

        if(max_t < max_t_y)
        {
            plane = 1;
            max_t = max_t_y;
        }

        if(max_t < max_t_z)
        {
            plane = 2;
            max_t = max_t_z;
        }

        // Check final candidate actually inside box
        boolean intersect = true;

        if(max_t < 0)
            intersect = false;
        else
        {
            if(plane != 0)
            {
                coord.x = (pos.x + max_t * dir.x);

                if(coord.x < xmin || coord.x > xmax)
                    intersect = false;
            } else {
                coord.x = c_plane_x;
            }

            if(plane != 1 && intersect)
            {
                coord.y = (pos.y + max_t * dir.y);

                if(coord.y < ymin || coord.y > ymax)
                    intersect = false;
            } else {
                coord.y = c_plane_y;
            }

            if(plane != 2 && intersect)
            {
                coord.z = (pos.z + max_t * dir.z);

                if(coord.z < zmin || coord.z > zmax)
                    intersect = false;
            } else {
                coord.z = c_plane_z;
            }
        }

        return intersect;              /* ray hits box */
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
    public void roundSize(double voxelSize){

        xmax = xmin + voxelSize*Math.round((xmax-xmin)/voxelSize);
        ymax = ymin + voxelSize*Math.round((ymax-ymin)/voxelSize);
        zmax = zmin + voxelSize*Math.round((zmax-zmin)/voxelSize);

        init();
    }

    public void roundBounds(double voxelSize){

        xmin = voxelSize*Math.floor(xmin/voxelSize);
        ymin = voxelSize*Math.floor(ymin/voxelSize);
        zmin = voxelSize*Math.floor(zmin/voxelSize);
        xmax = voxelSize*Math.ceil(xmax/voxelSize);
        ymax = voxelSize*Math.ceil(ymax/voxelSize);
        zmax = voxelSize*Math.ceil(zmax/voxelSize);

        init();
    }


    /**
     * Combines this bounding box with a bounding object   so that the
     * resulting bounding box encloses the original bounding box and the
     * specified bounds object.
     * @param bounds The other bounds object
     */
    public void combine(Bounds bounds) {
        if( xmin > bounds.xmin) xmin = bounds.xmin;
        if( ymin > bounds.ymin) ymin = bounds.ymin;
        if( zmin > bounds.zmin) zmin = bounds.zmin;
        if( xmax < bounds.xmax) xmax = bounds.xmax;
        if( ymax < bounds.ymax) ymax = bounds.ymax;
        if( zmax < bounds.zmax) zmax = bounds.zmax;
    }

    protected void init(){

        nx = getGridWidth();
        ny = getGridHeight();
        nz = getGridDepth();

    }


    public String toString(){
        return fmt("%12.7e %12.7e %12.7e %12.7e %12.7e %12.7e; %12.7e",xmin, xmax, ymin, ymax, zmin, zmax, m_voxelSize);
    }

    public String toString(double unit) {
        return fmt("%8.4f %8.4f %8.4f %8.4f %8.4f %8.4f; %8.4f",xmin/unit, xmax/unit, ymin/unit, ymax/unit, zmin/unit, zmax/unit, m_voxelSize/unit);
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