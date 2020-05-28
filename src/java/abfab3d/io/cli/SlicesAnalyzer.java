/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2019
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.io.cli;

import abfab3d.core.Bounds;

import static abfab3d.core.Output.printf;
import static java.lang.Math.min;
import static java.lang.Math.max;


/**
   calculates bounds and volume of slices 
 */
public class SlicesAnalyzer {
    
    
    SliceLayer m_slices[];

    double m_volume;
    Bounds m_bounds;

    public SlicesAnalyzer(SliceReader reader){

        m_slices = reader.getSlices();
        init();
    }

    
    void init(){
 
        double slicesArea = 0;
        double sliceStep = (m_slices[m_slices.length-1].getLayerHeight() - m_slices[0].getLayerHeight())/(m_slices.length-1);
        double a = Double.MAX_VALUE;

        Bounds bounds = new Bounds(a,-a,a,-a,a,-a);

        for(int i = 0; i < m_slices.length; i++){

            SliceLayer layer = m_slices[i];
            double layerZ = layer.getLayerHeight();

            PolyLine[] lines = layer.getPolyLines();
            
            Bounds layerBounds = new Bounds(a,-a,a,-a,a,-a);

            layerBounds.zmin = layerZ;
            layerBounds.zmax = layerZ;
            
            for(int k = 0; k < lines.length; k++){
                
                slicesArea += calcArea(lines[k]);
                
                double[] pnt = lines[k].getPoints();

                for(int m = 0; m < pnt.length; m += 2){

                    layerBounds.xmin = min(layerBounds.xmin, pnt[m]);
                    layerBounds.ymin = min(layerBounds.ymin, pnt[m+1]);
                    layerBounds.xmax = max(layerBounds.xmax, pnt[m]);
                    layerBounds.ymax = max(layerBounds.ymax, pnt[m+1]);
                }

            }
            
            bounds.combine(layerBounds);
            
        }                                    

        m_volume = slicesArea * sliceStep;
        m_bounds = bounds;
    }

    /**
       @return bounds of the layers
     */
    public Bounds getBounds(){

        return m_bounds;

    }

    /**
       @return volume of the layers
     */
    public double getVolume(){
        return m_volume;
    }

    public static double calcArea(PolyLine poly) {
        
        double area = 0;   // Accumulates area

        double[] points = poly.getPoints();
        int numPoints = points.length / 2;

        int j = numPoints - 1;
        for (int i=0; i < numPoints; i++) {

            area +=  (points[j*2]+points[i*2]) * (points[i*2+1]-points[j*2+1]);
            j = i;  //j is previous vertex to i
        }

        return area/2;
    }

}
