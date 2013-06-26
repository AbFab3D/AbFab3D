/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
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

import java.util.Vector;

import abfab3d.util.TriangleCollector;

import javax.vecmath.Vector3d;

import abfab3d.grid.Grid;
import abfab3d.grid.AttributeGrid;
import abfab3d.util.MathUtil;


import static abfab3d.util.Output.printf; 
import static abfab3d.util.Output.fmt; 
import static abfab3d.util.Output.time; 


/**
   class to convert collection of triangles into antialised voxel grid
   using ideas from Manson J. Schaefer S. (2011) Wavelet Rasterization. 
   
   it creates antialiased grid with maximal attribute maxAttriubuteValue

   voxels inside have attribute maxAttriubuteValue
   voxels outside have attribute 0
   voxels intersecting the surface have intermediate values

   rasterizer = new WaveletRasterizer(bounds, nx, ny, nz);
   // set maximal value of data item to be stored in grid voxels inside of the shape will have 
   rasterizer.setMaxAttributeValue(63); (any other ODD number is fine) 

   for(each triangle in collection){
      mr.addTri();
   }
   
   AttributeGrid grid = new AttributeGrid();
   mr.getGrid(grid); 
   

   @author Vladimir Bulatov
 */
public class WaveletRasterizer implements TriangleCollector {

    static final boolean DEBUG_GRID = false;
    static final boolean DEBUG_CALC = false;

    //int gridX, gridY, gridZ;
    
    double xmin, ymin, zmin;
    double scale;
    int m_nx, m_ny, m_nz;
    
    int maxAttributeValue=63; // default value for 6 bit storage size in ByteArray grid 
    
    int maxDepth = 2; // depth of grid subdivision 
    
    TNode root;  // root of octree 
    double rootCoeff; // value of coefficient c00 
    // count of calculated dtriangles (for statistics) 
    int triCount = 0;

    /**
       does rasterization for gird of specified size and bounds 
     */
    public WaveletRasterizer(double bounds[], int gridX, int gridY, int gridZ){

        this.xmin = bounds[0];
        this.ymin = bounds[2];
        this.zmin = bounds[4];

        //this.gridX = gridX;
        //this.gridY = gridY;
        //this.gridZ = gridZ;

        root = new TNode();

        int d1 = MathUtil.getMaxSubdivision(gridX);
        int d2 = MathUtil.getMaxSubdivision(gridY);
        int d3 = MathUtil.getMaxSubdivision(gridZ);
        maxDepth = d1;
        if(d2 > maxDepth) maxDepth = d2;
        if(d3 > maxDepth) maxDepth = d3;            

        int gridSize =  (1<<maxDepth);
        if(DEBUG_CALC){
            printf("WaveletRasterizer(), maxDepth :%d\n", maxDepth);
            printf("   octree grid: [%d x %d x %d]\n", gridSize,gridSize, gridSize);
        } 
        
        
        double xmax = xmin + gridSize*((bounds[1] - bounds[0])/gridX);

        // scale to fit extended grid size into unit cube 
        this.scale = 1./(xmax - xmin);
        

    }
    
    /**
       sets maximal values used to represent inside voxels 
     */
    public void setMaxAttributeValue(int value){
        maxAttributeValue = value;
    }
    
    /**
       method of TriangleCollector interface 
       it is called for each triangle in the collection
       values stored in vectors are copied into internal storage
    */
    public boolean addTri(Vector3d _v0,Vector3d _v1,Vector3d _v2){
        
        Vec v0 = new Vec(_v0);
        Vec v1 = new Vec(_v1);
        Vec v2 = new Vec(_v2);
        
        normalize(v0);
        normalize(v1);
        normalize(v2);
        Polygon triangle = new Polygon();
        triangle.add(v0);
        triangle.add(v1);
        triangle.add(v2);
        if(DEBUG_CALC)
            printf("\naddTri(%s)\n", getPolyString(triangle));


        rootCoeff += 
            (v0.v[0]*(v1.v[1]*v2.v[2] - v1.v[2]*v2.v[1]) 
             - v0.v[1]*(v1.v[0]*v2.v[2] - v1.v[2]*v2.v[0]) 
             + v0.v[2]*(v1.v[0]*v2.v[1] - v1.v[1]*v2.v[0]))/6;
        
        insertPoly(root, triangle, 0);
        return true;

    }


