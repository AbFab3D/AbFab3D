/*
 * ***************************************************************************
 *                   Shapeways, Inc Copyright (c) 2017
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

package abfab3d.util;

import java.util.Vector;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.MathUtil.clamp;
import static abfab3d.core.MathUtil.iround;

import static java.lang.Math.*;

/**
 * representation of ICC profile
 *
 * @author Vladimir Bulatov
 */
public class ICCProfile {

    static final boolean DEBUG_PARSING = true;

    static final double UINT16toDOUBLE = (1./0xFFFF);
    static final double INTtoS15FIXED16 = (1./(1<<16));
    static final double SCALE_FACTORS[] = new double[]{(1./0xFF),(1./0xFFFF),(1./0xFFFFFF),(1./0xFFFFFFFFL),};

    /**
       constructor from raw byte array 
     */
    public ICCProfile(byte data[]){
        
        parse(data);
        
    }



    A2B
        m_A2B0,  // Device->PCS  peceptual 
        m_A2B1,  // Device->PCS  colorimetric
        m_A2B2,  // Device->PCS  saturation 
        m_B2A0,  // PCS-> Device peceptual 
        m_B2A1,  // PCS-> Device colorimetric 
        m_B2A2;  // PCS-> Device saturation 

    XYZType m_wtpt;
    XYZType m_bkpt;
    DoubleArray m_chad;

    void parse(byte data[]){
        
        parseHeader(data);
        parseTagTable(data);
    }

    void parseHeader(byte data[]){

        if(DEBUG_PARSING) printf("ICCProfile.parseHeader(%s, %d)\n", data, data.length);

        int profileSize = getInt(data, 0);  // shall be equal to data.length;
        if(DEBUG_PARSING) printf("profileSize: %d\n", profileSize); 
        if(profileSize != data.length) 
            throw new RuntimeException(fmt("profileSize:%d != data.length",profileSize, data.length));        
        int CMMtype =  getInt(data, 4); 
        if(DEBUG_PARSING) printf("CMMtype: %d\n", CMMtype); 
        int version = getInt(data, 8); 
        if(DEBUG_PARSING) printf("version: %X\n", version); 
        String deviceClass = getSignature(data, 12); 
        if(DEBUG_PARSING) printf("deviceClass: %s\n", deviceClass); 
        String colorSpace = getSignature(data, 16); 
        if(DEBUG_PARSING) printf("colorSpace: %s\n", colorSpace); 
        String PCS =  getSignature(data, 20); 
        if(DEBUG_PARSING) printf("PCS: %s\n", PCS); 
        DateTime dt = getDataTime(data, 24);
        if(DEBUG_PARSING) printf("date: %s\n", dt); 
        String profileSig =  getSignature(data, 36);  // has to be "acsp"
        if(DEBUG_PARSING) printf("profleSignature: `%s`\n", profileSig); 
        if(!profileSig.equals("acsp")) 
            throw new RuntimeException(fmt("wrong profile signature:%s is different from `acsp`", profileSig));
        String platformSignature =  getSignature(data, 40);  // has to be "acsp"
        if(DEBUG_PARSING) printf("platformSignature: `%s`\n", platformSignature); 
        int flags = getInt(data, 44); 
        if(DEBUG_PARSING) printf("profileFlags: 0x%X\n", flags); 
        String manufacturer = getSignature(data, 48); 
        if(DEBUG_PARSING) printf("manufacturer: %s\n", manufacturer); 
        //int device = getInt(data,52); 
        String device = getSignature(data,52); 
        //if(DEBUG_PARSING) printf("device: 0x%X\n", device); 
        if(DEBUG_PARSING) printf("device: %s\n", device); 
        long deviceAttributes = getLong(data,56); 
        if(DEBUG_PARSING) printf("deviceAttributes: 0x%X\n", deviceAttributes); 
        int renderingIntent = getInt(data,64); 
        if(DEBUG_PARSING) printf("renderngIntent: 0x%X\n", renderingIntent); 
        XYZNumber illuminant = getXYZNumber(data,68); 
        if(DEBUG_PARSING) printf("illuminant: %s\n", illuminant);                
        
    }

