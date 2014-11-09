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

package abfab3d.io.output;

// External Imports



import java.util.Random;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;

import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;


import java.io.File;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import java.util.Vector;



import javax.vecmath.Vector3d;
import javax.vecmath.Vector2d;

import javax.imageio.ImageIO;


// external imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
import abfab3d.grid.AttributeMakerGeneral;
import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.ArrayAttributeGridInt;
import abfab3d.grid.ArrayAttributeGridLong;
import abfab3d.grid.AttributeDesc;
import abfab3d.grid.AttributeChannel;

import abfab3d.util.TriangleProducer;
import abfab3d.util.TriangleCollector;
import abfab3d.util.RectPacking;

import abfab3d.geom.TriangulatedModels;
import abfab3d.geom.ParametricSurfaces;
import abfab3d.geom.ParametricSurfaceMaker;

import abfab3d.util.MathUtil;
import abfab3d.util.ImageGray16;
import abfab3d.util.DefaultLongConverter;
import abfab3d.util.LongConverter;

import abfab3d.datasources.DataChannelMixer;
import abfab3d.datasources.Box;
import abfab3d.datasources.Sphere;
import abfab3d.datasources.Ring;
import abfab3d.datasources.Torus;
import abfab3d.datasources.ImageBitmap;
import abfab3d.datasources.DataTransformer;
import abfab3d.datasources.Intersection;
import abfab3d.datasources.Union;
import abfab3d.datasources.Subtraction;
import abfab3d.datasources.Triangle;
import abfab3d.datasources.Cylinder;
import abfab3d.datasources.LimitSet;
import abfab3d.datasources.VolumePatterns;

import abfab3d.transforms.RingWrap;
import abfab3d.transforms.FriezeSymmetry;
import abfab3d.transforms.WallpaperSymmetry;
import abfab3d.transforms.Rotation;
import abfab3d.transforms.CompositeTransform;
import abfab3d.transforms.Scale;
import abfab3d.transforms.SphereInversion;
import abfab3d.transforms.Translation;
import abfab3d.transforms.PlaneReflection;

import abfab3d.mesh.IndexedTriangleSetBuilder;

import abfab3d.datasources.VolumePatterns;

import abfab3d.grid.op.GridMaker;

import static java.lang.Math.sqrt;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;

import static abfab3d.util.MathUtil.maxDistance;
import static abfab3d.util.MathUtil.copyVector3;

import static abfab3d.util.Units.MM;


/**
 * Tests the functionality of TextureRenderer
 *
 * @author Vladimir Bulatov
 */
