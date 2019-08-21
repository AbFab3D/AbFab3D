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

package abfab3d.datasources;


import abfab3d.core.Color; 
import abfab3d.core.DataSource;
import abfab3d.core.Vec;
import abfab3d.core.ResultCodes;

import abfab3d.param.Parameter;
import abfab3d.param.EnumParameter;
import abfab3d.param.ColorParameter;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;


/**
   data source extract color Co  and alpha of overlay from given resulting composed color Cr and color of the background Cb 

   the composed color is given by the equation 

   Cr = (1-alpha)*Cb + alpha*Co 

   here Cb and Cr are known and alpha and Co are unknown 
   we ave 4 unknown: Co and alpha and 3 equations 

   trivial solution is alpha = 1, Co = Cr  - ovelay is opaque copy of input color 
   
   However, we want most transparent overlay with minimal alpha value 
   we must find minimal alpha for which each coor component of output color is withing [0,1] interval 
   

   @author Vladimir Bulatov
 */
public class OverlayExtractor  extends TransformableDataSource {

    static final boolean DEBUG = false;

    ColorParameter mp_backgroundColor = new ColorParameter("backgroundColor", new Color(1,1,1,1));

    static final String overlayTypeNames[] = {"darker", "brighter", "any"};
    static final int OVERLAY_TYPE_DARKER = 0, OVERLAY_TYPE_BRIGHTER = 1, OVERLAY_TYPE_ANY = 2;

    EnumParameter mp_type = new EnumParameter("type", overlayTypeNames, overlayTypeNames[0]);
    


    Parameter m_params[] = new Parameter[]{

        mp_backgroundColor, 
        mp_type,

    };
        

    private double m_background[] = new double[4];
    private int m_type;
    
    public OverlayExtractor(){
        super.addParams(m_params);
        
    }

    public OverlayExtractor(Color background){

        super.addParams(m_params);
        mp_backgroundColor.setValue(background);
    }

    public int initialize() {

        super.initialize();
        mp_backgroundColor.getValue().getValue(m_background);
        m_type = mp_type.getSelectedIndex();

        return ResultCodes.RESULT_OK;
    }

    public int getChannelsCount(){
        return 4;
    }

    public int getBaseValue(Vec pnt, Vec data) {
        
        
        extractOverlay(m_background, pnt.v, data.v, m_type);

        return ResultCodes.RESULT_OK;
        
    }
    
    
    // arbitrary sufficiently large number 
    static final double MAXG = 1.e3;
    
    static double get_best_g(double c0, double c2, int overlayType){
        
        
        double c20 = c2 - c0; 

        if(c20 == 0.){
            return MAXG; 
        }
        switch(overlayType){

        default:
        case OVERLAY_TYPE_ANY:

            if(c20 < 0) return -c0/c20;
            else  return (1. - c0)/c20;
 
        case OVERLAY_TYPE_DARKER:
            if(c20 < 0) return -c0/c20;
            else return MAXG;

        case OVERLAY_TYPE_BRIGHTER:

            if(c20 < 0) return MAXG;
            else return (1. - c0)/c20;                     

        }
        
    }
    

    static void extractOverlay(double backcolor[], double incolor[], double outcolor[], int overlayType){
        
        // using non premult color 
        // combination of color c1 with alpha "a"  over color c0 give color c2
        // c2 = (1-a) c0 + a c1 
        // given c2, c0  we have to find best (c1, a) 
        // c0 - background 
        // c2 - image to match (incolor) 
        // trivial solution if we put a = 1; c1 = c2;
        // we want to find value "a" closest to 0, 
        // such that all colors are inside of allowed range (0, 1) 
        //
        // c2-c0 = a (c1-c0) 
        // let g = 1/a
        // (c1-c0) = g(c2-c0)
        // c1 = c0 + g(c2-c0) 
        // c1 = 0, 1 are boundary values 
        // c0 + g(c2-c0) = 0 => g = -c0/(c2-c0) 
        // c0 + g(c2-c0) = 1 => g = (1-c0)/(c2-c0)
        // 
        // we have to find minimal positive value of (g > 1) for which c1 is at the boundary [0,1]
        // gmin = minimal values of all color components 
        // a = 1/gmin 
        // c1 = c0 + gmin (c2-c0) 
        
        
        double ming = MAXG;

        for(int i = 0; i < 3; i++){

            double g = get_best_g(backcolor[i],incolor[i], overlayType);
            if(DEBUG)printf("back: %5.1f in: %5.1f -> g: %5.1f\n",backcolor[i],incolor[i],g );
            if(g < ming)
                ming = g;
        }
        //printf("ming: %5.1f\n", ming);
        double a = 1.; 
        if(ming < MAXG){
            // have transparency 
            a = 1./ming;
        } else {
            a = 0.;
        }
        
        // make output 
        if(a == 0.){
            // overlay is transparent. alpha is 0, other colors are arbitrary 
            outcolor[0] = outcolor[1] = outcolor[2] = outcolor[3] = 0;
        } else {
            for(int i = 0; i < 3; i++){
                // non premult color 
                outcolor[i] = backcolor[i] + (incolor[i] - backcolor[i])/a;
            }   
            outcolor[3] = a;
        }           
    }  // extractOverlay()
           
}