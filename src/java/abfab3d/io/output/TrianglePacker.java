/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011-2016
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

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.GeneralPath;

import java.util.Vector;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector2d;

import abfab3d.mesh.IndexedTriangleSetBuilder;

import abfab3d.core.LongConverter;
import abfab3d.core.TriangleCollector;
import abfab3d.core.TriangleProducer;
import abfab3d.util.RectPacking;

import abfab3d.core.AttributeGrid;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.time;

/**
   class responsible for packing and rendering textured triangles into 2D texure image 

   @author Vladimir Bulatov
 */
public class TrianglePacker implements TriangleCollector, TriangleProducer {

    static final boolean DEBUG = false;

    // 3D triangles ordered according to canonical triangles
    Vector<Vector3d> m_tri = new Vector<Vector3d>();
    // canonicaly oriented 2D triangles 
    Vector<CanonicalTri> m_ctri = new Vector<CanonicalTri>();

    // texture coordinates of packed triangles in pixel units
    double m_texCoord[];
    // indices of texture coord
    int m_texCoordIndex[];
    // coordinates of 3D vertices
    double m_triCoord[];

    // indices of 3D faces
    int m_triIndex[];

    int m_triCount = 0;

    // gap between triangles in pixel units
    double m_gap = 1.; 
    // rectangles packer 
    RectPacking m_packer; 
    double m_pixelSize = 1;
    
    public TrianglePacker(){
    }

    public void setTexturePixelSize(double pixelSize){
        m_pixelSize = pixelSize;
    }

    /**
       set extra gap around triangles 
       @param gap size of safety band in pixel units 
     */
    public void setGap(double gap){

        m_gap = gap;

    }
    
    public Vector2d getPackedSize(){

        return m_packer.getPackedSize();

    }

    int getTriCount(){
        
        return m_triCount;
    }

    /**
       return texture triangles coordinates
     */
    public double[] getTexCoord(){

        return m_texCoord;

    }

    public int[] getTexCoordIndex(){

        return m_texCoordIndex;

    }

    public double[] getCoord(){

        return m_triCoord;

    }

    public int[] getCoordIndex(){

        return m_triIndex;

    }
    
    /**
       return size of rectangle with given index 
       
    */
    private void getCanonicalTri(int index, CanonicalTri tri){
        
        tri.set(m_ctri.get(index));
        
    }
    
    /**
       feeds triangles to TriangleCollector
     */
    public boolean getTriangles(TriangleCollector tcollector){
        
        for(int i = 0; i < m_triCount; i++){
            int ti = 3*i;
            tcollector.addTri(m_tri.get(ti),m_tri.get(ti+1),m_tri.get(ti+2));
        }  

        return true;
    }

