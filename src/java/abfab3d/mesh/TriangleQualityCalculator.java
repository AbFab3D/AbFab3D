/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.mesh;

import javax.vecmath.Vector3d;
import abfab3d.util.TriangleCollector;

import static abfab3d.util.Output.printf;


/**
 * Calculates the quality of triangular faces in the mesh. 
 *
 * @author Vladimir Bulatov
 */
public class TriangleQualityCalculator implements TriangleCollector {

    // area of equa;ateral triangle = COEFF*sidelen^2
    static final double COEFF = Math.sqrt(3)/4;


    double m_minQuality = 10.;
    double m_maxQuality = 0.;
    double m_totalQuality = 0.;

    int m_binCount = 100;
    int m_bins[];

    int m_triCount = 0;
    public TriangleQualityCalculator(){  
        this(100);
    }
    
    /**
       @param binCount how many bins to use for histogram 
     */
    public TriangleQualityCalculator(int binCount){  

        m_binCount = binCount;
        m_bins = new int[binCount];

    }


    public void printStat(){

        printf("TriangleQuaityCalculator.printStat()\n");
        printf("triangle count: %d\n", m_triCount);
        printf("min quality: %10.5f\n", getMinQuality());
        printf("max quality: %10.5f\n", getMaxQuality());
        printf("average quality: %10.5f\n", getAverageQuality());
        int maxCount = 0;
        for(int k = 0; k < m_bins.length; k++){

            if(m_bins[k] > maxCount)maxCount = m_bins[k];
        }

        int norm = 100; // length of visual string in characters

        printf("value count\n", m_totalQuality/m_triCount);
        for(int k = 0; k < m_binCount; k++){
            int v = m_bins[k] * norm/maxCount;
            double binValue = (k+1.)/m_binCount;
            printf("%4.2f %6d: ",binValue, m_bins[k]);
            for(int i = 0; i < v; i++){
                printf("+");
            }
            printf("\n");
        }
    }

    public double getMinQuality(){
        return m_minQuality;
    }
    public double getMaxQuality(){
        return m_maxQuality;
    }
    public double getAverageQuality(){

        return m_totalQuality/m_triCount;

    }

    Vector3d 
        v0 = new Vector3d(),
        v1 = new Vector3d(),
        v2 = new Vector3d(),
        normal = new Vector3d();
        


    public boolean addTri(Vector3d p0,Vector3d p1,Vector3d p2){

        v0.set(p0);
        v1.set(p1);
        v2.set(p2);

        v1.sub(v0);
        v2.sub(v0);
        
        normal.cross(v1,v2);
        
        double triArea = 0.5*normal.length();
        double len1 = v1.length();
        double len2 = v2.length();
        v2.sub(v1);
        double len = v2.length();

        if(len1 > len) len = len1;
        if(len2 > len) len = len2;

        double bestArea = len*len*COEFF;

        double quality = triArea / bestArea;
        if(quality < m_minQuality)m_minQuality = quality;
        if(quality > m_maxQuality)m_maxQuality = quality;
        m_totalQuality += quality;
        m_triCount++;
        
        int index = (int)Math.ceil(quality * m_binCount)-1;
        if(index < 0) index = 0;
        m_bins[index] ++;
        
        return true;

    }

    /**
     * Reset all variables so this class can be reused;
     */
    public void reset() {

    }

}
