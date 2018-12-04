/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
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

// External Imports

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.FileInputStream;
import java.io.File;
import java.util.Random;

import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ColorSpace;

import static abfab3d.core.Output.printf;
import static abfab3d.core.Output.fmt;
import static abfab3d.core.MathUtil.lerp;


/**


 */
public class TestICCProfile extends TestCase {

    static final boolean DEBUG = false;

    
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestICCProfile.class);
    }


    public void testNothing(){

    }


    public void devTestReadICC() throws Exception {

        ICC_Profile profile = ICC_Profile.getInstance(new FileInputStream("../../../PolyJet/StratasysJ750Vivid1_0.icc"));
        ICC_Profile rgbProfile = ICC_Profile.getInstance(new FileInputStream("../../../PolyJet/doc/sRGB Color Space Profile.icm"));
        
        //printf("profile: %s\n", profile);
        printProfile(profile);

        printf("ColorSpaceType: %d (%s)\n", profile.getColorSpaceType(),getTypeName(profile.getColorSpaceType()));
        printf("PSCType: %d (%s)\n", profile.getPCSType(),getTypeName(profile.getPCSType()));

        ICC_ColorSpace space = new ICC_ColorSpace(profile);
        ICC_ColorSpace rgbSpace = new ICC_ColorSpace(rgbProfile);

        printf("color space: %s\n", space);
        int numComponents = space.getNumComponents();
        printf("NumComponents: %d\n", space.getNumComponents());
        printf("type: %d (%s)\n", space.getType(), getTypeName(space.getType()));

        for(int i = 0; i < numComponents; i++){
            printf("component:%d name: %10s (%7.3f %7.3f)\n", i, space.getName(i), space.getMinValue(i),space.getMaxValue(i));            
        }
        

        //float colors[][] = new float[][]{{0,0,0,0},{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}, {1,1,0,0},{1,0,1,0},{0,1,1,0},};        
        //float colors[][] = new float[][]{{0,0,0,0},{0,0.2f,0.2f,0},{0,0.4f,0.4f,0},{0,0.6f,0.6f,0},{0,0.8f,0.8f,0},{0,1f,1f,0}};        
        //float colors[][] = makeColorTable(new double[]{0,0,0,0}, new double[]{1,0,1,0},10);
        //float colors[][] = makeColorTable(new double[]{0,1,1,0.7}, new double[]{0,0,0,0},10);
        //float colors[][] = makeColorTable(new double[]{1,0,0,0}, new double[]{0.0,1,1,0},10);
        float colors[][] = makeColorTable(new double[]{0,0,0,0}, new double[]{0,0,0,1},10);
        //printToRGB(space, rgbSpace, colors);
        //printFromRGB(space, colors);
        printToXYZ(space, rgbSpace, colors);
        //printFromXYZ(space, colors);
        
    }

    static void devTestFindRGBColor() throws Exception{

        ICC_Profile polyProfile = ICC_Profile.getInstance(new FileInputStream("../../../PolyJet/StratasysJ750Vivid1_0.icc"));
        ICC_Profile rgbProfile = ICC_Profile.getInstance(new FileInputStream("../../../PolyJet/doc/sRGB Color Space Profile.icm"));
        ICC_ColorSpace polySpace = new ICC_ColorSpace(polyProfile);
        ICC_ColorSpace rgbSpace = new ICC_ColorSpace(rgbProfile);
                
        //float color[] = toFloat(new double[]{0,1,0});        
        //float color[] = toFloat(new double[]{0,0,1});
        float color[] = toFloat(new double[]{0.4,0.4,0.5});

        //float color[] = toFloat(new double[]{0.000,0.322,0.636}); best blue toFloat(new double[]{(0.872,0.082,0.000,0.015}); 
        //float color[] = toFloat(new double[]{0.000,0.647,0.221}); best green toFloat(new double[]{( 0.136,0.000,0.413,0.002}); 
        int N = 1000000;
        float minMat[] = null;
        float minRGB[] = null;
        Random rnd = new Random(100);
        double minDist = 10;
        for(int i = 0; i < N; i++){
            float mat[] = new float[]{rnd.nextFloat(),rnd.nextFloat(),rnd.nextFloat(), rnd.nextFloat()};
            
            float xyz[] = polySpace.toCIEXYZ(mat);            
            float rgb[] = rgbSpace.fromCIEXYZ(xyz);    
            double dist = distance(rgb, color);
            if(dist < minDist){
                minDist = dist;
                minMat = mat;
                minRGB = rgb;
            }
            if(dist < 0.07) 
                printf(" material: %s, bestColor: %s dist: %7.3f\n", colorToString(mat), colorToString(rgb), dist);
        }
        printf("color: %s bestMaterial: %s, bestColor: %s dist: %7.3f\n", colorToString(color), colorToString(minMat), colorToString(minRGB), minDist);
            
    }

    static double distance(float c1[], float c2[]){
        
        double d = 0;
        for(int i = 0; i < c2.length; i++){
            double dd = c1[i]-c2[i];
            d += dd*dd;
        }
        return Math.sqrt(d);
    }
    
    float[][] makeColorTable(double c1[], double c2[], int count){

        float cc[][] = new float[count+1][4];
        for(int i = 0; i <= count; i++){
            cc[i] = toFloat(interpolate(c1, c2, (double)i/count));
        }
        return cc;
    }

    double[] interpolate(double c1[], double c2[], double x){        
        double cc[] = new double[c1.length];
        lerp(c1, c2, x, cc);
        return cc;
    }

    static float[] toFloat(double c[]){
        float cc[] = new float[c.length];
        for(int i = 0; i < cc.length; i++){
            cc[i] = (float)c[i];
        }
        return cc;
    }

    static void printToRGB(ICC_ColorSpace space,float colors[][]){
        printf("toRGB()\n");
        for(int i = 0; i < colors.length; i++){
            float c[] = space.toRGB(colors[i]);
            printf(" %s -> %s \n", colorToString(colors[i]), colorToString(c));
        }
    }
    static void printFromRGB(ICC_ColorSpace space, float colors[][]){
        printf("fromRGB()\n");
        for(int i = 0; i < colors.length; i++){
            float c[] = space.fromRGB(colors[i]);            
            printf(" %s -> %s \n", colorToString(colors[i]), colorToString(c));
        }
    }

    static void printToXYZ(ICC_ColorSpace space, ICC_ColorSpace rgbSpace, float colors[][]){
        printf("toXYZ()\n");
        for(int i = 0; i < colors.length; i++){
            float xyz[] = space.toCIEXYZ(colors[i]);            
            float rgb[] = rgbSpace.fromCIEXYZ(xyz);            
            printf("mat: %s xyz:%s rgb:%s \n", colorToString(colors[i]), colorToString(xyz),colorToString(rgb));
        }
    }

    static void printFromXYZ(ICC_ColorSpace space, float colors[][]){
        printf("fromXYZ()\n");
        for(int i = 0; i < colors.length; i++){
            float c[] = space.fromCIEXYZ(colors[i]);            
            printf(" %s -> %s \n", colorToString(colors[i]), colorToString(c));
        }
    }

    static String colorToString(float c[]){
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        for(int i = 0; i < c.length; i++){
            sb.append(fmt("%6.4f", c[i]));
            if( i+1 < c.length )
                sb.append(",");
        }    
        sb.append("}");
        return sb.toString();
    }
    
    static void printProfile(ICC_Profile profile){

        //printf("profile: %s\n", profile);
        byte data[] = profile.getData();
        printf("profile: %s\n", profile);
        printTagTable(data);
        
    }
    
    static void printTagTable(byte data[]){

        int offset = 128; // tag table offset 
        int tagCount = getInt(data, offset);
        printf("tagCount: %d\n", tagCount);
        offset += 4;
        for(int i = 0; i < tagCount; i++){
            String tagSig = getTagSig(data,offset);
            int tagOffset = getInt(data, offset+4);
            int tagSize = getInt(data, offset+8);
            printf("tag:%s(%8x) offset: %8d size: %8d\n", tagSig,getInt(data,offset),tagOffset,tagSize);
            if(tagSig.equals("A2B0") || tagSig.equals("A2B1") || tagSig.equals("A2B2") ||
               tagSig.equals("B2A0") || tagSig.equals("B2A1") || tagSig.equals("B2A2") ){
                //printA2B(data, tagOffset);
            }
            offset += 12; // tag table entry size 
        }
    }

    static void printA2B(byte data[], int offset){
        printf("A2B:");
        String sig = getTagSig(data,offset);
        int inChannels = data[offset+8];
        int outChannels = data[offset+9];
        int oBcurve = getInt(data, offset+12);
        int oMatrix = getInt(data, offset+16);
        int oMcurve = getInt(data, offset+20);
        int oCLUT = getInt(data, offset+24);
        int oAcurve = getInt(data, offset+28);

        printf("%s: in:%d out:%d Bcurve:%d Matrix:%d Mcurve:%d, CLUT:%d, Acurve:%d\n", sig, inChannels, outChannels, oBcurve, oMatrix, oMcurve, oCLUT, oAcurve);
        if(oAcurve != 0) 
            printGenCurve("A-curve", data, offset + oAcurve, inChannels);
        if(oBcurve != 0) 
            printGenCurve("B-curve", data, offset + oBcurve, outChannels);
        if(oCLUT != 0) 
            printCLUT(data, offset + oCLUT, inChannels, outChannels);            
    }

    static void printCLUT(byte data[], int offset, int inDim, int outDim){
        printf("CLUT:\n");
        printf("grid: ");
        int dim[] = new int[inDim];
        int pnt[] = new int[inDim]; // point in the center 

        int clutSize = 1; // size of CLUT data (to be calculated) 
        for(int i = 0; i < inDim; i++){
            dim[i] = data[offset+i];
            pnt[i] = 0;//(dim[i]-1)/2; // point in the center 
            clutSize *= dim[i];
            printf(" %d", dim[i]);
        }
        int dataPrecision = data[offset + 16];
        printf("  dataPrecision: %d\n", dataPrecision);
        clutSize *= outDim*dataPrecision;

        printf("  CLUTsize: %d\n", clutSize);

        int index = pnt[0];
        for(int i = 0; i < inDim-1; i++){
            index = index * dim[i] + pnt[i+1];
        }
        int dataStart = offset + 20;
        int firstPointOffset = index * outDim * dataPrecision;

        printf("pnt index: %d\n", index);
        double entry[] = new double[outDim];
        
        int x = 0;
        int y = 0;
        
        for(int z = 0; z < dim[inDim-2]; z++){
            for(int w = 0; w < dim[inDim-1]; w++){
                int pointOffset = w*outDim*dataPrecision + z *dim[inDim-1]*outDim*dataPrecision;
                getCLUTEntry(data, dataStart + firstPointOffset + pointOffset, dataPrecision, entry);
                printf("%s", entryToString(entry)); 
            } 
            printf("\n");
        }
        printf("\n");
    }

    static String entryToString(double entry[]){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < entry.length; i++){
            sb.append(fmt("%2d",(int)(entry[i]*99.9)));
            if(i < entry.length-1) sb.append(" ");
            else sb.append(",");
        }              
        return sb.toString();
    }

    static void getCLUTEntry(byte data[], int offset, int dataPrecision, double entry[]){

        for(int i = 0; i < entry.length; i++){            
            entry[i] = (double)getUInt16(data, (offset + i*dataPrecision))*(1./0xFFFF);
        }
    }

    static void printCurv(byte data[], int offset, int curveCount){

        for(int k = 0; k < curveCount; k++){
            printf("curv:%d\n",k); 
            int pointsCount = getInt(data, offset+8);
            printf("  pointsCount:%d\n",pointsCount);         
            int n = Math.min(1000, pointsCount/2);
            for(int i = 0; i < pointsCount+n-1; i+=n){
                int j = Math.min(i, pointsCount-1);
                printf("%5.3f %5.3f\n",(double)i/(pointsCount-1), getUInt16(data, offset + 12 + 2*j)*(1./0xFFFF));
            }
            offset += 12 + pointsCount*2;
        }
    }

    static void printGenCurve(String name, byte data[], int offset, int curveCount){
        printf("%s:\n", name);
        String sig = getTagSig(data,offset);
        if(sig.equals("curv")){
            printCurv(data, offset, curveCount);
        }
        printf("\n");
    }

    static int ubyte(byte x){
        return ((int)x) & 0xFF;
    }

    static String getTagSig(byte data[], int offset){
        byte s[] = new byte[]{data[offset],data[offset+1],data[offset+2], data[offset+3]};
        return new String(s);
    }

    static int getUInt16(byte data[], int offset){
        return (ubyte(data[offset]) << 8) | (ubyte(data[offset+1]));        
    }
    static int getInt(byte data[], int offset){
        return (ubyte(data[offset]) << 24) | (ubyte(data[offset+1]) << 16) | (ubyte(data[offset+2]) << 8) | (ubyte(data[offset+3]));
    }

    String printArray(byte wpt[]){
        
        StringBuffer buf = new StringBuffer();

        for(int i = 0; i < wpt.length; i++){
            buf.append(fmt("%2x ",wpt[i]));
        }
        return buf.toString();
    }

    String getTypeName(int type){
        switch(type){
        case ColorSpace.CS_CIEXYZ: return "CS_CIEXYZ";
        case ColorSpace.CS_GRAY: return "CS_GRAY";
        case ColorSpace.CS_LINEAR_RGB: return "CS_LINEAR_RGB";
        case ColorSpace.CS_PYCC: return "CS_PYCC";
        case ColorSpace.CS_sRGB: return "CS_sRGB";
        case ColorSpace.TYPE_2CLR: return "TYPE_2CLR";
        case ColorSpace.TYPE_3CLR: return "TYPE_3CLR";
        case ColorSpace.TYPE_4CLR: return "TYPE_4CLR";
        case ColorSpace.TYPE_5CLR: return "TYPE_5CLR";
        case ColorSpace.TYPE_6CLR: return "TYPE_6CLR";
        case ColorSpace.TYPE_7CLR: return "TYPE_7CLR";
        case ColorSpace.TYPE_8CLR: return "TYPE_8CLR";
        case ColorSpace.TYPE_9CLR: return "TYPE_9CLR";
        case ColorSpace.TYPE_ACLR: return "TYPE_ACLR";
        case ColorSpace.TYPE_BCLR: return "TYPE_BCLR";
        case ColorSpace.TYPE_CCLR: return "TYPE_CCLR";
        case ColorSpace.TYPE_CMY: return "TYPE_CMY";
        case ColorSpace.TYPE_CMYK: return "TYPE_CMYK";
        case ColorSpace.TYPE_DCLR: return "TYPE_DCLR";
        case ColorSpace.TYPE_ECLR: return "TYPE_ECLR";
        case ColorSpace.TYPE_FCLR: return "TYPE_FCLR";
        case ColorSpace.TYPE_GRAY: return "TYPE_GRAY";
        case ColorSpace.TYPE_HLS: return "TYPE_HLS";
        case ColorSpace.TYPE_HSV: return "TYPE_HSV";
        case ColorSpace.TYPE_Lab: return "TYPE_Lab";
        case ColorSpace.TYPE_Luv: return "TYPE_Luv";
        case ColorSpace.TYPE_RGB: return "TYPE_Luv";
        case ColorSpace.TYPE_XYZ: return "TYPE_XYZ";
        case ColorSpace.TYPE_YCbCr: return "TYPE_YCbCr";
        case ColorSpace.TYPE_Yxy: return "TYPE_Yxy";
        default: return "TYPE_UNKNOWN";
        }
    }
    
    
    public void devTestParseICCProfile() throws Exception{
        
        String iccPath = "../../../PolyJet/StratasysJ750Vivid1_0.icc";
        File f = new File(iccPath);
        long len = f.length();
        
        byte data[] = new byte[(int)len];
        FileInputStream in = new FileInputStream(iccPath);
        in.read(data);
        ICCProfile profile = new ICCProfile(data);
        
        double mat0[] = new double[]{0,0,0,0};
        double mat1[] = new double[]{0,1,0,0};

        double pcs[] = new double[3];
        double mm[] = new double[4];

        int N = 11;
        for(int i = 0; i < N; i++){
            double mat[] = interpolate(mat0, mat1, (double)i/(N-1));
            profile.convertA2B1(mat, pcs);            
            profile.convertB2A1(pcs, mm);            

            printf("mat: {%s} -> pcs: {%s} -> mat: {%s} \n", getString(mat), getString(pcs), getString(mm));
        }

        //profile.printA2B1Curve();
    }

    static String getString(double entry[]){
        
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < entry.length; i++){
            sb.append(fmt("%4.2f",entry[i]));
            if(i < entry.length-1) sb.append(",");
        }              
        return sb.toString();
    }
    
    void devTestCurve(){
        int N = 200;

        printf("ICCProfile.getCurve_sRGB(v)\n");
        for(int i = 0; i <= N; i++){
            double v = (double)i/N;
            double V = ICCProfile.getCurve_sRGB(v);
            double Vs = ICCProfile.getCurve_sRGBsimple(v);
            printf("%7.4f, %7.4f, %7.4f \n", v, V, Vs);
        }
    }


    public static void main(String arg[]) throws Exception {
        
        //new TestICCProfile().devTestReadICC();
        new TestICCProfile().devTestParseICCProfile();
        //new TestICCProfile().devTestFindRGBColor();
        //new TestICCProfile().devTestCurve();
        
    }
}
