/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2019
 *                                Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * ***************************************************************************
 */

package abfab3d.io.cli;

import abfab3d.core.Bounds;
import org.web3d.util.DoubleToString;

import java.io.*;
import java.util.Date;

import abfab3d.geom.TriangleMeshSlicer;
import abfab3d.geom.Slice;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.Units.MM;
import static java.lang.Math.*;

/**
 * Write slices to a Common Layer Interface file.
 *
 *  * Spec used:
 *  * http://web.archive.org/web/19970617041930/http://www.cranfield.ac.uk/aero/rapid/CLI/cli_v20.html
 *
 * @author Alan Hudson
 */
public class SLISliceWriter extends BaseSliceWriter implements Closeable {

    private static final boolean DEBUG = false;
    private static final byte CMD_START_LAYER = 1;
    private static final byte CMD_END_LAYER= 2;
    private static final byte CMD_START_POLYLINE = 3;
    private static final byte CMD_START_HATCH = 4;

    public static final String DEFAULT_CREATOR = "CPSLICECONVERTDATEI";

    public static final double DEFAULT_UNITS = 10*MM;

    private OutputStream os;
    private LoggingOutputStream los;
    private DataOutputStream dos;
    private double units;
    private double conv;
    private int sigDigits = 6;
    private boolean geomStarted = false;


    public SLISliceWriter(String file) throws IOException {

        this(file,DEFAULT_UNITS);

    }

