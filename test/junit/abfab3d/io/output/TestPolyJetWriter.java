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

import java.util.Vector;

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


import abfab3d.param.BaseParameterizable;

import abfab3d.grid.op.ImageLoader;
import abfab3d.datasources.ImageColorMap;
import abfab3d.datasources.Torus;
import abfab3d.datasources.Plane;
import abfab3d.datasources.DataSourceMixer;
import abfab3d.datasources.Mask;
import abfab3d.datasources.Abs;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Box;

import abfab3d.transforms.PeriodicWrap;
import abfab3d.transforms.Translation;
import abfab3d.transforms.Rotation;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;
import static abfab3d.core.Units.MM;
import static abfab3d.core.Units.CM;

import static abfab3d.util.ImageUtil.makeARGB;

import static java.lang.Math.*;


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

        double s = 10*MM;

        //DataSource model = makeTorus(20*MM);
        DataSource model = makeSphere(20*MM);

        Bounds bounds = model.getBounds();
        if(Bounds.isInfinite(bounds)) {
            bounds = new Bounds(-s,s,-s,s,-s,s);
        }

        PolyJetWriter writer = new PolyJetWriter();
        
        writer.setBounds(bounds);
        writer.set("model", model);        
        writer.set("ditheringType", 0);
        writer.set("firstSlice",bounds.getDepthVoxels(PolyJetWriter.SLICE_THICKNESS_HR)/2);
        writer.set("slicesCount", 6);
        writer.set("outFolder","/tmp/polyjet");
        //writer.set("mapping", "color_rgba");
        writer.set("mapping", "materials");

        writer.set("materials",new String[]{PolyJetWriter.S_CLEAR,
                                            PolyJetWriter.S_MAGENTA,
                                            PolyJetWriter.S_CYAN,                                              
                                            PolyJetWriter.S_YELLOW, 
                                            PolyJetWriter.S_BLACK,                                            
                                            PolyJetWriter.S_WHITE, 
                                            
            });
        writer.write();

    }

    //
    //  torus made with 6 materials
    //
    DataSource makeTorus(double size){

        double R = 0.4*size;
        double r = 0.5*size - R;
        double margin = 0.1*MM;
        double blur = 1*MM;
        double a = r;

        double s = R+r+margin;
        double dz = r+margin;
        
        Torus torus = new Torus(R,r);

        Mask mplane = new Mask(new Abs(new Plane(new Vector3d(1,0,0),0)), r/20, 0.1*MM);        
        mplane.addTransform(new Translation(new Vector3d(r/4,0,0)));
        mplane.addTransform(new PeriodicWrap(new Vector3d(r/2,0,0)));
        mplane.addTransform(new Rotation(0,2,1,Math.PI/6));

        Mask mplane1 = new Mask(new Abs(new Plane(new Vector3d(1,0,0),0)), r/20, 0.1*MM);        
        mplane1.addTransform(new Translation(new Vector3d(r/4,0,0)));
        mplane1.addTransform(new PeriodicWrap(new Vector3d(r/2,0,0)));
        mplane1.addTransform(new Rotation(0,1,1,2*Math.PI/3));

        Mask sphere = new Mask(new Sphere(a*0.45), 0, blur);        
        sphere.addTransform(new Translation(new Vector3d(a/2,a/2,a/2)));
        sphere.addTransform(new PeriodicWrap(new Vector3d(a,0,0),new Vector3d(0,a,0),new Vector3d(0,0,a)));
        sphere.addTransform(new Rotation(1,0,1,2*Math.PI/3));

        Mask sphere2 = new Mask(new Sphere(a*0.35), 0, blur/2);        
        sphere2.addTransform(new Translation(new Vector3d(a/2,a/2,a/2)));
        sphere2.addTransform(new PeriodicWrap(new Vector3d(a,0,0),new Vector3d(0,a,0),new Vector3d(0,0,a)));
        sphere2.addTransform(new Rotation(1,2,1,Math.PI/5));

        Mask sphere3 = new Mask(new Sphere(r/6), 0, 0.1*MM);        
        sphere3.addTransform(new Translation(new Vector3d(r/4,r/4,r/4)));
        sphere3.addTransform(new PeriodicWrap(new Vector3d(r/2,0,0),new Vector3d(0,r/2,0),new Vector3d(0,0,r/2)));
        sphere3.addTransform(new Rotation(1,0,1,Math.PI/5));
        
        DataSourceMixer model = new DataSourceMixer(torus, sphere, sphere2, sphere3, mplane, mplane1);

        model.setBounds(new Bounds(-s,s,-s,s,-dz,dz));

        return model;
    }


    DataSource makeSphere(double size){

        double R = 0.5*size;
        double r = R*0.7;
        double a = R*0.2;
        double b = R*0.2;
        double margin = 0.1*MM;
        double blur = 0.5*MM;
        double offset = 1*MM;

        double s = R+margin;
        
        Sphere sphere = new Sphere(R);

        Mask sphere1 = new Mask(new Abs(new Sphere(0,a,0,r)),offset,blur);
        Mask sphere2 = new Mask(new Abs(new Sphere(a/2,-a*sqrt(3)/2,0,r)), offset, blur);
        Mask sphere3 = new Mask(new Abs(new Sphere(-a/2,-a*sqrt(3)/2,0,r)),offset,blur);
        Mask box1 = new Mask(new Box(2*R, b, 2*R),0,blur);
        Mask box2 = new Mask(new Box(b, 2*R, 2*R),0,blur);
        
        DataSourceMixer model = new DataSourceMixer(sphere, sphere1,  sphere2, sphere3, box1, box2);
        
        model.setBounds(new Bounds(-s,s,-s,s,-s,s));

        return model;
    }

    /**
       enumerates all possible ordered combinations of 6 numbers from 0 to N giving sum N 
       it let create all possible combinatoions of PolyJet materials with (0,1/N,...N/N) volume fraction each 
     */
    static Vector<int[]> makeMaterialsCombinations(int N){
        
        //int count = 0;
        Vector<int[]> data = new Vector<int[]>();
        for(int i = 0; i <= N; i++ ){
            for(int j = i; j <= N; j++ ){
                for(int k = j; k <= N; k++ ){
                    for(int l = k; l <= N; l++ ){
                        for(int m = l; m <= N; m++ ){
                            int set[]  = new int[]{i,j-i,k-j,l-k,m-l,N-m};
                            printf("%d%d%d%d%d%d\n", set[0],set[1],set[2],set[3],set[4],set[5]);  
                            //count++;
                            data.add(set);
                        }
                    }
                }
            }            
        }
        //        printf("N:%d, count:%d\n", N, data.size());
        return data;
    }


    void makeMaterialSamplesTest(){

        double sampleSize = 10*MM;
        int quantization = 6;

        DataSource model = makeMaterialsSample(sampleSize, quantization);

        Bounds bounds = model.getBounds();

        PolyJetWriter writer = new PolyJetWriter();
        
        writer.setBounds(bounds);
        writer.set("model", model);        
        writer.set("ditheringType", 0);
        writer.set("firstSlice",bounds.getDepthVoxels(PolyJetWriter.SLICE_THICKNESS_HR)/2);
        writer.set("slicesCount", 1);
        writer.set("outFolder","/tmp/polyjet");
        //writer.set("mapping", "color_rgba");
        writer.set("mapping", "materials");

        writer.set("materials",new String[]{PolyJetWriter.S_WHITE, 
                                            PolyJetWriter.S_BLACK,
                                            PolyJetWriter.S_CLEAR,
                                            PolyJetWriter.S_YELLOW, 
                                            PolyJetWriter.S_MAGENTA,
                                            PolyJetWriter.S_CYAN,                                              
                                            
                                            
                                            
            });
        writer.write();

    }

    static DataSource makeMaterialsSample(double sampleSize, int quatization){

        Vector<int[]> data = makeMaterialsCombinations(quatization);
        int width = (int)ceil(sqrt(data.size()));
        double boxSize = sampleSize*width;
        Box box = new Box(boxSize,boxSize,1*MM);
        DataSource colorizer = new MaterialSampler(data, 1./quatization, width, boxSize);
        DataSourceMixer mixer = new DataSourceMixer(box, colorizer);
        double s = boxSize/2;
        Bounds bounds = new Bounds(-s,s,-s,s,-0.5*MM,0.5*MM);
        bounds.expand(0.2*MM);
        mixer.setBounds(bounds);
        return mixer;
        
    }


    static class MaterialSampler extends BaseParameterizable implements DataSource {
        
        Vector<int[]> data;
        int width;
        double boxSize;
        int quantization;
        double norm;
        double cellSize;

        MaterialSampler(Vector<int[]> data, double norm, int width, double boxSize){

            this.quantization = quantization;
            this.width = width;
            this.boxSize = boxSize;
            this.data = data;
            this.cellSize = boxSize/width;
            this.norm = norm;
        }

        public int getDataValue(Vec pnt, Vec dataValue){

            int x = (int)(((pnt.v[0] + boxSize/2))/cellSize);
            int y = (int)(((pnt.v[1] + boxSize/2))/cellSize);
            
            int index = (x + y*width);
            if(index >= data.size() || index < 0) 
                index = 0;
            int[] set = data.get(index);
            dataValue.v[0] = set[1]*norm;  // first material is background 
            dataValue.v[1] = set[2]*norm;
            dataValue.v[2] = set[3]*norm;
            dataValue.v[3] = set[4]*norm;
            dataValue.v[4] = set[5]*norm; // only store 5 materials. the first one is background
            return 1;
        }
        
        public int getChannelsCount(){
            return 5;
        }
    
        public Bounds getBounds(){
            return null;
        }
        
    }

    public static void main(String[] args) throws Exception {

        //new TestPolyJetWriter().devTestSingleImage();
        //new TestPolyJetWriter().devTestWriter();
        new TestPolyJetWriter().makeMaterialSamplesTest();
        //makeMaterialsCombinations(1);

    }
}
