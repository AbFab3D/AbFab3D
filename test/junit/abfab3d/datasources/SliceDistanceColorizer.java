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

import abfab3d.core.DataSource;
import abfab3d.core.Vec;
import abfab3d.core.ResultCodes;


import abfab3d.util.ColorMapperDistance;
import abfab3d.util.ColorMapper;


import static abfab3d.util.ImageUtil.getRGBA;

public class SliceDistanceColorizer extends TransformableDataSource {
        
    DataSource m_source;
    ColorMapper m_colorMapper;
    
    public SliceDistanceColorizer(DataSource source, ColorMapper colorMapper){
        m_colorMapper = colorMapper;
        m_source = source;
        
    }
    
    public int getBaseValue(Vec pnt, Vec data){
        
        m_source.getDataValue(pnt, data);
        
        int argb = m_colorMapper.getColor(data.v[0]); 
        getRGBA(argb, data.v);
        return ResultCodes.RESULT_OK;
    }
        
} // static class SliceDistanceColorizer 
