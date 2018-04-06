/******************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012-2014
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid.op;

import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import abfab3d.core.FontProducer;

import abfab3d.param.BaseParameterizable;
import abfab3d.param.URIParameter;
import abfab3d.param.StringParameter;
import abfab3d.param.Parameter;
import abfab3d.param.ParamCache;


import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.printf;


/**
    loader of system font 
 */
public class SystemFontLoader extends BaseParameterizable implements FontProducer {

    static final boolean DEBUG = false;
    static final boolean CACHING_ENABLED = true;

    StringParameter mp_name = new StringParameter("name", "font name");
    Parameter m_params[] = new Parameter[]{
        mp_name,
    };

    /**
       @param path - image path
     */
    public SystemFontLoader(String fontName){
        addParams(m_params);
        mp_name.setValue(fontName);
    }
    
    public Font getFont(){
        
        prepareFont();
        return m_font;

    }
    
    Font m_font;

    /**
              
       @Override
    */
    public void prepareFont(){
        
        Object co = null;
        String label = null;
        if(CACHING_ENABLED){
            label = getDataLabel();
            co = ParamCache.getInstance().get(label);
        }
        if (co == null) {
            m_font = new Font(mp_name.getValue(), Font.PLAIN, 12);
            if(CACHING_ENABLED){
                ParamCache.getInstance().put(label, m_font);
            }
        } else {
            m_font = (Font) co;
        }            
    }
    

} // class SystemFontLoader