    void parseTagTable(byte data[]){
        
        int offset = 128; // tag table offset 

        int tagCount = getInt(data, offset);
        if(DEBUG_PARSING)printf("tagCount: %d\n", tagCount);
        offset += 4;

        for(int i = 0; i < tagCount; i++){
            String tagSig = getSignature(data,offset);
            int tagOffset = getInt(data, offset+4);
            int tagSize = getInt(data, offset+8);
            if(DEBUG_PARSING)printf("tag:%s(%8x) offset: %8d size: %8d\n", tagSig,getInt(data,offset),tagOffset,tagSize);

            if(tagSig.equals("A2B0")){
                m_A2B0 = new A2B("A2B0",data, tagOffset);
            }else if(tagSig.equals("A2B1")){
                m_A2B1 = new A2B("A2B1", data, tagOffset);
            }else if(tagSig.equals("A2B2")){
                m_A2B2 = new A2B("A2B2", data, tagOffset);
            }else if(tagSig.equals("B2A0")){
                m_B2A0 = new A2B("B2A0", data, tagOffset);
            }else if(tagSig.equals("B2A1")){
                m_B2A1 = new A2B("B2A1",data, tagOffset);
            }else if(tagSig.equals("B2A2")){
                m_B2A2 = new A2B("B2A2",data, tagOffset);
            }else if(tagSig.equals("wtpt")){
                m_wtpt = getXYZType(data, tagOffset, tagSize, "wtpt");
                if(DEBUG_PARSING)printf("%s\n", m_wtpt);
            } else if(tagSig.equals("bkpt")){
                m_bkpt = getXYZType(data, tagOffset, tagSize, "bkpt");   
                if(DEBUG_PARSING)printf("%s\n", m_bkpt);
            } else if(tagSig.equals("chad")){
                // chromaticAdaptationTag (9.2.15)  
                m_chad = new DoubleArray("chad", getS15Fixed16Array(data, tagOffset, tagSize));   
                if(DEBUG_PARSING)printf("%s\n", m_chad);
            }else if(tagSig.equals("cprt")){
                // copyright 
            }else if(tagSig.equals("desc")){
                // profileDescriptionTag 
            }else if(tagSig.equals("gamt")){
                // gamutTag (9.2.28) 
                // permitted types : lut8Type or lut16Type or lutBToAType  
                //m_gamut = 
            }else if(tagSig.equals("view")){
                // viewingConditionTag 
                
            }else if(tagSig.equals("Yoav")){
                // ? 
            }

            
            offset += 12; // tag table entry size 
        }
        
    }
    
    public void convertA2B1(double in[], double out[]){
        m_A2B1.convert(in, out);
    }

    public void convertB2A1(double in[], double out[]){
        m_B2A1.convert(in, out);
    }


    public void printA2B1Curve(){

        m_A2B1.Acurve.print();

    }

    A2B parseA2B(byte data[], int offset){
        return null;
    }

    /**
       represent color conversion 
     */
    public static class A2B {

        String name;
        int inChannels;
        int outChannels;
        
        Curve Acurve;
        Curve Bcurve;
        CLUT clut;
        String sig; // mBA or mAB 
        public A2B(String name, byte data[], int offset){

            if(DEBUG_PARSING)printf("A2B(%s)\n", name);
            sig = getSignature(data,offset);
            inChannels = data[offset+8];
            outChannels = data[offset+9];

            int oBcurve = getInt(data, offset+12);
            int oMatrix = getInt(data, offset+16);
            int oMcurve = getInt(data, offset+20);
            int oCLUT = getInt(data, offset+24);
            int oAcurve = getInt(data, offset+28);
            
            if(DEBUG_PARSING)printf("%s: in:%d out:%d Bcurve:%d Matrix:%d Mcurve:%d, CLUT:%d, Acurve:%d\n", 
                                    sig, inChannels, outChannels, oBcurve, oMatrix, oMcurve, oCLUT, oAcurve);
            
            if(oAcurve != 0) 
                Acurve = new Curve("Acurve", data, offset + oAcurve, inChannels);
            if(oBcurve != 0) 
                Bcurve = new Curve("Bcurve",data, offset + oBcurve, outChannels);
            if(oCLUT != 0) 
                clut = new CLUT(data, offset + oCLUT, inChannels, outChannels);                        
            
        }

