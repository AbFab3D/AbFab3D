/***************************************************************************
 * 
 *                        Shapeways, Inc Copyright (c) 2019
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/


package abfab3d.geom;

import javax.vecmath.Vector3d;



import static abfab3d.core.MathUtil.getDistance;
import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static java.lang.Math.*;


public class ContourOptimizer {

    static final boolean DEBUG = false;
    int m_maxChainLength;
    double m_precision;
    double m_coord[];

    static int sm_debugCount = 1;

    public ContourOptimizer(double precision, int maxChainLength){

        m_precision = precision;
        m_maxChainLength = maxChainLength;

    }

    /**

       @param coord[] array of 3d coordinates of vertices 
       @return new contour optimized according to the parameters
       
     */
    public Contour optimize(double coord[], Contour contour){

        if(DEBUG){
            if(coord.length > 1000 && sm_debugCount > 0){
                sm_debugCount--;
                printf("coord.length:%d\n", coord.length);
            }
        }
        m_coord = coord;

        Vector3d pnt0 = new Vector3d();
        Vector3d pnt1 = new Vector3d();
        Vector3d pnt2 = new Vector3d();

        Contour outcont = new Contour();
        int count = contour.size();

        int chainStart = 0;  // current chain start
        int chainEnd = 1;    // current chain end 

        getContourPoint(contour, chainStart, pnt0); 
        getContourPoint(contour, chainEnd, pnt1);   

        // first point always added ?
        outcont.append(contour.get(chainStart));

        for(int chainNext = 2;  chainNext < count; chainNext++){
            
            getContourPoint(contour, chainNext, pnt2);
            
            if(DEBUG)printf("test chain: [%d - %d, %d]\n", chainStart, chainEnd, chainNext);

            if((chainEnd - chainStart) > m_maxChainLength || 
               getMaxDistance(contour, chainStart+1, chainNext, pnt0, pnt2) > m_precision){

                if(DEBUG)printf("close chain: [%d %d] ", chainStart, chainEnd);
                // current segment was last good approximation 
                outcont.append(contour.get(chainEnd));
                chainStart = chainEnd;
                chainEnd = chainEnd + 1;
                getContourPoint(contour, chainStart, pnt0);
                if(DEBUG)printf("new chain candidate: [%d %d]\n", chainStart, chainEnd);
            } else {
                // extend chain 
                chainEnd = chainNext; 
                if(DEBUG)printf("extend chain to [%d %d]\n", chainStart, chainEnd);
            }
        }
        // append last segment 
        outcont.append(contour.get(chainEnd));
        return outcont;

    }


    public Vector3d getPoint(int pointIndex, Vector3d pnt){

        if(pnt == null)
            pnt  = new Vector3d();
        
        int off = pointIndex*3;
        pnt.x = m_coord[off];
        pnt.y = m_coord[off+1];
        pnt.z = m_coord[off+2];

        return pnt;

    }
    

    public Vector3d getContourPoint(Contour contour, int index, Vector3d pnt){

        return getPoint(contour.get(index), pnt);
    }



    /**
       calculates max distance of chain of points of contour [start, end) from the line via (pnt0, pnt1) 
     */
    public double getMaxDistance(Contour contour, int start, int end, Vector3d pnt0, Vector3d pnt1){


        // working vectors 
        Vector3d p2 = new Vector3d();   // intermediate point 
        Vector3d line = new Vector3d(); // plane tangent
        Vector3d p2n = new Vector3d(); // projection of p2 to line(p0-p1)
        
        line.set(pnt1);
        line.sub(pnt0);
        line.normalize();
        double maxLen = 0;

        for(int i = start; i < end; i++){

            getContourPoint(contour, i, p2);
            p2.sub(pnt0);
            double dot = p2.dot(line);
            p2n.set(line);
            p2n.scale(dot);
            p2.sub(p2n);
            maxLen = max(maxLen, p2.length());
        }
        if(DEBUG)printf("getMaxDistance(  start: %d end: %d maxLen: %5.3f\n", start, end, maxLen);

        return maxLen;
    }

} // public class ContourOptimizer