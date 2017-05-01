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

import abfab3d.core.Grid2DProducer;
import abfab3d.core.Grid2D;
import abfab3d.grid.Grid2DShort;
import abfab3d.param.SourceWrapper;
import abfab3d.param.BaseParameterizable;
import abfab3d.param.URIParameter;
import abfab3d.param.Parameter;
import abfab3d.param.DoubleParameter;


import java.awt.image.BufferedImage;

import static abfab3d.core.Units.MM;
import static abfab3d.core.Units.PT;

public class ImageWrapper extends BaseParameterizable implements SourceWrapper {
    
    BufferedImage image;
    Grid2D grid;
    static final double DEFAULT_PIXEL_SIZE = PT; // 1 point 
    //private double vs;
    private String source;

    URIParameter mp_uri = new URIParameter("uri", "image path");
    DoubleParameter mp_voxelSize = new DoubleParameter("voxelSize", "voxel size", DEFAULT_PIXEL_SIZE);
    Parameter m_param[] = new Parameter[]{
        mp_uri        
    };
    

    public ImageWrapper(BufferedImage image){
        this(image,null,DEFAULT_PIXEL_SIZE);
    }

    public ImageWrapper(BufferedImage image, double vs){
        this(image,null,vs);
    }

    public ImageWrapper(BufferedImage image, String uri, double vs){
        super.addParams(m_param);
        this.image = image;
        mp_voxelSize.setValue(vs);
        mp_uri.setValue(uri);
    }

    public int getWidth(){
        return image.getWidth();
    }
    public int getHeight(){
        return image.getHeight();
    }

    public BufferedImage getImage(){
        return image;
    }

    /**
       return grid produced from this image 
       @Override 
     */
    //public Grid2D getGrid2D(){

    //return getGrid();

    //}


    /**
     * Get a 2D grid representation of this image
     * @return
     */
    /*
    public Grid2D getGrid() {
        
        if (grid != null) return grid;

        grid = new Grid2DSourceWrapper(source,Grid2DShort.convertImageToGrid(image, vs));

        return grid;
    }
    */
    /**
     * Set the source for this wrapper.  This will be returned as the getParamString for this object until a setter is called.
     */
    public void setSource(String val) {
        this.source = val;
    }

    public String getParamString() {
        if (source == null) return toString();
        return source;
    }

    public void getParamString(StringBuilder sb) {
        if (source == null) {
            sb.append(toString());
            return;
        }
        sb.append(source);
    }
}