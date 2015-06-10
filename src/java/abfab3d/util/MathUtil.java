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
import javax.vecmath.Vector4d;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;

// External Imports

// Internal Imports

import javax.vecmath.Matrix3d;
import javax.vecmath.SingularMatrixException;

import static java.lang.Math.sqrt;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static abfab3d.util.Output.fmt;

/**
 * Math utilities.
 *
 * @author Tony Wong
 * @author Vladimir Bulatov
 */
public class MathUtil {

    /**
       conversion factor from degree to radians 
     */
    public static double TORAD = Math.PI/180.; 
    
    /**
       conversion factor from radians to degree
     */
    public static double TODEG = 180./Math.PI; 

    /**
     * Calculate distance between two points in Euclidian space.
     * 
     * @param pos1 The first position as an int array of length 3
     * @param pos2 The second position as an int array of length 3
     * @return The distance as a double
     */
    public static double getDistance(int[] pos1, int[] pos2) {
    	int xDistance = pos2[0] - pos1[0];
    	int yDistance = pos2[1] - pos1[1];
    	int zDistance = pos2[2] - pos1[2];
    	
    	double distance = Math.sqrt(xDistance*xDistance + 
                                    yDistance*yDistance + 
                                    zDistance*zDistance);
    	
    	return distance;
    }

    /**
     * Calculate distance between two points in Euclidian space.
     * 
     * @param pos1 The first position as an double array of length 3
     * @param pos2 The second position as an double array of length 3
     * @return The distance as a double
     */
    public static double getDistance(double[] pos1, double[] pos2) {
    	double xDistance = pos2[0] - pos1[0];
    	double yDistance = pos2[1] - pos1[1];
    	double zDistance = pos2[2] - pos1[2];
    	
    	double distance = Math.sqrt(xDistance*xDistance + 
                                    yDistance*yDistance + 
                                    zDistance*zDistance);
    	
    	return distance;
    }

    /**
     * return distance between two points in Euclidian space.
     *
     */
    public static final double getDistance(Vector3d v0, Vector3d v1){
        double 
            x = v0.x - v1.x,
            y = v0.y - v1.y,
            z = v0.z - v1.z;        

        return sqrt(x*x + y*y + z*z);
    }


    /**
       extends bounds array by given margin 
     */
    public static double[] extendBounds(double bounds[], double margin){
        return new double[]{
            bounds[0] - margin, 
            bounds[1] + margin, 
            bounds[2] - margin, 
            bounds[3] + margin, 
            bounds[4] - margin, 
            bounds[5] + margin, 
        };
    }

    /**
       round the bounds to the voxel boundary. 
       upper boundary grows up, lower boundary grows down 
     */
    public static double[] roundBounds(double bounds[], double voxelSize){
        for(int i =0; i < 3; i++){
            bounds[2*i] = voxelSize*Math.floor(bounds[2*i]/voxelSize);
            bounds[2*i + 1] = voxelSize*Math.ceil(bounds[2*i+1]/voxelSize);
        }
        return bounds;
    }
    
    public static int[] getGridSize(double bounds[], double voxelSize){
        int n[] = new int[3];
        for(int i = 0; i < 3; i++)
            n[i] = (int)((bounds[2*i+1] - bounds[2*i])/voxelSize + 0.5);
        return n;
    }
    
    /**
       calculates bounds of array of 3D vertices stored in flat array 
     */
    public static double[] calculateBounds(double vertices[]){
        double bounds[] = new double[]{Double.MAX_VALUE,-Double.MAX_VALUE,
		Double.MAX_VALUE,-Double.MAX_VALUE,Double.MAX_VALUE,-Double.MAX_VALUE};
        int vcount = vertices.length/3;        

        for(int i = 0; i < vcount; i++){
            int ind = i*3;
            for(int k = 0; k < 3; k++){
                double v = vertices[ind + k];
                if( v < bounds[2*k]) bounds[2*k] = v;
                if( v > bounds[2*k+1]) bounds[2*k+1] = v;
            }
        }
        return bounds;        
    }

    /**
       return maximal count the n can be divided by 2 with rounding up. 
     */
    public static int getMaxSubdivision(int n){
        int d = 0;
        while( n > 1) {
            n = (n+1)/2;
            d++;
        }
        return d;
        
    }

    public static final double clamp(double x, double xmin, double xmax){
        if(x <= xmin)
            return xmin;
        if(x >= xmax)
            return xmax;
        return x;
    }

    public static final int clamp(int x, int xmin, int xmax){
        if(x <= xmin)
            return xmin;
        if(x >= xmax)
            return xmax;
        return x;
    }

    public static final long clamp(long x, long xmin, long xmax){
        if(x <= xmin)
            return xmin;
        if(x >= xmax)
            return xmax;
        return x;
    }

    
    // small number to detect degenerate matrix 
    static final double EPS = 1.e-9;
    static final double EPS2 = EPS*EPS; // squared epsilon

