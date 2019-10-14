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
import abfab3d.core.Initializable;

import abfab3d.util.ImageUtil;
import abfab3d.util.SliceCalculator;
import abfab3d.util.AbFab3DGlobals;


import abfab3d.param.BaseParameterizable;

import abfab3d.grid.op.ImageLoader;

import abfab3d.datasources.Text2D;
import abfab3d.datasources.Image3D;
import abfab3d.datasources.Composition;
import abfab3d.datasources.ImageColorMap;
import abfab3d.datasources.ColorGrid;
import abfab3d.datasources.Torus;
import abfab3d.datasources.Plane;
import abfab3d.datasources.DataSourceMixer;
import abfab3d.datasources.Mask;
import abfab3d.datasources.Abs;
import abfab3d.datasources.Mul;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Constant;
import abfab3d.datasources.Box;
import abfab3d.datasources.ImageColorMap3D;
import abfab3d.datasources.Union;

import abfab3d.transforms.PeriodicWrap;
import abfab3d.transforms.Translation;
import abfab3d.transforms.Rotation;


import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
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

    /**
       write simple file 
     */
    void devTestWriter(){

        double s = 10*MM;

        DataSource model = makeTorus(20*MM);
        //DataSource model = makeSphere(20*MM);

        Bounds bounds = model.getBounds();
        if(Bounds.isInfinite(bounds)) {
            bounds = new Bounds(-s,s,-s,s,-s,s);
        }

        AbFab3DGlobals.put(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY, 16);

        PolyJetWriter writer = new PolyJetWriter();
        writer.set("threadCount", 0);
        writer.setBounds(bounds);
        writer.set("model", model);        
        writer.set("sliceThickness", 1*MM);
        writer.set("ditheringType", 0);
        //writer.set("firstSlice",bounds.getDepthVoxels(PolyJetWriter.SLICE_THICKNESS_HR)/2);
        //writer.set("slicesCount", 150);
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

    /**
       
       
     */
    void devTestImageColorMap3D(){

        double s = 10*MM;

        int count = 275;
        //int count = 10;  // read only 10 layers 
        double vs = 0.09*MM;
        double width = 1250;
        double height = 500;
        double depth = count;
        double margin = 0.1*MM;
        double colorsMixRatio = 1.;
        double textWidth=50*MM, textHeight=5*MM, textDepth=1*MM;
        String label = fmt("19.09.25.RGBA",colorsMixRatio);
        //String pathTemplate = "/tmp/slices+alpha/slice%04d.png";
        String pathTemplate = "/tmp/squid_rgba/squid - bright normed 8 bit%04d.png";
        
        //String pathTemplate = "/tmp/slices+white/slice%04d.png";

        double sizeX = width*vs;
        double sizeY = height*vs;
        double sizeZ = count*vs;
        
        String outFolder = "/tmp/polyjet";

        printf("model size: [%7.2f x %7.2f x %7.2f]mm\n", sizeX/MM, sizeY/MM,sizeZ/MM);

        

        long t0  = time();
        
        ImageColorMap3D cm = new ImageColorMap3D(pathTemplate, 0, count, sizeX, sizeY, sizeZ);
        
        DataSource cm1 = new Mul(cm, new Constant(1,1,1,colorsMixRatio));

        Box box = new Box(sizeX, sizeY, sizeZ);
                
        DataSourceMixer cbox = new DataSourceMixer(box, cm1);

        Image3D text3d = makeText(label, textWidth, textHeight, textDepth);
        text3d.setTransform(new Translation(-sizeX/2+textWidth/2,sizeY/2-textHeight/2, sizeZ/2-textDepth/2));
        DataSource ctext = new DataSourceMixer(text3d, new Constant(0,0.5,0.5,1));

        Composition labeledBox = new Composition(Composition.BoverA, cbox, ctext);

        //DataSource model = makeSphere(20*MM);

        DataSource model = labeledBox;

        ((Initializable)model).initialize();

        printf("reading data done: %d ms\n", (time() - t0));

        Bounds bounds = cbox.getBounds();
        
        bounds.expand(margin);

        AbFab3DGlobals.put(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY, 8);

        PolyJetWriter writer = new PolyJetWriter();
        writer.set("threadCount", 0);
        writer.setBounds(bounds);
        writer.set("materialsRatio", 1);
        writer.set("model", model);        
        //writer.set("sliceThickness", PolyJetWriter.DEFAULT_SLICE_THICKNESS);
        //writer.set("ditheringType", 0);
        //writer.set("firstSlice",bounds.getDepthVoxels(PolyJetWriter.SLICE_THICKNESS_HR)/2);
        //writer.set("slicesCount", 150);
        writer.set("outFolder",outFolder);
        //writer.set("mapping", "materials");
        writer.set("mapping", "color_rgba");
        writer.set("materials",PolyJetWriter.DEFAULT_MATERIALS);

        t0  = time();
        printf("writing slices to %s\n", outFolder);
        writer.write();
        printf("writing slices done %d ms\n", (time() - t0));


    }

    void devTestColorConverter(){

        printf("devTestColorConverter()\n");
        Vec pnt = new Vec(1, 1,0,0,1);
        Vec data = new Vec(7);

        for(int i = 0; i <= 10; i++){
            double ratio = i*0.1;
            PolyJetWriter.DRGBA2Materials(pnt.v, data.v, ratio);
            printf("%s | %5.2f -> %s\n", pnt.toString("%5.2f"),ratio, data.toString("%5.2f"));
        }
        
    }


    /**
       
     */
    void devTestColorGrid(){

        printf("devTestColorGrid()\n");

        double margin = 1*MM;
        double csize = 20.*MM;
        double gap = 1*MM;
        int dim = 6;
        double inc = 1./(dim-1);

        Vector3d dimension = new Vector3d(dim,dim,dim);
        //Vector3d dimension = new Vector3d(5,5,1);
        Vector3d cellSize = new Vector3d(csize, csize, csize);        
        
        String outFolder = "/tmp/polyjet";

        ColorGrid grid = new ColorGrid();

        grid.set("dimension", dimension);
        grid.set("cellSize",cellSize);

        grid.set("c0", new Color(0,0,0,1));
        grid.set("cu", new Color(0, inc, 0, 0));
        grid.set("cv", new Color(inc, 0, 0, 0));
        grid.set("cw", new Color(0, 0, inc, 0));

        grid.set("gap", new Vector3d(gap, gap, gap));
        grid.set("gapColor", new Color(0.,0.,0.,0));
        
        Box box = new Box(cellSize.x*dimension.x, cellSize.y*dimension.y,cellSize.z*dimension.z);
        
        DataSourceMixer mix = new DataSourceMixer(box, grid);

        DataSource model = mix;

        mix.initialize();

        if(false){
            double x0 = csize*(-dimension.x+1)/2;
            double y0 = csize*(-dimension.y+1)/2;
            double z0 = csize*(-dimension.z+1)/2;
            
            for(int z = 0; z < dimension.z; z++){
                for(int y = 0; y < dimension.y; y++){
                    for(int x = 0; x < dimension.x; x++){                    
                        Vec pnt = new Vec(x*csize+x0, y*csize+y0, z*csize+z0);
                        Vec data = new Vec(5);
                        mix.getDataValue(pnt, data);
                        printf("%s",data.toString("%5.2f"));
                        //printf("%s",pnt.toString("%6.3f"));
                    }
                    printf("\n");
                }            
                printf("---\n");
            }
        }

        //if(true) return;
        Bounds bounds = model.getBounds();        
        bounds.expand(margin);

        AbFab3DGlobals.put(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY, 8);

        PolyJetWriter writer = new PolyJetWriter();
        writer.set("threadCount", 0);
        writer.setBounds(bounds);
        writer.set("model", model);        
        writer.set("sliceThickness",csize);        
        writer.set("outFolder",outFolder);
        writer.set("mapping", "color_rgba");
        writer.set("materials",PolyJetWriter.DEFAULT_MATERIALS);

        printf("writing slices to %s\n", outFolder);
        writer.write();

    }

    void devTestColorSwatch(){

        printf("devTestColorGrid()\n");

        double margin = 0.2*MM;
        double boxMargins = 5*MM;
        double csize = 15.*MM;
        double gap = 1*MM;
        int dim = 6;
        double inc = 1./(dim-1);
        Color textColor = new Color(0,0.0,0.0,1);

        Vector3d dimension = new Vector3d(dim,dim,1);
        Vector3d cellSize = new Vector3d(csize, csize, csize); 
        
        String outFolder = "/tmp/polyjet";


        double boxWidth = cellSize.x*dimension.x + 2*boxMargins;
        double boxHeight = cellSize.y*dimension.y + 2*boxMargins;
        double boxThickness = 3*MM;

        double textWidth = 20*MM, textHeight = 5*MM, textDepth=1*MM;

        ColorGrid grid = new ColorGrid();

        grid.set("dimension", dimension);
        grid.set("cellSize",cellSize);

        Color c0 = new Color(0,0,0,1);
        Color cu = new Color(0, inc, 0, 0);
        Color cv = new Color(inc, 0, 0, 0);
        Color cw = new Color(0, 0, inc, 0);

        grid.set("c0", c0);
        grid.set("cu", cu);
        grid.set("cv", cv);
        grid.set("cw", cw);

        grid.set("gap", new Vector3d(gap, gap, gap));
        grid.set("gapColor", new Color(0.,0.,0.,0));
        
        Box box = new Box(boxWidth, boxHeight,boxThickness);
        
        DataSourceMixer mix = new DataSourceMixer(box, grid);


        String f = "%3.1f  %3.1f  %3.1f  %3.1f";
        double dd = dim-1;
        String lab_00 = fmt(f, c0.r,                  c0.g,                  c0.b,                  c0.a);
        String lab_10 = fmt(f, c0.r + dd*cu.r,        c0.g + dd*cu.g,        c0.b + dd*cu.b,        c0.a + dd*cu.a);
        String lab_01 = fmt(f, c0.r + dd*cv.r,        c0.g + dd*cv.g,        c0.b + dd*cv.b,        c0.a + dd*cv.a);
        String lab_11 = fmt(f, c0.r + dd*(cu.r+cv.r), c0.g + dd*(cu.g+cv.g), c0.b + dd*(cu.b+cv.b), c0.a + dd*(cu.a+cv.a));

        Image3D label_top = makeText("r g b a", textWidth, textHeight, textDepth);
        Image3D label_00 = makeText(lab_00, textWidth, textHeight, textDepth);
        Image3D label_10 = makeText(lab_10, textWidth, textHeight, textDepth);
        Image3D label_01 = makeText(lab_01, textWidth, textHeight, textDepth);
        Image3D label_11 = makeText(lab_11, textWidth, textHeight, textDepth);

        double tx = (boxWidth-textWidth)/2;
        double ty = (boxHeight-textHeight)/2;
        double tz = (boxThickness-textDepth)/2;

        label_00.setTransform(new Translation(-tx, -ty, tz));
        label_10.setTransform(new Translation(tx, -ty, tz));
        label_01.setTransform(new Translation(-tx, ty, tz));
        label_11.setTransform(new Translation(tx, ty, tz));
        label_top.setTransform(new Translation(0, ty, tz));
        DataSource allText = new Union(label_00,label_10,label_01,label_11,label_top);
        DataSource ctext = new DataSourceMixer(allText, new Constant(textColor));
        Composition labeledBox = new Composition(Composition.BoverA, mix, ctext);

        DataSource model = labeledBox;

        ((Initializable)model).initialize();

        Bounds bounds = new Bounds(-boxWidth/2,boxWidth/2,-boxHeight/2,boxHeight/2,-boxThickness/2, boxThickness/2);//model.getBounds();        
        bounds.expand(margin);

        AbFab3DGlobals.put(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY, 8);

        PolyJetWriter writer = new PolyJetWriter();
        writer.set("threadCount", 0);
        writer.setBounds(bounds);
        writer.set("model", model);        
        // writer.set("sliceThickness",0.5*MM); 
        writer.set("outFolder",outFolder);
        writer.set("mapping", "color_rgba");
        writer.set("materials",PolyJetWriter.DEFAULT_MATERIALS);

        printf("writing slices to %s\n", outFolder);
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
        int quantization = 2;

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

    void devTestSliceCalculator(){

        double sampleSize = 10*MM;
        int quantization = 2;

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
        //writer.set("sliceCalculator", new DumbSliceCalculator());

        writer.set("materials",new String[]{PolyJetWriter.S_WHITE, 
                                            PolyJetWriter.S_BLACK,
                                            PolyJetWriter.S_CLEAR,
                                            PolyJetWriter.S_YELLOW, 
                                            PolyJetWriter.S_MAGENTA,
                                            PolyJetWriter.S_CYAN,                                              
                                            
                                            
                                            
            });
        writer.write();

    }


    /**
       calculates nothing for test 
     */
    static class DumbSliceCalculator implements SliceCalculator {
        
        public void getSliceData(DataSource source, Vector3d origin, Vector3d u, Vector3d v, int nu, int nv, int dataDimension, double sliceData[]){
            printf("getSliceData([%7.5f,%7.5f,%7.5f])\n", origin.x/MM, origin.y/MM, origin.z/MM);
        }
        
    }

    Image3D makeText(String txt, double width, double height, double depth){
  
        Text2D text2d = new Text2D(txt);
        
        text2d.setFontStyle(Text2D.BOLD);
        //text2d.set("spacing", args.textSpacing);
        //text2d.setInset(textInsets);
        text2d.setVoxelSize(0.01*MM);
        text2d.setFit("both"); // vertical, horizontal, both
        //text2d.setHorizAlign("CENTER"); // left, right, center
        text2d.setHorizAlign("LEFT"); // left, right, center
        text2d.setHeight(height);
        text2d.setWidth(width);
        
        Image3D textBox = new Image3D(text2d, width, height, depth,0.1*MM);
        textBox.setUseGrayscale(false);
        return textBox;
    }

    void devTestCopySlices() throws Exception {

        String inTemplate = "/tmp/squid_matlab/Out_%04d.png";
        String outTemplate = "/tmp/polyjet/slice_%d.png";
        for(int i = 0; true; i++){
            String inPath = fmt(inTemplate, i*2+1);
            printf("%s\n",inPath);
            File inFile = new File(inPath);
            if(!inFile.exists()){
                break;
            }
            String outPath = fmt(outTemplate, i);
            java.nio.file.Files.copy( 
                                     new java.io.File(inPath).toPath(), 
                                     new java.io.File(outPath).toPath(),
                                     java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                                     java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
                                     java.nio.file.LinkOption.NOFOLLOW_LINKS); 
        }
    }

    public static void main(String[] args) throws Exception {

        //new TestPolyJetWriter().devTestSingleImage();
        //new TestPolyJetWriter().devTestWriter();
        //new TestPolyJetWriter().makeMaterialSamplesTest();
        //makeMaterialsCombinations(1);
        //new TestPolyJetWriter().devTestSliceCalculator();
        new TestPolyJetWriter().devTestImageColorMap3D();
        //new TestPolyJetWriter().devTestColorGrid();
        //new TestPolyJetWriter().devTestColorSwatch();
        //new TestPolyJetWriter().devTestColorConverter();
        //new TestPolyJetWriter().devTestCopySlices();
    }
}
