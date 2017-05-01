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

package abfab3d.grid.op;

import abfab3d.core.DataSource;
import abfab3d.core.Vec;
import abfab3d.core.Grid2D;
import abfab3d.core.GridDataDesc;
import abfab3d.core.AttributePacker;

import abfab3d.param.BaseParameterizable;
import abfab3d.param.SNodeParameter;
import abfab3d.param.Parameter;
import abfab3d.param.BaseParameterizable;
import abfab3d.grid.Operation2D;



public class GridValueTransformer extends BaseParameterizable  implements Operation2D {
    
    
    SNodeParameter mp_transformer = new SNodeParameter("transformer");
    Parameter m_aparam[] = new Parameter[]{
        mp_transformer
    };

    public GridValueTransformer(DataSource transformer){
        super.addParams(m_aparam);
        mp_transformer.setValue(transformer);
    }
    
    public Grid2D execute(Grid2D grid) {
        
        DataSource dataTrans = (DataSource)mp_transformer.getValue();
        initialize(dataTrans);
        GridDataDesc dataDesc = grid.getDataDesc();
        AttributePacker ap = dataDesc.getAttributePacker();

        Vec  pnt = new Vec(4);
        Vec  data = new Vec(4);

        int nx = grid.getWidth();
        int ny = grid.getHeight();

        for(int iy = 0; iy < ny; iy++){
            for(int ix = 0; ix < nx; ix++){                
                ap.getData(grid.getAttribute(ix,iy),pnt);
                dataTrans.getDataValue(pnt, data);
                grid.setAttribute(ix, iy, ap.makeAttribute(data));
            }
        }
        return grid;
    }

}