    /**
       accept triangle from TriangleProducer 
     */
    public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2){
        if(false)printf("addTri((%7.5f,%7.5f,%7.5f),(%7.5f,%7.5f,%7.5f),(%7.5f,%7.5f,%7.5f))\n",v0.x,v0.y,v0.z,v1.x,v1.y,v1.z,v2.x,v2.y,v2.z);
        double len01 = dist(v0, v1);
        double len12 = dist(v1, v2);
        double len20 = dist(v2, v0);
        double maxLen = len01;
        int maxIndex = 0;
        if(len12 > maxLen){
            maxLen = len12; 
            maxIndex = 1;                
        }
        if(len20 > maxLen){
            maxLen = len20; 
            maxIndex = 2;
        }       
        // store triangle in canonical orientation with longest side first 
        switch(maxIndex){
        case 0: add(maxLen,v0,v1,v2); break;
        case 1: add(maxLen,v1,v2,v0); break;
        case 2: add(maxLen,v2,v0,v1); break;
        }
        
        return true;
    }
    
    
    Vector3d 
        p0 = new Vector3d(),
        p1 = new Vector3d(),
        p2 = new Vector3d(),
        normal = new Vector3d();
    
    //
    // places triangle into canonical position 
    //
    private void add(double len01, Vector3d v0, Vector3d v1, Vector3d v2){

        // area of triangle 
        
        p1.set(v1);
        p2.set(v2);
        
        p1.sub(v0);
        p2.sub(v0);
        
        normal.cross(p1,p2);
        
        // tri area 
        double triWidth = len01;
        double triArea2 = normal.length();
        double triHeight = triArea2/triWidth;
        
        double v1x = triWidth;
        double v2y = triHeight; // y- coordinate of 2D triangle 2
        double v2x = p1.dot(p2)/len01;
        
        m_ctri.add(new CanonicalTri(v1x/m_pixelSize, v2x/m_pixelSize, v2y/m_pixelSize, m_gap));
        
        //
        // all triangles are oriented canonically: v0,v1 is the longest side 
        //
        m_tri.add(new Vector3d(v0));
        m_tri.add(new Vector3d(v1));
        m_tri.add(new Vector3d(v2));
        m_triCount++;
    }
    

    /**
       pack all the triangles into 2D rectangle 
     */
    public void packTriangles(){
        
        int rc = getTriCount();
        
        CanonicalTri ct = new CanonicalTri();
        
        m_packer = new RectPacking();
        
        printf("RectPacking start\n");        
        long t0 = time();

        for(int i = 0; i < rc; i++){
            getCanonicalTri(i,ct);
            // add triangle with a safety gap around it 
            //TODO - make proper margin around triangle 
            
            //m_packer.addRect(ct.v1x+2*m_gap, ct.v2y + 2*m_gap);
            m_packer.addRect(ct.getWidth(), ct.getHeight());
            
            if(false)printf("tri: %d %s\n", i, ct);
        }
        m_packer.pack();        
        if(DEBUG)printf("RectPacking done\n");
        
        m_texCoord = new double[6*m_triCount];

        Vector2d origin = new Vector2d();

        for(int k = 0; k < m_triCount; k++){

            getCanonicalTri(k,ct);
            m_packer.getRectOrigin(k, origin);

            //TODO - make proper margin around triangle 
            //origin.x += m_gap;
            //origin.y += m_gap;

            int texindex = k*6;

            ct.getTexCoord(origin.x, origin.y,texindex, m_texCoord);

        }   

        // re-build the mesh 
        IndexedTriangleSetBuilder itsb = new IndexedTriangleSetBuilder();
        getTriangles(itsb);
        m_triCoord = itsb.getVertices();
        m_triIndex = itsb.getFaces();
        
        m_texCoordIndex = new int[m_triCount*3];
        
        for(int k = 0; k < m_texCoordIndex.length; k++){
            m_texCoordIndex[k] = k;
        }

        if(DEBUG)printf("packTriangles() done %d ms\n", time()-t0);
    }


    /**
       render triangles into texture 
       
       @param dataGrid 3D grid used to make colors
       @param texGrid 2D grid (3D grid with single y-slice) to accept the texture 
       @param extendWidth width of extension of rendered triangles
     */
    public void renderTexturedTriangles(AttributeGrid dataGrid, LongConverter colorMaker, AttributeGrid texGrid, double extWidth){
        
        TextureRenderer tr = new TextureRenderer(dataGrid, colorMaker, texGrid);

        double tri[][] = new double[3][3];
        double tex[][] = new double[3][2];
        double extTri[][] = new double[3][2];
        double triLines[][] = new double[3][3];


        for(int k = 0; k < m_triCount; k++){
            int tindex = 3*k;
            Vector3d 
                v0 = m_tri.get(tindex),
                v1 = m_tri.get(tindex+1),
                v2 = m_tri.get(tindex+2);
            copy(v0, tri[0]);
            copy(v1, tri[1]);
            copy(v2, tri[2]);
            int texindex = 6*k;

            tex[0][0] = m_texCoord[texindex];
            tex[0][1] = m_texCoord[texindex+1];
            tex[1][0] = m_texCoord[texindex+2];
            tex[1][1] = m_texCoord[texindex+3];
            tex[2][0] = m_texCoord[texindex+4];
            tex[2][1] = m_texCoord[texindex+5];

            if(extWidth == 0.0)
                tr.renderTriangle(tri, tex);
            else 
                tr.renderTriangleExtended(tri, tex, extWidth, extTri, triLines);
        }
    }
    
    
    /**
       draw all triangles into given graphics 
     */
    public void drawTriangles(Graphics2D graphics){        
                
        for(int k = 0; k < m_triCount; k++){

            int texindex = k*6;
            GeneralPath tri = new GeneralPath();
            tri.moveTo(m_texCoord[texindex],   m_texCoord[texindex+1]);
            tri.lineTo(m_texCoord[texindex+2], m_texCoord[texindex+3]);
            tri.lineTo(m_texCoord[texindex+4], m_texCoord[texindex+5]);
            tri.lineTo(m_texCoord[texindex],   m_texCoord[texindex+1]);
            
            graphics.setColor(new Color(150, 150, 255)); 
            graphics.fill(tri);
            graphics.setColor(Color.black); 
            graphics.draw(tri);
            
        }
    } // draw Triangles         

    //
    // distance between vectors 
    //
    static double dist(Vector3d v0, Vector3d v1){
        
        double x = v0.x - v1.x; 
        double y = v0.y - v1.y; 
        double z = v0.z - v1.z; 
        
        return Math.sqrt(x*x + y*y + z*z);
        
    }

    static void copy(Vector3d src, double[] dest){

        dest[0] = src.x;
        dest[1] = src.y;
        dest[2] = src.z;

    }

} //  class TrianglePacker

