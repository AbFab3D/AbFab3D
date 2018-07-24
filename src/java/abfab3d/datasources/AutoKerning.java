/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
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
import java.awt.*;

import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;

import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;


import java.awt.image.BufferedImage;

import java.util.Hashtable;

import javax.vecmath.Vector3d;
import javax.imageio.ImageIO;
import java.io.File;

import java.util.ArrayList;

import abfab3d.intersect.DataSourceIntersector;

import abfab3d.core.Vec;
import abfab3d.core.DataSource;
import abfab3d.core.Grid2D;
import abfab3d.core.Bounds;
import abfab3d.core.Grid2DProducer;
import abfab3d.core.MathUtil;

import abfab3d.transforms.Translation;

import abfab3d.grid.Grid2DSourceWrapper;
import abfab3d.grid.op.Grid2DtoImage;
import abfab3d.grid.op.ImageToGrid2D;
import abfab3d.grid.op.ImageMaker;

import static java.lang.Math.floor;
import static java.lang.Math.ceil;
import static java.lang.Math.min;
import static java.lang.Math.max;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;



/**
   responsibe for text layout with auto kerning 
 */
public class AutoKerning {

    static final boolean DEBUG = false;
    static final boolean WRITE_DEBUG_IMAGES = false;
    static final double DOT_SIZE = 4; // size of dots used in the debug images 


    /**
       
     */
    public GlyphVector layoutGlyphVector(Graphics2D textGraphics, Font font, String text, double fontSize, double glyphSpacing, double resolution) {

        if(DEBUG)printf("AutoKerning.layoutGlyphVector(%s, fontSize:%7.2f glyphSpacing:%7.2f, resolution:%7.2f)\n", text, fontSize, glyphSpacing, resolution);
        //double fontSize = 200;
        double x0 = fontSize*1.0; // arbitrary, used for visual tests only 
        double y0 = fontSize*0.8;
        
        Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
        
        //map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        map.put(TextAttribute.SIZE, new Double(fontSize));        
        font = font.deriveFont(map);
        double halfWidth = glyphSpacing/2;
        char ctext[] = text.toCharArray();
        int glyphCount = ctext.length;
        double glyphLocation[] = new double[glyphCount];

        if(DEBUG)printf("halfWidt: %7.2f\n", halfWidth);

        //GlyphVector gv = font.createGlyphVector(glyphGraphics.getFontRenderContext(), ctext);
        GlyphVector gv = font.layoutGlyphVector(textGraphics.getFontRenderContext(), ctext, 0, ctext.length, 0);

        Rectangle2D textRect = gv.getVisualBounds(); 
        if(DEBUG)printf("textRect: [x:%5.1f y:%5.1f w:%5.1f h:%5.1f]\n", 
                       textRect.getX(),textRect.getY(),textRect.getWidth(), textRect.getHeight());


        //double posx = textRect.getX(), posy = 0;
        double posx = 0, posy = y0;

        if(DEBUG)printf("glyph vector\n");
        double glyphOrigins[] = new double[ctext.length];
        for(int i = 0; i < glyphCount; i++){
            Point2D glyphPos = gv.getGlyphPosition(i);            
            Rectangle2D rect = gv.getGlyphVisualBounds(i).getBounds2D();            
            rect = getExpandedRect(rect, halfWidth);
            //glyphGraphics.draw(rect);
            double xr = posx + rect.getX();
            double yr = rect.getY();
            
            textGraphics.setColor(Color.blue);
            //glyphGraphics.draw(new Rectangle2D.Double(posx, rect.getY()+y, rect.getWidth(),rect.getHeight()));
            //glyphGraphics.draw(new Rectangle2D.Double(posx+x, y+rect.getY(), rect.getWidth(),rect.getHeight()));
            posy = rect.getY();
            
            Point2D pos = gv.getGlyphPosition(i); 

            if(DEBUG)printf("glyph[%d]:(%c) gpos:(%6.1f,%4.1f); grect:[(%6.1f,%6.1f); %5.1f x %5.1f]\n",
                            i, ctext[i],glyphPos.getX(), glyphPos.getY(), rect.getX(),rect.getY(),rect.getWidth(),rect.getHeight());   
            Point2D gp = new Point2D.Double(posx+(glyphPos.getX() - rect.getX()),0);
            gv.setGlyphPosition(i, gp); 
            
            textGraphics.setColor(Color.red);
            fillCircle(textGraphics, x0+gp.getX(),y0+gp.getY(), DOT_SIZE);

            glyphOrigins[i] = rect.getCenterX() - glyphPos.getX();
            
            textGraphics.setColor(Color.blue);
            fillCircle(textGraphics, x0+rect.getCenterX(),y0+rect.getCenterY(), DOT_SIZE);
            
            posx += rect.getWidth();
            
        }
                
        textGraphics.setColor(Color.BLACK); textGraphics.drawGlyphVector(gv, (float)x0,(float)y0);        
        textGraphics.setColor(Color.blue); drawCircle(textGraphics,x0,y0,8);
        
        BasicStroke outlineStroke = new BasicStroke((float)glyphSpacing, BasicStroke.CAP_ROUND,  	BasicStroke.JOIN_ROUND);
        textGraphics.setStroke(outlineStroke); textGraphics.setColor(Color.gray);
        Shape textOutline = gv.getOutline();
        AffineTransform at = new AffineTransform(); at.translate(x0,y0);

        textGraphics.setTransform(at);
        textGraphics.draw(textOutline);
        
        textGraphics.setColor(Color.BLACK);
        textGraphics.drawGlyphVector(gv, 0,0);
        //g.setTransform(savedTransform);
        double rectMargin = 2; // margin around glyphs to make sure we have some white space 
        double textMargins = halfWidth + rectMargin;

        if(DEBUG)printf("stacked rectangles\n");
        
        ArrayList<ImageMap> glyphImages = new ArrayList<ImageMap>();

        for(int i = 0; i < glyphCount; i++){
            
            Point2D gp = gv.getGlyphPosition(i);
            Shape glyphShape = gv.getGlyphOutline(i);
            Rectangle2D srect = getExpandedRect(glyphShape.getBounds(), textMargins);
            //printf("glyph: %c [%7.2f %7.2f ]\n",ctext[i], srect.getMinY(), srect.getMaxY());
            //if(DEBUG)printf("exRect:[%6.1f,%6.1f; %5.1f x %5.1f]\n",srect.getX(),srect.getY(),srect.getWidth(),srect.getHeight());
            //Rectangle2D rect = gv.getGlyphVisualBounds(i).getBounds2D();            
            textGraphics.setColor(Color.blue);
            textGraphics.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND,  	BasicStroke.JOIN_ROUND));
            textGraphics.draw(srect);
            textGraphics.setStroke(outlineStroke);  
            textGraphics.setColor(Color.gray);
            textGraphics.draw(glyphShape);
            textGraphics.setColor(Color.black);
            textGraphics.fill(glyphShape);            
            textGraphics.setColor(Color.red);
            fillCircle(textGraphics, gp.getX(),gp.getY(), 5);