        void convert(double in[], double out[]){
            
            // support for now only A-curve -> CLUT -> B-curve 
            clut.getValueLinear(in, out);
            //clut.getValueBox(in, out);

        }

    } // class A2B 

    /**
       represents curve 
     */
    public static class Curve {

        int channels;
        double curves[][];
        
        public Curve(String name, byte data[], int offset, int channels){
            
            if(DEBUG_PARSING)printf("Curve(%s)\n", name);
            this.channels = channels;
            curves = new double[channels][];
            
            int curveHeaderSize = 12;
            int curveOffset = offset;
            for(int k = 0; k < channels; k++){
                if(DEBUG_PARSING)printf("curve:%d ",k); 
                int pointsCount = getInt(data, offset+8);
                curves[k] = new double[pointsCount];
                if(DEBUG_PARSING)printf("cnt:%4d", pointsCount); 

                if(DEBUG_PARSING)printf("  pnts:"); 
                for(int i = 0; i < pointsCount; i++){
                    curves[k][i] = getUInt16(data, offset + curveHeaderSize + 2*i)*UINT16toDOUBLE;
                    if(i < 10) if(DEBUG_PARSING)printf(" %6.4f", curves[k][i]);
                    //intf("%5.3f %5.3f\n",(double)j/(pointsCount-1), getUInt16(data, offset + 12 + 2*j)/65533.);
                }
                if(DEBUG_PARSING)printf("\n");
                curveOffset += (curveHeaderSize + pointsCount*2);
            }            
        }

        void print(){
            int k = 0; 
            int skip = 100;
            printf("x\n");            
            for(int i = 0; i < curves[k].length; i += skip){
                printf("%7.5f\n", (double)i/curves[k].length);
            }
            printf("y\n");            
            for(int i = 0; i < curves[k].length; i += skip){
                printf("%7.5f\n", curves[k][i]);
            }
        }
    }

    /**
       represent CLUT (Color LookUp Table)
       it is inDim-dimesional array stored in one dimensional array 
       each entry has outDim values, thy eare stored sequentionally 

       
     */
    public static class CLUT {

        int inDim;
        int outDim;
        int dims[];
        double values[]; 

        public CLUT(byte data[], int offset, int inDim, int outDim){

            this.inDim = inDim;
            this.outDim = outDim;

            if(DEBUG_PARSING)printf("CLUT:\n");
            if(DEBUG_PARSING)printf("grid: ");
            dims = new int[inDim];
            
            int size = 1; // size of CLUT data (to be calculated) 
            for(int i = 0; i < inDim; i++){
                dims[i] = data[offset+i];
                size *= dims[i];
                if(DEBUG_PARSING)printf(" %d", dims[i]);
            }
            int dataPrecision = data[offset + 16];
            double scaleFactor = SCALE_FACTORS[dataPrecision-1];
            if(DEBUG_PARSING)printf("  dataPrecision: %d\n", dataPrecision);
            int dataCount = size *outDim;

            int clutSize = dataCount*dataPrecision + 20;

            if(DEBUG_PARSING)printf("  dataCount: %d CLUTsize: %d\n", dataCount, clutSize);

            values = new double[dataCount];

            int dataStart = offset + 20;
            for(int i = 0; i < dataCount; i++){
                values[i] = scaleFactor*getUInt16(data, (dataStart + i*dataPrecision));
            }

            //printLayer();

        }

        /**
           
           @return output values for given coord 
           
        */
        void getGridValue(int pnt[], double out[]){
            
            int index = pnt[0]; // index of first data entry 
            for(int d = 1; d < inDim; d++){
                index = index * dims[d] + pnt[d];
            }
            index *= outDim; // outDim data entries per point 
            for(int i = 0; i < outDim; i++){
                out[i] = values[index+i];
            }
        }

        static final double EPS = 1.e-6;

        void getValueBox(double in[], double out[]){

            int pnt[] = new int[inDim];
            for(int d = 0; d < inDim; d++){
                pnt[d] = iround((dims[d]-1)*clamp(in[d], 0, 1-EPS));                
            }
            getGridValue(pnt, out);
            
        }