    /**
       stores rasterization data into supplied grid 
     */
    public void getRaster(AttributeGrid grid){

        if(DEBUG_GRID){
            printf("\ngetRaster(%s)\n", grid.getClass().getName());
            printf("rootCoeff: %7.5f\n", rootCoeff);       
            //dumpTree(root, OFFSET);
        }

        m_nx = grid.getWidth();
        m_ny = grid.getHeight();
        m_nz = grid.getDepth();

        writeNode(grid, root, (1<< maxDepth), rootCoeff, 0,0,0);
        
    }
    static final String OFFSET = "    ";

    static void dumpTree(TNode node, String offset){

        double coeff[] = node.coeff;
        offset = offset + OFFSET;
        printf("%scoeff: (%6.3f %6.3f %6.3f %6.3f %6.3f %6.3f %6.3f)\n", offset, coeff[0],coeff[1],coeff[2],coeff[3],coeff[4],coeff[5],coeff[6]);
        for(int i = 0; i < 8; i++){
            TNode child = node.children[i];
            if(child != null){                
                printf("%s%s\n",offset, octantToString(i));
                dumpTree(child, offset);
            }
        }
    }


    /**
       
     */
    protected void writeNode(AttributeGrid grid, TNode node, int res, double val, int ix, int iy, int iz){

        if(DEBUG_GRID) 
            printf("writeNode(res:%d, val:%8.5f, xyz: [%d %d %d])\n",res, val, ix, iy, iz);

        double cvals[] = new double[8];
        double coeff[] = node.coeff;

        for(int octant = 0; octant < 8; octant++){
            if(false) printf("   octant: %d\n", octant);
            double cval = val;
            
            for (int func = 1; func < 8; func++){
                

                int foct = (func) & octant;

                int x = (foct & 1);
                int y = ((foct >> 1) & 1);
                int z = (foct >> 2);
                int s = (x ^ y ^ z);
                if(false) printf("      func: %d foct: %d xyz:(%d %d %d) s:%d\n", func, foct, x,y,z,s);
                if(s == 0)
                    cval += coeff[func-1];
                else 
                    cval -= coeff[func-1];
            }
            cvals[octant] = cval;
        }

        int res2 = res/2;
     
        TNode children[] = node.children;

	for (int octant = 0; octant < 8; octant++){

            int x = (octant & 1);
            int y = ((octant >> 1) & 1);
            int z = (octant >> 2);
            
            int ix1 = ix + x*res2;
            int iy1 = iy + y*res2;
            int iz1 = iz + z*res2;
            
            double cval = cvals[octant];            
            //if(DEBUG_GRID) printf("   octant: %d (%d, %d, %d): %8.5f, %s \n", octant, x,y,z, cval, children[octant]);

            TNode child = children[octant];

            if (child == null){
                // leaf, write solid block
                writeBlock(grid, cval, ix1, iy1, iz1, res2);
            }  else if (res2 > 2) {
                // internal node, do recursion
                writeNode(grid, child, res2, cval, ix1, iy1, iz1);
            } else { //if(res2 == 2) { // last leaf
                writeLeaf(grid, child, cval, ix1, iy1, iz1);
            }
	}
    }
    
