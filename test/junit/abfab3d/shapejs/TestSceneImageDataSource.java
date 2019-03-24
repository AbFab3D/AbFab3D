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
import java.io.IOException;
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

import abfab3d.datasources.Sphere;
import abfab3d.datasources.Box;
import abfab3d.datasources.VolumePatterns;
import abfab3d.datasources.Intersection;
import abfab3d.datasources.Union;


import static abfab3d.core.Units.MM;
import static abfab3d.core.Output.printf;
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
    
    public void devTestScene() throws IOException {
        
        int imageWidth = 500, imageHeight = 500;
        
        SceneImageDataSource sids = makeSceneImageDataSource(makeScene3BallsMM(5*MM));
        //SceneImageDataSource sids = makeSceneImageDataSource(makeScene3Balls(5*MM));
        //SceneImageDataSource sids = makeSceneImageDataSource(makeSceneBall(5*MM));
        //SceneImageDataSource sids = makeSceneImageDataSource(makeSceneGyroid());
        
        //sids.set("shadowsQuality",5);
        sids.set("shadowsQuality",10);

        ImageMaker im = new ImageMaker();
        
        im.set("imgRenderer",sids);
        im.set("threadCount",8);
        im.set("width", imageWidth);
        im.set("height", imageWidth);
        im.setBounds(new Bounds(-1,1,-1,1,-1,1));
        long t0 = time();
        printf("image[%d x %d]\n", imageWidth, imageHeight);
        BufferedImage image = im.getImage();
        printf("render time: %dms\n", (time()-t0));

        ImageIO.write(image, "png", new File("/tmp/renderedScene.png")); 
        
    }

    /**
       test specific pixels 
     */
    public void devTestDataSource() throws IOException {
        
        int N = 25;
        SceneImageDataSource sids = makeSceneImageDataSource(makeSceneBall(7*MM));
        sids.set("shadowsQuality",10);
        sids.initialize();
        sids.setDebug(false);
        double vs = 2./N;
        Bounds bounds = new Bounds(-1,1,-1,1,-1,1,vs);
        Vec img = new Vec(4);
        
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
    }
    
    static Scene makeSceneGyroid(){
    
        double radius = 25*MM;
        double s = 25.5*MM;
        double period = 18*MM;
        DataSource sphere = new Sphere(radius);
        DataSource gyroid = new VolumePatterns.Gyroid(period, 2*MM);
        //gyroid.set("period", 10*MM);
        Intersection intersect = new Intersection();
        intersect.setBlend(2*MM);
        intersect.add(sphere);
        intersect.add(gyroid);
        
        //Shape shape = new Shape(intersect, new SingleColorMaterial(0.5,0.5,0.5));
        //Scene scene = new Scene(shape,new Bounds(-s,s,-s,s,-s,s));
        Scene scene = new Scene(intersect,new Bounds(-s,s,-s,s,-s,s));
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

        Box box = new Box(2*s, 2*s, 2*MM);
        box.addTransform(new Translation(new Vector3d(0,0,-s*0.75)));
        union.add(box);
        
        Scene scene = new Scene(union,new Bounds(-s,s,-s,s,-s,s));
        return scene;
    }


    static Scene makeScene3BallsMM(double radius){
    
        //double radius = 5*MM;
        double s = 25.5*MM;
        DataSource sphere1 = new Sphere(radius);
        DataSource sphere2 = new Sphere(new Vector3d(2*radius, 0,0), radius);
        DataSource sphere3 = new Sphere(new Vector3d(-2*radius, 0,0), radius);
        DataSource sphere4 = new Sphere(new Vector3d(0,2*radius,0), radius);

        Box box = new Box(2*s, 2*s, 2*MM);
        box.addTransform(new Translation(new Vector3d(0,0,-s*0.75)));
        
        Scene scene = new Scene(new Bounds(-s,s,-s,s,-s,s));
        scene.addShape(new Shape(sphere1, new SingleColorMaterial(1,0.5,0.5)));
        scene.addShape(new Shape(sphere2, new SingleColorMaterial(0.5,1,0.5)));
        Material blue = new SingleColorMaterial(0.5,0.5,1);
        scene.addShape(new Shape(sphere3, blue));
        scene.addShape(new Shape(sphere4, blue));

        scene.addShape(new Shape(box, new SingleColorMaterial(0.7,0.7,0.7)));

        return scene;
    }

    static SceneImageDataSource makeSceneImageDataSource(Scene scene){
        
        double intensity = 0.9;

        Light[] lights = new Light[]{
            //new Light(new Vector3d(10,0,20),new Color(1,0,0),0.1,intensity),
            // new Light(new Vector3d(10,10,20),new Color(0,1,0),0,intensity),
            //new Light(new Vector3d(0,10,20),new Color(0,0,1),0,intensity),
            
            new Light(new Vector3d(-10,10,20), new Color(1,1,1), 0.1, 1.),

            //new Light(new Vector3d(-10,10,20), new Color(1,0,0), 0.1, 1.),
            //new Light(new Vector3d(0,10,20), new Color(0,0,1),0, 1.),            
            //new Light(new Vector3d(10,10,20),new Color(0,1,0), 0,  1.),
        };

        lights[0].set("angularSize", 0.2); 
        //lights[0].set("angularSize", 0.05); lights[1].set("angularSize", 0.05);lights[2].set("angularSize", 0.05);
        //lights[0].set("angularSize", 0.01); lights[1].set("angularSize", 0.1);lights[2].set("angularSize", 0.2);
        //lights[0].set("angularSize", 0.2); lights[1].set("angularSize", 0.2);lights[2].set("angularSize", 0.2);
        scene.setLights(lights);

        MatrixCamera camera = new MatrixCamera(getView());

        SceneImageDataSource sids = new SceneImageDataSource(scene,camera);
        sids.initialize();
        return sids;
                
    }


    /**
     * Test basic usage
     */
    public void devTestScript() throws IOException {

        int width = 100;
        int height = 100;
        String scriptPath = "test/scripts/gyrosphere_params.js";
        String baseDir = null;
        //URI uri = new File().toURI();
        //Script s = new Script(uri);

        ScriptManager sm = ScriptManager.getInstance();
        String jobID = UUID.randomUUID().toString();
        HashMap<String, Object> params = new HashMap<String, Object>();

        ScriptResources sr = sm.prepareScript(jobID, baseDir, scriptPath, params, true);
        sm.executeScript(sr);
        assertTrue("Eval failed", sr.evaluatedScript.isSuccess());

        Scene scene = (Scene)sr.evaluatedScript.getResult();

        ImageSetup setup = new ImageSetup(width, height, getView(), ImageSetup.IMAGE_JPEG, 0.5f, AntiAliasingType.NONE, false, 0f, 1);

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
        
        new TestSceneImageDataSource().devTestScene();
        //new TestSceneImageDataSource().devTestDataSource();

    }

}