    /**
     *
     * @param file
     * @param units 1.0 for mm
     * @throws IOException
     */
    public SLISliceWriter(String file, double units) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        init(bos,units);
    }

    /**
     *
     * @param os
     * @param binary
     * @param units 1.0 for mm
     */
    public SLISliceWriter(OutputStream os, boolean binary, double units) {
        init(os,units);
    }

    /**
     *
     * @param os
     * @param units 1.0 for mm
     */
    private void init(OutputStream os, double units) {
        if (DEBUG) {
            los = new LoggingOutputStream(os);  // TODO: Debug, do not use in production
            os = los;
        }
        this.os = os;


        if (!(os instanceof DataOutputStream)) dos = new DataOutputStream(os);
        else dos = (DataOutputStream) os;

        this.units = units;
        this.conv = 1.0/(units*MM);  // bake the conversion factor
    }

    public void setSigDigits(int digits) {
        sigDigits = digits;
    }

    public void write(Bounds bounds, String creator, SliceLayer[] layers) throws IOException {
        int polyLineCount = 0;
        int fileIndexPos = 128;

        for(SliceLayer layer : layers) {
            polyLineCount += layer.getPolyLines().length;
            /*
            polyLineCount += layer.getPolyLines().length;
            fileIndexPos += 12;  // 12 bytes per layer
            for(PolyLine line : layer.getPolyLines()) {
                fileIndexPos += 3 + line.getPoints().length * 2;
            }
             */
            fileIndexPos += calcSize(layer);
        }

        //fileIndexPos += 1;  // add end marker

        if(DEBUG)printf("fip: %d\n",fileIndexPos);

        setHeader(103,creator,fileIndexPos,layers.length,polyLineCount,units,bounds);
       
        dos.flush();
        if (DEBUG) printf("Position after header: %d\n", los.getCount());
        

        int off = 128;
        for(SliceLayer layer : layers) {
            addLayer(layer);
            dos.flush();
            if (DEBUG) {
                printf("Position after layer: %d  calcSize: %d  off: %d lines: %d points: %d\n", los.getCount()-48,calcSize(layer),off,layer.getPolyLines().length,layer.getTotalPointCount());
            }

            off += calcSize(layer);
        }


        dos.flush();
        if (DEBUG) {
            printf("Position after slice data: %d\n", los.getCount());
        }
        writeIndex(layers);

        close();
    }

    private void setHeader(int version, String creator, int fileIndexPos, int layerCount, int polyLineCount, double scale, Bounds bounds) throws IOException {
        byte[] magic = "EOS 1993 SLI FILE".getBytes();
        byte[] header = new byte[40];
        for(int i=0; i < header.length; i++) {
            header[i] = " ".getBytes()[0];
        }
        System.arraycopy(magic,0,header,0,magic.length);

        dos.write(header);

        writeUnsignedIntegerBinary(dos,version);  // byte 41
        writeUnsignedIntegerBinary(dos,1);  // byte 43. has always been 1
        writeLongBinary(dos,48);  // byte 45.  header size
        writeLongBinary(dos,16);  // byte 49.  u2, has always been 16
        writeLongBinary(dos,128); // byte 53.  u3, has been 128 and 228
        writeLongBinary(dos,128);  // byte 57.  fileSliceDataOffset, Some files have really large values here not sure why
        writeLongBinary(dos, fileIndexPos);  // byte 61

        byte[] carray = creator.getBytes();
        byte[] narray = new byte[40];
        for(int i=0; i < narray.length; i++) {
            narray[i] = " ".getBytes()[0];
        }
        if (carray.length >= 40) {
            System.arraycopy(carray,0,narray,0,40);
        } else {
            System.arraycopy(carray,0,narray,0,carray.length);
        }
        dos.write(narray); // Byte 102

        writeLongBinary(dos,layerCount);  // byte 106
        writeLongBinary(dos,polyLineCount);  // byte 110
        writeLongBinary(dos,18);  // byte 114 //u4, no idea value gets larger with file size
        byte[] unknown = new byte[32];
        dos.write(unknown);
        writeRealBinary(dos,scale);
        // bounds are stored in millimeters  
        writeRealBinary(dos,bounds.xmin/MM);
        writeRealBinary(dos,bounds.xmax/MM);
        writeRealBinary(dos,bounds.ymin/MM);
        writeRealBinary(dos,bounds.ymax/MM);
        writeRealBinary(dos,bounds.zmin/MM);
        writeRealBinary(dos,bounds.zmax/MM);
    }


    public void addLayer(SliceLayer layer) throws IOException {
        startLayer(layer.getLayerHeight());

        PolyLine[] lines = layer.getPolyLines();

        for(PolyLine line : lines) {
            addPolyLine(line);
        }
        Hatches[] hatches = layer.getHatches();

        for(Hatches hatch : hatches) {
            addHatches(hatch);
        }
        endLayer();
    }

    public void startLayer(double height) throws IOException {

        dos.write(CMD_START_LAYER);

        int pos = (int)((height * conv));
        //printf("pos:%d\n", pos);
        writeUnsignedIntegerBinary(dos,pos);

        dos.writeByte(102);
        dos.writeByte(230);
        dos.writeByte(200);
        dos.writeByte(67);
        dos.writeByte(102);
        dos.writeByte(230);
        dos.writeByte(200);
        dos.writeByte(67);
        //writeUnsignedIntegerBinary(dos,1137239654);  // Only ever seen this value in a file
        //writeUnsignedIntegerBinary(dos,1137239654);
        dos.write(0);  // Padding
    }

    public void addPolyLine(PolyLine pl) throws IOException {
        double[] points = pl.getPoints();
        dos.writeByte(CMD_START_POLYLINE);  // ascii: etx
        dos.writeByte(pl.getDir() & 0xFF);
        writeUnsignedIntegerBinary(dos,points.length / 2);
        for(int i=0; i < points.length; i++) {
            double pnt = points[i];
            int fp = (int) (points[i] * conv);
            if((fp & 0xFFFF0000) != 0 ) {
                // only can write 2 byte unsigned int
                throw new RuntimeException(fmt("unsined short int overflow in SLISliceWriter:%d \n", fp));
            }
            //printf("%d ", fp);
            writeUnsignedIntegerBinary(dos,fp);  // TODO: round?
        }
        //printf("\n");
    }

    public void addHatches(Hatches h) throws IOException {
        /*
        double[] points = h.getCoords();
        writeUnsignedIntegerBinary(dos,CLIScanner.CMD_START_HATCHES_LONG);
        writeLongBinary(dos,h.getId());
        writeLongBinary(dos,points.length / 4);
        for(int i=0; i < points.length; i++) {
            writeRealBinary(dos,points[i] * conv);
        }
         */
    }

    private void writeIndex(SliceLayer[] layers) throws IOException {
        if(DEBUG)printf("Writing index: \n");
        int idx = 0;
        int slicePos = 128;  // TODO: THis is dodgy
        for(SliceLayer layer : layers) {
            // pos as 2 bytes, loc as 4 bytes
            int fp = (int) (layer.getLayerHeight() * conv) ; 

            writeUnsignedIntegerBinary(dos,fp);

//            int pos = 192 + idx*6;
//            int pos = 175 + idx*6;
            writeLongBinary(dos,slicePos);
            if(DEBUG)printf("layer: %d pos: %d\n",idx,slicePos);
            slicePos += calcSize(layer);

            idx++;
        }
    }

    /*
                fileIndexPos += 12;  // 12 bytes per layer
            for(PolyLine line : layer.getPolyLines()) {
                fileIndexPos += 3 + line.getPoints().length * 2;
            }

     */

    private int calcSizeOld(SliceLayer layer) {
        int size = 12;  // cmd
        PolyLine[] lines = layer.getPolyLines();
        for(int i=0; i < lines.length; i++) {
            size += 4;  // Polyline start
            size += lines[i].getPoints().length * 2;
        }

        return size;
    }

    private int calcSize(SliceLayer layer) {
        int size = 13 + layer.getPolyLineCount() * 4 + layer.getTotalPointCount() * 2;

        return size;
    }


    public void endLayer() throws IOException {
        dos.write(CMD_END_LAYER);
    }

    public void close() {
        // TODO: Need to write the index
        try {
            dos.close();
            if (los != null) los.close();
            os.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }


    /**
       helper function to write slicer result into a file
     */
    public static void writeSLISlices(String outPath, TriangleMeshSlicer slicer) throws IOException {

        writeSLISlices(outPath, slicer, false);

    }

    public static void writeSLISlices(String outPath, TriangleMeshSlicer slicer, boolean writeOpenContours) throws IOException {
        
        if(DEBUG) printf("SLISliceWriter.writeSLISlices(%s)\n",outPath);

        SLISliceWriter writer = new SLISliceWriter(outPath);

        int dir = 0;
        int id = 1;
        SliceLayer layers[] = new SliceLayer[slicer.getSliceCount()];

        double a = Double.MAX_VALUE;
        Bounds bounds = new Bounds(a,-a,a,-a, a,-a);

        for(int i = 0; i < slicer.getSliceCount(); i++){
                        
            Slice slice = slicer.getSlice(i);
            
            int ccount;
            if(writeOpenContours) 
                ccount= slice.getOpenContourCount();
            else
                ccount= slice.getClosedContourCount(); 

            double z = slice.getSliceDistance();  
            SliceLayer layer = new SliceLayer(z);
            Bounds layerBounds = new Bounds(a,-a,a,-a,a,-a);

            layerBounds.zmin = z;
            layerBounds.zmax = z;
            
            for(int k = 0; k < ccount; k++){
                
                double pnt[];
                if(writeOpenContours){ 
                    pnt = slice.getOpenContourPoints(k);
                } else {
                    pnt = slice.getClosedContourPoints(k);
                }

                for(int m = 0; m < pnt.length; m += 2){
                    layerBounds.xmin = min(layerBounds.xmin, pnt[m]);
                    layerBounds.ymin = min(layerBounds.ymin, pnt[m+1]);
                    layerBounds.xmax = max(layerBounds.xmax, pnt[m]);
                    layerBounds.ymax = max(layerBounds.ymax, pnt[m+1]);
                }

                PolyLine line = new PolyLine(id, dir, pnt);  
                layer.addPolyLine(line);
            }

            bounds.combine(layerBounds);
            
            layers[i] = layer;
            
        }
        if(DEBUG)printf("SLI bounds: %s mm\n", bounds.toString(MM));
        writer.write(bounds, SLISliceWriter.DEFAULT_CREATOR, layers);

         if(DEBUG) printf("SLISliceWriter.writeSLISlices(%s) done\n",outPath);
               
    }


} // class SLISliceWriter 



class LoggingOutputStream extends OutputStream  {
    private OutputStream os;
    private int cnt = 0;

    public LoggingOutputStream(OutputStream os) {
        this.os = os;
    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
        cnt++;
    }

    public void write(byte[] b, int off, int length) throws IOException {
        cnt += length;
        os.write(b,off,length);
    }

    public void write(byte[] b) throws IOException {
        cnt += b.length;
        os.write(b);
    }


    @Override
    public void close() throws IOException {
        os.close();
    }

    public void flush() throws IOException {
        os.flush();
    }

    public int getCount() {
        return cnt;
    }




}