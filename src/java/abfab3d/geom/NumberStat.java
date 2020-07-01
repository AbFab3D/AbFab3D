/** 
 *                        Shapeways, Inc Copyright (c) 2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/


package abfab3d.geom;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Units.MM;


public class NumberStat {
    
    int m_counts[];
    double m_minValue;
    double m_maxValue;
    double m_binSize;
    int m_count;

        
    public NumberStat(double minValue, double maxValue, int count){
        
        m_minValue = minValue;
        m_maxValue = maxValue;
        m_count = count;
        m_counts = new int[count+2];
        m_binSize = (maxValue - minValue)/count;
    }

    public void add(double value){
       
        if(value < m_minValue) {
            m_counts[0]++;
        } else if(value >= m_maxValue){
            m_counts[m_counts.length-1]++;            
        } else {
            int bin = (int)((value - m_minValue)/m_binSize);
            m_counts[bin+1] ++;
        }
    }

    public void printStat(){

        int totalCount = 0;
        for(int i = 0; i < m_counts.length; i++){
            totalCount += m_counts[i];
        }

        printf(" *** stat ***\n");

        printf("     < %5.3f] mm: %d\n", m_minValue/MM, m_counts[0]);

        for(int i = 1; i < m_counts.length-1; i++){

            double v0 = m_minValue + (i-1)*m_binSize;
            double v1 = v0 + m_binSize;
            int c  = m_counts[i];
            double percent = 100*((double)c/totalCount);

            printf("[%5.3f %5.3f] mm: %4.1f%% %6d\n", v0/MM, v1/MM, percent, c);
        }
        printf("      > %5.3f mm: %d\n", m_maxValue/MM, m_counts[m_counts.length-1]);

        
    }
    
}
