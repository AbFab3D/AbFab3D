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


import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import abfab3d.core.ResultCodes;
import abfab3d.param.ColorParameter;
import abfab3d.param.IntParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.param.Vector3dParameter;

import abfab3d.core.Bounds;
import abfab3d.core.Vec;
import abfab3d.core.Color;

import static java.lang.Math.*;

import static abfab3d.core.Output.printf;
import static abfab3d.core.MathUtil.step10;
import static abfab3d.core.MathUtil.step01;
import static abfab3d.core.MathUtil.abs3;
import static abfab3d.core.MathUtil.scale3;
import static abfab3d.core.MathUtil.floor3;
import static abfab3d.core.MathUtil.str;

import static abfab3d.core.Units.MM;


/**

   3D grid of colored boxes arranged as 3-dimensional grid 

   the size of boxes is given by cellWidth, cellHeight and cellDepth 

   boxes are separated by gap of given width and color
   
   the grid is centered at origin 

   the color of box [k,m,n] is calculated as clamp(c0 + k*cu + m*cv + n*cw,0,1)

   with given offset color c0 and three increment colors cu, cv, cw;

   colors have 4 components RGBA 

   @author Vladimir Bulatov

 */

public class ColorGrid extends TransformableDataSource {
    
    static final boolean DEBUG = false;
    
    Vector3dParameter mp_cellSize = new Vector3dParameter("cellSize",new Vector3d(10*MM, 10*MM, 10*MM));
    Vector3dParameter mp_dimension = new Vector3dParameter("dimension",new Vector3d(5,5,1));
    Vector3dParameter mp_gap = new Vector3dParameter("gap",new Vector3d(0*MM,0*MM,0*MM));
    ColorParameter mp_gapColor = new ColorParameter("gapColor",new Color(0,0,0,1));

    ColorParameter mp_c0 = new ColorParameter("c0",new Color(0,0,0,1));
    ColorParameter mp_cu = new ColorParameter("cu","color increment vector U", new Color(0.25,0,0,1));
    ColorParameter mp_cv = new ColorParameter("cv","color increment vector V", new Color(0.,0.25,0,1));
    ColorParameter mp_cw = new ColorParameter("cw","color increment vector V", new Color(0.,0.,0.25,1));

    Parameter m_aparam[] = new Parameter[]{

        mp_cellSize,
        mp_dimension,
        mp_c0, mp_cu, mp_cv, mp_cw,
        
        mp_gap,
        mp_gapColor,

    };


    /**
     * @noRefGuide
     */
    public ColorGrid(){        

        super.addParams(m_aparam);

    }

    /**
     * @noRefGuide
     */
    public int initialize() {

        super.initialize();
        
        m_cu = ((Color)mp_cu.getValue()).toVector4d();
        m_cv = ((Color)mp_cv.getValue()).toVector4d();
        m_cw = ((Color)mp_cw.getValue()).toVector4d();
        m_c0 = ((Color)mp_c0.getValue()).toVector4d();

        m_gapColor = ((Color)mp_gapColor.getValue()).toVector4d();
        m_gap = mp_gap.getValue();

        m_gap.scale(0.5);

        m_cellSize = mp_cellSize.getValue();

        m_dimension = mp_dimension.getValue();

        m_shapeSize = scale3(m_cellSize,m_dimension);
        
        m_origin = new Vector3d(m_shapeSize);
        m_origin.scale(-0.5);

        m_shapeScale = new Vector3d(1./m_shapeSize.x,1./m_shapeSize.y,1./m_shapeSize.z);

        m_bounds  = new Bounds(-m_shapeSize.x/2, m_shapeSize.x/2,-m_shapeSize.y/2, m_shapeSize.y/2,-m_shapeSize.z/2, m_shapeSize.z/2);

        return ResultCodes.RESULT_OK;

    }

    Vector4d m_cu, m_cv, m_cw, m_c0;

    Vector3d m_gap;
    Vector3d m_cellSize;
    int m_width, m_height, m_depth;
    Vector3d m_origin;
    Vector3d m_shapeSize, m_shapeScale;
    Vector3d m_dimension;

    Vector4d m_gapColor;
    
    public int getChannelsCount(){
        return 4;
    }

    /**
     * @noRefGuide
     */
    public final int getBaseValue(Vec pnt, Vec data) {

        Vector4d color = new Vector4d(m_gapColor);
        Vector3d p = new Vector3d();
        pnt.get(p);
        String format = "%7.4f";

        if(DEBUG) printf("p:%s \n", str(format,p));
                
        scale3(p,m_shapeScale,p);  // p.xyz in (-0.5, 0.5)

        if(DEBUG) printf("scaled p:%s \n", str(format,p));

        if(abs(p.x) >= 0.5 || abs(p.y) >= 0.5 || abs(p.y) >= 0.5 ){

            data.set(m_gapColor);            
            return ResultCodes.RESULT_OK;                    
        }
                    
        p.add(HALF);// p in [0,1]

        if(DEBUG) printf("shifted p:%s \n", str(format,p));
        
        scale3(p, m_dimension, p); // p in [0, dimension]
                
        if(DEBUG) printf("dim p:%s \n", str(format,p));

        Vector3d cell = new Vector3d(p);
        cell = floor3(p);  // color cell 
        p.sub(cell);  // p in [0,1]

        if(DEBUG) printf("cell p:%s \n", str(format,p));

        p.sub(HALF);  // p in [-0.5,0.5]
        scale3(p,m_cellSize, p);  // p in [-cellSize/2, cellSize/2]        

        if(DEBUG) printf("cell p:%s \n", str(format,p));
        abs3(p,p); // p in [0, cellSize/2]
        p.scale(2.);// p in [0, cellSize]
        if(DEBUG) printf("scaled p:%s \n", str(format,p));
        p.sub(m_cellSize);
        if(DEBUG) printf("sub p:%s \n", str(format,p));
        p.add(m_gap);
        if(DEBUG) printf("add gap p:%s \n", str(format,p));

        if(p.x >= 0 || p.y >= 0 || p.z >= 0){
            // we are in the gap 
            data.set(m_gapColor);            
            return ResultCodes.RESULT_OK; 
        }
        // we are inside of colored box [cell.x, cell.y, cell.z]
        //
        // color = c0 + cu * cell.x + cv * cell.y + cw * cell.z;
        //
        
        color.set(m_c0);

        Vector4d cc = new Vector4d(m_cu);
        cc.scale(cell.x);
        color.add(cc);

        cc.set(m_cv);
        cc.scale(cell.y);
        color.add(cc);

        cc.set(m_cw);
        cc.scale(cell.z);
        color.add(cc);
        
        color.clamp(0,1);
        
        data.set(color);            

        return ResultCodes.RESULT_OK; 
                    
    }

    
    static final Vector3d HALF = new Vector3d(0.5, 0.5, 0.5);

}  // class ColorBoxes

