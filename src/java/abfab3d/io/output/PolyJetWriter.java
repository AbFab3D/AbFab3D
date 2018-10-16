/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011-2018
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fxi it.
 *
 ****************************************************************************/

package abfab3d.io.output;

import javax.vecmath.Vector3d;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;


// external imports


// Internal Imports
import abfab3d.core.Vec;
import abfab3d.core.Color;
import abfab3d.core.DataSource;
import abfab3d.core.Bounds;
import abfab3d.core.Initializable;
import abfab3d.core.MathUtil;

import abfab3d.param.Parameter;
import abfab3d.param.SNodeParameter;
import abfab3d.param.DoubleParameter;
import abfab3d.param.StringParameter;
import abfab3d.param.BaseParameterizable;



import abfab3d.util.ImageUtil;


import abfab3d.grid.op.ImageLoader;
import abfab3d.datasources.ImageColorMap;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Units.IN;

import static abfab3d.util.ImageUtil.makeARGB;

import static java.lang.Math.sqrt;

/**
   class to export DataSource into slices for PolyJet
*/
public class PolyJetWriter extends BaseParameterizable {

    static final boolean DEBUG = true;


    static final double SLICE_THICKNESS_HR =0.014*MM;
    static final double PIXEL_SIZE_X = IN/600;
    static final double PIXEL_SIZE_Y = IN/300;

    SNodeParameter mp_model = new SNodeParameter("model");
    DoubleParameter mp_xmin = new DoubleParameter("xmin", "xmin", 0);
    DoubleParameter mp_xmax = new DoubleParameter("xmax", "xmax", 0);
    DoubleParameter mp_ymin = new DoubleParameter("ymin", "ymin", 0);
    DoubleParameter mp_ymax = new DoubleParameter("ymax", "ymax", 0);
    DoubleParameter mp_zmin = new DoubleParameter("zmin", "zmin", 0);
    DoubleParameter mp_zmax = new DoubleParameter("zmax", "zmax", 0);

    StringParameter mp_outFolder = new StringParameter("outFolder","/tmp/polyjet");
    StringParameter mp_outPrefix = new StringParameter("outFolder","slice");

    
    Parameter m_aparam[] = new Parameter[]{

        mp_model,
        mp_xmin, 
        mp_xmax, 
        mp_ymin, 
        mp_ymax, 
        mp_zmin, 
        mp_zmax,
    };

    public PolyJetWriter(){
        super.addParams(m_aparam);
    }
    
    Bounds getBounds(){
        
        return new Bounds(mp_xmin.getValue(),mp_xmax.getValue(),
                          mp_ymin.getValue(),mp_ymax.getValue(),
                          mp_zmin.getValue(),mp_zmax.getValue());
    }


    public void setBounds(Bounds bounds) {

        mp_xmin.setValue(bounds.xmin);
        mp_xmax.setValue(bounds.xmax);
        mp_ymin.setValue(bounds.ymin);
        mp_ymax.setValue(bounds.ymax);
        mp_zmin.setValue(bounds.zmin);
        mp_zmax.setValue(bounds.zmax);

    }

    public void setModel(DataSource model) {

        mp_model.setValue(model);
        if (model instanceof Initializable) {
            ((Initializable) model).initialize();
        }
    }
    
    Bounds m_bounds;
    double m_sliceThickness, m_vsx, m_vsy;
    int m_nx, m_ny, m_nz;
    int m_channelsCount = 7;
    DataSource m_model;

    public void write(){
        
        String outFolder = mp_outFolder.getValue();
        String outPrefix = mp_outPrefix.getValue();
        m_model = (DataSource)(mp_model.getValue());
        MathUtil.initialize(m_model);
        m_bounds = getBounds();
        long t0 = time();

        m_sliceThickness = SLICE_THICKNESS_HR;
        m_vsx = PIXEL_SIZE_X;
        m_vsy = PIXEL_SIZE_Y;

        m_nz = m_bounds.getGridDepth(m_sliceThickness);
        m_nx = m_bounds.getGridWidth(m_vsx);
        m_ny = m_bounds.getGridHeight(m_vsy);
        double sliceData[] = new double[m_nx*m_ny*m_channelsCount];

        if(m_nx <=0 || m_ny <= 0 || m_nz <= 0){
            throw new RuntimeException(fmt("PolyJewtWriter: illegal output bounds:%s", m_bounds.toString()));
        }
        
        if(DEBUG) {
            printf("PolyJetWriter write()\n");
            printf("              outFolder: %s\n", outFolder);
            printf("              grid: [%d x %d x %d]\n", m_nx, m_ny, m_nz);
        }

        BufferedImage image =  new BufferedImage(m_nx, m_ny, BufferedImage.TYPE_INT_ARGB);
        DataBufferInt db = (DataBufferInt)image.getRaster().getDataBuffer();
        int[] imageData = db.getData();
        
        for(int iz = 0; iz < m_nz; iz++){
            calculateSlice(iz, sliceData);
            makeImage(sliceData, imageData); 
            String outPath = fmt("%s/%s_%d.png", outFolder, outPrefix, iz);
            try {
                ImageIO.write(image, "png", new File(outPath));
            } catch(Exception e){
                throw new RuntimeException(fmt("exception while writing to %s", outPath));
            }
        }
        if(DEBUG){
            printf("PolyJetWriter write() done %d ms\n", (time()-t0));
        }
    }

    protected void calculateSlice(int iz, double sliceData[]){
        
        //if(DEBUG)printf("calculateSlice(%d)\n", iz);

        double z = m_bounds.zmin + m_sliceThickness*(iz+0.5);
        double xmin = m_bounds.xmin + m_vsx/2;
        double ymin = m_bounds.ymin + m_vsy/2;
        Vec pnt = new Vec(3);
        Vec data = new Vec(m_channelsCount);
        
        for(int iy = 0; iy < m_ny; iy++){
            double y = ymin + iy*m_vsy;
            for(int ix = 0; ix < m_nx; ix++){
                double x = xmin + ix*m_vsx;
                pnt.set(x,y,z);
                m_model.getDataValue(pnt, data);
                data.get(sliceData, (iy*m_nx + ix)*m_channelsCount);
            }
        }

        
    }

    protected void makeImage(double sliceData[], int imageData[]){
        
        for(int iy = 0; iy < m_ny; iy++){
            for(int ix = 0; ix < m_nx; ix++){
                int dataOffset = (ix + iy*m_nx)*m_channelsCount;
                int imgOffset = (ix + (m_ny-1-iy)*m_nx);
                
                if(sliceData[dataOffset] < 0.) 
                    imageData[imgOffset] = VERO_CYAN;
                else 
                    imageData[imgOffset] = BLACK_PIXEL;
            }
        }
        //if(DEBUG)printf("writeSlice(%s)\n", path);
        
    }

    static final int BLACK_PIXEL = 0xFF000000;
    static final int WHITE_PIXEL = 0xFFFFFFFF;

    // colors of physical materials 
    static int icolors_phys[][] = new int[][]{
        {0,    90, 158, 255},// VeroCyan
        {166,  33,  98, 255}, //VeroMgnt or VeroFlexMGT 
        {200, 189,   3, 255}, //VeroYellow or VeroFlexYL   
        {26,  26,  29,  255},  //VeroBlack or VeroFlexBK  
        {240, 240, 240, 255},  // VeroPureWht     
        //  {227, 233, 253,  50}, //VeroClear or VeroFlexCLR   
    };

    static final int VERO_CYAN = makeARGB(icolors_phys[0]);
    static final int VERO_MGNT = makeARGB(icolors_phys[1]);
      
}