    protected void writeLeaf(AttributeGrid grid, TNode node, double value, int ix, int iy, int iz){

        if(DEBUG_GRID) 
            printf("writeLeaf(val:%8.5f, ix:%d iy:%d iz:%d)\n",value, ix, iy, iz);

        double cvals[] = new double[8];
        double coeff[] = node.coeff;

	for (int octant = 0; octant < 8; octant++){ // octants
            double cval = value;
            for (int func = 1; func < 8; func++){ // basis function 
                int foct = (func & octant); 
                
                int x = (foct & 1);
		int y = (foct >> 1) & 1;
		int z = (foct >> 2);
		int s = (x ^ y ^ z);
                if(s == 0)
                    cval += coeff[func-1];
                else 
                    cval -= coeff[func-1];
            }
            cvals[octant] = cval;            
	}
        
	// process octants 
	for (int octant = 0; octant < 8; octant++) {
            int x = (octant & 1);
            int y = (octant >> 1) & 1;
            int z = (octant >> 2);            
            int a = getAttribute(cvals[octant]);
            if(DEBUG_GRID)
                printf("          [%d,%d,%d] -> %6.4f, 0x%x\n", ix+x, iy+y, iz+z, cvals[octant], a); 
            if(a > 0) {
                grid.setAttribute(ix + x,iy + y, iz + z, a);
            }
            
	}
    }
    
    private final int getAttribute(double value){
        if(value < 0)
            value = 0.;
        if(value > 1)
            value = 1.;            

        return (int)(maxAttributeValue*value + 0.5);
    }
    
    //
    // fills solid block of grid with the same value
    //
    protected void writeBlock(AttributeGrid grid, double value, int ix, int iy, int iz, int size){
    
        int a = getAttribute(value);

        if(DEBUG_GRID) 
            printf("writeBlock(val:%8.5f, xyz:(%d %d %d), size:%d)-> a:%d\n",value, ix, iy, iz, size, a);

        if(a <= 0)
            return;
        
        if(size == 1){
            if(ix < m_nx && iy < m_ny && iz < m_nz) {
                grid.setAttribute(ix,iy,iz,a);
            }
            return;
        }
        int ix1 = ix + size;
        int iy1 = iy + size;
        int iz1 = iz + size;
        if(ix1 >= m_nx)ix1 = m_nx-1;
        if(iy1 >= m_ny)iy1 = m_ny-1;
        if(iz1 >= m_nz)iz1 = m_nz-1;
        
        for(int y = iy; y < iy1; y++){
            for(int x = ix; x < ix1; x++){
                for(int z = iz; z < iz1; z++){ 
                    //try {
                    grid.setAttribute(x,y,z,a);

                    //} catch(Exception e){
                    //    printf("bad params in writeBlock(ix:%d iy:%d iz:%d, size:%d)\n", ix, y, iz, size); 
                    //    if(exceptionsCount++ > 100)
                    //        throw new RuntimeException("too many bad points");
                    //    else 
                    //        return;
                    //}
                }
            }
        }
    }

    int exceptionsCount = 0;

    protected void calcPoly(double coeff[], Polygon poly, int octant){
        
        if(DEBUG_CALC){
            printf("calcPoly(octant:%s, poly:%s\n", octantToString(octant),getPolyString(poly) ); 
        }

        Vec v0 = poly.get(0);
        //
        // split convex polygon into triangles 
        // 
        for (int s = 0; s < poly.size() - 2; s++){
            
            Vec 
                v1 = poly.get(s+1),
                v2 = poly.get(s+2);                                        
            calcTriangle(coeff, v0, v1, v2, octant);
        }
        if(DEBUG_CALC){
            //double coeff[] = node.coeff;
            //printf("   coeff: (%6.3f %6.3f %6.3f %6.3f %6.3f %6.3f %6.3f)\n", coeff[0],coeff[1],coeff[2],coeff[3],coeff[4],coeff[5],coeff[6]);
        }
    }

    static final double A = 1./16.;
    static final double B = 1./48.;
    