public class TestTextureRenderer extends TestCase {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTextureRenderer.class);
    }

    public void testDumb(){
        //this test here is to make Test happy. 
    }


    public void checkLinearTransform(){
        
        Random rnd = new Random(101);

        //Vector3d t3[] = new Vector3d[]{new Vector3d(1,0,0),new Vector3d(0,1,0),new Vector3d(0,0,1)};
        //Vector2d t2[] = new Vector2d[]{new Vector2d(0,0),new Vector2d(100,0),new Vector2d(0,100)};
        double s = 100;
        int N = 10000000;
        double maxDist = 0;
        TriangleInterpolator tr = new TriangleInterpolator();
        double tt3[] = new double[3];
        double t3[][] = new double [3][3];
        double t2[][] = new double[3][2];
        double ct3[] = new double[3];

        long t0 = time();
        for(int k = 0; k < N; k++){
            
            for(int i = 0; i < 3; i++){

                double v3[] = t3[i];
                double v2[] = t2[i];

                v3[0] = s*rnd.nextDouble(); 
                v3[1] = s*rnd.nextDouble(); 
                v3[2] = s*rnd.nextDouble(); 
                v2[0] = s*rnd.nextDouble(); 
                v2[1] = s*rnd.nextDouble(); 
            }
            
            tr.init(t2, t3);
                        
            for(int i = 0; i < 3; i++){                
                tr.interpolate(t2[i][0],t2[i][1], tt3);
                double dd = maxDistance(t3[i], tt3);
                if(dd > maxDist) maxDist = dd;
            }
        }
        printf("count: %d time %d ms maxDist: %8.4g\n", N, (time() - t0), maxDist);
        // typical output count: 10000000 time 6547 ms maxDist: 1.102e-06
        // normally maxDist is below 1.e-14, but if 2D triangle is almost degenerate, there is larger error
    }

    public void makeTextureRendering(){

        double vs = 0.1*MM;
        int subvoxelResolution = 255;

        // data grid dimension 
        int nx = 200, ny = 200, nz = 200;
        // texture grid dimension 
        int ntx = 200, nty = 200;
        double sx = nx*vs, sy = ny*vs, sz = nz*vs, tx = ntx*vs, ty = nty*vs;
        
        
        double dataBounds[] = new double[]{-sx/2,sx/2,-sy/2,sy/2,-sz/2,sz/2};        
        double texBounds[] = new double[]{0, tx, 0, vs, 0, ty};

        AttributeGrid dataGrid = new ArrayAttributeGridInt(nx,ny,nz, vs, vs);
        dataGrid.setGridBounds(dataBounds);

        AttributeGrid texGrid = new ArrayAttributeGridInt(ntx,1,nty, vs, vs);
        texGrid.setGridBounds(texBounds);


        Sphere sphere1 = new Sphere(-0.1*sx, 0, 0, 0.55*sx);
        Sphere sphere2 = new Sphere(-0.1*sx, 0, 0, 0.45*sx);
        Sphere sphere3 = new Sphere(-0.1*sx, 0, 0, 0.35*sx);
        //VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(0.3*sx, 0.1*sx);
        //Torus torus = new Torus(0.2*sx, 0.1*sx);
        DataChannelMixer mux = new DataChannelMixer(sphere1, sphere2, sphere3);
        
        GridMaker gm = new GridMaker();  
        gm.setBounds(dataBounds);
        gm.setThreadCount(1);
        gm.setMargin(0);
        gm.setSource(mux);
        gm.setAttributeMaker(new AttributeMakerGeneral(new int[]{8,8,8}, true));
 
        
        printf("gm.makeGrid()\n");
        gm.makeGrid(dataGrid);        
        printf("gm.makeGrid() done\n");

        
        AttributeDesc attDesc = new AttributeDesc();
        attDesc.addChannel(new AttributeChannel(AttributeChannel.COLOR, "color", 24, 0));
        dataGrid.setAttributeDesc(attDesc);
        new SVXWriter().write(dataGrid, "/tmp/slices/dataGrid.svx");

        TextureRenderer renderer = new TextureRenderer(dataGrid, texGrid);

        double dataTri[][] = new double[][]{{nx-1,1,1},{1,ny-1,1},{1,1,nz-1}};
        //double dataTri[][] = new double[][]{{1,ny/2,1},{nx-1,ny/2,1},{1, ny/2 ,nz-1}};    // XZ plane 
        //double dataTri[][] = new double[][]{{ny/2,1,1},{ny/2,nx-1,1},{ny/2, 1, nz-1}};    // YZ plane 
        //double dataTri[][] = new double[][]{{1,1,ny/2},{nx-1,1,ny/2},{1, nz-1,ny/2}};    // XY plane 
        //double texTri[][] = new double[][]{{1,1},{ntx-1,1},{0.5*ntx -1, 0.866*nty-1 }};
        double texTri[][] = new double[][]{{1,1},{ntx-1,1},{1, nty-1 }};
        double texTri1[][] = new double[][]{{ntx-1,nty-1},{ntx-1,1},{1, nty-1 }};
        

        renderer.renderTriangle(dataTri, texTri);
        renderer.renderTriangle(dataTri, texTri1);
        
        texGrid.setAttributeDesc(attDesc);
        new SVXWriter().write(texGrid, "/tmp/slices/texGrid.svx");

        //double v3[][] = new double[][]{{}}; 
        
    }

    /**
       testing textured triangles output 
     */
    public void makeTexturedMesh() throws IOException{

        printf("makeTexturedTetrahedron()\n");
        
        double vs = 0.1*MM;

        double s = 10*MM;
        double bounds[] = new double[]{-s/2,s/2,-s/2,s/2,-s/2,s/2};

        double gs = s/vs; // size of grid 
        double gap = 1.;
        double center = gs/2;

        String baseDir = "/tmp/tex/";
        TriangleProducer mesh = new ParametricSurfaceMaker(new ParametricSurfaces.Patch(new Vector3d(2,2,center),new Vector3d(gs-2,2,center),
                                                                                        new Vector3d(gs-2,gs-2,center),new Vector3d(2,gs-2,center),
                                                                                        3,3));
        //TriangleProducer mesh = new TriangulatedModels.TetrahedronInParallelepiped(2.,2.,2., gs-2, gs-2, gs-2,0);
        //TriangleProducer mesh = new TriangulatedModels.Parallelepiped(2.,2.,2., gs-2, 0.7*gs-2, 0.5*gs-2);
        //TriangleProducer mesh = new TriangulatedModels.Sphere(gs/2-2, new Vector3d(gs/2,gs/2,gs/2), 1);
        //TriangleProducer mesh = new TriangulatedModels.Torus(gs/4, gs/2, 0.02);
        
        STLWriter writer = new STLWriter(baseDir + "mesh.stl");
        mesh.getTriangles(writer);
        writer.close();            
        
        //IndexedTriangleSetBuilder itsb = new IndexedTriangleSetBuilder();
        //mesh.getTriangles(itsb);
        //printf("vert count: %d\n", itsb.getVertexCount());
        //printf("tri count: %d\n", itsb.getFaceCount());

        //double vert[] = itsb.getVertices();
        
        TrianglePacker tp = new TrianglePacker(gap);
        mesh.getTriangles(tp);
   
        int rc = tp.getTriCount();
        printf("tripacker count: %d\n", tp.getTriCount());        
        tp.packTriangles();
        
        Vector2d area = tp.getPackedSize();

        printf("packedSize: [%7.2f x %7.2f] \n", area.x, area.y); 
               
        int imgWidth = (int)(area.x+2*gap);
        int imgHeight = (int)(area.y+2*gap);

        BufferedImage outImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = outImage.createGraphics();
        graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setStroke(new BasicStroke(1.f, BasicStroke.CAP_ROUND,  BasicStroke.JOIN_ROUND));        
        graphics.setColor(new Color(220,220,220));         
        graphics.fillRect(0,0, imgWidth, imgHeight);         
        
        tp.drawTriangles(graphics);
                    
        ImageIO.write(outImage, "png", new File(baseDir + "texture.png"));

        int ngx = (int)gs;        
        AttributeGrid colorGrid = makeColorGrid_2(bounds, vs);

        AttributeDesc attDesc = new AttributeDesc();
        attDesc.addChannel(new AttributeChannel(AttributeChannel.COLOR, "color", 24, 0));
        colorGrid.setAttributeDesc(attDesc);
        new SVXWriter(2).write(colorGrid, "/tmp/tex/colorGrid.svx");

        double texBounds[] = new double[]{0, imgWidth*vs, 0, vs, 0, imgHeight*vs};
        AttributeGrid texGrid = new ArrayAttributeGridInt(imgWidth,1,imgHeight, vs, vs);
        texGrid.setGridBounds(texBounds);
        texGrid.setAttributeDesc(attDesc);
        
        tp.renderTexturedTriangles(colorGrid, texGrid);

        new SVXWriter().write(texGrid, "/tmp/tex/texGrid.svx");

        double coord[] = tp.getCoord();
        int coordIndex[] = tp.getCoordIndex();

        printf("		coord Coordinate{\n"+
               "			point[\n");
        for(int k = 0; k < coord.length; k += 3){
            printf("\t\t\t%7.5f %7.5f %7.5f\n",coord[k],coord[k+1],coord[k+2]);
        }        
        printf("			]\n"+
               "		}\n"+
               "		coordIndex[\n");
        for(int k = 0; k < coordIndex.length; k += 3){
            printf("\t\t\t%d %d %d -1\n",coordIndex[k],coordIndex[k+1],coordIndex[k+2]);
        }                
        printf("		]\n");
        double texCoord[] = tp.getTexCoord();
        int texCoordIndex[] = tp.getTexCoordIndex();
        printf("		texCoord TextureCoordinate {\n"+
               "			point [\n");
        for(int k = 0; k < texCoord.length; k += 2){
            printf("\t\t\t%7.5f %7.5f\n",texCoord[k]/imgWidth, (imgHeight - texCoord[k+1])/imgHeight);
        }
        printf("			]\n"+
               "		}\n"+
               "		texCoordIndex[\n");
        for(int k = 0; k < texCoordIndex.length; k += 3){
            printf("\t\t\t%d %d %d -1\n",texCoordIndex[k],texCoordIndex[k+1],texCoordIndex[k+2]);
        }                        
        printf("		]\n");        
        
    }  //makeTexturedMesh()


    AttributeGrid makeColorGrid_1(double bounds[], double vs){

        
        int ng[] = MathUtil.getGridSize(bounds, vs);
        
        AttributeGrid dataGrid = new ArrayAttributeGridInt(ng[0],ng[1],ng[2], vs, vs);

        dataGrid.setGridBounds(bounds);        
        
        double sx = bounds[1] - bounds[0];

        Sphere sphere1 = new Sphere(-0.1*sx, 0, 0, 1.55*sx);
        Sphere sphere2 = new Sphere(-0.1*sx, 0, 0, 0.45*sx);
        Sphere sphere3 = new Sphere(-0.1*sx, 0, 0, 0.35*sx);

        //VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(0.3*sx, 0.1*sx);
        //Torus torus = new Torus(0.2*sx, 0.1*sx);

        DataChannelMixer mux = new DataChannelMixer(sphere1, sphere2, sphere3);
        
        GridMaker gm = new GridMaker();  
        gm.setThreadCount(1);
        gm.setMargin(0);
        gm.setSource(mux);

        gm.setAttributeMaker(new AttributeMakerGeneral(new int[]{8,8,8}, true));
         
        printf("gm.makeGrid(%d x %d x %d)\n", ng[0],ng[1],ng[2]);
        gm.makeGrid(dataGrid);        
        printf("gm.makeGrid() done\n");

        return dataGrid;
    }
    

    AttributeGrid makeColorGrid_2(double bounds[], double vs){

        
        int ng[] = MathUtil.getGridSize(bounds, vs);
        
        AttributeGrid dataGrid = new ArrayAttributeGridInt(ng[0],ng[1],ng[2], vs, vs);

        dataGrid.setGridBounds(bounds);        
        
        double sx = bounds[1] - bounds[0];

        Sphere sphere1 = new Sphere(-0.1*sx, 0, 0, 1.55*sx);
        Sphere sphere2 = new Sphere(-0.1*sx, 0, 0, 0.45*sx);
        VolumePatterns.Gyroid gyroid = new VolumePatterns.Gyroid(0.5*sx, 0.02*sx);

        DataChannelMixer mux = new DataChannelMixer(sphere1, sphere2, gyroid);
        
        GridMaker gm = new GridMaker();  
        gm.setThreadCount(1);
        gm.setMargin(0);
        gm.setSource(mux);

        gm.setAttributeMaker(new AttributeMakerGeneral(new int[]{8,8,8}, true));
         
        printf("gm.makeGrid(%d x %d x %d)\n", ng[0],ng[1],ng[2]);
        gm.makeGrid(dataGrid);        
        printf("gm.makeGrid() done\n");

        return dataGrid;
    }


    public static void main(String[] args) throws IOException {
        //new TestSlicesWriter().multichannelTest();
        //new TestTextureRenderer().checkLinearTransform();
        //new TestTextureRenderer().checkTextureRendering();
        new TestTextureRenderer().makeTexturedMesh();
    }    
}
