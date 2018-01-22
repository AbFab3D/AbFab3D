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

import java.util.Arrays;

import abfab3d.core.Bounds;
import abfab3d.core.Grid2D;
import abfab3d.core.GridDataChannel;
import abfab3d.core.MathUtil;

import abfab3d.param.BaseParameterizable;
import abfab3d.param.DoubleParameter;
import abfab3d.param.Parameter;
import abfab3d.grid.Operation2D;


import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.Units.MM;

public class GaussianBlur extends BaseParameterizable  implements Operation2D {
    
    
    DoubleParameter mp_blurWidth = new DoubleParameter("blurWidth", 0.1*MM);
    DoubleParameter mp_threshold = new DoubleParameter("threshold", "threshold for gaussian kernel", 0.001);

    
    Parameter m_aprarm[] = new Parameter[]{
        mp_blurWidth,
        mp_threshold
    };

    public GaussianBlur(double blurWidth){
        
        mp_blurWidth.setValue(blurWidth);
    }
    
    public Grid2D execute(Grid2D grid) {
        
        GridDataChannel dataCannel = grid.getDataDesc().getChannel(0);
        Bounds bounds = grid.getGridBounds();
        double blurWidthPixels = mp_blurWidth.getValue()/grid.getVoxelSize();
        double[] kernel = MathUtil.getGaussianKernel(blurWidthPixels, mp_threshold.getValue());
        
        // TODO custom channel selection 
        GridDataChannel dataChannel = grid.getDataDesc().getChannel(0); 
        convolute(grid, dataChannel, kernel);

        return grid;
    }

    private int m_nx, m_ny;
        
    public void convolute(Grid2D grid, GridDataChannel channel, double kernel[]){
        
        m_nx = grid.getWidth();
        m_ny = grid.getHeight();
        int s = Math.max(m_nx, m_ny);
        
        double row[] = new double[s];
        
        convoluteX(grid, channel, kernel, row);
        convoluteY(grid, channel, kernel, row);
        
    }

    void convoluteX(Grid2D grid, GridDataChannel channel, double kernel[], double row[]){
                
        int w = m_nx;
        int h = m_ny;
        
        int ksize = kernel.length/2;
        int w1 = w-1;

        for(int y = 0; y < h; y++){
            
            // init accumulator array 
            Arrays.fill(row, 0, w, 0.);
            int offsety = y*w;

            for(int x = 0; x < w; x++){
                
                for(int k = 0; k < kernel.length; k++){

                    //int kx = x + k - ksize;
                    int xx = x - (k-ksize); //offsety + x + k;

                    xx = clamp(xx, 0, w1); // boundary conditions 
                    row[x] += kernel[k] * channel.getValue(grid.getAttribute(xx,y)); 
                }
            }             
            for(int x = 0; x < w; x++){
                grid.setAttribute(x,y,channel.makeAtt(row[x]));
            }                            
        }
    }
    
    void convoluteY(Grid2D grid,  GridDataChannel channel, double kernel[], double row[]){

        int w = m_nx;
        int h = m_ny;
        int ksize = kernel.length/2;
        int h1 = h-1;

        for(int x = 0; x < w; x++){
            // init accumulator array 
            Arrays.fill(row, 0, h, 0.);

            for(int y = 0; y < h; y++){                
                
                for(int k = 0; k < kernel.length; k++){
                    int yy = y - (k-ksize); 
                    yy = clamp(yy, 0, h1); // boundary conditions 
                    row[y] += kernel[k] * channel.getValue(grid.getAttribute(x,yy)); 
                }
            } 
            
            for(int y = 0; y < h; y++){
                grid.setAttribute(x,y,channel.makeAtt(row[y]));
            }                    
        }
    } // convolute y 



}