    static final int // elements of 3x3 matrix stored in array 
        M00 = 0, M01 = 1, M02 = 2, 
        M10 = 3, M11 = 4, M12 = 5, 
        M20 = 6, M21 = 7, M22 = 8; 



    /**
     * General invert routine.  Inverts m1 and places the result in "this".
     * Note that this routine handles both the "this" version and the
     * non-"this" version.
     *
     * Also note that since this routine is slow anyway, we will worry
     * about allocating a little bit of garbage as it sucks.  
     * 
     * @param result scratch double[9] array
     * @param row_perm scratch int[3] array
     * @param row_scale scratch double [3] array
     * @param tmp scratch double[9] array
     */
    public static final void invertGeneral(Matrix3d m1, double result[], int[] row_perm, double[] row_scale, double[] tmp) {
        for(int i=0; i < 9; i++) {
            result[i] = 0;
            tmp[i] = 0;
        }
        
        row_perm[0] = 0;
        row_perm[1] = 0;
        row_perm[2] = 0;

        row_scale[0] = 0;
        row_scale[1] = 0;
        row_scale[2] = 0;
        
        int i, r, c;

        // Use LU decomposition and backsubstitution code specifically
        // for floating-point 3x3 matrices.

        // Copy source matrix to t1tmp
        tmp[0] = m1.m00;
        tmp[1] = m1.m01;
        tmp[2] = m1.m02;

        tmp[3] = m1.m10;
        tmp[4] = m1.m11;
        tmp[5] = m1.m12;

        tmp[6] = m1.m20;
        tmp[7] = m1.m21;
        tmp[8] = m1.m22;


        // Calculate LU decomposition: Is the matrix singular?
        if (!luDecomposition(tmp, row_perm, row_scale)) {
            // Matrix has no inverse
            throw new SingularMatrixException("Singular Matrix");
        }

        // Perform back substitution on the identity matrix
        for(i=0;i<9;i++) {
            result[i] = 0.0;
        }
        result[0] = 1.0; result[4] = 1.0; result[8] = 1.0;
        luBacksubstitution(tmp, row_perm, result);

        m1.m00 = result[0];
        m1.m01 = result[1];
        m1.m02 = result[2];

        m1.m10 = result[3];
        m1.m11 = result[4];
        m1.m12 = result[5];

        m1.m20 = result[6];
        m1.m21 = result[7];
        m1.m22 = result[8];

    }