    static final double C1[] = new double[]{0, A};
    static final double L1[] = new double[]{B, -B};
    static final double CX2[][] = new double[][]{{0,0}, {A, -A}};
    static final double L2[][] = new double[][]{{B, -B}, {-B, B}};
    static final double CX3[][][] = new double[][][]{{{0, A},{0,-A}},{{0, -A}, {0, A}}};
    static final double L3[][][] = new double[][][]{{{B,-B},{-B, B}},{{-B, B},{B,-B}}};
    /**
       calculate contribution to coedfficients from triangle (v0, v1, v2) in given octant (i,j,k)
     */
    protected void calcTriangle(double coeff[], Vec v0, Vec v1, Vec v2, int octant){

        Vec 
            normal = new Vec(),
            sum = new Vec();   

        int i = (octant & 1);
        int j = ((octant >> 1) & 1);
        int k = ((octant >> 2) & 1);

     
        if(DEBUG_CALC){
            triCount++;
            printf("calcTriangle(%d (%d %d %d) [%5.2f,%5.2f,%5.2f] [%5.2f,%5.2f,%5.2f] [%5.2f,%5.2f,%5.2f])\n", triCount, i, j, k, 
                   v0.v[0],v0.v[1],v0.v[2],
                   v1.v[0],v1.v[1],v1.v[2],
                   v2.v[0],v2.v[1],v2.v[2]);
        }
        Vec.getNormal(v0, v1, v2, normal);
        Vec.getSum(v0, v1, v2, sum);
        if(false){
            printf("normal:[%5.2f,%5.2f,%5.2f] ", normal.v[0],normal.v[1],normal.v[2]);
            printf("sum:[%5.2f,%5.2f,%5.2f] \n", sum.v[0],sum.v[1],sum.v[2]);
        }

        coeff[0] += (C1[i] + L1[i]*sum.v[0])*normal.v[0];
        coeff[1] += (C1[j] + L1[j]*sum.v[1])*normal.v[1];        
        coeff[3] += (C1[k] + L1[k]*sum.v[2])*normal.v[2];

        coeff[5] += (CX2[j][k] + L2[j][k]*sum.v[1])*normal.v[1];
        coeff[2] += (CX2[i][j] + L2[i][j]*sum.v[0])*normal.v[0];
        coeff[4] += (CX2[i][k] + L2[i][k]*sum.v[0])*normal.v[0];

        coeff[6] += (CX3[k][j][i] + L3[k][j][i]*sum.v[0])*normal.v[0];

        if(DEBUG_CALC){
            printf("coeff: (%6.3f %6.3f %6.3f %6.3f %6.3f %6.3f %6.3f)\n", coeff[0],coeff[1],coeff[2],coeff[3],coeff[4],coeff[5],coeff[6]);
        }
        

    }

    static final int XAXIS = 0, YAXIS = 1, ZAXIS = 2; 

    protected void insertPolySplit(TNode node, Polygon poly, int depth){

        if(DEBUG_CALC)
            printf("insertPolySplit(depth:%d, poly:%s)\n", depth, getPolyString(poly));
        if(depth > maxDepth)
            return;
	// scale by a factor of 2
        for(int i = 0; i < poly.size(); i++){
            Vec p = poly.get(i);
            p.v[0] *= 2;
            p.v[1] *= 2;
            p.v[2] *= 2;
        }

	// split poly
	Polygon splitPoly[] = new Polygon[8];
        for(int i = 0; i < 8; i++)
            splitPoly[i] = new Polygon();

	// do full split
	Polygon splitX[] = new Polygon[]{new Polygon(),new Polygon()};
	Polygon splitY[] = new Polygon[]{new Polygon(),new Polygon(),new Polygon(),new Polygon()};

	split(poly, splitX[0],splitX[1], XAXIS);

	for (int i = 0; i < 2; i++){
            
            if (splitX[i].size() == 0)
                continue;
            
            split(splitX[i], splitY[i], splitY[i+2], YAXIS);
            
            for (int  j = 0; j < 2; j++){
                
                int ij = i + j*2;
                if (splitY[ij].size() == 0)
                    continue;
                split(splitY[ij], splitPoly[ij], splitPoly[ij+4], ZAXIS);
                
                for (int k = 0; k < 2; k++){
                    
                    int ijk = ij + k*4;

                    if (splitPoly[ijk].size() == 0)
                        continue;
                    
                    int octant = i | (j << 1) | (k << 2);
                    // add to coefficients
                    calcPoly(node.coeff, splitPoly[ijk], ijk);
                    
                    if(depth < maxDepth-1){
                        insertPoly(node.getChild(ijk), splitPoly[ijk], depth+1);
                    } else {
                        insertPolyLeaf(node.getChild(ijk), splitPoly[ijk],depth+1);
                    }
                }
            }
	}
    }
            
