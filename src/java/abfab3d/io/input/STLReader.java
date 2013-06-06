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

package abfab3d.io.input;

import java.io.*;
import java.util.zip.GZIPInputStream;


import javax.vecmath.Vector3f;
import javax.vecmath.Vector3d;
import abfab3d.util.TriangleCollector;

import static java.lang.System.currentTimeMillis;

/**
   STL files reader. Triangles from STL fiule are passed to TriangleCollector 

   @author Vladimir Bulatov
 */
import static abfab3d.util.Output.printf; 


/**
   class to read collection of triangles from STL file 

   @author Vladimir Bulatov
 */
public class STLReader  {

    static final boolean DEBUG = false;

    public static final double SCALE = 1./1000.; //to convert form STL standard millimeters into meters
    TriangleCollector out;

    public static int readInt(DataInputStream data) throws IOException{
        
        int i = data.readUnsignedByte() | (data.readUnsignedByte()<<8)|
            (data.readUnsignedByte()<<16)|(data.readUnsignedByte()<<24);      
        
        return i;
    }
    
    public static float readFloat(DataInputStream data) throws IOException{
        
        //return data.readFloat();
        int i = data.readUnsignedByte() | (data.readUnsignedByte()<<8)|
            (data.readUnsignedByte()<<16)|(data.readUnsignedByte()<<24);      
        return Float.intBitsToFloat(i);
        
    }
    
    public static Vector3d readVector3Df(DataInputStream data, Vector3d v) throws IOException{
        
        v.x = readFloat(data)*SCALE;
        v.y = readFloat(data)*SCALE;
        v.z = readFloat(data)*SCALE;

        return v;
    }

    public STLReader(){        
    }
    
    public void read(String path, TriangleCollector out) throws IOException{
        if(DEBUG)
            printf("STLReader.read(%s, %s)\n", path, out);
        long t0 = currentTimeMillis();
        
        this.out = out;

        InputStream bis = null;

        if (path.lastIndexOf(".gz") > -1) {
            bis = new GZIPInputStream(new FileInputStream(path), (1 << 14));
        } else {
            bis = new BufferedInputStream(new FileInputStream(path), (1 << 14));
        }
        DataInputStream data = new DataInputStream(bis);
        
        data.skip(80);
        
        int fcount = readInt(data);      
        if(DEBUG)
            printf("fcount: %d\n",fcount);
        int faces = 0;

        Vector3d 
            v0 = new Vector3d(),
            v1 = new Vector3d(),
            v2 = new Vector3d();
        //for(int f = 0; f < fcount; f++){
        try {
            while(true) {
                // ignore normal 
                data.skip(3*4);
                readVector3Df(data, v0);                
                readVector3Df(data, v1);                
                readVector3Df(data, v2);                
                if(out != null)
                    out.addTri(v0,v1, v2);
                
                data.skip(2); // unsused stuff 
                faces++;                
            }
        } catch(Exception e){
            data.close();
            
            if(DEBUG)
                printf("faces read: %d\n", faces);
        }
        if(DEBUG)
            printf("STLReader.read() done in %d ms\n", (currentTimeMillis() - t0));
        
    }

}

