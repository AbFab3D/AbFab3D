package abfab3d.shapejs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;



import static java.lang.Math.*;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.TORADIANS;

/**
 *
 */
public class TestReflection {

    public static Test suite() {
        return new TestSuite(TestReflection.class);
    }
    
    public void testNothing() {
        
    }

    public void devTestReflection(){
        double increment = 5;

        double etaI = 1;  // incident medium index of refrection 
        double etaT = 2.4;// transmission medium index of refrection 

        for(double angle = 0; angle <= 90.1; angle += increment){
            
            double cosT = cos(angle*TORADIANS);
            
            double ref = Reflection.reflectionCoeff(etaI, etaT, cosT);
            printf("a: %5.1f  ref: %4.3f\n", angle, ref);
            
        }


    }


    public static void main(String arg[]){
        new TestReflection().devTestReflection();
    }

}
