/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2016
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/

package abfab3d.shapejs;

// External Imports


import abfab3d.util.AbFab3DGlobals;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.imageio.ImageIO;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.UUID;

import abfab3d.core.Material;
import abfab3d.core.Color;
import abfab3d.core.Vec;
import abfab3d.core.DataSource;
import abfab3d.core.Bounds;

import abfab3d.param.Shape;

import abfab3d.grid.op.ImageMaker;

import abfab3d.transforms.Translation;
import abfab3d.transforms.PeriodicWrap;
import abfab3d.transforms.Rotation;

import abfab3d.datasources.Abs;
import abfab3d.datasources.Sub;
import abfab3d.datasources.Intersection;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Box;
import abfab3d.datasources.VolumePatterns;
import abfab3d.datasources.Intersection;
import abfab3d.datasources.Union;
import org.apache.commons.io.IOUtils;


import static abfab3d.core.Units.MM;
import static abfab3d.core.Units.TORADIANS;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Output.time;


/**
 * Tests the functionality of the SceneImageDataSource
 */
public class TestSceneImageDataSource extends TestCase {
    public static final float[] backgroundColor = new float[]{1, 1, 1};

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestSceneImageDataSource.class);
    }
    
    public void testNothing() {
        
    }


    static int sm_raytracingDepth  = 4;
    static int sm_maxIntersections  = 10;
    static double sm_surfaceJump  = 0.001;
    

    public void devTestScene() throws IOException {

        //int imageWidth = 512, imageHeight = 512;
        String outPath = "/tmp/renderedScene.png";
        //int imageWidth = 4000, imageHeight = 4000;
        //int imageWidth = 2000, imageHeight = 2000;
        int imageWidth = 960, imageHeight = 540;

        //SceneImageDataSource sids = makeSceneImageDataSource(makeSceneBallsLatticeMM(5*MM));
        //SceneImageDataSource sids = makeSceneImageDataSource(makeSceneBallsMM(5*MM));
        //SceneImageDataSource sids = makeSceneImageDataSource(makeSceneBallsMM2(5*MM));
        //SceneImageDataSource sids = makeSceneImageDataSource(makeScene3Balls(5*MM));
        //SceneImageDataSource sids = makeSceneImageDataSource(makeSceneBall(5*MM));
        //SceneImageDataSource sids = makeSceneImageDataSource(makeSceneGyroid());
        //SceneImageDataSource sids = makeSceneImageDataSource(makeSceneGyroidRotated(new Rotation(0,1,0,20*TORADIANS)));
        SceneImageDataSource sids = makeSceneImageDataSource(makeSceneGyroidRotated2(new Rotation(0,1,0,20*TORADIANS)));

        //sids.set("shadowsQuality",5);
        sids.set("shadowsQuality",0);
        sids.set("volumeRendererLayerThickness",0.1*MM);
        

        ImageMaker im = new ImageMaker();

        im.set("imgRenderer",sids);
        im.set("threadCount",8);
        im.set("width", imageWidth);
        im.set("height", imageHeight);
        double s = (double)imageWidth/imageHeight;
        im.setBounds(new Bounds(-s,s,-1,1,-1,1));
        long t0 = time();
        printf("image[%d x %d]\n", imageWidth, imageHeight);
        BufferedImage image = im.getImage();
        printf("render time: %dms\n", (time()-t0));
        printf("writng image: %s\n", outPath); 
        ImageIO.write(image, "png", new File(outPath));
        

    }

    public void devTestRotation() throws IOException {

        int imageWidth = 960, imageHeight = 540;
        //int imageWidth = 1920, imageHeight = 1080;
        //int imageWidth = 4000, imageHeight = 4000;
        //int imageWidth = 2000, imageHeight = 2000;

        //SceneImageDataSource sids = makeSceneImageDataSource(makeSceneBallsLatticeMM(5*MM));
        //SceneImageDataSource sids = makeSceneImageDataSource(makeSceneBallsMM(5*MM));
        //SceneImageDataSource sids = makeSceneImageDataSource(makeScene3Balls(5*MM));
        //SceneImageDataSource sids = makeSceneImageDataSource(makeSceneBall(5*MM));
        //SceneImageDataSource sids = makeSceneImageDataSource(makeSceneGyroid());

        int frames = 360;
        for(int i = 0; i < frames; i+=1){

            double alpha = (i)*TORADIANS;

            SceneImageDataSource sids = makeSceneImageDataSource(makeSceneGyroidRotated2(new Rotation(1,1,1,alpha)));
            
            sids.set("shadowsQuality",10);
            ImageMaker im = new ImageMaker();
            im.set("imgRenderer",sids);
            im.set("threadCount",8);
            im.set("width", imageWidth);
            im.set("height", imageHeight);
            double s = (double)imageWidth/imageHeight;
            im.setBounds(new Bounds(-s,s,-1,1,-1,1));
            long t0 = time();
            printf("image[%d x %d]\n", imageWidth, imageHeight);
            BufferedImage image = im.getImage();
            printf("render time: %dms\n", (time()-t0));
            String outPath = fmt("/tmp/rotation/f%03d.png", i);
            printf("writing image: %s\n", outPath); 
            ImageIO.write(image, "png", new File(outPath));
        }
    }

    public void devTestPerformance() throws IOException {

        printf("devTestPerformance()\n");
        int count = 4;
        AbFab3DGlobals.put(AbFab3DGlobals.MAX_PROCESSOR_COUNT_KEY, Runtime.getRuntime().availableProcessors());
        for(int i=0; i < count; i++) {
            int imageWidth = 512, imageHeight = 512;
            //int imageWidth = 4000, imageHeight = 4000;
            //int imageWidth = 2000, imageHeight = 2000;

            //SceneImageDataSource sids = makeSceneImageDataSource(makeSceneBallsMM(5*MM));
            //SceneImageDataSource sids = makeSceneImageDataSource(makeScene3Balls(5*MM));
            //SceneImageDataSource sids = makeSceneImageDataSource(makeSceneBall(5*MM));
            SceneImageDataSource sids = makeSceneImageDataSource(makeSceneGyroid());

            //sids.set("shadowsQuality",5);
            sids.set("shadowsQuality", 10);

            ImageMaker im = new ImageMaker();

            im.set("imgRenderer", sids);
            im.set("threadCount", 0);
            im.set("width", imageWidth);
            im.set("height", imageWidth);
            im.setBounds(new Bounds(-1, 1, -1, 1, -1, 1));
            long t0 = time();
            printf("image[%d x %d]\n", imageWidth, imageHeight);
            BufferedImage image = im.getImage();
            printf("render time: %dms\n", (time() - t0));

            // ImageIO.write(image, "png", new File("/tmp/renderedScene.png"));
        }
    }

    /**
       test specific pixels 
     */
    public void devTestDataSource() throws IOException {
        
        printf("devTestDataSource()\n");
        int N = 1;
        SceneImageDataSource sids = makeSceneImageDataSource(makeSceneBallsMM(5*MM));
        //SceneImageDataSource sids = makeSceneImageDataSource(makeSceneBall(7*MM));
        sids.set("shadowsQuality",10);
        //sids.initialize();
        sids.setDebug(true);
        double vs = 2./N;
        Bounds bounds = new Bounds(-1,1,-1,1,-1,1,vs);
        Vec img = new Vec(4);
        
        for(int i = 0; i < N; i++){
            double x = 0.02 + i*0.01,y = 0.0;
            Vec pp = new Vec(x,y);
        //printf("pnt: [%6.2f,%6.2f]\n", x, y);
            sids.getDataValue(pp,img);
            printf("(%7.3f,%7.3f) -> [%5.2f,%5.2f,%5.2f,%5.2f]\n",pp.v[0],pp.v[1], img.v[0],img.v[1],img.v[2],img.v[3]);
        }
        /*
        for(int j = 0; j < N; j++){
            //for(int j = 33; j < 34; j++){
            printf("%3d:",j);
            //for(int i = 30; i < 32; i++){
                for(int i = 0; i < N; i++){
                double x = -1 + 2.*i/N, 
                    y =  1 - 2.*j/N;
                Vec pp = new Vec(x, y);
                //printf("pnt: [%6.2f,%6.2f]\n", x, y);
                sids.getDataValue(pp,img);
                printf("%3d", (int)(99*img.v[0]));
            }        
            printf("\n");
        }
        */
    }
    
    static Scene makeSceneGyroid(){
    
        double radius = 25*MM;
        double s = 25.5*MM;
        double period = 18*MM;
        double thickness = 1*MM;
        DataSource sphere = new Sphere(radius);
        DataSource gyroid = new VolumePatterns.Gyroid(period, thickness);
        //gyroid.set("period", 10*MM);
        Intersection intersect = new Intersection();
        intersect.setBlend(thickness);
        intersect.add(sphere);
        intersect.add(gyroid);
        
        //Shape shape = new Shape(intersect, new SingleColorMaterial(0.5,0.5,0.5));
        //Scene scene = new Scene(shape,new Bounds(-s,s,-s,s,-s,s));

        SingleColorMaterial blue = new SingleColorMaterial(new Color(0.5,0.5,1));
        blue.setShaderParam("transmittanceCoeff", new Vector3d(0.01,0,0));
        blue.setShaderParam("shininess", 0.5);

        Scene scene = new Scene(new Shape(intersect, blue),new Bounds(-s,s,-s,s,-s,s));
        return scene;
    }

    static Scene makeSceneGyroidRotated(Rotation rotation){
    
        double radius = 25*MM;
        double s = 25.5*MM;
        double period = 18*MM;
        double thickness = 1*MM;
        DataSource sphere = new Sphere(radius);
        DataSource gyroid = new VolumePatterns.Gyroid(period, thickness);
        //gyroid.set("period", 10*MM);
        Intersection intersect = new Intersection();
        intersect.setBlend(thickness);
        intersect.add(sphere);
        intersect.add(gyroid);
        intersect.setTransform(rotation);
        //Shape shape = new Shape(intersect, new SingleColorMaterial(0.5,0.5,0.5));
        //Scene scene = new Scene(shape,new Bounds(-s,s,-s,s,-s,s));

        SingleColorMaterial blue = new SingleColorMaterial(new Color(0.5,0.5,1));
        blue.setShaderParam("transmittanceCoeff", new Vector3d(0.01,0,0));
        blue.setShaderParam("shininess", 0.5);

        Scene scene = new Scene(new Shape(intersect, blue),new Bounds(-s,s,-s,s,-s,s));
        return scene;
    }

    static Scene makeSceneGyroidRotated2(Rotation rotation){
    
        double radius = 25*MM;
        double s = 25.5*MM;
        double period = 18*MM;
        double thickness = 0.4*MM;
        DataSource sphere = new Sphere(radius);
        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(period, thickness);
        VolumePatterns.Gyroid gyroid2 = new VolumePatterns.Gyroid(period, thickness);
        gyroid.set("period", 25*MM);
        gyroid.set("level", 1.);
        gyroid2.set("period", 25*MM);
        gyroid2.set("level", -1.);

        Intersection intersect = new Intersection(sphere, gyroid);
        intersect.setBlend(thickness);
        intersect.setTransform(rotation);

        Intersection intersect2 = new Intersection(sphere, gyroid2);
        intersect2.setBlend(thickness);
        intersect2.setTransform(rotation);

        SingleColorMaterial blue = new SingleColorMaterial(new Color(0.2,0.2,0.9));
        // blue.setShaderParam("transmittanceCoeff", new Vector3d(0.001,0.001,0.001));
        //blue.setShaderParam("transmittanceCoeff", new Vector3d(0.001,0.001,0.1));
        //blue.setShaderParam("transmittanceCoeff", new Vector3d(0.001,0.001,0.1));
        blue.setShaderParam("shininess", 0.5);
        blue.setShaderParam("surfaceAlpha", 0.3);

        SingleColorMaterial red = new SingleColorMaterial(new Color(0.9,0.2,0.2));
        //red.setShaderParam("transmittanceCoeff", new Vector3d(0.1,0.001,0.001));
        red.setShaderParam("shininess", 0.5);
        red.setShaderParam("surfaceAlpha", 0.3);

        Scene scene = new Scene(new Bounds(-s,s,-s,s,-s,s));
        scene.addShape(new Shape(intersect, blue));
        scene.addShape(new Shape(intersect2, red));
        
        return scene;
    }

    static Scene makeSceneBall(double radius){
    
        //double radius = 5*MM;
        double s = 25.5*MM;
        DataSource sphere = new Sphere(radius);
        Union union = new Union();

        union.add(sphere);
        Box box = new Box(2*s, 2*s, 2*MM);
        box.addTransform(new Translation(new Vector3d(0,0,-s*0.75)));
        union.add(box);
        
        Scene scene = new Scene(union,new Bounds(-s,s,-s,s,-s,s));
        return scene;
    }

    static Scene makeScene3Balls(double radius){
    
        //double radius = 5*MM;
        double s = 25.5*MM;
        DataSource sphere = new Sphere(radius);
        DataSource sphere1 = new Sphere(new Vector3d(2*radius, 0,0), radius);
        DataSource sphere2 = new Sphere(new Vector3d(-2*radius, 0,0), radius);

        Union union = new Union();

        union.add(sphere);
        union.add(sphere1);
        union.add(sphere2);
        
        double period = 2*MM;
        double boxSize  = 0.8*period;
        PeriodicWrap wrap = new PeriodicWrap(new Vector3d(period, 0,0),new Vector3d(0,period, 0));
        wrap.setOrigin(new Vector3d(-period/2, -period/2,0));
        Box box = new Box(boxSize, boxSize, boxSize);
        
        box.addTransform(wrap);
        box.addTransform(new Translation(new Vector3d(0,0,-s*0.75)));
        union.add(box);
        
        Scene scene = new Scene(union,new Bounds(-s,s,-s,s,-s,s));
        return scene;
    }

    static Scene makeScene3Balls2(double radius){
    
        //double radius = 5*MM;
        double s = 25.5*MM;
        DataSource sphere = new Sphere(radius);
        DataSource sphere1 = new Sphere(new Vector3d(2*radius, 0,0), radius);
        DataSource sphere2 = new Sphere(new Vector3d(-2*radius, 0,0), radius);

        Union union = new Union();

        union.add(sphere);
        union.add(sphere1);
        union.add(sphere2);

        Box box = new Box(2*s, 2*s, 2*MM);
        box.addTransform(new Translation(new Vector3d(0,0,-s*0.75)));
        union.add(box);
        
        Scene scene = new Scene(union,new Bounds(-s,s,-s,s,-s,s));
        return scene;
    }

    /**
       Spheres made of different materials 
     */
    static Scene makeSceneBallsMM(double radius){
    
        //double radius = 5*MM;
        double s = 25.5*MM;
        double period = 2*s/11;
        double boxSize  = 0.7*period;
        double boxDepth = 0.1*boxSize;

        //DataSource sphere1 = new Sphere(radius);
        DataSource sphere1 = new Sub(new Abs(new Sphere(new Vector3d(0,0,0), radius)),0.05*MM);
        //DataSource sphere2 = new Sphere(new Vector3d(2*radius+3*period, 2*radius,0), radius);
        DataSource sphere2 = new Sub(new Abs(new Sphere(new Vector3d(2*radius,0,0), radius)),0.1*MM);
        //DataSource sphere2 = new Sphere(new Vector3d(2*radius, 0,0), radius);

        DataSource sphere3 = new Sphere(new Vector3d(-2*radius, 0,0), radius);
        DataSource sphere4 = new Sphere(new Vector3d(0,2*radius,0), radius);
        //DataSource sphere5 = new Sphere(new Vector3d(radius,0,2*radius), radius);
        DataSource sphere5 = new Sub(new Abs(new Sphere(new Vector3d(radius,0,2*radius), radius)),0.05*MM);

        PeriodicWrap wrap = new PeriodicWrap(new Vector3d(period, 0,0),new Vector3d(0,period, 0));
        wrap.setOrigin(new Vector3d(-period/2, -period/2,0));
        Box box = new Box(boxSize, boxSize, boxDepth);
        box.set("rounding", 0.5*MM);
        box.addTransform(wrap);
        box.addTransform(new Translation(new Vector3d(0,0,-radius-boxDepth)));

        Scene scene = new Scene(new Bounds(-s,s,-s,s,-s,s));


        SingleColorMaterial green = new SingleColorMaterial(0.,0.9,0.);

        //green.setShaderParam("transmittanceCoeff", new Vector3d(0.03,0.03,0.03));
        green.setShaderParam("transmittanceCoeff", new Vector3d(0.001,0.01,0.001));
        green.setShaderParam("shininess", 0.5);

        SingleColorMaterial pink = new SingleColorMaterial(1,0.,0.3);
        pink.setShaderParam("transmittanceCoeff", new Vector3d(0.01,0.001,0.01));
        pink.setShaderParam("shininess", 0.5);
        
        scene.addShape(new Shape(sphere1, pink));
        scene.addShape(new Shape(sphere2, green));
        
        // semi transparent sphere
        SingleColorMaterial blue = new SingleColorMaterial(new Color(0.0,0.,1));
        //blue.setShaderParam("albedo", new Color(0.01, 0.01, 0.01));
        blue.setShaderParam("transmittanceCoeff", new Vector3d(0.001,0.001,0.01));
        blue.setShaderParam("shininess", 0.5);

        scene.addShape(new Shape(sphere3, blue));
        scene.addShape(new Shape(sphere4, blue));
        scene.addShape(new Shape(sphere5, blue));

        scene.addShape(new Shape(box, new SingleColorMaterial(0.7,0.7,0.7)));

        return scene;
    }


    static Scene makeSceneBallsMM2(double radius){
    
        //double radius = 5*MM;
        double s = 25.5*MM;
        double period = 2*s/11;
        double boxSize  = 0.7*period;
        double boxDepth = 0.1*boxSize;

        DataSource sphere0 = new Sphere(new Vector3d(-4*radius,radius,0), radius);
        DataSource sphere0a = new Sub(new Abs(new Sphere(new Vector3d(-4*radius,-radius,0), radius)),0.05*MM);
        DataSource sphere1 = new Sphere(new Vector3d(-2*radius,radius,0), radius);
        DataSource sphere1a = new Sub(new Abs(new Sphere(new Vector3d(-2*radius,-radius,0), radius)),0.05*MM);
        DataSource sphere2 = new Sphere(new Vector3d( 0*radius,radius,0), radius);
        DataSource sphere2a = new Sub(new Abs(new Sphere(new Vector3d(0*radius,-radius,0), radius)),0.05*MM);
        DataSource sphere3 = new Sphere(new Vector3d( 2*radius,radius,0), radius);
        DataSource sphere3a = new Sub(new Abs(new Sphere(new Vector3d(2*radius,-radius,0), radius)),0.05*MM);
        DataSource sphere4 = new Sphere(new Vector3d( 4*radius,radius,0), radius);
        DataSource sphere4a = new Sub(new Abs(new Sphere(new Vector3d(4*radius,-radius,0), radius)),0.05*MM);

        PeriodicWrap wrap = new PeriodicWrap(new Vector3d(period, 0,0),new Vector3d(0,period, 0));
        wrap.setOrigin(new Vector3d(-period/2, -period/2,0));
        Box box = new Box(boxSize, boxSize, boxDepth);
        box.set("rounding", 0.5*MM);
        box.addTransform(wrap);
        box.addTransform(new Translation(new Vector3d(0,0,-radius-boxDepth)));

        Scene scene = new Scene(new Bounds(-s,s,-s,s,-s,s));
        scene.addShape(new Shape(box, new SingleColorMaterial(0.7,0.7,0.7)));

        SingleColorMaterial green = new SingleColorMaterial(0.,0.9,0.);
        green.setShaderParam("transmittanceCoeff", new Vector3d(0.001,0.01,0.001));
        green.setShaderParam("shininess", 0.5);

        SingleColorMaterial yellow = new SingleColorMaterial(0.9,0.9,0.);
        yellow.setShaderParam("transmittanceCoeff", new Vector3d(0.01,0.01,0.001));
        yellow.setShaderParam("shininess", 0.5);

        SingleColorMaterial pink = new SingleColorMaterial(1,0.,0.3);
        pink.setShaderParam("transmittanceCoeff", new Vector3d(0.01,0.001,0.01));
        pink.setShaderParam("shininess", 0.5);
        
        SingleColorMaterial blue = new SingleColorMaterial(new Color(0.0,0.,1));
        blue.setShaderParam("transmittanceCoeff", new Vector3d(0.001,0.001,0.01));
        blue.setShaderParam("shininess", 0.5);

        SingleColorMaterial cyan = new SingleColorMaterial(new Color(0,0.2,0.5));
        cyan.setShaderParam("transmittanceCoeff", new Vector3d(0.001,0.01,0.01));
        cyan.setShaderParam("shininess", 0.5);

        SingleColorMaterial white = new SingleColorMaterial(new Color(0.9,0.9,0.9));
        white.setShaderParam("transmittanceCoeff", new Vector3d(0.001,0.001,0.001));
        white.setShaderParam("shininess", 0.5);

        SingleColorMaterial black = new SingleColorMaterial(new Color(0.05,0.05,0.05));
        black.setShaderParam("transmittanceCoeff", new Vector3d(0.001,0.001,0.001));
        black.setShaderParam("shininess", 0.5);


        scene.addShape(new Shape(sphere1, cyan));
        scene.addShape(new Shape(sphere1a, cyan));
        scene.addShape(new Shape(sphere2, pink));
        scene.addShape(new Shape(sphere2a, pink));
        scene.addShape(new Shape(sphere3, yellow));
        scene.addShape(new Shape(sphere3a, yellow));
        scene.addShape(new Shape(sphere4, white));
        scene.addShape(new Shape(sphere4a, white));
        scene.addShape(new Shape(sphere0, black));
        scene.addShape(new Shape(sphere0a, black));

        return scene;
    }

    /**
       Spheres made of different materials 
     */
    static Scene makeSceneBallsLatticeMM(double radius){
    
        double period = 2*radius + 0.5*MM;
        double s = 4.5*period;

        Sphere sphere1 = new Sphere(radius);

        PeriodicWrap wrap = new PeriodicWrap(new Vector3d(period, 0,0),new Vector3d(0,period, 0),new Vector3d(0,0,period));

        wrap.setOrigin(new Vector3d(-period/2, -period/2, -period/2));

        sphere1.setTransform(wrap);
        Intersection int1 = new Intersection(sphere1, new Box(7*period,7*period,7*period));
        SingleColorMaterial blue = new SingleColorMaterial(new Color(0.5,0.5,1, 0.1));
        blue.setShaderParam("transmittanceCoeff", new Vector3d(0.01,0,0));
        blue.setShaderParam("shininess", 0.5);

        Scene scene = new Scene(new Bounds(-s,s,-s,s,-s,s));
        scene.addShape(new Shape(int1, blue));
        

        // semi transparent sphere

        return scene;
    }

    static SceneImageDataSource makeSceneImageDataSource(Scene scene){
        
        scene.setLights(makeSingleWhiteLight());

        MatrixCamera camera = new MatrixCamera(getView(), Math.atan(0.25));

        SceneImageDataSource sids = new SceneImageDataSource(scene,camera);
        sids.set("raytracingDepth", sm_raytracingDepth);
        sids.set("maxIntersections", sm_maxIntersections);
        sids.set("surfaceJump", sm_surfaceJump);

        sids.initialize();
        return sids;
                
    }

    static Light[] makeSingleWhiteLight(){

        double intensity = 0.9;

        Light[] lights = new Light[]{
            new Light(new Vector3d(-10,10,20), new Color(1,1,1), 0.1, 1.),
        };
        lights[0].set("angularSize", 0.2); 
        return lights;
    }

    static Light[] make3ColorLights(){

        double intensity = 0.9;

        Light[] lights = new Light[]{
            new Light(new Vector3d(10,0,20),new Color(1,0,0),0.1,intensity),
            new Light(new Vector3d(10,10,20),new Color(0,1,0),0,intensity),
            new Light(new Vector3d(0,10,20),new Color(0,0,1),0,intensity),            
        };
        
        lights[0].set("angularSize", 0.05); lights[1].set("angularSize", 0.05);lights[2].set("angularSize", 0.05);
        //lights[0].set("angularSize", 0.01); lights[1].set("angularSize", 0.1);lights[2].set("angularSize", 0.2);
        //lights[0].set("angularSize", 0.2); lights[1].set("angularSize", 0.2);lights[2].set("angularSize", 0.2);

        return lights;
    }


    /**
     * Test basic usage
     */
    public void devTestScript() throws IOException {

        int width = 256;
        int height = 256;
        String scriptPath = "test/scripts/gyrosphere_params.js";
        String baseDir = null;
        //URI uri = new File().toURI();
        //Script s = new Script(uri);

        ScriptManager sm = ScriptManager.getInstance();
        String jobID = UUID.randomUUID().toString();
        HashMap<String, Object> params = new HashMap<String, Object>();

        String script = IOUtils.toString(new FileInputStream(scriptPath));
        ScriptResources sr = sm.prepareScript(jobID, baseDir, script, params, false);
        sm.executeScript(sr);
        assertTrue("Eval failed", sr.evaluatedScript.isSuccess());

        Scene scene = (Scene)sr.evaluatedScript.getResult();

        MatrixCamera camera = new MatrixCamera(getView());

        SceneImageDataSource sids = new SceneImageDataSource(scene,camera);

        ImageMaker im = new ImageMaker();

        im.set("imgRenderer",sids);
        im.set("width", width);
        im.set("height", height);
        im.setBounds(new Bounds(-1,1,-1,1,-1,1));

        BufferedImage image = im.getImage();

        ImageIO.write(image, "png", new File("/tmp/renderedScene.png")); 
        
    }

    static Matrix4f getView() {
        return getView(4);
    }

    static Matrix4f getView(double pos) {
        float[] DEFAULT_TRANS = new float[]{0, 0, (float) pos};
        float z = DEFAULT_TRANS[2];
        float rotx = 0;
        float roty = 0;

        Vector3f trans = new Vector3f();
        Matrix4f tmat = new Matrix4f();
        Matrix4f rxmat = new Matrix4f();
        Matrix4f rymat = new Matrix4f();

        trans.z = z;
        tmat.set(trans, 1.0f);

        rxmat.rotX(rotx);
        rymat.rotY(roty);

        Matrix4f mat = new Matrix4f();
        mat.mul(tmat, rxmat);
        mat.mul(rymat);

        return mat;
    }

    public static void main(String arg[])throws Exception{
        
        //new TestSceneImageDataSource().devTestScript();
        //new TestSceneImageDataSource().devTestPerformance();
        //new TestSceneImageDataSource().devTestScene();
        new TestSceneImageDataSource().devTestRotation();
        //new TestSceneImageDataSource().devTestDataSource();
    }
}
