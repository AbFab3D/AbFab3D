/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2016
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/
package abfab3d.shapejs;


import static abfab3d.core.Output.printf;

/**
   responsible for calculation of reflected and refraction 
 */
public class Reflection {
    
    /**
       calculated Schlick approximation to reflection coefficient on the boindary of 2 medium with 
       different index of refraction 
       @param etaI refraction index of medium of incident ray 
       @param etaT refraction index of medium of transmitted ray        
       @param cosT - between normal and incident ray 
     */
    public static double reflectionCoeff(double etaI, double etaT, double cosTheta){

        double r0 = (etaT-etaI)/(etaT+etaI);

        r0 *= r0;  // Fresnel coeff for ray normal to the surface
        //printf("R0: %7.3f\n", r0);

        return r0 + (1-r0)*Math.pow((1-cosTheta),5); 
                        
    }


}