        //
        //   
        // 
        void getValueLinear(double in[], double out[]){

            int pnt[] = new int[inDim];
            double x[] = new double[inDim]; // normalized point location in cell

            for(int d = 0; d < inDim; d++){
                double xx = (dims[d]-1)*clamp(in[d], 0, 1-EPS);
                pnt[d] = (int)floor(xx);                
                x[d] = xx - pnt[d];
            }

            int counter = 1 << inDim; //counter of all corners 
            int cp[] = new int[inDim]; //int coordinates of a corner points 
            double cv[] = new double[outDim]; // values at the corners 
            set(out, 0.);

            for(int c = 0; c < counter; c++){
                float factor = 1;
                for(int d = 0; d < inDim; d++){
                    int flag = ((c >> d)& 1); 
                    if(flag == 0){
                        factor *= (1-x[d]);
                        cp[d] = pnt[d];
                    } else {
                        factor *= x[d];
                        cp[d] = pnt[d]+1;
                    }
                }
                getGridValue(cp, cv);
                mulAdd(out, factor, cv);
            }           
        }

        //
        // set array values to value
        //
        static void set(double array[], double value){
            for(int d = 0; d < array.length; d++){
                array[d] = value;
            }
        }

        //
        // A[] += factor*B[];
        //
        static void mulAdd(double a[], double factor, double b[]){
            for(int d = 0; d < a.length; d++){
                a[d] += factor*b[d];
            }
        }
        
        void printLayer(){
            
            int pnt[] = new int[inDim];
            double out[] = new double[outDim];
            pnt[inDim-2] = 10;
            pnt[inDim-1] = dims[inDim-1];
            if(DEBUG_PARSING)printf("CLUT layer %s\n", getString(pnt));
            
            for(int i = 0; i < dims[0]; i++){
                for(int j = 0; j < min(10, dims[1]); j++){
                    pnt[0] = i;
                    pnt[1] = j;
                    getGridValue(pnt, out);
                    if(DEBUG_PARSING)printf("%s ", getString(out));
                }
                if(DEBUG_PARSING)printf("\n");
            }                
        }
        
        static String getString(String format, double entry[]){
            
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < entry.length; i++){
                sb.append(fmt(format,entry[i]));
                if(i < entry.length-1) sb.append(" ");
                else sb.append(",");
            }              
            return sb.toString();
        }

