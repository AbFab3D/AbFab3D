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

package abfab3d.io.output;

import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import abfab3d.util.TriangleCollector;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector3d;


import static java.lang.System.currentTimeMillis;

import static abfab3d.util.Output.printf; 


/**
   class to write collection of triangles to STL file 

   triangles are written via addTri() method of TriangleCollector interface 
   
   it is important to call close(). 
   It writes total triangle count to the file header. 

   @author Vladimir Bulatov
 */
public class STLWriter implements TriangleCollector {

    static byte buffer[] = new byte[4];
    static final int STL_HEADER_LENGTH = 80;

    static final double SCALE = 1000; // to convert to MM standard for STL 

    static final byte STLHeader[] = new byte[STL_HEADER_LENGTH];    

    Vector3d defaultNormal = new Vector3d(0.,0.,0.);
    OutputStream m_output; 
    int m_triCount = 0;
    FileOutputStream m_fileStream;
    String m_path; // file path to write to 

    boolean isOpened = false; // if output file is opened 

    static void writeInt4(OutputStream out, int value) throws IOException{
        
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 24) & 0xFF);
        
    }
    
    static byte buffer2[] = new byte[2];
    static void writeInt2(OutputStream out, int value) throws IOException{
        //Output.out.println(" " + value);
        buffer2[0] = (byte)(value & 0xFF);
        buffer2[1] = (byte)((value >> 8) & 0xFF);
        out.write(buffer2);
        
    }

    static byte[] getBytes(int value){
        return new byte[]{
            (byte)(value & 0xFF),
            (byte)((value >> 8) & 0xFF),
            (byte)((value >> 16) & 0xFF),
            (byte)((value >> 24) & 0xFF)
        };
    }
    
    static void writeFloat(OutputStream out, float fvalue) throws IOException{
        
        int value = Float.floatToRawIntBits(fvalue);
        writeInt4(out, value);
        
    }

    static void writeFloat(OutputStream out, double dvalue) throws IOException{
        
        int value = Float.floatToRawIntBits((float)dvalue);
        writeInt4(out, value);
        
    }
    
    static void writeVector(OutputStream out, Vector3f v) throws IOException{

        writeFloat(out, v.x*SCALE);
        writeFloat(out, v.y*SCALE);
        writeFloat(out, v.z*SCALE);

    }

    static void writeVector(OutputStream out, Vector3d v) throws IOException{

        writeFloat(out, v.x*SCALE);
        writeFloat(out, v.y*SCALE);
        writeFloat(out, v.z*SCALE);

    }

    static void writeTri(OutputStream out, Vector3f normal, Vector3f v[], int v1, int v2, int v3) throws IOException{
        
        writeVector(out, normal);

        writeVector(out, v[v1]);
        writeVector(out, v[v2]);
        writeVector(out, v[v3]);
        
        writeInt2(out, 0); // attribute byte count 0
        
    }    

    /**

       constructor to write to specified file
       
     */
    public STLWriter(String filePath) throws IOException {
        

        m_path = filePath;
        m_fileStream = new FileOutputStream(m_path);
        isOpened = true;
        
        m_output = new BufferedOutputStream(m_fileStream);
        
        m_output.write(STLHeader); 
        writeInt4(m_output, 0);        
        
    }

    public void finalize(){
        try {
            if(isOpened){
                close();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
       does the writing of triangle count and closing the stream 
     */
    public void close() throws IOException{

        isOpened = false;
        m_output.flush();
        m_fileStream.flush();
        m_output.close();
        m_fileStream.close();

        // write tricount after STL header 
        RandomAccessFile raf = new RandomAccessFile(m_path,"rw");
        raf.seek(STL_HEADER_LENGTH);
        raf.write(getBytes(m_triCount));
        raf.close();

    }

    /**
       method of interface TrianleCollector 
     */
    public boolean addTri(Vector3d v0, Vector3d v1, Vector3d v2 ){

        try {
            m_triCount++;

            writeVector(m_output, defaultNormal);
            writeVector(m_output, v0);
            writeVector(m_output, v1);
            writeVector(m_output, v2);
            
            writeInt2(m_output, 0); // attribute byte count 0
            return true;

        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
} // class STLWriter
 
