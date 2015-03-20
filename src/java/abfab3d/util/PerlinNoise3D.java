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

import java.util.Random;



import static java.lang.Math.floor;
import static java.lang.Math.abs;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;
import static abfab3d.util.MathUtil.toInterval;

/**
 *
 * calculates Perlin Noise in any 3D point.
 * Random 3D gradient vectors are calculated on cubic grid. 
 * 
 *  it generates noise values approx in range (-0.8, 0.8) 
 * @author Vladimir Bulatov
 *
 */
public class PerlinNoise3D { 

    static final boolean DEBUG = true;

    int nx, ny, nz;
    int nxy;
    double grad[];
    
    /**
       @param nx size of grid in x direction 
       @param ny size of grid in y direction 
       @param nz size of grid in z direction 
       @param seed seed to be used for random numbers generator
     */
    public PerlinNoise3D(int nx, int ny, int nz, int seed){
        if(nx < 1 || ny < 1 || nz < 1 )
            throw new RuntimeException(fmt("ilegal arguments in PerlinNoise3D(%d, %d, %d, %d",nx,ny,nz,seed));
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
        nxy = nx*ny;
        this.grad = makeGradients( seed);
    }

    public PerlinNoise3D(int nx, int ny, int nz, double gradients[]){
        if(nx < 1 || ny < 1 || nz < 1 )
            throw new RuntimeException(fmt("ilegal arguments in PerlinNoise3D(%d, %d, %d, gradients)",nx,ny,nz));
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
        this.nxy = nx*ny;
        this.grad = gradients;
    }

    private double[] makeGradients(int seed){

        int nxyz3 = nx*ny*nz*3;
        double grad[] = new double[nxyz3];

        nxy = nx*ny;

        Random rnd = new Random(seed);
        for(int i = 0; i < nxyz3; i++){
            grad[i] = 2*rnd.nextDouble()-1;
        }        
        return grad;
    }

    public double[] getGradients() {
        return grad;
    }

    /**
       @return noise value 
     */
    public double get(double x,double y,double z){

        //if(DEBUG) printf("%5.2f, %5.2f, %5.2f )\n", x, y, z);
        x = toInterval(x,nx);
        y = toInterval(y,ny);
        z = toInterval(z,nz);
        //if(DEBUG) printf("%5.2f, %5.2f, %5.2f )\n", x, y, z);

        // half voxel shift 
        x -= 0.5;
        y -= 0.5;
        z -= 0.5;
        
        int 
            ix = (int)floor(x),
            iy = (int)floor(y),
            iz = (int)floor(z),
            ix1 = ix+1,
            iy1 = iy+1,
            iz1 = iz+1;
        //if(DEBUG) printf("1) ix:(%2d %2d %2d) ix1:(%2d %2d %2d)\n", ix, iy, iz,ix1, iy1, iz1);
        
        double 
            dx = x - ix,
            dy = y - iy,
            dz = z - iz,
            dx1 = dx-1.,
            dy1 = dy-1.,
            dz1 = dz-1.;
        //  periodical wrap 
        if(ix < 0 ) ix += nx;
        if(iy < 0 ) iy += ny;
        if(iz < 0 ) iz += nz;
        if(ix1 >= nx ) ix1 -= nx;
        if(iy1 >= ny ) iy1 -= ny;
        if(iz1 >= nz ) iz1 -= nz;
        //if(DEBUG) printf("2) ix:(%2d %2d %2d) ix1:(%2d %2d %2d)\n", ix, iy, iz,ix1, iy1, iz1);
       
        int g000 = grad(ix, iy, iz),
            g100 = grad(ix1,iy, iz),
            g110 = grad(ix1,iy1,iz),
            g010 = grad(ix,iy1, iz),
            g001 = grad(ix, iy, iz1),
            g101 = grad(ix1,iy, iz1),
            g111 = grad(ix1,iy1,iz1),
            g011 = grad(ix, iy1,iz1);

        double 
            m000 = magnitude(g000,dx, dy, dz),
            m100 = magnitude(g100,dx1,dy, dz),
            m110 = magnitude(g110,dx1,dy1,dz),
            m010 = magnitude(g010,dx, dy1,dz),
            m001 = magnitude(g001,dx, dy, dz1),
            m101 = magnitude(g101,dx1,dy, dz1),
            m111 = magnitude(g111,dx1,dy1,dz1),
            m011 = magnitude(g011,dx, dy1,dz1);
        double 
            w000 = weight( dx,  dy,  dz),
            w100 = weight(-dx1, dy,  dz),
            w010 = weight( dx, -dy1, dz),
            w110 = weight(-dx1,-dy1, dz),
            w001 = weight( dx,  dy, -dz1),
            w101 = weight(-dx1, dy, -dz1),
            w011 = weight( dx, -dy1,-dz1),
            w111 = weight(-dx1,-dy1,-dz1);
        
        return 
            w000*m000 + w100*m100 + w110*m110 + w010*m010 +
            w001*m001 + w101*m101 + w111*m111 + w011*m011;            
    }    
    
    // offset to gradient components 
    private final int grad(int x, int y, int z){
        return (x + y*nx + z * nxy)*3;
    }
    

    double weight(double dx, double dy, double dz){
        return cubicInterp(1-dx)*cubicInterp(1-dy)*cubicInterp(1-dz); 

    }

    // smooth interpolation from (0 to 1) inside interval (0,1) 
    static final double cubicInterp(double t) {
        return t*t *(3 - 2*t); 
    }
    double magnitude(int g, double dx, double dy, double dz){
        return grad[g]*dx + grad[g+1]*dy + grad[g+2]*dz;
    }

}


