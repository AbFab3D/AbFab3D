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

package abfab3d.util;

import java.util.Vector;
import java.util.Arrays;
import java.util.Comparator;


import javax.vecmath.Vector2d;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;


/**
   packing an array of rectangular objects into smales possible square 
   the algorithm works as follows. g

   1) sort objects into decreasing height 
      rectangles are possible rotates to make (height >= width) 
   2) pack objects into vertical strip of some fixed size into horizontal layers 
   with rectangles aligned at the bottom of each layer 
   2.1) fit next rect into lowest possible layer 
   2.2) if no existing layer can accomodatate new object - create new layer 
   3) cut strip into several vertical strips of fixed height to fit into square region 

   see the illustration 
   <embed src="doc-files/RectPacking.svg" type="image/svg+xml"/> 

   surfvey of packing algorithms: http://cgi.csc.liv.ac.uk/~epa/surveyhtml.html>
   
   @author Vladimir Bulatov
  
 */
public class RectPacking {

    public static boolean DEBUG = true;

    // vector of rectangles 
    Vector<Rect> m_vrect = new Vector<Rect>();
    Rect m_rect[]; // array of sorted rectangles 
    
    double m_maxW = 0; // max width of rectangles
    double m_maxH = 0; // max height of rectangles
    
    int m_count;  // count of rectangles 

    double m_bandWidth = 0.;
    double m_bandHeight = 0.;

    double m_area = 0.;

    Vector<Row> m_rows = new Vector<Row>(100);

    //double m_widthFactor = 5; // width of packing band relative to maximal rectangle width 
    

    public RectPacking(){
        
    }

    /**
       add new rectangle to the pack 
     */
    public void addRect(double w, double h){
        if(w > m_maxW)
            m_maxW = w;

        if(h > m_maxH)
            m_maxH = h;
        m_area += w*h;

        m_vrect.add(new Rect(0,0,w,h));

        m_count++;

    }


    /**
       perform the packing 
     */
    public void pack(){

        m_rect = m_vrect.toArray(new Rect[m_count]);
        long t0 = time();
        Arrays.sort(m_rect, new HeightComparator());
        if(DEBUG){
            printf("rect count: %d\n", m_count);
            printf("max width: %7.2f\n", m_maxW);
            printf("max height: %7.2f\n", m_maxH);
            long t1 = time();
            printf("sorting time: %d ms\n", (t1 - t0));            
            //printf("sorted rect\n");
            //for(int i = 0; i < m_count; i++){
            //   // printf("%s\n", m_rect[i]);
            //}
            t0 = t1;
        }

        // choose the width of the vertical band 
        m_bandWidth = Math.sqrt(m_area*1.2); // we use single wide band 
        // bad case of really wide rect 

        if(m_bandWidth < m_maxW) m_bandWidth = m_maxW;


        // first rect shall has m_maxH after sorting 
        double currentY = 0.;
        Row currentRow = new Row(currentY, m_bandWidth, m_maxH);
        m_bandHeight = currentRow.rowH;

        m_rows.add(currentRow);
        for(int i = 0; i < m_count; i++){
            Rect rect = m_rect[i];

            if(!currentRow.addRect(rect)){
                // failed to add rect to current row                 
                currentY += currentRow.rowH; // location of new row 
                currentRow = new Row(currentY, m_bandWidth, rect.h);
                m_rows.add(currentRow);
                m_bandHeight += currentRow.rowH;
                
                if(!currentRow.addRect(rect)) // should not happens 
                    throw new RuntimeException(fmt("faled to add rect[%s] to empty row", rect));                
            }            
        }        

        if(DEBUG){
            printf("packed rows: %d\n", m_rows.size());
            printf("packed rect: %7.2f x %7.2f\n", m_bandWidth, m_bandHeight);
            printf("packing ratio: %7.2f\n", m_area/(m_bandWidth* m_bandHeight));
            printf("packing time: %d ms\n", (time() - t0));            
            if(false){
                for(int i = 0; i < m_rows.size(); i++){
                    printf("row: %s\n", m_rows.get(i));
                }
            }
            
            //for(int i = 0; i < m_count; i++){
            //    printf("%s\n", m_rect[i]);
            //}
        }
    }

    /**
       
       returns the size of the rectangular area where the rectangles are packed 
     */
    public Vector2d getPackedSize(){

        return new Vector2d(m_bandWidth, m_bandHeight);

    }

    /**
       @param origin lower left corner of the packed rectangle 
       @return origin of the packed rectangle 
     */
    public void getRectOrigin(int index, Vector2d origin){
        Rect rect = m_vrect.get(index);
        origin.x = rect.x;
        origin.y = rect.y;
    }
    
    /**
       represents single rectangle 
     */
    public static class Rect {
       
        public double x; // x-coord 
        public double y; // ycoord 
        public double w; // width 
        public double h; // height

        public Rect(double x, double y, double w, double h){
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
        public String toString(){
            return fmt("[%7.2f,%7.2f,%7.2f,%7.2f]", x,y,w,h);
        }
    }

    public static class HeightComparator implements Comparator<Rect> {

        public int compare(Rect r1, Rect r2) {            
            double d = r2.h - r1.h;
            if(d < 0.) return -1;
            else if(d > 0.) return 1;
            else return 0;
        }
    }
    
    static class Row {

        double rowW;
        double rowH; 
        double rowY;
        int rectCount = 0;

        double remainder; 
        
        // row of given width and height 
        Row(double yposition, double rwidth, double rheight){

            rowY = yposition;

            rowH = rheight;
            rowW = rwidth;

            remainder = rwidth;            
        }

        // try to place rect int this row          
        boolean addRect(Rect rect){
            if(rect.w <= remainder){
                rectCount++;
                // have space 
                rect.x = rowW - remainder; 
                rect.y = rowY; 
                remainder-= rect.w; 
                return true;
            } else {
                // no place 
                return false;
            }
        }
        public String toString(){
            return fmt("[y:%7.2f, h: %7.2f, count %4d]", rowY, rowH, rectCount);
        }
    }

}
