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

// External Imports

// Internal Imports

import javax.vecmath.Matrix3d;
import javax.vecmath.SingularMatrixException;

import static java.lang.Math.sqrt;

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


    public static final double distance(Vector3d v0, Vector3d v1){
        double 
            x = v0.x - v1.x,
            y = v0.y - v1.y,
            z = v0.z - v1.z;        

        return sqrt(x*x + y*y + z*z);
    }


    public static Vector3d midPoint(Vector3d v0, Vector3d v1){
        
        return new Vector3d((v0.x + v1.x)/2,(v0.y + v1.y)/2,(v0.z + v1.z)/2);

    }

}
