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

// External Imports


// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import javax.vecmath.Vector3d;

// Internal Imports

import abfab3d.core.Vec;
import abfab3d.core.Bounds;
import abfab3d.core.DataSource;
import abfab3d.core.AttributeGrid;
import abfab3d.core.TriangleProducer;
import abfab3d.core.TriangleCollector;
import abfab3d.core.AttributedTriangleProducer;
import abfab3d.core.AttributedTriangleCollector;

import abfab3d.util.ColorMapperDistance;
import abfab3d.util.ColorMapperDensity;
import abfab3d.util.ColorMapper;

import abfab3d.geom.Octahedron;
import abfab3d.geom.TriangulatedModels;

import abfab3d.grid.op.ImageMaker;
import abfab3d.grid.op.SliceMaker;


import abfab3d.grid.op.GridMaker;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;

/**
 * Tests the functionality of Sphere
 *
 * @version
 */
public class TestDistanceToMeshDataSource extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDistanceToMeshDataSource.class);
    }

    public void testNothing() {
        printf("testNothing()\n");
    }


    //
    //  testing distance to sphere 
    //
    void devTestDistanceSlice() throws Exception {
        
        double sx = 1.*MM; 
        double sy = 1.*MM; 
        double sz = 2.*MM; 
        double margins = 10*MM;
        double radius = 10*MM;
        int imgWidth = 1000;
        int imgHeight = 1000;
        double voxelSize = 1*MM;
        double bandWidth = voxelSize;
        //TriangleProducer producer = new TriangulatedModels.Box(0.4*voxelSize,0.4*voxelSize,0.4*voxelSize,sx, sy, sz);
        TriangleProducer producer = new TriangulatedModels.Sphere(radius, new Vector3d(0,0,0), 1);
        
        DistanceToMeshDataSource dmds = new DistanceToMeshDataSource(producer);
        dmds.set("margins", margins);
        dmds.set("useMultiPass", false);
        dmds.set("voxelSize", voxelSize);

        //dmds.set("surfaceVoxelSize", 1./3);
        dmds.set("interpolationType", DistanceToMeshDataSource.INTERPOLATION_LINEAR);
        //dmds.set("shellHalfThickness", 2.6);
        dmds.set("maxDistance", 10*MM);
        
        Bounds mbounds = dmds.getMeshBounds();
        printf("mesh bounds:(%s)\n", mbounds);
        dmds.initialize();
        dmds.initialize();
        

        //double s = radius + margins;
        //Bounds bounds = new Bounds(-s, s, -s, s, 0*MM, 0*MM);
        double s = radius + margins + 5*voxelSize;
        Bounds bounds = new Bounds(-s, +s, -s, s, 0*MM, 0*MM);

        int N = 30;
        Vec pnt = new Vec(3);
        Vec data = new Vec(3);
        if(false){
            for(int i = 0; i <= N; i++){
                double 
                    y = -s + 2*i*s/N,
                    z = 0,
                    x = 0;
                
                pnt.v[0] = x;
                pnt.v[1] = y;
                pnt.v[2] = z;
                dmds.getDataValue(pnt, data);
                printf("(%7.2f, %7.2f, %7.2f) -> %7.2f mm\n", x/MM, y/MM,z/MM, data.v[0]/MM);
            }
        }
        ImageMaker im = new ImageMaker();

        ColorMapper colorMapper = new ColorMapperDistance(bandWidth);        
        if(false){
            BufferedImage img1 = im.renderImage(imgWidth, imgHeight, bounds, new SliceDistanceColorizer(dmds, colorMapper));        
            ImageIO.write(img1, "png", new File("/tmp/00_sphere_dist_java.png"));        
        }
        int NN = 50;
        for(int i  = 0; i <= NN; i++){
            double vs = voxelSize/2;
            double z0 = -vs*NN/2;
            Bounds bnds = new Bounds(-s, +s, -s, s, z0 + vs*i, z0+vs*i);
            BufferedImage img1 = im.renderImage(imgWidth, imgHeight, bnds, new SliceDistanceColorizer(dmds, colorMapper));        
            ImageIO.write(img1, "png", new File(fmt("/tmp/00_sphere_dist_java_%03d.png",i)));                            
        }
        
    }


    //
    //  testing colored distance sphere 
    //
    void devTestColoredSphere() throws Exception {
        
        double margins = 10*MM;
        double radius = 10*MM;
        double patternSize = 2*MM;
        int imgWidth = 1000;
        int imgHeight = 1000;
        double voxelSize = 0.1*MM;
        double bandWidth = 1*MM;
        TriangleProducer tProducer = new TriangulatedModels.Sphere(radius, new Vector3d(0,0,0), 5);
        AttributedTriangleProducer atProducer = new MeshColorizer(tProducer, MeshColorizer.COLORING_SURFACE);
        DataSource colorizer = new CheckerboardColorizer3D(1./10, new Vector3d(1.,1,0.2),new Vector3d(0.2,0.3,1.0));
        DistanceToMeshDataSource dmds = new DistanceToMeshDataSource(atProducer, colorizer);
        dmds.set("margins", margins);
        dmds.set("useMultiPass", true);
        dmds.set("voxelSize", voxelSize);

        //dmds.set("surfaceVoxelSize", 1./3);
        dmds.set("interpolationType", DistanceToMeshDataSource.INTERPOLATION_LINEAR);
        //dmds.set("shellHalfThickness", 2.6);
        dmds.set("maxDistance", 10*MM);
        
        Bounds mbounds = dmds.getMeshBounds();
        printf("mesh bounds:(%s)\n", mbounds);
        dmds.initialize();
        
        double s = radius + margins + 5*voxelSize;
        Bounds bounds = new Bounds(-s, +s, -s, s, 0*MM, 0*MM);

        int N = 30;
        Vec pnt = new Vec(3);
        Vec data = new Vec(6);
        if(false){
            for(int i = 0; i <= N; i++){
                double 
                    y = -s + 2*i*s/N,
                    z = 0,
                    x = 0;
                
                pnt.v[0] = x;
                pnt.v[1] = y;
                pnt.v[2] = z;
                dmds.getDataValue(pnt, data);
                printf("(%7.2f, %7.2f, %7.2f) -> %7.2f mm\n", x/MM, y/MM,z/MM, data.v[0]/MM);
            }
        }
        ImageMaker im = new ImageMaker();

        ColorMapper colorDistance = new ColorMapperDistance(bandWidth);        
        if(true){
            BufferedImage img1 = im.renderImage(imgWidth, imgHeight, bounds, new SliceDistanceColorizer(dmds, colorDistance));        
            ImageIO.write(img1, "png", new File("/tmp/00_colored_sphere_dist_java.png"));        
            BufferedImage img2 = im.renderImage(imgWidth, imgHeight, bounds, new SliceColorColorizer(dmds));        
            ImageIO.write(img2, "png", new File("/tmp/00_colored_sphere_color_java.png"));        
        }

        SliceMaker sm = new SliceMaker();
        ColorMapper colorizers[] = new ColorMapper[]{
            new ColorMapperDistance(bandWidth),
            new ColorMapperDensity(),
            new ColorMapperDensity(),
            new ColorMapperDensity(),
        };

        for(int ch = 0; ch < 4; ch++){
            double z = 0*MM;
            Vector3d orig = new Vector3d(bounds.xmin, bounds.ymin, z);
            Vector3d pntu = new Vector3d(bounds.xmax, bounds.ymin, z);
            Vector3d pntv = new Vector3d(bounds.xmin, bounds.ymax, z);
            BufferedImage img = sm.renderSlice(imgWidth, imgHeight,  orig, pntu, pntv, dmds, ch,  colorizers[ch]);        
            ImageIO.write(img, "png", new File(fmt("/tmp/00_colored_sphere_%d_java.png", ch)));        
        }
        
            /*
        if(false){
            int NN = 50;
            for(int i  = 0; i <= NN; i++){
                double vs = voxelSize/2;
                double z0 = -vs*NN/2;
                Bounds bnds = new Bounds(-s, +s, -s, s, z0 + vs*i, z0+vs*i);
                BufferedImage img1 = im.renderImage(imgWidth, imgHeight, bnds, new SliceDistanceColorizer(dmds, colorMapper));        
                ImageIO.write(img1, "png", new File(fmt("/tmp/00_color_sphere_dist_java_%03d.png",i)));                            
            }
        }
            */
        
    }


    static class MeshColorizer implements AttributedTriangleProducer, TriangleCollector {

        TriangleProducer m_tProducer; 
        AttributedTriangleCollector m_atCollector; 
        static final int 
            COLORING_NONE = 0,
            COLORING_SURFACE = 2,
            COLORING_VOLUME = 3;


        int m_dataDimension = 3;
        int m_coloringType = COLORING_NONE;

        Vec m_w0, m_w1, m_w2; // work data vectors  

        public MeshColorizer(TriangleProducer producer, int coloringType){

            m_tProducer = producer;
            switch(coloringType){
            default:
            case 0:
                // no coloring 
                m_coloringType = COLORING_NONE;
                m_dataDimension = 3;
                break;
            case 1:
                // "texture" coloring 
                m_coloringType = COLORING_SURFACE;
                m_dataDimension = 5;
                break;
            case 2: // volume coloring 
                m_coloringType = COLORING_VOLUME;
                m_dataDimension = 6;
                break;                
            }

            m_w0 = new Vec(m_dataDimension);
            m_w1 = new Vec(m_dataDimension);
            m_w2 = new Vec(m_dataDimension);

        }


        public int getDataDimension(){
            return m_dataDimension;
        }

        public boolean getAttTriangles(AttributedTriangleCollector atCollector){
            m_atCollector = atCollector;
            return m_tProducer.getTriangles(this);
        }

        public boolean addTri(Vector3d p0, Vector3d p1, Vector3d p2){
            
            m_w0.set(p0);
            m_w1.set(p1);
            m_w2.set(p2);
            switch(m_coloringType){
            default:
            case COLORING_NONE: 
                break;
            case COLORING_SURFACE: 
                getTextureCoord2(m_w0);
                getTextureCoord2(m_w1);
                getTextureCoord2(m_w2);
                break;
            case COLORING_VOLUME: 
                getTextureCoord3(m_w0);
                getTextureCoord3(m_w1);
                getTextureCoord3(m_w2);
                break;
            }
            return m_atCollector.addAttTri(m_w0, m_w1, m_w2);
        }        
        
        //
        // writes surface texture coordinate into p.v[3],p.v[4]
        //
        void getTextureCoord2(Vec p){  
            p.v[3] = p.v[0];
            p.v[4] = p.v[1];
        }
        //
        // writes surface texture coordinate into p.v[3],p.v[4],p.v[5],
        //
        void getTextureCoord3(Vec p){            
            p.v[3] = p.v[0];
            p.v[4] = p.v[1];            
            p.v[5] = p.v[2];            
        }
        
    } // class MeshColorizer

    //
    // convert 2D texture coordinate into 3D color components using checkerboard pattern
    //
    static class CheckerboardColorizer implements DataSource {

        Vector3d m_color0 = new Vector3d();
        Vector3d m_color1 = new Vector3d();
        double m_size;
        public CheckerboardColorizer(double size, Vector3d color0, Vector3d color1){
            m_size = size;
            m_color0.set(color0);
            m_color1.set(color1);            
        }

        public int getDataValue(Vec p, Vec data){            
            int ix = (int)Math.floor(p.v[0]/m_size);
            int iy = (int)Math.floor(p.v[1]/m_size);
            int c = (ix + iy) & 1;
            switch(c){
            case 0: data.set(m_color0); break;
            case 1: data.set(m_color1); break;                
            }
            return 0;
        }
        public int getChannelsCount(){
            return 3;
        }

        public Bounds getBounds(){
            return null;
        }
    } // class CheckerboardColorizer

    //
    // convert 3D texture coordinate into 3D color components using 3D checkerboard pattern
    //
    static class CheckerboardColorizer3D implements DataSource {

        Vector3d m_color0 = new Vector3d();
        Vector3d m_color1 = new Vector3d();
        static final int TYPE_CHECKERS = 0;
        static final int TYPE_SEGMENTS = 1;
        int m_type = TYPE_SEGMENTS;
        double m_size;
        public CheckerboardColorizer3D(double size, Vector3d color0, Vector3d color1){
            m_size = size;
            m_color0.set(color0);
            m_color1.set(color1);            
        }

        public int getDataValue(Vec p, Vec data){

            switch(m_type){
            case TYPE_CHECKERS:
            default: 
                {
                    int ix = (int)Math.floor(p.v[0]/m_size);
                    int iy = (int)Math.floor(p.v[1]/m_size);
                    int iz = (int)Math.floor(p.v[2]/m_size);
                    int c = (ix + iy + iz) & 1;
                    switch(c){
                    case 0: data.set(m_color0); break;
                    case 1: data.set(m_color1); break;                
                    }
                }
                break;
            case TYPE_SEGMENTS:
                {                
                    int t = (int)Math.floor(((Math.atan2(p.v[1],p.v[0])/Math.PI+1)/m_size));
                    if( (t & 1) == 1) 
                        data.set(m_color0);
                    else 
                        data.set(m_color1);                
                }
                break;
            }
            return 0;
        }
         public int getChannelsCount(){
            return 3;
        }
         public Bounds getBounds(){
            return null;
        }
  } // class CheckerboardColorizer3D



    public static void main(String[] args) throws Exception {

        //new TestDistanceToMeshDataSource().devTestSlice();
        new TestDistanceToMeshDataSource().devTestColoredSphere();
        
    }
    
}