        static String getString(double entry[]){
            
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < entry.length; i++){
                sb.append(fmt("%2d",(int)(entry[i]*99.9)));
                if(i < entry.length-1) sb.append(" ");
                else sb.append(",");
            }              
            return sb.toString();
        }

        static String getString(int entry[]){
            
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < entry.length; i++){
                sb.append(fmt("%2d",entry[i]));
                if(i < entry.length-1) sb.append(" ");
                else sb.append(",");
            }              
            return sb.toString();
        }
        
        /**
           return dimensions of grid 
        */
        int[] getDimensions(){
            
            return dims;
            
        }
        
        
        static void getCLUTEntry(byte data[], int offset, int dataPrecision, double scale, double entry[]){
            
            for(int i = 0; i < entry.length; i++){            
                entry[i] = scale*getUInt16(data, (offset + i*dataPrecision));
            }
        }
        
    } // class CLUT 
    
    static String arrayToString(double entry[]){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < entry.length; i++){
            sb.append(fmt("%2d",(int)(entry[i]*99.999)));
            if(i < entry.length-1) sb.append(" ");
            else sb.append(",");
        }              
        return sb.toString();
    }

    static class DateTime {
        int year;
        int month;
        int day;
        int hour;
        int minute;
        int second;
        
        DateTime(int year, int month, int day, int hour, int minute, int second){

            this.year = year;
            this.month = month;
            this.day = day;
            this.hour = hour;
            this.minute = minute;
            this.second = second;

        }

        public String toString(){
            return fmt("%d/%d/%d  %dh:%dm:%ds",year, month, day, hour, minute, second);
        }
        
    } // class DateTime 

    static class XYZNumber {
        double x, y,z;
        XYZNumber(double x, double y, double z){
            this.x = x;
            this.y = y;
            this.z = z;            
        }
        public String toString(){
            return fmt("{%7.5f,%7.5f,%7.5f}", x,y,z);
        }
    }

    // represent variable set of XYZ 
    static class XYZType {

        Vector<XYZNumber> entries;
        String name;
        XYZType(String name, Vector<XYZNumber> entries){
            this.name = name;
            this.entries = entries;
        }

        public String toString(){

            StringBuffer sb = new StringBuffer();
            sb.append(name);
            sb.append(":");
            for(int i = 0; i < entries.size(); i++)
                sb.append(entries.get(i).toString());
            return sb.toString();
        }        
    } // class XYZType

    static int ubyte(byte x){
        return ((int)x) & 0xFF;
    }

    static long ulong(int x){
        return ((long)x) & 0xFFFFFFFF;
    }

    static String getSignature(byte data[], int offset){

        byte s[] = new byte[]{data[offset],data[offset+1],data[offset+2], data[offset+3]};
        return new String(s);

    }

    static int getUInt16(byte data[], int offset){
        return (ubyte(data[offset]) << 8) | (ubyte(data[offset+1]));        
    }
    static int getInt(byte data[], int offset){
        return (ubyte(data[offset]) << 24) | (ubyte(data[offset+1]) << 16) | (ubyte(data[offset+2]) << 8) | (ubyte(data[offset+3]));
    }

    static double getS15Fixed16(byte data[], int offset){
        return getInt(data, offset)*INTtoS15FIXED16;
        
    }
    
    //
    //   array of doubles
    // 
    static class DoubleArray {
        
        String name;
        double values[];
        DoubleArray(String name, double values[]){
            this.name = name;
            this.values = values;
        }
        public String toString(){

            StringBuffer sb = new StringBuffer();
            sb.append(name);
            sb.append(":[");
            for(int i = 0; i < values.length; i++){
                sb.append(fmt("%7.5f",values[i]));
                if(i < values.length-1){
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();            
        }
    }

    static double[] getS15Fixed16Array(byte data[], int offset, int size){

        int headerSize = 8;
        int entrySize = 4;
        int count = (size-headerSize)/entrySize;

        double values[] = new double[count];
        for(int i = 0; i < count; i++){
            values[i] = getS15Fixed16(data, headerSize + entrySize*i);
        }
        return values;
    }

    static long getLong(byte data[], int offset){
        return ulong(getInt(data, offset)) | (ulong(getInt(data, offset+4))<<32) ;
    }

    static DateTime getDataTime(byte data[], int offset){
        return new DateTime(getUInt16(data,offset),
                            getUInt16(data,offset+2),
                            getUInt16(data,offset+4),
                            getUInt16(data,offset+6),
                            getUInt16(data,offset+8),
                            getUInt16(data,offset+10));
    }

    static XYZNumber getXYZNumber(byte data[], int offset){
        return new XYZNumber(getS15Fixed16(data,offset),getS15Fixed16(data,offset+4),getS15Fixed16(data,offset+8));
    }

    // parses tag of XYZType 
    static XYZType getXYZType(byte data[], int offset, int size, String name){
        int tagHeaderSize = 8;
        int tagEntrySize = 12;
        int count = (size-tagHeaderSize)/tagEntrySize; // total count of encoded triplets 
        Vector<XYZNumber> entries = new Vector<XYZNumber>(count);
        for(int i = 0; i < count; i++){
            entries.add(getXYZNumber(data, offset + tagHeaderSize + i * tagEntrySize));
        }
        return new XYZType(name, entries);

    }

    //
    //  exact conversion from linear rgb to nonlinear RGB coordinates used by sRGB coilorspace 
    //  http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_RGB.html 
    public static double getCurve_sRGB(double v){
        if(v < 0.0031308)
            return 12.92*v;
        else 
            return 1.055*pow(v, 1./2.4)-0.055;
    }

    //
    //  simplified conversion from linear rgb to nonlinear RGB coordinates used by sRGB coilorspace 
    //  http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_RGB.html 
    public static double getCurve_sRGBsimple(double v){
        return pow(v, 1./2.2);
    }

        
}