            Grid2D glyphGrid = getShapeImage(glyphShape, srect, outlineStroke, true, true);
            Bounds b = glyphGrid.getGridBounds();

            String label = fmt("glyph:0x%X; font:%s",(int)ctext[i], font);
            if(DEBUG)printf("glyph: %c  glyphBounds:[%7.1f %7.1f;%7.1f %7.1f]\n", ctext[i], b.xmin, b.xmax,b.ymin, b.ymax);
            if(DEBUG)printf("  label:%s\n", label);

            ImageMap imageMap = new ImageMap((Grid2DProducer)(new Grid2DSourceWrapper(label,glyphGrid)), srect.getWidth(), srect.getHeight(), 1.);
            // flip of y-coordinate 
            imageMap.set("center",new Vector3d(0, -srect.getCenterY(),0.));
            imageMap.set("whiteDisplacement",1.);
            imageMap.set("blackDisplacement",-1.);
            //imageMap.initialize();
            glyphImages.add(imageMap);

        }
        
        if(DEBUG)printf("glyphCount:%d\n", glyphImages.size());
        for(int i = 0; i < glyphCount; i++){
            if (WRITE_DEBUG_IMAGES) {
                ImageMap imageMap = glyphImages.get(i);
                Grid2D grid = imageMap.getBitmapGrid();
                Bounds b = imageMap.getBounds();
                if(DEBUG)printf("glyph: %c  bounds:[%7.1f %7.1f;%7.1f %7.1f]\n", text.charAt(i), b.xmin, b.xmax,b.ymin, b.ymax);

                try {                    
                    ImageIO.write(Grid2DtoImage.getARGBImage(grid), "png", new File(fmt("/tmp/01_glyph_%02d.png",i)));
                } catch(Exception e){
                    e.printStackTrace();
                }                           
            }            
        }
        
        DataSourceIntersector dsi = new DataSourceIntersector();
        dsi.set("dimension", 2);
        dsi.set("voxelSize",resolution);
        dsi.set("minStep",resolution);
        dsi.set("maxDistance", 2*font.getSize());

        Vector3d direction = new Vector3d(-1,0,0);
        ImageMap firstGlyph = glyphImages.get(0);
        
        Union un = new Union();
        //un.add(makeLeftFence());
        un.add(firstGlyph);
        Bounds firstGlyphBounds = firstGlyph.getBounds();
        double ymin = firstGlyphBounds.ymin;
        double ymax = firstGlyphBounds.ymax;
        double firstGlyphCenter = firstGlyphBounds.getSizeX()/2;
        double xmin = firstGlyphBounds.xmin + firstGlyphCenter;
        double xmax = firstGlyphBounds.xmax + firstGlyphCenter;

        if(DEBUG)printf("firstGlyphCenter: %7.2f firstGlyphSizeX: %7.2f \n",firstGlyphCenter, firstGlyphBounds.getSizeX());
        if(DEBUG)printf("xmin: %7.2f xmax: %7.2f \n",xmin, xmax);
        firstGlyph.setTransform(new Translation(firstGlyphCenter,0,0));

        glyphLocation[0] =   firstGlyphCenter - glyphOrigins[0];
        
        if(DEBUG)printf("packing glyphs\n");

        for(int i = 1; i < glyphCount; i++){

            ImageMap glyph2 = glyphImages.get(i);
            Bounds b2 = glyph2.getBounds();
            ymin = min(ymin, b2.ymin);
            ymax = max(ymax, b2.ymax);
            if(DEBUG)printf("  glyph[%d](%c)\n",i,text.charAt(i));
            Vector3d start = new Vector3d( xmax + b2.getSizeX()/2, 0, 0);
            if(DEBUG) printf("    start: %7.2f\n",start.x);
            //printValuesOnRay(glyph2, 0, new Vector3d(-b2.getSizeX()/2, 0,0),new Vector3d(1,0,0),1., 100);
            //un.initialize();
            DataSourceIntersector.Result is = dsi.getShapesIntersection(un, glyph2,start, direction);

            if(DEBUG)printf("    packing result: %s\n", is.toString(1.));
            double glyphShift = is.getLocation().x;
            if(is.getCode()  != DataSourceIntersector.RESULT_INTERSECTION_FOUND){
                // no intersection found. Pack glyph to the left side 
                glyphShift = b2.getSizeX()/2;
            }

            glyph2.setTransform(new Translation(glyphShift,0,0));
            glyphLocation[i] = glyphShift -glyphOrigins[i]; 
            if(DEBUG)printf("    glyphShift:%7.2f\n", glyphShift);

            un.add(glyph2);
            xmax = max(xmax, glyphShift + b2.xmax);
            if(DEBUG)printf("    xmax: %7.2f\n", xmax);
        }

        if(WRITE_DEBUG_IMAGES){
            ImageMaker im = new ImageMaker();
            Bounds imBounds = new Bounds(xmin, xmax, ymin, ymax,0,1);
            if(DEBUG)printf("packed text bounds: %s\n", imBounds.toString(1.));
            //un.initialize();
            BufferedImage img = im.renderImage(imBounds.getWidthVoxels(1.), imBounds.getHeightVoxels(1.), imBounds, new DistanceToColor(un));
            try {
                ImageIO.write(img, "png", new File("/tmp/02_packed_glyphs.png"));
            } catch(Exception e){
                e.printStackTrace();            
            }
        }
        //double xmin = glyphImages.get(0);
        //int rightBound[] = new int[0];

        for(int i = 0; i < ctext.length; i++){
            gv.setGlyphPosition(i, new Point2D.Double(glyphLocation[i],0)); 
        }
        
        return gv;
    }
    

    static ImageMap sm_leftFence;

    ImageMap makeLeftFence(){
        if(sm_leftFence != null)
            return sm_leftFence;
        // size of bitmap 
        int width = 10; int height = 20;

        double fenceWidth=10, fenceHeight = 500;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)image.getGraphics();
        g.setColor(Color.white);
        g.fill(new Rectangle2D.Double(0,0,width, height));
        g.setColor(Color.black);
        g.fill(new Rectangle2D.Double(1,1,width-2, height-2));
        
        
        ImageMap imageMap = new ImageMap(image, fenceWidth, fenceHeight, 1.);
        imageMap.set("center",new Vector3d(-fenceWidth/2,0.,0.));
        imageMap.set("whiteDisplacement",1.);
        imageMap.set("blackDisplacement",-1.);
        //imageMap.setTransform(new Translation());
        //imageMap.initialize();
        sm_leftFence = imageMap;
        return sm_leftFence;
        
    }


    static Rectangle2D getExpandedRect(Rectangle2D rect, double width){
        return new Rectangle2D.Double(rect.getX()-width,rect.getY()-width, rect.getWidth() + 2*width, rect.getHeight() + 2*width);
    }


    static final void fillCircle(Graphics2D g, double x, double y, double r){
        g.fill(new Ellipse2D.Double(x-r,y-r,2*r,2*r));
    }

    static final void drawCircle(Graphics2D g, double x, double y, double r){
        g.draw(new Ellipse2D.Double(x-r,y-r,2*r,2*r));
    }


    //
    // draw shape into given rectangle using stroke and return image converted to Grid2D 
    //
    static Grid2D getShapeImage(Shape shape, Rectangle2D rect, Stroke stroke, boolean doFill, boolean doOutline){
        
        roundRect(rect, 1.);

        int width = (int)(rect.getWidth());
        int height = (int)(rect.getHeight());
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D)image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.setColor(Color.white);
        g.fill(new Rectangle2D.Double(0,0,width, height));
        AffineTransform at = new AffineTransform();
        at.translate(-rect.getX(),-rect.getY());
        g.setTransform(at);                
        g.setColor(Color.black);

        g.setStroke(stroke);
        if(doFill)g.fill(shape);

        if(doOutline)g.draw(shape);
        
        double x0 = rect.getX();
        double x1 = rect.getX() + rect.getWidth();
        // rect is in graphics coordinates with Y-down 
        // bounds are in screen coordinates with Y-up 
        double y0 = -(rect.getY() + rect.getHeight());
        double y1 = -(rect.getY());

        double z0 = 0;
        double z1 = 1;

        return ImageToGrid2D.makeGrid(image, true, new Bounds(x0, x1, y0, y1, z0, z1));
        
    }


    /**
       expand rectangle to the nearest voxel boundary
       @return rounder rect 
     */
    public static void roundRect(Rectangle2D rect, double voxelSize){

        double x0 = voxelSize*floor(rect.getX()/voxelSize);
        double y0 = voxelSize*floor(rect.getY()/voxelSize);
        double x1 = voxelSize*ceil((rect.getX() + rect.getWidth())/voxelSize);
        double y1 = voxelSize*ceil((rect.getY() + rect.getHeight())/voxelSize);
        rect.setFrameFromDiagonal(x0,y0, x1,y1);
        
    }

    static void printValuesOnRay(DataSource ds, int dataChannelIndex, Vector3d start, Vector3d direction, double step, int count){
        Vec pnt = new Vec(3);
        Vec value = new Vec(ds.getChannelsCount());
        Vector3d dir = new Vector3d();

        for(int i = 0; i < count; i++){
            double dist = step*i;
            dir.set(direction);
            dir.scale(dist);
            pnt.set(start);
            pnt.addSet(dir);
            ds.getDataValue(pnt, value);
            printf("(%9.5f %9.5f %9.5f) -> %9.5f\n",pnt.v[0],pnt.v[1],pnt.v[2], value.v[dataChannelIndex]);           
        }
    }

    
    static class DistanceToColor implements DataSource {

        DataSource m_dataSource;

        public DistanceToColor(DataSource dataSource){
            m_dataSource = dataSource;
        }

        public Bounds getBounds(){
            return m_dataSource.getBounds();
        }

        public int getChannelsCount(){
            return 4;
        }

        public int getDataValue(Vec pnt, Vec data) {                

            int res = m_dataSource.getDataValue(pnt, data);
            
            double distance = data.v[0];

            double c = 1-MathUtil.step10(distance, 0., 1.);

            data.v[0] = c;
            data.v[1] = c;
            data.v[2] = c;
            data.v[3] = 1;  // alpha               
            return res;
        }

    } // static class DistanceToColor 

} // class AutoKerning