    protected void insertPolyLeaf(TNode node, Polygon poly, int depth){
        
        if(DEBUG_CALC)
            printf("insertPolyLeaf(depth:%d, poly:%s)\n", depth, getPolyString(poly));
        if(depth >= maxDepth)
            return; 

        Vec v0 = poly.get(0);
	int octant = getOctant(v0);

	for (int s = poly.size()-1; s > 0; s--){
            if(getOctant(poly.get(s)) != octant){
                insertPolySplit(node, poly, depth+1);
                return;
            }
        }

        //  polygon is in one octant. We are on the leaf - end of calculations 
        // 
	// normalize to the unit cube
        //
	for (int v = poly.size()-1; v >= 0; v--){
            normalizeOctant(poly.get(v), octant);
	}
        
        calcPoly(node.coeff, poly, octant);

    }

    protected void insertPoly(TNode node, Polygon poly, int depth){
        
        if(DEBUG_CALC){
            printf("insertPoly     (depth:%d, poly:%s)\n", depth, getPolyString(poly));
        }

        Vec v0 = poly.get(0);
        
        int octant =  getOctant(v0);
        
        for(int s = poly.size()-1; s > 0; s--){
            
            if(getOctant(poly.get(s)) != octant){  
                // polygon occupy more than 1 octant 
                insertPolySplit(node, poly, depth);
                
                return;
                
            }                   
        }
        
        //printf("  single octant: %s\n", octantToString(octant));
        // the whole polygon is in one octant                    
        for(int s = poly.size()-1; s >= 0; s--){
            normalizeOctant(poly.get(s),octant);
        }
        calcPoly(node.coeff, poly, octant);
        
        if(depth < maxDepth-1){            
            // go deeper
            insertPoly(node.getChild(octant), poly, depth+1);
            return;           
        } else {             
            //insertPolyLeaf(node.getChild(octant), poly, depth+1);
            return;
        }
        
    }

    /**
       normalize input vector to given octant 
     */
    static final void normalizeOctant(Vec p, int octant){

        p.v[0] = 2*p.v[0] - (octant & 1);
        p.v[1] = 2*p.v[1] - ((octant >> 1) & 1);
        p.v[2] = 2*p.v[2] - ((octant >> 2) & 1);

    }

    /**
       in which octant is the point 
     */
    static final int getOctant(Vec p){

        return getHalf(p.v[0]) | (getHalf(p.v[1]) << 1) | (getHalf(p.v[2]) << 2);

    }
    
    /**
       where is the point ? 
     */
    static final int getHalf(double v){
        if(v < 0.5) 
            return 0;
        else 
            return 1;            
    }
    
    /**
       scale vector to fit into unit cube 
     */
    void normalize(Vec p){

        p.v[0] = (p.v[0] - xmin)*scale;
        p.v[1] = (p.v[1] - ymin)*scale;
        p.v[2] = (p.v[2] - zmin)*scale;

    }

   
    //
    // split Polygon p in two  by plane orthogonal to given axis at distance from origin 1. 
    // and store results in a (left), b (right)
    //
    static void split(Polygon p, Polygon a, Polygon b, int axis){
        
	int size = p.size();
        
	if (size == 0)
            return;
        a.clear();
        b.clear();

	int axis1 = (axis+1)%3;
	int axis2 = (axis+2)%3;

	Vec vp = p.get(size-1);

	double xp = vp.v[axis];
        
	int inp = (xp < 1.)? 1 : 0;

	for (int i = 0; i < size; i++)	{

            Vec v = p.get(i);
            double x = v.v[axis];

            int in = (x < 1.) ? 1: 0;

            int selector = (inp << 1) | in;

            switch (selector){

            case 3://if (inp < 1 && in < 1)                
                {
                    a.add(new Vec(v));
                }
                break;
            case 2: //(inp < 1 && in >= 1)			
                {
                    double div = x - xp;
                    
                    double t = (1 - xp)/div;
                    if (t < 0. || t > 1.) 
                        t = 0.5;                    
                    Vec m = new Vec();
                    double pp1 = vp.v[axis1];
                    double pp2 = vp.v[axis2];
                    m.v[axis] = 1.; 
                    m.v[axis1] = (v.v[axis1] - pp1)*t + pp1;
                    m.v[axis2] = (v.v[axis2] - pp2)*t + pp2;                    
                    
                    a.add(new Vec(m));
                    
                    b.add(new Vec(m));
                    b.add(new Vec(v));
                }
                break;
            case 1: // (inp >= 1) (in < 1)
                {
                    double div = x - xp;
                    double t = (1 - xp)/div;
                    if (t < 0 || t > 1) t = 0.5;                    
                    Vec m = new Vec();
                    double pp1 = vp.v[axis1];
                    double pp2 = vp.v[axis2];
                    m.v[axis] = 1.; 
                    m.v[axis1] = (v.v[axis1] - pp1)*t + pp1;
                    m.v[axis2] = (v.v[axis2] - pp2)*t + pp2;

                    a.add(new Vec(m));
                    a.add(new Vec(v));                    
                    b.add(new Vec(m));
                }
                break;
            case 0: // (inp > 1) (in > 1)
                {
                    b.add(new Vec(v));
                }
            }
            
            xp = x;
            vp = v;
            inp = in;
	}

	for (int i = 0; i < b.size(); i++) {
            // shift polygon b into (0,1)
            // 
            b.get(i).v[axis]--;
        }
    }  // split()
    