    /**
     * Given a 3x3 array "matrix0", this function replaces it with the
     * LU decomposition of a row-wise permutation of itself.  The input
     * parameters are "matrix0" and "dimen".  The array "matrix0" is also
     * an output parameter.  The vector "row_perm[3]" is an output
     * parameter that contains the row permutations resulting from partial
     * pivoting.  The output parameter "even_row_xchg" is 1 when the
     * number of row exchanges is even, or -1 otherwise.  Assumes data
     * type is always double.
     *
     * This function is similar to luDecomposition, except that it
     * is tuned specifically for 3x3 matrices.
     *
     * @return true if the matrix is nonsingular, or false otherwise.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling,
    //	      _Numerical_Recipes_in_C_, Cambridge University Press,
    //	      1988, pp 40-45.
    //
    static boolean luDecomposition(double[] matrix0,
                                   int[] row_perm, double[] row_scale) {

        row_scale[0] = 0;
        row_scale[1] = 0;
        row_scale[2] = 0;

        // Determine implicit scaling information by looping over rows
        {
            int i, j;
            int ptr, rs;
            double big, temp;

            ptr = 0;
            rs = 0;

            // For each row ...
            i = 3;
            while (i-- != 0) {
                big = 0.0;

                // For each column, find the largest element in the row
                j = 3;
                while (j-- != 0) {
                    temp = matrix0[ptr++];
                    temp = Math.abs(temp);
                    if (temp > big) {
                        big = temp;
                    }
                }

                // Is the matrix singular?
                if (big == 0.0) {
                    return false;
                }
                row_scale[rs++] = 1.0 / big;
            }
        }

        {
            int j;
            int mtx;

            mtx = 0;

            // For all columns, execute Crout's method
            for (j = 0; j < 3; j++) {
                int i, imax, k;
                int target, p1, p2;
                double sum, big, temp;

                // Determine elements of upper diagonal matrix U
                for (i = 0; i < j; i++) {
                    target = mtx + (3*i) + j;
                    sum = matrix0[target];
                    k = i;
                    p1 = mtx + (3*i);
                    p2 = mtx + j;
                    while (k-- != 0) {
                        sum -= matrix0[p1] * matrix0[p2];
                        p1++;
                        p2 += 3;
                    }
                    matrix0[target] = sum;
                }

                // Search for largest pivot element and calculate
                // intermediate elements of lower diagonal matrix L.
                big = 0.0;
                imax = -1;
                for (i = j; i < 3; i++) {
                    target = mtx + (3*i) + j;
                    sum = matrix0[target];
                    k = j;
                    p1 = mtx + (3*i);
                    p2 = mtx + j;
                    while (k-- != 0) {
                        sum -= matrix0[p1] * matrix0[p2];
                        p1++;
                        p2 += 3;
                    }
                    matrix0[target] = sum;

                    // Is this the best pivot so far?
                    if ((temp = row_scale[i] * Math.abs(sum)) >= big) {
                        big = temp;
                        imax = i;
                    }
                }

                if (imax < 0) {
                    throw new RuntimeException("Matrix3d13");
                }

                // Is a row exchange necessary?
                if (j != imax) {
                    // Yes: exchange rows
                    k = 3;
                    p1 = mtx + (3*imax);
                    p2 = mtx + (3*j);
                    while (k-- != 0) {
                        temp = matrix0[p1];
                        matrix0[p1++] = matrix0[p2];
                        matrix0[p2++] = temp;
                    }

                    // Record change in scale factor
                    row_scale[imax] = row_scale[j];
                }

                // Record row permutation
                row_perm[j] = imax;

                // Is the matrix singular
                if (matrix0[(mtx + (3*j) + j)] == 0.0) {
                    return false;
                }

                // Divide elements of lower diagonal matrix L by pivot
                if (j != (3-1)) {
                    temp = 1.0 / (matrix0[(mtx + (3*j) + j)]);
                    target = mtx + (3*(j+1)) + j;
                    i = 2 - j;
                    while (i-- != 0) {
                        matrix0[target] *= temp;
                        target += 3;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Solves a set of linear equations.  The input parameters "matrix1",
     * and "row_perm" come from luDecompostionD3x3 and do not change
     * here.  The parameter "matrix2" is a set of column vectors assembled
     * into a 3x3 matrix of floating-point values.  The procedure takes each
     * column of "matrix2" in turn and treats it as the right-hand side of the
     * matrix equation Ax = LUx = b.  The solution vector replaces the
     * original column of the matrix.
     *
     * If "matrix2" is the identity matrix, the procedure replaces its contents
     * with the inverse of the matrix from which "matrix1" was originally
     * derived.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling,
    //	      _Numerical_Recipes_in_C_, Cambridge University Press,
    //	      1988, pp 44-45.
    //
    static void luBacksubstitution(double[] matrix1,
                                   int[] row_perm,
                                   double[] matrix2) {

        int i, ii, ip, j, k;
        int rp;
        int cv, rv;

        //	rp = row_perm;
        rp = 0;

        // For each column vector of matrix2 ...
        for (k = 0; k < 3; k++) {
            //	    cv = &(matrix2[0][k]);
            cv = k;
            ii = -1;

            // Forward substitution
            for (i = 0; i < 3; i++) {
                double sum;

                ip = row_perm[rp+i];
                sum = matrix2[cv+3*ip];
                matrix2[cv+3*ip] = matrix2[cv+3*i];
                if (ii >= 0) {
                    //		    rv = &(matrix1[i][0]);
                    rv = i*3;
                    for (j = ii; j <= i-1; j++) {
                        sum -= matrix1[rv+j] * matrix2[cv+3*j];
                    }
                }
                else if (sum != 0.0) {
                    ii = i;
                }
                matrix2[cv+3*i] = sum;
            }

            // Backsubstitution
            //	    rv = &(matrix1[3][0]);
            rv = 2*3;
            matrix2[cv+3*2] /= matrix1[rv+2];

            rv -= 3;
            matrix2[cv+3*1] = (matrix2[cv+3*1] -
                    matrix1[rv+2] * matrix2[cv+3*2]) / matrix1[rv+1];

            rv -= 3;
            matrix2[cv+4*0] = (matrix2[cv+3*0] -
                    matrix1[rv+1] * matrix2[cv+3*1] -
                    matrix1[rv+2] * matrix2[cv+3*2]) / matrix1[rv+0];

        }
    }


    public static void invertAffine(Matrix3d src, Matrix3d dst) {
        // COMPUTE ADJOINT COFACTOR MATRIX FOR THE ROTATION/SCALE 3x3 SUBMATRIX

        for (int i = 0 ; i < 3 ; i++)
            for (int j = 0 ; j < 3 ; j++) {
                int iu = (i + 1) % 3, iv = (i + 2) % 3;
                int ju = (j + 1) % 3, jv = (j + 2) % 3;
                dst.setElement(j,i,src.getElement(iu,ju) * src.getElement(iv,jv) - src.getElement(iu,jv) * src.getElement(iv,ju));
            }

        // RENORMALIZE BY DETERMINANT TO INVERT ROTATION/SCALE SUBMATRIX

        double det = src.getElement(0,0)*dst.getElement(0,0) + src.getElement(1,0)*dst.getElement(0,1) + src.getElement(2,0)*dst.getElement(0,2);
        for (int i = 0 ; i < 3 ; i++)
            for (int j = 0 ; j < 3 ; j++)
                dst.setElement(i,j, dst.getElement(i,j) / det);

        // INVERT TRANSLATION

        for (int i = 0 ; i < 3 ; i++)
            dst.setElement(i,3,-dst.getElement(i,0)*src.getElement(0,3) - dst.getElement(i,1)*src.getElement(1,3) - dst.getElement(i,2)*src.getElement(2,3));

        // NO PERSPECTIVE

        for (int i = 0 ; i < 4 ; i++)
            dst.setElement(3,i, i < 3 ? 0 : 1);
    }

    public static void invertAffine2(Matrix3d src, Matrix3d dst) {
        // COMPUTE ADJOINT COFACTOR MATRIX FOR THE ROTATION/SCALE 3x3 SUBMATRIX

        for (int i = 0 ; i < 3 ; i++)
            for (int j = 0 ; j < 3 ; j++) {
                int iu = (i + 1) % 3, iv = (i + 2) % 3;
                int ju = (j + 1) % 3, jv = (j + 2) % 3;
                dst.setElement(i,j,src.getElement(ju,iu) * src.getElement(jv,iv) - src.getElement(ju,iv) * src.getElement(jv,iu));
            }

        // RENORMALIZE BY DETERMINANT TO INVERT ROTATION/SCALE SUBMATRIX

        double det = src.getElement(0,0)*dst.getElement(0,0) + src.getElement(0,1)*dst.getElement(1,0) + src.getElement(0,2)*dst.getElement(2,0);
        for (int i = 0 ; i < 3 ; i++)
            for (int j = 0 ; j < 3 ; j++)
                dst.setElement(j,i, dst.getElement(j,i) / det);

        // INVERT TRANSLATION

        for (int i = 0 ; i < 3 ; i++)
            dst.setElement(3,i,-dst.getElement(0,i)*src.getElement(3,0) - dst.getElement(1,i)*src.getElement(3,1) - dst.getElement(2,i)*src.getElement(3,2));

        // NO PERSPECTIVE

        for (int i = 0 ; i < 4 ; i++)
            dst.setElement(i,3, i < 3 ? 0 : 1);
    }


    static final double DEFAULT_GAUSS_THRESHOLD = 0.01;

    /**
       returns normalized 1D gaussian kernel truncated to finite width 
       exp( -1/2 (x/s)^2)
       threshold - values below threshold are 0.
     */
    public static double[] getGaussianKernel(double sigma){
        return getGaussianKernel(sigma, DEFAULT_GAUSS_THRESHOLD);
    }
    public static double[] getGaussianKernel(double sigma, double threshold){
        
        double s2 = sigma * sigma; 
        threshold = Math.abs(threshold); 
        if(threshold == 0.) 
            threshold = DEFAULT_GAUSS_THRESHOLD;
        if(threshold >= 1.) 
            threshold = 0.999;
        
        double xmax = Math.sqrt(2 * s2 * Math.abs(Math.log(threshold)));

        int imax = (int)Math.floor(xmax);
        double kernel[] = new double[2*imax + 1];
        double sum = 0.;
        for(int i = -imax; i <= imax; i++){
            double v = Math.exp(-0.5*(i*i/s2));            
            kernel[i + imax] = v;
            sum += v;
        }
        double norm = 1./sum;
        for(int i = 0; i < (2*imax +1); i++){
            kernel[i] *= norm;
        }
        return kernel;
    }


    
    /**
       return kernel for box smoothing
     */
    public static double[] getBoxKernel(int width){
        double kernel[] = new double[2*width+1];
        double w = 1./(kernel.length);
        for(int i =0; i < kernel.length; i++){
            kernel[i] = w;
        }
        return kernel;
    }



