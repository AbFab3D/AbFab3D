/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
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

// External Imports


import javax.vecmath.Vector3d;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;


// external imports


// Internal Imports
import abfab3d.core.Bounds;
import abfab3d.core.Vec;
import abfab3d.core.Color;
import abfab3d.core.DataSource;
import abfab3d.util.ImageUtil;


import abfab3d.grid.op.ImageLoader;
import abfab3d.datasources.ImageColorMap;
import abfab3d.datasources.Torus;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Units.CM;

import static abfab3d.util.ImageUtil.makeARGB;

import static java.lang.Math.sqrt;


/**
 * Tests the functionality of SlicesWriter
 *
 * @version
 */
public class TestPolyJetWriter {

    public void testNothing() {

    }


    void devTestSingleImage()throws Exception {
        
        printf("devTestSingleImage()\n");

        //String inPath = "../../../PolyJet/models/01_Buddha/slice_11.png"; //  4BYTE_ABGR 
        //String inPath = "test/images/color_test.svg";
        String inPath = "test/images/color_test1.svg";
        //String inPath = "/tmp/color_test.png";
        //String inPath = "test/images/color_test.png";
        String outPath = "/tmp/color_test.png";
        int rasterWidth = 1000;
        double sizeX = 70*MM;
        double sizeZ = 10*MM;

        

        ImageLoader loader = new ImageLoader(inPath);
        loader.set("backgroundColor", new Color(0,0,0,0));
        loader.set("svgRasterizationWidth", rasterWidth);

        double sizeY = (sizeX*loader.getHeight())/loader.getWidth();
        
        BufferedImage image = loader.getImage();        

        printf("image:[%d x %d] %s\n", loader.getWidth(),loader.getHeight(), ImageUtil.getImageTypeName(image.getType()));
        
        ImageColorMap cm = new ImageColorMap(loader, sizeX, sizeY, sizeZ);
        cm.set("center",new Vector3d(sizeX/2,sizeY/2,0));
        cm.initialize();

        int nx = rasterWidth;
        int ny = (int)(sizeY/(sizeX/nx));
        BufferedImage outImage = makeSlice(cm, nx, ny, sizeX/nx, sizeY/ny);

        ImageIO.write(outImage, "png", new File(outPath));

    }

    BufferedImage makeSlice(DataSource source, int nx, int ny, double dx, double dy){

        Vec pnt = new Vec(3);
        Vec data = new Vec(4);

        BufferedImage image =  new BufferedImage(nx, ny, BufferedImage.TYPE_INT_ARGB);
        DataBufferInt db = (DataBufferInt)image.getRaster().getDataBuffer();
        int[] imageData = db.getData();
        double error[] = new double[4];

        for(int iy = 0; iy < ny; iy++){
            for(int ix = 0; ix < nx; ix++){
                pnt.set(dx*ix, dy*iy, 0.);
                source.getDataValue(pnt, data);
                //gammaCorrect(data.v);
                convertToWorkingSpace(data.v);
                
                data.addSet(error);

                int index = getClosestColor(data.v);
                data.clamp(-1,2);
                //data.clamp(-EPS,1+EPS);
                getError(data.v, colors[index], error);
                imageData[ix + nx*(ny-1-iy)] = colorsARGB[index];
            }
        }
        return image;
    }

    static final double GAMMA=2.2;
    // static final double GAMMA=1./2.2;
    static void gammaCorrect(double v[]){
        for(int i = 0; i < 3; i++){
            v[i] = Math.pow(v[i], GAMMA);
        }
    }

    static final double EPS = 1.e-3;
    void getError(double v[], double u[], double error[]){
        for(int i =0; i < v.length; i++){
            error[i] = 0.99*(v[i] - u[i]);
        }
    }

    // ideal rgb colors 
    static int icolors_rgb[][] = new int[][]{
        {0,    0,  0, 255},  // Black
        {255,255,255, 255},  // Wht     
        {255,  0,  0, 255},  // Red
        {  0,255,  0, 255},  // Green
        {  0,  0,255, 255},  // Blue
    };

    //
    // ideal subtractive colors used for output
    //
    static int icolors_cmyk[][] = new int[][]{ 
        {0,   255, 255, 255},// Cyan
        {255,   0, 255, 255}, //Mgnt 
        {255, 255,   0, 255}, //Yellow 
        {  0,   0,   0, 255}, //Black 
        {255, 255, 255, 255}, // White     
    };
   
    // colors of physical materials 
    static int icolors_phys[][] = new int[][]{
        //  {227, 233, 253,  50}, //VeroClear or VeroFlexCLR   
        {0,    90, 158, 255},// VeroCyan
        {166,  33,  98, 255}, //VeroMgnt or VeroFlexMGT 
        {200, 189,   3, 255}, //VeroYellow or VeroFlexYL   
        {26,  26,  29,  255},  //VeroBlack or VeroFlexBK  
        {240, 240, 240, 255},  // VeroPureWht     
    };
 
    static int icolors[][];   // rgba colors of materials 
    static int colorsARGB[];  // rgba colors of materials packed into ints
    static double colors[][]; // coordinates of normalized color components 

    static final double NORM = (1./255.);

    static {
        //icolors = icolors_cmyk;
        icolors = icolors_phys;
        //icolors = icolors_rgb;
        colors = new double[icolors.length][];
        colorsARGB = new int[icolors.length];
        for(int i = 0; i < colors.length; i++){
            int r = icolors[i][0];
            int g = icolors[i][1];
            int b = icolors[i][2];
            int a = icolors[i][3];            
            colorsARGB[i] = makeARGB(r,g,b,a);
            colors[i] = new double[]{r*NORM,g*NORM, b*NORM,a*NORM};
            convertToWorkingSpace(colors[i]);
        }
    }
    
    static void convertToWorkingSpace(double v[]){
        // subtractive colors
        //rgb2cmy(v);        
    }

    static void rgb2cmy(double v[]){
        v[0] = 1.-v[0];
        v[1] = 1.-v[1];
        v[2] = 1.-v[2];
    }

    int getClosestColor(double v[]){

        double minDist = 100;
        int minIndex = -1;
        for(int i = 0; i < colors.length; i++){
            double dist = colorDistance(v,colors[i]);
            if(dist <= minDist){
                minDist = dist;
                minIndex = i;
            }
        }
        return minIndex;
    }

    //static final double coeff[] = new double[]{1,1,1.5}; // c1
    //static final double coeff[] = new double[]{1,1,1.3};  // c2
    static final double coeff[] = new double[]{1,0.8,1.3};  // c3

    static double colorDistance(double v[],double u[]){
        double x = v[0] - u[0];
        double y = v[1] - u[1];
        double z = v[2] - u[2];
        return x*x*coeff[0] + y*y*coeff[1] + z*z*coeff[2];   
    }

    void devTestWriter(){

        double R = 7*MM;
        double r = 2.9*MM;
        double margin = 0.1*MM;

        double s = R+r+margin;
        double dz = r+margin;
        
        Torus torus = new Torus(R,r);
        DataSource model = torus;

        PolyJetWriter writer = new PolyJetWriter();
        
        writer.setBounds(new Bounds(-s,s,-s,s,-dz,dz));
        writer.set("model", model);

        writer.write();

    }

    public static void main(String[] args) throws Exception {

        //new TestPolyJetWriter().devTestSingleImage();
        new TestPolyJetWriter().devTestWriter();

    }
}