    // simple class to represent 3d vector 
    static class Vec{

        double v[] = new double[3];

        Vec(){
        }
        Vec(Vec p){
            v[0] = p.v[0];
            v[1] = p.v[1];
            v[2] = p.v[2];
        }
        Vec(Vector3d vv){
            v[0] = vv.x;
            v[1] = vv.y;
            v[2] = vv.z;
        }

        static void getNormal(Vec v0, Vec v1, Vec v2, Vec result){
            double 
                x1 = v1.v[0] - v0.v[0],
                y1 = v1.v[1] - v0.v[1],
                z1 = v1.v[2] - v0.v[2],
                x2 = v2.v[0] - v0.v[0],
                y2 = v2.v[1] - v0.v[1],
                z2 = v2.v[2] - v0.v[2];
            
            result.v[0] = y1*z2 - z1*y2;
            result.v[1] = z1*x2 - x1*z2;
            result.v[2] = x1*y2 - y1*x2;
            
        }
        
        static void getSum(Vec v0, Vec v1, Vec v2, Vec result){
            
            result.v[0] = v0.v[0] +  v1.v[0] + v2.v[0];
            result.v[1] = v0.v[1] +  v1.v[1] + v2.v[1];
            result.v[2] = v0.v[2] +  v1.v[2] + v2.v[2];
            
        }
        
    } // class Vec

    //
    // array of Vec 
    //
    static class Polygon {
        
        Vec v[] = new Vec[10]; // max size of polygins is 6 (sometimes 8) 
        int size = 0;

        Polygon(){            
        }
        
        void clear() {
            size = 0;
        }
        
        Vec get(int index){
            
            return v[index];
        }

        void add(Vec a){
            v[size] = a;
            size++;
        } 
        
        int size(){
            return size;
        }
        
    } // class Polygon 


    
    // node of octree 
    static class TNode {

        TNode children[] = new TNode[8];
        double coeff[] = new double[7];
        int flags;        
        
        /**
           return child for given octant (allocates node new if necessary)
        */
        protected TNode getChild(int octant){

            TNode child = children[octant];
            if(child == null){
                child = new TNode();
                children[octant] = child;
            }
            return child;
        }
        
    }

    static String getPolyString(Polygon poly){

        StringBuffer sb = new StringBuffer();
        for (int s = 0; s < poly.size(); s++){
            Vec p = poly.get(s);
            sb.append(fmt("(%4.2f %4.2f %4.2f) ",p.v[0],p.v[1],p.v[2]));
        }
        return sb.toString();
    }
    
    static String octantToString(int i){

        return (((i & 1) == 1)? "1":"0") + (((i & 2) == 2)? "1":"0") + (((i & 4) == 4)? "1":"0");

    }
} // class WaveletRasterizer