    /**
       return distance between two vectors 
     */
    public static double distance(Vector3d v0, Vector3d v1){

        return getDistance(v0,v1);

    }

    /**
       return mid point of 2 vectors 
     */
    public static Vector3d midPoint(Vector3d v0, Vector3d v1){
        
        return new Vector3d((v0.x + v1.x)/2,(v0.y + v1.y)/2,(v0.z + v1.z)/2);

    }


    // linear intepolation
    // x < -1 return 1;
    // x >  1 returns 0
    public static final double _interpolate_linear(double x){

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
      step from 0 to 1

    1                          _____________________
                              /
                             /
                            .
                           /.
     0 ___________________/ .

                            0
     */
    public static final double step01(double x, double vs){

        if(x <= -vs)
            return 0.;

        if(x >=  vs)
            return 1.;

        return (x+vs)/(2*vs);

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
      step from 1 to 0

    1     _________
                   \
                    \
                     .
                      \
     0               . \_______________

                     0
    */
    public static final double step10(double x, double vs){

        if(x <=  -vs)
            return 1.;

        if(x >=  vs)
            return 0.;

        return (vs-x)/(2*vs);

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
    // smooth cubic polynom between (0, 1) on interval (-1,1)
    public static final double interpolate_cubic(double x){

        if( x <= -1) return 1;
        else if( x >= 1)  return 0;
        else 
            return 0.25*x*(x*x - 3.) + 0.5;
    }

    /**
       moves x inside of interval (0,y) by subtracting transslating x into right direction by integer number of interval
     */
    public static final double toInterval(double x, double y){

        return (x - y*Math.floor(x/y));

    }
    
    /**
       returns interpolated value for box with given boundaries 
     */
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

    /**
       normalized 3D part of 4D vector 
     */    
    static public final void normalizePlane(Vector4d p){
        double norm = sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
        p.scale(1./norm);
        
    }

    /**
       calculate determinant of 3x3 matix represented as 1D array 
     */
    static double determinant3(double m[]){
        return 
            + m[M00] * (m[M11]*m[M22] - m[M12]*m[M21])
            - m[M10] * (m[M01]*m[M22] - m[M21]*m[M02]) 
            + m[M20] * (m[M01]*m[M12] - m[M11]*m[M02]);
        
    }


    /**
       solves system of 3 linear equation with 3 variables
       M X = C
       stores result in X 
       @param M - matrix stored as linear array 
       
       m00*x0  + m01*x1 + m02*x2 = c0
       m10*x0  + m11*x1 + m12*x2 = c1
       m20*x0  + m21*x1 + m22*x2 = c2        
       
       return false if equation has no unique solution exists (determinant is 0) 
       
     */

    public static boolean solveLinear3(double m[], double c[], double x[]){
        double det = determinant3(m);
        if(Math.abs(det) < EPS) 
            return false;
            
        // calculate minors 
        double d0 = 
            + c[0] * (m[M11]*m[M22] - m[M21]*m[M12])
            - c[1] * (m[M01]*m[M22] - m[M21]*m[M02]) 
            + c[2] * (m[M01]*m[M12] - m[M11]*m[M02]);
        double d1 = 
            - c[0] * (m[M10]*m[M22] - m[M20]*m[M12])
            + c[1] * (m[M00]*m[M22] - m[M20]*m[M02]) 
            - c[2] * (m[M00]*m[M12] - m[M10]*m[M02]);
        
        double d2 = 
            + c[0] * (m[M10]*m[M21] - m[M20]*m[M11])
            - c[1] * (m[M00]*m[M21] - m[M20]*m[M01]) 
            + c[2] * (m[M00]*m[M11] - m[M10]*m[M01]);

        x[0] = d0/det;
        x[1] = d1/det;
        x[2] = d2/det;

        return true;
    }

    /**
       calculated result of multiplication of 3x3 matrix and vector 3 vector
       y = M x 
       y0 = m00*x0 + m01*x1 + m02*x2 
       y1 = m10*x0 + m11*x1 + m12*x2 
       y2 = m20*x0 + m21*x1 + m22*x2 
       
     */
    public static void multMV3(double m[], double x[], double y[]){

        y[0] = m[M00] * x[0] + m[M01] * x[1] + m[M02] * x[2];
        y[1] = m[M10] * x[0] + m[M11] * x[1] + m[M12] * x[2];
        y[2] = m[M20] * x[0] + m[M21] * x[1] + m[M22] * x[2];
        
    }
    
    /**
       multiply two matrices M and N and place result in R
       r = m  * n 
     */
    public static void multMM3(double m[], double n[], double r[]){

        r[M00] = m[M00]*n[M00] + m[M01]*n[M10] + m[M02]*n[M20];
        r[M10] = m[M10]*n[M00] + m[M11]*n[M10] + m[M12]*n[M20];
        r[M20] = m[M20]*n[M00] + m[M21]*n[M10] + m[M22]*n[M20];

        r[M01] = m[M00]*n[M01] + m[M01]*n[M11] + m[M02]*n[M21];
        r[M11] = m[M10]*n[M01] + m[M11]*n[M11] + m[M12]*n[M21];
        r[M21] = m[M20]*n[M01] + m[M21]*n[M11] + m[M22]*n[M21];

        r[M02] = m[M00]*n[M02] + m[M01]*n[M12] + m[M02]*n[M22];
        r[M12] = m[M10]*n[M02] + m[M11]*n[M12] + m[M12]*n[M22];
        r[M22] = m[M20]*n[M02] + m[M21]*n[M12] + m[M22]*n[M22];
        
    }

    /**
       copies matrix m into n x
     */
    public static final double[] copyMatrix3(double m[], double n[]) {
        
        if(n == null) n = new double[m.length];
        System.arraycopy(m, 0, n, 0, m.length);
        return n;
    }

    public static final double[] copyMatrix3(Matrix3d m, double n[]) {
        
        if(n == null) n = new double[9];
        n[M00] = m.m00; n[M01] = m.m01; n[M02] = m.m02;
        n[M10] = m.m10; n[M11] = m.m11; n[M12] = m.m12;
        n[M20] = m.m20; n[M21] = m.m21; n[M22] = m.m22;
        return n;
    }

    public static final Matrix3d copyMatrix3(double n[], Matrix3d m) {
        if(m == null) m = new Matrix3d(); 
        m.m00 = n[M00]; m.m01 = n[M01]; m.m02 = n[M02]; 
        m.m10 = n[M10]; m.m11 = n[M11]; m.m12 = n[M12]; 
        m.m20 = n[M20]; m.m21 = n[M21]; m.m22 = n[M22]; 
        return m;
    }

    public static final double[] copyVector3(Vector3d v, double c[]) {
        if(c == null)
            c = new double[3];
        c[0] = v.x;
        c[1] = v.y;
        c[2] = v.z;
        return c;
    }

    /**
       invert 3x3 matrix represented as linear array 
       stores result in the same matrix 
       @return 1 if success or 0 if matrix is non invertible 
     */
    public static final int invertMatrix3(double m[]) {
        
        double det = determinant3(m);
        if(det == 0.0){ // it is probably safe enough to compare with 0.0  
            return 0;
        }
        det = 1./det;
        double 
            m00 = (m[M11]*m[M22] - m[M21]*m[M12])*det,
            m01 = (m[M02]*m[M21] - m[M22]*m[M01])*det,
            m02 = (m[M01]*m[M12] - m[M11]*m[M02])*det,

            m10 = (m[M12]*m[M20] - m[M22]*m[M10])*det,
            m11 = (m[M00]*m[M22] - m[M20]*m[M02])*det,
            m12 = (m[M02]*m[M10] - m[M12]*m[M00])*det,

            m20 = (m[M10]*m[M21] - m[M20]*m[M11])*det,
            m21 = (m[M01]*m[M20] - m[M21]*m[M00])*det,
            m22 = (m[M00]*m[M11] - m[M10]*m[M01])*det;

        m[M00] = m00;
        m[M01] = m01;
        m[M02] = m02;
        m[M10] = m10;
        m[M11] = m11;
        m[M12] = m12;
        m[M20] = m20;
        m[M21] = m21;
        m[M22] = m22;

        return 1;
    }


    /**
       set m to be unit matrix and return it.
     */
    public static final double[] getUnitMatrix3(double m[]) {
        if(m == null) m = new double[9];
        
        m[M00] = 1; m[M01] = 0; m[M02] = 0;
        m[M10] = 0; m[M11] = 1; m[M12] = 0;
        m[M20] = 0; m[M21] = 0; m[M22] = 1;

        return m;
    }

    public static final String fmtMatrix3(String format, double m[]) {
        return fmt("(%s %s %s;%s %s %s;%s %s %s)",
                   fmt(format, m[0]),fmt(format, m[1]),fmt(format, m[2]),
                   fmt(format, m[3]),fmt(format, m[4]),fmt(format, m[5]),
                   fmt(format, m[6]),fmt(format, m[7]),fmt(format, m[8])
                   );
    }

    /**
       calculates maximal distance between 2 arrays 
     */
    public static double maxDistance(double x[], double y[]){

        int n = x.length;
        double maxDist = 0;
        for(int i = 0; i < n; i++){
            double d = Math.abs(x[i] - y[i]);
            if(d > maxDist) 
                maxDist = d;
        }
        return maxDist;
    }

    /**
       calculates best plane via given 3D points 
       @param coord - coordinates of points stored in flat array x0,y0,z0,x1,y,1,z1,...
       @param m - working array of length 9 
       @param c - working array of length 3 
       @param - coefficients of of equation of plane: px * x + py * y + pz * z + 1 = 0;

       we minimize the sum of distances of point to the plane 
       sum_i( px * xi + py * yi + pz * zi + 1)^2 = minimum 
       
       
    */
    public static boolean getBestPlane(double coord[], double m[], double c[], double plane[]){

        double sxx = 0,sxy = 0, sxz = 0, syy = 0, syz = 0, szz = 0, sx = 0, sy = 0, sz = 0;
        int n = coord.length/3;
        
        if(n < 3) {
            return false;
        }

        for(int i = 0, k = 0; i < n; i++, k += 3){
            
            double x = 
                coord[k],
                y = coord[k+1],
                z = coord[k+2];
            
            sx += x;
            sy += y;
            sz += z;
            sxx += x*x;
            sxy += x*y;
            sxz += x*z;
            syy += y*y;
            syz += y*z;
            szz += z*z;

        }

        // try plane equation ax + by + cz + 1 = 0;
        c[0] = -sx;
        c[1] = -sy;
        c[2] = -sz;
        m[M00] = sxx; m[M01] = sxy; m[M02] = sxz;
        m[M10] = sxy; m[M11] = syy; m[M12] = syz;
        m[M20] = sxz; m[M21] = syz; m[M22] = szz;

        if(solveLinear3(m, c, plane)){            
            plane[3] = 1;
            normalizePlane(plane);
            return true;
        }

        // try plane equation ax + by + z + d = 0;

        c[0] = -sxz;
        c[1] = -syz;
        c[2] = -sz;
        m[M00] = sxx; m[M01] = sxy; m[M02] = sx;
        m[M10] = sxy; m[M11] = syy; m[M12] = sy;
        m[M20] = sx;  m[M21] = sy;  m[M22] = n;
        if(solveLinear3(m, c, plane)){  
            double d = plane[2];
            plane[2] = 1;
            plane[3] = d;
            normalizePlane(plane);
            return true;
        }

        // try plane equation ax + y + cz + d = 0;
        c[0] = -sxy;
        c[1] = -sy;
        c[2] = -syz;
        m[M00] = sxx; m[M01] = sx; m[M02] = sxz;
        m[M10] = sx;  m[M11] = n;   m[M12] = sz;
        m[M20] = sxz; m[M21] = sz; m[M22] = szz;
        
        if(solveLinear3(m, c, plane)){  
            double d = plane[1];
            plane[1] = 1;
            plane[3] = d;
            normalizePlane(plane);
            return true;
        }

        // try plane equation x + by + cz + d = 0;
        c[0] = -sx;
        c[1] = -sxy;
        c[2] = -sxz;
        m[M00] = n;  m[M01] = sy; m[M02] = sz;
        m[M10] = sy; m[M11] = syy; m[M12] = syz;
        m[M20] = sz; m[M21] = syz; m[M22] = szz;
        
        if(solveLinear3(m, c, plane)){  
            double d = plane[0];
            plane[0] = 1;
            plane[3] = d;
            normalizePlane(plane);
            return true;
        }
        plane[0] = plane[1] = plane[2] = plane[3] = 0;
        return false;
        
    }


    /**
       make plane coefficients normaliuzed 
     */
    public static void normalizePlane(double plane[]){

        double s0 = plane[0]*plane[0];
        double s1 = plane[1]*plane[1];
        double s2 = plane[2]*plane[2];
        double s3 = plane[3]*plane[3];
        
        double s = sqrt(s0 + s1 + s2 + s3);

        plane[0] /= s;
        plane[1] /= s;
        plane[2] /= s;
        plane[3] /= s;

    }
    
    /**
       makes signed int from short stored as 2 low bytes in long
    */
    public static final int L2S(long v){
        return (int)((short)v);
    }

    /**
       makes mask with given bit count 
     */
    public static final long getBitMask(int bitCount){
        
        if(bitCount < 1) 
            return 0;
        if(bitCount >= 64)
            return 0xFFFFFFFFL;
        return ((1L << bitCount)-1);
        /*
        long mask = 0;
        for(int i = 0; i < bitCount; i++){
            mask |= (1L << i);
        }

        return ((1L << bitCount)-1);
        */
    }


    /**
       
     */
    public static final double lerp(double x1, double x2, double t){
        return x1 + t * (x2-x1);
    }


    /**
       return point where indicator function becomes 0 on the segment (p0,p1) if values of function at he ends of segment are (v0,v1) 
       // indicator function algorithm is based on 
       // 
       // J. Manson, J. Smith, and S. Schaefer (2011) 
       // Contouring Discrete Indicator Functions
       @param result - return value of intersection 
       */
    public static void intersectIF(Vector3d p0, Vector3d p1, double v0, double v1, Vector3d result){

        double c = coeffIF(0.5*(1-v0),0.5*(1-v1));
        result.x = lerp(p0.x,p1.x, c); 
        result.y = lerp(p0.y,p1.y, c); 
        result.z = lerp(p0.z,p1.z, c); 
    }

    //
    public static final double coeffIF(double v1, double v2){
        if (v1 < v2)
            return coeffIF2(v1, v2);
        else
            return 1. - coeffIF2(v2, v1);
    }
    
    public static final double coeffIF2(double v1, double v2){

        int selector = 0;
	if (3*v1 >= v2) // test 1-3
            selector += 1;
	if (v1 + 2 >= 3*v2) // test 1-4
            selector += 2;
        
	switch (selector){
            
	case 3: // must be 1
            return (v1 - .5) / (v1 - v2);  
	case 0: // must be 2
            return 1.5 - v1 - v2;            
	case 2: // test 2-3
            {
                double d = v1*(v1+v2);
                double s = 2*v1+2*v2-1;
                if (4*d > s*s)	{
                    return 1. - (2*v2 - 1) / (8*v1 + 4*v2 -8*sqrt(d)); // must be 3
                } else {
                    return 1.5 - v1 - v2; // must be 2
                }
            }
            
	case 1: // test 2-4
            {
                double b1 = 1 - v2;
                double b2 = 1 - v1;
                
                double d = b1*(b1+b2);
                double s = 2*b1+2*b2-1;
                if (4*d > s*s){
                    return (2*b2 - 1) / (8*b1 + 4*b2 - 8*sqrt(d)); // must be 4
                } else {
                    return 1.5 - v1 - v2; // must be 2
                }
            }
	}
        
	return 0;
    }
    

    /**
       return mathematic fractional part {x} = (x - [x]}
     */
    public final static double frac(double x){
        return (x - Math.floor(x));
    }

    /**
       return (v1.[v2 x v3])
     */
    final public static double tripleProduct(Vector3d v1, Vector3d v2, Vector3d v3){
        return 
            v1.x *(v2.y * v3.z - v2.z * v3.y) + 
            v1.y *(v2.z * v3.x - v2.x * v3.z) + 
            v1.z *(v2.x * v3.y - v2.y * v3.x);
    }

    /**
       convert quaternion into AxisAngle representation 
     */
    public static final AxisAngle4d getAxisAngle(Quat4d q){

        double mag = q.x*q.x + q.y*q.y + q.z*q.z;
        double x,y,z,angle;
        if ( mag > EPS2 ) {
            mag = Math.sqrt(mag);
            double invMag = 1.0/mag;
            
            x = q.x*invMag;
            y = q.y*invMag;
            z = q.z*invMag;
            angle = 2.0*Math.atan2(mag, q.w);
        } else {
            x = 0.0;
            y = 1.0;
            z = 0.0;
            angle = 0;
        }
        return new AxisAngle4d(x,y,z,angle);
    }

    /**
       naive conversion of matrix into axis angle
       it fails when angle is of rotation is 180
     */
    static AxisAngle4d getAxisAngle_v0(Matrix3d m){

        double x,y,z,angle;

        x = (m.m21 - m.m12);
        y = (m.m02 - m.m20);
        z = (m.m10 - m.m01);

        double mag = x*x + y*y + z*z;
        
        if (mag > EPS2 ) {
            mag = Math.sqrt(mag);

            double sin = 0.5*mag;
            double cos = 0.5*(m.m00 + m.m11 + m.m22 - 1.0);            
            angle = Math.atan2(sin, cos);

            double invMag = 1.0/mag;
            x = x*invMag;
            y = y*invMag;
            z = z*invMag;
        } else {
            // this is wrong, does not handles case of rotation by 180 
            x = 0.0;
            y = 1.0;
            z = 0.0;
            angle = 0.0;
        }
        return new AxisAngle4d(x,y,z,angle);

    }

    /**
       @return axis and angle or rotation represented as matrix 
     */
    public static AxisAngle4d getAxisAngle(Matrix3d m){

        double q0=0, q1, q2, q3;
        double trace = m.m00 + m.m11 + m.m22;
        //printf("trace: %7.5f\n", trace);

        if(trace > 0) {
            q0 = 0.5*sqrt(trace + 1);
            q1 = (m.m21 - m.m12)/(4*q0);
            q2 = (m.m02 - m.m20)/(4*q0);
            q3 = (m.m10 - m.m01)/(4*q0);
        } else { // negative trace 
            q1 = 0.5*sqrt(1 + m.m00 - m.m11 - m.m22);
            q2 = 0.5*sqrt(1 + m.m11 - m.m22 - m.m00);
            q3 = 0.5*sqrt(1 + m.m22 - m.m00 - m.m11);
            double a1 = abs(q1);
            double a2 = abs(q2);
            double a3 = abs(q3);            
            if(a1 >= a2 && a1 >= a3) {
                //printf("trace1: %7.5f\n", trace);            
                // a1 is the largest
                q0 = (m.m21 - m.m12)/(4*q1);
                q2 = (m.m10 + m.m01)/(4*q1);
                q3 = (m.m02 + m.m20)/(4*q1);
            } else if (a2 >= a1 && a2>= a3){
                //printf("trace2: %7.5f\n", trace);            
                // a2 is the largest                
                q0 = (m.m02 - m.m20)/(4*q2);
                q1 = (m.m01 + m.m10)/(4*q2);                
                q3 = (m.m12 + m.m21)/(4*q2);                
            } else if(a3 >= a1 && a3 >= a2){
                //printf("trace3: %7.5f\n", trace);            
                // a3 is the largest
                q0 = (m.m10 - m.m01)/(4*q3);
                q1 = (m.m20 + m.m02)/(4*q3);                
                q2 = (m.m12 + m.m21)/(4*q3);
            }            
        } /// negative trace 

        return getAxisAngle(new Quat4d(q1,q2,q3,q0));
    }

    /**
       @return axis and angle or rotation which rotates basis vectors standard orthogonal basis (1,0,0),(0,1,0),(0,0,1) into orthonormal basis (v1, v2, v3)
     */
    public static AxisAngle4d getAxisAngle(Vector3d v1, Vector3d v2, Vector3d v3){

        Matrix3d m = new Matrix3d(v1.x,v2.x, v3.x,v1.y,v2.y,v3.y,v1.z,v2.z,v3.z);
        return getAxisAngle(m);
        
    }
}
